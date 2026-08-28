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
import org.openrewrite.mainframe.ims.tree.Statement;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A device format: the {@code FMT} and everything down to its {@code FMTEND}.
 * <p>
 * This is the MFS answer to a BMS map set. It says what a terminal shows — the device, the pages, and
 * every field at its position — while the {@link Message}s of the same member say what the program
 * exchanges. The two meet at {@link MessageField#getDeviceField()}: an {@code MFLD} names a
 * {@code DFLD}, so a field of the program's message area has a place on the screen and a length.
 */
@Value
public class FormatSet implements Trait<Ims.MacroStatement> {

    Cursor cursor;

    /**
     * The name the format set is known by, which is the label in column 1: a {@code FMT} has no
     * operand to carry it.
     */
    public String getName() {
        return getTree().getSimpleName();
    }

    /**
     * The devices the format set is written for. One is the common case; a format set that serves
     * several terminal types writes a {@code DEV} for each.
     */
    public List<Device> getDevices() {
        return within(new Device.Matcher());
    }

    public List<Division> getDivisions() {
        return within(new Division.Matcher());
    }

    /**
     * The device pages in the order they are written. A message chooses between them by naming one on
     * its {@code LPAGE SOR=}, which is how one format set shows two layouts.
     */
    public List<DevicePage> getDevicePages() {
        return within(new DevicePage.Matcher());
    }

    /**
     * Every device field of the format set, across all of its pages.
     */
    public List<DeviceField> getDeviceFields() {
        return within(new DeviceField.Matcher());
    }

    /**
     * The device field of this name, or null. Where two pages carry the same label — which is how
     * {@code CLMF02} lets one message layout serve both — this is the one on the first page, so a
     * caller that knows the page should ask {@link DevicePage#getField} instead.
     */
    public @Nullable DeviceField getField(String name) {
        for (DeviceField field : getDeviceFields()) {
            if (name.equalsIgnoreCase(field.getName())) {
                return field;
            }
        }
        return null;
    }

    public int getLine() {
        return Definitions.lineOf(cursor, getTree().getOperation());
    }

    private <T extends Trait<?>> List<T> within(SimpleTraitMatcher<T> matcher) {
        List<T> found = new ArrayList<>();
        for (Statement statement : Definitions.withinFormatSet(cursor)) {
            matcher.get(new Cursor(cursor.getParentOrThrow(), statement)).ifPresent(found::add);
        }
        return found;
    }

    public static class Matcher extends SimpleTraitMatcher<FormatSet> {

        @Override
        protected @Nullable FormatSet test(Cursor cursor) {
            Object value = cursor.getValue();
            if (!(value instanceof Ims.MacroStatement)) {
                return null;
            }
            // The label is the format set's only name, so a FMT written without one is not a format
            // set anything could reach.
            Ims.MacroStatement statement = (Ims.MacroStatement) value;
            return statement.isOperation("FMT") && !statement.getSimpleName().isEmpty() ?
                    new FormatSet(cursor) : null;
        }
    }

    @Override
    public String toString() {
        return "FMT " + getName().toUpperCase(Locale.ROOT);
    }
}
