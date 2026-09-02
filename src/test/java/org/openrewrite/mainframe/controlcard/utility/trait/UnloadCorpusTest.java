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
package org.openrewrite.mainframe.controlcard.utility.trait;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Parser;
import org.openrewrite.SourceFile;
import org.openrewrite.mainframe.cobol.Corpus;
import org.openrewrite.mainframe.controlcard.utility.InStreamUnloadDeck;
import org.openrewrite.mainframe.controlcard.utility.UtilityParser;
import org.openrewrite.mainframe.controlcard.utility.marker.Dialect;
import org.openrewrite.mainframe.controlcard.utility.tree.Utility;
import org.openrewrite.mainframe.jcl.JclParser;
import org.openrewrite.mainframe.jcl.tree.Jcl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

/**
 * Reads the Db2 utility control cards of real applications, as members of their own and as the
 * in-stream data of the jobs that run them, and reports what the traits found.
 * <p>
 * Gated on {@code JCL_CORPUS} rather than a variable of its own, as the bind, link-edit and sort
 * decks are: a control card is reached through the step that runs it, and it is the JCL half of an
 * estate that holds both.
 * <p>
 * The measurement that matters is the count. A deck whose keywords are misgrouped still parses and
 * still prints back — it just says something else — so every deck's statements are counted again off
 * the source text, and the fixture, whose every card is written down in its own INTERLINKS document,
 * is checked deck by deck.
 */
@EnabledIfEnvironmentVariable(named = "JCL_CORPUS", matches = ".+")
class UnloadCorpusTest {

    @Test
    void readsRealUtilityDecks() throws IOException {
        Path corpus = Paths.get(System.getenv("JCL_CORPUS"));

        int members = 0;
        int statements = 0;
        int written = 0;
        int unloads = 0;
        int selects = 0;
        int outputs = 0;
        List<String> failures = new ArrayList<>();
        boolean fixtureFound = false;

        System.out.println("Db2 utility decks read, by application:");
        for (Path repository : Corpus.repositories(corpus)) {
            List<Path> decks = Corpus.utilityCards(repository);
            if (decks.isEmpty()) {
                continue;
            }
            fixtureFound |= Corpus.isFixture(repository);
            for (Path member : decks) {
                String name = corpus.relativize(member).toString();
                String source = new String(Files.readAllBytes(member));
                Utility.CompilationUnit cu = parse(member, source);
                if (cu == null) {
                    failures.add(name + ": did not parse");
                    continue;
                }
                if (!source.equals(cu.printAll())) {
                    failures.add(name + ": did not print back");
                    continue;
                }
                List<UtilityStatement> read = new UtilityStatement.Matcher().lower(cu).collect(toList());
                int count = countStatements(source);
                written += count;
                if (read.size() != count) {
                    failures.add(name + ": " + read.size() + " statements read, " + count + " written");
                    continue;
                }
                members++;
                statements += read.size();
                for (UnloadCommand unload : new UnloadCommand.Matcher().lower(cu).collect(toList())) {
                    unloads++;
                    selects += unload.getSelects().size();
                    outputs += unload.getOutputDdNames().size();
                }
            }
            System.out.printf("  %-40s %3d%n", repository.getFileName(), decks.size());
        }
        assertThat(members).as("no Db2 utility decks found under %s", corpus).isPositive();

        int inStream = 0;
        int inStreamStatements = 0;
        for (Path repository : Corpus.repositories(corpus)) {
            for (Path job : Corpus.jobs(repository)) {
                String name = corpus.relativize(job).toString();
                Jcl.CompilationUnit cu = parseJob(job, new String(Files.readAllBytes(job)));
                if (cu == null) {
                    continue;
                }
                for (InStreamUnloadDeck deck : InStreamUnloadDeck.of(cu)) {
                    inStream++;
                    int read = new UtilityStatement.Matcher()
                            .lower(deck.getDeck()).collect(toList()).size();
                    int count = countStatements(deck.getCards().getText());
                    if (read != count) {
                        failures.add(name + " " + deck.getDdName() + ": " + read +
                                     " statements read in-stream, " + count + " written");
                        continue;
                    }
                    inStreamStatements += read;
                }
            }
        }

        System.out.printf("Db2 utility corpus: %d members, %d statements (%d unloads, %d selects, " +
                          "%d output DD names); %d decks written in-stream, %d statements%n",
                members, statements, unloads, selects, outputs, inStream, inStreamStatements);
        if (!failures.isEmpty()) {
            System.out.println("failures:");
            failures.forEach(f -> System.out.println("  " + f));
        }

        assertThat(failures).isEmpty();
        assertThat(statements).isEqualTo(written);
        assertThat(fixtureFound).as("mainframe-fixtures under %s", corpus).isTrue();
    }

