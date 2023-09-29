/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.jcl.internal;

import org.openrewrite.Cursor;
import org.openrewrite.PrintOutputCapture;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.jcl.JclVisitor;
import org.openrewrite.jcl.marker.*;
import org.openrewrite.jcl.tree.*;
import org.openrewrite.marker.Marker;
import org.openrewrite.marker.Markers;

import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

public class JclPrinter<P> extends JclVisitor<PrintOutputCapture<P>> {
    public static final UnaryOperator<String> JCL_MARKER_WRAPPER =
            out -> "~~" + out + (out.isEmpty() ? "" : "~~") + ">";

    @Override
    public Jcl.CompilationUnit visitCompilationUnit(Jcl.CompilationUnit cu, PrintOutputCapture<P> p) {
        beforeSyntax(cu, Space.Location.COMPILATION_UNIT_PREFIX, p);
        visit(cu.getStatements(), p);
        afterSyntax(cu, p);
        visitSpace(cu.getEof(), Space.Location.COMPILATION_UNIT_EOF, p);
        return cu;
    }

    @Override
    public Jcl visitAssignment(Jcl.Assignment assignment, PrintOutputCapture<P> p) {
        beforeSyntax(assignment, Space.Location.ASSIGNMENT_PREFIX, p);
        visit(assignment.getVariable(), p);
        visitLeftPadded("=", assignment.getPadding().getAssignment(), JclLeftPadded.Location.ASSIGNMENT, p);
        afterSyntax(assignment, p);
        return assignment;
    }

    @Override
    public Jcl visitComment(Jcl.Comment comment, PrintOutputCapture<P> p) {
        beforeSyntax(comment, Space.Location.COMMENT_PREFIX, p);
        visit(comment.getWord(), p);
        afterSyntax(comment, p);
        return comment;
    }

    @Override
    public Jcl visitControlM(Jcl.ControlM controlM, PrintOutputCapture<P> p) {
        beforeSyntax(controlM, Space.Location.CONTROL_M_PREFIX, p);
        visit(controlM.getWord(), p);
        afterSyntax(controlM, p);
        return controlM;
    }

    @Override
    public Jcl visitCntlStatement(Jcl.CntlStatement cntlStatement, PrintOutputCapture<P> p) {
        beforeSyntax(cntlStatement, Space.Location.CNTL_STATEMENT_PREFIX, p);
        p.append("//");
        visit(cntlStatement.getStep(), p);
        visit(cntlStatement.getName(), p);
        afterSyntax(cntlStatement, p);
        return cntlStatement;
    }

    @Override
    public Jcl visitDataDefinitionStatement(Jcl.DataDefinitionStatement dataDefinitionStatement, PrintOutputCapture<P> p) {
        beforeSyntax(dataDefinitionStatement, Space.Location.DATA_DEFINITION_STATEMENT_PREFIX, p);
        p.append("//");
        visit(dataDefinitionStatement.getStep(), p);
        visit(dataDefinitionStatement.getName(), p);
        visitContainer("", dataDefinitionStatement.getPadding().getParameters(), JclContainer.Location.PARAMETERS, ",", "", p);
        afterSyntax(dataDefinitionStatement, p);
        return dataDefinitionStatement;
    }

    @Override
    public Jcl visitDataDefinitionStream(Jcl.DataDefinitionStream ddStream, PrintOutputCapture<P> p) {
        beforeSyntax(ddStream, Space.Location.DATA_DEFINITION_STATEMENT_PREFIX, p);
        p.append("//");
        visit(ddStream.getStep(), p);
        visit(ddStream.getName(), p);
        visit(ddStream.getParameter(), p);
        visitRightPadded(ddStream.getPadding().getStreamParameters(), JclRightPadded.Location.PARAMETERS, "", p);
        afterSyntax(ddStream, p);
        return ddStream;
    }

    @Override
    public Jcl visitElseStatement(Jcl.IfStatement.ElseStatement elseStatement, PrintOutputCapture<P> p) {
        beforeSyntax(elseStatement, Space.Location.ELSE_STATEMENT_PREFIX, p);
        p.append("//");
        visit(elseStatement.getStep(), p);
        visit(elseStatement.getName(), p);
        visit(elseStatement.getStatements(), p);
        afterSyntax(elseStatement, p);
        return elseStatement;
    }

    @Override
    public Jcl visitEmptyStatement(Jcl.EmptyStatement emptyStatement, PrintOutputCapture<P> p) {
        beforeSyntax(emptyStatement, Space.Location.EMPTY_STATEMENT_PREFIX, p);
        p.append("//");
        afterSyntax(emptyStatement, p);
        return emptyStatement;
    }

