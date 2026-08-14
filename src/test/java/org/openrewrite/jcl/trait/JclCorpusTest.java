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
package org.openrewrite.jcl.trait;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.SourceFile;
import org.openrewrite.jcl.JclParser;
import org.openrewrite.jcl.tree.Jcl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reads the JCL of a real application and reports what the traits found, the same way
 * {@code CorpusCoverageTest} does for COBOL. Gated on {@code JCL_CORPUS} pointing at a checkout,
 * because the corpus is not redistributed with this repository.
 * <p>
 * Assertions here are deliberately weak. The point is not that any particular job parses a
 * particular way, but that the model does not fall over on real JCL and that the counts are of the
 * right order — a model reporting no data sets across a hundred members is broken whatever its unit
 * tests say.
 */
@EnabledIfEnvironmentVariable(named = "JCL_CORPUS", matches = ".+")
class JclCorpusTest {

    @Test
    void readsRealJcl() throws IOException {
        Path corpus = Paths.get(System.getenv("JCL_CORPUS"));
        List<Path> members = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(corpus)) {
            walk.filter(Files::isRegularFile)
                    .filter(p -> p.toString().toLowerCase().endsWith(".jcl"))
                    .forEach(members::add);
        }
        assertThat(members).as("no .jcl found under %s", corpus).isNotEmpty();

        int steps = 0;
        int programs = 0;
        int dds = 0;
        int dataSets = 0;
        int concatenations = 0;
        List<String> failures = new ArrayList<>();

        for (Path member : members) {
            try {
                String source = new String(Files.readAllBytes(member));
                List<SourceFile> parsed = JclParser.builder().build()
                        .parse(new InMemoryExecutionContext(), source)
                        .collect(java.util.stream.Collectors.toList());
                if (parsed.isEmpty() || !(parsed.get(0) instanceof Jcl.CompilationUnit)) {
                    failures.add(member.getFileName() + ": did not parse");
                    continue;
                }
                List<Step> read = new Step.Matcher().lower((Jcl.CompilationUnit) parsed.get(0))
                        .collect(Collectors.toList());

                // The traits must find exactly the EXEC cards the source has. Counting them
                // independently is the only thing that turns "it ran without complaining" into
                // evidence that the file was read correctly.
                int written = countExecCards(source);
                if (written != read.size()) {
                    failures.add(member.getFileName() + ": " + read.size() +
                            " steps read, " + written + " EXEC cards written");
                }

                for (Step step : read) {
                    steps++;
                    if (step.getProgram() != null) {
                        programs++;
                    }
                    for (DataDefinition dd : step.getDataDefinitions()) {
                        dds++;
                        dataSets += dd.getDataSets().size();
                        if (dd.getDataSets().size() > 1) {
                            concatenations++;
                        }
                    }
                }
            } catch (Exception e) {
                failures.add(member.getFileName() + ": " + e.getClass().getSimpleName());
            }
        }

        System.out.printf("JCL: %d members, %d steps (%d naming a program), %d DD, %d data sets, " +
                        "%d concatenations%n",
                members.size(), steps, programs, dds, dataSets, concatenations);
        if (!failures.isEmpty()) {
            System.out.println("failures:");
            failures.forEach(f -> System.out.println("  " + f));
        }

        assertThat(failures).isEmpty();
        assertThat(steps).isPositive();
        assertThat(dataSets).isPositive();
    }

    private static final java.util.regex.Pattern EXEC_CARD =
            java.util.regex.Pattern.compile("^//[^*\\s]*\\s+EXEC(\\s|$)", java.util.regex.Pattern.MULTILINE);

    private static int countExecCards(String source) {
        java.util.regex.Matcher matcher = EXEC_CARD.matcher(source);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }
}
