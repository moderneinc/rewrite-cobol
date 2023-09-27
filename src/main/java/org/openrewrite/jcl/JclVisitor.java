/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.jcl;

import org.openrewrite.Cursor;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.jcl.tree.*;
import org.openrewrite.marker.Markers;

import java.util.List;

public class JclVisitor<P> extends TreeVisitor<Jcl, P> {

    public Jcl visitCompilationUnit(Jcl.CompilationUnit compilationUnit, P p) {
        Jcl.CompilationUnit c = compilationUnit;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.COMPILATION_UNIT_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withStatements(ListUtils.map(c.getStatements(), e -> visitAndCast(e, p)));
        c = c.withEof(visitSpace(c.getEof(), Space.Location.COMPILATION_UNIT_EOF, p));
        return c;
    }

    public Jcl visitAssignment(Jcl.Assignment assignment, P p ) {
        Jcl.Assignment a = assignment;
        a = a.withPrefix(visitSpace(a.getPrefix(), Space.Location.ASSIGNMENT_PREFIX, p));
        a = a.withMarkers(visitMarkers(a.getMarkers(), p));
        a = a.withVariable(visitAndCast(a.getVariable(), p));
        a = a.getPadding().withAssignment(visitLeftPadded(a.getPadding().getAssignment(), JclLeftPadded.Location.ASSIGNMENT, p));
        return a;
    }

    public Jcl visitCntlStatement(Jcl.CntlStatement cntlStatement, P p ) {
        Jcl.CntlStatement c = cntlStatement;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.CNTL_STATEMENT_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withStep(visitAndCast(c.getStep(), p));
        c = c.withName(visitAndCast(c.getName(), p));
        return c;
    }

    public Jcl visitComment(Jcl.Comment comment, P p) {
        Jcl.Comment c = comment;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.COMMENT_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withWord(visitAndCast(c.getWord(), p));
        return c;
    }

    public Jcl visitDataDefinitionStatement(Jcl.DataDefinitionStatement dataDefinitionStatement, P p) {
        Jcl.DataDefinitionStatement d = dataDefinitionStatement;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DATA_DEFINITION_STATEMENT_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        d = d.withStep(visitAndCast(d.getStep(), p));
        d = d.withName(visitAndCast(d.getName(), p));
        d = d.getPadding().withParameters(visitContainer(d.getPadding().getParameters(), JclContainer.Location.PARAMETERS, p));
        return d;
    }

    public Jcl visitDataDefinitionStream(Jcl.DataDefinitionStream ddStream, P p) {
        Jcl.DataDefinitionStream d = ddStream;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DATA_DEFINITION_STREAM_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        d = d.withStep(visitAndCast(d.getStep(), p));
        d = d.withName(visitAndCast(d.getName(), p));
        d = d.withParameter(visitAndCast(d.getParameter(), p));
        d = d.getPadding().withStreamParameters(ListUtils.map(d.getPadding().getStreamParameters(), t -> visitRightPadded(t, JclRightPadded.Location.PARAMETERS, p)));
        return d;
    }

    public Jcl visitElseStatement(Jcl.IfStatement.ElseStatement elseStatement, P p) {
        Jcl.IfStatement.ElseStatement e = elseStatement;
        e = e.withPrefix(visitSpace(e.getPrefix(), Space.Location.ELSE_STATEMENT_PREFIX, p));
        e = e.withMarkers(visitMarkers(e.getMarkers(), p));
        e = e.withStep(visitAndCast(e.getStep(), p));
        e = e.withName(visitAndCast(e.getName(), p));
        e = e.withStatements(ListUtils.map(e.getStatements(), s -> visitAndCast(s, p)));
        return e;
    }

    public Jcl visitEndCntlStatement(Jcl.EndCntlStatement endCntlStatement, P p) {
        Jcl.EndCntlStatement e = endCntlStatement;
        e = e.withPrefix(visitSpace(e.getPrefix(), Space.Location.END_CNTL_STATEMENT_PREFIX, p));
        e = e.withMarkers(visitMarkers(e.getMarkers(), p));
        e = e.withStep(visitAndCast(e.getStep(), p));
        e = e.withName(visitAndCast(e.getName(), p));
        return e;
    }

