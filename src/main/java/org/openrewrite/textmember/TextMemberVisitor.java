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

import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.textmember.tree.TextMember;

public class TextMemberVisitor<P> extends TreeVisitor<TextMember, P> {

    public TextMember visitCompilationUnit(TextMember.CompilationUnit compilationUnit, P p) {
        TextMember.CompilationUnit c = compilationUnit;
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        return c.withLines(ListUtils.map(c.getLines(), l -> visitAndCast(l, p)));
    }

    public TextMember visitLine(TextMember.Line line, P p) {
        return line.withMarkers(visitMarkers(line.getMarkers(), p));
    }
}
