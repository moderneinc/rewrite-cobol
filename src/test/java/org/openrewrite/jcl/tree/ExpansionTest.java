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
package org.openrewrite.jcl.tree;

import org.junit.jupiter.api.Test;
import org.openrewrite.jcl.ExpandedPrinter;
import org.openrewrite.jcl.JclIsoVisitor;
import org.openrewrite.jcl.marker.ExpandedMember;
import org.openrewrite.jcl.marker.ResolvedText;
import org.openrewrite.jcl.marker.Symbolic;
import org.openrewrite.test.RewriteTest;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.jcl.tree.ParserAssertions.jclWithProcedures;
import static org.openrewrite.jcl.tree.ParserAssertions.procedureMember;

/**
 * What a job actually runs, read from the members it names.
 * <p>
 * Every case here also asserts that the source prints back byte for byte, since {@code rewriteRun}
 * compares before with after: a resolved procedure is in the tree and never in the text.
 */
class ExpansionTest implements RewriteTest {

    private static final List<Path> BATCH = singletonList(procedureMember("CLMBATCH",
      """
        //CLMBATCH PROC PGM=,HLQ=CLM.PROD,REGION=4M
        //RUN      EXEC PGM=&PGM,REGION=&REGION
        //STEPLIB  DD DISP=SHR,DSN=&HLQ..LOADLIB
        //SYSOUT   DD SYSOUT=*
        //SYSTSIN  DD DUMMY
        //         PEND
        """));

    private static List<Jcl.Expansion> expansions(Jcl.CompilationUnit cu) {
        List<Jcl.Expansion> found = new ArrayList<>();
        new JclIsoVisitor<List<Jcl.Expansion>>() {
            @Override
            public Jcl.Expansion visitExpansion(Jcl.Expansion expansion, List<Jcl.Expansion> out) {
                out.add(expansion);
                return super.visitExpansion(expansion, out);
            }
        }.visit(cu, found);
        return found;
    }

    private static List<String> cards(Jcl.Expansion expansion) {
        List<String> texts = new ArrayList<>();
        for (Statement statement : expansion.getStatements()) {
            if (statement instanceof Jcl.JobControlStatement) {
                texts.add(((Jcl.JobControlStatement) statement).getName().getText());
            }
        }
        return texts;
    }

    private static Jcl.JobControlStatement card(Jcl.Expansion expansion, String name) {
        for (Statement statement : expansion.getStatements()) {
            if (statement instanceof Jcl.JobControlStatement &&
                ((Jcl.JobControlStatement) statement).getSimpleName().equalsIgnoreCase(name)) {
                return (Jcl.JobControlStatement) statement;
            }
        }
        throw new AssertionError("no card named " + name);
    }

    private static String resolved(Jcl.JobControlStatement statement, String keyword) {
        Jcl.KeywordParameter parameter = statement.getParameter(keyword);
        assertThat(parameter).as("%s on %s", keyword, statement.getName().getText()).isNotNull();
        return parameter.getMarkers().findFirst(ResolvedText.class)
          .map(ResolvedText::getText).orElseGet(parameter::getValueText);
    }

    @Test
    void expandsCatalogedProcedure() {
        rewriteRun(
          jclWithProcedures(
            """
              //CLMJ010  JOB (CLM),'EXTRACT',CLASS=P
              //EXTRACT  EXEC CLMBATCH,PGM=CLMB010
              """,
            BATCH,
            spec -> spec.afterRecipe(cu -> {
                assertThat(expansions(cu)).singleElement().satisfies(expansion -> {
                    assertThat(expansion.getMemberName()).isEqualTo("CLMBATCH");
                    assertThat(expansion.getKind()).isEqualTo(Jcl.Expansion.Kind.PROCEDURE);
                    assertThat(cards(expansion)).containsExactly("//RUN", "//STEPLIB", "//SYSOUT", "//SYSTSIN");
                });
            })
          )
        );
    }

