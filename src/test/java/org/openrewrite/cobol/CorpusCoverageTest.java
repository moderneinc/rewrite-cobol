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
import org.jspecify.annotations.Nullable;
import org.openrewrite.SourceFile;
import org.openrewrite.Tree;
import org.openrewrite.cobol.marker.CopiedWord;
import org.openrewrite.cobol.marker.ElidedExec;
import org.openrewrite.cobol.trait.CopybookReference;
import org.openrewrite.cobol.tree.Cobol;
import org.openrewrite.cobol.tree.Statement;
import org.openrewrite.marker.Range;
import org.openrewrite.tree.ParseError;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Parses a corpus of real COBOL and reports how much of it the parser can actually read.
 * <p>
 * The NIST suite is ANSI conformance code: no CICS, no IMS, no JCL. This is the first real online
 * and batch code the parser has seen, so its purpose is to find what is broken, not to pass. The one
 * application it must pass is the fixture, which is written so that all of it parses.
 * <p>
 * Run with { COBOL_CORPUS=/path/to/corpus}.
 */
class CorpusCoverageTest {

    @EnabledIfEnvironmentVariable(named = "COBOL_CORPUS", matches = ".+")
    @Test
    void measure() throws IOException {
        Path root = Paths.get(System.getenv("COBOL_CORPUS"));

        // Each application is parsed against its own copybook library, and reported on its own: a
        // number for the corpus as a whole says nothing about which application the parser cannot read.
        List<SourceFile> programs = new ArrayList<>();
        int copybooksParsed = 0;
        int copybookErrors = 0;
        List<String> fixtureNotRead = new ArrayList<>();
        boolean fixtureFound = false;
        System.out.println("programs parsed, copybooks not found, by application:");
        for (Path repository : Corpus.repositories(root)) {
            List<Parser.Input> programInputs = Corpus.inputs(Corpus.programs(repository));
            if (programInputs.isEmpty()) {
                continue;
            }
            List<SourceFile> copybooks = CopybookParser.builder().build()
              .parseInputs(Corpus.inputs(Corpus.copybooks(repository)), root, new InMemoryExecutionContext())
              .collect(Collectors.toList());
            copybooksParsed += copybooks.size() - errors(copybooks).size();
            copybookErrors += errors(copybooks).size();

            List<SourceFile> parsed = CobolParser.builder().copybooks(copybooks).build()
              .parseInputs(programInputs, root, new InMemoryExecutionContext())
              .collect(Collectors.toList());
            System.out.printf("  %-40s %3d of %3d parsed, %3d missing a copybook%n",
              repository.getFileName(), parsed.size() - errors(parsed).size(), parsed.size(),
              parsed.stream().filter(CorpusCoverageTest::missingACopybook).count());
            programs.addAll(parsed);
            if (Corpus.isFixture(repository)) {
                fixtureFound = true;
                fixtureNotRead.addAll(notRead(copybooks));
                fixtureNotRead.addAll(notRead(parsed));
            }
        }
        System.out.printf("copybooks: %d parsed, %d errors%n", copybooksParsed, copybookErrors);

        List<SourceFile> failed = errors(programs);
        System.out.printf("%nCOBOL: %d of %d parsed (%.0f%%), %d failed%n",
          programs.size() - failed.size(), programs.size(),
          100.0 * (programs.size() - failed.size()) / Math.max(programs.size(), 1), failed.size());

        // Group by the syntax message so a single grammar gap does not look like fifty problems.
        Map<String, List<String>> byCause = new LinkedHashMap<>();
        for (SourceFile error : failed) {
            byCause.computeIfAbsent(normalize(cause(error)), k -> new ArrayList<>())
              .add(error.getSourcePath().toString());
        }

        System.out.println("\nfailures grouped by cause:");
        byCause.entrySet().stream()
          .sorted((a, b) -> b.getValue().size() - a.getValue().size())
          .forEach(e -> {
              System.out.printf("  [%d] %s%n", e.getValue().size(), e.getKey());
              e.getValue().forEach(file -> System.out.printf("      %s%n", file));
          });

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

        // The fixture is the exception: all of it parses by construction. A fixture the walk could not see, a
        // symbolic link say, would otherwise pass as an empty application.
        assertThat(fixtureFound).as("mainframe-fixtures under %s", root).isTrue();
        assertThat(fixtureNotRead).isEmpty();

        // A position landing anywhere but on the word it names is worse than no position at all, so every word
        // the corpus prints is read back out of the source at the offset reported for it. Sequence numbers in
        // columns 1 to 6 and identification text in 73 to 80 are what a naive span would swallow, and the corpus
        // has both.
        List<String> misplaced = new ArrayList<>();
        List<String> unexplained = new ArrayList<>();
        int words = 0;
        int placedWords = 0;
        int statements = 0;
        int placedStatements = 0;
        for (SourceFile program : programs) {
            if (!(program instanceof Cobol.CompilationUnit)) {
                continue;
            }
            Cobol.CompilationUnit cu = (Cobol.CompilationUnit) program;
            SourcePositions positions = SourcePositions.of(cu);
            for (Cobol.Word word : wordsIn(cu)) {
                // A continued word is broken around the column areas it spans, so its position covers them
                // too, and a stand-in for an elided EXEC covers the block it stands for. Neither reads back
                // as its own text; the statement count below is what holds the EXEC blocks to their place.
                if (word.getContinuation() != null || word.getMarkers().findFirst(ElidedExec.class).isPresent()) {
                    continue;
                }
                words++;
                Range range = positions.get(word);
                if (range == null) {
                    // Copied in from a copybook, which prints there and not here. Anything else printing
                    // nothing is a gap, not a position that belongs elsewhere, so it is named rather than
                    // passed over.
                    if (!word.getWord().isEmpty() &&
                        !word.getMarkers().findFirst(CopiedWord.class).isPresent()) {
                        unexplained.add(cu.getSourcePath() + " " + word.getWord());
                    }
                    continue;
                }
                placedWords++;
                if (!positions.textOf(range).equals(word.getWord())) {
                    misplaced.add(cu.getSourcePath() + " line " + range.getStart().getLine() + ": expected " +
                                  word.getWord() + " but found " + positions.textOf(range));
                }
            }
            // A statement nothing can point at is one a catalog export has to leave out, so the ones without
            // a position have to be exactly the ones a copybook wrote.
            for (Statement statement : statementsIn(cu)) {
                statements++;
                if (positions.get(statement) != null) {
                    placedStatements++;
                } else if (!writtenByACopybook(statement)) {
                    unexplained.add(cu.getSourcePath() + " " + statement.getClass().getSimpleName());
                }
            }
        }
        System.out.printf("%npositions: %d of %d words placed, %d misplaced; %d of %d statements placed%n",
          placedWords, words, misplaced.size(), placedStatements, statements);
        assertThat(misplaced).isEmpty();
        assertThat(unexplained).isEmpty();
    }

