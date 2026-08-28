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
package org.openrewrite.mainframe.cobol.tree.cobol;

import org.junit.jupiter.api.Test;
import org.openrewrite.ExecutionContext;
import org.openrewrite.mainframe.cobol.CobolIsoVisitor;
import org.openrewrite.mainframe.cobol.CobolTest;
import org.openrewrite.mainframe.cobol.tree.Cobol;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.mainframe.cobol.Assertions.cobol;
import static org.openrewrite.test.RewriteTest.toRecipe;

/**
 * A COBOL 2002 floating comment ({@code *>}) ends the content area wherever it appears, and the rest of the line
 * belongs to the comment area. Printing round-trips whether or not the words after a floating comment were read
 * from the right source positions, so these assert the structure rather than the output.
 */
class FloatingCommentTest extends CobolTest {

    private List<String> entriesIn(String source) {
        List<String> entries = new ArrayList<>();
        rewriteRun(
          spec -> spec.recipe(toRecipe(() -> new CobolIsoVisitor<ExecutionContext>() {
              @Override
              public Cobol.DataDescriptionEntry visitDataDescriptionEntry(Cobol.DataDescriptionEntry entry,
                                                                         ExecutionContext ctx) {
                  if (entry.getName() != null) {
                      entries.add(entry.getName().getWord());
                  }
                  return super.visitDataDescriptionEntry(entry, ctx);
              }

              @Override
              public Cobol.Picture visitPicture(Cobol.Picture picture, ExecutionContext ctx) {
                  StringBuilder chars = new StringBuilder();
                  for (Cobol.Word word : picture.getWords()) {
                      chars.append(word.getWord());
                  }
                  entries.add("PIC " + chars + (picture.getParenthesized() == null ? "" :
                    picture.getParenthesized().print(getCursor())));
                  return super.visitPicture(picture, ctx);
              }
          })),
          cobol(source)
        );
        return entries;
    }

    private List<String> literalsIn(String source) {
        List<String> literals = new ArrayList<>();
        rewriteRun(
          spec -> spec.recipe(toRecipe(() -> new CobolIsoVisitor<ExecutionContext>() {
              @Override
              public Cobol.Word visitWord(Cobol.Word word, ExecutionContext ctx) {
                  if (word.getWord().startsWith("'")) {
                      literals.add(word.getWord());
                  }
                  return super.visitWord(word, ctx);
              }
          })),
          cobol(source)
        );
        return literals;
    }

    @Test
    void floatingCommentOnItsOwnLine() {
        assertThat(entriesIn(
          """
            000000 IDENTIFICATION DIVISION.                                        \s
            000000 PROGRAM-ID. FLTPGM.                                             \s
            000000 DATA DIVISION.                                                  \s
            000000 WORKING-STORAGE SECTION.                                        \s
            000000 01  WS-GROUP.                                                   \s
            000000     *> Input parameters (composite key)                         \s
            000000     05 WS-SORTCODE   PIC 9(6).                                  \s
            000000     05 WS-ACCNO      PIC 9(8).                                  \s
            000000 PROCEDURE DIVISION.                                             \s
            000000     GOBACK.                                                     \s
            """
        )).containsExactly("WS-GROUP", "WS-SORTCODE", "PIC 9(6)", "WS-ACCNO", "PIC 9(8)");
    }

    @Test
    void trailingFloatingComment() {
        assertThat(entriesIn(
          """
            000000 IDENTIFICATION DIVISION.                                        \s
            000000 PROGRAM-ID. FLTPGM.                                             \s
            000000 DATA DIVISION.                                                  \s
            000000 WORKING-STORAGE SECTION.                                        \s
            000000 01  WS-SORTCODE      PIC 9(6). *> the sorting code              \s
            000000 01  WS-ACCNO         PIC 9(8).                                  \s
            000000 PROCEDURE DIVISION.                                             \s
            000000     GOBACK.                                                     \s
            """
        )).containsExactly("WS-SORTCODE", "PIC 9(6)", "WS-ACCNO", "PIC 9(8)");
    }

    /**
     * The tag does not have to be followed by a separator.
     */
    @Test
    void floatingCommentWithoutASeparator() {
        assertThat(entriesIn(
          """
            000000 IDENTIFICATION DIVISION.                                        \s
            000000 PROGRAM-ID. FLTPGM.                                             \s
            000000 DATA DIVISION.                                                  \s
            000000 WORKING-STORAGE SECTION.                                        \s
            000000     *>no separator after the tag                                \s
            000000 01  WS-SORTCODE      PIC 9(6). *>                               \s
            000000 PROCEDURE DIVISION.                                             \s
            000000     GOBACK.                                                     \s
            """
        )).containsExactly("WS-SORTCODE", "PIC 9(6)");
    }

