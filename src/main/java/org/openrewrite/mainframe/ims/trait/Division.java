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
 * Which way the device pages under it go: one {@code DIV}.
 * <p>
 * {@code TYPE=INOUT} is a screen the operator both reads and types into; {@code TYPE=OUTPUT} is one
 * that is only sent, which is what a notice and a printed page are. It is the format set's own answer
 * to whether a MID exists for it.
 */
@Value
public class Division implements Trait<Ims.MacroStatement> {

    Cursor cursor;

    /**
     * From {@code TYPE=}: {@code INOUT}, {@code INPUT} or {@code OUTPUT}.
     */
    public @Nullable String getType() {
        return Operands.firstOf(getTree(), "TYPE");
    }

    /**
     * Whether the device pages under it are ever received from, which rules out a printer and a
     * notice.
     */
    public boolean isInput() {
        String type = getType();
        return type != null && !"OUTPUT".equalsIgnoreCase(type);
    }

    public @Nullable FormatSet getFormatSet() {
        return Definitions.formatSetOf(cursor);
    }

    public int getLine() {
        return Definitions.lineOf(cursor, getTree().getOperation());
    }

    public static class Matcher extends SimpleTraitMatcher<Division> {

        @Override
        protected @Nullable Division test(Cursor cursor) {
            Object value = cursor.getValue();
            return value instanceof Ims.MacroStatement && ((Ims.MacroStatement) value).isOperation("DIV") ?
                    new Division(cursor) : null;
        }
    }

    @Override
    public String toString() {
        String type = getType();
        return "DIV " + (type == null ? "" : type.toUpperCase(Locale.ROOT));
    }
}
