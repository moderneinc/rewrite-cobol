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
package org.openrewrite.sas.trait;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.SourceFile;
import org.openrewrite.cobol.Corpus;
import org.openrewrite.jcl.JclParser;
import org.openrewrite.jcl.tree.Jcl;
import org.openrewrite.text.PlainText;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Reads the SAS of the estate and reports what the traits found. Gated on {@code SAS_CORPUS} pointing
 * at a checkout, because the corpus is not redistributed with this repository.
 * <p>
 * No public application in the corpus has any SAS, so the fixture is the whole measurement — and it
 * is the right one: {@code docs/INTERLINKS.md} sections 21.1 to 21.5 were written before any of this
 * read a {@code .sas} member, so every count below was somebody else's answer first.
 */
@EnabledIfEnvironmentVariable(named = "SAS_CORPUS", matches = ".+")
class SasCorpusTest {

    @Test
    void readsRealSas() throws IOException {
        Path corpus = Paths.get(System.getenv("SAS_CORPUS"));

        int members = 0;
        int statements = 0;
        int includes = 0;
        int libraries = 0;
        int files = 0;
        int macros = 0;
        int macroCalls = 0;
        int layouts = 0;
        int tables = 0;
        List<String> failures = new ArrayList<>();
        boolean fixtureFound = false;

        System.out.println("SAS members read, by application:");
        for (Path repository : Corpus.repositories(corpus)) {
            List<Path> paths = Corpus.sasPrograms(repository);
            if (paths.isEmpty()) {
                continue;
            }
            fixtureFound |= Corpus.isFixture(repository);
            int read = 0;
            for (Path member : paths) {
                members++;
                String name = corpus.relativize(member).toString();
                String source = new String(Files.readAllBytes(member));
                PlainText cu = parse(corpus, member);
                if (!source.equals(cu.printAll())) {
                    failures.add(name + ": did not print back");
                    continue;
                }

                // Counting the semicolons is the independent count: a statement is what one ends, and
                // the boundaries are all wrong if the two do not agree.
                List<Statements.Statement> parsed = Statements.in(source);
                int written = semicolons(source);
                int terminated = 0;
                for (Statements.Statement statement : parsed) {
                    terminated += statement.isTerminated() ? 1 : 0;
                }
                if (written != terminated) {
                    failures.add(name + ": " + terminated + " statements terminated, " + written +
                                 " semicolons written");
                } else {
                    read++;
                }

                statements += parsed.size();
                includes += new Include.Matcher().require(cu, null).getReferences().size();
                libraries += new Library.Matcher().require(cu, null).getReferences().size();
                files += new FileReference.Matcher().require(cu, null).getReferences().size();
                macros += new MacroDefinition.Matcher().require(cu, null).getMacros().size();
                macroCalls += new MacroCall.Matcher().require(cu, null).getReferences().size();
                layouts += new InputLayout.Matcher().require(cu, null).getLayouts().size();
                for (SqlQuery.Query query : new SqlQuery.Matcher().require(cu, null).getQueries()) {
                    tables += query.getTables().size();
                }
            }
            System.out.printf("  %-40s %3d of %3d%n", repository.getFileName(), read, paths.size());
        }
        assertThat(members).as("no SAS member found under %s", corpus).isPositive();

        System.out.printf("SAS corpus: %d members, %d statements, %d %%INCLUDE, %d LIBNAME, " +
                          "%d external files, %d macro definitions, %d macro invocations, " +
                          "%d INPUT layouts, %d tables read%n",
          members, statements, includes, libraries, files, macros, macroCalls, layouts, tables);
        if (!failures.isEmpty()) {
            System.out.println("failures:");
            failures.forEach(failure -> System.out.println("  " + failure));
        }

        assertThat(failures).isEmpty();
        assertThat(fixtureFound).as("mainframe-fixtures under %s", corpus).isTrue();
    }

