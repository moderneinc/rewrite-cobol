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
import org.openrewrite.Cursor;
import org.openrewrite.mainframe.jcl.tree.Jcl;
import org.openrewrite.test.RewriteTest;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.mainframe.jcl.Assertions.jcl;

class StepTest implements RewriteTest {

    @Test
    void readsStepsAndTheirDataDefinitions() {
        rewriteRun(
          jcl(
            """
              //ACCTJOB  JOB (ACCT),'DAILY POST',CLASS=A,MSGCLASS=X
              //STEP010  EXEC PGM=ACCTPOST,PARM='RUN'
              //ACCTDD   DD  DSN=PROD.ACCOUNT.MASTER,DISP=SHR
              //TRANDD   DD  DSN=PROD.TRAN.DAILY,DISP=(NEW,CATLG,DELETE)
              //SYSOUT   DD  SYSOUT=*
              //STEP020  EXEC PGM=ACCTRPT
              //RPTDD    DD  DSN=PROD.ACCOUNT.REPORT,DISP=(NEW,CATLG)
              """,
            spec -> spec.afterRecipe(cu -> {
                List<Step> steps = steps(cu);
                assertThat(steps).extracting(Step::getName).containsExactly("STEP010", "STEP020");
                assertThat(steps).extracting(Step::getProgram).containsExactly("ACCTPOST", "ACCTRPT");
                assertThat(steps.get(0).getProcedure()).isNull();

                Step step = steps.get(0);
                assertThat(step.getDataDefinitions()).extracting(DataDefinition::getName)
                  .containsExactly("ACCTDD", "TRANDD", "SYSOUT");
                // The DDs of the next step are its own.
                assertThat(steps.get(1).getDataDefinitions()).extracting(DataDefinition::getName)
                  .containsExactly("RPTDD");

                DataDefinition acct = step.getDataDefinition("ACCTDD");
                assertThat(acct).isNotNull();
                assertThat(acct.getDataSets()).singleElement().satisfies(ds -> {
                    assertThat(ds.getName()).isEqualTo("PROD.ACCOUNT.MASTER");
                    assertThat(ds.getDisposition().getStatus()).isEqualTo(Disposition.Status.SHR);
                    assertThat(ds.getDisposition().isInput()).isTrue();
                });

                DataDefinition tran = step.getDataDefinition("TRANDD");
                assertThat(tran.getDataSets()).singleElement().satisfies(ds -> {
                    assertThat(ds.getDisposition().getStatus()).isEqualTo(Disposition.Status.NEW);
                    assertThat(ds.getDisposition().getNormal()).isEqualTo("CATLG");
                    assertThat(ds.getDisposition().getAbnormal()).isEqualTo("DELETE");
                    assertThat(ds.getDisposition().isInput()).isFalse();
                });

                DataDefinition sysout = step.getDataDefinition("SYSOUT");
                assertThat(sysout.getSysout()).isEqualTo("*");
                assertThat(sysout.getDataSets()).isEmpty();
            })
          )
        );
    }

    /**
     * A DD with no name of its own concatenates onto the one before it, and the two are one DD as far
     * as the program reading them is concerned. Reported separately, a concatenation names data sets
     * that no DD claims — which is the shape every STEPLIB in a portfolio takes.
     */
    @Test
    void concatenatesUnnamedDataDefinitions() {
        rewriteRun(
          jcl(
            """
              //STEP010  EXEC PGM=IEBGENER
              //STEPLIB  DD  DSN=PROD.LOADLIB,DISP=SHR
              //         DD  DSN=SYS1.COB2LIB,DISP=SHR
              //         DD  DSN=CEE.SCEERUN,DISP=SHR
              //SYSIN    DD  DUMMY
              """,
            spec -> spec.afterRecipe(cu -> {
                Step step = steps(cu).get(0);
                assertThat(step.getDataDefinitions()).extracting(DataDefinition::getName)
                  .containsExactly("STEPLIB", "SYSIN");
                assertThat(step.getDataDefinition("STEPLIB").getDataSets()).extracting(DataSet::getName)
                  .containsExactly("PROD.LOADLIB", "SYS1.COB2LIB", "CEE.SCEERUN");
                assertThat(step.getDataDefinition("SYSIN").isDummy()).isTrue();
            })
          )
        );
    }

