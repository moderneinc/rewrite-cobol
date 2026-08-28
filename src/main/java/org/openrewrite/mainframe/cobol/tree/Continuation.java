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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Value;
import lombok.With;
import org.openrewrite.Cursor;
import org.openrewrite.PrintOutputCapture;
import org.openrewrite.mainframe.cobol.internal.CobolPreprocessorSourcePrinter;
import org.openrewrite.mainframe.cobol.internal.CobolSourcePrinter;
import org.openrewrite.marker.Marker;
import org.openrewrite.marker.Markers;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.openrewrite.mainframe.cobol.internal.CobolSourcePrinter.COBOL_MARKER_WRAPPER;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@c")
@Value
@With
public class Continuation {

    Markers markers;
    Map<Integer, List<ColumnArea>> continuations;

    @SuppressWarnings("DuplicatedCode")
    public <P> void printContinuation(CobolPreprocessorSourcePrinter<P> sourcePrinter, Cursor cursor, CobolPreprocessor.Word word, boolean printColumns, PrintOutputCapture<P> p) {
        for (Marker marker : getMarkers().getMarkers()) {
            p.out.append(p.getMarkerPrinter().beforeSyntax(marker, new Cursor(cursor, marker), COBOL_MARKER_WRAPPER));
        }

        if (continuations.containsKey(0)) {
            for (ColumnArea columnArea : continuations.get(0)) {
                if (columnArea instanceof SequenceArea) {
                    columnArea.printColumnArea(sourcePrinter, cursor, printColumns, p);
                } else if (columnArea instanceof IndicatorArea) {
                    columnArea.printColumnArea(sourcePrinter, cursor, printColumns, p);
                }
            }
        }

        sourcePrinter.visitSpace(word.getPrefix(), Space.Location.CONTINUATION_PREFIX, p);

        // Reported once per line, so each piece of the word has a position of its own.
        int start = p.out.length();
        char[] charArray = word.getCobolWord().getWord().toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            if (i != 0 && continuations.containsKey(i)) {
                sourcePrinter.wordPrinted(word, start, p.out.length());
                for (ColumnArea columnArea : continuations.get(i)) {
                    columnArea.printColumnArea(sourcePrinter, cursor, printColumns, p);
                }
                start = p.out.length();
            }
            char c = charArray[i];
            p.append(c);
        }
        sourcePrinter.wordPrinted(word, start, p.out.length());

        List<List<ColumnArea>> lastColumnAreas = continuations.entrySet().stream()
                .filter(it -> it.getKey() > word.getCobolWord().getWord().length())
                .map(Map.Entry::getValue)
                .collect(toList());

        if (!lastColumnAreas.isEmpty()) {
            List<ColumnArea> columnAreas = lastColumnAreas.get(0);
            for (ColumnArea columnArea : columnAreas) {
                if (columnArea instanceof CommentArea) {
                    columnArea.printColumnArea(sourcePrinter, cursor, printColumns, p);
                }
            }
        }

        for (Marker marker : getMarkers().getMarkers()) {
            p.out.append(p.getMarkerPrinter().afterSyntax(marker, new Cursor(cursor, marker), COBOL_MARKER_WRAPPER));
        }
    }

    public <P> void printContinuation(CobolSourcePrinter<P> sourcePrinter, Cursor cursor, Cobol.Word word, boolean printColumns, PrintOutputCapture<P> p) {
        for (Marker marker : markers.getMarkers()) {
            p.out.append(p.getMarkerPrinter().beforeSyntax(marker, new Cursor(cursor, marker), COBOL_MARKER_WRAPPER));
        }

        if (continuations.containsKey(0)) {
            for (ColumnArea columnArea : continuations.get(0)) {
                if (columnArea instanceof SequenceArea) {
                    columnArea.printColumnArea(sourcePrinter, cursor, printColumns, p);
                } else if (columnArea instanceof IndicatorArea) {
                    columnArea.printColumnArea(sourcePrinter, cursor, printColumns, p);
                }
            }
        }

        sourcePrinter.visitSpace(word.getPrefix(), Space.Location.CONTINUATION_PREFIX, p);

        // Reported once per line, so each piece of the word has a position of its own.
        int start = p.out.length();
        char[] charArray = word.getWord().toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            if (i != 0 && continuations.containsKey(i)) {
                sourcePrinter.wordPrinted(word, start, p.out.length());
                for (ColumnArea columnArea : continuations.get(i)) {
                    columnArea.printColumnArea(sourcePrinter, cursor, printColumns, p);
                }
                start = p.out.length();
            }
            char c = charArray[i];
            p.append(c);
        }
        sourcePrinter.wordPrinted(word, start, p.out.length());

        List<List<ColumnArea>> lastColumnAreas = continuations.entrySet().stream()
                .filter(it -> it.getKey() > word.getWord().length())
                .map(Map.Entry::getValue)
                .collect(toList());

        if (!lastColumnAreas.isEmpty()) {
            List<ColumnArea> columnAreas = lastColumnAreas.get(0);
            for (ColumnArea columnArea : columnAreas) {
                if (columnArea instanceof CommentArea) {
                    columnArea.printColumnArea(sourcePrinter, cursor, printColumns, p);
                }
            }
        }

        for (Marker marker : markers.getMarkers()) {
            p.out.append(p.getMarkerPrinter().afterSyntax(marker, new Cursor(cursor, marker), COBOL_MARKER_WRAPPER));
        }
    }
}
