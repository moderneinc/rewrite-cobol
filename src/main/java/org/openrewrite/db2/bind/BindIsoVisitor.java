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
package org.openrewrite.db2.bind;

import org.openrewrite.db2.bind.tree.Bind;

public class BindIsoVisitor<P> extends BindVisitor<P> {

    @Override
    public Bind.CompilationUnit visitCompilationUnit(Bind.CompilationUnit compilationUnit, P p) {
        return (Bind.CompilationUnit) super.visitCompilationUnit(compilationUnit, p);
    }

    @Override
    public Bind.Command visitCommand(Bind.Command command, P p) {
        return (Bind.Command) super.visitCommand(command, p);
    }

    @Override
    public Bind.Operand visitOperand(Bind.Operand operand, P p) {
        return (Bind.Operand) super.visitOperand(operand, p);
    }

    @Override
    public Bind.Word visitWord(Bind.Word word, P p) {
        return (Bind.Word) super.visitWord(word, p);
    }
}