    @Test
    void marksTheCallAndSaysWhereTheProcedureCameFrom() {
        rewriteRun(
          jclWithProcedures(
            "//EXTRACT  EXEC CLMBATCH,PGM=CLMB010\n",
            BATCH,
            spec -> spec.afterRecipe(cu -> {
                Jcl.JobControlStatement exec = (Jcl.JobControlStatement) cu.getStatements().get(0);
                assertThat(exec.getMarkers().findFirst(ExpandedMember.class)).hasValueSatisfying(member -> {
                    assertThat(member.getMemberName()).isEqualTo("CLMBATCH");
                    assertThat(member.getStatus()).isEqualTo(ExpandedMember.Status.EXPANDED);
                });
            })
          )
        );
    }

    @Test
    void marksAProcedureThatIsNotInTheLibrary() {
        rewriteRun(
          jclWithProcedures(
            "//COMPILE  EXEC IGYWCL,MEM=CLMB010\n",
            BATCH,
            spec -> spec.afterRecipe(cu -> {
                Jcl.JobControlStatement exec = (Jcl.JobControlStatement) cu.getStatements().get(0);
                assertThat(exec.getMarkers().findFirst(ExpandedMember.class)).hasValueSatisfying(member -> {
                    assertThat(member.getMemberName()).isEqualTo("IGYWCL");
                    assertThat(member.getStatus()).isEqualTo(ExpandedMember.Status.MISSING);
                });
                assertThat(expansions(cu)).isEmpty();
            })
          )
        );
    }

    @Test
    void readsProcEqualsAndThePositionalNameAlike() {
        rewriteRun(
          jclWithProcedures(
            "//EXTRACT  EXEC PROC=CLMBATCH,PGM=CLMB010\n",
            BATCH,
            spec -> spec.afterRecipe(cu ->
              assertThat(expansions(cu)).singleElement()
                .satisfies(e -> assertThat(e.getMemberName()).isEqualTo("CLMBATCH")))
          )
        );
    }

    @Test
    void substitutesProcedureDefaultsAndOverrides() {
        rewriteRun(
          jclWithProcedures(
            "//EXTRACT  EXEC CLMBATCH,PGM=CLMB010\n",
            BATCH,
            spec -> spec.afterRecipe(cu -> {
                Jcl.Expansion expansion = expansions(cu).get(0);
                // PGM= is an override, HLQ= and REGION= are the procedure's own defaults.
                assertThat(resolved(card(expansion, "RUN"), "PGM")).isEqualTo("CLMB010");
                assertThat(resolved(card(expansion, "RUN"), "REGION")).isEqualTo("4M");
                assertThat(resolved(card(expansion, "STEPLIB"), "DSN")).isEqualTo("CLM.PROD.LOADLIB");
            })
          )
        );
    }

    @Test
    void aSetInTheJobReachesTheProcedure() {
        rewriteRun(
          jclWithProcedures(
            """
              //         SET HLQ=CLM.TEST
              //EXTRACT  EXEC CLMBATCH,PGM=CLMB010,HLQ=&HLQ
              """,
            BATCH,
            spec -> spec.afterRecipe(cu ->
              assertThat(resolved(card(expansions(cu).get(0), "STEPLIB"), "DSN"))
                .isEqualTo("CLM.TEST.LOADLIB"))
          )
        );
    }

    /**
     * A trailing period ends a symbol and is eaten, which is what makes {@code &HLQ..LOADLIB} read
     * as one qualifier and a period rather than as a symbol named {@code HLQ.}.
     */
    @Test
    void aPeriodEndsASymbol() {
        rewriteRun(
          jclWithProcedures(
            """
              //         SET ENV=PROD
              //         SET HLQ=CLM.&ENV
              //RUN      EXEC PGM=IEFBR14
              //MASTER   DD DISP=SHR,DSN=&HLQ..CLMMAST
              """,
            BATCH,
            spec -> spec.afterRecipe(cu -> {
                Jcl.JobControlStatement dd = (Jcl.JobControlStatement) cu.getStatements().get(3);
                assertThat(resolved(dd, "DSN")).isEqualTo("CLM.PROD.CLMMAST");
            })
          )
        );
    }

