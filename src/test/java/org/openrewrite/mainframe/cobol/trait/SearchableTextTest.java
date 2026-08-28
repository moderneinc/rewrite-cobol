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
package org.openrewrite.mainframe.cobol.trait;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Parser;
import org.openrewrite.SourceFile;
import org.openrewrite.mainframe.cobol.CobolParser;
import org.openrewrite.mainframe.cobol.CopybookParser;
import org.openrewrite.mainframe.cobol.Corpus;
import org.openrewrite.mainframe.cobol.SourcePositions;
import org.openrewrite.mainframe.cobol.tree.Cobol;
import org.openrewrite.marker.Range;
import org.openrewrite.test.RewriteTest;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.mainframe.cobol.Assertions.cobol;
import static org.openrewrite.test.RewriteTest.toRecipe;

/**
 * The program is written the way real mainframe source is, with sequence numbers and an
 * identification area, because neither is text a search should find.
 */
class SearchableTextTest implements RewriteTest {

    private static final String PROGRAM =
      """
        000100******************************************************************IDNT0001
        000200* Program:     SRCHTXT                                           *IDNT0002
        000300* Function:    Show what a search reads                          *IDNT0003
        000400******************************************************************IDNT0004
        000500 IDENTIFICATION DIVISION.                                         IDNT0005
        000600 PROGRAM-ID. SRCHTXT.                                             IDNT0006
        000700 DATE-WRITTEN. JULY 2022.                                         IDNT0007
        000800 DATA DIVISION.                                                   IDNT0008
        000900 WORKING-STORAGE SECTION.                                         IDNT0009
        001000 01  WS-ACCT-ID       PIC X(30) VALUE 'ACCT'.                     IDNT0010
        001100*--- Rates are stored as a fraction of one -----------------------IDNT0011
        001200 01  WS-RATE          PIC 9V99  VALUE 0.05.                       IDNT0012
        001300     *> A floating comment on a line of its own
        001400 PROCEDURE DIVISION.                                              IDNT0014
        001500 MAIN-PARA.                                                       IDNT0015
        001600     MOVE 'X' TO WS-ACCT-ID.               *> and one after code
        001700     DISPLAY 'ACCOUNT FILE OPEN FAILED, CHECK THE DD STATEMENT INP
        001800-    'UT ' WS-ACCT-ID.                                            IDNT0018
        001900     EXEC CICS READ FILE('ACCTFILE') INTO(WS-ACCT-ID) END-EXEC.   IDNT0019
        002000     GOBACK.                                                      IDNT0020
        """;

    private static class Found {
        final List<SearchableText> words = new ArrayList<>();
        SourcePositions positions;

        SearchableText word(String text) {
            return words.stream().filter(w -> w.getText().equals(text)).findFirst().orElseThrow();
        }

        List<SearchableText.Comment> comments() {
            return words.stream().flatMap(w -> w.getComments().stream()).collect(Collectors.toList());
        }

        String at(Range range) {
            return range.getStart().getLine() + ":" + range.getStart().getColumn() +
                   "-" + range.getEnd().getLine() + ":" + range.getEnd().getColumn() +
                   " " + positions.textOf(range);
        }
    }

    private Found parse(String source) {
        Found found = new Found();
        AtomicReference<Cobol.CompilationUnit> cu = new AtomicReference<>();
        rewriteRun(
          spec -> spec.recipe(toRecipe(() ->
            new SearchableText.Matcher().<ExecutionContext>asVisitor((text, ctx) -> {
                found.words.add(text);
                cu.compareAndSet(null, text.getCursor().firstEnclosing(Cobol.CompilationUnit.class));
                return text.getTree();
            }))),
          cobol(source)
        );
        found.positions = SourcePositions.of(cu.get());
        return found;
    }

