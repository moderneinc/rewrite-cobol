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
import org.junit.jupiter.api.io.TempDir;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Parser;
import org.openrewrite.SourceFile;
import org.openrewrite.mainframe.controlcard.utility.marker.Dialect;
import org.openrewrite.mainframe.controlcard.utility.tree.Utility;
import org.openrewrite.test.RewriteTest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.mainframe.controlcard.utility.Assertions.utilityCard;

class UtilityParserTest implements RewriteTest {

    @Test
    void unloadWithOneSelect() {
        rewriteRun(
          utilityCard(
            """
              -- UNLOAD CLM.POLICY FOR THE MONTHLY FEED.
                GLOBAL
                       SQLID CLM
                       SELMSG NUMBERED ;
                UNLOAD TABLESPACE CLMDB01.CLMTSPOL
                       DB2 NO
                       LOCK YES
                       QUIESCE YES
                  SELECT *
                    FROM CLM.POLICY
                   WHERE STATUS_CODE = 'A'
                  OUTDDN (POLUNL)
                  FORMAT DSNTIAUL
                  LOADDDN POLLOAD
              """,
            spec -> spec.afterRecipe(cu -> {
                assertThat(cu.getStatements()).hasSize(2);
                Utility.Block global = (Utility.Block) cu.getStatements().get(0);
                assertThat(global.isVerb("GLOBAL")).isTrue();
                assertThat(global.getOperand("SQLID").getValueText()).isEqualTo("CLM");

                Utility.Block unload = (Utility.Block) cu.getStatements().get(1);
                assertThat(unload.getOperand("TABLESPACE").getValueText()).isEqualTo("CLMDB01.CLMTSPOL");
                assertThat(unload.getOperand("DB2").getValueText()).isEqualTo("NO");

                Utility.Block select = unload.getBlocks("SELECT").get(0);
                assertThat(select.getValueText()).isEqualTo("*");
                assertThat(select.getOperand("FROM").getValueText()).isEqualTo("CLM.POLICY");
                // The value of a keyword runs to the next keyword, however many cards away.
                assertThat(select.getOperand("WHERE").getValueText()).isEqualTo("STATUS_CODE = 'A'");
                assertThat(select.getOperand("OUTDDN").getValueText()).isEqualTo("(POLUNL)");

                Utility.Block format = select.getBlocks("FORMAT").get(0);
                assertThat(format.getValueText()).isEqualTo("DSNTIAUL");
                // The railroad writes LOADDDN inside the format block, and a deck writes it after it.
                assertThat(format.getOperand("LOADDDN").getValueText()).isEqualTo("POLLOAD");
            })
          )
        );
    }

    /**
     * Three SELECTs in one UNLOAD is one pass over the table space, so each one has to carry its own
     * output DD and its own layout rather than the unload carrying one of each.
     */
    @Test
    void severalSelectsInOneUnload() {
        rewriteRun(
          utilityCard(
            """
                UNLOAD TABLESPACE CLMDB01.CLMTSHST
                  SELECT CLAIM_NO, POLICY_NO
                    FROM CLM.CLAIM_HIST
                   WHERE STATUS_CODE = 'O'
                  OUTDDN (OPENCLM)
                  FORMAT DSNTIAUL
                  SELECT CLAIM_NO, POSTED_DATE
                    FROM CLM.CLAIM_HIST
                   ORDER BY POSTED_DATE DESC
                  OUTDDN (CLOSCLM)
                  FORMAT DELIMITED SEP ',' DELIM '"'
              """,
            spec -> spec.afterRecipe(cu -> {
                Utility.Block unload = (Utility.Block) cu.getStatements().get(0);
                assertThat(unload.getBlocks("SELECT")).hasSize(2);

                Utility.Block first = unload.getBlocks("SELECT").get(0);
                assertThat(first.getValueText()).isEqualTo("CLAIM_NO, POLICY_NO");
                assertThat(first.getOperand("OUTDDN").getValueText()).isEqualTo("(OPENCLM)");

                Utility.Block second = unload.getBlocks("SELECT").get(1);
                assertThat(second.getOperand("ORDER").getValueText()).isEqualTo("BY POSTED_DATE DESC");
                Utility.Block delimited = second.getBlocks("FORMAT").get(0);
                assertThat(delimited.getValueText()).isEqualTo("DELIMITED");
                assertThat(delimited.getOperand("SEP").getValueText()).isEqualTo("','");
                assertThat(delimited.getOperand("DELIM").getValueText()).isEqualTo("'\"'");
            })
          )
        );
    }

