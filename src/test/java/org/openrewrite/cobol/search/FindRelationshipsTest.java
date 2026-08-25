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

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.cobol.CobolTest;
import org.openrewrite.cobol.table.CobolRelationships.Row;
import org.openrewrite.test.RecipeSpec;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.openrewrite.cobol.Assertions.cobol;
import static org.openrewrite.cobol.Assertions.copybook;
import static org.openrewrite.cobol.table.CobolRelationships.ResourceAction.*;
import static org.openrewrite.cobol.table.CobolRelationships.ResourceType.*;
import static org.openrewrite.controlcard.idcams.Assertions.idcamsCard;
import static org.openrewrite.controlcard.sort.Assertions.sortCard;
import static org.openrewrite.controlm.Assertions.controlM;
import static org.openrewrite.db2.bind.Assertions.bind;
import static org.openrewrite.jcl.Assertions.jcl;
import static org.openrewrite.linkedit.Assertions.linkEdit;
import static org.openrewrite.listload.Assertions.listLoad;

class FindRelationshipsTest extends CobolTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new FindRelationships());
    }

    private static String lineAt(String source, @Nullable Integer line) {
        assertThat(line).isNotNull();
        return source.split("\n", -1)[line - 1];
    }

	@DocumentExample @Test void IC201A() {
        String linkEdit = """
          *
          INCLUDE OBJLIB(IC201A)    MODULE FOO
          *INCLUDE OBJLIB(ABCD02)
          """;
        String bindPackage = """
          BIND PACKAGE(&PROD0.EXT) OWNER(&SBS100S) -                        \s
             QUALIFIER(&SBS100S.EXT) MEMBER(IC201A) -                      \s
             SQLERROR(NOPACKAGE) VALIDATE(BIND) FLAG(I) ISOLATION(CS) -
             RELEASE(COMMIT) EXPLAIN(YES) CURRENTDATA(YES) -          \s
             ACTION(ADD)     -                                        \s
             ENABLE(*)                                                \s
          """;
        String bindPlan = """
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
          """;
        rewriteRun(
          spec -> spec.dataTable(Row.class, rows -> {
              assertThat(rows).extracting(Row::getDependent).contains("IC201A", "BINDCARDPACKAGE", "BINDCARDPLAN");
              assertThat(rows).extracting(Row::getDependency).contains("IC202A", "IC201A", "PROD0.*");
              assertThat(rows).extracting(Row::getAction).contains(CALL, INCLUDE, DEFINES, BINDS);
              // Which library an object is read from is the shop's own business, so the DD name is
              // carried rather than judged. The commented-out card names nothing.
              assertThat(rows).filteredOn(r -> r.getDependentType() == LINKEDIT).singleElement().satisfies(r -> {
                  assertThat(r.getDependency()).isEqualTo("IC201A");
                  assertThat(r.getActionMetadata()).isEqualTo("OBJLIB");
                  assertThat(r.getDependentPath()).isEqualTo("linkeditcards/LINKEDIT1");
                  assertThat(lineAt(linkEdit, r.getDependentLine())).isEqualTo("INCLUDE OBJLIB(IC201A)    MODULE FOO");
              });
              // The plan lists a collection rather than the packages in it, so what it reaches is a
              // wildcard and not yet a program.
              assertThat(rows).filteredOn(r -> r.getDependentType() == BINDPLAN).singleElement().satisfies(r -> {
                  assertThat(r.getDependent()).isEqualTo("LINKEDIT1");
                  assertThat(r.getDependency()).isEqualTo("PROD0.*");
                  assertThat(r.getDependencyType()).isEqualTo(BINDPACKAGE);
                  assertThat(r.getDependentPath()).isEqualTo("bindcards/BINDCARDPLAN");
                  assertThat(lineAt(bindPlan, r.getDependentLine())).contains("BIND PLAN(LINKEDIT1)");
              });
              // DB2 names the package after the DBRM, and the DBRM after the program the precompile
              // read, so this is the one edge that reaches the COBOL.
              assertThat(rows).filteredOn(r -> r.getDependencyType() == DBRM).singleElement().satisfies(r -> {
                  assertThat(r.getDependent()).isEqualTo("EXT.IC201A");
                  assertThat(r.getDependency()).isEqualTo("IC201A");
                  assertThat(r.getDependentPath()).isEqualTo("bindcards/BINDCARDPACKAGE");
                  assertThat(lineAt(bindPackage, r.getDependentLine())).contains("BIND PACKAGE(&PROD0.EXT)");
              });
          }),
          cobol(
            getNistResource("IC201A.CBL"),
            "",
            spec -> spec.after(s -> s).path("IC201A.CBL")
          ),
          linkEdit(linkEdit, spec -> spec.path("linkeditcards/LINKEDIT1")),
          bind(bindPackage, spec -> spec.path("bindcards/BINDCARDPACKAGE")),
          bind(bindPlan, spec -> spec.path("bindcards/BINDCARDPLAN"))
        );
    }

    @Test
    void bindDeckWrittenInAJob() {
        rewriteRun(
          spec -> spec.dataTable(Row.class, rows -> {
              assertThat(rows).filteredOn(r -> r.getDependencyType() == DBRM).singleElement().satisfies(r -> {
                  assertThat(r.getDependent()).isEqualTo("CARDDEMO.CBACT01C");
                  assertThat(r.getDependency()).isEqualTo("CBACT01C");
                  // The anchor is the job's own line, not the deck's, so it points at the card a
                  // reader would open the job to find.
                  assertThat(r.getDependentPath()).isEqualTo("jcl/BINDCARD.jcl");
                  assertThat(r.getDependentLine()).isEqualTo(5);
              });
              assertThat(rows).filteredOn(r -> r.getDependentType() == JCL).singleElement().satisfies(r -> {
                  assertThat(r.getDependent()).isEqualTo("BINDCARD");
                  assertThat(r.getAction()).isEqualTo(DEFINES);
                  assertThat(r.getDependency()).isEqualTo("CARDDEMO.CBACT01C");
              });
          }),
          jcl(
            """
              //BINDCARD JOB (),CLASS=A
              //BIND    EXEC PGM=IKJEFT01
              //SYSTSIN DD *
               DSN SYSTEM(DBC1)
               BIND PACKAGE(CARDDEMO) MEMBER(CBACT01C) -
                    ACTION(REPLACE) ISOLATION(CS)
               END
              /*
              """,
            spec -> spec.path("jcl/BINDCARD.jcl")
          )
        );
    }

    @Test
    void linkEditDeckWrittenInAJob() {
        rewriteRun(
          spec -> spec.dataTable(Row.class, rows -> {
              assertThat(rows).filteredOn(r -> r.getDependencyType() == LOAD_MODULE).singleElement().satisfies(r -> {
                  assertThat(r.getDependent()).isEqualTo("BASE64");
                  assertThat(r.getDependentType()).isEqualTo(JCL);
                  assertThat(r.getDependency()).isEqualTo("BASE64O");
                  // The anchor is the job's own line, not the deck's, so it points at the card a
                  // reader would open the job to find.
                  assertThat(r.getDependentPath()).isEqualTo("jcl/BASE64.jcl");
                  assertThat(r.getDependentLine()).isEqualTo(6);
              });
              assertThat(rows).filteredOn(r -> r.getDependentType() == LOAD_MODULE)
                .extracting(Row::getAction, Row::getDependency)
                .containsExactlyInAnyOrder(tuple(CONTAINS, "BASE64O"), tuple(CONTAINS, "BASE64E"));
          }),
          jcl(
            """
              //BASE64   JOB (),CLASS=A
              //COBOL    EXEC PROC=LINKEDIT,COND=(4,LE)
              //BINDER.SYSLIN DD *
                INCLUDE OBJLIB(BASE64O)
                INCLUDE OBJLIB(BASE64E)
                NAME BASE64O(R)
              /*
              """,
            spec -> spec.path("jcl/BASE64.jcl")
          )
        );
    }

    /**
     * A VSAM file exists because a {@code DEFINE} made it, and the jobs that read it say only that
     * they read it. A sort card yields no such edge: {@code SORTIN} is a DD name, and only the JCL
     * says what it is bound to.
     */
    @Test
    void idcamsDefineNamesTheDataSetItMakes() {
        String define = """
          /* DEFCLM01 - CLAIM MASTER KSDS. */
          DELETE CLM.PROD.CLMMAST CLUSTER PURGE
          SET MAXCC = 0
          DEFINE CLUSTER (NAME(CLM.PROD.CLMMAST)      -
                          INDEXED KEYS(10 0))         -
                 DATA    (NAME(CLM.PROD.CLMMAST.DATA))
          """;
        rewriteRun(
          spec -> spec.dataTable(Row.class, rows -> {
              assertThat(rows).filteredOn(r -> r.getDependencyType() == DATA_SET)
                .extracting(Row::getDependency)
                .containsExactly("CLM.PROD.CLMMAST", "CLM.PROD.CLMMAST.DATA");
              assertThat(rows).filteredOn(r -> r.getDependencyType() == DATA_SET).allSatisfy(r -> {
                  assertThat(r.getDependent()).isEqualTo("DEFCLM01");
                  assertThat(r.getDependentType()).isEqualTo(CONTROL_CARD);
                  assertThat(r.getAction()).isEqualTo(DEFINES);
                  assertThat(r.getDependentPath()).isEqualTo("ctlcard/DEFCLM01.ctl");
                  assertThat(lineAt(define, r.getDependentLine())).contains("DEFINE CLUSTER");
              });
          }),
          idcamsCard(define, spec -> spec.path("ctlcard/DEFCLM01.ctl")),
          sortCard(
            """
              * SRTCLM01 - SORT THE DAILY CLAIM EXTRACT.
                SORT FIELDS=(53,4,CH,A,1,10,CH,A)
                INCLUDE COND=(57,1,CH,EQ,C'O')
              """,
            spec -> spec.path("ctlcard/SRTCLM01.ctl")
          )
        );
    }

    @Test
    void idcamsDeckWrittenInAJob() {
        rewriteRun(
          spec -> spec.dataTable(Row.class, rows ->
            assertThat(rows).filteredOn(r -> r.getDependencyType() == DATA_SET).singleElement().satisfies(r -> {
                assertThat(r.getDependent()).isEqualTo("DEFKSDS");
                assertThat(r.getDependentType()).isEqualTo(JCL);
                assertThat(r.getDependency()).isEqualTo("SYSD.STOCK.HISTORY");
                // The anchor is the job's own line, not the deck's, so it points at the card a
                // reader would open the job to find.
                assertThat(r.getDependentPath()).isEqualTo("jcl/DEFKSDS.jcl");
                assertThat(r.getDependentLine()).isEqualTo(5);
            })),
          jcl(
            """
              //DEFKSDS JOB (P0L1),'STOCK TRADER KSDS',MSGCLASS=A
              //STEPNAME EXEC PGM=IDCAMS
              //SYSPRINT DD   SYSOUT=*
              //SYSIN    DD   *
                  DEFINE CLUSTER (NAME(SYSD.STOCK.HISTORY)                -
                                  INDEXED                                 -
                                  RECSZ(100 100)                          -
                                  KEYS(29 0))
              /*
              """,
            spec -> spec.path("jcl/DEFKSDS.jcl")
          )
        );
    }

    @Test
    void anchorsPointAtTheStatement() {
        String program = """
          000000 IDENTIFICATION DIVISION.
                 PROGRAM-ID.
                     ANCHORS.
                 DATA DIVISION.
                 WORKING-STORAGE SECTION.
                 01 DCL_ANCHOR_TBL_NUM_1 PIC X(3).
                 01 GRP-01.
                     COPY ANCHOR_COPY.
                 EXEC SQL INCLUDE ANCHOR_INCLUDE END-EXEC.
                 EXEC SQL DECLARE ANCHOR_TBL TABLE
                 ( NUM_1                  CHAR(3) NOT NULL
                 ) END-EXEC.
                 PROCEDURE DIVISION.
                     EXEC SQL
                         SELECT NUM_1
                         INTO :DCL_ANCHOR_TBL_NUM_1
                         FROM ANCHOR_TBL
                     END-EXEC.
                     EXEC SQL
                         UPDATE ANCHOR_TBL
                         SET NUM_1 = :DCL_ANCHOR_TBL_NUM_1
                     END-EXEC.
                     CALL "ANCHORED".
          """;
        String include = getNistResource("ANCHOR_INCLUDE.CPY");
        rewriteRun(
          spec -> spec.dataTable(Row.class, rows -> {
              assertThat(rows).filteredOn(r -> r.getAction() == COPY).singleElement().satisfies(r -> {
                  assertThat(r.getDependentPath()).isEqualTo("ANCHORS.CBL");
                  assertThat(lineAt(program, r.getDependentLine())).contains("COPY ANCHOR_COPY");
                  assertThat(r.getDependencyPath()).endsWith("ANCHOR_COPY.CPY");
              });
              assertThat(rows).filteredOn(r -> r.getAction() == INCLUDE && "ANCHORS".equals(r.getDependent()))
                .singleElement().satisfies(r -> {
                    assertThat(r.getDependentPath()).isEqualTo("ANCHORS.CBL");
                    assertThat(lineAt(program, r.getDependentLine())).contains("EXEC SQL INCLUDE ANCHOR_INCLUDE");
                    assertThat(r.getDependencyPath()).endsWith("ANCHOR_INCLUDE.CPY");
                });
              assertThat(rows).filteredOn(r -> r.getAction() == INCLUDE && "ANCHOR_INCLUDE".equals(r.getDependent()))
                .satisfiesExactlyInAnyOrder(
                  r -> {
                      assertThat(r.getDependentPath()).isEqualTo("ANCHOR_INCLUDE.CPY");
                      assertThat(lineAt(include, r.getDependentLine())).contains("EXEC SQL INCLUDE NESTED_INCLUDE");
                      assertThat(r.getDependencyPath()).isNull();
                  },
                  // The same statement seen where it was copied to, which prints nothing there and so is not anchored.
                  r -> {
                      assertThat(r.getDependentPath()).isNull();
                      assertThat(r.getDependentLine()).isNull();
                  });
              assertThat(rows).filteredOn(r -> r.getAction() == ACCESS && "CREATE".equals(r.getActionMetadata()))
                .singleElement().satisfies(r ->
                  assertThat(lineAt(program, r.getDependentLine())).contains("DECLARE ANCHOR_TBL TABLE"));
              assertThat(rows).filteredOn(r -> r.getAction() == ACCESS && "READ".equals(r.getActionMetadata()))
                .singleElement().satisfies(r ->
                  assertThat(lineAt(program, r.getDependentLine())).contains("FROM ANCHOR_TBL"));
              assertThat(rows).filteredOn(r -> r.getAction() == ACCESS && "UPDATE".equals(r.getActionMetadata()))
                .singleElement().satisfies(r ->
                  assertThat(lineAt(program, r.getDependentLine())).contains("UPDATE ANCHOR_TBL"));
              assertThat(rows).filteredOn(r -> r.getAction() == CALL).singleElement().satisfies(r -> {
                  assertThat(lineAt(program, r.getDependentLine())).contains("CALL \"ANCHORED\"");
                  assertThat(r.getDependencyPath()).isNull();
                  assertThat(r.getDependencyLine()).isNull();
              });
          }),
          cobol(program, "", spec -> spec.after(s -> s).path("ANCHORS.CBL")),
          copybook(include, spec -> spec.after(s -> s).path("ANCHOR_INCLUDE.CPY"))
        );
    }

    /**
     * A card can hold more than one BIND, and each one's MEMBERs are those written before the next
     * BIND begins.
     */
    @Test
    void multipleBindsOnOneCard() {
        rewriteRun(
          spec -> spec.dataTable(Row.class, rows -> {
              // Each BIND's MEMBER belongs to that BIND, not to the first one on the card.
              assertThat(rows).filteredOn(r -> r.getDependencyType() == DBRM)
                .extracting(Row::getDependent, Row::getDependency)
                .containsExactlyInAnyOrder(
                  tuple("EXT.FIRSTPGM", "FIRSTPGM"),
                  tuple("EXT.SECONDPGM", "SECONDPGM"));
              assertThat(rows).filteredOn(r -> r.getDependentType() == BINDPLAN).singleElement().satisfies(r -> {
                  assertThat(r.getDependent()).isEqualTo("LINKEDIT9");
                  assertThat(r.getDependency()).isEqualTo("PROD0.*");
              });
          }),
          bind(
            """
              BIND PACKAGE(&PROD0.EXT) OWNER(&SBS100S) -
                 QUALIFIER(&SBS100S.EXT) MEMBER(FIRSTPGM) -
                 ACTION(ADD)
              BIND PACKAGE(&PROD1.EXT) OWNER(&SBS100S) -
                 QUALIFIER(&SBS100S.EXT) MEMBER(SECONDPGM) -
                 ACTION(ADD)
              BIND PLAN(LINKEDIT9) OWNER(SBS100S) -
                 PKLIST(PROD0.*)
              """,
            spec -> spec.path("bindcards/MULTIBIND"))
        );
    }

    /**
     * A load module exists because a link-edit deck named it, and the step that runs it says only that
     * it runs it. What the deck leaves out is an answer too: the reserve calculation is included and so
     * is bound in, while the one CLMB030 calls through a data name is a module of its own.
     */
    @Test
    void aDeckNamesTheLoadModuleAndWhatIsBoundIntoIt() {
        String deck = """
          *  CLMC020 - CLAIM INQUIRY.  CLMU020 IS CALLED STATICALLY.
            INCLUDE SYSLIB(DFHECI)
            INCLUDE OBJLIB(CLMU020)
            ENTRY CLMC020
            ALIAS CLMINQ
            NAME CLMC020(R)
          """;
        rewriteRun(
          spec -> spec.dataTable(Row.class, rows -> {
              assertThat(rows).filteredOn(r -> r.getDependencyType() == LOAD_MODULE)
                .extracting(Row::getDependent, Row::getAction, Row::getDependency)
                .containsExactly(
                  tuple("CLMC020", DEFINES, "CLMC020"),
                  // An alias is a directory entry of its own, so a step naming it finds this module.
                  tuple("CLMC020", DEFINES, "CLMINQ"));
              assertThat(rows).filteredOn(r -> r.getDependentType() == LOAD_MODULE)
                .extracting(Row::getAction, Row::getDependency)
                .containsExactlyInAnyOrder(
                  tuple(ENTRY, "CLMC020"),
                  tuple(CONTAINS, "DFHECI"),
                  tuple(CONTAINS, "CLMU020"));
              assertThat(rows).filteredOn(r -> r.getAction() == INCLUDE).allSatisfy(r ->
                assertThat(lineAt(deck, r.getDependentLine())).contains("INCLUDE " + r.getActionMetadata()));
          }),
          linkEdit(deck, spec -> spec.path("CLMC020.lnk"))
        );
    }

    /**
     * A deck says what a module was meant to hold and a listing says what it holds: the language
     * interface and the runtime the autocall pulled in are in the module and in no deck anybody wrote,
     * so a module's parts are only complete once the listing has been read.
     */
    @Test
    void aListingNamesTheSectionsAModuleHolds() {
        String listing = String.join("\n",
          "1                                          A M B L I S T                        PAGE     1",
          "0                                          ** MODULE SUMMARY **",
          "0",
          "      MEMBER NAME:                  CLMC020",
          "      MAIN ENTRY POINT:             00000000",
          "      LIBRARY:                      DDNAME=CICSLOAD DSNAME=CLM.PROD.CICSLOAD",
          "      MODULE SIZE (HEX):            00003658",
          "0",
          "0                                          ** CONTROL SECTION SUMMARY **",
          "0",
          "      CSECT NAME    ORIGIN    LENGTH    TYPE   AMODE   ENTRY NAME    LOCATION",
          "      CLMC020       00000000  00002F10  SD     31",
          "      DFHECI        00002F10  00000058  SD     31      DFHEI1        00002F18",
          "      CLMU020       00002F68  000006E8  SD     31",
          "");
        rewriteRun(
          spec -> spec.dataTable(Row.class, rows -> {
              assertThat(rows).filteredOn(r -> r.getDependencyType() == CSECT)
                .extracting(Row::getDependent, Row::getAction, Row::getDependency)
                .containsExactly(
                  tuple("CLMC020", CONTAINS, "CLMC020"),
                  tuple("CLMC020", CONTAINS, "DFHECI"),
                  tuple("CLMC020", CONTAINS, "CLMU020"));
              assertThat(rows).filteredOn(r -> r.getAction() == ENTRY).singleElement().satisfies(r ->
                assertThat(r.getDependency()).isEqualTo("CLMC020"));
              assertThat(rows).allSatisfy(r ->
                assertThat(lineAt(listing, r.getDependentLine())).contains(r.getDependency()));
          }),
          listLoad(listing, spec -> spec.path("CICSLOAD.amblist"))
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
        String program = """
          000000 IDENTIFICATION DIVISION.                                         *
                 PROGRAM-ID. IC109A.                                              *
                 DATA DIVISION.                                                   *
                 LINKAGE SECTION.                                                 *
                     01  GRP-01.                                                  *
                         COPY INCEPTION.                                          *
          """;
        rewriteRun(
          spec -> spec.dataTable(Row.class, rows -> {
              assertThat(rows).extracting(Row::getDependent).contains("IC109A", "INCEPTION", "INCEPTION_2");
              assertThat(rows).extracting(Row::getDependentType).contains(COBOL, COPYBOOK);
              assertThat(rows).extracting(Row::getAction).contains(COPY);
              assertThat(rows).extracting(Row::getDependency).contains("INCEPTION", "INCEPTION_2", "INCEPTION_3");
              assertThat(rows).extracting(Row::getDependencyType).contains(COPYBOOK);
              assertThat(rows).filteredOn(r -> r.getDependentType() == COBOL).singleElement().satisfies(r -> {
                  assertThat(r.getDependentPath()).isEqualTo("COPY_IN_COPY.CBL");
                  assertThat(lineAt(program, r.getDependentLine())).contains("COPY INCEPTION.");
              });
              // A copy statement inside a copybook prints nothing in the program, so it has no anchor there.
              assertThat(rows).filteredOn(r -> r.getDependentType() == COPYBOOK).allSatisfy(r -> {
                  assertThat(r.getDependentPath()).isNull();
                  assertThat(r.getDependentLine()).isNull();
              });
          }),
          cobol(program, "", spec -> spec.after(s -> s).path("COPY_IN_COPY.CBL"))
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
        String schedule = """
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
          """;
        rewriteRun(
          spec -> spec.dataTable(Row.class, rows -> {
              assertThat(rows).extracting(Row::getDependent).containsOnly("CTM_SCHEDULE", "JOBNAME1", "JOBNAME2", "JOBNAME3", "JOBNAME4");
              assertThat(rows).extracting(Row::getDependentType).contains(CONTROL_M_SCHEDULE);
              assertThat(rows).extracting(Row::getAction).contains(TRIGGERS);
              assertThat(rows).extracting(Row::getDependency).contains("JCL_JOB", "CTM_SCHEDULE");
              assertThat(rows).extracting(Row::getDependencyType).contains(JCL, CONTROL_M_SCHEDULE);
              assertThat(rows).filteredOn(r -> r.getDependencyType() == JCL).singleElement().satisfies(r -> {
                  assertThat(r.getDependentPath()).isEqualTo("CTM_SCHEDULE.ctms");
                  assertThat(lineAt(schedule, r.getDependentLine())).contains("MEMNAME JCL_JOB");
                  assertThat(r.getDependencyPath()).isNull();
              });
              // A schedule waiting on another names it, so the anchor is on the dependency side.
              assertThat(rows).filteredOn(r -> r.getDependencyType() == CONTROL_M_SCHEDULE).allSatisfy(r -> {
                  assertThat(r.getDependentPath()).isNull();
                  assertThat(r.getDependencyPath()).isEqualTo("CTM_SCHEDULE.ctms");
                  assertThat(lineAt(schedule, r.getDependencyLine())).contains("GROUP_" + r.getDependent() + "_OK");
              });
          }),
          controlM(schedule, spec -> spec.after(s -> s).path("CTM_SCHEDULE.ctms")));
    }
}