    /**
     * INTERLINKS 21.2. Four {@code %INCLUDE} statements over one member, read by every program in the
     * subsystem — three from members of the library and one from the program the job writes in-stream.
     * {@code SASSRC} is not a path: it is the DD the procedure allocates on {@code CLM.PROD.SAS}.
     */
    @Test
    void readsEveryIncludeTheFixtureWrites() throws IOException {
        List<String> included = new ArrayList<>();
        for (Map.Entry<String, PlainText> program : programs().entrySet()) {
            for (Include.Reference include :
              new Include.Matcher().require(program.getValue(), null).getReferences()) {
                included.add(program.getKey() + " %INCLUDE " + include.getDdName() + "(" +
                             include.getMember() + ")");
            }
        }
        assertThat(included).containsExactly(
          "CLMSAUD %INCLUDE SASSRC(CLMSMAC)",
          "CLMSEXTR %INCLUDE SASSRC(CLMSMAC)",
          "CLMSPOL %INCLUDE SASSRC(CLMSMAC)",
          "CLMSTAT %INCLUDE SASSRC(CLMSMAC)");
    }

    /**
     * INTERLINKS 21.3. Every library and every file the subsystem reaches is a DD name the step
     * resolves: four {@code LIBNAME} statements over the one libref {@code CLMSAS}, and two
     * {@code INFILE}s whose DD names are the ones the COBOL programs {@code SELECT} for the same two
     * data sets.
     */
    @Test
    void readsTheDdNamesTheFixtureReachesItsDataBy() throws IOException {
        List<String> libraries = new ArrayList<>();
        List<String> files = new ArrayList<>();
        for (Map.Entry<String, PlainText> program : programs().entrySet()) {
            for (Library.Reference library :
              new Library.Matcher().require(program.getValue(), null).getReferences()) {
                libraries.add(program.getKey() + " LIBNAME " + library.getName() + " -> DD " +
                              library.getDdName());
            }
            for (FileReference.Reference file :
              new FileReference.Matcher().require(program.getValue(), null).getReferences()) {
                files.add(program.getKey() + " " + file.getKind() + " " + file.getName() + " -> DD " +
                          file.getDdName());
            }
        }

        assertThat(libraries).containsExactly(
          "CLMSAUD LIBNAME CLMSAS -> DD CLMSAS",
          "CLMSEXTR LIBNAME CLMSAS -> DD CLMSAS",
          "CLMSPOL LIBNAME CLMSAS -> DD CLMSAS",
          "CLMSTAT LIBNAME CLMSAS -> DD CLMSAS");
        // Two INFILEs and no FILE at all: every report goes to SASLIST, which SAS opens itself.
        assertThat(files).containsExactly(
          "CLMSAUD INFILE CLMAUDIT -> DD CLMAUDIT",
          "CLMSEXTR INFILE CLMEXTR -> DD CLMEXTR");
    }

    /**
     * INTERLINKS 21.2. One macro, defined in {@code CLMSMAC} and invoked five times: once in
     * {@code CLMSEXTR}, twice in {@code CLMSAUD}, once in {@code CLMSPOL} and once in the program the
     * job writes.
     */
    @Test
    void readsTheMacroTheFixtureDefinesAndEveryInvocationOfIt() throws IOException {
        Map<String, PlainText> programs = programs();
        List<MacroDefinition.Macro> defined = new ArrayList<>();
        for (PlainText program : programs.values()) {
            defined.addAll(new MacroDefinition.Matcher().require(program, null).getMacros());
        }
        assertThat(defined).extracting(MacroDefinition.Macro::getName, MacroDefinition.Macro::getParameters)
          .containsExactly(tuple("CLMTITL", List.of("SUBTTL")));

        Map<String, Integer> invoked = new TreeMap<>();
        for (Map.Entry<String, PlainText> program : programs.entrySet()) {
            for (MacroCall.Reference macro :
              new MacroCall.Matcher().require(program.getValue(), null).getReferences()) {
                assertThat(macro.isDefinedBy(List.of("CLMSMAC", "CLMTITL"))).as("%s", macro).isTrue();
                invoked.merge(program.getKey(), 1, Integer::sum);
            }
        }
        assertThat(invoked).containsExactly(entry("CLMSAUD", 2), entry("CLMSEXTR", 1),
          entry("CLMSPOL", 1), entry("CLMSTAT", 1));

        // The argument is the subtitle the report prints, which is the only thing the invocation says.
        assertThat(new MacroCall.Matcher().require(programs.get("CLMSPOL"), null).getReferences())
          .singleElement()
          .satisfies(macro -> assertThat(macro.getArguments())
            .containsExactly("POLICIES RESERVED ABOVE 80 PERCENT OF THE COVERAGE LIMIT"));
    }

