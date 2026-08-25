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
package org.openrewrite.linkedit;

import org.junit.jupiter.api.Test;
import org.openrewrite.linkedit.trait.LinkEditDeck;
import org.openrewrite.test.RewriteTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.jcl.Assertions.jcl;

class InStreamLinkEditDeckTest implements RewriteTest {

    @Test
    void deckWrittenInAJob() {
        rewriteRun(
          jcl(
            """
              //CICSASM  JOB (),CLASS=A
              //ASM      EXEC DFHEITAL
              //LKED.SYSIN   DD *
               INCLUDE SYSLIB(DFHEAI)
               ENTRY DFH$PCPE
               NAME  DFH$PCPE(R)
              /*
              """,
            spec -> spec.afterRecipe(cu -> {
                List<InStreamLinkEditDeck> decks = InStreamLinkEditDeck.of(cu);
                assertThat(decks).hasSize(1);
                assertThat(decks.get(0).getDdName()).isEqualTo("SYSIN");
                assertThat(decks.get(0).getLine()).isEqualTo(4);

                LinkEditDeck deck = new LinkEditDeck.Matcher().require(decks.get(0).getDeck(), null);
                assertThat(deck.getModule().getText()).isEqualTo("DFH$PCPE");
                assertThat(deck.getEntry().getText()).isEqualTo("DFH$PCPE");
                // The deck's own lines are counted from its first card; the job says where that is.
                assertThat(deck.getModule().getLine()).isEqualTo(3);
            })
          )
        );
    }

    /**
     * The binder reads its control statements from {@code SYSLIN}, which is also where the object deck
     * comes from, so a job may write them there rather than on the {@code SYSIN} a compile-and-link
     * procedure concatenates to it.
     */
    @Test
    void deckWrittenOnSyslin() {
        rewriteRun(
          jcl(
            """
              //LINKU020 JOB (),CLASS=A
              //LKED     EXEC PGM=IEWL,PARM='LIST,MAP,RENT'
              //SYSLMOD  DD DSN=CLM.PROD.LOADLIB,DISP=SHR
              //SYSLIN   DD *
                INCLUDE OBJLIB(CLMU020)
                ENTRY CLMU020
                ALIAS CLMRESV
                NAME CLMU020(R)
              /*
              """,
            spec -> spec.afterRecipe(cu -> {
                List<InStreamLinkEditDeck> decks = InStreamLinkEditDeck.of(cu);
                assertThat(decks).hasSize(1);
                assertThat(decks.get(0).getDdName()).isEqualTo("SYSLIN");

                LinkEditDeck deck = new LinkEditDeck.Matcher().require(decks.get(0).getDeck(), null);
                assertThat(deck.getIncludes()).singleElement().satisfies(include ->
                  assertThat(include.getMember()).isEqualTo("CLMU020"));
                assertThat(deck.getAliases()).extracting(LinkEditDeck.Name::getText).containsExactly("CLMRESV");
            })
          )
        );
    }

    /**
     * A step that reads its cards from a {@code LINKLIB} member writes nothing in the job, so the job
     * has no deck of its own to read.
     */
    @Test
    void cardsReadFromAMemberAreNotWrittenInTheJob() {
        rewriteRun(
          jcl(
            """
              //CLMCMPB  JOB (CLM,PROD),'COMPILE CLAIMS BATCH',CLASS=A
              //CMPB010  EXEC CLMCLB,MEM=CLMB010,HLQ=&HLQ
              //LKED.SYSIN DD DISP=SHR,DSN=&HLQ..LINKLIB(CLMB010)
              //
              """,
            spec -> spec.afterRecipe(cu -> assertThat(InStreamLinkEditDeck.of(cu)).isEmpty())
          )
        );
    }

    /**
     * A {@code SYSIN} holds whatever the step reading it wants, so a deck is taken by what it says
     * rather than by the DD it was written under.
     */
    @Test
    void streamThatLinksNothingIsNotADeck() {
        rewriteRun(
          jcl(
            """
              //SORTCLM JOB (),CLASS=A
              //SORT    EXEC PGM=SORT
              //SYSIN   DD *
                SORT FIELDS=(53,4,CH,A)
                INCLUDE COND=(57,1,CH,EQ,C'O')
              /*
              """,
            spec -> spec.afterRecipe(cu -> assertThat(InStreamLinkEditDeck.of(cu)).isEmpty())
          )
        );
    }
}
