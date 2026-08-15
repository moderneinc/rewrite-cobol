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
package org.openrewrite.cobol.tree.cobol;

import org.junit.jupiter.api.Test;
import org.openrewrite.cobol.CobolTest;

import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.cobol.Assertions.cobol;

/**
 * A change set is keyed by source file id, so two compilation units sharing one would have every
 * diff attributed to whichever of them was seen last.
 */
class CompilationUnitIdentityTest extends CobolTest {

    @Test
    void twoProgramsAreTwoSourceFiles() {
        Set<UUID> ids = new LinkedHashSet<>();
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                        \s
              000002 PROGRAM-ID. ONE.                                                \s
              """,
            spec -> spec.path("ONE.CBL").afterRecipe(cu -> ids.add(cu.getId()))
          ),
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                        \s
              000002 PROGRAM-ID. TWO.                                                \s
              """,
            spec -> spec.path("TWO.CBL").afterRecipe(cu -> ids.add(cu.getId()))
          )
        );
        assertThat(ids).hasSize(2);
    }

    /**
     * A moved file is the same file at a different path, and that is only readable while the id
     * stays put — which is why identity is a field rather than something derived from the path.
     */
    @Test
    void theIdSurvivesARename() {
        rewriteRun(
          cobol(
            """
              000001 IDENTIFICATION DIVISION.                                        \s
              000002 PROGRAM-ID. ONE.                                                \s
              """,
            spec -> spec.path("ONE.CBL").afterRecipe(cu ->
              assertThat(cu.withSourcePath(Paths.get("moved/ONE.CBL")).getId()).isEqualTo(cu.getId()))
          )
        );
    }
}
