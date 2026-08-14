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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.openrewrite.cobol.Assertions.cobolPostProcess;

class PartialWordReplacingTest extends CobolTest {

    /**
     * A reductive replacement chooses its type by comparing the number of words it matches against
     * the number it replaces them with. It used to compare against the number of <em>sites</em> the
     * rule matched instead, so a rule was silently skipped whenever it happened to match exactly as
     * many times as it has words — three words matching at three sites, here.
     * <p>
     * Nothing fails when that happens: the copybook is expanded with the library text intact, and
     * {@code DISPLAY OLD ONE TWO} is as valid as {@code DISPLAY NEWNAME}. The program parses, and
     * every analysis built on it is wrong.
     */
    @Test
    void reductiveReplacementMatchingAsManyTimesAsItHasWords() {
        rewriteRun(
          cobolPostProcess(
            """
              000001 IDENTIFICATION DIVISION.                                         000000000
              000002 PROGRAM-ID. REDUC3.                                              000000000
              000003 PROCEDURE DIVISION.                                              000000000
              000004     COPY REDUC3 REPLACING                                        000000000
              000005       ==OLD ONE TWO== BY ==NEWNAME== .                           000000000""",
            """
              IDENTIFICATION DIVISION.
              PROGRAM-ID. REDUC3.
              PROCEDURE DIVISION.
                  DISPLAY        NEWNAME
                  DISPLAY        NEWNAME
                  DISPLAY        NEWNAME
              .
              """
          )
        );
    }

    /**
     * IBM Enterprise COBOL lets pseudo-text match part of a text word, so
     * {@code ==(TESTVAR1)== BY ==DESCRIPTION==} turns {@code FLG-(TESTVAR1)-NOT-OK} into
     * {@code FLG-DESCRIPTION-NOT-OK}. AWS CardDemo uses the idiom throughout.
     * <p>
     * We cannot do it yet. A reductive replacement keeps the following text in its original columns
     * by blanking the words it removes where they stand, which is exactly what a partial-word
     * replacement must not do — it leaves {@code FLG-DESCRIPTION         -NOT-OK}, three text words
     * where the program means one. Joining them requires a single token of the COBOL grammar to map
     * back onto seven words of the copybook spanning two replacement marker blocks, and the printer
     * maps one token to one word.
     * <p>
     * Until that changes, refuse it. The alternative is not a parse failure but a program that
     * parses into the wrong tree: {@code 05 FLG-(TESTVAR1)-SW PIC X} becomes a data item named
     * {@code FLG-DESCRIPTION} followed by a stray {@code -SW}.
     */
    @Test
    void partialWordReplacingIsRefused() {
        assertThatThrownBy(() -> rewriteRun(
          cobolPostProcess(
            """
              000001 IDENTIFICATION DIVISION.                                         000000000
              000002 PROGRAM-ID. PARTWRD.                                             000000000
              000003 PROCEDURE DIVISION.                                              000000000
              000004     COPY PARTWRD REPLACING                                       000000000
              000005       ==(TESTVAR1)== BY ==DESCRIPTION==                          000000000
              000006       ==(SCRNVAR2)== BY ==TRTYDSC== .                            000000000""",
            null
          )
        )).hasRootCauseMessage("Partial word replacement is not supported: ==(TESTVAR1)==.");
    }
}
