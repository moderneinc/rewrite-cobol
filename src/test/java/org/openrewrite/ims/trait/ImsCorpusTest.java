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
package org.openrewrite.ims.trait;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.SourceFile;
import org.openrewrite.cobol.Corpus;
import org.openrewrite.ims.ImsParser;
import org.openrewrite.ims.tree.Ims;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Reads the IMS gen source of real applications and reports what the traits found, the same way
 * {@code BmsCorpusTest} does for BMS. Gated on {@code IMS_CORPUS} pointing at a checkout, because the
 * corpus is not redistributed with this repository.
 * <p>
 * Two assertions matter. Printing back byte for byte says the columns survived, and counting the
 * macros against an independent count of the source says they were grouped into the right statements
 * — a misgrouped continuation prints back perfectly and says something else. The fixture is measured
 * against {@code docs/INTERLINKS.md} sections 19.1 and 19.2, which were written before any of this
 * read a DBD.
 */
@EnabledIfEnvironmentVariable(named = "IMS_CORPUS", matches = ".+")
class ImsCorpusTest {

    @Test
    void readsRealDatabases() throws IOException {
        Path corpus = Paths.get(System.getenv("IMS_CORPUS"));

        int members = 0;
        int databases = 0;
        int segments = 0;
        int fields = 0;
        int references = 0;
        int writtenSegments = 0;
        int writtenFields = 0;
        int writtenLogicalChildren = 0;
        int logicalChildren = 0;
        List<String> failures = new ArrayList<>();
        boolean fixtureFound = false;

        System.out.println("DBDs read, by application:");
        for (Path repository : Corpus.repositories(corpus)) {
            List<Path> files = Corpus.databases(repository);
            if (files.isEmpty()) {
                continue;
            }
            fixtureFound |= Corpus.isFixture(repository);
            int read = 0;
            for (Path member : files) {
                members++;
                String name = corpus.relativize(member).toString();
                String source = new String(Files.readAllBytes(member));
                List<SourceFile> parsed = ImsParser.builder().build()
                        .parseInputs(Corpus.inputs(singletonList(member)), corpus,
                                new InMemoryExecutionContext())
                        .collect(Collectors.toList());
                if (parsed.size() != 1 || !(parsed.get(0) instanceof Ims.CompilationUnit)) {
                    failures.add(name + ": did not parse");
                    continue;
                }
                Ims.CompilationUnit cu = (Ims.CompilationUnit) parsed.get(0);

                if (!source.equals(cu.printAll())) {
                    failures.add(name + ": did not print back");
                    continue;
                }

                // The traits must find exactly the macros the source has. Counting them independently
                // is the only thing that turns "it ran without complaining" into evidence that the
                // file was read correctly.
                boolean counted = true;
                for (String macro : new String[]{"SEGM", "FIELD", "LCHILD"}) {
                    int inSource = countMacro(source, macro);
                    int inTree = countOperation(cu, macro);
                    if (inSource != inTree) {
                        failures.add(name + ": " + inTree + " " + macro + " read, " + inSource + " written");
                        counted = false;
                    }
                }
                writtenSegments += countMacro(source, "SEGM");
                writtenFields += countMacro(source, "FIELD");
                writtenLogicalChildren += countMacro(source, "LCHILD");

                // An operand read as an operation is what a mishandled continuation looks like, and
                // it is silent: the statement still prints back, it just says something else.
                for (Ims.MacroStatement statement : statementsIn(cu)) {
                    if (statement.getOperation().getText().contains("=")) {
                        failures.add(name + ": read '" +
                                statement.getOperation().getText() + "' as an operation");
                        counted = false;
                    }
                }
                if (counted) {
                    read++;
                }

                for (Database database : new Database.Matcher().lower(cu).collect(Collectors.toList())) {
                    databases++;
                    references += database.getReferences().size();
                    for (Database.Reference reference : database.getReferences()) {
                        if (reference.getDatabase().isEmpty() || reference.getMember().isEmpty()) {
                            failures.add(name + ": a reference naming " + reference.getDatabase() +
                                    '.' + reference.getMember());
                        }
                    }
                    for (Segment segment : database.getSegments()) {
                        segments++;
                        fields += segment.getFields().size();
                        logicalChildren += segment.getLogicalChildren().size();
                        if (segment.getBytes() == null) {
                            failures.add(name + ": " + segment.getName() + " says no length");
                        }
                    }
                }
            }
            System.out.printf("  %-40s %3d of %3d%n", repository.getFileName(), read, files.size());
        }
        assertThat(members).as("no DBD found under %s", corpus).isPositive();

        System.out.printf("IMS corpus: %d members, %d databases, %d segments, %d fields, " +
                        "%d references to another database%n",
                members, databases, segments, fields, references);
        if (!failures.isEmpty()) {
            System.out.println("failures:");
            failures.forEach(f -> System.out.println("  " + f));
        }

        assertThat(failures).isEmpty();

        // Every segment and field is reachable from the database it belongs to. Containment is read
        // from position rather than from brackets, so one the walk cannot reach is one no report
        // would find.
        assertThat(segments).as("segments reachable through their database").isEqualTo(writtenSegments);
        assertThat(fields).as("fields reachable through their segment").isEqualTo(writtenFields);
        assertThat(logicalChildren).as("logical children reachable through their segment")
                .isEqualTo(writtenLogicalChildren);

        // Every map set is required to read, so the fixture only has to be there: one the walk could
        // not see, a symbolic link say, would otherwise pass as an empty application.
        assertThat(fixtureFound).as("mainframe-fixtures under %s", corpus).isTrue();
    }

