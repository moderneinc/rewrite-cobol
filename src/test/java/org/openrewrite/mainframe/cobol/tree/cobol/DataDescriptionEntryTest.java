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
import org.openrewrite.Issue;
import org.openrewrite.mainframe.cobol.CobolIsoVisitor;
import org.openrewrite.mainframe.cobol.CobolTest;
import org.openrewrite.mainframe.cobol.tree.Cobol;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.mainframe.cobol.Assertions.cobol;
import static org.openrewrite.test.RewriteTest.toRecipe;

/**
 * Data items have to reach the LST individually. Printing round-trips whether or not entries are
 * separated correctly, so these assert the structure rather than the output.
 */
class DataDescriptionEntryTest extends CobolTest {

    private List<String> namesIn(String source) {
        List<String> names = new ArrayList<>();
        rewriteRun(
          spec -> spec.recipe(toRecipe(() -> new CobolIsoVisitor<ExecutionContext>() {
              @Override
              public Cobol.DataDescriptionEntry visitDataDescriptionEntry(Cobol.DataDescriptionEntry entry,
                                                                          ExecutionContext ctx) {
                  if (entry.getName() != null) {
                      names.add(entry.getName().getWord());
                  }
                  return super.visitDataDescriptionEntry(entry, ctx);
              }
          })),
          cobol(source)
        );
        return names;
    }

    @Test
    void entriesEndingInAPictureAreSeparate() {
        assertThat(namesIn(
          """
            000000 IDENTIFICATION DIVISION.                                        \s
            000000 PROGRAM-ID. PICPGM.                                             \s
            000000 DATA DIVISION.                                                  \s
            000000 WORKING-STORAGE SECTION.                                        \s
            000000 01  WS-A          PIC X(8).                                     \s
            000000 01  WS-B          PIC X(8).                                     \s
            000000 01  WS-C          PIC 9(4).                                     \s
            000000 PROCEDURE DIVISION.                                             \s
            000000     GOBACK.                                                     \s
            """
        )).containsExactly("WS-A", "WS-B", "WS-C");
    }

    @Test
    void groupItemsAndTheirChildren() {
        assertThat(namesIn(
          """
            000000 IDENTIFICATION DIVISION.                                        \s
            000000 PROGRAM-ID. GRPPGM.                                             \s
            000000 DATA DIVISION.                                                  \s
            000000 WORKING-STORAGE SECTION.                                        \s
            000000 01  WS-CUSTOMER.                                                \s
            000000     05 WS-ID       PIC X(8).                                    \s
            000000     05 WS-NAME     PIC X(30).                                   \s
            000000     05 WS-SSN      PIC X(9).                                    \s
            000000 01  WS-TOTAL       PIC S9(7)V99.                                \s
            000000 PROCEDURE DIVISION.                                             \s
            000000     GOBACK.                                                     \s
            """
        )).containsExactly("WS-CUSTOMER", "WS-ID", "WS-NAME", "WS-SSN", "WS-TOTAL");
    }

    /**
     * The decimal point of an edited picture is part of the picture, not the end of the entry.
     */
    @Test
    void editedPicturesKeepTheirDecimalPoint() {
        assertThat(namesIn(
          """
            000000 IDENTIFICATION DIVISION.                                        \s
            000000 PROGRAM-ID. EDTPGM.                                             \s
            000000 DATA DIVISION.                                                  \s
            000000 WORKING-STORAGE SECTION.                                        \s
            000000 01  WS-AMOUNT      PIC ZZ,ZZ9.99.                               \s
            000000 01  WS-RATE        PIC ---9.999.                                \s
            000000 01  WS-NEXT        PIC X(4).                                    \s
            000000 PROCEDURE DIVISION.                                             \s
            000000     GOBACK.                                                     \s
            """
        )).containsExactly("WS-AMOUNT", "WS-RATE", "WS-NEXT");
    }

    @Test
    void picturesFollowedByOtherClauses() {
        assertThat(namesIn(
          """
            000000 IDENTIFICATION DIVISION.                                        \s
            000000 PROGRAM-ID. CLSPGM.                                             \s
            000000 DATA DIVISION.                                                  \s
            000000 WORKING-STORAGE SECTION.                                        \s
            000000 01  WS-FLAG        PIC X      VALUE SPACE.                      \s
            000000 01  WS-COUNT       PIC 9(4)   COMP.                             \s
            000000 01  WS-PACKED      PIC S9(7)V99 COMP-3 VALUE ZERO.              \s
            000000 PROCEDURE DIVISION.                                             \s
            000000     GOBACK.                                                     \s
            """
        )).containsExactly("WS-FLAG", "WS-COUNT", "WS-PACKED");
    }

    @Issue("https://github.com/openrewrite/rewrite-cobol/issues/111")
    @Test
    void nistStyleColumnsSeparateEntries() {
        assertThat(namesIn(
          """
            000100 IDENTIFICATION DIVISION.                                         NC1034.2
            000200 PROGRAM-ID. NC103A.                                              NC1034.2
            000300 DATA DIVISION.                                                   NC1034.2
            000400 WORKING-STORAGE SECTION.                                         NC1034.2
            003800 01  PRINT-REC PICTURE X(120).                                    NC1034.2
            003900 01  DUMMY-RECORD PICTURE X(120).                                 NC1034.2
            004000 01  TEST-RESULTS.                                                NC1034.2
            004200     02 FILLER                   PIC X      VALUE SPACE.          NC1034.2
            004300     02 FEATURE                  PIC X(20)  VALUE SPACE.          NC1034.2
            005000 PROCEDURE DIVISION.                                              NC1034.2
            005100     GOBACK.                                                      NC1034.2
            """
        )).containsExactly("PRINT-REC", "DUMMY-RECORD", "TEST-RESULTS", "FILLER", "FEATURE");
    }
}
