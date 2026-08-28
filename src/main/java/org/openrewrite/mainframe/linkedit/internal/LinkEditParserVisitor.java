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
package org.openrewrite.mainframe.linkedit.internal;

import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jspecify.annotations.Nullable;
import org.openrewrite.FileAttributes;
import org.openrewrite.mainframe.linkedit.internal.grammar.LinkEditLexer;
import org.openrewrite.mainframe.linkedit.internal.grammar.LinkEditParser;
import org.openrewrite.mainframe.linkedit.internal.grammar.LinkEditParserBaseVisitor;
import org.openrewrite.mainframe.linkedit.tree.LinkEdit;
import org.openrewrite.mainframe.linkedit.tree.Space;
import org.openrewrite.mainframe.linkedit.tree.Statement;
import org.openrewrite.marker.Markers;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.openrewrite.Tree.randomId;
import static org.openrewrite.mainframe.linkedit.LinkEditLineReader.DATA_COLUMNS;
import static org.openrewrite.mainframe.linkedit.tree.Space.EMPTY;

@RequiredArgsConstructor
public class LinkEditParserVisitor extends LinkEditParserBaseVisitor<LinkEdit> {

    private final Path path;
    private final @Nullable FileAttributes fileAttributes;
    private final String source;
    private final Charset charset;
    private final boolean charsetBomMarked;

    /**
     * The line reader works out which cards open a statement and which continue one. It is the only
     * place that information survives, so grouping words back into statements needs the token stream
     * rather than just the parse tree.
     */
    private final BufferedTokenStream tokens;

    private int cursor = 0;
    private int cardStart = 0;

    @Override
    public LinkEdit.CompilationUnit visitCompilationUnit(LinkEditParser.CompilationUnitContext ctx) {
        List<Statement> statements = new ArrayList<>();
        List<LinkEditParser.WordContext> pending = new ArrayList<>();

        for (LinkEditParser.WordContext word : ctx.word()) {
            if (beginsStatement(word)) {
                flush(pending, statements);
            }
            pending.add(word);
        }
        flush(pending, statements);

        return new LinkEdit.CompilationUnit(
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
     * Turns the words gathered so far into one control statement: the operator, and the operands
     * after it.
     */
    private void flush(List<LinkEditParser.WordContext> pending, List<Statement> statements) {
        if (pending.isEmpty()) {
            return;
        }
        Space prefix = whitespace();
        List<LinkEdit.Word> read = new ArrayList<>(pending.size());
        for (LinkEditParser.WordContext context : pending) {
            read.add(word(context));
        }
        pending.clear();

        List<LinkEdit.Word> words = separated(read);
        LinkEdit.Word operator = words.get(0).withPrefix(EMPTY);
        List<LinkEdit> operands = new ArrayList<>();
        for (int i = 1; i < words.size(); i++) {
            LinkEdit.Word word = words.get(i);
            // A comma between two operands is not an operand, but it has to print back where it was
            // written.
            if (",".equals(word.getText())) {
                operands.add(word);
                continue;
            }
            i = operand(words, i, operands);
        }

        statements.add(new LinkEdit.ControlStatement(randomId(), prefix, Markers.EMPTY, operator, operands));
    }

    /**
     * Breaks the statement's words at the commas that separate one operand from the next.
     * <p>
     * The binder separates operands with a comma as readily as with a blank, so
     * {@code OBJLIB(CLMU010),SYSLIB(DFHECI)} is one word to the lexer and two operands to the
     * statement. Only a comma outside every parenthesis separates — the commas within a value list
     * belong to it — so the depth is counted across the whole statement rather than within each word,
     * which is what a list written over several cards needs.
     */
    private static List<LinkEdit.Word> separated(List<LinkEdit.Word> words) {
        List<LinkEdit.Word> separated = new ArrayList<>(words.size());
        int depth = 0;
        for (LinkEdit.Word word : words) {
            String text = word.getText();
            // A quoted literal is one word to the lexer and holds no operand break.
            if (text.startsWith("'")) {
                separated.add(word);
                continue;
            }
            int start = 0;
            boolean firstPart = true;
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '(') {
                    depth++;
                } else if (c == ')') {
                    depth--;
                } else if (c == ',' && depth == 0) {
                    if (i > start) {
                        separated.add(part(word, firstPart, text.substring(start, i)));
                        firstPart = false;
                    }
                    separated.add(part(word, firstPart, ","));
                    firstPart = false;
                    start = i + 1;
                }
            }
            if (start < text.length()) {
                separated.add(part(word, firstPart, text.substring(start)));
            }
        }
        return separated;
    }

