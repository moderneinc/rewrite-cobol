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
package org.openrewrite.mainframe.cobol.tree;

import lombok.Value;
import lombok.With;
import org.openrewrite.Cursor;
import org.openrewrite.PrintOutputCapture;
import org.openrewrite.mainframe.cobol.internal.CobolPreprocessorSourcePrinter;
import org.openrewrite.mainframe.cobol.internal.CobolSourcePrinter;
import org.openrewrite.marker.Marker;
import org.openrewrite.marker.Markers;

import static org.openrewrite.mainframe.cobol.internal.CobolSourcePrinter.COBOL_MARKER_WRAPPER;

@Value
@With
public class SequenceArea implements ColumnArea {

    Markers markers;
    String sequence;

    @Override
    public <P> void printColumnArea(CobolPreprocessorSourcePrinter<P> sourcePrinter, Cursor cursor, boolean printColumns, PrintOutputCapture<P> p) {
        //noinspection DuplicatedCode
        if (printColumns) {
            for (Marker marker : markers.getMarkers()) {
                p.out.append(p.getMarkerPrinter().beforeSyntax(marker, new Cursor(cursor, marker), COBOL_MARKER_WRAPPER));
            }

            p.out.append(sequence);

            for (Marker marker : markers.getMarkers()) {
                p.out.append(p.getMarkerPrinter().afterSyntax(marker, new Cursor(cursor, marker), COBOL_MARKER_WRAPPER));
            }
        }
    }

    @Override
    public <P> void printColumnArea(CobolSourcePrinter<P> sourcePrinter, Cursor cursor, boolean printColumns, PrintOutputCapture<P> p) {
        //noinspection DuplicatedCode
        if (printColumns) {
            for (Marker marker : markers.getMarkers()) {
                p.out.append(p.getMarkerPrinter().beforeSyntax(marker, new Cursor(cursor, marker), COBOL_MARKER_WRAPPER));
            }

            p.out.append(sequence);

            for (Marker marker : markers.getMarkers()) {
                p.out.append(p.getMarkerPrinter().afterSyntax(marker, new Cursor(cursor, marker), COBOL_MARKER_WRAPPER));
            }
        }
    }
}
