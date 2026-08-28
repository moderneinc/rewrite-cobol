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
package org.openrewrite.mainframe.controlcard.sort.internal;

import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jspecify.annotations.Nullable;
import org.openrewrite.FileAttributes;
import org.openrewrite.mainframe.controlcard.sort.internal.grammar.SortCardLexer;
import org.openrewrite.mainframe.controlcard.sort.internal.grammar.SortCardParser;
import org.openrewrite.mainframe.controlcard.sort.internal.grammar.SortCardParserBaseVisitor;
import org.openrewrite.mainframe.controlcard.sort.tree.Sort;
import org.openrewrite.mainframe.controlcard.sort.tree.Space;
import org.openrewrite.mainframe.controlcard.sort.tree.Statement;
import org.openrewrite.marker.Markers;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.openrewrite.Tree.randomId;
import static org.openrewrite.mainframe.controlcard.sort.tree.Space.EMPTY;

@RequiredArgsConstructor
public class SortParserVisitor extends SortCardParserBaseVisitor<Sort> {

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

    @Override
    public Sort.CompilationUnit visitCompilationUnit(SortCardParser.CompilationUnitContext ctx) {
        List<Statement> statements = new ArrayList<>();
        List<SortCardParser.WordContext> pending = new ArrayList<>();

        for (SortCardParser.WordContext word : ctx.word()) {
            if (beginsStatement(word)) {
                flush(pending, statements);
            }
            pending.add(word);
        }
        flush(pending, statements);

        return new Sort.CompilationUnit(
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
    private void flush(List<SortCardParser.WordContext> pending, List<Statement> statements) {
        if (pending.isEmpty()) {
            return;
        }
        Space prefix = whitespace();
        List<Sort.Word> read = new ArrayList<>(pending.size());
        for (SortCardParser.WordContext context : pending) {
            read.add(word(context));
        }
        pending.clear();

        List<Sort.Word> words = separated(read);
        Sort.Word operator = words.get(0).withPrefix(EMPTY);
        List<Sort> operands = new ArrayList<>();
        for (int i = 1; i < words.size(); i++) {
            Sort.Word word = words.get(i);
            // Neither the comma between two operands nor the dash ending a continued ICETOOL card is
            // an operand, but both have to print back where they were written.
            if (",".equals(word.getText()) || "-".equals(word.getText())) {
                operands.add(word);
                continue;
            }
            i = operand(words, i, operands);
        }

        statements.add(new Sort.ControlStatement(randomId(), prefix, Markers.EMPTY, operator, operands));
    }

    /**
     * Breaks the statement's words at the commas that separate one operand from the next.
     * <p>
     * DFSORT separates operands with a comma as readily as with a blank, so
     * {@code FIELDS=(1,10,A),FORMAT=CH} is one word to the lexer and two operands to the statement.
     * Only a comma outside every parenthesis separates — the commas within a value belong to it — so
     * the depth is counted across the whole statement rather than within each word, which is what a
     * value written over several cards needs.
     */
    private static List<Sort.Word> separated(List<Sort.Word> words) {
        List<Sort.Word> separated = new ArrayList<>(words.size());
        int depth = 0;
        for (Sort.Word word : words) {
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
    private static Sort.Word part(Sort.Word word, boolean first, String text) {
        return first ? word.withText(text) : new Sort.Word(randomId(), EMPTY, Markers.EMPTY, text);
    }

    /**
     * Reads the operand beginning at {@code start} and returns the index of its last word.
     * <p>
     * DFSORT separates a keyword from its value with an equals sign and ICETOOL with parentheses, so
     * both are read: {@code FIELDS=(53,4,CH,A)} and {@code FROM(IN1)} are one word each,
     * {@code EQUALS} is a keyword with no value at all, and a value runs to the parenthesis that
     * closes it however many cards away that is.
     */
    private static int operand(List<Sort.Word> words, int start, List<Sort> operands) {
        Sort.Word first = words.get(start);
        String text = first.getText();
        int equals = text.indexOf('=');
        int open = text.indexOf('(');
        int split = equals >= 0 && (open < 0 || equals < open) ? equals : open;

        Sort.Word keyword = first;
        List<Sort.Word> value = new ArrayList<>();
        int i = start;

        if (split >= 0) {
            keyword = first.withText(text.substring(0, split));
            value.add(new Sort.Word(randomId(), EMPTY, Markers.EMPTY, text.substring(split)));
        } else {
            // A keyword may be written on one card and its value opened on the next, so the
            // continuation between them is looked through — and kept, so that it still prints.
            int group = start + 1;
            while (group < words.size() && "-".equals(words.get(group).getText())) {
                group++;
            }
            if (group < words.size() && words.get(group).getText().startsWith("(")) {
                while (i < group) {
                    value.add(words.get(++i));
                }
            }
        }

        int depth = 0;
        for (Sort.Word word : value) {
            depth += depthOf(word.getText());
        }
        while (depth > 0 && i + 1 < words.size()) {
            Sort.Word word = words.get(++i);
            value.add(word);
            depth += depthOf(word.getText());
        }

        operands.add(new Sort.Operand(randomId(), first.getPrefix(), Markers.EMPTY,
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
    public Sort visitTerminal(TerminalNode node) {
        return new Sort.Word(
                randomId(),
                sourceBefore(node.getText()),
                Markers.EMPTY,
                node.getText()
        );
    }

    private Sort.Word word(SortCardParser.WordContext ctx) {
        return (Sort.Word) visit(ctx.TEXT() == null ? ctx.STRINGLITERAL() : ctx.TEXT());
    }

    /**
     * Whether this word opens a control statement rather than continuing the one before it.
     */
    private boolean beginsStatement(SortCardParser.WordContext ctx) {
        List<Token> hidden = tokens.getHiddenTokensToLeft(ctx.getStart().getTokenIndex());
        if (hidden != null) {
            for (Token token : hidden) {
                if (token.getType() == SortCardLexer.CARD) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Everything between one word and the next: white space, and the comment cards written among the
     * statements, which the lexer hid and which print back from here.
     */
    private Space whitespace() {
        int start = cursor;
        while (cursor < source.length()) {
            if (Character.isWhitespace(source.charAt(cursor))) {
                cursor++;
            } else if (source.charAt(cursor) == '*' && (cursor == 0 || source.charAt(cursor - 1) == '\n')) {
                while (cursor < source.length() && source.charAt(cursor) != '\n') {
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
