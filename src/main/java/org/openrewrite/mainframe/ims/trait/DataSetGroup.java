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

import java.util.ArrayList;
import java.util.List;

/**
 * A {@code DATASET}: one group of the data sets a database is stored in.
 * <p>
 * What it says that nothing else does is the DD names. A running job has to allocate every one of
 * them or the database will not open, so this is an edge from a database to a job step — and it is a
 * DD name and not a data set: only the JCL says what each is bound to.
 */
@Value
public class DataSetGroup implements Trait<Ims.MacroStatement> {

    Cursor cursor;

    /**
     * The label in column 1, which a shop writes to number the groups. Empty where there is none.
     */
    public String getName() {
        return getTree().getSimpleName();
    }

    /**
     * The DD name of the primary data set, from {@code DD1=}.
     */
    public @Nullable String getPrimaryDdName() {
        return Operands.firstOf(getTree(), "DD1");
    }

    /**
     * The DD name of the overflow data set, from {@code DD2=}. Only HISAM has one.
     */
    public @Nullable String getOverflowDdName() {
        return Operands.firstOf(getTree(), "DD2");
    }

    /**
     * Every DD name this group needs, primary first.
     */
    public List<String> getDdNames() {
        List<String> ddNames = new ArrayList<>();
        String primary = getPrimaryDdName();
        if (primary != null) {
            ddNames.add(primary);
        }
        String overflow = getOverflowDdName();
        if (overflow != null) {
            ddNames.add(overflow);
        }
        return ddNames;
    }

    public @Nullable String getDevice() {
        return Operands.firstOf(getTree(), "DEVICE");
    }

    /**
     * The logical and block lengths from {@code RECORD=}. For a GSAM database this is the whole of
     * what it holds, there being no segment to describe it.
     */
    public List<Integer> getRecordLengths() {
        List<Integer> lengths = new ArrayList<>();
        for (String member : Operands.listOf(getTree(), "RECORD")) {
            Integer length = Operands.integerOf(member);
            if (length != null) {
                lengths.add(length);
            }
        }
        return lengths;
    }

    public @Nullable Database getDatabase() {
        return Definitions.databaseOf(cursor);
    }

    public int getLine() {
        return Definitions.lineOf(cursor, getTree().getOperation());
    }

    public static class Matcher extends SimpleTraitMatcher<DataSetGroup> {

        @Override
        protected @Nullable DataSetGroup test(Cursor cursor) {
            Object value = cursor.getValue();
            return value instanceof Ims.MacroStatement &&
                   ((Ims.MacroStatement) value).isOperation("DATASET") ?
                    new DataSetGroup(cursor) : null;
        }
    }

    @Override
    public String toString() {
        return "DATASET " + getDdNames();
    }
}
