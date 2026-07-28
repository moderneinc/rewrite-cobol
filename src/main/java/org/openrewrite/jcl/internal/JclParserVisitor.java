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
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jspecify.annotations.Nullable;
import org.openrewrite.FileAttributes;
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

import static org.openrewrite.Tree.randomId;
import static org.openrewrite.jcl.tree.Space.EMPTY;

@RequiredArgsConstructor
public class JclParserVisitor extends JCLParserBaseVisitor<Jcl> {

    private final Path path;
    private final @Nullable FileAttributes fileAttributes;
    private final String source;
    private final Charset charset;
    private final boolean charsetBomMarked;

    private int cursor;

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
        for (JCLParser.StatementContext statement : ctx.statement()) {
            statements.add((Statement) visitStatement(statement));
        }

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

    @Override
    public Jcl visitJclWord(JCLParser.JclWordContext ctx) {
        Space prefix = whitespace();
        Markers markers = Markers.EMPTY;
        Jcl.Word word = visit(ctx.JCL_TEXT(), ctx.JCL_STRINGLITERAL());
        if (ctx.jclCommentArea() != null) {
            markers = markers.addIfAbsent(mapCommentArea(ctx.jclCommentArea()));
        }
        return new Jcl.JclStatement(
                randomId(),
                prefix,
                markers,
                word
        );
    }

    @Override
    public Jcl visitJclTrailingComment(JCLParser.JclTrailingCommentContext ctx) {
        Markers markers = Markers.EMPTY;
        Jcl.JclStatement jclStatement = (Jcl.JclStatement) visit(ctx.jclWord(0));

        markers = markers.addIfAbsent(mapTrailingComment(ctx.jclWord().subList(1, ctx.jclWord().size())));
        if (ctx.jclCommentArea() != null) {
            markers = markers.addIfAbsent(mapCommentArea(ctx.jclCommentArea()));
        }
        return jclStatement.withMarkers(markers);
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