    /**
     * INTERLINKS 21.4. The two {@code INPUT} layouts are the copybooks written again in SAS: the
     * column is the offset and the informat is what the COBOL picture is on the tape. Eleven of
     * {@code cpy/CLMEXTR}'s twelve items and seven of the eight in the {@code AUDIT-RECORD} of
     * {@code cbl/CLMB030}; both trailing {@code FILLER}s are not read.
     */
    @Test
    void readsTheTwoInputLayoutsColumnForColumn() throws IOException {
        Map<String, PlainText> programs = programs();

        assertThat(layout(programs.get("CLMSEXTR")))
          .extracting(InputLayout.Field::getColumn, InputLayout.Field::getName,
            InputLayout.Field::getInformat, InputLayout.Field::getBytes)
          .containsExactly(
            tuple(1, "CLAIMNO", "$CHAR10.", 10),
            tuple(11, "POLICYNO", "$CHAR12.", 12),
            tuple(23, "CLMNAME", "$CHAR30.", 30),
            tuple(53, "TYPECODE", "$CHAR4.", 4),
            tuple(57, "STATUS", "$CHAR1.", 1),
            tuple(58, "LOSSDATE", "YYMMDD8.", 8),
            tuple(66, "AMTCLM", "ZD13.2", 13),
            tuple(79, "AMTRSV", "ZD13.2", 13),
            tuple(92, "AMTPAID", "ZD13.2", 13),
            tuple(105, "ADJUSTER", "$CHAR8.", 8),
            tuple(113, "RUNDATE", "YYMMDD8.", 8));

        assertThat(layout(programs.get("CLMSAUD")))
          .extracting(InputLayout.Field::getColumn, InputLayout.Field::getName,
            InputLayout.Field::getInformat, InputLayout.Field::getBytes)
          .containsExactly(
            tuple(1, "RUNDATE", "YYMMDD8.", 8),
            tuple(9, "PROGRAM", "$CHAR8.", 8),
            tuple(17, "CLAIMNO", "$CHAR10.", 10),
            tuple(27, "TYPECODE", "$CHAR4.", 4),
            tuple(31, "OLDRSV", "ZD13.2", 13),
            tuple(44, "NEWRSV", "ZD13.2", 13),
            tuple(57, "RETCODE", "2.", 2));

        // The record is 200 bytes and 120 bytes; what the layouts read stops where the FILLER starts.
        assertThat(end(layout(programs.get("CLMSEXTR")))).isEqualTo(121);
        assertThat(end(layout(programs.get("CLMSAUD")))).isEqualTo(59);
    }

    /**
     * INTERLINKS 21.5. One statement of the subsystem goes to DB2, and it reads the view rather than
     * the table under it. The other two names a {@code PROC SQL} step reads are data sets of the SAS
     * library, which no DB2 catalog has ever heard of.
     */
    @Test
    void readsTheOneDb2TableTheFixtureReaches() throws IOException {
        List<SqlQuery.Table> tables = new ArrayList<>();
        for (PlainText program : programs().values()) {
            for (SqlQuery.Query query : new SqlQuery.Matcher().require(program, null).getQueries()) {
                tables.addAll(query.getTables());
            }
        }
        assertThat(tables).extracting(SqlQuery.Table::getName, SqlQuery.Table::getDbms)
          .containsExactly(
            tuple("CLM.POLICY_ACTIVE", "DB2"),
            tuple("CLMSAS.POLACT", null),
            tuple("CLMSAS.CLMDAY", null));
    }

