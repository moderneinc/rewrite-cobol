/*
 * Copyright 2025 the original author or authors.
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
package org.openrewrite.mainframe.bms.trait;

import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.mainframe.bms.tree.Bms;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * A field on a screen: one {@code DFHMDF}.
 * <p>
 * A named field is the end of a data flow that otherwise stops at the edge of the program — the
 * COBOL data item a program moves into is generated from here, so this is where a report can say
 * which screen field, at which position, a value reaches.
 */
@Value
public class Field implements Trait<Bms.MacroStatement> {

    Cursor cursor;

    /**
     * One item the symbolic map generates for a field, and the letter BMS appends to the field's
     * name to make it. Declared in the order the generated copybook writes them.
     * <p>
     * A program never names the field — it names these. {@code MOVE WS-NAME TO TRNNAMEO} puts a
     * value on the screen, while {@code TRNNAMEL} only says how much the operator typed and
     * {@code TRNNAMEC} what colour it is drawn in.
     * <p>
     * Which of these a mapset actually generates depends on its {@code EXTATT}, {@code DSATTS},
     * {@code MAPATTS} and {@code MODE}. They are all offered anyway: this is a lookup from a COBOL
     * data name back to the field it came from, where an extra candidate nothing references costs
     * nothing and a missing one loses the join.
     */
    public enum Subfield {
        /**
         * How much the operator typed, which is how a program tells an empty field from one filled
         * with blanks.
         */
        LENGTH('L'),
        FLAG('F'),
        ATTRIBUTE('A'),
        INPUT('I'),
        COLOR('C'),
        PROGRAMMED_SYMBOLS('P'),
        HIGHLIGHT('H'),
        VALIDATION('V'),
        OUTLINE('U'),
        SOSI('M'),
        TRANSPARENCY('T'),
        OUTPUT('O');

        private final char suffix;

        Subfield(char suffix) {
            this.suffix = suffix;
        }

        public char getSuffix() {
            return suffix;
        }

        /**
         * Whether this subfield carries the field's value rather than how it is drawn or how much of
         * it was typed. Only these two cross the boundary between a program and the screen, so they
         * are the only ones a lineage answer should report.
         */
        public boolean isValue() {
            return this == INPUT || this == OUTPUT;
        }

        static @Nullable Subfield of(char suffix) {
            for (Subfield subfield : values()) {
                if (subfield.suffix == Character.toUpperCase(suffix)) {
                    return subfield;
                }
            }
            return null;
        }
    }

    /**
     * The field's name, or null for a {@code DFHMDF} that writes a literal on the screen. An unnamed
     * field generates nothing in the symbolic map, so no program can read or write it.
     */
    public @Nullable String getName() {
        String name = getTree().getSimpleName();
        return name.isEmpty() ? null : name;
    }

    /**
     * Where the field starts, from {@code POS}. The scalar form is a displacement from the start of
     * the map, so it is decoded against the width of the map the field is written under.
     */
    public @Nullable Position getPosition() {
        MapDefinition map = getMap();
        return Position.of(Operands.textOf(getTree(), "POS"),
                map == null ? Mapset.PAGE_COLUMNS : map.getWidth());
    }

    /**
     * How many characters the field occupies on the screen, from {@code LENGTH=}.
     */
    public @Nullable Integer getLength() {
        return Operands.integerOf(getTree(), "LENGTH");
    }

    /**
     * What the field says before a program puts anything in it, from {@code INITIAL=}, without its
     * quotes. This is where the labels on a screen are written.
     */
    public @Nullable String getInitial() {
        return Operands.unquote(Operands.textOf(getTree(), "INITIAL"));
    }

    public Set<Attribute> getAttributes() {
        Set<Attribute> attributes = EnumSet.noneOf(Attribute.class);
        for (String token : Operands.listOf(getTree(), "ATTRB")) {
            Attribute attribute = Attribute.from(token);
            if (attribute != null) {
                attributes.add(attribute);
            }
        }
        return attributes;
    }

