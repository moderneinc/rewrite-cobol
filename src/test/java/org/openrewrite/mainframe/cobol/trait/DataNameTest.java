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
package org.openrewrite.mainframe.cobol.trait;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.SourceFile;
import org.openrewrite.mainframe.cobol.CobolParser;
import org.openrewrite.mainframe.cobol.CopybookParser;
import org.openrewrite.mainframe.cobol.Corpus;
import org.openrewrite.mainframe.cobol.tree.Cobol;
import org.openrewrite.test.RewriteTest;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.mainframe.cobol.Assertions.cobol;
import static org.openrewrite.test.RewriteTest.toRecipe;

class DataNameTest implements RewriteTest {

    private List<DataName> names(String source) {
        List<DataName> found = new ArrayList<>();
        rewriteRun(
          spec -> spec.recipe(toRecipe(() -> new DataName.Matcher().<ExecutionContext>asVisitor((name, ctx) -> {
              found.add(name);
              return name.getTree();
          }))),
          cobol(source)
        );
        return found;
    }

    private static String describe(DataName name) {
        return (name.isDefinition() ? name.getLevel() + " " : "") + name.getName();
    }

    private static DataName definitionOf(List<DataName> names, String name) {
        return names.stream()
          .filter(n -> n.isDefinition() && n.getName().equals(name))
          .findFirst()
          .orElseThrow();
    }

    private static List<DataName> usagesOf(List<DataName> names, String name) {
        return names.stream()
          .filter(n -> !n.isDefinition() && n.getName().equals(name))
          .collect(Collectors.toList());
    }

    @Test
    void tellsADefinitionFromAUsage() {
        List<DataName> names = names(
          """
            000000 IDENTIFICATION DIVISION.                                        \s
            000000 PROGRAM-ID. XREFPGM.                                            \s
            000000 DATA DIVISION.                                                  \s
            000000 WORKING-STORAGE SECTION.                                        \s
            000000 01  WS-REC.                                                     \s
            000000     05  WS-ACCT-ID     PIC X(10).                               \s
            000000     05  WS-ACCT-NUM    REDEFINES WS-ACCT-ID PIC 9(10).          \s
            000000     05  WS-UNUSED      PIC X(10).                               \s
            000000 PROCEDURE DIVISION.                                             \s
            000000     MOVE 'X' TO WS-ACCT-ID.                                     \s
            000000     GOBACK.                                                     \s
            """
        );

        assertThat(names).extracting(DataNameTest::describe)
          .containsExactly("1 WS-REC", "5 WS-ACCT-ID", "5 WS-ACCT-NUM", "WS-ACCT-ID", "5 WS-UNUSED", "WS-ACCT-ID");

        DataName moved = usagesOf(names, "WS-ACCT-ID").get(1);
        assertThat(moved.getDefinition()).isNotNull().satisfies(definition -> {
            assertThat(definition.getLevel()).isEqualTo(5);
            assertThat(definition.getParent()).isNotNull().extracting(DataName::getName).isEqualTo("WS-REC");
        });
        assertThat(moved.getLevel()).isEqualTo(5);
        assertThat(moved.getCopybook()).isNull();

        assertThat(definitionOf(names, "WS-ACCT-ID").isReferenced()).isTrue();
        assertThat(definitionOf(names, "WS-ACCT-NUM").isReferenced()).isFalse();
        assertThat(definitionOf(names, "WS-UNUSED").isReferenced()).isFalse();
        assertThat(definitionOf(names, "WS-REC").isReferenced()).isFalse();
        assertThat(definitionOf(names, "WS-REC").getParent()).isNull();
    }

