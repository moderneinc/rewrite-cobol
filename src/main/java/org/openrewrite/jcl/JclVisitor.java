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
package org.openrewrite.jcl;

import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.jcl.tree.Jcl;
import org.openrewrite.jcl.tree.Space;

public class JclVisitor<P> extends TreeVisitor<Jcl, P> {

    public Jcl visitCompilationUnit(Jcl.CompilationUnit compilationUnit, P p) {
        Jcl.CompilationUnit c = compilationUnit;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.COMPILATION_UNIT_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withStatements(ListUtils.map(c.getStatements(), e -> visitAndCast(e, p)));
        return c.withEof(visitSpace(c.getEof(), Space.Location.COMPILATION_UNIT_EOF, p));
    }

    public Jcl visitComment(Jcl.Comment comment, P p) {
        Jcl.Comment c = comment;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.COMMENT_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        return c.withWord(visitAndCast(c.getWord(), p));
    }

    public Jcl visitControlM(Jcl.ControlM controlM, P p) {
        Jcl.ControlM c = controlM;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.CONTROL_M_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        return c.withWord(visitAndCast(c.getWord(), p));
    }

    public Jcl visitDataDefinitionStream(Jcl.DataDefinitionStream ddStream, P p) {
        Jcl.DataDefinitionStream d = ddStream;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DATA_DEFINITION_STREAM_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        return d.withWord(visitAndCast(d.getWord(), p));
    }

    public Jcl visitJobControlStatement(Jcl.JobControlStatement statement, P p) {
        Jcl.JobControlStatement j = statement;
        j = j.withPrefix(visitSpace(j.getPrefix(), Space.Location.JOB_CONTROL_STATEMENT_PREFIX, p));
        j = j.withMarkers(visitMarkers(j.getMarkers(), p));
        j = j.withName(visitAndCast(j.getName(), p));
        if (j.getOperation() != null) {
            j = j.withOperation(visitAndCast(j.getOperation(), p));
        }
        return j.withOperands(ListUtils.map(j.getOperands(), o -> visitAndCast(o, p)));
    }

    public Jcl visitKeywordParameter(Jcl.KeywordParameter parameter, P p) {
        Jcl.KeywordParameter k = parameter;
        k = k.withPrefix(visitSpace(k.getPrefix(), Space.Location.PARAMETER_PREFIX, p));
        k = k.withMarkers(visitMarkers(k.getMarkers(), p));
        k = k.withKeyword(visitAndCast(k.getKeyword(), p));
        return k.withValue(ListUtils.map(k.getValue(), w -> visitAndCast(w, p)));
    }

    public Jcl visitPositionalParameter(Jcl.PositionalParameter parameter, P p) {
        Jcl.PositionalParameter pp = parameter;
        pp = pp.withPrefix(visitSpace(pp.getPrefix(), Space.Location.PARAMETER_PREFIX, p));
        pp = pp.withMarkers(visitMarkers(pp.getMarkers(), p));
        return pp.withValue(ListUtils.map(pp.getValue(), w -> visitAndCast(w, p)));
    }

    public Jcl visitJes2(Jcl.Jes2 jes2, P p) {
        Jcl.Jes2 j = jes2;
        j = j.withPrefix(visitSpace(j.getPrefix(), Space.Location.JES2_PREFIX, p));
        j = j.withMarkers(visitMarkers(j.getMarkers(), p));
        return j.withWord(visitAndCast(j.getWord(), p));
    }

    public Jcl visitJes3(Jcl.Jes3 jes3, P p) {
        Jcl.Jes3 j = jes3;
        j = j.withPrefix(visitSpace(j.getPrefix(), Space.Location.JES2_PREFIX, p));
        j = j.withMarkers(visitMarkers(j.getMarkers(), p));
        return j.withWord(visitAndCast(j.getWord(), p));
    }

    public Jcl visitUnknown(Jcl.Unknown unknown, P p) {
        Jcl.Unknown u = unknown;
        u = u.withPrefix(visitSpace(u.getPrefix(), Space.Location.UNKNOWN_PREFIX, p));
        u = u.withMarkers(visitMarkers(u.getMarkers(), p));
        return u.withWord(visitAndCast(u.getWord(), p));
    }

    public Jcl visitWord(Jcl.Word word, P p) {
        Jcl.Word w = word;
        w = w.withPrefix(visitSpace(w.getPrefix(), Space.Location.WORD_PREFIX, p));
        return w.withMarkers(visitMarkers(w.getMarkers(), p));
    }

    public Space visitSpace(Space space, Space.Location location, P p) {
        return space;
    }
}
