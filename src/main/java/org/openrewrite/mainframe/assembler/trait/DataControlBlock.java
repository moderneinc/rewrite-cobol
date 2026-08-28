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
package org.openrewrite.mainframe.assembler.trait;

import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.mainframe.assembler.tree.Assembler;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

/**
 * A {@code DCB}, which is where an assembler program says what DD name it reads or writes.
 * <p>
 * It is the assembler's {@code SELECT ... ASSIGN TO}: the program names a DD and nothing else, and
 * only the JCL says what data set the DD is bound to. Everything else on the statement — the
 * organisation, the record format, the record length — is what the program expects the data set to be,
 * which is what makes a mismatch with the JCL findable.
 */
@Value
public class DataControlBlock implements Trait<Assembler.Instruction> {

    Cursor cursor;

    /**
     * The label the block is addressed by, which is what an {@code OPEN} or a {@code GET} names.
     */
    public String getName() {
        return getTree().getSimpleName();
    }

    public @Nullable String getDdName() {
        return getTree().getParameterValue("DDNAME");
    }

    /**
     * The data set organisation the program expects: {@code PS}, {@code PO}, {@code IS}.
     */
    public @Nullable String getOrganization() {
        return getTree().getParameterValue("DSORG");
    }

    /**
     * How the program reads or writes it: {@code (GM)} is a get in move mode, {@code (PM)} a put.
     */
    public @Nullable String getAccess() {
        return getTree().getParameterValue("MACRF");
    }

    public @Nullable String getRecordFormat() {
        return getTree().getParameterValue("RECFM");
    }

    public @Nullable Integer getRecordLength() {
        String length = getTree().getParameterValue("LRECL");
        try {
            return length == null ? null : Integer.valueOf(length.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Where control goes at end of file, which is the only place a sequential read says what it does
     * when the data runs out.
     */
    public @Nullable String getEndOfDataLabel() {
        return getTree().getParameterValue("EODAD");
    }

    public int getLine() {
        return Statements.lineOf(cursor, getTree().getOperation());
    }

    public static class Matcher extends SimpleTraitMatcher<DataControlBlock> {

        @Override
        protected @Nullable DataControlBlock test(Cursor cursor) {
            Object value = cursor.getValue();
            return value instanceof Assembler.Instruction &&
                   ((Assembler.Instruction) value).isOperation("DCB") ?
                    new DataControlBlock(cursor) : null;
        }
    }

    @Override
    public String toString() {
        return "DCB " + getName() + " DDNAME=" + getDdName();
    }
}
