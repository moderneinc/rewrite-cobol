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
 * A segment: the {@code SEGM} and the fields, logical children and index fields written under it.
 * <p>
 * A segment is what a DL/I call asks for by name, and its length and key are what a program's I/O
 * area has to agree with.
 */
@Value
public class Segment implements Trait<Ims.MacroStatement> {

    Cursor cursor;

    public String getName() {
        String name = Operands.firstOf(getTree(), "NAME");
        return name == null ? "" : name;
    }

    /**
     * Whether the segment is the root, from {@code PARENT=0}.
     */
    public boolean isRoot() {
        String parent = physicalParent();
        return parent == null || "0".equals(parent);
    }

    /**
     * The segment this one hangs under in the same database, or null for the root.
     */
    public @Nullable String getParentName() {
        return isRoot() ? null : physicalParent();
    }

    /**
     * The second parent of {@code PARENT=((phys),(lpar,PHYSICAL,dbd))}, or null for a segment with
     * one parent.
     * <p>
     * This is what makes a logical child: the segment is stored under its physical parent here and
     * reached from a segment in another database, so a program that has one has the other without a
     * second database call.
     */
    public @Nullable LogicalParent getLogicalParent() {
        List<String> parents = Operands.listOf(getTree(), "PARENT");
        if (parents.size() < 2) {
            return null;
        }
        List<String> parent = Operands.membersOf(parents.get(1));
        return parent.isEmpty() ? null :
                new LogicalParent(parent.get(0), parent.size() > 2 ? parent.get(2) : null);
    }

    /**
     * How long the segment is, from {@code BYTES=}. This is the length a program's I/O area has to
     * be, and the copybook that describes it is the same number of bytes.
     */
    public @Nullable Integer getBytes() {
        return Operands.integerOf(getTree(), "BYTES");
    }

    /**
     * The pointers the segment carries, from {@code POINTER=}: {@code TWIN}, {@code TWINBWD},
     * {@code LPARNT} and the rest.
     */
    public List<String> getPointers() {
        return Operands.listOf(getTree(), "POINTER");
    }

    /**
     * The insert, replace and delete rules, from {@code RULES=}.
     */
    public List<String> getRules() {
        return Operands.listOf(getTree(), "RULES");
    }

    public List<Field> getFields() {
        return within(new Field.Matcher());
    }

    public @Nullable Field getField(String name) {
        for (Field field : getFields()) {
            if (field.getName().equalsIgnoreCase(name)) {
                return field;
            }
        }
        return null;
    }

    /**
     * The field the segment is keyed on, from the {@code SEQ} of a {@code FIELD NAME=(name,SEQ,U)}.
     * Null for a segment with no key, which DL/I may only read sequentially.
     */
    public @Nullable Field getSequenceField() {
        for (Field field : getFields()) {
            if (field.isSequence()) {
                return field;
            }
        }
        return null;
    }

    public List<LogicalChild> getLogicalChildren() {
        return within(new LogicalChild.Matcher());
    }

    public List<IndexField> getIndexFields() {
        return within(new IndexField.Matcher());
    }

    public @Nullable Database getDatabase() {
        return Definitions.databaseOf(cursor);
    }

    public int getLine() {
        return Definitions.lineOf(cursor, getTree().getOperation());
    }

    private @Nullable String physicalParent() {
        List<String> parents = Operands.listOf(getTree(), "PARENT");
        if (parents.isEmpty()) {
            return null;
        }
        List<String> parent = Operands.membersOf(parents.get(0));
        return parent.isEmpty() ? null : parent.get(0);
    }

    private <T extends Trait<?>> List<T> within(SimpleTraitMatcher<T> matcher) {
        List<T> found = new ArrayList<>();
        for (Statement statement : Definitions.withinSegment(cursor)) {
            matcher.get(new Cursor(cursor.getParentOrThrow(), statement)).ifPresent(found::add);
        }
        return found;
    }

    /**
     * A segment in another database that this one is also a child of, and the database holding it.
     * The database is null where the logical parent is in this database.
     */
    @Value
    public static class LogicalParent {
        String segment;

        @Nullable
        String database;
    }

    public static class Matcher extends SimpleTraitMatcher<Segment> {

        @Override
        protected @Nullable Segment test(Cursor cursor) {
            Object value = cursor.getValue();
            if (!(value instanceof Ims.MacroStatement)) {
                return null;
            }
            Ims.MacroStatement statement = (Ims.MacroStatement) value;
            return statement.isOperation("SEGM") && Operands.firstOf(statement, "NAME") != null ?
                    new Segment(cursor) : null;
        }
    }

    @Override
    public String toString() {
        return "SEGM " + getName().toUpperCase(Locale.ROOT);
    }
}
