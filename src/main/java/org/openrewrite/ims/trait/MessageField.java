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

import java.util.List;

/**
 * One field of a message: an {@code MFLD}.
 * <p>
 * This is where the screen and the program meet. The field names a {@code DFLD}, so it has a place on
 * the screen and a length; and it is one field of the area the program declares the message with,
 * matched <em>by order and length and never by name</em> — the COBOL data name is the programmer's
 * and has nothing to do with the label here.
 */
@Value
public class MessageField implements Trait<Ims.MacroStatement> {

    Cursor cursor;

    /**
     * The device field this one is filled from or written to. Null where the message field carries a
     * literal instead, which is how a MID supplies the transaction code.
     */
    public @Nullable String getDeviceFieldName() {
        String operand = Operands.positionalOf(getTree());
        if (operand == null || Operands.isQuoted(operand)) {
            return null;
        }
        // MFLD (SDATE,DATE2) names a device field and the system literal that fills it.
        List<String> members = Operands.membersOf(operand);
        String name = members.isEmpty() ? operand : members.get(0);
        return Operands.isQuoted(name) ? null : name;
    }

    /**
     * The literal the field carries, without its quotes and with the blanks it was padded with.
     */
    public @Nullable String getLiteral() {
        String operand = Operands.positionalOf(getTree());
        if (Operands.isQuoted(operand)) {
            return Operands.unquote(operand);
        }
        List<String> members = Operands.membersOf(operand);
        return members.isEmpty() || !Operands.isQuoted(members.get(0)) ? null :
                Operands.unquote(members.get(0));
    }

    /**
     * The device field itself, where the format set is written in the same member. A format set with
     * two device pages carries the same labels twice, so the one the message's own {@code LPAGE}
     * names is preferred over the first.
     */
    public @Nullable DeviceField getDeviceField() {
        String name = getDeviceFieldName();
        if (name == null) {
            return null;
        }
        LogicalPage page = getLogicalPage();
        DevicePage devicePage = page == null ? null : page.getDevicePage();
        if (devicePage != null) {
            return devicePage.getField(name);
        }
        Message message = getMessage();
        FormatSet format = message == null ? null : message.getFormat();
        return format == null ? null : format.getField(name);
    }

    /**
     * How many bytes the field occupies in the message area, from {@code LTH=}. A field that writes
     * none is as long as its literal, or as long as the device field it names.
     */
    public @Nullable Integer getLength() {
        Integer length = Operands.integerOf(getTree(), "LTH");
        if (length != null) {
            return length;
        }
        String literal = getLiteral();
        if (literal != null) {
            return literal.length();
        }
        DeviceField field = getDeviceField();
        return field == null ? null : field.getLength();
    }

    /**
     * Where the field starts in the area the program declares the message with, counted from zero and
     * past the prefix IMS supplies. Null where an earlier field's length cannot be worked out, since a
     * displacement short by one field points at the wrong data item.
     */
    public @Nullable Integer getOffset() {
        MessageSegment segment = getSegment();
        if (segment == null) {
            return null;
        }
        Integer offset = segment.getOffset();
        if (offset == null) {
            return null;
        }
        offset += Message.PREFIX_LENGTH;
        for (MessageField field : segment.getFields()) {
            if (field.getTree() == getTree()) {
                return offset;
            }
            Integer length = field.getLength();
            if (length == null) {
                return null;
            }
            offset += length;
        }
        return null;
    }

    /**
     * From {@code ATTR=}, which on a message field says whether the program supplies the device
     * field's attribute bytes itself rather than how the field is drawn.
     */
    public List<String> getAttributes() {
        return Operands.listOf(getTree(), "ATTR");
    }

    /**
     * Which end of the field the data is put at and what the rest is filled with, from {@code JUST=}:
     * {@code L} or {@code R}.
     */
    public @Nullable String getJustification() {
        return Operands.firstOf(getTree(), "JUST");
    }

    public @Nullable MessageSegment getSegment() {
        return Definitions.messageSegmentOf(cursor);
    }

    public @Nullable LogicalPage getLogicalPage() {
        return Definitions.logicalPageOf(cursor);
    }

    public @Nullable Message getMessage() {
        return Definitions.messageOf(cursor);
    }

    public int getLine() {
        return Definitions.lineOf(cursor, getTree().getOperation());
    }

    public static class Matcher extends SimpleTraitMatcher<MessageField> {

        @Override
        protected @Nullable MessageField test(Cursor cursor) {
            Object value = cursor.getValue();
            return value instanceof Ims.MacroStatement && ((Ims.MacroStatement) value).isOperation("MFLD") ?
                    new MessageField(cursor) : null;
        }
    }

    @Override
    public String toString() {
        String name = getDeviceFieldName();
        return "MFLD " + (name == null ? "'" + getLiteral() + "'" : name) + " LTH=" + getLength();
    }
}
