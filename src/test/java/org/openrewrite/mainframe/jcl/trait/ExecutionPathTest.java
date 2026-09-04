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
import org.openrewrite.mainframe.jcl.tree.Jcl;
import org.openrewrite.test.RewriteTest;

import java.nio.file.Path;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.mainframe.jcl.tree.ParserAssertions.jcl;
import static org.openrewrite.mainframe.jcl.tree.ParserAssertions.jclWithProcedures;
import static org.openrewrite.mainframe.jcl.tree.ParserAssertions.parmMember;
import static org.openrewrite.mainframe.jcl.tree.ParserAssertions.procedureMember;

/**
 * The five fixture unload jobs are the cases here, written down to the lines each one turns on:
 * a deck already in the base utility's dialect, a deck of the unload product's, a deck reached
 * through a procedure symbolic, a member nobody supplied and a procedure nobody supplied.
 */
class ExecutionPathTest implements RewriteTest {

    /**
     * A member of the procedure library has to be supplied for symbols to be filled in at all, and
     * these jobs call no procedure. This one stands for the library.
     */
    private static final List<Path> LIBRARY = List.of(procedureMember("@JOBCARD",
      """
        //*  THE JOB CARD EVERY JOB INCLUDES
        """));

    @Test
    void readsAStepWhoseEveryNameResolves() {
        rewriteRun(
          jcl(
            """
              //CLMUNLB  JOB (CLM,PROD),'UNLOAD POLICY BASE',CLASS=P
              //UNLPOL   EXEC PGM=DSNUTILB,PARM='DB2P,CLMUNLB.UNLPOL'
              //SYSREC   DD DSN=CLM.PROD.UNLOAD.POLICY.BASE,DISP=(NEW,CATLG)
              //SYSIN    DD *
                UNLOAD TABLESPACE CLMDB01.CLMTSPOL
                  FROM TABLE CLM.POLICY
                  UNLDDN SYSREC
              /*
              """,
            spec -> spec.afterRecipe(cu -> {
                ExecutionPath path = path(cu);
                // The base utility's defaults are published and the same everywhere, so a deck that
                // codes none of them still says everything it does.
                assertThat(path.getGaps()).isEmpty();
                assertThat(path.getVerdict()).isEqualTo(ExecutionPath.Verdict.RESOLVED);
                assertThat(path.isResolved()).isTrue();
            })
          )
        );
    }

    /**
     * Every name in this job resolves and it still does not say what it writes: {@code FORMAT},
     * {@code DB2}, {@code LOCK} and {@code QUIESCE} come from the parmlib member on {@code INFPLIB},
     * which is in no application library. Change the parmlib and the job writes a different file
     * without a line of it changing.
     */
    @Test
    void saysWhichKeywordsTheDeckLeavesToTheSite() {
        rewriteRun(
          jcl(
            """
              //CLMUNLD  JOB (CLM,PROD),'UNLOAD POLICY DEFAULTS',CLASS=P
              //UNLPOL   EXEC PGM=INZUTILB,PARM='DB2P,CLMUNLD'
              //INFPLIB  DD DISP=SHR,DSN=DB2UNL.PARMLIB
              //POLDFT   DD DSN=CLM.PROD.UNLOAD.POLICY.PREPROD,DISP=(NEW,CATLG)
              //SYSIN    DD *
                UNLOAD TABLESPACE CLMDB01.CLMTSPOL
                  SELECT * FROM CLM.POLICY
                  OUTDDN (POLDFT)
              /*
              """,
            spec -> spec.afterRecipe(cu -> {
                ExecutionPath path = path(cu);
                assertThat(path.getVerdict()).isEqualTo(ExecutionPath.Verdict.INHERITED);
                // The path resolves; only what the step does is undecided.
                assertThat(path.isResolved()).isTrue();
                assertThat(path.getGaps()).extracting(Gap::getKind)
                  .containsOnly(Gap.Kind.KEYWORD_INHERITED);
                assertThat(path.getGaps()).extracting(Gap::getName)
                  .contains("FORMAT", "DB2", "LOCK", "QUIESCE");
                assertThat(path.getGaps()).extracting(Gap::getDdName).containsOnly("SYSIN");
                assertThat(path.getGaps()).extracting(Gap::getStep).containsOnly("UNLPOL");
            })
          )
        );
    }

    /**
     * A deck that codes what it writes and how it reads leaves nothing to the site, and a step whose
     * names all resolve too is one a transformation may be applied to. This is the deck that says so
     * and the one above is the deck that does not, and the difference between them is four keywords.
     */
    @Test
    void aCodedKeywordIsNotInherited() {
        rewriteRun(
          jcl(
            """
              //CLMUNLP  JOB (CLM,PROD),'UNLOAD CLM.POLICY',CLASS=P
              //UNLPOL   EXEC PGM=INZUTILB,PARM='DB2P,CLMUNLP'
              //POLUNL   DD DSN=CLM.PROD.UNLOAD.POLICY,DISP=(NEW,CATLG)
              //SYSIN    DD *
                GLOBAL
                       SQLID CLM ;
                UNLOAD TABLESPACE CLMDB01.CLMTSPOL
                       DB2 NO
                       LOCK YES
                       QUIESCE YES
                  SELECT * FROM CLM.POLICY
                  OUTDDN (POLUNL)
                  FORMAT DSNTIAUL
              /*
              """,
            spec -> spec.afterRecipe(cu -> {
                ExecutionPath path = path(cu);
                assertThat(path.getVerdict()).isEqualTo(ExecutionPath.Verdict.RESOLVED);
                assertThat(path.getGaps()).isEmpty();
            })
          )
        );
    }

