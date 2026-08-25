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
package org.openrewrite.controlm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.SourceFile;
import org.openrewrite.controlm.tree.ControlM;
import org.openrewrite.controlm.trait.ScheduledJob;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A whole portfolio's schedule arrives as one file — Desjardins' is 2.39M lines — so this reads a
 * synthetic export of that shape and reports what it cost. Gated on {@code CONTROLM_SCALE} because
 * the file is built in memory and the run wants a heap the ordinary tests do not.
 * <p>
 * Measured on an Apple M-series laptop, JDK 21: 200,000 jobs, 2,266,671 lines, 281MB of text read in
 * 3.6s and printed back identically in 1.2s, with the traits reading every job in a further 0.9s.
 * The tree costs 3.8GB, nearly all of it the per-node ids and the values themselves, so an export
 * this size is a memory question rather than a time one.
 */
@EnabledIfEnvironmentVariable(named = "CONTROLM_SCALE", matches = ".+")
class ExportScaleTest {

    private static final int JOBS_PER_FOLDER = 6;

    /**
     * Ten lines each, so this is the 2.3M-line file a portfolio of this size exports as.
     */
    private static final int JOBS = 200_000;

    @Test
    void readsAWholePortfolioAtOnce() {
        int jobs = JOBS;
        String export = export(jobs);
        int lines = export.split("\n", -1).length - 1;
        System.out.printf("export: %,d lines, %,d MB, %,d jobs%n",
          lines, export.length() / (1024 * 1024), jobs);

        long start = System.nanoTime();
        List<SourceFile> parsed = ControlMParser.builder().build()
          .parse(new InMemoryExecutionContext(), export)
          .collect(Collectors.toList());
        long read = System.nanoTime() - start;
        assertThat(parsed).singleElement().isInstanceOf(ControlM.CompilationUnit.class);
        ControlM.CompilationUnit cu = (ControlM.CompilationUnit) parsed.get(0);

        start = System.nanoTime();
        String printed = cu.printAll();
        long print = System.nanoTime() - start;

        start = System.nanoTime();
        long found = new ScheduledJob.Matcher().lower(cu).count();
        long traits = System.nanoTime() - start;

        System.gc();
        Runtime runtime = Runtime.getRuntime();
        long held = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        System.out.printf("read %.1fs, printed %.1fs, %,d jobs read by the traits in %.1fs, " +
                          "%,d MB held (the export text included)%n",
          read / 1e9, print / 1e9, found, traits / 1e9, held);

        assertThat(printed).isEqualTo(export);
        assertThat(found).isEqualTo(jobs);
    }

