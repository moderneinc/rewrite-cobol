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
package org.openrewrite.mainframe.bms;

import org.openrewrite.mainframe.bms.tree.Bms;

public class BmsIsoVisitor<P> extends BmsVisitor<P> {

    @Override
    public Bms.CompilationUnit visitCompilationUnit(Bms.CompilationUnit compilationUnit, P p) {
        return (Bms.CompilationUnit) super.visitCompilationUnit(compilationUnit, p);
    }

    @Override
    public Bms.MacroStatement visitMacroStatement(Bms.MacroStatement statement, P p) {
        return (Bms.MacroStatement) super.visitMacroStatement(statement, p);
    }

    @Override
    public Bms.Comment visitComment(Bms.Comment comment, P p) {
        return (Bms.Comment) super.visitComment(comment, p);
    }

    @Override
    public Bms.Unknown visitUnknown(Bms.Unknown unknown, P p) {
        return (Bms.Unknown) super.visitUnknown(unknown, p);
    }

    @Override
    public Bms.KeywordOperand visitKeywordOperand(Bms.KeywordOperand operand, P p) {
        return (Bms.KeywordOperand) super.visitKeywordOperand(operand, p);
    }

    @Override
    public Bms.PositionalOperand visitPositionalOperand(Bms.PositionalOperand operand, P p) {
        return (Bms.PositionalOperand) super.visitPositionalOperand(operand, p);
    }

    @Override
    public Bms.Word visitWord(Bms.Word word, P p) {
        return (Bms.Word) super.visitWord(word, p);
    }
}
