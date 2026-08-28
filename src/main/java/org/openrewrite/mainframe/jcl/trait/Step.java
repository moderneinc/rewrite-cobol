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
package org.openrewrite.mainframe.jcl.trait;

import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.mainframe.jcl.marker.ExpandedMember;
import org.openrewrite.mainframe.jcl.marker.Symbolic;
import org.openrewrite.mainframe.jcl.marker.Symbolics;
import org.openrewrite.mainframe.jcl.tree.Jcl;
import org.openrewrite.mainframe.jcl.tree.Parameter;
import org.openrewrite.mainframe.jcl.tree.Statement;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * A job step: an EXEC statement and the DD statements written under it.
 * <p>
 * A step is the unit that runs a program against data sets, so it is the unit the batch half of a
 * portfolio is described in — this program, these files, in this job, in this order.
 * <p>
 * A step that calls a procedure runs the procedure's steps, not one of its own, and those are read
 * through {@link #getProcedureSteps()} — with the overrides written under this EXEC already applied
 * and its symbols already filled in. The DD cards written under such a step are its overrides, so
 * {@link #getDataDefinitions()} still reports them as written.
 */
@Value
public class Step implements Trait<Jcl.JobControlStatement> {

    Cursor cursor;

    /**
     * The step name, or empty for an unnamed step. An unnamed step cannot be referred to by a later
     * step or by a scheduler, which is worth knowing when a job has to be split.
     */
    public String getName() {
        return getTree().getSimpleName();
    }

    /**
     * The step name qualified by the step that called the procedure it is written in, the way a
     * step of an expanded procedure is named in a listing: {@code EXTRACT.RUN}. The name on its own
     * for a step written in the member itself.
     */
    public String getQualifiedName() {
        Step caller = getCallingStep();
        return caller == null ? getName() : caller.getName() + "." + getName();
    }

    /**
     * The program run, from {@code PGM=}, with its symbols filled in — a procedure step written
     * {@code PGM=&PGM} answers the program the calling job passed. Null when the step calls a
     * procedure instead.
     */
    public @Nullable String getProgram() {
        Jcl.KeywordParameter pgm = getTree().getParameter("PGM");
        return pgm == null || getProcedure() != null ? null : Steps.resolved(pgm);
    }

    /**
     * The procedure invoked. {@code EXEC MYPROC} and {@code EXEC PROC=MYPROC} say the same thing, so
     * both are read here. Null when the step names a program.
     * <p>
     * A step that names a procedure may still carry {@code PGM=}, and it is a symbol the procedure
     * reads rather than a program this step runs — {@code EXEC CLMBATCH,PGM=CLMB010} runs whatever
     * the procedure's own {@code EXEC PGM=&PGM} names.
     */
    public @Nullable String getProcedure() {
        Jcl.KeywordParameter proc = getTree().getParameter("PROC");
        if (proc != null) {
            return Steps.resolved(proc);
        }
        Jcl.PositionalParameter positional = getTree().getPositionalParameter();
        return positional == null ? null : Steps.resolved(positional);
    }

    /**
     * The steps this one runs through the procedure it calls, in order, or empty when it names a
     * program or the procedure was not supplied. Their DD statements already have this step's
     * overrides applied.
     */
    public List<Step> getProcedureSteps() {
        Jcl.Expansion expansion = getExpansion();
        if (expansion == null) {
            return emptyList();
        }
        List<Step> steps = new ArrayList<>();
        Cursor within = new Cursor(cursor.getParentOrThrow(), expansion);
        for (Statement statement : expansion.getStatements()) {
            if (statement instanceof Jcl.JobControlStatement &&
                ((Jcl.JobControlStatement) statement).isOperation("EXEC")) {
                steps.add(new Step(new Cursor(within, statement)));
            }
        }
        return steps;
    }

    /**
     * The member this step was resolved out of — a procedure, or an INCLUDE group that holds steps
     * of its own — or null for a step written in the member being read. No EXEC card of this member
     * was written for a step that has one.
     */
    public @Nullable String getExpandedFrom() {
        Jcl.Expansion expansion = Steps.expansionOf(cursor);
        return expansion == null ? null : expansion.getMemberName();
    }

    /**
     * The step that called the procedure this one is written in, or null for a step written in the
     * member itself or brought in by an INCLUDE.
     */
    public @Nullable Step getCallingStep() {
        Jcl.Expansion expansion = Steps.expansionOf(cursor);
        if (expansion == null) {
            return null;
        }
        Cursor within = cursor.getParentOrThrow();
        Statement previous = null;
        for (Statement statement : Steps.enclosing(within)) {
            if (statement == expansion) {
                break;
            }
            previous = statement;
        }
        return previous instanceof Jcl.JobControlStatement &&
               ((Jcl.JobControlStatement) previous).isOperation("EXEC") ?
                new Step(new Cursor(within.getParentOrThrow(), previous)) : null;
    }

    /**
     * Every symbol in effect for this step, each with its value and the statement kind that gave it
     * that value — the procedure's own default, the calling EXEC's override, a SET in the job or in
     * a member it included. Empty when the member sets none.
     */
    public List<Symbolic> getSymbolics() {
        return getTree().getMarkers().findFirst(Symbolics.class)
                .map(Symbolics::getSymbolics)
                .orElse(emptyList());
    }

    public @Nullable Symbolic getSymbolic(String name) {
        for (Symbolic symbolic : getSymbolics()) {
            if (symbolic.getName().equalsIgnoreCase(name)) {
                return symbolic;
            }
        }
        return null;
    }

    /**
     * Whether this step calls a procedure that was not in the supplied procedure library — a job
     * whose steps cannot be read is exactly what a portfolio with a gap in it looks like.
     */
    public boolean isProcedureMissing() {
        return getTree().getMarkers().findFirst(ExpandedMember.class)
                .filter(member -> member.getStatus() == ExpandedMember.Status.MISSING)
                .isPresent();
    }

    private Jcl.@Nullable Expansion getExpansion() {
        boolean after = false;
        for (Statement statement : Steps.enclosing(cursor)) {
            if (statement == getTree()) {
                after = true;
            } else if (after) {
                return statement instanceof Jcl.Expansion ? (Jcl.Expansion) statement : null;
            }
        }
        return null;
    }

    /**
     * The DD statements of this step, concatenations counted once.
     */
    public List<DataDefinition> getDataDefinitions() {
        List<DataDefinition> dds = new ArrayList<>();
        DataDefinition.Matcher matcher = new DataDefinition.Matcher();
        for (Statement statement : Steps.withinStep(cursor)) {
            matcher.get(new Cursor(cursor.getParentOrThrow(), statement)).ifPresent(dds::add);
        }
        return dds;
    }

    /**
     * The DD of this step with the given name, or null. DD names are unique within a step, which is
     * what makes this the join a program's {@code ASSIGN} clause needs.
     */
    public @Nullable DataDefinition getDataDefinition(String ddName) {
        for (DataDefinition dd : getDataDefinitions()) {
            if (dd.getName().equalsIgnoreCase(ddName)) {
                return dd;
            }
        }
        return null;
    }

    public static class Matcher extends SimpleTraitMatcher<Step> {

        @Override
        protected @Nullable Step test(Cursor cursor) {
            Object value = cursor.getValue();
            return value instanceof Jcl.JobControlStatement &&
                   ((Jcl.JobControlStatement) value).isOperation("EXEC") ? new Step(cursor) : null;
        }
    }

    @Override
    public String toString() {
        return "STEP " + getName();
    }
}
