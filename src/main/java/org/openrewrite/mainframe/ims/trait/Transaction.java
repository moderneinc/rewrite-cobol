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
 * A {@code TRANSACT} of the stage 1 deck: a code a terminal types, answered by the PSB of the
 * {@code APPLCTN} above it.
 * <p>
 * Several transactions under one application are answered by the one program, which tells them apart
 * by the code it reads out of the message.
 */
@Value
public class Transaction implements Trait<Ims.MacroStatement> {

    Cursor cursor;

    /**
     * The transaction code, from {@code CODE=}.
     */
    public String getCode() {
        String code = Operands.firstOf(getTree(), "CODE");
        return code == null ? "" : code;
    }

    /**
     * How long the scratchpad area is, from {@code SPA=}. A transaction with one is conversational:
     * IMS keeps the area between messages and hands it back on the next.
     */
    public @Nullable Integer getScratchpadSize() {
        return Operands.integerOf(getTree(), "SPA");
    }

    public @Nullable Application getApplication() {
        return Definitions.applicationOf(cursor);
    }

    public int getLine() {
        return Definitions.lineOf(cursor, getTree().getOperation());
    }

    public static class Matcher extends SimpleTraitMatcher<Transaction> {

        @Override
        protected @Nullable Transaction test(Cursor cursor) {
            Object value = cursor.getValue();
            if (!(value instanceof Ims.MacroStatement)) {
                return null;
            }
            Ims.MacroStatement statement = (Ims.MacroStatement) value;
            return statement.isOperation("TRANSACT") && Operands.firstOf(statement, "CODE") != null ?
                    new Transaction(cursor) : null;
        }
    }

    @Override
    public String toString() {
        return "TRANSACT " + getCode().toUpperCase(Locale.ROOT);
    }
}
