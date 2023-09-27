/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.jcl;

import org.openrewrite.jcl.tree.Jcl;

public class JclIsoVisitor<P> extends JclVisitor<P> {

    @Override
    public Jcl.CompilationUnit visitCompilationUnit(Jcl.CompilationUnit compilationUnit, P p) {
        return (Jcl.CompilationUnit) super.visitCompilationUnit(compilationUnit, p);
    }

    @Override
    public Jcl.Assignment visitAssignment(Jcl.Assignment assignment, P p) {
        return (Jcl.Assignment) super.visitAssignment(assignment, p);
    }

    @Override
    public Jcl.Comment visitComment(Jcl.Comment comment, P p) {
        return (Jcl.Comment) super.visitComment(comment, p);
    }

    @Override
    public Jcl.ControlM visitControlM(Jcl.ControlM controlM, P p) {
        return (Jcl.ControlM) super.visitControlM(controlM, p);
    }

    @Override
    public Jcl.CntlStatement visitCntlStatement(Jcl.CntlStatement cntlStatement, P p) {
        return (Jcl.CntlStatement) super.visitCntlStatement(cntlStatement, p);
    }

    @Override
    public Jcl.DataDefinitionStatement visitDataDefinitionStatement(Jcl.DataDefinitionStatement dataDefinitionStatement, P p) {
        return (Jcl.DataDefinitionStatement) super.visitDataDefinitionStatement(dataDefinitionStatement, p);
    }

    @Override
    public Jcl.DataDefinitionStream visitDataDefinitionStream(Jcl.DataDefinitionStream ddStream, P p) {
        return (Jcl.DataDefinitionStream) super.visitDataDefinitionStream(ddStream, p);
    }

    @Override
    public Jcl.IfStatement.ElseStatement visitElseStatement(Jcl.IfStatement.ElseStatement elseStatement, P p) {
        return (Jcl.IfStatement.ElseStatement) super.visitElseStatement(elseStatement, p);
    }

    @Override
    public Jcl.EndCntlStatement visitEndCntlStatement(Jcl.EndCntlStatement endCntlStatement, P p) {
        return (Jcl.EndCntlStatement) super.visitEndCntlStatement(endCntlStatement, p);
    }

    @Override
    public Jcl.IfStatement.EndIfStatement visitEndIfStatement(Jcl.IfStatement.EndIfStatement endIfStatement, P p) {
        return (Jcl.IfStatement.EndIfStatement) super.visitEndIfStatement(endIfStatement, p);
    }

    @Override
    public Jcl.ExecStatement visitExecStatement(Jcl.ExecStatement execStatement, P p) {
        return (Jcl.ExecStatement) super.visitExecStatement(execStatement, p);
    }

    @Override
    public Jcl.ExportStatement visitExportStatement(Jcl.ExportStatement exportStatement, P p) {
        return (Jcl.ExportStatement) super.visitExportStatement(exportStatement, p);
    }

    @Override
    public Jcl.Identifier visitIdentifier(Jcl.Identifier identifier, P p) {
        return (Jcl.Identifier) super.visitIdentifier(identifier, p);
    }

    @Override
    public Jcl.IfStatement visitIfStatement(Jcl.IfStatement ifStatement, P p) {
        return (Jcl.IfStatement) super.visitIfStatement(ifStatement, p);
    }

    @Override
    public Jcl.IncludeStatement visitIncludeStatement(Jcl.IncludeStatement includeStatement, P p) {
        return (Jcl.IncludeStatement) super.visitIncludeStatement(includeStatement, p);
    }

    @Override
    public Jcl.JclLibStatement visitJclLibStatement(Jcl.JclLibStatement jclLibStatement, P p) {
        return (Jcl.JclLibStatement) super.visitJclLibStatement(jclLibStatement, p);
    }

    @Override
    public Jcl.JclName visitJclName(Jcl.JclName jclName, P p) {
        return (Jcl.JclName) super.visitJclName(jclName, p);
    }

    @Override
    public Jcl.JobStatement visitJobStatement(Jcl.JobStatement jobStatement, P p) {
        return (Jcl.JobStatement) super.visitJobStatement(jobStatement, p);
    }

    @Override
    public Jcl.Literal visitLiteral(Jcl.Literal literal, P p) {
        return (Jcl.Literal) super.visitLiteral(literal, p);
    }

    @Override
    public Jcl.OutputStatement visitOutputStatement(Jcl.OutputStatement outputStatement, P p) {
        return (Jcl.OutputStatement) super.visitOutputStatement(outputStatement, p);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Jcl> Jcl.Parentheses<T> visitParentheses(Jcl.Parentheses<T> parentheses, P p) {
        return (Jcl.Parentheses<T>) super.visitParentheses(parentheses, p);
    }

    @Override
    public Jcl.PendStatement visitPendStatement(Jcl.PendStatement pendStatement, P p) {
        return (Jcl.PendStatement) super.visitPendStatement(pendStatement, p);
    }

    @Override
    public Jcl.ProcStatement visitProcStatement(Jcl.ProcStatement procStatement, P p) {
        return (Jcl.ProcStatement) super.visitProcStatement(procStatement, p);
    }

    @Override
    public Jcl.SetStatement visitSetStatement(Jcl.SetStatement setStatement, P p) {
        return (Jcl.SetStatement) super.visitSetStatement(setStatement, p);
    }

    @Override
    public Jcl.XmitStatement visitXmitStatement(Jcl.XmitStatement xmitStatement, P p) {
        return (Jcl.XmitStatement) super.visitXmitStatement(xmitStatement, p);
    }

    @Override
    public Jcl.Jes2 visitJes2(Jcl.Jes2 jes2, P p) {
        return (Jcl.Jes2) super.visitJes2(jes2, p);
    }

    @Override
    public Jcl.Jes3 visitJes3(Jcl.Jes3 jes3, P p) {
        return (Jcl.Jes3) super.visitJes3(jes3, p);
    }

    @Override
    public Jcl.Unknown visitUnknown(Jcl.Unknown unknown, P p) {
        return (Jcl.Unknown) super.visitUnknown(unknown, p);
    }

    @Override
    public Jcl.Word visitWord(Jcl.Word word, P p) {
        return (Jcl.Word) super.visitWord(word, p);
    }
}
