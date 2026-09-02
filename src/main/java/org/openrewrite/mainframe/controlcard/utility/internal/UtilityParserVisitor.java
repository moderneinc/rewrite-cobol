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
package org.openrewrite.mainframe.controlcard.utility.internal;

import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jspecify.annotations.Nullable;
import org.openrewrite.FileAttributes;
import org.openrewrite.mainframe.controlcard.utility.Keywords;
import org.openrewrite.mainframe.controlcard.utility.UtilityLineReader;
import org.openrewrite.mainframe.controlcard.utility.internal.grammar.UtilityCardParser;
import org.openrewrite.mainframe.controlcard.utility.internal.grammar.UtilityCardParserBaseVisitor;
import org.openrewrite.mainframe.controlcard.utility.marker.Dialect;
import org.openrewrite.mainframe.controlcard.utility.tree.Space;
import org.openrewrite.mainframe.controlcard.utility.tree.Statement;
import org.openrewrite.mainframe.controlcard.utility.tree.Utility;
import org.openrewrite.marker.Markers;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static org.openrewrite.Tree.randomId;
import static org.openrewrite.mainframe.controlcard.utility.tree.Space.EMPTY;

/**
 * Groups the words of a deck into the blocks they were written as.
 * <p>
 * The grammar reads a deck as words, because a utility control statement has no punctuation to group
 * it by. What decides where an operand ends is the vocabulary: a word that is a keyword of the block
 * being read opens an operand, a word that opens a block nested in it pushes one, and a verb that
 * opens a control statement closes everything down to the deck. Anything else belongs to whatever is
 * being read.
 */
@RequiredArgsConstructor
public class UtilityParserVisitor extends UtilityCardParserBaseVisitor<Utility> {

    private final Path path;
    private final @Nullable FileAttributes fileAttributes;
    private final String source;
    private final Charset charset;
    private final boolean charsetBomMarked;

    private int cursor = 0;
    private boolean beforeFirstKeyword = true;

    @Override
    public Utility.CompilationUnit visitCompilationUnit(UtilityCardParser.CompilationUnitContext ctx) {
        List<Statement> statements = new ArrayList<>();
        Deque<Block> open = new ArrayDeque<>();
        int depth = 0;

        for (UtilityCardParser.WordContext wordCtx : ctx.word()) {
            Utility.Word word = word(wordCtx);
            if (depth == 0 || open.isEmpty()) {
                place(word, open, statements);
            } else {
                open.peek().append(word);
            }
            depth = Math.max(0, depth + depthOf(word.getText()));
        }
        closeTo(null, open, statements);

        return new Utility.CompilationUnit(
                randomId(),
                path,
                fileAttributes,
                EMPTY,
                Markers.EMPTY.add(new Dialect(randomId(), Dialect.of(statements))),
                charset.name(),
                charsetBomMarked,
                null,
                statements,
                Space.build(source.substring(cursor))
        );
    }

    /**
     * Puts one word where the vocabulary says it belongs.
     */
    private void place(Utility.Word word, Deque<Block> open, List<Statement> statements) {
        String text = word.getText();
        String keyword = keywordOf(text);
        Block block = open.peek();

        if (";".equals(text) && block != null) {
            // Written where a block ends, but it is what follows that closes the block: the unload
            // product allows one between a SELECT's SQL and its OUTDDN as well.
            block.flush();
            block.contents.add(word);
            return;
        }
        if (block != null && Keywords.isOperandOf(block.verb, keyword)) {
            block.startOperand(word, keyword);
            return;
        }
        if (block != null && Keywords.opensBlockIn(block.verb, keyword)) {
            open.push(block.open(word));
            return;
        }
        for (Block enclosing : open) {
            if (enclosing != block && Keywords.opensBlockIn(enclosing.verb, keyword)) {
                closeTo(enclosing, open, statements);
                open.push(enclosing.open(word));
                return;
            }
        }
        if (block == null || Keywords.opensStatement(keyword)) {
            closeTo(null, open, statements);
            open.push(new Block(word));
            return;
        }
        block.append(word);
    }