    /**
     * {@code SET ERR-FLG-OFF TO TRUE} moves {@code 'N'} to {@code WS-ERR-FLG}, so it is a usage of
     * that item even though nothing in the program writes its name. A test of a condition name reads
     * the condition, and is not counted for the item.
     */
    @Test
    void countsASetOfAConditionNameAsAUsageOfItsConditionalVariable() {
        List<DataName> names = names(
          """
            000000 IDENTIFICATION DIVISION.                                        \s
            000000 PROGRAM-ID. FLAGPGM.                                            \s
            000000 DATA DIVISION.                                                  \s
            000000 WORKING-STORAGE SECTION.                                        \s
            000000 01  WS-FLAGS.                                                   \s
            000000     05  WS-ERR-FLG     PIC X VALUE 'N'.                         \s
            000000         88  ERR-FLG-ON         VALUE 'Y'.                       \s
            000000         88  ERR-FLG-OFF        VALUE 'N'.                       \s
            000000     05  WS-EOF-FLG     PIC X VALUE 'N'.                         \s
            000000         88  END-OF-FILE        VALUE 'Y'.                       \s
            000000 PROCEDURE DIVISION.                                             \s
            000000     SET ERR-FLG-OFF TO TRUE.                                    \s
            000000     IF END-OF-FILE                                              \s
            000000         GOBACK                                                  \s
            000000     END-IF.                                                     \s
            """
        );

        DataName set = usagesOf(names, "ERR-FLG-OFF").get(0);
        assertThat(set.isSet()).isTrue();
        assertThat(set.isConditionName()).isTrue();
        assertThat(set.getLevel()).isEqualTo(88);
        assertThat(set.getConditionalVariable()).isNotNull().extracting(DataName::getName).isEqualTo("WS-ERR-FLG");

        DataName tested = usagesOf(names, "END-OF-FILE").get(0);
        assertThat(tested.isSet()).isFalse();
        assertThat(tested.getConditionalVariable()).isNotNull().extracting(DataName::getName).isEqualTo("WS-EOF-FLG");

        assertThat(definitionOf(names, "WS-ERR-FLG").isReferenced()).isTrue();
        assertThat(definitionOf(names, "ERR-FLG-OFF").isReferenced()).isTrue();
        assertThat(definitionOf(names, "ERR-FLG-ON").isReferenced()).isFalse();
        assertThat(definitionOf(names, "WS-EOF-FLG").isReferenced()).isFalse();
        assertThat(definitionOf(names, "ERR-FLG-ON").getConditionalVariable())
          .isNotNull().extracting(DataName::getName).isEqualTo("WS-ERR-FLG");
        assertThat(definitionOf(names, "WS-ERR-FLG").getConditionalVariable()).isNull();
    }

    /**
     * {@code WS-ROW (WS-IDX)} is a {@code TableCall} whose subscript is a sibling of the name; the
     * occurrence is of the table, and the index within it is an occurrence of its own.
     */
    @Test
    void resolvesASubscriptedOrReferenceModifiedOccurrenceToTheItem() {
        List<DataName> names = names(
          """
            000000 IDENTIFICATION DIVISION.                                        \s
            000000 PROGRAM-ID. TABLPGM.                                            \s
            000000 DATA DIVISION.                                                  \s
            000000 WORKING-STORAGE SECTION.                                        \s
            000000 01  WS-TABLE.                                                   \s
            000000     05  WS-ROW  OCCURS 3 TIMES  PIC X(5).                       \s
            000000 01  WS-IDX     PIC 9.                                           \s
            000000 01  WS-BUF     PIC X(20).                                       \s
            000000 PROCEDURE DIVISION.                                             \s
            000000     MOVE WS-ROW (WS-IDX) TO WS-BUF (1:5).                       \s
            """
        );

        assertThat(names).filteredOn(n -> !n.isDefinition()).extracting(DataNameTest::describe)
          .containsExactly("WS-ROW", "WS-IDX", "WS-BUF");

        DataName row = usagesOf(names, "WS-ROW").get(0);
        assertThat(row.isSubscripted()).isTrue();
        assertThat(row.isReferenceModified()).isFalse();
        assertThat(row.getDefinition()).isNotNull().extracting(DataName::getLevel).isEqualTo(5);

        DataName index = usagesOf(names, "WS-IDX").get(0);
        assertThat(index.isSubscripted()).isFalse();
        assertThat(index.getDefinition()).isNotNull();

        DataName buffer = usagesOf(names, "WS-BUF").get(0);
        assertThat(buffer.isReferenceModified()).isTrue();
        assertThat(buffer.isSubscripted()).isFalse();
        assertThat(buffer.getDefinition()).isNotNull();

        assertThat(definitionOf(names, "WS-ROW").isReferenced()).isTrue();
        assertThat(definitionOf(names, "WS-IDX").isReferenced()).isTrue();
        assertThat(definitionOf(names, "WS-BUF").isReferenced()).isTrue();
        assertThat(definitionOf(names, "WS-TABLE").isReferenced()).isFalse();
    }

