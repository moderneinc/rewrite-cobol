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
package org.openrewrite.mainframe.assembler;

import org.openrewrite.TreeVisitor;
import org.openrewrite.mainframe.assembler.tree.Assembler;
import org.openrewrite.mainframe.assembler.tree.Space;
import org.openrewrite.internal.ListUtils;

public class AssemblerVisitor<P> extends TreeVisitor<Assembler, P> {

    public Assembler visitCompilationUnit(Assembler.CompilationUnit compilationUnit, P p) {
        Assembler.CompilationUnit c = compilationUnit;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.COMPILATION_UNIT_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withStatements(ListUtils.map(c.getStatements(), e -> visitAndCast(e, p)));
        return c.withEof(visitSpace(c.getEof(), Space.Location.COMPILATION_UNIT_EOF, p));
    }

    public Assembler visitInstruction(Assembler.Instruction instruction, P p) {
        Assembler.Instruction i = instruction;
        i = i.withPrefix(visitSpace(i.getPrefix(), Space.Location.INSTRUCTION_PREFIX, p));
        i = i.withMarkers(visitMarkers(i.getMarkers(), p));
        if (i.getName() != null) {
            i = i.withName(visitAndCast(i.getName(), p));
        }
        i = i.withOperation(visitAndCast(i.getOperation(), p));
        return i.withOperands(ListUtils.map(i.getOperands(), o -> visitAndCast(o, p)));
    }

    public Assembler visitComment(Assembler.Comment comment, P p) {
        Assembler.Comment c = comment;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.COMMENT_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        return c.withParts(ListUtils.map(c.getParts(), part -> visitAndCast(part, p)));
    }

    public Assembler visitUnknown(Assembler.Unknown unknown, P p) {
        Assembler.Unknown u = unknown;
        u = u.withPrefix(visitSpace(u.getPrefix(), Space.Location.UNKNOWN_PREFIX, p));
        u = u.withMarkers(visitMarkers(u.getMarkers(), p));
        return u.withWord(visitAndCast(u.getWord(), p));
    }

    public Assembler visitOperand(Assembler.Operand operand, P p) {
        Assembler.Operand o = operand;
        o = o.withPrefix(visitSpace(o.getPrefix(), Space.Location.OPERAND_PREFIX, p));
        o = o.withMarkers(visitMarkers(o.getMarkers(), p));
        return o.withParts(ListUtils.map(o.getParts(), part -> visitAndCast(part, p)));
    }

    public Assembler visitContinuation(Assembler.Continuation continuation, P p) {
        Assembler.Continuation c = continuation;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.CONTINUATION_PREFIX, p));
        return c.withMarkers(visitMarkers(c.getMarkers(), p));
    }

    public Assembler visitWord(Assembler.Word word, P p) {
        Assembler.Word w = word;
        w = w.withPrefix(visitSpace(w.getPrefix(), Space.Location.WORD_PREFIX, p));
        return w.withMarkers(visitMarkers(w.getMarkers(), p));
    }

    public Space visitSpace(Space space, Space.Location location, P p) {
        return space;
    }
}