    @Test
    void aSymbolNothingSetIsLeftAsWritten() {
        rewriteRun(
          jclWithProcedures(
            """
              //RUN      EXEC PGM=IEFBR14
              //MASTER   DD DISP=SHR,DSN=&HLQ..CLMMAST,VOL=SER=&SYSUID
              """,
            BATCH,
            spec -> spec.afterRecipe(cu -> {
                Jcl.JobControlStatement dd = (Jcl.JobControlStatement) cu.getStatements().get(1);
                assertThat(resolved(dd, "DSN")).isEqualTo("&HLQ..CLMMAST");
                ResolvedText text = dd.getParameter("DSN").getMarkers().findFirst(ResolvedText.class).orElseThrow();
                assertThat(text.getSymbolics()).singleElement().satisfies(symbolic -> {
                    assertThat(symbolic.getName()).isEqualTo("HLQ");
                    assertThat(symbolic.getValue()).isNull();
                    assertThat(symbolic.getOrigin()).isEqualTo(Symbolic.Origin.UNDEFINED);
                });
                assertThat(dd.getParameter("VOL").getMarkers().findFirst(ResolvedText.class).orElseThrow()
                  .getSymbolics()).singleElement()
                  .satisfies(symbolic -> assertThat(symbolic.getOrigin()).isEqualTo(Symbolic.Origin.SYSTEM));
            })
          )
        );
    }

    /**
     * {@code &&WORK} names a temporary data set, not a symbol called {@code &WORK}.
     */
    @Test
    void aDoubledAmpersandIsNotASymbol() {
        rewriteRun(
          jclWithProcedures(
            """
              //RUN      EXEC PGM=IEFBR14
              //TEMP     DD DSN=&&WORK,DISP=(NEW,PASS)
              """,
            BATCH,
            spec -> spec.afterRecipe(cu -> {
                Jcl.JobControlStatement dd = (Jcl.JobControlStatement) cu.getStatements().get(1);
                assertThat(dd.getParameter("DSN").getMarkers().findFirst(ResolvedText.class)).isEmpty();
            })
          )
        );
    }

    /**
     * Apostrophes are how a value with special characters in it is written, and are not part of the
     * value: a procedure that defaults one and uses it inside apostrophes runs with one pair, not
     * two.
     */
    @Test
    void apostrophesAroundAValueAreNotPartOfIt() {
        rewriteRun(
          jclWithProcedures(
            "//COMPILE  EXEC CLMCOB\n",
            singletonList(procedureMember("CLMCOB",
              """
                //CLMCOB   PROC CBLOPTS='APOST,RENT'
                //COB      EXEC PGM=IGYCRCTL,PARM='&CBLOPTS'
                //         PEND
                """)),
            spec -> spec.afterRecipe(cu ->
              assertThat(resolved(card(expansions(cu).get(0), "COB"), "PARM"))
                .isEqualTo("'APOST,RENT'"))
          )
        );
    }

    /**
     * A symbol set from another symbol that nothing set stands for nothing itself, so every use of
     * it is left as it was written rather than filled in with half a name.
     */
    @Test
    void aSymbolSetFromOneNothingSetIsNotFilledIn() {
        rewriteRun(
          jclWithProcedures(
            """
              //         SET HLQ=&INSTHLQ.
              //RUN      EXEC PGM=IEFBR14
              //MASTER   DD DISP=SHR,DSN=&HLQ..CLMMAST
              """,
            BATCH,
            spec -> spec.afterRecipe(cu -> {
                Jcl.JobControlStatement dd = (Jcl.JobControlStatement) cu.getStatements().get(2);
                assertThat(resolved(dd, "DSN")).isEqualTo("&HLQ..CLMMAST");
                assertThat(dd.getParameter("DSN").getMarkers().findFirst(ResolvedText.class).orElseThrow()
                  .getSymbolics()).singleElement().satisfies(symbolic -> {
                    assertThat(symbolic.getValue()).isNull();
                    assertThat(symbolic.getOrigin()).isEqualTo(Symbolic.Origin.SET);
                });
            })
          )
        );
    }

