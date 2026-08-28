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
package org.openrewrite.mainframe.db2.bind;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class BindLineReaderTest {

    /**
     * A line continues the one above it because that one ended in a dash, and nothing in its own text
     * says so.
     */
    @Test
    void continuationComesFromTheDashOnTheLineAbove() {
        String source =
          "DSN SYSTEM(DB2P)\n" +
          "BIND PACKAGE(CLMPKG) -   \n" +
          "     MEMBER(CLMD010)\n";
        assertThat(BindLineReader.readLines(source)).isEqualTo(
          "^^CARD^^DSN SYSTEM(DB2P)\n" +
          "^^CARD^^BIND PACKAGE(CLMPKG) -   \n" +
          "^^CARD_CONT^^     MEMBER(CLMD010)\n");
    }

    @Test
    void carriageReturnsAreKept() {
        assertThat(BindLineReader.readLines("BIND PACKAGE(A) -\r\n MEMBER(P)\r\n")).isEqualTo(
          "^^CARD^^BIND PACKAGE(A) -\r\n^^CARD_CONT^^ MEMBER(P)\r\n");
    }

    @ValueSource(strings = {
      "DSN SYSTEM(DB2P)\nBIND PACKAGE(A) MEMBER(P)\nEND\n",
      "BIND PACKAGE(A) MEMBER(P)\n",
      " REBIND PLAN(CLMPLAN)\n",
      "DSN SYSTEM(DB2P)\n\n*** BASE TABLES\nBIND PACKAGE(A) MEMBER(P)\n"
    })
    @ParameterizedTest
    void readsAsABindDeck(String source) {
        assertThat(BindLineReader.isBindDeck(source)).isTrue();
    }

    @ValueSource(strings = {
      "DSN SYSTEM(DB2P)\nRUN PROGRAM(DSNTEP2) PLAN(DSNTEP2)\nEND\n",
      "//DB2BIND JOB (),CLASS=A\n//BIND EXEC PGM=IKJEFT01\n",
      "SELECT * FROM SYSIBM.SYSPACKDEP;\n",
      "  SORT FIELDS=(1,8,CH,A)\n",
      ""
    })
    @ParameterizedTest
    void doesNotReadAsABindDeck(String source) {
        assertThat(BindLineReader.isBindDeck(source)).isFalse();
    }
}
