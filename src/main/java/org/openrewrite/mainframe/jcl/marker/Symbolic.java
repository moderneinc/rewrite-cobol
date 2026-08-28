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
package org.openrewrite.mainframe.jcl.marker;

import lombok.Value;
import lombok.With;
import org.jspecify.annotations.Nullable;

/**
 * A JCL symbol and the value it stands for, with the statement kind that gave it that value.
 * <p>
 * The same job run in another environment reads different data sets, so a data set name is only
 * half a fact until the symbols in it are filled in — and which statement supplied a value is what
 * says whether changing it is a job change, a library change, or a scheduler change.
 */
@With
@Value
public class Symbolic {

    /**
     * The symbol, without its leading {@code &}: {@code HLQ}, {@code SYSUID}.
     */
    String name;

    /**
     * What it stands for, or null when nothing in the job says — a system symbol, or one referred
     * to and never set.
     */
    @Nullable
    String value;

    Origin origin;

    public enum Origin {
        /** A {@code SET} statement in the job, or in a member it included. */
        SET,
        /** The default on the {@code PROC} statement of the procedure being run. */
        PROCEDURE,
        /** A parameter on the {@code EXEC} statement that called the procedure. */
        OVERRIDE,
        /** A system symbol such as {@code &SYSUID}, whose value the system supplies at run time. */
        SYSTEM,
        /** Referred to and never set. */
        UNDEFINED
    }
}