    @Test
    void expandsAnIncludeMember() {
        List<Path> library = List.of(
          procedureMember("@JOBCARD",
            """
              //         SET ENV=PROD
              //         SET HLQ=CLM.&ENV
              """),
          BATCH.get(0));
        rewriteRun(
          jclWithProcedures(
            """
              //CLMJ010  JOB (CLM),'EXTRACT'
              //         INCLUDE MEMBER=@JOBCARD
              //EXTRACT  EXEC CLMBATCH,PGM=CLMB010,HLQ=&HLQ
              """,
            library,
            spec -> spec.afterRecipe(cu -> {
                List<Jcl.Expansion> found = expansions(cu);
                assertThat(found).extracting(Jcl.Expansion::getMemberName)
                  .containsExactly("@JOBCARD", "CLMBATCH");
                assertThat(found.get(0).getKind()).isEqualTo(Jcl.Expansion.Kind.INCLUDE);
                // The included SET statements are in effect for the cards after the INCLUDE.
                assertThat(resolved(card(found.get(1), "STEPLIB"), "DSN")).isEqualTo("CLM.PROD.LOADLIB");
            })
          )
        );
    }

    @Test
    void expandsAnInStreamProcedure() {
        rewriteRun(
          jclWithProcedures(
            """
              //CLMJ010  JOB (CLM),'EXTRACT'
              //MYPROC   PROC HLQ=CLM.TEST
              //RUN      EXEC PGM=IEFBR14
              //MASTER   DD DISP=SHR,DSN=&HLQ..CLMMAST
              //         PEND
              //EXTRACT  EXEC MYPROC
              """,
            BATCH,
            spec -> spec.afterRecipe(cu -> {
                assertThat(expansions(cu)).singleElement().satisfies(expansion -> {
                    assertThat(expansion.getMemberName()).isEqualTo("MYPROC");
                    assertThat(resolved(card(expansion, "MASTER"), "DSN")).isEqualTo("CLM.TEST.CLMMAST");
                });
                Jcl.JobControlStatement exec = (Jcl.JobControlStatement) cu.getStatements().get(5);
                assertThat(exec.getMarkers().findFirst(ExpandedMember.class).orElseThrow().getStatus())
                  .isEqualTo(ExpandedMember.Status.IN_STREAM);
            })
          )
        );
    }

    @Test
    void mergesADataDefinitionOverride() {
        rewriteRun(
          jclWithProcedures(
            """
              //EXTRACT  EXEC CLMBATCH,PGM=CLMB010
              //RUN.SYSTSIN DD DSN=CLM.PROD.CTLCARD(RUNTEP2),DISP=SHR
              """,
            BATCH,
            spec -> spec.afterRecipe(cu -> {
                Jcl.JobControlStatement systsin = card(expansions(cu).get(0), "SYSTSIN");
                assertThat(resolved(systsin, "DSN")).isEqualTo("CLM.PROD.CTLCARD(RUNTEP2)");
                assertThat(resolved(systsin, "DISP")).isEqualTo("SHR");
                // A data set replaces the procedure's DUMMY.
                assertThat(systsin.getParameters()).noneMatch(p -> p instanceof Jcl.PositionalParameter);
            })
          )
        );
    }

    @Test
    void addsADataDefinitionTheProcedureDoesNotHave() {
        rewriteRun(
          jclWithProcedures(
            """
              //EXTRACT  EXEC CLMBATCH,PGM=CLMB010
              //RUN.CLMMAST DD DISP=SHR,DSN=CLM.PROD.CLMMAST
              """,
            BATCH,
            spec -> spec.afterRecipe(cu -> {
                Jcl.Expansion expansion = expansions(cu).get(0);
                assertThat(cards(expansion)).containsExactly("//RUN", "//STEPLIB", "//SYSOUT",
                  "//SYSTSIN", "//CLMMAST");
                assertThat(resolved(card(expansion, "CLMMAST"), "DSN")).isEqualTo("CLM.PROD.CLMMAST");
            })
          )
        );
    }

    /**
     * z/OS lets a one-step procedure take an override written without the step name.
     */
    @Test
    void anUnqualifiedOverrideBelongsToTheOneStep() {
        rewriteRun(
          jclWithProcedures(
            """
              //EXTRACT  EXEC CLMBATCH,PGM=CLMB010
              //SYSOUT   DD SYSOUT=D
              """,
            BATCH,
            spec -> spec.afterRecipe(cu ->
              assertThat(resolved(card(expansions(cu).get(0), "SYSOUT"), "SYSOUT")).isEqualTo("D"))
          )
        );
    }

