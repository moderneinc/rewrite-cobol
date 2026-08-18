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
package org.openrewrite.bms;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.SourceFile;
import org.openrewrite.bms.marker.SequenceArea;
import org.openrewrite.bms.tree.Bms;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Printing back exactly, over the layouts a test fixture cannot express.
 * <p>
 * These parse directly rather than through {@code Assertions.bms}, because the test framework trims
 * the common indentation off a source block — which for BMS would move the name field, the operand
 * field and column 72, and so change what is being tested.
 */
class BmsParserTest {

    @Test
    void aSequenceAreaIsKeptAndPrintedBack() {
        String source =
          "COSGN00 DFHMSD LANG=COBOL" + blanks(47) + "COSGN001\n" +
          "        DFHMDF POS=(1,1),LENGTH=6" + blanks(40) + "COSGN002\n";

        Bms.CompilationUnit cu = parse(source);
        assertThat(cu.printAll()).isEqualTo(source);

        // Columns 73-80 are not operands. They are carried as a marker on the word that ends the
        // line they belong to, which is where a statement written over several lines needs them.
        assertThat(sequenceAreasIn(cu)).containsExactly("COSGN001", "COSGN002");
    }

    /**
     * A line padded out to 80 with blanks has no sequence area, only trailing space. Reading one
     * there would put a word where the source has none.
     */
    @Test
    void blankColumnsAreNotASequenceArea() {
        String source = "COSGN00 DFHMSD LANG=COBOL" + blanks(55) + "\n";

        Bms.CompilationUnit cu = parse(source);
        assertThat(cu.printAll()).isEqualTo(source);
        assertThat(cu.getStatements()).hasSize(1);
    }

    @Test
    void windowsLineEndings() {
        String source =
          "COSGN00 DFHMSD LANG=COBOL,                                             -\r\n" +
          "               MODE=INOUT\r\n" +
          "        END\r\n";

        Bms.CompilationUnit cu = parse(source);
        assertThat(cu.printAll()).isEqualTo(source);
        assertThat(cu.getStatements()).hasSize(2);
    }

    @Test
    void noTrailingNewline() {
        String source =
          "COSGN00 DFHMSD LANG=COBOL\n" +
          "        END";

        Bms.CompilationUnit cu = parse(source);
        assertThat(cu.printAll()).isEqualTo(source);
        assertThat(cu.getStatements()).hasSize(2);
    }

    @Test
    void anEmptyLineBetweenStatements() {
        String source =
          "COSGN00 DFHMSD LANG=COBOL\n" +
          "\n" +
          "        END\n";

        Bms.CompilationUnit cu = parse(source);
        assertThat(cu.printAll()).isEqualTo(source);
        assertThat(cu.getStatements()).hasSize(2);
    }

    /**
     * An apostrophe in a comment is not the start of a literal, and a comment line runs to the end
     * of the line whatever is written in it.
     */
    @Test
    void anApostropheInAComment() {
        String source =
          "* Don't take this as the start of a string\n" +
          "COSGN00 DFHMSD LANG=COBOL\n";

        Bms.CompilationUnit cu = parse(source);
        assertThat(cu.printAll()).isEqualTo(source);
        assertThat(cu.getStatements()).hasSize(2);
        assertThat(cu.getStatements().get(0)).isInstanceOf(Bms.Comment.class);
        assertThat(((Bms.Comment) cu.getStatements().get(0)).getWord().getText())
          .isEqualTo("* Don't take this as the start of a string");
    }

    private static List<String> sequenceAreasIn(Bms.CompilationUnit cu) {
        List<String> areas = new ArrayList<>();
        new BmsIsoVisitor<Integer>() {
            @Override
            public Bms.Word visitWord(Bms.Word word, Integer p) {
                word.getMarkers().findFirst(SequenceArea.class)
                  .ifPresent(sequenceArea -> areas.add(sequenceArea.getText()));
                return super.visitWord(word, p);
            }
        }.visit(cu, 0);
        return areas;
    }

    private static Bms.CompilationUnit parse(String source) {
        List<SourceFile> parsed = BmsParser.builder().build()
          .parse(new InMemoryExecutionContext(), source)
          .collect(Collectors.toList());
        assertThat(parsed).hasSize(1);
        assertThat(parsed.get(0)).isInstanceOf(Bms.CompilationUnit.class);
        return (Bms.CompilationUnit) parsed.get(0);
    }

    private static String blanks(int n) {
        StringBuilder blanks = new StringBuilder();
        for (int i = 0; i < n; i++) {
            blanks.append(' ');
        }
        return blanks.toString();
    }
}
