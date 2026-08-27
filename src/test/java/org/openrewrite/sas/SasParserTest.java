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
package org.openrewrite.sas;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.sas.tree.Sas;
import org.openrewrite.test.RewriteTest;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.openrewrite.sas.Assertions.sas;

class SasParserTest implements RewriteTest {

    @DocumentExample
    @Test
    void readsAStatementUpToItsSemicolon() {
        rewriteRun(
          sas(
            """
              LIBNAME CLMSAS;

              DATA CLMSAS.CLMDAY;
                 INFILE CLMEXTR LRECL=200 RECFM=FB;
              RUN;
              """,
            spec -> spec.afterRecipe(cu ->
              assertThat(statements(cu)).extracting(Sas.Statement::getKeyword, Sas.Statement::getWordTexts)
                .containsExactly(
                  tuple("LIBNAME", List.of("LIBNAME", "CLMSAS")),
                  tuple("DATA", List.of("DATA", "CLMSAS.CLMDAY")),
                  tuple("INFILE", List.of("INFILE", "CLMEXTR", "LRECL=200", "RECFM=FB")),
                  tuple("RUN", List.of("RUN"))))
          )
        );
    }

    @Test
    void keepsABlockCommentWhereverItStands() {
        rewriteRun(
          sas(
            """
              /*  CLMSMAC - THE FORMATS AND THE TITLE MACRO.  */
              OPTIONS LS=132 /* AS THE ACTUARIES ASKED */ PS=60;
              """,
            spec -> spec.afterRecipe(cu -> {
                assertThat(cu.getStatements().get(0)).isInstanceOfSatisfying(Sas.Comment.class,
                  comment -> assertThat(comment.getText())
                    .isEqualTo("/*  CLMSMAC - THE FORMATS AND THE TITLE MACRO.  */"));
                Sas.Statement options = statements(cu).get(0);
                assertThat(options.getWordTexts()).containsExactly("OPTIONS", "LS=132", "PS=60");
                assertThat(options.getParts()).element(2)
                  .isInstanceOfSatisfying(Sas.Comment.class,
                    comment -> assertThat(comment.getText()).isEqualTo("/* AS THE ACTUARIES ASKED */"));
            })
          )
        );
    }

    /**
     * A statement beginning {@code *} is a comment that runs to its own semicolon, and the same
     * character in the middle of a statement multiplies. Only where the statement starts says which.
     */
    @Test
    void tellsACommentStatementFromAMultiplication() {
        rewriteRun(
          sas(
            """
              * THE RESERVE; IS NOT WHAT IT WAS;
              RSVCHG = NEWRSV * OLDRSV;
              """,
            spec -> spec.afterRecipe(cu -> {
                assertThat(cu.getStatements().get(0)).isInstanceOfSatisfying(Sas.Comment.class,
                  comment -> assertThat(comment.getText()).isEqualTo("* THE RESERVE;"));
                assertThat(statements(cu)).extracting(Sas.Statement::getWordTexts)
                  .containsExactly(
                    List.of("IS", "NOT", "WHAT", "IT", "WAS"),
                    List.of("RSVCHG", "=", "NEWRSV", "*", "OLDRSV"));
            })
          )
        );
    }

    @Test
    void takesAQuotedStringWholeHoweverItIsPunctuated() {
        rewriteRun(
          sas(
            """
              TITLE1 "CASCADE MUTUAL - CLAIMS; RESERVES - &SYSDATE";
              FOOTNOTE1 'CLM.PROD.SAS - PAGER 4142';
              """,
            spec -> spec.afterRecipe(cu ->
              assertThat(statements(cu)).extracting(Sas.Statement::getWordTexts)
                .containsExactly(
                  List.of("TITLE1", "\"CASCADE MUTUAL - CLAIMS; RESERVES - &SYSDATE\""),
                  List.of("FOOTNOTE1", "'CLM.PROD.SAS - PAGER 4142'")))
          )
        );
    }

    @Test
    void readsAStatementWrittenOverSeveralLines() {
        rewriteRun(
          sas(
            """
              INPUT @001 CLAIMNO  $CHAR10.
                    @011 POLICYNO $CHAR12.
                    @023 CLMNAME  $CHAR30.;
              """,
            spec -> spec.afterRecipe(cu ->
              assertThat(statements(cu)).singleElement()
                .satisfies(input -> assertThat(input.getWordTexts()).containsExactly(
                  "INPUT", "@001", "CLAIMNO", "$CHAR10.", "@011", "POLICYNO", "$CHAR12.",
                  "@023", "CLMNAME", "$CHAR30.")))
          )
        );
    }

    /**
     * A member that ends without a semicolon still reads. What is written is a statement; whether SAS
     * would have run it is the run's business.
     */
    @Test
    void readsALastStatementNobodyTerminated() {
        rewriteRun(
          sas(
            "%MEND CLMTITL",
            spec -> spec.afterRecipe(cu -> {
                assertThat(statements(cu)).singleElement()
                  .satisfies(statement -> assertThat(statement.getEnd()).isNull());
                assertThat(statements(cu).get(0).getKeyword()).isEqualTo("%MEND");
            })
          )
        );
    }

    private static List<Sas.Statement> statements(Sas.CompilationUnit cu) {
        return cu.getStatements().stream()
          .filter(Sas.Statement.class::isInstance)
          .map(Sas.Statement.class::cast)
          .collect(Collectors.toList());
    }
}
