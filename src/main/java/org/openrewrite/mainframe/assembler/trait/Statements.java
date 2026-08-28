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
package org.openrewrite.mainframe.assembler.trait;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.mainframe.assembler.AssemblerIsoVisitor;
import org.openrewrite.mainframe.assembler.tree.Assembler;
import org.openrewrite.mainframe.assembler.tree.Space;
import org.openrewrite.mainframe.assembler.tree.Statement;
import org.openrewrite.mainframe.controlcard.CardLines;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static java.util.Collections.emptyList;

/**
 * Walking the statements around one, which is how a member says what belongs to what.
 * <p>
 * HLASM source is a flat run of statements and means something structured: the constants after a
 * {@code DSECT} lay it out, the statement after a {@code MACRO} is a prototype and not an invocation,
 * and a {@code BALR} a line or two below a {@code L R15,=V(NAME)} is the call that pair makes. Nothing
 * but position says so, so the tree stays flat and the relationships are read from the cursor.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class Statements {

    static List<Statement> allIn(Cursor cursor) {
        Assembler.CompilationUnit cu = cursor.firstEnclosing(Assembler.CompilationUnit.class);
        return cu == null ? emptyList() : cu.getStatements();
    }

    /**
     * The statements after {@code cursor}'s, up to the first one {@code stop} answers for.
     */
    static List<Assembler.Instruction> following(Cursor cursor, Predicate<Assembler.Instruction> stop) {
        List<Statement> statements = allIn(cursor);
        List<Assembler.Instruction> within = new ArrayList<>();
        for (int i = indexOf(statements, cursor.getValue()) + 1;
             i > 0 && i < statements.size(); i++) {
            Statement statement = statements.get(i);
            if (statement instanceof Assembler.Instruction) {
                Assembler.Instruction instruction = (Assembler.Instruction) statement;
                if (stop.test(instruction)) {
                    break;
                }
                within.add(instruction);
            }
        }
        return within;
    }

    /**
     * The instruction before {@code cursor}'s, comments passed over, or null where it is the first.
     */
    static Assembler.@Nullable Instruction before(Cursor cursor) {
        List<Statement> statements = allIn(cursor);
        for (int i = indexOf(statements, cursor.getValue()) - 1; i >= 0; i--) {
            if (statements.get(i) instanceof Assembler.Instruction) {
                return (Assembler.Instruction) statements.get(i);
            }
        }
        return null;
    }

    /**
     * The next {@code limit} instructions after {@code cursor}'s, comments passed over. A V-con call is
     * a load and a branch a statement or two apart, so how far to look is the caller's to say.
     */
    static List<Assembler.Instruction> next(Cursor cursor, int limit) {
        List<Statement> statements = allIn(cursor);
        List<Assembler.Instruction> found = new ArrayList<>(limit);
        for (int i = indexOf(statements, cursor.getValue()) + 1;
             i > 0 && i < statements.size() && found.size() < limit; i++) {
            if (statements.get(i) instanceof Assembler.Instruction) {
                found.add((Assembler.Instruction) statements.get(i));
            }
        }
        return found;
    }

    /**
     * The constant a label holds, or null where the member does not write one. This is what turns the
     * {@code A10GHN} of a DL/I call into the {@code GHN} the four byte function code says.
     */
    static @Nullable String constantOf(Cursor cursor, String label) {
        return Constants.valueOf(definitionOf(cursor, label));
    }

    /**
     * The operand of the {@code DC} a label holds, as written, or null where the member declares none.
     */
    static @Nullable String definitionOf(Cursor cursor, String label) {
        for (Statement statement : allIn(cursor)) {
            if (statement instanceof Assembler.Instruction) {
                Assembler.Instruction instruction = (Assembler.Instruction) statement;
                if (instruction.isOperation("DC") && label.equalsIgnoreCase(instruction.getSimpleName())) {
                    return instruction.getOperandText(0);
                }
            }
        }
        return null;
    }

    /**
     * The one-based line of the member a word was written on. Every row a member contributes is
     * anchored at the line of the statement that said it.
     */
    static int lineOf(Cursor cursor, Assembler.Word word) {
        return CardLines.of(cursor, Assembler.CompilationUnit.class, words()).getOrDefault(word.getId(), 1);
    }

    /**
     * Whether a name is a variable symbol, which is not a name at all: what {@code &NAME CSECT} in a
     * macro definition calls the section is decided when the macro is expanded.
     */
    static boolean isVariable(@Nullable String name) {
        return name != null && name.startsWith("&");
    }

    private static int indexOf(List<Statement> statements, Object statement) {
        for (int i = 0; i < statements.size(); i++) {
            if (statements.get(i) == statement) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Where the words of a member are, for {@link CardLines} to count the lines between them.
     */
    private static AssemblerIsoVisitor<CardLines> words() {
        return new AssemblerIsoVisitor<CardLines>() {
            @Override
            public Space visitSpace(Space space, Space.Location location, CardLines lines) {
                lines.space(space.getWhitespace());
                return space;
            }

            @Override
            public Assembler.Word visitWord(Assembler.Word word, CardLines lines) {
                Assembler.Word w = super.visitWord(word, lines);
                lines.word(w.getId());
                return w;
            }
        };
    }
}