    /**
     * Two substitution systems are written the same way in one deck and only one of them is the
     * job's. {@code SYMBOLS=JCLONLY} has JES put {@code &HLQ} into the card text before the step
     * runs; {@code &DB.} and {@code &TS.} are the unload product's own template variables, are still
     * there when it reads the deck, and no {@code SET} was ever going to define them. Counting those
     * as symbols nothing set makes every job that uses a template unresolvable.
     */
    @Test
    void aTemplateVariableIsNotASymbolTheJobLeftUnset() {
        rewriteRun(
          jclWithProcedures(
            """
              //CLMUNLF  JOB (CLM,PROD),'CLAIM HISTORY FAN OUT',CLASS=P
              //         SET HLQ=CLM.PROD
              //FANOUT   EXEC PGM=INZUTILB,PARM='DB2P,CLMUNLF'
              //SYSIN    DD *,SYMBOLS=JCLONLY
                TEMPLATE HSTCARD
                         DSN '&HLQ..UNLOAD.&DB..&TS..LOADCARD'
                         UNIT SYSDA
                UNLOAD TABLESPACE CLMDB01.CLMTSHST
                       DB2 NO
                       LOCK NO
                       QUIESCE YES
                  SELECT * FROM CLM.CLAIM_HIST
                  OUTDDN (OPENCLM)
                  FORMAT DSNTIAUL
              /*
              """,
            LIBRARY,
            spec -> spec.afterRecipe(cu -> {
                ExecutionPath path = path(cu);
                assertThat(path.getGaps()).extracting(Gap::getKind)
                  .doesNotContain(Gap.Kind.SYMBOL_UNDEFINED);
                assertThat(path.getVerdict()).isEqualTo(ExecutionPath.Verdict.RESOLVED);
            })
          )
        );
    }

    /**
     * A deck that opens a utility statement and holds a card nothing can read is a step whose
     * behaviour is not known, and saying so is the whole point: read as far as the bad card and the
     * job looks like one that says everything it does.
     */
    @Test
    void saysWhenTheDeckDidNotRead() {
        rewriteRun(
          jcl(
            """
              //CLMUNLP  JOB (CLM,PROD),'UNLOAD CLM.POLICY',CLASS=P
              //UNLPOL   EXEC PGM=INZUTILB,PARM='DB2P,CLMUNLP'
              //SYSIN    DD *
                UNLOAD TABLESPACE CLMDB01.CLMTSPOL
                  SELECT * FROM CLM.POLICY WHERE STATUS_CODE = 'A
                  OUTDDN (POLUNL)
              /*
              """,
            spec -> spec.afterRecipe(cu -> {
                ExecutionPath path = path(cu);
                assertThat(path.getVerdict()).isEqualTo(ExecutionPath.Verdict.UNRESOLVED);
                assertThat(path.getGaps()).singleElement().satisfies(gap -> {
                    assertThat(gap.getKind()).isEqualTo(Gap.Kind.CARDS_NOT_READ);
                    assertThat(gap.getDdName()).isEqualTo("SYSIN");
                    // The DD is what the gap is about, so there is no other name to give.
                    assertThat(gap.getName()).isEmpty();
                });
            })
          )
        );
    }

    @Test
    void saysWhenTheProcedureWasNotSupplied() {
        rewriteRun(
          jclWithProcedures(
            """
              //CLMUNLH  JOB (CLM,PROD),'UNLOAD CLAIM HISTORY',CLASS=P
              //UNLHST   EXEC CLMUNL,UID=CLMUNLH,DECK=UNLCLM01
              """,
            LIBRARY,
            spec -> spec.afterRecipe(cu -> {
                ExecutionPath path = path(cu);
                assertThat(path.getVerdict()).isEqualTo(ExecutionPath.Verdict.UNRESOLVED);
                assertThat(path.isResolved()).isFalse();
                assertThat(path.getGaps()).singleElement().satisfies(gap -> {
                    assertThat(gap.getKind()).isEqualTo(Gap.Kind.PROCEDURE_MISSING);
                    assertThat(gap.getStep()).isEqualTo("UNLHST");
                    assertThat(gap.getName()).isEqualTo("CLMUNL");
                });
            })
          )
        );
    }

