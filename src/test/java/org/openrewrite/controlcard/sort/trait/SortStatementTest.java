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
package org.openrewrite.controlcard.sort.trait;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.controlcard.sort.Assertions.sortCard;

class SortStatementTest implements RewriteTest {

    @Test
    void readsTheControlFieldsAndTheCondition() {
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
                List<SortStatement> statements = new SortStatement.Matcher().lower(cu).collect(Collectors.toList());
                assertThat(statements).hasSize(3);

                SortStatement sort = statements.get(0);
                assertThat(sort.getOperator()).isEqualTo("SORT");
                assertThat(sort.isCopy()).isFalse();
                assertThat(sort.getFields()).containsExactly(
                  new SortStatement.Field(53, 4, "CH", "A"),
                  new SortStatement.Field(1, 10, "CH", "A"));
                // The comment cards do not shift the line the statement was written on.
                assertThat(sort.getLine()).isEqualTo(3);

                SortStatement include = statements.get(1);
                assertThat(include.getOperator()).isEqualTo("INCLUDE");
                assertThat(include.getOperand("COND")).isEqualTo("57,1,CH,EQ,C'O',OR,57,1,CH,EQ,C'P'");
                // Only a SORT, MERGE or SUM has control fields; a condition is not a list of keys.
                assertThat(include.getFields()).isEmpty();

                assertThat(statements.get(2).hasOperand("EQUALS")).isTrue();
            })
          )
        );
    }

    /**
     * A {@code SUM} field carries no order, so the parts a field is written with have to be
     * recognised by what they look like rather than counted off.
     */
    @Test
    void sumFieldsHaveNoOrder() {
        rewriteRun(
          sortCard(
            """
                SORT FIELDS=(53,4,CH,A)
                SUM FIELDS=(66,13,ZD,79,13,ZD,92,13,ZD)
              """,
            spec -> spec.afterRecipe(cu -> {
                List<SortStatement> statements = new SortStatement.Matcher().lower(cu).collect(Collectors.toList());
                assertThat(statements.get(1).getFields()).containsExactly(
                  new SortStatement.Field(66, 13, "ZD", null),
                  new SortStatement.Field(79, 13, "ZD", null),
                  new SortStatement.Field(92, 13, "ZD", null));
            })
          )
        );
    }

    /**
     * With a {@code FORMAT} operand the format is left off each field, so a three-part field is a
     * position, a length and an order.
     */
    @Test
    void formatWrittenOnceForEveryField() {
        rewriteRun(
          sortCard(
            """
                SORT FIELDS=(1,10,A,53,4,D),FORMAT=CH
              """,
            spec -> spec.afterRecipe(cu -> {
                SortStatement sort = new SortStatement.Matcher().lower(cu).collect(Collectors.toList()).get(0);
                assertThat(sort.getOperand("FORMAT")).isEqualTo("CH");
                assertThat(sort.getFields()).containsExactly(
                  new SortStatement.Field(1, 10, null, "A"),
                  new SortStatement.Field(53, 4, null, "D"));
            })
          )
        );
    }

    @Test
    void copyOrdersNothing() {
        rewriteRun(
          sortCard(
            """
                SORT   FIELDS=COPY
              """,
            spec -> spec.afterRecipe(cu -> {
                SortStatement sort = new SortStatement.Matcher().lower(cu).collect(Collectors.toList()).get(0);
                assertThat(sort.isCopy()).isTrue();
                assertThat(sort.getFields()).isEmpty();
            })
          )
        );
    }
}