    @Override
    public Jcl visitEndCntlStatement(Jcl.EndCntlStatement endCntlStatement, PrintOutputCapture<P> p) {
        beforeSyntax(endCntlStatement, Space.Location.END_CNTL_STATEMENT_PREFIX, p);
        p.append("//");
        visit(endCntlStatement.getStep(), p);
        visit(endCntlStatement.getName(), p);
        afterSyntax(endCntlStatement, p);
        return endCntlStatement;
    }

    @Override
    public Jcl visitEndIfStatement(Jcl.IfStatement.EndIfStatement endIfStatement, PrintOutputCapture<P> p) {
        beforeSyntax(endIfStatement, Space.Location.END_IF_STATEMENT_PREFIX, p);
        p.append("//");
        visit(endIfStatement.getStep(), p);
        visit(endIfStatement.getName(), p);
        afterSyntax(endIfStatement, p);
        return endIfStatement;
    }

    @Override
    public Jcl visitExecStatement(Jcl.ExecStatement execStatement, PrintOutputCapture<P> p) {
        beforeSyntax(execStatement, Space.Location.EXEC_STATEMENT_PREFIX, p);
        p.append("//");
        visit(execStatement.getStep(), p);
        visit(execStatement.getName(), p);
        visitContainer("", execStatement.getPadding().getParameters(), JclContainer.Location.PARAMETERS, ",", "", p);
        afterSyntax(execStatement, p);
        return execStatement;
    }

    @Override
    public Jcl visitExportStatement(Jcl.ExportStatement exportStatement, PrintOutputCapture<P> p) {
        beforeSyntax(exportStatement, Space.Location.EXPORT_STATEMENT_PREFIX, p);
        p.append("//");
        visit(exportStatement.getStep(), p);
        visit(exportStatement.getName(), p);
        visitContainer("", exportStatement.getPadding().getParameters(), JclContainer.Location.PARAMETERS, ",", "", p);
        afterSyntax(exportStatement, p);
        return exportStatement;
    }

    @Override
    public Jcl visitIdentifier(Jcl.Identifier identifier, PrintOutputCapture<P> p) {
        beforeSyntax(identifier, Space.Location.IDENTIFIER_PREFIX, p);
        p.append(identifier.getSimpleName());
        afterSyntax(identifier, p);
        return identifier;
    }

    @Override
    public Jcl visitIfStatement(Jcl.IfStatement ifStatement, PrintOutputCapture<P> p) {
        beforeSyntax(ifStatement, Space.Location.IF_STATEMENT_PREFIX, p);
        p.append("//");
        visit(ifStatement.getStep(), p);
        visit(ifStatement.getName(), p);
        visit(ifStatement.getCondition(), p);
        visit(ifStatement.getThenWord(), p);
        visit(ifStatement.getStatements(), p);
        visit(ifStatement.getElseStatement(), p);
        visit(ifStatement.getEndIfStatement(), p);
        afterSyntax(ifStatement, p);
        return ifStatement;
    }

    @Override
    public Jcl visitIncludeStatement(Jcl.IncludeStatement includeStatement, PrintOutputCapture<P> p) {
        beforeSyntax(includeStatement, Space.Location.INCLUDE_STATEMENT_PREFIX, p);
        p.append("//");
        visit(includeStatement.getStep(), p);
        visit(includeStatement.getName(), p);
        visitContainer("", includeStatement.getPadding().getParameters(), JclContainer.Location.PARAMETERS, ",", "", p);
        afterSyntax(includeStatement, p);
        return includeStatement;
    }

    @Override
    public Jcl visitJclLibStatement(Jcl.JclLibStatement jclLibStatement, PrintOutputCapture<P> p) {
        beforeSyntax(jclLibStatement, Space.Location.JCL_LIB_STATEMENT_PREFIX, p);
        p.append("//");
        visit(jclLibStatement.getStep(), p);
        visit(jclLibStatement.getName(), p);
        visitContainer("", jclLibStatement.getPadding().getParameters(), JclContainer.Location.PARAMETERS, ",", "", p);
        afterSyntax(jclLibStatement, p);
        return jclLibStatement;
    }

    @Override
    public Jcl visitJclName(Jcl.JclName jclName, PrintOutputCapture<P> p) {
        beforeSyntax(jclName, Space.Location.JCL_NAME_PREFIX, p);
        visit(jclName.getName(), p);
        visit(jclName.getParentheses(), p);
        afterSyntax(jclName, p);
        return jclName;
    }

    @Override
    public Jcl visitJobStatement(Jcl.JobStatement jobStatement, PrintOutputCapture<P> p) {
        beforeSyntax(jobStatement, Space.Location.JOB_STATEMENT_PREFIX, p);
        p.append("//");
        visit(jobStatement.getStep(), p);
        visit(jobStatement.getName(), p);
        visitContainer("", jobStatement.getPadding().getParameters(), JclContainer.Location.PARAMETERS, ",", "", p);
        afterSyntax(jobStatement, p);
        return jobStatement;
    }

