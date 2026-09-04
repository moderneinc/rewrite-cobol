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
package org.openrewrite.mainframe.controlcard.utility.trait;

import org.junit.jupiter.api.Test;
import org.openrewrite.mainframe.controlcard.utility.marker.Dialect;
import org.openrewrite.test.RewriteTest;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.mainframe.controlcard.utility.Assertions.utilityCard;

class UnloadCommandTest implements RewriteTest {

    @Test
    void whatAnUnloadReadsAndWhereItPutsIt() {
        rewriteRun(
          utilityCard(
            """
                GLOBAL
                       SQLID CLM
                       SELMSG NUMBERED ;
                UNLOAD TABLESPACE CLMDB01.CLMTSHST
                       DB2 NO
                       LOCK YES
                       QUIESCE YES
                  SELECT CLAIM_NO, POLICY_NO
                    FROM CLM.CLAIM_HIST
                   WHERE POSTED_DATE > '2001-12-31'
                  OUTDDN (HSTUNL)
                  FORMAT VARIABLE ALL
                  LOADDDN HSTLOAD
              """,
            spec -> spec.afterRecipe(cu -> {
                UnloadCommand unload = new UnloadCommand.Matcher().lower(cu).findFirst().orElseThrow();
                assertThat(unload.getDialect()).isEqualTo(Dialect.Kind.HIGH_PERFORMANCE_UNLOAD);
                assertThat(unload.getTableSpace()).isEqualTo("CLMDB01.CLMTSHST");
                assertThat(unload.getTables()).containsExactly("CLM.CLAIM_HIST");
                assertThat(unload.getOutputDdNames()).containsExactly("HSTUNL");
                assertThat(unload.getLoadDdNames()).containsExactly("HSTLOAD");
                assertThat(unload.getFormats()).containsExactly("VARIABLE ALL");
                assertThat(unload.getDb2()).isEqualTo("NO");
                assertThat(unload.getLine()).isEqualTo(4);
                assertThat(unload.toString()).isEqualTo("UNLOAD CLMDB01.CLMTSHST");
            })
          )
        );
    }

    /**
     * One pass over the table space fanning out to three files. Each SELECT carries its own output
     * DD and its own layout, which is what the base utility has no answer to.
     */
    @Test
    void severalSelectsFanOutToSeveralFiles() {
        rewriteRun(
          utilityCard(
            """
                UNLOAD TABLESPACE CLMDB01.CLMTSHST
                       DB2 NO
                  SELECT CLAIM_NO FROM CLM.CLAIM_HIST WHERE STATUS_CODE = 'O'
                  OUTDDN (OPENCLM)
                  FORMAT DSNTIAUL
                  SELECT CLAIM_NO FROM CLM.CLAIM_HIST WHERE STATUS_CODE = 'C'
                  OUTDDN (CLOSCLM)
                  FORMAT DSNTIAUL
                  LOADDDN HSTCARD
                  SELECT CLAIM_NO FROM CLM.CLAIM_HIST WHERE STATUS_CODE = 'D'
                  OUTDDN (DENYCLM)
                  FORMAT DELIMITED SEP ','
              """,
            spec -> spec.afterRecipe(cu -> {
                UnloadCommand unload = new UnloadCommand.Matcher().lower(cu).findFirst().orElseThrow();
                assertThat(unload.getSelects()).hasSize(3);
                assertThat(unload.getOutputDdNames()).containsExactly("OPENCLM", "CLOSCLM", "DENYCLM");
                assertThat(unload.getLoadDdNames()).containsExactly("HSTCARD");
                assertThat(unload.getFormats()).containsExactly("DSNTIAUL", "DSNTIAUL", "DELIMITED");
            })
          )
        );
    }

