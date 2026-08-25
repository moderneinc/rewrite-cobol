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
package org.openrewrite.controlcard.idcams;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openrewrite.controlcard.idcams.tree.Idcams;
import org.openrewrite.test.RewriteTest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.controlcard.idcams.Assertions.idcamsCard;

class IdcamsParserTest implements RewriteTest {

    @Test
    void defineClusterWithItsComponents() {
        rewriteRun(
          idcamsCard(
            """
              /* DEFCLM01 - CLAIM MASTER KSDS.  RECORD LAYOUT COPYBOOK CLMREC.  */
              DELETE CLM.PROD.CLMMAST CLUSTER PURGE
              SET MAXCC = 0
              DEFINE CLUSTER (NAME(CLM.PROD.CLMMAST)               -
                              INDEXED                              -
                              KEYS(10 0)                           -
                              RECORDSIZE(300 300)                  -
                              VOLUMES(PRD001))                     -
                     DATA    (NAME(CLM.PROD.CLMMAST.DATA)          -
                              CONTROLINTERVALSIZE(4096))           -
                     INDEX   (NAME(CLM.PROD.CLMMAST.INDEX)         -
                              CONTROLINTERVALSIZE(2048))
              """,
            spec -> spec.afterRecipe(cu -> {
                assertThat(cu.getStatements()).hasSize(3);

                Idcams.Command delete = (Idcams.Command) cu.getStatements().get(0);
                assertThat(delete.getVerb().getText()).isEqualTo("DELETE");
                assertThat(delete.getNamedParameters()).hasSize(3);

                // A group runs to the parenthesis that closes it, which is eleven cards and four
                // continuation dashes away.
                Idcams.Command define = (Idcams.Command) cu.getStatements().get(2);
                assertThat(define.getParameter("CLUSTER").getValueText())
                  .startsWith("(NAME(CLM.PROD.CLMMAST) INDEXED")
                  .endsWith("VOLUMES(PRD001))");
                assertThat(define.getParameter("DATA").getValueText())
                  .isEqualTo("(NAME(CLM.PROD.CLMMAST.DATA) CONTROLINTERVALSIZE(4096))");
                assertThat(define.getParameter("INDEX").getValueText())
                  .isEqualTo("(NAME(CLM.PROD.CLMMAST.INDEX) CONTROLINTERVALSIZE(2048))");
            })
          )
        );
    }

    @Test
    void quotedNamesAreLexedOnTheirOwn() {
        rewriteRun(
          idcamsCard(
            """
              DEF ALIAS (NAME('SYS1.DB2.V9.SDSNLOAD') -
              REL('SYS1.DB2.V12.SDSNLOAD')) CAT('CAT.MCAT')
              LISTCAT ENTRIES(SYS1.DB2.V9.SDSNLOAD) ALL
              """,
            spec -> spec.afterRecipe(cu -> {
                assertThat(cu.getStatements()).hasSize(2);
                Idcams.Command define = (Idcams.Command) cu.getStatements().get(0);
                assertThat(define.getParameter("ALIAS").getValueText())
                  .isEqualTo("(NAME('SYS1.DB2.V9.SDSNLOAD') REL('SYS1.DB2.V12.SDSNLOAD'))");
                assertThat(define.getParameter("CAT").getValueText()).isEqualTo("('CAT.MCAT')");
            })
          )
        );
    }

    @Test
    void commandContinuedBeforeItsEntryName() {
        rewriteRun(
          idcamsCard(
            """
                ALTER -
                    HLQ.DEVT.DFHCSD     -
                    NEWNM(HLQ.DEVB.DFHCSD)
                ALTER -
                    HLQ.DEVT.DFHCSD.DATA     -
                    NEWNM(HLQ.DEVB.DFHCSD.DATA)
              """,
            spec -> spec.afterRecipe(cu -> {
                assertThat(cu.getStatements()).hasSize(2);
                Idcams.Command alter = (Idcams.Command) cu.getStatements().get(0);
                assertThat(alter.getNamedParameters()).hasSize(2);
                assertThat(alter.getParameter("NEWNM").getValueText()).isEqualTo("(HLQ.DEVB.DFHCSD)");
            })
          )
        );
    }

    /**
     * A comment says nothing about the command it interrupts, so one written between two cards of a
     * continued command leaves it continued.
     */
    @Test
    void commentCardsBetweenAndWithinCommands() {
        rewriteRun(
          idcamsCard(
            """
                /* REPCLM01 - RELOAD THE CLAIM MASTER FROM THE LATEST BACKUP.     */
                /* CLMJ001 STEP LOADMAST: BACKUP AND MASTER DD CARDS.             */
                REPRO INFILE(BACKUP) -
                /* THE BACKUP IS THE ONE CLMJ080 TOOK */
                      OUTFILE(MASTER) REPLACE
              """,
            spec -> spec.afterRecipe(cu -> {
                assertThat(cu.getStatements()).hasSize(1);
                assertThat(((Idcams.Command) cu.getStatements().get(0)).getNamedParameters()).hasSize(3);
            })
          )
        );
    }

    @Test
    void refusesAMemberThatIsNotAnIdcamsDeck(@TempDir Path directory) throws IOException {
        Files.write(directory.resolve("DEFCLM01.ctl"),
          "  /* CLAIM MASTER */\n  DELETE CLM.PROD.CLMMAST CLUSTER PURGE\n".getBytes(StandardCharsets.UTF_8));
        Files.write(directory.resolve("SRTCLM01.ctl"), "  SORT FIELDS=(1,10,CH,A)\n".getBytes(StandardCharsets.UTF_8));
        Files.write(directory.resolve("RUNCLM01.ctl"), "  DSN SYSTEM(DB2P)\n".getBytes(StandardCharsets.UTF_8));

        IdcamsParser parser = IdcamsParser.builder().build();
        assertThat(parser.accept(directory.resolve("DEFCLM01.ctl"))).isTrue();
        assertThat(parser.accept(directory.resolve("SRTCLM01.ctl"))).isFalse();
        assertThat(parser.accept(directory.resolve("RUNCLM01.ctl"))).isFalse();
    }

    /**
     * {@code VERIFY} is an ICETOOL operator as well, and the two are told apart by what they name
     * their input with.
     */
    @Test
    void verifyIsIdcamsOnlyWhenItNamesADataSet() {
        assertThat(IdcamsLineReader.isIdcamsDeck("  VERIFY DATASET(CLM.PROD.CLMMAST)\n")).isTrue();
        assertThat(IdcamsLineReader.isIdcamsDeck("  VERIFY FROM(IN1) ON(9,7,CH)\n")).isFalse();
    }
}
