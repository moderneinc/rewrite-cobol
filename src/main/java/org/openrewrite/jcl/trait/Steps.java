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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.openrewrite.Cursor;
import org.openrewrite.jcl.tree.Jcl;
import org.openrewrite.jcl.tree.Statement;

import java.util.ArrayList;
import java.util.List;

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
     * Whether an unnamed DD continues the one before it rather than standing on its own. It only
     * continues something if there is something to continue: an unnamed DD with no DD before it in
     * the step is a DD in its own right, and dropping it would lose a data set nothing else names.
     */
    static boolean continuesADataDefinition(Cursor cursor) {
        Jcl.CompilationUnit cu = cursor.firstEnclosing(Jcl.CompilationUnit.class);
        if (cu == null) {
            return false;
        }
        Object self = cursor.getValue();
        List<Statement> statements = cu.getStatements();
        int from = statements.indexOf(self);
        for (int i = from - 1; i >= 0; i--) {
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

    private static List<Statement> following(Cursor cursor, java.util.function.Predicate<Statement> stop) {
        Jcl.CompilationUnit cu = cursor.firstEnclosing(Jcl.CompilationUnit.class);
        if (cu == null) {
            return emptyList();
        }
        Object self = cursor.getValue();
        List<Statement> statements = cu.getStatements();
        int from = -1;
        for (int i = 0; i < statements.size(); i++) {
            if (statements.get(i) == self) {
                from = i;
                break;
            }
        }
        if (from < 0) {
            return emptyList();
        }
        List<Statement> within = new ArrayList<>();
        for (int i = from + 1; i < statements.size() && !stop.test(statements.get(i)); i++) {
            within.add(statements.get(i));
        }
        return within;
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
