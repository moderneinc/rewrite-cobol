/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.jcl.internal;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.openrewrite.FileAttributes;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.jcl.internal.grammar.JCLParser;
import org.openrewrite.jcl.internal.grammar.JCLParserBaseVisitor;
import org.openrewrite.jcl.marker.*;
import org.openrewrite.jcl.tree.*;
import org.openrewrite.marker.Marker;
import org.openrewrite.marker.Markers;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static org.openrewrite.Tree.randomId;
import static org.openrewrite.jcl.tree.Space.EMPTY;

public class JclParserVisitor extends JCLParserBaseVisitor<Jcl> {

    private final Path path;
    private final @Nullable FileAttributes fileAttributes;
    private final String source;
    private final Charset charset;
    private final boolean charsetBomMarked;

    private int cursor = 0;

    public JclParserVisitor(Path path,
                            @Nullable FileAttributes fileAttributes,
                            String source,
                            Charset charset,
                            boolean charsetBomMarked) {
        this.path = path;
        this.fileAttributes = fileAttributes;
        this.source = source;
        this.charset = charset;
        this.charsetBomMarked = charsetBomMarked;
    }

    public <T> T visit(@Nullable ParseTree... trees) {
        for (ParseTree tree : trees) {
            if (tree != null) {
                //noinspection unchecked
                return (T) visit(tree);
            }
        }
        throw new IllegalStateException("Expected one of the supplied trees to be non-null");
    }

    public <T> T visitNullable(@Nullable ParseTree tree) {
        if (tree == null) {
            //noinspection ConstantConditions
            return null;
        }
        //noinspection unchecked
        return (T) super.visit(tree);
    }

