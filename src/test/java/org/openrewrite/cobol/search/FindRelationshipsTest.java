/*
 * Copyright 2025 the original author or authors.
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
package org.openrewrite.cobol.search;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.cobol.CobolTest;
import org.openrewrite.cobol.table.CobolRelationships.Row;
import org.openrewrite.test.RecipeSpec;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.cobol.Assertions.cobol;
import static org.openrewrite.cobol.Assertions.copybook;
import static org.openrewrite.cobol.table.CobolRelationships.ResourceAction.*;
import static org.openrewrite.cobol.table.CobolRelationships.ResourceType.*;
import static org.openrewrite.controlm.Assertions.controlM;
import static org.openrewrite.test.SourceSpecs.text;

class FindRelationshipsTest extends CobolTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new FindRelationships());
    }

	@DocumentExample @Test void IC201A() {
        rewriteRun(
          spec -> spec.dataTable(Row.class, rows -> {
              assertThat(rows).extracting(Row::getDependent).contains("IC201A", "LINKEDIT1", "BINDCARDPACKAGE", "BINDCARDPLAN");
              assertThat(rows).extracting(Row::getDependency).contains("IC202A", "IC201A", "LINKEDIT1");
              assertThat(rows).extracting(Row::getDependencyType).contains(COBOL, COBOL, LINKEDIT);
              assertThat(rows).extracting(Row::getAction).contains(CALL, INCLUDE, PLAN, MEMBER);
          }),
          cobol(
            getNistResource("IC201A.CBL"),
            "",
            spec -> spec.after(s -> s).path("IC201A.CBL")
          ),
          text(
			"""
              *
              INCLUDE OBJLIB(IC201A)    MODULE FOO
              *INCLUDE OBJLIB(ABCD02)
              """,
            spec -> spec.path("linkeditcards/LINKEDIT1")),
          text(
			"""
            BIND PACKAGE(&PROD0.EXT) OWNER(&SBS100S) -                        \s
               QUALIFIER(&SBS100S.EXT) MEMBER(IC201A) -                      \s
               SQLERROR(NOPACKAGE) VALIDATE(BIND) FLAG(I) ISOLATION(CS) -
               RELEASE(COMMIT) EXPLAIN(YES) CURRENTDATA(YES) -          \s
               ACTION(ADD)     -                                        \s
               ENABLE(*)                                                \s
            """,
            spec -> spec.path("bindcards/BINDCARDPACKAGE")),
          text(
			"""
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
            spec -> spec.path("bindcards/BINDCARDPLAN"))
        );
    }

    @Test
    void includeCopybookWithCopyAndInclude() {
        rewriteRun(
          spec -> spec.dataTable(Row.class, rows -> {
              assertThat(rows).extracting(Row::getDependent).contains("USE_COPY_AND_INCLUDE", "EXEC_SQL_INCLUDE");
              assertThat(rows).extracting(Row::getDependentType).contains(COPYBOOK, COBOL);
              assertThat(rows).extracting(Row::getAction).contains(INCLUDE, COPY);
              assertThat(rows).extracting(Row::getDependency).contains("USE_COPY_AND_INCLUDE", "EMPTY_COPY", "EMPTY_INCLUDE");
              assertThat(rows).extracting(Row::getDependencyType).contains(COPYBOOK);
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
              assertThat(rows).extracting(Row::getDependent).contains("DECLARE_TABLE_2", "EXEC_SQL_CREATE");
              assertThat(rows).extracting(Row::getDependentType).contains(COPYBOOK, COBOL);
              assertThat(rows).extracting(Row::getAction).contains(INCLUDE, ACCESS);
              assertThat(rows).extracting(Row::getDependency).contains("DECLARE_PROD_TBL_02", "PROD_TBL_01", "PROD_TBL_02");
              assertThat(rows).extracting(Row::getDependencyType).contains(SQL_TABLE, COPYBOOK);
              assertThat(rows).extracting(Row::getActionMetadata).contains("CREATE");
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
                     EXEC SQL INCLUDE DECLARE_PROD_TBL_02 END-EXEC.
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
              assertThat(rows).extracting(Row::getDependent).contains("DECLARE_TABLE_2", "DECLARE_TABLE_3", "EXEC_SQL_READ");
              assertThat(rows).extracting(Row::getDependentType).contains(COPYBOOK, COBOL);
              assertThat(rows).extracting(Row::getAction).contains(INCLUDE, ACCESS);
              assertThat(rows).extracting(Row::getDependency).contains("DECLARE_TABLE_2", "DECLARE_TABLE_3", "PROD_TBL_02", "PROD_TBL_03");
              assertThat(rows).extracting(Row::getDependencyType).contains(SQL_TABLE, COPYBOOK);
              assertThat(rows).extracting(Row::getActionMetadata).contains("CREATE", "READ");
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
              assertThat(rows).extracting(Row::getDependent).contains("DECLARE_TABLE_2", "EXEC_SQL_UPDATE");
              assertThat(rows).extracting(Row::getDependentType).contains(COPYBOOK, COBOL);
              assertThat(rows).extracting(Row::getAction).contains(INCLUDE, ACCESS);
              assertThat(rows).extracting(Row::getDependency).contains("DECLARE_TABLE_2", "PROD_TBL_02");
              assertThat(rows).extracting(Row::getDependencyType).contains(SQL_TABLE, COPYBOOK);
              assertThat(rows).extracting(Row::getActionMetadata).contains("CREATE", "INSERT", "UPDATE");
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
              assertThat(rows).extracting(Row::getDependent).contains("DECLARE_TABLE_2", "EXEC_SQL_DELETE");
              assertThat(rows).extracting(Row::getDependentType).contains(COPYBOOK, COBOL);
              assertThat(rows).extracting(Row::getAction).contains(INCLUDE, ACCESS);
              assertThat(rows).extracting(Row::getDependency).contains("DECLARE_TABLE_2", "PROD_TBL_02");
              assertThat(rows).extracting(Row::getDependencyType).contains(SQL_TABLE, COPYBOOK);
              assertThat(rows).extracting(Row::getActionMetadata).contains("CREATE", "DELETE");
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
              assertThat(rows).extracting(Row::getDependent).contains("IC109A", "INCEPTION", "INCEPTION_2");
              assertThat(rows).extracting(Row::getDependentType).contains(COBOL, COPYBOOK);
              assertThat(rows).extracting(Row::getAction).contains(COPY);
              assertThat(rows).extracting(Row::getDependency).contains("INCEPTION", "INCEPTION_2", "INCEPTION_3");
              assertThat(rows).extracting(Row::getDependencyType).contains(COPYBOOK);
          }),
          cobol(
            """
              000000 IDENTIFICATION DIVISION.                                         *
                     PROGRAM-ID. IC109A.                                              *
                     DATA DIVISION.                                                   *
                     LINKAGE SECTION.                                                 *
                         01  GRP-01.                                                  *
                             COPY INCEPTION.                                          *
              """,
			"", spec -> spec.after(s -> s).path("COPY_IN_COPY.CBL")
          )
        );
    }

    @Test
    void execSqlCreateCursor() {
        // Ensure cursors are not detected as SQL tables.
        rewriteRun(
          spec -> spec.dataTable(Row.class, rows -> {
              assertThat(rows).extracting(Row::getDependent).contains("DECLARE_TABLE_2", "EXEC_SQL_CREATE");
              assertThat(rows).extracting(Row::getDependentType).contains(COPYBOOK, COBOL);
              assertThat(rows).extracting(Row::getAction).contains(INCLUDE, ACCESS);
              assertThat(rows).extracting(Row::getDependency).contains("DECLARE_PROD_TBL_02", "PROD_TBL_01", "PROD_TBL_02");
              assertThat(rows).extracting(Row::getDependencyType).contains(SQL_TABLE, COPYBOOK);
              assertThat(rows).extracting(Row::getActionMetadata).contains("CREATE", "READ");
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
                     EXEC SQL INCLUDE DECLARE_PROD_TBL_02 END-EXEC.
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
              assertThat(rows).extracting(Row::getDependent).contains("DECLARE_TABLE_2", "EXEC_SQL_READ");
              assertThat(rows).extracting(Row::getDependentType).contains(COPYBOOK, COBOL);
              assertThat(rows).extracting(Row::getAction).contains(INCLUDE, ACCESS);
              assertThat(rows).extracting(Row::getDependency).contains("DECLARE_TABLE_2", "PROD_TBL_02");
              assertThat(rows).extracting(Row::getDependencyType).contains(SQL_TABLE, COPYBOOK);
              assertThat(rows).extracting(Row::getActionMetadata).contains("CREATE", "READ");
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
              assertThat(rows).extracting(Row::getDependent).containsOnly("SM206A");
              assertThat(rows).extracting(Row::getDependency)
                .containsExactly(IntStream.range(1, 10).mapToObj(n -> "KP00" + n).toArray(String[]::new));
              assertThat(rows).extracting(Row::isDependencyMissing).containsOnly(false);
              assertThat(rows).extracting(Row::getDependencyType).containsOnly(COPYBOOK);
              assertThat(rows).extracting(Row::getAction).containsOnly(COPY);
          }),
          cobol(
            getNistResource("SM206A.CBL"),
            "",
            spec -> spec.after(s -> s).path("SM206A.CBL")
          ));
    }

    @Test
    void controlMScheduleToJcl() {
        rewriteRun(
          spec -> spec.dataTable(Row.class, rows -> {
              assertThat(rows).extracting(Row::getDependent).containsOnly("CTM_SCHEDULE", "JOBNAME1", "JOBNAME2", "JOBNAME3", "JOBNAME4");
              assertThat(rows).extracting(Row::getDependentType).contains(CONTROL_M_SCHEDULE);
              assertThat(rows).extracting(Row::getAction).contains(TRIGGERS);
              assertThat(rows).extracting(Row::getDependency).contains("JCL_JOB", "CTM_SCHEDULE");
              assertThat(rows).extracting(Row::getDependencyType).contains(JCL, CONTROL_M_SCHEDULE);
          }),
          controlM(
            """
              +---------------------------------- BROWSE -----------------------------------+
              | MEMNAME JCL_JOB         MEMLIB LIB_NAME                                     |
              | OWNER                  TASKTYPE         PREVENT-NCT2 DFLT                   |
              | APPL                                    GROUP                               |
              | DESC                                                                        |
              |                                                                             |
              | OVERLIB                                                   STAT CAL          |
              | SCHENV                         SYSTEM ID                  NJE NODE          |
              | SET VAR                                                                     |
              | CTB STEP AT         NAME            TYPE                                    |
              | DOCMEM                 DOCLIB                                               |
              | =========================================================================== |
              | SCHEDULE RBC                                                                |
              | RELATIONSHIP (AND/OR) O                                                     |
              | DAYS                                                          DCAL          |
              |                                                                    AND/OR   |
              |                                                                             |
              | WDAYS   0                                                     WCAL          |
              | MONTHS  1- Y 2- Y 3- Y 4- Y 5- Y 6- Y 7- Y 8- Y 9- Y 10- Y 11- Y 12- Y      |
              | DATES                                                                       |
              | CONFCAL          SHIFT       RETRO N MAXWAIT 70  D-CAT                      |
              | MINIMUM          PDS                                                        |
              | DEFINITION ACTIVE FROM          UNTIL                                       |
              | =========================================================================== |
              | IN  GROUP_JOBNAME1_OK  ODAT   GROUP_JOBNAME2_OK  ODAT                       |
              |     GROUP_JOBNAME3_OK  ODAT - GROUP_JOBNAME4_OK  ODAT +                     |
              | CONTROL                                                                     |
              | RESOURCE INIT5                0001          UNICPAL              0001       |
              |          STOPALL              0001          DB2S                 0001       |
              |                                                                             |
              | FROM TIME         +     DAYS    UNTIL TIME      +     DAYS                  |
              | DUE OUT TIME      +     DAYS    PRIORITY NN  SAC    CONFIRM                 |
              | TIME ZONE:                                                                  |
              | =========================================================================== |
              | OUT  GROUP_JOBNAME1_OK  ODAT  GROUP_JOBNAME2_OK  ODAT                       |
              |      GROUP_JOBNAME3_OK  ODAT  GROUP_JOBNAME4_OK  ODAT                       |
              | AUTO-ARCHIVE Y          SYSDB    Y      MAXDAYS      MAXRUNS                |
              | SYSOUT OP   (C,D,F,N,R)                                              FROM   |
              | MAXRERUN      RERUNMEM                                                      |
              | CAPTURE BY   (W - WORD / C - CHAR)                                          |
              | CYCLIC TYPE: C                                   INTERVAL         FROM      |
              | INTERVAL SEQUENCE: +         +         +         +         +                |
              | SPECIFIC TIMES:                                             TOLERANCE       |
              |                       +           +           +           +           +     |
              |                                                                             |
              | STEP RANGE         FR (PGM.PROC)          .          TO          .          |
              | ON PGMST ANYSTEP  PROCST          CODES <C0005                        A/O   |
              |   DO OK                                                                     |
              | ON PGMST ANYSTEP  PROCST          CODES NOTOK                         A/O   |
              |   DO IFRERUN  FROM $EXERR   .          TO          .              CONFIRM N |
              | ON PGMST ANYSTEP  PROCST          CODES OK                            A/O   |
              |   DO SYSOUT   OPT  C PRM C                                            FRM   |
              |   DO SYSOUT   OPT  R PRM                                              FRM   |
              |   DO                                                                        |
              | ON PGMST          PROCST          CODES                               A/O   |
              |   DO                                                                        |
              | ON SYSOUT                                          FROM     TO        A/O   |
              |   DO                                                                        |
              | ON VAR                                                                      |
              |   DO                                                                        |
              | SHOUT WHEN NOTOK    TIME       +     DAYS      TO U-MECO           URGN R   |
              |   MS %%JOBNAME ENDED NOTOK!   SUPPORT => H_247  -- J69                      |
              | SHOUT WHEN          TIME       +     DAYS      TO                  URGN     |
              |   MS                                                                        |
              | =========================================================================== |
              | APPL TYPE                                  APPL VER                         |
              | APPL FORM                                  CM   VER                         |
              | INSTREAM JCL: N                                                             |
              |                                                                             |
              """,
			spec -> spec.after(s -> s).path("CTM_SCHEDULE.ctms")
          ));
    }
}
