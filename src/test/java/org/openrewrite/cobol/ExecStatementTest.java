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
package org.openrewrite.cobol;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.cobol.marker.ElidedExec;
import org.openrewrite.cobol.tree.Cobol;
import org.openrewrite.cobol.tree.CobolPreprocessor;
import org.openrewrite.cobol.tree.Statement;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.cobol.Assertions.cobol;

/**
 * Preprocessing removes an EXEC block from the text the COBOL grammar is handed, and the preprocessor rule
 * {@code execCicsStatement : EXEC CICS charData END_EXEC DOT?} takes the period that ended the sentence with it. A
 * tagged line and a period stand in for them in the parser input so that the EXEC is a statement in its own right and
 * the sentence is still terminated. Both stand-ins are marked {@link ElidedExec}: the source text prints from the EXEC
 * statement attached to them, never from the words themselves.
 */
class ExecStatementTest extends CobolTest {

    @Test
    void execCicsAsTheOnlyStatementInAParagraph() {
        rewriteRun(
          cobol(
            """
              000000 IDENTIFICATION DIVISION.                                        \s
              000000 PROGRAM-ID. PARAEXEC.                                           \s
              000000 PROCEDURE DIVISION.                                             \s
              000000 MAIN-PARA.                                                      \s
              000000     EXEC CICS RETURN END-EXEC.                                  \s
              000000 A-EXIT.                                                         \s
              000000     EXIT.                                                       \s
              """,
            spec -> spec.afterRecipe(cu -> {
                assertThat(paragraphNames(cu)).containsExactly("MAIN-PARA", "A-EXIT");
                assertThat(execs(cu)).containsExactly("EXEC CICS RETURN END-EXEC .");
                assertThat(statementTypes(cu)).contains(Cobol.ExecCicsStatement.class);
            })
          )
        );
    }

    /**
     * The elided period is the only terminator the sentence has. Without it the IF ran on and swallowed the next
     * paragraph's name as a statement.
     */
    @Test
    void execCicsAsTheOnlyBodyOfAnIf() {
        rewriteRun(
          cobol(
            """
              000000 IDENTIFICATION DIVISION.                                        \s
              000000 PROGRAM-ID. IFEXEC.                                             \s
              000000 PROCEDURE DIVISION.                                             \s
              000000 MAIN-PARA.                                                      \s
              000000     IF WS-FLAG = 'R'                                            \s
              000000        EXEC CICS SEND TEXT FROM(WS-MSG) END-EXEC.               \s
              000000 A-EXIT.                                                         \s
              000000     EXIT.                                                       \s
              """,
            spec -> spec.afterRecipe(cu -> {
                assertThat(paragraphNames(cu)).containsExactly("MAIN-PARA", "A-EXIT");
                assertThat(execs(cu)).containsExactly("EXEC CICS SEND TEXT FROM ( WS-MSG ) END-EXEC .");
                assertThat(standIns(cu)).containsExactly("", ".");
            })
          )
        );
    }

    /**
     * An EXEC that ends no sentence has no period to stand back up, only the block itself.
     */
    @Test
    void execCicsAsTheOnlyBodyOfAnIfWithEndIf() {
        rewriteRun(
          cobol(
            """
              000000 IDENTIFICATION DIVISION.                                        \s
              000000 PROGRAM-ID. IFEXEC2.                                            \s
              000000 PROCEDURE DIVISION.                                             \s
              000000 MAIN-PARA.                                                      \s
              000000     IF WS-FLAG = 'R'                                            \s
              000000        EXEC CICS SEND TEXT FROM(WS-MSG) END-EXEC                \s
              000000     END-IF.                                                     \s
              000000     GOBACK.                                                     \s
              """,
            spec -> spec.afterRecipe(cu -> {
                assertThat(paragraphNames(cu)).containsExactly("MAIN-PARA");
                assertThat(standIns(cu)).containsExactly("");
                assertThat(statementTypes(cu)).contains(Cobol.ExecCicsStatement.class);
            })
          )
        );
    }

    /**
     * Each EXEC gets its own statement, rather than the second piling onto the word the first is attached to.
     */
    @Test
    void consecutiveExecs() {
        rewriteRun(
          cobol(
            """
              000000 IDENTIFICATION DIVISION.                                        \s
              000000 PROGRAM-ID. TWOEXEC.                                            \s
              000000 PROCEDURE DIVISION.                                             \s
              000000 MAIN-PARA.                                                      \s
              000000     EXEC CICS ASKTIME ABSTIME(WS-ABSTIME) END-EXEC.             \s
              000000     EXEC CICS RETURN END-EXEC.                                  \s
              000000 A-EXIT.                                                         \s
              000000     EXIT.                                                       \s
              """,
            spec -> spec.afterRecipe(cu -> {
                assertThat(paragraphNames(cu)).containsExactly("MAIN-PARA", "A-EXIT");
                assertThat(execsPerStatement(cu)).containsExactly(
                  List.of("EXEC CICS ASKTIME ABSTIME ( WS-ABSTIME ) END-EXEC ."),
                  List.of("EXEC CICS RETURN END-EXEC ."));
            })
          )
        );
    }

    @Test
    void execSql() {
        rewriteRun(
          cobol(
            """
              000000 IDENTIFICATION DIVISION.                                        \s
              000000 PROGRAM-ID. SQLEXEC.                                            \s
              000000 PROCEDURE DIVISION.                                             \s
              000000 MAIN-PARA.                                                      \s
              000000     EXEC SQL SELECT NAME INTO :WS-NAME FROM CUST END-EXEC.      \s
              000000     GOBACK.                                                     \s
              """,
            spec -> spec.afterRecipe(cu ->
              assertThat(statementTypes(cu)).contains(Cobol.ExecSqlStatement.class))
          )
        );
    }

