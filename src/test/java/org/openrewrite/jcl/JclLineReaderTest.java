/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.jcl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

public class JclLineReaderTest {

    @Test
    void controlM() {
        assertThat(JclLineReader
          .readLines("%%LIBSYM NAME.FIELD %%MEMSYM NAME.FIELD"))
          .isEqualTo("^^CM^^%%LIBSYM NAME.FIELD %%MEMSYM NAME.FIELD");
    }

    @Test
    void jcl() {
        assertThat(JclLineReader
          .readLines("//JOB1 JOB ,'H.H. MORRILL'"))
          .isEqualTo("^^JCL_STATEMENT^^//JOB1 JOB ,'H.H. MORRILL'");
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
          .readLines("//*SYSTSPRT DD SYSOUT=*"))
          .isEqualTo("^^JES3^^//*SYSTSPRT DD SYSOUT=*");
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
          .readLines("//NAME                                                                  commentArea"))
          .isEqualTo("^^JCL_STATEMENT^^//NAME                                                                  ^^CA_START^^commentArea");
    }

    @ParameterizedTest
    @CsvSource(value = {
      "//* Line comment:^^COMMENT^^//* Line comment",
      "//**********************************************************************:^^COMMENT^^//**********************************************************************",
      "//********************************************************************* commentArea:^^COMMENT^^//********************************************************************* ^^CA_START^^commentArea",
    }, delimiter = ':')
    void comments(String before, String after) {
        assertThat(JclLineReader
          .readLines("%s".formatted(before)))
          .isEqualTo("%s".formatted(after));
    }
}