    @Test
    void baseUtilityUnload() {
        rewriteRun(
          utilityCard(
            """
                UNLOAD TABLESPACE CLMDB01.CLMTSPOL
                       SHRLEVEL REFERENCE
                       UNLDDN SYSREC
                       PUNCHDDN SYSPUNCH
                  FROM TABLE CLM.POLICY
                     ( POLICY_NO       POSITION(*) CHAR(12),
                       PREMIUM_AMT     POSITION(*) DECIMAL PACKED )
                       WHEN (STATUS_CODE = 'A')
              """,
            spec -> spec.afterRecipe(cu -> {
                Utility.Block unload = (Utility.Block) cu.getStatements().get(0);
                assertThat(unload.getOperand("SHRLEVEL").getValueText()).isEqualTo("REFERENCE");
                assertThat(unload.getOperand("UNLDDN").getValueText()).isEqualTo("SYSREC");
                assertThat(unload.getBlocks("SELECT")).isEmpty();

                Utility.Block from = unload.getBlocks("FROM").get(0);
                // The field list is written in parentheses over as many cards as it needs, and no
                // word inside them can open an operand.
                assertThat(from.getValueText()).startsWith("TABLE CLM.POLICY ( POLICY_NO");
                assertThat(from.getOperand("WHEN").getValueText()).isEqualTo("(STATUS_CODE = 'A')");
            })
          )
        );
    }

    /**
     * The program name on the EXEC card cannot say which utility runs, so the deck has to.
     */
    @Test
    void dialectIsReadOffTheDeck() {
        rewriteRun(
          utilityCard(
            """
                UNLOAD TABLESPACE CLMDB01.CLMTSPOL
                  SELECT * FROM CLM.POLICY
                  OUTDDN (POLUNL)
              """,
            spec -> spec.afterRecipe(cu ->
              assertThat(Dialect.of(cu.getMarkers())).isEqualTo(Dialect.Kind.HIGH_PERFORMANCE_UNLOAD))
          )
        );
        rewriteRun(
          utilityCard(
            """
                UNLOAD TABLESPACE CLMDB01.CLMTSPOL
                       SHRLEVEL REFERENCE
                  FROM TABLE CLM.POLICY
              """,
            spec -> spec.afterRecipe(cu ->
              assertThat(Dialect.of(cu.getMarkers())).isEqualTo(Dialect.Kind.BASE_UTILITY))
          )
        );
    }

    @Test
    void runstatsWritesItsValueAgainstItsKeyword() {
        rewriteRun(
          utilityCard(
            """
                RUNSTATS TABLESPACE CLMDB01.CLMTSPOL TABLE(ALL) INDEX(ALL)
                RUNSTATS TABLESPACE CLMDB01.CLMTSHST TABLE(ALL) INDEX(ALL)
              """,
            spec -> spec.afterRecipe(cu -> {
                assertThat(cu.getStatements()).hasSize(2);
                Utility.Block runstats = (Utility.Block) cu.getStatements().get(0);
                assertThat(runstats.getOperand("TABLESPACE").getValueText()).isEqualTo("CLMDB01.CLMTSPOL");
                assertThat(runstats.getOperand("TABLE").getValueText()).isEqualTo("(ALL)");
                assertThat(runstats.getOperand("INDEX").getValueText()).isEqualTo("(ALL)");
            })
          )
        );
    }

    @Test
    void templateWithBothKindsOfVariable() {
        rewriteRun(
          utilityCard(
            """
                TEMPLATE HSTCARD
                         DSN '&HLQ..UNLOAD.&DB..&TS..LOADCARD'
                         UNIT SYSDA SPACE (2,1) CYL
                         DISP (NEW,CATLG,CATLG)
                UNLOAD TABLESPACE CLMDB01.CLMTSHST
                  SELECT * FROM CLM.CLAIM_HIST OUTDDN (HSTUNL)
              """,
            spec -> spec.afterRecipe(cu -> {
                Utility.Block template = (Utility.Block) cu.getStatements().get(0);
                assertThat(template.getValueText()).isEqualTo("HSTCARD");
                assertThat(template.getOperand("DSN").getValueText())
                  .isEqualTo("'&HLQ..UNLOAD.&DB..&TS..LOADCARD'");
                // CYL is written after the primary and secondary quantity and belongs to SPACE.
                assertThat(template.getOperand("SPACE").getValueText()).isEqualTo("(2,1) CYL");
                assertThat(template.getOperand("DISP").getValueText()).isEqualTo("(NEW,CATLG,CATLG)");
            })
          )
        );
    }

