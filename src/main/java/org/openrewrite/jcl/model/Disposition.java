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

import java.util.Locale;

/**
 * A DD statement's {@code DISP} parameter.
 * <p>
 * The status sub-parameter is what says whether a step reads a data set or produces it, which makes
 * it the difference between a job that consumes a file and one that owns it.
 */
@Value
public class Disposition {

    Status status;

    /**
     * What becomes of the data set when the step ends normally: {@code KEEP}, {@code DELETE},
     * {@code CATLG}, {@code UNCATLG}, {@code PASS}. Null when not given.
     */
    @Nullable
    String normal;

    /**
     * What becomes of the data set when the step abends. Null when not given.
     */
    @Nullable
    String abnormal;

    public enum Status {
        /**
         * The step creates the data set.
         */
        NEW,
        /**
         * The step has exclusive use of an existing data set.
         */
        OLD,
        /**
         * The step shares an existing data set with other jobs, which is how a data set comes to be
         * read by many steps at once.
         */
        SHR,
        /**
         * The step appends to an existing data set, or creates it if it does not exist.
         */
        MOD,
        /**
         * No status was given, in which case z/OS defaults to {@code NEW}. Recorded as its own value
         * rather than silently defaulted, because a report that cannot tell an omission from a
         * decision is not evidence.
         */
        UNSPECIFIED
    }

    /**
     * Whether the step reads the data set rather than producing it. {@code NEW} produces;
     * {@code MOD} does both and is reported as producing, since that is the half that changes the
     * data.
     */
    public boolean isInput() {
        return status == Status.SHR || status == Status.OLD;
    }

    /**
     * Reads a {@code DISP} operand, with or without its parentheses:
     * {@code SHR}, {@code (NEW,CATLG,DELETE)}, {@code (,PASS)}.
     */
    public static Disposition parse(String operand) {
        String value = operand.trim();
        if (value.startsWith("(")) {
            value = value.substring(1);
        }
        if (value.endsWith(")")) {
            value = value.substring(0, value.length() - 1);
        }
        String[] parts = value.split(",", -1);
        return new Disposition(
                statusOf(parts[0].trim()),
                parts.length > 1 && !parts[1].trim().isEmpty() ? parts[1].trim().toUpperCase(Locale.ROOT) : null,
                parts.length > 2 && !parts[2].trim().isEmpty() ? parts[2].trim().toUpperCase(Locale.ROOT) : null);
    }

    private static Status statusOf(String status) {
        switch (status.toUpperCase(Locale.ROOT)) {
            case "NEW":
                return Status.NEW;
            case "OLD":
                return Status.OLD;
            case "SHR":
                return Status.SHR;
            case "MOD":
                return Status.MOD;
            default:
                return Status.UNSPECIFIED;
        }
    }
}
