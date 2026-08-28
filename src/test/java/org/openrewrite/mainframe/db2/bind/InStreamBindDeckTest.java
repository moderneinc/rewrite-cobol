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
package org.openrewrite.mainframe.db2.bind;

import org.junit.jupiter.api.Test;
import org.openrewrite.mainframe.db2.bind.trait.BindCommand;
import org.openrewrite.test.RewriteTest;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.mainframe.jcl.Assertions.jcl;

class InStreamBindDeckTest implements RewriteTest {

    @Test
    void deckWrittenInAJob() {
        rewriteRun(
          jcl(
            """
              //DB2BIND JOB (ACCT#),'BIND PROGRAMS',CLASS=A
              //BIND    EXEC PGM=IKJEFT01,DYNAMNBR=20
              //DBRMLIB  DD  DSN=SYSD.STOCK.DBRMLIB,DISP=SHR
              //SYSTSIN DD *
              DSN SYSTEM(DB2D)
              BIND PACKAGE (STOCKTRD)  -
                   MEMBER(ACCT01)      -
                   ACTION (REPLACE)
              END
              /*
              """,
            spec -> spec.afterRecipe(cu -> {
                List<InStreamBindDeck> decks = InStreamBindDeck.of(cu);
                assertThat(decks).hasSize(1);
                assertThat(decks.get(0).getDdName()).isEqualTo("SYSTSIN");
                assertThat(decks.get(0).getLine()).isEqualTo(5);

                List<BindCommand> commands = new BindCommand.Matcher()
                  .lower(decks.get(0).getDeck()).collect(Collectors.toList());
                assertThat(commands).hasSize(1);
                assertThat(commands.get(0).getCollection()).isEqualTo("STOCKTRD");
                assertThat(commands.get(0).getMembers()).containsExactly("ACCT01");
                // The deck's own lines are counted from its first card; the job says where that is.
                assertThat(commands.get(0).getLine()).isEqualTo(2);
            })
          )
        );
    }

    @Test
    void ddOverriddenOnAProcedureStep() {
        rewriteRun(
          jcl(
            """
              //CBLDB21C JOB (),CLASS=A
              //COMPILE  EXEC DB2CBL,MBR=CBLDB21
              //BIND.SYSTSIN  DD *,SYMBOLS=CNVTSYS
               DSN SYSTEM(DBCG)
               BIND PLAN(&SYSUID) PKLIST(&SYSUID..*) MEMBER(CBLDB21) -
                    ACT(REP) ISO(CS) ENCODING(EBCDIC)
              /*
              """,
            spec -> spec.afterRecipe(cu -> {
                List<InStreamBindDeck> decks = InStreamBindDeck.of(cu);
                assertThat(decks).hasSize(1);
                assertThat(decks.get(0).getDdName()).isEqualTo("SYSTSIN");

                BindCommand bind = new BindCommand.Matcher()
                  .lower(decks.get(0).getDeck()).findFirst().orElseThrow();
                assertThat(bind.getPlans()).containsExactly("&SYSUID");
                assertThat(bind.getMembers()).containsExactly("CBLDB21");
            })
          )
        );
    }

    @Test
    void streamThatBindsNothingIsNotADeck() {
        rewriteRun(
          jcl(
            """
              //GRANT JOB (),CLASS=A
              //RUN   EXEC PGM=IKJEFT01
              //SYSTSIN DD *
              DSN SYSTEM(DBCG)
              RUN PROGRAM(DSNTEP2) PLAN(DSNTEP2)
              //SYSIN DD *
               GRANT EXECUTE ON PLAN CBSA TO IBMUSER;
              /*
              """,
            spec -> spec.afterRecipe(cu -> assertThat(InStreamBindDeck.of(cu)).isEmpty())
          )
        );
    }

    @Test
    void severalStepsBindInOneJob() {
        rewriteRun(
          jcl(
            """
              //CLMCMPD JOB (),CLASS=A
              //BIND1 EXEC PGM=IKJEFT01
              //SYSTSIN DD *
              DSN SYSTEM(DB2P)
              BIND PACKAGE(CLMPKG) MEMBER(CLMD010)
              END
              /*
              //BIND2 EXEC PGM=IKJEFT01
              //SYSTSIN DD *
              DSN SYSTEM(DB2P)
              BIND PLAN(CLMPLAN) PKLIST(CLMPKG.*)
              END
              /*
              """,
            spec -> spec.afterRecipe(cu -> {
                List<InStreamBindDeck> decks = InStreamBindDeck.of(cu);
                assertThat(decks).hasSize(2);
                assertThat(decks.get(0).getLine()).isEqualTo(4);
                assertThat(decks.get(1).getLine()).isEqualTo(10);

                BindCommand plan = new BindCommand.Matcher()
                  .lower(decks.get(1).getDeck()).findFirst().orElseThrow();
                assertThat(plan.getPackageList()).containsExactly("CLMPKG.*");
            })
          )
        );
    }
}
