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
package org.openrewrite.jcl;

import org.openrewrite.jcl.tree.Jcl;

public class JclIsoVisitor<P> extends JclVisitor<P> {

    @Override
    public Jcl.CompilationUnit visitCompilationUnit(Jcl.CompilationUnit compilationUnit, P p) {
        return (Jcl.CompilationUnit) super.visitCompilationUnit(compilationUnit, p);
    }

    @Override
    public Jcl.Comment visitComment(Jcl.Comment comment, P p) {
        return (Jcl.Comment) super.visitComment(comment, p);
    }

    @Override
    public Jcl.ControlM visitControlM(Jcl.ControlM controlM, P p) {
        return (Jcl.ControlM) super.visitControlM(controlM, p);
    }

    @Override
    public Jcl.JclStatement visitJclStatement(Jcl.JclStatement jclStatement, P p) {
        return (Jcl.JclStatement) super.visitJclStatement(jclStatement, p);
    }

    @Override
    public Jcl.KeywordParameter visitKeywordParameter(Jcl.KeywordParameter parameter, P p) {
        return (Jcl.KeywordParameter) super.visitKeywordParameter(parameter, p);
    }

    @Override
    public Jcl.PositionalParameter visitPositionalParameter(Jcl.PositionalParameter parameter, P p) {
        return (Jcl.PositionalParameter) super.visitPositionalParameter(parameter, p);
    }

    @Override
    public Jcl.Jes2 visitJes2(Jcl.Jes2 jes2, P p) {
        return (Jcl.Jes2) super.visitJes2(jes2, p);
    }

    @Override
    public Jcl.Jes3 visitJes3(Jcl.Jes3 jes3, P p) {
        return (Jcl.Jes3) super.visitJes3(jes3, p);
    }

    @Override
    public Jcl.Unknown visitUnknown(Jcl.Unknown unknown, P p) {
        return (Jcl.Unknown) super.visitUnknown(unknown, p);
    }

    @Override
    public Jcl.Word visitWord(Jcl.Word word, P p) {
        return (Jcl.Word) super.visitWord(word, p);
    }
}
