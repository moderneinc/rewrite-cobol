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
package org.openrewrite.jcl.tree;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.jcl.tree.ParserAssertions.jcl;

class IfTest implements RewriteTest {

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


    @Test
    void multipleIfs() {
        rewriteRun(
          jcl(
            """
              //Name IF (1 EQ 1) THEN                                                 commentArea
              //Name IF (1 EQ 1) THEN                                                 commentArea
              //Name EXPORT DSNAME='3400-6'                                           commentArea
              //* comment                                                             commentArea
              // ENDIF                                                                commentArea
              //* comment                                                             commentArea
              // ENDIF                                                                commentArea
              //                                                                      commentArea
              """
          )
        );
    }
}
