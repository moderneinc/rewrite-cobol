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
package org.openrewrite.assembler.trait;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.SourceFile;
import org.openrewrite.assembler.AssemblerIsoVisitor;
import org.openrewrite.assembler.AssemblerParser;
import org.openrewrite.assembler.tree.Assembler;
import org.openrewrite.assembler.tree.Space;
import org.openrewrite.cobol.Corpus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Reads the HLASM of real applications and reports what the traits found, the same way
 * {@code ImsCorpusTest} does for the gen libraries. Gated on {@code ASM_CORPUS} pointing at a checkout,
 * because the corpus is not redistributed with this repository.
 * <p>
 * Two assertions matter over the public applications. Printing back byte for byte says the columns
 * survived, and counting the operations against an independent count of the source says the lines were
 * grouped into the right statements — a misgrouped continuation prints back perfectly and reports an
 * operand as an operation. The fixture is the measurement: {@code docs/INTERLINKS.md} sections 20.2 to
 * 20.6 were written before any of this read an assembler member.
 */
@EnabledIfEnvironmentVariable(named = "ASM_CORPUS", matches = ".+")
class AssemblerCorpusTest {

    @Test
    void readsRealAssembler() throws IOException {
        Path corpus = Paths.get(System.getenv("ASM_CORPUS"));

        int members = 0;
        int sections = 0;
        int dummySections = 0;
        int fields = 0;
        int copies = 0;
        int macros = 0;
        int macroCalls = 0;
        int calls = 0;
        int dliCalls = 0;
        int dataControlBlocks = 0;
        int entryPoints = 0;
        List<String> failures = new ArrayList<>();
        boolean fixtureFound = false;

        System.out.println("assembler members read, by application:");
        for (Path repository : Corpus.repositories(corpus)) {
            List<Path> files = Corpus.assemblerMembers(repository);
            if (files.isEmpty()) {
                continue;
            }
            fixtureFound |= Corpus.isFixture(repository);
            int read = 0;
            for (Path member : files) {
                members++;
                String name = corpus.relativize(member).toString();
                String source = new String(Files.readAllBytes(member));
                List<SourceFile> parsed = AssemblerParser.builder().build()
                  .parseInputs(Corpus.inputs(singletonList(member)), corpus, new InMemoryExecutionContext())
                  .collect(Collectors.toList());
                if (parsed.size() != 1 || !(parsed.get(0) instanceof Assembler.CompilationUnit)) {
                    failures.add(name + ": did not parse");
                    continue;
                }
                Assembler.CompilationUnit cu = (Assembler.CompilationUnit) parsed.get(0);

                if (!source.equals(cu.printAll())) {
                    failures.add(name + ": did not print back");
                    continue;
                }

                // The traits must find exactly the statements the source has. Counting them
                // independently is the only thing that turns "it ran without complaining" into
                // evidence that the columns were read correctly.
                boolean counted = true;
                for (Map.Entry<String, Integer> written : operationCounts(source).entrySet()) {
                    int inTree = countOperation(cu, written.getKey());
                    if (inTree != written.getValue()) {
                        failures.add(name + ": " + inTree + " " + written.getKey() + " read, " +
                                     written.getValue() + " written");
                        counted = false;
                    }
                }

                // An operand read as an operation is what a mishandled continuation looks like, and it
                // is silent: the statement still prints back, it just says something else.
                for (Assembler.Instruction instruction : instructionsIn(cu)) {
                    String operation = instruction.getOperation().getText();
                    if (operation.contains("=") || operation.contains(",") || operation.contains("'")) {
                        failures.add(name + ": read '" + operation + "' as an operation");
                        counted = false;
                    }
                }

                // The other silent way to print back perfectly and say nothing: text nobody took
                // into a node ends up in the white space in front of the next one.
                String swallowed = swallowedText(cu);
                if (swallowed != null) {
                    failures.add(name + ": left '" + swallowed + "' in the white space");
                    counted = false;
                }
                if (counted) {
                    read++;
                }

                for (ControlSection section : new ControlSection.Matcher().lower(cu).collect(Collectors.toList())) {
                    sections++;
                    if (section.isDummy()) {
                        dummySections++;
                    }
                    fields += section.getFields().size();
                }
                copies += new Copy.Matcher().lower(cu).collect(Collectors.toList()).size();
                macroCalls += new MacroCall.Matcher().lower(cu).collect(Collectors.toList()).size();
                calls += new Call.Matcher().lower(cu).collect(Collectors.toList()).size();
                dliCalls += new DliCall.Matcher().lower(cu).collect(Collectors.toList()).size();
                dataControlBlocks += new DataControlBlock.Matcher().lower(cu).collect(Collectors.toList()).size();
                entryPoints += new EntryPoint.Matcher().lower(cu).collect(Collectors.toList()).size();
                macros += new MacroDefinition.Matcher().lower(cu).collect(Collectors.toList()).size();
            }
            System.out.printf("  %-40s %3d of %3d%n", repository.getFileName(), read, files.size());
        }
        assertThat(members).as("no assembler member found under %s", corpus).isPositive();

        System.out.printf("assembler corpus: %d members, %d control sections of which %d dummy, " +
                          "%d constants, %d COPY, %d macro definitions, %d macro invocations, " +
                          "%d calls of which %d DL/I, %d DCBs, %d entry points%n",
          members, sections, dummySections, fields, copies, macros, macroCalls, calls, dliCalls,
          dataControlBlocks, entryPoints);
        if (!failures.isEmpty()) {
            System.out.println("failures:");
            failures.forEach(failure -> System.out.println("  " + failure));
        }

        assertThat(failures).isEmpty();

        // Every member is required to read, so the fixture only has to be there: one the walk could
        // not see, a symbolic link say, would otherwise pass as an empty application.
        assertThat(fixtureFound).as("mainframe-fixtures under %s", corpus).isTrue();
    }

