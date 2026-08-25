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

import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.controlm.tree.ControlM;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.util.ArrayList;
import java.util.List;

/**
 * One job the scheduler runs: the {@code JOB} of an export, or the whole of a {@code .ctms} panel.
 * <p>
 * This is the far end of the batch estate. A JCL member says what a job does; only the schedule says
 * when it runs, what has to have finished first, and which library the member is taken from — so this
 * is where "who runs this program, and when" is answered.
 * <p>
 * A SMART table is a job too. It carries its own task type, calendar and conditions, and the jobs of
 * the table run under it, so it is read here and answers null for {@link #getMemberName()}:
 * {@link #isTable()} tells the two apart.
 */
@Value
public class ScheduledJob implements Trait<ControlM> {

    Cursor cursor;

    /**
     * The name the scheduler knows the job by. On the panel that is the member name, which is what
     * the z/OS screen titles the definition with.
     */
    public @Nullable String getName() {
        ControlM.Element element = element();
        return element == null ? getMemberName() : element.getAttributeText("JOBNAME");
    }

    /**
     * The JCL member run, from {@code MEMNAME}. This is the join to the batch half of the portfolio,
     * and it is null for a SMART table, which runs the jobs under it rather than a member of its own.
     */
    public @Nullable String getMemberName() {
        return value("MEMNAME", "MEMNAME");
    }

    /**
     * The library the member is taken from, from {@code MEMLIB}. Two applications routinely schedule
     * a member of the same name out of different libraries, so an edge that ignores this joins the
     * wrong job to the wrong JCL.
     */
    public @Nullable String getLibrary() {
        return value("MEMLIB", "MEMLIB");
    }

    public @Nullable String getApplication() {
        return value("APPLICATION", "APPL");
    }

    /**
     * The table the job runs in: the folder it is written under in an export, the panel's
     * {@code GROUP} field otherwise.
     * <p>
     * A job also carries the name in {@code PARENT_FOLDER}, and that copy goes stale — CardDemo's
     * export has a job under one table still naming the one it was moved out of — so it is only read
     * when the job is not written under a folder at all.
     */
    public @Nullable String getGroup() {
        ControlM.Element element = element();
        if (element == null) {
            return Schedules.panelValue(definition(), "GROUP");
        }
        Cursor parent = cursor.getParent();
        if (parent != null && parent.getValue() instanceof ControlM.Element) {
            String folder = ((ControlM.Element) parent.getValue()).getAttributeText("FOLDER_NAME");
            if (folder != null) {
                return folder;
            }
        }
        String group = element.getAttributeText("PARENT_FOLDER");
        return group == null ? element.getAttributeText("SUB_APPLICATION") : group;
    }

    /**
     * What kind of work the scheduler is being asked to do: {@code Job} for a member submitted to
     * JES, {@code SMART Table} for a table, and a dozen others for the work Control-M does itself.
     */
    public @Nullable String getTaskType() {
        return value("TASKTYPE", "TASKTYPE");
    }

    /**
     * Where the job is submitted: the NJE node on the panel, the agent's node id in an export.
     */
    public @Nullable String getNode() {
        ControlM.Element element = element();
        if (element == null) {
            return Schedules.panelValue(definition(), "NJE NODE");
        }
        String node = element.getAttributeText("NODEID");
        return node == null ? element.getAttributeText("NJE_NODE") : node;
    }

    /**
     * The user the job runs under, from {@code RUN_AS} or the panel's {@code OWNER}.
     */
    public @Nullable String getOwner() {
        return value("RUN_AS", "OWNER");
    }

    public @Nullable String getDescription() {
        ControlM.Element element = element();
        return element == null ? Schedules.panelDescription(definition()) : element.getAttributeText("DESCRIPTION");
    }

    /**
     * Whether this is a SMART table rather than a job that submits a member.
     */
    public boolean isTable() {
        ControlM.Element element = element();
        return element != null && element.isName("SMART_FOLDER");
    }

