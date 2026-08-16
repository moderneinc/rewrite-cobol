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
import org.openrewrite.cobol.tree.Cobol;
import org.openrewrite.cobol.tree.Statement;
import org.openrewrite.marker.Range;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.cobol.Assertions.cobol;

/**
 * The program is written the way real mainframe source is: sequence numbers in columns 1 to 6 and an
 * identification area in 73 to 80. Neither belongs to a statement, and a position that swallowed
 * them would point a catalog at the line rather than at the code.
 */
class SourcePositionsTest extends CobolTest {

    private static final String PROGRAM =
      """
        000100 IDENTIFICATION DIVISION.                                         00000010
        000200 PROGRAM-ID. POSNS.                                               00000020
        000300 DATA DIVISION.                                                   00000030
        000400 WORKING-STORAGE SECTION.                                         00000040
        000500 01  WS-IN            PIC X(30).                                  00000050
        000600 01  WS-WORK          PIC X(30).                                  00000060
        000700 PROCEDURE DIVISION.                                              00000070
        000800 MAIN-PARA.                                                       00000080
        000900     MOVE WS-IN TO WS-WORK.                                       00000090
        001000     DISPLAY WS-WORK.                                             00000100
        001100     GOBACK.                                                      00000110
        """;

    @Test
    void placesEachStatementAtItsOwnWords() {
        List<String> placed = new ArrayList<>();
        rewriteRun(
          cobol(PROGRAM, spec -> spec.afterRecipe(cu -> {
              SourcePositions positions = SourcePositions.of(cu);
              assertThat(positions.getSource()).isEqualTo(PROGRAM.stripTrailing());
              for (Statement statement : statementsIn(cu)) {
                  Range range = positions.get(statement);
                  assertThat(range).isNotNull();
                  placed.add(range.getStart().getLine() + ":" + range.getStart().getColumn() +
                             "-" + range.getEnd().getLine() + ":" + range.getEnd().getColumn() +
                             " " + positions.textOf(range));
              }
          }))
        );

        // The terminating period ends the sentence rather than the statement, so it is outside.
        assertThat(placed).containsExactly(
          "9:12-9:33 MOVE WS-IN TO WS-WORK",
          "10:12-10:27 DISPLAY WS-WORK",
          "11:12-11:18 GOBACK");
    }

    /**
     * A copied statement prints nothing in the program that includes it, so it has nowhere to point
     * at here. Reporting no position is the answer; the copybook is what has one.
     */
    @Test
    void leavesACopiedStatementWithoutAPosition() {
        rewriteRun(
          cobol(
            """
              000100 IDENTIFICATION DIVISION.                                         00000010
              000200 PROGRAM-ID. POSCOPY.                                             00000020
              000300 DATA DIVISION.                                                   00000030
              000400 WORKING-STORAGE SECTION.                                         00000040
              000500 01  WS-IN            PIC X(30).                                  00000050
              000600 01  WS-WORK          PIC X(30).                                  00000060
              000700 PROCEDURE DIVISION.                                              00000070
              000800 MAIN-PARA.                                                       00000080
              000900     COPY COPYSTMT.                                               00000090
              001000     DISPLAY WS-WORK.                                             00000100
              001100     GOBACK.                                                      00000110
              """,
            spec -> spec.afterRecipe(cu -> {
                SourcePositions positions = SourcePositions.of(cu);
                List<Statement> statements = statementsIn(cu);
                assertThat(statements.get(0)).isInstanceOf(Cobol.MoveStatement.class);
                assertThat(positions.get(statements.get(0))).isNull();

                Range display = positions.get(statements.get(1));
                assertThat(display).isNotNull();
                assertThat(positions.textOf(display)).isEqualTo("DISPLAY WS-WORK");
            }))
        );
    }

    private static List<Statement> statementsIn(Cobol.CompilationUnit cu) {
        List<Statement> statements = new ArrayList<>();
        new CobolIsoVisitor<Integer>() {
            @Override
            public Cobol.Paragraph visitParagraph(Cobol.Paragraph paragraph, Integer p) {
                for (Cobol.Sentence sentence : paragraph.getSentences()) {
                    statements.addAll(sentence.getStatements());
                }
                return paragraph;
            }
        }.visit(cu, 0);
        return statements;
    }
}
