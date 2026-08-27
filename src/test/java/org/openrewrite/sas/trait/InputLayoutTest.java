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
package org.openrewrite.sas.trait;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.sas.tree.Sas;
import org.openrewrite.test.RewriteTest;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.openrewrite.sas.Assertions.sas;

class InputLayoutTest implements RewriteTest {

    /**
     * INTERLINKS 21.4. The layout is the copybook written again: {@code @001 CLAIMNO $CHAR10.} says
     * the same thing as {@code 05 EXT-CLAIM-NO PIC X(10)} at offset zero, and the informat says how
     * many bytes of the record it took.
     */
    @DocumentExample
    @Test
    void readsEachVariableAtTheColumnItStartsIn() {
        rewriteRun(
          sas(
            """
              DATA CLMSAS.CLMAUD;
                 INFILE CLMAUDIT LRECL=120 RECFM=FB;
                 INPUT @001 RUNDATE  YYMMDD8.
                       @009 PROGRAM  $CHAR8.
                       @031 OLDRSV   ZD13.2
                       @057 RETCODE  2.;
              RUN;
              """,
            spec -> spec.afterRecipe(cu ->
              assertThat(fields(cu)).extracting(InputLayout.Field::getColumn,
                  InputLayout.Field::getName, InputLayout.Field::getInformat,
                  InputLayout.Field::getBytes)
                .containsExactly(
                  tuple(1, "RUNDATE", "YYMMDD8.", 8),
                  tuple(9, "PROGRAM", "$CHAR8.", 8),
                  tuple(31, "OLDRSV", "ZD13.2", 13),
                  tuple(57, "RETCODE", "2.", 2)))
          )
        );
    }

    /**
     * A variable written with no {@code @} in front of it starts where the one before it ended, which
     * is where the pointer stands. Reading it as column one instead lays the whole record over itself.
     */
    @Test
    void followsThePointerWhereNoColumnIsWritten() {
        rewriteRun(
          sas(
            """
              DATA CLM;
                 INPUT @001 CLAIMNO $CHAR10. POLICYNO $CHAR12. TYPECODE $CHAR4.;
              RUN;
              """,
            spec -> spec.afterRecipe(cu ->
              assertThat(fields(cu)).extracting(InputLayout.Field::getColumn, InputLayout.Field::getName)
                .containsExactly(tuple(1, "CLAIMNO"), tuple(11, "POLICYNO"), tuple(23, "TYPECODE")))
          )
        );
    }

    /**
     * An informat that writes no width lets SAS decide, and nothing here can say how wide that is —
     * so the width is nothing rather than a guess, and every column after it says so too.
     */
    @Test
    void saysNothingAboutAWidthTheInformatDoesNotWrite() {
        rewriteRun(
          sas(
            """
              DATA CLM;
                 INPUT @001 CLAIMNO $CHAR. POLICYNO;
              RUN;
              """,
            spec -> spec.afterRecipe(cu ->
              assertThat(fields(cu)).extracting(InputLayout.Field::getColumn,
                  InputLayout.Field::getName, InputLayout.Field::getBytes)
                .containsExactly(tuple(1, "CLAIMNO", 0), tuple(1, "POLICYNO", 0)))
          )
        );
    }

    /**
     * A record layout is read in a DATA step and nowhere else. The macro language has an
     * {@code %INPUT} of its own, and a {@code RUN;} ends the step above — so what stands after one is
     * in no step at all, and read as a layout it reports columns of a record nobody read.
     */
    @Test
    void readsNoLayoutOutsideADataStep() {
        rewriteRun(
          sas(
            """
              %WINDOW ASK;
              %INPUT CLAIMNO;

              DATA CLM;
                 SET CLMSAS.CLMDAY;
              RUN;

              INPUT @001 CLAIMNO $CHAR10.;
              """,
            spec -> spec.afterRecipe(cu ->
              assertThat(new InputLayout.Matcher().lower(cu).collect(Collectors.toList())).isEmpty())
          )
        );
    }

    private static List<InputLayout.Field> fields(Sas.CompilationUnit cu) {
        List<InputLayout> layouts = new InputLayout.Matcher().lower(cu).collect(Collectors.toList());
        assertThat(layouts).hasSize(1);
        return layouts.get(0).getFields();
    }
}
