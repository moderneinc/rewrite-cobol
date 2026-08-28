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
package org.openrewrite.mainframe.jcl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.SourceFile;
import org.openrewrite.mainframe.cobol.Corpus;
import org.openrewrite.mainframe.jcl.marker.GeneratedParmContent;
import org.openrewrite.mainframe.jcl.tree.Jcl;
import org.openrewrite.mainframe.jcl.tree.Statement;
import org.openrewrite.marker.Range;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reads every position back out of the source of real jobs. A position landing anywhere but on the
 * word it names is worse than no position at all, so each word is compared to the text at the offset
 * reported for it, and each statement has to be placed somewhere: in this member, or against the
 * {@code EXEC} or {@code INCLUDE} card that brought it in.
 * <p>
 * Gated on {@code JCL_CORPUS} pointing at a checkout, because the corpus is not redistributed with
 * this repository. Every JCL member of an application is offered as a procedure library member, so
 * the jobs are measured with their procedures resolved rather than as they were written.
 */
@EnabledIfEnvironmentVariable(named = "JCL_CORPUS", matches = ".+")
class SourcePositionsCorpusTest {

    @Test
    void placesEveryStatementOfTheCorpus() throws IOException {
        Path corpus = Paths.get(System.getenv("JCL_CORPUS"));

        int members = 0;
        int words = 0;
        int statements = 0;
        int placed = 0;
        int broughtIn = 0;
        List<String> misplaced = new ArrayList<>();
        List<String> unplaced = new ArrayList<>();

        for (Path repository : Corpus.repositories(corpus)) {
            List<Path> jobs = Corpus.jobs(repository);
            if (jobs.isEmpty()) {
                continue;
            }
            List<SourceFile> parsed = JclParser.builder().procedureLibrary(jobs).build()
              .parseInputs(Corpus.inputs(jobs), corpus, new InMemoryExecutionContext())
              .collect(Collectors.toList());
            for (SourceFile source : parsed) {
                if (!(source instanceof Jcl.CompilationUnit)) {
                    continue;
                }
                members++;
                Jcl.CompilationUnit cu = (Jcl.CompilationUnit) source;
                SourcePositions positions = SourcePositions.of(cu);

                List<Statement> written = new ArrayList<>();
                List<Statement> expanded = new ArrayList<>();
                gather(cu.getStatements(), false, written, expanded);

                for (Statement statement : written) {
                    statements++;
                    Range range = positions.get(statement);
                    if (range == null) {
                        if (printsSomething(statement)) {
                            unplaced.add(cu.getSourcePath() + " " + statement.getClass().getSimpleName());
                        }
                        continue;
                    }
                    placed++;
                    for (Jcl.Word word : wordsIn(statement)) {
                        if (word.getText().isEmpty()) {
                            continue;
                        }
                        words++;
                        Range at = positions.get(word);
                        if (at == null || !positions.textOf(at).equals(word.getText())) {
                            misplaced.add(cu.getSourcePath() + " line " + range.getStart().getLine() +
                                          ": expected " + word.getText() + " but found " +
                                          (at == null ? "no position" : positions.textOf(at)));
                        }
                    }
                }

                for (Statement statement : expanded) {
                    statements++;
                    SourcePositions.Expanded at = positions.expanded(statement);
                    if (at == null) {
                        if (printsSomething(statement)) {
                            unplaced.add(cu.getSourcePath() + " " + statement.getClass().getSimpleName() +
                                         " brought in by an expansion");
                        }
                        continue;
                    }
                    broughtIn++;
                    for (Jcl.Word word : wordsIn(statement)) {
                        if (word.getText().isEmpty()) {
                            continue;
                        }
                        words++;
                        SourcePositions.Expanded wordAt = positions.expanded(word);
                        if (wordAt == null || !wordAt.getText().equals(word.getText())) {
                            misplaced.add(cu.getSourcePath() + " " + at.getMemberName() + " line " +
                                          at.getRange().getStart().getLine() + ": expected " + word.getText() +
                                          " but found " + (wordAt == null ? "no position" : wordAt.getText()));
                        }
                    }
                }
            }
        }

        assertThat(members).as("no JCL found under %s", corpus).isPositive();
        System.out.printf("%npositions: %d members, %d of %d statements placed and %d anchored to the " +
                          "card that brought them in, %d words read back, %d misplaced%n",
          members, placed, statements, broughtIn, words, misplaced.size());
        if (!unplaced.isEmpty()) {
            System.out.println("not placed:");
            unplaced.forEach(f -> System.out.println("  " + f));
        }
        if (!misplaced.isEmpty()) {
            System.out.println("misplaced:");
            misplaced.subList(0, Math.min(50, misplaced.size())).forEach(f -> System.out.println("  " + f));
        }

        assertThat(words).isPositive();
        assertThat(broughtIn).as("no job in the corpus resolved a procedure").isPositive();
        assertThat(misplaced).isEmpty();
        assertThat(unplaced).isEmpty();
    }

    /**
     * The statements of the member itself, and the ones a procedure or INCLUDE member wrote.
     */
    private static void gather(List<Statement> statements, boolean expansion,
                               List<Statement> written, List<Statement> expanded) {
        for (Statement statement : statements) {
            if (statement instanceof Jcl.Expansion) {
                gather(((Jcl.Expansion) statement).getStatements(), true, written, expanded);
            } else {
                (expansion ? expanded : written).add(statement);
            }
        }
    }

    /**
     * Whether a statement writes anything at all. Content grafted in from an external member is not
     * the job's own source and prints nothing, so it has nowhere here to point at.
     */
    private static boolean printsSomething(Statement statement) {
        if (statement.getMarkers().findFirst(GeneratedParmContent.class).isPresent()) {
            return false;
        }
        for (Jcl.Word word : wordsIn(statement)) {
            if (!word.getText().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static List<Jcl.Word> wordsIn(Jcl tree) {
        List<Jcl.Word> words = new ArrayList<>();
        new JclIsoVisitor<Integer>() {
            @Override
            public Jcl.Word visitWord(Jcl.Word word, Integer p) {
                words.add(word);
                return word;
            }
        }.visit(tree, 0);
        return words;
    }
}