    /**
     * A name declared in two records is told apart by {@code OF}. Written without it the reference
     * is one the compiler would reject, and resolves to nothing; both declarations count as named,
     * since one of them was meant.
     */
    @Test
    void qualifiesANameDeclaredTwice() {
        List<DataName> names = names(
          """
            000000 IDENTIFICATION DIVISION.                                        \s
            000000 PROGRAM-ID. QUALPGM.                                            \s
            000000 DATA DIVISION.                                                  \s
            000000 WORKING-STORAGE SECTION.                                        \s
            000000 01  REC-A.                                                      \s
            000000     05  CUST-ID  PIC X(5).                                      \s
            000000 01  REC-B.                                                      \s
            000000     05  CUST-ID  PIC X(5).                                      \s
            000000     05  CUST-NM  PIC X(5).                                      \s
            000000 PROCEDURE DIVISION.                                             \s
            000000     MOVE CUST-ID OF REC-A TO CUST-ID OF REC-B.                  \s
            000000     DISPLAY CUST-ID.                                            \s
            000000     DISPLAY CUST-NM.                                            \s
            """
        );

        assertThat(names).filteredOn(n -> !n.isDefinition()).extracting(DataNameTest::describe)
          .containsExactly("CUST-ID", "REC-A", "CUST-ID", "REC-B", "CUST-ID", "CUST-NM");

        List<DataName> usages = usagesOf(names, "CUST-ID");
        assertThat(usages.get(0).getQualifiers()).containsExactly("REC-A");
        assertThat(usages.get(0).getParent()).isNotNull().extracting(DataName::getName).isEqualTo("REC-A");
        assertThat(usages.get(1).getParent()).isNotNull().extracting(DataName::getName).isEqualTo("REC-B");
        assertThat(usages.get(2).getQualifiers()).isEmpty();
        assertThat(usages.get(2).getDefinition()).isNull();

        DataName qualifier = usagesOf(names, "REC-A").get(0);
        assertThat(qualifier.getQualifiers()).isEmpty();
        assertThat(qualifier.getDefinition()).isNotNull().extracting(DataName::getLevel).isEqualTo(1);

        assertThat(usagesOf(names, "CUST-NM").get(0).getDefinition()).isNotNull();
        assertThat(names).filteredOn(DataName::isDefinition).allSatisfy(d -> assertThat(d.isReferenced()).isTrue());
    }

    /**
     * FLTCMT declares a record with a condition name. The definitions are the copybook's; the usages
     * the program's own.
     */
    @Test
    void attributesACopiedDefinitionToItsCopybook() {
        List<DataName> names = names(
          """
            000000 IDENTIFICATION DIVISION.                                        \s
            000000 PROGRAM-ID. COPYPGM.                                            \s
            000000 DATA DIVISION.                                                  \s
            000000 WORKING-STORAGE SECTION.                                        \s
            000000 COPY FLTCMT.                                                    \s
            000000 PROCEDURE DIVISION.                                             \s
            000000     SET FLTCMT-EYE-VALID TO TRUE.                               \s
            000000     MOVE 1 TO FLTCMT-ACCNO.                                     \s
            """
        );

        assertThat(names).extracting(DataNameTest::describe).containsExactly(
          "1 FLTCMT-COMMAREA", "3 FLTCMT-EYE", "88 FLTCMT-EYE-VALID", "3 FLTCMT-SORTCODE", "3 FLTCMT-ACCNO",
          "FLTCMT-EYE-VALID", "FLTCMT-ACCNO");
        assertThat(names).filteredOn(DataName::isDefinition)
          .allSatisfy(d -> assertThat(d.getCopybook()).isEqualTo("FLTCMT"));
        assertThat(names).filteredOn(n -> !n.isDefinition())
          .allSatisfy(u -> assertThat(u.getCopybook()).isNull());

        DataName set = usagesOf(names, "FLTCMT-EYE-VALID").get(0);
        assertThat(set.getDefinition()).isNotNull().extracting(DataName::getCopybook).isEqualTo("FLTCMT");
        assertThat(set.getConditionalVariable()).isNotNull().extracting(DataName::getName).isEqualTo("FLTCMT-EYE");

        assertThat(definitionOf(names, "FLTCMT-EYE").isReferenced()).isTrue();
        assertThat(definitionOf(names, "FLTCMT-ACCNO").isReferenced()).isTrue();
        assertThat(definitionOf(names, "FLTCMT-SORTCODE").isReferenced()).isFalse();
        assertThat(definitionOf(names, "FLTCMT-COMMAREA").isReferenced()).isFalse();
    }

