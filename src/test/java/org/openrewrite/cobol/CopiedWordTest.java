/*
 * Copyright 2026 the original author or authors.
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
package org.openrewrite.cobol;

import org.junit.jupiter.api.Test;
import org.openrewrite.ExecutionContext;
import org.openrewrite.cobol.marker.CopiedWord;
import org.openrewrite.cobol.tree.Cobol;
import org.openrewrite.test.RewriteTest;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.cobol.Assertions.cobol;
import static org.openrewrite.test.RewriteTest.toRecipe;

/**
 * Which copybook wrote each word. The marker names the copybook rather than pointing at the
 * statement that copied it, because a pointer to a node does not survive being stored: the Moderne
 * CLI omits node ids from a build LST and regenerates them on read.
 */
class CopiedWordTest implements RewriteTest {

    private Map<String, String> copiedWords(String source) {
        Map<String, String> byWord = new LinkedHashMap<>();
        rewriteRun(
          spec -> spec.recipe(toRecipe(() -> new CobolIsoVisitor<ExecutionContext>() {
              @Override
              public Cobol.Word visitWord(Cobol.Word word, ExecutionContext ctx) {
                  word.getMarkers().findFirst(CopiedWord.class).ifPresent(copied -> {
                      if (!word.getWord().isEmpty()) {
                          byWord.put(word.getWord(), copied.getCopybook());
                      }
                  });
                  return super.visitWord(word, ctx);
              }
          })),
          cobol(source)
        );
        return byWord;
    }

    @Test
    void namesTheCopybookAWordCameFrom() {
        assertThat(copiedWords(
          """
            000000 IDENTIFICATION DIVISION.                                         *
                   PROGRAM-ID. IC109A.                                              *
                   DATA DIVISION.                                                   *
                   LINKAGE SECTION.                                                 *
                       01  GRP-01.                                                  *
                           COPY INCEPTION_3.                                        *
            """
        )).containsEntry("SUB-CALLED", "INCEPTION_3")
          .containsEntry("DN1", "INCEPTION_3");
    }

    /**
     * The innermost copybook, not the one the program named. INCEPTION copies INCEPTION_2, which
     * copies INCEPTION_3, and the fields are declared in the last of those — which is the file
     * somebody changing them would open.
     */
    @Test
    void namesTheCopybookThatDeclaredIt() {
        assertThat(copiedWords(
          """
            000000 IDENTIFICATION DIVISION.                                         *
                   PROGRAM-ID. IC109A.                                              *
                   DATA DIVISION.                                                   *
                   LINKAGE SECTION.                                                 *
                       01  GRP-01.                                                  *
                           COPY INCEPTION.                                          *
            """
        )).containsEntry("SUB-CALLED", "INCEPTION_3")
          .containsEntry("DN1", "INCEPTION_3");
    }
}
