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
package org.openrewrite.linkedit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class LinkEditLineReaderTest {

    /**
     * A card continues the one above it because column 72 of that card held something, and nothing in
     * its own text says so.
     */
    @Test
    void continuationComesFromColumn72OfTheCardAbove() {
        String source =
          data("  INCLUDE OBJLIB(CLMU010,") + "X\n" +
          "                  CLMU020)\n";
        assertThat(LinkEditLineReader.readLines(source)).isEqualTo(
          "^^CARD^^" + data("  INCLUDE OBJLIB(CLMU010,") + "\n" +
          "^^CARD_CONT^^                  CLMU020)\n");
    }

    /**
     * Column 72 is the continuation column and nothing else is. A card padded out to 80 with blanks
     * ends its statement, and the sequence numbers a shop writes in columns 73-80 neither continue one
     * nor reach the grammar as operands.
     */
    @Test
    void onlyColumn72Continues() {
        String source =
          data("  ENTRY CLMB010") + "         \n" +
          data("  NAME CLMB010(R)") + " 00000200\n";
        assertThat(LinkEditLineReader.readLines(source)).isEqualTo(
          "^^CARD^^" + data("  ENTRY CLMB010") + "\n" +
          "^^CARD^^" + data("  NAME CLMB010(R)") + "\n");
    }

    @Test
    void carriageReturnsAreKept() {
        assertThat(LinkEditLineReader.readLines("  ENTRY A\r\n  NAME A(R)\r\n")).isEqualTo(
          "^^CARD^^  ENTRY A\r\n^^CARD^^  NAME A(R)\r\n");
    }

    @Test
    void aCommentDoesNotEndTheStatementItIsWrittenInsideOf() {
        String source =
          data("  INCLUDE OBJLIB(CLMU010,") + "X\n" +
          "*  AND THE ONE THE RESERVE CALCULATION NEEDS\n" +
          "           CLMU020)\n";
        assertThat(LinkEditLineReader.readLines(source)).isEqualTo(
          "^^CARD^^" + data("  INCLUDE OBJLIB(CLMU010,") + "\n" +
          "^^COMMENT^^*  AND THE ONE THE RESERVE CALCULATION NEEDS\n" +
          "^^CARD_CONT^^           CLMU020)\n");
    }

    @ValueSource(strings = {
      "  INCLUDE OBJLIB(CLMU010)\n  ENTRY CLMB020\n  NAME CLMB020(R)\n",
      "*  CLMB010 - CLAIM EXTRACT.\n  ENTRY CLMB010\n  NAME CLMB010(R)\n",
      " SETOPT  PARM(AMODE=31)\n INCLUDE SYSLIB(GVBDAYS)\n ENTRY   GVBDAYS\n NAME    GVBDAYS(R)\n",
      "INCLUDE OBJLIB(IC201A)    MODULE FOO\n",
      " NAME  DFH$PCPE(R)\n"
    })
    @ParameterizedTest
    void readsAsALinkEditDeck(String source) {
        assertThat(LinkEditLineReader.isLinkEditDeck(source)).isTrue();
    }

    /**
     * {@code INCLUDE} opens a DFSORT deck and a job as readily as a binder deck, so it counts only
     * when it names a DD the way the binder does.
     */
    @ValueSource(strings = {
      "  SORT FIELDS=(1,8,CH,A)\n  INCLUDE COND=(57,1,CH,EQ,C'O')\n",
      "  INCLUDE COND=(57,1,CH,EQ,C'O')\n",
      "//         INCLUDE MEMBER=@JOBCARD\n",
      "DSN SYSTEM(DB2P)\nBIND PACKAGE(A) MEMBER(P)\nEND\n",
      " SETOPT  PARM(AMODE=31)\n SETOPT  PARM(RMODE=24)\n",
      ""
    })
    @ParameterizedTest
    void doesNotReadAsALinkEditDeck(String source) {
        assertThat(LinkEditLineReader.isLinkEditDeck(source)).isFalse();
    }

    /**
     * A card padded out to the last column operands may be written in.
     */
    private static String data(String text) {
        StringBuilder card = new StringBuilder(text);
        while (card.length() < LinkEditLineReader.DATA_COLUMNS) {
            card.append(' ');
        }
        return card.toString();
    }
}