    /**
     * The words of an {@code EXEC} block are not parsed, so a data name in one is known by where it
     * sits: after a colon in SQL, inside an option's parentheses in CICS. {@code NAME} is a column
     * and {@code LENGTH} an option; neither is declared, and neither is an occurrence.
     */
    @Test
    void readsAHostVariableAndAnOptionOperandInAnExecBlock() {
        List<DataName> names = names(
          """
            000000 IDENTIFICATION DIVISION.                                        \s
            000000 PROGRAM-ID. EXECPGM.                                            \s
            000000 DATA DIVISION.                                                  \s
            000000 WORKING-STORAGE SECTION.                                        \s
            000000 01  WS-NAME   PIC X(20).                                        \s
            000000 01  WS-RESP   PIC S9(8) COMP.                                   \s
            000000 01  WS-REC    PIC X(80).                                        \s
            000000 01  WS-LEN    PIC S9(4) COMP.                                   \s
            000000 01  WS-NONE   PIC X.                                            \s
            000000 PROCEDURE DIVISION.                                             \s
            000000     EXEC SQL SELECT NAME INTO :WS-NAME FROM CUSTOMER END-EXEC.  \s
            000000     EXEC CICS RECEIVE INTO(WS-REC) LENGTH(WS-LEN) RESP(WS-RESP) \s
            000000          END-EXEC.                                              \s
            """
        );

        assertThat(names).filteredOn(n -> !n.isDefinition()).extracting(DataNameTest::describe)
          .containsExactly("WS-NAME", "WS-REC", "WS-LEN", "WS-RESP");
        assertThat(names).filteredOn(n -> !n.isDefinition())
          .allSatisfy(u -> assertThat(u.getDefinition()).isNotNull());
        assertThat(definitionOf(names, "WS-NAME").isReferenced()).isTrue();
        assertThat(definitionOf(names, "WS-RESP").isReferenced()).isTrue();
        assertThat(definitionOf(names, "WS-NONE").isReferenced()).isFalse();
    }

    @Test
    void keepsANestedProgramsNamesApart() {
        List<DataName> names = names(
          """
            000000 IDENTIFICATION DIVISION.                                        \s
            000000 PROGRAM-ID. OUTERPGM.                                           \s
            000000 DATA DIVISION.                                                  \s
            000000 WORKING-STORAGE SECTION.                                        \s
            000000 01  WS-SHARED   PIC X.                                          \s
            000000 PROCEDURE DIVISION.                                             \s
            000000     MOVE 'Y' TO WS-SHARED.                                      \s
            000000 IDENTIFICATION DIVISION.                                        \s
            000000 PROGRAM-ID. INNERPGM.                                           \s
            000000 DATA DIVISION.                                                  \s
            000000 WORKING-STORAGE SECTION.                                        \s
            000000 01  WS-SHARED   PIC X.                                          \s
            000000 PROCEDURE DIVISION.                                             \s
            000000     GOBACK.                                                     \s
            000000 END PROGRAM INNERPGM.                                           \s
            000000 END PROGRAM OUTERPGM.                                           \s
            """
        );

        List<DataName> definitions = names.stream().filter(DataName::isDefinition).collect(Collectors.toList());
        assertThat(definitions).hasSize(2);
        assertThat(definitions.get(0).isReferenced()).isTrue();
        assertThat(definitions.get(1).isReferenced()).isFalse();
        assertThat(usagesOf(names, "WS-SHARED")).singleElement()
          .satisfies(u -> assertThat(u.getDefinition()).isNotNull()
            .extracting(d -> d.getTree().getId()).isEqualTo(definitions.get(0).getTree().getId()));
    }

