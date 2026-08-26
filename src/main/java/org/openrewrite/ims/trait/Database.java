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
 * A database: the {@code DBD} and everything written under it.
 * <p>
 * The DBD is the only place a database's own shape is written down. A PSB says which databases a
 * program may reach and a DL/I call says which segment it asked for, but only this says what the
 * segments are, what they hold, how they are keyed and which other database they reach.
 */
@Value
public class Database implements Trait<Ims.MacroStatement> {

    Cursor cursor;

    public String getName() {
        String name = Operands.firstOf(getTree(), "NAME");
        return name == null ? "" : name;
    }

    /**
     * How the database is organised, from {@code ACCESS=}: {@code HDAM}, {@code HIDAM},
     * {@code HISAM}, {@code INDEX} or {@code GSAM}.
     */
    public @Nullable String getAccessMethod() {
        return Operands.firstOf(getTree(), "ACCESS");
    }

    /**
     * Everything {@code ACCESS=} says, which after the organisation is the operating system access
     * method the data sets are read with: {@code VSAM}, {@code OSAM} or {@code BSAM}.
     */
    public List<String> getAccess() {
        return Operands.listOf(getTree(), "ACCESS");
    }

    /**
     * The randomizing module an HDAM database places its roots with, from {@code RMNAME=}. Only HDAM
     * has one, and changing it moves every root.
     */
    public @Nullable String getRandomizer() {
        return Operands.firstOf(getTree(), "RMNAME");
    }

    /**
     * Whether the database is a sequential file DL/I reaches through a GSAM PCB. A GSAM database has
     * no {@code SEGM} at all — its record is the whole of it — so nothing else here answers for it.
     */
    public boolean isSequential() {
        return "GSAM".equalsIgnoreCase(getAccessMethod());
    }

    /**
     * The data set groups the database is stored in, in the order they are written.
     */
    public List<DataSetGroup> getDataSetGroups() {
        return within(new DataSetGroup.Matcher());
    }

    /**
     * The DD names a job step has to supply to open this database, across every data set group.
     * These are DD names and not data sets: only the JCL says what each is bound to.
     */
    public List<String> getDdNames() {
        List<String> ddNames = new ArrayList<>();
        for (DataSetGroup group : getDataSetGroups()) {
            ddNames.addAll(group.getDdNames());
        }
        return ddNames;
    }

    public List<Segment> getSegments() {
        return within(new Segment.Matcher());
    }

    public @Nullable Segment getSegment(String name) {
        for (Segment segment : getSegments()) {
            if (segment.getName().equalsIgnoreCase(name)) {
                return segment;
            }
        }
        return null;
    }

    /**
     * The segment with no parent, which is where every path into the database starts. Null for a
     * GSAM database, which has no segments.
     */
    public @Nullable Segment getRootSegment() {
        for (Segment segment : getSegments()) {
            if (segment.isRoot()) {
                return segment;
            }
        }
        return null;
    }

    /**
     * Every name this database writes down that belongs to another one, in source order.
     * <p>
     * This is the whole of what a DBD says about the rest of an estate, and there is nowhere else to
     * read it: a secondary index and its target name each other, a logical child names the database
     * holding its logical parent, and an {@code XDFLD} names the field the index is built on.
     */
    public List<Reference> getReferences() {
        List<Reference> references = new ArrayList<>();
        for (Segment segment : getSegments()) {
            Segment.LogicalParent parent = segment.getLogicalParent();
            if (parent != null) {
                references.add(new Reference(Reference.Kind.LOGICAL_PARENT,
                        parent.getDatabase() == null ? getName() : parent.getDatabase(),
                        parent.getSegment(), segment.getLine()));
            }
            for (LogicalChild child : segment.getLogicalChildren()) {
                references.add(new Reference(Reference.Kind.LOGICAL_CHILD,
                        child.getDatabaseName() == null ? getName() : child.getDatabaseName(),
                        child.getSegmentName(), child.getLine()));
            }
            for (IndexField index : segment.getIndexFields()) {
                for (String field : index.getSearchFields()) {
                    references.add(new Reference(Reference.Kind.INDEX_SOURCE, getName(), field,
                            index.getLine()));
                }
            }
        }
        return references;
    }

    public int getLine() {
        return Definitions.lineOf(cursor, getTree().getOperation());
    }

    private <T extends Trait<?>> List<T> within(SimpleTraitMatcher<T> matcher) {
        List<T> found = new ArrayList<>();
        for (Statement statement : Definitions.withinDatabase(cursor)) {
            matcher.get(new Cursor(cursor.getParentOrThrow(), statement)).ifPresent(found::add);
        }
        return found;
    }

    /**
     * A name another database owns, and the statement that named it.
     */
    @Value
    public static class Reference {
        Kind kind;

        /**
         * The database named, which for an {@code XDFLD} is this one: the field an index is built on
         * belongs to the segment being indexed.
         */
        String database;

        /**
         * The segment or field inside it.
         */
        String member;

        /**
         * The one-based line of the macro that named it.
         */
        int line;

        public enum Kind {
            /**
             * An {@code LCHILD}: the index segment a secondary index holds, or the logical child a
             * logical parent points back at.
             */
            LOGICAL_CHILD,
            /**
             * The second parent of a {@code SEGM PARENT=((phys),(lpar,PHYSICAL,dbd))}, which is how
             * a segment in one database reaches a segment in another.
             */
            LOGICAL_PARENT,
            /**
             * The {@code SRCH=} field of an {@code XDFLD}, which is what a secondary index is keyed
             * on.
             */
            INDEX_SOURCE
        }
    }

    public static class Matcher extends SimpleTraitMatcher<Database> {

        @Override
        protected @Nullable Database test(Cursor cursor) {
            Object value = cursor.getValue();
            if (!(value instanceof Ims.MacroStatement)) {
                return null;
            }
            Ims.MacroStatement statement = (Ims.MacroStatement) value;
            return statement.isOperation("DBD") && Operands.firstOf(statement, "NAME") != null ?
                    new Database(cursor) : null;
        }
    }

    @Override
    public String toString() {
        return "DBD " + getName().toUpperCase(Locale.ROOT);
    }
}
