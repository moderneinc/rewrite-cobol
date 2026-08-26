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
import org.openrewrite.ims.tree.Statement;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * One screenful: a {@code DPAGE} and the device fields written under it.
 * <p>
 * A format set with two device pages shows two layouts of the same fields, and a message picks one by
 * naming it on {@code LPAGE SOR=}. That is how {@code CLMF02} pages a claim's payments: the same
 * labels at different places, one message layout, two MODs.
 */
@Value
public class DevicePage implements Trait<Ims.MacroStatement> {

    Cursor cursor;

    /**
     * The name a {@code LPAGE SOR=} reaches the page by, which is the label in column 1. Null for a
     * page written without one, which a format set with only one may be.
     */
    public @Nullable String getName() {
        String name = getTree().getSimpleName();
        return name.isEmpty() ? null : name;
    }

    /**
     * Where the cursor lands when the page arrives, from {@code CURSOR=((line,column))}. Null for a
     * page nothing is typed into.
     */
    public @Nullable Position getCursorPosition() {
        return Position.of(Operands.textOf(getTree(), "CURSOR"));
    }

    /**
     * What the operator's unfilled input fields are padded with, from {@code FILL=}: {@code PT} to
     * leave what is there, {@code NULL} to clear it, {@code C'x'} for a character.
     */
    public @Nullable String getFill() {
        return Operands.firstOf(getTree(), "FILL");
    }

    public List<DeviceField> getFields() {
        List<DeviceField> fields = new ArrayList<>();
        for (Statement statement : Definitions.withinDevicePage(cursor)) {
            new DeviceField.Matcher().get(new Cursor(cursor.getParentOrThrow(), statement))
                    .ifPresent(fields::add);
        }
        return fields;
    }

    public @Nullable DeviceField getField(String name) {
        for (DeviceField field : getFields()) {
            if (name.equalsIgnoreCase(field.getName())) {
                return field;
            }
        }
        return null;
    }

    public @Nullable FormatSet getFormatSet() {
        return Definitions.formatSetOf(cursor);
    }

    public int getLine() {
        return Definitions.lineOf(cursor, getTree().getOperation());
    }

    public static class Matcher extends SimpleTraitMatcher<DevicePage> {

        @Override
        protected @Nullable DevicePage test(Cursor cursor) {
            Object value = cursor.getValue();
            return value instanceof Ims.MacroStatement && ((Ims.MacroStatement) value).isOperation("DPAGE") ?
                    new DevicePage(cursor) : null;
        }
    }

    @Override
    public String toString() {
        String name = getName();
        return "DPAGE" + (name == null ? "" : " " + name.toUpperCase(Locale.ROOT));
    }
}
