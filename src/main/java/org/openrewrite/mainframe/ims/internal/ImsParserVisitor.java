/*
 * Copyright 2026 the original author or authors.
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
package org.openrewrite.mainframe.ims.internal;

import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jspecify.annotations.Nullable;
import org.openrewrite.FileAttributes;
import org.openrewrite.mainframe.ims.internal.grammar.IMSLexer;
import org.openrewrite.mainframe.ims.internal.grammar.IMSParser;
import org.openrewrite.mainframe.ims.internal.grammar.IMSParserBaseVisitor;
import org.openrewrite.mainframe.ims.marker.SequenceArea;
import org.openrewrite.mainframe.ims.tree.Ims;
import org.openrewrite.mainframe.ims.tree.Space;
import org.openrewrite.mainframe.ims.tree.Statement;
import org.openrewrite.marker.Markers;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.openrewrite.Tree.randomId;
import static org.openrewrite.mainframe.ims.tree.Space.EMPTY;

@RequiredArgsConstructor
public class ImsParserVisitor extends IMSParserBaseVisitor<Ims> {

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
    public Ims.CompilationUnit visitCompilationUnit(IMSParser.CompilationUnitContext ctx) {
        List<Statement> statements = new ArrayList<>(ctx.statement().size());
        List<IMSParser.ImsContext> pending = new ArrayList<>();

        for (IMSParser.StatementContext statement : ctx.statement()) {
            if (statement.ims() == null) {
                flush(pending, statements);
                statements.add((Statement) visitStatement(statement));
                continue;
            }
            if (beginsStatement(statement.ims())) {
                flush(pending, statements);
            }
            pending.add(statement.ims());
        }
        flush(pending, statements);

        return new Ims.CompilationUnit(
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
    private void flush(List<IMSParser.ImsContext> pending, List<Statement> statements) {
        if (pending.isEmpty()) {
            return;
        }
        boolean named = isNamed(pending.get(0));
        Space prefix = whitespace();
        List<Ims.Word> words = new ArrayList<>(pending.size());
        List<Boolean> startsLine = new ArrayList<>(pending.size());
        for (IMSParser.ImsContext ims : pending) {
            words.add(word(ims));
            startsLine.add(beginsLine(ims));
        }
        pending.clear();

        Ims.Word name = named ? words.get(0).withPrefix(EMPTY) : null;
        int operationIndex = named ? 1 : 0;
        if (operationIndex >= words.size()) {
            // A name field with nothing after it. Not a macro invocation, so it is not typed as one.
            statements.add(new Ims.Unknown(randomId(), prefix, Markers.EMPTY,
                    words.get(0).withPrefix(EMPTY)));
            return;
        }
        Ims.Word operation = named ? words.get(operationIndex) : words.get(0).withPrefix(EMPTY);

        List<Ims> operands = new ArrayList<>();
        boolean expectOperand = true;
        for (int i = operationIndex + 1; i < words.size(); i++) {
            Ims.Word word = words.get(i);
            if (startsLine.get(i)) {
                // A continuation line resumes the operand field. Unlike JCL there is no punctuation
                // opening it — the line simply picks up where the one above left off.
                expectOperand = true;
            }
            if (expectOperand) {
                // The operand field runs to the first blank. A quoted string is its own token, so it
                // can span several words — the ones with nothing between them.
                StringBuilder run = new StringBuilder(word.getText());
                Ims.Word last = word;
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

        statements.add(new Ims.MacroStatement(randomId(), prefix, Markers.EMPTY, name, operation, operands));
    }

    /**
     * The operands of one operand field.
     * <p>
     * The field is a comma separated list, and the commas are not aligned with the tokens: the lexer
     * breaks on quotes, so {@code INTERNALTYPECONVERTER=CHAR,PATTERN='yyyy-MM-dd HH:mm:ss'} arrives
     * as two words of which the first holds two operands. So the run is split on its text rather
     * than on token boundaries,
     * with each comma kept on the operand it follows. Concatenating the results reproduces the run
     * exactly, which is what printing needs.
     *
     * @param trailing markers from the last word of the run — a sequence area belongs at the end of
     *                 the line, so it goes on the last operand.
     */
    private static List<Ims> operands(Space prefix, String run, Markers trailing) {
        List<Ims> operands = new ArrayList<>();
        List<String> parts = splitOnTopLevelCommas(run);
        for (String text : parts) {
            boolean isLast = operands.size() == parts.size() - 1;
            Markers markers = isLast ? trailing : Markers.EMPTY;
            Space operandPrefix = operands.isEmpty() ? prefix : EMPTY;

            int equals = indexOfAssignment(text);
            if (equals <= 0) {
                operands.add(new Ims.PositionalOperand(randomId(), operandPrefix, Markers.EMPTY,
                        singletonList(new Ims.Word(randomId(), EMPTY, markers, text))));
            } else {
                // The keyword is its own word so it can be read and replaced without string work.
                Ims.Word keyword = new Ims.Word(randomId(), EMPTY, Markers.EMPTY, text.substring(0, equals));
                Ims.Word value = new Ims.Word(randomId(), EMPTY, markers, text.substring(equals));
                operands.add(new Ims.KeywordOperand(randomId(), operandPrefix, Markers.EMPTY,
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
     * The first {@code =} outside parentheses and quotes, so {@code ACCESS=(HDAM,VSAM)} is one
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
    public Ims visitCommentWord(IMSParser.CommentWordContext ctx) {
        Space prefix = whitespace();
        Markers markers = Markers.EMPTY;
        Ims.Word word = (Ims.Word) visit(ctx.COMMENT_TEXT());
        if (ctx.commentSequenceArea() != null) {
            markers = markers.addIfAbsent(mapSequenceArea(ctx.commentSequenceArea()));
        }
        return new Ims.Comment(
                randomId(),
                prefix,
                markers,
                word
        );
    }

    @Override
    public Ims visitUnknownWord(IMSParser.UnknownWordContext ctx) {
        Space prefix = whitespace();
        Markers markers = Markers.EMPTY;
        Ims.Word word;
        if (ctx.UNKNOWN_TEXT() == null && ctx.UNKNOWN_STRINGLITERAL() == null) {
            word = new Ims.Word(randomId(), EMPTY, Markers.EMPTY, "");
        } else {
            word = visit(ctx.UNKNOWN_TEXT(), ctx.UNKNOWN_STRINGLITERAL());
        }
        if (ctx.unknownSequenceArea() != null) {
            markers = markers.addIfAbsent(mapSequenceArea(ctx.unknownSequenceArea()));
        }
        return new Ims.Unknown(
                randomId(),
                prefix,
                markers,
                word
        );
    }

    @Override
    public Ims visitTerminal(TerminalNode node) {
        return new Ims.Word(
                randomId(),
                sourceBefore(node.getText()),
                Markers.EMPTY,
                node.getText()
        );
    }

    private Ims.Word word(IMSParser.ImsContext ctx) {
        IMSParser.ImsWordContext word = ctx.imsWord();
        Ims.Word w = visit(word.IMS_TEXT(), word.IMS_STRINGLITERAL());
        if (word.imsSequenceArea() != null) {
            w = w.withMarkers(w.getMarkers().addIfAbsent(mapSequenceArea(word.imsSequenceArea())));
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
    private boolean beginsStatement(IMSParser.ImsContext ctx) {
        int marker = lineMarker(ctx);
        return marker == IMSLexer.IMS_NAMED || marker == IMSLexer.IMS_STATEMENT;
    }

    /**
     * Whether the statement this word opens writes a name field in column 1.
     */
    private boolean isNamed(IMSParser.ImsContext ctx) {
        return lineMarker(ctx) == IMSLexer.IMS_NAMED;
    }

    /**
     * Whether this word is the first on its line, either opening a statement or continuing one.
     */
    private boolean beginsLine(IMSParser.ImsContext ctx) {
        return lineMarker(ctx) != -1;
    }

    private int lineMarker(IMSParser.ImsContext ctx) {
        List<Token> hidden = tokens.getHiddenTokensToLeft(ctx.getStart().getTokenIndex());
        if (hidden != null) {
            for (Token token : hidden) {
                if (token.getType() == IMSLexer.IMS_NAMED ||
                    token.getType() == IMSLexer.IMS_STATEMENT ||
                    token.getType() == IMSLexer.IMS_CONTINUATION) {
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
