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
import java.util.Collections;
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
 * <p>
 * A PSB is written the other way up. Its {@code PCB}s come first, each with the {@code SENSEG}s and
 * {@code SENFLD}s that belong to it, and the {@code PSBGEN} that names them all closes the member —
 * so a PSB is read backwards from its {@code PSBGEN}.
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
     * The statements before {@code cursor}'s, back to where its PSB begins, in source order.
     */
    static List<Statement> withinPsb(Cursor cursor) {
        List<Statement> within = new ArrayList<>(preceding(cursor, Definitions::endsPsb));
        Collections.reverse(within);
        return within;
    }

    /**
     * The statements after {@code cursor}'s, up to the next PCB or the end of the PSB.
     */
    static List<Statement> withinPcb(Cursor cursor) {
        return following(cursor, statement -> endsPsb(statement) || isOperation(statement, "PCB"));
    }

    /**
     * The statements after {@code cursor}'s, up to the next sensitive segment, the next PCB or the
     * end of the PSB.
     */
    static List<Statement> withinSensitiveSegment(Cursor cursor) {
        return following(cursor, statement -> endsPsb(statement) || isOperation(statement, "PCB") ||
                                              isOperation(statement, "SENSEG"));
    }

    /**
     * The statements after {@code cursor}'s, up to the next application or database of the stage 1
     * deck.
     */
    static List<Statement> withinApplication(Cursor cursor) {
        return following(cursor, Definitions::endsApplication);
    }

    /**
     * The segment a field, logical child or index field belongs to, or null for one written before
     * any segment.
     */
    static @Nullable Segment segmentOf(Cursor cursor) {
        return preceding(cursor, "SEGM", Definitions::endsDatabase, Segment::new);
    }

    static @Nullable Database databaseOf(Cursor cursor) {
        return preceding(cursor, "DBD", statement -> isOperation(statement, "END"), Database::new);
    }

    /**
     * The PCB a sensitive segment belongs to, or null for one written before any PCB.
     */
    static @Nullable Pcb pcbOf(Cursor cursor) {
        return preceding(cursor, "PCB", Definitions::endsPsb, Pcb::new);
    }

    /**
     * The sensitive segment a sensitive field belongs to, or null for one written before any.
     */
    static @Nullable SensitiveSegment sensitiveSegmentOf(Cursor cursor) {
        return preceding(cursor, "SENSEG",
                statement -> endsPsb(statement) || isOperation(statement, "PCB"), SensitiveSegment::new);
    }

    /**
     * The {@code PSBGEN} closing the PSB {@code cursor}'s statement belongs to, which is the only
     * place the PSB's name is written.
     */
    static @Nullable Psb psbOf(Cursor cursor) {
        return following(cursor, "PSBGEN", statement -> isOperation(statement, "END"), Psb::new);
    }

    /**
     * The application a transaction belongs to, or null for one written before any.
     */
    static @Nullable Application applicationOf(Cursor cursor) {
        return preceding(cursor, "APPLCTN", Definitions::endsApplication, Application::new);
    }

    /**
     * The one-based line of the member this word was written on. Every row a DBD contributes is
     * anchored at the line of the macro that said it.
     */
    static int lineOf(Cursor cursor, Ims.Word word) {
        return CardLines.of(cursor, Ims.CompilationUnit.class, words()).getOrDefault(word.getId(), 1);
    }

    /**
     * The nearest statement before {@code cursor}'s invoking {@code operation}. The statement that
     * opens the containing definition is itself the one being looked for, so a match is tested before
     * {@code stop} is.
     */
    private static <T> @Nullable T preceding(Cursor cursor, String operation, Predicate<Statement> stop,
                                             Function<Cursor, T> as) {
        List<Statement> statements = statementsOf(cursor);
        for (int i = indexOf(statements, cursor.getValue()) - 1; i >= 0; i--) {
            Statement statement = statements.get(i);
            if (isOperation(statement, operation)) {
                return as.apply(new Cursor(cursor.getParentOrThrow(), statement));
            }
            if (stop.test(statement)) {
                return null;
            }
        }
        return null;
    }

    private static <T> @Nullable T following(Cursor cursor, String operation, Predicate<Statement> stop,
                                             Function<Cursor, T> as) {
        List<Statement> statements = statementsOf(cursor);
        for (int i = indexOf(statements, cursor.getValue()) + 1; i > 0 && i < statements.size(); i++) {
            Statement statement = statements.get(i);
            if (isOperation(statement, operation)) {
                return as.apply(new Cursor(cursor.getParentOrThrow(), statement));
            }
            if (stop.test(statement)) {
                return null;
            }
        }
        return null;
    }

    private static List<Statement> following(Cursor cursor, Predicate<Statement> stop) {
        List<Statement> statements = statementsOf(cursor);
        List<Statement> within = new ArrayList<>();
        for (int i = indexOf(statements, cursor.getValue()) + 1;
             i > 0 && i < statements.size() && !stop.test(statements.get(i)); i++) {
            within.add(statements.get(i));
        }
        return within;
    }

    /**
     * The statements before {@code cursor}'s, nearest first, which is the order a walk back through
     * them wants.
     */
    private static List<Statement> preceding(Cursor cursor, Predicate<Statement> stop) {
        List<Statement> statements = statementsOf(cursor);
        List<Statement> within = new ArrayList<>();
        for (int i = indexOf(statements, cursor.getValue()) - 1;
             i >= 0 && !stop.test(statements.get(i)); i--) {
            within.add(statements.get(i));
        }
        return within;
    }

    private static List<Statement> statementsOf(Cursor cursor) {
        Ims.CompilationUnit cu = cursor.firstEnclosing(Ims.CompilationUnit.class);
        return cu == null ? emptyList() : cu.getStatements();
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
     * {@code PSBGEN} closes the PSB the member gens, whichever way it is walked: forwards it is the
     * last statement of the PSB, backwards it is the last statement of the one before.
     */
    private static boolean endsPsb(Statement statement) {
        return isOperation(statement, "PSBGEN") || isOperation(statement, "END");
    }

    /**
     * A stage 1 application runs to the next one. A {@code DATABASE} ends it too, because the
     * databases are written after the applications and a {@code TRANSACT} among them would belong to
     * neither.
     */
    private static boolean endsApplication(Statement statement) {
        return isOperation(statement, "APPLCTN") || isOperation(statement, "DATABASE") ||
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
