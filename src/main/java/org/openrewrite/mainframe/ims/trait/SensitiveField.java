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

import java.util.Locale;

/**
 * A {@code SENFLD}: one field of a segment the program may see.
 * <p>
 * Where a PCB is field sensitive the I/O area is not the segment. It holds these fields, in this
 * order, at the offsets {@link #getStart()} gives — so a copybook checked against the {@code BYTES=}
 * of the {@code SEGM} would disagree, and rightly: the field list is what it describes.
 */
@Value
public class SensitiveField implements Trait<Ims.MacroStatement> {

    Cursor cursor;

    public String getName() {
        String name = Operands.firstOf(getTree(), "NAME");
        return name == null ? "" : name;
    }

    /**
     * Where the field begins in the I/O area, one based, from {@code START=}. This is an offset into
     * what the program sees and not into the segment.
     */
    public @Nullable Integer getStart() {
        return Operands.integerOf(getTree(), "START");
    }

    /**
     * Whether the program may replace the field, from {@code REPL=}. Left out it may.
     */
    public boolean isReplaceable() {
        return !"NO".equalsIgnoreCase(Operands.firstOf(getTree(), "REPL"));
    }

    public @Nullable SensitiveSegment getSegment() {
        return Definitions.sensitiveSegmentOf(cursor);
    }

    public int getLine() {
        return Definitions.lineOf(cursor, getTree().getOperation());
    }

    public static class Matcher extends SimpleTraitMatcher<SensitiveField> {

        @Override
        protected @Nullable SensitiveField test(Cursor cursor) {
            Object value = cursor.getValue();
            if (!(value instanceof Ims.MacroStatement)) {
                return null;
            }
            Ims.MacroStatement statement = (Ims.MacroStatement) value;
            return statement.isOperation("SENFLD") && Operands.firstOf(statement, "NAME") != null ?
                    new SensitiveField(cursor) : null;
        }
    }

    @Override
    public String toString() {
        return "SENFLD " + getName().toUpperCase(Locale.ROOT);
    }
}
