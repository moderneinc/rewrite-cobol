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
package org.openrewrite.mainframe.controlcard;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.mainframe.jcl.tree.ParserAssertions.jcl;
import static org.openrewrite.mainframe.jcl.tree.ParserAssertions.jclWithProcedures;
import static org.openrewrite.mainframe.jcl.tree.ParserAssertions.parmMember;
import static org.openrewrite.mainframe.jcl.tree.ParserAssertions.procedureMember;

class InStreamCardsTest implements RewriteTest {

    private static final List<Path> LIBRARY = List.of(
      procedureMember("CLMSORT",
        """
          //CLMSORT  PROC HLQ=CLM.PROD
          //SORT     EXEC PGM=SORT
          //SORTIN   DD DISP=SHR,DSN=&HLQ..CLMMAST
          //SORTOUT  DD DSN=&HLQ..CLMSORT,DISP=(NEW,CATLG)
          //SYSIN    DD *
            SORT FIELDS=(1,8,CH,A)
            INCLUDE COND=(9,2,CH,EQ,C'CA')
          /*
          //         PEND
          """));

    @Test
    void cardsWrittenInTheJob() {
        rewriteRun(
          jcl(
            """
              //CLMJ010  JOB (CLM),'SORT CLAIMS',CLASS=P
              //SORT     EXEC PGM=SORT
              //SORTIN   DD DISP=SHR,DSN=CLM.PROD.CLMMAST
              //SYSIN    DD *
                SORT FIELDS=(1,8,CH,A)
              /*
              """,
            spec -> spec.afterRecipe(cu -> {
                List<InStreamCards> decks = InStreamCards.of(cu);
                assertThat(decks).singleElement().satisfies(deck -> {
                    assertThat(deck.getDdName()).isEqualTo("SYSIN");
                    assertThat(deck.getLine()).isEqualTo(5);
                    assertThat(deck.getText()).isEqualTo("  SORT FIELDS=(1,8,CH,A)");
                });
            })
          )
        );
    }

    /**
     * A SYSIN written in a cataloged procedure is a deck this job runs, so a reader of decks has to
     * see it. z/OS has allowed in-stream data in a procedure since V2R1 and shops write them there.
     */
    @Test
    void cardsWrittenInACatalogedProcedure() {
        rewriteRun(
          jclWithProcedures(
            """
              //CLMJ020  JOB (CLM),'SORT CLAIMS',CLASS=P
              //STEP1    EXEC CLMSORT,HLQ=CLM.TEST
              """, LIBRARY,
            spec -> spec.afterRecipe(cu -> {
                List<InStreamCards> decks = InStreamCards.of(cu);
                assertThat(decks).singleElement().satisfies(deck -> {
                    assertThat(deck.getDdName()).isEqualTo("SYSIN");
                    // Nothing of the procedure is written in the job, so the deck is reached at the
                    // EXEC that called it.
                    assertThat(deck.getLine()).isEqualTo(2);
                    assertThat(deck.getText()).isEqualTo(
                      "  SORT FIELDS=(1,8,CH,A)\n  INCLUDE COND=(9,2,CH,EQ,C'CA')");
                });
            })
          )
        );
    }

    /**
     * A deck the DD named as a member of a library is a deck this job runs too, and it is the shape
     * a shop uses when several jobs run the same cards. Its cards print nowhere in the job, so what
     * they say is put back together from the words the graft left behind — including the columns
     * each word was written in, without which a comment card swallows the deck.
     */
    @Test
    void cardsOfTheMemberADdNamed() {
        rewriteRun(
          jcl(
            """
              //CLMJ040  JOB (CLM),'SORT CLAIMS',CLASS=P
              //SORT     EXEC PGM=SORT
              //SYSIN    DD DISP=SHR,DSN=CLM.PROD.CTLCARD(SRTCLM01)
              """,
            List.of(parmMember("SRTCLM01",
              """
                * SORT THE CLAIM MASTER BY ACCOUNT
                  SORT FIELDS=(1,8,CH,A)
                  INCLUDE COND=(9,2,CH,EQ,C'CA')
                """)),
            spec -> spec.afterRecipe(cu -> {
                List<InStreamCards> decks = InStreamCards.of(cu);
                assertThat(decks).singleElement().satisfies(deck -> {
                    assertThat(deck.getDdName()).isEqualTo("SYSIN");
                    // The deck is reached at the DD that named the member.
                    assertThat(deck.getLine()).isEqualTo(3);
                    assertThat(deck.getText()).isEqualTo("""
                      * SORT THE CLAIM MASTER BY ACCOUNT
                        SORT FIELDS=(1,8,CH,A)
                        INCLUDE COND=(9,2,CH,EQ,C'CA')""");
                    // The cards belong to the member, so nothing writes them back through the job.
                    assertThat(deck.getCards()).isEmpty();
                });
            })
          )
        );
    }

    /**
     * A deck the job writes over a procedure step's SYSIN is one deck: the job's card is where it
     * was written, and the copy resolved into the step is the same cards.
     */
    @Test
    void countsAnOverriddenDeckOnce() {
        rewriteRun(
          jclWithProcedures(
            """
              //CLMJ030  JOB (CLM),'SORT CLAIMS',CLASS=P
              //STEP1    EXEC CLMSORT
              //SORT.SYSIN  DD *
                SORT FIELDS=(1,4,CH,D)
              /*
              """, LIBRARY,
            spec -> spec.afterRecipe(cu -> {
                List<InStreamCards> decks = InStreamCards.of(cu);
                assertThat(decks).singleElement().satisfies(deck -> {
                    assertThat(deck.getDdName()).isEqualTo("SYSIN");
                    assertThat(deck.getLine()).isEqualTo(4);
                    assertThat(deck.getText()).isEqualTo("  SORT FIELDS=(1,4,CH,D)");
                });
            })
          )
        );
    }
}
