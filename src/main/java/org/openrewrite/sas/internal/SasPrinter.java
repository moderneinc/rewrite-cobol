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
package org.openrewrite.sas.internal;

import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.PrintOutputCapture;
import org.openrewrite.marker.Marker;
import org.openrewrite.marker.Markers;
import org.openrewrite.sas.SasVisitor;
import org.openrewrite.sas.tree.Sas;
import org.openrewrite.sas.tree.Space;

import java.util.function.UnaryOperator;

public class SasPrinter<P> extends SasVisitor<PrintOutputCapture<P>> {
    public static final UnaryOperator<String> SAS_MARKER_WRAPPER =
            out -> "~~" + out + (out.isEmpty() ? "" : "~~") + ">";

    @Override
    public Sas visitCompilationUnit(Sas.CompilationUnit cu, PrintOutputCapture<P> p) {
        beforeSyntax(cu, Space.Location.COMPILATION_UNIT_PREFIX, p);
        visit(cu.getStatements(), p);
        afterSyntax(cu, p);
        visitSpace(cu.getEof(), Space.Location.COMPILATION_UNIT_EOF, p);
        return cu;
    }

    @Override
    public Sas visitStatement(Sas.Statement statement, PrintOutputCapture<P> p) {
        beforeSyntax(statement, Space.Location.STATEMENT_PREFIX, p);
        for (Sas part : statement.getParts()) {
            visit(part, p);
        }
        if (statement.getEnd() != null) {
            visitSpace(statement.getEnd(), Space.Location.STATEMENT_END, p);
            p.append(';');
        }
        afterSyntax(statement, p);
        return statement;
    }

    @Override
    public Sas visitComment(Sas.Comment comment, PrintOutputCapture<P> p) {
        beforeSyntax(comment, Space.Location.COMMENT_PREFIX, p);
        p.append(comment.getText());
        afterSyntax(comment, p);
        return comment;
    }

    @Override
    public Sas visitWord(Sas.Word word, PrintOutputCapture<P> p) {
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

    protected void beforeSyntax(Sas s, Space.Location loc, PrintOutputCapture<P> p) {
        beforeSyntax(s.getPrefix(), s.getMarkers(), loc, p);
    }

    protected void beforeSyntax(Space prefix, Markers markers, Space.@Nullable Location loc, PrintOutputCapture<P> p) {
        for (Marker marker : markers.getMarkers()) {
            p.out.append(p.getMarkerPrinter().beforePrefix(marker, new Cursor(getCursor(), marker), SAS_MARKER_WRAPPER));
        }
        if (loc != null) {
            visitSpace(prefix, loc, p);
        }
        visitMarkers(markers, p);
        for (Marker marker : markers.getMarkers()) {
            p.out.append(p.getMarkerPrinter().beforeSyntax(marker, new Cursor(getCursor(), marker), SAS_MARKER_WRAPPER));
        }
    }

    protected void afterSyntax(Sas s, PrintOutputCapture<P> p) {
        afterSyntax(s.getMarkers(), p);
    }

    protected void afterSyntax(Markers markers, PrintOutputCapture<P> p) {
        for (Marker marker : markers.getMarkers()) {
            p.out.append(p.getMarkerPrinter().afterSyntax(marker, new Cursor(getCursor(), marker), SAS_MARKER_WRAPPER));
        }
    }
}
