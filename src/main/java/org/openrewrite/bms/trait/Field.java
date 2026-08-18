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
package org.openrewrite.bms.trait;

import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.bms.tree.Bms;
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

    /**
     * What BMS appends to a field's name when it generates the symbolic map, in the order the
     * generated copybook writes them.
     * <p>
     * Which of these are actually generated depends on the mapset's {@code EXTATT}, {@code DSATTS}
     * and {@code MAPATTS}, and on {@code MODE}. They are all offered anyway: this is a lookup from a
     * COBOL data name back to the field it came from, where an extra candidate nothing references
     * costs nothing and a missing one loses the join.
     */
    private static final String SUFFIXES = "LFAICPHVMUTO";

    Cursor cursor;

    /**
     * The field's name, or null for a {@code DFHMDF} that writes a literal on the screen. An unnamed
     * field generates nothing in the symbolic map, so no program can read or write it.
     */
    public @Nullable String getName() {
        String name = getTree().getSimpleName();
        return name.isEmpty() ? null : name;
    }

    public @Nullable Position getPosition() {
        return Position.of(Operands.textOf(getTree(), "POS"));
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
        return getName() == null ? null : getName() + "O";
    }

    /**
     * The COBOL data name a program reads what the operator typed from: the field's name with
     * {@code I} appended.
     */
    public @Nullable String getInputName() {
        return getName() == null ? null : getName() + "I";
    }

    /**
     * The COBOL data name holding how much the operator typed, which is how a program tells an empty
     * field from one filled with blanks: the field's name with {@code L} appended.
     */
    public @Nullable String getLengthName() {
        return getName() == null ? null : getName() + "L";
    }

    /**
     * Every COBOL data name the symbolic map generates for this field.
     */
    public Set<String> getGeneratedNames() {
        Set<String> names = new LinkedHashSet<>();
        String name = getName();
        if (name != null) {
            for (char suffix : SUFFIXES.toCharArray()) {
                names.add(name + suffix);
            }
        }
        return names;
    }

    /**
     * Whether a COBOL data name is one the symbolic map generates for this field. This is the join
     * between a program's data flow and the screen: a program naming {@code TRNNAMEO} is writing to
     * the field {@code TRNNAME}.
     */
    public boolean generates(String cobolDataName) {
        String trimmed = cobolDataName.trim();
        for (String generated : getGeneratedNames()) {
            if (generated.equalsIgnoreCase(trimmed)) {
                return true;
            }
        }
        return false;
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
