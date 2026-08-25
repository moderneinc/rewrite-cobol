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
package org.openrewrite.listload;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.ParseExceptionResult;
import org.openrewrite.Parser;
import org.openrewrite.SourceFile;
import org.openrewrite.listload.tree.ListLoad;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.tree.ParseError;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.listload.Assertions.listLoad;

class ListLoadParserTest implements RewriteTest {

    @Test
    void keepsEveryLineAReportWasPrintedAs() {
        String report = String.join("\n",
          "1                                          A M B L I S T                                 PAGE     1",
          " ",
          "    LISTLOAD OUTPUT=MODLIST,DDN=LOADLIB",
          " ",
          "0                                          ** MODULE SUMMARY **",
          "0",
          "      MEMBER NAME:                  CLMB010",
          "");

        rewriteRun(
          listLoad(report, spec -> spec.afterRecipe(cu -> {
              assertThat(cu.getLines()).hasSize(7);
              assertThat(cu.getLines().get(0).isPageBreak()).isTrue();
              assertThat(cu.getLines().get(4).getText()).isEqualTo(
                "                                          ** MODULE SUMMARY **");
          }))
        );
    }

    @Test
    void keepsARequestDeck() {
        rewriteRun(
          listLoad("  LISTLOAD OUTPUT=MODLIST,DDN=LOADLIB\n  LISTIDR  DDN=LOADLIB\n",
            spec -> spec.afterRecipe(cu -> assertThat(cu.getLines()).hasSize(2)))
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"LOADLIB.amblist", "LOADLIB.AMBLIST", "CLMB010.binder", "CLM.listload"})
    void acceptsAListingByExtension(String name) {
        assertThat(ListLoadParser.builder().build().accept(Paths.get(name))).isTrue();
    }

    /**
     * A control card library holds the AMBLIST request deck beside the sort and IDCAMS cards, and
     * nothing in a member's name says which it is.
     */
    @Test
    void typesAControlCardMemberByWhatItAsksFor(@TempDir Path tempDir) throws IOException {
        Path request = write(tempDir, "LSTCLM01.ctl", "  LISTLOAD OUTPUT=MODLIST,DDN=LOADLIB\n");
        Path sort = write(tempDir, "SRTCLM01.ctl", "  SORT FIELDS=(1,8,CH,A)\n");
        Path parm = write(tempDir, "PRMCLM01.ctl", "  RUNDATE=20260203,RUNMODE=PROD\n");

        ListLoadParser parser = ListLoadParser.builder().build();
        assertThat(parser.accept(request)).isTrue();
        assertThat(parser.accept(sort)).isFalse();
        assertThat(parser.accept(parm)).isFalse();
    }

    /**
     * A member named for a listing but holding something else is reported as such rather than read as
     * a listing of nothing, and under its own type, so a parse-quality report can tell it from a gap
     * in the reader.
     */
    @Test
    void aMemberThatPromisesAListingAndIsNotIsSaidSo() {
        Parser.Input input = new Parser.Input(Paths.get("LOADLIB.amblist"),
          () -> new ByteArrayInputStream("  RUNDATE=20260203\n".getBytes(StandardCharsets.UTF_8)));
        SourceFile parsed = ListLoadParser.builder().build()
          .parseInputs(singletonList(input), null, new InMemoryExecutionContext()).findFirst().orElseThrow();

        assertThat(parsed).isInstanceOf(ParseError.class);
        ParseExceptionResult failure = parsed.getMarkers().findFirst(ParseExceptionResult.class).orElseThrow();
        assertThat(failure.getParserType()).isEqualTo("ListLoadParser");
        assertThat(failure.getExceptionType()).isEqualTo("WrongLanguageException");
        assertThat(failure.getMessage()).contains("LOADLIB.amblist is not a load module listing");
    }

    /**
     * Byte for byte, which is the whole point of holding a report as the lines it was printed as: the
     * line a printer left blank, the ending a shop's transfer left behind, and a last line the file
     * does not end.
     */
    @Test
    void printsBackWhatItWasGiven(@TempDir Path tempDir) throws IOException {
        String report = "1                     A M B L I S T\r\n \r\n      MEMBER NAME:   CLMB010";
        Path listing = write(tempDir, "LOADLIB.amblist", report);

        SourceFile parsed = ListLoadParser.builder().build()
          .parseInputs(singletonList(new Parser.Input(listing, () -> {
              try {
                  return Files.newInputStream(listing);
              } catch (IOException e) {
                  throw new UncheckedIOException(e);
              }
          })), null, new InMemoryExecutionContext()).findFirst().orElseThrow();

        assertThat(parsed).isInstanceOf(ListLoad.CompilationUnit.class);
        assertThat(((ListLoad.CompilationUnit) parsed).getLines()).hasSize(3);
        assertThat(parsed.printAll()).isEqualTo(report);
    }

    private static Path write(Path dir, String name, String content) throws IOException {
        Path path = dir.resolve(name);
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        return path;
    }
}
