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

import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.ims.tree.Ims;
import org.openrewrite.ims.tree.Statement;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A {@code SENSEG}: one segment of the database the program may see through this PCB.
 * <p>
 * A database has more segments than a program is entitled to, so this is the narrower list — and the
 * {@code PROCOPT} on it is narrower again than the PCB's, which is what a data access matrix reads.
 */
@Value
public class SensitiveSegment implements Trait<Ims.MacroStatement> {

    Cursor cursor;

    public String getName() {
        String name = Operands.firstOf(getTree(), "NAME");
        return name == null ? "" : name;
    }

    /**
     * Whether the segment is the root of the path, from {@code PARENT=0}.
     */
    public boolean isRoot() {
        String parent = Operands.firstOf(getTree(), "PARENT");
        return parent == null || "0".equals(parent);
    }

    /**
     * The segment this one hangs under, or null for the root. A program may only see a segment whose
     * parents it can see too, so these chain back to the root.
     */
    public @Nullable String getParentName() {
        return isRoot() ? null : Operands.firstOf(getTree(), "PARENT");
    }

    /**
     * What the program may do to this segment, from {@code PROCOPT=}. Where it is left out the PCB's
     * own applies.
     */
    public @Nullable String getProcessingOptions() {
        return Operands.firstOf(getTree(), "PROCOPT");
    }

    /**
     * The fields the program may see, in the order they build the I/O area. Empty for a segment the
     * program sees whole, which is most of them.
     */
    public List<SensitiveField> getSensitiveFields() {
        List<SensitiveField> fields = new ArrayList<>();
        for (Statement statement : Definitions.withinSensitiveSegment(cursor)) {
            new SensitiveField.Matcher().get(new Cursor(cursor.getParentOrThrow(), statement))
                    .ifPresent(fields::add);
        }
        return fields;
    }

    public @Nullable Pcb getPcb() {
        return Definitions.pcbOf(cursor);
    }

    public int getLine() {
        return Definitions.lineOf(cursor, getTree().getOperation());
    }

    public static class Matcher extends SimpleTraitMatcher<SensitiveSegment> {

        @Override
        protected @Nullable SensitiveSegment test(Cursor cursor) {
            Object value = cursor.getValue();
            if (!(value instanceof Ims.MacroStatement)) {
                return null;
            }
            Ims.MacroStatement statement = (Ims.MacroStatement) value;
            return statement.isOperation("SENSEG") && Operands.firstOf(statement, "NAME") != null ?
                    new SensitiveSegment(cursor) : null;
        }
    }

    @Override
    public String toString() {
        return "SENSEG " + getName().toUpperCase(Locale.ROOT);
    }
}
