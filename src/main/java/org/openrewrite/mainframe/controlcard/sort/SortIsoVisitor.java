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
package org.openrewrite.mainframe.controlcard.sort;

import org.openrewrite.mainframe.controlcard.sort.tree.Sort;

public class SortIsoVisitor<P> extends SortVisitor<P> {

    @Override
    public Sort.CompilationUnit visitCompilationUnit(Sort.CompilationUnit compilationUnit, P p) {
        return (Sort.CompilationUnit) super.visitCompilationUnit(compilationUnit, p);
    }

    @Override
    public Sort.ControlStatement visitControlStatement(Sort.ControlStatement controlStatement, P p) {
        return (Sort.ControlStatement) super.visitControlStatement(controlStatement, p);
    }

    @Override
    public Sort.Operand visitOperand(Sort.Operand operand, P p) {
        return (Sort.Operand) super.visitOperand(operand, p);
    }

    @Override
    public Sort.Word visitWord(Sort.Word word, P p) {
        return (Sort.Word) super.visitWord(word, p);
    }
}
