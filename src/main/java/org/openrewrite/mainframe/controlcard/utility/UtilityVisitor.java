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
package org.openrewrite.mainframe.controlcard.utility;

import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.mainframe.controlcard.utility.tree.Space;
import org.openrewrite.mainframe.controlcard.utility.tree.Utility;

public class UtilityVisitor<P> extends TreeVisitor<Utility, P> {

    public Utility visitCompilationUnit(Utility.CompilationUnit compilationUnit, P p) {
        Utility.CompilationUnit c = compilationUnit;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.COMPILATION_UNIT_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withStatements(ListUtils.map(c.getStatements(), s -> visitAndCast(s, p)));
        return c.withEof(visitSpace(c.getEof(), Space.Location.COMPILATION_UNIT_EOF, p));
    }

    public Utility visitBlock(Utility.Block block, P p) {
        Utility.Block b = block;
        b = b.withPrefix(visitSpace(b.getPrefix(), Space.Location.BLOCK_PREFIX, p));
        b = b.withMarkers(visitMarkers(b.getMarkers(), p));
        b = b.withVerb(visitAndCast(b.getVerb(), p));
        b = b.withValue(ListUtils.map(b.getValue(), w -> visitAndCast(w, p)));
        return b.withContents(ListUtils.map(b.getContents(), c -> visitAndCast(c, p)));
    }

    public Utility visitOperand(Utility.Operand operand, P p) {
        Utility.Operand o = operand;
        o = o.withPrefix(visitSpace(o.getPrefix(), Space.Location.OPERAND_PREFIX, p));
        o = o.withMarkers(visitMarkers(o.getMarkers(), p));
        o = o.withKeyword(visitAndCast(o.getKeyword(), p));
        return o.withValue(ListUtils.map(o.getValue(), w -> visitAndCast(w, p)));
    }

    public Utility visitWord(Utility.Word word, P p) {
        Utility.Word w = word;
        w = w.withPrefix(visitSpace(w.getPrefix(), Space.Location.WORD_PREFIX, p));
        return w.withMarkers(visitMarkers(w.getMarkers(), p));
    }

    public Space visitSpace(Space space, Space.Location location, P p) {
        return space;
    }
}
