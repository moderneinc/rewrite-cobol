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
package org.openrewrite.mainframe.ims.trait;

import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.mainframe.ims.tree.Ims;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.util.List;
import java.util.Locale;

/**
 * An {@code XDFLD}: the name a secondary index is entered by, and the fields of the indexed segment
 * it is built from.
 * <p>
 * It is not a field of the segment — nothing in the record holds it. A program qualifies a call on
 * this name and DL/I reads the index database instead, which is how a claim is found by its adjuster
 * rather than by its number.
 */
@Value
public class IndexField implements Trait<Ims.MacroStatement> {

    Cursor cursor;

    public String getName() {
        String name = Operands.firstOf(getTree(), "NAME");
        return name == null ? "" : name;
    }

    /**
     * The fields of the indexed segment the index key is built from, from {@code SRCH=}.
     */
    public List<String> getSearchFields() {
        return Operands.listOf(getTree(), "SRCH");
    }

    /**
     * The fields appended to the key to tell duplicates apart, from {@code SUBSEQ=}.
     */
    public List<String> getSubsequenceFields() {
        return Operands.listOf(getTree(), "SUBSEQ");
    }

    public @Nullable Segment getSegment() {
        return Definitions.segmentOf(cursor);
    }

    public @Nullable Database getDatabase() {
        return Definitions.databaseOf(cursor);
    }

    public int getLine() {
        return Definitions.lineOf(cursor, getTree().getOperation());
    }

    public static class Matcher extends SimpleTraitMatcher<IndexField> {

        @Override
        protected @Nullable IndexField test(Cursor cursor) {
            Object value = cursor.getValue();
            if (!(value instanceof Ims.MacroStatement)) {
                return null;
            }
            Ims.MacroStatement statement = (Ims.MacroStatement) value;
            return statement.isOperation("XDFLD") && Operands.firstOf(statement, "NAME") != null ?
                    new IndexField(cursor) : null;
        }
    }

    @Override
    public String toString() {
        return "XDFLD " + getName().toUpperCase(Locale.ROOT);
    }
}
