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
 * A {@code COPY}, which reads a member of the macro library in where it stands.
 * <p>
 * The same library holds the macros, so a member reached by {@code COPY} and a member reached by
 * invoking it are told apart by the statement and not by where they live: {@code maclib/CLMREGS} is
 * copied, {@code maclib/CLMSAVE} is invoked, and both are {@code CLM.PROD.MACLIB}.
 */
@Value
public class Copy implements Trait<Assembler.Instruction> {

    Cursor cursor;

    /**
     * The member copied in.
     */
    public String getMember() {
        String member = getTree().getOperandText(0);
        return member == null ? "" : member;
    }

    public int getLine() {
        return Statements.lineOf(cursor, getTree().getOperation());
    }

    public static class Matcher extends SimpleTraitMatcher<Copy> {

        @Override
        protected @Nullable Copy test(Cursor cursor) {
            Object value = cursor.getValue();
            return value instanceof Assembler.Instruction &&
                   ((Assembler.Instruction) value).isOperation("COPY") &&
                   ((Assembler.Instruction) value).getOperandText(0) != null ?
                    new Copy(cursor) : null;
        }
    }

    @Override
    public String toString() {
        return "COPY " + getMember();
    }
}
