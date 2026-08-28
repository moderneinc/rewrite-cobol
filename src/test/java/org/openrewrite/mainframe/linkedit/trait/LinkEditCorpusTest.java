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
package org.openrewrite.mainframe.linkedit.trait;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Parser;
import org.openrewrite.SourceFile;
import org.openrewrite.mainframe.cobol.Corpus;
import org.openrewrite.mainframe.jcl.JclParser;
import org.openrewrite.mainframe.jcl.tree.Jcl;
import org.openrewrite.mainframe.linkedit.InStreamLinkEditDeck;
import org.openrewrite.mainframe.linkedit.LinkEditLineReader;
import org.openrewrite.mainframe.linkedit.LinkEditParser;
import org.openrewrite.mainframe.linkedit.tree.LinkEdit;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

/**
 * Reads the link-edit decks of real applications, as members of their own and as the in-stream data of
 * the jobs that link them, and reports what the trait found.
 * <p>
 * Gated on {@code JCL_CORPUS} rather than a variable of its own: a link-edit deck is reached through
 * the compile job that runs it, and it is the JCL half of an estate that holds both.
 * <p>
 * The measurement that matters is the count. A deck whose continuation is mishandled still parses and
 * still prints back — it just says something else — so every deck's statements are counted again off
 * the source text, and the fixture, whose every card is written down in its own INTERLINKS document,
 * is checked name by name.
 */
@EnabledIfEnvironmentVariable(named = "JCL_CORPUS", matches = ".+")
class LinkEditCorpusTest {

    @Test
    void readsRealLinkEditDecks() throws IOException {
        Path corpus = Paths.get(System.getenv("JCL_CORPUS"));

        int decks = 0;
        int statements = 0;
        int written = 0;
        int modules = 0;
        int entries = 0;
        int aliases = 0;
        int includes = 0;
        List<String> failures = new ArrayList<>();
        boolean fixtureFound = false;

        System.out.println("link-edit decks read, by application:");
        for (Path repository : Corpus.repositories(corpus)) {
            List<Path> members = new ArrayList<>(Corpus.linkEditDecks(repository));
            members.addAll(templates(repository));
            if (members.isEmpty()) {
                continue;
            }
            fixtureFound |= Corpus.isFixture(repository);
            for (Path member : members) {
                decks++;
                String name = corpus.relativize(member).toString();
                String source = new String(Files.readAllBytes(member));
                // A template is not a member any parser would accept by name, so it is read
                // straight from its text.
                LinkEdit.CompilationUnit cu = member.getFileName().toString().endsWith(".ftl") ?
                        LinkEditParser.parse(member, source) : parse(member, source);
                if (cu == null) {
                    failures.add(name + ": did not parse");
                    continue;
                }
                if (!source.equals(cu.printAll())) {
                    failures.add(name + ": did not print back");
                    continue;
                }

                int count = countStatements(source);
                written += count;
                if (cu.getStatements().size() != count) {
                    failures.add(name + ": " + cu.getStatements().size() + " statements read, " + count + " written");
                    continue;
                }
                statements += cu.getStatements().size();

                LinkEditDeck deck = new LinkEditDeck.Matcher().require(cu, null);
                modules += deck.getModule() == null ? 0 : 1;
                entries += deck.getEntry() == null ? 0 : 1;
                aliases += deck.getAliases().size();
                includes += deck.getIncludes().size();
            }
            System.out.printf("  %-40s %3d%n", repository.getFileName(), members.size());
        }
        assertThat(decks).as("no link-edit decks found under %s", corpus).isPositive();

        int inStream = 0;
        int inStreamStatements = 0;
        for (Path repository : Corpus.repositories(corpus)) {
            for (Path job : Corpus.jobs(repository)) {
                Jcl.CompilationUnit cu = parseJob(job, new String(Files.readAllBytes(job)));
                if (cu == null) {
                    continue;
                }
                for (InStreamLinkEditDeck deck : InStreamLinkEditDeck.of(cu)) {
                    inStream++;
                    inStreamStatements += deck.getDeck().getStatements().size();
                }
            }
        }

        System.out.printf("link-edit corpus: %d decks, %d statements (%d modules, %d entries, %d aliases, " +
                        "%d included members); %d decks written in-stream, %d statements%n",
                decks, statements, modules, entries, aliases, includes, inStream, inStreamStatements);
        if (!failures.isEmpty()) {
            System.out.println("failures:");
            failures.forEach(f -> System.out.println("  " + f));
        }

        assertThat(failures).isEmpty();
        assertThat(statements).isEqualTo(written);
        assertThat(fixtureFound).as("mainframe-fixtures under %s", corpus).isTrue();
        // A shop that keeps its decks in a library also writes them in the odd job, so none of one
        // shape means that shape is not being found at all.
        assertThat(inStream).isPositive();
    }

    /**
     * A control card library holds decks of every kind side by side and nothing in a member's name
     * says which it is, so a member typed as one thing must not also read as another.
     */
    @Test
    void noMemberIsClaimedByTwoParsers() throws IOException {
        Path corpus = Paths.get(System.getenv("JCL_CORPUS"));
        List<String> claimedTwice = new ArrayList<>();
        for (Path repository : Corpus.repositories(corpus)) {
            List<Path> decks = Corpus.linkEditDecks(repository);
            for (Path other : Corpus.bindDecks(repository)) {
                if (decks.contains(other)) {
                    claimedTwice.add(corpus.relativize(other) + ": link-edit and bind");
                }
            }
            for (Path other : Corpus.sortCards(repository)) {
                if (decks.contains(other)) {
                    claimedTwice.add(corpus.relativize(other) + ": link-edit and sort");
                }
            }
            for (Path other : Corpus.idcamsCards(repository)) {
                if (decks.contains(other)) {
                    claimedTwice.add(corpus.relativize(other) + ": link-edit and IDCAMS");
                }
            }
        }
        assertThat(claimedTwice).isEmpty();
    }