    /**
     * The conditions that have to be there before the scheduler will run this job.
     */
    public List<InCondition> getInConditions() {
        List<InCondition> conditions = new ArrayList<>();
        ControlM.Element element = element();
        if (element != null) {
            for (ControlM.Element in : element.getElements("INCOND")) {
                conditions.add(new InCondition(new Cursor(cursor, in)));
            }
            return conditions;
        }
        ControlM.InputSection input = Schedules.sectionOf(cursor, ControlM.InputSection.class);
        if (input != null) {
            Cursor within = new Cursor(cursor.getParentOrThrow(), input);
            for (ControlM line : input.getInputNames()) {
                if (line instanceof ControlM.Input) {
                    Cursor at = new Cursor(within, line);
                    for (ControlM name : ((ControlM.Input) line).getInput()) {
                        new InCondition.Matcher().get(new Cursor(at, name)).ifPresent(conditions::add);
                    }
                }
            }
        }
        return conditions;
    }

    /**
     * The conditions this job adds when it ends, and the ones it takes away — the two halves of how
     * one job makes the next eligible.
     */
    public List<OutCondition> getOutConditions() {
        List<OutCondition> conditions = new ArrayList<>();
        ControlM.Element element = element();
        if (element != null) {
            for (ControlM.Element out : element.getElements("OUTCOND")) {
                conditions.add(new OutCondition(new Cursor(cursor, out)));
            }
            return conditions;
        }
        ControlM.OutputSection output = Schedules.sectionOf(cursor, ControlM.OutputSection.class);
        if (output != null) {
            Cursor within = new Cursor(cursor.getParentOrThrow(), output);
            for (ControlM line : output.getOutputNames()) {
                if (line instanceof ControlM.Output) {
                    Cursor at = new Cursor(within, line);
                    for (ControlM name : ((ControlM.Output) line).getOutput()) {
                        new OutCondition.Matcher().get(new Cursor(at, name)).ifPresent(conditions::add);
                    }
                }
            }
        }
        return conditions;
    }

    /**
     * The calendars the job's scheduling days are validated against.
     */
    public List<CalendarReference> getCalendarReferences() {
        List<CalendarReference> references = new ArrayList<>();
        ControlM.Element element = element();
        if (element != null) {
            for (ControlM.Attribute attribute : element.getAttributes()) {
                new CalendarReference.Matcher().get(new Cursor(cursor, attribute)).ifPresent(references::add);
            }
            return references;
        }
        ControlM.ScheduleSection schedule = Schedules.sectionOf(cursor, ControlM.ScheduleSection.class);
        if (schedule != null) {
            Cursor within = new Cursor(cursor.getParentOrThrow(), schedule);
            for (ControlM line : schedule.getLines()) {
                if (line instanceof ControlM.Line) {
                    Cursor at = new Cursor(within, line);
                    for (ControlM word : ((ControlM.Line) line).getParameters()) {
                        new CalendarReference.Matcher().get(new Cursor(at, word)).ifPresent(references::add);
                    }
                }
            }
        }
        return references;
    }

    /**
     * The table this job runs in, or null for a table itself and for a job the panel says nothing
     * about.
     */
    public @Nullable GroupMembership getGroupMembership() {
        return new GroupMembership.Matcher().get(cursor).orElse(null);
    }

    /**
     * The same fact under the name each dialect writes it with: an attribute of the export's element,
     * or a field of the panel's definition section.
     */
    private @Nullable String value(String attribute, String panelField) {
        ControlM.Element element = element();
        return element == null ? Schedules.panelValue(definition(), panelField) : element.getAttributeText(attribute);
    }

    private ControlM.@Nullable Element element() {
        Object value = cursor.getValue();
        return value instanceof ControlM.Element ? (ControlM.Element) value : null;
    }

    private ControlM.DefinitionSection definition() {
        return (ControlM.DefinitionSection) cursor.getValue();
    }

    public static class Matcher extends SimpleTraitMatcher<ScheduledJob> {

        @Override
        protected @Nullable ScheduledJob test(Cursor cursor) {
            Object value = cursor.getValue();
            if (value instanceof ControlM.DefinitionSection) {
                return new ScheduledJob(cursor);
            }
            return value instanceof ControlM.Element &&
                   (((ControlM.Element) value).isName("JOB") || ((ControlM.Element) value).isName("SMART_FOLDER")) ?
                    new ScheduledJob(cursor) : null;
        }
    }

    @Override
    public String toString() {
        return (isTable() ? "TABLE " : "JOB ") + getName();
    }
}
