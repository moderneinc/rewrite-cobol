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

/**
 * A paragraph or a section: the unit {@code PERFORM} and {@code GO TO} name, and so the unit a
 * control flow graph has nodes for.
 * <p>
 * The two are one concept as far as anything that reaches them is concerned — {@code PERFORM
 * A-SECTION} and {@code PERFORM A-PARA} are written the same and mean the same — which is why they
 * are one trait rather than two.
 */
@Value
public class Procedure implements Trait<Cobol> {

    Cursor cursor;

    /**
     * The paragraph or section name, upper cased, or empty where it has none. Callers compare these
     * against names written elsewhere in the program, which COBOL does case insensitively.
     */
    public String getName() {
        Cobol tree = getTree();
        String name = tree instanceof Cobol.Paragraph ?
                Names.upperOf(((Cobol.Paragraph) tree).getParagraphName()) :
                Names.upperOf(((Cobol.ProcedureSection) tree).getProcedureSectionHeader().getSectionName());
        return name == null ? "" : name;
    }

    public boolean isSection() {
        return getTree() instanceof Cobol.ProcedureSection;
    }

    public static class Matcher extends SimpleTraitMatcher<Procedure> {

        @Override
        protected @Nullable Procedure test(Cursor cursor) {
            Object value = cursor.getValue();
            return value instanceof Cobol.Paragraph || value instanceof Cobol.ProcedureSection ?
                    new Procedure(cursor) : null;
        }
    }

    /**
     * The procedure {@code cursor} is inside, or null for a statement written before any paragraph.
     * A paragraph within a section answers with the paragraph: it is the finer of the two names, and
     * the one a {@code PERFORM} would have to use to reach that statement.
     */
    public static @Nullable Procedure of(Cursor cursor) {
        for (Iterator<Cursor> path = cursor.getPathAsCursors(); path.hasNext(); ) {
            Cursor enclosing = path.next();
            Object value = enclosing.getValue();
            if (value instanceof Cobol.Paragraph || value instanceof Cobol.ProcedureSection) {
                return new Procedure(enclosing);
            }
        }
        return null;
    }

    /**
     * The name of the procedure {@code cursor} is inside, or empty where it is in none.
     */
    public static String nameOf(Cursor cursor) {
        Procedure procedure = of(cursor);
        return procedure == null ? "" : procedure.getName();
    }

    @Override
    public String toString() {
        return getName() + (isSection() ? " SECTION" : "");
    }
}