    /**
     * A semicolon ends a block wherever it is written, but the product also allows one between a
     * SELECT's SQL and its OUTDDN, so it is what follows that closes a block and not the semicolon.
     */
    @Test
    void semicolonInsideASelect() {
        rewriteRun(
          utilityCard(
            """
                UNLOAD TABLESPACE
                DB2 NO COPYDDN COPYIN
                SELECT * FROM MY.TAB;
                OUTDDN (SREC001)
                FORMAT DSNTIAUL
              """,
            spec -> spec.afterRecipe(cu -> {
                assertThat(cu.getStatements()).hasSize(1);
                Utility.Block unload = (Utility.Block) cu.getStatements().get(0);
                assertThat(unload.getOperand("COPYDDN").getValueText()).isEqualTo("COPYIN");
                Utility.Block select = unload.getBlocks("SELECT").get(0);
                assertThat(select.getOperand("FROM").getValueText()).isEqualTo("MY.TAB");
                assertThat(select.getOperand("OUTDDN").getValueText()).isEqualTo("(SREC001)");
            })
          )
        );
    }

    /**
     * A card is a comment because of what is in column one: {@code --} anywhere, and {@code *} only
     * before the first keyword, since after it {@code *} is the whole of a SELECT.
     */
    @Test
    void commentCards() {
        rewriteRun(
          utilityCard(
            """
              * WRITTEN BEFORE THE FIRST KEYWORD, SO A COMMENT
                UNLOAD TABLESPACE CLMDB01.CLMTSPOL
              -- AND THIS ONE ANYWHERE
                  SELECT *
                    FROM CLM.POLICY
                  OUTDDN (POLUNL)
              """,
            spec -> spec.afterRecipe(cu -> {
                assertThat(cu.getStatements()).hasSize(1);
                Utility.Block unload = (Utility.Block) cu.getStatements().get(0);
                assertThat(unload.getBlocks("SELECT").get(0).getValueText()).isEqualTo("*");
            })
          )
        );
    }

    @Test
    void aMemberThatRunsNoUtilityIsNotADeck(@TempDir Path tempDir) throws IOException {
        Path member = tempDir.resolve("PRMCLM01.ctl");
        Files.write(member, "19981130 19980101 A N\n".getBytes(StandardCharsets.UTF_8));

        assertThat(UtilityParser.builder().build().accept(member)).isFalse();
    }

    @Test
    void aSortDeckIsNotADeck(@TempDir Path tempDir) throws IOException {
        Path member = tempDir.resolve("SRTCLM01.ctl");
        Files.write(member, "  SORT FIELDS=(53,4,CH,A,1,10,CH,A)\n".getBytes(StandardCharsets.UTF_8));

        assertThat(UtilityParser.builder().build().accept(member)).isFalse();
    }

    /**
     * A member written on z/OS and moved to a repository keeps the line ending it was moved with, so
     * a deck has to print back through whichever it was.
     */
    @Test
    void printsBackACarriageReturnedDeck(@TempDir Path tempDir) throws IOException {
        String source = "  UNLOAD TABLESPACE CLMDB01.CLMTSPOL\r\n         DB2 NO\r\n" +
                        "    SELECT * FROM CLM.POLICY\r\n    OUTDDN (POLUNL)\r\n";
        Path member = tempDir.resolve("UNLCLM.ctl");
        Files.write(member, source.getBytes(StandardCharsets.UTF_8));

        SourceFile parsed = UtilityParser.builder().build()
          .parseInputs(singletonList(new Parser.Input(member,
            () -> new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8)))),
            null, new InMemoryExecutionContext())
          .findFirst()
          .orElseThrow();

        assertThat(parsed.printAll()).isEqualTo(source);
    }
}