    /**
     * Whether the operator can type into this field, which is what makes it an input. Requires
     * {@code UNPROT} to be written: a field that says nothing about protection is protected.
     */
    public boolean isInput() {
        return getAttributes().contains(Attribute.UNPROT);
    }

    public boolean isProtected() {
        return !isInput();
    }

    public boolean isNumeric() {
        return getAttributes().contains(Attribute.NUM);
    }

    /**
     * Whether the field is hidden as it is typed, which is how a password is written.
     */
    public boolean isDark() {
        return getAttributes().contains(Attribute.DRK);
    }

    public @Nullable String getColor() {
        return Operands.textOf(getTree(), "COLOR");
    }

    public @Nullable String getHighlight() {
        return Operands.textOf(getTree(), "HILIGHT");
    }

    /**
     * The picture the symbolic map's input item is generated with, from {@code PICIN=}.
     */
    public @Nullable String getPictureIn() {
        return Operands.unquote(Operands.textOf(getTree(), "PICIN"));
    }

    public @Nullable String getPictureOut() {
        return Operands.unquote(Operands.textOf(getTree(), "PICOUT"));
    }

    /**
     * How many times the field repeats, from {@code OCCURS=}. This is what makes a screen a table.
     */
    public @Nullable Integer getOccurs() {
        return Operands.integerOf(getTree(), "OCCURS");
    }

    /**
     * The COBOL data name a program moves a value into to put it on the screen: the field's name
     * with {@code O} appended. Null for an unnamed field.
     */
    public @Nullable String getOutputName() {
        return getName() == null ? null : getName() + Subfield.OUTPUT.getSuffix();
    }

    /**
     * The COBOL data name a program reads what the operator typed from: the field's name with
     * {@code I} appended.
     */
    public @Nullable String getInputName() {
        return getName() == null ? null : getName() + Subfield.INPUT.getSuffix();
    }

    /**
     * The COBOL data name holding how much the operator typed, which is how a program tells an empty
     * field from one filled with blanks: the field's name with {@code L} appended.
     */
    public @Nullable String getLengthName() {
        return getName() == null ? null : getName() + Subfield.LENGTH.getSuffix();
    }

    /**
     * Every COBOL data name the symbolic map generates for this field.
     */
    public Set<String> getGeneratedNames() {
        Set<String> names = new LinkedHashSet<>();
        String name = getName();
        if (name != null) {
            for (Subfield subfield : Subfield.values()) {
                names.add(name + subfield.getSuffix());
            }
        }
        return names;
    }

    /**
     * Which item of the symbolic map a COBOL data name is, or null if it is not this field's. This is
     * the join between a program's data flow and the screen: a program naming {@code TRNNAMEO} is
     * writing what the field {@code TRNNAME} shows, and one naming {@code TRNNAMEL} is only asking
     * how much was typed into it.
     */
    public @Nullable Subfield subfieldOf(String cobolDataName) {
        String name = getName();
        String trimmed = cobolDataName.trim();
        if (name == null || trimmed.length() != name.length() + 1 ||
            !trimmed.regionMatches(true, 0, name, 0, name.length())) {
            return null;
        }
        return Subfield.of(trimmed.charAt(name.length()));
    }

    /**
     * Whether a COBOL data name is one the symbolic map generates for this field.
     */
    public boolean generates(String cobolDataName) {
        return subfieldOf(cobolDataName) != null;
    }

    public @Nullable MapDefinition getMap() {
        return Maps.mapOf(cursor);
    }

    public static class Matcher extends SimpleTraitMatcher<Field> {

        @Override
        protected @Nullable Field test(Cursor cursor) {
            Object value = cursor.getValue();
            return value instanceof Bms.MacroStatement &&
                   ((Bms.MacroStatement) value).isOperation("DFHMDF") ? new Field(cursor) : null;
        }
    }

    @Override
    public String toString() {
        String name = getName();
        return "FIELD " + (name == null ? "(literal)" : name.toUpperCase(Locale.ROOT)) +
               (getPosition() == null ? "" : " AT " + getPosition());
    }
}
