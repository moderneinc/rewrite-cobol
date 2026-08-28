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
package org.openrewrite.mainframe.linkedit;

import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.mainframe.linkedit.tree.LinkEdit;
import org.openrewrite.mainframe.linkedit.tree.Space;

public class LinkEditVisitor<P> extends TreeVisitor<LinkEdit, P> {

    public LinkEdit visitCompilationUnit(LinkEdit.CompilationUnit compilationUnit, P p) {
        LinkEdit.CompilationUnit c = compilationUnit;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.COMPILATION_UNIT_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withStatements(ListUtils.map(c.getStatements(), s -> visitAndCast(s, p)));
        return c.withEof(visitSpace(c.getEof(), Space.Location.COMPILATION_UNIT_EOF, p));
    }

    public LinkEdit visitControlStatement(LinkEdit.ControlStatement controlStatement, P p) {
        LinkEdit.ControlStatement c = controlStatement;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.CONTROL_STATEMENT_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withOperator(visitAndCast(c.getOperator(), p));
        return c.withOperands(ListUtils.map(c.getOperands(), o -> visitAndCast(o, p)));
    }

    public LinkEdit visitOperand(LinkEdit.Operand operand, P p) {
        LinkEdit.Operand o = operand;
        o = o.withPrefix(visitSpace(o.getPrefix(), Space.Location.OPERAND_PREFIX, p));
        o = o.withMarkers(visitMarkers(o.getMarkers(), p));
        o = o.withKeyword(visitAndCast(o.getKeyword(), p));
        return o.withValue(ListUtils.map(o.getValue(), w -> visitAndCast(w, p)));
    }

    public LinkEdit visitWord(LinkEdit.Word word, P p) {
        LinkEdit.Word w = word;
        w = w.withPrefix(visitSpace(w.getPrefix(), Space.Location.WORD_PREFIX, p));
        return w.withMarkers(visitMarkers(w.getMarkers(), p));
    }

    public Space visitSpace(Space space, Space.Location location, P p) {
        return space;
    }
}
