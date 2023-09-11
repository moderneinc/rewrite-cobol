/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.jcl;

import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.jcl.tree.*;

public class JclVisitor<P> extends TreeVisitor<Jcl, P> {

    public Jcl visitCompilationUnit(Jcl.CompilationUnit compilationUnit, P p) {
        Jcl.CompilationUnit c = compilationUnit;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.COMPILATION_UNIT_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withStatements(ListUtils.map(c.getStatements(), e -> visitAndCast(e, p)));
        c = c.withEof(visitSpace(c.getEof(), Space.Location.COMPILATION_UNIT_EOF, p));
        return c;
    }

    public Jcl visitComment(Jcl.Comment comment, P p) {
        Jcl.Comment c = comment;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.COMMENT_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withWord(visitAndCast(c.getWord(), p));
        return c;
    }

    public Jcl visitControlM(Jcl.ControlM controlM, P p) {
        Jcl.ControlM c = controlM;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.CONTROL_M_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withWord(visitAndCast(c.getWord(), p));
        return c;
    }

    public Jcl visitDataDefinitionStream(Jcl.DataDefinitionStream ddStream, P p) {
        Jcl.DataDefinitionStream d = ddStream;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DATA_DEFINITION_STREAM_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        d = d.withWord(visitAndCast(d.getWord(), p));
        return d;
    }

    public Jcl visitJclStatement(Jcl.JclStatement jclStatement, P p) {
        Jcl.JclStatement j = jclStatement;
        j = j.withPrefix(visitSpace(j.getPrefix(), Space.Location.JES2_PREFIX, p));
        j = j.withMarkers(visitMarkers(j.getMarkers(), p));
        j = j.withWord(visitAndCast(j.getWord(), p));
        return j;
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
}
