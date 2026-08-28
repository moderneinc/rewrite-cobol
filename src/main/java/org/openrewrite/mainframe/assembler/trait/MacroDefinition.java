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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A macro this member defines, read from the prototype: the statement between the {@code MACRO} and
 * the body, whose operation is the macro's name and whose operands are the parameters it takes.
 * <p>
 * This is what says a macro library member is a macro rather than a copy member. Most of the
 * {@code .mac} members of a shop are DSECTs read by {@code COPY}, and the two are told apart nowhere
 * else: {@code maclib/CLMSAVE} defines a macro because it writes {@code &NAME CLMSAVE &BASE,&SAVE=,&ID=},
 * and {@code maclib/CLMREGS} does not because it writes no prototype at all.
 * <p>
 * The body is not read and the macro is not expanded. What a parameter is used for is the body's
 * business; what a caller may pass is the prototype's, and that is all this answers.
 */
@Value
public class MacroDefinition implements Trait<Assembler.Instruction> {

    /**
     * The prototype, not the {@code MACRO} before it: the prototype is where the name is written.
     */
    Cursor cursor;

    public String getName() {
        return getTree().getOperation().getText();
    }

    /**
     * The variable symbol the caller's name field arrives in, such as the {@code &NAME} a linkage
     * macro labels the control section it generates with, or null where the macro takes none.
     */
    public @Nullable String getLabelParameter() {
        String label = getTree().getSimpleName();
        return label.isEmpty() ? null : parameterName(label);
    }

    /**
     * The positional parameters in order, without the ampersand that makes them variable symbols,
     * since that is how a caller reads a prototype.
     */
    public List<String> getPositionalParameters() {
        List<String> positional = new ArrayList<>();
        for (Assembler.Operand operand : getTree().getParameters()) {
            if (operand.getKeyword() == null && !operand.getText().isEmpty()) {
                positional.add(parameterName(operand.getText()));
            }
        }
        return positional;
    }

    /**
     * The keyword parameters and the default each carries, which is empty for a keyword written
     * {@code &SAVE=} with nothing after it.
     */
    public Map<String, String> getKeywordParameters() {
        Map<String, String> keywords = new LinkedHashMap<>();
        for (Assembler.Operand operand : getTree().getParameters()) {
            String keyword = operand.getKeyword();
            if (keyword != null) {
                keywords.put(parameterName(keyword), operand.getValue());
            }
        }
        return keywords;
    }

    public int getLine() {
        return Statements.lineOf(cursor, getTree().getOperation());
    }

    private static String parameterName(String symbol) {
        return symbol.startsWith("&") ? symbol.substring(1) : symbol;
    }

    /**
     * Whether the statement is a prototype, which is the one right after a {@code MACRO}. Nothing else
     * distinguishes it from an invocation of the macro it is naming.
     */
    static boolean isPrototype(Cursor cursor) {
        Assembler.Instruction before = Statements.before(cursor);
        return before != null && before.isOperation("MACRO");
    }

    public static class Matcher extends SimpleTraitMatcher<MacroDefinition> {

        @Override
        protected @Nullable MacroDefinition test(Cursor cursor) {
            Object value = cursor.getValue();
            if (!(value instanceof Assembler.Instruction) || !isPrototype(cursor)) {
                return null;
            }
            Assembler.Instruction prototype = (Assembler.Instruction) value;
            return Statements.isVariable(prototype.getOperation().getText()) ? null :
                    new MacroDefinition(cursor);
        }
    }

    @Override
    public String toString() {
        return "MACRO " + getName() + " " + getPositionalParameters() + " " + getKeywordParameters();
    }
}
