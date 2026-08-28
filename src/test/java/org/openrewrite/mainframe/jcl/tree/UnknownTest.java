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
package org.openrewrite.mainframe.jcl.tree;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.mainframe.jcl.tree.ParserAssertions.jcl;

class UnknownTest implements RewriteTest {

    @Test
    void unknown() {
        rewriteRun(
          jcl(
            """
              //* %%RANGE 1 42
                TRACE OFF
                MAXCOMMAND 42
                CALLMEM SET42
                CALLMEM SET42
                 SETOLOC %%MAXRC = 42
              %%RANGE 1 42
              CALLMEM ORDERJOB %%LIBRO LCOPYJCL ALL
              %%DATTR FORCE C
              %% RANGE 1 42
              """
          )
        );
    }

    @Test
    void unknownWithCommentAreas() {
        rewriteRun(
          jcl(
            """
              *---------------------------------------------------------------------- commentArea
                                                                                      commentArea
                                                                                      'literal  '
                TRACE OFF                                                             commentArea
                MAXCOMMAND 42                                                         commentArea
                CALLMEM SET42                                                         commentArea
                CALLMEM SET42                                                         commentArea
                 SETOLOC %%MAXRC = 42                                                 commentArea
              %%RANGE 1 42                                                            commentArea
              CALLMEM ORDERJOB %%LIBRO LCOPYJCL ALL                                   commentArea
              %%DATTR FORCE C                                                         commentArea
              %% RANGE 1 42                                                           commentArea
              """
          )
        );
    }
}
