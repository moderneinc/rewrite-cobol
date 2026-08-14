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
package org.openrewrite.jcl.model;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.jcl.Assertions.jcl;

class JobStreamTest implements RewriteTest {

    @Test
    void readsJobStepsAndDataDefinitions() {
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
                JobStream job = JobStream.of(cu);
                assertThat(job.getJobName()).isEqualTo("ACCTJOB");
                assertThat(job.getJobParameters()).containsEntry("CLASS", "A").containsEntry("MSGCLASS", "X");

                assertThat(job.getSteps()).hasSize(2);

                Step step = job.getSteps().get(0);
                assertThat(step.getName()).isEqualTo("STEP010");
                assertThat(step.getProgram()).isEqualTo("ACCTPOST");
                assertThat(step.getProcedure()).isNull();
                assertThat(step.getParameters()).containsEntry("PARM", "'RUN'");
                assertThat(step.getDataDefinitions()).extracting(DataDefinition::getName)
                  .containsExactly("ACCTDD", "TRANDD", "SYSOUT");

                DataDefinition acct = step.dd("ACCTDD");
                assertThat(acct).isNotNull();
                assertThat(acct.getDataSets()).singleElement().satisfies(ds -> {
                    assertThat(ds.getName()).isEqualTo("PROD.ACCOUNT.MASTER");
                    assertThat(ds.getDisposition()).isNotNull();
                    assertThat(ds.getDisposition().getStatus()).isEqualTo(Disposition.Status.SHR);
                    assertThat(ds.getDisposition().isInput()).isTrue();
                });

                DataDefinition tran = step.dd("TRANDD");
                assertThat(tran).isNotNull();
                assertThat(tran.getDataSets()).singleElement().satisfies(ds -> {
                    assertThat(ds.getDisposition().getStatus()).isEqualTo(Disposition.Status.NEW);
                    assertThat(ds.getDisposition().getNormal()).isEqualTo("CATLG");
                    assertThat(ds.getDisposition().getAbnormal()).isEqualTo("DELETE");
                    assertThat(ds.getDisposition().isInput()).isFalse();
                });

                DataDefinition sysout = step.dd("SYSOUT");
                assertThat(sysout).isNotNull();
                assertThat(sysout.getSysout()).isEqualTo("*");
                assertThat(sysout.getDataSets()).isEmpty();

                assertThat(job.getSteps().get(1).getName()).isEqualTo("STEP020");
                assertThat(job.getSteps().get(1).getProgram()).isEqualTo("ACCTRPT");
            })
          )
        );
    }

    /**
     * A DD with no name of its own concatenates onto the one before it. Read as separate statements,
     * a concatenation reports data sets that nothing names — which is the shape every SYSLIB and
     * STEPLIB in a portfolio takes.
     */
    @Test
    void concatenatesUnnamedDataDefinitions() {
        rewriteRun(
          jcl(
            """
              //LIBJOB   JOB (ACCT),'LIBS'
              //STEP010  EXEC PGM=IEBGENER
              //STEPLIB  DD  DSN=PROD.LOADLIB,DISP=SHR
              //         DD  DSN=SYS1.COB2LIB,DISP=SHR
              //         DD  DSN=CEE.SCEERUN,DISP=SHR
              //SYSIN    DD  DUMMY
              """,
            spec -> spec.afterRecipe(cu -> {
                Step step = JobStream.of(cu).getSteps().get(0);
                assertThat(step.getDataDefinitions()).extracting(DataDefinition::getName)
                  .containsExactly("STEPLIB", "SYSIN");

                assertThat(step.dd("STEPLIB")).isNotNull();
                assertThat(step.dd("STEPLIB").getDataSets()).extracting(DataSet::getName)
                  .containsExactly("PROD.LOADLIB", "SYS1.COB2LIB", "CEE.SCEERUN");

                assertThat(step.dd("SYSIN")).isNotNull();
                assertThat(step.dd("SYSIN").isDummy()).isTrue();
            })
          )
        );
    }

    /**
     * Operands spread over continuation lines are one parameter list, and the comment field after
     * the operand on each line is not part of it.
     */
    @Test
    void joinsContinuationLines() {
        rewriteRun(
          jcl(
            """
              //CONTJOB  JOB (ACCT),'CONT'
              //STEP010  EXEC PGM=ACCTPOST
              //ACCTDD   DD  DSN=PROD.ACCOUNT.MASTER,           THE MASTER
              //             DISP=SHR,
              //             AMP=('BUFND=10,BUFNI=5')
              """,
            spec -> spec.afterRecipe(cu -> {
                DataDefinition dd = JobStream.of(cu).getSteps().get(0).dd("ACCTDD");
                assertThat(dd).isNotNull();
                assertThat(dd.getDataSets()).singleElement().satisfies(ds -> {
                    assertThat(ds.getName()).isEqualTo("PROD.ACCOUNT.MASTER");
                    assertThat(ds.getDisposition().getStatus()).isEqualTo(Disposition.Status.SHR);
                });
                // The commas inside AMP belong to its sub-parameters, not to the DD's parameter list.
                assertThat(dd.getParameters()).containsEntry("AMP", "('BUFND=10,BUFNI=5')");
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
              //GDGJOB   JOB (ACCT),'GDG'
              //STEP010  EXEC PGM=ACCTPOST
              //CARDS    DD  DSN=PROD.PARMLIB(ACCTCARD),DISP=SHR
              //NEWGEN   DD  DSN=PROD.ACCOUNT.HIST(+1),DISP=(NEW,CATLG)
              //TEMP     DD  DSN=&&WORK,DISP=(NEW,PASS)
              """,
            spec -> spec.afterRecipe(cu -> {
                Step step = JobStream.of(cu).getSteps().get(0);

                DataSet cards = step.dd("CARDS").getDataSets().get(0);
                assertThat(cards.getName()).isEqualTo("PROD.PARMLIB");
                assertThat(cards.getMember()).isEqualTo("ACCTCARD");
                assertThat(cards.isGenerationDataGroup()).isFalse();

                DataSet generation = step.dd("NEWGEN").getDataSets().get(0);
                assertThat(generation.getName()).isEqualTo("PROD.ACCOUNT.HIST");
                assertThat(generation.isGenerationDataGroup()).isTrue();

                DataSet temporary = step.dd("TEMP").getDataSets().get(0);
                assertThat(temporary.isTemporary()).isTrue();
            })
          )
        );
    }

    /**
     * {@code EXEC MYPROC} and {@code EXEC PROC=MYPROC} say the same thing, and a step that runs a
     * procedure runs no program of its own.
     */
    @Test
    void readsAProcedureStepEitherWayItIsWritten() {
        rewriteRun(
          jcl(
            """
              //PROCJOB  JOB (ACCT),'PROCS'
              //STEP010  EXEC ACCTPROC
              //STEP020  EXEC PROC=RPTPROC,COND=(4,LT)
              """,
            spec -> spec.afterRecipe(cu -> {
                JobStream job = JobStream.of(cu);
                assertThat(job.getSteps()).extracting(Step::getProcedure)
                  .containsExactly("ACCTPROC", "RPTPROC");
                assertThat(job.getSteps()).extracting(Step::getProgram).containsOnlyNulls();
                assertThat(job.getSteps().get(1).getParameters()).containsEntry("COND", "(4,LT)");
            })
          )
        );
    }

    /**
     * In-stream data is how SYSIN control cards arrive, and a DD carrying them names no data set.
     */
    @Test
    void marksInStreamData() {
        rewriteRun(
          jcl(
            """
              //SORTJOB  JOB (ACCT),'SORT'
              //STEP010  EXEC PGM=SORT
              //SORTIN   DD  DSN=PROD.ACCOUNT.MASTER,DISP=SHR
              //SYSIN    DD  *
                SORT FIELDS=(1,11,CH,A)
              /*
              """,
            spec -> spec.afterRecipe(cu -> {
                Step step = JobStream.of(cu).getSteps().get(0);
                assertThat(step.dd("SYSIN")).isNotNull();
                assertThat(step.dd("SYSIN").isInStream()).isTrue();
                assertThat(step.dd("SYSIN").getDataSets()).isEmpty();
                assertThat(step.dd("SORTIN").isInStream()).isFalse();
            })
          )
        );
    }

    /**
     * A member with no JOB card — a procedure, or a fragment meant to be included — still has steps
     * worth reading, and a model that required a JOB card would report nothing for it.
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
                JobStream job = JobStream.of(cu);
                assertThat(job.getJobName()).isEmpty();
                assertThat(job.getSteps()).singleElement()
                  .satisfies(step -> assertThat(step.getProgram()).isEqualTo("ACCTPOST"));
            })
          )
        );
    }
}
