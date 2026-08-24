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

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.ParseExceptionResult;
import org.openrewrite.Parser;
import org.openrewrite.SourceFile;
import org.openrewrite.cobol.Corpus;
import org.openrewrite.jcl.JclParser;
import org.openrewrite.jcl.tree.Jcl;
import org.openrewrite.jcl.tree.Statement;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reads the JCL of real applications and reports how much of it the model actually understood, the
 * same way {@code CorpusCoverageTest} does for COBOL. Gated on {@code JCL_CORPUS} pointing at a
 * checkout, because the corpus is not redistributed with this repository.
 * <p>
 * A member counts as read when the traits find exactly the EXEC cards it has, nothing in it fell to
 * {@link Jcl.Unknown}, and every job control statement has an operation that is a JCL statement
 * rather than a fragment of the line before it. That coverage is reported per application rather
 * than asserted, because what the parser cannot yet read is the point of the measurement. Printing
 * back byte for byte is asserted: losing the text of a member the parser did read is never
 * acceptable.
 */
@EnabledIfEnvironmentVariable(named = "JCL_CORPUS", matches = ".+")
class JclCorpusTest {

    @Test
    void readsRealJcl() throws IOException {
        Path corpus = Paths.get(System.getenv("JCL_CORPUS"));

        int members = 0;
        int steps = 0;
        int programs = 0;
        int dds = 0;
        int dataSets = 0;
        int concatenations = 0;
        List<String> notPrintedBack = new ArrayList<>();
        List<String> notRead = new ArrayList<>();

        System.out.println("members read, by application:");
        for (Path repository : Corpus.repositories(corpus)) {
            List<Path> jobs = Corpus.jobs(repository);
            if (jobs.isEmpty()) {
                continue;
            }
            int read = 0;
            for (Path member : jobs) {
                members++;
                String name = corpus.relativize(member).toString();
                String source = new String(Files.readAllBytes(member));
                // Parsed by path rather than from the string: a member named .jcl that holds no JCL
                // is refused by name, and a string has no name.
                List<SourceFile> parsed = JclParser.builder().build()
                        .parseInputs(singletonList(new Parser.Input(member, () -> new ByteArrayInputStream(source.getBytes()))),
                                corpus, new InMemoryExecutionContext())
                        .collect(Collectors.toList());
                if (parsed.isEmpty() || !(parsed.get(0) instanceof Jcl.CompilationUnit)) {
                    notRead.add(name + ": " + cause(parsed));
                    continue;
                }
                Jcl.CompilationUnit cu = (Jcl.CompilationUnit) parsed.get(0);
                if (!source.equals(cu.printAll())) {
                    notPrintedBack.add(name);
                    continue;
                }

                String misread = misread(cu, source);
                if (misread != null) {
                    notRead.add(name + ": " + misread);
                    continue;
                }
                read++;

                for (Step step : new Step.Matcher().lower(cu).collect(Collectors.toList())) {
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
            }
            System.out.printf("  %-40s %3d of %3d%n", repository.getFileName(), read, jobs.size());
        }
        assertThat(members).as("no JCL found under %s", corpus).isPositive();

        System.out.printf("JCL: %d members, %d steps (%d naming a program), %d DD, %d data sets, " +
                        "%d concatenations%n",
                members, steps, programs, dds, dataSets, concatenations);
        if (!notRead.isEmpty()) {
            System.out.println("not read:");
            notRead.forEach(f -> System.out.println("  " + f));
        }
        if (!notPrintedBack.isEmpty()) {
            System.out.println("not printed back:");
            notPrintedBack.forEach(f -> System.out.println("  " + f));
        }

        assertThat(notPrintedBack).isEmpty();
        assertThat(steps).isPositive();
        assertThat(dataSets).isPositive();
    }

    /**
     * Why a member that parsed does not count as read, or null when it does. The traits must find
     * exactly the EXEC cards the source has; counting them independently is the only thing that
     * turns "it ran without complaining" into evidence that the file was read correctly.
     */
    private static @Nullable String misread(Jcl.CompilationUnit cu, String source) {
        int written = countExecCards(source);
        long read = new Step.Matcher().lower(cu).count();
        if (written != read) {
            return read + " steps read, " + written + " EXEC cards written";
        }
        for (Statement statement : cu.getStatements()) {
            if (statement instanceof Jcl.Unknown) {
                return "could not place '" + ((Jcl.Unknown) statement).getWord().getText() + "'";
            }
            if (statement instanceof Jcl.JobControlStatement) {
                Jcl.JobControlStatement jcl = (Jcl.JobControlStatement) statement;
                String operation = jcl.getOperation() == null ? "" : jcl.getOperation().getText();
                if (!operation.matches("[A-Za-z]+")) {
                    return "read '" + operation + "' as an operation";
                }
            }
        }
        return null;
    }

    private static String cause(List<SourceFile> parsed) {
        return parsed.isEmpty() ? "did not parse" : parsed.get(0).getMarkers()
                .findFirst(ParseExceptionResult.class)
                .map(e -> e.getMessage().split("\n", 2)[0])
                .orElse("did not parse");
    }

    private static final Pattern EXEC_CARD =
            Pattern.compile("^//[^*\\s]*\\s+EXEC(\\s|$)", Pattern.MULTILINE);

    private static int countExecCards(String source) {
        Matcher matcher = EXEC_CARD.matcher(source);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }
}
