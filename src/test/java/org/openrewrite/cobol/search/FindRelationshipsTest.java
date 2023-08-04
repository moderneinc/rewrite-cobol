/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.cobol.search;

import org.junit.jupiter.api.Test;
import org.openrewrite.cobol.CobolTest;
import org.openrewrite.cobol.table.CobolRelationships.Row;
import org.openrewrite.test.RecipeSpec;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.cobol.Assertions.cobol;
import static org.openrewrite.cobol.table.CobolRelationships.ResourceAction.CALL;
import static org.openrewrite.cobol.table.CobolRelationships.ResourceAction.COPY;
import static org.openrewrite.cobol.table.CobolRelationships.ResourceType.COBOL;
import static org.openrewrite.cobol.table.CobolRelationships.ResourceType.COPYBOOK;

public class FindRelationshipsTest extends CobolTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new FindRelationships());
    }

    @Test
    void IC201A() {
        rewriteRun(
          spec -> spec.dataTable(Row.class, rows -> {
              assertThat(rows.stream().map(Row::getDependent)).containsOnly("IC201A.CBL");
              assertThat(rows.stream().map(Row::getDependency)).containsOnly("IC202A");
              assertThat(rows.stream().map(Row::getDependencyType)).containsOnly(COBOL);
              assertThat(rows.stream().map(Row::getAction)).containsOnly(CALL);
          }),
          cobol(
            getNistResource("IC201A.CBL"),
            "",
            spec -> spec.after(s -> s).path("IC201A.CBL")
          )
        );
    }

    @Test
    void SM206A() {
        rewriteRun(
          spec -> spec.dataTable(Row.class, rows -> {
              assertThat(rows.stream().map(Row::getDependent)).containsOnly("SM206A.CBL");
              assertThat(rows.stream().map(Row::getDependency))
                .containsExactly(IntStream.range(1, 10).mapToObj(n -> "KP00" + n).toArray(String[]::new));
              assertThat(rows.stream().map(Row::isDependencyMissing)).containsOnly(false);
              assertThat(rows.stream().map(Row::getDependencyType)).containsOnly(COPYBOOK);
              assertThat(rows.stream().map(Row::getAction)).containsOnly(COPY);
          }),
          cobol(
            getNistResource("SM206A.CBL"),
            "",
            spec -> spec.after(s -> s).path("SM206A.CBL")
          ));
    }
}
