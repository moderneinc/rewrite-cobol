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
package org.openrewrite.db2.bind.trait;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Parser;
import org.openrewrite.SourceFile;
import org.openrewrite.cobol.Corpus;
import org.openrewrite.db2.bind.BindParser;
import org.openrewrite.db2.bind.InStreamBindDeck;
import org.openrewrite.db2.bind.tree.Bind;
import org.openrewrite.jcl.JclParser;
import org.openrewrite.jcl.tree.Jcl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reads the bind decks of real applications, as members of their own and as the in-stream data of the
 * jobs that run them, and reports what the traits found.
 * <p>
 * Gated on {@code JCL_CORPUS} rather than a variable of its own: a bind deck is reached through the
 * jobs that run it, and it is the JCL half of an estate that holds both.
 * <p>
 * The measurement that matters is the count. A deck whose continuation is mishandled still parses and
 * still prints back — it just says something else — so every deck's binds are counted again off the
 * source text, and the fixture, whose every bind is written down in its own INTERLINKS document, is
 * checked name by name.
 */
@EnabledIfEnvironmentVariable(named = "JCL_CORPUS", matches = ".+")
class BindCorpusTest {

    @Test
    void readsRealBindDecks() throws IOException {
        Path corpus = Paths.get(System.getenv("JCL_CORPUS"));

        int decks = 0;
        int commands = 0;
        int written = 0;
        int packages = 0;
        int plans = 0;
        int members = 0;
        List<String> failures = new ArrayList<>();
        boolean fixtureFound = false;

        System.out.println("bind decks read, by application:");
        for (Path repository : Corpus.repositories(corpus)) {
            List<Path> files = Corpus.bindDecks(repository);
            if (files.isEmpty()) {
                continue;
            }
            fixtureFound |= Corpus.isFixture(repository);
            for (Path member : files) {
                decks++;
                String name = corpus.relativize(member).toString();
                String source = new String(Files.readAllBytes(member));
                Bind.CompilationUnit cu = parse(member, source);
                if (cu == null) {
                    failures.add(name + ": did not parse");
                    continue;
                }
                if (!source.equals(cu.printAll())) {
                    failures.add(name + ": did not print back");
                    continue;
                }

                List<BindCommand> read = new BindCommand.Matcher().lower(cu).collect(Collectors.toList());
                int count = countCommands(source);
                written += count;
                if (read.size() != count) {
                    failures.add(name + ": " + read.size() + " binds read, " + count + " written");
                    continue;
                }
                commands += read.size();
                for (BindCommand command : read) {
                    packages += command.getPackages().size();
                    plans += command.getPlans().size();
                    members += command.getMembers().size();
                }
            }
            System.out.printf("  %-40s %3d%n", repository.getFileName(), files.size());
        }
        assertThat(decks).as("no bind decks found under %s", corpus).isPositive();

        int inStream = 0;
        int inStreamCommands = 0;
        boolean applications = false;
        for (Path repository : Corpus.repositories(corpus)) {
            List<Path> jobs = Corpus.jobs(repository);
            applications |= !jobs.isEmpty() && !Corpus.isFixture(repository);
            for (Path job : jobs) {
                String name = corpus.relativize(job).toString();
                String source = new String(Files.readAllBytes(job));
                Jcl.CompilationUnit cu = parseJob(job, source);
                if (cu == null) {
                    continue;
                }
                int read = 0;
                for (InStreamBindDeck deck : InStreamBindDeck.of(cu)) {
                    inStream++;
                    read += new BindCommand.Matcher().lower(deck.getDeck()).collect(Collectors.toList()).size();
                }
                // Counted off the job rather than off its SYSTSINs: a bind the deck finding missed
                // is still a BIND card in the file, and this is what says so.
                int count = countCommands(source);
                if (read != count) {
                    failures.add(name + ": " + read + " binds read in-stream, " + count + " written");
                    continue;
                }
                inStreamCommands += read;
            }
        }

        System.out.printf("bind corpus: %d decks, %d binds (%d packages, %d plans, %d DBRM members); " +
                        "%d decks written in-stream, %d binds%n",
                decks, commands, packages, plans, members, inStream, inStreamCommands);
        if (!failures.isEmpty()) {
            System.out.println("failures:");
            failures.forEach(f -> System.out.println("  " + f));
        }

        assertThat(failures).isEmpty();
        assertThat(commands).isEqualTo(written);
        assertThat(fixtureFound).as("mainframe-fixtures under %s", corpus).isTrue();
        // A shop that keeps its decks in a library also writes them in the odd job, so real
        // applications reporting none of one shape mean that shape is not being found at all.
        // CLAIMS writes every deck as a member, so a root holding the fixture alone has none.
        if (applications) {
            assertThat(inStream).isPositive();
        }
    }

