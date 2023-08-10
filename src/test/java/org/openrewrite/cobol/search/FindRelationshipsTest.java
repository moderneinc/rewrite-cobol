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
import static org.openrewrite.cobol.table.CobolRelationships.ResourceAction.*;
import static org.openrewrite.cobol.table.CobolRelationships.ResourceType.*;
import static org.openrewrite.test.SourceSpecs.text;

public class FindRelationshipsTest extends CobolTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new FindRelationships());
    }

    @Test
    void IC201A() {
        rewriteRun(
          spec -> spec.dataTable(Row.class, rows -> {
              assertThat(rows.stream().map(Row::getDependent)).contains("IC201A", "LINKEDIT1", "BINDCARDPACKAGE", "BINDCARDPLAN");
              assertThat(rows.stream().map(Row::getDependency)).contains("IC202A", "IC201A", "LINKEDIT1");
              assertThat(rows.stream().map(Row::getDependencyType)).contains(COBOL, COBOL, LINKEDIT);
              assertThat(rows.stream().map(Row::getAction)).contains(CALL, INCLUDE, PLAN, MEMBER);
          }),
          cobol(
            getNistResource("IC201A.CBL"),
            "",
            spec -> spec.after(s -> s).path("IC201A.CBL")
          ),
          text("""
              *
              INCLUDE OBJLIB(IC201A)    MODULE FOO
              *INCLUDE OBJLIB(ABCD02)
              """,
            (spec) -> spec.path("linkeditcards/LINKEDIT1")),
          text("""
            BIND PACKAGE(PROD0) OWNER(SBS100S) -                        \s
               QUALIFIER(SBS100S) MEMBER(IC201A) -                      \s
               SQLERROR(NOPACKAGE) VALIDATE(BIND) FLAG(I) ISOLATION(CS) -
               RELEASE(COMMIT) EXPLAIN(YES) CURRENTDATA(YES) -          \s
               ACTION(ADD)     -                                        \s
               ENABLE(*)                                                \s
            """,
            (spec) -> spec.path("bindcards/BINDCARDPACKAGE")),
          text("""
              BIND PLAN(LINKEDIT1) OWNER(SBS100S) -            \s
                 QUALIFIER(SBS100S) -                         \s
                 PKLIST(PROD0.*)  -                           \s
                 VALIDATE(BIND)        -                      \s
                 FLAG(I) ISOLATION(CS) -                      \s
                 CACHESIZE(0) -                               \s
                 ACQUIRE(USE) -                               \s
                 RELEASE(COMMIT) EXPLAIN(YES) CURRENTDATA(YES) -
                 ACTION(REPLACE) RETAIN -                     \s
                 ENABLE(*)    \s
              """,
            (spec) -> spec.path("bindcards/BINDCARDPLAN"))
        );
    }

    @Test
    void SM206A() {
        rewriteRun(
          spec -> spec.dataTable(Row.class, rows -> {
              assertThat(rows.stream().map(Row::getDependent)).containsOnly("SM206A");
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