    /**
     * INTERLINKS 20.2: five {@code COPY} statements over three copy members, and fourteen invocations
     * of the two macros the shop wrote. Which of the macros are the shop's is the library's answer —
     * {@code CALL}, {@code DCB}, {@code OPEN}, {@code GET}, {@code PUT}, {@code CLOSE} and {@code WTO}
     * come out of {@code SYS1.MACLIB}, which is not in the repository at all.
     */
    @Test
    void readsWhatTheFixtureReachesInItsMacroLibrary() throws IOException {
        List<String> library = new ArrayList<>();
        for (Path member : Corpus.assemblerMembers(fixture("maclib"))) {
            library.add(memberName(member));
        }
        assertThat(library).containsExactly("CLMPCBD", "CLMRECD", "CLMREGS", "CLMRTRN", "CLMSAVE");

        // Two of the five are macros and three are copy members, and only the prototype says which:
        // CLMSAVE takes a base register and the keywords SAVE= and ID=, CLMRTRN a return code and REG=.
        List<MacroDefinition> defined = new ArrayList<>();
        for (Assembler.CompilationUnit member : members(fixture("maclib"))) {
            new MacroDefinition.Matcher().lower(member).forEach(defined::add);
        }
        assertThat(defined).extracting(MacroDefinition::getName, MacroDefinition::getLabelParameter,
            MacroDefinition::getPositionalParameters,
            macro -> new ArrayList<>(macro.getKeywordParameters().keySet()))
          .containsExactly(
            tuple("CLMRTRN", "NAME", List.of("RC"), List.of("REG")),
            tuple("CLMSAVE", "NAME", List.of("BASE"), List.of("SAVE", "ID")));

        Map<String, Integer> copied = new TreeMap<>();
        Map<String, Integer> invoked = new TreeMap<>();
        List<String> statements = new ArrayList<>();
        for (Map.Entry<String, Assembler.CompilationUnit> program : programs().entrySet()) {
            for (Copy copy : new Copy.Matcher().lower(program.getValue()).collect(Collectors.toList())) {
                copied.merge(copy.getMember(), 1, Integer::sum);
                statements.add(program.getKey() + " COPY " + copy.getMember());
            }
            for (MacroCall macro : new MacroCall.Matcher().lower(program.getValue()).collect(Collectors.toList())) {
                if (macro.isDefinedBy(library)) {
                    invoked.merge(macro.getName(), 1, Integer::sum);
                }
            }
        }

        assertThat(statements).containsExactly(
          "CLMA010 COPY CLMREGS",
          "CLMA010 COPY CLMPCBD",
          "CLMU030 COPY CLMREGS",
          "CLMU040 COPY CLMREGS",
          "CLMU040 COPY CLMRECD");
        assertThat(copied).containsExactly(entry("CLMPCBD", 1), entry("CLMRECD", 1), entry("CLMREGS", 3));
        // A CLMRTRN count is also the count of a program's exits: three in CLMU030, four in each of
        // the others.
        assertThat(invoked).containsExactly(entry("CLMRTRN", 11), entry("CLMSAVE", 3));
    }

