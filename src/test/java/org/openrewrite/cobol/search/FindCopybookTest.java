/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.cobol.search;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openrewrite.Issue;
import org.openrewrite.cobol.CobolTest;
import org.openrewrite.cobol.table.CobolRelationships;
import org.openrewrite.cobol.table.CopybookSource;
import org.openrewrite.cobol.table.CopybookSource.Row;
import org.openrewrite.test.RecipeSpec;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.cobol.Assertions.cobol;
import static org.openrewrite.cobol.table.CopybookSource.ResolutionStatus.MISSING_SOURCE;
import static org.openrewrite.cobol.table.CopybookSource.ResolutionStatus.RESOLVED;

class FindCopybookTest extends CobolTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new FindCopybook(null, false));
    }

    @ParameterizedTest
    @ValueSource(strings = {
      "COPY INCEPTION.                                          *",
      "EXEC SQL INCLUDE INCEPTION END-EXEC.                     *",
    })
    void bookIsNotUsed(String input) {
        rewriteRun(
          spec -> spec.recipe(new FindCopybook("KP008", false)),
          cobol(
            """
              000000 IDENTIFICATION DIVISION.                                         *
                     PROGRAM-ID. IC109A.                                              *
                     DATA DIVISION.                                                   *
                     LINKAGE SECTION.                                                 *
                         01  GRP-01.                                                  *
                             %s
              """.formatted(input)
          )
        );
    }

    @Issue("https://github.com/moderneinc/rewrite-cobol/issues/102")
    @ParameterizedTest
    @ValueSource(strings = {
      "COPY MISSING_BOOK.",
      "EXEC SQL INCLUDE MISSING_BOOK END-EXEC.",
    })
    void findMissingCopybookInCopySource(String input) {
        rewriteRun(
          spec -> spec.recipe(new FindCopybook(null, true)).dataTable(Row.class, rows -> {
              assertThat(rows.stream().map(CopybookSource.Row::getCopybookName)).containsOnly("MISSING");
              assertThat(rows.stream().map(Row::getResolutionStatus)).containsOnly(MISSING_SOURCE);
          }),
          cobol(
            """
              000000 IDENTIFICATION DIVISION.                                         *
                     PROGRAM-ID.                                                      *
                         MISSING_BOOK.                                                *
                     DATA DIVISION.                                                   *
                     LINKAGE SECTION.                                                 *
                     01  GRP-01.                                                      *
                         %s
              
                    *******************************************************************
                    /                                                                 *
                             02  SPECIAL-FLAGS.                                       *
                                 03  DN7 PICTURE X.                                   *
                                 03  DN8 PICTURE X.                                   *
                                 03  DN9 PICTURE X.                                   *
              """.formatted(input),
            """
              000000 IDENTIFICATION DIVISION.                                         *
                     PROGRAM-ID.                                                      *
                         MISSING_BOOK.                                                *
                     DATA DIVISION.                                                   *
                     LINKAGE SECTION.                                                 *
                     01  GRP-01.                                                      *
                         %s
              
                    *******************************************************************
                    /                                                                 *
                             02  SPECIAL-FLAGS.                                       *
                                 03  DN7 PICTURE X.                                   *
                                 03  DN8 PICTURE X.                                   *
                                 03  DN9 PICTURE X.                                   *
              """.formatted(input)
          )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
      "COPY INCEPTION.                                          *",
      "EXEC SQL INCLUDE INCEPTION END-EXEC.                     *",
    })
    void onlyFindMissingCopybooks(String input) {
        rewriteRun(
          spec -> spec.recipe(new FindCopybook(null, true)).dataTable(Row.class, rows -> {
              assertThat(rows.stream().map(CopybookSource.Row::getCopybookName)).containsOnly("MISSING");
              assertThat(rows.stream().map(Row::getResolutionStatus)).containsOnly(MISSING_SOURCE);
          }),
          cobol(
            """
              000000 IDENTIFICATION DIVISION.                                         *
                     PROGRAM-ID. IC109A.                                              *
                     DATA DIVISION.                                                   *
                     LINKAGE SECTION.                                                 *
                         01  GRP-01.                                                  *
                             COPY MISSING.                                            *
                             EXEC SQL INCLUDE MISSING END-EXEC.                       *
                             %s
              """.formatted(input),
            """
              000000 IDENTIFICATION DIVISION.                                         *
                     PROGRAM-ID. IC109A.                                              *
                     DATA DIVISION.                                                   *
                     LINKAGE SECTION.                                                 *
                         01  GRP-01.                                                  *
                             COPY ~~>MISSING.                                            *
                             EXEC SQL INCLUDE ~~>MISSING END-EXEC.                       *
                             %s
              """.formatted(input)
          )
        );
    }


    @Test
    void isUsedInCopyStatement() {
        rewriteRun(
          spec -> spec.recipe(new FindCopybook("INCEPTION", false)).dataTable(Row.class, rows -> {
              assertThat(rows.stream().map(CopybookSource.Row::getCopybookName)).containsOnly("INCEPTION");
              assertThat(rows.stream().map(Row::getResolutionStatus)).containsOnly(RESOLVED);
          }),
          cobol(
            """
              000000 IDENTIFICATION DIVISION.                                         *
                     PROGRAM-ID. IC109A.                                              *
                     DATA DIVISION.                                                   *
                     LINKAGE SECTION.                                                 *
                         01  GRP-01.                                                  *
                             COPY INCEPTION.                                          *
              """,
            """
              000000 IDENTIFICATION DIVISION.                                         *
                     PROGRAM-ID. IC109A.                                              *
                     DATA DIVISION.                                                   *
                     LINKAGE SECTION.                                                 *
                         01  GRP-01.                                                  *
                             COPY ~~>INCEPTION.                                          *
              """
          )
        );
    }

    @Test
    void isUsedInIncludeStatement() {
        rewriteRun(
          spec -> spec.recipe(new FindCopybook("INCEPTION", false)).dataTable(Row.class, rows -> {
              assertThat(rows.stream().map(CopybookSource.Row::getCopybookName)).containsOnly("INCEPTION");
              assertThat(rows.stream().map(Row::getResolutionStatus)).containsOnly(RESOLVED);
          }),
          cobol(
            """
              000000 IDENTIFICATION DIVISION.                                         *
                     PROGRAM-ID. IC109A.                                              *
                     DATA DIVISION.                                                   *
                     LINKAGE SECTION.                                                 *
                         01  GRP-01.                                                  *
                             EXEC SQL INCLUDE INCEPTION END-EXEC.                     *
              """,
            """
              000000 IDENTIFICATION DIVISION.                                         *
                     PROGRAM-ID. IC109A.                                              *
                     DATA DIVISION.                                                   *
                     LINKAGE SECTION.                                                 *
                         01  GRP-01.                                                  *
                             EXEC SQL INCLUDE ~~>INCEPTION END-EXEC.                     *
              """
          )
        );
    }

    @Test
    void sm206a() {
        rewriteRun(
          spec -> spec.dataTable(Row.class, rows -> {
              assertThat(rows.stream().map(Row::getCopybookName))
                .containsExactly(IntStream.range(1, 10).mapToObj(n -> "KP00" + n).toArray(String[]::new));
              assertThat(rows.stream().map(Row::getResolutionStatus)).containsOnly(RESOLVED);
          }),
          cobol(
            getNistResource("SM206A.CBL"),
            "",
            spec -> spec.after(s -> s)
          )
        );
    }
}