    /**
     * The fixture's own oracle: INTERLINKS section 13 writes down all twelve statements of
     * {@code claims/cardlib}, which package each bind makes and which program it reaches.
     */
    @Test
    void readsTheFixtureDecksNameByName() throws IOException {
        Path cardlib = Paths.get(System.getenv("JCL_CORPUS"), "mainframe-fixtures", "claims", "cardlib");
        assertThat(Files.isDirectory(cardlib)).as("%s", cardlib).isTrue();

        List<BindCommand> binds = new ArrayList<>();
        List<BindCommand> rebinds = new ArrayList<>();
        for (Path member : Corpus.bindDecks(cardlib)) {
            Bind.CompilationUnit cu = parse(member, new String(Files.readAllBytes(member)));
            assertThat(cu).as("%s", member).isNotNull();
            for (BindCommand command : new BindCommand.Matcher().lower(cu).collect(Collectors.toList())) {
                (command.getKind() == BindCommand.Kind.REBIND ? rebinds : binds).add(command);
            }
        }

        assertThat(binds).hasSize(6);
        assertThat(rebinds).hasSize(2);

        assertThat(names(binds, BindCommand::getMembers)).containsExactly("CLMC040", "CLMD010", "CLMD020", "CLMD030");
        assertThat(names(binds, BindCommand::getPlans)).containsExactly("CLMCICS", "CLMPLAN");
        assertThat(names(binds, BindCommand::getPackageList)).containsExactly("CLMPKG.*", "CLMPKG.CLMC040");
        assertThat(names(rebinds, BindCommand::getPlans)).containsExactly("CLMCICS", "CLMPLAN");

        // Every bind in the fixture carries the same owner and qualifier, and the qualifier is the
        // schema the SQL in the programs actually reads.
        for (BindCommand bind : binds) {
            assertThat(bind.getOwner()).isEqualTo("CLMPROD");
            assertThat(bind.getQualifier()).isEqualTo("CLM");
            assertThat(bind.getCollection()).isEqualTo(bind.bindsPlan() ? null : "CLMPKG");
        }
    }

    private static TreeSet<String> names(List<BindCommand> commands,
                                         Function<BindCommand, List<String>> of) {
        TreeSet<String> names = new TreeSet<>();
        commands.forEach(command -> names.addAll(of.apply(command)));
        return names;
    }

    /**
     * Counted off the source rather than off the tree: a line whose first word is the verb opens a
     * subcommand, and nothing a continuation line can begin with is one.
     */
    private static int countCommands(String source) {
        int count = 0;
        for (String line : source.split("\n", -1)) {
            String[] words = line.trim().split("[\\s(]+", 2);
            String verb = words[0].toUpperCase(Locale.ROOT);
            if ("BIND".equals(verb) || "REBIND".equals(verb)) {
                count++;
            }
        }
        return count;
    }

    private static Bind.@Nullable CompilationUnit parse(Path member, String source) {
        // Parsed by path rather than from the string: a member that binds nothing is refused by name,
        // and a string has no name.
        List<SourceFile> parsed = BindParser.builder().build()
                .parseInputs(singletonList(new Parser.Input(member, () -> new ByteArrayInputStream(source.getBytes()))),
                        null, new InMemoryExecutionContext())
                .collect(Collectors.toList());
        return parsed.size() == 1 && parsed.get(0) instanceof Bind.CompilationUnit ?
                (Bind.CompilationUnit) parsed.get(0) : null;
    }

    private static Jcl.@Nullable CompilationUnit parseJob(Path job, String source) {
        List<SourceFile> parsed = JclParser.builder().build()
                .parseInputs(singletonList(new Parser.Input(job, () -> new ByteArrayInputStream(source.getBytes()))),
                        null, new InMemoryExecutionContext())
                .collect(Collectors.toList());
        return parsed.size() == 1 && parsed.get(0) instanceof Jcl.CompilationUnit ?
                (Jcl.CompilationUnit) parsed.get(0) : null;
    }
}
