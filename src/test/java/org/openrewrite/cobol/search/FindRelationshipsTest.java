/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.cobol.search;

import org.junit.jupiter.api.Test;
import org.openrewrite.cobol.CobolTest;
import org.openrewrite.cobol.table.CobolRelationships.Row;
import org.openrewrite.test.RecipeSpec;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.cobol.Assertions.cobol;
import static org.openrewrite.cobol.Assertions.preprocessor;
import static org.openrewrite.cobol.table.CobolRelationships.ResourceAction.*;
import static org.openrewrite.cobol.table.CobolRelationships.ResourceType.*;
import static org.openrewrite.test.SourceSpecs.text;

public class FindRelationshipsTest extends CobolTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new FindRelationships());
    }

    @Test
    void IC201A() {
        rewriteRun(
          spec -> spec.dataTable(Row.class, rows -> {
              assertThat(rows.stream().map(Row::getDependent)).contains("IC201A", "LINKEDIT1", "BINDCARDPACKAGE", "BINDCARDPLAN");
              assertThat(rows.stream().map(Row::getDependency)).contains("IC202A", "IC201A", "LINKEDIT1");
              assertThat(rows.stream().map(Row::getDependencyType)).contains(COBOL, COBOL, LINKEDIT);
              assertThat(rows.stream().map(Row::getAction)).contains(CALL, INCLUDE, PLAN, MEMBER);
          }),
          cobol(
            getNistResource("IC201A.CBL"),
            "",
            spec -> spec.after(s -> s).path("IC201A.CBL")
          ),
          text("""
              *
              INCLUDE OBJLIB(IC201A)    MODULE FOO
              *INCLUDE OBJLIB(ABCD02)
              """,
            (spec) -> spec.path("linkeditcards/LINKEDIT1")),
          text("""
            BIND PACKAGE(PROD0) OWNER(SBS100S) -                        \s
               QUALIFIER(SBS100S) MEMBER(IC201A) -                      \s
               SQLERROR(NOPACKAGE) VALIDATE(BIND) FLAG(I) ISOLATION(CS) -
               RELEASE(COMMIT) EXPLAIN(YES) CURRENTDATA(YES) -          \s
               ACTION(ADD)     -                                        \s
               ENABLE(*)                                                \s
            """,
            (spec) -> spec.path("bindcards/BINDCARDPACKAGE")),
          text("""
              BIND PLAN(LINKEDIT1) OWNER(SBS100S) -            \s
                 QUALIFIER(SBS100S) -                         \s
                 PKLIST(PROD0.*)  -                           \s
                 VALIDATE(BIND)        -                      \s
                 FLAG(I) ISOLATION(CS) -                      \s
                 CACHESIZE(0) -                               \s
                 ACQUIRE(USE) -                               \s
                 RELEASE(COMMIT) EXPLAIN(YES) CURRENTDATA(YES) -
                 ACTION(REPLACE) RETAIN -                     \s
                 ENABLE(*)    \s
              """,
            (spec) -> spec.path("bindcards/BINDCARDPLAN"))
        );
    }

    @Test
    void execSql() {
        rewriteRun(
          spec -> spec.dataTable(Row.class, rows -> {
              assertThat(rows.stream().map(Row::getDependent)).contains("DECLARE_TABLE_2", "DECLARE_TABLE_3", "EXEC_SQL");
              assertThat(rows.stream().map(Row::getDependency)).contains("PROD_TBL_01", "PROD_TBL_02", "PROD_TBL_03");
              assertThat(rows.stream().map(Row::getDependentType)).contains(COPYBOOK, COBOL);
              assertThat(rows.stream().map(Row::getDependencyType)).contains(SQL_TABLE);
              assertThat(rows.stream().map(Row::getAction)).contains(DECLARE, INCLUDE);
          }),
          cobol(
            """
              000000 IDENTIFICATION DIVISION.
                     PROGRAM-ID.
                         EXEC_SQL.
                     DATA DIVISION.
                     WORKING-STORAGE SECTION.
                     01 FILLER PIC X(10) VALUE 'PGM WORKING-STORAGE: EXEC_SQL_PROD_2'.
              
                    * Create SQL table in the COBOL source.
                    *    EXEC SQL statement to declare a table
                         EXEC SQL DECLARE PROD_TBL_01 TABLE
                         ( NUM_1                  CHAR(3) NOT NULL,
                           NUM_2                  CHAR(5) NOT NULL,
                           CREATED_DATE           DATE NOT NULL
                         ) END-EXEC.
              
                    * Include SQL table from another COBOL source.
                    * These SQL tables are created through copybooks.
                     EXEC SQL INCLUDE DECLARE_TABLE_2 END-EXEC.
                     EXEC SQL INCLUDE DECLARE_TABLE_3 END-EXEC.
              
                    * Create cursors for tables
                    * Cursor for table 1
                     EXEC SQL
                         DECLARE CURSOR_1 CURSOR FOR
                         SELECT NUM_1,
                                NUM_2
                         FROM PROD_TBL_01
                         FOR FETCH ONLY
                     END-EXEC.
              
                    * Cursor for table 2
                     EXEC SQL
                         DECLARE CURSOR_2 CURSOR FOR
                         SELECT NUM_1,
                                NUM_2
                         FROM PROD_TBL_02
                         FOR FETCH ONLY
                     END-EXEC.
              
                    * Cursor for table 3
                     EXEC SQL
                         DECLARE CURSOR_3 CURSOR FOR
                         SELECT NUM_1,
                                NUM_2
                         FROM PROD_TBL_03
                         FOR FETCH ONLY
                     END-EXEC.
              """,
            spec -> spec.after(s -> s).path("EXEC_SQL.CBL")
          ),
          preprocessor(
            """
              000000*    EXEC SQL statement to declare a table
                         EXEC SQL DECLARE PROD_TBL_02 TABLE
                         ( NUM_1                  CHAR(3) NOT NULL,
                           NUM_2                  CHAR(5) NOT NULL,
                           CREATED_DATE           DATE NOT NULL
                         ) END-EXEC.
              """,
            spec -> spec.after(s -> s).path("DECLARE_TABLE_2.CPY")
          ),
          preprocessor(
            """
              000000*    EXEC SQL statement to declare a table
                         EXEC SQL DECLARE PROD_TBL_03 TABLE
                         ( NUM_1                  CHAR(3) NOT NULL,
                           NUM_2                  CHAR(5) NOT NULL,
                           CREATED_DATE           DATE NOT NULL
                         ) END-EXEC.
                    * Create cursor for table 3
                         EXEC SQL
                             DECLARE CURSOR_IN_COPY CURSOR FOR
                             SELECT NUM_1,
                                    NUM_2
                             FROM PROD_TBL_03
                             FOR FETCH ONLY
                         END-EXEC.
              """,
            spec -> spec.after(s -> s).path("DECLARE_TABLE_3.CPY")
          )
        );
    }

    @Test
    void SM206A() {
        rewriteRun(
          spec -> spec.dataTable(Row.class, rows -> {
              assertThat(rows.stream().map(Row::getDependent)).containsOnly("SM206A");
              assertThat(rows.stream().map(Row::getDependency))
                .containsExactly(IntStream.range(1, 10).mapToObj(n -> "KP00" + n).toArray(String[]::new));
              assertThat(rows.stream().map(Row::isDependencyMissing)).containsOnly(false);
              assertThat(rows.stream().map(Row::getDependencyType)).containsOnly(COPYBOOK);
              assertThat(rows.stream().map(Row::getAction)).containsOnly(COPY);
          }),
          cobol(
            getNistResource("SM206A.CBL"),
            "",
            spec -> spec.after(s -> s).path("SM206A.CBL")
          ));
    }
}
