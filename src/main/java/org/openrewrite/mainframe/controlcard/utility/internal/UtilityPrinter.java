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
package org.openrewrite.mainframe.controlcard.utility.internal;

import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.PrintOutputCapture;
import org.openrewrite.mainframe.controlcard.utility.UtilityVisitor;
import org.openrewrite.mainframe.controlcard.utility.tree.Space;
import org.openrewrite.mainframe.controlcard.utility.tree.Utility;
import org.openrewrite.marker.Marker;
import org.openrewrite.marker.Markers;

import java.util.function.UnaryOperator;

public class UtilityPrinter<P> extends UtilityVisitor<PrintOutputCapture<P>> {
    public static final UnaryOperator<String> UTILITY_MARKER_WRAPPER =
            out -> "~~" + out + (out.isEmpty() ? "" : "~~") + ">";

    @Override
    public Utility visitCompilationUnit(Utility.CompilationUnit cu, PrintOutputCapture<P> p) {
        beforeSyntax(cu, Space.Location.COMPILATION_UNIT_PREFIX, p);
        visit(cu.getStatements(), p);
        afterSyntax(cu, p);
        visitSpace(cu.getEof(), Space.Location.COMPILATION_UNIT_EOF, p);
        return cu;
    }

    @Override
    public Utility visitBlock(Utility.Block block, PrintOutputCapture<P> p) {
        beforeSyntax(block, Space.Location.BLOCK_PREFIX, p);
        visit(block.getVerb(), p);
        for (Utility.Word word : block.getValue()) {
            visit(word, p);
        }
        for (Utility content : block.getContents()) {
            visit(content, p);
        }
        afterSyntax(block, p);
        return block;
    }

    @Override
    public Utility visitOperand(Utility.Operand operand, PrintOutputCapture<P> p) {
        beforeSyntax(operand, Space.Location.OPERAND_PREFIX, p);
        visit(operand.getKeyword(), p);
        for (Utility.Word word : operand.getValue()) {
            visit(word, p);
        }
        afterSyntax(operand, p);
        return operand;
    }

    @Override
    public Utility visitWord(Utility.Word word, PrintOutputCapture<P> p) {
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

    protected void beforeSyntax(Utility u, Space.Location loc, PrintOutputCapture<P> p) {
        beforeSyntax(u.getPrefix(), u.getMarkers(), loc, p);
    }

    protected void beforeSyntax(Space prefix, Markers markers, Space.@Nullable Location loc, PrintOutputCapture<P> p) {
        for (Marker marker : markers.getMarkers()) {
            p.out.append(p.getMarkerPrinter().beforePrefix(marker, new Cursor(getCursor(), marker), UTILITY_MARKER_WRAPPER));
        }
        if (loc != null) {
            visitSpace(prefix, loc, p);
        }
        visitMarkers(markers, p);
        for (Marker marker : markers.getMarkers()) {
            p.out.append(p.getMarkerPrinter().beforeSyntax(marker, new Cursor(getCursor(), marker), UTILITY_MARKER_WRAPPER));
        }
    }

    protected void afterSyntax(Utility u, PrintOutputCapture<P> p) {
        afterSyntax(u.getMarkers(), p);
    }

    protected void afterSyntax(Markers markers, PrintOutputCapture<P> p) {
        for (Marker marker : markers.getMarkers()) {
            p.out.append(p.getMarkerPrinter().afterSyntax(marker, new Cursor(getCursor(), marker), UTILITY_MARKER_WRAPPER));
        }
    }
}
