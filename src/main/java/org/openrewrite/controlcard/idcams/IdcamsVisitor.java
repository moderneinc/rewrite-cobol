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
package org.openrewrite.controlcard.idcams;

import org.openrewrite.TreeVisitor;
import org.openrewrite.controlcard.idcams.tree.Idcams;
import org.openrewrite.controlcard.idcams.tree.Space;
import org.openrewrite.internal.ListUtils;

public class IdcamsVisitor<P> extends TreeVisitor<Idcams, P> {

    public Idcams visitCompilationUnit(Idcams.CompilationUnit compilationUnit, P p) {
        Idcams.CompilationUnit c = compilationUnit;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.COMPILATION_UNIT_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withStatements(ListUtils.map(c.getStatements(), s -> visitAndCast(s, p)));
        return c.withEof(visitSpace(c.getEof(), Space.Location.COMPILATION_UNIT_EOF, p));
    }

    public Idcams visitCommand(Idcams.Command command, P p) {
        Idcams.Command c = command;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.COMMAND_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withVerb(visitAndCast(c.getVerb(), p));
        return c.withParameters(ListUtils.map(c.getParameters(), o -> visitAndCast(o, p)));
    }

    public Idcams visitParameter(Idcams.Parameter parameter, P p) {
        Idcams.Parameter o = parameter;
        o = o.withPrefix(visitSpace(o.getPrefix(), Space.Location.PARAMETER_PREFIX, p));
        o = o.withMarkers(visitMarkers(o.getMarkers(), p));
        o = o.withKeyword(visitAndCast(o.getKeyword(), p));
        return o.withValue(ListUtils.map(o.getValue(), w -> visitAndCast(w, p)));
    }

    public Idcams visitWord(Idcams.Word word, P p) {
        Idcams.Word w = word;
        w = w.withPrefix(visitSpace(w.getPrefix(), Space.Location.WORD_PREFIX, p));
        return w.withMarkers(visitMarkers(w.getMarkers(), p));
    }

    public Space visitSpace(Space space, Space.Location location, P p) {
        return space;
    }
}
