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
package org.openrewrite.linkedit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.ParseExceptionResult;
import org.openrewrite.Parser;
import org.openrewrite.SourceFile;
import org.openrewrite.linkedit.tree.LinkEdit;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.tree.ParseError;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.linkedit.Assertions.linkEdit;

class LinkEditParserTest implements RewriteTest {

    @Test
    void deckForOneModule() {
        rewriteRun(
          linkEdit(
            """
              *  CLMC020 - CLAIM INQUIRY.  CLMU020 IS CALLED STATICALLY.
                INCLUDE SYSLIB(DFHECI)
                INCLUDE OBJLIB(CLMU020)
                ENTRY CLMC020
                NAME CLMC020(R)
              """,
            spec -> spec.afterRecipe(cu -> {
                assertThat(cu.getStatements()).hasSize(4);
                LinkEdit.ControlStatement include = (LinkEdit.ControlStatement) cu.getStatements().get(1);
                assertThat(include.getOperator().getText()).isEqualTo("INCLUDE");
                assertThat(include.getParameters().get(0).getKeyword().getText()).isEqualTo("OBJLIB");
                assertThat(include.getParameters().get(0).getValueText()).isEqualTo("(CLMU020)");
                LinkEdit.ControlStatement name = (LinkEdit.ControlStatement) cu.getStatements().get(3);
                assertThat(name.getParameters().get(0).getKeyword().getText()).isEqualTo("CLMC020");
                assertThat(name.getParameters().get(0).getValueText()).isEqualTo("(R)");
            })
          )
        );
    }

    /**
     * {@code ENTRY} and {@code ALIAS} write a name with no parentheses at all, so an operand with no
     * value has to survive as an operand rather than be swallowed by the operator.
     */
    @Test
    void namesWrittenWithoutParentheses() {
        rewriteRun(
          linkEdit(
            """
                ENTRY CLMU020
                ALIAS CLMRESV,CLMOLDRS
                NAME CLMU020(R)
              """,
            spec -> spec.afterRecipe(cu -> {
                LinkEdit.ControlStatement entry = (LinkEdit.ControlStatement) cu.getStatements().get(0);
                assertThat(entry.getParameters()).singleElement().satisfies(operand -> {
                    assertThat(operand.getKeyword().getText()).isEqualTo("CLMU020");
                    assertThat(operand.getValue()).isEmpty();
                });
                LinkEdit.ControlStatement alias = (LinkEdit.ControlStatement) cu.getStatements().get(1);
                assertThat(alias.getParameters()).extracting(o -> o.getKeyword().getText())
                  .containsExactly("CLMRESV", "CLMOLDRS");
            })
          )
        );
    }

    /**
     * The binder separates operands with a comma as readily as with a blank, and the commas inside a
     * member list belong to the list.
     */
    @Test
    void operandsSeparatedByCommas() {
        rewriteRun(
          linkEdit(
            """
                INCLUDE OBJLIB(CLMU010,CLMU020),SYSLIB(DFHECI)
                MODE AMODE(31),RMODE(ANY)
                NAME CLMC030(R)
              """,
            spec -> spec.afterRecipe(cu -> {
                LinkEdit.ControlStatement include = (LinkEdit.ControlStatement) cu.getStatements().get(0);
                assertThat(include.getParameters()).extracting(o -> o.getKeyword().getText())
                  .containsExactly("OBJLIB", "SYSLIB");
                assertThat(include.getParameters().get(0).getValueText()).isEqualTo("(CLMU010,CLMU020)");
                LinkEdit.ControlStatement mode = (LinkEdit.ControlStatement) cu.getStatements().get(1);
                assertThat(mode.getParameters()).extracting(o -> o.getKeyword().getText())
                  .containsExactly("AMODE", "RMODE");
            })
          )
        );
    }

    /**
     * A member list continued into the next card is one operand, and the continuation is column 72 of
     * the card above rather than anything on the card itself.
     * <p>
     * Parsed from the text rather than through a source spec, since a spec trims the indentation off
     * a deck and the columns are the whole point.
     */
    @Test
    void memberListWrittenOverTwoCards() {
        String source =
          data("  INCLUDE OBJLIB(CLMU010,") + "X\n" +
          "                 CLMU030)\n" +
          "  NAME CLMI050(R)\n";
        LinkEdit.CompilationUnit cu = LinkEditParser.parse(Paths.get("CLMI050.lnk"), source);

        assertThat(cu.getStatements()).hasSize(2);
        LinkEdit.ControlStatement include = (LinkEdit.ControlStatement) cu.getStatements().get(0);
        assertThat(include.getParameters()).singleElement().satisfies(operand ->
          assertThat(operand.getValueText()).isEqualTo("(CLMU010, CLMU030)"));
        assertThat(cu.printAll()).isEqualTo(source);
    }

