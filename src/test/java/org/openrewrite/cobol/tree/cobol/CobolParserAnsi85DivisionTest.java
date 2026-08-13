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
package org.openrewrite.cobol.tree.cobol;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.jupiter.ExpectedToFail;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Issue;
import org.openrewrite.cobol.CobolIsoVisitor;
import org.openrewrite.cobol.CobolTest;
import org.openrewrite.cobol.tree.Cobol;
import org.openrewrite.internal.StringUtils;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.cobol.Assertions.cobol;
import static org.openrewrite.test.RewriteTest.toRecipe;

class CobolParserAnsi85DivisionTest extends CobolTest {

    @ExpectedToFail
    @Issue("https://github.com/openrewrite/rewrite-cobol/issues/17")
    @Test
    void invalidGrammar() {
        rewriteRun(
          cobol(
            "000001 IDENTIFICATION."
          )
        );
    }

    @Issue("https://github.com/openrewrite/rewrite-cobol/issues/35")
    @Test
    void shortColumnLength() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION  DIVISION.
              0002
              000300 PROGRAM-ID    . HELLO.
              000400 PROCEDURE DIVISION.
              0005
              000600 STOP RUN.
              """
          )
        );
    }

    @Issue("https://github.com/openrewrite/rewrite-cobol/issues/24")
    @Test
    void emptyIndicatorArea() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION  DIVISION.
              000200
              000300 PROGRAM-ID    . HELLO.
              000400 PROCEDURE DIVISION.
              000500
              000600 STOP RUN.
              """
          )
        );
    }

    @Issue("https://github.com/openrewrite/rewrite-cobol/issues/31")
    @Test
    void startTriggerInComment() {
        rewriteRun(
          cobol(
            """
              000000 IDENTIFICATION DIVISION.
                     PROGRAM-ID. HELLO.
                    *DATE_COMPILED.
                    *REMARKS.
                     DATA DIVISION.
                         WORKING-STORAGE SECTION.
                             77 X PIC 99.                                             C_AREA.05
                             77 Y PIC 99.                                             C_AREA.06
                             77 Z PIC 99.                                             C_AREA.07
              """
          )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
      // Empty line.
      "",
      // Empty indicator.
      "000000",
      // Partial content.
      "000000       ",
      // Empty comment.
      """
        000000/
              ******************************************************************
              ******************************************************************
              /
        """,
      // Trigger stop in comment.
      """
        000000*DATE_COMPILED.
              *REMARKS.
              *DATA DIVISION.
              *    WORKING-STORAGE SECTION.
        """,
      // Comment block.
      """
        000000* Comments
              * Comments
              * Comments
              * Comments
        """
    })
    void afterCommentEntry(String afterCommentEntry) {
        rewriteRun(
          cobol(
            """
              000000 IDENTIFICATION DIVISION.
                     PROGRAM-ID. HELLO.
                     AUTHOR.  MODERNE.
              %s
                     DATA DIVISION.
                         WORKING-STORAGE SECTION.
                             77 X PIC 99.                                             C_AREA.05
                             77 Y PIC 99.                                             C_AREA.06
                             77 Z PIC 99.                                             C_AREA.07
              """.formatted(StringUtils.trimIndent(afterCommentEntry))
          )
        );
    }

    @Issue("https://github.com/openrewrite/rewrite-cobol/issues/29")
    @Test
    void cblRent() {
        rewriteRun(
          cobol(
            """
                     CBL RENT
                     CBL ADATA
                     CBL DBCS
              000001 IDENTIFICATION  DIVISION .                                       C_AREA.01
              000002 PROGRAM-ID    . HELLO     .                                      C_AREA.02
              000003 PROCEDURE DIVISION.                                              C_AREA.03
              000004 DISPLAY 'Hello world!'.                                          C_AREA.04
              000005 STOP RUN.                                                        C_AREA.05
              """
          )
        );
    }

    @Issue("https://github.com/openrewrite/rewrite-cobol/issues/33")
    @Test
    void specialRegister() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.
                     PROGRAM-ID. HELLO.
                     PROCEDURE DIVISION.
                     SET NAME TO ADDRESS OF NEW-NAME.
                     STOP RUN.
              """
          )
        );
    }

    @Test
    void helloWorld() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION  DIVISION .                                       C_AREA.01
              000002 PROGRAM-ID    . HELLO     .                                      C_AREA.02
              000003 PROCEDURE DIVISION.                                              C_AREA.03
              000004 DISPLAY 'Hello world!'.                                          C_AREA.04
              000005 STOP RUN.                                                        C_AREA.05
              """
          )
        );
    }

    @Test
    void customWhitespace() {
        rewriteRun(
          cobol(
            """
                    * The commas between the preprocessor directives are commas and not whitespace.
                     CBL RENT,ADATA,DBCS
                    * Use IBM spec whitespace characters with delimiter before new lines.
              000001 IDENTIFICATION , DIVISION. ,
              000002 PROGRAM-ID. ; HELLO. ;
              000003 PROCEDURE DIVISION.
                         NAME SECTION 01.
                    * Trailing comma whitespace.
                             CLOSE NAME01     ,
                    * Trailing semi-colon whitespace.
                                   NAME02     ;
                    * Trailing comma
                                   NAME03.
              000005 STOP RUN.
              """
          )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
      """
        000000* conditionNameSubscriptReference.
                PROCEDURE DIVISION.
                EVALUATE IDENTIFIER (ALL,ALL,ALL,).
        """,
      """
        000000* dataValueClause.
                DATA DIVISION.
                WORKING-STORAGE SECTION.
                    77 VALUE IS IDENTIFIER,IDENTIFIER,IDENTIFIER,IDENTIFIER.
        """,
      """
        000000* displayStatement.
                PROCEDURE DIVISION.
                DISPLAY IDENTIFIER,IDENTIFIER,IDENTIFIER,IDENTIFIER.
        """,
      """
        000000* fileDescriptionEntry.
                DATA DIVISION.
                FILE SECTION.
                FD IDENTIFIER ,VALUE OF IDENTIFIER ZERO
                              ,VALUE OF IDENTIFIER ZERO
                              ,VALUE OF IDENTIFIER ZERO.
        """,
      """
        000000* functionCallArguments.
                DATA DIVISION.
                REPORT SECTION.
                RD REPORT-01.
                    77 SUM FUNCTION INTEGER (ZERO,ZERO,ZERO,).
        """,
      """
        000000* pictureChars.
               DATA DIVISION.
               WORKING-STORAGE SECTION.
                 02  FILLER PIC Z1,Z2,Z3,.
        """,
      """
        000000* reportGroupSumClause.
               DATA DIVISION.
               REPORT SECTION.
               RD REPORT-01.
                   77 SUM IDENTIFIER,IDENTIFIER,IDENTIFIER UPON
                   IDENTIFIER,.
        """,
      """
        000000* stringSendingPhrase.
                PROCEDURE DIVISION.
                STRING IDENTIFIER,IDENTIFIER,IDENTIFIER,IDENTIFIER
                        FOR IDENTIFIER
                        INTO IDENTIFIER.
        """,
      """
        000000* tableCallSubscripts.
                DATA DIVISION.
                REPORT SECTION.
                RD REPORT-01.
                    77 SUM IDENTIFIER (ALL,ALL,ALL,).
        """,
    })
    void commaDelimiters(String input) {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.
              000002 PROGRAM-ID. HELLO-WORLD.
              %s
              """.formatted(input),
            spec -> spec.afterRecipe(cu -> {
                var count = new AtomicInteger();
                new CobolIsoVisitor<AtomicInteger>() {
                    @Override
                    public Cobol.Word visitWord(Cobol.Word word, AtomicInteger atomicInteger) {
                        if (",".equals(word.getWord())) {
                            atomicInteger.incrementAndGet();
                        }
                        return super.visitWord(word, atomicInteger);
                    }
                }.visit(cu, count);
                assertThat(count.get()).as("Comma count").isEqualTo(3);
            })
          )
        );
    }

    @Issue("https://github.com/openrewrite/rewrite-cobol/issues/45")
    @ParameterizedTest
    @ValueSource(strings = {
      """
        000000 01  LOG-HDR-4.                                                   00000000
                       02  FILLER PIC X VALUE                                   00000000
              -        'this is a variant of the continuation of a literal      00000000
              -        'and will print without issues if detected correctly'.   00000000
        """,
      """
        000000 01  LOG-HDR-4.                                                   00000000
                       02  FILLER PIC X VALUE                           "this is00000000
              -        "another variant of a continuation".                     00000000
        """,
      """
        000000 01  LOG-HDR-4.                                                   00000000
                       02  FILLER PIC X VALUE                          "this is"00000000
              -        ""another variant of a continuation".                    00000000
        """,
      """
        000000 01  LOG-HDR-4.                                                   00000000
                       02  FILLER PIC X VALUE                                   00000000
              -        'A''line                                              one00000000
              -        'line                                               two.'00000000
              -        .                                                        00000000
        """,
      """
        000000 01  LOG-HDR-4.                                                   00000000
                       02  FILLER PIC X VALUE                                   00000000
              -        'line 1                                            today'00000000
              -        ''s date                                          line 2 00000000
              -        'line 3'.                                                00000000
        """,
      """
        000000 01  LOG-HDR-4.                                                   00000000
                       02  FILLER PIC X VALUE  "********************************00000000
              -        "**************".                                        00000000
        """,
      """
        000000 01  LOG-HDR-4.                                                   00000000
                   02  FILLER PIC X VALUE  "************************************00000000
              -    "**************".                                            00000000
                   02  FILLER PIC X VALUE  "************************************00000000
              -    "**************".                                            00000000
        """
    })

    void continuedLiterals(String continuation) {
        rewriteRun(
          cobol("""
            000000* The continuation tests assert a literal was not split into      00000000
                  * multiple tokens.                                                00000000
                   IDENTIFICATION DIVISION.                                         00000000
                   PROGRAM-ID.                                                      00000000
                       CM101M.                                                      00000000
                   DATA DIVISION.                                                   00000000
                   WORKING-STORAGE SECTION.                                         00000000
            %s
                       02  FILLER PIC X(11) VALUE ALL "-".                          00000000
                       02  FILLER PIC X VALUE SPACES.                               00000000
            """.formatted(StringUtils.trimIndent(continuation))
          )
        );
    }

    @Issue("https://github.com/openrewrite/rewrite-cobol/issues/45")
    @Test
    void continuedWords() {
        rewriteRun(
          cobol(
			"""
            000100 IDENTIFICATION DIVISION.                                         00000000
                   PROGRAM-ID.                                                      00000000
                       CM101M.                                                      00000000
                   DATA DIVISION.                                                   00000000
                   WORKING-STORAGE SECTION.                                         00000000
                   77  WS-TEST-12-DATA                                              00000000
                                          PIC S9(                                   00000000
                  -                              6)V9(6).                           00000000
                   77  PROCEDURE                                                    00000000
                  -    DIVISION PICTURE X.                                          00000000
                   77  CONT-                                                        00000000
                  -         A             PIC                                       00000000
                  -                          TURE X(10) VAL                         00000000
                  -                                        UE               "GOVERNM00000000
                  -    "ENT".                                                       00000000
            """
          )
        );
    }

    @Test
    void eject() {
        rewriteRun(
          cobol(
            """
              000000 IDENTIFICATION DIVISION .                                       \s
                     PROGRAM-ID . HELLO-WORLD .                                      \s
                     DATA DIVISION .                                                 \s
                         WORKING-STORAGE SECTION .                                   \s
                         EJECT                                                       \s
                             77 X PIC 99.                                            \s
                             77 Y PIC 99.                                            \s
                             77 Z PIC 99.                                            \s
                     PROCEDURE DIVISION .                                            \s
                         SET X TO 10 .                                               \s
                         SET Y TO 25 .                                               \s
                         ADD X Y GIVING Z .                                          \s
                         DISPLAY "X + Y = "Z .                                       \s
                     STOP RUN .                                                      \s
              """
          )
        );
    }

    @Test
    void exec() {
        rewriteRun(
          cobol(
            """
              000000 IDENTIFICATION DIVISION .                                       \s
                     PROGRAM-ID . HELLO-WORLD .                                      \s
                     DATA DIVISION .                                                 \s
                         WORKING-STORAGE SECTION .                                   \s
                         EXEC CICS 01 END-EXEC                                       \s
                         EXEC SQL 01 END-EXEC                                        \s
                         EXEC SQLIMS 01 END-EXEC                                     \s
                             77 X PIC 99.                                            \s
                             77 Y PIC 99.                                            \s
                             77 Z PIC 99.                                            \s
                     PROCEDURE DIVISION .                                            \s
                         SET X TO 10 .                                               \s
                         SET Y TO 25 .                                               \s
                         ADD X Y GIVING Z .                                          \s
                         DISPLAY "X + Y = "Z .                                       \s
                     STOP RUN .                                                      \s
              """
          )
        );
    }

    /**
     * The command level interface to IMS. Unlike EXEC CICS and EXEC SQL it has no dedicated LST
     * node; it is preserved as a preprocessor EXEC statement attached to the word that follows it.
     */
    @Test
    void execDli() {
        rewriteRun(
          cobol(
            """
              000000 IDENTIFICATION DIVISION.                                        \s
              000000 PROGRAM-ID. DLIPGM.                                             \s
              000000 PROCEDURE DIVISION.                                             \s
              000000     EXEC DLI GU USING PCB(1) SEGMENT(ACCOUNT) INTO(WS-REC)      \s
              000000          END-EXEC.                                              \s
              000000     EXEC DLI ISRT USING PCB(2) SEGMENT(HISTORY) FROM(WS-REC)    \s
              000000          END-EXEC.                                              \s
              000000     GOBACK.                                                     \s
              """
          )
        );
    }

    @Test
    void skip() {
        rewriteRun(
          cobol(
            """
              000000 IDENTIFICATION DIVISION .                                       \s
                     PROGRAM-ID . HELLO-WORLD .                                      \s
                     DATA DIVISION .                                                 \s
                         WORKING-STORAGE SECTION .                                   \s
                         SKIP3                                                       \s
                             77 X PIC 99.                                            \s
                             77 Y PIC 99.                                            \s
                             77 Z PIC 99.                                            \s
                     PROCEDURE DIVISION .                                            \s
                         SET X TO 10 .                                               \s
                         SET Y TO 25 .                                               \s
                         ADD X Y GIVING Z .                                          \s
                         DISPLAY "X + Y = "Z .                                       \s
                     STOP RUN .                                                      \s
              """
          )
        );
    }

    @Test
    void title() {
        rewriteRun(
          cobol(
            """
              000000 IDENTIFICATION DIVISION .                                       \s
                     PROGRAM-ID . HELLO-WORLD .                                      \s
                     DATA DIVISION .                                                 \s
                         WORKING-STORAGE SECTION .                                   \s
                         TITLE 01                                                    \s
                             77 X PIC 99.                                            \s
                             77 Y PIC 99.                                            \s
                             77 Z PIC 99.                                            \s
                     PROCEDURE DIVISION .                                            \s
                         SET X TO 10 .                                               \s
                         SET Y TO 25 .                                               \s
                         ADD X Y GIVING Z .                                          \s
                         DISPLAY "X + Y = "Z .                                       \s
                     STOP RUN .                                                      \s
              """
          )
        );
    }

    @Test
    void arithmetic() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION .                                        C_AREA.01
              000002 PROGRAM-ID . HELLO-WORLD .                                       C_AREA.02
              000003 DATA DIVISION .                                                  C_AREA.03
              000004     WORKING-STORAGE SECTION .                                    C_AREA.04
              000005         77 X PIC 99.                                             C_AREA.05
              000006         77 Y PIC 99.                                             C_AREA.06
              000007         77 Z PIC 99.                                             C_AREA.07
              000008 PROCEDURE DIVISION .                                             C_AREA.08
              000009     SET X TO 10 .                                                C_AREA.09
              000010     SET Y TO 25 .                                                C_AREA.10
              000011     ADD X Y GIVING Z .                                           C_AREA.11
              000012     DISPLAY "X + Y = "Z .                                        C_AREA.12
              000013 STOP RUN .                                                       C_AREA.13
              """
          )
        );
    }

    @Test
    void commaDelimitedDisplayStatements() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.
              000002 PROGRAM-ID . HELLO-WORLD.
              000008 PROCEDURE DIVISION.
              000012     DISPLAY 'N1',NAME1.
              000012     DISPLAY 'N2',NAME2.
              000012     DISPLAY 'N2',NAME3.
              000013 STOP RUN.
              """
          )
        );
    }

    @Test
    void environmentDivision() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID.                                                      C_AREA.02
              000003     IC109A.                                                      C_AREA.03
              000004 ENVIRONMENT DIVISION.                                            C_AREA.04
              000005 CONFIGURATION SECTION.                                           C_AREA.05
              000006 SOURCE-COMPUTER.                                                 C_AREA.06
              000007     XXXXX082.                                                    C_AREA.07
              000008 OBJECT-COMPUTER.                                                 C_AREA.08
              000009     XXXXX083                                                     C_AREA.09
              000010     MEMORY SIZE XXXXX068 CHARACTERS                              C_AREA.10
              000011     PROGRAM COLLATING SEQUENCE IS COLLATING-SEQ-1.               C_AREA.11
              000012 SPECIAL-NAMES.                                                   C_AREA.12
              000013     ALPHABET PRG-COLL-SEQ IS                                     C_AREA.13
              000014     STANDARD-2.                                                  C_AREA.14
              000015 INPUT-OUTPUT SECTION.                                            C_AREA.15
              000016 FILE-CONTROL. SELECT OPTIONAL IDENTIFIER ASSIGN TO DISK.         C_AREA.16
              000017 I-O-CONTROL. IDENTIFIER.                                         C_AREA.17
              000018 RERUN ON IDENTIFIER EVERY 10 RECORDS                             C_AREA.18
              000019 SAME RECORD AREA FOR IDENTIFIER                                  C_AREA.19
              000020 MULTIPLE FILE TAPE CONTAINS IDENTIFIER POSITION 10               C_AREA.20
              000021 COMMITMENT CONTROL FOR IDENTIFIER.                               C_AREA.21
              """
          )
        );
    }

    @Test
    void inputOutputSection() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID.                                                      C_AREA.02
              000003     IC109A.                                                      C_AREA.03
              000004 ENVIRONMENT DIVISION.                                            C_AREA.04
              000005 INPUT-OUTPUT SECTION.                                            C_AREA.05
              000006 FILE-CONTROL.                                                    C_AREA.06
              000007     SELECT PRINT-FILE ASSIGN TO                                  C_AREA.07
              000008         XXXXX055.                                                C_AREA.08
              000009     SELECT SEQ-FILE ASSIGN TO                                    C_AREA.09
              000010         XXXXX014.                                                C_AREA.10
              000011     SELECT SEQ-FILE RESERVE NO ALTERNATE AREA.                   C_AREA.11
              000012     SELECT SEQ-FILE ORGANIZATION IS RECORD BINARY INDEXED.       C_AREA.12
              000013     SELECT SEQ-FILE PADDING CHARACTER IS IDENTIFIER              C_AREA.13
              000014         IN IDENTIFIER.                                           C_AREA.14
              000015     SELECT SEQ-FILE RECORD DELIMITER IS STANDAR-1.               C_AREA.15
              000016     SELECT SEQ-FILE ACCESS MODE IS SEQUENTIAL.                   C_AREA.16
              000017     SELECT SEQ-FILE RECORD KEY IS IDENTIFIER IN IDENTIFIER       C_AREA.18
              000018         PASSWORD IS IDENTIFIER WITH DUPLICATES.                  C_AREA.18
              000019     SELECT SEQ-FILE ALTERNATE RECORD KEY IS IDENTIFIER IN        C_AREA.19
              000020         IDENTIFIER PASSWORD IS IDENTIFIER WITH DUPLICATES.       C_AREA.20
              000021     SELECT SEQ-FILE FILE STATUS IS IDENTIFIER IN IDENTIFIER      C_AREA.21
              000022         IDENTIFIER IN IDENTIFIER.                                C_AREA.22
              000023     SELECT SEQ-FILE RELATIVE KEY IS IDENTIFIER IN IDENTIFIER.    C_AREA.23
              """
          )
        );
    }

    @Test
    void procedureDivision() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION  DIVISION .                                       C_AREA.01
              000002 PROGRAM-ID    . HELLO     .                                      C_AREA.02
              000003 PROCEDURE DIVISION USING GRP-01 GIVING dataName.                 C_AREA.03
              000004 DECLARATIVES.                                                    C_AREA.04
              000005 sectionName SECTION 77.                                          C_AREA.05
              000006 USE GLOBAL AFTER STANDARD ERROR PROCEDURE ON INPUT.              C_AREA.06
              000007 END DECLARATIVES.                                                C_AREA.07
              """
          )
        );
    }

    @Test
    void divisionUsing() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION  DIVISION .                                       C_AREA.01
              000002 PROGRAM-ID    . HELLO     .                                      C_AREA.02
              000003 PROCEDURE DIVISION USING GRP-01.                                 C_AREA.03
              000004 STOP RUN.                                                        C_AREA.04
              """
          )
        );
    }

    @Test
    void ic109a() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID.                                                      C_AREA.02
              000003     IC109A.                                                      C_AREA.03
              000004 ENVIRONMENT DIVISION.                                            C_AREA.04
              000005 CONFIGURATION SECTION.                                           C_AREA.05
              000006 SOURCE-COMPUTER.                                                 C_AREA.06
              000007     XXXXX082.                                                    C_AREA.07
              000008 OBJECT-COMPUTER.                                                 C_AREA.08
              000009     XXXXX083.                                                    C_AREA.09
              000010 INPUT-OUTPUT SECTION.                                            C_AREA.10
              000011 FILE-CONTROL.                                                    C_AREA.11
              000012     SELECT PRINT-FILE ASSIGN TO                                  C_AREA.12
              000013     XXXXX055.                                                    C_AREA.13
              000014 DATA DIVISION.                                                   C_AREA.14
              000015 FILE SECTION.                                                    C_AREA.15
              000016 FD  PRINT-FILE.                                                  C_AREA.16
              000017 01  PRINT-REC PICTURE X(120).                                    C_AREA.17
              000018 01  DUMMY-RECORD PICTURE X(120).                                 C_AREA.18
              000019 WORKING-STORAGE SECTION.                                         C_AREA.19
              000020 77  WS1 PICTURE X.                                               C_AREA.20
              000021 LINKAGE SECTION.                                                 C_AREA.21
              000022 01  GRP-01.                                                      C_AREA.22
              000023     02  SUB-CALLED.                                              C_AREA.23
              000024         03  DN1  PICTURE X(6).                                   C_AREA.24
              000025         03  DN2  PICTURE X(6).                                   C_AREA.25
              000026         03  DN3  PICTURE X(6).                                   C_AREA.26
              000027     02  TIMES-CALLED.                                            C_AREA.27
              000028         03  DN4  PICTURE S999.                                   C_AREA.28
              000029         03  DN5  PICTURE S999.                                   C_AREA.29
              000030         03  DN6  PICTURE S999.                                   C_AREA.30
              000031     02  SPECIAL-FLAGS.                                           C_AREA.31
              000032         03  DN7 PICTURE X.                                       C_AREA.32
              000033         03  DN8 PICTURE X.                                       C_AREA.33
              000034         03  DN9 PICTURE X.                                       C_AREA.34
              000035 PROCEDURE DIVISION USING GRP-01.                                 C_AREA.35
              000036 SECT-IC109-0001 SECTION.                                         C_AREA.36
              000037 PARA-IC109.                                                      C_AREA.37
              000038     MOVE "IC109A" TO DN1.                                        C_AREA.38
              000039     MOVE SPACE TO WS1.                                           C_AREA.39
              000040     CALL "IC110A" USING WS1 GRP-01.                              C_AREA.40
              000041     ADD 1 TO DN4.                                                C_AREA.41
              000042     MOVE WS1 TO DN9.                                             C_AREA.42
              000043 EXIT-IC109.                                                      C_AREA.43
              000045     EXIT PROGRAM.                                                C_AREA.45
              000046 END-OF                                                           C_AREA.46
              """
          )
        );
    }

    @Test
    void moveStatement() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.1
              000002 PROGRAM-ID. MOVETEST.                                            C_AREA.2
              000003 DATA DIVISION.                                                   C_AREA.3
              000004 PROCEDURE DIVISION USING GRP-01.                                 C_AREA.4
              000005 PARA-MOVETEST.                                                   C_AREA.5
              000006     MOVE "MOVETEST" TO DN1.                                      C_AREA.6
              000007     MOVE SPACE TO WS1.                                           C_AREA.7
              """
          )
        );
    }

    @Test
    void mergeStatement() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.1
              000002 PROGRAM-ID. MERGETEST.                                           C_AREA.2
              000003 PROCEDURE DIVISION.                                              C_AREA.3
              000004 MERGE-TEST.                                                      C_AREA.4
              000005     MERGE ST-FS4  ON ASCENDING KEY SORT-KEY                      C_AREA.5
              000006         USING  SQ-FS1  SQ-FS2                                    C_AREA.6
              000007         OUTPUT PROCEDURE IS MERGE-OUTPUT-PROC.                   C_AREA.7
              """
          )
        );
    }

    @Test
    void multiplyStatement() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.1
              000002 PROGRAM-ID. MULTIPLYTEST.                                        C_AREA.2
              000003 PROCEDURE DIVISION.                                              C_AREA.3
              000004 MULTIPLY -1.3 BY MULT4 ROUNDED.                                  C_AREA.4
              """
          )
        );
    }

    @Test
    void openStatement() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.1
              000002 PROGRAM-ID. OPENTEST.                                            C_AREA.2
              000003 PROCEDURE DIVISION.                                              C_AREA.3
              000004 OPEN OUTPUT SQ-FS2.                                              C_AREA.4
              000005 OPEN INPUT TFIL REVERSED.                                        C_AREA.5
              000006 OPEN INPUT TFIL WITH NO REWIND.                                  C_AREA.6
              """
          )
        );
    }

    @Test
    void performStatement() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.1
              000002 PROGRAM-ID. PARSERTEST.                                          C_AREA.2
              000003 PROCEDURE DIVISION.                                              C_AREA.3
              000004 PERFORM ST301M-MERGE THRU ST301M-SORT 1 TIMES.                   C_AREA.4
              """
          )
        );
    }

    @Test
    void readStatement() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.1
              000002 PROGRAM-ID. READTEST.                                            C_AREA.2
              000003 PROCEDURE DIVISION.                                              C_AREA.3
              000004 READ SQ-FS3 END .                                                C_AREA.4
              """
          )
        );
    }

    @Test
    void receiveStatement() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.1
              000002 PROGRAM-ID. MERGETEST.                                           C_AREA.2
              000003 PROCEDURE DIVISION.                                              C_AREA.3
              000004 RECEIVE CM-INQUE-1 MESSAGE INTO MSG-72                           C_AREA.4
              000005     NO DATA.                                                     C_AREA.5
              """
          )
        );
    }

    @Issue("https://github.com/openrewrite/rewrite-cobol/issues/59")
    @Test
    void fileDescriptionEntryClauses() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID.                                                      C_AREA.02
              000003     IC109A.                                                      C_AREA.03
              000004 DATA DIVISION.                                                   C_AREA.04
              000005 FILE SECTION.                                                    C_AREA.05
              000006 FD  PRINT-FILE.                                                  C_AREA.06
              000007 LABEL RECORD IS STANDARD.                                        C_AREA.08
              000008 LABEL RECORD ARE STANDARD.                                       C_AREA.07
              000009 LABEL RECORDS ARE STANDARD.                                      C_AREA.09
              000010 LABEL RECORDS IS STANDARD.                                       C_AREA.10
              000011 DATA RECORD IS IDENTIFIER.                                       C_AREA.11
              000012 DATA RECORD ARE IDENTIFIER.                                      C_AREA.12
              000013 DATA RECORDS ARE IDENTIFIER.                                     C_AREA.13
              000014 DATA RECORDS IS IDENTIFIER.                                      C_AREA.14
              000015 REPORT IS IDENTIFIER.                                            C_AREA.15
              000016 REPORT ARE IDENTIFIER.                                           C_AREA.16
              000017 REPORTS ARE IDENTIFIER.                                          C_AREA.17
              000018 REPORTS IS IDENTIFIER.                                           C_AREA.18
              """
          )
        );
    }

    @Test
    void fileSection() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID.                                                      C_AREA.02
              000003     IC109A.                                                      C_AREA.03
              000004 DATA DIVISION.                                                   C_AREA.04
              000005 FILE SECTION.                                                    C_AREA.05
              000006 FD  PRINT-FILE.                                                  C_AREA.06
              000007 IS EXTERNAL.                                                     C_AREA.07
              000008 IS GLOBAL.                                                       C_AREA.08
              000009 BLOCK CONTAINS 1 TO 10 RECORDS.                                  C_AREA.09
              000010 RECORD CONTAINS 10 CHARACTERS.                                   C_AREA.10
              000011 RECORD IS VARYING IN SIZE FROM 1 TO 10 CHARACTERS                C_AREA.11
              000012     DEPENDING ON IDENTIFIER IN IDENTIFIER.                       C_AREA.12
              000013 RECORD CONTAINS 1 TO 10 CHARACTERS.                              C_AREA.13
              000014 LABEL RECORD IS OMITTED.                                         C_AREA.14
              000015 VALUE OF IDENTIFIER IS 10.                                       C_AREA.15
              000016 LINAGE IS 10 LINES WITH FOOTING AT 10.                           C_AREA.16
              000017 LINAGE IS 10 LINES AT TOP 10.                                    C_AREA.17
              000018 LINAGE IS 10 LINES AT BOTTOM 10.                                 C_AREA.18
              000019 CODE-SET IS IDENTIFIER.                                          C_AREA.19
              000020 RECORDING MODE IS IDENTIFIER.                                    C_AREA.20
              000021 DATA RECORD IS IDENTIFIER.                                       C_AREA.21
              000022 REPORT IS IDENTIFIER.                                            C_AREA.22
              000023 01  PRINT-REC PICTURE X(120).                                    C_AREA.23
              000024 01  DUMMY-RECORD PICTURE X(120).                                 C_AREA.24
              """
          )
        );
    }

    @Test
    void linkageSection() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002     PROGRAM-ID.                                                  C_AREA.02
              000003         IC109A.                                                  C_AREA.03
              000004     DATA DIVISION.                                               C_AREA.04
              000005     LINKAGE SECTION.                                             C_AREA.05
              000006     01  GRP-01.                                                  C_AREA.06
              000007         02  SUB-CALLED.                                          C_AREA.07
              000008             03  DN1  PICTURE X(6).                               C_AREA.08
              000009             03  DN2  PICTURE X(6).                               C_AREA.09
              000010             03  DN3  PICTURE X(6).                               C_AREA.10
              000011         02  TIMES-CALLED.                                        C_AREA.11
              000012             03  DN4  PICTURE S999.                               C_AREA.12
              000013             03  DN5  PICTURE S999.                               C_AREA.13
              000014             03  DN6  PICTURE S999.                               C_AREA.14
              000015         02  SPECIAL-FLAGS.                                       C_AREA.15
              000016             03  DN7 PICTURE X.                                   C_AREA.16
              000017             03  DN8 PICTURE X.                                   C_AREA.17
              000018             03  DN9 PICTURE X.                                   C_AREA.18
              """
          )
        );
    }

    @Test
    void localStorageSection() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID. LocalStorage.                                        C_AREA.02
              000003 DATA DIVISION.                                                   C_AREA.03
              000004 LOCAL-STORAGE Section.                                           C_AREA.04
              000005 01  NUM  PIC 9(4).                                               C_AREA.05
              """
          )
        );
    }

    @Test
    void dataBaseSection() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID. DBSection.                                           C_AREA.02
              000003 DATA DIVISION.                                                   C_AREA.03
              000004 DATA-BASE SECTION.                                               C_AREA.04
              000005 01 TRUE INVOKE TRUE                                              C_AREA.05
              """
          )
        );
    }

    @Test
    void screenSection() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID. DBSection.                                           C_AREA.02
              000003 DATA DIVISION.                                                   C_AREA.03
              000004 SCREEN SECTION.                                                  C_AREA.04
              000005 01 SCREEN1 BLANK LINE                                            C_AREA.05
              000006 BELL                                                             C_AREA.06
              000007 BLINK                                                            C_AREA.07
              000008 ERASE EOL                                                        C_AREA.08
              000009 HIGHLIGHT                                                        C_AREA.09
              000010 GRID                                                             C_AREA.10
              000011 UNDERLINE                                                        C_AREA.11
              000012 SIZE IS IDENTIFIER IN IDENTIFIER                                 C_AREA.12
              000013 LINE NUMBER IS PLUS IDENTIFIER IN IDENTIFIER                     C_AREA.13
              000014 COLUMN NUMBER IS PLUS IDENTIFIER IN IDENTIFIER                   C_AREA.14
              000015 FOREGROUND-COLOR IS IDENTIFIER IN IDENTIFIER                     C_AREA.15
              000016 BACKGROUND-COLOR IS IDENTIFIER IN IDENTIFIER                     C_AREA.16
              000017 CONTROL IS IDENTIFIER IN IDENTIFIER                              C_AREA.17
              000018 VALUE IS 10                                                      C_AREA.18
              000019 PICTURE IS $(10)                                                 C_AREA.19
              000020 FROM IDENTIFIER IN IDENTIFIER TO IDENTIFIER IN IDENTIFIER        C_AREA.20
              000021 USING IDENTIFIER IN IDENTIFIER                                   C_AREA.21
              000022 USAGE IS DISPLAY                                                 C_AREA.22
              000023 BLANK WHEN ZERO                                                  C_AREA.23
              000024 JUSTIFIED RIGHT                                                  C_AREA.24
              000025 SIGN IS LEADING SEPARATE CHARACTER                               C_AREA.25
              000026 AUTO                                                             C_AREA.26
              000027 SECURE                                                           C_AREA.27
              000028 REQUIRED                                                         C_AREA.28
              000029 PROMPT CHARACTER IS IDENTIFIER IN IDENTIFIER OCCURS 01 TIMES     C_AREA.29
              000030 FULL                                                             C_AREA.30
              000031 ZERO-FILL                                                        C_AREA.31
              000032 .                                                                C_AREA.32
              """
          )
        );
    }

    @Disabled("Potential lexer issue: The REVERSE-VIDEO token maps to RESERVE-VIDEO")
    @Test
    void reverseVideo() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID. DBSection.                                           C_AREA.02
              000003 DATA DIVISION.                                                   C_AREA.03
              000004 SCREEN SECTION.                                                  C_AREA.04
              000005 01 REVERSE-VIDEO.                                                C_AREA.05
              """
          )
        );
    }

    @Test
    void acceptStatement() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID. acceptStatement.                                     C_AREA.02
              000003 PROCEDURE DIVISION.                                              C_AREA.03
              000004 PARAGRAPH_NAME.                                                  C_AREA.04
              000005 ACCEPT identifier FROM DATE YYYYMMDD END-ACCEPT                  C_AREA.05
              000006 ACCEPT identifier FROM ESCAPE KEY                                C_AREA.06
              000007 ACCEPT identifier FROM mnemonicName                              C_AREA.07
              000008 ACCEPT identifier MESSAGE COUNT.                                 C_AREA.08
              """
          )
        );
    }

    @Test
    void alterStatement() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION .                                        C_AREA.01
              000002 PROGRAM-ID . HELLO-WORLD .                                       C_AREA.02
              000003 PROCEDURE DIVISION .                                             C_AREA.03
              000004 ALTER PARA-54 TO PROCEED TO PARA-54B.                            C_AREA.04
              000005 ALTER PARA-23 TO PARA-24.                                        C_AREA.05
              """
          )
        );
    }

    @Test
    void cancelStatement() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID. acceptStatement.                                     C_AREA.02
              000003 PROCEDURE DIVISION.                                              C_AREA.03
              000004 PARAGRAPH_NAME.                                                  C_AREA.04
              000005 CANCEL "literal"                                                 C_AREA.05
              000006 CANCEL identifier                                                C_AREA.06
              000007 CANCEL libraryName BYTITLE.                                      C_AREA.07
              """
          )
        );
    }

    @Test
    void closeStatement() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID. acceptStatement.                                     C_AREA.02
              000003 PROCEDURE DIVISION.                                              C_AREA.03
              000004 PARAGRAPH_NAME.                                                  C_AREA.04
              000005 CLOSE fileName UNIT FOR REMOVAL WITH LOCK                        C_AREA.05
              000006 CLOSE fileName WITH NO REWIND                                    C_AREA.06
              000007 CLOSE fileName NO WAIT USING CLOSE-DISPOSITION OF ABORT          C_AREA.07
              000008 CLOSE fileName NO WAIT USING ASSOCIATED-DATA identifier          C_AREA.08
              000009 CLOSE fileName NO WAIT USING ASSOCIATED-DATA-LENGTH              C_AREA.09
              000010 OF identifier.                                                   C_AREA.10
              """
          )
        );
    }

    @Test
    void rewriteStatement() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID. acceptStatement.                                     C_AREA.02
              000003 PROCEDURE DIVISION.                                              C_AREA.03
              000004 PARAGRAPH_NAME.                                                  C_AREA.04
              000005 REWRITE dataName IN fileName END-REWRITE.                        C_AREA.05
              """
          )
        );
    }

    @Test
    void callStatement() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID. acceptStatement.                                     C_AREA.02
              000003 PROCEDURE DIVISION USING GRP-01.                                 C_AREA.03
              000004 SECT-IC109-0001 SECTION.                                         C_AREA.04
              000005 PARA-IC109.                                                      C_AREA.05
              000006     CALL "IC110A" USING BY REFERENCE WS1 GRP-01.                 C_AREA.06
              000007     CALL "IC110A" USING BY VALUE ADDRESS OF GRP-01.              C_AREA.07
              000008     CALL "IC110A" USING BY CONTENT LENGTH OF GRP-01.             C_AREA.08
              000009     CALL "IC110A" GIVING GRP-01.                                 C_AREA.09
              000010     CALL "IC110A" ON OVERFLOW CONTINUE.                          C_AREA.10
              000011     CALL "IC110A" ON EXCEPTION CONTINUE.                         C_AREA.11
              000012     CALL "IC110A" NOT ON EXCEPTION CONTINUE.                     C_AREA.12
              """
          )
        );
    }

    @Test
    void writeStatement() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID. acceptStatement.                                     C_AREA.02
              000003 PROCEDURE DIVISION USING GRP-01.                                 C_AREA.03
              000004 PARA-IC109.                                                      C_AREA.04
              000005     WRITE IC110A FROM GRP-01.                                    C_AREA.05
              000006     WRITE IC110A BEFORE ADVANCING PAGE.                          C_AREA.06
              000007     WRITE IC110A BEFORE ADVANCING 10 LINES.                      C_AREA.07
              000008     WRITE IC110A BEFORE ADVANCING GRP-01.                        C_AREA.08
              000009     WRITE IC110A AT END-OF-PAGE CONTINUE.                        C_AREA.09
              000010     WRITE IC110A NOT AT END-OF-PAGE CONTINUE.                    C_AREA.10
              000011     WRITE IC110A INVALID KEY CONTINUE.                           C_AREA.11
              000012     WRITE IC110A NOT INVALID KEY CONTINUE.                       C_AREA.12
              """
          )
        );
    }

    @Test
    void computeStatement() {
        rewriteRun(
          cobol(
			"""
            000001 IDENTIFICATION DIVISION .                                        C_AREA.01
            000002 PROGRAM-ID . HELLO-WORLD .                                       C_AREA.02
            000003 PROCEDURE DIVISION .                                             C_AREA.03
            000004     COMPUTE V = (1 + 2) .                                        C_AREA.04
            000005     COMPUTE LAG-TIME =                                           C_AREA.05
            000006         ((SYS-HRS * 3600) + (SYS-MINS * 60) + SYS-SECS) -        C_AREA.06
            000007         ((HOURS OF MSG-TIME * 3600) + (MINUTES OF MSG-TIME * 60) C_AREA.07
            000008         + SECONDS OF MSG-TIME)                                   C_AREA.08
            000009         END-COMPUTE .                                            C_AREA.09
            """
          )
        );
    }

    @Test
    void divideStatement() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID. acceptStatement.                                     C_AREA.02
              000003 PROCEDURE DIVISION USING GRP-01.                                 C_AREA.03
              000004 SIG-TEST-GF-5-0.                                                 C_AREA.04
              000005     DIVIDE 0.097 INTO DIV7 ROUNDED.                              C_AREA.05
              000006     DIVIDE 0.097 INTO DIV7 GIVING DIV8 ROUNDED.                  C_AREA.06
              000007     DIVIDE 0.097 BY DIV7 GIVING DIV8 ROUNDED.                    C_AREA.07
              000008     DIVIDE 0.097 INTO DIV7 REMAINDER DIV9.                       C_AREA.08
              000009     DIVIDE 0.097 INTO DIV7 ON SIZE ERROR CONTINUE.               C_AREA.09
              000010     DIVIDE 0.097 INTO DIV7 NOT ON SIZE ERROR CONTINUE.           C_AREA.10
              """
          )
        );
    }

    @Test
    void evaluateStatement() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID. acceptStatement.                                     C_AREA.02
              000003 PROCEDURE DIVISION USING GRP-01.                                 C_AREA.03
              000004 F-ANNUITY-02.                                                    C_AREA.04
              000005 EVALUATE IC110A END-EVALUATE.                                    C_AREA.05
              000006 EVALUATE IC110A ALSO IC110B.                                     C_AREA.06
              000007 EVALUATE IC110A                                                  C_AREA.07
              000008 WHEN ANY ALSO ANY                                                C_AREA.08
              000009     CONTINUE                                                     C_AREA.09
              000010 WHEN IDENTIFIER THRU IDENTIFIER                                  C_AREA.10
              000011     CONTINUE                                                     C_AREA.11
              000012 WHEN TRUE                                                        C_AREA.12
              000013     CONTINUE                                                     C_AREA.13
              000014 WHEN OTHER                                                       C_AREA.14
              000015     CONTINUE.                                                    C_AREA.15
              """
          )
        );
    }

    @Test
    void conditions() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID. acceptStatement.                                     C_AREA.02
              000003 PROCEDURE DIVISION USING GRP-01.                                 C_AREA.03
              000004 F-ANNUITY-02.                                                    C_AREA.04
              000005 EVALUATE IC110A                                                  C_AREA.05
              000006 WHEN IDENTIFIER IS NOT ALPHABETIC-LOWER                          C_AREA.06
              000007     CONTINUE                                                     C_AREA.07
              000008 WHEN IDENTIFIER IN IDENTIFIER                                    C_AREA.08
              000009     CONTINUE.                                                    C_AREA.09
              """
          )
        );
    }

    @Test
    void conditionNameSubscriptReference() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID. acceptStatement.                                     C_AREA.02
              000003 PROCEDURE DIVISION.                                              C_AREA.03
              000004 COMMA-SUBSCRIPT-TEST.                                            C_AREA.04
              000005 EVALUATE NOT IDENTIFIER (IDENTIFIER, IDENTIFIER IDENTIFIER)      C_AREA.05
              000006 .                                                                C_AREA.06
              """
          )
        );
    }

    @Test
    void sendStatement() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID. acceptStatement.                                     C_AREA.02
              000003 PROCEDURE DIVISION.                                              C_AREA.03
              000004 SEND CM-OUTQUE-1 FROM MSG-70 WITH EMI                            C_AREA.04
              000005     AFTER ADVANCING PAGE.                                        C_AREA.05
              """
          )
        );
    }

    @Test
    void tableCallTest() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID. acceptStatement.                                     C_AREA.02
              000003 PROCEDURE DIVISION.                                              C_AREA.03
              000004 COMMA-SUBSCRIPT-TEST.                                            C_AREA.04
              000005 EVALUATE SUBSCRIPT                                               C_AREA.05
              000006 WHEN IDENTIFIER (IDENTIFIER, IDENTIFIER IDENTIFIER)              C_AREA.06
              000007     CONTINUE.                                                    C_AREA.07
              """
          )
        );
    }

    @Test
    void functionCallTest() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID. acceptStatement.                                     C_AREA.02
              000003 PROCEDURE DIVISION.                                              C_AREA.03
              000004 COMMA-SUBSCRIPT-TEST.                                            C_AREA.04
              000005 EVALUATE SUBSCRIPT                                               C_AREA.05
              000006 WHEN IDENTIFIER (FUNCTION INTEGER                                C_AREA.06
              000007 (IDENTIFIER, IDENTIFIER IDENTIFIER) (1: 10))                     C_AREA.07
              000008     CONTINUE.                                                    C_AREA.08
              """
          )
        );
    }

    @Test
    void relationConditions() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID. acceptStatement.                                     C_AREA.02
              000003 PROCEDURE DIVISION.                                              C_AREA.03
              000004 F-ANNUITY-02.                                                    C_AREA.04
              000005 EVALUATE IC110A                                                  C_AREA.05
              000006 WHEN NOT +IDENTIFIER IS NOT ZERO                                 C_AREA.06
              000007 WHEN NOT +IDENTIFIER IS GREATER OR EQUAL +IDENTIFIER             C_AREA.07
              000008 WHEN NOT +ZERO GREATER THAN (IDENTIFIER AND IDENTIFIER OR        C_AREA.08
              000009 IDENTIFIER) .                                                    C_AREA.09
              """
          )
        );
    }

    @Test
    void multiElementLiteral() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID. acceptStatement.                                     C_AREA.02
              000003 PROCEDURE DIVISION.                                              C_AREA.03
              000004 Literal-Test.                                                    C_AREA.04
              000005 EVALUATE DFHRESP (IDENTIFIER).                                   C_AREA.05
              """
          )
        );
    }

    @Test
    void multiElementIdentifier() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID. acceptStatement.                                     C_AREA.02
              000003 PROCEDURE DIVISION.                                              C_AREA.03
              000004 Identifier-Test.                                                 C_AREA.04
              000005 EVALUATE IDENTIFIER IN IDENTIFIER.                               C_AREA.05
              """
          )
        );
    }

    @Test
    void openMultipleStatements() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID. acceptStatement.                                     C_AREA.02
              000003 PROCEDURE DIVISION.                                              C_AREA.03
              000004 OPEN-FILES.                                                      C_AREA.04
              000005     OPEN     INPUT IDENTIFIER REVERSED INPUT IDENTIFIER          C_AREA.05
              000006         REVERSED                                                 C_AREA.06
              000007     OPEN     OUTPUT IDENTIFIER WITH NO REWIND IDENTIFIER         C_AREA.07
              000008         WITH NO REWIND                                           C_AREA.08
              000009     OPEN     I-O IDENTIFIER IDENTIFIER I-O IDENTIFIER            C_AREA.09
              000010         IDENTIFIER                                               C_AREA.10
              000011     OPEN     EXTEND IDENTIFIER IDENTIFIER EXTEND IDENTIFIER      C_AREA.11
              000012         IDENTIFIER.                                              C_AREA.12
              """
          )
        );
    }

    @Test
    void outOfOrderOpenStatements() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID. acceptStatement.                                     C_AREA.02
              000003 PROCEDURE DIVISION.                                              C_AREA.03
              000004 OPEN-FILES.                                                      C_AREA.04
              000005     OPEN     INPUT IDENTIFIER OUTPUT IDENTIFIER INPUT            C_AREA.05
              000006     IDENTIFIER OUTPUT IDENTIFIER.                                C_AREA.06
              """
          )
        );
    }

    @Test
    void unstringStatement() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID. acceptStatement.                                     C_AREA.02
              000003 PROCEDURE DIVISION.                                              C_AREA.03
              000004 UNSTRING-TEST.                                                   C_AREA.04
              000005     UNSTRING IDENTIFIER DELIMITED BY ALL IDENTIFIER OR ALL       C_AREA.05
              000006         IDENTIFIER INTO IDENTIFIER DELIMITER IN                  C_AREA.06
              000007         IDENTIFIER COUNT IN IDENTIFIER END-UNSTRING              C_AREA.07
              000008     UNSTRING IDENTIFIER INTO IDENTIFIER WITH POINTER IDENTIFIER  C_AREA.08
              000009     UNSTRING IDENTIFIER INTO IDENTIFIER TALLYING IN IDENTIFIER   C_AREA.09
              000010     UNSTRING IDENTIFIER INTO IDENTIFIER ON OVERFLOW              C_AREA.10
              000011     UNSTRING IDENTIFIER INTO IDENTIFIER NOT ON OVERFLOW.         C_AREA.11
              """
          )
        );
    }

    @Test
    void terminateStatement() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID. terminateStatement.                                  C_AREA.02
              000003 PROCEDURE DIVISION.                                              C_AREA.03
              000004 RW301M-CONTROL.                                                  C_AREA.04
              000005     TERMINATE RFIL2.                                             C_AREA.05
              """
          )
        );
    }

    @Test
    void generateStatement() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID. terminateStatement.                                  C_AREA.02
              000003 PROCEDURE DIVISION.                                              C_AREA.03
              000004 RW301M-CONTROL.                                                  C_AREA.04
              000005     GENERATE RREC.                                               C_AREA.05
              """
          )
        );
    }

    @Test
    void subtractStatement() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID. subtractStatement.                                   C_AREA.02
              000003 PROCEDURE DIVISION.                                              C_AREA.03
              000004 RW301M-CONTROL.                                                  C_AREA.04
              000005     SUBTRACT 1 FROM ERROR-COUNTER                                C_AREA.05
              000006     SUBTRACT N-10 FROM 0 GIVING N-19                             C_AREA.06
              000007     SUBTRACT CORRESPONDING IDENTIFIER FROM IDENTIFIER ROUNDED.   C_AREA.07
              """
          )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
      """
        000005* only word.
                   EXIT.
        """,
      """
        000005* format-1
                   IDENTIFIER. EXIT.
        """,
      """
        000005* format-2
                   EXIT PROGRAM.
        """,
      """
        000005* format-3
                   EXIT METHOD.
        """,
      """
        000005* format-5. Note: format-4 does not exist in the spec.
                   EXIT PERFORM
                   EXIT PERFORM CYCLE.
        """,
      """
        000005* format-6.
                   EXIT PARAGRAPH
                   EXIT SECTION.
        """,
    })
    void exitStatement(String input) {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID. exitStatement.                                       C_AREA.02
              000003 PROCEDURE DIVISION.                                              C_AREA.03
              000004 RW301M-CONTROL.                                                  C_AREA.04
              %s
              """.formatted(input)
          )
        );
    }

    /**
     * The name in the EXIT statement's format-1, `paragraph-name. EXIT.`, opens a paragraph. It is
     * a procedure name that PERFORM THRU and GO TO can target, so it has to reach the LST as one
     * rather than being folded into the statement.
     */
    @Issue("https://github.com/openrewrite/rewrite-cobol/issues/111")
    @Test
    void exitParagraphIsAParagraph() {
        AtomicInteger paragraphs = new AtomicInteger();
        AtomicInteger exits = new AtomicInteger();
        rewriteRun(
          spec -> spec.recipe(toRecipe(() -> new CobolIsoVisitor<>() {
              @Override
              public Cobol.Paragraph visitParagraph(Cobol.Paragraph paragraph, ExecutionContext ctx) {
                  paragraphs.incrementAndGet();
                  return super.visitParagraph(paragraph, ctx);
              }

              @Override
              public Cobol.Exit visitExit(Cobol.Exit exit, ExecutionContext ctx) {
                  exits.incrementAndGet();
                  assertThat(exit.getWords()).hasSize(1);
                  assertThat(exit.getWords().get(0).getWord()).isEqualToIgnoringCase("EXIT");
                  return super.visitExit(exit, ctx);
              }
          })),
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID. exitParagraph.                                       C_AREA.02
              000003 PROCEDURE DIVISION.                                              C_AREA.03
              000004 RW301M-CONTROL.                                                  C_AREA.04
              000005     PERFORM RW301M-BODY THRU RW301M-EXIT.                        C_AREA.05
              000006 RW301M-BODY.                                                     C_AREA.06
              000007     MOVE 1 TO WS-X.                                              C_AREA.07
              000008 RW301M-EXIT.                                                     C_AREA.08
              000009     EXIT.                                                        C_AREA.09
              """
          )
        );
        assertThat(paragraphs).hasValue(3);
        assertThat(exits).hasValue(1);
    }

    @Test
    void sortStatement() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID. sortStatement.                                       C_AREA.02
              000003 PROCEDURE DIVISION.                                              C_AREA.03
              000004 SORT-STATEMENT.                                                  C_AREA.04
              000005     SORT SORTFILE-1B                                             C_AREA.05
              000006         ON DESCENDING KEY KEY-1                                  C_AREA.06
              000007         ON ASCENDING KEY KEY-2                                   C_AREA.07
              000008         ON DESCENDING KEY KEY-3                                  C_AREA.08
              000009         ASCENDING KEY-4 KEY-5                                    C_AREA.09
              000010     USING SORTIN-1B                                              C_AREA.10
              000011     GIVING SORTOUT-1B.                                           C_AREA.11
              """
          )
        );
    }

    @Test
    void stringStatement() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID. subtractStatement.                                   C_AREA.02
              000003 PROCEDURE DIVISION.                                              C_AREA.03
              000004 RW301M-CONTROL.                                                  C_AREA.04
              000005     STRING NONNUMERICLITERAL, NONNUMERICLITERAL                  C_AREA.05
              000006     NONNUMERICLITERAL DELIMITED BY SIZE                          C_AREA.06
              000007     INTO IDENTIFIER                                              C_AREA.07
              000008     WITH POINTER IDENTIFIER END-STRING .                         C_AREA.08
              """
          )
        );
    }

    @Test
    void startStatement() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID. subtractStatement.                                   C_AREA.02
              000003 PROCEDURE DIVISION.                                              C_AREA.03
              000004 STA-TEST-GF-01.                                                  C_AREA.04
              000005     START IX-FS2 KEY IS NOT LESS THAN IDENTIFIER                 C_AREA.05
              000006     IN IDENTIFIER END-START.                                     C_AREA.06
              000007 .                                                                C_AREA.07
              """
          )
        );
    }

    @Test
    void entryStatement() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION  DIVISION.
              000002 PROGRAM-ID. HELLO.
              000003 PROCEDURE DIVISION.
              000004 ENTRY 'Hello world!'
              000005 ENTRY 'Hello world!' USING IDENTIFIER, IDENTIFIER, IDENTIFIER.
              """
          )
        );
    }

    @Test
    void goToStatement() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID. subtractStatement.                                   C_AREA.02
              000003 PROCEDURE DIVISION.                                              C_AREA.03
              000004 ACCEPT-TEST-01.                                                  C_AREA.04
              000005     GO TO CM105-FINI.                                            C_AREA.05
              000006     GO TO CM105-FINI DEPENDING ON IDENTIFIER IN IDENTIFIER.      C_AREA.06
              000007 .                                                                C_AREA.07
              """
          )
        );
    }

    @Test
    void ifStatement() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID. subtractStatement.                                   C_AREA.02
              000003 PROCEDURE DIVISION.                                              C_AREA.03
              000004 IF--TEST-GF-99.                                                  C_AREA.04
              000005     IF      ZERO IS EQUAL TO IF-D1                               C_AREA.05
              000006        THEN PERFORM PASS                                         C_AREA.06
              000007     ELSE                                                         C_AREA.07
              000008         PERFORM FAIL.                                            C_AREA.08
              000009 .                                                                C_AREA.09
              """
          )
        );
    }

    @Test
    void initializeStatement() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID. subtractStatement.                                   C_AREA.02
              000003 PROCEDURE DIVISION.                                              C_AREA.03
              000004 INI-TEST-GF-1-0.                                                 C_AREA.04
              000005     INITIALIZE IDENTIFIER IN IDENTIFIER REPLACING NATIONAL       C_AREA.05
              000006     DATA BY 42.                                                  C_AREA.06
              000007 .                                                                C_AREA.07
              """
          )
        );
    }

    @Test
    void initiateStatement() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID. subtractStatement.                                   C_AREA.02
              000003 PROCEDURE DIVISION.                                              C_AREA.03
              000004 RW301M-CONTROL.                                                  C_AREA.04
              000005     INITIATE RFIL2.                                              C_AREA.05
              000006 .                                                                C_AREA.06
              """
          )
        );
    }

    @Test
    void dataValueInterval() {
        rewriteRun(
          cobol(
            """
              000100 IDENTIFICATION DIVISION.                                         CM1014.2
              000200 PROGRAM-ID.                                                      CM1014.2
              000300     CM101M.                                                      CM1014.2
              003200 DATA DIVISION.                                                   CM1014.2
              004000 WORKING-STORAGE SECTION.                                         CM1014.2
              008000 01  LOG-HDR-4.                                                   CM1014.2
              008100     02  FILLER PIC X VALUE SPACE.                                CM1014.2
              008200     02  FILLER PIC X(11) VALUE ALL "-".                          CM1014.2
              008300     02  FILLER PIC X VALUE SPACES.                               CM1014.2
              """
          )
        );
    }

    @Test
    void inspectStatement() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID. subtractStatement.                                   C_AREA.02
              000003 PROCEDURE DIVISION.                                              C_AREA.03
              000004 RW301M-CONTROL.                                                  C_AREA.04
              000005     INSPECT IDENTIFIER IN IDENTIFIER TALLYING                    C_AREA.05
              000006         IDENTIFIER IN IDENTIFIER                                 C_AREA.06
              000007         FOR CHARACTER BEFORE INITIAL 42                          C_AREA.07
              000008     INSPECT IDENTIFIER IN IDENTIFIER REPLACING CHARACTER BY      C_AREA.08
              000009         IDENTIFIER IN IDENTIFIER BEFORE INITIAL 42               C_AREA.09
              000010     INSPECT IDENTIFIER IN IDENTIFIER TALLYING                    C_AREA.10
              000011         IDENTIFIER IN IDENTIFIER FOR CHARACTER BEFORE            C_AREA.11
              000012         IDENTIFIER IN IDENTIFIER REPLACING ALL                   C_AREA.12
              000013         IDENTIFIER IN IDENTIFIER BY IDENTIFIER IN IDENTIFIER     C_AREA.13
              000014     INSPECT IDENTIFIER IN IDENTIFIER CONVERTING                  C_AREA.14
              000015         IDENTIFIER IN IDENTIFIER TO IDENTIFIER IN IDENTIFIER     C_AREA.15
              000016         BEFORE IDENTIFIER IN IDENTIFIER                          C_AREA.16
              000017 .                                                                C_AREA.17
              """
          )
        );
    }

    @Test
    void communicationSection() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID. communicationSection.                                C_AREA.02
              000003 DATA DIVISION.                                                   C_AREA.03
              000004 COMMUNICATION SECTION.                                           C_AREA.04
              000005     CD COMMNAME FOR INITIAL INPUT.                               C_AREA.05
              000006     CD COMMNAME FOR OUTPUT.                                      C_AREA.06
              000007     CD COMMNAME FOR INITIAL I-O.                                 C_AREA.07
              """
          )
        );
    }

    @Test
    void reportSection() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID. communicationSection.                                C_AREA.02
              000003 DATA DIVISION.                                                   C_AREA.03
              000004 REPORT SECTION.                                                  C_AREA.04
              000005     RD IDENTIFIER IN IDENTIFIER IS GLOBAL.                       C_AREA.05
              000006     10 IDENTIFIER LINE NUMBER IS 10 ON NEXT PAGE.                C_AREA.06
              """
          )
        );
    }

    /**
     * The LIMIT keyword is optional in the RD PAGE clause, so `PAGE 30` says what
     * `PAGE LIMIT IS 30 LINES` says.
     */
    @Test
    void reportPageLimitClause() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID. reportPageLimit.                                     C_AREA.02
              000003 DATA DIVISION.                                                   C_AREA.03
              000004 REPORT SECTION.                                                  C_AREA.04
              000005     RD REPORT-1 PAGE LIMIT IS 60 LINES.                          C_AREA.05
              000006     10 IDENTIFIER LINE NUMBER IS 10 ON NEXT PAGE.                C_AREA.06
              000007     RD REPORT-2 PAGE 60 LINES.                                   C_AREA.07
              000008     10 IDENTIFIER LINE NUMBER IS 10 ON NEXT PAGE.                C_AREA.08
              000009     RD REPORT-3 PAGE 30                                          C_AREA.09
              000010     HEADING 1 FIRST DETAIL 6 LAST DETAIL 25.                     C_AREA.10
              000011     10 IDENTIFIER LINE NUMBER IS 10 ON NEXT PAGE.                C_AREA.11
              """
          )
        );
    }

    @Test
    void programLibrarySection() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                         C_AREA.01
              000002 PROGRAM-ID. communicationSection.                                C_AREA.02
              000003 DATA DIVISION.                                                   C_AREA.03
              000004 PROGRAM-LIBRARY SECTION.                                         C_AREA.04
              000005     LD IDENTIFIER EXPORT ATTRIBUTE SHARING IS DONTCARE           C_AREA.05
              000006         ENTRY-PROCEDURE IDENTIFIER FOR ZERO                      C_AREA.06
              000007     LB IDENTIFIER IMPORT IS GLOBAL IS COMMON ATTRIBUTE           C_AREA.07
              000008     FUNCTIONNAME IS ZERO LIBACCESS IS                            C_AREA.08
              000009         BYFUNCTION LIBPARAMETER IS ZERO                          C_AREA.09
              """
          )
        );
    }

    @Issue("https://github.com/openrewrite/rewrite-cobol/issues/4")
    @Test
    void noTrailingWhitespace() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION  DIVISION  .
              000002 PROGRAM-ID    . HELLO     .
              000003 PROCEDURE DIVISION        .
              000004 DISPLAY 'Hello world!'    .
              000005 STOP RUN                  .
              """
          )
        );
    }

    @Issue("https://github.com/openrewrite/rewrite-cobol/issues/4")
    @Test
    void emptyNewLines() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION  DIVISION .                                       C_AREA.01
              
              000002 PROGRAM-ID    . HELLO     .                                      C_AREA.02
              """
          )
        );
    }
}
