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
package org.openrewrite.db2.bind;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.ParseExceptionResult;
import org.openrewrite.Parser;
import org.openrewrite.SourceFile;
import org.openrewrite.db2.bind.tree.Bind;
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
import static org.openrewrite.db2.bind.Assertions.bind;

class BindParserTest implements RewriteTest {

    @Test
    void packageBind() {
        rewriteRun(
          bind(
            """
              DSN SYSTEM(DB2P)
              BIND PACKAGE(CLMPKG) OWNER(CLMPROD) QUALIFIER(CLM) -
                   MEMBER(CLMD010) LIBRARY('CLM.PROD.DBRMLIB') -
                   ACTION(REPLACE) VALIDATE(BIND) ISOLATION(CS)
              END
              """,
            spec -> spec.afterRecipe(cu -> {
                assertThat(cu.getStatements()).hasSize(3);
                Bind.Command command = (Bind.Command) cu.getStatements().get(1);
                assertThat(command.getVerb().getText()).isEqualTo("BIND");
                assertThat(command.getParameter("MEMBER").getValueText()).isEqualTo("(CLMD010)");
                // The lexer breaks the quoted data set name out on its own, so the value is three
                // words that have to read back as one.
                assertThat(command.getParameter("LIBRARY").getValueText()).isEqualTo("('CLM.PROD.DBRMLIB')");
            })
          )
        );
    }

    @Test
    void keywordSeparatedFromItsValueByABlank() {
        rewriteRun(
          bind(
            """
              DSN     SYSTEM    (DB2D)
              BIND    PACKAGE   (STOCKTRD)   -
                      MEMBER    (ACCT01)     -
                      ACTION (REPLACE)  RETAIN
              END
              """,
            spec -> spec.afterRecipe(cu -> {
                assertThat(((Bind.Command) cu.getStatements().get(0)).getParameter("SYSTEM").getValueText())
                  .isEqualTo("(DB2D)");
                Bind.Command command = (Bind.Command) cu.getStatements().get(1);
                assertThat(command.getParameter("PACKAGE").getValueText()).isEqualTo("(STOCKTRD)");
                // RETAIN takes no value, so it has to survive as an operand of its own rather than
                // being swallowed by the one in front of it.
                assertThat(command.getParameter("RETAIN").getValue()).isEmpty();
            })
          )
        );
    }

    @Test
    void verbOnALineOfItsOwn() {
        rewriteRun(
          bind(
            """
              BIND                    -
                   PLAN(&PLAN.)       -
                   PKLIST(&COLLID..*) -
                   ACTION(REPLACE)
              """,
            spec -> spec.afterRecipe(cu -> {
                assertThat(cu.getStatements()).hasSize(1);
                Bind.Command command = (Bind.Command) cu.getStatements().get(0);
                assertThat(command.getParameter("PLAN").getValueText()).isEqualTo("(&PLAN.)");
                assertThat(command.getParameter("PKLIST").getValueText()).isEqualTo("(&COLLID..*)");
            })
          )
        );
    }

    @Test
    void listContinuedInsideItsOwnParentheses() {
        rewriteRun(
          bind(
            """
              BIND PLAN(BANK) -
               PKLIST( -
               NULLID.*,PHBANK.* )
              """,
            spec -> spec.afterRecipe(cu -> {
                Bind.Command command = (Bind.Command) cu.getStatements().get(0);
                assertThat(command.getParameter("PKLIST").getValueText()).isEqualTo("( NULLID.*,PHBANK.* )");
            })
          )
        );
    }

    @Test
    void aBlankLineEndsACommand() {
        rewriteRun(
          bind(
            """
              DSN SYSTEM(DB2D)

              BIND PACKAGE(A) MEMBER(P1)

              BIND PACKAGE(A) MEMBER(P2)
              END
              """,
            spec -> spec.afterRecipe(cu -> assertThat(cu.getStatements()).hasSize(4))
          )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"BINDPKG1.bnd", "BINDPKG1.BND", "cardlib/BINDPKG1.Bnd"})
    void acceptsADeckByExtension(String name) {
        assertThat(BindParser.builder().build().accept(Paths.get(name))).isTrue();
    }

    /**
     * A {@code CARDLIB} member has no extension to be known by, so the parser reads far enough into
     * one to see whether it binds anything.
     */
    @Test
    void acceptsAnExtensionlessMemberThatBinds(@TempDir Path tempDir) throws IOException {
        Path deck = write(tempDir, "BINDPKG1", "DSN SYSTEM(DB2P)\nBIND PACKAGE(CLMPKG) MEMBER(CLMD010)\nEND\n");
        Path queries = write(tempDir, "CATPKDEP", "SELECT * FROM SYSIBM.SYSPACKDEP;\n");
        Path notice = write(tempDir, "LICENSE", "Apache License, Version 2.0\n");

        BindParser parser = BindParser.builder().build();
        assertThat(parser.accept(deck)).isTrue();
        assertThat(parser.accept(queries)).isFalse();
        assertThat(parser.accept(notice)).isFalse();
    }

    /**
     * The grammar reads any command deck, so a member named for the language but binding nothing is
     * refused by name rather than read as an empty deck — and under its own type, so a parse-quality
     * report can tell it from a grammar gap.
     */
    @Test
    void aMemberThatBindsNothingIsSaidSo() {
        Parser.Input input = new Parser.Input(Paths.get("RUNTEP2.bnd"),
          () -> new ByteArrayInputStream("DSN SYSTEM(DB2P)\nRUN PROGRAM(DSNTEP2) PLAN(DSNTEP2)\nEND\n".getBytes(StandardCharsets.UTF_8)));
        SourceFile parsed = BindParser.builder().build()
          .parseInputs(singletonList(input), null, new InMemoryExecutionContext()).findFirst().orElseThrow();

        assertThat(parsed).isInstanceOf(ParseError.class);
        ParseExceptionResult failure = parsed.getMarkers().findFirst(ParseExceptionResult.class).orElseThrow();
        assertThat(failure.getParserType()).isEqualTo("BindParser");
        assertThat(failure.getExceptionType()).isEqualTo("WrongLanguageException");
        assertThat(failure.getMessage()).contains("RUNTEP2.bnd is not a bind deck: it has no BIND or REBIND subcommand.");
    }

    private static Path write(Path directory, String name, String content) throws IOException {
        Path path = directory.resolve(name);
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        return path;
    }
}
