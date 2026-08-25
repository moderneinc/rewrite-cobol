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
package org.openrewrite.controlcard.idcams.internal;

import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.PrintOutputCapture;
import org.openrewrite.controlcard.idcams.IdcamsVisitor;
import org.openrewrite.controlcard.idcams.tree.Idcams;
import org.openrewrite.controlcard.idcams.tree.Space;
import org.openrewrite.marker.Marker;
import org.openrewrite.marker.Markers;

import java.util.function.UnaryOperator;

public class IdcamsPrinter<P> extends IdcamsVisitor<PrintOutputCapture<P>> {
    public static final UnaryOperator<String> IDCAMS_MARKER_WRAPPER =
            out -> "~~" + out + (out.isEmpty() ? "" : "~~") + ">";

    @Override
    public Idcams visitCompilationUnit(Idcams.CompilationUnit cu, PrintOutputCapture<P> p) {
        beforeSyntax(cu, Space.Location.COMPILATION_UNIT_PREFIX, p);
        visit(cu.getStatements(), p);
        afterSyntax(cu, p);
        visitSpace(cu.getEof(), Space.Location.COMPILATION_UNIT_EOF, p);
        return cu;
    }

    @Override
    public Idcams visitCommand(Idcams.Command command, PrintOutputCapture<P> p) {
        beforeSyntax(command, Space.Location.COMMAND_PREFIX, p);
        visit(command.getVerb(), p);
        for (Idcams parameter : command.getParameters()) {
            visit(parameter, p);
        }
        afterSyntax(command, p);
        return command;
    }

    @Override
    public Idcams visitParameter(Idcams.Parameter parameter, PrintOutputCapture<P> p) {
        beforeSyntax(parameter, Space.Location.PARAMETER_PREFIX, p);
        visit(parameter.getKeyword(), p);
        for (Idcams.Word word : parameter.getValue()) {
            visit(word, p);
        }
        afterSyntax(parameter, p);
        return parameter;
    }

    @Override
    public Idcams visitWord(Idcams.Word word, PrintOutputCapture<P> p) {
        beforeSyntax(word, Space.Location.WORD_PREFIX, p);
        p.append(word.getText());
        afterSyntax(word, p);
        return word;
    }

    @Override
    public Space visitSpace(Space space, Space.Location location, PrintOutputCapture<P> p) {
        p.append(space.getWhitespace());
        return space;
    }

    protected void beforeSyntax(Idcams i, Space.Location loc, PrintOutputCapture<P> p) {
        beforeSyntax(i.getPrefix(), i.getMarkers(), loc, p);
    }

    protected void beforeSyntax(Space prefix, Markers markers, Space.@Nullable Location loc, PrintOutputCapture<P> p) {
        for (Marker marker : markers.getMarkers()) {
            p.out.append(p.getMarkerPrinter().beforePrefix(marker, new Cursor(getCursor(), marker), IDCAMS_MARKER_WRAPPER));
        }
        if (loc != null) {
            visitSpace(prefix, loc, p);
        }
        visitMarkers(markers, p);
        for (Marker marker : markers.getMarkers()) {
            p.out.append(p.getMarkerPrinter().beforeSyntax(marker, new Cursor(getCursor(), marker), IDCAMS_MARKER_WRAPPER));
        }
    }

    protected void afterSyntax(Idcams i, PrintOutputCapture<P> p) {
        afterSyntax(i.getMarkers(), p);
    }

    protected void afterSyntax(Markers markers, PrintOutputCapture<P> p) {
        for (Marker marker : markers.getMarkers()) {
            p.out.append(p.getMarkerPrinter().afterSyntax(marker, new Cursor(getCursor(), marker), IDCAMS_MARKER_WRAPPER));
        }
    }
}
