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
package org.openrewrite.cobol.trait;

import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.cobol.tree.Cobol;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.util.Iterator;
import java.util.List;

/**
 * The program a statement belongs to.
 * <p>
 * Every row a portfolio analysis writes is attributed to a program, so this is the question asked
 * most often and from the deepest part of the tree. Read from the cursor rather than tracked as
 * visitor state: a nested program's statements answer with the nested program, and a lambda handed a
 * cursor can ask without being a visitor of its own.
 */
@Value
public class Program implements Trait<Cobol.ProgramUnit> {

    Cursor cursor;

    /**
     * The name from {@code PROGRAM-ID}, or empty where the paragraph carries none.
     */
    public String getName() {
        String name = Names.of(getTree().getIdentificationDivision().getProgramIdParagraph().getProgramName());
        return name == null ? "" : name;
    }

    public static class Matcher extends SimpleTraitMatcher<Program> {

        @Override
        protected @Nullable Program test(Cursor cursor) {
            return cursor.getValue() instanceof Cobol.ProgramUnit ? new Program(cursor) : null;
        }
    }

    /**
     * The program {@code cursor} is inside, innermost first.
     * <p>
     * At the compilation unit itself — where an analysis that reports once per file asks — there is
     * no program above to find, so the file's first program answers instead. A file holding several
     * is rare and the first is the one it is named for.
     */
    public static @Nullable Program of(Cursor cursor) {
        for (Iterator<Cursor> path = cursor.getPathAsCursors(); path.hasNext(); ) {
            Cursor enclosing = path.next();
            Object value = enclosing.getValue();
            if (value instanceof Cobol.ProgramUnit) {
                return new Program(enclosing);
            }
            if (value instanceof Cobol.CompilationUnit) {
                List<Cobol.ProgramUnit> units = ((Cobol.CompilationUnit) value).getProgramUnits();
                return units.isEmpty() ? null : new Program(new Cursor(enclosing, units.get(0)));
            }
        }
        return null;
    }

    /**
     * The name of the program {@code cursor} is inside. {@code UNKNOWN} where there is none to read,
     * which keeps a row attributable to something rather than dropping it.
     */
    public static String nameOf(Cursor cursor) {
        return nameOf(of(cursor));
    }

    /**
     * The name of the program a file declares, for a caller holding the file rather than a cursor
     * into it.
     */
    public static String nameOf(Cobol.CompilationUnit cu) {
        List<Cobol.ProgramUnit> units = cu.getProgramUnits();
        return units.isEmpty() ? "UNKNOWN" :
                nameOf(new Program(new Cursor(new Cursor(null, Cursor.ROOT_VALUE), units.get(0))));
    }

    private static String nameOf(@Nullable Program program) {
        if (program == null) {
            return "UNKNOWN";
        }
        String name = program.getName();
        return name.isEmpty() ? "UNKNOWN" : name;
    }

    @Override
    public String toString() {
        return "PROGRAM-ID. " + getName();
    }
}
