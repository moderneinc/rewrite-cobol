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
package org.openrewrite.mainframe.sas.trait;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.text.PlainText;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.openrewrite.test.SourceSpecs.text;

class SqlQueryTest implements RewriteTest {

    /**
     * INTERLINKS 21.5. Which side of the connection a name is on is what a lineage recipe needs, and
     * the source says: inside the parentheses of {@code FROM CONNECTION TO DB2} is a DB2 table, and
     * outside is a data set of a SAS library that no DB2 catalog has heard of.
     */
    @DocumentExample
    @Test
    void tellsATableReadThroughAConnectionFromADataSetOfASasLibrary() {
        rewriteRun(
          text(
            """
              PROC SQL;
                 CONNECT TO DB2 (SSID=&DB2SSN);
                 CREATE TABLE CLMSAS.POLACT AS
                    SELECT * FROM CONNECTION TO DB2
                      (SELECT POLICY_NO AS POLICYNO
                         FROM CLM.POLICY_ACTIVE
                        ORDER BY POLICY_NO);
                 DISCONNECT FROM DB2;
              QUIT;

              PROC SQL;
                 CREATE TABLE CLMSAS.POLEXP AS
                    SELECT P.POLICYNO, SUM(C.AMTRSV) AS RESERVED
                      FROM CLMSAS.POLACT P,
                           CLMSAS.CLMDAY C
                     WHERE P.POLICYNO = C.POLICYNO;
              QUIT;
              """,
            spec -> spec.path("sas/CLMSPOL.sas").afterRecipe(cu ->
              assertThat(tables(cu)).extracting(SqlQuery.Table::getName, SqlQuery.Table::getDbms,
                  SqlQuery.Table::getLine)
                .containsExactly(
                  tuple("CLM.POLICY_ACTIVE", "DB2", 6),
                  tuple("CLMSAS.POLACT", null, 14),
                  tuple("CLMSAS.CLMDAY", null, 15)))
          )
        );
    }

    /**
     * {@code DISCONNECT FROM DB2} names the connection, not a table. Read as a {@code FROM} list it
     * reports a table called DB2 in every program that closes one.
     */
    @Test
    void readsNoTableOnAConnectStatement() {
        rewriteRun(
          text(
            """
              PROC SQL;
                 CONNECT TO DB2 (SSID=DB2P);
                 DISCONNECT FROM DB2;
              QUIT;
              """,
            spec -> spec.path("sas/CLMSPOL.sas").afterRecipe(cu -> assertThat(tables(cu)).isEmpty())
          )
        );
    }

    /**
     * A {@code FROM} outside a {@code PROC SQL} step is not SQL at all. The step's {@code QUIT;} ends
     * it, and what stands after one is in no step.
     */
    @Test
    void readsNoTableOutsideAProcSqlStep() {
        rewriteRun(
          text(
            """
              PROC SQL;
              QUIT;

              PROC PRINT DATA=CLMSAS.POLEXP;
                 VAR POLICYNO;
              RUN;

              SELECT AMTRSV FROM CLMSAS.CLMDAY;
              """,
            spec -> spec.path("sas/CLMSPOL.sas").afterRecipe(cu -> assertThat(tables(cu)).isEmpty())
          )
        );
    }

    private static List<SqlQuery.Table> tables(PlainText cu) {
        List<SqlQuery.Table> tables = new ArrayList<>();
        for (SqlQuery.Query query : new SqlQuery.Matcher().require(cu, null).getQueries()) {
            tables.addAll(query.getTables());
        }
        return tables;
    }
}
