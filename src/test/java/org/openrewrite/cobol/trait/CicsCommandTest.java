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
package org.openrewrite.cobol.trait;

import org.junit.jupiter.api.Test;
import org.openrewrite.ExecutionContext;
import org.openrewrite.test.RewriteTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.cobol.Assertions.cobol;
import static org.openrewrite.test.RewriteTest.toRecipe;

class CicsCommandTest implements RewriteTest {

    private List<CicsCommand> parse(String source) {
        List<CicsCommand> found = new ArrayList<>();
        rewriteRun(
          spec -> spec.recipe(toRecipe(() -> new CicsCommand.Matcher().<ExecutionContext>asVisitor((command, ctx) -> {
              found.add(command);
              return command.getTree();
          }))),
          cobol(source)
        );
        return found;
    }

    @Test
    void decomposesAFileRead() {
        List<CicsCommand> commands = parse(
          """
            000000 IDENTIFICATION DIVISION.                                        \s
            000000 PROGRAM-ID. CICSPGM.                                            \s
            000000 PROCEDURE DIVISION.                                             \s
            000000     EXEC CICS READ FILE('ACCTFILE') INTO(WS-REC)                \s
            000000          RIDFLD(WS-KEY) RESP(WS-RESP) END-EXEC.                 \s
            000000     STOP RUN.                                                   \s
            """
        );

        assertThat(commands).hasSize(1);
        CicsCommand read = commands.get(0);
        assertThat(read.getVerb()).isEqualTo("READ");
        assertThat(read.getQualifier()).isNull();
        assertThat(read.getCommand()).isEqualTo("READ");
        assertThat(read.getOption("FILE")).isEqualTo("'ACCTFILE'");
        assertThat(read.operand("FILE")).isEqualTo("ACCTFILE");
        assertThat(read.getOptions()).containsOnlyKeys("FILE", "INTO", "RIDFLD", "RESP");
    }

    @Test
    void readsQualifiersThatChangeMeaning() {
        List<CicsCommand> commands = parse(
          """
            000000 IDENTIFICATION DIVISION.                                        \s
            000000 PROGRAM-ID. CICSPGM.                                            \s
            000000 PROCEDURE DIVISION.                                             \s
            000000     EXEC CICS WRITEQ TS QUEUE('SESSION1') FROM(WS-D) END-EXEC.  \s
            000000     EXEC CICS WRITEQ TD QUEUE('AUDITLOG') FROM(WS-D) END-EXEC.  \s
            000000     EXEC CICS SEND MAP('ACCTM') MAPSET('ACCTMS') END-EXEC.      \s
            000000     STOP RUN.                                                   \s
            """
        );

        assertThat(commands).hasSize(3);
        assertThat(commands.get(0).getCommand()).isEqualTo("WRITEQ TS");
        assertThat(commands.get(0).getResources()).singleElement()
          .satisfies(a -> assertThat(a.getKind()).isEqualTo(CicsResourceAccess.Kind.TS_QUEUE));
        assertThat(commands.get(1).getCommand()).isEqualTo("WRITEQ TD");
        assertThat(commands.get(1).getResources()).singleElement()
          .satisfies(a -> assertThat(a.getKind()).isEqualTo(CicsResourceAccess.Kind.TD_QUEUE));
        assertThat(commands.get(2).getCommand()).isEqualTo("SEND MAP");
    }

    @Test
    void readsOptionsWithoutOperands() {
        List<CicsCommand> commands = parse(
          """
            000000 IDENTIFICATION DIVISION.                                        \s
            000000 PROGRAM-ID. CICSPGM.                                            \s
            000000 PROCEDURE DIVISION.                                             \s
            000000     EXEC CICS READ FILE('ACCT') INTO(WS-R) RIDFLD(WS-K)         \s
            000000          UPDATE END-EXEC.                                       \s
            000000     STOP RUN.                                                   \s
            """
        );

        assertThat(commands).hasSize(1);
        assertThat(commands.get(0).hasOption("UPDATE")).isTrue();
        assertThat(commands.get(0).getOption("UPDATE")).isEmpty();
    }

    @Test
    void readsSubscriptedOperands() {
        List<CicsCommand> commands = parse(
          """
            000000 IDENTIFICATION DIVISION.                                        \s
            000000 PROGRAM-ID. CICSPGM.                                            \s
            000000 PROCEDURE DIVISION.                                             \s
            000000     EXEC CICS LINK PROGRAM(WS-TAB(1)) END-EXEC.                 \s
            000000     STOP RUN.                                                   \s
            """
        );

        assertThat(commands).hasSize(1);
        assertThat(commands.get(0).getOption("PROGRAM")).isEqualTo("WS-TAB(1)");
    }

