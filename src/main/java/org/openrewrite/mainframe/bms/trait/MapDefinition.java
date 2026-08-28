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
import org.openrewrite.mainframe.bms.tree.Statement;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A map: the {@code DFHMDI} and the fields written under it.
 * <p>
 * A map is one screen, or one region of one, and it is the name a program sends. Named for the
 * macro rather than simply {@code Map}, which in Java means something else entirely.
 */
@Value
public class MapDefinition implements Trait<Bms.MacroStatement> {

    Cursor cursor;

    public String getName() {
        return getTree().getSimpleName();
    }

    /**
     * How many lines the map occupies, from {@code SIZE=(lines,columns)}.
     */
    public @Nullable Integer getLines() {
        return sizeAt(0);
    }

    public @Nullable Integer getColumns() {
        return sizeAt(1);
    }

    /**
     * How many columns wide the map is: its own {@code SIZE}, or the page width of the device the
     * mapset is written for when it declares none. A scalar {@code POS=n} counts against this.
     */
    public int getWidth() {
        Integer columns = getColumns();
        if (columns != null && columns > 0) {
            return columns;
        }
        Mapset mapset = getMapset();
        return mapset == null ? Mapset.PAGE_COLUMNS : mapset.getPageColumns();
    }

    /**
     * Where the map begins on the screen, from {@code LINE=}. Absent on a map that fills the screen.
     */
    public @Nullable Integer getLine() {
        return Operands.integerOf(getTree(), "LINE");
    }

    public @Nullable Integer getColumn() {
        return Operands.integerOf(getTree(), "COLUMN");
    }

    public List<Field> getFields() {
        List<Field> fields = new ArrayList<>();
        Field.Matcher matcher = new Field.Matcher();
        for (Statement statement : Maps.withinMap(cursor)) {
            matcher.get(new Cursor(cursor.getParentOrThrow(), statement)).ifPresent(fields::add);
        }
        return fields;
    }

    /**
     * The fields a program can name, which are the ones the symbolic map generates data items for.
     * A {@code DFHMDF} without a name field writes a literal on the screen and nothing else.
     */
    public List<Field> getNamedFields() {
        List<Field> named = new ArrayList<>();
        for (Field field : getFields()) {
            if (field.getName() != null) {
                named.add(field);
            }
        }
        return named;
    }

    /**
     * The COBOL record a program moves values into to fill this map: the map's name with {@code O}
     * appended. It redefines the input record, so the two are one piece of storage under two names.
     */
    public String getOutputRecordName() {
        return getName() + Field.Subfield.OUTPUT.getSuffix();
    }

    /**
     * The COBOL record a program receives this map into: the map's name with {@code I} appended.
     */
    public String getInputRecordName() {
        return getName() + Field.Subfield.INPUT.getSuffix();
    }

    /**
     * Whether a COBOL record name is the symbolic map generated for this map. This is how a map is
     * recovered from a command that names none: {@code SEND MAP(CCARD-NEXT-MAP) FROM(CACTVWAO)}
     * decides which map it sends at run time, but only one map generates {@code CACTVWAO}.
     */
    public boolean generates(String cobolRecordName) {
        String trimmed = cobolRecordName.trim();
        return trimmed.equalsIgnoreCase(getOutputRecordName()) ||
               trimmed.equalsIgnoreCase(getInputRecordName());
    }

    public @Nullable Field getField(String name) {
        for (Field field : getFields()) {
            if (name.equalsIgnoreCase(field.getName())) {
                return field;
            }
        }
        return null;
    }

    public @Nullable Mapset getMapset() {
        return Maps.mapsetOf(cursor);
    }

    private @Nullable Integer sizeAt(int index) {
        List<String> size = Operands.listOf(getTree(), "SIZE");
        return size.size() == 2 ? Operands.integerOf(size.get(index)) : null;
    }

    public static class Matcher extends SimpleTraitMatcher<MapDefinition> {

        @Override
        protected @Nullable MapDefinition test(Cursor cursor) {
            Object value = cursor.getValue();
            if (!(value instanceof Bms.MacroStatement)) {
                return null;
            }
            Bms.MacroStatement statement = (Bms.MacroStatement) value;
            return statement.isOperation("DFHMDI") && !statement.getSimpleName().isEmpty() ?
                    new MapDefinition(cursor) : null;
        }
    }

    @Override
    public String toString() {
        return "MAP " + getName().toUpperCase(Locale.ROOT);
    }
}
