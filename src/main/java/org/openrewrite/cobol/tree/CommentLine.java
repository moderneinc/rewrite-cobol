/*
 * Copyright 2024 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.cobol.tree;

import lombok.Value;
import lombok.With;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.PrintOutputCapture;
import org.openrewrite.cobol.internal.CobolPreprocessorSourcePrinter;
import org.openrewrite.cobol.internal.CobolSourcePrinter;
import org.openrewrite.marker.Marker;
import org.openrewrite.marker.Markers;

import java.util.function.UnaryOperator;

@Value
public class CommentLine implements CobolLine, Comment {

    @With
    Markers markers;

    @Nullable
    @With
    SequenceArea sequenceArea;

    @With
    IndicatorArea indicatorArea;

    @With
    String contentArea;

    @Nullable
    @With
    CommentArea commentArea;

    @With
    boolean isCopiedSource;

    private static final UnaryOperator<String> COBOL_MARKER_WRAPPER =
            out -> "~~" + out + (out.isEmpty() ? "" : "~~") + ">";

    @Override
    public <P> void printCobolLine(CobolPreprocessorSourcePrinter<P> sourcePrinter, Cursor cursor, PrintOutputCapture<P> p) {
        for (Marker marker : markers.getMarkers()) {
            p.out.append(p.getMarkerPrinter().beforeSyntax(marker, new Cursor(cursor, this), COBOL_MARKER_WRAPPER));
        }

        if (!isCopiedSource) {
            if (sequenceArea != null) {
                sequenceArea.printColumnArea(sourcePrinter, cursor, true, p);
            }
            indicatorArea.printColumnArea(sourcePrinter, cursor, true, p);
            p.append(contentArea);
            if (commentArea != null) {
                commentArea.printColumnArea(sourcePrinter, cursor, true, p);
            }
        }

        for (Marker marker : markers.getMarkers()) {
            p.out.append(p.getMarkerPrinter().afterSyntax(marker, new Cursor(cursor, this), COBOL_MARKER_WRAPPER));
        }
    }

    @Override
    public <P> void printCobolLine(CobolSourcePrinter<P> sourcePrinter, Cursor cursor, PrintOutputCapture<P> p) {
        for (Marker marker : markers.getMarkers()) {
            p.out.append(p.getMarkerPrinter().beforeSyntax(marker, new Cursor(cursor, this), COBOL_MARKER_WRAPPER));
        }

        if (!isCopiedSource) {
            if (sequenceArea != null) {
                sequenceArea.printColumnArea(sourcePrinter, cursor, true, p);
            }
            indicatorArea.printColumnArea(sourcePrinter, cursor, true, p);
            p.append(contentArea);
            if (commentArea != null) {
                commentArea.printColumnArea(sourcePrinter, cursor, true, p);
            }
        }

        for (Marker marker : markers.getMarkers()) {
            p.out.append(p.getMarkerPrinter().afterSyntax(marker, new Cursor(cursor, this), COBOL_MARKER_WRAPPER));
        }
    }
}
