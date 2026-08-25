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
package org.openrewrite.linkedit;

import org.openrewrite.linkedit.tree.LinkEdit;

public class LinkEditIsoVisitor<P> extends LinkEditVisitor<P> {

    @Override
    public LinkEdit.CompilationUnit visitCompilationUnit(LinkEdit.CompilationUnit compilationUnit, P p) {
        return (LinkEdit.CompilationUnit) super.visitCompilationUnit(compilationUnit, p);
    }

    @Override
    public LinkEdit.ControlStatement visitControlStatement(LinkEdit.ControlStatement controlStatement, P p) {
        return (LinkEdit.ControlStatement) super.visitControlStatement(controlStatement, p);
    }

    @Override
    public LinkEdit.Operand visitOperand(LinkEdit.Operand operand, P p) {
        return (LinkEdit.Operand) super.visitOperand(operand, p);
    }

    @Override
    public LinkEdit.Word visitWord(LinkEdit.Word word, P p) {
        return (LinkEdit.Word) super.visitWord(word, p);
    }
}
