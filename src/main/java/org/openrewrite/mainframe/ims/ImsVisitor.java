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
package org.openrewrite.mainframe.ims;

import org.openrewrite.TreeVisitor;
import org.openrewrite.mainframe.ims.tree.Ims;
import org.openrewrite.mainframe.ims.tree.Space;
import org.openrewrite.internal.ListUtils;

public class ImsVisitor<P> extends TreeVisitor<Ims, P> {

    public Ims visitCompilationUnit(Ims.CompilationUnit compilationUnit, P p) {
        Ims.CompilationUnit c = compilationUnit;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.COMPILATION_UNIT_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withStatements(ListUtils.map(c.getStatements(), e -> visitAndCast(e, p)));
        return c.withEof(visitSpace(c.getEof(), Space.Location.COMPILATION_UNIT_EOF, p));
    }

    public Ims visitMacroStatement(Ims.MacroStatement statement, P p) {
        Ims.MacroStatement m = statement;
        m = m.withPrefix(visitSpace(m.getPrefix(), Space.Location.MACRO_STATEMENT_PREFIX, p));
        m = m.withMarkers(visitMarkers(m.getMarkers(), p));
        if (m.getName() != null) {
            m = m.withName(visitAndCast(m.getName(), p));
        }
        m = m.withOperation(visitAndCast(m.getOperation(), p));
        return m.withOperands(ListUtils.map(m.getOperands(), o -> visitAndCast(o, p)));
    }

    public Ims visitComment(Ims.Comment comment, P p) {
        Ims.Comment c = comment;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.COMMENT_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        return c.withWord(visitAndCast(c.getWord(), p));
    }

    public Ims visitUnknown(Ims.Unknown unknown, P p) {
        Ims.Unknown u = unknown;
        u = u.withPrefix(visitSpace(u.getPrefix(), Space.Location.UNKNOWN_PREFIX, p));
        u = u.withMarkers(visitMarkers(u.getMarkers(), p));
        return u.withWord(visitAndCast(u.getWord(), p));
    }

    public Ims visitKeywordOperand(Ims.KeywordOperand operand, P p) {
        Ims.KeywordOperand k = operand;
        k = k.withPrefix(visitSpace(k.getPrefix(), Space.Location.OPERAND_PREFIX, p));
        k = k.withMarkers(visitMarkers(k.getMarkers(), p));
        k = k.withKeyword(visitAndCast(k.getKeyword(), p));
        return k.withValue(ListUtils.map(k.getValue(), w -> visitAndCast(w, p)));
    }

    public Ims visitPositionalOperand(Ims.PositionalOperand operand, P p) {
        Ims.PositionalOperand po = operand;
        po = po.withPrefix(visitSpace(po.getPrefix(), Space.Location.OPERAND_PREFIX, p));
        po = po.withMarkers(visitMarkers(po.getMarkers(), p));
        return po.withValue(ListUtils.map(po.getValue(), w -> visitAndCast(w, p)));
    }

    public Ims visitWord(Ims.Word word, P p) {
        Ims.Word w = word;
        w = w.withPrefix(visitSpace(w.getPrefix(), Space.Location.WORD_PREFIX, p));
        return w.withMarkers(visitMarkers(w.getMarkers(), p));
    }

    public Space visitSpace(Space space, Space.Location location, P p) {
        return space;
    }
}
