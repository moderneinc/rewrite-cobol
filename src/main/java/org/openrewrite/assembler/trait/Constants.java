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
import lombok.NoArgsConstructor;
import lombok.Value;
import org.jspecify.annotations.Nullable;

/**
 * How many bytes a {@code DC} or {@code DS} operand takes, which is the whole of what gives a
 * {@code DSECT} a layout.
 * <p>
 * An operand is a duplication factor, a type letter, the modifiers of that type and a nominal value —
 * {@code 0CL10}, {@code PL7}, {@code 18F}, {@code X'4020'} — and the byte count comes from whichever
 * of those were written: the length modifier where there is one, the value where the type takes its
 * length from the value, and the type's own length otherwise. Nothing here evaluates an expression, so
 * a length written as one is unknown rather than guessed at.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class Constants {

    /**
     * What one operand of a {@code DC} or {@code DS} lays down.
     */
    @Value
    static class Layout {
        /**
         * The duplication factor, which is 0 for a group item that names the bytes after it without
         * taking any of its own.
         */
        int duplication;

        /**
         * What one duplication of the operand occupies, nominal values and all.
         */
        int bytes;

        /**
         * The boundary the assembler moves the location counter to before laying the operand down,
         * which is 1 wherever a length modifier was written.
         */
        int alignment;

        int getAdvance() {
            return duplication * bytes;
        }
    }

    /**
     * The layout of an operand, or null where it is written in a way this does not read — a
     * duplication factor or a length given as an expression, or a type that takes its length from a
     * value it does not carry.
     */
    static @Nullable Layout of(@Nullable String operand) {
        if (operand == null || operand.isEmpty()) {
            return null;
        }
        int at = 0;
        int duplication = 1;
        int digits = at;
        while (digits < operand.length() && Character.isDigit(operand.charAt(digits))) {
            digits++;
        }
        if (digits > at) {
            duplication = Integer.parseInt(operand.substring(at, digits));
            at = digits;
        } else if (operand.charAt(at) == '(') {
            return null;
        }
        if (at >= operand.length() || TYPES.indexOf(Character.toUpperCase(operand.charAt(at))) < 0) {
            return null;
        }
        char type = Character.toUpperCase(operand.charAt(at++));

        Integer length = null;
        boolean aligned = true;
        while (at < operand.length() && "LSEP".indexOf(Character.toUpperCase(operand.charAt(at))) >= 0) {
            char modifier = Character.toUpperCase(operand.charAt(at++));
            if (at < operand.length() && operand.charAt(at) == '(') {
                return null;
            }
            int from = at;
            while (at < operand.length() && (Character.isDigit(operand.charAt(at)) ||
                                             (at == from && operand.charAt(at) == '-'))) {
                at++;
            }
            if (at == from) {
                return null;
            }
            if (modifier == 'L') {
                length = Integer.valueOf(operand.substring(from, at));
                aligned = false;
            }
        }

        String nominal = nominalOf(operand, at);
        int bytes;
        if (length != null) {
            bytes = length * values(type, nominal);
        } else if (IMPLICIT.indexOf(type) >= 0) {
            bytes = implicitLength(type) * values(type, nominal);
        } else if (nominal == null) {
            return null;
        } else {
            bytes = lengthOf(type, nominal);
        }
        return new Layout(duplication, bytes, aligned ? alignmentOf(type) : 1);
    }

    /**
     * The nominal value a {@code DC} holds, quotes taken off and padding kept, or null for an operand
     * that carries none. The padding is what a length is counted from, so this does not trim.
     */
    static @Nullable String valueOf(@Nullable String operand) {
        if (operand == null) {
            return null;
        }
        int quote = operand.indexOf('\'');
        return quote < 0 || !operand.endsWith("'") || operand.length() < quote + 2 ?
                null : operand.substring(quote + 1, operand.length() - 1);
    }

    /**
     * The name an address constant reaches: the {@code NAME} of {@code V(NAME)}, {@code A(NAME)} or
     * the literal {@code =V(NAME)}.
     */
    static @Nullable String addressOf(@Nullable String operand, char type) {
        if (operand == null) {
            return null;
        }
        String text = operand.startsWith("=") ? operand.substring(1) : operand;
        if (text.length() < 4 || Character.toUpperCase(text.charAt(0)) != type ||
            text.charAt(1) != '(' || !text.endsWith(")")) {
            return null;
        }
        String name = text.substring(2, text.length() - 1).trim();
        return name.isEmpty() ? null : name;
    }

    /**
     * The types that take a length from what they hold rather than from the type.
     */
    private static final String FROM_VALUE = "CGXBPZ";

    /**
     * The types whose length is the same whatever they hold.
     */
    private static final String IMPLICIT = "ADEFHJLQRSVY";

    private static final String TYPES = FROM_VALUE + IMPLICIT;

    /**
     * How many nominal values were written. Only the types that hold a number take a list of them; in
     * a character or hexadecimal constant a comma is data.
     */
    private static int values(char type, @Nullable String nominal) {
        if (nominal == null || "CGXB".indexOf(type) >= 0) {
            return 1;
        }
        int count = 1;
        int depth = 0;
        for (int i = 0; i < nominal.length(); i++) {
            char c = nominal.charAt(i);
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
            } else if (c == ',' && depth == 0) {
                count++;
            }
        }
        return count;
    }

    private static int implicitLength(char type) {
        switch (type) {
            case 'D':
            case 'R':
                return 8;
            case 'H':
            case 'S':
            case 'Y':
                return 2;
            case 'L':
                return 16;
            default:
                return 4;
        }
    }

    private static int alignmentOf(char type) {
        switch (type) {
            case 'D':
            case 'L':
            case 'R':
                return 8;
            case 'H':
            case 'S':
            case 'Y':
                return 2;
            case 'A':
            case 'E':
            case 'F':
            case 'J':
            case 'Q':
            case 'V':
                return 4;
            default:
                return 1;
        }
    }

    /**
     * What a value of a type that has no length of its own occupies.
     */
    private static int lengthOf(char type, String nominal) {
        switch (type) {
            case 'X':
                return (nominal.length() + 1) / 2;
            case 'B':
                return (nominal.length() + 7) / 8;
            case 'P':
                return (packedDigits(nominal) + 2) / 2;
            case 'Z':
                return packedDigits(nominal);
            default:
                return nominal.length();
        }
    }

    private static int packedDigits(String nominal) {
        int digits = 0;
        for (int i = 0; i < nominal.length(); i++) {
            if (Character.isDigit(nominal.charAt(i))) {
                digits++;
            }
        }
        return digits;
    }

    /**
     * The nominal value written after the modifiers, in quotes or in parentheses.
     */
    private static @Nullable String nominalOf(String operand, int at) {
        if (at >= operand.length()) {
            return null;
        }
        char open = operand.charAt(at);
        if (open == '\'' && operand.endsWith("'") && operand.length() > at + 1) {
            return operand.substring(at + 1, operand.length() - 1);
        }
        if (open == '(' && operand.endsWith(")")) {
            return operand.substring(at + 1, operand.length() - 1);
        }
        return null;
    }
}
