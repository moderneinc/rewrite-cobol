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
package org.openrewrite.mainframe.bms.trait;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.mainframe.bms.tree.Bms;
import org.openrewrite.mainframe.bms.tree.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static java.util.Collections.emptyList;

/**
 * Walking the statements around one, which is how a mapset says what belongs to what.
 * <p>
 * A mapset is written as a flat run of macro invocations and means a hierarchy: a {@code DFHMDF}
 * belongs to the {@code DFHMDI} above it and that to the {@code DFHMSD} above that. Nothing but
 * position says so, so the tree stays flat and the relationships are read from the cursor.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class Maps {

    /**
     * The statements after {@code cursor}'s, up to the one that ends its mapset.
     */
    static List<Statement> withinMapset(Cursor cursor) {
        return following(cursor, Maps::endsMapset);
    }

    /**
     * The statements after {@code cursor}'s, up to the next map or the end of the mapset.
     */
    static List<Statement> withinMap(Cursor cursor) {
        return following(cursor, statement -> endsMapset(statement) || isOperation(statement, "DFHMDI"));
    }

    /**
     * The map a field belongs to, or null for a field written before any. Read backwards through the
     * statements until one of them turns out to be the {@code DFHMDI}.
     */
    static @Nullable MapDefinition mapOf(Cursor cursor) {
        return preceding(cursor, "DFHMDI", MapDefinition::new);
    }

    /**
     * The mapset a map belongs to, or null for a map written before any.
     */
    static @Nullable Mapset mapsetOf(Cursor cursor) {
        return preceding(cursor, "DFHMSD", Mapset::new);
    }

    private static <T> @Nullable T preceding(Cursor cursor, String operation,
                                             java.util.function.Function<Cursor, T> as) {
        Bms.CompilationUnit cu = cursor.firstEnclosing(Bms.CompilationUnit.class);
        if (cu == null) {
            return null;
        }
        List<Statement> statements = cu.getStatements();
        for (int i = indexOf(statements, cursor.getValue()) - 1; i >= 0; i--) {
            Statement statement = statements.get(i);
            if (isOperation(statement, operation) &&
                !((Bms.MacroStatement) statement).getSimpleName().isEmpty()) {
                return as.apply(new Cursor(cursor.getParentOrThrow(), statement));
            }
            if (isOperation(statement, "END")) {
                return null;
            }
        }
        return null;
    }

    private static List<Statement> following(Cursor cursor, Predicate<Statement> stop) {
        Bms.CompilationUnit cu = cursor.firstEnclosing(Bms.CompilationUnit.class);
        if (cu == null) {
            return emptyList();
        }
        List<Statement> statements = cu.getStatements();
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

    private static int indexOf(List<Statement> statements, Object statement) {
        for (int i = 0; i < statements.size(); i++) {
            if (statements.get(i) == statement) {
                return i;
            }
        }
        return -1;
    }

    static boolean isOperation(Statement statement, String operation) {
        return statement instanceof Bms.MacroStatement &&
               ((Bms.MacroStatement) statement).isOperation(operation);
    }

    /**
     * {@code DFHMSD TYPE=FINAL} ends a mapset and so does the next {@code DFHMSD} opening one, which
     * is why this does not look at the name field.
     */
    private static boolean endsMapset(Statement statement) {
        return isOperation(statement, "DFHMSD") || isOperation(statement, "END");
    }
}
