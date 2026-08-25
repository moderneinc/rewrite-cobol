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
package org.openrewrite.listload.internal;

import org.openrewrite.Cursor;
import org.openrewrite.PrintOutputCapture;
import org.openrewrite.listload.ListLoadVisitor;
import org.openrewrite.listload.tree.ListLoad;
import org.openrewrite.marker.Marker;
import org.openrewrite.marker.Markers;

import java.util.function.UnaryOperator;

public class ListLoadPrinter<P> extends ListLoadVisitor<PrintOutputCapture<P>> {
    public static final UnaryOperator<String> LIST_LOAD_MARKER_WRAPPER =
            out -> "~~" + out + (out.isEmpty() ? "" : "~~") + ">";

    @Override
    public ListLoad visitCompilationUnit(ListLoad.CompilationUnit cu, PrintOutputCapture<P> p) {
        beforeSyntax(cu.getMarkers(), p);
        visit(cu.getLines(), p);
        afterSyntax(cu.getMarkers(), p);
        return cu;
    }

    @Override
    public ListLoad visitLine(ListLoad.Line line, PrintOutputCapture<P> p) {
        beforeSyntax(line.getMarkers(), p);
        p.append(line.getCarriageControl());
        p.append(line.getText());
        afterSyntax(line.getMarkers(), p);
        p.append(line.getLineEnding());
        return line;
    }

    protected void beforeSyntax(Markers markers, PrintOutputCapture<P> p) {
        for (Marker marker : markers.getMarkers()) {
            p.out.append(p.getMarkerPrinter().beforePrefix(marker, new Cursor(getCursor(), marker), LIST_LOAD_MARKER_WRAPPER));
        }
        visitMarkers(markers, p);
        for (Marker marker : markers.getMarkers()) {
            p.out.append(p.getMarkerPrinter().beforeSyntax(marker, new Cursor(getCursor(), marker), LIST_LOAD_MARKER_WRAPPER));
        }
    }

    protected void afterSyntax(Markers markers, PrintOutputCapture<P> p) {
        for (Marker marker : markers.getMarkers()) {
            p.out.append(p.getMarkerPrinter().afterSyntax(marker, new Cursor(getCursor(), marker), LIST_LOAD_MARKER_WRAPPER));
        }
    }
}
