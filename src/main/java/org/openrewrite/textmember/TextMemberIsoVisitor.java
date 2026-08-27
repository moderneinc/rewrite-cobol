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
package org.openrewrite.textmember;

import org.openrewrite.textmember.tree.TextMember;

public class TextMemberIsoVisitor<P> extends TextMemberVisitor<P> {

    @Override
    public TextMember.CompilationUnit visitCompilationUnit(TextMember.CompilationUnit compilationUnit, P p) {
        return (TextMember.CompilationUnit) super.visitCompilationUnit(compilationUnit, p);
    }

    @Override
    public TextMember.Line visitLine(TextMember.Line line, P p) {
        return (TextMember.Line) super.visitLine(line, p);
    }
}
