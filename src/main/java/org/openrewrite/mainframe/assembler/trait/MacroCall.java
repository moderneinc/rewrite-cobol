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

import java.util.Collection;
import java.util.List;

/**
 * An invocation of a macro, which is any operation the assembler does not define itself.
 * <p>
 * Whether the shop wrote the macro or IBM ships it is not written anywhere in the program — the
 * statement is the same either way — so it is answered by the library: a name a {@code .mac} member of
 * the same set defines is the shop's, and {@code CALL}, {@code DCB}, {@code OPEN} and the rest come out
 * of {@code SYS1.MACLIB}, which is not in the repository. {@link #isDefinedBy} is that join.
 */
@Value
public class MacroCall implements Trait<Assembler.Instruction> {

    Cursor cursor;

    public String getName() {
        return getTree().getOperation().getText();
    }

    /**
     * The name field, which a linkage macro uses as the label of what it generates: {@code CLMSAVE}
     * starts the control section its label names.
     */
    public String getLabel() {
        return getTree().getSimpleName();
    }

    public List<String> getOperands() {
        return getTree().getOperandTexts();
    }

    public @Nullable String getOperand(String keyword) {
        return getTree().getParameterValue(keyword);
    }

    public int getLine() {
        return Statements.lineOf(cursor, getTree().getOperation());
    }

    /**
     * Whether one of these library members defines the macro, which is what makes it the shop's rather
     * than IBM's. Members are named as a library names them, without an extension.
     */
    public boolean isDefinedBy(Collection<String> members) {
        for (String member : members) {
            if (member.equalsIgnoreCase(getName())) {
                return true;
            }
        }
        return false;
    }

    public static class Matcher extends SimpleTraitMatcher<MacroCall> {

        @Override
        protected @Nullable MacroCall test(Cursor cursor) {
            Object value = cursor.getValue();
            if (!(value instanceof Assembler.Instruction)) {
                return null;
            }
            Assembler.Instruction instruction = (Assembler.Instruction) value;
            // A prototype names the macro rather than invoking it. Read as an invocation, every macro
            // library member reports one call of itself.
            if (!Operations.isMacro(instruction.getOperation().getText()) ||
                MacroDefinition.isPrototype(cursor)) {
                return null;
            }
            return new MacroCall(cursor);
        }
    }

    @Override
    public String toString() {
        return getName() + " " + getOperands();
    }
}
