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
import org.openrewrite.bms.tree.Statement;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A map set: the {@code DFHMSD} and the maps written under it.
 * <p>
 * This is the name a program sends, as {@code EXEC CICS SEND MAP('COSGN0A') MAPSET('COSGN00')}, so
 * it is the unit the presentation half of an application is described in.
 */
@Value
public class Mapset implements Trait<Bms.MacroStatement> {

    static final int PAGE_COLUMNS = 80;

    Cursor cursor;

    public String getName() {
        return getTree().getSimpleName();
    }

    /**
     * The device the maps here are written for, from {@code TERM=}.
     */
    public @Nullable String getTerminal() {
        return Operands.textOf(getTree(), "TERM");
    }

    /**
     * How wide a page is on that device. A map that declares no {@code SIZE} is one page, so this is
     * the width its fields are placed in. Only the model 1 display is narrower than 80 columns.
     */
    public int getPageColumns() {
        String terminal = getTerminal();
        return terminal != null && "3270-1".equalsIgnoreCase(terminal.trim()) ? 40 : PAGE_COLUMNS;
    }

    /**
     * The language the symbolic map is generated for, from {@code LANG=}.
     */
    public @Nullable String getLanguage() {
        return Operands.textOf(getTree(), "LANG");
    }

    /**
     * {@code IN}, {@code OUT} or {@code INOUT}, from {@code MODE=}: whether the maps here are
     * received, sent, or both.
     */
    public @Nullable String getMode() {
        return Operands.textOf(getTree(), "MODE");
    }

    public @Nullable String getStorage() {
        return Operands.textOf(getTree(), "STORAGE");
    }

    /**
     * The device controls applied to every map here, from {@code CTRL=}: {@code FREEKB},
     * {@code ALARM} and the rest.
     */
    public List<String> getControls() {
        return Operands.listOf(getTree(), "CTRL");
    }

    /**
     * Whether the symbolic map carries the extended attributes — colour, highlighting, validation.
     * {@code EXTATT=MAPONLY} sets them on the map without generating the fields for them, so only
     * {@code YES} answers this.
     */
    public boolean hasExtendedAttributes() {
        String extatt = Operands.textOf(getTree(), "EXTATT");
        return extatt != null && "YES".equalsIgnoreCase(extatt.trim());
    }

    /**
     * Whether the symbolic map begins with the twelve byte filler a CICS program needs in front of
     * it, from {@code TIOAPFX=YES}. Nothing else in the map moves, so this changes where the fields
     * start and not what they are called.
     */
    public boolean hasTerminalPrefix() {
        String tioapfx = Operands.textOf(getTree(), "TIOAPFX");
        return tioapfx != null && "YES".equalsIgnoreCase(tioapfx.trim());
    }

    public List<MapDefinition> getMaps() {
        List<MapDefinition> maps = new ArrayList<>();
        MapDefinition.Matcher matcher = new MapDefinition.Matcher();
        for (Statement statement : Maps.withinMapset(cursor)) {
            matcher.get(new Cursor(cursor.getParentOrThrow(), statement)).ifPresent(maps::add);
        }
        return maps;
    }

    public @Nullable MapDefinition getMap(String name) {
        for (MapDefinition map : getMaps()) {
            if (map.getName().equalsIgnoreCase(name)) {
                return map;
            }
        }
        return null;
    }

    public static class Matcher extends SimpleTraitMatcher<Mapset> {

        @Override
        protected @Nullable Mapset test(Cursor cursor) {
            Object value = cursor.getValue();
            if (!(value instanceof Bms.MacroStatement)) {
                return null;
            }
            Bms.MacroStatement statement = (Bms.MacroStatement) value;
            // DFHMSD TYPE=FINAL closes a mapset rather than opening one, and writes no name field.
            return statement.isOperation("DFHMSD") && !statement.getSimpleName().isEmpty() ?
                    new Mapset(cursor) : null;
        }
    }

    @Override
    public String toString() {
        return "MAPSET " + getName().toUpperCase(Locale.ROOT);
    }
}
