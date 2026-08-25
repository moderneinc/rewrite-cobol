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
import org.openrewrite.cobol.tree.CobolPreprocessor;
import org.openrewrite.cobol.tree.Statement;
import org.openrewrite.marker.Range;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.cobol.Assertions.cobol;
import static org.openrewrite.cobol.Assertions.copybook;

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
     * The EXEC blocks are where a lineage edge crosses the program boundary, so they are the
     * statements a catalog most wants to point at. Preprocessing takes their text out of what the
     * grammar sees and the printer puts it back, which is a path worth pinning separately.
     */
    @Test
    void placesAnExecBlockAtItsOwnText() {
        rewriteRun(
          cobol(
            """
              000100 IDENTIFICATION DIVISION.                                         00000010
              000200 PROGRAM-ID. POSEXEC.                                             00000020
              000300 DATA DIVISION.                                                   00000030
              000400 WORKING-STORAGE SECTION.                                         00000040
              000500 01  WS-ACCT          PIC X(30).                                  00000050
              000600 PROCEDURE DIVISION.                                              00000060
              000700 MAIN-PARA.                                                       00000070
              000800     EXEC CICS READ FILE('ACCTFILE') INTO(WS-ACCT) END-EXEC.      00000080
              000900     GOBACK.                                                      00000090
              """,
            spec -> spec.afterRecipe(cu -> {
                SourcePositions positions = SourcePositions.of(cu);
                Statement exec = statementsIn(cu).get(0);
                assertThat(exec).isInstanceOf(Cobol.ExecCicsStatement.class);

                // The period comes with the block: preprocessing takes the whole card out of the text the
                // grammar sees, and what stands in its place is the period, printing nothing of its own.
                Range range = positions.get(exec);
                assertThat(range).isNotNull();
                assertThat(positions.textOf(range))
                  .isEqualTo("EXEC CICS READ FILE('ACCTFILE') INTO(WS-ACCT) END-EXEC.");
                assertThat(range.getStart().getLine()).isEqualTo(8);
                assertThat(range.getStart().getColumn()).isEqualTo(12);
            }))
        );
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

    /**
     * The COPY statement itself is written in the program even though what it brings in is not. It
     * prints through the preprocessor rather than as a word of the program, so it is placed apart.
     */
    @Test
    void placesACopyStatementWhereItWasWritten() {
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
              001000     GOBACK.                                                      00000100
              """,
            spec -> spec.afterRecipe(cu -> {
                SourcePositions positions = SourcePositions.of(cu);
                Range range = positions.get(copyStatementsIn(cu).get(0));
                assertThat(range).isNotNull();
                assertThat(positions.textOf(range)).isEqualTo("COPY COPYSTMT.");
                assertThat(range.getStart().getLine()).isEqualTo(9);
                assertThat(range.getStart().getColumn()).isEqualTo(12);
            }))
        );
    }

    /**
     * A copybook parsed on its own is preprocessor source outright, so everything in it is placed
     * the same way.
     */
    @Test
    void placesTheStatementsOfACopybook() {
        rewriteRun(
          copybook(
            """
              000100* Nothing here is written by anything but this member.             00000010
              000200 COPY COPYSTMT.                                                    00000020
              """,
            spec -> spec.afterRecipe(cpy -> {
                SourcePositions positions = SourcePositions.of(cpy);
                Range range = positions.get(copyStatementsIn(cpy).get(0));
                assertThat(range).isNotNull();
                assertThat(positions.textOf(range)).isEqualTo("COPY COPYSTMT.");
                assertThat(range.getStart().getLine()).isEqualTo(2);
                assertThat(range.getStart().getColumn()).isEqualTo(8);
            }))
        );
    }

    /**
     * A literal continued over a column-7 break is one word and two lines of source. The word's
     * range spans both; its pieces are what a highlight needs, one per line. The comment line and
     * the floating comment are not nodes, so they are found by identity.
     */
    @Test
    void placesEachLineOfAContinuedLiteralAndTheCommentsAroundIt() {
        rewriteRun(
          cobol(
            """
              000100 IDENTIFICATION DIVISION.                                         00000010
              000200 PROGRAM-ID. POSCONT.                                             00000020
              000300 PROCEDURE DIVISION.                                              00000030
              000400 MAIN-PARA.                                                       00000040
              000500* The message is wider than a card.                               00000050
              000600     DISPLAY 'ACCOUNT FILE OPEN FAILED, CHECK THE DD STATEMENT INP
              000700-    'UT'.                                              *> so far
              000800     GOBACK.                                                      00000080
              """,
            spec -> spec.afterRecipe(cu -> {
                SourcePositions positions = SourcePositions.of(cu);
                Cobol.Word display = ((Cobol.Display) statementsIn(cu).get(0)).getDisplay();
                Cobol.Word literal = (Cobol.Word) ((Cobol.Display) statementsIn(cu).get(0)).getOperands().get(0);

                assertThat(positions.textOf(positions.get(literal)))
                  .startsWith("'ACCOUNT FILE").endsWith("UT'").contains("\n000700-");
                assertThat(positions.pieces(literal)).extracting(positions::textOf)
                  .containsExactly("'ACCOUNT FILE OPEN FAILED, CHECK THE DD STATEMENT INP", "UT'");

                assertThat(display.getLines()).singleElement().satisfies(line ->
                  assertThat(positions.textOf(positions.get(line)).trim())
                    .isEqualTo("The message is wider than a card."));
                // The floating comment belongs to the last word on its line, which is the period.
                Range comment = positions.get(wordsIn(cu).stream()
                  .map(Cobol.Word::getCommentArea)
                  .filter(area -> area != null && area.getComment().startsWith("*>"))
                  .findFirst().orElseThrow());
                assertThat(positions.textOf(comment)).isEqualTo("*> so far");
                assertThat(comment.getStart().getLine()).isEqualTo(7);
            }))
        );
    }

    private static List<CobolPreprocessor.CopyStatement> copyStatementsIn(Cobol.CompilationUnit cu) {
        List<CobolPreprocessor.CopyStatement> copies = new ArrayList<>();
        for (Cobol.Word word : wordsIn(cu)) {
            for (CobolPreprocessor preprocessorStatement : word.getPreprocessorStatements()) {
                if (preprocessorStatement instanceof CobolPreprocessor.CopyStatement) {
                    copies.add((CobolPreprocessor.CopyStatement) preprocessorStatement);
                }
            }
        }
        return copies;
    }

    private static List<CobolPreprocessor.CopyStatement> copyStatementsIn(CobolPreprocessor.Copybook copybook) {
        List<CobolPreprocessor.CopyStatement> copies = new ArrayList<>();
        new CobolPreprocessorIsoVisitor<Integer>() {
            @Override
            public CobolPreprocessor.CopyStatement visitCopyStatement(CobolPreprocessor.CopyStatement copyStatement, Integer p) {
                copies.add(copyStatement);
                return copyStatement;
            }
        }.visit(copybook, 0);
        return copies;
    }

    private static List<Cobol.Word> wordsIn(Cobol.CompilationUnit cu) {
        List<Cobol.Word> words = new ArrayList<>();
        new CobolIsoVisitor<Integer>() {
            @Override
            public Cobol.Word visitWord(Cobol.Word word, Integer p) {
                words.add(word);
                return word;
            }
        }.visit(cu, 0);
        return words;
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
