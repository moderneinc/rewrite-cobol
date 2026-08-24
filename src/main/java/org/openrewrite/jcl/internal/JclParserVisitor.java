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
package org.openrewrite.jcl.internal;

import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jspecify.annotations.Nullable;
import org.openrewrite.FileAttributes;
import org.openrewrite.jcl.internal.grammar.JCLLexer;
import org.openrewrite.jcl.internal.grammar.JCLParser;
import org.openrewrite.jcl.internal.grammar.JCLParserBaseVisitor;
import org.openrewrite.jcl.marker.CommentArea;
import org.openrewrite.jcl.marker.TrailingComment;
import org.openrewrite.jcl.tree.Jcl;
import org.openrewrite.jcl.tree.Space;
import org.openrewrite.jcl.tree.Statement;
import org.openrewrite.marker.Markers;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.openrewrite.Tree.randomId;
import static org.openrewrite.jcl.tree.Space.EMPTY;

@RequiredArgsConstructor
public class JclParserVisitor extends JCLParserBaseVisitor<Jcl> {

    private final Path path;
    private final @Nullable FileAttributes fileAttributes;
    private final String source;
    private final Charset charset;
    private final boolean charsetBomMarked;

    /**
     * The line reader works out which lines begin a statement and which continue one, and puts that
     * on the hidden channel. It is the only place that information survives, so grouping words back
     * into statements needs the token stream rather than just the parse tree.
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
    public Jcl.CompilationUnit visitCompilationUnit(JCLParser.CompilationUnitContext ctx) {
        List<Statement> statements = new ArrayList<>(ctx.statement().size());
        List<JCLParser.StatementContext> pending = new ArrayList<>();

        List<JCLParser.StatementContext> all = ctx.statement();
        for (int i = 0; i < all.size(); i++) {
            JCLParser.StatementContext statement = all.get(i);
            if (statement.jcl() == null) {
                // A comment card between a statement's lines does not end the statement.
                if (statement.comment() != null && !pending.isEmpty() && continuesAfterComments(all, i)) {
                    pending.add(statement);
                    continue;
                }
                flush(pending, statements);
                statements.add((Statement) visitStatement(statement));
                continue;
            }
            if (beginsStatement(statement.jcl())) {
                flush(pending, statements);
            }
            pending.add(statement);
        }
        flush(pending, statements);

        return new Jcl.CompilationUnit(
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

    @Override
    public Jcl visitCommentWord(JCLParser.CommentWordContext ctx) {
        Space prefix = whitespace();
        Markers markers = Markers.EMPTY;
        Jcl.Word word = visit(ctx.COMMENT_TEXT(), ctx.COMMENT_STRINGLITERAL());
        if (ctx.commentCommentArea() != null) {
            markers = markers.addIfAbsent(mapCommentArea(ctx.commentCommentArea()));
        }
        return new Jcl.Comment(
                randomId(),
                prefix,
                markers,
                word
        );
    }

    @Override
    public Jcl visitControlMWord(JCLParser.ControlMWordContext ctx) {
        Space prefix = whitespace();
        Markers markers = Markers.EMPTY;
        Jcl.Word word = visit(ctx.CM_TEXT(), ctx.CM_STRINGLITERAL());
        if (ctx.controlMCommentArea() != null) {
            markers = markers.addIfAbsent(mapCommentArea(ctx.controlMCommentArea()));
        }
        return new Jcl.ControlM(
                randomId(),
                prefix,
                markers,
                word
        );
    }

    /**
     * Turns the words gathered so far into one statement: name field, operation, and everything
     * after it.
     * <p>
     * The operand field ends at the first blank on a line, and what follows is the comment field.
     * Those comment words stay in the operand list as plain words rather than becoming parameters,
     * so that the statement still prints back exactly while only the parts that mean something are
     * typed.
     */
    private void flush(List<JCLParser.StatementContext> pending, List<Statement> statements) {
        if (pending.isEmpty()) {
            return;
        }
        JCLParser.JclContext first = pending.get(0).jcl();
        Space prefix = whitespace();
        List<Jcl> words = new ArrayList<>(pending.size());
        List<Boolean> startsLine = new ArrayList<>(pending.size());
        for (JCLParser.StatementContext statement : pending) {
            if (statement.jcl() == null) {
                words.add(visitStatement(statement));
                startsLine.add(false);
            } else {
                words.add(word(statement.jcl()));
                startsLine.add(beginsLine(statement.jcl()));
            }
        }
        boolean endsStream = endsStream(first);
        pending.clear();

        // The delimiter statement ends in-stream data. It is not a job control statement: it has no
        // name field and no operation, and anything after it is a comment.
        if (endsStream) {
            List<Jcl.Word> comment = new ArrayList<>(words.size() - 1);
            for (Jcl word : words.subList(1, words.size())) {
                comment.add((Jcl.Word) word);
            }
            statements.add(new Jcl.Delimiter(randomId(), prefix, Markers.EMPTY,
                    ((Jcl.Word) words.get(0)).withPrefix(EMPTY), comment));
            return;
        }
        // The null statement, // alone, marks the end of a job. Also not a job control statement.
        if (words.size() == 1 && "//".equals(((Jcl.Word) words.get(0)).getText())) {
            statements.add(new Jcl.NullStatement(randomId(), prefix, Markers.EMPTY,
                    ((Jcl.Word) words.get(0)).withPrefix(EMPTY)));
            return;
        }

        Jcl.Word name = ((Jcl.Word) words.get(0)).withPrefix(EMPTY);
        Jcl.Word operation = words.size() > 1 ? (Jcl.Word) words.get(1) : null;

        List<Jcl> operands = new ArrayList<>();
        boolean expectOperand = operation != null;
        for (int i = 2; i < words.size(); i++) {
            if (words.get(i) instanceof Jcl.Comment) {
                operands.add(words.get(i));
                continue;
            }
            Jcl.Word word = (Jcl.Word) words.get(i);
            if (startsLine.get(i)) {
                // The // opening a continuation line. Kept as a word: it is punctuation, not an
                // operand, and the operand field starts again after it.
                operands.add(word);
                expectOperand = true;
                continue;
            }
            if (expectOperand) {
                // The operand field runs to the first blank. A quoted string is its own token, so it
                // can span several words — the ones with nothing between them.
                StringBuilder run = new StringBuilder(word.getText());
                Jcl.Word last = word;
                while (i + 1 < words.size() && words.get(i + 1) instanceof Jcl.Word && !startsLine.get(i + 1) &&
                       words.get(i + 1).getPrefix().getWhitespace().isEmpty()) {
                    last = (Jcl.Word) words.get(++i);
                    run.append(last.getText());
                }
                operands.addAll(parameters(word.getPrefix(), run.toString(), last.getMarkers()));
                expectOperand = false;
                continue;
            }
            operands.add(word);
        }

        statements.add(new Jcl.JobControlStatement(randomId(), prefix, Markers.EMPTY, name, operation, operands));
    }

