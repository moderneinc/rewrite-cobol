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
package org.openrewrite.listload;

import org.junit.jupiter.api.Test;
import org.openrewrite.listload.tree.ListLoad;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class ListLoadLineReaderTest {

    @Test
    void columnOneOfAReportIsCarriageControlAndNotText() {
        String report = String.join("\n",
          "1                     A M B L I S T",
          "0",
          "      MEMBER NAME:    CLMB010",
          "");

        assertThat(ListLoadLineReader.readLines(report))
          .extracting(ListLoad.Line::getCarriageControl, ListLoad.Line::getText, ListLoad.Line::getLineEnding)
          .containsExactly(
            tuple("1", "                     A M B L I S T", "\n"),
            tuple("0", "", "\n"),
            tuple(" ", "     MEMBER NAME:    CLMB010", "\n"));
    }

    /**
     * A request deck is cards and not print, so nothing was ever acted on in column 1.
     */
    @Test
    void aRequestDeckKeepsItsWholeCard() {
        assertThat(ListLoadLineReader.readLines("  LISTLOAD OUTPUT=MODLIST,DDN=LOADLIB\n"))
          .extracting(ListLoad.Line::getCarriageControl, ListLoad.Line::getText)
          .containsExactly(tuple("", "  LISTLOAD OUTPUT=MODLIST,DDN=LOADLIB"));
    }

    @Test
    void keepsTheLineEndingsItWasGiven() {
        assertThat(ListLoadLineReader.readLines("  LISTIDR DDN=LOADLIB\r\n  LISTLOAD DDN=LOADLIB"))
          .extracting(ListLoad.Line::getText, ListLoad.Line::getLineEnding)
          .containsExactly(
            tuple("  LISTIDR DDN=LOADLIB", "\r\n"),
            tuple("  LISTLOAD DDN=LOADLIB", ""));
    }

    @Test
    void typesAReportByItsHeadingAndADeckByItsFunction() {
        assertThat(ListLoadLineReader.isReport("1                     A M B L I S T          PAGE 1\n")).isTrue();
        assertThat(ListLoadLineReader.isReport("1z/OS V2 R5 BINDER\n IEW2278I B352 INVOCATION PARAMETERS - LIST,MAP\n")).isTrue();
        assertThat(ListLoadLineReader.isRequest("  LISTLOAD OUTPUT=MODLIST,DDN=LOADLIB\n")).isTrue();

        assertThat(ListLoadLineReader.isModuleListing("  SORT FIELDS=(1,8,CH,A)\n")).isFalse();
        assertThat(ListLoadLineReader.isModuleListing("  DELETE CLM.PROD.EXTRACT\n")).isFalse();
    }

    /**
     * A member that says nothing about itself must not cost a full read to type: a report announces
     * itself in its heading and a deck in its first card.
     */
    @Test
    void doesNotReadAWholeFileToRefuseIt() {
        StringBuilder parms = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            parms.append("  RECFM=FB,LRECL=80\n");
        }
        parms.append("1                     A M B L I S T\n");
        assertThat(ListLoadLineReader.isModuleListing(parms.toString())).isFalse();
    }
}
