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
package org.openrewrite.ims.trait;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.controlcard.CardLines;
import org.openrewrite.ims.ImsIsoVisitor;
import org.openrewrite.ims.tree.Ims;
import org.openrewrite.ims.tree.Space;
import org.openrewrite.ims.tree.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Collections.emptyList;

/**
 * Walking the statements around one, which is how a gen member says what belongs to what.
 * <p>
 * A DBD is written as a flat run of macro invocations and means a hierarchy: a {@code FIELD},
 * {@code LCHILD} or {@code XDFLD} belongs to the {@code SEGM} above it and that to the {@code DBD}
 * above that. Nothing but position says so, so the tree stays flat and the relationships are read
 * from the cursor.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class Definitions {

    /**
     * The statements after {@code cursor}'s, up to the one that ends its database.
     */
    static List<Statement> withinDatabase(Cursor cursor) {
        return following(cursor, Definitions::endsDatabase);
    }

    /**
     * The statements after {@code cursor}'s, up to the next segment or the end of the database.
     */
    static List<Statement> withinSegment(Cursor cursor) {
        return following(cursor, statement -> endsDatabase(statement) || isOperation(statement, "SEGM"));
    }

    /**
     * The segment a field, logical child or index field belongs to, or null for one written before
     * any segment.
     */
    static @Nullable Segment segmentOf(Cursor cursor) {
        return preceding(cursor, "SEGM", Segment::new);
    }

    static @Nullable Database databaseOf(Cursor cursor) {
        return preceding(cursor, "DBD", Database::new);
    }

    /**
     * The one-based line of the member this word was written on. Every row a DBD contributes is
     * anchored at the line of the macro that said it.
     */
    static int lineOf(Cursor cursor, Ims.Word word) {
        return CardLines.of(cursor, Ims.CompilationUnit.class, words()).getOrDefault(word.getId(), 1);
    }

    private static <T> @Nullable T preceding(Cursor cursor, String operation, Function<Cursor, T> as) {
        Ims.CompilationUnit cu = cursor.firstEnclosing(Ims.CompilationUnit.class);
        if (cu == null) {
            return null;
        }
        List<Statement> statements = cu.getStatements();
        for (int i = indexOf(statements, cursor.getValue()) - 1; i >= 0; i--) {
            Statement statement = statements.get(i);
            if (isOperation(statement, operation)) {
                return as.apply(new Cursor(cursor.getParentOrThrow(), statement));
            }
            if (isOperation(statement, "END")) {
                return null;
            }
        }
        return null;
    }

    private static List<Statement> following(Cursor cursor, Predicate<Statement> stop) {
        Ims.CompilationUnit cu = cursor.firstEnclosing(Ims.CompilationUnit.class);
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
        return statement instanceof Ims.MacroStatement &&
               ((Ims.MacroStatement) statement).isOperation(operation);
    }

    /**
     * {@code DBDGEN} closes the database the member gens, and a second {@code DBD} would open
     * another.
     */
    private static boolean endsDatabase(Statement statement) {
        return isOperation(statement, "DBD") || isOperation(statement, "DBDGEN") ||
               isOperation(statement, "END");
    }

    /**
     * Where the words of a member are, for {@link CardLines} to count the lines between them.
     */
    private static ImsIsoVisitor<CardLines> words() {
        return new ImsIsoVisitor<CardLines>() {
            @Override
            public Space visitSpace(Space space, Space.Location location, CardLines lines) {
                lines.space(space.getWhitespace());
                return space;
            }

            @Override
            public Ims.Word visitWord(Ims.Word word, CardLines lines) {
                Ims.Word w = super.visitWord(word, lines);
                lines.word(w.getId());
                return w;
            }
        };
    }
}