    private static boolean writtenByACopybook(Cobol tree) {
        for (Cobol.Word word : wordsIn(tree)) {
            if (!word.getWord().isEmpty() && !word.getMarkers().findFirst(CopiedWord.class).isPresent()) {
                return false;
            }
        }
        return true;
    }

    private static List<Cobol.Word> wordsIn(Cobol cu) {
        List<Cobol.Word> words = new ArrayList<>();
        new CobolIsoVisitor<Integer>() {
            @Override
            public Cobol.Word visitWord(Cobol.Word word, Integer p) {
                words.add(word);
                return word;
            }
        }.visit(cu, 0);
        return words;
    }

    private static List<Statement> statementsIn(Cobol.CompilationUnit cu) {
        List<Statement> statements = new ArrayList<>();
        new CobolIsoVisitor<Integer>() {
            @Override
            public @Nullable Cobol visit(@Nullable Tree tree, Integer p) {
                if (tree instanceof Statement) {
                    statements.add((Statement) tree);
                }
                return super.visit(tree, p);
            }
        }.visit(cu, 0);
        return statements;
    }

    /**
     * Strips the file and position so the same grammar gap in fifty programs groups as one cause.
     */
    private static String normalize(String message) {
        String firstLine = message.split("\n", 2)[0];
        return firstLine
          // The offending token is the point; the file, position and the expecting-set are noise.
          .replaceAll("in .+? at line \\d+:\\d+", "at <position>")
          .replaceAll("expecting \\{[^}]*\\}", "expecting {…}")
          .replaceAll("expecting \\S+$", "expecting {…}")
          .trim();
    }

    private static List<SourceFile> errors(List<SourceFile> parsed) {
        return parsed.stream().filter(s -> s instanceof ParseError).collect(Collectors.toList());
    }

    private static String cause(SourceFile error) {
        return error.getMarkers().findFirst(ParseExceptionResult.class)
          .map(ParseExceptionResult::getMessage)
          .orElse("unknown");
    }

    /**
     * Each file that was not read, with what kept it from being: a syntax error, or a copybook not found.
     */
    private static List<String> notRead(List<SourceFile> parsed) {
        List<String> notRead = new ArrayList<>();
        for (SourceFile file : parsed) {
            if (file instanceof ParseError) {
                notRead.add(file.getSourcePath() + ": " + cause(file));
            } else if (missingACopybook(file)) {
                notRead.add(file.getSourcePath() + ": missing a copybook");
            }
        }
        return notRead;
    }

    // A program whose copybook was not found parses, and reads as though the fields it uses were
    // never declared, so it is counted apart from the ones the parser could not read.
    private static boolean missingACopybook(SourceFile program) {
        if (!(program instanceof Cobol.CompilationUnit)) {
            return false;
        }
        List<CopybookReference> unresolved = new ArrayList<>();
        new CopybookReference.Matcher().<Integer>asVisitor((reference, p) -> {
            if (!reference.isResolved()) {
                unresolved.add(reference);
            }
            return reference.getTree();
        }).visit(program, 0);
        return !unresolved.isEmpty();
    }
}
