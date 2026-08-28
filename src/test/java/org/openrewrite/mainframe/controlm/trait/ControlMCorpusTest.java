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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.SourceFile;
import org.openrewrite.mainframe.cobol.Corpus;
import org.openrewrite.mainframe.controlm.ControlMParser;
import org.openrewrite.mainframe.controlm.tree.ControlM;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reads the Control-M of the corpus and reports what the traits found. Gated on
 * {@code CONTROLM_CORPUS} pointing at a checkout, because the corpus is not redistributed here.
 * <p>
 * The fixture application ships the same twenty-four jobs twice — once as the z/OS panel, once as the
 * XML an export writes — and section 11 of its INTERLINKS document counts every edge by hand. Both
 * dialects are read here and checked against those counts, so a field read out of the wrong place in
 * one of them cannot pass: the two would stop agreeing.
 */
@EnabledIfEnvironmentVariable(named = "CONTROLM_CORPUS", matches = ".+")
class ControlMCorpusTest {

    @Test
    void readsRealSchedules() throws IOException {
        Path corpus = Paths.get(System.getenv("CONTROLM_CORPUS"));

        int members = 0;
        int jobs = 0;
        int scheduled = 0;
        int conditions = 0;
        int calendars = 0;
        List<String> failures = new ArrayList<>();
        boolean fixtureFound = false;

        System.out.println("schedules read, by application:");
        for (Path repository : Corpus.repositories(corpus)) {
            List<Path> files = Corpus.schedules(repository);
            if (files.isEmpty()) {
                continue;
            }
            fixtureFound |= Corpus.isFixture(repository);
            List<ScheduledJob> exported = new ArrayList<>();
            List<ScheduledJob> browsed = new ArrayList<>();
            List<Calendar> defined = new ArrayList<>();
            int read = 0;
            for (Path member : files) {
                members++;
                String name = corpus.relativize(member).toString();
                String source = new String(Files.readAllBytes(member));
                List<SourceFile> parsed = ControlMParser.builder().build()
                  .parse(new InMemoryExecutionContext(), source)
                  .collect(Collectors.toList());
                if (parsed.isEmpty() || !(parsed.get(0) instanceof ControlM.CompilationUnit)) {
                    failures.add(name + ": did not parse");
                    continue;
                }
                ControlM.CompilationUnit cu = (ControlM.CompilationUnit) parsed.get(0);
                if (!source.equals(cu.printAll())) {
                    failures.add(name + ": did not print back");
                    continue;
                }
                read++;

                List<ScheduledJob> here = new ScheduledJob.Matcher().lower(cu).collect(Collectors.toList());
                (isExport(source) ? exported : browsed).addAll(here);
                defined.addAll(new Calendar.Matcher().lower(cu).collect(Collectors.toList()));
            }

            jobs += exported.size() + browsed.size();
            calendars += defined.size();
            for (ScheduledJob job : concat(exported, browsed)) {
                if (job.getMemberName() != null) {
                    scheduled++;
                }
                conditions += job.getInConditions().size() + job.getOutConditions().size();
            }
            System.out.printf("  %-40s %3d of %3d files, %3d jobs (%d exported, %d browsed), " +
                              "%3d triggers, %d calendars%n",
              repository.getFileName(), read, files.size(), exported.size() + browsed.size(),
              exported.size(), browsed.size(),
              Schedules.triggersAmong(exported).size() + Schedules.triggersAmong(browsed).size(),
              defined.size());

            if (Corpus.isFixture(repository)) {
                assertFixture(exported, browsed, defined);
            } else if ("carddemo".equals(repository.getFileName().toString())) {
                assertCardDemo(exported);
            }
        }
        assertThat(members).as("no Control-M found under %s", corpus).isPositive();

        System.out.printf("Control-M corpus: %d files, %d jobs (%d scheduling a member), " +
                          "%d conditions, %d calendars%n",
          members, jobs, scheduled, conditions, calendars);
        if (!failures.isEmpty()) {
            System.out.println("failures:");
            failures.forEach(failure -> System.out.println("  " + failure));
        }

        assertThat(failures).isEmpty();
        assertThat(fixtureFound).as("mainframe-fixtures under %s", corpus).isTrue();
        assertThat(scheduled).as("jobs naming a JCL member").isPositive();
    }