    public Jcl visitEndIfStatement(Jcl.IfStatement.EndIfStatement endIfStatement, P p) {
        Jcl.IfStatement.EndIfStatement e = endIfStatement;
        e = e.withPrefix(visitSpace(e.getPrefix(), Space.Location.END_IF_STATEMENT_PREFIX, p));
        e = e.withMarkers(visitMarkers(e.getMarkers(), p));
        e = e.withStep(visitAndCast(e.getStep(), p));
        e = e.withName(visitAndCast(e.getName(), p));
        return e;
    }

    public Jcl visitExecStatement(Jcl.ExecStatement execStatement, P p) {
        Jcl.ExecStatement e = execStatement;
        e = e.withPrefix(visitSpace(e.getPrefix(), Space.Location.EXEC_STATEMENT_PREFIX, p));
        e = e.withMarkers(visitMarkers(e.getMarkers(), p));
        e = e.withStep(visitAndCast(e.getStep(), p));
        e = e.withName(visitAndCast(e.getName(), p));
        e = e.getPadding().withParameters(visitContainer(e.getPadding().getParameters(), JclContainer.Location.PARAMETERS, p));
        return e;
    }

    public Jcl visitExportStatement(Jcl.ExportStatement exportStatement, P p) {
        Jcl.ExportStatement e = exportStatement;
        e = e.withPrefix(visitSpace(e.getPrefix(), Space.Location.EXPORT_STATEMENT_PREFIX, p));
        e = e.withMarkers(visitMarkers(e.getMarkers(), p));
        e = e.withStep(visitAndCast(e.getStep(), p));
        e = e.withName(visitAndCast(e.getName(), p));
        e = e.getPadding().withParameters(visitContainer(e.getPadding().getParameters(), JclContainer.Location.PARAMETERS, p));
        return e;
    }

    public Jcl visitIdentifier(Jcl.Identifier identifier, P p) {
        Jcl.Identifier i = identifier;
        i = i.withPrefix(visitSpace(i.getPrefix(), Space.Location.IDENTIFIER_PREFIX, p));
        i = i.withMarkers(visitMarkers(i.getMarkers(), p));
        return i;
    }

    public Jcl visitIfStatement(Jcl.IfStatement ifStatement, P p) {
        Jcl.IfStatement i = ifStatement;
        i = i.withPrefix(visitSpace(i.getPrefix(), Space.Location.IF_STATEMENT_PREFIX, p));
        i = i.withMarkers(visitMarkers(i.getMarkers(), p));
        i = i.withStep(visitAndCast(i.getStep(), p));
        i = i.withName(visitAndCast(i.getName(), p));
        i = i.withCondition(ListUtils.map(i.getCondition(), c -> visitAndCast(c, p)));
        i = i.withThenWord(visitAndCast(i.getThenWord(), p));
        i = i.withStatements(ListUtils.map(i.getStatements(), s -> visitAndCast(s, p)));
        i = i.withElseStatement(visitAndCast(i.getElseStatement(), p));
        i = i.withEndIfStatement(visitAndCast(i.getEndIfStatement(), p));
        return i;
    }

    public Jcl visitIncludeStatement(Jcl.IncludeStatement includeStatement, P p) {
        Jcl.IncludeStatement i = includeStatement;
        i = i.withPrefix(visitSpace(i.getPrefix(), Space.Location.INCLUDE_STATEMENT_PREFIX, p));
        i = i.withMarkers(visitMarkers(i.getMarkers(), p));
        i = i.withStep(visitAndCast(i.getStep(), p));
        i = i.withName(visitAndCast(i.getName(), p));
        i = i.getPadding().withParameters(visitContainer(i.getPadding().getParameters(), JclContainer.Location.PARAMETERS, p));
        return i;
    }

    public Jcl visitJclLibStatement(Jcl.JclLibStatement jclLibStatement, P p) {
        Jcl.JclLibStatement j = jclLibStatement;
        j = j.withPrefix(visitSpace(j.getPrefix(), Space.Location.JOB_STATEMENT_PREFIX, p));
        j = j.withMarkers(visitMarkers(j.getMarkers(), p));
        j = j.withStep(visitAndCast(j.getStep(), p));
        j = j.withName(visitAndCast(j.getName(), p));
        j = j.getPadding().withParameters(visitContainer(j.getPadding().getParameters(), JclContainer.Location.PARAMETERS, p));
        return j;
    }

    public Jcl visitJclName (Jcl.JclName jclName, P p) {
        Jcl.JclName j = jclName;
        j = j.withPrefix(visitSpace(j.getPrefix(), Space.Location.JCL_NAME_PREFIX, p));
        j = j.withMarkers(visitMarkers(j.getMarkers(), p));
        j = j.withName(visitAndCast(j.getName(), p));
        j = j.withParentheses(visitAndCast(j.getParentheses(), p));
        return j;
    }

