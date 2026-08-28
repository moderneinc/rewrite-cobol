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

/**
 * One segment of a message: a {@code SEG} and the fields under it.
 * <p>
 * A segment is what a single {@code GU} or {@code ISRT} moves, and it carries its own
 * {@link Message#PREFIX_LENGTH} byte prefix — so a message written as several is several areas and not
 * one long one. Named apart from {@link Segment}, which is a database segment.
 */
@Value
public class MessageSegment implements Trait<Ims.MacroStatement> {

    Cursor cursor;

    public List<MessageField> getFields() {
        List<MessageField> fields = new ArrayList<>();
        for (Statement statement : Definitions.withinMessageSegment(cursor)) {
            new MessageField.Matcher().get(new Cursor(cursor.getParentOrThrow(), statement))
                    .ifPresent(fields::add);
        }
        return fields;
    }

    /**
     * How many bytes the segment occupies, its prefix included, or null where a field's length cannot
     * be worked out.
     */
    public @Nullable Integer getLength() {
        int length = Message.PREFIX_LENGTH;
        for (MessageField field : getFields()) {
            Integer fieldLength = field.getLength();
            if (fieldLength == null) {
                return null;
            }
            length += fieldLength;
        }
        return length;
    }

    /**
     * How many bytes of the message come before this segment, counted from zero.
     */
    public @Nullable Integer getOffset() {
        Message message = getMessage();
        if (message == null) {
            return 0;
        }
        int offset = 0;
        for (MessageSegment segment : message.getSegments()) {
            if (segment.getTree() == getTree()) {
                return offset;
            }
            Integer length = segment.getLength();
            if (length == null) {
                return null;
            }
            offset += length;
        }
        return null;
    }

    public @Nullable Message getMessage() {
        return Definitions.messageOf(cursor);
    }

    public @Nullable LogicalPage getLogicalPage() {
        return Definitions.logicalPageOf(cursor);
    }

    public int getLine() {
        return Definitions.lineOf(cursor, getTree().getOperation());
    }

    public static class Matcher extends SimpleTraitMatcher<MessageSegment> {

        @Override
        protected @Nullable MessageSegment test(Cursor cursor) {
            Object value = cursor.getValue();
            return value instanceof Ims.MacroStatement && ((Ims.MacroStatement) value).isOperation("SEG") ?
                    new MessageSegment(cursor) : null;
        }
    }

    @Override
    public String toString() {
        return "SEG of " + getMessage();
    }
}