    @Test
    void tellsTheLayersApart() {
        Found found = parse(PROGRAM);

        assertThat(found.words).extracting(SearchableText::toString)
          .contains(
            "CODE WS-ACCT-ID",
            "STRING_LITERAL 'ACCT'",
            "NUMERIC_LITERAL 0.05",
            "COMMENT JULY 2022.",
            "STRING_LITERAL 'ACCTFILE'")
          // A level number is structure and a picture is a shape; neither is a value to search for.
          .doesNotContain("NUMERIC_LITERAL 01", "NUMERIC_LITERAL 30", "NUMERIC_LITERAL 99")
          .contains("CODE 01", "CODE 30");
    }

    /**
     * Ora-Web's rule: a hyphen, a period and an underscore end a token, so {@code ACCT} finds
     * {@code WS-ACCT-ID}. Whitespace and punctuation end one as well, or nothing in a literal could
     * be found by the word inside it.
     */
    @Test
    void endsATokenAtAHyphenPeriodOrUnderscore() {
        assertThat(SearchableText.tokens("WS-ACCT-ID")).extracting(SearchableText.Token::getText)
          .containsExactly("WS", "ACCT", "ID");
        assertThat(SearchableText.tokens("CUST.NAME_1")).extracting(SearchableText.Token::getText)
          .containsExactly("CUST", "NAME", "1");
        assertThat(SearchableText.tokens("'e=\"width:70%; font:12px'")).extracting(SearchableText.Token::getText)
          .containsExactly("e", "width", "70", "font", "12px");
        assertThat(SearchableText.tokens("WS-ACCT-ID")).extracting(SearchableText.Token::getOffset)
          .containsExactly(0, 3, 8);
    }

    /**
     * The literal is one thing saying INPUT, which a phrase has to match, and two lines of source,
     * which a highlight has to respect.
     */
    @Test
    void joinsALiteralContinuedAcrossLinesAndPlacesEachPiece() {
        Found found = parse(PROGRAM);
        SearchableText literal = found.word("'ACCOUNT FILE OPEN FAILED, CHECK THE DD STATEMENT INPUT '");

        assertThat(literal.getLayer()).isEqualTo(SearchableText.Layer.STRING_LITERAL);
        assertThat(literal.getTokens()).extracting(SearchableText.Token::getText).contains("INPUT");

        List<SearchableText.Piece> pieces = literal.getPieces();
        assertThat(pieces).extracting(SearchableText.Piece::getText)
          .containsExactly("'ACCOUNT FILE OPEN FAILED, CHECK THE DD STATEMENT INP", "UT '");
        assertThat(pieces).extracting(SearchableText.Piece::getOffset).containsExactly(0, 53);
        assertThat(pieces).extracting(piece -> found.at(piece.range(found.positions)))
          .containsExactly(
            "17:20-17:73 'ACCOUNT FILE OPEN FAILED, CHECK THE DD STATEMENT INP",
            "18:13-18:17 UT '");
        assertThat(found.at(found.positions.get(literal.getTree()))).startsWith("17:20-18:17 ");
    }

    /**
     * The banner is what every CardDemo program opens with, and the asterisks down each side of it
     * are drawing, not words. The all-asterisk rules say nothing and are not reported at all.
     */
    @Test
    void stripsTheBordersOffABoxedComment() {
        Found found = parse(PROGRAM);

        assertThat(found.word("IDENTIFICATION").getComments())
          .extracting(comment -> found.at(comment.range(found.positions)))
          .containsExactly(
            "2:9-2:29 Program:     SRCHTXT",
            "3:9-3:46 Function:    Show what a search reads");
        assertThat(found.comments()).extracting(comment -> found.at(comment.range(found.positions)))
          .contains("11:12-11:49 Rates are stored as a fraction of one");
    }

