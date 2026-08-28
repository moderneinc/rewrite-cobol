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

/**
 * The table a job runs in.
 * <p>
 * Every job names a table, but only a SMART table is an entity of its own — one that is ordered,
 * held and rerun as a unit, and that carries conditions and a calendar of its own. Containment is
 * worth an edge for those and not for the rest, which is why {@link #isSmartTable()} is here.
 */
@Value
public class GroupMembership implements Trait<ControlM> {

    Cursor cursor;

    public String getGroup() {
        String group = getJob().getGroup();
        return group == null ? "" : group;
    }

    public ScheduledJob getJob() {
        return new ScheduledJob(cursor);
    }

    /**
     * Whether the table containing this job is a SMART table. A {@code .ctms} panel says which table
     * a job is in but not what kind, so a job read from one always answers false.
     */
    public boolean isSmartTable() {
        Cursor parent = cursor.getParent();
        Object table = parent == null ? null : parent.getValue();
        return table instanceof ControlM.Element && ((ControlM.Element) table).isName("SMART_FOLDER");
    }

    public static class Matcher extends SimpleTraitMatcher<GroupMembership> {

        @Override
        protected @Nullable GroupMembership test(Cursor cursor) {
            ScheduledJob job = new ScheduledJob.Matcher().get(cursor).orElse(null);
            return job == null || job.isTable() || job.getGroup() == null ?
                    null : new GroupMembership(cursor);
        }
    }

    @Override
    public String toString() {
        return getGroup() + " CONTAINS " + getJob().getName();
    }
}