    @Override
    public Jcl visitLiteral(Jcl.Literal literal, PrintOutputCapture<P> p) {
        beforeSyntax(literal, Space.Location.LITERAL_PREFIX, p);
        p.append(literal.getValueSource());
        afterSyntax(literal, p);
        return literal;
    }

    @Override
    public Jcl visitOutputStatement(Jcl.OutputStatement outputStatement, PrintOutputCapture<P> p) {
        beforeSyntax(outputStatement, Space.Location.OUTPUT_STATEMENT_PREFIX, p);
        p.append("//");
        visit(outputStatement.getStep(), p);
        visit(outputStatement.getName(), p);
        visitContainer("", outputStatement.getPadding().getParameters(), JclContainer.Location.PARAMETERS, ",", "", p);
        afterSyntax(outputStatement, p);
        return outputStatement;
    }

    @Override
    public <T extends Jcl> Jcl visitParentheses(Jcl.Parentheses<T> parentheses, PrintOutputCapture<P> p) {
        beforeSyntax(parentheses, Space.Location.PARENTHESES_PREFIX, p);
        visitContainer("(", parentheses.getPadding().getTrees(), JclContainer.Location.PARENTHESES, ",", ")", p);
        afterSyntax(parentheses, p);
        return parentheses;
    }

    @Override
    public Jcl visitPendStatement(Jcl.PendStatement pendStatement, PrintOutputCapture<P> p) {
        beforeSyntax(pendStatement, Space.Location.PEND_STATEMENT_PREFIX, p);
        p.append("//");
        visit(pendStatement.getStep(), p);
        visit(pendStatement.getName(), p);
        afterSyntax(pendStatement, p);
        return pendStatement;
    }

    @Override
    public Jcl visitProcStatement(Jcl.ProcStatement procStatement, PrintOutputCapture<P> p) {
        beforeSyntax(procStatement, Space.Location.PROC_STATEMENT_PREFIX, p);
        p.append("//");
        visit(procStatement.getStep(), p);
        visit(procStatement.getName(), p);
        visitContainer("", procStatement.getPadding().getParameters(), JclContainer.Location.PARAMETERS, ",", "", p);
        afterSyntax(procStatement, p);
        return procStatement;
    }

    @Override
    public Jcl visitSetStatement(Jcl.SetStatement setStatement, PrintOutputCapture<P> p) {
        beforeSyntax(setStatement, Space.Location.SET_STATEMENT_PREFIX, p);
        p.append("//");
        visit(setStatement.getStep(), p);
        visit(setStatement.getName(), p);
        visitContainer("", setStatement.getPadding().getParameters(), JclContainer.Location.PARAMETERS, ",", "", p);
        afterSyntax(setStatement, p);
        return setStatement;
    }

    @Override
    public Jcl visitXmitStatement(Jcl.XmitStatement xmitStatement, PrintOutputCapture<P> p) {
        beforeSyntax(xmitStatement, Space.Location.XMIT_STATEMENT_PREFIX, p);
        p.append("//");
        visit(xmitStatement.getStep(), p);
        visit(xmitStatement.getName(), p);
        visitContainer("", xmitStatement.getPadding().getParameters(), JclContainer.Location.PARAMETERS, ",", "", p);
        afterSyntax(xmitStatement, p);
        return xmitStatement;
    }

    @Override
    public Jcl visitJes2(Jcl.Jes2 jes2, PrintOutputCapture<P> p) {
        beforeSyntax(jes2, Space.Location.JES2_PREFIX, p);
        visit(jes2.getWord(), p);
        afterSyntax(jes2, p);
        return jes2;
    }

    @Override
    public Jcl visitJes3(Jcl.Jes3 jes3, PrintOutputCapture<P> p) {
        beforeSyntax(jes3, Space.Location.JES2_PREFIX, p);
        visit(jes3.getWord(), p);
        afterSyntax(jes3, p);
        return jes3;
    }

    @Override
    public Jcl visitUnknown(Jcl.Unknown unknown, PrintOutputCapture<P> p) {
        beforeSyntax(unknown, Space.Location.UNKNOWN_PREFIX, p);
        visit(unknown.getWord(), p);
        afterSyntax(unknown, p);
        return unknown;
    }

    @Override
    public Jcl visitWord(Jcl.Word word, PrintOutputCapture<P> p) {
        beforeSyntax(word, Space.Location.WORD_PREFIX, p);
        p.append(word.getText());
        afterSyntax(word, p);
        return word;
    }

