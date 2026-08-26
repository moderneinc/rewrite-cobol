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
package org.openrewrite.ims;

import org.openrewrite.ims.tree.Ims;

public class ImsIsoVisitor<P> extends ImsVisitor<P> {

    @Override
    public Ims.CompilationUnit visitCompilationUnit(Ims.CompilationUnit compilationUnit, P p) {
        return (Ims.CompilationUnit) super.visitCompilationUnit(compilationUnit, p);
    }

    @Override
    public Ims.MacroStatement visitMacroStatement(Ims.MacroStatement statement, P p) {
        return (Ims.MacroStatement) super.visitMacroStatement(statement, p);
    }

    @Override
    public Ims.Comment visitComment(Ims.Comment comment, P p) {
        return (Ims.Comment) super.visitComment(comment, p);
    }

    @Override
    public Ims.Unknown visitUnknown(Ims.Unknown unknown, P p) {
        return (Ims.Unknown) super.visitUnknown(unknown, p);
    }

    @Override
    public Ims.KeywordOperand visitKeywordOperand(Ims.KeywordOperand operand, P p) {
        return (Ims.KeywordOperand) super.visitKeywordOperand(operand, p);
    }

    @Override
    public Ims.PositionalOperand visitPositionalOperand(Ims.PositionalOperand operand, P p) {
        return (Ims.PositionalOperand) super.visitPositionalOperand(operand, p);
    }

    @Override
    public Ims.Word visitWord(Ims.Word word, P p) {
        return (Ims.Word) super.visitWord(word, p);
    }
}