    @Test
    void classifiesFileAccess() {
        List<CicsCommand> commands = parse(
          """
            000000 IDENTIFICATION DIVISION.                                        \s
            000000 PROGRAM-ID. CICSPGM.                                            \s
            000000 PROCEDURE DIVISION.                                             \s
            000000     EXEC CICS READ FILE('ACCT') INTO(WS-R) UPDATE END-EXEC.     \s
            000000     EXEC CICS REWRITE FILE('ACCT') FROM(WS-R) END-EXEC.         \s
            000000     EXEC CICS DELETE FILE('ACCT') END-EXEC.                     \s
            000000     EXEC CICS LINK PROGRAM('SUBPGM') END-EXEC.                  \s
            000000     STOP RUN.                                                   \s
            """
        );

        assertThat(commands).hasSize(4);
        assertThat(commands.get(0).getResources())
          .singleElement()
          .satisfies(a -> {
              assertThat(a.getKind()).isEqualTo(CicsResourceAccess.Kind.FILE);
              assertThat(a.getName()).isEqualTo("ACCT");
              // READ ... UPDATE reserves the record, so it is not a plain read.
              assertThat(a.getAccess()).isEqualTo(CicsResourceAccess.Access.UPDATE);
              assertThat(a.isDynamic()).isFalse();
          });
        assertThat(commands.get(1).getResources().get(0).getAccess())
          .isEqualTo(CicsResourceAccess.Access.UPDATE);
        assertThat(commands.get(2).getResources().get(0).getAccess())
          .isEqualTo(CicsResourceAccess.Access.DELETE);
        assertThat(commands.get(3).getResources())
          .singleElement()
          .satisfies(a -> {
              assertThat(a.getKind()).isEqualTo(CicsResourceAccess.Kind.PROGRAM);
              assertThat(a.getName()).isEqualTo("SUBPGM");
              assertThat(a.getAccess()).isEqualTo(CicsResourceAccess.Access.LINK);
          });
    }

    @Test
    void marksDynamicResourceNames() {
        List<CicsCommand> commands = parse(
          """
            000000 IDENTIFICATION DIVISION.                                        \s
            000000 PROGRAM-ID. CICSPGM.                                            \s
            000000 PROCEDURE DIVISION.                                             \s
            000000     EXEC CICS LINK PROGRAM(WS-PGM) END-EXEC.                    \s
            000000     STOP RUN.                                                   \s
            """
        );

        assertThat(commands.get(0).getResources())
          .singleElement()
          .satisfies(a -> {
              assertThat(a.getName()).isEqualTo("WS-PGM");
              assertThat(a.isDynamic()).isTrue();
          });
    }

    @Test
    void readsMultiLineCommands() {
        List<CicsCommand> commands = parse(
          """
            000000 IDENTIFICATION DIVISION.                                        \s
            000000 PROGRAM-ID. CICSPGM.                                            \s
            000000 PROCEDURE DIVISION.                                             \s
            000000     EXEC CICS LINK                                              \s
            000000          PROGRAM('SUBPGM1')                                     \s
            000000          COMMAREA(WS-COMM)                                      \s
            000000          LENGTH(100)                                            \s
            000000     END-EXEC.                                                   \s
            """
        );

        assertThat(commands).hasSize(1);
        assertThat(commands.get(0).getVerb()).isEqualTo("LINK");
        assertThat(commands.get(0).operand("PROGRAM")).isEqualTo("SUBPGM1");
        assertThat(commands.get(0).getOption("LENGTH")).isEqualTo("100");
    }

    /**
     * The command is on the word that stands in for the block, so that is what a caller marks. The
     * block's own text prints from the preprocessor statement and would not carry a marker.
     */
    @Test
    void isAnchoredOnTheStandInWord() {
        List<CicsCommand> commands = parse(
          """
            000000 IDENTIFICATION DIVISION.                                        \s
            000000 PROGRAM-ID. CICSPGM.                                            \s
            000000 PROCEDURE DIVISION.                                             \s
            000000 MAIN-PARA.                                                      \s
            000000     EXEC CICS LINK PROGRAM('SUBPGM') END-EXEC.                  \s
            """
        );

        assertThat(commands).hasSize(1);
        assertThat(commands.get(0).getTree().getWord()).isEmpty();
        assertThat(Program.nameOf(commands.get(0).getCursor())).isEqualTo("CICSPGM");
        assertThat(Procedure.nameOf(commands.get(0).getCursor())).isEqualTo("MAIN-PARA");
    }
}
