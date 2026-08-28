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
import org.openrewrite.mainframe.jcl.Operands;
import org.openrewrite.mainframe.jcl.tree.Jcl;
import org.openrewrite.mainframe.jcl.tree.Statement;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.util.ArrayList;
import java.util.List;

/**
 * A JOB statement.
 * <p>
 * A member need not have one — a procedure has none, and neither does a fragment written to be
 * included — so the steps of a member are found with {@link Step.Matcher} rather than through here.
 */
@Value
public class Job implements Trait<Jcl.JobControlStatement> {

    Cursor cursor;

    public String getName() {
        return getTree().getSimpleName();
    }

    /**
     * The message class, the accounting information, and the rest of the JOB card are read through
     * {@link Jcl.JobControlStatement#getParameter}; only the name is worth a method of its own.
     */
    public @Nullable String getParameter(String keyword) {
        Jcl.KeywordParameter parameter = getTree().getParameter(keyword);
        return parameter == null ? null : Steps.resolved(parameter);
    }

    /**
     * The data sets a {@code JCLLIB ORDER=} says to search for the job's procedures and INCLUDE
     * members, in order, symbols filled in.
     * <p>
     * They are reported rather than searched: a data set name says nothing about where the member
     * is checked in, so expansion resolves by member name across everything supplied. What the
     * order is good for is saying which library a job expects to be built against, and finding the
     * jobs that expect one that is not in the portfolio.
     */
    public List<String> getProcedureLibraries() {
        List<String> libraries = new ArrayList<>();
        for (Statement statement : cursor.firstEnclosingOrThrow(Jcl.CompilationUnit.class).getStatements()) {
            if (!(statement instanceof Jcl.JobControlStatement) ||
                !((Jcl.JobControlStatement) statement).isOperation("JCLLIB")) {
                continue;
            }
            Jcl.KeywordParameter order = ((Jcl.JobControlStatement) statement).getParameter("ORDER");
            if (order != null) {
                libraries.addAll(Operands.list(Steps.resolved(order)));
            }
        }
        return libraries;
    }

    public static class Matcher extends SimpleTraitMatcher<Job> {

        @Override
        protected @Nullable Job test(Cursor cursor) {
            Object value = cursor.getValue();
            return value instanceof Jcl.JobControlStatement &&
                   ((Jcl.JobControlStatement) value).isOperation("JOB") ? new Job(cursor) : null;
        }
    }

    @Override
    public String toString() {
        return "JOB " + getName();
    }
}
