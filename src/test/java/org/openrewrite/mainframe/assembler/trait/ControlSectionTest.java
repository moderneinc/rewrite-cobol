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
package org.openrewrite.mainframe.assembler.trait;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RewriteTest;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.openrewrite.mainframe.assembler.Assertions.assembler;

class ControlSectionTest implements RewriteTest {

    @DocumentExample
    @Test
    void readsADummySectionAsTheLayoutOfSomebodyElsesRecord() {
        rewriteRun(
          assembler(
            """
              CLMRECD  DSECT
              CLMRKEY  DS    0CL10                    0  CLM-KEY
              CLMRCLM  DS    CL10                     0  CLM-CLAIM-NO
              CLMRPOL  DS    CL12                    10  CLM-POLICY-NO
              CLMRSTA  DS    CL1                     22  CLM-STATUS
              CLMRAMC  DS    PL7                     23  CLM-AMT-CLAIMED
              CLMRLEN  EQU   *-CLMRECD               30
              CLMROPEN EQU   C'O'
              """,
            spec -> spec.afterRecipe(cu -> assertThat(new ControlSection.Matcher().lower(cu)
              .collect(Collectors.toList())).singleElement().satisfies(section -> {
                assertThat(section.getName()).isEqualTo("CLMRECD");
                assertThat(section.getKind()).isEqualTo(ControlSection.Kind.DSECT);
                assertThat(section.isDummy()).isTrue();
                // The layout stops at the EQU that measures it, so the equates after it are not rows.
                assertThat(section.getFields()).extracting(ControlSection.Field::getName,
                    ControlSection.Field::getOffset, ControlSection.Field::getBytes,
                    ControlSection.Field::getType)
                  .containsExactly(
                    tuple("CLMRKEY", 0, 10, "0CL10"),
                    tuple("CLMRCLM", 0, 10, "CL10"),
                    tuple("CLMRPOL", 10, 12, "CL12"),
                    tuple("CLMRSTA", 22, 1, "CL1"),
                    tuple("CLMRAMC", 23, 7, "PL7"));
                assertThat(section.getLength()).isEqualTo(30);
                assertThat(section.getLengthSymbol()).isEqualTo("CLMRLEN");
                assertThat(section.getLabels())
                  .containsExactly("CLMRKEY", "CLMRCLM", "CLMRPOL", "CLMRSTA", "CLMRAMC");
            }))
          )
        );
    }

    /**
     * A fullword is placed on a fullword boundary unless a length modifier says otherwise, so an
     * offset is not simply the sum of the lengths before it.
     */
    @Test
    void movesToTheBoundaryATypeIsAlignedOn() {
        rewriteRun(
          assembler(
            """
              PCBD     DSECT
              PCBDBD   DS    CL8
              PCBLEV   DS    CL1
              PCBRSV   DS    F
              PCBSAVE  DS    18F
              PCBLEN   EQU   *-PCBD
              """,
            spec -> spec.afterRecipe(cu -> assertThat(new ControlSection.Matcher().lower(cu)
              .collect(Collectors.toList())).singleElement().satisfies(section -> {
                assertThat(section.getFields()).extracting(ControlSection.Field::getName,
                    ControlSection.Field::getOffset, ControlSection.Field::getBytes)
                  .containsExactly(
                    tuple("PCBDBD", 0, 8),
                    tuple("PCBLEV", 8, 1),
                    tuple("PCBRSV", 12, 4),
                    tuple("PCBSAVE", 16, 4));
                assertThat(section.getLength()).isEqualTo(88);
            }))
          )
        );
    }

    @Test
    void countsEveryValueAConstantHolds() {
        rewriteRun(
          assembler(
            """
              WEIGHTS  CSECT
              U30WGT   DC    H'7,6,5,4,3,2'
              PATTERN  DC    X'4020202020202120'
              BLANK    DC    C' '
              """,
            spec -> spec.afterRecipe(cu -> assertThat(new ControlSection.Matcher().lower(cu)
              .collect(Collectors.toList())).singleElement()
              .extracting(section -> section.getFields().stream()
                .map(field -> field.getName() + ":" + field.getOffset() + ":" + field.getBytes())
                .collect(Collectors.toList()))
              .isEqualTo(List.of("U30WGT:0:12", "PATTERN:12:8", "BLANK:20:1")))
          )
        );
    }

    @Test
    void saysItDoesNotKnowAnOffsetItCannotWork() {
        rewriteRun(
          assembler(
            """
              MOVED    DSECT
              FIRST    DS    CL8
              SECOND   DS    CL(L'FIRST)
              THIRD    DS    CL4
              """,
            spec -> spec.afterRecipe(cu -> assertThat(new ControlSection.Matcher().lower(cu)
              .collect(Collectors.toList())).singleElement().satisfies(section -> {
                assertThat(section.getFields()).extracting(ControlSection.Field::getName,
                    ControlSection.Field::getOffset)
                  .containsExactly(tuple("FIRST", 0), tuple("SECOND", 8), tuple("THIRD", null));
                assertThat(section.getLength()).isNull();
            }))
          )
        );
    }
}
