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
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.util.List;
import java.util.Locale;

/**
 * A field on the screen: one {@code DFLD}.
 * <p>
 * Two kinds are written the same way. A labelled field is somewhere the program puts a value or reads
 * one, and an unlabelled field is a literal — the headings and prompts that are on the screen whatever
 * the program does. Only a labelled field can be named by an {@code MFLD}.
 */
@Value
public class DeviceField implements Trait<Ims.MacroStatement> {

    Cursor cursor;

    /**
     * The label in column 1, which is the name an {@code MFLD} reaches the field by. Null for a field
     * that writes a literal, which no message can name.
     */
    public @Nullable String getName() {
        String name = getTree().getSimpleName();
        return name.isEmpty() ? null : name;
    }

    /**
     * The text written on the screen, without its quotes and with the blanks it was padded with.
     * Null for a labelled field, whose contents come from the program.
     */
    public @Nullable String getLiteral() {
        String operand = Operands.positionalOf(getTree());
        return Operands.isQuoted(operand) ? Operands.unquote(operand) : null;
    }

    /**
     * Where the field starts, from {@code POS=(line,column)}, counted from one.
     */
    public @Nullable Position getPosition() {
        return Position.of(Operands.textOf(getTree(), "POS"));
    }

    /**
     * How many characters the field occupies, from {@code LTH=}. A literal field usually writes none,
     * and is as long as the literal.
     */
    public @Nullable Integer getLength() {
        Integer length = Operands.integerOf(getTree(), "LTH");
        if (length != null) {
            return length;
        }
        String literal = getLiteral();
        return literal == null ? null : literal.length();
    }

    /**
     * How the field is drawn and whether it may be typed into, from {@code ATTR=}: {@code PROT},
     * {@code NUM}, {@code HI}, {@code NODISP}.
     */
    public List<String> getAttributes() {
        return Operands.listOf(getTree(), "ATTR");
    }

    /**
     * The extended attributes, from {@code EATTR=}: colour, highlighting, outlining. Only a terminal
     * that has them honours them.
     */
    public List<String> getExtendedAttributes() {
        return Operands.listOf(getTree(), "EATTR");
    }

    /**
     * Whether the operator cannot type into the field, from {@code ATTR=PROT}. Every literal is
     * protected whether it says so or not; this reports what was written.
     */
    public boolean isProtected() {
        for (String attribute : getAttributes()) {
            if ("PROT".equalsIgnoreCase(attribute)) {
                return true;
            }
        }
        return false;
    }

    public @Nullable DevicePage getDevicePage() {
        return Definitions.devicePageOf(cursor);
    }

    public @Nullable FormatSet getFormatSet() {
        return Definitions.formatSetOf(cursor);
    }

    public int getLine() {
        return Definitions.lineOf(cursor, getTree().getOperation());
    }

    public static class Matcher extends SimpleTraitMatcher<DeviceField> {

        @Override
        protected @Nullable DeviceField test(Cursor cursor) {
            Object value = cursor.getValue();
            return value instanceof Ims.MacroStatement && ((Ims.MacroStatement) value).isOperation("DFLD") ?
                    new DeviceField(cursor) : null;
        }
    }

    @Override
    public String toString() {
        String name = getName();
        return "DFLD " + (name == null ? "'" + getLiteral() + "'" : name.toUpperCase(Locale.ROOT)) +
               " at " + getPosition();
    }
}
