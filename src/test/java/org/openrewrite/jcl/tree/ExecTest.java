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

class ExecTest implements RewriteTest {

    @ParameterizedTest
    @ValueSource(
      strings = {
        "NAME EXEC",
        "     EXEC",
        "     EXEC                                                             commentArea"
      }
    )
    void exec(String input) {
        rewriteRun(
          jcl(
            "//%s".formatted(input)
          )
        );
    }

    @Test
    void parameterAssignment() {
        rewriteRun(
          jcl("//Name EXEC PGM=WT1")
        );
    }

    @Test
    void specialCharacters() {
        rewriteRun(
          jcl("//Name EXEC PGM='3400-6'")
        );
    }

    @Test
    void parensAssignment() {
        rewriteRun(
          jcl("//Name EXEC COND.PSTEP3=(4,GT,PSTEP1)")
        );
    }

    @Test
    void startsWithComma() {
        rewriteRun(
          jcl("//Name EXEC TIME=(,50)")
        );
    }

    @Test
    void multipleParameterTypes() {
        rewriteRun(
          jcl("//Name EXEC COND.PSTEP3=(4,GT,PSTEP1),RD=R")
        );
    }

    @Test
    void procParameter() {
        rewriteRun(
          jcl("//Name EXEC PROC=WRIT35")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
      """
        //Name EXEC DSNAME=DS4,           *trailing comment                     commentArea
        //        DISP=(NEW,KEEP),
        //        SPACE=(TRK,(5,1,2))
        """,
      "//Name EXEC DSNAME=DS4,DISP=(NEW,KEEP)  *trailing comment               commentArea",
      "//Name EXEC DSNAME=DS4,DISP=(NEW,KEEP)                                 *",
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
