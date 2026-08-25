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
package org.openrewrite.controlcard.sort.internal;

import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.PrintOutputCapture;
import org.openrewrite.controlcard.sort.SortVisitor;
import org.openrewrite.controlcard.sort.tree.Sort;
import org.openrewrite.controlcard.sort.tree.Space;
import org.openrewrite.marker.Marker;
import org.openrewrite.marker.Markers;

import java.util.function.UnaryOperator;

public class SortPrinter<P> extends SortVisitor<PrintOutputCapture<P>> {
    public static final UnaryOperator<String> SORT_MARKER_WRAPPER =
            out -> "~~" + out + (out.isEmpty() ? "" : "~~") + ">";

    @Override
    public Sort visitCompilationUnit(Sort.CompilationUnit cu, PrintOutputCapture<P> p) {
        beforeSyntax(cu, Space.Location.COMPILATION_UNIT_PREFIX, p);
        visit(cu.getStatements(), p);
        afterSyntax(cu, p);
        visitSpace(cu.getEof(), Space.Location.COMPILATION_UNIT_EOF, p);
        return cu;
    }

    @Override
    public Sort visitControlStatement(Sort.ControlStatement statement, PrintOutputCapture<P> p) {
        beforeSyntax(statement, Space.Location.CONTROL_STATEMENT_PREFIX, p);
        visit(statement.getOperator(), p);
        for (Sort operand : statement.getOperands()) {
            visit(operand, p);
        }
        afterSyntax(statement, p);
        return statement;
    }

    @Override
    public Sort visitOperand(Sort.Operand operand, PrintOutputCapture<P> p) {
        beforeSyntax(operand, Space.Location.OPERAND_PREFIX, p);
        visit(operand.getKeyword(), p);
        for (Sort.Word word : operand.getValue()) {
            visit(word, p);
        }
        afterSyntax(operand, p);
        return operand;
    }

    @Override
    public Sort visitWord(Sort.Word word, PrintOutputCapture<P> p) {
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

    protected void beforeSyntax(Sort s, Space.Location loc, PrintOutputCapture<P> p) {
        beforeSyntax(s.getPrefix(), s.getMarkers(), loc, p);
    }

    protected void beforeSyntax(Space prefix, Markers markers, Space.@Nullable Location loc, PrintOutputCapture<P> p) {
        for (Marker marker : markers.getMarkers()) {
            p.out.append(p.getMarkerPrinter().beforePrefix(marker, new Cursor(getCursor(), marker), SORT_MARKER_WRAPPER));
        }
        if (loc != null) {
            visitSpace(prefix, loc, p);
        }
        visitMarkers(markers, p);
        for (Marker marker : markers.getMarkers()) {
            p.out.append(p.getMarkerPrinter().beforeSyntax(marker, new Cursor(getCursor(), marker), SORT_MARKER_WRAPPER));
        }
    }

    protected void afterSyntax(Sort s, PrintOutputCapture<P> p) {
        afterSyntax(s.getMarkers(), p);
    }

    protected void afterSyntax(Markers markers, PrintOutputCapture<P> p) {
        for (Marker marker : markers.getMarkers()) {
            p.out.append(p.getMarkerPrinter().afterSyntax(marker, new Cursor(getCursor(), marker), SORT_MARKER_WRAPPER));
        }
    }
}
