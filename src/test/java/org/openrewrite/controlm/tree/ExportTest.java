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
package org.openrewrite.controlm.tree;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.controlm.tree.ParserAssertions.controlM;

/**
 * The XML dialect: what {@code exportdeftable} and {@code exportdefcal} write, told apart from the
 * z/OS panel by the file's root element rather than by its name.
 */
class ExportTest implements RewriteTest {

    @Test
    void deftable() {
        rewriteRun(
          controlM(
            """
              <?xml version="1.0" encoding="ISO-8859-1"?>
              <DEFTABLE>
                <SMART_FOLDER FOLDER_NAME="CLMNIGHT" TASKTYPE="SMART Table" JOBNAME="CLMNIGHT" DAYSCAL="CLMWORK">
                  <JOB JOBISN="1" JOBNAME="CLMJ010" MEMNAME="CLMJ010" MEMLIB="CLM.PROD.JCL" TASKTYPE="Job">
                    <INCOND NAME="CLMMAST-CLOSED" ODATE="ODAT" AND_OR="A" />
                    <OUTCOND NAME="CLMNIGHT_CLMJ010_OK" ODATE="ODAT" SIGN="+" />
                  </JOB>
                  <OUTCOND NAME="CLAIMS_CLMNIGHT_OK" ODATE="ODAT" SIGN="+" />
                </SMART_FOLDER>
              </DEFTABLE>
              """,
            spec -> spec.afterRecipe(cu -> {
                assertThat(cu.getSections()).hasSize(2);
                assertThat(cu.getSections().get(0)).isInstanceOf(ControlM.Directive.class);

                ControlM.Element table = (ControlM.Element) cu.getSections().get(1);
                assertThat(table.getName()).isEqualTo("DEFTABLE");
                ControlM.Element folder = table.getElements("SMART_FOLDER").get(0);
                assertThat(folder.getAttributeText("TASKTYPE")).isEqualTo("SMART Table");
                assertThat(folder.getElements("JOB")).hasSize(1);
                assertThat(folder.getElements("OUTCOND")).hasSize(1);
                assertThat(folder.getElements("JOB").get(0).getAttributeText("MEMNAME")).isEqualTo("CLMJ010");
            })
          )
        );
    }

    /**
     * A job written with nothing under it is not the same as one written closed, and the two print
     * differently.
     */
    @Test
    void emptyBodyIsNotAClosedTag() {
        rewriteRun(
          controlM(
            """
              <DEFTABLE>
                <FOLDER FOLDER_NAME="CLMONREQ">
                  <JOB JOBNAME="CLMCMPI" MEMNAME="CLMCMPI">
                  </JOB>
                  <JOB JOBNAME="CLMJ002" MEMNAME="CLMJ002" />
                </FOLDER>
              </DEFTABLE>
              """,
            spec -> spec.afterRecipe(cu -> {
                ControlM.Element folder = ((ControlM.Element) cu.getSections().get(0)).getElements("FOLDER").get(0);
                assertThat(folder.getElements("JOB").get(0).getElements()).isEmpty();
                assertThat(folder.getElements("JOB").get(1).getElements()).isNull();
            })
          )
        );
    }

    @Test
    void doctypeAndComments() {
        rewriteRun(
          controlM(
            """
              <?xml version="1.0" encoding="ISO-8859-1"?>
              <!DOCTYPE DEFCAL SYSTEM "defcal.dtd">
              <DEFCAL>
                <!-- the working days the claims stream runs on -->
                <CALENDAR DATACENTER="CTMPROD" NAME="CLMWORK" TYPE="Regular">
                  <YEAR NAME="2026" DAYS="NYY" />
                </CALENDAR>
              </DEFCAL>
              """,
            spec -> spec.afterRecipe(cu -> {
                assertThat(cu.getSections()).hasSize(3);
                ControlM.Element calendars = (ControlM.Element) cu.getSections().get(2);
                assertThat(calendars.getElements()).hasSize(2);
                assertThat(calendars.getElements().get(0)).isInstanceOf(ControlM.Directive.class);
                assertThat(calendars.getElements("CALENDAR")).hasSize(1);
            })
          )
        );
    }

    /**
     * A value is kept as written so the file prints back; what it says is what the entities resolve
     * to.
     */
    @Test
    void entitiesInAValue() {
        rewriteRun(
          controlM(
            """
              <DEFTABLE>
                <JOB DESCRIPTION="RESERVE &amp; PAYMENT" />
              </DEFTABLE>
              """,
            spec -> spec.afterRecipe(cu -> {
                ControlM.Element job = ((ControlM.Element) cu.getSections().get(0)).getElements("JOB").get(0);
                assertThat(job.getAttribute("DESCRIPTION").getValue().getText())
                  .isEqualTo("\"RESERVE &amp; PAYMENT\"");
                assertThat(job.getAttributeText("DESCRIPTION")).isEqualTo("RESERVE & PAYMENT");
            })
          )
        );
    }

    @Test
    void singleQuotedValues() {
        rewriteRun(
          controlM(
            """
              <DEFTABLE>
                <JOB MEMNAME='CLMJ010' MEMLIB = "CLM.PROD.JCL" />
              </DEFTABLE>
              """,
            spec -> spec.afterRecipe(cu -> {
                ControlM.Element job = ((ControlM.Element) cu.getSections().get(0)).getElements("JOB").get(0);
                assertThat(job.getAttributeText("MEMNAME")).isEqualTo("CLMJ010");
                assertThat(job.getAttributeText("MEMLIB")).isEqualTo("CLM.PROD.JCL");
            })
          )
        );
    }
}
