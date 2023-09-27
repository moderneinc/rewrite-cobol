/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.jcl.tree;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.jcl.tree.ParserAssertions.jcl;

public class IfTest implements RewriteTest {

    @ParameterizedTest
    @ValueSource(
      strings = {
        "NAME IF (1 EQ 1) THEN",
        "     IF (1 EQ 1) THEN",
        "     IF (1 EQ 1) THEN                                                 commentArea"
      }
    )
    void ifStatement(String input) {
        rewriteRun(
          jcl(
            """
              //%s
              //Name EXPORT DSNAME='3400-6'
              // ENDIF
              """.formatted(input)
          )
        );
    }

    @ParameterizedTest
    @ValueSource(
      strings = {
        "NAME ELSE",
        "     ELSE                                                             commentArea"
      }
    )
    void ifElseStatement(String input) {
        rewriteRun(
          jcl(
            """
              //Name IF (1 EQ 1) THEN
              //Name EXPORT DSNAME='3400-6'
              //%s
              //Name EXPORT DSNAME='3400-6'
              // ENDIF
              """.formatted(input)
          )
        );
    }

    @ParameterizedTest
    @ValueSource(
      strings = {
        "NAME ENDIF",
        "     ENDIF                                                            commentArea"
      }
    )
    void endif(String input) {
        rewriteRun(
          jcl(
            """
              //Name IF (1 EQ 1) THEN
              //Name EXPORT DSNAME='3400-6'
              //%s
              """.formatted(input)
          )
        );
    }
}
