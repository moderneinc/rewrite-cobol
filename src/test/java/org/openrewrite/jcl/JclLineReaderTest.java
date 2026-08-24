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
package org.openrewrite.jcl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class JclLineReaderTest {

    @Test
    void controlM() {
        assertThat(JclLineReader
          .readLines("%%LIBSYM NAME.FIELD %%MEMSYM NAME.FIELD"))
          .isEqualTo("^^CM^^%%LIBSYM NAME.FIELD %%MEMSYM NAME.FIELD");
    }

    @Test
    void controlMIfConditions() {
        assertThat(JclLineReader
          .readLines(
            """
              //*
              //* Normal CM
              %%IF (1 EQ 1) THEN
              //JOB1 JOB
              %%ELSE
              //JOB1 JOB
              %%ENDIF
              //*
              //* CM in comments
              //* %%IF (1 EQ 1) THEN
              //JOB1 JOB
              //* %%ELSE
              //JOB1 JOB
              //* %%ENDIF
              //*
              //* Mixed in and out of comments
              //* %%IF (1 EQ 1) THEN
              //JOB1 JOB
              %%ENDIF
              """))
          .isEqualTo(
            """
              ^^COMMENT^^//*
              ^^COMMENT^^//* Normal CM
              ^^CM^^%%IF (1 EQ 1) THEN
              ^^JCL_STATEMENT^^//JOB1 JOB
              ^^CM^^%%ELSE
              ^^JCL_STATEMENT^^//JOB1 JOB
              ^^CM^^%%ENDIF
              ^^COMMENT^^//*
              ^^COMMENT^^//* CM in comments
              ^^COMMENT^^//* %%IF (1 EQ 1) THEN
              ^^JCL_STATEMENT^^//JOB1 JOB
              ^^COMMENT^^//* %%ELSE
              ^^JCL_STATEMENT^^//JOB1 JOB
              ^^COMMENT^^//* %%ENDIF
              ^^COMMENT^^//*
              ^^COMMENT^^//* Mixed in and out of comments
              ^^COMMENT^^//* %%IF (1 EQ 1) THEN
              ^^JCL_STATEMENT^^//JOB1 JOB
              ^^CM^^%%ENDIF
              """
          );
    }

    @Test
    void jcl() {
        assertThat(JclLineReader
          .readLines("//JOB1 JOB ,'H.H. MORRILL'"))
          .isEqualTo("^^JCL_STATEMENT^^//JOB1 JOB ,'H.H. MORRILL'");
    }

    @Test
    void stringLiteral() {
        assertThat(JclLineReader
          .readLines("//JOB1 DD DNS='*****'"))
          .isEqualTo("^^JCL_STATEMENT^^//JOB1 DD DNS='*****'");
    }


    @Test
    void jclContinuation() {
        assertThat(JclLineReader
          .readLines(
            """
              //%%JOBNAME.%%OTHER JOB (1,2,3),
              //    'NAME',
              //    MSGCLASS=A
              """))
          .isEqualTo(
            """
              ^^JCL_STATEMENT^^//%%JOBNAME.%%OTHER JOB (1,2,3),
              ^^JCL_CONT^^//    'NAME',
              ^^JCL_CONT^^//    MSGCLASS=A
              """
          );
    }

    @Test
    void jes2() {
        assertThat(JclLineReader
          .readLines("/*JOBPARAM A=1,B=2,C=%%NAME.FIELD"))
          .isEqualTo("^^JES2^^/*JOBPARAM A=1,B=2,C=%%NAME.FIELD");
    }

    @Test
    void jes2Continuation() {
        assertThat(JclLineReader
          .readLines(
            """
              /*JOBPARAM A=1,
              /*B=2,C=%%NAME.FIELD
              """
          ))
          .isEqualTo(
            """
              ^^JES2^^/*JOBPARAM A=1,
              ^^JES2_CONT^^/*B=2,C=%%NAME.FIELD
              """
          );
    }

    @Test
    void jes3() {
        assertThat(JclLineReader
          .readLines("//*MAIN CLASS=A,SYSTEM=SY1"))
          .isEqualTo("^^JES3^^//*MAIN CLASS=A,SYSTEM=SY1");
    }

    /**
     * JES3 statements are a fixed set. A card somebody commented out begins the same way and is a
     * comment, not a JES3 statement with a DD for an operand.
     */
    @Test
    void aCommentedOutCardIsAComment() {
        assertThat(JclLineReader
          .readLines("//*SYSTSPRT DD SYSOUT=*"))
          .isEqualTo("^^COMMENT^^//*SYSTSPRT DD SYSOUT=*");
    }

    @Test
    void jes3Continuation() {
        assertThat(JclLineReader
          .readLines(
            """
              //*NET NETID=EXP1,RELEASE=91,2,3,
              //*4,5)
              """
          ))
          .isEqualTo("""
            ^^JES3^^//*NET NETID=EXP1,RELEASE=91,2,3,
            ^^JES3_CONT^^//*4,5)
            """
          );
    }

    @Test
    void ddStream() {
        assertThat(JclLineReader
          .readLines(
            """
              //OBJECT DD *
               REPL DBN=%%NAME.FIELD,SEG=NAME,KEY=(1,2,3),
                 FIELDS=(ABC=XYZ)
               REPL DBN=%%NAME.FIELD,SEG=NAME,KEY=(4,5,6),
                 FIELDS=(ABC=XYZ)
              """
          ))
          .isEqualTo(
            """
              ^^JCL_STATEMENT^^//OBJECT DD *
              ^^STREAM^^ REPL DBN=%%NAME.FIELD,SEG=NAME,KEY=(1,2,3),
              ^^STREAM^^   FIELDS=(ABC=XYZ)
              ^^STREAM^^ REPL DBN=%%NAME.FIELD,SEG=NAME,KEY=(4,5,6),
              ^^STREAM^^   FIELDS=(ABC=XYZ)
              """
          );
    }

    @Test
    void ddStreamWithTrailingComment() {
        assertThat(JclLineReader
          .readLines(
            """
              //OBJECT DD *    * Why is this possible?
               REPL DBN=%%NAME.FIELD,SEG=NAME,KEY=(1,2,3),
                 FIELDS=(ABC=XYZ)
               REPL DBN=%%NAME.FIELD,SEG=NAME,KEY=(4,5,6),
                 FIELDS=(ABC=XYZ)
              """
          ))
          .isEqualTo(
            """
              ^^JCL_STATEMENT^^//OBJECT DD *    ^^TC_START^^* Why is this possible?^^TC_STOP^^
              ^^STREAM^^ REPL DBN=%%NAME.FIELD,SEG=NAME,KEY=(1,2,3),
              ^^STREAM^^   FIELDS=(ABC=XYZ)
              ^^STREAM^^ REPL DBN=%%NAME.FIELD,SEG=NAME,KEY=(4,5,6),
              ^^STREAM^^   FIELDS=(ABC=XYZ)
              """
          );
    }

    @Test
    void ddStreamEnd() {
        assertThat(JclLineReader
          .readLines(
            """
              //OBJECT DD *
               REPL DBN=%%NAME.FIELD,SEG=NAME,KEY=(1,2,3),
                 FIELDS=(ABC=XYZ)
               REPL DBN=%%NAME.FIELD,SEG=NAME,KEY=(4,5,6),
                 FIELDS=(ABC=XYZ)
              /*
              //*
              //JOB1 JOB ,'H.H. MORRILL'
              """
          ))
          .isEqualTo(
            """
              ^^JCL_STATEMENT^^//OBJECT DD *
              ^^STREAM^^ REPL DBN=%%NAME.FIELD,SEG=NAME,KEY=(1,2,3),
              ^^STREAM^^   FIELDS=(ABC=XYZ)
              ^^STREAM^^ REPL DBN=%%NAME.FIELD,SEG=NAME,KEY=(4,5,6),
              ^^STREAM^^   FIELDS=(ABC=XYZ)
              ^^STREAM_END^^/*
              ^^COMMENT^^//*
              ^^JCL_STATEMENT^^//JOB1 JOB ,'H.H. MORRILL'
              """
          );
    }

    @Test
    void trailingCommentAndCommentArea() {
        assertThat(JclLineReader
          .readLines(
            """
              //OBJECT DD *    * Why is this possible?                                commentArea
               REPL DBN=%%NAME.FIELD,SEG=NAME,KEY=(1,2,3),
                 FIELDS=(ABC=XYZ)
               REPL DBN=%%NAME.FIELD,SEG=NAME,KEY=(4,5,6),
                 FIELDS=(ABC=XYZ)
              """
          ))
          .isEqualTo(
            """
              ^^JCL_STATEMENT^^//OBJECT DD *    ^^TC_START^^* Why is this possible?                                ^^TC_STOP^^^^CA_START^^commentArea
              ^^STREAM^^ REPL DBN=%%NAME.FIELD,SEG=NAME,KEY=(1,2,3),
              ^^STREAM^^   FIELDS=(ABC=XYZ)
              ^^STREAM^^ REPL DBN=%%NAME.FIELD,SEG=NAME,KEY=(4,5,6),
              ^^STREAM^^   FIELDS=(ABC=XYZ)
              """
          );
    }

    @Test
    void commentArea() {
        assertThat(JclLineReader
          .readLines("//                                                                      commentArea"))
          .isEqualTo("^^JCL^^//                                                                      ^^CA_START^^commentArea");
    }

    @ParameterizedTest
    @CsvSource(value = {
      "//* Line comment:^^COMMENT^^//* Line comment",
      "//**********************************************************************:^^COMMENT^^//**********************************************************************",
      "//********************************************************************* commentArea:^^COMMENT^^//********************************************************************* ^^CA_START^^commentArea",
      "//*=:^^COMMENT^^//*=",
      "//*-:^^COMMENT^^//*-",
      "//*/:^^COMMENT^^//*/",
      "//*~:^^COMMENT^^//*~",
      "//*:^^COMMENT^^//*",
    }, delimiter = ':')
    void comments(String before, String after) {
        assertThat(JclLineReader
          .readLines("%s".formatted(before)))
          .isEqualTo("%s".formatted(after));
    }

    /**
     * A data line is data in every column. Carving columns 73 on into a comment area cut the text
     * of a member whose lines were never held to 72 columns, and the tree no longer printed back.
     */
    @Test
    void dataPastColumn72() {
        String data = "  " + "x".repeat(78);
        assertThat(JclLineReader
          .readLines(
            """
              //SYSIN    DD *
              %s
              /*
              """.formatted(data)))
          .isEqualTo(
            """
              ^^JCL_STATEMENT^^//SYSIN    DD *
              ^^STREAM^^%s
              ^^STREAM_END^^/*
              """.formatted(data));
    }

    /**
     * With DLM in force, only the delimiter ends the data — a line beginning /* is data, which is
     * the whole point of naming a delimiter.
     */
    @Test
    void dlmKeepsDelimiterLookalikesAsData() {
        assertThat(JclLineReader
          .readLines(
            """
              //SYSTSIN  DD DATA,DLM=$$
              /* Define the keyring */
              RACDCERT ADDRING(ZOWERING) ID(ZWESVUSR)
              $$
              """))
          .isEqualTo(
            """
              ^^JCL_STATEMENT^^//SYSTSIN  DD DATA,DLM=$$
              ^^STREAM^^/* Define the keyring */
              ^^STREAM^^RACDCERT ADDRING(ZOWERING) ID(ZWESVUSR)
              ^^STREAM_END^^$$
              """);
    }

    /**
     * DD DATA exists to pass JCL through: // in the data is data. In a DD * it ends the data.
     */
    @Test
    void ddDataKeepsJclAsData() {
        assertThat(JclLineReader
          .readLines(
            """
              //SYSUT1   DD DATA
              //INNER    JOB
              /*
              //SYSUT2   DD *
              //NEXT     EXEC PGM=X
              """))
          .isEqualTo(
            """
              ^^JCL_STATEMENT^^//SYSUT1   DD DATA
              ^^STREAM^^//INNER    JOB
              ^^STREAM_END^^/*
              ^^JCL_STATEMENT^^//SYSUT2   DD *
              ^^JCL_STATEMENT^^//NEXT     EXEC PGM=X
              """);
    }

    /**
     * A literal too long for one card carries on at column 16 of the next, and may carry on again
     * from there. Whether the second card continues is known only from the first: it has no comma
     * and, on its own, an even number of quotes.
     */
    @Test
    void aLiteralOverThreeCards() {
        assertThat(JclLineReader
          .readLines(
            """
              //INHFS    DD PATH='/usr/cicsts/work/cicsbbbb/CPFWLP/bbbbbbb/CPFWLP/wlp
              //             /usr/servers/defaultServer/logs/messages_20.20.22_22.17.
              //             18.0.log'
              //OUTMVS   DD DISP=SHR,DSN=USER.MESSAGES.LOG
              """))
          .isEqualTo(
            """
              ^^JCL_STATEMENT^^//INHFS    DD PATH='/usr/cicsts/work/cicsbbbb/CPFWLP/bbbbbbb/CPFWLP/wlp
              ^^JCL_CONT^^//             /usr/servers/defaultServer/logs/messages_20.20.22_22.17.
              ^^JCL_CONT^^//             18.0.log'
              ^^JCL_STATEMENT^^//OUTMVS   DD DISP=SHR,DSN=USER.MESSAGES.LOG
              """);
    }

    /**
     * An apostrophe in the comment field is not a quote. Counting it made the statement continue
     * onto the card after it.
     */
    @Test
    void anApostropheInACommentFieldIsNotAQuote() {
        assertThat(JclLineReader
          .readLines(
            """
              //STEP1    EXEC PGM=IEFBR14  DON'T RUN THIS
              //STEP2    EXEC PGM=IEFBR14
              """))
          .isEqualTo(
            """
              ^^JCL_STATEMENT^^//STEP1    EXEC PGM=IEFBR14  DON'T RUN THIS
              ^^JCL_STATEMENT^^//STEP2    EXEC PGM=IEFBR14
              """);
    }

    /**
     * A literal still open at column 72 and closed after it is the operand, not a comment area: the
     * line was never held to 72 columns, and cutting it there would leave the quote unclosed.
     */
    @Test
    void aLiteralClosedPastColumn72IsNotACommentArea() {
        String card = "//${instance-UKO_SERVER_STC_NAME} PROC PARMS='${instance-UKO_SERVER_STC_NAME}'";
        assertThat(card.length()).isGreaterThan(72);
        assertThat(JclLineReader.readLines(card)).isEqualTo("^^JCL_STATEMENT^^" + card);
    }

    @Test
    void isJcl() {
        assertThat(JclLineReader.isJcl("//IBMUSERK  JOB ACCT#,\n// CLASS=A\n")).isTrue();
        assertThat(JclLineReader.isJcl("//* a comment first\n//S1 EXEC PGM=X\n")).isTrue();
        assertThat(JclLineReader.isJcl("//JOBCARD\n//S1 EXEC PGM=X\n")).isTrue();
        assertThat(JclLineReader.isJcl("\n//    SET HLQ='IBMUSER'\n")).isTrue();
        assertThat(JclLineReader.isJcl("/* REXX */\nsay 'hello'\n")).isFalse();
        assertThat(JclLineReader.isJcl("// A C++ comment\nint main() {}\n")).isFalse();
        assertThat(JclLineReader.isJcl("* CICS SIT overrides\nSIT=6$,\n")).isFalse();
        assertThat(JclLineReader.isJcl("")).isFalse();
    }
}
