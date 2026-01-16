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

class JobTest implements RewriteTest {

    @ParameterizedTest
    @ValueSource(
      strings = {
        "NAME JOB",
        "     JOB",
        "     JOB                                                              commentArea"
      }
    )
    void job(String input) {
        rewriteRun(
          jcl(
            "//%s".formatted(input)
          )
        );
    }

    @Test
    void parameterLiteral() {
        rewriteRun(
          jcl("//Name JOB 'name'")
        );
    }

    @Test
    void parameterLiteralStartsWithComma() {
        rewriteRun(
                jcl("//JOB1 JOB ,'H.H. MORRILL'")
        );
    }

    @Test
    void parameterAssignment() {
        rewriteRun(
          jcl("//Name JOB CLASS=A")
        );
    }

    @Test
    void specialCharacters() {
        rewriteRun(
          jcl("//Name JOB CLASS='3400-6'")
        );
    }

    @Test
    void parensAssignment() {
        rewriteRun(
          jcl("//Name JOB MSGLEVEL=(1,1)")
        );
    }

    @Test
    void startsWithComma() {
        rewriteRun(
          jcl("//Name JOB (,DEPTD58,921)")
        );
    }

    @Test
    void multipleParameterTypes() {
        rewriteRun(
          jcl("//Name JOB 'name',CLASS=A,MSGLEVEL=(1,1)")
        );
    }
}