    @Test
    void baseUtilityUnloadReadsTheSameQuestions() {
        rewriteRun(
          utilityCard(
            """
                UNLOAD TABLESPACE CLMDB01.CLMTSPOL
                       SHRLEVEL REFERENCE
                       UNLDDN SYSREC
                       PUNCHDDN SYSPUNCH
                  FROM TABLE CLM.POLICY
                     ( POLICY_NO POSITION(*) CHAR(12) )
                       WHEN (STATUS_CODE = 'A')
              """,
            spec -> spec.afterRecipe(cu -> {
                UnloadCommand unload = new UnloadCommand.Matcher().lower(cu).findFirst().orElseThrow();
                assertThat(unload.getDialect()).isEqualTo(Dialect.Kind.BASE_UTILITY);
                assertThat(unload.getTables()).containsExactly("CLM.POLICY");
                assertThat(unload.getOutputDdNames()).containsExactly("SYSREC");
                assertThat(unload.getLoadDdNames()).containsExactly("SYSPUNCH");
                assertThat(unload.getShrLevel()).isEqualTo("REFERENCE");
                // The base utility's defaults are published, so nothing here is a site setting.
                assertThat(unload.getInheritedKeywords()).isEmpty();
            })
          )
        );
    }

    /**
     * A keyword a deck leaves out is not a default a reader may assume: it is whatever the parmlib
     * member on INFPLIB says today, and that member is in no application library.
     */
    @Test
    void whatIsCodedAndWhatComesFromTheSite() {
        rewriteRun(
          utilityCard(
            """
                UNLOAD TABLESPACE CLMDB01.CLMTSPOL
                  SELECT * FROM CLM.POLICY
                  OUTDDN (POLDFT)
              """,
            spec -> spec.afterRecipe(cu -> {
                UnloadCommand unload = new UnloadCommand.Matcher().lower(cu).findFirst().orElseThrow();
                assertThat(unload.isCoded("FORMAT")).isFalse();
                assertThat(unload.isCoded("DB2")).isFalse();
                assertThat(unload.getInheritedKeywords())
                  .containsExactly("FORMAT", "DB2", "QUIESCE", "LOCK");
            })
          )
        );
        rewriteRun(
          utilityCard(
            """
                GLOBAL DB2 NO ;
                UNLOAD TABLESPACE CLMDB01.CLMTSPOL
                       LOCK YES
                       QUIESCE YES
                  SELECT * FROM CLM.POLICY
                  OUTDDN (POLUNL)
                  FORMAT DSNTIAUL
              """,
            spec -> spec.afterRecipe(cu -> {
                UnloadCommand unload = new UnloadCommand.Matcher().lower(cu).findFirst().orElseThrow();
                // DB2 is written once, in the GLOBAL block, and governs every unload after it.
                assertThat(unload.isCoded("DB2")).isTrue();
                assertThat(unload.getDb2()).isEqualTo("NO");
                // Nothing that decides what the file holds is left to the site.
                assertThat(unload.getInheritedKeywords()).isEmpty();
            })
          )
        );
        rewriteRun(
          utilityCard(
            """
                UNLOAD TABLESPACE CLMDB01.CLMTSPOL
                  SELECT * FROM CLM.POLICY
                  OUTDDN (POLUNL)
                  FORMAT DSNTIAUL ;
                GLOBAL DB2 NO ;
                UNLOAD TABLESPACE CLMDB01.CLMTSHST
                  SELECT * FROM CLM.CLAIM_HIST
                  OUTDDN (HSTUNL)
                  FORMAT DSNTIAUL
              """,
            spec -> spec.afterRecipe(cu -> {
                List<UnloadCommand> unloads = new UnloadCommand.Matcher().lower(cu).collect(toList());
                // A GLOBAL governs what follows it, so the unload written above it takes DB2 from the
                // site and the one below it does not.
                assertThat(unloads.get(0).isCoded("DB2")).isFalse();
                assertThat(unloads.get(0).getDb2()).isNull();
                assertThat(unloads.get(1).isCoded("DB2")).isTrue();
                assertThat(unloads.get(1).getDb2()).isEqualTo("NO");
            })
          )
        );
    }

