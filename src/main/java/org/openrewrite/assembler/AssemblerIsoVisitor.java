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
package org.openrewrite.assembler;

import org.openrewrite.assembler.tree.Assembler;

public class AssemblerIsoVisitor<P> extends AssemblerVisitor<P> {

    @Override
    public Assembler.CompilationUnit visitCompilationUnit(Assembler.CompilationUnit compilationUnit, P p) {
        return (Assembler.CompilationUnit) super.visitCompilationUnit(compilationUnit, p);
    }

    @Override
    public Assembler.Instruction visitInstruction(Assembler.Instruction instruction, P p) {
        return (Assembler.Instruction) super.visitInstruction(instruction, p);
    }

    @Override
    public Assembler.Comment visitComment(Assembler.Comment comment, P p) {
        return (Assembler.Comment) super.visitComment(comment, p);
    }

    @Override
    public Assembler.Unknown visitUnknown(Assembler.Unknown unknown, P p) {
        return (Assembler.Unknown) super.visitUnknown(unknown, p);
    }

    @Override
    public Assembler.Operand visitOperand(Assembler.Operand operand, P p) {
        return (Assembler.Operand) super.visitOperand(operand, p);
    }

    @Override
    public Assembler.Continuation visitContinuation(Assembler.Continuation continuation, P p) {
        return (Assembler.Continuation) super.visitContinuation(continuation, p);
    }

    @Override
    public Assembler.Word visitWord(Assembler.Word word, P p) {
        return (Assembler.Word) super.visitWord(word, p);
    }
}
