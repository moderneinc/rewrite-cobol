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
package org.openrewrite.mainframe.assembler.internal;

import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.PrintOutputCapture;
import org.openrewrite.mainframe.assembler.AssemblerVisitor;
import org.openrewrite.mainframe.assembler.marker.SequenceArea;
import org.openrewrite.mainframe.assembler.tree.Assembler;
import org.openrewrite.mainframe.assembler.tree.Space;
import org.openrewrite.marker.Marker;
import org.openrewrite.marker.Markers;

import java.util.function.UnaryOperator;

public class AssemblerPrinter<P> extends AssemblerVisitor<PrintOutputCapture<P>> {
    public static final UnaryOperator<String> ASSEMBLER_MARKER_WRAPPER =
            out -> "~~" + out + (out.isEmpty() ? "" : "~~") + ">";

    @Override
    public Assembler visitCompilationUnit(Assembler.CompilationUnit cu, PrintOutputCapture<P> p) {
        beforeSyntax(cu, Space.Location.COMPILATION_UNIT_PREFIX, p);
        visit(cu.getStatements(), p);
        afterSyntax(cu, p);
        visitSpace(cu.getEof(), Space.Location.COMPILATION_UNIT_EOF, p);
        return cu;
    }

    @Override
    public Assembler visitInstruction(Assembler.Instruction instruction, PrintOutputCapture<P> p) {
        beforeSyntax(instruction, Space.Location.INSTRUCTION_PREFIX, p);
        visit(instruction.getName(), p);
        visit(instruction.getOperation(), p);
        for (Assembler operand : instruction.getOperands()) {
            visit(operand, p);
        }
        afterSyntax(instruction, p);
        return instruction;
    }

    @Override
    public Assembler visitComment(Assembler.Comment comment, PrintOutputCapture<P> p) {
        beforeSyntax(comment, Space.Location.COMMENT_PREFIX, p);
        for (Assembler part : comment.getParts()) {
            visit(part, p);
        }
        afterSyntax(comment, p);
        return comment;
    }

    @Override
    public Assembler visitUnknown(Assembler.Unknown unknown, PrintOutputCapture<P> p) {
        beforeSyntax(unknown, Space.Location.UNKNOWN_PREFIX, p);
        visit(unknown.getWord(), p);
        afterSyntax(unknown, p);
        return unknown;
    }

    @Override
    public Assembler visitOperand(Assembler.Operand operand, PrintOutputCapture<P> p) {
        beforeSyntax(operand, Space.Location.OPERAND_PREFIX, p);
        for (Assembler part : operand.getParts()) {
            visit(part, p);
        }
        afterSyntax(operand, p);
        return operand;
    }

    @Override
    public Assembler visitContinuation(Assembler.Continuation continuation, PrintOutputCapture<P> p) {
        beforeSyntax(continuation, Space.Location.CONTINUATION_PREFIX, p);
        p.append(continuation.getText());
        afterSyntax(continuation, p);
        return continuation;
    }

    @Override
    public Assembler visitWord(Assembler.Word word, PrintOutputCapture<P> p) {
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

    protected void beforeSyntax(Assembler a, Space.Location loc, PrintOutputCapture<P> p) {
        beforeSyntax(a.getPrefix(), a.getMarkers(), loc, p);
    }

    protected void beforeSyntax(Space prefix, Markers markers, Space.@Nullable Location loc, PrintOutputCapture<P> p) {
        for (Marker marker : markers.getMarkers()) {
            p.out.append(p.getMarkerPrinter().beforePrefix(marker, new Cursor(getCursor(), marker), ASSEMBLER_MARKER_WRAPPER));
        }
        if (loc != null) {
            visitSpace(prefix, loc, p);
        }
        visitMarkers(markers, p);
        for (Marker marker : markers.getMarkers()) {
            p.out.append(p.getMarkerPrinter().beforeSyntax(marker, new Cursor(getCursor(), marker), ASSEMBLER_MARKER_WRAPPER));
        }
    }

    protected void afterSyntax(Assembler a, PrintOutputCapture<P> p) {
        afterSyntax(a.getMarkers(), p);
    }

    protected void afterSyntax(Markers markers, PrintOutputCapture<P> p) {
        for (Marker marker : markers.getMarkers()) {
            if (marker instanceof SequenceArea) {
                SequenceArea sequenceArea = (SequenceArea) marker;
                visitSpace(sequenceArea.getPrefix(), Space.Location.SEQUENCE_AREA_PREFIX, p);
                p.append(sequenceArea.getText());
            }
            p.out.append(p.getMarkerPrinter().afterSyntax(marker, new Cursor(getCursor(), marker), ASSEMBLER_MARKER_WRAPPER));
        }
    }
}
