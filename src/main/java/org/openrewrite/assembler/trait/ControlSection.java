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

import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.assembler.tree.Assembler;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A control section and what it lays out: a {@code CSECT}, {@code RSECT} or {@code START} that holds
 * code, or a {@code DSECT} that holds nothing and describes the bytes somebody else's record occupies.
 * <p>
 * A DSECT is the assembler's copybook. {@code maclib/CLMRECD} describes the same 300 bytes as the
 * COBOL {@code cpy/CLMREC}, field for field, and the only thing that can be checked against the other
 * is the layout — so {@link #getFields()} works the location counter forward through the constants and
 * gives each one where it starts and how long it is.
 * <p>
 * The section runs to the next one, to the {@code END}, or to the {@code EQU *-name} that measures it.
 * A shop writes that EQU to name the length, and it is where the layout stops: what follows it belongs
 * to the program and not to the record.
 */
@Value
public class ControlSection implements Trait<Assembler.Instruction> {

    Cursor cursor;

    /**
     * The name in the label field, or empty for the unnamed section a program with no {@code CSECT}
     * statement of its own is assembled into.
     */
    public String getName() {
        return getTree().getSimpleName();
    }

    public Kind getKind() {
        return Kind.valueOf(getTree().getOperation().getUpperText());
    }

    public boolean isDummy() {
        return getKind() == Kind.DSECT;
    }

    public int getLine() {
        return Statements.lineOf(cursor, getTree().getOperation());
    }

    /**
     * The constants of the section in order, each at the offset the location counter had reached.
     * <p>
     * An offset is null from the first operand this does not read onwards — a length written as an
     * expression, or an {@code ORG} moving the counter — because a layout that is guessed at is worse
     * than one that says it does not know.
     */
    public List<Field> getFields() {
        return read().fields;
    }

    /**
     * How many bytes the section takes, or null where something in it moved the location counter in a
     * way this does not read.
     */
    public @Nullable Integer getLength() {
        return read().length;
    }

    /**
     * The symbol an {@code EQU *-name} gives the length, such as {@code CLMRLEN}, or null where the
     * section measures itself nowhere.
     */
    public @Nullable String getLengthSymbol() {
        return read().lengthSymbol;
    }

    /**
     * The names defined inside the section, which are what a reference from elsewhere in the member
     * reaches.
     */
    public List<String> getLabels() {
        List<String> labels = new ArrayList<>();
        for (Assembler.Instruction instruction : within()) {
            if (!instruction.getSimpleName().isEmpty()) {
                labels.add(instruction.getSimpleName());
            }
        }
        return labels;
    }

    private List<Assembler.Instruction> within() {
        String name = getName();
        return Statements.following(cursor, instruction -> endsSection(instruction, name));
    }

    private static boolean endsSection(Assembler.Instruction instruction, String name) {
        String operation = instruction.getOperation().getUpperText();
        return "CSECT".equals(operation) || "DSECT".equals(operation) || "RSECT".equals(operation) ||
               "START".equals(operation) || "END".equals(operation) ||
               ("EQU".equals(operation) && isLengthOf(instruction, name));
    }

    /**
     * Whether the {@code EQU} is measuring the section, which is written {@code *-name}.
     */
    private static boolean isLengthOf(Assembler.Instruction instruction, String name) {
        String operand = instruction.getOperandText(0);
        return operand != null && !name.isEmpty() &&
               operand.replace(" ", "").equalsIgnoreCase("*-" + name);
    }

    private Layout read() {
        List<Field> fields = new ArrayList<>();
        String name = getName();
        Integer at = 0;
        for (Assembler.Instruction instruction : within()) {
            if (instruction.isOperation("ORG") || instruction.isOperation("LOCTR") ||
                instruction.isOperation("CNOP")) {
                at = null;
                continue;
            }
            if (!instruction.isOperation("DS") && !instruction.isOperation("DC")) {
                continue;
            }
            Integer bytes = null;
            Integer offset = at;
            for (String operand : instruction.getOperandTexts()) {
                Constants.Layout layout = Constants.of(operand);
                if (layout == null) {
                    at = null;
                    break;
                }
                if (at != null && layout.getAlignment() > 1) {
                    at += (layout.getAlignment() - at % layout.getAlignment()) % layout.getAlignment();
                    if (bytes == null) {
                        offset = at;
                    }
                }
                if (bytes == null) {
                    bytes = layout.getBytes();
                }
                if (at != null) {
                    at += layout.getAdvance();
                }
            }
            fields.add(new Field(instruction.getSimpleName(), offset, bytes,
                    instruction.getOperandText(0) == null ? "" : instruction.getOperandText(0),
                    Statements.lineOf(cursor, instruction.getOperation())));
        }

        // The section stops at the EQU that measures it, so the EQU itself is the statement after the
        // ones walked above.
        String lengthSymbol = null;
        for (Assembler.Instruction instruction : Statements.following(cursor,
                instruction -> !instruction.isOperation("EQU") && endsSection(instruction, name))) {
            if (instruction.isOperation("EQU") && isLengthOf(instruction, name)) {
                lengthSymbol = instruction.getSimpleName();
                break;
            }
        }
        return new Layout(fields, at, lengthSymbol);
    }

    /**
     * What a control section is: code, or a description of somebody else's bytes.
     */
    public enum Kind {
        CSECT,
        DSECT,
        RSECT,
        START
    }

    /**
     * One constant of the section, at the offset the location counter had reached.
     */
    @Value
    public static class Field {
        String name;

        @Nullable
        Integer offset;

        /**
         * One item's length, which is the assembler's own {@code L'} attribute: 10 for {@code 0CL10}
         * and 4 for {@code 18F}, where the eighteen of them together advance the counter by 72.
         */
        @Nullable
        Integer bytes;

        /**
         * The operand as written: {@code CL10}, {@code PL7}, {@code 0CL8}, {@code 18F}.
         */
        String type;

        int line;
    }

    @Value
    private static class Layout {
        List<Field> fields;

        @Nullable
        Integer length;

        @Nullable
        String lengthSymbol;
    }

    public static class Matcher extends SimpleTraitMatcher<ControlSection> {

        @Override
        protected @Nullable ControlSection test(Cursor cursor) {
            Object value = cursor.getValue();
            if (!(value instanceof Assembler.Instruction)) {
                return null;
            }
            Assembler.Instruction instruction = (Assembler.Instruction) value;
            if (Statements.isVariable(instruction.getSimpleName())) {
                return null;
            }
            String operation = instruction.getOperation().getUpperText();
            for (Kind kind : Kind.values()) {
                if (kind.name().equals(operation)) {
                    return new ControlSection(cursor);
                }
            }
            return null;
        }
    }

    @Override
    public String toString() {
        return getKind() + " " + getName().toUpperCase(Locale.ROOT);
    }
}
