/*
 * Copyright 2025 the original author or authors.
 * <p>
 * Licensed under the Moderne Source Available License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://docs.moderne.io/licensing/moderne-source-available-license
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.mainframe.bms.internal;

import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jspecify.annotations.Nullable;
import org.openrewrite.FileAttributes;
import org.openrewrite.mainframe.bms.internal.grammar.BMSLexer;
import org.openrewrite.mainframe.bms.internal.grammar.BMSParser;
import org.openrewrite.mainframe.bms.internal.grammar.BMSParserBaseVisitor;
import org.openrewrite.mainframe.bms.marker.SequenceArea;
import org.openrewrite.mainframe.bms.tree.Bms;
import org.openrewrite.mainframe.bms.tree.Space;
import org.openrewrite.mainframe.bms.tree.Statement;
import org.openrewrite.marker.Markers;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.openrewrite.Tree.randomId;
import static org.openrewrite.mainframe.bms.tree.Space.EMPTY;

@RequiredArgsConstructor
public class BmsParserVisitor extends BMSParserBaseVisitor<Bms> {

    private final Path path;
    private final @Nullable FileAttributes fileAttributes;
    private final String source;
    private final Charset charset;
    private final boolean charsetBomMarked;

    /**
     * The line reader works out which lines open a statement, which continue one, and which carry a
     * name field. It is the only place that information survives, so grouping words back into
     * statements needs the token stream rather than just the parse tree.
     */
    private final BufferedTokenStream tokens;

    private int cursor = 0;

    public <T> T visit(@Nullable ParseTree... trees) {
        for (ParseTree tree : trees) {
            if (tree != null) {
                //noinspection unchecked
                return (T) visit(tree);
            }
        }
        throw new IllegalStateException("Expected one of the supplied trees to be non-null");
    }

    @Override
    public Bms.CompilationUnit visitCompilationUnit(BMSParser.CompilationUnitContext ctx) {
        List<Statement> statements = new ArrayList<>(ctx.statement().size());
        List<BMSParser.BmsContext> pending = new ArrayList<>();

        for (BMSParser.StatementContext statement : ctx.statement()) {
            if (statement.bms() == null) {
                flush(pending, statements);
                statements.add((Statement) visitStatement(statement));
                continue;
            }
            if (beginsStatement(statement.bms())) {
                flush(pending, statements);
            }
            pending.add(statement.bms());
        }
        flush(pending, statements);

        return new Bms.CompilationUnit(
                randomId(),
                path,
                fileAttributes,
                EMPTY,
                Markers.EMPTY,
                charset.name(),
                charsetBomMarked,
                null,
                statements,
                Space.build(source.substring(cursor))
        );
    }

    /**
     * Turns the words gathered so far into one statement: name field, operation, and everything
     * after it.
     * <p>
     * The operand field ends at the first blank, and what follows is the comment field — the
     * character in column 72 saying the statement carries on among it. Those words stay in the
     * operand list as plain words rather than becoming operands, so that the statement still prints
     * back exactly while only the parts that mean something are typed.
     */
    private void flush(List<BMSParser.BmsContext> pending, List<Statement> statements) {
        if (pending.isEmpty()) {
            return;
        }
        boolean named = isNamed(pending.get(0));
        Space prefix = whitespace();
        List<Bms.Word> words = new ArrayList<>(pending.size());
        List<Boolean> startsLine = new ArrayList<>(pending.size());
        for (BMSParser.BmsContext bms : pending) {
            words.add(word(bms));
            startsLine.add(beginsLine(bms));
        }
        pending.clear();

        Bms.Word name = named ? words.get(0).withPrefix(EMPTY) : null;
        int operationIndex = named ? 1 : 0;
        if (operationIndex >= words.size()) {
            // A name field with nothing after it. Not a macro invocation, so it is not typed as one.
            statements.add(new Bms.Unknown(randomId(), prefix, Markers.EMPTY,
                    words.get(0).withPrefix(EMPTY)));
            return;
        }
        Bms.Word operation = named ? words.get(operationIndex) : words.get(0).withPrefix(EMPTY);

        List<Bms> operands = new ArrayList<>();
        boolean expectOperand = true;
        for (int i = operationIndex + 1; i < words.size(); i++) {
            Bms.Word word = words.get(i);
            if (startsLine.get(i)) {
                // A continuation line resumes the operand field. Unlike JCL there is no punctuation
                // opening it — the line simply picks up where the one above left off.
                expectOperand = true;
            }
            if (expectOperand) {
                // The operand field runs to the first blank. A quoted string is its own token, so it
                // can span several words — the ones with nothing between them.
                StringBuilder run = new StringBuilder(word.getText());
                Bms.Word last = word;
                while (i + 1 < words.size() && !startsLine.get(i + 1) &&
                       words.get(i + 1).getPrefix().getWhitespace().isEmpty()) {
                    last = words.get(++i);
                    run.append(last.getText());
                }
                operands.addAll(operands(word.getPrefix(), run.toString(), last.getMarkers()));
                expectOperand = false;
                continue;
            }
            operands.add(word);
        }

        statements.add(new Bms.MacroStatement(randomId(), prefix, Markers.EMPTY, name, operation, operands));
    }

    /**
     * The operands of one operand field.
     * <p>
     * The field is a comma separated list, and the commas are not aligned with the tokens: the lexer
     * breaks on quotes, so {@code POS=(1,1),INITIAL='Tran :'} arrives as two words of which the
     * first holds two operands. So the run is split on its text rather than on token boundaries,
     * with each comma kept on the operand it follows. Concatenating the results reproduces the run
     * exactly, which is what printing needs.
     *
     * @param trailing markers from the last word of the run — a sequence area belongs at the end of
     *                 the line, so it goes on the last operand.
     */
    private static List<Bms> operands(Space prefix, String run, Markers trailing) {
        List<Bms> operands = new ArrayList<>();
        List<String> parts = splitOnTopLevelCommas(run);
        for (String text : parts) {
            boolean isLast = operands.size() == parts.size() - 1;
            Markers markers = isLast ? trailing : Markers.EMPTY;
            Space operandPrefix = operands.isEmpty() ? prefix : EMPTY;

            int equals = indexOfAssignment(text);
            if (equals <= 0) {
                operands.add(new Bms.PositionalOperand(randomId(), operandPrefix, Markers.EMPTY,
                        singletonList(new Bms.Word(randomId(), EMPTY, markers, text))));
            } else {
                // The keyword is its own word so it can be read and replaced without string work.
                Bms.Word keyword = new Bms.Word(randomId(), EMPTY, Markers.EMPTY, text.substring(0, equals));
                Bms.Word value = new Bms.Word(randomId(), EMPTY, markers, text.substring(equals));
                operands.add(new Bms.KeywordOperand(randomId(), operandPrefix, Markers.EMPTY,
                        keyword, singletonList(value)));
            }
        }
        return operands;
    }

    /**
     * Splits on commas outside parentheses and quotes, keeping each comma on the operand it follows
     * so that the pieces still concatenate to the original.
     */
    private static List<String> splitOnTopLevelCommas(String run) {
        List<String> parts = new ArrayList<>();
        int depth = 0;
        int start = 0;
        boolean quoted = false;
        for (int i = 0; i < run.length(); i++) {
            char c = run.charAt(i);
            if (c == '\'') {
                quoted = !quoted;
            } else if (quoted) {
                continue;
            } else if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
            } else if (c == ',' && depth == 0) {
                parts.add(run.substring(start, i + 1));
                start = i + 1;
            }
        }
        if (start < run.length()) {
            parts.add(run.substring(start));
        }
        return parts;
    }

    /**
     * The first {@code =} outside parentheses and quotes, so {@code JUSTIFY=(RIGHT,ZERO)} is one
     * keyword rather than two.
     */
    private static int indexOfAssignment(String operand) {
        int depth = 0;
        boolean quoted = false;
        for (int i = 0; i < operand.length(); i++) {
            char c = operand.charAt(i);
            if (c == '\'') {
                quoted = !quoted;
            } else if (quoted) {
                continue;
            } else if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
            } else if (c == '=' && depth == 0) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public Bms visitCommentWord(BMSParser.CommentWordContext ctx) {
        Space prefix = whitespace();
        Markers markers = Markers.EMPTY;
        Bms.Word word = (Bms.Word) visit(ctx.COMMENT_TEXT());
        if (ctx.commentSequenceArea() != null) {
            markers = markers.addIfAbsent(mapSequenceArea(ctx.commentSequenceArea()));
        }
        return new Bms.Comment(
                randomId(),
                prefix,
                markers,
                word
        );
    }

    @Override
    public Bms visitUnknownWord(BMSParser.UnknownWordContext ctx) {
        Space prefix = whitespace();
        Markers markers = Markers.EMPTY;
        Bms.Word word;
        if (ctx.UNKNOWN_TEXT() == null && ctx.UNKNOWN_STRINGLITERAL() == null) {
            word = new Bms.Word(randomId(), EMPTY, Markers.EMPTY, "");
        } else {
            word = visit(ctx.UNKNOWN_TEXT(), ctx.UNKNOWN_STRINGLITERAL());
        }
        if (ctx.unknownSequenceArea() != null) {
            markers = markers.addIfAbsent(mapSequenceArea(ctx.unknownSequenceArea()));
        }
        return new Bms.Unknown(
                randomId(),
                prefix,
                markers,
                word
        );
    }

    @Override
    public Bms visitTerminal(TerminalNode node) {
        return new Bms.Word(
                randomId(),
                sourceBefore(node.getText()),
                Markers.EMPTY,
                node.getText()
        );
    }

    private Bms.Word word(BMSParser.BmsContext ctx) {
        BMSParser.BmsWordContext word = ctx.bmsWord();
        Bms.Word w = visit(word.BMS_TEXT(), word.BMS_STRINGLITERAL());
        if (word.bmsSequenceArea() != null) {
            w = w.withMarkers(w.getMarkers().addIfAbsent(mapSequenceArea(word.bmsSequenceArea())));
        }
        return w;
    }

    private SequenceArea mapSequenceArea(ParserRuleContext ctx) {
        String text = ctx.getChild(ctx.getChildCount() - 1).getText();
        return new SequenceArea(randomId(), sourceBefore(text), text);
    }

    /**
     * Whether this word opens a statement rather than continuing the one before it.
     */
    private boolean beginsStatement(BMSParser.BmsContext ctx) {
        int marker = lineMarker(ctx);
        return marker == BMSLexer.BMS_NAMED || marker == BMSLexer.BMS_STATEMENT;
    }

    /**
     * Whether the statement this word opens writes a name field in column 1.
     */
    private boolean isNamed(BMSParser.BmsContext ctx) {
        return lineMarker(ctx) == BMSLexer.BMS_NAMED;
    }

    /**
     * Whether this word is the first on its line, either opening a statement or continuing one.
     */
    private boolean beginsLine(BMSParser.BmsContext ctx) {
        return lineMarker(ctx) != -1;
    }

    private int lineMarker(BMSParser.BmsContext ctx) {
        List<Token> hidden = tokens.getHiddenTokensToLeft(ctx.getStart().getTokenIndex());
        if (hidden != null) {
            for (Token token : hidden) {
                if (token.getType() == BMSLexer.BMS_NAMED ||
                    token.getType() == BMSLexer.BMS_STATEMENT ||
                    token.getType() == BMSLexer.BMS_CONTINUATION) {
                    return token.getType();
                }
            }
        }
        return -1;
    }

    private void skip(@Nullable String token) {
        if (token != null && source.startsWith(token, cursor)) {
            cursor += token.length();
        }
    }

    private Space whitespace() {
        int endIndex = indexOfNextNonWhitespace(cursor, source);
        String prefix = source.substring(cursor, endIndex);
        cursor += prefix.length();
        return Space.build(prefix);
    }

    private Space sourceBefore(String untilDelim) {
        Space prefix = whitespace();
        skip(untilDelim);
        return Space.build(prefix.getWhitespace());
    }

    public static int indexOfNextNonWhitespace(int cursor, String source) {
        int delimIndex = cursor;
        for (; delimIndex < source.length(); delimIndex++) {
            if (!Character.isWhitespace(source.charAt(delimIndex))) {
                break;
            }
        }
        return delimIndex;
    }
}