    /**
     * Columns 72-80 are the binder's, not the operand field's: what a shop numbers its cards with
     * prints back, and reads as nothing.
     */
    @Test
    void sequenceNumbersAreNotOperands() {
        String source =
          data("  INCLUDE OBJLIB(CLMU010)") + " 00000100\n" +
          data("  NAME CLMB020(R)") + " 00000200\n";
        LinkEdit.CompilationUnit cu = LinkEditParser.parse(Paths.get("CLMB020.lnk"), source);

        assertThat(cu.getStatements()).hasSize(2);
        LinkEdit.ControlStatement include = (LinkEdit.ControlStatement) cu.getStatements().get(0);
        assertThat(include.getParameters()).singleElement().satisfies(operand ->
          assertThat(operand.getKeyword().getText()).isEqualTo("OBJLIB"));
        assertThat(cu.printAll()).isEqualTo(source);
    }

    @Test
    void quotedText() {
        rewriteRun(
          linkEdit(
            """
                INCLUDE OBJLIB(CLMB010)
                IDENTIFY CLMB010('BUILT 2026-08-25 BY CLMCMPB')
                NAME CLMB010(R)
              """,
            spec -> spec.afterRecipe(cu -> {
                LinkEdit.ControlStatement identify = (LinkEdit.ControlStatement) cu.getStatements().get(1);
                assertThat(identify.getParameters()).singleElement().satisfies(operand -> {
                    assertThat(operand.getKeyword().getText()).isEqualTo("CLMB010");
                    assertThat(operand.getValueText()).isEqualTo("('BUILT 2026-08-25 BY CLMCMPB')");
                });
            })
          )
        );
    }

    @Test
    void optionsSetBeforeTheModuleIsNamed() {
        rewriteRun(
          linkEdit(
            """
               SETOPT  PARM(AMODE=31)
               SETOPT  PARM(RMODE=ANY)
               INCLUDE SYSLIB(GVBDAYS)
               ENTRY   GVBDAYS
               NAME    GVBDAYS(R)
              """,
            spec -> spec.afterRecipe(cu -> {
                assertThat(cu.getStatements()).hasSize(5);
                LinkEdit.ControlStatement setopt = (LinkEdit.ControlStatement) cu.getStatements().get(0);
                assertThat(setopt.getParameters()).singleElement().satisfies(operand -> {
                    assertThat(operand.getKeyword().getText()).isEqualTo("PARM");
                    assertThat(operand.getValueText()).isEqualTo("(AMODE=31)");
                });
            })
          )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"CLMB010.lnk", "CLMB010.LNK", "linkeditjcl/ABNDPROC.lked"})
    void acceptsADeckByExtension(String name) {
        assertThat(LinkEditParser.builder().build().accept(Paths.get(name))).isTrue();
    }

    /**
     * A {@code LINKLIB} member has no extension to be known by, so the parser reads far enough into
     * one to see whether it builds anything.
     */
    @Test
    void acceptsAnExtensionlessMemberThatLinks(@TempDir Path tempDir) throws IOException {
        Path deck = write(tempDir, "CLMB010", "  ENTRY CLMB010\n  NAME CLMB010(R)\n");
        Path sort = write(tempDir, "SRTCLM01", "  SORT FIELDS=(1,8,CH,A)\n  INCLUDE COND=(57,1,CH,EQ,C'O')\n");
        Path notice = write(tempDir, "LICENSE", "Apache License, Version 2.0\n");

        LinkEditParser parser = LinkEditParser.builder().build();
        assertThat(parser.accept(deck)).isTrue();
        assertThat(parser.accept(sort)).isFalse();
        assertThat(parser.accept(notice)).isFalse();
    }

    /**
     * The grammar reads any deck of operators and operands, so a member named for the language but
     * building nothing is refused by name rather than read as an empty deck — and under its own type,
     * so a parse-quality report can tell it from a grammar gap.
     */
    @Test
    void aMemberThatLinksNothingIsSaidSo() {
        Parser.Input input = new Parser.Input(Paths.get("LNKOPTS.lnk"),
          () -> new ByteArrayInputStream(" SETOPT  PARM(AMODE=31)\n SETOPT  PARM(RMODE=24)\n".getBytes(StandardCharsets.UTF_8)));
        SourceFile parsed = LinkEditParser.builder().build()
          .parseInputs(singletonList(input), null, new InMemoryExecutionContext()).findFirst().orElseThrow();

        assertThat(parsed).isInstanceOf(ParseError.class);
        ParseExceptionResult failure = parsed.getMarkers().findFirst(ParseExceptionResult.class).orElseThrow();
        assertThat(failure.getParserType()).isEqualTo("LinkEditParser");
        assertThat(failure.getExceptionType()).isEqualTo("WrongLanguageException");
        assertThat(failure.getMessage())
          .contains("LNKOPTS.lnk is not a link-edit deck: it has no INCLUDE, ENTRY, ALIAS or NAME statement.");
    }

    private static Path write(Path directory, String name, String content) throws IOException {
        Path path = directory.resolve(name);
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        return path;
    }

    private static String data(String text) {
        StringBuilder card = new StringBuilder(text);
        while (card.length() < LinkEditLineReader.DATA_COLUMNS) {
            card.append(' ');
        }
        return card.toString();
    }
}