    /**
     * The fixture's own oracle: INTERLINKS section 22 writes down the six decks of the unload set,
     * which dialect each is in, what each says and which DD name each keyword of it reaches.
     */
    @Test
    void readsTheFixtureDecksDeckByDeck() throws IOException {
        Path claims = Paths.get(System.getenv("JCL_CORPUS"), "mainframe-fixtures", "claims");
        assertThat(Files.isDirectory(claims)).as("%s", claims).isTrue();

        // 22.1: six unload decks, four of them members of ctlcard and two written in stream. The
        // fifth member of ctlcard is STACLM01, whose two RUNSTATS are read by the same island.
        List<Utility.CompilationUnit> decks = new ArrayList<>();
        for (Path member : Corpus.utilityCards(claims.resolve("ctlcard"))) {
            Utility.CompilationUnit cu = parse(member, new String(Files.readAllBytes(member)));
            assertThat(cu).as("%s", member).isNotNull();
            decks.add(cu);
        }
        assertThat(decks).hasSize(5);

        int inStream = 0;
        for (Path job : Corpus.jobs(claims.resolve("jcl"))) {
            Jcl.CompilationUnit cu = parseJob(job, new String(Files.readAllBytes(job)));
            assertThat(cu).as("%s", job).isNotNull();
            for (InStreamUnloadDeck deck : InStreamUnloadDeck.of(cu)) {
                decks.add(deck.getDeck());
                inStream++;
            }
        }
        assertThat(inStream).isEqualTo(2);

        // 22.4: ten blocks over the six unload decks — six UNLOAD, three GLOBAL and one TEMPLATE —
        // and beside those the two RUNSTATS of ctlcard/STACLM01.
        List<UtilityStatement> read = new ArrayList<>();
        List<UnloadCommand> unloads = new ArrayList<>();
        for (Utility.CompilationUnit deck : decks) {
            read.addAll(new UtilityStatement.Matcher().lower(deck).collect(toList()));
            unloads.addAll(new UnloadCommand.Matcher().lower(deck).collect(toList()));
        }
        TreeMap<String, Integer> verbs = new TreeMap<>();
        read.forEach(statement -> verbs.merge(statement.getVerb(), 1, Integer::sum));
        assertThat(verbs).containsExactly(
                entry("GLOBAL", 3),
                entry("RUNSTATS", 2),
                entry("TEMPLATE", 1),
                entry("UNLOAD", 6));

        // 22.1: five decks are the unload product's and one the base utility's.
        assertThat(unloads.stream()
                .filter(u -> u.getDialect() == Dialect.Kind.BASE_UTILITY)).hasSize(1);

        // 22.4: seven SELECT blocks and seven OUTDDNs, one per SELECT, and six FORMAT blocks. The
        // eighth output DD is the base utility deck's UNLDDN, which is where it writes instead.
        assertThat(unloads.stream().mapToInt(u -> u.getSelects().size()).sum()).isEqualTo(7);
        assertThat(unloads.stream().mapToInt(u -> u.getOutputDdNames().size()).sum()).isEqualTo(8);
        assertThat(unloads.stream().mapToInt(u -> u.getFormats().size()).sum()).isEqualTo(6);
        // and beside those three LOADDDNs, one PUNCHDDN and one COPYDDN.
        assertThat(unloads.stream().mapToInt(u -> u.getLoadDdNames().size()).sum()).isEqualTo(4);
        assertThat(unloads.stream().filter(u -> u.getCopyDdName() != null)).hasSize(1);

        // 22.4: four of the six code DB2 and three code LOCK and QUIESCE.
        assertThat(unloads.stream().filter(u -> u.getDb2() != null)).hasSize(4);
        assertThat(unloads.stream().filter(u -> u.getLock() != null)).hasSize(3);
        assertThat(unloads.stream().filter(u -> u.getQuiesce() != null)).hasSize(3);

        // 22.3: every DD name an unload deck writes to, and the one that reaches a TEMPLATE instead.
        Set<String> ddNames = new HashSet<>();
        unloads.forEach(u -> {
            ddNames.addAll(u.getOutputDdNames());
            ddNames.addAll(u.getLoadDdNames());
            if (u.getCopyDdName() != null) {
                ddNames.add(u.getCopyDdName());
            }
        });
        assertThat(ddNames).containsExactlyInAnyOrder("POLUNL", "POLLOAD", "HSTUNL", "HSTLOAD",
                "SYSREC", "SYSPUNCH", "OPENCLM", "CLOSCLM", "DENYCLM", "HSTCARD", "HSTCOPY",
                "HSTIMG", "POLDFT");
    }