    /**
     * The counts INTERLINKS section 11 arrived at by reading the fixture, asserted against what the
     * traits find. The panel is one job per member and has no table of its own, so it writes one
     * fewer job, one fewer condition and one fewer calendar reference than the export — everything
     * else has to match.
     */
    private static void assertFixture(List<ScheduledJob> exported, List<ScheduledJob> browsed,
                                      List<Calendar> defined) {
        assertThat(exported).as("24 jobs and the SMART table they run in").hasSize(25);
        assertThat(browsed).as("one panel per job").hasSize(24);

        assertThat(named(exported)).as("every job scheduled exactly once").isEqualTo(named(browsed));
        assertThat(named(exported)).hasSize(24);

        assertThat(inConditions(exported)).as("IN statements").isEqualTo(15);
        assertThat(inConditions(browsed)).isEqualTo(15);
        assertThat(outConditions(exported)).as("OUT statements, the table's included").isEqualTo(21);
        assertThat(outConditions(browsed)).isEqualTo(20);
        assertThat(conditionNames(exported)).as("condition names").hasSize(16);
        Set<String> onlyExported = new LinkedHashSet<>(conditionNames(exported));
        onlyExported.removeAll(conditionNames(browsed));
        assertThat(onlyExported).as("the table's own condition, which no panel writes")
          .containsExactly("CLAIMS_CLMNIGHT_OK");

        assertThat(Schedules.triggersAmong(exported)).as("predecessor to successor").hasSize(11);
        assertThat(Schedules.triggersAmong(browsed)).hasSize(11);
        assertThat(across(Schedules.triggersAmong(exported)))
          .as("chains that leave the table they start in").isEqualTo(2);

        assertThat(calendarReferences(exported)).as("jobs and the table naming a calendar").isEqualTo(10);
        assertThat(calendarReferences(browsed)).isEqualTo(9);

        assertThat(memberships(exported, true)).as("containment by a SMART table").isEqualTo(6);
        assertThat(memberships(exported, false)).as("every job names its table").isEqualTo(24);
        assertThat(memberships(browsed, false)).isEqualTo(24);

        assertThat(defined).as("CLMWORK and CLMWKEND").hasSize(2);
        for (Calendar calendar : defined) {
            assertThat(calendar.getYears()).containsExactly(2025, 2026);
            int days = "CLMWORK".equals(calendar.getName()) ? 307 : 102;
            assertThat(calendar.getDays(2025)).as("%s in 2025", calendar.getName()).hasSize(days);
            assertThat(calendar.getDays(2026)).as("%s in 2026", calendar.getName()).hasSize(days);
        }
    }

    /**
     * The one export in the corpus that a scheduler really wrote, and it is shaped nothing like the
     * fixture: no library on any job, no calendars but a wildcard rule-based one, and three tables
     * that each hold jobs of the same names. It is here because those are the differences a reader
     * written against the fixture alone would get wrong.
     */
    private static void assertCardDemo(List<ScheduledJob> exported) {
        assertThat(exported).as("15 jobs and the 2 SMART tables").hasSize(17);
        assertThat(scheduling(exported)).as("every job names a member").isEqualTo(15);
        assertThat(named(exported)).as("names repeat across tables").hasSize(9);

        assertThat(inConditions(exported)).isEqualTo(12);
        assertThat(outConditions(exported)).isEqualTo(23);
        assertThat(Schedules.triggersAmong(exported)).as("every condition waited on is added")
          .hasSize(12);

        // Every job says RULE_BASED_CALENDARS NAME="*", which names no calendar to point an edge at.
        assertThat(calendarReferences(exported)).isZero();

        assertThat(memberships(exported, true)).as("containment by a SMART table").isEqualTo(5);
        assertThat(memberships(exported, false)).isEqualTo(15);
    }

    private static boolean isExport(String source) {
        return source.trim().startsWith("<");
    }

    private static List<ScheduledJob> concat(List<ScheduledJob> first, List<ScheduledJob> second) {
        List<ScheduledJob> all = new ArrayList<>(first);
        all.addAll(second);
        return all;
    }

    private static int scheduling(List<ScheduledJob> jobs) {
        int count = 0;
        for (ScheduledJob job : jobs) {
            if (job.getMemberName() != null) {
                count++;
            }
        }
        return count;
    }

    private static Set<String> named(List<ScheduledJob> jobs) {
        Set<String> names = new LinkedHashSet<>();
        for (ScheduledJob job : jobs) {
            if (job.getMemberName() != null) {
                names.add(job.getMemberName());
            }
        }
        return names;
    }

    private static int inConditions(List<ScheduledJob> jobs) {
        int count = 0;
        for (ScheduledJob job : jobs) {
            count += job.getInConditions().size();
        }
        return count;
    }

    private static int outConditions(List<ScheduledJob> jobs) {
        int count = 0;
        for (ScheduledJob job : jobs) {
            count += job.getOutConditions().size();
        }
        return count;
    }

    private static Set<String> conditionNames(List<ScheduledJob> jobs) {
        Set<String> names = new LinkedHashSet<>();
        for (ScheduledJob job : jobs) {
            for (InCondition in : job.getInConditions()) {
                names.add(in.getName());
            }
            for (OutCondition out : job.getOutConditions()) {
                names.add(out.getName());
            }
        }
        return names;
    }

    private static int calendarReferences(List<ScheduledJob> jobs) {
        int count = 0;
        for (ScheduledJob job : jobs) {
            count += job.getCalendarReferences().size();
        }
        return count;
    }

    private static int memberships(List<ScheduledJob> jobs, boolean smartOnly) {
        int count = 0;
        for (ScheduledJob job : jobs) {
            GroupMembership membership = job.getGroupMembership();
            if (membership != null && (!smartOnly || membership.isSmartTable())) {
                count++;
            }
        }
        return count;
    }

    private static int across(List<Schedules.Trigger> triggers) {
        int count = 0;
        for (Schedules.Trigger trigger : triggers) {
            String from = trigger.getPredecessor().getGroup();
            if (from != null && !from.equals(trigger.getSuccessor().getGroup())) {
                count++;
            }
        }
        return count;
    }
}