    /**
     * Run with {@code COBOL_CORPUS=/path/to/corpus}. GenApp declares its records through copybooks
     * and names a called program only inside an {@code EXEC CICS LINK}; CardDemo sets and tests
     * condition names its copybooks declare.
     */
    @EnabledIfEnvironmentVariable(named = "COBOL_CORPUS", matches = ".+")
    @Test
    void readsRealPrograms() throws IOException {
        Path root = Paths.get(System.getenv("COBOL_CORPUS"));
        Map<String, List<DataName>> programs = new HashMap<>();
        int definitions = 0;
        int usages = 0;
        int resolved = 0;
        int unreferenced = 0;
        int throughSet = 0;
        Map<String, Integer> unresolved = new HashMap<>();
        Map<String, List<String>> unresolvedIn = new HashMap<>();

        for (Path repository : Corpus.repositories(root)) {
            String application = repository.getFileName().toString();
            if (!application.equals("genapp") && !application.equals("carddemo")) {
                continue;
            }
            List<SourceFile> copybooks = CopybookParser.builder().build()
              .parseInputs(Corpus.inputs(Corpus.copybooks(repository)), root, new InMemoryExecutionContext())
              .collect(Collectors.toList());
            List<SourceFile> parsed = CobolParser.builder().copybooks(copybooks).build()
              .parseInputs(Corpus.inputs(Corpus.programs(repository)), root, new InMemoryExecutionContext())
              .collect(Collectors.toList());
            for (SourceFile program : parsed) {
                if (!(program instanceof Cobol.CompilationUnit)) {
                    continue;
                }
                String name = program.getSourcePath().getFileName().toString();
                List<DataName> names = new ArrayList<>();
                new DataName.Matcher().<Integer>asVisitor((dataName, p) -> {
                    names.add(dataName);
                    return dataName.getTree();
                }).visit(program, 0);
                programs.put(name, names);

                for (DataName dataName : names) {
                    if (dataName.isDefinition()) {
                        definitions++;
                        if (!dataName.isReferenced()) {
                            unreferenced++;
                        } else if (names.stream().noneMatch(u -> !u.isDefinition() && u.getDefinition() != null &&
                                                                 u.getDefinition().getTree().getId().equals(dataName.getTree().getId()))) {
                            throughSet++;
                        }
                    } else {
                        usages++;
                        if (dataName.getDefinition() != null) {
                            resolved++;
                        } else {
                            unresolved.merge(dataName.getName().toUpperCase(), 1, Integer::sum);
                            unresolvedIn.computeIfAbsent(name, k -> new ArrayList<>()).add(dataName.getName().toUpperCase());
                        }
                    }
                }
            }
        }

        System.out.printf("definitions: %d, of which %d nothing names and %d are named only by a SET of a condition name%n",
          definitions, unreferenced, throughSet);
        System.out.printf("usages: %d, %d resolved, unresolved most often %s%n", usages, resolved,
          unresolved.entrySet().stream()
            .sorted((a, b) -> b.getValue() - a.getValue())
            .limit(12)
            .map(e -> e.getKey() + " x" + e.getValue())
            .collect(Collectors.joining(", ")));

        // GenApp: a called program's name is a 77 item reached only through EXEC CICS LINK Program(LGACDB01).
        List<DataName> lgacus01 = programs.get("lgacus01.cbl");
        assertThat(lgacus01).isNotNull();
        assertThat(definitionOf(lgacus01, "LGACDB01").isReferenced()).isTrue();
        assertThat(usagesOf(lgacus01, "LGACDB01")).singleElement().satisfies(link ->
          assertThat((Object) link.getCursor().getParentTreeCursor().getValue()).isInstanceOf(Cobol.Word.class));
        assertThat(definitionOf(lgacus01, "WS-EYECATCHER").isReferenced()).isFalse();
        assertThat(definitionOf(lgacus01, "CA-RETURN-CODE")).satisfies(field -> {
            assertThat(field.getCopybook()).isEqualTo("LGCMAREA");
            assertThat(field.isReferenced()).isTrue();
        });
        // Every name the program uses and does not declare is one CICS supplies.
        assertThat(unresolvedIn.getOrDefault("lgacus01.cbl", new ArrayList<>()))
          .allSatisfy(n -> assertThat(n).startsWith("EIB"));

        // CardDemo: the commarea's condition names are the copybook's, and are set and tested here.
        List<DataName> cosgn00c = programs.get("COSGN00C.cbl");
        assertThat(cosgn00c).isNotNull();
        assertThat(usagesOf(cosgn00c, "CDEMO-USRTYP-ADMIN")).singleElement().satisfies(tested -> {
            assertThat(tested.isSet()).isFalse();
            assertThat(tested.getDefinition()).isNotNull().extracting(DataName::getCopybook).isEqualTo("COCOM01Y");
            assertThat(tested.getConditionalVariable()).isNotNull().extracting(DataName::getName).isEqualTo("CDEMO-USER-TYPE");
        });
        assertThat(usagesOf(cosgn00c, "ERR-FLG-OFF")).singleElement().satisfies(set -> {
            assertThat(set.isSet()).isTrue();
            assertThat(set.getConditionalVariable()).isNotNull().extracting(DataName::getName).isEqualTo("WS-ERR-FLG");
        });
    }
}
