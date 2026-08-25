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

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.SourceFile;
import org.openrewrite.controlm.ControlMParser;
import org.openrewrite.controlm.tree.ControlM;
import org.openrewrite.test.RewriteTest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.controlm.tree.ParserAssertions.controlM;

/**
 * The same job, browsed on the z/OS panel and exported as XML, has to read the same either way.
 * Desjardins ships one of the two dialects and nobody knows yet which, so a trait that answered
 * differently would be a report that changed with the export format.
 */
class ScheduledJobTest implements RewriteTest {

    private static final String PANEL =
      """
        +---------------------------------- BROWSE -----------------------------------+
        | MEMNAME CLMJ010         MEMLIB CLM.PROD.JCL                                 |
        | OWNER   CLMPROD        TASKTYPE JOB      PREVENT-NCT2 Y DFLT N              |
        | APPL    CLAIMS                          GROUP CLMNIGHT                      |
        | DESC    DAILY CLAIM EXTRACT                                                 |
        | DOCMEM  CLMJ010        DOCLIB CLM.PROD.DOC                                  |
        | =========================================================================== |
        | SCHEDULE RBC                                                                |
        | DAYS    ALL                                                   DCAL CLMWORK  |
        | WDAYS                                                         WCAL          |
        | CONFCAL          SHIFT       RETRO N MAXWAIT 00  D-CAT                      |
        | =========================================================================== |
        | IN       CLMMAST-CLOSED       ODAT                                          |
        | CONTROL  CLMMAST              S                                             |
        | =========================================================================== |
        | OUT      CLMNIGHT_CLMJ010_OK  ODAT +                                        |
        | AUTO-ARCHIVE Y          SYSDB    Y      MAXDAYS      MAXRUNS                |
        | =========================================================================== |
        | APPL TYPE                                  APPL VER                         |
        """;

    private static final String EXPORT =
      """
        <?xml version="1.0" encoding="ISO-8859-1"?>
        <DEFTABLE>
          <SMART_FOLDER FOLDER_NAME="CLMNIGHT" APPLICATION="CLAIMS" SUB_APPLICATION="CLMNIGHT" JOBNAME="CLMNIGHT" TASKTYPE="SMART Table" DAYSCAL="CLMWORK" TYPE="2">
            <JOB JOBISN="1" APPLICATION="CLAIMS" SUB_APPLICATION="CLMNIGHT" JOBNAME="CLMJ010" MEMNAME="CLMJ010" MEMLIB="CLM.PROD.JCL" DESCRIPTION="DAILY CLAIM EXTRACT" RUN_AS="CLMPROD" TASKTYPE="Job" DAYSCAL="CLMWORK" CONFCAL="">
              <INCOND NAME="CLMMAST-CLOSED" ODATE="ODAT" AND_OR="A" />
              <OUTCOND NAME="CLMNIGHT_CLMJ010_OK" ODATE="ODAT" SIGN="+" />
            </JOB>
            <OUTCOND NAME="CLAIMS_CLMNIGHT_OK" ODATE="ODAT" SIGN="+" />
          </SMART_FOLDER>
        </DEFTABLE>
        """;

    @Test
    void readsThePanel() {
        rewriteRun(
          controlM(PANEL, spec -> spec.afterRecipe(cu -> {
              ScheduledJob job = jobsOf(cu).get(0);
              assertThat(job.getName()).isEqualTo("CLMJ010");
              assertThat(job.getMemberName()).isEqualTo("CLMJ010");
              assertThat(job.getLibrary()).isEqualTo("CLM.PROD.JCL");
              assertThat(job.getApplication()).isEqualTo("CLAIMS");
              assertThat(job.getGroup()).isEqualTo("CLMNIGHT");
              assertThat(job.getTaskType()).isEqualTo("JOB");
              assertThat(job.getOwner()).isEqualTo("CLMPROD");
              assertThat(job.getDescription()).isEqualTo("DAILY CLAIM EXTRACT");
              assertThat(job.getNode()).isNull();
              assertThat(job.isTable()).isFalse();

              assertThat(job.getInConditions()).singleElement().satisfies(in -> {
                  assertThat(in.getName()).isEqualTo("CLMMAST-CLOSED");
                  assertThat(in.getDate()).isEqualTo("ODAT");
              });
              assertThat(job.getOutConditions()).singleElement().satisfies(out -> {
                  assertThat(out.getName()).isEqualTo("CLMNIGHT_CLMJ010_OK");
                  assertThat(out.getDate()).isEqualTo("ODAT");
                  assertThat(out.isAdded()).isTrue();
              });
          }))
        );
    }

