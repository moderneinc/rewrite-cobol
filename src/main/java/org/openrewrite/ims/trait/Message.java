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
 * A message the program exchanges with a terminal: one {@code MSG} and its fields.
 * <p>
 * This is the name a program passes. A MOD — {@code TYPE=OUTPUT} — is what an {@code ISRT} against the
 * I/O PCB names as its fourth argument; a MID — {@code TYPE=INPUT} — is what the operator's reply
 * arrives formatted by, and the call that reads it names nothing at all.
 * <p>
 * The message is also the layout of the area the program declares it with. The fields map onto the
 * copybook one to one and in order, after the prefix IMS supplies, so {@link #getLength()} is what a
 * copybook can be checked against and {@link MessageField#getOffset()} says where each field lands.
 */
@Value
public class Message implements Trait<Ims.MacroStatement> {

    /**
     * The {@code LL} and {@code ZZ} IMS puts in front of every segment of a message. No {@code MFLD}
     * describes them and the program declares them, so they are counted here and nowhere else.
     */
    public static final int PREFIX_LENGTH = 4;

    Cursor cursor;

    /**
     * The name the message is known by, which is the label in column 1.
     */
    public String getName() {
        return getTree().getSimpleName();
    }

    /**
     * From {@code TYPE=}: {@code INPUT} for a MID, {@code OUTPUT} for a MOD.
     */
    public @Nullable String getType() {
        return Operands.firstOf(getTree(), "TYPE");
    }

    /**
     * Whether this is a MID, the message a terminal sends in.
     */
    public boolean isInput() {
        return "INPUT".equalsIgnoreCase(getType());
    }

    /**
     * Whether this is a MOD, the message a program sends out.
     */
    public boolean isOutput() {
        return "OUTPUT".equalsIgnoreCase(getType());
    }

    /**
     * The format set the message is laid out on, from the first value of {@code SOR=}. The second
     * value says what to do with a device feature mismatch and names nothing.
     */
    public @Nullable String getFormatName() {
        return Operands.firstOf(getTree(), "SOR");
    }

    /**
     * The format set itself, where it is written in the same member — which is how MFS libraries are
     * kept. Null when the {@code SOR=} names one held elsewhere.
     */
    public @Nullable FormatSet getFormat() {
        String name = getFormatName();
        if (name == null) {
            return null;
        }
        for (FormatSet format : Definitions.formatSetsIn(cursor)) {
            if (name.equalsIgnoreCase(format.getName())) {
                return format;
            }
        }
        return null;
    }

    /**
     * The message that formats what comes back, from {@code NXT=}. A MOD names the MID its reply
     * arrives on and a MID names the MOD its answer goes out on, so the two chain. Null for a message
     * that is not answered, which is what a printed page is.
     */
    public @Nullable String getNextName() {
        return Operands.firstOf(getTree(), "NXT");
    }

    /**
     * The logical pages, in the order they are written. A message that writes none has one implied
     * layout, which is the common case.
     */
    public List<LogicalPage> getLogicalPages() {
        return within(new LogicalPage.Matcher());
    }

    /**
     * The segments, in the order they are written. Each is one segment of the message queue, with a
     * prefix of its own.
     */
    public List<MessageSegment> getSegments() {
        return within(new MessageSegment.Matcher());
    }

    /**
     * Every field of the message, in source order.
     */
    public List<MessageField> getFields() {
        return within(new MessageField.Matcher());
    }

    /**
     * How many bytes the area the program declares this message with holds, prefixes included. Null
     * where a field's length cannot be worked out, since a total short by one field is worse than
     * none.
     */
    public @Nullable Integer getLength() {
        int length = 0;
        for (MessageSegment segment : getSegments()) {
            Integer segmentLength = segment.getLength();
            if (segmentLength == null) {
                return null;
            }
            length += segmentLength;
        }
        return length;
    }

    public int getLine() {
        return Definitions.lineOf(cursor, getTree().getOperation());
    }

    private <T extends Trait<?>> List<T> within(SimpleTraitMatcher<T> matcher) {
        List<T> found = new ArrayList<>();
        for (Statement statement : Definitions.withinMessage(cursor)) {
            matcher.get(new Cursor(cursor.getParentOrThrow(), statement)).ifPresent(found::add);
        }
        return found;
    }

    public static class Matcher extends SimpleTraitMatcher<Message> {

        @Override
        protected @Nullable Message test(Cursor cursor) {
            Object value = cursor.getValue();
            if (!(value instanceof Ims.MacroStatement)) {
                return null;
            }
            // The label is what a program passes and what a NXT= names, so a MSG written without one
            // is not a message anything could reach.
            Ims.MacroStatement statement = (Ims.MacroStatement) value;
            return statement.isOperation("MSG") && !statement.getSimpleName().isEmpty() ?
                    new Message(cursor) : null;
        }
    }

    @Override
    public String toString() {
        return "MSG " + getName().toUpperCase(Locale.ROOT) + " " + getType();
    }
}
