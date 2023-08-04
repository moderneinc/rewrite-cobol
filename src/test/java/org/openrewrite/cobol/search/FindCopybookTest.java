/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.cobol.search;

import org.junit.jupiter.api.Test;
import org.openrewrite.cobol.CobolTest;
import org.openrewrite.cobol.table.CopybookSource.Row;
import org.openrewrite.test.RecipeSpec;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.PathUtils.separatorsToUnix;
import static org.openrewrite.cobol.Assertions.cobol;
import static org.openrewrite.cobol.table.CopybookSource.ResolutionStatus.RESOLVED;

public class FindCopybookTest extends CobolTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new FindCopybook(null));
    }

    @Test
    void bookIsNotUsed() {
        rewriteRun(
          spec -> spec.recipe(new FindCopybook("KP008")),
          cobol(getNistResource("CM101M.CBL"))
        );
    }

    @Test
    void sm103A() {
        rewriteRun(
          spec -> spec.dataTable(Row.class, rows -> {
              assertThat(rows).hasSize(7);
              Row r0 = rows.get(0);
              assertThat(r0.getCopybookName()).isEqualTo("K3SCA");
              assertThat(r0.getResolutionStatus()).isEqualTo(RESOLVED);
              assertThat(separatorsToUnix(r0.getCopybookSourcePath())).isEqualTo("gov/nist/copybooks/K3SCA.CPY");
          }),
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