    /**
     * A job of the shape the fixture's export writes: one line of forty-odd attributes and nine lines
     * of conditions, resources and what to do when it fails. Every sixth job starts a new table, and
     * each waits on the one before it, so the trigger chains are as long as a real stream's.
     */
    private static String export(int jobs) {
        StringBuilder out = new StringBuilder(jobs * 1600);
        out.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
        out.append("<DEFTABLE xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                   "xsi:noNamespaceSchemaLocation=\"Folder.xsd\">\n");
        for (int job = 1; job <= jobs; job++) {
            int folder = (job - 1) / JOBS_PER_FOLDER + 1;
            int within = (job - 1) % JOBS_PER_FOLDER + 1;
            if (within == 1) {
                if (folder > 1) {
                    out.append("\t</FOLDER>\n");
                }
                out.append("\t<FOLDER FOLDER_NAME=\"CLM").append(folder)
                  .append("\" DATACENTER=\"CTMPROD\" FOLDER_DSN=\"CLM.PROD.SCHEDULE\" " +
                          "FOLDER_ORDER_METHOD=\"SYSTEM\" MODIFIED=\"False\" LAST_UPLOAD=\"20260615\" " +
                          "REAL_FOLDER_ID=\"").append(folder).append("\" TYPE=\"1\">\n");
            }
            String name = "CLMJ" + job;
            out.append("\t\t<JOB JOBISN=\"").append(within)
              .append("\" APPLICATION=\"CLAIMS\" SUB_APPLICATION=\"CLM").append(folder)
              .append("\" JOBNAME=\"").append(name).append("\" MEMNAME=\"").append(name)
              .append("\" MEMLIB=\"CLM.PROD.JCL\" DESCRIPTION=\"DAILY CLAIM EXTRACT\" " +
                      "CREATED_BY=\"T032417\" RUN_AS=\"CLMPROD\" CRITICAL=\"0\" TASKTYPE=\"Job\" " +
                      "CYCLIC=\"0\" CONFIRM=\"0\" RETRO=\"0\" MAXWAIT=\"0\" MAXRERUN=\"0\" " +
                      "AUTOARCH=\"1\" MAXDAYS=\"0\" MAXRUNS=\"0\" TIMEFROM=\"2200\" DAYS=\"ALL\" " +
                      "DAYSCAL=\"CLMWORK\" JAN=\"1\" FEB=\"1\" MAR=\"1\" APR=\"1\" MAY=\"1\" " +
                      "JUN=\"1\" JUL=\"1\" AUG=\"1\" SEP=\"1\" OCT=\"1\" NOV=\"1\" DEC=\"1\" " +
                      "DAYS_AND_OR=\"O\" SYSDB=\"1\" PREVENT_NCT2=\"Y\" DOCMEM=\"").append(name)
              .append("\" DOCLIB=\"CLM.PROD.DOC\" CREATION_USER=\"T032417\" " +
                      "CREATION_DATE=\"19970414\" CREATION_TIME=\"093012\" " +
                      "CHANGE_USERID=\"T118203\" CHANGE_DATE=\"20240305\" CHANGE_TIME=\"141522\" " +
                      "JOB_VERSION=\"01.00\" RULE_BASED_CALENDAR_RELATIONSHIP=\"O\" " +
                      "APPL_TYPE=\"OS\" USE_INSTREAM_JCL=\"N\" IS_CURRENT_VERSION=\"Y\" " +
                      "VERSION_SERIAL=\"1\" PARENT_FOLDER=\"CLM").append(folder).append("\">\n");
            if (within > 1) {
                out.append("\t\t\t<INCOND NAME=\"CLM").append(folder).append("_CLMJ").append(job - 1)
                  .append("_OK\" ODATE=\"ODAT\" AND_OR=\"A\" />\n");
                out.append("\t\t\t<OUTCOND NAME=\"CLM").append(folder).append("_CLMJ").append(job - 1)
                  .append("_OK\" ODATE=\"ODAT\" SIGN=\"-\" />\n");
            } else {
                out.append("\t\t\t<INCOND NAME=\"CLMMAST-CLOSED\" ODATE=\"ODAT\" AND_OR=\"A\" />\n");
                out.append("\t\t\t<CONTROL NAME=\"CLMMAST\" TYPE=\"S\" />\n");
            }
            out.append("\t\t\t<OUTCOND NAME=\"CLM").append(folder).append("_").append(name)
              .append("_OK\" ODATE=\"ODAT\" SIGN=\"+\" />\n");
            out.append("\t\t\t<QUANTITATIVE NAME=\"DB2P\" QUANT=\"0001\" ONFAIL=\"R\" ONOK=\"R\" />\n");
            out.append("\t\t\t<ON PGMST=\"ANYSTEP\" PROCST=\"\" CODES=\"NOTOK\" AND_OR=\"O\">\n");
            out.append("\t\t\t\t<DOIFRERUN FROM=\"$EXERR\" CONFIRM=\"N\" />\n");
            out.append("\t\t\t\t<DOSHOUT URGENCY=\"R\" DEST=\"OPER2\" " +
                       "MESSAGE=\"%%JOBNAME ENDED NOTOK - RESTART FROM $EXERR\" />\n");
            out.append("\t\t\t</ON>\n");
            out.append("\t\t\t<SHOUT WHEN=\"NOTOK\" TIME=\"\" URGENCY=\"R\" DEST=\"OPER2\" " +
                       "MESSAGE=\"CLAIMS %%JOBNAME ENDED NOTOK - CALL CLAIMS SUPPORT\" />\n");
            out.append("\t\t</JOB>\n");
        }
        out.append("\t</FOLDER>\n");
        out.append("</DEFTABLE>\n");
        return out.toString();
    }
}
