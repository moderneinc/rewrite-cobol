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
package org.openrewrite.controlcard.idcams.internal;

import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jspecify.annotations.Nullable;
import org.openrewrite.FileAttributes;
import org.openrewrite.controlcard.idcams.internal.grammar.IdcamsCardLexer;
import org.openrewrite.controlcard.idcams.internal.grammar.IdcamsCardParser;
import org.openrewrite.controlcard.idcams.internal.grammar.IdcamsCardParserBaseVisitor;
import org.openrewrite.controlcard.idcams.tree.Idcams;
import org.openrewrite.controlcard.idcams.tree.Space;
import org.openrewrite.controlcard.idcams.tree.Statement;
import org.openrewrite.marker.Markers;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.openrewrite.Tree.randomId;
import static org.openrewrite.controlcard.idcams.tree.Space.EMPTY;

@RequiredArgsConstructor
public class IdcamsParserVisitor extends IdcamsCardParserBaseVisitor<Idcams> {

    private final Path path;
    private final @Nullable FileAttributes fileAttributes;
    private final String source;
    private final Charset charset;
    private final boolean charsetBomMarked;

    /**
     * The line reader works out which cards open a command and which continue one. It is the only
     * place that information survives, so grouping words back into commands needs the token stream
     * rather than just the parse tree.
     */
    private final BufferedTokenStream tokens;

    private int cursor = 0;

    @Override
    public Idcams.CompilationUnit visitCompilationUnit(IdcamsCardParser.CompilationUnitContext ctx) {
        List<Statement> statements = new ArrayList<>();
        List<IdcamsCardParser.WordContext> pending = new ArrayList<>();

        for (IdcamsCardParser.WordContext word : ctx.word()) {
            if (beginsCommand(word)) {
                flush(pending, statements);
            }
            pending.add(word);
        }
        flush(pending, statements);

        return new Idcams.CompilationUnit(
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
     * Turns the words gathered so far into one command: the verb, and the parameters after it.
     */
    private void flush(List<IdcamsCardParser.WordContext> pending, List<Statement> statements) {
        if (pending.isEmpty()) {
            return;
        }
        Space prefix = whitespace();
        List<Idcams.Word> words = new ArrayList<>(pending.size());
        for (IdcamsCardParser.WordContext context : pending) {
            words.add(word(context));
        }
        pending.clear();

        Idcams.Word verb = words.get(0).withPrefix(EMPTY);
        List<Idcams> parameters = new ArrayList<>();
        for (int i = 1; i < words.size(); i++) {
            Idcams.Word word = words.get(i);
            // The dash or plus ending a continued card is not a parameter, but it has to print
            // back where it was written.
            if (isContinuation(word.getText())) {
                parameters.add(word);
                continue;
            }
            i = parameter(words, i, parameters);
        }

        statements.add(new Idcams.Command(randomId(), prefix, Markers.EMPTY, verb, parameters));
    }

    /**
     * Reads the parameter beginning at {@code start} and returns the index of its last word.
     * <p>
     * The value runs from the opening parenthesis to the one that closes it, which is not the same as
     * to the end of the word or of the card: {@code NAME('CLM.PROD.CLMMAST')} is three tokens because
     * the lexer breaks the quoted name out, {@code DATA (NAME(x))} writes the parenthesis in a word
     * of its own, and a {@code DEFINE CLUSTER} group is written over as many cards as it has
     * parameters.
     */
    private static int parameter(List<Idcams.Word> words, int start, List<Idcams> parameters) {
        Idcams.Word first = words.get(start);
        int open = first.getText().indexOf('(');
        Idcams.Word keyword = first;
        List<Idcams.Word> value = new ArrayList<>();
        int i = start;

        if (open >= 0) {
            keyword = first.withText(first.getText().substring(0, open));
            value.add(new Idcams.Word(randomId(), EMPTY, Markers.EMPTY, first.getText().substring(open)));
        } else {
            // A keyword may be written on one card and its group opened on the next, so the
            // continuation between them is looked through — and kept, so that it still prints.
            int group = start + 1;
            while (group < words.size() && isContinuation(words.get(group).getText())) {
                group++;
            }
            if (group < words.size() && words.get(group).getText().startsWith("(")) {
                while (i < group) {
                    value.add(words.get(++i));
                }
            }
        }

        int depth = 0;
        for (Idcams.Word word : value) {
            depth += depthOf(word.getText());
        }
        while (depth > 0 && i + 1 < words.size()) {
            Idcams.Word word = words.get(++i);
            value.add(word);
            depth += depthOf(word.getText());
        }

        parameters.add(new Idcams.Parameter(randomId(), first.getPrefix(), Markers.EMPTY,
                keyword.withPrefix(EMPTY), value));
        return i;
    }

    private static boolean isContinuation(String text) {
        return "-".equals(text) || "+".equals(text);
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
    public Idcams visitTerminal(TerminalNode node) {
        return new Idcams.Word(
                randomId(),
                sourceBefore(node.getText()),
                Markers.EMPTY,
                node.getText()
        );
    }

    private Idcams.Word word(IdcamsCardParser.WordContext ctx) {
        return (Idcams.Word) visit(ctx.TEXT() == null ? ctx.STRINGLITERAL() : ctx.TEXT());
    }

    /**
     * Whether this word opens a command rather than continuing the one before it.
     */
    private boolean beginsCommand(IdcamsCardParser.WordContext ctx) {
        List<Token> hidden = tokens.getHiddenTokensToLeft(ctx.getStart().getTokenIndex());
        if (hidden != null) {
            for (Token token : hidden) {
                if (token.getType() == IdcamsCardLexer.CARD) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Everything between one word and the next: white space, and the comments written among the
     * commands, which the lexer hid and which print back from here.
     */
    private Space whitespace() {
        int start = cursor;
        while (cursor < source.length()) {
            if (Character.isWhitespace(source.charAt(cursor))) {
                cursor++;
                continue;
            }
            int close = source.startsWith("/*", cursor) ? closingComment() : -1;
            if (close < 0) {
                break;
            }
            cursor = close + 2;
        }
        return Space.build(source.substring(start, cursor));
    }

    /**
     * Where the comment at the cursor closes, or -1 when it does not close on the card it opened on —
     * in which case it is not a comment at all, but words that happen to begin with a slash.
     */
    private int closingComment() {
        int close = source.indexOf("*/", cursor + 2);
        int newline = source.indexOf('\n', cursor + 2);
        return close >= 0 && (newline < 0 || close < newline) ? close : -1;
    }

    private Space sourceBefore(String untilDelim) {
        Space prefix = whitespace();
        if (source.startsWith(untilDelim, cursor)) {
            cursor += untilDelim.length();
        }
        return prefix;
    }
}
