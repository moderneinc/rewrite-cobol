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

import org.openrewrite.mainframe.controlcard.utility.tree.Utility;

public class UtilityIsoVisitor<P> extends UtilityVisitor<P> {

    @Override
    public Utility.CompilationUnit visitCompilationUnit(Utility.CompilationUnit compilationUnit, P p) {
        return (Utility.CompilationUnit) super.visitCompilationUnit(compilationUnit, p);
    }

    @Override
    public Utility.Block visitBlock(Utility.Block block, P p) {
        return (Utility.Block) super.visitBlock(block, p);
    }

    @Override
    public Utility.Operand visitOperand(Utility.Operand operand, P p) {
        return (Utility.Operand) super.visitOperand(operand, p);
    }

    @Override
    public Utility.Word visitWord(Utility.Word word, P p) {
        return (Utility.Word) super.visitWord(word, p);
    }
}