    @Test
    void substitutesExportedSymbolsIntoInStreamData() {
        rewriteRun(
          jclWithProcedures(
            """
              //BLDDB01  JOB (ACCT),'BUILD'
              //         EXPORT SYMLIST=(DB2SYS)
              //         SET DB2SYS=DB2P
              //         SET DB2PLAN=CLMPLAN
              //BUILDDB  EXEC PGM=IKJEFT01
              //SYSTSIN  DD *,SYMBOLS=EXECSYS
               DSN SYSTEM(&DB2SYS) PLAN(&DB2PLAN)
              """,
            BATCH,
            spec -> spec.afterRecipe(cu -> {
                List<String> data = new ArrayList<>();
                for (Statement statement : cu.getStatements()) {
                    if (statement instanceof Jcl.DataDefinitionStream) {
                        data.add(statement.getMarkers().findFirst(ResolvedText.class)
                          .map(ResolvedText::getText)
                          .orElseGet(() -> ((Jcl.DataDefinitionStream) statement).getWord().getText()));
                    }
                }
                // DB2SYS is exported and DB2PLAN is not, so only the first is substituted.
                assertThat(data).containsExactly("DSN", "SYSTEM(DB2P)", "PLAN(&DB2PLAN)");
            })
          )
        );
    }

    @Test
    void leavesInStreamDataAloneWithoutASymbolsParameter() {
        rewriteRun(
          jclWithProcedures(
            """
              //         EXPORT SYMLIST=*
              //         SET DB2SYS=DB2P
              //BUILDDB  EXEC PGM=IKJEFT01
              //SYSTSIN  DD *
               DSN SYSTEM(&DB2SYS)
              """,
            BATCH,
            spec -> spec.afterRecipe(cu -> assertThat(cu.getStatements())
              .filteredOn(s -> s instanceof Jcl.DataDefinitionStream)
              .allSatisfy(s -> assertThat(s.getMarkers().findFirst(ResolvedText.class)).isEmpty()))
          )
        );
    }

    /**
     * A procedure that calls itself would expand for ever; the member is expanded once and the
     * second call is reported the way a missing one is.
     */
    @Test
    void doesNotExpandAProcedureIntoItself() {
        rewriteRun(
          jclWithProcedures(
            "//RUNIT    EXEC LOOPER\n",
            singletonList(procedureMember("LOOPER",
              """
                //LOOPER   PROC
                //INNER    EXEC LOOPER
                //         PEND
                """)),
            spec -> spec.afterRecipe(cu -> assertThat(expansions(cu)).hasSize(1))
          )
        );
    }

    /**
     * The resolved body follows the card that called it, and the override cards stay where they
     * were written — so the listing says both what was asked for and what it resolved to.
     */
    @Test
    void printsTheResolvedListing() {
        rewriteRun(
          jclWithProcedures(
            """
              //CLMJ010  JOB (CLM),'EXTRACT'
              //         SET HLQ=CLM.TEST
              //EXTRACT  EXEC CLMBATCH,PGM=CLMB010,HLQ=&HLQ
              //RUN.CLMMAST DD DISP=SHR,DSN=&HLQ..CLMMAST
              """,
            BATCH,
            spec -> spec.afterRecipe(cu -> assertThat(ExpandedPrinter.print(cu)).isEqualTo(
              """
                //CLMJ010  JOB (CLM),'EXTRACT'
                //         SET HLQ=CLM.TEST
                //EXTRACT  EXEC CLMBATCH,PGM=CLMB010,HLQ=CLM.TEST
                //*+ BEGIN PROC CLMBATCH
                //RUN      EXEC PGM=CLMB010,REGION=4M
                //STEPLIB  DD DISP=SHR,DSN=CLM.TEST.LOADLIB
                //SYSOUT   DD SYSOUT=*
                //SYSTSIN  DD DUMMY
                //CLMMAST DD DISP=SHR,DSN=CLM.TEST.CLMMAST
                //*+ END PROC CLMBATCH
                //RUN.CLMMAST DD DISP=SHR,DSN=CLM.TEST.CLMMAST"""))
          )
        );
    }
}
