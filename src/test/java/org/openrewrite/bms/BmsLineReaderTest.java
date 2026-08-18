/*
 * Copyright 2025 the original author or authors.
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
package org.openrewrite.bms;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BmsLineReaderTest {

    @Test
    void classifiesLines() {
        String read = BmsLineReader.readLines(
          "* a comment\n" +
          "COSGN00 DFHMSD LANG=COBOL\n" +
          "        DFHMDF POS=(1,1)\n");
        assertThat(read).isEqualTo(
          "^^COMMENT^^* a comment\n" +
          "^^BMS_NAMED^^COSGN00 DFHMSD LANG=COBOL\n" +
          "^^BMS^^        DFHMDF POS=(1,1)\n");
    }

    /**
     * A line continues the one above it because of column 72, and nothing in its own text says so.
     */
    @Test
    void continuationComesFromColumnSeventyTwo() {
        String source =
          "COSGN00 DFHMSD CTRL=(ALARM,FREEKB),                                    -\n" +
          "               EXTATT=YES\n";
        assertThat(BmsLineReader.readLines(source)).isEqualTo(
          "^^BMS_NAMED^^COSGN00 DFHMSD CTRL=(ALARM,FREEKB),                                    -\n" +
          "^^BMS_CONT^^               EXTATT=YES\n");
    }

    @Test
    void sequenceAreaIsSplitOffOnlyWhenItHoldsSomething() {
        String padded = "        DFHMDF POS=(1,1)" + blanks(56) + "\n";
        assertThat(BmsLineReader.readLines(padded)).doesNotContain("^^CA_START^^");

        String numbered = "        DFHMDF POS=(1,1)" + blanks(48) + "COSGN001\n";
        assertThat(BmsLineReader.readLines(numbered)).isEqualTo(
          "^^BMS^^        DFHMDF POS=(1,1)" + blanks(48) + "^^CA_START^^COSGN001\n");
    }

    private static String blanks(int n) {
        StringBuilder blanks = new StringBuilder();
        for (int i = 0; i < n; i++) {
            blanks.append(' ');
        }
        return blanks.toString();
    }
}