    /**
     * The parameters of one operand field.
     * <p>
     * The field is a comma separated list, and the commas are not aligned with the tokens: the lexer
     * breaks on quotes, so {@code (ACCT),'DAILY POST',CLASS=A} arrives as three words of which the
     * last holds two parameters. So the run is split on its text rather than on token boundaries,
     * with each comma kept on the parameter it follows. Concatenating the results reproduces the run
     * exactly, which is what printing needs.
     *
     * @param trailing markers from the last word of the run — a comment area belongs at the end of
     *                 the line, so it goes on the last parameter.
     */
    private static List<Jcl> parameters(Space prefix, String run, Markers trailing) {
        List<Jcl> parameters = new ArrayList<>();
        for (String text : splitOnTopLevelCommas(run)) {
            boolean isLast = parameters.size() == countOf(run) - 1;
            Markers markers = isLast ? trailing : Markers.EMPTY;
            Space parameterPrefix = parameters.isEmpty() ? prefix : EMPTY;

            int equals = indexOfAssignment(text);
            if (equals <= 0) {
                parameters.add(new Jcl.PositionalParameter(randomId(), parameterPrefix, Markers.EMPTY,
                        singletonList(new Jcl.Word(randomId(), EMPTY, markers, text))));
            } else {
                // The keyword is its own word so it can be read and replaced without string work.
                Jcl.Word keyword = new Jcl.Word(randomId(), EMPTY, Markers.EMPTY, text.substring(0, equals));
                Jcl.Word value = new Jcl.Word(randomId(), EMPTY, markers, text.substring(equals));
                parameters.add(new Jcl.KeywordParameter(randomId(), parameterPrefix, Markers.EMPTY,
                        keyword, singletonList(value)));
            }
        }
        return parameters;
    }

