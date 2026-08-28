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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.mainframe.jcl.marker.ResolvedText;
import org.openrewrite.mainframe.jcl.tree.Jcl;
import org.openrewrite.mainframe.jcl.tree.Parameter;
import org.openrewrite.mainframe.jcl.tree.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static java.util.Collections.emptyList;

/**
 * Walking the statements around one, which is how a job stream says what belongs to what.
 * <p>
 * JCL is written as a flat run of cards and means a hierarchy: a DD belongs to the step above it, a
 * concatenated DD to the DD above that, and nothing but position says so. There are no brackets to
 * put in the tree, so the tree stays flat and the relationships are read from the cursor.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class Steps {

    /**
     * The statements after {@code cursor}'s, up to the one that ends its step.
     */
    static List<Statement> withinStep(Cursor cursor) {
        return following(cursor, Steps::endsStep);
    }

    /**
     * The statements after {@code cursor}'s, up to the next named DD or the end of the step. These
     * are what belongs to a DD: the DDs concatenated onto it, and any data written in the stream.
     */
    static List<Statement> withinDataDefinition(Cursor cursor) {
        return following(cursor, statement -> endsStep(statement) || isNamedDataDefinition(statement));
    }

    /**
     * The step a statement belongs to, or null for one written before any step. The inverse of
     * {@link Step#getDataDefinitions()}, and read the same way: backwards through the cards until
     * one of them turns out to be the EXEC.
     */
    static @Nullable Step stepOf(Cursor cursor) {
        List<Statement> statements = enclosing(cursor);
        for (int i = indexOf(statements, cursor.getValue()) - 1; i >= 0; i--) {
            Statement statement = statements.get(i);
            if (isOperation(statement, "EXEC")) {
                return new Step(new Cursor(cursor.getParentOrThrow(), statement));
            }
            if (isOperation(statement, "JOB") || isOperation(statement, "PEND")) {
                return null;
            }
        }
        return null;
    }

    /**
     * Whether an unnamed DD continues the one before it rather than standing on its own. It only
     * continues something if there is something to continue: an unnamed DD with no DD before it in
     * the step is a DD in its own right, and dropping it would lose a data set nothing else names.
     */
    static boolean continuesADataDefinition(Cursor cursor) {
        List<Statement> statements = enclosing(cursor);
        for (int i = indexOf(statements, cursor.getValue()) - 1; i >= 0; i--) {
            Statement statement = statements.get(i);
            if (isOperation(statement, "DD")) {
                return true;
            }
            if (endsStep(statement)) {
                return false;
            }
        }
        return false;
    }

    private static List<Statement> following(Cursor cursor, Predicate<Statement> stop) {
        List<Statement> statements = enclosing(cursor);
        int from = indexOf(statements, cursor.getValue());
        if (from < 0) {
            return emptyList();
        }
        List<Statement> within = new ArrayList<>();
        for (int i = from + 1; i < statements.size() && !stop.test(statements.get(i)); i++) {
            within.add(statements.get(i));
        }
        return within;
    }

    /**
     * The run of cards a statement is written in: the member's own, or the body of the procedure or
     * INCLUDE member expanded into it. A card resolved out of a procedure belongs to the cards of
     * that procedure and to nothing in the calling member.
     */
    static List<Statement> enclosing(Cursor cursor) {
        Cursor parent = cursor.getParent();
        if (parent != null && parent.getValue() instanceof Jcl.Expansion) {
            return ((Jcl.Expansion) parent.getValue()).getStatements();
        }
        Jcl.CompilationUnit cu = cursor.firstEnclosing(Jcl.CompilationUnit.class);
        return cu == null ? emptyList() : cu.getStatements();
    }

    /**
     * The expansion a statement was resolved into, or null for one written in the member itself.
     */
    static Jcl.@Nullable Expansion expansionOf(Cursor cursor) {
        Cursor parent = cursor.getParent();
        return parent != null && parent.getValue() instanceof Jcl.Expansion ?
                (Jcl.Expansion) parent.getValue() : null;
    }

    /**
     * What a parameter says once its symbols are filled in, which is what it says at all when the
     * job runs. The same as the text as written wherever nothing was substituted.
     */
    static String resolved(Parameter parameter) {
        return parameter.getMarkers().findFirst(ResolvedText.class)
                .map(ResolvedText::getText)
                .orElseGet(() -> parameter instanceof Jcl.KeywordParameter ?
                        ((Jcl.KeywordParameter) parameter).getValueText() :
                        ((Jcl.PositionalParameter) parameter).getValueText());
    }

    private static int indexOf(List<Statement> statements, Object statement) {
        for (int i = 0; i < statements.size(); i++) {
            if (statements.get(i) == statement) {
                return i;
            }
        }
        return -1;
    }

    static boolean isOperation(Statement statement, String operation) {
        return statement instanceof Jcl.JobControlStatement &&
               ((Jcl.JobControlStatement) statement).isOperation(operation);
    }

    static boolean isNamedDataDefinition(Statement statement) {
        return isOperation(statement, "DD") &&
               !((Jcl.JobControlStatement) statement).getSimpleName().isEmpty();
    }

    private static boolean endsStep(Statement statement) {
        return isOperation(statement, "EXEC") || isOperation(statement, "JOB") ||
               isOperation(statement, "PEND") || statement instanceof Jcl.NullStatement;
    }
}
