/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.cobol.search;

import org.junit.jupiter.api.Test;
import org.openrewrite.cobol.CobolTest;
import org.openrewrite.cobol.table.CobolRelationships;
import org.openrewrite.test.RecipeSpec;

import static org.openrewrite.cobol.Assertions.cobol;

public class FindRelationshipsTest extends CobolTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new FindCopybook("KP008"));
    }

    @Test
    void sm103A() {
        rewriteRun(
//          spec -> spec.recipe(new FindCopybook(""))
//            .dataTable(CobolRelationships.Row.class, rows -> {
//            }),
          cobol(
            getNistResource("SM103A.CBL"),
            "",
            spec -> spec.after(s -> s)
          )
        );
    }

    @Test
    void sm206a() {
        rewriteRun(
//          spec -> spec.dataTable(CobolRelationships.Row.class, rows -> {
//          }),
          cobol(
            getNistResource("SM206A.CBL"),
            "",
            spec -> spec.after(s -> s)
          ));
    }
}