    /**
     * The tag only starts a comment at the beginning of the content area or after a space.
     */
    @Test
    void tagInsideALiteralIsNotAComment() {
        assertThat(literalsIn(
          """
            000000 IDENTIFICATION DIVISION.                                        \s
            000000 PROGRAM-ID. FLTPGM.                                             \s
            000000 PROCEDURE DIVISION.                                             \s
            000000     DISPLAY 'A *> B'.                                           \s
            000000     DISPLAY 'C'. *> and a real comment                          \s
            000000     GOBACK.                                                     \s
            """
        )).containsExactly("'A *> B'", "'C'");
    }

    @Test
    void floatingCommentAfterAContinuedLiteral() {
        assertThat(literalsIn(
          """
            000000 IDENTIFICATION DIVISION.                                        \s
            000000 PROGRAM-ID. FLTPGM.                                             \s
            000000 PROCEDURE DIVISION.                                             \s
            000000     DISPLAY 'first part                                          \s
            000000-    ' second part'. *> a trailing comment                       \s
            000000     GOBACK.                                                     \s
            """
        )).containsExactly("'first part                                           second part'");
    }

    @Test
    void floatingCommentBeforeTheCommentArea() {
        assertThat(entriesIn(
          """
            000000 IDENTIFICATION DIVISION.                                         C_AREA.01
            000000 PROGRAM-ID. FLTPGM.                                              C_AREA.02
            000000 DATA DIVISION.                                                   C_AREA.03
            000000 WORKING-STORAGE SECTION.                                         C_AREA.04
            000000 01  WS-SORTCODE      PIC 9(6). *> the sorting code               C_AREA.05
            000000     *> a whole line of comment                                   C_AREA.06
            000000 01  WS-ACCNO         PIC 9(8).                                   C_AREA.07
            000000 PROCEDURE DIVISION.                                              C_AREA.08
            000000     GOBACK.                                                      C_AREA.09
            """
        )).containsExactly("WS-SORTCODE", "PIC 9(6)", "WS-ACCNO", "PIC 9(8)");
    }

    /**
     * The tag ends the content area even when it runs right up against column 72.
     */
    @Test
    void floatingCommentAtTheEndOfTheContentArea() {
        rewriteRun(
          cobol(
            """
              000000 IDENTIFICATION DIVISION.                                         C_AREA.01
              000000 PROGRAM-ID. FLTPGM.                                              C_AREA.02
              000000 PROCEDURE DIVISION.                                              C_AREA.03
              000000     GOBACK.                                                   *> C_AREA.04
              """
          )
        );
    }

    /**
     * A comment line is a comment already; the tag inside one is just text.
     */
    @Test
    void tagOnACommentLineIsNotAFloatingComment() {
        rewriteRun(
          cobol(
            """
              000000 IDENTIFICATION DIVISION.                                        \s
              000000 PROGRAM-ID. FLTPGM.                                             \s
              000000/ a page break comment mentioning *> the tag                     \s
              000000* a comment mentioning *> the tag                                \s
              000000 PROCEDURE DIVISION.                                             \s
              000000     GOBACK.                                                     \s
              """
          )
        );
    }

    @Test
    void floatingCommentInsideACommentEntry() {
        rewriteRun(
          cobol(
            """
              000000 IDENTIFICATION DIVISION.                                        \s
              000000 PROGRAM-ID. FLTPGM.                                             \s
              000000 AUTHOR.  MODERNE.                                               \s
              000000     *> a floating comment inside a comment entry                \s
              000000     more comment entry text                                     \s
              000000 PROCEDURE DIVISION.                                             \s
              000000     GOBACK.                                                     \s
              """
          )
        );
    }

    /**
     * Copied source is read against the copybook's own column areas, then printed into the program being parsed.
     */
    @Test
    void floatingCommentInACopiedSource() {
        assertThat(entriesIn(
          """
            000000 IDENTIFICATION DIVISION.                                        \s
            000000 PROGRAM-ID. FLTPGM.                                             \s
            000000 DATA DIVISION.                                                  \s
            000000 WORKING-STORAGE SECTION.                                        \s
            000000 COPY FLTCMT.                                                    \s
            000000 PROCEDURE DIVISION.                                             \s
            000000     GOBACK.                                                     \s
            """
        )).containsExactly("FLTCMT-COMMAREA", "FLTCMT-EYE", "PIC X(4)", "FLTCMT-EYE-VALID",
          "FLTCMT-SORTCODE", "PIC 9(6)", "FLTCMT-ACCNO", "PIC 9(8)");
    }
}
