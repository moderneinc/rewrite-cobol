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

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A calendar an {@code exportdefcal} file defines: the days of a year the shop counts as working
 * days, or as weekend days, or as whatever else the schedule reads against.
 * <p>
 * A year is written as twelve months of thirty-one positions, {@code Y} or {@code N}, so the days a
 * job actually runs on are only readable by decoding it — which is what turns "runs on
 * {@code CLMWORK}" into the year calendar the JCL Summary report shows.
 */
@Value
public class Calendar implements Trait<ControlM.Element> {

    /**
     * Thirty-one positions per month whether the month has them or not, so a position past the end of
     * a month is written {@code N} and read as no day at all.
     */
    private static final int DAYS_PER_MONTH = 31;

    Cursor cursor;

    public @Nullable String getName() {
        return getTree().getAttributeText("NAME");
    }

    /**
     * {@code Regular} for a calendar of dates, {@code Periodic} for one whose days are named periods,
     * {@code Rule Based} for one generated from a rule.
     */
    public @Nullable String getType() {
        return getTree().getAttributeText("TYPE");
    }

    public @Nullable String getDataCenter() {
        return getTree().getAttributeText("DATACENTER");
    }

    /**
     * The years the export carries, in the order it wrote them.
     */
    public List<Integer> getYears() {
        List<Integer> years = new ArrayList<>();
        for (ControlM.Element year : getTree().getElements("YEAR")) {
            Integer named = numberOf(year.getAttributeText("NAME"));
            if (named != null) {
                years.add(named);
            }
        }
        return years;
    }

    /**
     * Every day of a year this calendar has, or empty for a year it does not carry.
     */
    public List<LocalDate> getDays(int year) {
        List<LocalDate> days = new ArrayList<>();
        for (ControlM.Element written : getTree().getElements("YEAR")) {
            if (!Integer.valueOf(year).equals(numberOf(written.getAttributeText("NAME")))) {
                continue;
            }
            String marks = written.getAttributeText("DAYS");
            if (marks == null) {
                continue;
            }
            for (int at = 0; at < marks.length(); at++) {
                if (marks.charAt(at) != 'Y' && marks.charAt(at) != 'y') {
                    continue;
                }
                try {
                    days.add(LocalDate.of(year, at / DAYS_PER_MONTH + 1, at % DAYS_PER_MONTH + 1));
                } catch (DateTimeException e) {
                    // a position past the end of a short month, which a calendar may still mark
                }
            }
        }
        return days;
    }

    public boolean contains(LocalDate day) {
        Set<LocalDate> days = new LinkedHashSet<>(getDays(day.getYear()));
        return days.contains(day);
    }

    private static @Nullable Integer numberOf(@Nullable String text) {
        try {
            return text == null ? null : Integer.valueOf(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static class Matcher extends SimpleTraitMatcher<Calendar> {

        @Override
        protected @Nullable Calendar test(Cursor cursor) {
            Object value = cursor.getValue();
            return value instanceof ControlM.Element && ((ControlM.Element) value).isName("CALENDAR") ?
                    new Calendar(cursor) : null;
        }
    }

    @Override
    public String toString() {
        return "CALENDAR " + getName();
    }
}