    /**
     * INTERLINKS 19.1 and 19.2, which say what the fixture's six DBDs hold and which of them name
     * each other. The public applications are report-only; this is the measurement.
     */
    @Test
    void readsTheFixtureAsItsOwnDocumentationDescribesIt() throws IOException {
        Path dbds = Paths.get(System.getenv("IMS_CORPUS")).resolve("mainframe-fixtures/claims/ims/dbd");
        assertThat(Files.isDirectory(dbds)).as("%s", dbds).isTrue();

        List<Database> databases = new ArrayList<>();
        for (Path member : Corpus.databases(dbds)) {
            List<SourceFile> parsed = ImsParser.builder().build()
                    .parseInputs(Corpus.inputs(singletonList(member)), dbds, new InMemoryExecutionContext())
                    .collect(Collectors.toList());
            assertThat(parsed).singleElement().isInstanceOf(Ims.CompilationUnit.class);
            new Database.Matcher().lower(parsed.get(0)).forEach(databases::add);
        }

        // 19.1: six DBDs, eight segments, thirty fields.
        assertThat(databases).extracting(Database::getName, Database::getAccessMethod)
                .containsExactly(
                        tuple("CLMDBD01", "HDAM"),
                        tuple("CLMDBD02", "HIDAM"),
                        tuple("CLMDBD03", "HISAM"),
                        tuple("CLMDBG01", "GSAM"),
                        tuple("CLMDBX01", "INDEX"),
                        tuple("CLMDBX02", "INDEX"));
        assertThat(databases.stream().mapToInt(d -> d.getSegments().size()).sum()).isEqualTo(8);
        assertThat(databases.stream()
                .flatMap(d -> d.getSegments().stream())
                .mapToInt(s -> s.getFields().size()).sum()).isEqualTo(30);

        // The DD names a job step has to supply, HISAM's two among them.
        assertThat(databases).flatExtracting(Database::getDdNames).containsExactly(
                "CLMDB01", "CLMDB02", "CLMTYP1", "CLMTYP2", "CLMGSIN", "CLMDBX01", "CLMDBX02");

        // Each segment's length and the field it is keyed on, and whether two may share a key.
        assertThat(databases.stream().flatMap(d -> d.getSegments().stream()))
                .extracting(Segment::getName, Segment::getParentName, Segment::getBytes,
                        segment -> segment.getSequenceField().getName(),
                        segment -> segment.getSequenceField().isUnique())
                .containsExactly(
                        tuple("CLMROOT", null, 65, "CLMKEY", true),
                        tuple("CLMDETL", "CLMROOT", 57, "DETLSEQ", true),
                        tuple("CLMPLNK", "CLMROOT", 12, "CLMLPCK", true),
                        tuple("POLROOT", null, 120, "POLKEY", true),
                        tuple("POLCOVR", "POLROOT", 60, "COVRSEQ", true),
                        tuple("TYPROOT", null, 80, "TYPCODE", true),
                        tuple("CLMXSEG", null, 18, "CLMXKEY", false),
                        tuple("POLINDX", null, 12, "POLXKEY", true));

        // A GSAM database has no SEGM at all, which is what makes it GSAM.
        Database gsam = databases.get(3);
        assertThat(gsam.getSegments()).isEmpty();
        assertThat(gsam.getDataSetGroups()).singleElement()
                .satisfies(group -> assertThat(group.getRecordLengths()).containsExactly(200, 27800));

        // 19.2: seven references over five DBDs, every one naming a DBD and a segment or field in it.
        assertThat(databases.stream().flatMap(d -> d.getReferences().stream()))
                .extracting(Database.Reference::getKind, Database.Reference::getDatabase,
                        Database.Reference::getMember, Database.Reference::getLine)
                .containsExactly(
                        tuple(Database.Reference.Kind.LOGICAL_CHILD, "CLMDBX01", "CLMXSEG", 26),
                        tuple(Database.Reference.Kind.INDEX_SOURCE, "CLMDBD01", "CLMADJR", 27),
                        tuple(Database.Reference.Kind.LOGICAL_PARENT, "CLMDBD02", "POLROOT", 38),
                        tuple(Database.Reference.Kind.LOGICAL_CHILD, "CLMDBX02", "POLINDX", 22),
                        tuple(Database.Reference.Kind.LOGICAL_CHILD, "CLMDBD01", "CLMPLNK", 23),
                        tuple(Database.Reference.Kind.LOGICAL_CHILD, "CLMDBD01", "CLMROOT", 19),
                        tuple(Database.Reference.Kind.LOGICAL_CHILD, "CLMDBD02", "POLROOT", 14));
    }

    private static int countMacro(String source, String macro) {
        int count = 0;
        for (String line : source.split("\n", -1)) {
            if (line.startsWith("*") || line.trim().isEmpty()) {
                continue;
            }
            // The operation is the second field, so a database name or a comment holding the macro's
            // name does not count.
            String[] words = line.trim().split("\\s+");
            int operation = line.charAt(0) == ' ' ? 0 : 1;
            if (words.length > operation && words[operation].equalsIgnoreCase(macro)) {
                count++;
            }
        }
        return count;
    }

    private static int countOperation(Ims.CompilationUnit cu, String macro) {
        int count = 0;
        for (Ims.MacroStatement statement : statementsIn(cu)) {
            if (statement.isOperation(macro)) {
                count++;
            }
        }
        return count;
    }

    private static List<Ims.MacroStatement> statementsIn(Ims.CompilationUnit cu) {
        return cu.getStatements().stream()
                .filter(Ims.MacroStatement.class::isInstance)
                .map(Ims.MacroStatement.class::cast)
                .collect(Collectors.toList());
    }
}
