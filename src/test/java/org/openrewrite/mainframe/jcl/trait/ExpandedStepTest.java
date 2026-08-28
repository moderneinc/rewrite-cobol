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
package org.openrewrite.mainframe.jcl.trait;

import org.junit.jupiter.api.Test;
import org.openrewrite.mainframe.jcl.marker.Symbolic;
import org.openrewrite.mainframe.jcl.tree.Jcl;
import org.openrewrite.test.RewriteTest;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.mainframe.jcl.tree.ParserAssertions.jclWithProcedures;
import static org.openrewrite.mainframe.jcl.tree.ParserAssertions.procedureMember;

/**
 * What a step answers once the procedure it calls has been resolved: the program it really runs,
 * the data sets it really opens, and where each symbol's value came from.
 */
class ExpandedStepTest implements RewriteTest {

    private static final List<Path> LIBRARY = List.of(
      procedureMember("@JOBCARD",
        """
          //         SET ENV=PROD
          //         SET HLQ=CLM.&ENV
          """),
      procedureMember("CLMBATCH",
        """
          //CLMBATCH PROC PGM=,HLQ=CLM.PROD,REGION=4M
          //RUN      EXEC PGM=&PGM,REGION=&REGION
          //STEPLIB  DD DISP=SHR,DSN=&HLQ..LOADLIB
          //SYSOUT   DD SYSOUT=*
          //SYSTSIN  DD DUMMY
          //         PEND
          """),
      procedureMember("CLMCLB",
        """
          //CLMCLB   PROC MEM=,HLQ=CLM.PROD
          //COB      EXEC PGM=IGYCRCTL
          //SYSIN    DD DISP=SHR,DSN=&HLQ..COBOL(&MEM)
          //LKED     EXEC PGM=IEWL
          //SYSLMOD  DD DISP=SHR,DSN=&HLQ..LOADLIB(&MEM)
          //SYSIN    DD DUMMY
          //         PEND
          """));

    private static List<Step> steps(Jcl.CompilationUnit cu) {
        return new Step.Matcher().lower(cu).collect(Collectors.toList());
    }

    private static final String EXTRACT = """
      //CLMJ010  JOB (CLM),'CLAIMS EXTRACT',CLASS=P
      //PROCLIB  JCLLIB ORDER=(CLM.PROD.PROCLIB)
      //         INCLUDE MEMBER=@JOBCARD
      //EXTRACT  EXEC CLMBATCH,PGM=CLMB010,HLQ=&HLQ
      //RUN.CLMMAST  DD DISP=SHR,DSN=&HLQ..CLMMAST
      """;

    @Test
    void aStepRunsTheProceduresSteps() {
        rewriteRun(
          jclWithProcedures(EXTRACT, LIBRARY,
            spec -> spec.afterRecipe(cu -> {
                Step extract = steps(cu).get(0);
                assertThat(extract.getName()).isEqualTo("EXTRACT");
                assertThat(extract.getProcedure()).isEqualTo("CLMBATCH");
                assertThat(extract.getProgram()).isNull();

                assertThat(extract.getProcedureSteps()).singleElement().satisfies(run -> {
                    assertThat(run.getName()).isEqualTo("RUN");
                    assertThat(run.getQualifiedName()).isEqualTo("EXTRACT.RUN");
                    assertThat(run.getProgram()).isEqualTo("CLMB010");
                    assertThat(run.getCallingStep()).isNotNull();
                    assertThat(run.getCallingStep().getName()).isEqualTo("EXTRACT");
                });
            })
          )
        );
    }

    @Test
    void aProcedureStepOpensTheDataSetsTheJobNamed() {
        rewriteRun(
          jclWithProcedures(EXTRACT, LIBRARY,
            spec -> spec.afterRecipe(cu -> {
                Step run = steps(cu).get(0).getProcedureSteps().get(0);
                assertThat(run.getDataDefinitions()).extracting(DataDefinition::getName)
                  .containsExactly("STEPLIB", "SYSOUT", "SYSTSIN", "CLMMAST");
                assertThat(run.getDataDefinition("STEPLIB").getDataSets())
                  .extracting(DataSet::getName).containsExactly("CLM.PROD.LOADLIB");
                assertThat(run.getDataDefinition("CLMMAST").getDataSets())
                  .extracting(DataSet::getName).containsExactly("CLM.PROD.CLMMAST");
            })
          )
        );
    }