    /**
     * INTERLINKS 20.3. Five {@code CALL} statements reach across the application and three of them are
     * written in assembler; the other two are the COBOL callers, which this reader never sees.
     */
    @Test
    void readsEveryCallTheFixtureWritesInAssembler() throws IOException {
        List<String> calls = new ArrayList<>();
        for (Map.Entry<String, Assembler.CompilationUnit> program : programs().entrySet()) {
            for (Call call : new Call.Matcher().lower(program.getValue()).collect(Collectors.toList())) {
                calls.add(program.getKey() + " " + call.getKind() + " " + call.getTarget());
            }
        }
        assertThat(calls).containsExactly(
          "CLMA010 CALL_MACRO ASMTDLI",
          "CLMA010 CALL_MACRO ASMTDLI",
          "CLMU040 CALL_MACRO CLMU030");
    }

    /**
     * INTERLINKS 20.4. The DSECT and the copybook describe the same bytes, so the layout is what one
     * can be checked against the other with: nineteen rows over three hundred bytes for the claim
     * master record, fifty for the database PCB mask and sixty five for the root segment.
     */
    @Test
    void readsTheFixtureLayoutsAsItsOwnDocumentationDescribesThem() throws IOException {
        Map<String, ControlSection> sections = new LinkedHashMap<>();
        for (Assembler.CompilationUnit member : members(fixture("maclib"))) {
            new ControlSection.Matcher().lower(member)
              .forEach(section -> sections.put(section.getName(), section));
        }
        for (Assembler.CompilationUnit member : programs().values()) {
            new ControlSection.Matcher().lower(member)
              .forEach(section -> sections.put(section.getName(), section));
        }

        assertThat(sections.values()).extracting(ControlSection::getName, ControlSection::getKind,
            ControlSection::getLength, ControlSection::getLengthSymbol,
            section -> section.getFields().size())
          .containsExactly(
            tuple("CLMPCBD", ControlSection.Kind.DSECT, 50, "PCBDLEN", 9),
            tuple("CLMRECD", ControlSection.Kind.DSECT, 300, "CLMRLEN", 19),
            tuple("CLMROOTD", ControlSection.Kind.DSECT, 65, "ROOTLEN", 9));

        // Row for row against section 20.4's table, which is the join to cpy/CLMREC.
        assertThat(sections.get("CLMRECD").getFields())
          .extracting(ControlSection.Field::getOffset, ControlSection.Field::getBytes,
            ControlSection.Field::getName)
          .containsExactly(
            tuple(0, 10, "CLMRKEY"),
            tuple(0, 10, "CLMRCLM"),
            tuple(10, 12, "CLMRPOL"),
            tuple(22, 30, "CLMRNAM"),
            tuple(52, 8, "CLMRLOS"),
            tuple(52, 4, "CLMRLYR"),
            tuple(56, 2, "CLMRLMO"),
            tuple(58, 2, "CLMRLDY"),
            tuple(60, 8, "CLMRRPT"),
            tuple(68, 4, "CLMRTYP"),
            tuple(72, 1, "CLMRSTA"),
            tuple(73, 7, "CLMRAMC"),
            tuple(80, 7, "CLMRAMR"),
            tuple(87, 7, "CLMRAMP"),
            tuple(94, 8, "CLMRADJ"),
            tuple(102, 8, "CLMRUPD"),
            tuple(110, 8, "CLMRUSR"),
            tuple(118, 60, "CLMRDSC"),
            tuple(178, 122, "CLMRFIL"));

        // The key of the DEFINE CLUSTER, which is the ten bytes at offset zero.
        ControlSection.Field key = sections.get("CLMRECD").getFields().get(1);
        assertThat(key.getOffset()).isZero();
        assertThat(key.getBytes()).isEqualTo(10);

        assertThat(sections.get("CLMPCBD").getFields())
          .extracting(ControlSection.Field::getName, ControlSection.Field::getOffset,
            ControlSection.Field::getType)
          .containsExactly(
            tuple("PCBDBD", 0, "CL8"),
            tuple("PCBLEV", 8, "CL2"),
            tuple("PCBSTAT", 10, "CL2"),
            tuple("PCBPROC", 12, "CL4"),
            tuple("PCBRSV", 16, "F"),
            tuple("PCBSEGN", 20, "CL8"),
            tuple("PCBKFBL", 28, "F"),
            tuple("PCBNSEG", 32, "F"),
            tuple("PCBKFB", 36, "CL14"));
    }

