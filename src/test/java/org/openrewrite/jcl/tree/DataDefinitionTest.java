/*
 * Copyright 2024 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.jcl.tree;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.jcl.tree.ParserAssertions.jcl;

class DataDefinitionTest implements RewriteTest {

    @ParameterizedTest
    @ValueSource(
      strings = {
        "NAME DD",
        "     DD",
        "     DD                                                               commentArea",
      }
    )
    void dd(String input) {
        rewriteRun(
          jcl(
            "//%s".formatted(input)
          )
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

    @ParameterizedTest
    @ValueSource(strings = {
        """
          //Name DD DSNAME=DS4,           *trailing comment                       commentArea
          //        DISP=(NEW,KEEP),                                              commentArea
          //        SPACE=(CYC,(1,1),),                                           commentArea
          //        DCB=(A=FB,B=80,C=0)                                           commentArea
          """,
        """
          //Name DD DSNAME=DS4,DISP=(NEW,KEEP)  *trailing comment                 commentArea
          """,
        """
          //Name DD DSNAME=DS4,DISP=(NEW,KEEP)  <=trailing comment                 commentArea
          """,
      }
    )
    void trailingComments(String input) {
        rewriteRun(
          jcl(
            "%s".formatted(input)
          )
        );
    }
}
