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
package org.openrewrite.db2.bind.internal;

import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.PrintOutputCapture;
import org.openrewrite.db2.bind.BindVisitor;
import org.openrewrite.db2.bind.tree.Bind;
import org.openrewrite.db2.bind.tree.Space;
import org.openrewrite.marker.Marker;
import org.openrewrite.marker.Markers;

import java.util.function.UnaryOperator;

public class BindPrinter<P> extends BindVisitor<PrintOutputCapture<P>> {
    public static final UnaryOperator<String> BIND_MARKER_WRAPPER =
            out -> "~~" + out + (out.isEmpty() ? "" : "~~") + ">";

    @Override
    public Bind visitCompilationUnit(Bind.CompilationUnit cu, PrintOutputCapture<P> p) {
        beforeSyntax(cu, Space.Location.COMPILATION_UNIT_PREFIX, p);
        visit(cu.getStatements(), p);
        afterSyntax(cu, p);
        visitSpace(cu.getEof(), Space.Location.COMPILATION_UNIT_EOF, p);
        return cu;
    }

    @Override
    public Bind visitCommand(Bind.Command command, PrintOutputCapture<P> p) {
        beforeSyntax(command, Space.Location.COMMAND_PREFIX, p);
        visit(command.getVerb(), p);
        for (Bind operand : command.getOperands()) {
            visit(operand, p);
        }
        afterSyntax(command, p);
        return command;
    }

    @Override
    public Bind visitOperand(Bind.Operand operand, PrintOutputCapture<P> p) {
        beforeSyntax(operand, Space.Location.OPERAND_PREFIX, p);
        visit(operand.getKeyword(), p);
        for (Bind.Word word : operand.getValue()) {
            visit(word, p);
        }
        afterSyntax(operand, p);
        return operand;
    }

    @Override
    public Bind visitWord(Bind.Word word, PrintOutputCapture<P> p) {
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

    protected void beforeSyntax(Bind b, Space.Location loc, PrintOutputCapture<P> p) {
        beforeSyntax(b.getPrefix(), b.getMarkers(), loc, p);
    }

    protected void beforeSyntax(Space prefix, Markers markers, Space.@Nullable Location loc, PrintOutputCapture<P> p) {
        for (Marker marker : markers.getMarkers()) {
            p.out.append(p.getMarkerPrinter().beforePrefix(marker, new Cursor(getCursor(), marker), BIND_MARKER_WRAPPER));
        }
        if (loc != null) {
            visitSpace(prefix, loc, p);
        }
        visitMarkers(markers, p);
        for (Marker marker : markers.getMarkers()) {
            p.out.append(p.getMarkerPrinter().beforeSyntax(marker, new Cursor(getCursor(), marker), BIND_MARKER_WRAPPER));
        }
    }

    protected void afterSyntax(Bind b, PrintOutputCapture<P> p) {
        afterSyntax(b.getMarkers(), p);
    }

    protected void afterSyntax(Markers markers, PrintOutputCapture<P> p) {
        for (Marker marker : markers.getMarkers()) {
            p.out.append(p.getMarkerPrinter().afterSyntax(marker, new Cursor(getCursor(), marker), BIND_MARKER_WRAPPER));
        }
    }
}
