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

import java.util.ArrayList;
import java.util.List;

/**
 * A name this member uses and does not define: {@code EXTRN} for one the binder must resolve, and
 * {@code WXTRN} for one it may leave unresolved rather than go looking for it.
 * <p>
 * It is the other end of {@link EntryPoint}, and the difference between the two matters to a build:
 * an {@code EXTRN} the link-edit deck does not include is what the binder's autocall goes hunting for.
 */
@Value
public class ExternalName implements Trait<Assembler.Instruction> {

    Cursor cursor;

    public List<String> getNames() {
        List<String> names = new ArrayList<>(getTree().getOperandTexts());
        names.removeIf(String::isEmpty);
        return names;
    }

    /**
     * Whether the reference is weak, which is what {@code WXTRN} says: the binder resolves it if the
     * name turns up and leaves it alone if it does not.
     */
    public boolean isWeak() {
        return getTree().isOperation("WXTRN");
    }

    public int getLine() {
        return Statements.lineOf(cursor, getTree().getOperation());
    }

    public static class Matcher extends SimpleTraitMatcher<ExternalName> {

        @Override
        protected @Nullable ExternalName test(Cursor cursor) {
            Object value = cursor.getValue();
            if (!(value instanceof Assembler.Instruction)) {
                return null;
            }
            Assembler.Instruction instruction = (Assembler.Instruction) value;
            return (instruction.isOperation("EXTRN") || instruction.isOperation("WXTRN")) &&
                   instruction.getOperandText(0) != null ? new ExternalName(cursor) : null;
        }
    }

    @Override
    public String toString() {
        return (isWeak() ? "WXTRN " : "EXTRN ") + getNames();
    }
}