    public Jcl visitJobStatement(Jcl.JobStatement jobStatement, P p) {
        Jcl.JobStatement j = jobStatement;
        j = j.withPrefix(visitSpace(j.getPrefix(), Space.Location.JOB_STATEMENT_PREFIX, p));
        j = j.withMarkers(visitMarkers(j.getMarkers(), p));
        j = j.withStep(visitAndCast(j.getStep(), p));
        j = j.withName(visitAndCast(j.getName(), p));
        j = j.getPadding().withParameters(visitContainer(j.getPadding().getParameters(), JclContainer.Location.PARAMETERS, p));
        return j;
    }

    public Jcl visitLiteral(Jcl.Literal literal, P p) {
        Jcl.Literal l = literal;
        l = l.withPrefix(visitSpace(l.getPrefix(), Space.Location.LITERAL_PREFIX, p));
        l = l.withMarkers(visitMarkers(l.getMarkers(), p));
        return l;
    }

    public Jcl visitOutputStatement(Jcl.OutputStatement outputStatement, P p) {
        Jcl.OutputStatement o = outputStatement;
        o = o.withPrefix(visitSpace(o.getPrefix(), Space.Location.OUTPUT_STATEMENT_PREFIX, p));
        o = o.withMarkers(visitMarkers(o.getMarkers(), p));
        o = o.withStep(visitAndCast(o.getStep(), p));
        o = o.withName(visitAndCast(o.getName(), p));
        o = o.getPadding().withParameters(visitContainer(o.getPadding().getParameters(), JclContainer.Location.PARAMETERS, p));
        return o;
    }

    public <T extends Jcl> Jcl visitParentheses(Jcl.Parentheses<T> parentheses, P p) {
        Jcl.Parentheses<T> pa = parentheses;
        pa = pa.withPrefix(visitSpace(pa.getPrefix(), Space.Location.PARENTHESES_PREFIX, p));
        pa = pa.getPadding().withTrees(ListUtils.map(pa.getPadding().getTrees(), t -> visitRightPadded(t, JclRightPadded.Location.PARENTHESES, p)));
        pa = pa.withMarkers(visitMarkers(pa.getMarkers(), p));
        return pa;
    }

    public Jcl visitPendStatement(Jcl.PendStatement pendStatement, P p) {
        Jcl.PendStatement pe = pendStatement;
        pe = pe.withPrefix(visitSpace(pe.getPrefix(), Space.Location.PEND_STATEMENT_PREFIX, p));
        pe = pe.withMarkers(visitMarkers(pe.getMarkers(), p));
        pe = pe.withStep(visitAndCast(pe.getStep(), p));
        pe = pe.withName(visitAndCast(pe.getName(), p));
        return pe;
    }

    public Jcl visitProcStatement(Jcl.ProcStatement procStatement, P p) {
        Jcl.ProcStatement pr = procStatement;
        pr = pr.withPrefix(visitSpace(pr.getPrefix(), Space.Location.PROC_STATEMENT_PREFIX, p));
        pr = pr.withMarkers(visitMarkers(pr.getMarkers(), p));
        pr = pr.withStep(visitAndCast(pr.getStep(), p));
        pr = pr.withName(visitAndCast(pr.getName(), p));
        pr = pr.getPadding().withParameters(visitContainer(pr.getPadding().getParameters(), JclContainer.Location.PARAMETERS, p));
        return pr;
    }

    public Jcl visitSetStatement(Jcl.SetStatement setStatement, P p) {
        Jcl.SetStatement s = setStatement;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SET_STATEMENT_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withStep(visitAndCast(s.getStep(), p));
        s = s.withName(visitAndCast(s.getName(), p));
        s = s.getPadding().withParameters(visitContainer(s.getPadding().getParameters(), JclContainer.Location.PARAMETERS, p));
        return s;
    }

    public Jcl visitXmitStatement(Jcl.XmitStatement xmitStatement, P p) {
        Jcl.XmitStatement x = xmitStatement;
        x = x.withPrefix(visitSpace(x.getPrefix(), Space.Location.XMIT_STATEMENT_PREFIX, p));
        x = x.withMarkers(visitMarkers(x.getMarkers(), p));
        x = x.withStep(visitAndCast(x.getStep(), p));
        x = x.withName(visitAndCast(x.getName(), p));
        x = x.getPadding().withParameters(visitContainer(x.getPadding().getParameters(), JclContainer.Location.PARAMETERS, p));
        return x;
    }

