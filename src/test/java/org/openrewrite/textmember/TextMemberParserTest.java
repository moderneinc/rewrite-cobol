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
package org.openrewrite.textmember;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Parser;
import org.openrewrite.SourceFile;
import org.openrewrite.db2.bind.BindParser;
import org.openrewrite.jcl.JclParser;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.textmember.tree.TextMember;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.textmember.Assertions.cSource;
import static org.openrewrite.textmember.Assertions.clist;
import static org.openrewrite.textmember.Assertions.document;
import static org.openrewrite.textmember.Assertions.pliSource;
import static org.openrewrite.textmember.Assertions.rexx;

class TextMemberParserTest implements RewriteTest {

    @ParameterizedTest
    @CsvSource({
      "CLMCMPX.rexx,REXX", "OPERCMD.REX,REXX", "build.rx,REXX",
      "CLMSUB.clist,CLIST", "CLMSUB.CLST,CLIST",
      "CLMJ010.docjob,DOCUMENT", "CLMB010.docpgm,DOCUMENT", "CLMMAST.docfich,DOCUMENT",
      "CLAIMS.docappl,DOCUMENT", "NIGHTLY.docoper,DOCUMENT",
      "base64c.c,C", "zowetypes.h,C",
      "BNKSTMT.pli,PLI", "MACSAMP.PL1,PLI"})
    void typesAMemberByItsExtension(String name, TextMember.Kind kind) {
        assertThat(readerFor(kind).accept(Paths.get(name))).isTrue();
    }

    /**
     * The rule TSO/E itself uses, which is the only thing that types an exec kept the way a PDS member
     * arrives: {@code SYSEXEC} runs a member whose first line is a comment holding the word REXX and
     * refuses one whose is not.
     */
    @Test
    void typesAnExtensionlessMemberByTheCommentTsoReads(@TempDir Path tempDir) throws IOException {
        Path exec = write(tempDir, "CLMCMPX", "/* REXX */\nPARSE UPPER ARG LIST\n");
        Path apostrophe = write(tempDir, "OPERCMD", "/* rexx - issue an operator command */\nEXIT 0\n");
        Path notAnExec = write(tempDir, "CLMNOTES", "/* THE CLAIMS LIBRARIES */\nNOTHING RUNS THIS\n");
        Path clist = write(tempDir, "CLMSUB", "PROC 1 JOB NOASK\nCONTROL NOFLUSH\n");

        RexxParser parser = RexxParser.builder().build();
        assertThat(parser.accept(exec)).isTrue();
        assertThat(parser.accept(apostrophe)).isTrue();
        assertThat(parser.accept(notAnExec)).isFalse();
        // A CLIST says nothing about itself, which is why one is typed by its extension and no more.
        assertThat(parser.accept(clist)).isFalse();
        assertThat(ClistParser.builder().build().accept(clist)).isFalse();
    }

    /**
     * Nothing is claimed twice. A library holds an exec beside the jobs and the bind decks, and each of
     * the three readers that take an extensionless member answers for its own first line.
     */
    @Test
    void leavesAnExtensionlessMemberAnotherReaderClaims(@TempDir Path tempDir) throws IOException {
        Path job = write(tempDir, "CLMJ010", "//CLMJ010  JOB (CLM),'CLAIM EXTRACT'\n//EXTRACT EXEC PGM=CLMB010\n");
        Path bind = write(tempDir, "BINDPLAN", "  BIND PLAN(CLMPLAN) PKLIST(CLMPKG.*)\n");
        Path exec = write(tempDir, "CLMPICK", "/* REXX */\nADDRESS TSO\n");

        RexxParser rexx = RexxParser.builder().build();
        assertThat(rexx.accept(job)).isFalse();
        assertThat(rexx.accept(bind)).isFalse();
        assertThat(rexx.accept(exec)).isTrue();
        assertThat(JclParser.builder().build().accept(exec)).isFalse();
        assertThat(BindParser.builder().build().accept(exec)).isFalse();
    }

    @Test
    void keepsEveryLineOfAScript() {
        rewriteRun(
          clist(
            """
              PROC 1 JOB NOASK
              /*  CLMSUB - SUBMIT ONE CLAIMS JOB  */
              SUBMIT '&CLMHLQ..JCL(&JOB)'
              """,
            spec -> spec.afterRecipe(cu -> {
                assertThat(cu.getKind()).isEqualTo(TextMember.Kind.CLIST);
                assertThat(cu.getLines()).hasSize(3);
                assertThat(cu.getLines().get(2).getText()).isEqualTo("SUBMIT '&CLMHLQ..JCL(&JOB)'");
            }))
        );
    }

    @Test
    void keepsEveryLineOfARunBook() {
        rewriteRun(
          document(
            """
              DOCJOB   CLMJ010                                CASCADE MUTUAL - CLAIMS
              ========================================================================
              JOB          CLMJ010                    LIBRARY  CLM.PROD.JCL
              """,
            spec -> spec.afterRecipe(cu -> assertThat(cu.getKind()).isEqualTo(TextMember.Kind.DOCUMENT))
          )
        );
    }

    @Test
    void keepsEveryLineOfARexxExec() {
        rewriteRun(
          rexx(
            """
              /* REXX */
              PARSE UPPER ARG JOB .
              """)
        );
    }

    /**
     * The languages nothing here reads are held the same way, which is what keeps a C source from
     * reaching a repository as a text file.
     */
    @Test
    void keepsTheLanguagesNothingReads() {
        rewriteRun(
          cSource(
            """
              int main(int argc, char **argv) {
                return 0;
              }
              """)
        );
        rewriteRun(
          pliSource(
            """
              BNKSTMT: PROCEDURE OPTIONS(MAIN);
                 DCL COUNT FIXED BIN(31);
              END BNKSTMT;
              """)
        );
    }

    /**
     * Byte for byte: the ending a shop's transfer left behind, a line left blank, and a last line the
     * file does not end.
     */
    @Test
    void printsBackWhatItWasGiven(@TempDir Path tempDir) throws IOException {
        String source = "/* REXX */\r\n\r\n\"SUBMIT '\"HLQ\".JCL(\"JOB\")'\"";
        Path exec = write(tempDir, "CLMPICK.rexx", source);

        SourceFile parsed = RexxParser.builder().build()
          .parseInputs(singletonList(new Parser.Input(exec, () -> {
              try {
                  return Files.newInputStream(exec);
              } catch (IOException e) {
                  throw new UncheckedIOException(e);
              }
          })), null, new InMemoryExecutionContext()).findFirst().orElseThrow();

        assertThat(parsed).isInstanceOf(TextMember.CompilationUnit.class);
        assertThat(((TextMember.CompilationUnit) parsed).getLines()).hasSize(3);
        assertThat(parsed.printAll()).isEqualTo(source);
    }

    private static Parser readerFor(TextMember.Kind kind) {
        switch (kind) {
            case REXX:
                return RexxParser.builder().build();
            case CLIST:
                return ClistParser.builder().build();
            case DOCUMENT:
                return DocumentParser.builder().build();
            case C:
                return CParser.builder().build();
            default:
                return PliParser.builder().build();
        }
    }

    private static Path write(Path dir, String name, String content) throws IOException {
        Path path = dir.resolve(name);
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        return path;
    }
}
