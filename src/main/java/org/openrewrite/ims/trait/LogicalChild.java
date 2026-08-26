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
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.util.List;

/**
 * An {@code LCHILD}: a segment in another database that points at the one this is written under.
 * <p>
 * The same macro does two jobs. In a target database it declares the index segment of a secondary
 * index or the logical child of a logical parent; in an {@code INDEX} database it points back at the
 * segment being indexed and names the {@code XDFLD} the index is entered through. Either way it
 * names a database and a segment inside it, which is the edge.
 */
@Value
public class LogicalChild implements Trait<Ims.MacroStatement> {

    Cursor cursor;

    /**
     * The segment named, from the first member of {@code NAME=(segment,dbd)}.
     */
    public String getSegmentName() {
        List<String> name = Operands.listOf(getTree(), "NAME");
        return name.isEmpty() ? "" : name.get(0);
    }

    /**
     * The database holding it, from the second member of {@code NAME=}. Null where the segment is in
     * this database.
     */
    public @Nullable String getDatabaseName() {
        List<String> name = Operands.listOf(getTree(), "NAME");
        return name.size() > 1 ? name.get(1) : null;
    }

    /**
     * How the segment is reached, from {@code POINTER=}: {@code INDX} a secondary index,
     * {@code SYMB} a symbolic pointer, {@code SNGL}, {@code NONE} for a unidirectional logical
     * relationship.
     */
    public @Nullable String getPointer() {
        return Operands.firstOf(getTree(), "POINTER");
    }

    /**
     * The {@code XDFLD} the index is entered through, from {@code INDEX=}. Written on the index
     * database's side, and for a primary index it is the target segment's own sequence field.
     */
    public @Nullable String getIndexFieldName() {
        return Operands.firstOf(getTree(), "INDEX");
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

    public static class Matcher extends SimpleTraitMatcher<LogicalChild> {

        @Override
        protected @Nullable LogicalChild test(Cursor cursor) {
            Object value = cursor.getValue();
            if (!(value instanceof Ims.MacroStatement)) {
                return null;
            }
            Ims.MacroStatement statement = (Ims.MacroStatement) value;
            return statement.isOperation("LCHILD") && Operands.firstOf(statement, "NAME") != null ?
                    new LogicalChild(cursor) : null;
        }
    }

    @Override
    public String toString() {
        String database = getDatabaseName();
        return "LCHILD " + (database == null ? getSegmentName() : database + '.' + getSegmentName());
    }
}