    /**
     * The fixture's own oracle: INTERLINKS section 12 writes down all 60 statements of
     * {@code claims/linklib}, which module each deck builds and which object each one is built from.
     */
    @Test
    void readsTheFixtureDecksNameByName() throws IOException {
        Path linklib = Paths.get(System.getenv("JCL_CORPUS"), "mainframe-fixtures", "claims", "linklib");
        assertThat(Files.isDirectory(linklib)).as("%s", linklib).isTrue();

        List<LinkEditDeck> decks = new ArrayList<>();
        for (Path member : Corpus.linkEditDecks(linklib)) {
            LinkEdit.CompilationUnit cu = parse(member, new String(Files.readAllBytes(member)));
            assertThat(cu).as("%s", member).isNotNull();
            decks.add(new LinkEditDeck.Matcher().require(cu, null));
        }

        assertThat(decks).hasSize(19);
        assertThat(decks).allSatisfy(deck -> {
            assertThat(deck.getModule()).isNotNull();
            assertThat(deck.getEntry()).isNotNull();
            assertThat(deck.isReplacing()).isTrue();
        });

        // Fourteen decks enter at the program's own CSECT; the five DL/I programs enter at the
        // DLITCBL label they declare, which is the deck's whole reason for saying so.
        assertThat(decks).filteredOn(deck -> "DLITCBL".equals(deck.getEntry().getText()))
          .extracting(deck -> deck.getModule().getText())
          .containsExactly("CLMI010", "CLMI020", "CLMI030", "CLMI040", "CLMI050");
        assertThat(decks).filteredOn(deck -> !"DLITCBL".equals(deck.getEntry().getText()))
          .allSatisfy(deck -> assertThat(deck.getEntry().getText()).isEqualTo(deck.getModule().getText()));

        // Two second directory entries: the name the message region loads CLMI030 by, and the name
        // policy administration still calls CLMU020 by.
        assertThat(decks).flatExtracting(LinkEditDeck::getAliases).extracting(LinkEditDeck.Name::getText)
          .containsExactly("CLMPSB02", "CLMRESV");

        // Every object library include and the CALL it matches: what is included is bound in, what is
        // not is called dynamically and is a load module of its own.
        TreeMap<String, List<String>> statics = new TreeMap<>();
        TreeMap<String, Integer> stubs = new TreeMap<>();
        for (LinkEditDeck deck : decks) {
            for (LinkEditDeck.Include include : deck.getIncludes()) {
                if ("OBJLIB".equals(include.getDdName())) {
                    statics.computeIfAbsent(deck.getModule().getText(), k -> new ArrayList<>()).add(include.getMember());
                } else {
                    stubs.merge(include.getMember(), 1, Integer::sum);
                }
            }
        }
        assertThat(statics).containsExactly(
          entry("CLMB020", singletonList("CLMU010")),
          entry("CLMC020", singletonList("CLMU020")),
          entry("CLMC030", singletonList("CLMU010")),
          entry("CLMI050", singletonList("CLMU030")),
          entry("CLMU040", singletonList("CLMU030")));
        assertThat(stubs).containsExactly(
          entry("DFHECI", 5), entry("DFSLI000", 6), entry("DSNCLI", 1), entry("DSNELI", 3));
    }

    /**
     * The decks a shop generates rather than keeps, which reach a repository as templates. A template
     * with no directive in it is a deck like any other; one with a directive is not source anybody
     * links from, so it is left alone.
     */
    private static List<Path> templates(Path repository) throws IOException {
        try (Stream<Path> paths = Files.walk(repository)) {
            return paths
              .filter(Files::isRegularFile)
              .filter(p -> p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".ftl"))
              .filter(p -> Corpus.isSource(repository.relativize(p)))
              .filter(p -> {
                  String source = read(p);
                  return !source.contains("<#") && LinkEditLineReader.isLinkEditDeck(source);
              })
              .sorted()
              .collect(Collectors.toList());
        }
    }

    private static String read(Path path) {
        try {
            return new String(Files.readAllBytes(path));
        } catch (IOException e) {
            return "";
        }
    }

    /**
     * Counted off the source rather than off the tree: a card that is neither a comment nor the
     * continuation of the card above it opens a statement.
     */
    private static int countStatements(String source) {
        int count = 0;
        boolean continued = false;
        for (String line : source.split("\n", -1)) {
            if (line.startsWith("*")) {
                continue;
            }
            String data = line.length() > LinkEditLineReader.DATA_COLUMNS ?
                    line.substring(0, LinkEditLineReader.DATA_COLUMNS) : line;
            if (data.trim().isEmpty()) {
                continued = false;
                continue;
            }
            if (!continued) {
                count++;
            }
            continued = line.length() > LinkEditLineReader.DATA_COLUMNS &&
                        !Character.isWhitespace(line.charAt(LinkEditLineReader.DATA_COLUMNS));
        }
        return count;
    }

    private static LinkEdit.@Nullable CompilationUnit parse(Path member, String source) {
        // Parsed by path rather than from the string: a member that links nothing is refused by name,
        // and a string has no name.
        List<SourceFile> parsed = LinkEditParser.builder().build()
                .parseInputs(singletonList(new Parser.Input(member, () -> new ByteArrayInputStream(source.getBytes()))),
                        null, new InMemoryExecutionContext())
                .collect(Collectors.toList());
        return parsed.size() == 1 && parsed.get(0) instanceof LinkEdit.CompilationUnit ?
                (LinkEdit.CompilationUnit) parsed.get(0) : null;
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
