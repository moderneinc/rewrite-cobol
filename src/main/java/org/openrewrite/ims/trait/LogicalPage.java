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

/**
 * Which screenful a message is laid out on: one {@code LPAGE}.
 * <p>
 * Two MODs over one format set differ by nothing but this. {@code CLMI2O} and {@code CLMI2P} carry the
 * same nineteen fields against the same 312 byte area, and the {@code SOR=} here is what makes one the
 * first page of a claim's payments and the other the continuation.
 */
@Value
public class LogicalPage implements Trait<Ims.MacroStatement> {

    Cursor cursor;

    /**
     * The device page the fields are placed on, from {@code SOR=}.
     */
    public @Nullable String getDevicePageName() {
        return Operands.firstOf(getTree(), "SOR");
    }

    /**
     * The device page itself, where the format set is written in the same member.
     */
    public @Nullable DevicePage getDevicePage() {
        String name = getDevicePageName();
        Message message = getMessage();
        FormatSet format = message == null ? null : message.getFormat();
        if (name == null || format == null) {
            return null;
        }
        for (DevicePage page : format.getDevicePages()) {
            if (name.equalsIgnoreCase(page.getName())) {
                return page;
            }
        }
        return null;
    }

    public List<MessageSegment> getSegments() {
        List<MessageSegment> segments = new ArrayList<>();
        for (Statement statement : Definitions.withinLogicalPage(cursor)) {
            new MessageSegment.Matcher().get(new Cursor(cursor.getParentOrThrow(), statement))
                    .ifPresent(segments::add);
        }
        return segments;
    }

    public @Nullable Message getMessage() {
        return Definitions.messageOf(cursor);
    }

    public int getLine() {
        return Definitions.lineOf(cursor, getTree().getOperation());
    }

    public static class Matcher extends SimpleTraitMatcher<LogicalPage> {

        @Override
        protected @Nullable LogicalPage test(Cursor cursor) {
            Object value = cursor.getValue();
            return value instanceof Ims.MacroStatement && ((Ims.MacroStatement) value).isOperation("LPAGE") ?
                    new LogicalPage(cursor) : null;
        }
    }

    @Override
    public String toString() {
        return "LPAGE " + getDevicePageName();
    }
}
