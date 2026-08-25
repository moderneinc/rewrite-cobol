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
package org.openrewrite.jcl;

import org.openrewrite.PrintOutputCapture;
import org.openrewrite.jcl.internal.JclPrinter;
import org.openrewrite.jcl.marker.ExpandedMember;
import org.openrewrite.jcl.marker.ResolvedText;
import org.openrewrite.jcl.marker.TrailingComment;
import org.openrewrite.jcl.tree.Jcl;
import org.openrewrite.jcl.tree.Space;
import org.openrewrite.marker.Marker;
import org.openrewrite.marker.Markers;

import java.util.List;

/**
 * Prints a job the way it runs rather than the way it was written: procedures and INCLUDE members
 * in place, symbols filled in, DD overrides applied. Each body brought in is bracketed by a
 * {@code //*+} comment naming the member it came from, so the listing says where every card came
 * from and is still JCL that could be submitted.
 * <p>
 * Two things the resolved listing does not preserve. Columns 73-80 are dropped, since they carry
 * sequence numbers rather than JCL; and a card whose symbols resolve to something longer than they
 * were written may run past column 71, which is inherent to substitution and is what the original
 * member is for.
 */
public class ExpandedPrinter<P> extends JclPrinter<P> {

    public static String print(Jcl.CompilationUnit cu) {
        PrintOutputCapture<Integer> out = new PrintOutputCapture<>(0);
        new ExpandedPrinter<Integer>().visit(cu, out);
        return out.getOut();
    }

    @Override
    public Jcl visitExpansion(Jcl.Expansion expansion, PrintOutputCapture<P> p) {
        String kind = expansion.getKind() == Jcl.Expansion.Kind.PROCEDURE ? "PROC" : "INCLUDE";
        p.append("\n//*+ BEGIN " + kind + " " + expansion.getMemberName());
        visit(expansion.getStatements(), p);
        p.append("\n//*+ END " + kind + " " + expansion.getMemberName());
        return expansion;
    }

    @Override
    public Jcl visitJobControlStatement(Jcl.JobControlStatement statement, PrintOutputCapture<P> p) {
        super.visitJobControlStatement(statement, p);
        statement.getMarkers().findFirst(ExpandedMember.class)
                .filter(member -> member.getStatus() == ExpandedMember.Status.MISSING)
                .ifPresent(member -> p.append("\n//*+ MEMBER " + member.getMemberName() + " NOT FOUND"));
        return statement;
    }

    @Override
    public Jcl visitKeywordParameter(Jcl.KeywordParameter parameter, PrintOutputCapture<P> p) {
        ResolvedText resolved = parameter.getMarkers().findFirst(ResolvedText.class).orElse(null);
        if (resolved == null) {
            return super.visitKeywordParameter(parameter, p);
        }
        beforeSyntax(parameter, Space.Location.PARAMETER_PREFIX, p);
        visit(parameter.getKeyword(), p);
        p.append("=" + resolved.getText() + separator(parameter.getValue()));
        afterSyntax(parameter, p);
        return parameter;
    }

    @Override
    public Jcl visitPositionalParameter(Jcl.PositionalParameter parameter, PrintOutputCapture<P> p) {
        ResolvedText resolved = parameter.getMarkers().findFirst(ResolvedText.class).orElse(null);
        if (resolved == null) {
            return super.visitPositionalParameter(parameter, p);
        }
        beforeSyntax(parameter, Space.Location.PARAMETER_PREFIX, p);
        p.append(resolved.getText() + separator(parameter.getValue()));
        afterSyntax(parameter, p);
        return parameter;
    }

    @Override
    public Jcl visitDataDefinitionStream(Jcl.DataDefinitionStream data, PrintOutputCapture<P> p) {
        ResolvedText resolved = data.getMarkers().findFirst(ResolvedText.class).orElse(null);
        if (resolved == null) {
            return super.visitDataDefinitionStream(data, p);
        }
        beforeSyntax(data, Space.Location.DATA_DEFINITION_STREAM_PREFIX, p);
        visitSpace(data.getWord().getPrefix(), Space.Location.WORD_PREFIX, p);
        p.append(resolved.getText());
        afterSyntax(data, p);
        return data;
    }

    /**
     * Columns 73-80 are a sequence number, not part of the statement, and a resolved card is not
     * the card that was numbered.
     */
    @Override
    protected void afterSyntax(Markers markers, PrintOutputCapture<P> p) {
        for (Marker marker : markers.getMarkers()) {
            if (marker instanceof TrailingComment) {
                TrailingComment comment = (TrailingComment) marker;
                visitSpace(comment.getPrefix(), Space.Location.TRAILING_COMMENT_PREFIX, p);
                p.append(comment.getComment());
            }
        }
    }

    private static String separator(List<Jcl.Word> value) {
        return value.get(value.size() - 1).getText().endsWith(",") ? "," : "";
    }
}