    public Jcl visitControlM(Jcl.ControlM controlM, P p) {
        Jcl.ControlM c = controlM;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.CONTROL_M_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withWord(visitAndCast(c.getWord(), p));
        return c;
    }

    public Jcl visitJes2(Jcl.Jes2 jes2, P p) {
        Jcl.Jes2 j = jes2;
        j = j.withPrefix(visitSpace(j.getPrefix(), Space.Location.JES2_PREFIX, p));
        j = j.withMarkers(visitMarkers(j.getMarkers(), p));
        j = j.withWord(visitAndCast(j.getWord(), p));
        return j;
    }

    public Jcl visitJes3(Jcl.Jes3 jes3, P p) {
        Jcl.Jes3 j = jes3;
        j = j.withPrefix(visitSpace(j.getPrefix(), Space.Location.JES2_PREFIX, p));
        j = j.withMarkers(visitMarkers(j.getMarkers(), p));
        j = j.withWord(visitAndCast(j.getWord(), p));
        return j;
    }

    public Jcl visitUnknown(Jcl.Unknown unknown, P p) {
        Jcl.Unknown u = unknown;
        u = u.withPrefix(visitSpace(u.getPrefix(), Space.Location.UNKNOWN_PREFIX, p));
        u = u.withMarkers(visitMarkers(u.getMarkers(), p));
        u = u.withWord(visitAndCast(u.getWord(), p));
        return u;
    }

    public Jcl visitWord(Jcl.Word word, P p) {
        Jcl.Word w = word;
        w = w.withPrefix(visitSpace(w.getPrefix(), Space.Location.WORD_PREFIX, p));
        w = w.withMarkers(visitMarkers(w.getMarkers(), p));
        return w;
    }

    public Space visitSpace(Space space, Space.Location location, P p) {
        return space;
    }

    public <J2 extends Jcl> JclContainer<J2> visitContainer(@Nullable JclContainer<J2> container,
                                                            JclContainer.Location loc, P p) {
        if (container == null) {
            //noinspection ConstantConditions
            return null;
        }
        setCursor(new Cursor(getCursor(), container));

        Space before = visitSpace(container.getBefore(), loc.getBeforeLocation(), p);
        List<JclRightPadded<J2>> js = ListUtils.map(container.getPadding().getElements(), t -> visitRightPadded(t, loc.getElementLocation(), p));

        setCursor(getCursor().getParent());

        return js == container.getPadding().getElements() && before == container.getBefore() ?
                container :
                JclContainer.build(before, js, container.getMarkers());
    }

    public <T> JclLeftPadded<T> visitLeftPadded(@Nullable JclLeftPadded<T> left, JclLeftPadded.Location loc, P p) {
        if (left == null) {
            //noinspection ConstantConditions
            return null;
        }

        setCursor(new Cursor(getCursor(), left));

        Space before = visitSpace(left.getBefore(), loc.getBeforeLocation(), p);
        T t = left.getElement();

        if (t instanceof Jcl) {
            //noinspection unchecked
            t = visitAndCast((Jcl) left.getElement(), p);
        }

        setCursor(getCursor().getParent());
        if (t == null) {
            // If nothing changed leave AST node the same
            if (left.getElement() == null && before == left.getBefore()) {
                return left;
            }
            //noinspection ConstantConditions
            return null;
        }

        return (before == left.getBefore() && t == left.getElement()) ? left : new JclLeftPadded<>(before, t, left.getMarkers());
    }

    public <T> JclRightPadded<T> visitRightPadded(@Nullable JclRightPadded<T> right, JclRightPadded.Location loc, P p) {
        if (right == null) {
            //noinspection ConstantConditions
            return null;
        }

        setCursor(new Cursor(getCursor(), right));

        T t = right.getElement();
        if (t instanceof Jcl) {
            //noinspection unchecked
            t = visitAndCast((Jcl) right.getElement(), p);
        }

        setCursor(getCursor().getParent());
        if (t == null) {
            //noinspection ConstantConditions
            return null;
        }

        Space after = visitSpace(right.getAfter(), loc.getAfterLocation(), p);
        Markers markers = visitMarkers(right.getMarkers(), p);
        return (after == right.getAfter() && t == right.getElement() && markers == right.getMarkers()) ?
                right : new JclRightPadded<>(t, after, markers);
    }
}
