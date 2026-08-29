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
                // The operand field runs to the first blank outside a quoted string, so it is
                // gathered a card at a time: a literal too long for one card carries on over the
                // blanks and the line break inside it.
                List<Bms.Word> cards = new ArrayList<>();
                Space cardPrefix = word.getPrefix();
                StringBuilder card = new StringBuilder(word.getText());
                Markers cardMarkers = word.getMarkers();
                boolean quoted = crossesQuote(word.getText());
                while (i + 1 < words.size()) {
                    Bms.Word next = words.get(i + 1);
                    String blanks = next.getPrefix().getWhitespace();
                    if (!quoted && !blanks.isEmpty()) {
                        break;
                    }
                    quoted ^= crossesQuote(next.getText());
                    if (startsLine.get(++i)) {
                        cards.add(new Bms.Word(randomId(), cardPrefix, cardMarkers, card.toString()));
                        cardPrefix = next.getPrefix();
                        card = new StringBuilder(next.getText());
                    } else {
                        card.append(blanks).append(next.getText());
                    }
                    cardMarkers = next.getMarkers();
                }
                cards.add(new Bms.Word(randomId(), cardPrefix, cardMarkers, card.toString()));
                operands.addAll(operands(cards));
                expectOperand = false;
                continue;
            }
            operands.add(word);
        }

        statements.add(new Bms.MacroStatement(randomId(), prefix, Markers.EMPTY, name, operation, operands));
    }

    /**
     * The operands of one operand field, given the cards it was written over.
     * <p>
     * The field is a comma separated list, and the commas are not aligned with the tokens: the lexer
     * breaks on quotes, so {@code POS=(1,1),INITIAL='Tran :'} arrives as two words of which the
     * first holds two operands. So the field is split on its text rather than on token boundaries,
     * with each comma kept on the operand it follows. An operand keeps one word per card it covers,
     * so that the card's own prefix and sequence area are still printed where they were written.
     */
    private static List<Bms> operands(List<Bms.Word> cards) {
        StringBuilder field = new StringBuilder();
        for (Bms.Word card : cards) {
            field.append(card.getText());
        }

        List<Bms> operands = new ArrayList<>();
        int card = 0;
        int offset = 0;
        for (String text : splitOnTopLevelCommas(field.toString())) {
            List<Bms.Word> value = new ArrayList<>();
            Space operandPrefix = EMPTY;
            for (int taken = 0; taken < text.length(); ) {
                Bms.Word from = cards.get(card);
                int end = Math.min(from.getText().length(), offset + text.length() - taken);
                Space wordPrefix = offset == 0 ? from.getPrefix() : EMPTY;
                if (value.isEmpty()) {
                    operandPrefix = wordPrefix;
                    wordPrefix = EMPTY;
                }
                value.add(new Bms.Word(randomId(), wordPrefix,
                        end == from.getText().length() ? from.getMarkers() : Markers.EMPTY,
                        from.getText().substring(offset, end)));
                taken += end - offset;
                offset = end;
                if (offset == from.getText().length()) {
                    card++;
                    offset = 0;
                }
            }

            int equals = indexOfAssignment(text);
            if (equals <= 0 || equals >= value.get(0).getText().length()) {
                operands.add(new Bms.PositionalOperand(randomId(), operandPrefix, Markers.EMPTY, value));
            } else {
                // The keyword is its own word so it can be read and replaced without string work.
                Bms.Word first = value.get(0);
                Bms.Word keyword = new Bms.Word(randomId(), EMPTY, Markers.EMPTY, first.getText().substring(0, equals));
                value.set(0, first.withText(first.getText().substring(equals)));
                operands.add(new Bms.KeywordOperand(randomId(), operandPrefix, Markers.EMPTY, keyword, value));
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
     * Whether the word holds an odd number of quotes, so what follows it is on the other side of
     * one from what came before.
     */
    private static boolean crossesQuote(String text) {
        int quotes = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\'') {
                quotes++;
            }
        }
        return quotes % 2 == 1;
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