    /**
     * One piece of a word that was broken up. Only the first keeps the white space in front of the
     * word, since the pieces after it were written with nothing between them.
     */
    private static LinkEdit.Word part(LinkEdit.Word word, boolean first, String text) {
        return first ? word.withText(text) : new LinkEdit.Word(randomId(), EMPTY, Markers.EMPTY, text);
    }

    /**
     * Reads the operand beginning at {@code start} and returns the index of its last word.
     * <p>
     * The value runs from the opening parenthesis to the one that closes it, which is not the same as
     * to the end of the word or of the card: {@code IDENTIFY CLMB010('BUILD 2026')} is three tokens
     * because the lexer breaks the quoted literal out, and an {@code INCLUDE} may list as many members
     * as it has cards to write them on. An operand with no parentheses at all is a name of its own,
     * which is how {@code ENTRY}, {@code ALIAS} and {@code ORDER} are written.
     */
    private static int operand(List<LinkEdit.Word> words, int start, List<LinkEdit> operands) {
        LinkEdit.Word first = words.get(start);
        int open = first.getText().indexOf('(');
        LinkEdit.Word keyword = first;
        List<LinkEdit.Word> value = new ArrayList<>();
        int i = start;

        if (open >= 0) {
            keyword = first.withText(first.getText().substring(0, open));
            value.add(new LinkEdit.Word(randomId(), EMPTY, Markers.EMPTY, first.getText().substring(open)));
        } else if (start + 1 < words.size() && words.get(start + 1).getText().startsWith("(")) {
            value.add(words.get(++i));
        }

        int depth = 0;
        for (LinkEdit.Word word : value) {
            depth += depthOf(word.getText());
        }
        while (depth > 0 && i + 1 < words.size()) {
            LinkEdit.Word word = words.get(++i);
            value.add(word);
            depth += depthOf(word.getText());
        }

        operands.add(new LinkEdit.Operand(randomId(), first.getPrefix(), Markers.EMPTY,
                keyword.withPrefix(EMPTY), value));
        return i;
    }

    /**
     * How far the parentheses in a word open or close. Quoted text is skipped, since a literal may
     * hold either character.
     */
    private static int depthOf(String text) {
        int depth = 0;
        boolean quoted = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\'') {
                quoted = !quoted;
            } else if (quoted) {
                continue;
            } else if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
            }
        }
        return depth;
    }

    @Override
    public LinkEdit visitTerminal(TerminalNode node) {
        return new LinkEdit.Word(
                randomId(),
                sourceBefore(node.getText()),
                Markers.EMPTY,
                node.getText()
        );
    }

    private LinkEdit.Word word(LinkEditParser.WordContext ctx) {
        return (LinkEdit.Word) visit(ctx.TEXT() == null ? ctx.STRINGLITERAL() : ctx.TEXT());
    }

    /**
     * Whether this word opens a control statement rather than continuing the one before it.
     */
    private boolean beginsStatement(LinkEditParser.WordContext ctx) {
        List<Token> hidden = tokens.getHiddenTokensToLeft(ctx.getStart().getTokenIndex());
        if (hidden != null) {
            for (Token token : hidden) {
                if (token.getType() == LinkEditLexer.CARD) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Everything between one word and the next: white space, the comment cards written among the
     * statements, and columns 72-80 of every card. None of them reached the grammar, and all of them
     * print back from here.
     */
    private Space whitespace() {
        int start = cursor;
        while (cursor < source.length()) {
            char c = source.charAt(cursor);
            if (c == '\n' || c == '\r') {
                cursor++;
                cardStart = cursor;
            } else if (Character.isWhitespace(c)) {
                cursor++;
            } else if ((c == '*' && cursor == cardStart) || cursor - cardStart >= DATA_COLUMNS) {
                while (cursor < source.length() && source.charAt(cursor) != '\n' && source.charAt(cursor) != '\r') {
                    cursor++;
                }
            } else {
                break;
            }
        }
        return Space.build(source.substring(start, cursor));
    }

    private Space sourceBefore(String untilDelim) {
        Space prefix = whitespace();
        if (source.startsWith(untilDelim, cursor)) {
            cursor += untilDelim.length();
        }
        return prefix;
    }
}
