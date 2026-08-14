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
package org.openrewrite.jcl.tree;

import org.junit.jupiter.api.Test;
import org.openrewrite.jcl.JclIsoVisitor;
import org.openrewrite.test.RewriteTest;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.jcl.Assertions.jcl;

/**
 * The shape of the tree, as against whether it prints back.
 * <p>
 * Every other test here checks that parsing and printing round-trips, and a flat run of words
 * round-trips perfectly while telling you nothing. These check that the words are grouped into the
 * statements they belong to and that the parts of a statement are what they claim to be.
 */
class JobControlStatementTest implements RewriteTest {

    @Test
    void aStatementIsOneNode() {
        rewriteRun(
          jcl(
            """
              //ACCTJOB  JOB (ACCT),'DAILY POST',CLASS=A
              //STEP010  EXEC PGM=ACCTPOST
              //ACCTDD   DD  DSN=PROD.ACCOUNT.MASTER,DISP=SHR
              """,
            spec -> spec.afterRecipe(cu -> {
                List<Jcl.JobControlStatement> statements = statementsIn(cu);
                assertThat(statements).hasSize(3);
                assertThat(statements).extracting(Jcl.JobControlStatement::getSimpleName)
                  .containsExactly("ACCTJOB", "STEP010", "ACCTDD");
                assertThat(statements).extracting(s -> s.getOperation().getText())
                  .containsExactly("JOB", "EXEC", "DD");

                Jcl.JobControlStatement dd = statements.get(2);
                assertThat(dd.isOperation("DD")).isTrue();
                assertThat(dd.getParameter("DSN")).isNotNull();
                assertThat(dd.getParameter("DSN").getValueText()).isEqualTo("PROD.ACCOUNT.MASTER");
                assertThat(dd.getParameter("DISP").getValueText()).isEqualTo("SHR");
                assertThat(dd.getParameter("UNIT")).isNull();
            })
          )
        );
    }

    /**
     * The whole reason the words have to be grouped: a statement written over four lines is one
     * statement, and its parameters belong to it wherever they were written.
     */
    @Test
    void aContinuedStatementIsStillOneNode() {
        rewriteRun(
          jcl(
            """
              //ACCTDD   DD  DSN=PROD.ACCOUNT.MASTER,
              //             DISP=SHR,
              //             UNIT=SYSDA,
              //             SPACE=(CYL,(1,1),RLSE)
              """,
            spec -> spec.afterRecipe(cu -> {
                assertThat(statementsIn(cu)).singleElement().satisfies(dd -> {
                    assertThat(dd.getParameters()).hasSize(4);
                    assertThat(dd.getParameter("DISP").getValueText()).isEqualTo("SHR");
                    assertThat(dd.getParameter("UNIT").getValueText()).isEqualTo("SYSDA");
                    // The commas inside SPACE belong to its sub-parameters and must not split it.
                    assertThat(dd.getParameter("SPACE").getValueText()).isEqualTo("(CYL,(1,1),RLSE)");
                });
            })
          )
        );
    }

    /**
     * A statement continues when its operand field ends with a comma, and the comment field that
     * follows on the same line does not change that.
     */
    @Test
    void aCommentFieldDoesNotEndAStatement() {
        rewriteRun(
          jcl(
            """
              //ACCTDD   DD  DSN=PROD.ACCOUNT.MASTER,           THE MASTER
              //             DISP=SHR
              """,
            spec -> spec.afterRecipe(cu ->
              assertThat(statementsIn(cu)).singleElement().satisfies(dd -> {
                  assertThat(dd.getParameter("DISP").getValueText()).isEqualTo("SHR");
                  // The comment field is kept so the statement prints back, but it is not a parameter.
                  assertThat(dd.getParameters()).hasSize(2);
                  assertThat(dd.getOperands()).hasSizeGreaterThan(2);
              }))
          )
        );
    }

    @Test
    void tellsAKeywordFromAPositionalParameter() {
        rewriteRun(
          jcl(
            """
              //SYSIN    DD  DUMMY
              //SORTIN   DD  DSN=PROD.TRAN.DAILY,DISP=SHR
              """,
            spec -> spec.afterRecipe(cu -> {
                List<Jcl.JobControlStatement> statements = statementsIn(cu);
                assertThat(statements.get(0).getParameters()).singleElement()
                  .isInstanceOf(Jcl.PositionalParameter.class)
                  .satisfies(p -> assertThat(((Jcl.PositionalParameter) p).getValueText()).isEqualTo("DUMMY"));
                assertThat(statements.get(1).getParameters())
                  .allMatch(Jcl.KeywordParameter.class::isInstance);
            })
          )
        );
    }

    /**
     * A quoted string is its own token, so a parameter list containing one used to arrive as three
     * unrelated words. It has to come back as the parameters it is.
     */
    @Test
    void readsParametersAroundAQuotedString() {
        rewriteRun(
          jcl(
            """
              //ACCTJOB  JOB (ACCT),'DAILY POST',CLASS=A,MSGCLASS=X
              """,
            spec -> spec.afterRecipe(cu ->
              assertThat(statementsIn(cu)).singleElement().satisfies(job -> {
                  assertThat(job.getParameters()).hasSize(4);
                  assertThat(job.getParameter("CLASS").getValueText()).isEqualTo("A");
                  assertThat(job.getParameter("MSGCLASS").getValueText()).isEqualTo("X");
              }))
          )
        );
    }

    /**
     * The point of a tree over a report: a parameter can be replaced and the source printed back with
     * that one change and nothing else. No derived model can do this.
     */
    @Test
    void aParameterCanBeChanged() {
        rewriteRun(
          spec -> spec.recipe(org.openrewrite.test.RewriteTest.toRecipe(() -> new JclIsoVisitor<org.openrewrite.ExecutionContext>() {
              @Override
              public Jcl.KeywordParameter visitKeywordParameter(Jcl.KeywordParameter parameter, org.openrewrite.ExecutionContext ctx) {
                  if ("DISP".equalsIgnoreCase(parameter.getKeyword().getText()) &&
                      "SHR".equals(parameter.getValueText())) {
                      return parameter.withValueText("OLD");
                  }
                  return parameter;
              }
          })),
          jcl(
            """
              //ACCTDD   DD  DSN=PROD.ACCOUNT.MASTER,
              //             DISP=SHR,
              //             UNIT=SYSDA
              """,
            """
              //ACCTDD   DD  DSN=PROD.ACCOUNT.MASTER,
              //             DISP=OLD,
              //             UNIT=SYSDA
              """
          )
        );
    }

    private static List<Jcl.JobControlStatement> statementsIn(Jcl.CompilationUnit cu) {
        return cu.getStatements().stream()
                .filter(Jcl.JobControlStatement.class::isInstance)
                .map(Jcl.JobControlStatement.class::cast)
                .collect(Collectors.toList());
    }
}