    /**
     * INTERLINKS 20.5 and 20.6. {@code CLMA010} is the one DL/I program written in assembler, and what
     * it asks IMS for is written nowhere on the call: the function comes from the constant the first
     * argument names, and the segment from the first eight bytes of the SSA's.
     */
    @Test
    void readsTheFixtureDliCallsAndDdNames() throws IOException {
        Assembler.CompilationUnit purge = programs().get("CLMA010");
        assertThat(purge).isNotNull();

        assertThat(new DliCall.Matcher().lower(purge).collect(Collectors.toList()))
          .extracting(DliCall::getIface, DliCall::getFunction, DliCall::getPcbRegister,
            DliCall::getIoArea, DliCall::getSegments, DliCall::getLine)
          .containsExactly(
            tuple("ASMTDLI", "GHN", "R11", "A10ROOT", List.of("CLMROOT"), 50),
            // The delete passes no SSA: it deletes the root the get held, and the CLMDETL under it by
            // cascade. That the segment is CLMROOT is the reader of the pair's answer, not this one's.
            tuple("ASMTDLI", "DLET", "R11", "A10ROOT", List.of(), 58));

        assertThat(new DataControlBlock.Matcher().lower(purge).collect(Collectors.toList()))
          .extracting(DataControlBlock::getName, DataControlBlock::getDdName,
            DataControlBlock::getRecordLength)
          .containsExactly(
            tuple("A10CRD", "PURGCARD", 80),
            tuple("A10RPT", "PURGRPT", 133));

        // CLMU030 and CLMU040 open nothing at all: a subroutine reads what its caller hands it.
        for (Map.Entry<String, Assembler.CompilationUnit> program : programs().entrySet()) {
            if (!"CLMA010".equals(program.getKey())) {
                assertThat(new DataControlBlock.Matcher().lower(program.getValue())
                  .collect(Collectors.toList())).as("%s", program.getKey()).isEmpty();
            }
        }
    }

    /**
     * The name a caller writes. None of the three programs has a {@code CSECT} statement — the entry
     * macro generates one from the label it is invoked with — so the {@code END} is where the module's
     * name is written down.
     */
    @Test
    void readsTheNameEachFixtureProgramOffersItsCallers() throws IOException {
        List<String> offered = new ArrayList<>();
        for (Map.Entry<String, Assembler.CompilationUnit> program : programs().entrySet()) {
            new EntryPoint.Matcher().lower(program.getValue())
              .forEach(entry -> offered.add(program.getKey() + " " + entry.getKind() + " " +
                                            String.join(",", entry.getNames())));
        }
        assertThat(offered).containsExactly(
          "CLMA010 END CLMA010",
          "CLMU030 END CLMU030",
          "CLMU040 END CLMU040");
    }

