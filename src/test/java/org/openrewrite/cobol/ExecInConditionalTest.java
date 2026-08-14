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
package org.openrewrite.cobol;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ExpectedToFail;

import static org.openrewrite.cobol.Assertions.cobol;

/**
 * An EXEC block is elided from the text the COBOL grammar is handed, and the grammar rule
 * {@code execCicsStatement : EXEC CICS charData END_EXEC DOT?} makes the terminating period part of
 * the EXEC. So eliding the EXEC also elides the period that ended the sentence.
 * <p>
 * When the EXEC is the only body of a period-terminated conditional, the grammar is handed
 * {@code IF WS-FLAG = 'R'} with no terminator at all, and the IF swallows the next paragraph's name
 * as a statement. This accounts for 21 of the 29 parse failures over the CardDemo, Bank-of-Z and
 * GenApp corpus.
 */
class ExecInConditionalTest extends CobolTest {

    @ExpectedToFail("Eliding the EXEC also elides the period that terminated the sentence")
    @Test
    void execCicsAsTheOnlyBodyOfAnIf() {
        rewriteRun(
          cobol(
            """
              000000 IDENTIFICATION DIVISION.                                        \s
              000000 PROGRAM-ID. IFEXEC.                                             \s
              000000 PROCEDURE DIVISION.                                             \s
              000000 MAIN-PARA.                                                      \s
              000000     IF WS-FLAG = 'R'                                            \s
              000000        EXEC CICS SEND TEXT FROM(WS-MSG) END-EXEC.               \s
              000000 A-EXIT.                                                         \s
              000000     EXIT.                                                       \s
              """
          )
        );
    }

    @Test
    void execCicsAsTheOnlyBodyOfAnIfWithEndIf() {
        rewriteRun(
          cobol(
            """
              000000 IDENTIFICATION DIVISION.                                        \s
              000000 PROGRAM-ID. IFEXEC2.                                            \s
              000000 PROCEDURE DIVISION.                                             \s
              000000 MAIN-PARA.                                                      \s
              000000     IF WS-FLAG = 'R'                                            \s
              000000        EXEC CICS SEND TEXT FROM(WS-MSG) END-EXEC                \s
              000000     END-IF.                                                     \s
              000000     GOBACK.                                                     \s
              """
          )
        );
    }

    @Test
    void execCicsAsTheOnlyStatementInAParagraph() {
        rewriteRun(
          cobol(
            """
              000000 IDENTIFICATION DIVISION.                                        \s
              000000 PROGRAM-ID. PARAEXEC.                                           \s
              000000 PROCEDURE DIVISION.                                             \s
              000000 MAIN-PARA.                                                      \s
              000000     EXEC CICS RETURN END-EXEC.                                  \s
              000000 A-EXIT.                                                         \s
              000000     EXIT.                                                       \s
              """
          )
        );
    }
}