    @Override
    public Space visitSpace(Space space, Space.Location location, PrintOutputCapture<P> p) {
        p.append(space.getWhitespace());
        return space;
    }

    @Override
    public <M extends Marker> M visitMarker(Marker marker, PrintOutputCapture<P> p) {
        if (marker instanceof Comma) {
            p.append(",");
        }
        return super.visitMarker(marker, p);
    }

    protected void visitContainer(String before, @Nullable JclContainer<? extends Jcl> container, JclContainer.Location location,
                                  String suffixBetween, @Nullable String after, PrintOutputCapture<P> p) {
        if (container == null) {
            return;
        }
        beforeSyntax(container.getBefore(), container.getMarkers(), location.getBeforeLocation(), p);
        p.append(before);
        if (container.getMarkers().findFirst(OmitFirstParam.class).isPresent()) {
            p.append(",");
        }
        visitRightPadded(container.getPadding().getElements(), location.getElementLocation(), suffixBetween, p);
        afterSyntax(container.getMarkers(), p);
        p.append(after == null ? "" : after);
    }

    protected void visitLeftPadded(@Nullable String prefix, @Nullable JclLeftPadded<? extends Jcl> leftPadded, JclLeftPadded.Location location, PrintOutputCapture<P> p) {
        if (leftPadded != null) {
            beforeSyntax(leftPadded.getBefore(), leftPadded.getMarkers(), location.getBeforeLocation(), p);
            if (prefix != null) {
                p.append(prefix);
            }
            visit(leftPadded.getElement(), p);
            afterSyntax(leftPadded.getMarkers(), p);
        }
    }

    protected void visitRightPadded(List<? extends JclRightPadded<? extends Jcl>> nodes, JclRightPadded.Location location, String suffixBetween, PrintOutputCapture<P> p) {
        for (int i = 0; i < nodes.size(); i++) {
            JclRightPadded<? extends Jcl> node = nodes.get(i);
            visit(node.getElement(), p);
            visitSpace(node.getAfter(), location.getAfterLocation(), p);
            visitMarkers(node.getMarkers(), p);
            if (i < nodes.size() - 1) {
                p.append(suffixBetween);
            }
            for (Marker marker : node.getMarkers().getMarkers()) {
                if (marker instanceof TrailingComma) {
                    p.append(",");
                } else if (marker instanceof TrailingComment) {
                    TrailingComment tc = (TrailingComment) marker;
                    visitSpace(tc.getPrefix(), Space.Location.TRAILING_COMMENT_PREFIX, p);
                    p.append(tc.getComment());
                } else if (marker instanceof CommentArea) {
                    CommentArea ca = (CommentArea) marker;
                    visitSpace(ca.getPrefix(), Space.Location.COMMENT_AREA_PREFIX, p);
                    p.append(ca.getComment());
                }
            }
        }
    }

    protected void beforeSyntax(Jcl c, Space.Location loc, PrintOutputCapture<P> p) {
        beforeSyntax(c.getPrefix(), c.getMarkers(), loc, p);
    }

    protected void beforeSyntax(Space prefix, Markers markers, @Nullable Space.Location loc, PrintOutputCapture<P> p) {
        for (Marker marker : markers.getMarkers()) {
            p.out.append(p.getMarkerPrinter().beforePrefix(marker, new Cursor(getCursor(), marker), JCL_MARKER_WRAPPER));
            if (marker instanceof Continuation) {
                p.append("//");
            }
        }
        if (loc != null) {
            visitSpace(prefix, loc, p);
        }
        visitMarkers(markers, p);
        for (Marker marker : markers.getMarkers()) {
            p.out.append(p.getMarkerPrinter().beforeSyntax(marker, new Cursor(getCursor(), marker), JCL_MARKER_WRAPPER));
        }
    }

    protected void afterSyntax(Jcl c, PrintOutputCapture<P> p) {
        afterSyntax(c.getMarkers(), p);
    }

    protected void afterSyntax(Markers markers, PrintOutputCapture<P> p) {
        for (Marker marker : markers.getMarkers()) {
            if (marker instanceof TrailingComma) {
                p.append(",");
            } else if (marker instanceof TrailingComment) {
                TrailingComment tc = (TrailingComment) marker;
                visitSpace(tc.getPrefix(), Space.Location.TRAILING_COMMENT_PREFIX, p);
                p.append(tc.getComment());
            } else if (marker instanceof CommentArea) {
                CommentArea ca = (CommentArea) marker;
                visitSpace(ca.getPrefix(), Space.Location.COMMENT_AREA_PREFIX, p);
                p.append(ca.getComment());
            }
            p.out.append(p.getMarkerPrinter().afterSyntax(marker, new Cursor(getCursor(), marker), JCL_MARKER_WRAPPER));
        }
    }
}