    @Test
    void execDli() {
        rewriteRun(
          cobol(
            """
              000000 IDENTIFICATION DIVISION.                                        \s
              000000 PROGRAM-ID. DLIEXEC.                                            \s
              000000 PROCEDURE DIVISION.                                             \s
              000000 MAIN-PARA.                                                      \s
              000000     EXEC DLI GU USING PCB(1) SEGMENT(CUSTOMER) END-EXEC.        \s
              000000     GOBACK.                                                     \s
              """,
            spec -> spec.afterRecipe(cu -> {
                assertThat(statementTypes(cu)).contains(Cobol.ExecDliStatement.class);
                assertThat(execs(cu)).containsExactly(
                  "EXEC DLI GU USING PCB ( 1 ) SEGMENT ( CUSTOMER ) END-EXEC .");
            })
          )
        );
    }

    /**
     * Outside the procedure division an EXEC is not a statement and a period is not a sentence terminator, so the
     * block stays elided or the data description entries around it stop parsing.
     */
    @Test
    void execSqlInWorkingStorageStaysElided() {
        rewriteRun(
          cobol(
            """
              000000 IDENTIFICATION DIVISION.                                        \s
              000000 PROGRAM-ID. DECLTBL.                                            \s
              000000 DATA DIVISION.                                                  \s
              000000 WORKING-STORAGE SECTION.                                        \s
              000000     EXEC SQL DECLARE CUSTOMER TABLE (NAME CHAR(4)) END-EXEC.    \s
              000000 77 X PIC 99.                                                    \s
              000000 PROCEDURE DIVISION.                                             \s
              000000     GOBACK.                                                     \s
              """,
            spec -> spec.afterRecipe(cu -> {
                assertThat(standIns(cu)).isEmpty();
                assertThat(statementTypes(cu)).doesNotContain(Cobol.ExecSqlStatement.class);
            })
          )
        );
    }

    private static List<String> paragraphNames(Cobol.CompilationUnit cu) {
        List<String> names = new ArrayList<>();
        new CobolIsoVisitor<Integer>() {
            @Override
            public Cobol.Paragraph visitParagraph(Cobol.Paragraph paragraph, Integer p) {
                names.add(((Cobol.Word) paragraph.getParagraphName()).getWord());
                return super.visitParagraph(paragraph, p);
            }
        }.visit(cu, 0);
        return names;
    }

    /**
     * The text of every word that stands in for an elided EXEC: the block itself has none, the period it took with it
     * keeps its own.
     */
    private static List<String> standIns(Cobol.CompilationUnit cu) {
        List<String> words = new ArrayList<>();
        new CobolIsoVisitor<Integer>() {
            @Override
            public Cobol.Word visitWord(Cobol.Word word, Integer p) {
                if (word.getMarkers().findFirst(ElidedExec.class).isPresent()) {
                    words.add(word.getWord());
                }
                return super.visitWord(word, p);
            }
        }.visit(cu, 0);
        return words;
    }

    private static List<Class<?>> statementTypes(Cobol.CompilationUnit cu) {
        List<Class<?>> types = new ArrayList<>();
        new CobolIsoVisitor<Integer>() {
            @Override
            public @Nullable Cobol visit(@Nullable Tree tree, Integer p) {
                if (tree instanceof Statement) {
                    types.add(tree.getClass());
                }
                return super.visit(tree, p);
            }
        }.visit(cu, 0);
        return types;
    }

    private static List<String> execs(Cobol.CompilationUnit cu) {
        List<String> execs = new ArrayList<>();
        execsPerStatement(cu).forEach(execs::addAll);
        return execs;
    }

    /**
     * The EXEC blocks reachable from each EXEC statement, so that a statement and the command it stands for are known
     * to belong together.
     */
    private static List<List<String>> execsPerStatement(Cobol.CompilationUnit cu) {
        List<List<String>> execs = new ArrayList<>();
        new CobolIsoVisitor<Integer>() {
            @Override
            public Cobol.ExecCicsStatement visitExecCicsStatement(Cobol.ExecCicsStatement exec, Integer p) {
                execs.add(commands(exec.getExecCicsLines()));
                return super.visitExecCicsStatement(exec, p);
            }

            @Override
            public Cobol.ExecDliStatement visitExecDliStatement(Cobol.ExecDliStatement exec, Integer p) {
                execs.add(commands(exec.getExecDliLines()));
                return super.visitExecDliStatement(exec, p);
            }

            @Override
            public Cobol.ExecSqlStatement visitExecSqlStatement(Cobol.ExecSqlStatement exec, Integer p) {
                execs.add(commands(exec.getExecSqlLines()));
                return super.visitExecSqlStatement(exec, p);
            }
        }.visit(cu, 0);
        return execs;
    }

    private static List<String> commands(List<Cobol.Word> lines) {
        List<String> commands = new ArrayList<>();
        for (Cobol.Word line : lines) {
            for (CobolPreprocessor statement : line.getPreprocessorStatements()) {
                if (statement instanceof CobolPreprocessor.ExecStatement) {
                    List<String> words = new ArrayList<>();
                    new CobolPreprocessorIsoVisitor<Integer>() {
                        @Override
                        public CobolPreprocessor.Word visitWord(CobolPreprocessor.Word word, Integer p) {
                            words.add(word.getCobolWord().getWord());
                            return word;
                        }
                    }.visit(statement, 0);
                    commands.add(String.join(" ", words));
                }
            }
        }
        return commands;
    }
}