    @Test
    void saysWhichSymbolNothingSets() {
        rewriteRun(
          jclWithProcedures(
            """
              //CLMUNLD  JOB (CLM,PROD),'UNLOAD POLICY DEFAULTS',CLASS=P
              //UNLPOL   EXEC PGM=INZUTILB,PARM='&DB2SSN,CLMUNLD'
              //POLDFT   DD DSN=&HLQ..UNLOAD.POLICY.PREPROD,DISP=(NEW,CATLG)
              """,
            LIBRARY,
            spec -> spec.afterRecipe(cu -> {
                ExecutionPath path = path(cu);
                assertThat(path.getVerdict()).isEqualTo(ExecutionPath.Verdict.UNRESOLVED);
                assertThat(path.getGaps()).extracting(Gap::getKind)
                  .containsOnly(Gap.Kind.SYMBOL_UNDEFINED);
                // The EXEC card names one and the DD names the other, and the DD says which DD.
                assertThat(path.getGaps()).extracting(Gap::getName)
                  .containsExactly("DB2SSN", "HLQ");
                assertThat(path.getGaps()).extracting(Gap::getDdName)
                  .containsExactly("", "POLDFT");
            })
          )
        );
    }

    /**
     * The control card member the SYSIN names is the whole of what the step does, so a job whose
     * member was not supplied says nothing about the table it unloads.
     */
    @Test
    void saysWhichMemberWasNotSupplied() {
        rewriteRun(
          jcl(
            """
              //CLMUNLB  JOB (CLM,PROD),'UNLOAD POLICY BASE',CLASS=P
              //UNLPOL   EXEC PGM=DSNUTILB,PARM='DB2P,CLMUNLB.UNLPOL'
              //SYSIN    DD DISP=SHR,DSN=CLM.PROD.CTLCARD(UNLCLM02)
              """,
            List.of(parmMember("UNLCLM04", "  UNLOAD TABLESPACE CLMDB01.CLMTSPOL\n")),
            spec -> spec.afterRecipe(cu -> {
                ExecutionPath path = path(cu);
                assertThat(path.getVerdict()).isEqualTo(ExecutionPath.Verdict.UNRESOLVED);
                assertThat(path.getGaps()).singleElement().satisfies(gap -> {
                    assertThat(gap.getKind()).isEqualTo(Gap.Kind.MEMBER_MISSING);
                    assertThat(gap.getDdName()).isEqualTo("SYSIN");
                    assertThat(gap.getName()).isEqualTo("UNLCLM02");
                });
            })
          )
        );
    }

    /**
     * The job the whole exercise is about: no line of it says which table is unloaded or how. The
     * member is the value of a symbolic the EXEC passes, the SYSIN that reads it is written in the
     * procedure, and the keywords the member leaves out are answered by a parmlib. The verdict is
     * about the step written here and the reasons are about the step of the procedure it runs.
     */
    @Test
    void followsTheDeckAProcedureNames() {
        rewriteRun(
          jclWithProcedures(
            """
              //CLMUNLD  JOB (CLM,PROD),'UNLOAD POLICY DEFAULTS',CLASS=P
              //UNLPOL   EXEC CLMUNL,UID=CLMUNLD,DECK=UNLCLM04
              //UNL.POLDFT   DD DSN=CLM.PROD.UNLOAD.POLICY.PREPROD,DISP=(NEW,CATLG)
              """,
            List.of(procedureMember("CLMUNL",
              """
                //CLMUNL   PROC HLQ=CLM.PROD,DB2SSN=DB2P,UID=,DECK=
                //UNL      EXEC PGM=INZUTILB,PARM='&DB2SSN,&UID'
                //INFPLIB  DD DISP=SHR,DSN=DB2UNL.PARMLIB
                //SYSIN    DD DISP=SHR,DSN=&HLQ..CTLCARD(&DECK)
                //         PEND
                """)),
            List.of(parmMember("UNLCLM04",
              """
                -- NO FORMAT, NO DB2, NO LOCK AND NO QUIESCE: ALL FOUR COME FROM THE
                -- PARMLIB MEMBER INZUTIL ON INFPLIB, WHICH IS NOT IN THIS LIBRARY.
                  UNLOAD TABLESPACE CLMDB01.CLMTSPOL
                    SELECT *
                      FROM CLM.POLICY
                    OUTDDN (POLDFT)
                """)),
            spec -> spec.afterRecipe(cu -> {
                // One verdict for the one step the job writes, not a second for the procedure's.
                assertThat(new ExecutionPath.Matcher().lower(cu).collect(toList())).hasSize(1);

                ExecutionPath path = path(cu);
                assertThat(path.getVerdict()).isEqualTo(ExecutionPath.Verdict.INHERITED);
                assertThat(path.getGaps()).extracting(Gap::getKind)
                  .containsOnly(Gap.Kind.KEYWORD_INHERITED);
                assertThat(path.getGaps()).extracting(Gap::getName)
                  .contains("FORMAT", "DB2", "LOCK", "QUIESCE");
                assertThat(path.getGaps()).extracting(Gap::getStep).containsOnly("UNLPOL.UNL");
            })
          )
        );
    }

    private static ExecutionPath path(Jcl.CompilationUnit cu) {
        return new ExecutionPath.Matcher().lower(cu).findFirst().orElseThrow();
    }
}
