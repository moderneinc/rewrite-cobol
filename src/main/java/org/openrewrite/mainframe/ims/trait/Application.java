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
import org.openrewrite.mainframe.ims.tree.Statement;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * An {@code APPLCTN} of the stage 1 deck: a PSB the control region is told about, and the
 * transactions written under it.
 * <p>
 * This is the only place a transaction code is tied to a PSB, and so the only place the online side
 * of an IMS estate can be resolved from. What it does not say is the program: a message driven
 * application is loaded by the name the PSB has, and which program was link-edited under that name is
 * for a link-edit deck to say and a composition to join.
 */
@Value
public class Application implements Trait<Ims.MacroStatement> {

    Cursor cursor;

    /**
     * The PSB, from {@code PSB=}.
     */
    public @Nullable String getPsbName() {
        return Operands.firstOf(getTree(), "PSB");
    }

    /**
     * How the program is run, from {@code PGMTYPE=}: {@code TP} for one the message region schedules,
     * {@code BATCH} for one a job step runs.
     */
    public @Nullable String getProgramType() {
        return Operands.firstOf(getTree(), "PGMTYPE");
    }

    /**
     * Whether the message region schedules the program, which is what decides where its masks begin.
     */
    public boolean isMessageDriven() {
        return "TP".equalsIgnoreCase(getProgramType());
    }

    /**
     * The transactions answered by this PSB, in the order they are written. A batch application has
     * none: its job names the PSB itself.
     */
    public List<Transaction> getTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        for (Statement statement : Definitions.withinApplication(cursor)) {
            new Transaction.Matcher().get(new Cursor(cursor.getParentOrThrow(), statement))
                    .ifPresent(transactions::add);
        }
        return transactions;
    }

    public int getLine() {
        return Definitions.lineOf(cursor, getTree().getOperation());
    }

    public static class Matcher extends SimpleTraitMatcher<Application> {

        @Override
        protected @Nullable Application test(Cursor cursor) {
            Object value = cursor.getValue();
            if (!(value instanceof Ims.MacroStatement)) {
                return null;
            }
            Ims.MacroStatement statement = (Ims.MacroStatement) value;
            return statement.isOperation("APPLCTN") && Operands.firstOf(statement, "PSB") != null ?
                    new Application(cursor) : null;
        }
    }

    @Override
    public String toString() {
        String psb = getPsbName();
        return "APPLCTN " + (psb == null ? "" : psb.toUpperCase(Locale.ROOT));
    }
}
