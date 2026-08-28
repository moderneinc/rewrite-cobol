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
package org.openrewrite.mainframe.controlcard.sort;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openrewrite.mainframe.controlcard.sort.tree.Sort;
import org.openrewrite.test.RewriteTest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.mainframe.controlcard.sort.Assertions.sortCard;

class SortParserTest implements RewriteTest {

    @Test
    void sortIncludeAndOption() {
        rewriteRun(
          sortCard(
            """
              * SRTCLM01 - SORT THE DAILY CLAIM EXTRACT BY TYPE CODE THEN CLAIM
              * NUMBER, KEEPING OPEN AND PENDING CLAIMS ONLY.
                SORT FIELDS=(53,4,CH,A,1,10,CH,A)
                INCLUDE COND=(57,1,CH,EQ,C'O',OR,57,1,CH,EQ,C'P')
                OPTION EQUALS
              """,
            spec -> spec.afterRecipe(cu -> {
                assertThat(cu.getStatements()).hasSize(3);
                Sort.ControlStatement sort = (Sort.ControlStatement) cu.getStatements().get(0);
                assertThat(sort.getOperator().getText()).isEqualTo("SORT");
                assertThat(sort.getParameter("FIELDS").getValueText()).isEqualTo("=(53,4,CH,A,1,10,CH,A)");

                // The lexer breaks the quoted literals out on their own, so the condition is five
                // words that have to read back as one.
                Sort.ControlStatement include = (Sort.ControlStatement) cu.getStatements().get(1);
                assertThat(include.getParameter("COND").getValueText())
                  .isEqualTo("=(57,1,CH,EQ,C'O',OR,57,1,CH,EQ,C'P')");

                // EQUALS takes no value at all.
                Sort.ControlStatement option = (Sort.ControlStatement) cu.getStatements().get(2);
                assertThat(option.getParameter("EQUALS").getValue()).isEmpty();
            })
          )
        );
    }

    @Test
    void statementContinuedByATrailingComma() {
        rewriteRun(
          sortCard(
            """
                SORT FIELDS=(53,4,CH,A)
                SUM FIELDS=(66,13,ZD,79,13,ZD,92,13,ZD)
                OUTREC FIELDS=(1:53,4,8:66,13,ZD,EDIT=(SIIIIIIIIIIT.TT),
                              28:79,13,ZD,EDIT=(SIIIIIIIIIIT.TT),
                              48:92,13,ZD,EDIT=(SIIIIIIIIIIT.TT))
              """,
            spec -> spec.afterRecipe(cu -> {
                assertThat(cu.getStatements()).hasSize(3);
                Sort.ControlStatement outrec = (Sort.ControlStatement) cu.getStatements().get(2);
                assertThat(outrec.getOperator().getText()).isEqualTo("OUTREC");
                assertThat(outrec.getParameter("FIELDS").getValueText()).isEqualTo(
                  "=(1:53,4,8:66,13,ZD,EDIT=(SIIIIIIIIIIT.TT), 28:79,13,ZD,EDIT=(SIIIIIIIIIIT.TT), " +
                  "48:92,13,ZD,EDIT=(SIIIIIIIIIIT.TT))");
            })
          )
        );
    }

    @Test
    void icetoolWritesItsOperandsInParentheses() {
        rewriteRun(
          sortCard(
            """
                SELECT FROM(IN1) TO(OUT1) USING(ABCD) ON(9,7,CH) NODUPS
              """,
            spec -> spec.afterRecipe(cu -> {
                Sort.ControlStatement select = (Sort.ControlStatement) cu.getStatements().get(0);
                assertThat(select.getOperator().getText()).isEqualTo("SELECT");
                assertThat(select.getParameter("FROM").getValueText()).isEqualTo("(IN1)");
                assertThat(select.getParameter("ON").getValueText()).isEqualTo("(9,7,CH)");
                assertThat(select.getParameter("NODUPS").getValue()).isEmpty();
            })
          )
        );
    }

    @Test
    void copyReformatsWithoutOrdering() {
        rewriteRun(
          sortCard(
            """
                SORT   FIELDS=COPY
              """,
            spec -> spec.afterRecipe(cu ->
              assertThat(((Sort.ControlStatement) cu.getStatements().get(0)).getParameter("FIELDS").getValueText())
                .isEqualTo("=COPY"))
          )
        );
    }

    /**
     * A comma separates one operand from the next as readily as a blank does, and the lexer sees no
     * difference between that comma and the ones inside a value.
     */
    @Test
    void operandsSeparatedByCommas() {
        rewriteRun(
          sortCard(
            """
                SORT FIELDS=(1,10,A,53,4,D),FORMAT=CH
                OPTION COPY,VLSHRT
              """,
            spec -> spec.afterRecipe(cu -> {
                Sort.ControlStatement sort = (Sort.ControlStatement) cu.getStatements().get(0);
                assertThat(sort.getParameter("FIELDS").getValueText()).isEqualTo("=(1,10,A,53,4,D)");
                assertThat(sort.getParameter("FORMAT").getValueText()).isEqualTo("=CH");

                Sort.ControlStatement option = (Sort.ControlStatement) cu.getStatements().get(1);
                assertThat(option.getParameters()).hasSize(2);
                assertThat(option.getParameter("VLSHRT")).isNotNull();
            })
          )
        );
    }

    @Test
    void blankAndCommentCardsBetweenStatements() {
        rewriteRun(
          sortCard(
            """
                SORT FIELDS=(1,10,CH,A)

              * WHY THE DUPLICATES GO
                SUM FIELDS=NONE
              """,
            spec -> spec.afterRecipe(cu -> assertThat(cu.getStatements()).hasSize(2))
          )
        );
    }

    /**
     * A member is typed by what its first statement says, so the parser has to refuse a control card
     * that sorts nothing even when it sits in the same library under the same extension.
     */
    @Test
    void refusesAMemberThatIsNotASortDeck(@TempDir Path directory) throws IOException {
        Files.write(directory.resolve("SRTCLM01.ctl"), "  SORT FIELDS=(1,10,CH,A)\n".getBytes(StandardCharsets.UTF_8));
        Files.write(directory.resolve("DEFCLM01.ctl"), "  DELETE CLM.PROD.CLMMAST CLUSTER PURGE\n".getBytes(StandardCharsets.UTF_8));
        Files.write(directory.resolve("PRMCLM01.ctl"), "19981130 19980101 A N\n".getBytes(StandardCharsets.UTF_8));

        SortParser parser = SortParser.builder().build();
        assertThat(parser.accept(directory.resolve("SRTCLM01.ctl"))).isTrue();
        assertThat(parser.accept(directory.resolve("DEFCLM01.ctl"))).isFalse();
        assertThat(parser.accept(directory.resolve("PRMCLM01.ctl"))).isFalse();
    }

    /**
     * {@code INCLUDE} opens a link-edit deck as well, and the two are told apart by the operand and
     * by nothing else.
     */
    @Test
    void includeIsASortStatementOnlyWithItsOwnOperand() {
        assertThat(SortLineReader.isSortDeck("  INCLUDE COND=(1,3,CH,EQ,C'ABC')\n")).isTrue();
        assertThat(SortLineReader.isSortDeck("  INCLUDE OBJLIB(CLMB010)\n")).isFalse();
    }
}