    /**
     * INTERLINKS 8.7 and 21.1. {@code jcl/CLMSTAT} runs three members of the SAS library and carries
     * a fourth program in-stream, and that fourth has no member name at all — the member name is the
     * only name a SAS program has.
     */
    @Test
    void readsTheProgramTheJobWritesInStream() throws IOException {
        Jcl.CompilationUnit job = job();
        List<InstreamSas> streams = new InstreamSas.Matcher().lower(job).collect(Collectors.toList());
        assertThat(streams).extracting(InstreamSas::getName, InstreamSas::getLine)
          .containsExactly(tuple("SYSIN", 35));

        PlainText program = streams.get(0).parse();
        assertThat(streams.get(0).getText()).isEqualTo(program.printAll());

        // The three the job runs by name are members; this one is only in the job.
        assertThat(new Library.Matcher().require(program, null).getReferences())
          .singleElement().satisfies(library -> assertThat(library.getDdName()).isEqualTo("CLMSAS"));
    }

    /**
     * The four programs of the subsystem: the three members the job runs by name, the macro library
     * member every one of them includes, and the program written in the job — which is keyed by the
     * job's name because it has none of its own.
     */
    private static Map<String, PlainText> programs() throws IOException {
        Map<String, PlainText> programs = new LinkedHashMap<>();
        Path directory = fixture("sas");
        for (Path member : Corpus.sasPrograms(directory)) {
            programs.put(memberName(member), parse(directory, member));
        }
        Jcl.CompilationUnit job = job();
        for (InstreamSas stream : new InstreamSas.Matcher().lower(job).collect(Collectors.toList())) {
            programs.put(memberName(job.getSourcePath()), stream.parse());
        }
        return programs;
    }

    private static Jcl.CompilationUnit job() throws IOException {
        Path directory = fixture("jcl");
        Path member = directory.resolve("CLMSTAT.jcl");
        assertThat(Files.isRegularFile(member)).as("%s", member).isTrue();
        List<SourceFile> parsed = JclParser.builder().build()
          .parseInputs(Corpus.inputs(singletonList(member)), directory, new InMemoryExecutionContext())
          .collect(Collectors.toList());
        assertThat(parsed).singleElement().isInstanceOf(Jcl.CompilationUnit.class);
        return (Jcl.CompilationUnit) parsed.get(0);
    }

    private static List<InputLayout.Field> layout(@Nullable PlainText program) {
        assertThat(program).isNotNull();
        List<InputLayout.Layout> layouts = new InputLayout.Matcher().require(program, null).getLayouts();
        assertThat(layouts).hasSize(1);
        return layouts.get(0).getFields();
    }

    /**
     * The column after the last one the layout reads, which is where the record's {@code FILLER}
     * starts.
     */
    private static int end(List<InputLayout.Field> fields) {
        InputLayout.Field last = fields.get(fields.size() - 1);
        return last.getColumn() + last.getBytes();
    }

    private static PlainText parse(Path directory, Path member) {
        List<SourceFile> parsed = Corpus.plainTextReader()
          .parseInputs(Corpus.inputs(singletonList(member)), directory, new InMemoryExecutionContext())
          .collect(Collectors.toList());
        assertThat(parsed).singleElement().isInstanceOf(PlainText.class);
        return (PlainText) parsed.get(0);
    }

    private static Path fixture(String library) {
        Path directory = Paths.get(System.getenv("SAS_CORPUS"))
          .resolve("mainframe-fixtures/claims").resolve(library);
        assertThat(Files.isDirectory(directory)).as("%s", directory).isTrue();
        return directory;
    }

    private static String memberName(Path member) {
        String name = member.getFileName().toString();
        return name.contains(".") ? name.substring(0, name.indexOf('.')) : name;
    }

    /**
     * How many semicolons the source writes outside a comment and a quoted string, counted the way a
     * person reading it would.
     */
    private static int semicolons(String source) {
        int count = 0;
        boolean statement = false;
        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            if (source.startsWith("/*", i)) {
                int close = source.indexOf("*/", i + 2);
                i = close < 0 ? source.length() : close + 1;
            } else if (!statement && (c == '*' || source.startsWith("%*", i))) {
                int end = source.indexOf(';', i);
                i = end < 0 ? source.length() : end;
            } else if (c == '\'' || c == '"') {
                int close = source.indexOf(c, i + 1);
                i = close < 0 ? source.length() : close;
            } else if (c == ';') {
                count++;
                statement = false;
            } else if (!Character.isWhitespace(c)) {
                statement = true;
            }
        }
        return count;
    }


}
