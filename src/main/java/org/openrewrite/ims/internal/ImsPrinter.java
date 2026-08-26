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
package org.openrewrite.ims.internal;

import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.PrintOutputCapture;
import org.openrewrite.ims.ImsVisitor;
import org.openrewrite.ims.marker.SequenceArea;
import org.openrewrite.ims.tree.Ims;
import org.openrewrite.ims.tree.Space;
import org.openrewrite.marker.Marker;
import org.openrewrite.marker.Markers;

import java.util.function.UnaryOperator;

public class ImsPrinter<P> extends ImsVisitor<PrintOutputCapture<P>> {
    public static final UnaryOperator<String> IMS_MARKER_WRAPPER =
            out -> "~~" + out + (out.isEmpty() ? "" : "~~") + ">";

    @Override
    public Ims visitCompilationUnit(Ims.CompilationUnit cu, PrintOutputCapture<P> p) {
        beforeSyntax(cu, Space.Location.COMPILATION_UNIT_PREFIX, p);
        visit(cu.getStatements(), p);
        afterSyntax(cu, p);
        visitSpace(cu.getEof(), Space.Location.COMPILATION_UNIT_EOF, p);
        return cu;
    }

    @Override
    public Ims visitMacroStatement(Ims.MacroStatement statement, PrintOutputCapture<P> p) {
        beforeSyntax(statement, Space.Location.MACRO_STATEMENT_PREFIX, p);
        visit(statement.getName(), p);
        visit(statement.getOperation(), p);
        for (Ims operand : statement.getOperands()) {
            visit(operand, p);
        }
        afterSyntax(statement, p);
        return statement;
    }

    @Override
    public Ims visitComment(Ims.Comment comment, PrintOutputCapture<P> p) {
        beforeSyntax(comment, Space.Location.COMMENT_PREFIX, p);
        visit(comment.getWord(), p);
        afterSyntax(comment, p);
        return comment;
    }

    @Override
    public Ims visitUnknown(Ims.Unknown unknown, PrintOutputCapture<P> p) {
        beforeSyntax(unknown, Space.Location.UNKNOWN_PREFIX, p);
        visit(unknown.getWord(), p);
        afterSyntax(unknown, p);
        return unknown;
    }

    @Override
    public Ims visitKeywordOperand(Ims.KeywordOperand operand, PrintOutputCapture<P> p) {
        beforeSyntax(operand, Space.Location.OPERAND_PREFIX, p);
        visit(operand.getKeyword(), p);
        for (Ims.Word word : operand.getValue()) {
            visit(word, p);
        }
        afterSyntax(operand, p);
        return operand;
    }

    @Override
    public Ims visitPositionalOperand(Ims.PositionalOperand operand, PrintOutputCapture<P> p) {
        beforeSyntax(operand, Space.Location.OPERAND_PREFIX, p);
        for (Ims.Word word : operand.getValue()) {
            visit(word, p);
        }
        afterSyntax(operand, p);
        return operand;
    }

    @Override
    public Ims visitWord(Ims.Word word, PrintOutputCapture<P> p) {
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

    protected void beforeSyntax(Ims b, Space.Location loc, PrintOutputCapture<P> p) {
        beforeSyntax(b.getPrefix(), b.getMarkers(), loc, p);
    }

    protected void beforeSyntax(Space prefix, Markers markers, Space.@Nullable Location loc, PrintOutputCapture<P> p) {
        for (Marker marker : markers.getMarkers()) {
            p.out.append(p.getMarkerPrinter().beforePrefix(marker, new Cursor(getCursor(), marker), IMS_MARKER_WRAPPER));
        }
        if (loc != null) {
            visitSpace(prefix, loc, p);
        }
        visitMarkers(markers, p);
        for (Marker marker : markers.getMarkers()) {
            p.out.append(p.getMarkerPrinter().beforeSyntax(marker, new Cursor(getCursor(), marker), IMS_MARKER_WRAPPER));
        }
    }

    protected void afterSyntax(Ims b, PrintOutputCapture<P> p) {
        afterSyntax(b.getMarkers(), p);
    }

    protected void afterSyntax(Markers markers, PrintOutputCapture<P> p) {
        for (Marker marker : markers.getMarkers()) {
            if (marker instanceof SequenceArea) {
                SequenceArea sequenceArea = (SequenceArea) marker;
                visitSpace(sequenceArea.getPrefix(), Space.Location.SEQUENCE_AREA_PREFIX, p);
                p.append(sequenceArea.getText());
            }
            p.out.append(p.getMarkerPrinter().afterSyntax(marker, new Cursor(getCursor(), marker), IMS_MARKER_WRAPPER));
        }
    }
}
