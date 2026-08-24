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
package org.openrewrite.bms.trait;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.SourceFile;
import org.openrewrite.bms.BmsParser;
import org.openrewrite.bms.tree.Bms;
import org.openrewrite.cobol.Corpus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reads the BMS of real applications and reports what the traits found, the same way
 * {@code JclCorpusTest} does for JCL. Gated on {@code BMS_CORPUS} pointing at a checkout, because
 * the corpus is not redistributed with this repository.
 * <p>
 * Printing back byte for byte is the assertion that matters here. A map set is assembler source with
 * meaning in its columns, and a parser that quietly loses a blank has changed what the screen looks
 * like.
 */
@EnabledIfEnvironmentVariable(named = "BMS_CORPUS", matches = ".+")
class BmsCorpusTest {

    @Test
    void readsRealMapsets() throws IOException {
        Path corpus = Paths.get(System.getenv("BMS_CORPUS"));

        int members = 0;
        int mapsets = 0;
        int maps = 0;
        int fields = 0;
        int named = 0;
        int inputs = 0;
        int positioned = 0;
        int written = 0;
        List<String> failures = new ArrayList<>();

        System.out.println("map sets read, by application:");
        for (Path repository : Corpus.repositories(corpus)) {
            List<Path> files = Corpus.mapsets(repository);
            if (files.isEmpty()) {
                continue;
            }
            int read = 0;
            for (Path member : files) {
                members++;
                String name = corpus.relativize(member).toString();
                String source = new String(Files.readAllBytes(member));
                List<SourceFile> parsed = BmsParser.builder().build()
                        .parse(new InMemoryExecutionContext(), source)
                        .collect(Collectors.toList());
                if (parsed.isEmpty() || !(parsed.get(0) instanceof Bms.CompilationUnit)) {
                    failures.add(name + ": did not parse");
                    continue;
                }
                Bms.CompilationUnit cu = (Bms.CompilationUnit) parsed.get(0);

                if (!source.equals(cu.printAll())) {
                    failures.add(name + ": did not print back");
                    continue;
                }

                // The traits must find exactly the macros the source has. Counting them independently
                // is the only thing that turns "it ran without complaining" into evidence that the
                // file was read correctly.
                boolean counted = true;
                for (String macro : new String[]{"DFHMSD", "DFHMDI", "DFHMDF"}) {
                    int inSource = countMacro(source, macro);
                    int inTree = countOperation(cu, macro);
                    if (inSource != inTree) {
                        failures.add(name + ": " + inTree + " " + macro + " read, " + inSource + " written");
                        counted = false;
                    }
                    if ("DFHMDF".equals(macro)) {
                        written += inSource;
                    }
                }

                // An operand read as an operation is what a mishandled continuation looks like, and
                // it is silent: the statement still prints back, it just says something else.
                for (Bms.MacroStatement statement : statementsIn(cu)) {
                    if (statement.getOperation().getText().contains("=")) {
                        failures.add(name + ": read '" +
                                statement.getOperation().getText() + "' as an operation");
                        counted = false;
                    }
                }
                if (counted) {
                    read++;
                }

                List<Mapset> mapsetsRead = new Mapset.Matcher().lower(cu).collect(Collectors.toList());
                mapsets += mapsetsRead.size();
                for (Mapset mapset : mapsetsRead) {
                    maps += mapset.getMaps().size();
                    for (MapDefinition map : mapset.getMaps()) {
                        for (Field field : map.getFields()) {
                            fields++;
                            if (field.getName() != null) {
                                named++;
                            }
                            if (field.isInput()) {
                                inputs++;
                            }
                            if (field.getPosition() != null) {
                                positioned++;
                            }
                        }
                    }
                }
            }
            System.out.printf("  %-40s %3d of %3d%n", repository.getFileName(), read, files.size());
        }
        assertThat(members).as("no .bms found under %s", corpus).isPositive();

        System.out.printf("BMS corpus: %d files, %d mapsets, %d maps, %d fields " +
                        "(%d named, %d input, %d positioned)%n",
                members, mapsets, maps, fields, named, inputs, positioned);
        if (!failures.isEmpty()) {
            System.out.println("failures:");
            failures.forEach(f -> System.out.println("  " + f));
        }

        assertThat(failures).isEmpty();
        assertThat(mapsets).as("no mapsets read from %d files", members).isPositive();
        assertThat(maps).isPositive();

        // Every field is reachable from the map it belongs to. Containment is read from position
        // rather than from brackets, so a field the walk cannot reach is one no report would find.
        assertThat(fields).as("fields reachable through their map").isEqualTo(written);

        // Every field says where it goes. A field without a position is one this model failed to
        // read, not one the screen leaves unplaced.
        assertThat(positioned).isEqualTo(fields);

        // A screen nobody can type into is a screen nobody uses, so a corpus of real applications
        // reporting no inputs would mean the attributes are not being read.
        assertThat(inputs).isPositive();
    }

    private static int countMacro(String source, String macro) {
        int count = 0;
        for (String line : source.split("\n", -1)) {
            if (line.startsWith("*") || line.length() < 10) {
                continue;
            }
            // The operation is the second field, so a mapset name or a literal holding the macro's
            // name does not count.
            String[] words = line.trim().split("\\s+");
            int operation = line.charAt(0) == ' ' ? 0 : 1;
            if (words.length > operation && words[operation].equalsIgnoreCase(macro)) {
                count++;
            }
        }
        return count;
    }

    private static int countOperation(Bms.CompilationUnit cu, String macro) {
        int count = 0;
        for (Bms.MacroStatement statement : statementsIn(cu)) {
            if (statement.isOperation(macro)) {
                count++;
            }
        }
        return count;
    }

    private static List<Bms.MacroStatement> statementsIn(Bms.CompilationUnit cu) {
        return cu.getStatements().stream()
                .filter(Bms.MacroStatement.class::isInstance)
                .map(Bms.MacroStatement.class::cast)
                .collect(Collectors.toList());
    }
}
