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
package org.openrewrite.mainframe.controlcard.utility;

import org.junit.jupiter.api.Test;
import org.openrewrite.mainframe.controlcard.utility.trait.Template;
import org.openrewrite.mainframe.controlcard.utility.trait.UnloadCommand;
import org.openrewrite.mainframe.controlcard.utility.tree.Utility;
import org.openrewrite.mainframe.jcl.tree.Jcl;
import org.openrewrite.test.RewriteTest;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.openrewrite.mainframe.jcl.Assertions.jcl;
import static org.openrewrite.mainframe.jcl.tree.ParserAssertions.jclWithProcedures;
import static org.openrewrite.mainframe.jcl.tree.ParserAssertions.procedureMember;

class InStreamUnloadDeckTest implements RewriteTest {

    private static final String JOB = """
      //CLMUNLP  JOB (CLM,PROD),'UNLOAD CLM.POLICY',CLASS=P
      //UNLPOL   EXEC PGM=INZUTILB,PARM='DB2P,CLMUNLP'
      //POLUNL   DD DSN=CLM.PROD.UNLOAD.POLICY,DISP=(NEW,CATLG)
      //SYSIN    DD *
        UNLOAD TABLESPACE CLMDB01.CLMTSPOL
               DB2 NO
               LOCK YES
               QUIESCE YES
          SELECT * FROM CLM.POLICY
          OUTDDN (POLUNL)
          FORMAT DSNTIAUL
      /*
      """;

    @Test
    void deckWrittenInAJob() {
        rewriteRun(
          jcl(JOB,
            spec -> spec.afterRecipe(cu -> {
                List<InStreamUnloadDeck> decks = InStreamUnloadDeck.of(cu);
                assertThat(decks).hasSize(1);
                assertThat(decks.get(0).getDdName()).isEqualTo("SYSIN");
                assertThat(decks.get(0).getLine()).isEqualTo(5);

                UnloadCommand unload = new UnloadCommand.Matcher()
                  .lower(decks.get(0).getDeck()).findFirst().orElseThrow();
                assertThat(unload.getTableSpace()).isEqualTo("CLMDB01.CLMTSPOL");
                assertThat(unload.getOutputDdNames()).containsExactly("POLUNL");
                assertThat(unload.getLock()).isEqualTo("YES");
            })
          )
        );
    }

    /**
     * The deck the job runs is named nowhere in it: the member is the value of a symbolic the EXEC
     * passes, and the SYSIN that reads it is written in the procedure.
     */
    @Test
    void deckReachedThroughAProcedure() {
        rewriteRun(
          jclWithProcedures(
            """
              //CLMUNLH  JOB (CLM,PROD),'UNLOAD CLAIM HISTORY',CLASS=P
              //UNLHST   EXEC CLMUNL,DECK=UNLCLM01
              """,
            List.of(procedureMember("CLMUNL",
              """
                //CLMUNL   PROC HLQ=CLM.PROD,DECK=
                //UNL      EXEC PGM=INZUTILB
                //SYSIN    DD *
                  UNLOAD TABLESPACE CLMDB01.CLMTSHST
                         DB2 NO
                    SELECT * FROM CLM.CLAIM_HIST
                    OUTDDN (HSTUNL)
                //         PEND
                """)),
            spec -> spec.afterRecipe(cu -> {
                List<InStreamUnloadDeck> decks = InStreamUnloadDeck.of(cu);
                assertThat(decks).hasSize(1);
                // Nothing of the procedure is written in the job, so the deck is reached at the EXEC.
                assertThat(decks.get(0).getLine()).isEqualTo(2);

                UnloadCommand unload = new UnloadCommand.Matcher()
                  .lower(decks.get(0).getDeck()).findFirst().orElseThrow();
                assertThat(unload.getTableSpace()).isEqualTo("CLMDB01.CLMTSHST");

                // The cards belong to the procedure, so they cannot be written back through the job.
                assertThatThrownBy(() -> decks.get(0).write(cu))
                  .isInstanceOf(IllegalStateException.class);
            })
          )
        );
    }