    /**
     * A member in parentheses is a library member, unless it is a relative generation, in which case
     * the same syntax means a generation data group. Reporting {@code MY.GDG(+1)} as a member of a
     * library would put a data set in the wrong place in every batch flow that uses one.
     */
    @Test
    void tellsAMemberFromAGeneration() {
        rewriteRun(
          jcl(
            """
              //STEP010  EXEC PGM=ACCTPOST
              //CARDS    DD  DSN=PROD.PARMLIB(ACCTCARD),DISP=SHR
              //NEWGEN   DD  DSN=PROD.ACCOUNT.HIST(+1),DISP=(NEW,CATLG)
              //TEMP     DD  DSN=&&WORK,DISP=(NEW,PASS)
              """,
            spec -> spec.afterRecipe(cu -> {
                Step step = steps(cu).get(0);

                DataSet cards = step.getDataDefinition("CARDS").getDataSets().get(0);
                assertThat(cards.getName()).isEqualTo("PROD.PARMLIB");
                assertThat(cards.getMember()).isEqualTo("ACCTCARD");
                assertThat(cards.isGenerationDataGroup()).isFalse();

                DataSet generation = step.getDataDefinition("NEWGEN").getDataSets().get(0);
                assertThat(generation.getName()).isEqualTo("PROD.ACCOUNT.HIST");
                assertThat(generation.isGenerationDataGroup()).isTrue();

                assertThat(step.getDataDefinition("TEMP").getDataSets().get(0).isTemporary()).isTrue();
            })
          )
        );
    }

    @Test
    void readsAProcedureStepEitherWayItIsWritten() {
        rewriteRun(
          jcl(
            """
              //STEP010  EXEC ACCTPROC
              //STEP020  EXEC PROC=RPTPROC,COND=(4,LT)
              """,
            spec -> spec.afterRecipe(cu -> {
                assertThat(steps(cu)).extracting(Step::getProcedure)
                  .containsExactly("ACCTPROC", "RPTPROC");
                assertThat(steps(cu)).extracting(Step::getProgram).containsOnlyNulls();
            })
          )
        );
    }

    /**
     * In-stream data belongs to the DD that introduced it, and that DD names no data set.
     */
    @Test
    void readsInStreamData() {
        rewriteRun(
          jcl(
            """
              //STEP010  EXEC PGM=SORT
              //SORTIN   DD  DSN=PROD.ACCOUNT.MASTER,DISP=SHR
              //SYSIN    DD  *
                SORT FIELDS=(1,11,CH,A)
              /*
              """,
            spec -> spec.afterRecipe(cu -> {
                Step step = steps(cu).get(0);
                DataDefinition sysin = step.getDataDefinition("SYSIN");
                assertThat(sysin.isInStream()).isTrue();
                assertThat(sysin.getDataSets()).isEmpty();
                assertThat(sysin.getInStreamData())
                  .extracting(d -> d.getWord().getText())
                  .containsExactly("SORT", "FIELDS=(1,11,CH,A)");
                assertThat(step.getDataDefinition("SORTIN").isInStream()).isFalse();
            })
          )
        );
    }

