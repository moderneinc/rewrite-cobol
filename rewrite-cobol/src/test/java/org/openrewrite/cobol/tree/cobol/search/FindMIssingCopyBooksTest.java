/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.cobol.tree.cobol.search;

import org.junit.jupiter.api.Test;
import org.openrewrite.cobol.CobolTest;
import org.openrewrite.cobol.search.FindMissingCopyBooks;
import org.openrewrite.test.RecipeSpec;

import static org.openrewrite.cobol.Assertions.cobol;

public class FindMIssingCopyBooksTest extends CobolTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new FindMissingCopyBooks());
    }

    @Test
    void missingCopyBook() {
        rewriteRun(
          cobol(
            """
              000000* Prevent trim
                     IDENTIFICATION DIVISION.
                     PROGRAM-ID. MISSING-COPYBOOK.
                     DATA DIVISION.
                     FILE SECTION.
                     FD  PRINT-FILE.
                     COPY MISSING-COPYBOOK.
                     01  DUMMY-RECORD PICTURE X(120).
              """,
              """
              000000* Prevent trim
                     IDENTIFICATION DIVISION.
                     PROGRAM-ID. MISSING-COPYBOOK.
                     DATA DIVISION.
                     FILE SECTION.
                     FD  PRINT-FILE.
                     COPY ~~>MISSING-COPYBOOK.
                     01  DUMMY-RECORD PICTURE X(120).
              """, true)
        );
    }

    @Test
    void sm101A() {
        rewriteRun(
          cobol(getNistResource("SM101A.CBL"), true)
        );
    }
}
