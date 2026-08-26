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
package org.openrewrite.ims.trait;

import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.ims.tree.Ims;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The terminal a format set is written for: one {@code DEV}.
 * <p>
 * How big the screen is, and which of the operator's keys reach the program at all. A key listed on
 * {@code PFK=} puts its literal in a named field, so the program tests a word rather than a key
 * number — which is the only place that word is written down.
 */
@Value
public class Device implements Trait<Ims.MacroStatement> {

    Cursor cursor;

    /**
     * The terminal type, from {@code TYPE=}: {@code 3270} for a display, {@code 3270P} for a printer.
     * The model comes separately, so {@code TYPE=(3270,2)} yields {@code 3270} here.
     */
    public @Nullable String getType() {
        return Operands.firstOf(getTree(), "TYPE");
    }

    /**
     * The screen size, from the second value of {@code TYPE=(3270,n)}: 1 is 12 lines by 40 columns
     * and 2 is 24 by 80. Null where the type names no model.
     */
    public @Nullable Integer getModel() {
        List<String> type = Operands.listOf(getTree(), "TYPE");
        return type.size() < 2 ? null : Operands.integerOf(type.get(1));
    }

    /**
     * Whether the device prints rather than displays, which is what {@code 3270P} says. A printed
     * format is never answered, so its messages have no {@code NXT=}.
     */
    public boolean isPrinter() {
        String type = getType();
        return type != null && type.toUpperCase(Locale.ROOT).endsWith("P");
    }

    /**
     * The device features the format set depends on, from {@code FEAT=}.
     */
    public List<String> getFeatures() {
        return Operands.listOf(getTree(), "FEAT");
    }

    /**
     * The device field IMS writes its own messages into, from {@code SYSMSG=}. The program never
     * fills it, which is why it is a {@code DFLD} with no {@code MFLD}.
     */
    public @Nullable String getSystemMessageField() {
        return Operands.firstOf(getTree(), "SYSMSG");
    }

    /**
     * The default system control area, from {@code DSCA=}, which says what the terminal does when the
     * page arrives: sound the alarm, unlock the keyboard, erase what was there.
     */
    public @Nullable String getSystemControlArea() {
        return Operands.firstOf(getTree(), "DSCA");
    }

    /**
     * The device field a pressed function key's literal lands in, from the first value of
     * {@code PFK=}. It is an {@code MFLD} of the input message, so the program reads a word like
     * {@code EXIT} rather than a key number.
     */
    public @Nullable String getFunctionKeyField() {
        String field = Operands.firstOf(getTree(), "PFK");
        return field == null || field.indexOf('=') >= 0 ? null : field;
    }

    /**
     * What each function key sends, in the order written.
     */
    public List<FunctionKey> getFunctionKeys() {
        List<FunctionKey> keys = new ArrayList<>();
        for (String member : Operands.listOf(getTree(), "PFK")) {
            int equals = member.indexOf('=');
            if (equals <= 0) {
                continue;
            }
            Integer number = Operands.integerOf(member.substring(0, equals));
            String literal = Operands.unquote(member.substring(equals + 1));
            if (number != null && literal != null) {
                keys.add(new FunctionKey(number, literal));
            }
        }
        return keys;
    }

    public @Nullable FormatSet getFormatSet() {
        return Definitions.formatSetOf(cursor);
    }

    public int getLine() {
        return Definitions.lineOf(cursor, getTree().getOperation());
    }

    /**
     * One key of a {@code PFK=}, and the word it puts in the field.
     */
    @Value
    public static class FunctionKey {

        /**
         * The key the operator pressed, so 3 is PF3.
         */
        int number;

        /**
         * What arrives in the field, exactly as written: the program's level 88s test it character
         * for character.
         */
        String literal;

        @Override
        public String toString() {
            return "PF" + number + "='" + literal + "'";
        }
    }

    public static class Matcher extends SimpleTraitMatcher<Device> {

        @Override
        protected @Nullable Device test(Cursor cursor) {
            Object value = cursor.getValue();
            return value instanceof Ims.MacroStatement && ((Ims.MacroStatement) value).isOperation("DEV") ?
                    new Device(cursor) : null;
        }
    }

    @Override
    public String toString() {
        Integer model = getModel();
        return "DEV " + getType() + (model == null ? "" : " model " + model);
    }
}
