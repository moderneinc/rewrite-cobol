/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.jcl.tree;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.jcl.tree.ParserAssertions.jcl;

public class DataDefinitionTest implements RewriteTest {

    @Test
    void dd() {
        rewriteRun(
          jcl("//Name DD")
        );
    }

    @Test
    void parameterAssignment() {
        rewriteRun(
          jcl("//Name DD DSNAME=ALPHA.PGM")
        );
    }

    @Test
    void specialCharacters() {
        rewriteRun(
          jcl("//Name DD DSNAME='3400-6'")
        );
    }

    @Test
    void multiAssignment() {
        rewriteRun(
          jcl("//Name DD VOLUME=SER=389984")
        );
    }
    @Test
    void nameWithParameter() {
        rewriteRun(
          jcl("//Name DD DSNAME=REPORT.THREE(WEEK3)")
        );
    }

    @Test
    void parensAssignment() {
        rewriteRun(
          jcl("//Name DD DISP=(NEW,KEEP)")
        );
    }

    @Test
    void startsWithComma() {
        rewriteRun(
          jcl("//Name DD DISP=(,KEEP)")
        );
    }

    @Test
    void multipleParameterTypes() {
        rewriteRun(
          jcl("//Name DD DSNAME=DS4,DISP=(NEW,KEEP),SPACE=(TRK,(5,1,2))")
        );
    }

    @Test
    void outputParameter() {
        rewriteRun(
          jcl("//Name DD OUTPUT=(*.OUT1,*.OUT2)")
        );
    }

    @Test
    void ddStream() {
        rewriteRun(
          jcl(
            """
              //OBJECT DD *
               REPL DBN=%%NAME.FIELD,SEG=NAME,KEY=(1,2,3),
                 FIELDS=(ABC=XYZ)
               REPL DBN=%%NAME.FIELD,SEG=NAME,KEY=(4,5,6),
                 FIELDS=(ABC=XYZ)
              /*
              """
          )
        );
    }

    @Test
    void ddStreamLiteral() {
        rewriteRun(
          jcl(
            """
              //OBJECT DD *
              SUBJECT:'%%JOBNAME -                                                  ' commentArea
              //JOB1 JOB ,'H.H. MORRILL'
              """
          )
        );
    }

    @Test
    void commentAreaOnDDStream() {
        rewriteRun(
          jcl(
            """
              //OBJECT DD *    * Why is this possible?                                commentArea
                REPL DBN=%%NAME.FIELD,SEG=NAME,KEY=(1,2,3),                           commentArea
                 FIELDS=(ABC=XYZ)                                                     commentArea
                REPL DBN=%%NAME.FIELD,SEG=NAME,KEY=(4,5,6),                           commentArea
                 FIELDS=(ABC=XYZ)                                                     commentArea
              /*
              """
          )
        );
    }

    @Test
    void trailingCommentAndCommentArea() {
        rewriteRun(
          jcl(
            """
              //OBJECT DD *    * Why is this possible?                                commentArea
                REPL DBN=%%NAME.FIELD,SEG=NAME,KEY=(1,2,3),                           commentArea
                 FIELDS=(ABC=XYZ)                                                     commentArea
                REPL DBN=%%NAME.FIELD,SEG=NAME,KEY=(4,5,6),                           commentArea
                 FIELDS=(ABC=XYZ)                                                     commentArea
              /*
              """
          )
        );
    }

    @Test
    void commentAreaAfterStreamEnd() {
        rewriteRun(
          jcl(
            """
              //OBJECT DD *    * Why is this possible?                                commentArea
                REPL DBN=%%NAME.FIELD,SEG=NAME,KEY=(1,2,3),
                 FIELDS=(ABC=XYZ)
                REPL DBN=%%NAME.FIELD,SEG=NAME,KEY=(4,5,6),
                 FIELDS=(ABC=XYZ)
              /*                                                                      commentArea
              """
          )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
      "SYSUID",
      "%SYSUID",
      "%NAME..%OTHERNAME",
      "OBJECT(&MBR)",
      "(NAME-PART,EQ,C'OTHER')",
      "LIST AGGREGATE(%%NAME) - OUTDATASET(%%OTHERNAME)"
    })
    void ddNames(String input) {
        rewriteRun(
          jcl(
            """
            //JOB1 DD *
              %s
            """.formatted(input)
          )
        );
    }

}
