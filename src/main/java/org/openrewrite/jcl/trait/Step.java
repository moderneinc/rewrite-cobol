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
package org.openrewrite.jcl.trait;

import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.jcl.tree.Jcl;
import org.openrewrite.jcl.tree.Parameter;
import org.openrewrite.jcl.tree.Statement;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.util.ArrayList;
import java.util.List;

/**
 * A job step: an EXEC statement and the DD statements written under it.
 * <p>
 * A step is the unit that runs a program against data sets, so it is the unit the batch half of a
 * portfolio is described in — this program, these files, in this job, in this order.
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
     * The program run, from {@code PGM=}. Null when the step invokes a procedure instead.
     */
    public @Nullable String getProgram() {
        Jcl.KeywordParameter pgm = getTree().getParameter("PGM");
        return pgm == null ? null : pgm.getValueText();
    }

    /**
     * The procedure invoked. {@code EXEC MYPROC} and {@code EXEC PROC=MYPROC} say the same thing, so
     * both are read here. Null when the step names a program.
     */
    public @Nullable String getProcedure() {
        Jcl.KeywordParameter proc = getTree().getParameter("PROC");
        if (proc != null) {
            return proc.getValueText();
        }
        if (getProgram() != null) {
            return null;
        }
        for (Parameter parameter : getTree().getParameters()) {
            if (parameter instanceof Jcl.PositionalParameter) {
                return ((Jcl.PositionalParameter) parameter).getValueText();
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
