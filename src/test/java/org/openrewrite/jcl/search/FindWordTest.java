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
package org.openrewrite.jcl.search;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.jcl.table.JclWordSearchResult;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.jcl.Assertions.jcl;

class FindWordTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new FindWord("DD", true));
    }

    @DocumentExample
    @Test
    void exactMatch() {
        rewriteRun(
          spec -> spec.dataTable(JclWordSearchResult.Row.class, rows ->
              assertThat(rows).singleElement()
                  .extracting(JclWordSearchResult.Row::getMatchedWord)
                  .isEqualTo("DD")),
          jcl(
            """
            //JOB1 JOB ,'H.H. MORRILL'
            //SYSPRINT DD SYSOUT=*
            """,
            """
            //JOB1 JOB ,'H.H. MORRILL'
            //SYSPRINT ~~>DD SYSOUT=*
            """
          )
        );
    }

    /**
     * A keyword parameter is now two words, the keyword and its value, so {@code SYSOUT.*} matches
     * the keyword and marks it there. That is a narrower answer than the whole {@code SYSOUT=*} the
     * flat token stream gave, and a more useful one: the mark lands on the thing that was searched
     * for.
     */
    @Test
    void regexMatch() {
        rewriteRun(
          spec -> spec.recipe(new FindWord("SYSOUT.*", false))
              .dataTable(JclWordSearchResult.Row.class, rows ->
                  assertThat(rows).singleElement()
                      .extracting(JclWordSearchResult.Row::getMatchedWord)
                      .isEqualTo("SYSOUT")),
          jcl(
            """
            //JOB1 JOB ,'H.H. MORRILL'
            //SYSPRINT DD SYSOUT=*
            """,
            """
            //JOB1 JOB ,'H.H. MORRILL'
            //SYSPRINT DD ~~>SYSOUT=*
            """
          )
        );
    }

    @Test
    void noMatch() {
        rewriteRun(
          jcl(
            """
            //JOB1 JOB ,'H.H. MORRILL'
            //STEPA EXEC PROC=P
            """
          )
        );
    }
}
