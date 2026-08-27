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
package org.openrewrite.sas;

import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.sas.tree.Sas;
import org.openrewrite.sas.tree.Space;

public class SasVisitor<P> extends TreeVisitor<Sas, P> {

    public Sas visitCompilationUnit(Sas.CompilationUnit compilationUnit, P p) {
        Sas.CompilationUnit c = compilationUnit;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.COMPILATION_UNIT_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withStatements(ListUtils.map(c.getStatements(), s -> visitAndCast(s, p)));
        return c.withEof(visitSpace(c.getEof(), Space.Location.COMPILATION_UNIT_EOF, p));
    }

    public Sas visitStatement(Sas.Statement statement, P p) {
        Sas.Statement s = statement;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.STATEMENT_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withParts(ListUtils.map(s.getParts(), part -> visitAndCast(part, p)));
        if (s.getEnd() != null) {
            s = s.withEnd(visitAndCast(s.getEnd(), p));
        }
        return s;
    }

    public Sas visitComment(Sas.Comment comment, P p) {
        Sas.Comment c = comment;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.COMMENT_PREFIX, p));
        return c.withMarkers(visitMarkers(c.getMarkers(), p));
    }

    public Sas visitWord(Sas.Word word, P p) {
        Sas.Word w = word;
        w = w.withPrefix(visitSpace(w.getPrefix(), Space.Location.WORD_PREFIX, p));
        return w.withMarkers(visitMarkers(w.getMarkers(), p));
    }

    public Space visitSpace(Space space, Space.Location location, P p) {
        return space;
    }
}