    /**
     * Closes every block inside {@code keep}, or the whole deck when it is null, putting each one
     * into what encloses it.
     */
    private void closeTo(@Nullable Block keep, Deque<Block> open, List<Statement> statements) {
        while (!open.isEmpty() && open.peek() != keep) {
            Utility.Block closed = open.pop().build();
            if (open.isEmpty()) {
                statements.add(closed);
            } else {
                open.peek().contents.add(closed);
            }
        }
        if (keep != null) {
            keep.flush();
        }
    }

    /**
     * The keyword a word is written under: {@code TABLE(ALL)} is the keyword {@code TABLE} with the
     * value written against it, which is how a utility writes an operand as often as with a blank.
     */
    private static String keywordOf(String text) {
        int open = text.indexOf('(');
        return open < 0 ? text : text.substring(0, open);
    }

    /**
     * How far the parentheses in a word open or close. A quoted string is a word of its own, so
     * nothing here has to look inside one.
     */
    private static int depthOf(String text) {
        if (text.startsWith("'")) {
            return 0;
        }
        int depth = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
            }
        }
        return depth;
    }

    /**
     * A block being read: the verb it opened with, and everything gathered under it so far.
     */
    private static class Block {
        private final String verb;
        private final Utility.Word verbWord;
        private final Space prefix;
        private final List<Utility.Word> value = new ArrayList<>();
        private final List<Utility> contents = new ArrayList<>();

        private Utility.@Nullable Word keyword;
        private Space operandPrefix = EMPTY;
        private List<Utility.Word> operandValue = new ArrayList<>();

        Block(Utility.Word verb) {
            this.verb = verb.getText();
            this.verbWord = verb.withPrefix(EMPTY);
            this.prefix = verb.getPrefix();
        }

        Block open(Utility.Word verb) {
            flush();
            return new Block(verb);
        }

        void startOperand(Utility.Word word, String keyword) {
            flush();
            operandPrefix = word.getPrefix();
            if (keyword.length() < word.getText().length()) {
                this.keyword = word.withPrefix(EMPTY).withText(keyword);
                operandValue.add(new Utility.Word(randomId(), EMPTY, Markers.EMPTY,
                        word.getText().substring(keyword.length())));
            } else {
                this.keyword = word.withPrefix(EMPTY);
            }
        }

        /**
         * A word that opens nothing: part of the operand being read, or of what the block is about
         * when none has been opened yet. Once anything has been written under the block a stray word
         * joins the contents rather than the value, so that it prints back where it was written.
         */
        void append(Utility.Word word) {
            if (keyword != null) {
                operandValue.add(word);
            } else if (contents.isEmpty()) {
                value.add(word);
            } else {
                contents.add(word);
            }
        }

        void flush() {
            if (keyword != null) {
                contents.add(new Utility.Operand(randomId(), operandPrefix, Markers.EMPTY, keyword,
                        operandValue));
                keyword = null;
                operandPrefix = EMPTY;
                operandValue = new ArrayList<>();
            }
        }

        Utility.Block build() {
            flush();
            return new Utility.Block(randomId(), prefix, Markers.EMPTY, verbWord, value, contents);
        }
    }

    @Override
    public Utility visitTerminal(TerminalNode node) {
        return new Utility.Word(
                randomId(),
                sourceBefore(node.getText()),
                Markers.EMPTY,
                node.getText()
        );
    }

    private Utility.Word word(UtilityCardParser.WordContext ctx) {
        TerminalNode node = ctx.TEXT() != null ? ctx.TEXT() :
                ctx.STRINGLITERAL() != null ? ctx.STRINGLITERAL() : ctx.SEMICOLON();
        return (Utility.Word) visit(node);
    }

    /**
     * Everything between one word and the next: white space, and the comment cards written among the
     * statements, which the lexer hid and which print back from here.
     */
    private Space whitespace() {
        int start = cursor;
        while (cursor < source.length()) {
            char c = source.charAt(cursor);
            if (Character.isWhitespace(c)) {
                cursor++;
            } else if ((cursor == 0 || source.charAt(cursor - 1) == '\n') &&
                       UtilityLineReader.isComment(source, cursor, beforeFirstKeyword)) {
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
        beforeFirstKeyword = false;
        return prefix;
    }
}