    /**
     * A DD found on its own has to be able to name its step, or nothing can say which program opens
     * it.
     */
    @Test
    void readsTheStepADataDefinitionBelongsTo() {
        rewriteRun(
          jcl(
            """
              //ACCTJOB  JOB (ACCT),'DAILY POST'
              //JOBLIB   DD  DSN=PROD.LOADLIB,DISP=SHR
              //STEP010  EXEC PGM=ACCTPOST
              //ACCTDD   DD  DSN=PROD.ACCOUNT.MASTER,DISP=SHR
              //STEP020  EXEC PGM=ACCTRPT
              //RPTDD    DD  DSN=PROD.ACCOUNT.REPORT,DISP=(NEW,CATLG)
              """,
            spec -> spec.afterRecipe(cu -> {
                List<DataDefinition> dds = new DataDefinition.Matcher().lower(cu).collect(Collectors.toList());
                assertThat(dds).extracting(DataDefinition::getName)
                  .containsExactly("JOBLIB", "ACCTDD", "RPTDD");
                // A JOBLIB is the job's, not any step's.
                assertThat(dds.get(0).getStep()).isNull();
                assertThat(dds.get(1).getStep().getName()).isEqualTo("STEP010");
                assertThat(dds.get(2).getStep().getName()).isEqualTo("STEP020");
            })
          )
        );
    }

    @Test
    void readsTheJobCard() {
        rewriteRun(
          jcl(
            """
              //ACCTJOB  JOB (ACCT),'DAILY POST',CLASS=A,MSGCLASS=X
              //STEP010  EXEC PGM=ACCTPOST
              """,
            spec -> spec.afterRecipe(cu ->
              assertThat(new Job.Matcher().lower(cu).collect(Collectors.toList()))
                .singleElement()
                .satisfies(job -> {
                    assertThat(job.getName()).isEqualTo("ACCTJOB");
                    assertThat(job.getParameter("CLASS")).isEqualTo("A");
                    assertThat(job.getParameter("MSGCLASS")).isEqualTo("X");
                }))
          )
        );
    }

    /**
     * A member with no JOB card — a procedure, or a fragment meant to be included — still has steps
     * worth reading.
     */
    @Test
    void readsAMemberWithNoJobCard() {
        rewriteRun(
          jcl(
            """
              //STEP010  EXEC PGM=ACCTPOST
              //ACCTDD   DD  DSN=PROD.ACCOUNT.MASTER,DISP=SHR
              """,
            spec -> spec.afterRecipe(cu -> {
                assertThat(new Job.Matcher().lower(cu)).isEmpty();
                assertThat(steps(cu)).singleElement()
                  .satisfies(step -> assertThat(step.getProgram()).isEqualTo("ACCTPOST"));
            })
          )
        );
    }

    /**
     * A utility reads its PARM by position, and a group in it is one position: Db2 High Performance
     * Unload is passed {@code ssid,uid} and options that carry commas of their own.
     */
    @Test
    void readsThePositionsOfAParm() {
        rewriteRun(
          jcl(
            """
              //UNLOAD   EXEC PGM=INZUTILB,PARM='DB2P,CLMU001,HIDDEN(USER,PSWD)'
              //SYSPRINT DD  SYSOUT=*
              """,
            spec -> spec.afterRecipe(cu -> {
                Step step = steps(cu).get(0);
                assertThat(step.getParm()).isEqualTo("DB2P,CLMU001,HIDDEN(USER,PSWD)");
                assertThat(step.getParmPositions())
                  .containsExactly("DB2P", "CLMU001", "HIDDEN(USER,PSWD)");
                assertThat(step.getParmPosition(0)).isEqualTo("DB2P");
                assertThat(step.getParmPosition(3)).isNull();
            })
          )
        );
    }

    @Test
    void readsNoParmFromAStepThatPassesNone() {
        rewriteRun(
          jcl(
            "//STEP010  EXEC PGM=ACCTPOST\n",
            spec -> spec.afterRecipe(cu -> {
                Step step = steps(cu).get(0);
                assertThat(step.getParm()).isNull();
                assertThat(step.getParmPositions()).isEmpty();
                assertThat(step.getParmPosition(0)).isNull();
            })
          )
        );
    }

    private static List<Step> steps(Jcl.CompilationUnit cu) {
        return new Step.Matcher().lower(cu).collect(Collectors.toList());
    }
}
