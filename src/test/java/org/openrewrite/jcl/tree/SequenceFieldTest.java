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
package org.openrewrite.jcl.tree;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.SourceFile;
import org.openrewrite.jcl.JclParser;
import org.openrewrite.jcl.JclVisitor;
import org.openrewrite.jcl.marker.CommentArea;
import org.openrewrite.marker.Marker;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Columns 73-80, which JCL leaves to whoever is numbering the cards.
 * <p>
 * These parse directly rather than through {@code ParserAssertions.jcl}, because the whole point is
 * where a character sits and the test framework trims a source block's common indentation.
 */
class SequenceFieldTest {

    /**
     * The field is read whole. It used to be one word of it: the lexer breaks on a quote, so
     * {@code WAIT=30,F=WRAP')} became three tokens and only the first was kept — silently, because
     * the field is a marker and what it does not hold is not printed either.
     */
    @Test
    void aSequenceFieldHoldingAQuote() {
        String source =
          "//TEST     JOB 'X'\n" +
          "//SYSPRINT DD SYSOUT=*" + blanks(50) + "WAIT=30,F=WRAP')\n";

        assertThat(parse(source).printAll()).isEqualTo(source);
        assertThat(commentAreasIn(source)).containsExactly("WAIT=30,F=WRAP')");
    }

    @Test
    void anOrdinarySequenceNumber() {
        String source =
          "//TEST     JOB 'X'\n" +
          "//SYSPRINT DD SYSOUT=*" + blanks(50) + "00010016\n";

        assertThat(parse(source).printAll()).isEqualTo(source);
        assertThat(commentAreasIn(source)).containsExactly("00010016");
    }

    /**
     * A line reaching column 73 with no blank in front of it has no sequence field — it is a line too
     * long to be JCL. Bank-of-Z writes its jobs as Jinja templates, whose placeholders are longer
     * than what replaces them, so the lines only come inside 80 columns once rendered.
     * <p>
     * Splitting one anyway cuts a word in half, and the halves cannot be put back: the parser walks
     * the original source, where nothing separates them. It used to lose the rest of the file.
     */
    @Test
    void aLineTooLongToHaveASequenceField() {
        String source =
          "//TEST JOB 'X'\n" +
          "//  PARM=('IMSPLEX={{ ims.dfs_imsplex }},ROUTE={{ ims.dfs_ssid }},WAIT=30')\n" +
          "//SYSPRINT DD SYSOUT=*\n";

        assertThat(parse(source).printAll()).isEqualTo(source);
        assertThat(commentAreasIn(source)).isEmpty();
    }

    /**
     * The statement after a long line is still its own statement. Losing it was how this showed up
     * over the corpus: a job reported one step fewer than it has.
     */
    @Test
    void aLongLineDoesNotSwallowWhatFollows() {
        String source =
          "//TEST JOB 'X'\n" +
          "//  PARM=('IMSPLEX={{ ims.dfs_imsplex }},ROUTE={{ ims.dfs_ssid }},WAIT=30')\n" +
          "//LDTTYPE  EXEC PGM=IEFBR14,COND=(0,NE)\n" +
          "//RUNTEP2  EXEC PGM=IKJEFT01,DYNAMNBR=20\n";

        Jcl.CompilationUnit cu = parse(source);
        assertThat(cu.printAll()).isEqualTo(source);
        assertThat(cu.getStatements())
          .filteredOn(Jcl.JobControlStatement.class::isInstance)
          .extracting(s -> ((Jcl.JobControlStatement) s).getSimpleName())
          .contains("LDTTYPE", "RUNTEP2");
    }

    private static List<String> commentAreasIn(String source) {
        List<String> comments = new ArrayList<>();
        new JclVisitor<Integer>() {
            @Override
            public Jcl visitWord(Jcl.Word word, Integer p) {
                for (Marker marker : word.getMarkers().getMarkers()) {
                    if (marker instanceof CommentArea) {
                        comments.add(((CommentArea) marker).getComment());
                    }
                }
                return super.visitWord(word, p);
            }
        }.visit(parse(source), 0);
        return comments;
    }

    private static String blanks(int count) {
        StringBuilder blanks = new StringBuilder();
        for (int i = 0; i < count; i++) {
            blanks.append(' ');
        }
        return blanks.toString();
    }

    private static Jcl.CompilationUnit parse(String source) {
        List<SourceFile> parsed = JclParser.builder().build()
          .parse(new InMemoryExecutionContext(t -> {
              throw new IllegalStateException(t);
          }), source)
          .collect(Collectors.toList());
        assertThat(parsed).hasSize(1);
        assertThat(parsed.get(0)).isInstanceOf(Jcl.CompilationUnit.class);
        return (Jcl.CompilationUnit) parsed.get(0);
    }
}
