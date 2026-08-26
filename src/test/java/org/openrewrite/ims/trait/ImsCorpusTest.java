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
import static org.openrewrite.ims.trait.Psb.ProgramKind.BATCH;
import static org.openrewrite.ims.trait.Psb.ProgramKind.MESSAGE_DRIVEN;

/**
 * Reads the IMS gen source of real applications and reports what the traits found, the same way
 * {@code BmsCorpusTest} does for BMS. Gated on {@code IMS_CORPUS} pointing at a checkout, because the
 * corpus is not redistributed with this repository.
 * <p>
 * Two assertions matter. Printing back byte for byte says the columns survived, and counting the
 * macros against an independent count of the source says they were grouped into the right statements
 * — a misgrouped continuation prints back perfectly and says something else. The fixture is measured
 * against {@code docs/INTERLINKS.md} sections 19.1 to 19.4, which were written before any of this
 * read a gen member.
 */
@EnabledIfEnvironmentVariable(named = "IMS_CORPUS", matches = ".+")
class ImsCorpusTest {

    @Test
    void readsRealDefinitions() throws IOException {
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
        int programSpecifications = 0;
        int pcbs = 0;
        int sensitiveSegments = 0;
        int writtenPcbs = 0;
        int writtenSensitiveSegments = 0;
        List<String> failures = new ArrayList<>();
        boolean fixtureFound = false;

        System.out.println("gen members read, by application:");
        for (Path repository : Corpus.repositories(corpus)) {
            List<Path> files = Corpus.imsDefinitions(repository);
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
                for (String macro : new String[]{"SEGM", "FIELD", "LCHILD", "PCB", "SENSEG", "SENFLD"}) {
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
                writtenPcbs += countMacro(source, "PCB");
                writtenSensitiveSegments += countMacro(source, "SENSEG");

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

                for (Psb psb : new Psb.Matcher().lower(cu).collect(Collectors.toList())) {
                    programSpecifications++;
                    for (Pcb pcb : psb.getPcbs()) {
                        pcbs++;
                        sensitiveSegments += pcb.getSensitiveSegments().size();
                        if (pcb.isDatabase() && pcb.getDatabaseName() == null) {
                            failures.add(name + ": PCB " + pcb.getPosition() + " names no database");
                        }
                    }
                }
            }
            System.out.printf("  %-40s %3d of %3d%n", repository.getFileName(), read, files.size());
        }
        assertThat(members).as("no gen member found under %s", corpus).isPositive();

        System.out.printf("IMS corpus: %d members, %d databases, %d segments, %d fields, " +
                        "%d references to another database, %d PSBs, %d PCBs, %d sensitive segments%n",
                members, databases, segments, fields, references, programSpecifications, pcbs,
                sensitiveSegments);
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
        assertThat(pcbs).as("PCBs reachable through their PSB").isEqualTo(writtenPcbs);
        assertThat(sensitiveSegments).as("sensitive segments reachable through their PCB")
                .isEqualTo(writtenSensitiveSegments);

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
        List<Database> databases = new ArrayList<>();
        for (SourceFile member : fixture("dbd")) {
            new Database.Matcher().lower(member).forEach(databases::add);
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

    /**
     * INTERLINKS 19.3, which says what the fixture's six PSBs hand their programs. The PCB order is
     * the measurement: a database named against the wrong position is a defect no round trip catches.
     */
    @Test
    void readsTheFixturePsbsAsItsOwnDocumentationDescribesIt() throws IOException {
        List<Psb> psbs = new ArrayList<>();
        for (SourceFile member : fixture("psb")) {
            new Psb.Matcher().lower(member).forEach(psbs::add);
        }

        // Six PSBs of ten PCBs: eight database, one GSAM, one TP.
        assertThat(psbs).extracting(Psb::getName, Psb::getLanguage, Psb::isCompatible)
                .containsExactly(
                        tuple("CLMPSB01", "COBOL", false),
                        tuple("CLMPSB02", "COBOL", false),
                        tuple("CLMPSB03", "COBOL", false),
                        tuple("CLMPSB04", "COBOL", true),
                        tuple("CLMPSB05", "COBOL", false),
                        tuple("CLMPSB06", "COBOL", false));
        assertThat(psbs.stream().flatMap(psb -> psb.getPcbs().stream()))
                .extracting(Pcb::getType, Pcb::getDatabaseName, Pcb::getProcessingOptions,
                        Pcb::getProcessingSequence, Pcb::getName)
                .containsExactly(
                        tuple("DB", "CLMDBD01", "A", null, null),
                        tuple("TP", null, null, null, null),
                        tuple("DB", "CLMDBD01", "G", null, null),
                        tuple("DB", "CLMDBD03", "G", null, null),
                        tuple("DB", "CLMDBD01", "G", "CLMDBX01", null),
                        tuple("GSAM", "CLMDBG01", "GS", null, null),
                        tuple("DB", "CLMDBD01", "AP", null, null),
                        tuple("DB", "CLMDBD01", "A", null, "CLMPCB1"),
                        tuple("DB", "CLMDBD01", "G", null, null),
                        tuple("DB", "CLMDBD03", "G", null, null));

        // Fourteen sensitive segments, and the seven SENFLDs of CLMPSB02's second PCB, which build a
        // fifty byte I/O area out of a sixty five byte segment.
        assertThat(psbs.stream().flatMap(psb -> psb.getPcbs().stream())
                .mapToInt(pcb -> pcb.getSensitiveSegments().size()).sum()).isEqualTo(14);
        Pcb fieldSensitive = psbs.get(1).getPcbs().get(1);
        assertThat(fieldSensitive.getSensitiveSegments().get(0).getSensitiveFields())
                .extracting(SensitiveField::getName, SensitiveField::getStart)
                .containsExactly(
                        tuple("CLMKEY", 1),
                        tuple("CLMPOL", 11),
                        tuple("CLMTYP", 23),
                        tuple("CLMSTAT", 27),
                        tuple("CLMLOSS", 28),
                        tuple("CLMAMTC", 36),
                        tuple("CLMADJR", 43));
        assertThat(fieldSensitive.getSensitiveSegments().get(1).getSensitiveFields()).isEmpty();

        // Section 6.1, program by program: which PCB the nth mask is depends on what runs under the
        // PSB, and the two exceptions name a PCB instead of counting.
        assertThat(psbs.get(0).getPcbAtMask(1, BATCH)).isNotNull()
                .extracting(Pcb::getDatabaseName).isEqualTo("CLMDBD01");
        assertThat(psbs.get(1).getPcbAtMask(1, MESSAGE_DRIVEN)).isNull();
        assertThat(psbs.get(1).getPcbAtMask(3, MESSAGE_DRIVEN)).isNotNull()
                .extracting(Pcb::getDatabaseName).isEqualTo("CLMDBD01");
        assertThat(psbs.get(1).getPcbAtMask(4, MESSAGE_DRIVEN)).isNotNull()
                .extracting(Pcb::getDatabaseName).isEqualTo("CLMDBD03");
        assertThat(psbs.get(3).getPcbAtMask(2, BATCH)).isNotNull()
                .extracting(Pcb::getDatabaseName).isEqualTo("CLMDBG01");
        assertThat(psbs.get(3).getPcbAtMask(3, BATCH)).isNotNull()
                .extracting(Pcb::getDatabaseName).isEqualTo("CLMDBD01");
        assertThat(psbs.get(4).getPcb("CLMPCB1")).isNotNull()
                .extracting(Pcb::getDatabaseName).isEqualTo("CLMDBD01");
        assertThat(psbs.get(5).getDatabasePcb(1)).isNotNull()
                .extracting(Pcb::getDatabaseName).isEqualTo("CLMDBD01");
        assertThat(psbs.get(5).getDatabasePcb(2)).isNotNull()
                .extracting(Pcb::getDatabaseName).isEqualTo("CLMDBD03");
    }

    /**
     * INTERLINKS 19.4, the stage 1 deck: thirteen macros, and the only place a transaction code is
     * tied to a PSB.
     */
    @Test
    void readsTheFixtureStage1AsItsOwnDocumentationDescribesIt() throws IOException {
        List<SourceFile> deck = fixture("stage1");
        assertThat(deck).hasSize(1);

        List<Application> applications = new Application.Matcher().lower(deck.get(0))
                .collect(Collectors.toList());
        assertThat(applications).extracting(Application::getPsbName, Application::getProgramType,
                        Application::isMessageDriven, Application::getLine)
                .containsExactly(
                        tuple("CLMPSB02", "TP", true, 17),
                        tuple("CLMPSB01", "BATCH", false, 27),
                        tuple("CLMPSB03", "BATCH", false, 28),
                        tuple("CLMPSB04", "BATCH", false, 29),
                        tuple("CLMPSB05", "BATCH", false, 30),
                        tuple("CLMPSB06", "BATCH", false, 31));

        // Both transactions are under the one APPLCTN, so both are answered by the one program.
        assertThat(applications.get(0).getTransactions())
                .extracting(Transaction::getCode, Transaction::getScratchpadSize, Transaction::getLine)
                .containsExactly(
                        tuple("CLMINQ", 150, 18),
                        tuple("CLMDTL", 150, 21));
        assertThat(applications.stream().skip(1).flatMap(a -> a.getTransactions().stream())).isEmpty();

        // Five databases, and CLMDBG01 is not among them: a GSAM database is allocated by the batch
        // job that reads it and is never known to the control region.
        assertThat(new DatabaseAccess.Matcher().lower(deck.get(0)).collect(Collectors.toList()))
                .extracting(DatabaseAccess::getName, DatabaseAccess::getAccess, DatabaseAccess::getLine)
                .containsExactly(
                        tuple("CLMDBD01", "UP", 36),
                        tuple("CLMDBX01", "UP", 37),
                        tuple("CLMDBD02", "RD", 38),
                        tuple("CLMDBX02", "RD", 39),
                        tuple("CLMDBD03", "RD", 40));
    }

    /**
     * One gen library of the fixture, parsed.
     */
    private static List<SourceFile> fixture(String library) throws IOException {
        Path directory = Paths.get(System.getenv("IMS_CORPUS"))
                .resolve("mainframe-fixtures/claims/ims").resolve(library);
        assertThat(Files.isDirectory(directory)).as("%s", directory).isTrue();

        List<SourceFile> members = new ArrayList<>();
        for (Path member : Corpus.imsDefinitions(directory)) {
            List<SourceFile> parsed = ImsParser.builder().build()
                    .parseInputs(Corpus.inputs(singletonList(member)), directory,
                            new InMemoryExecutionContext())
                    .collect(Collectors.toList());
            assertThat(parsed).singleElement().isInstanceOf(Ims.CompilationUnit.class);
            members.add(parsed.get(0));
        }
        return members;
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
