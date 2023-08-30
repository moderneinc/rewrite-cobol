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
import static org.openrewrite.cobol.Assertions.*;
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
            BIND PACKAGE(&PROD0.EXT) OWNER(&SBS100S) -                        \s
               QUALIFIER(&SBS100S.EXT) MEMBER(IC201A) -                      \s
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
    void includeCopybookWithCopyAndInclude() {
        rewriteRun(
          spec -> spec.dataTable(Row.class, rows -> {
              assertThat(rows.stream().map(Row::getDependent)).contains("USE_COPY_AND_INCLUDE", "EXEC_SQL_INCLUDE");
              assertThat(rows.stream().map(Row::getDependentType)).contains(COPYBOOK, COBOL);
              assertThat(rows.stream().map(Row::getAction)).contains(INCLUDE, COPY);
              assertThat(rows.stream().map(Row::getDependency)).contains("USE_COPY_AND_INCLUDE", "EMPTY_COPY", "EMPTY_INCLUDE");
              assertThat(rows.stream().map(Row::getDependencyType)).contains(COPYBOOK);
          }),
          cobol(
            """
              000000 IDENTIFICATION DIVISION.
                     PROGRAM-ID.
                         EXEC_SQL_INCLUDE.
                     DATA DIVISION.
                     WORKING-STORAGE SECTION.
                     01 FILLER PIC X(10) VALUE 'PGM WORKING-STORAGE: EXEC_SQL_INCLUDE'
                     .
                         EXEC SQL DECLARE PROD_TBL_01 TABLE
                         ( NUM_1                  CHAR(3) NOT NULL,
                           NUM_2                  CHAR(3) NOT NULL
                         ) END-EXEC.
                     EXEC SQL INCLUDE USE_COPY_AND_INCLUDE END-EXEC.
              """,
            spec -> spec.after(s -> s).path("EXEC_SQL_INCLUDE.CBL")
          ),
          copybook(
            """
              000000 COPY EMPTY_COPY.
              000000 EXEC SQL INCLUDE EMPTY_INCLUDE END-EXEC.
              """,
            spec -> spec.after(s -> s).path("USE_COPY_AND_INCLUDE.CPY")
          )
        );
    }

    @Test
    void execSqlCreateTable() {
        rewriteRun(
          spec -> spec.dataTable(Row.class, rows -> {
              assertThat(rows.stream().map(Row::getDependent)).contains("DECLARE_TABLE_2", "EXEC_SQL_CREATE");
              assertThat(rows.stream().map(Row::getDependentType)).contains(COPYBOOK, COBOL);
              assertThat(rows.stream().map(Row::getAction)).contains(INCLUDE, ACCESS);
              assertThat(rows.stream().map(Row::getDependency)).contains("DECLARE_TABLE_2", "PROD_TBL_01", "PROD_TBL_02");
              assertThat(rows.stream().map(Row::getDependencyType)).contains(SQL_TABLE, COPYBOOK);
              assertThat(rows.stream().map(Row::getActionMetadata)).contains("CREATE");
          }),
          cobol(
            """
              000000 IDENTIFICATION DIVISION.
                     PROGRAM-ID.
                         EXEC_SQL_CREATE.
                     DATA DIVISION.
                     WORKING-STORAGE SECTION.
                     01 FILLER PIC X(10) VALUE 'PGM WORKING-STORAGE: EXEC_SQL_CREATE'.
              
                    * Create SQL table in the COBOL source.
                    *    EXEC SQL statement to declare a table
                         EXEC SQL DECLARE PROD_TBL_01 TABLE
                         ( NUM_1                  CHAR(3) NOT NULL,
                           NUM_2                  CHAR(3) NOT NULL
                         ) END-EXEC.

                    * Include SQL table from another COBOL source.
                    * These SQL tables are created through copybooks.
                     EXEC SQL INCLUDE DECLARE_TABLE_2 END-EXEC.
              """,
            spec -> spec.after(s -> s).path("EXEC_SQL_CREATE_TABLE.CBL")
          ),
          copybook(
            """
              000000*    EXEC SQL statement to declare a table
                         EXEC SQL DECLARE PROD_TBL_02 TABLE
                         ( NUM_1                  CHAR(3) NOT NULL,
                           NUM_2                  CHAR(3) NOT NULL
                         ) END-EXEC.
              """,
            spec -> spec.after(s -> s).path("DECLARE_TABLE_2.CPY")
          )
        );
    }

    @Test
    void execSqlReadTable() {
        rewriteRun(
          spec -> spec.dataTable(Row.class, rows -> {
              assertThat(rows.stream().map(Row::getDependent)).contains("DECLARE_TABLE_2", "DECLARE_TABLE_3", "EXEC_SQL_READ");
              assertThat(rows.stream().map(Row::getDependentType)).contains(COPYBOOK, COBOL);
              assertThat(rows.stream().map(Row::getAction)).contains(INCLUDE, ACCESS);
              assertThat(rows.stream().map(Row::getDependency)).contains("DECLARE_TABLE_2", "DECLARE_TABLE_3", "PROD_TBL_02", "PROD_TBL_03");
              assertThat(rows.stream().map(Row::getDependencyType)).contains(SQL_TABLE, COPYBOOK);
              assertThat(rows.stream().map(Row::getActionMetadata)).contains("CREATE", "READ");
          }),
          cobol(
            """
              000000 IDENTIFICATION DIVISION.
                     PROGRAM-ID.
                         EXEC_SQL_READ.
                     DATA DIVISION.
                     WORKING-STORAGE SECTION.
                     01 FILLER PIC X(10) VALUE 'PGM WORKING-STORAGE: EXEC_SQL_READ'.
                     01 DCL_PROD_TBL_02_NUM_1 PIC X(3).

                    * Include SQL table from another COBOL source.
                    * These SQL tables are created through copybooks.
                     EXEC SQL INCLUDE DECLARE_TABLE_2 END-EXEC.
                     EXEC SQL INCLUDE DECLARE_TABLE_3 END-EXEC.

                     EXEC SQL
                         SELECT COUNT(*)
                         INTO :DCL_PROD_TBL_02_NUM_1
                         FROM PROD_TBL_02
                         WHERE EXISTS (
                             SELECT *
                             FROM PROD_TBL_03
                         )
                     END-EXEC.
              """,
            spec -> spec.after(s -> s).path("EXEC_SQL_READ.CBL")
          ),
          copybook(
            """
              000000*    EXEC SQL statement to declare a table
                         EXEC SQL DECLARE PROD_TBL_02 TABLE
                         ( NUM_1                  CHAR(3) NOT NULL,
                           NUM_2                  CHAR(3) NOT NULL
                         ) END-EXEC.
              """,
            spec -> spec.after(s -> s).path("DECLARE_TABLE_2.CPY")
          ),
          copybook(
            """
              000000*    EXEC SQL statement to declare a table
                         EXEC SQL DECLARE PROD_TBL_03 TABLE
                         ( NUM_1                  CHAR(3) NOT NULL,
                           NUM_2                  CHAR(3) NOT NULL
                         ) END-EXEC.
                     01 DCL_PROD_TBL_03_NUM_2 PIC X(3).
                     EXEC SQL
                         SELECT COUNT(*)
                         INTO :DCL_PROD_TBL_03_NUM_2
                         FROM PROD_TBL_03
                     END-EXEC.
              """,
            spec -> spec.after(s -> s).path("DECLARE_TABLE_3.CPY")
          )
        );
    }

    @Test
    void execSqlUpdateTable() {
        rewriteRun(
          spec -> spec.dataTable(Row.class, rows -> {
              assertThat(rows.stream().map(Row::getDependent)).contains("DECLARE_TABLE_2", "EXEC_SQL_UPDATE");
              assertThat(rows.stream().map(Row::getDependentType)).contains(COPYBOOK, COBOL);
              assertThat(rows.stream().map(Row::getAction)).contains(INCLUDE, ACCESS);
              assertThat(rows.stream().map(Row::getDependency)).contains("DECLARE_TABLE_2", "PROD_TBL_02");
              assertThat(rows.stream().map(Row::getDependencyType)).contains(SQL_TABLE, COPYBOOK);
              assertThat(rows.stream().map(Row::getActionMetadata)).contains("CREATE", "INSERT", "UPDATE");
          }),
          cobol(
            """
              000000 IDENTIFICATION DIVISION.
                     PROGRAM-ID.
                         EXEC_SQL_UPDATE.
                     DATA DIVISION.
                     WORKING-STORAGE SECTION.
                     01 FILLER PIC X(10) VALUE 'PGM WORKING-STORAGE: EXEC_SQL_UPDATE'.
                     01 DCL_PROD_TBL_02_NUM_1 PIC X(3).
                     01 DCL_PROD_TBL_02_NUM_2 PIC X(3).
              
                    * Include SQL table from another COBOL source.
                    * These SQL tables are created through copybooks.
                     EXEC SQL INCLUDE DECLARE_TABLE_2 END-EXEC.

                     EXEC SQL
                         UPDATE PROD_TBL_02
                         SET NUM_1 = :DCL_PROD_TBL_02_NUM_1
                     END-EXEC.

                     EXEC SQL
                         INSERT INTO PROD_TBL_02
                                (NUM_1,
                                 NUM_2)
                         VALUES (:DCL_PROD_TBL_02_NUM_1
                                 :DCL_PROD_TBL_02_NUM_2)
                     END-EXEC.
              """,
            spec -> spec.after(s -> s).path("EXEC_SQL_UPDATE.CBL")
          ),
          copybook(
            """
              000000*    EXEC SQL statement to declare a table
                         EXEC SQL DECLARE PROD_TBL_02 TABLE
                         ( NUM_1                  CHAR(3) NOT NULL,
                           NUM_2                  CHAR(3) NOT NULL
                         ) END-EXEC.
                         
                         01 DCL_PROD_TBL_02_NUM_1_CPY PIC X(3).
                         EXEC SQL
                            UPDATE PROD_TBL_02
                            SET NUM_1 = :DCL_PROD_TBL_02_NUM_1_CPY
                         END-EXEC.
              """,
            spec -> spec.after(s -> s).path("DECLARE_TABLE_2.CPY")
          )
        );
    }

    @Test
    void execSqlDeleteTable() {
        rewriteRun(
          spec -> spec.dataTable(Row.class, rows -> {
              assertThat(rows.stream().map(Row::getDependent)).contains("DECLARE_TABLE_2", "EXEC_SQL_DELETE");
              assertThat(rows.stream().map(Row::getDependentType)).contains(COPYBOOK, COBOL);
              assertThat(rows.stream().map(Row::getAction)).contains(INCLUDE, ACCESS);
              assertThat(rows.stream().map(Row::getDependency)).contains("DECLARE_TABLE_2", "PROD_TBL_02");
              assertThat(rows.stream().map(Row::getDependencyType)).contains(SQL_TABLE, COPYBOOK);
              assertThat(rows.stream().map(Row::getActionMetadata)).contains("CREATE", "DELETE");
          }),
          cobol(
            """
              000000 IDENTIFICATION DIVISION.
                     PROGRAM-ID.
                         EXEC_SQL_DELETE.
                     DATA DIVISION.
                     WORKING-STORAGE SECTION.
                     01 FILLER PIC X(10) VALUE 'PGM WORKING-STORAGE: EXEC_SQL_DELETE'.
                     01 DCL_PROD_TBL_02_NUM_1 PIC X(3).
              
                    * Include SQL table from another COBOL source.
                    * These SQL tables are created through copybooks.
                     EXEC SQL INCLUDE DECLARE_TABLE_2 END-EXEC.

                     EXEC SQL
                         DELETE FROM PROD_TBL_02
                         WHERE NUM_1 = :DCL_PROD_TBL_02_NUM_1
                     END-EXEC.
              """,
            spec -> spec.after(s -> s).path("EXEC_SQL_DELETE.CBL")
          ),
          copybook(
            """
              000000*    EXEC SQL statement to declare a table
                         EXEC SQL DECLARE PROD_TBL_02 TABLE
                         ( NUM_1                  CHAR(3) NOT NULL,
                           NUM_2                  CHAR(3) NOT NULL
                         ) END-EXEC.
                         
                         01 DCL_PROD_TBL_02_NUM_1_CPY PIC X(3).
                         EXEC SQL
                            DELETE FROM PROD_TBL_02
                            WHERE NUM_1 = :DCL_PROD_TBL_02_NUM_1_CPY
                         END-EXEC.
              """,
            spec -> spec.after(s -> s).path("DECLARE_TABLE_2.CPY")
          )
        );
    }

    @Test
    void aCopyInACopy() {
        rewriteRun(
          spec -> spec.dataTable(Row.class, rows -> {
              assertThat(rows.stream().map(Row::getDependent)).contains("IC109A", "INCEPTION", "INCEPTION_2");
              assertThat(rows.stream().map(Row::getDependentType)).contains(COBOL, COPYBOOK);
              assertThat(rows.stream().map(Row::getAction)).contains(COPY);
              assertThat(rows.stream().map(Row::getDependency)).contains("INCEPTION", "INCEPTION_2", "INCEPTION_3");
              assertThat(rows.stream().map(Row::getDependencyType)).contains(COPYBOOK);
          }),
          cobol(
            """
              000000 IDENTIFICATION DIVISION.                                         *
                     PROGRAM-ID. IC109A.                                              *
                     DATA DIVISION.                                                   *
                     LINKAGE SECTION.                                                 *
                         01  GRP-01.                                                  *
                             COPY INCEPTION.                                          *
              """, "", spec -> spec.after(s -> s).path("COPY_IN_COPY.CBL")
          )
        );
    }

    @Test
    void execSqlCreateCursor() {
        // Ensure cursors are not detected as SQL tables.
        rewriteRun(
          spec -> spec.dataTable(Row.class, rows -> {
              assertThat(rows.stream().map(Row::getDependent)).contains("DECLARE_TABLE_2", "EXEC_SQL_CREATE");
              assertThat(rows.stream().map(Row::getDependentType)).contains(COPYBOOK, COBOL);
              assertThat(rows.stream().map(Row::getAction)).contains(INCLUDE, ACCESS);
              assertThat(rows.stream().map(Row::getDependency)).contains("DECLARE_TABLE_2", "PROD_TBL_01", "PROD_TBL_02");
              assertThat(rows.stream().map(Row::getDependencyType)).contains(SQL_TABLE, COPYBOOK);
              assertThat(rows.stream().map(Row::getActionMetadata)).contains("CREATE", "READ");
          }),
          cobol(
            """
              000000 IDENTIFICATION DIVISION.
                     PROGRAM-ID.
                         EXEC_SQL_CREATE.
                     DATA DIVISION.
                     WORKING-STORAGE SECTION.
                     01 FILLER PIC X(10) VALUE 'PGM WORKING-STORAGE: EXEC_SQL_CREATE'.
              
                    * Create SQL table in the COBOL source.
                    *    EXEC SQL statement to declare a table
                         EXEC SQL DECLARE PROD_TBL_01 TABLE
                         ( NUM_1                  CHAR(3) NOT NULL,
                           NUM_2                  CHAR(3) NOT NULL
                         ) END-EXEC.
              
                    * Create cursors for tables
                    * Cursor for table 1
                     EXEC SQL
                         DECLARE CURSOR_1 CURSOR FOR
                         SELECT NUM_1,
                                NUM_2
                         FROM PROD_TBL_01
                         FOR FETCH ONLY
                     END-EXEC.

                    * Include SQL table from another COBOL source.
                    * These SQL tables are created through copybooks.
                     EXEC SQL INCLUDE DECLARE_TABLE_2 END-EXEC.
              """,
            spec -> spec.after(s -> s).path("EXEC_SQL_CREATE.CBL")
          ),
          copybook(
            """
              000000*    EXEC SQL statement to declare a table
                         EXEC SQL DECLARE PROD_TBL_02 TABLE
                         ( NUM_1                  CHAR(3) NOT NULL,
                           NUM_2                  CHAR(3) NOT NULL
                         ) END-EXEC.
                    * Create cursor for table 2
                         EXEC SQL
                             DECLARE CURSOR_IN_COPY CURSOR FOR
                             SELECT NUM_1,
                                    NUM_2
                             FROM PROD_TBL_02
                             FOR FETCH ONLY
                         END-EXEC.
              """,
            spec -> spec.after(s -> s).path("DECLARE_TABLE_2.CPY")
          )
        );
    }

    @Test
    void execSqlReadCursor() {
        // Ensure cursors are not detected as SQL tables.
        rewriteRun(
          spec -> spec.dataTable(Row.class, rows -> {
              assertThat(rows.stream().map(Row::getDependent)).contains("DECLARE_TABLE_2", "EXEC_SQL_READ");
              assertThat(rows.stream().map(Row::getDependentType)).contains(COPYBOOK, COBOL);
              assertThat(rows.stream().map(Row::getAction)).contains(INCLUDE, ACCESS);
              assertThat(rows.stream().map(Row::getDependency)).contains("DECLARE_TABLE_2", "PROD_TBL_02");
              assertThat(rows.stream().map(Row::getDependencyType)).contains(SQL_TABLE, COPYBOOK);
              assertThat(rows.stream().map(Row::getActionMetadata)).contains("CREATE", "READ");
          }),
          cobol(
            """
              000000 IDENTIFICATION DIVISION.
                     PROGRAM-ID.
                         EXEC_SQL_READ.
                     DATA DIVISION.
                     WORKING-STORAGE SECTION.
                     01 FILLER PIC X(10) VALUE 'PGM WORKING-STORAGE: EXEC_SQL_READ'.
                     01 DCL_PROD_TBL_02_NUM_1 PIC X(3).

                    * Include SQL table from another COBOL source.
                    * These SQL tables are created through copybooks.
                     EXEC SQL INCLUDE DECLARE_TABLE_2 END-EXEC.

                     EXEC SQL
                         DECLARE CURSOR_1 CURSOR FOR
                         SELECT NUM_1,
                                NUM_2
                         FROM DECLARE_TABLE_2
                         FOR FETCH ONLY
                     END-EXEC.

                     EXEC SQL
                         SELECT NUM_1
                         INTO :DCL_PROD_TBL_02_NUM_1
                         FROM CURSOR_1
                     END-EXEC.
              """,
            spec -> spec.after(s -> s).path("EXEC_SQL_READ.CBL")
          ),
          copybook(
            """
              000000*    EXEC SQL statement to declare a table
                         EXEC SQL DECLARE PROD_TBL_02 TABLE
                         ( NUM_1                  CHAR(3) NOT NULL,
                           NUM_2                  CHAR(3) NOT NULL
                         ) END-EXEC.

                     01 DCL_PROD_TBL_02_NUM_1_CRS PIC X(3).

                     EXEC SQL
                         DECLARE CURSOR_IN_COPY CURSOR FOR
                         SELECT NUM_1,
                                NUM_2
                         FROM PROD_TBL_02
                         FOR FETCH ONLY
                     END-EXEC.

                     EXEC SQL
                         SELECT NUM_1
                         INTO :DCL_PROD_TBL_02_NUM_1_CRS
                         FROM CURSOR_IN_COPY
                     END-EXEC.
              """,
            spec -> spec.after(s -> s).path("DECLARE_TABLE_2.CPY")
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
