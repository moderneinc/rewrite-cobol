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
package org.openrewrite.mainframe.cobol.trait;

import org.junit.jupiter.api.Test;
import org.openrewrite.ExecutionContext;
import org.openrewrite.mainframe.cobol.CobolIsoVisitor;
import org.openrewrite.mainframe.cobol.tree.Cobol;
import org.openrewrite.test.RewriteTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.mainframe.cobol.Assertions.cobol;
import static org.openrewrite.test.RewriteTest.toRecipe;

/**
 * Where a statement sits, read from its cursor. Analyses used to track this as visitor state, which
 * meant extending a base class to get at it and meant the answer outlived what it was about.
 */
class ProcedureTest implements RewriteTest {

    /**
     * Every {@code DISPLAY} in the source, as "program/procedure".
     */
    private List<String> locations(String source) {
        List<String> found = new ArrayList<>();
        rewriteRun(
          spec -> spec.recipe(toRecipe(() -> new CobolIsoVisitor<ExecutionContext>() {
              @Override
              public Cobol.Display visitDisplay(Cobol.Display display, ExecutionContext ctx) {
                  found.add(Program.nameOf(getCursor()) + "/" + Procedure.nameOf(getCursor()));
                  return super.visitDisplay(display, ctx);
              }
          })),
          cobol(source)
        );
        return found;
    }

    /**
     * A paragraph within a section answers with the paragraph: it is the finer of the two names, and
     * the one a {@code PERFORM} would have to use to reach the statement.
     */
    @Test
    void readsTheParagraphWithinASection() {
        assertThat(locations(
          """
            000000 IDENTIFICATION DIVISION.                                        \s
            000000 PROGRAM-ID. SECTPGM.                                            \s
            000000 PROCEDURE DIVISION.                                             \s
            000000 MAIN-SECTION SECTION.                                           \s
            000000 FIRST-PARA.                                                     \s
            000000     DISPLAY 'ONE'.                                              \s
            000000 SECOND-PARA.                                                    \s
            000000     DISPLAY 'TWO'.                                              \s
            """
        )).containsExactly("SECTPGM/FIRST-PARA", "SECTPGM/SECOND-PARA");
    }

    /**
     * The name stops applying at the end of what it names. Tracked as visitor state it did not: a
     * statement after the last paragraph kept reporting that paragraph.
     */
    @Test
    void readsNothingOutsideAnyProcedure() {
        assertThat(locations(
          """
            000000 IDENTIFICATION DIVISION.                                        \s
            000000 PROGRAM-ID. FLATPGM.                                            \s
            000000 PROCEDURE DIVISION.                                             \s
            000000     DISPLAY 'ONE'.                                              \s
            """
        )).containsExactly("FLATPGM/");
    }

    /**
     * An analysis that reports once per file asks at the compilation unit, where there is no program
     * above to find.
     */
    @Test
    void readsTheProgramOfTheFileItself() {
        List<String> found = new ArrayList<>();
        rewriteRun(
          spec -> spec.recipe(toRecipe(() -> new CobolIsoVisitor<ExecutionContext>() {
              @Override
              public Cobol.CompilationUnit visitCompilationUnit(Cobol.CompilationUnit cu, ExecutionContext ctx) {
                  found.add(Program.nameOf(getCursor()));
                  return super.visitCompilationUnit(cu, ctx);
              }
          })),
          cobol(
            """
              000000 IDENTIFICATION DIVISION.                                        \s
              000000 PROGRAM-ID. FILEPGM.                                            \s
              000000 PROCEDURE DIVISION.                                             \s
              000000     GOBACK.                                                     \s
              """
          )
        );

        assertThat(found).containsExactly("FILEPGM");
    }

    @Test
    void readsTheInnermostProgram() {
        assertThat(locations(
          """
            000000 IDENTIFICATION DIVISION.                                        \s
            000000 PROGRAM-ID. OUTERPGM.                                           \s
            000000 PROCEDURE DIVISION.                                             \s
            000000 OUTER-PARA.                                                     \s
            000000     DISPLAY 'OUTER'.                                            \s
            000000 IDENTIFICATION DIVISION.                                        \s
            000000 PROGRAM-ID. INNERPGM.                                           \s
            000000 PROCEDURE DIVISION.                                             \s
            000000 INNER-PARA.                                                     \s
            000000     DISPLAY 'INNER'.                                            \s
            000000 END PROGRAM INNERPGM.                                           \s
            000000 END PROGRAM OUTERPGM.                                           \s
            """
        )).containsExactly("OUTERPGM/OUTER-PARA", "INNERPGM/INNER-PARA");
    }
}
