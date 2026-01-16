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
package org.openrewrite.jcl.internal;

import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.PrintOutputCapture;
import org.openrewrite.jcl.JclVisitor;
import org.openrewrite.jcl.marker.CommentArea;
import org.openrewrite.jcl.marker.TrailingComment;
import org.openrewrite.jcl.tree.Jcl;
import org.openrewrite.jcl.tree.Space;
import org.openrewrite.marker.Marker;
import org.openrewrite.marker.Markers;

import java.util.function.UnaryOperator;

public class JclPrinter<P> extends JclVisitor<PrintOutputCapture<P>> {
    public static final UnaryOperator<String> JCL_MARKER_WRAPPER =
            out -> "~~" + out + (out.isEmpty() ? "" : "~~") + ">";

    @Override
    public Jcl.CompilationUnit visitCompilationUnit(Jcl.CompilationUnit cu, PrintOutputCapture<P> p) {
        beforeSyntax(cu, Space.Location.COMPILATION_UNIT_PREFIX, p);
        visit(cu.getStatements(), p);
        afterSyntax(cu, p);
        visitSpace(cu.getEof(), Space.Location.COMPILATION_UNIT_EOF, p);
        return cu;
    }

    @Override
    public Jcl visitComment(Jcl.Comment comment, PrintOutputCapture<P> p) {
        beforeSyntax(comment, Space.Location.COMMENT_PREFIX, p);
        visit(comment.getWord(), p);
        afterSyntax(comment, p);
        return comment;
    }

    @Override
    public Jcl visitControlM(Jcl.ControlM controlM, PrintOutputCapture<P> p) {
        beforeSyntax(controlM, Space.Location.CONTROL_M_PREFIX, p);
        visit(controlM.getWord(), p);
        afterSyntax(controlM, p);
        return controlM;
    }

    @Override
    public Jcl visitDataDefinitionStream(Jcl.DataDefinitionStream ddStream, PrintOutputCapture<P> p) {
        beforeSyntax(ddStream, Space.Location.DATA_DEFINITION_STREAM_PREFIX, p);
        visit(ddStream.getWord(), p);
        afterSyntax(ddStream, p);
        return ddStream;
    }

    @Override
    public Jcl visitJclStatement(Jcl.JclStatement jclStatement, PrintOutputCapture<P> p) {
        beforeSyntax(jclStatement, Space.Location.JCL_STATEMENT_PREFIX, p);
        visit(jclStatement.getWord(), p);
        afterSyntax(jclStatement, p);
        return jclStatement;
    }

    @Override
    public Jcl visitJes2(Jcl.Jes2 jes2, PrintOutputCapture<P> p) {
        beforeSyntax(jes2, Space.Location.JES2_PREFIX, p);
        visit(jes2.getWord(), p);
        afterSyntax(jes2, p);
        return jes2;
    }

    @Override
    public Jcl visitJes3(Jcl.Jes3 jes3, PrintOutputCapture<P> p) {
        beforeSyntax(jes3, Space.Location.JES2_PREFIX, p);
        visit(jes3.getWord(), p);
        afterSyntax(jes3, p);
        return jes3;
    }

    @Override
    public Jcl visitUnknown(Jcl.Unknown unknown, PrintOutputCapture<P> p) {
        beforeSyntax(unknown, Space.Location.UNKNOWN_PREFIX, p);
        visit(unknown.getWord(), p);
        afterSyntax(unknown, p);
        return unknown;
    }

    @Override
    public Jcl visitWord(Jcl.Word word, PrintOutputCapture<P> p) {
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

    protected void beforeSyntax(Jcl c, Space.Location loc, PrintOutputCapture<P> p) {
        beforeSyntax(c.getPrefix(), c.getMarkers(), loc, p);
    }

    protected void beforeSyntax(Space prefix, Markers markers, Space.@Nullable Location loc, PrintOutputCapture<P> p) {
        for (Marker marker : markers.getMarkers()) {
            p.out.append(p.getMarkerPrinter().beforePrefix(marker, new Cursor(getCursor(), marker), JCL_MARKER_WRAPPER));
        }
        if (loc != null) {
            visitSpace(prefix, loc, p);
        }
        visitMarkers(markers, p);
        for (Marker marker : markers.getMarkers()) {
            p.out.append(p.getMarkerPrinter().beforeSyntax(marker, new Cursor(getCursor(), marker), JCL_MARKER_WRAPPER));
        }
    }

    protected void afterSyntax(Jcl c, PrintOutputCapture<P> p) {
        afterSyntax(c.getMarkers(), p);
    }

    protected void afterSyntax(Markers markers, PrintOutputCapture<P> p) {
        for (Marker marker : markers.getMarkers()) {
            if (marker instanceof TrailingComment) {
                TrailingComment tc = (TrailingComment) marker;
                visitSpace(tc.getPrefix(), Space.Location.TRAILING_COMMENT_PREFIX, p);
                p.append(tc.getComment());
            } else if (marker instanceof CommentArea) {
                CommentArea ca = (CommentArea) marker;
                visitSpace(ca.getPrefix(), Space.Location.COMMENT_AREA_PREFIX, p);
                p.append(ca.getComment());
            }
            p.out.append(p.getMarkerPrinter().afterSyntax(marker, new Cursor(getCursor(), marker), JCL_MARKER_WRAPPER));
        }
    }
}