    private static int countOf(String run) {
        return splitOnTopLevelCommas(run).size();
    }

    /**
     * Splits on commas outside parentheses and quotes, keeping each comma on the parameter it
     * follows so that the pieces still concatenate to the original.
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
     * The first {@code =} outside parentheses and quotes, so {@code AMP=('BUFND=5')} is one keyword
     * rather than two.
     */
    private static int indexOfAssignment(String parameter) {
        int depth = 0;
        boolean quoted = false;
        for (int i = 0; i < parameter.length(); i++) {
            char c = parameter.charAt(i);
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

    /**
     * The word of a JCL context, carrying any comment area or trailing comment as markers. They sat
     * on the enclosing statement before, which was one word; on the word they print in the same
     * place.
     */
    private Jcl.Word word(JCLParser.JclContext ctx) {
        if (ctx.jclTrailingComment() != null) {
            JCLParser.JclTrailingCommentContext tc = ctx.jclTrailingComment();
            Jcl.Word word = word(tc.jclWord(0));
            Markers markers = Markers.EMPTY.addIfAbsent(
                    mapTrailingComment(tc.jclWord().subList(1, tc.jclWord().size())));
            if (tc.jclCommentArea() != null) {
                markers = markers.addIfAbsent(mapCommentArea(tc.jclCommentArea()));
            }
            return word.withMarkers(markers);
        }
        return word(ctx.jclWord());
    }

    private Jcl.Word word(JCLParser.JclWordContext ctx) {
        Jcl.Word word = visit(ctx.JCL_TEXT(), ctx.JCL_STRINGLITERAL());
        if (ctx.jclCommentArea() != null) {
            word = word.withMarkers(word.getMarkers().addIfAbsent(mapCommentArea(ctx.jclCommentArea())));
        }
        return word;
    }

    /**
     * Whether the first JCL word after the comment cards from {@code from} on continues the
     * statement before them.
     */
    private boolean continuesAfterComments(List<JCLParser.StatementContext> all, int from) {
        for (int i = from; i < all.size(); i++) {
            if (all.get(i).jcl() != null) {
                return lineMarker(all.get(i).jcl()) == JCLLexer.JCL_CONTINUATION;
            }
            if (all.get(i).comment() == null) {
                return false;
            }
        }
        return false;
    }

    /**
     * Whether this word opens a statement rather than continuing the one before it.
     */
    private boolean beginsStatement(JCLParser.JclContext ctx) {
        int marker = lineMarker(ctx);
        return marker == JCLLexer.JCL_STATEMENT || marker == JCLLexer.JCL_STREAM_END;
    }

    /**
     * Whether this word is the delimiter that ends in-stream data. Only the line reader knows, since
     * a DD can name any delimiter it likes with {@code DLM}.
     */
    private boolean endsStream(JCLParser.JclContext ctx) {
        return lineMarker(ctx) == JCLLexer.JCL_STREAM_END;
    }

    /**
     * Whether this word is the first on its line, either opening a statement or continuing one.
     */
    private boolean beginsLine(JCLParser.JclContext ctx) {
        return lineMarker(ctx) != -1;
    }

    private int lineMarker(JCLParser.JclContext ctx) {
        List<Token> hidden = tokens.getHiddenTokensToLeft(ctx.getStart().getTokenIndex());
        if (hidden != null) {
            for (Token token : hidden) {
                if (token.getType() == JCLLexer.JCL_STATEMENT ||
                    token.getType() == JCLLexer.JCL_CONTINUATION ||
                    token.getType() == JCLLexer.JCL_STREAM_END) {
                    return token.getType();
                }
            }
        }
        return -1;
    }

    @Override
    public Jcl visitJes2Word(JCLParser.Jes2WordContext ctx) {
        Space prefix = whitespace();
        Markers markers = Markers.EMPTY;
        Jcl.Word word = visit(ctx.JES2_TEXT(), ctx.JES2_STRINGLITERAL());
        if (ctx.jes2CommentArea() != null) {
            markers = markers.addIfAbsent(mapCommentArea(ctx.jes2CommentArea()));
        }
        return new Jcl.Jes2(
                randomId(),
                prefix,
                markers,
                word
        );
    }

    @Override
    public Jcl visitJes3Word(JCLParser.Jes3WordContext ctx) {
        Space prefix = whitespace();
        Markers markers = Markers.EMPTY;
        Jcl.Word word = visit(ctx.JES3_TEXT(), ctx.JES3_STRINGLITERAL());
        if (ctx.jes3CommentArea() != null) {
            markers = markers.addIfAbsent(mapCommentArea(ctx.jes3CommentArea()));
        }
        return new Jcl.Jes3(
                randomId(),
                prefix,
                markers,
                word
        );
    }

    @Override
    public Jcl visitStreamWord(JCLParser.StreamWordContext ctx) {
        Space prefix = whitespace();
        Markers markers = Markers.EMPTY;
        Jcl.Word word = visit(ctx.STREAM_TEXT(), ctx.STREAM_STRINGLITERAL());
        if (ctx.streamCommentArea() != null) {
            markers = markers.addIfAbsent(mapCommentArea(ctx.streamCommentArea()));
        }
        return new Jcl.DataDefinitionStream(
                randomId(),
                prefix,
                markers,
                word
        );
    }

    @Override
    public Jcl visitUnknownWord(JCLParser.UnknownWordContext ctx) {
        Space prefix = whitespace();
        Markers markers = Markers.EMPTY;
        Jcl.Word word;
        if (ctx.UNKNOWN_TEXT() == null && ctx.UNKNOWN_STRINGLITERAL() == null) {
            word = new Jcl.Word(
                    randomId(),
                    EMPTY,
                    Markers.EMPTY,
                    ""
            );
        } else {
            word = visit(ctx.UNKNOWN_TEXT(), ctx.UNKNOWN_STRINGLITERAL());
        }
        if (ctx.unknownCommentArea() != null) {
            markers = markers.addIfAbsent(mapCommentArea(ctx.unknownCommentArea()));
        }
        return new Jcl.Unknown(
                randomId(),
                prefix,
                markers,
                word
        );
    }

    @Override
    public Jcl visitTerminal(TerminalNode node) {
        return new Jcl.Word(
                randomId(),
                sourceBefore(node.getText()),
                Markers.EMPTY,
                node.getText()
        );
    }

    private CommentArea mapCommentArea(ParserRuleContext ctx) {
        return new CommentArea(
                randomId(),
                sourceBefore(ctx.getChild(ctx.getChildCount() - 1).getText()),
                ctx.getChild(ctx.getChildCount() - 1).getText()
        );
    }

    private TrailingComment mapTrailingComment(List<JCLParser.JclWordContext> rules) {
        Space prefix = whitespace();
        StringBuilder trailingComment = new StringBuilder();
        for (ParserRuleContext ctx : rules) {
            trailingComment.append(sourceBefore(ctx.getText()).getWhitespace());
            trailingComment.append(ctx.getText());
        }
        return new TrailingComment(
                randomId(),
                prefix,
                trailingComment.toString()
        );
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
