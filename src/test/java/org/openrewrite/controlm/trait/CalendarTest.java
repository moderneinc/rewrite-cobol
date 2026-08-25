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
package org.openrewrite.controlm.trait;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.controlm.tree.ParserAssertions.controlM;

/**
 * A {@code DEFCAL} year is twelve months of thirty-one positions whether the month has them or not,
 * so the days a job runs on are only readable by decoding it.
 */
class CalendarTest implements RewriteTest {

    @Test
    void readsTheDaysOfAYear() {
        rewriteRun(
          controlM(
            defcal("NYY"),
            spec -> spec.afterRecipe(cu -> {
                Calendar calendar = new Calendar.Matcher().lower(cu).collect(Collectors.toList()).get(0);
                assertThat(calendar.getName()).isEqualTo("CLMWORK");
                assertThat(calendar.getType()).isEqualTo("Regular");
                assertThat(calendar.getDataCenter()).isEqualTo("CTMPROD");
                assertThat(calendar.getYears()).containsExactly(2026);
                assertThat(calendar.getDays(2026)).containsExactly(
                  LocalDate.of(2026, 1, 2), LocalDate.of(2026, 1, 3));
                assertThat(calendar.contains(LocalDate.of(2026, 1, 2))).isTrue();
                assertThat(calendar.contains(LocalDate.of(2026, 1, 1))).isFalse();
                assertThat(calendar.getDays(2025)).isEmpty();
            })
          )
        );
    }

    /**
     * February is written with thirty-one positions like every other month, and 2026 has twenty-eight
     * days. A position past the end of a month is no day at all.
     */
    @Test
    void ignoresAPositionPastTheEndOfAMonth() {
        rewriteRun(
          controlM(
            defcal(marks(31 + 27, 31 + 29)),
            spec -> spec.afterRecipe(cu -> {
                Calendar calendar = new Calendar.Matcher().lower(cu).collect(Collectors.toList()).get(0);
                assertThat(calendar.getDays(2026)).containsExactly(LocalDate.of(2026, 2, 28));
            })
          )
        );
    }

    private static String marks(int... positions) {
        char[] days = new char[372];
        Arrays.fill(days, 'N');
        for (int position : positions) {
            days[position] = 'Y';
        }
        return new String(days);
    }

    private static String defcal(String days) {
        return """
          <?xml version="1.0" encoding="ISO-8859-1"?>
          <DEFCAL>
            <CALENDAR DATACENTER="CTMPROD" NAME="CLMWORK" TYPE="Regular">
              <YEAR NAME="2026" DESCRIPTION="CLAIMS BATCH DAYS" DAYS="%s" />
            </CALENDAR>
          </DEFCAL>
          """.formatted(days);
    }
}
