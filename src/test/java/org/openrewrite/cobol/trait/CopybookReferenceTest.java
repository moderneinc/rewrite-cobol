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
package org.openrewrite.cobol.trait;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openrewrite.ExecutionContext;
import org.openrewrite.test.RewriteTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.cobol.Assertions.cobol;
import static org.openrewrite.test.RewriteTest.toRecipe;

class CopybookReferenceTest implements RewriteTest {

    private List<CopybookReference> parse(String source) {
        List<CopybookReference> found = new ArrayList<>();
        rewriteRun(
          spec -> spec.recipe(toRecipe(() ->
            new CopybookReference.Matcher().<ExecutionContext>asVisitor((reference, ctx) -> {
                found.add(reference);
                return reference.getTree();
            }))),
          cobol(source)
        );
        return found;
    }

    /**
     * {@code COPY} and {@code EXEC SQL INCLUDE} are unrelated node types holding the name in
     * different places. They mean the same thing, so they read the same here.
     */
    @ParameterizedTest
    @ValueSource(strings = {
      "COPY INCEPTION_3.",
      "EXEC SQL INCLUDE INCEPTION_3 END-EXEC.",
    })
    void readsEitherWayOfWritingIt(String written) {
        List<CopybookReference> references = parse(
          """
            000000 IDENTIFICATION DIVISION.                                         *
                   PROGRAM-ID. IC109A.                                              *
                   DATA DIVISION.                                                   *
                   LINKAGE SECTION.                                                 *
                       01  GRP-01.                                                  *
                           %s
            """.formatted(written)
        );

        assertThat(references).singleElement().satisfies(reference -> {
            assertThat(reference.getName()).isEqualTo("INCEPTION_3");
            assertThat(reference.isResolved()).isTrue();
            assertThat(reference.isMissing()).isFalse();
        });
    }

    @Test
    void reportsACopybookNothingResolvedTo() {
        List<CopybookReference> references = parse(
          """
            000000 IDENTIFICATION DIVISION.                                         *
                   PROGRAM-ID. IC109A.                                              *
                   DATA DIVISION.                                                   *
                   LINKAGE SECTION.                                                 *
                       01  GRP-01.                                                  *
                           COPY NOSUCHBOOK.                                         *
            """
        );

        assertThat(references).singleElement().satisfies(reference -> {
            assertThat(reference.isMissing()).isTrue();
            assertThat(reference.isResolved()).isFalse();
            assertThat(reference.getSourcePath()).isNull();
        });
    }

    /**
     * A copybook that copies another is reached too, and reached once. INCEPTION copies INCEPTION_2,
     * which copies INCEPTION_3, and preprocessing hangs each of the three off a word of the program
     * — so walking into a resolved copybook as well reports the inner ones once per level above them.
     */
    @Test
    void reachesACopybookThatCopiesAnotherExactlyOnce() {
        List<CopybookReference> references = parse(
          """
            000000 IDENTIFICATION DIVISION.                                         *
                   PROGRAM-ID. IC109A.                                              *
                   DATA DIVISION.                                                   *
                   LINKAGE SECTION.                                                 *
                       01  GRP-01.                                                  *
                           COPY INCEPTION.                                          *
            """
        );

        assertThat(references).extracting(CopybookReference::getName)
          .containsExactly("INCEPTION", "INCEPTION_2", "INCEPTION_3");
    }

    /**
     * The word the reference hangs off is where the program text around it is, which is what a
     * caller reports as context.
     */
    @Test
    void knowsTheWordItHangsOff() {
        List<CopybookReference> references = parse(
          """
            000000 IDENTIFICATION DIVISION.                                         *
                   PROGRAM-ID. IC109A.                                              *
                   DATA DIVISION.                                                   *
                   LINKAGE SECTION.                                                 *
                       01  GRP-01.                                                  *
                           COPY INCEPTION_3.                                        *
            """
        );

        assertThat(references).singleElement().satisfies(reference ->
          assertThat(reference.getEnclosingWord()).isNotNull());
    }
}
