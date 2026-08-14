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
package org.openrewrite.jcl.model;

import lombok.Value;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * One EXEC statement and the DD statements belonging to it.
 * <p>
 * A step is the unit that runs a program against data sets, so it is the unit the batch half of a
 * portfolio is described in: this program, these files, in this job, in this order.
 */
@Value
public class Step {

    /**
     * The step name, or empty for an unnamed step. Unnamed steps cannot be referred to by later
     * steps or by a scheduler, which is worth knowing when a job has to be split.
     */
    String name;

    /**
     * The program run, from {@code PGM=}. Null when the step invokes a procedure instead.
     */
    @Nullable
    String program;

    /**
     * The procedure invoked, from {@code PROC=} or from a bare positional operand. Null when the
     * step names a program.
     */
    @Nullable
    String procedure;

    List<DataDefinition> dataDefinitions;

    /**
     * Every EXEC parameter as written, keyed by upper cased keyword: {@code PARM}, {@code COND},
     * {@code REGION}, {@code TIME}.
     */
    Map<String, String> parameters;

    /**
     * The DD of this step with the given name, or null. DD names are unique within a step, which is
     * what makes this the join a program's {@code ASSIGN} clause needs.
     */
    public @Nullable DataDefinition dd(String ddName) {
        for (DataDefinition dd : dataDefinitions) {
            if (dd.getName().equalsIgnoreCase(ddName)) {
                return dd;
            }
        }
        return null;
    }
}