    @Override
    public Jcl.CompilationUnit visitCompilationUnit(JCLParser.CompilationUnitContext ctx) {
        return new Jcl.CompilationUnit(
                randomId(),
                path,
                fileAttributes,
                EMPTY,
                Markers.EMPTY,
                charset.name(),
                charsetBomMarked,
                null,
                createStatements(ctx.statement()),
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
    public Jcl visitCntlStatement(JCLParser.CntlStatementContext ctx) {
        Jcl.CntlStatement cntl = new Jcl.CntlStatement(
                randomId(),
                sourceBefore("//"),
                Markers.EMPTY,
                visitNullable(ctx.jclName()),
                (Name) visit(ctx.cntlName())
        );
        if (ctx.jclCommentArea() != null) {
            cntl = cntl.withMarkers(cntl.getMarkers().addIfAbsent(mapCommentArea(ctx.jclCommentArea())));
        }
        return cntl;
    }

    @Override
    public Jcl visitCntlName(JCLParser.CntlNameContext ctx) {
        Jcl.Identifier id = createIdentifier(ctx.JCL_CNTL().getText());
        if (ctx.jclCommentArea() != null) {
            id = id.withMarkers(id.getMarkers().addIfAbsent(mapCommentArea(ctx.jclCommentArea())));
        }
        return id;
    }

    @Override
    public Jcl visitDdStatement(JCLParser.DdStatementContext ctx) {
        Jcl.DataDefinitionStatement dd = new Jcl.DataDefinitionStatement(
                randomId(),
                sourceBefore("//"),
                Markers.EMPTY,
                visitNullable(ctx.jclName()),
                (Name) visit(ctx.ddName()),
                createParameters(ctx.JCL_COMMA_CHAR(), ctx.parameterArgument())
        );
        if (ctx.jclTrailingComment() != null) {
            for (Marker marker : mapTrailingComment(ctx.jclTrailingComment())) {
                dd = dd.withMarkers(dd.getMarkers().addIfAbsent(marker));
            }
        }
        return dd;
    }

    @Override
    public Jcl visitDdName(JCLParser.DdNameContext ctx) {
        Jcl.Identifier id = createIdentifier(ctx.JCL_DD().getText());
        if (ctx.jclCommentArea() != null) {
            id = id.withMarkers(id.getMarkers().addIfAbsent(mapCommentArea(ctx.jclCommentArea())));
        }
        return id;
    }

    @Override
    public Jcl visitDdStreamStatement(JCLParser.DdStreamStatementContext ctx) {
        Jcl.DataDefinitionStream dd = new Jcl.DataDefinitionStream(
                randomId(),
                sourceBefore("//"),
                Markers.EMPTY,
                visitNullable(ctx.jclName()),
                (Name) visit(ctx.ddName()),
                (Name) visit(ctx.parameter()),
                convertAll(ctx.streamText(), noDelim, noDelim)
        );
        if (ctx.jclTrailingComment() != null) {
            for (Marker marker : mapTrailingComment(ctx.jclTrailingComment())) {
                dd = dd.withMarkers(dd.getMarkers().addIfAbsent(marker));
            }
        }
        return dd;
    }

    @Override
    public Jcl visitStreamText(JCLParser.StreamTextContext ctx) {
        Jcl jcl = visit(ctx.STREAM_TEXT(), ctx.STREAM_STRINGLITERAL());
        if (ctx.streamJclCommentArea() != null) {
            jcl = jcl.withMarkers(jcl.getMarkers().addIfAbsent(mapCommentArea(ctx.streamJclCommentArea())));
        }
        return jcl;
    }

    @Override
    public Jcl visitElseStatement(JCLParser.ElseStatementContext ctx) {
        return new Jcl.IfStatement.ElseStatement(
                randomId(),
                sourceBefore("//"),
                Markers.EMPTY,
                visitNullable(ctx.jclName()),
                (Name) visit(ctx.elseName()),
                createStatements(ctx.statement())
        );
    }

    @Override
    public Jcl visitElseName(JCLParser.ElseNameContext ctx) {
        Jcl.Identifier id = createIdentifier(ctx.JCL_ELSE().getText());
        if (ctx.jclCommentArea() != null) {
            id = id.withMarkers(id.getMarkers().addIfAbsent(mapCommentArea(ctx.jclCommentArea())));
        }
        return id;
    }

    @Override
    public Jcl visitEmptyStatement(JCLParser.EmptyStatementContext ctx) {
        Space prefix = sourceBefore("//");
        Markers markers = Markers.EMPTY;
        if (ctx.jclCommentArea() != null) {
            markers = markers.addIfAbsent(mapCommentArea(ctx.jclCommentArea()));
        }
        return new Jcl.EmptyStatement(
                randomId(),
                prefix,
                markers
        );
    }

    @Override
    public Jcl visitEndcntlStatement(JCLParser.EndcntlStatementContext ctx) {
        Jcl.CntlStatement cntl = new Jcl.CntlStatement(
                randomId(),
                sourceBefore("//"),
                Markers.EMPTY,
                visitNullable(ctx.jclName()),
                (Name) visit(ctx.endcntlName())
        );
        if (ctx.jclCommentArea() != null) {
            cntl = cntl.withMarkers(cntl.getMarkers().addIfAbsent(mapCommentArea(ctx.jclCommentArea())));
        }
        return cntl;
    }

    @Override
    public Jcl visitEndcntlName(JCLParser.EndcntlNameContext ctx) {
        Jcl.Identifier id = createIdentifier(ctx.JCL_ENDCNTL().getText());
        if (ctx.jclCommentArea() != null) {
            id = id.withMarkers(id.getMarkers().addIfAbsent(mapCommentArea(ctx.jclCommentArea())));
        }
        return id;
    }

    @Override
    public Jcl visitEndifStatement(JCLParser.EndifStatementContext ctx) {
        return new Jcl.IfStatement.EndIfStatement(
                randomId(),
                sourceBefore("//"),
                Markers.EMPTY,
                visitNullable(ctx.jclName()),
                (Name) visit(ctx.endifName())
        );
    }

    @Override
    public Jcl visitEndifName(JCLParser.EndifNameContext ctx) {
        Jcl.Identifier id = createIdentifier(ctx.JCL_ENDIF().getText());
        if (ctx.jclCommentArea() != null) {
            id = id.withMarkers(id.getMarkers().addIfAbsent(mapCommentArea(ctx.jclCommentArea())));
        }
        return id;
    }

    @Override
    public Jcl visitExecStatement(JCLParser.ExecStatementContext ctx) {
        return new Jcl.ExecStatement(
                randomId(),
                sourceBefore("//"),
                Markers.EMPTY,
                visitNullable(ctx.jclName()),
                (Name) visit(ctx.execName()),
                createParameters(ctx.JCL_COMMA_CHAR(), ctx.parameterArgument())
        );
    }

    @Override
    public Jcl visitExecName(JCLParser.ExecNameContext ctx) {
        Jcl.Identifier id = createIdentifier(ctx.JCL_EXEC().getText());
        if (ctx.jclCommentArea() != null) {
            id = id.withMarkers(id.getMarkers().addIfAbsent(mapCommentArea(ctx.jclCommentArea())));
        }
        return id;
    }

    @Override
    public Jcl visitExportStatement(JCLParser.ExportStatementContext ctx) {
        return new Jcl.ExportStatement(
                randomId(),
                sourceBefore("//"),
                Markers.EMPTY,
                visitNullable(ctx.jclName()),
                (Name) visit(ctx.exportName()),
                createParameters(ctx.JCL_COMMA_CHAR(), ctx.parameterArgument())
        );
    }

    @Override
    public Jcl visitExportName(JCLParser.ExportNameContext ctx) {
        Jcl.Identifier id = createIdentifier(ctx.JCL_EXPORT().getText());
        if (ctx.jclCommentArea() != null) {
            id = id.withMarkers(id.getMarkers().addIfAbsent(mapCommentArea(ctx.jclCommentArea())));
        }
        return id;
    }

    @Override
    public Jcl visitIfStatement(JCLParser.IfStatementContext ctx) {
        Space prefix = sourceBefore("//");
        Name step = visitNullable(ctx.jclName());
        Name name = (Name) visit(ctx.ifName());
        List<Name> condition = new ArrayList<>(ctx.IF_CONDITION_TEXT().size());
        for (TerminalNode conditionText : ctx.IF_CONDITION_TEXT()) {
            condition.add((Name) visit(conditionText));
        }

        return new Jcl.IfStatement(
                randomId(),
                prefix,
                Markers.EMPTY,
                step,
                name,
                condition,
                (Name) visit(ctx.thenName()),
                createStatements(ctx.statement()),
                visitNullable(ctx.elseStatement()),
                (Jcl.IfStatement.EndIfStatement) visit(ctx.endifStatement())
        );
    }

    @Override
    public Jcl visitIfName(JCLParser.IfNameContext ctx) {
        Jcl.Identifier id = createIdentifier(ctx.JCL_IF().getText());
        if (ctx.jclCommentArea() != null) {
            id = id.withMarkers(id.getMarkers().addIfAbsent(mapCommentArea(ctx.jclCommentArea())));
        }
        return id;
    }

    @Override
    public Jcl visitThenName(JCLParser.ThenNameContext ctx) {
        Jcl.Identifier id = createIdentifier(ctx.IF_CONDITION_THEN().getText());
        if (ctx.jclCommentArea() != null) {
            id = id.withMarkers(id.getMarkers().addIfAbsent(mapCommentArea(ctx.jclCommentArea())));
        }
        return id;
    }

    @Override
    public Jcl visitIncludeStatement(JCLParser.IncludeStatementContext ctx) {
        return new Jcl.IncludeStatement(
                randomId(),
                sourceBefore("//"),
                Markers.EMPTY,
                visitNullable(ctx.jclName()),
                (Name) visit(ctx.includeName()),
                createParameters(ctx.JCL_COMMA_CHAR(), ctx.parameterArgument())
        );
    }

    @Override
    public Jcl visitIncludeName(JCLParser.IncludeNameContext ctx) {
        Jcl.Identifier id = createIdentifier(ctx.JCL_INCLUDE().getText());
        if (ctx.jclCommentArea() != null) {
            id = id.withMarkers(id.getMarkers().addIfAbsent(mapCommentArea(ctx.jclCommentArea())));
        }
        return id;
    }

    @Override
    public Jcl visitJclName(JCLParser.JclNameContext ctx) {
        if (ctx.JCL_CONT() != null) {
            skip("//");
        }
        Jcl jcl = visit(ctx.JCL_PARAMETER(), ctx.JCL_NAME_FIELD(), ctx.jclKeyword());
        if (ctx.JCL_CONT() != null) {
            jcl = jcl.withMarkers(jcl.getMarkers().addIfAbsent(new Continuation(randomId())));
        }
        if (ctx.jclCommentArea() != null) {
            jcl = jcl.withMarkers(jcl.getMarkers().addIfAbsent(mapCommentArea(ctx.jclCommentArea())));
        }
        return jcl;
    }

    @Override
    public Jcl visitJclLibStatement(JCLParser.JclLibStatementContext ctx) {
        return new Jcl.JobStatement(
                randomId(),
                sourceBefore("//"),
                Markers.EMPTY,
                visitNullable(ctx.jclName()),
                (Name) visit(ctx.jclLibName()),
                createParameters(ctx.JCL_COMMA_CHAR(), ctx.parameterArgument())
        );
    }

    @Override
    public Jcl visitJclLibName(JCLParser.JclLibNameContext ctx) {
        Jcl.Identifier id = createIdentifier(ctx.JCL_JCLLIB().getText());
        if (ctx.jclCommentArea() != null) {
            id = id.withMarkers(id.getMarkers().addIfAbsent(mapCommentArea(ctx.jclCommentArea())));
        }
        return id;
    }

    @Override
    public Jcl visitJclWord(JCLParser.JclWordContext ctx) {
        if (ctx.JCL_CONT() != null) {
            skip("//");
        }
        Jcl jcl = visit(ctx.JCL_STRINGLITERAL(), ctx.jclName());
        if (ctx.JCL_CONT() != null) {
            jcl = jcl.withMarkers(jcl.getMarkers().addIfAbsent(new Continuation(randomId())));
        }
        if (ctx.jclCommentArea() != null) {
            jcl = jcl.withMarkers(jcl.getMarkers().addIfAbsent(mapCommentArea(ctx.jclCommentArea())));
        }
        return jcl;
    }

    @Override
    public Jcl visitJobStatement(JCLParser.JobStatementContext ctx) {
        return new Jcl.JobStatement(
                randomId(),
                sourceBefore("//"),
                Markers.EMPTY,
                visitNullable(ctx.jclName()),
                (Name) visit(ctx.jobName()),
                createParameters(ctx.JCL_COMMA_CHAR(), ctx.parameterArgument())
        );
    }

    @Override
    public Jcl visitJobName(JCLParser.JobNameContext ctx) {
        Jcl.Identifier id = createIdentifier(ctx.JCL_JOB().getText());
        if (ctx.jclCommentArea() != null) {
            id = id.withMarkers(id.getMarkers().addIfAbsent(mapCommentArea(ctx.jclCommentArea())));
        }
        return id;
    }

    @Override
    public Jcl visitName(JCLParser.NameContext ctx) {
        return new Jcl.JclName(
                randomId(),
                whitespace(),
                Markers.EMPTY,
                (Jcl.Word) visit(ctx.jclWord()),
                visitNullable(ctx.parameterParentheses())
        );
    }

    @Override
    public Jcl visitOutputStatement(JCLParser.OutputStatementContext ctx) {
        return new Jcl.OutputStatement(
                randomId(),
                sourceBefore("//"),
                Markers.EMPTY,
                visitNullable(ctx.jclName()),
                (Name) visit(ctx.outputName()),
                createParameters(ctx.JCL_COMMA_CHAR(), ctx.parameterArgument())
        );
    }

    @Override
    public Jcl visitOutputName(JCLParser.OutputNameContext ctx) {
        Jcl.Identifier id = createIdentifier(ctx.JCL_OUTPUT().getText());
        if (ctx.jclCommentArea() != null) {
            id = id.withMarkers(id.getMarkers().addIfAbsent(mapCommentArea(ctx.jclCommentArea())));
        }
        return id;
    }

    @Override
    public Jcl visitParameter(JCLParser.ParameterContext ctx) {
        Jcl jcl = visit(ctx.name(), ctx.parameterAssignment(), ctx.parameterParentheses());
        if (ctx.jclTrailingComment() != null) {
            for (Marker marker : mapTrailingComment(ctx.jclTrailingComment())) {
                jcl = jcl.withMarkers(jcl.getMarkers().addIfAbsent(marker));
            }
        }
        return jcl;
    }

    @Override
    public Jcl visitParameterAssignment(JCLParser.ParameterAssignmentContext ctx) {
        return new Jcl.Assignment(
                randomId(),
                whitespace(),
                Markers.EMPTY,
                (Jcl.Word) visit(ctx.jclName()),
                padLeft(sourceBefore("="), (Parameter) visit(ctx.parameter()))
        );
    }

    @Override
    public Jcl visitParameterParentheses(JCLParser.ParameterParenthesesContext ctx) {
        Space prefix = whitespace();
        Markers markers = Markers.EMPTY;

        Markers container = Markers.EMPTY;
        if (ctx.JCL_CONT() != null) {
            skip("//");
            container = container.addIfAbsent(new Continuation(randomId()));
        }
        Space before = sourceBefore("(");
        List<JclRightPadded<Jcl>> padded = new ArrayList<>(ctx.parameterArgument().size());
        if (ctx.JCL_COMMA_CHAR() != null) {
            skip(",");
            container = container.addIfAbsent(new OmitFirstParam(randomId()));
        }

        for (int i = 0; i < ctx.parameterArgument().size(); i++) {
            JCLParser.ParameterArgumentContext parameterArgument = ctx.parameterArgument().get(i);
            Jcl tree = visit(parameterArgument.parameter());
            Marker marker = null;
            JclRightPadded<Jcl> p = padRight(tree, i < ctx.parameterArgument().size() - 1 ?
                    sourceBefore(",") : sourceBefore(")"));
            if (parameterArgument.jclCommentArea() != null) {
                marker = mapCommentArea(parameterArgument.jclCommentArea());
            }
            padded.add(marker == null ? p : p.withMarkers(p.getMarkers().addIfAbsent(marker)));
        }
        if (ctx.jclCommentArea() != null) {
            markers = markers.addIfAbsent(mapCommentArea(ctx.jclCommentArea()));
        }
        return new Jcl.Parentheses<>(
                randomId(),
                prefix,
                markers,
                JclContainer.build(before, padded, container)
        );
    }

    @Override
    public Jcl visitPendStatement(JCLParser.PendStatementContext ctx) {
        return new Jcl.PendStatement(
                randomId(),
                sourceBefore("//"),
                Markers.EMPTY,
                visitNullable(ctx.jclName()),
                (Name) visit(ctx.pendName())
        );
    }

    @Override
    public Jcl visitPendName(JCLParser.PendNameContext ctx) {
        Jcl.Identifier id = createIdentifier(ctx.JCL_PEND().getText());
        if (ctx.jclCommentArea() != null) {
            id = id.withMarkers(id.getMarkers().addIfAbsent(mapCommentArea(ctx.jclCommentArea())));
        }
        return id;
    }

    @Override
    public Jcl visitProcStatement(JCLParser.ProcStatementContext ctx) {
        return new Jcl.ProcStatement(
                randomId(),
                sourceBefore("//"),
                Markers.EMPTY,
                visitNullable(ctx.jclName()),
                (Name) visit(ctx.procName()),
                createParameters(ctx.JCL_COMMA_CHAR(), ctx.parameterArgument())
        );
    }

    @Override
    public Jcl visitProcName(JCLParser.ProcNameContext ctx) {
        Jcl.Identifier id = createIdentifier(ctx.JCL_PROC().getText());
        if (ctx.jclCommentArea() != null) {
            id = id.withMarkers(id.getMarkers().addIfAbsent(mapCommentArea(ctx.jclCommentArea())));
        }
        return id;
    }

    @Override
    public Jcl visitSetStatement(JCLParser.SetStatementContext ctx) {
        return new Jcl.SetStatement(
                randomId(),
                sourceBefore("//"),
                Markers.EMPTY,
                visitNullable(ctx.jclName()),
                (Name) visit(ctx.setName()),
                createParameters(ctx.JCL_COMMA_CHAR(), ctx.parameterArgument())
        );
    }

    @Override
    public Jcl visitSetName(JCLParser.SetNameContext ctx) {
        Jcl.Identifier id = createIdentifier(ctx.JCL_SET().getText());
        if (ctx.jclCommentArea() != null) {
            id = id.withMarkers(id.getMarkers().addIfAbsent(mapCommentArea(ctx.jclCommentArea())));
        }
        return id;
    }

    @Override
    public Jcl visitXmitStatement(JCLParser.XmitStatementContext ctx) {
        return new Jcl.XmitStatement(
                randomId(),
                sourceBefore("//"),
                Markers.EMPTY,
                visitNullable(ctx.jclName()),
                (Name) visit(ctx.xmitName()),
                createParameters(ctx.JCL_COMMA_CHAR(), ctx.parameterArgument())
        );
    }

    @Override
    public Jcl visitXmitName(JCLParser.XmitNameContext ctx) {
        Jcl.Identifier id = createIdentifier(ctx.JCL_XMIT().getText());
        if (ctx.jclCommentArea() != null) {
            id = id.withMarkers(id.getMarkers().addIfAbsent(mapCommentArea(ctx.jclCommentArea())));
        }
        return id;
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

    private Jcl.Identifier createIdentifier(String name) {
        Space prefix = whitespace();
        skip(name);
        return new Jcl.Identifier(
                randomId(),
                prefix,
                Markers.EMPTY,
                name
        );
    }

    private CommentArea mapCommentArea(ParserRuleContext ctx) {
        return new CommentArea(
                randomId(),
                sourceBefore(ctx.getChild(ctx.getChildCount() - 1).getText()),
                ctx.getChild(ctx.getChildCount() - 1).getText()
        );
    }

    private List<Marker> mapTrailingComment(JCLParser.JclTrailingCommentContext ctx) {
        List<Marker> markers = new ArrayList<>(1 + (ctx.jclCommentArea() == null ? 0 : 1));
        Space prefix = whitespace();
        StringBuilder trailingComment = new StringBuilder();
        for (TerminalNode word : ctx.TRAILING_COMMENT_TEXT()) {
            trailingComment.append(sourceBefore(word.getText()).getWhitespace());
            trailingComment.append(word.getText());
        }
        markers.add(new TrailingComment(
                randomId(),
                prefix,
                trailingComment.toString()
        ));
        if (ctx.jclCommentArea() != null) {
            markers.add(mapCommentArea(ctx.jclCommentArea()));
        }
        return markers;
    }

    private JclContainer<Parameter> createParameters(@Nullable TerminalNode leadingComma, List<JCLParser.ParameterArgumentContext> parameters) {
        Space before = whitespace();
        Markers markers = Markers.EMPTY;
        if (leadingComma != null) {
            skip(",");
            markers = markers.addIfAbsent(new OmitFirstParam(randomId()));
        }
        return JclContainer.build(before, parameters.isEmpty() ? emptyList() :
                convertAll(parameters, commaDelim, t -> EMPTY), markers);
    }

    private final Function<ParseTree, Space> commaDelim = ignored -> sourceBefore(",");
    private final Function<ParseTree, Space> noDelim = ignored -> EMPTY;

    private List<Statement> createStatements(List<JCLParser.StatementContext> statements) {
        List<Statement> converted = new ArrayList<>(statements.size());
        for (JCLParser.StatementContext statement : statements) {
            converted.add((Statement) visitStatement(statement));
        }
        return converted;
    }

    private <J2 extends Jcl> List<JclRightPadded<J2>> convertAll(List<? extends ParserRuleContext> elements,
                                                                 Function<ParseTree, Space> innerSuffix,
                                                                 Function<ParseTree, Space> suffix) {
        if (elements.isEmpty()) {
            return emptyList();
        }

        List<JclRightPadded<J2>> converted = new ArrayList<>(elements.size());
        for (int i = 0; i < elements.size(); i++) {
            ParseTree element = elements.get(i);
            if (element instanceof JCLParser.ParameterArgumentContext) {
                JCLParser.ParameterArgumentContext p = (JCLParser.ParameterArgumentContext) element;
                //noinspection unchecked
                J2 j = (J2) visit(p.parameter());
                Space after = i == elements.size() - 1 ? suffix.apply(element) : innerSuffix.apply(element);
                Markers markers = Markers.EMPTY;
                if (p.jclCommentArea() != null) {
                    markers = markers.addIfAbsent(mapCommentArea(p.jclCommentArea()));
                }
                JclRightPadded<J2> rightPadded = padRight(j, after).withMarkers(markers);
                converted.add(rightPadded);
            } else {
                //noinspection unchecked
                J2 j = (J2) visit(element);
                Space after = i == elements.size() - 1 ? suffix.apply(element) : innerSuffix.apply(element);
                JclRightPadded<J2> rightPadded = padRight(j, after);
                converted.add(rightPadded);
            }
        }
        return converted.isEmpty() ? emptyList() : converted;
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

    private <T> JclLeftPadded<T> padLeft(Space left, T tree) {
        return new JclLeftPadded<>(left, tree, Markers.EMPTY);
    }

    @SuppressWarnings("SameParameterValue")
    private <T> JclRightPadded<T> padRight(T tree, @Nullable Space right) {
        return new JclRightPadded<>(tree, right == null ? EMPTY : right, Markers.EMPTY);
    }
}
