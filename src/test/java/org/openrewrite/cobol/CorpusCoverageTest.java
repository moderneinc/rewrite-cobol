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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Parser;
import org.openrewrite.ParseExceptionResult;
import org.openrewrite.SourceFile;
import org.openrewrite.tree.ParseError;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Parses a corpus of real COBOL and reports how much of it the parser can actually read.
 * <p>
 * The NIST suite is ANSI conformance code: no CICS, no IMS, no JCL. This is the first real online
 * and batch code the parser has seen, so its purpose is to find what is broken, not to pass.
 * <p>
 * Run with { COBOL_CORPUS=/path/to/corpus}.
 */
class CorpusCoverageTest {

    @EnabledIfEnvironmentVariable(named = "COBOL_CORPUS", matches = ".+")
    @Test
    void measure() throws IOException {
        Path root = Paths.get(System.getenv("COBOL_CORPUS"));

        List<Parser.Input> copybookInputs = inputs(root, ".cpy");
        List<SourceFile> copybooks = CopybookParser.builder().build()
          .parseInputs(copybookInputs, root, new InMemoryExecutionContext())
          .collect(Collectors.toList());
        System.out.printf("copybooks: %d parsed, %d errors%n",
          copybooks.size() - errors(copybooks).size(), errors(copybooks).size());

        List<Parser.Input> programInputs = inputs(root, ".cbl");
        List<SourceFile> programs = CobolParser.builder().copybooks(copybooks).build()
          .parseInputs(programInputs, root, new InMemoryExecutionContext())
          .collect(Collectors.toList());

        List<SourceFile> failed = errors(programs);
        System.out.printf("%nCOBOL: %d of %d parsed (%.0f%%), %d failed%n",
          programs.size() - failed.size(), programs.size(),
          100.0 * (programs.size() - failed.size()) / Math.max(programs.size(), 1), failed.size());

        // Group by the syntax message so a single grammar gap does not look like fifty problems.
        Map<String, List<String>> byCause = new LinkedHashMap<>();
        for (SourceFile error : failed) {
            String message = error.getMarkers().findFirst(ParseExceptionResult.class)
              .map(ParseExceptionResult::getMessage)
              .orElse("unknown");
            byCause.computeIfAbsent(normalize(message), k -> new ArrayList<>())
              .add(error.getSourcePath().getFileName().toString());
        }

        System.out.println("\nfailures grouped by cause:");
        byCause.entrySet().stream()
          .sorted((a, b) -> b.getValue().size() - a.getValue().size())
          .forEach(e -> System.out.printf("  [%d] %s%n      e.g. %s%n",
            e.getValue().size(), e.getKey(),
            e.getValue().stream().limit(4).collect(Collectors.joining(", "))));

        // Parsing a program is only half of it: whatever preprocessing takes out of the text the grammar sees has to
        // come back on the way out, byte for byte.
        List<String> notPrintedBack = new ArrayList<>();
        for (SourceFile program : programs) {
            if (program instanceof ParseError) {
                continue;
            }
            String original = new String(Files.readAllBytes(root.resolve(program.getSourcePath())), program.getCharset());
            if (!original.equals(program.printAll())) {
                notPrintedBack.add(program.getSourcePath().getFileName().toString());
            }
        }
        System.out.printf("%nround trip: %d of %d printed back unchanged%n",
          programs.size() - failed.size() - notPrintedBack.size(), programs.size() - failed.size());

        // Coverage is reported rather than asserted, because what the parser cannot yet read is the point of the
        // measurement. Losing the text of a program it did read is a different thing, and is never acceptable.
        assertThat(notPrintedBack).isEmpty();
    }

    /**
     * Strips the file and position so the same grammar gap in fifty programs groups as one cause.
     */
    private static String normalize(String message) {
        String firstLine = message.split("\n", 2)[0];
        return firstLine
          // The offending token is the point; the file, position and the expecting-set are noise.
          .replaceAll("in \\S+ at line \\d+:\\d+", "at <position>")
          .replaceAll("expecting \\{[^}]*\\}", "expecting {…}")
          .replaceAll("expecting \\S+$", "expecting {…}")
          .trim();
    }

    private static List<SourceFile> errors(List<SourceFile> parsed) {
        return parsed.stream().filter(s -> s instanceof ParseError).collect(Collectors.toList());
    }

    private static List<Parser.Input> inputs(Path root, String extension) throws IOException {
        try (Stream<Path> paths = Files.walk(root)) {
            return paths
              .filter(p -> p.toString().toLowerCase().endsWith(extension))
              .sorted()
              .map(p -> new Parser.Input(p, () -> {
                  try {
                      return Files.newInputStream(p);
                  } catch (IOException e) {
                      throw new UncheckedIOException(e);
                  }
              }))
              .collect(Collectors.toList());
        }
    }
}
