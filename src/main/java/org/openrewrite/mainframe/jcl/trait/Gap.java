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

/**
 * One reason a step does not read as something a transformation may be applied to, said in fields
 * rather than in a sentence so a report can group and count them.
 *
 * @see ExecutionPath
 */
@Value
public class Gap {

    Kind kind;

    /**
     * The step the gap was found in, qualified by the step that called its procedure — a gap in
     * {@code EXTRACT.RUN} was found in the procedure that {@code EXTRACT} calls.
     */
    String step;

    /**
     * The DD the gap was found under, or empty where it was not found under one.
     */
    String ddName;

    /**
     * What the gap is about: the procedure, the symbol, the member, the keyword. Empty for
     * {@link Kind#CARDS_NOT_READ}, where the DD named above is itself what the gap is about.
     */
    String name;

    /**
     * Why a step does not read. Each says which verdict it carries the path to, and the reasons that
     * leave a name unfollowed are not the same as the one that follows every name and still cannot
     * say what the step does.
     */
    public enum Kind {

        /** The step calls a procedure that was not in the procedure library supplied. */
        PROCEDURE_MISSING(ExecutionPath.Verdict.UNRESOLVED),

        /** A symbol the step is written with is referred to and never set. */
        SYMBOL_UNDEFINED(ExecutionPath.Verdict.UNRESOLVED),

        /** A DD names a library member that was not supplied. */
        MEMBER_MISSING(ExecutionPath.Verdict.UNRESOLVED),

        /**
         * The cards a DD hands the step open a control statement of a language read here, and could
         * not be read as one.
         */
        CARDS_NOT_READ(ExecutionPath.Verdict.UNRESOLVED),

        /**
         * A deck leaves a keyword to a setting written outside the application libraries — the site
         * parmlib of the unload product answers {@code FORMAT}, {@code DB2}, {@code LOCK} and the
         * rest for every deck that does not code them. Every name in the job resolves and the job
         * still writes a different file when that setting changes.
         */
        KEYWORD_INHERITED(ExecutionPath.Verdict.INHERITED);

        private final ExecutionPath.Verdict verdict;

        Kind(ExecutionPath.Verdict verdict) {
            this.verdict = verdict;
        }

        public ExecutionPath.Verdict getVerdict() {
            return verdict;
        }
    }

    @Override
    public String toString() {
        return kind + " " + step + (ddName.isEmpty() ? "" : "." + ddName) +
               (name.isEmpty() ? "" : " " + name);
    }
}
