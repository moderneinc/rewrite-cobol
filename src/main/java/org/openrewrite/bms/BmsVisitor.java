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
package org.openrewrite.bms;

import org.openrewrite.TreeVisitor;
import org.openrewrite.bms.tree.Bms;
import org.openrewrite.bms.tree.Space;
import org.openrewrite.internal.ListUtils;

public class BmsVisitor<P> extends TreeVisitor<Bms, P> {

    public Bms visitCompilationUnit(Bms.CompilationUnit compilationUnit, P p) {
        Bms.CompilationUnit c = compilationUnit;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.COMPILATION_UNIT_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withStatements(ListUtils.map(c.getStatements(), e -> visitAndCast(e, p)));
        return c.withEof(visitSpace(c.getEof(), Space.Location.COMPILATION_UNIT_EOF, p));
    }

    public Bms visitMacroStatement(Bms.MacroStatement statement, P p) {
        Bms.MacroStatement m = statement;
        m = m.withPrefix(visitSpace(m.getPrefix(), Space.Location.MACRO_STATEMENT_PREFIX, p));
        m = m.withMarkers(visitMarkers(m.getMarkers(), p));
        if (m.getName() != null) {
            m = m.withName(visitAndCast(m.getName(), p));
        }
        m = m.withOperation(visitAndCast(m.getOperation(), p));
        return m.withOperands(ListUtils.map(m.getOperands(), o -> visitAndCast(o, p)));
    }

    public Bms visitComment(Bms.Comment comment, P p) {
        Bms.Comment c = comment;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.COMMENT_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        return c.withWord(visitAndCast(c.getWord(), p));
    }

    public Bms visitUnknown(Bms.Unknown unknown, P p) {
        Bms.Unknown u = unknown;
        u = u.withPrefix(visitSpace(u.getPrefix(), Space.Location.UNKNOWN_PREFIX, p));
        u = u.withMarkers(visitMarkers(u.getMarkers(), p));
        return u.withWord(visitAndCast(u.getWord(), p));
    }

    public Bms visitKeywordOperand(Bms.KeywordOperand operand, P p) {
        Bms.KeywordOperand k = operand;
        k = k.withPrefix(visitSpace(k.getPrefix(), Space.Location.OPERAND_PREFIX, p));
        k = k.withMarkers(visitMarkers(k.getMarkers(), p));
        k = k.withKeyword(visitAndCast(k.getKeyword(), p));
        return k.withValue(ListUtils.map(k.getValue(), w -> visitAndCast(w, p)));
    }

    public Bms visitPositionalOperand(Bms.PositionalOperand operand, P p) {
        Bms.PositionalOperand po = operand;
        po = po.withPrefix(visitSpace(po.getPrefix(), Space.Location.OPERAND_PREFIX, p));
        po = po.withMarkers(visitMarkers(po.getMarkers(), p));
        return po.withValue(ListUtils.map(po.getValue(), w -> visitAndCast(w, p)));
    }

    public Bms visitWord(Bms.Word word, P p) {
        Bms.Word w = word;
        w = w.withPrefix(visitSpace(w.getPrefix(), Space.Location.WORD_PREFIX, p));
        return w.withMarkers(visitMarkers(w.getMarkers(), p));
    }

    public Space visitSpace(Space space, Space.Location location, P p) {
        return space;
    }
}