    /**
     * An image copy is neither locked nor quiesced, so a deck that reads one codes neither keyword
     * and the rows come from a DD rather than from the table space.
     */
    @Test
    void unloadFromAnImageCopy() {
        rewriteRun(
          utilityCard(
            """
                UNLOAD TABLESPACE CLMDB01.CLMTSHST
                       DB2 NO
                       COPYDDN HSTCOPY
                  SELECT *
                    FROM CLM.CLAIM_HIST
                  OUTDDN (HSTIMG)
                  FORMAT DSNTIAUL
              """,
            spec -> spec.afterRecipe(cu -> {
                UnloadCommand unload = new UnloadCommand.Matcher().lower(cu).findFirst().orElseThrow();
                assertThat(unload.getCopyDdName()).isEqualTo("HSTCOPY");
                assertThat(unload.getLock()).isNull();
                assertThat(unload.getQuiesce()).isNull();
                // Neither is left to the site either: the product rejects both here, so the deck
                // is saying so rather than saying nothing.
                assertThat(unload.getInheritedKeywords()).isEmpty();
            })
          )
        );
    }

    /**
     * The same deck asking for the rows through Db2 instead. COPYDDN_STRICT in the site parmlib
     * decides whether that is rejected or the copy is dropped and the live table unloaded, so which
     * data set is read is a site setting, and a live table is locked and quiesced by one too.
     */
    @Test
    void unloadOfAnImageCopyAskedForThroughDb2() {
        rewriteRun(
          utilityCard(
            """
                UNLOAD TABLESPACE CLMDB01.CLMTSHST
                       DB2 FORCE
                       COPYDDN HSTCOPY
                  SELECT *
                    FROM CLM.CLAIM_HIST
                  OUTDDN (HSTIMG)
                  FORMAT DSNTIAUL
              """,
            spec -> spec.afterRecipe(cu -> {
                UnloadCommand unload = new UnloadCommand.Matcher().lower(cu).findFirst().orElseThrow();
                assertThat(unload.getCopyDdName()).isEqualTo("HSTCOPY");
                assertThat(unload.getDb2()).isEqualTo("FORCE");
                assertThat(unload.getInheritedKeywords()).containsExactly("QUIESCE", "LOCK");
            })
          )
        );
    }

    /**
     * A SELECT is part of the unload that holds it, so only the statements of the deck are matched.
     */
    @Test
    void everyStatementOfADeck() {
        rewriteRun(
          utilityCard(
            """
                TEMPLATE HSTCARD DSN 'CLM.PROD.&DB..LOADCARD'
                GLOBAL SQLID CLM ;
                UNLOAD TABLESPACE CLMDB01.CLMTSHST
                  SELECT * FROM CLM.CLAIM_HIST OUTDDN (HSTUNL)
              """,
            spec -> spec.afterRecipe(cu -> {
                List<String> statements = new UtilityStatement.Matcher().lower(cu)
                  .map(UtilityStatement::toString).collect(toList());
                assertThat(statements).containsExactly(
                  "TEMPLATE HSTCARD", "GLOBAL", "UNLOAD CLMDB01.CLMTSHST");
            })
          )
        );
    }

    @Test
    void runstatsIsAStatementToo() {
        rewriteRun(
          utilityCard(
            """
                RUNSTATS TABLESPACE CLMDB01.CLMTSPOL TABLE(ALL) INDEX(ALL)
                RUNSTATS TABLESPACE CLMDB01.CLMTSHST TABLE(ALL) INDEX(ALL)
              """,
            spec -> spec.afterRecipe(cu -> {
                List<UtilityStatement> statements = new UtilityStatement.Matcher().lower(cu)
                  .collect(toList());
                assertThat(statements).hasSize(2);
                assertThat(statements.get(0).getVerb()).isEqualTo("RUNSTATS");
                assertThat(statements.get(0).getObject()).isEqualTo("CLMDB01.CLMTSPOL");
                assertThat(statements.get(0).getKeywords())
                  .containsExactly("TABLESPACE", "TABLE", "INDEX");
                assertThat(statements.get(1).getLine()).isEqualTo(2);
                assertThat(new UnloadCommand.Matcher().lower(cu)).isEmpty();
            })
          )
        );
    }
}
