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
package org.openrewrite.controlm.trait;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.controlm.tree.ControlM;
import org.openrewrite.controlm.tree.Section;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyList;

/**
 * Reading a job definition out of either dialect, and joining the definitions to each other.
 * <p>
 * A schedule reaches this repository twice: as the panel a z/OS operator browses, where a field is a
 * word at a fixed place on a line, and as the XML {@code exportdeftable} writes, where the same field
 * is an attribute. The two shapes meet here so that every trait answers the same question once.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Schedules {

    /**
     * The field names of the panel's schedule section. A calendar field left empty is followed on the
     * line by the next field's name rather than by nothing, so reading its value means knowing these.
     */
    private static final Set<String> SCHEDULE_FIELDS = new HashSet<>(Arrays.asList(
            "SCHEDULE", "RBC", "RELATIONSHIP", "(AND/OR)", "AND/OR", "DAYS", "DCAL", "WDAYS", "WCAL",
            "MONTHS", "DATES", "CONFCAL", "SHIFT", "RETRO", "MAXWAIT", "D-CAT", "MINIMUM", "PDS",
            "DEFINITION", "ACTIVE", "FROM", "UNTIL"));

    /**
     * A job that adds a condition and a job that requires it, which is the order the two actually run
     * in. The condition is the only thing either job names, so the pair is found by name and holds
     * across tables and across files.
     */
    @Value
    public static class Trigger {
        ScheduledJob predecessor;
        ScheduledJob successor;
        String condition;

        /**
         * The date the successor asks for the condition on: {@code ODAT} for today's run,
         * {@code PREV} for the run before, which is how a job waits on yesterday's backup.
         */
        @Nullable
        String date;
    }

    /**
     * Every predecessor-to-successor pair among these jobs. Pass the jobs of a whole portfolio, not of
     * one file: a table's jobs routinely wait on conditions another table's jobs add.
     */
    public static List<Trigger> triggersAmong(Collection<ScheduledJob> jobs) {
        Map<String, List<ScheduledJob>> adders = new LinkedHashMap<>();
        for (ScheduledJob job : jobs) {
            for (OutCondition out : job.getOutConditions()) {
                if (out.isAdded()) {
                    adders.computeIfAbsent(out.getName(), name -> new ArrayList<>()).add(job);
                }
            }
        }
        List<Trigger> triggers = new ArrayList<>();
        for (ScheduledJob successor : jobs) {
            for (InCondition in : successor.getInConditions()) {
                for (ScheduledJob predecessor : adders.getOrDefault(in.getName(), emptyList())) {
                    if (predecessor.getTree() != successor.getTree()) {
                        triggers.add(new Trigger(predecessor, successor, in.getName(), in.getDate()));
                    }
                }
            }
        }
        return triggers;
    }

    /**
     * The value of a field of the panel's definition section, or null when the panel leaves it empty.
     */
    static @Nullable String panelValue(ControlM.DefinitionSection definition, String field) {
        for (ControlM line : definition.getLines()) {
            if (line instanceof ControlM.Line) {
                for (ControlM written : ((ControlM.Line) line).getParameters()) {
                    if (written instanceof ControlM.Parameter &&
                        ((ControlM.Parameter) written).getOption().equalsIgnoreCase(field)) {
                        ControlM.Word value = ((ControlM.Parameter) written).getValue();
                        return value == null ? null : value.getText();
                    }
                }
            }
        }
        return null;
    }

    static @Nullable String panelDescription(ControlM.DefinitionSection definition) {
        for (ControlM line : definition.getLines()) {
            if (line instanceof ControlM.Line) {
                for (ControlM written : ((ControlM.Line) line).getParameters()) {
                    if (written instanceof ControlM.Description) {
                        StringBuilder text = new StringBuilder();
                        for (ControlM.Word word : ((ControlM.Description) written).getDescription()) {
                            if (text.length() > 0) {
                                text.append(' ');
                            }
                            text.append(word.getText());
                        }
                        return text.length() == 0 ? null : text.toString();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Whether a word of the panel's schedule section names a field rather than a value.
     */
    static boolean isScheduleField(String word) {
        return SCHEDULE_FIELDS.contains(word.toUpperCase());
    }

    /**
     * The section of the file this cursor is in, or null when the panel writes none. A {@code .ctms}
     * member is one job, so its sections are siblings of the definition the job is read from.
     */
    static <S extends Section> @Nullable S sectionOf(Cursor cursor, Class<S> kind) {
        Object parent = cursor.getParentOrThrow().getValue();
        if (!(parent instanceof ControlM.CompilationUnit)) {
            return null;
        }
        for (Section section : ((ControlM.CompilationUnit) parent).getSections()) {
            if (kind.isInstance(section)) {
                return kind.cast(section);
            }
        }
        return null;
    }
}
