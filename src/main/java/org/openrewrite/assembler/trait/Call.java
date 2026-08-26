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
package org.openrewrite.assembler.trait;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.assembler.tree.Assembler;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.util.List;
import java.util.Locale;

import static java.util.Collections.emptyList;

/**
 * A call out of the module, written either of the two ways a shop writes one.
 * <p>
 * The {@code CALL} macro is the one that says so: {@code CALL CLMU030,MF=(E,U40PLST)} names the
 * subroutine and the parameter list in one statement. The other way is three statements that say
 * nothing on their own — a V-type address constant, a load of it into R15 and a {@code BALR 14,15} —
 * and it is a call because of the shape and not because of any word in it.
 * <p>
 * Which module the name reaches is the binder's answer, not this one: a name the link-edit deck
 * includes is bound in, and a name it leaves out is loaded at run time.
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Call implements Trait<Assembler.Instruction> {

    /**
     * The {@code CALL} statement, or the load of the V-con that a branch turns into a call.
     */
    Cursor cursor;

    /**
     * The name called.
     */
    String target;

    Kind kind;

    /**
     * The arguments of a {@code CALL macro}, in order, as they were written: a name, or {@code (R11)}
     * for an address already in a register. Empty for a call that passes its list some other way.
     */
    List<String> arguments;

    /**
     * The parameter list of an execute form call, from {@code MF=(E,list)}, or null.
     */
    @Nullable
    String parameterList;

    /**
     * Whether the last argument is flagged, which is what {@code VL} writes and what a called program
     * counts its arguments by.
     */
    boolean variableLength;

    /**
     * Whether the call reaches the DL/I language interface rather than a program of the estate. What
     * such a call reached is a database, which {@link DliCall} reads.
     */
    public boolean isDli() {
        return DliCall.isInterface(target);
    }

    public int getLine() {
        return Statements.lineOf(cursor, getTree().getOperation());
    }

    /**
     * How the call was written.
     */
    public enum Kind {
        /**
         * The {@code CALL} macro, which expands to the load and the branch.
         */
        CALL_MACRO,
        /**
         * A V-type address constant loaded into R15 and branched to, which is what the macro would
         * have generated and what a program writes when it wants the registers left alone.
         */
        V_CON
    }

    public static class Matcher extends SimpleTraitMatcher<Call> {

        @Override
        protected @Nullable Call test(Cursor cursor) {
            Object value = cursor.getValue();
            if (!(value instanceof Assembler.Instruction)) {
                return null;
            }
            Assembler.Instruction instruction = (Assembler.Instruction) value;
            return instruction.isOperation("CALL") ? fromMacro(cursor, instruction) :
                    fromAddressConstant(cursor, instruction);
        }
    }

    private static @Nullable Call fromMacro(Cursor cursor, Assembler.Instruction call) {
        List<Assembler.Operand> operands = call.getParameters();
        String target = operands.isEmpty() ? null : operands.get(0).getText();
        if (target == null || target.isEmpty() || target.startsWith("(")) {
            return null;
        }
        List<String> arguments = emptyList();
        String parameterList = null;
        boolean variableLength = false;
        for (Assembler.Operand operand : operands.subList(1, operands.size())) {
            String text = operand.getText();
            if (operand.isKeyword("MF")) {
                List<String> form = operand.getMembers();
                parameterList = form.size() > 1 ? form.get(1) : null;
            } else if (text.startsWith("(") && text.endsWith(")")) {
                arguments = operand.getMembers();
            } else if ("VL".equalsIgnoreCase(text)) {
                variableLength = true;
            }
        }
        return new Call(cursor, target, Kind.CALL_MACRO, arguments, parameterList, variableLength);
    }

    /**
     * The load half of a V-con call, which is a call only when a branch on R15 follows it.
     */
    private static @Nullable Call fromAddressConstant(Cursor cursor, Assembler.Instruction load) {
        if (!load.isOperation("L") && !load.isOperation("LG") && !load.isOperation("LLGT")) {
            return null;
        }
        if (!isRegister(load.getOperandText(0), 15) || !branchesOnRegister15(cursor)) {
            return null;
        }
        String operand = load.getOperandText(1);
        String target = Constants.addressOf(operand, 'V');
        if (target == null && operand != null) {
            target = Constants.addressOf(Statements.definitionOf(cursor, operand), 'V');
        }
        return target == null ? null :
                new Call(cursor, target, Kind.V_CON, emptyList(), null, false);
    }

    /**
     * Whether one of the next few statements branches on R15 and comes back, which is what turns the
     * load into a call. The two are written a statement or two apart, with the parameter list being
     * built between them.
     */
    private static boolean branchesOnRegister15(Cursor cursor) {
        for (Assembler.Instruction instruction : Statements.next(cursor, 3)) {
            String operation = instruction.getOperation().getUpperText();
            if ("BALR".equals(operation) || "BASR".equals(operation) || "BASSM".equals(operation)) {
                return isRegister(instruction.getOperandText(0), 14) &&
                       isRegister(instruction.getOperandText(1), 15);
            }
        }
        return false;
    }

    /**
     * Whether an operand names a general register. The equates that give it a name are copied in from
     * the macro library rather than written here, so the conventional spellings are taken as read.
     */
    private static boolean isRegister(@Nullable String operand, int register) {
        if (operand == null) {
            return false;
        }
        String text = operand.trim().toUpperCase(Locale.ROOT);
        return text.equals(String.valueOf(register)) || text.equals("R" + register) ||
               text.equals("GR" + register) || text.equals("REG" + register);
    }

    @Override
    public String toString() {
        return "CALL " + target + " (" + kind + ")";
    }
}
