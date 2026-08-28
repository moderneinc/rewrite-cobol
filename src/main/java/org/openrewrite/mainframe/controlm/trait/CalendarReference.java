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
package org.openrewrite.mainframe.controlm.trait;

import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.mainframe.controlm.tree.ControlM;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.util.List;
import java.util.Locale;

/**
 * A job naming a calendar its scheduling days are read against.
 * <p>
 * The days a definition writes are only candidates — {@code WDAYS 1,2,3,4,5} is every weekday until a
 * calendar says which of those the shop is open on. Which of the three fields names the calendar says
 * what it decides, so the kind is part of the reference and not a detail of it.
 */
@Value
public class CalendarReference implements Trait<ControlM> {

    Cursor cursor;

    Kind kind;

    /**
     * What the calendar decides.
     */
    public enum Kind {
        /**
         * {@code CONFCAL}: the days the job may run on at all. A day the calendar does not have is a
         * day the job is dropped or shifted, whatever else the definition says.
         */
        CONFIRMATION,
        /**
         * {@code DAYSCAL}, the panel's {@code DCAL}: which of the month days written apply.
         */
        DAYS,
        /**
         * {@code WEEKSCAL}, the panel's {@code WCAL}: which of the weekdays written apply.
         */
        WEEK
    }

    /**
     * The name of the calendar, which is the name a {@code DEFCAL} export defines it under.
     */
    public String getCalendar() {
        Object value = cursor.getValue();
        if (value instanceof ControlM.Attribute) {
            return ((ControlM.Attribute) value).getValueText();
        }
        ControlM.Word named = valueAfter(cursor);
        return named == null ? "" : named.getText();
    }

    private static @Nullable Kind kindOf(String field) {
        switch (field.toUpperCase(Locale.ROOT)) {
            case "CONFCAL":
                return Kind.CONFIRMATION;
            case "DAYSCAL":
            case "DCAL":
                return Kind.DAYS;
            case "WEEKSCAL":
            case "WCAL":
                return Kind.WEEK;
            default:
                return null;
        }
    }

    /**
     * The word written after a panel field, or null when the field is empty and the next word on the
     * line is the name of the field that follows it.
     */
    private static ControlM.@Nullable Word valueAfter(Cursor cursor) {
        Cursor parent = cursor.getParent();
        if (parent == null || !(parent.getValue() instanceof ControlM.Line) ||
            parent.getParent() == null || !(parent.getParent().getValue() instanceof ControlM.ScheduleSection)) {
            return null;
        }
        List<ControlM> words = ((ControlM.Line) parent.getValue()).getParameters();
        int at = words.indexOf(cursor.getValue());
        if (at < 0 || at + 1 >= words.size() || !(words.get(at + 1) instanceof ControlM.Word)) {
            return null;
        }
        ControlM.Word next = (ControlM.Word) words.get(at + 1);
        return Schedules.isScheduleField(next.getText()) ? null : next;
    }

    public static class Matcher extends SimpleTraitMatcher<CalendarReference> {

        @Override
        protected @Nullable CalendarReference test(Cursor cursor) {
            Object value = cursor.getValue();
            if (value instanceof ControlM.Attribute) {
                ControlM.Attribute attribute = (ControlM.Attribute) value;
                Kind kind = kindOf(attribute.getName());
                return kind == null || attribute.getValueText().isEmpty() ?
                        null : new CalendarReference(cursor, kind);
            }
            if (!(value instanceof ControlM.Word)) {
                return null;
            }
            Kind kind = kindOf(((ControlM.Word) value).getText());
            return kind == null || valueAfter(cursor) == null ? null : new CalendarReference(cursor, kind);
        }
    }

    @Override
    public String toString() {
        return kind + " CALENDAR " + getCalendar();
    }
}