    /**
     * 22.6: the deck says what a job would cost to move off the product, and the two jobs the
     * section says are undecidable are undecidable for reasons a reader can name.
     */
    @Test
    void readsWhatEachFixtureDeckLeavesToTheSite() throws IOException {
        Path ctlcard = Paths.get(System.getenv("JCL_CORPUS"), "mainframe-fixtures", "claims", "ctlcard");

        UnloadCommand defaults = unloadOf(ctlcard.resolve("UNLCLM04.ctl"));
        assertThat(defaults.getInheritedKeywords())
                .contains("FORMAT", "DB2", "LOCK", "QUIESCE");
        assertThat(defaults.getOutputDdNames()).containsExactly("POLDFT");

        // The base utility deck is already the target, and its defaults are published rather than
        // being a site setting.
        UnloadCommand base = unloadOf(ctlcard.resolve("UNLCLM02.ctl"));
        assertThat(base.getDialect()).isEqualTo(Dialect.Kind.BASE_UTILITY);
        assertThat(base.getShrLevel()).isEqualTo("REFERENCE");
        assertThat(base.getTables()).containsExactly("CLM.POLICY");
        assertThat(base.getInheritedKeywords()).isEmpty();

        // The image copy unload codes neither LOCK nor QUIESCE because a copy is neither.
        UnloadCommand copy = unloadOf(ctlcard.resolve("UNLCLM03.ctl"));
        assertThat(copy.getCopyDdName()).isEqualTo("HSTCOPY");
        assertThat(copy.getLock()).isNull();
        assertThat(copy.getQuiesce()).isNull();

        UnloadCommand history = unloadOf(ctlcard.resolve("UNLCLM01.ctl"));
        assertThat(history.getTableSpace()).isEqualTo("CLMDB01.CLMTSHST");
        assertThat(history.getFormats()).containsExactly("VARIABLE ALL");
        assertThat(history.getInheritedKeywords()).doesNotContain("FORMAT", "DB2", "LOCK", "QUIESCE");
    }

    private static UnloadCommand unloadOf(Path member) throws IOException {
        Utility.CompilationUnit cu = parse(member, new String(Files.readAllBytes(member)));
        assertThat(cu).as("%s", member).isNotNull();
        return new UnloadCommand.Matcher().lower(cu).findFirst().orElseThrow();
    }

    private static final Set<String> VERBS = new HashSet<>(Arrays.asList(
            "GLOBAL", "TEMPLATE", "LISTDEF", "LISTDEFTBV", "PROCESS_OPTIONS"));

    private static final Set<String> OBJECT_VERBS = new HashSet<>(Arrays.asList(
            "UNLOAD", "LOAD", "COPY", "COPYTOCOPY", "MERGECOPY", "REORG", "RUNSTATS", "CHECK",
            "QUIESCE", "REBUILD", "RECOVER", "REPAIR", "MODIFY", "STOSPACE"));

    private static final Set<String> OBJECTS = new HashSet<>(Arrays.asList(
            "DATA", "TABLESPACE", "TABLESPACES", "TABLESPACESET", "INDEXSPACE", "INDEXSPACES",
            "INDEX", "TABLE", "DATABASE", "LIST", "STOGROUP", "RECOVERY", "STATISTICS", "LOB",
            "OBJECT", "LOCATE", "SET", "DBD"));

    /**
     * Counted off the source rather than off the tree: a card opens a statement when its first word
     * is a utility verb and, for the verbs that are also keywords of one, when the word after it
     * names the object the utility works on. {@code QUIESCE YES} under an unload is that keyword and
     * not the utility of the same name.
     */
    private static int countStatements(String source) {
        int count = 0;
        boolean beforeFirstKeyword = true;
        for (String line : source.split("\n", -1)) {
            if (line.startsWith("--") || beforeFirstKeyword && line.startsWith("*")) {
                continue;
            }
            String text = line.trim();
            if (text.isEmpty()) {
                continue;
            }
            beforeFirstKeyword = false;
            String[] words = text.split("[\\s(]+", 3);
            String verb = words[0].toUpperCase(Locale.ROOT);
            String object = words.length > 1 ? words[1].toUpperCase(Locale.ROOT) : "";
            if (VERBS.contains(verb) || OBJECT_VERBS.contains(verb) && OBJECTS.contains(object)) {
                count++;
            }
        }
        return count;
    }

    private static Utility.@Nullable CompilationUnit parse(Path member, String source) {
        // Parsed by path rather than from the string: a member that runs no utility is refused by
        // name, and a string has no name.
        List<SourceFile> parsed = UtilityParser.builder().build()
                .parseInputs(singletonList(new Parser.Input(member, () -> new ByteArrayInputStream(source.getBytes()))),
                        null, new InMemoryExecutionContext())
                .collect(toList());
        return parsed.size() == 1 && parsed.get(0) instanceof Utility.CompilationUnit ?
                (Utility.CompilationUnit) parsed.get(0) : null;
    }

    private static Jcl.@Nullable CompilationUnit parseJob(Path job, String source) {
        List<SourceFile> parsed = JclParser.builder().build()
                .parseInputs(singletonList(new Parser.Input(job, () -> new ByteArrayInputStream(source.getBytes()))),
                        null, new InMemoryExecutionContext())
                .collect(toList());
        return parsed.size() == 1 && parsed.get(0) instanceof Jcl.CompilationUnit ?
                (Jcl.CompilationUnit) parsed.get(0) : null;
    }
}
