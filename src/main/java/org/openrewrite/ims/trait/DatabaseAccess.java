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

import java.util.Locale;

/**
 * A {@code DATABASE} of the stage 1 deck: a DBD the control region is told about, and what it may do
 * with it.
 * <p>
 * A database missing from here is not a database the online system can open, whatever its DBD says —
 * which is why a GSAM database, allocated by the batch job that reads it, is never written here.
 */
@Value
public class DatabaseAccess implements Trait<Ims.MacroStatement> {

    Cursor cursor;

    /**
     * The DBD, from {@code DBD=}.
     */
    public String getName() {
        String name = Operands.firstOf(getTree(), "DBD");
        return name == null ? "" : name;
    }

    /**
     * What the control region may do with it, from {@code ACCESS=}: {@code UP} to update,
     * {@code RD} to read, {@code EX} exclusively, {@code RO} read-only.
     */
    public @Nullable String getAccess() {
        return Operands.firstOf(getTree(), "ACCESS");
    }

    public int getLine() {
        return Definitions.lineOf(cursor, getTree().getOperation());
    }

    public static class Matcher extends SimpleTraitMatcher<DatabaseAccess> {

        @Override
        protected @Nullable DatabaseAccess test(Cursor cursor) {
            Object value = cursor.getValue();
            if (!(value instanceof Ims.MacroStatement)) {
                return null;
            }
            Ims.MacroStatement statement = (Ims.MacroStatement) value;
            return statement.isOperation("DATABASE") && Operands.firstOf(statement, "DBD") != null ?
                    new DatabaseAccess(cursor) : null;
        }
    }

    @Override
    public String toString() {
        return "DATABASE " + getName().toUpperCase(Locale.ROOT);
    }
}
