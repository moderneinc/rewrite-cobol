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
package org.openrewrite.db2.bind.internal;

import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jspecify.annotations.Nullable;
import org.openrewrite.FileAttributes;
import org.openrewrite.db2.bind.internal.grammar.BindLexer;
import org.openrewrite.db2.bind.internal.grammar.BindParser;
import org.openrewrite.db2.bind.internal.grammar.BindParserBaseVisitor;
import org.openrewrite.db2.bind.tree.Bind;
import org.openrewrite.db2.bind.tree.Space;
import org.openrewrite.db2.bind.tree.Statement;
import org.openrewrite.marker.Markers;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.openrewrite.Tree.randomId;
import static org.openrewrite.db2.bind.tree.Space.EMPTY;

@RequiredArgsConstructor
public class BindParserVisitor extends BindParserBaseVisitor<Bind> {

    private final Path path;
    private final @Nullable FileAttributes fileAttributes;
    private final String source;
    private final Charset charset;
    private final boolean charsetBomMarked;

    /**
     * The line reader works out which lines open a subcommand and which continue one. It is the only
     * place that information survives, so grouping words back into commands needs the token stream
     * rather than just the parse tree.
     */
    private final BufferedTokenStream tokens;

    private int cursor = 0;

    @Override
    public Bind.CompilationUnit visitCompilationUnit(BindParser.CompilationUnitContext ctx) {
        List<Statement> statements = new ArrayList<>();
        List<BindParser.WordContext> pending = new ArrayList<>();

        for (BindParser.WordContext word : ctx.word()) {
            if (beginsCommand(word)) {
                flush(pending, statements);
            }
            pending.add(word);
        }
        flush(pending, statements);

        return new Bind.CompilationUnit(
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
     * Turns the words gathered so far into one subcommand: the verb, and the operands after it.
     */
    private void flush(List<BindParser.WordContext> pending, List<Statement> statements) {
        if (pending.isEmpty()) {
            return;
        }
        Space prefix = whitespace();
        List<Bind.Word> words = new ArrayList<>(pending.size());
        for (BindParser.WordContext context : pending) {
            words.add(word(context));
        }
        pending.clear();

        Bind.Word verb = words.get(0).withPrefix(EMPTY);
        List<Bind> operands = new ArrayList<>();
        for (int i = 1; i < words.size(); i++) {
            Bind.Word word = words.get(i);
            // The dash ending a continued line is not an operand, but it has to print back where it
            // was written.
            if ("-".equals(word.getText())) {
                operands.add(word);
                continue;
            }
            i = operand(words, i, operands);
        }

        statements.add(new Bind.Command(randomId(), prefix, Markers.EMPTY, verb, operands));
    }

    /**
     * Reads the operand beginning at {@code start} and returns the index of its last word.
     * <p>
     * The value runs from the opening parenthesis to the one that closes it, which is not the same as
     * to the end of the word or of the line: {@code LIBRARY('CLM.PROD.DBRMLIB')} is three tokens
     * because the lexer breaks the quoted string out, {@code SYSTEM (DB2P)} writes the parenthesis in
     * a word of its own, and a {@code PKLIST} may be written over as many lines as it has packages.
     */
    private static int operand(List<Bind.Word> words, int start, List<Bind> operands) {
        Bind.Word first = words.get(start);
        int open = first.getText().indexOf('(');
        Bind.Word keyword = first;
        List<Bind.Word> value = new ArrayList<>();
        int i = start;

        if (open >= 0) {
            keyword = first.withText(first.getText().substring(0, open));
            value.add(new Bind.Word(randomId(), EMPTY, Markers.EMPTY, first.getText().substring(open)));
        } else if (start + 1 < words.size() && words.get(start + 1).getText().startsWith("(")) {
            value.add(words.get(++i));
        }

        int depth = 0;
        for (Bind.Word word : value) {
            depth += depthOf(word.getText());
        }
        while (depth > 0 && i + 1 < words.size()) {
            Bind.Word word = words.get(++i);
            value.add(word);
            depth += depthOf(word.getText());
        }

        operands.add(new Bind.Operand(randomId(), first.getPrefix(), Markers.EMPTY,
                keyword.withPrefix(EMPTY), value));
        return i;
    }

    /**
     * How far the parentheses in a word open or close. Quoted text is skipped, since a data set name
     * may hold either character.
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
    public Bind visitTerminal(TerminalNode node) {
        return new Bind.Word(
                randomId(),
                sourceBefore(node.getText()),
                Markers.EMPTY,
                node.getText()
        );
    }

    private Bind.Word word(BindParser.WordContext ctx) {
        return (Bind.Word) visit(ctx.TEXT() == null ? ctx.STRINGLITERAL() : ctx.TEXT());
    }

    /**
     * Whether this word opens a subcommand rather than continuing the one before it.
     */
    private boolean beginsCommand(BindParser.WordContext ctx) {
        List<Token> hidden = tokens.getHiddenTokensToLeft(ctx.getStart().getTokenIndex());
        if (hidden != null) {
            for (Token token : hidden) {
                if (token.getType() == BindLexer.CARD) {
                    return true;
                }
            }
        }
        return false;
    }

    private Space whitespace() {
        int endIndex = cursor;
        while (endIndex < source.length() && Character.isWhitespace(source.charAt(endIndex))) {
            endIndex++;
        }
        String prefix = source.substring(cursor, endIndex);
        cursor += prefix.length();
        return Space.build(prefix);
    }

    private Space sourceBefore(String untilDelim) {
        Space prefix = whitespace();
        if (source.startsWith(untilDelim, cursor)) {
            cursor += untilDelim.length();
        }
        return prefix;
    }
}