    /**
     * A deck edited through the island and written back into the job: one card changes and every
     * other byte of the job is what it was.
     */
    @Test
    void writesAnEditedDeckBackIntoTheJob() {
        rewriteRun(
          jcl(JOB,
            spec -> spec.afterRecipe(cu -> {
                InStreamUnloadDeck deck = InStreamUnloadDeck.of(cu).get(0);
                Jcl.CompilationUnit written = deck.withDeck(quiesced(deck.getDeck())).write(cu);

                assertThat(written.printAll())
                  .isEqualTo(cu.printAll().replace("QUIESCE YES", "QUIESCE NO"));
                // What the job now runs reads back through the island as what was written.
                assertThat(new UnloadCommand.Matcher()
                  .lower(InStreamUnloadDeck.of(written).get(0).getDeck())
                  .findFirst().orElseThrow().getQuiesce()).isEqualTo("NO");
            })
          )
        );
    }

    /**
     * Writing a deck back unchanged changes nothing, which is what makes the edit above the only
     * change the job carries.
     */
    @Test
    void writesAnUneditedDeckBackAsItWas() {
        rewriteRun(
          jcl(JOB,
            spec -> spec.afterRecipe(cu -> {
                InStreamUnloadDeck deck = InStreamUnloadDeck.of(cu).get(0);
                assertThat(deck.write(cu).printAll()).isEqualTo(cu.printAll());
            })
          )
        );
    }

    /**
     * A deck holds the cards it was read from and writing replaces them, so a deck read before an
     * earlier write says so rather than writing nothing.
     */
    @Test
    void refusesADeckReadBeforeAnEarlierWrite() {
        rewriteRun(
          jcl(JOB,
            spec -> spec.afterRecipe(cu -> {
                InStreamUnloadDeck deck = InStreamUnloadDeck.of(cu).get(0);
                Jcl.CompilationUnit written = deck.write(cu);
                assertThatThrownBy(() -> deck.write(written)).isInstanceOf(IllegalStateException.class);
            })
          )
        );
    }

    /**
     * SYMBOLS=JCLONLY has JES substitute the job's symbols into the card text before the step runs.
     * The utility's own template variables are written the same way and are still there when it
     * reads the deck, so the two have to be told apart by name.
     */
    @Test
    void jclSymbolsAndTemplateVariablesInOneDeck() {
        rewriteRun(
          jcl(
            """
              //CLMUNLF  JOB (CLM,PROD),'CLAIM HISTORY FAN OUT',CLASS=P
              //         SET HLQ=CLM.PROD
              //FANOUT   EXEC PGM=INZUTILB
              //SYSIN    DD *,SYMBOLS=JCLONLY
                TEMPLATE HSTCARD
                         DSN '&HLQ..UNLOAD.&DB..&TS..LOADCARD'
                         UNIT SYSDA
                UNLOAD TABLESPACE CLMDB01.CLMTSHST
                  SELECT * FROM CLM.CLAIM_HIST
                  OUTDDN (HSTUNL)
              /*
              """,
            spec -> spec.afterRecipe(cu -> {
                Utility.CompilationUnit deck = InStreamUnloadDeck.of(cu).get(0).getDeck();
                Template template = new Template.Matcher().lower(deck).findFirst().orElseThrow();
                assertThat(template.getName()).isEqualTo("HSTCARD");
                assertThat(template.getVariables()).containsExactly("DB", "TS");
                assertThat(template.getSymbols()).containsExactly("HLQ");
            })
          )
        );
    }

    private static Utility.CompilationUnit quiesced(Utility.CompilationUnit deck) {
        return (Utility.CompilationUnit) new UtilityIsoVisitor<Integer>() {
            @Override
            public Utility.Operand visitOperand(Utility.Operand operand, Integer p) {
                Utility.Operand o = super.visitOperand(operand, p);
                if (!"QUIESCE".equalsIgnoreCase(o.getKeyword().getText())) {
                    return o;
                }
                return o.withValue(o.getValue().stream()
                  .map(word -> word.withText("NO"))
                  .collect(toList()));
            }
        }.visit(deck, 0);
    }
}