    private static Map<String, Assembler.CompilationUnit> programs() throws IOException {
        Map<String, Assembler.CompilationUnit> programs = new LinkedHashMap<>();
        Path directory = fixture("asm");
        for (Path member : Corpus.assemblerMembers(directory)) {
            programs.put(memberName(member), parse(directory, member));
        }
        return programs;
    }

    private static List<Assembler.CompilationUnit> members(Path directory) throws IOException {
        List<Assembler.CompilationUnit> members = new ArrayList<>();
        for (Path member : Corpus.assemblerMembers(directory)) {
            members.add(parse(directory, member));
        }
        return members;
    }

    private static Assembler.CompilationUnit parse(Path directory, Path member) {
        List<SourceFile> parsed = AssemblerParser.builder().build()
          .parseInputs(Corpus.inputs(singletonList(member)), directory, new InMemoryExecutionContext())
          .collect(Collectors.toList());
        assertThat(parsed).singleElement().isInstanceOf(Assembler.CompilationUnit.class);
        return (Assembler.CompilationUnit) parsed.get(0);
    }

    private static Path fixture(String library) {
        Path directory = Paths.get(System.getenv("ASM_CORPUS"))
          .resolve("mainframe-fixtures/claims").resolve(library);
        assertThat(Files.isDirectory(directory)).as("%s", directory).isTrue();
        return directory;
    }

    private static String memberName(Path member) {
        String name = member.getFileName().toString();
        return name.contains(".") ? name.substring(0, name.indexOf('.')) : name;
    }

    /**
     * How many times the source writes each of the operations the traits are read from, counted the
     * way a person reading the columns would: a comment says nothing, a continuation line has no
     * operation of its own, and the operation is the field after the name.
     */
    private static Map<String, Integer> operationCounts(String source) {
        Map<String, Integer> counts = new TreeMap<>();
        for (String operation : new String[]{"CSECT", "DSECT", "RSECT", "COPY", "MACRO", "MEND",
          "CALL", "DCB", "ENTRY", "EXTRN", "WXTRN"}) {
            counts.put(operation, 0);
        }
        boolean continued = false;
        for (String line : source.split("\n", -1)) {
            String body = line.length() > 71 ? line.substring(0, 71) : line;
            boolean continues = line.length() > 71 && line.charAt(71) != ' ';
            if (body.startsWith("*") || body.startsWith(".*")) {
                continued = continues;
                continue;
            }
            if (body.trim().isEmpty()) {
                continued = false;
                continue;
            }
            if (continued) {
                continued = continues;
                continue;
            }
            continued = continues;
            String[] words = body.trim().split("\\s+");
            int operation = body.charAt(0) == ' ' ? 0 : 1;
            if (words.length > operation) {
                counts.computeIfPresent(words[operation].toUpperCase(Locale.ROOT),
                  (name, count) -> count + 1);
            }
        }
        return counts;
    }

    /**
     * The first white space of the member holding something that is not white space, or null when
     * every character of it was taken into a node of its own.
     */
    private static @Nullable String swallowedText(Assembler.CompilationUnit cu) {
        AtomicReference<String> swallowed = new AtomicReference<>();
        new AssemblerIsoVisitor<Integer>() {
            @Override
            public Space visitSpace(Space space, Space.Location location, Integer p) {
                if (!space.getWhitespace().trim().isEmpty()) {
                    swallowed.compareAndSet(null, space.getWhitespace().trim());
                }
                return space;
            }
        }.visit(cu, 0);
        return swallowed.get();
    }

    private static int countOperation(Assembler.CompilationUnit cu, String operation) {
        int count = 0;
        for (Assembler.Instruction instruction : instructionsIn(cu)) {
            if (instruction.isOperation(operation)) {
                count++;
            }
        }
        return count;
    }

    private static List<Assembler.Instruction> instructionsIn(Assembler.CompilationUnit cu) {
        return cu.getStatements().stream()
          .filter(Assembler.Instruction.class::isInstance)
          .map(Assembler.Instruction.class::cast)
          .collect(Collectors.toList());
    }
}