    /**
     * A comment entry is a word to the grammar, and reads the same way a comment line does: what
     * {@code DATE-WRITTEN} says, without the whitespace the card padded it with.
     */
    @Test
    void readsACommentEntryAsAComment() {
        Found found = parse(PROGRAM);
        SearchableText entry = found.word("JULY 2022.");

        assertThat(entry.getLayer()).isEqualTo(SearchableText.Layer.COMMENT);
        assertThat(entry.getPieces()).singleElement().satisfies(piece ->
          assertThat(found.at(piece.range(found.positions))).isEqualTo("7:22-7:32 JULY 2022."));
    }

    @Test
    void readsAFloatingCommentOnItsOwnLineOrAfterCode() {
        Found found = parse(PROGRAM);

        assertThat(found.comments()).extracting(SearchableText.Comment::getText)
          .contains("A floating comment on a line of its own", "and one after code");
        assertThat(found.comments()).extracting(comment -> found.at(comment.range(found.positions)))
          .contains(
            "13:15-13:54 A floating comment on a line of its own",
            "16:53-16:71 and one after code");
    }

    /**
     * Sequence numbers and the identification area are on every line and are not what anybody is
     * looking for.
     */
    @Test
    void leavesTheSequenceAndIdentificationAreasOut() {
        Found found = parse(PROGRAM);

        assertThat(found.words).extracting(SearchableText::getText).noneMatch(text -> text.contains("IDNT"));
        assertThat(found.comments()).extracting(SearchableText.Comment::getText).noneMatch(text -> text.contains("IDNT"));
        assertThat(found.words).extracting(SearchableText::getText).noneMatch(text -> text.matches("00\\d{4}"));
    }

    /**
     * Preprocessing takes an EXEC block out of the text the grammar sees, and it is where the
     * literals a search most wants — file names, table names — are written.
     */
    @Test
    void placesTheWordsOfAnExecBlock() {
        Found found = parse(PROGRAM);
        SearchableText file = found.word("'ACCTFILE'");

        assertThat(file.getPieces()).singleElement().satisfies(piece ->
          assertThat(found.at(piece.range(found.positions))).isEqualTo("19:32-19:42 'ACCTFILE'"));
    }

    /**
     * The copybook's fields are the copybook's text and the copybook is searched as itself; what the
     * program wrote is the COPY statement. INCEPTION copies INCEPTION_2, which copies INCEPTION_3,
     * and those two statements are written in the copybooks, not here.
     */
    @Test
    void leavesACopybooksTextToTheCopybook() {
        Found found = parse(
          """
            000000 IDENTIFICATION DIVISION.                                         *
                   PROGRAM-ID. IC109A.                                              *
                   DATA DIVISION.                                                   *
                   LINKAGE SECTION.                                                 *
                       01  GRP-01.                                                  *
                           COPY INCEPTION.                                          *
            """
        );

        assertThat(found.words).extracting(SearchableText::getText)
          .contains("COPY", "INCEPTION")
          .doesNotContain("INCEPTION_2", "INCEPTION_3", "SUB-CALLED", "DN1", "PICTURE");
    }

