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
package org.openrewrite.mainframe.bms.tree;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.mainframe.bms.Assertions.bms;

/**
 * The shape of the tree, as against whether it prints back.
 * <p>
 * A flat run of words round-trips perfectly while telling you nothing, so these check that the words
 * are grouped into the macro statements they belong to and that the parts of a statement are what
 * they claim to be.
 */
class MacroStatementTest implements RewriteTest {

    @Test
    void aStatementIsOneNode() {
        rewriteRun(
          bms(
            """
              COSGN00 DFHMSD LANG=COBOL,MODE=INOUT,TIOAPFX=YES
              COSGN0A DFHMDI SIZE=(24,80)
              TRNNAME DFHMDF POS=(1,8),LENGTH=4,ATTRB=(ASKIP,FSET,NORM)
                      DFHMSD TYPE=FINAL
                      END
              """,
            spec -> spec.afterRecipe(cu -> {
                List<Bms.MacroStatement> statements = statementsIn(cu);
                assertThat(statements).hasSize(5);
                assertThat(statements).extracting(Bms.MacroStatement::getSimpleName)
                  .containsExactly("COSGN00", "COSGN0A", "TRNNAME", "", "");
                assertThat(statements).extracting(s -> s.getOperation().getText())
                  .containsExactly("DFHMSD", "DFHMDI", "DFHMDF", "DFHMSD", "END");

                Bms.MacroStatement field = statements.get(2);
                assertThat(field.isOperation("DFHMDF")).isTrue();
                assertThat(field.getParameter("POS")).isNotNull();
                assertThat(field.getParameter("POS").getValueText()).isEqualTo("(1,8)");
                assertThat(field.getParameter("LENGTH").getValueText()).isEqualTo("4");
                assertThat(field.getParameter("ATTRB").getValueText()).isEqualTo("(ASKIP,FSET,NORM)");
                assertThat(field.getParameter("COLOR")).isNull();
            })
          )
        );
    }

    /**
     * The name field begins in column 1, so a statement without one is told apart by where its
     * operation starts and by nothing else.
     */
    @Test
    void aStatementWithNoNameField() {
        rewriteRun(
          bms(
            """
              COSGN0A DFHMDI SIZE=(24,80)
                      DFHMDF POS=(1,1),LENGTH=6,INITIAL='Tran :'
              """,
            spec -> spec.afterRecipe(cu -> {
                Bms.MacroStatement field = statementsIn(cu).get(1);
                assertThat(field.getName()).isNull();
                assertThat(field.getSimpleName()).isEmpty();
                assertThat(field.getOperation().getText()).isEqualTo("DFHMDF");
                assertThat(field.getParameter("INITIAL").getValueText()).isEqualTo("'Tran :'");
            })
          )
        );
    }

    /**
     * A statement written over several lines is still one statement. Nothing in the text of a
     * continuation line says so — it is a continuation because of column 72 of the line above it.
     */
    @Test
    void continuationGathersOneStatement() {
        rewriteRun(
          bms(
            """
              COSGN00 DFHMSD CTRL=(ALARM,FREEKB),                                    -
                             EXTATT=YES,                                             -
                             LANG=COBOL,                                             -
                             TYPE=&&SYSPARM
              """,
            spec -> spec.afterRecipe(cu -> {
                List<Bms.MacroStatement> statements = statementsIn(cu);
                assertThat(statements).hasSize(1);
                Bms.MacroStatement mapset = statements.get(0);
                assertThat(mapset.getSimpleName()).isEqualTo("COSGN00");
                assertThat(mapset.getParameter("CTRL").getValueText()).isEqualTo("(ALARM,FREEKB)");
                assertThat(mapset.getParameter("EXTATT").getValueText()).isEqualTo("YES");
                assertThat(mapset.getParameter("LANG").getValueText()).isEqualTo("COBOL");
                assertThat(mapset.getParameter("TYPE").getValueText()).isEqualTo("&&SYSPARM");
            })
          )
        );
    }

    /**
     * Any character in column 72 continues the statement; the corpus writes all three of these.
     */
    @Test
    void anyContinuationCharacter() {
        rewriteRun(
          bms(
            """
              SSMAP   DFHMSD TYPE=MAP,MODE=INOUT,                                    X
                             LANG=COBOL,                                             *
                             STORAGE=AUTO,                                           -
                             TIOAPFX=YES
              """,
            spec -> spec.afterRecipe(cu -> assertThat(statementsIn(cu)).hasSize(1))
          )
        );
    }

    /**
     * A comment line is a statement of its own, and does not swallow the macro that follows it.
     */
    @Test
    void commentLines() {
        rewriteRun(
          bms(
            """
              ******************************************************************
              *    CardDemo - Login Screen
              ******************************************************************
              COSGN00 DFHMSD LANG=COBOL
              """,
            spec -> spec.afterRecipe(cu -> {
                assertThat(cu.getStatements()).hasSize(4);
                assertThat(cu.getStatements().subList(0, 3)).allMatch(s -> s instanceof Bms.Comment);
                assertThat(statementsIn(cu)).hasSize(1);
            })
          )
        );
    }

    @Test
    void quotedValueHoldingCommasAndBlanks() {
        rewriteRun(
          bms(
            """
              COSGN0A DFHMDI SIZE=(24,80)
                      DFHMDF POS=(24,1),LENGTH=22,INITIAL='ENTER=Sign-on, F3=Exit'
              """,
            spec -> spec.afterRecipe(cu -> {
                Bms.MacroStatement field = statementsIn(cu).get(1);
                assertThat(field.getParameters()).hasSize(3);
                assertThat(field.getParameter("INITIAL").getValueText())
                  .isEqualTo("'ENTER=Sign-on, F3=Exit'");
            })
          )
        );
    }

    private static List<Bms.MacroStatement> statementsIn(Bms.CompilationUnit cu) {
        return cu.getStatements().stream()
          .filter(Bms.MacroStatement.class::isInstance)
          .map(Bms.MacroStatement.class::cast)
          .collect(Collectors.toList());
    }
}
