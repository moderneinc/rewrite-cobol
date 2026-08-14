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
import org.openrewrite.cobol.marker.ElidedDot;
import org.openrewrite.cobol.tree.Cobol;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.cobol.Assertions.cobol;

/**
 * An EXEC block is elided from the text the COBOL grammar is handed, and the grammar rule
 * {@code execCicsStatement : EXEC CICS charData END_EXEC DOT?} makes the terminating period part of
 * the EXEC. So eliding the EXEC also elides the period that ended the sentence.
 * <p>
 * When the EXEC is the only body of a period-terminated conditional, the grammar was handed
 * {@code IF WS-FLAG = 'R'} with no terminator at all, and the IF swallowed the next paragraph's name
 * as a statement. The period is now re-emitted into the parser input, and the word the grammar
 * produces for it is marked {@link ElidedDot} so that the printer takes it from the EXEC statement
 * attached to that word rather than printing it a second time.
 */
class ExecInConditionalTest extends CobolTest {

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
              """,
            spec -> spec.afterRecipe(cu -> {
                assertThat(paragraphNames(cu)).containsExactly("MAIN-PARA", "A-EXIT");
                assertThat(elidedDots(cu)).containsExactly(".");
            })
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
              """,
            spec -> spec.afterRecipe(cu -> {
                assertThat(paragraphNames(cu)).containsExactly("MAIN-PARA");
                assertThat(elidedDots(cu)).isEmpty();
            })
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
              """,
            spec -> spec.afterRecipe(cu -> {
                assertThat(paragraphNames(cu)).containsExactly("MAIN-PARA", "A-EXIT");
                assertThat(elidedDots(cu)).containsExactly(".");
            })
          )
        );
    }

    /**
     * Adjacent EXEC statements are all re-attached to the first word that follows them, so each of
     * their periods has to be accounted for.
     */
    @Test
    void consecutiveExecs() {
        rewriteRun(
          cobol(
            """
              000000 IDENTIFICATION DIVISION.                                        \s
              000000 PROGRAM-ID. TWOEXEC.                                            \s
              000000 PROCEDURE DIVISION.                                             \s
              000000 MAIN-PARA.                                                      \s
              000000     EXEC CICS ASKTIME ABSTIME(WS-ABSTIME) END-EXEC.             \s
              000000     EXEC CICS RETURN END-EXEC.                                  \s
              000000 A-EXIT.                                                         \s
              000000     EXIT.                                                       \s
              """,
            spec -> spec.afterRecipe(cu -> {
                assertThat(paragraphNames(cu)).containsExactly("MAIN-PARA", "A-EXIT");
                assertThat(elidedDots(cu)).containsExactly(".", ".");
            })
          )
        );
    }

    /**
     * Outside the procedure division a period is not a sentence terminator, so the period of an
     * elided EXEC has to stay elided or the data description entries around it stop parsing.
     */
    @Test
    void execSqlInWorkingStorageKeepsItsPeriodElided() {
        rewriteRun(
          cobol(
            """
              000000 IDENTIFICATION DIVISION.                                        \s
              000000 PROGRAM-ID. DECLTBL.                                            \s
              000000 DATA DIVISION.                                                  \s
              000000 WORKING-STORAGE SECTION.                                        \s
              000000     EXEC SQL DECLARE CUSTOMER TABLE (NAME CHAR(4)) END-EXEC.    \s
              000000 77 X PIC 99.                                                    \s
              000000 PROCEDURE DIVISION.                                             \s
              000000     GOBACK.                                                     \s
              """,
            spec -> spec.afterRecipe(cu -> assertThat(elidedDots(cu)).isEmpty())
          )
        );
    }

    private static List<String> paragraphNames(Cobol.CompilationUnit cu) {
        List<String> names = new ArrayList<>();
        new CobolIsoVisitor<Integer>() {
            @Override
            public Cobol.Paragraph visitParagraph(Cobol.Paragraph paragraph, Integer p) {
                names.add(((Cobol.Word) paragraph.getParagraphName()).getWord());
                return super.visitParagraph(paragraph, p);
            }
        }.visit(cu, 0);
        return names;
    }

    private static List<String> elidedDots(Cobol.CompilationUnit cu) {
        List<String> words = new ArrayList<>();
        new CobolIsoVisitor<Integer>() {
            @Override
            public Cobol.Word visitWord(Cobol.Word word, Integer p) {
                if (word.getMarkers().findFirst(ElidedDot.class).isPresent()) {
                    words.add(word.getWord());
                }
                return super.visitWord(word, p);
            }
        }.visit(cu, 0);
        return words;
    }
}
