/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.controlm;

import org.openrewrite.TreeVisitor;
import org.openrewrite.controlm.tree.ControlM;
import org.openrewrite.controlm.tree.Space;
import org.openrewrite.internal.ListUtils;

public class ControlMVisitor<P> extends TreeVisitor<ControlM, P> {

    public ControlM visitCompilationUnit(ControlM.CompilationUnit compilationUnit, P p) {
        ControlM.CompilationUnit c = compilationUnit;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.COMPILATION_UNIT_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withStatements(ListUtils.map(c.getStatements(), e -> visitAndCast(e, p)));
        c = c.withEof(visitSpace(c.getEof(), Space.Location.COMPILATION_UNIT_EOF, p));
        return c;
    }

    public Space visitSpace(Space space, Space.Location location, P p) {
        return space;
    }
}