    /**
     * Run with {@code COBOL_CORPUS=/path/to/corpus}. Every piece and comment of every program is
     * checked against the source it claims to sit at, because each of these produced plausible
     * output before it was right.
     */
    @EnabledIfEnvironmentVariable(named = "COBOL_CORPUS", matches = ".+")
    @Test
    void readsRealPrograms() throws IOException {
        Path root = Paths.get(System.getenv("COBOL_CORPUS"));
        Map<SearchableText.Layer, Integer> byLayer = new EnumMap<>(SearchableText.Layer.class);
        int comments = 0;
        int continued = 0;
        Map<String, Integer> unplaced = new HashMap<>();
        List<String> misplaced = new ArrayList<>();
        Map<String, List<SearchableText>> programs = new HashMap<>();
        Map<String, SourcePositions> positionsOf = new HashMap<>();

        for (Path repository : Corpus.repositories(root)) {
            List<Parser.Input> programInputs = Corpus.inputs(Corpus.programs(repository));
            if (programInputs.isEmpty()) {
                continue;
            }
            List<SourceFile> copybooks = CopybookParser.builder().build()
              .parseInputs(Corpus.inputs(Corpus.copybooks(repository)), root, new InMemoryExecutionContext())
              .collect(Collectors.toList());
            List<SourceFile> parsed = CobolParser.builder().copybooks(copybooks).build()
              .parseInputs(programInputs, root, new InMemoryExecutionContext())
              .collect(Collectors.toList());
            for (SourceFile program : parsed) {
                if (!(program instanceof Cobol.CompilationUnit)) {
                    continue;
                }
                Cobol.CompilationUnit cu = (Cobol.CompilationUnit) program;
                String name = program.getSourcePath().getFileName().toString();
                SourcePositions positions = SourcePositions.of(cu);
                List<SearchableText> words = new ArrayList<>();
                new SearchableText.Matcher().<Integer>asVisitor((text, p) -> {
                    words.add(text);
                    return text.getTree();
                }).visit(cu, 0);
                programs.put(name, words);
                positionsOf.put(name, positions);

                for (SearchableText word : words) {
                    byLayer.merge(word.getLayer(), 1, Integer::sum);
                    List<SearchableText.Piece> pieces = word.getPieces();
                    if (pieces.size() > 1) {
                        continued++;
                    }
                    for (SearchableText.Piece piece : pieces) {
                        Range range = piece.range(positions);
                        if (range == null) {
                            unplaced.merge(word.getText(), 1, Integer::sum);
                        } else if (!positions.textOf(range).equals(piece.getText())) {
                            misplaced.add(name + ": " + piece.getText() + " at " + positions.textOf(range));
                        }
                    }
                    for (SearchableText.Comment comment : word.getComments()) {
                        comments++;
                        Range range = comment.range(positions);
                        if (range == null || !positions.textOf(range).equals(comment.getText())) {
                            misplaced.add(name + ": " + comment.getText() + " at " +
                                          (range == null ? "nowhere" : positions.textOf(range)));
                        }
                    }
                }
            }
        }

        System.out.printf("words by layer: %s%n", byLayer);
        System.out.printf("comments: %d, words continued across lines: %d%n", comments, continued);
        // The words of a COPY statement print through the preprocessor and are not placed yet.
        System.out.printf("words without a position: %d, most often %s%n",
          unplaced.values().stream().mapToInt(Integer::intValue).sum(),
          unplaced.entrySet().stream()
            .sorted((a, b) -> b.getValue() - a.getValue())
            .limit(5)
            .map(e -> e.getKey() + " x" + e.getValue())
            .collect(Collectors.joining(", ")));
        assertThat(misplaced).isEmpty();

        // CardDemo's statement writer keeps its HTML in 88-level literals wider than a card.
        List<SearchableText> statement = programs.get("CBSTM03A.CBL");
        SearchableText html = statement.stream()
          .filter(w -> w.getText().startsWith("'<table  align=\"center\" frame=\"box\" styl"))
          .findFirst().orElseThrow();
        assertThat(html.getText()).endsWith("e=\"width:70%; font:12px Segoe UI,sans-serif;\">'");
        assertThat(html.getTokens()).extracting(SearchableText.Token::getText).contains("style", "width", "70");
        assertThat(html.getPieces()).extracting(piece -> piece.range(positionsOf.get("CBSTM03A.CBL")).getStart().getLine())
          .containsExactly(157, 158);

        // Every CardDemo program opens with a boxed banner.
        List<SearchableText.Comment> banner = programs.get("COACTVWC.cbl").stream()
          .flatMap(w -> w.getComments().stream())
          .limit(3)
          .collect(Collectors.toList());
        assertThat(banner).extracting(SearchableText.Comment::getText)
          .containsExactly(
            "Program:     COACTVWC.CBL",
            "Layer:       Business logic",
            "Function:    Accept and process Account View request");
    }
}
