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
 * A field of a segment: the part of it DL/I can search on.
 * <p>
 * A DBD names only the fields a call may qualify on, so this is not the segment's layout — the
 * copybook is that. What it does say is where each searchable field sits, which is what lets a
 * segment search argument be read against the record a program moves.
 */
@Value
public class Field implements Trait<Ims.MacroStatement> {

    Cursor cursor;

    public String getName() {
        List<String> name = Operands.listOf(getTree(), "NAME");
        return name.isEmpty() ? "" : name.get(0);
    }

    /**
     * Whether this is the segment's key, from the {@code SEQ} of {@code NAME=(name,SEQ,U)}.
     */
    public boolean isSequence() {
        List<String> name = Operands.listOf(getTree(), "NAME");
        return name.size() > 1 && "SEQ".equalsIgnoreCase(name.get(1));
    }

    /**
     * Whether two segments may carry the same key, from the {@code U} or {@code M} that follows
     * {@code SEQ}. Only a sequence field says, so a field that is not one is not unique either.
     */
    public boolean isUnique() {
        List<String> name = Operands.listOf(getTree(), "NAME");
        return isSequence() && name.size() > 2 && "U".equalsIgnoreCase(name.get(2));
    }

    public @Nullable Integer getBytes() {
        return Operands.integerOf(getTree(), "BYTES");
    }

    /**
     * The one-based byte of the segment the field starts at, from {@code START=}.
     */
    public @Nullable Integer getStart() {
        return Operands.integerOf(getTree(), "START");
    }

    /**
     * How the bytes are held, from {@code TYPE=}: {@code C} character, {@code P} packed decimal,
     * {@code X} hexadecimal.
     */
    public @Nullable String getType() {
        return Operands.firstOf(getTree(), "TYPE");
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

    public static class Matcher extends SimpleTraitMatcher<Field> {

        @Override
        protected @Nullable Field test(Cursor cursor) {
            Object value = cursor.getValue();
            if (!(value instanceof Ims.MacroStatement)) {
                return null;
            }
            Ims.MacroStatement statement = (Ims.MacroStatement) value;
            return statement.isOperation("FIELD") && Operands.firstOf(statement, "NAME") != null ?
                    new Field(cursor) : null;
        }
    }

    @Override
    public String toString() {
        return "FIELD " + getName().toUpperCase(Locale.ROOT);
    }
}