    @Test
    void everySymbolSaysWhereItsValueCameFrom() {
        rewriteRun(
          jclWithProcedures(EXTRACT, LIBRARY,
            spec -> spec.afterRecipe(cu -> {
                Step run = steps(cu).get(0).getProcedureSteps().get(0);
                assertThat(run.getSymbolic("ENV")).isNotNull()
                  .satisfies(env -> {
                      assertThat(env.getValue()).isEqualTo("PROD");
                      assertThat(env.getOrigin()).isEqualTo(Symbolic.Origin.SET);
                  });
                assertThat(run.getSymbolic("PGM")).isNotNull()
                  .satisfies(pgm -> {
                      assertThat(pgm.getValue()).isEqualTo("CLMB010");
                      assertThat(pgm.getOrigin()).isEqualTo(Symbolic.Origin.OVERRIDE);
                  });
                assertThat(run.getSymbolic("REGION")).isNotNull()
                  .satisfies(region -> {
                      assertThat(region.getValue()).isEqualTo("4M");
                      assertThat(region.getOrigin()).isEqualTo(Symbolic.Origin.PROCEDURE);
                  });
                // The job passed HLQ=&HLQ, so the override carries the SET value into the procedure.
                assertThat(run.getSymbolic("HLQ")).isNotNull()
                  .satisfies(hlq -> {
                      assertThat(hlq.getValue()).isEqualTo("CLM.PROD");
                      assertThat(hlq.getOrigin()).isEqualTo(Symbolic.Origin.OVERRIDE);
                  });
            })
          )
        );
    }

    /**
     * A step counted through the traits is a step the job runs, so a two-step procedure called once
     * is two steps and the calling EXEC is not a third.
     */
    @Test
    void everyStepOfAMultiStepProcedureIsFound() {
        rewriteRun(
          jclWithProcedures(
            """
              //CLMCMPB  JOB (CLM),'COMPILE'
              //CMPB010  EXEC CLMCLB,MEM=CLMB010
              //LKED.SYSIN  DD DISP=SHR,DSN=CLM.PROD.LINKLIB(CLMB010)
              """,
            LIBRARY,
            spec -> spec.afterRecipe(cu -> {
                Step compile = steps(cu).get(0);
                assertThat(compile.getProcedureSteps()).extracting(Step::getQualifiedName)
                  .containsExactly("CMPB010.COB", "CMPB010.LKED");
                Step cob = compile.getProcedureSteps().get(0);
                assertThat(cob.getProgram()).isEqualTo("IGYCRCTL");
                assertThat(cob.getDataDefinition("SYSIN").getDataSets())
                  .extracting(DataSet::getMember).containsExactly("CLMB010");
                // The override names the LKED step, so only that step's SYSIN is replaced.
                Step lked = compile.getProcedureSteps().get(1);
                assertThat(lked.getDataDefinition("SYSIN").getDataSets())
                  .extracting(DataSet::getMember).containsExactly("CLMB010");
                assertThat(lked.getDataDefinition("SYSLMOD").getDataSets())
                  .extracting(DataSet::getName).containsExactly("CLM.PROD.LOADLIB");
            })
          )
        );
    }

    @Test
    void readsTheProcedureLibrariesAJobExpects() {
        rewriteRun(
          jclWithProcedures(EXTRACT, LIBRARY,
            spec -> spec.afterRecipe(cu ->
              assertThat(new Job.Matcher().lower(cu).collect(Collectors.toList()))
                .singleElement()
                .satisfies(job -> assertThat(job.getProcedureLibraries())
                  .containsExactly("CLM.PROD.PROCLIB")))
          )
        );
    }

    @Test
    void saysWhenTheProcedureIsNotInTheLibrary() {
        rewriteRun(
          jclWithProcedures(
            "//CMPI060  EXEC IMSCOBOL,MBR=CLMI060\n",
            singletonList(LIBRARY.get(0)),
            spec -> spec.afterRecipe(cu -> {
                Step step = steps(cu).get(0);
                assertThat(step.getProcedure()).isEqualTo("IMSCOBOL");
                assertThat(step.isProcedureMissing()).isTrue();
                assertThat(step.getProcedureSteps()).isEmpty();
            })
          )
        );
    }
}