    @Test
    void readsTheExport() {
        rewriteRun(
          controlM(EXPORT, spec -> spec.afterRecipe(cu -> {
              List<ScheduledJob> jobs = jobsOf(cu);
              assertThat(jobs).hasSize(2);

              ScheduledJob table = jobs.get(0);
              assertThat(table.isTable()).isTrue();
              assertThat(table.getName()).isEqualTo("CLMNIGHT");
              assertThat(table.getMemberName()).isNull();
              assertThat(table.getOutConditions()).singleElement()
                .extracting(OutCondition::getName).isEqualTo("CLAIMS_CLMNIGHT_OK");

              ScheduledJob job = jobs.get(1);
              assertThat(job.getName()).isEqualTo("CLMJ010");
              assertThat(job.getMemberName()).isEqualTo("CLMJ010");
              assertThat(job.getLibrary()).isEqualTo("CLM.PROD.JCL");
              assertThat(job.getApplication()).isEqualTo("CLAIMS");
              assertThat(job.getGroup()).isEqualTo("CLMNIGHT");
              assertThat(job.getTaskType()).isEqualTo("Job");
              assertThat(job.getOwner()).isEqualTo("CLMPROD");
              assertThat(job.getDescription()).isEqualTo("DAILY CLAIM EXTRACT");
              assertThat(job.isTable()).isFalse();

              assertThat(job.getInConditions()).singleElement().satisfies(in -> {
                  assertThat(in.getName()).isEqualTo("CLMMAST-CLOSED");
                  assertThat(in.getDate()).isEqualTo("ODAT");
              });
              assertThat(job.getOutConditions()).singleElement().satisfies(out -> {
                  assertThat(out.getName()).isEqualTo("CLMNIGHT_CLMJ010_OK");
                  assertThat(out.isAdded()).isTrue();
              });
          }))
        );
    }

    /**
     * The one field the panel writes as a word with nothing to separate it from the next field's
     * name: an empty {@code CONFCAL} is followed by {@code SHIFT}, which is not a calendar.
     */
    @Test
    void readsOnlyTheCalendarsNamed() {
        rewriteRun(
          controlM(PANEL, spec -> spec.afterRecipe(cu ->
            assertThat(jobsOf(cu).get(0).getCalendarReferences()).singleElement().satisfies(reference -> {
                assertThat(reference.getKind()).isEqualTo(CalendarReference.Kind.DAYS);
                assertThat(reference.getCalendar()).isEqualTo("CLMWORK");
            })))
        );
    }

    @Test
    void readsCalendarsFromTheExport() {
        rewriteRun(
          controlM(EXPORT, spec -> spec.afterRecipe(cu -> {
              List<ScheduledJob> jobs = jobsOf(cu);
              assertThat(jobs.get(0).getCalendarReferences()).singleElement()
                .extracting(CalendarReference::getCalendar).isEqualTo("CLMWORK");
              // CONFCAL is written empty, which is the export's way of saying it names none
              assertThat(jobs.get(1).getCalendarReferences()).singleElement().satisfies(reference -> {
                  assertThat(reference.getKind()).isEqualTo(CalendarReference.Kind.DAYS);
                  assertThat(reference.getCalendar()).isEqualTo("CLMWORK");
              });
          }))
        );
    }

    @Test
    void onlyASmartTableContains() {
        rewriteRun(
          controlM(EXPORT, spec -> spec.afterRecipe(cu -> {
              List<GroupMembership> memberships = new GroupMembership.Matcher().lower(cu)
                .collect(Collectors.toList());
              assertThat(memberships).singleElement().satisfies(membership -> {
                  assertThat(membership.getGroup()).isEqualTo("CLMNIGHT");
                  assertThat(membership.getJob().getName()).isEqualTo("CLMJ010");
                  assertThat(membership.isSmartTable()).isTrue();
              });
          }))
        );
    }

    /**
     * A condition is the only thing either job names, so a chain is found across files the same way
     * it is found within one.
     */
    @Test
    void triggersAcrossFiles() {
        String successor = PANEL
          .replace("CLMJ010", "CLMJ020")
          .replace("CLMMAST-CLOSED", "CLMNIGHT_CLMJ010_OK");
        List<SourceFile> parsed = ControlMParser.builder().build()
          .parse(new InMemoryExecutionContext(), EXPORT, successor)
          .collect(Collectors.toList());

        List<ScheduledJob> jobs = new ArrayList<>();
        for (SourceFile source : parsed) {
            jobs.addAll(jobsOf((ControlM.CompilationUnit) source));
        }

        List<Schedules.Trigger> triggers = Schedules.triggersAmong(jobs);
        assertThat(triggers).singleElement().satisfies(trigger -> {
            assertThat(trigger.getPredecessor().getName()).isEqualTo("CLMJ010");
            assertThat(trigger.getSuccessor().getName()).isEqualTo("CLMJ020");
            assertThat(trigger.getCondition()).isEqualTo("CLMNIGHT_CLMJ010_OK");
            assertThat(trigger.getDate()).isEqualTo("ODAT");
        });
    }

    private static List<ScheduledJob> jobsOf(ControlM.CompilationUnit cu) {
        return new ScheduledJob.Matcher().lower(cu).collect(Collectors.toList());
    }
}
