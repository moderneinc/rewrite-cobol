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
package org.openrewrite.listload.trait;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Parser;
import org.openrewrite.SourceFile;
import org.openrewrite.cobol.Corpus;
import org.openrewrite.text.PlainText;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Reads the load module listings of real applications and reports what the trait found.
 * <p>
 * Gated on {@code JCL_CORPUS} rather than a variable of its own: a listing is what the job that
 * builds or reports on a library printed, and it is the JCL half of an estate that holds both.
 * <p>
 * A report parses whatever it is given, so nothing is learned from it parsing. What is measured is
 * the count: every module and every control section the trait read is counted again off the source
 * text by a different rule, and the fixture, whose listings are written down module by module in its
 * own INTERLINKS document, is checked name by name.
 */
@EnabledIfEnvironmentVariable(named = "JCL_CORPUS", matches = ".+")
class ListLoadCorpusTest {

    /**
     * A control section summary row of an AMBLIST report, counted by its fixed columns rather than by
     * the trait's rule of two hexadecimal fields.
     */
    private static final Pattern SUMMARY_ROW =
      Pattern.compile("(?m)^ +\\S+ +[0-9A-F]{8} +[0-9A-F]{8} +SD\\b");

    /**
     * A module map row of a binder listing, counted the same way: the columns rather than the shape.
     */
    private static final Pattern MAP_ROW = Pattern.compile("(?m)^ +[0-9A-F]+ +\\S+ +CSECT\\b");

    private static final Pattern MEMBER = Pattern.compile("(?m)^.*MEMBER NAME:");

    private static final Pattern NAME_CARD = Pattern.compile("(?m)^.*IEW2322I.*\\sNAME\\s");

    @Test
    void readsRealModuleListings() throws IOException {
        Path corpus = Paths.get(System.getenv("JCL_CORPUS"));

        int listings = 0;
        int modules = 0;
        int written = 0;
        int csects = 0;
        int csectsWritten = 0;
        int entries = 0;
        int aliases = 0;
        int translators = 0;
        List<String> failures = new ArrayList<>();
        boolean fixtureFound = false;

        System.out.println("load module listings read, by application:");
        for (Path repository : Corpus.repositories(corpus)) {
            List<Path> members = Corpus.moduleListings(repository);
            if (members.isEmpty()) {
                continue;
            }
            fixtureFound |= Corpus.isFixture(repository);
            for (Path member : members) {
                listings++;
                String name = corpus.relativize(member).toString();
                String source = new String(Files.readAllBytes(member));
                PlainText cu = parse(member, source);
                if (cu == null) {
                    failures.add(name + ": did not parse");
                    continue;
                }
                if (!source.equals(cu.printAll())) {
                    failures.add(name + ": did not print back");
                    continue;
                }

                List<ModuleListing.Module> read = new ModuleListing.Matcher().require(cu, null).getModules();
                int count = count(MEMBER, source) + count(NAME_CARD, source);
                written += count;
                if (read.size() != count) {
                    failures.add(name + ": " + read.size() + " modules read, " + count + " written");
                    continue;
                }
                int sections = count(SUMMARY_ROW, source) + count(MAP_ROW, source);
                csectsWritten += sections;
                int placed = 0;
                for (ModuleListing.Module module : read) {
                    placed += module.getCsects().size();
                    entries += module.getEntry() == null ? 0 : 1;
                    aliases += module.getAliases().size();
                    for (ModuleListing.Csect csect : module.getCsects()) {
                        translators += csect.getTranslator() == null ? 0 : 1;
                    }
                }
                if (placed != sections) {
                    failures.add(name + ": " + placed + " control sections read, " + sections + " written");
                    continue;
                }
                modules += read.size();
                csects += placed;
            }
            System.out.printf("  %-40s %3d%n", repository.getFileName(), members.size());
        }
        assertThat(listings).as("no load module listings found under %s", corpus).isPositive();

        System.out.printf("listload corpus: %d listings, %d modules, %d control sections " +
                          "(%d entry points, %d aliases, %d translators)%n",
          listings, modules, csects, entries, aliases, translators);
        if (!failures.isEmpty()) {
            System.out.println("failures:");
            failures.forEach(f -> System.out.println("  " + f));
        }

        assertThat(failures).isEmpty();
        assertThat(modules).isEqualTo(written);
        assertThat(csects).isEqualTo(csectsWritten);
        assertThat(fixtureFound).as("mainframe-fixtures under %s", corpus).isTrue();
    }

    /**
     * The fixture's own oracle: INTERLINKS section 14 writes down all sixteen members the two AMBLIST
     * runs list, the thirteen the binder printed, and which programs each module holds.
     */
    @Test
    void readsTheFixtureListingsModuleByModule() throws IOException {
        Path listload = Paths.get(System.getenv("JCL_CORPUS"), "mainframe-fixtures", "claims", "listload");
        assertThat(Files.isDirectory(listload)).as("%s", listload).isTrue();

        TreeMap<String, List<ModuleListing.Module>> byMember = new TreeMap<>();
        for (Path member : Corpus.moduleListings(listload)) {
            PlainText cu = parse(member, new String(Files.readAllBytes(member)));
            assertThat(cu).as("%s", member).isNotNull();
            byMember.put(member.getFileName().toString(),
              new ModuleListing.Matcher().require(cu, null).getModules());
        }
        assertThat(byMember).hasSize(15);
        assertThat(byMember.keySet().stream().filter(m -> m.endsWith(".binder"))).hasSize(13);

        assertThat(names(byMember.get("LOADLIB.amblist"))).containsExactly(
          "CLMB010", "CLMB020", "CLMB030", "CLMD010", "CLMD020", "CLMD030", "CLMI010", "CLMU020");
        assertThat(names(byMember.get("CICSLOAD.amblist"))).containsExactly(
          "CLMC010", "CLMC020", "CLMC030", "CLMC040", "CLMC050", "CLMS01", "CLMS02", "CLMS03");
        for (String member : byMember.keySet()) {
            if (member.endsWith(".binder")) {
                assertThat(names(byMember.get(member)))
                  .containsExactly(member.substring(0, member.indexOf('.')));
            }
        }

        // Every module is entered at its own CSECT but the DL/I one, which is entered at the DLITCBL
        // label the program declares — and the only alias in the fixture is the old name CLMU020 is
        // still called by.
        List<ModuleListing.Module> listed = new ArrayList<>();
        listed.addAll(byMember.get("LOADLIB.amblist"));
        listed.addAll(byMember.get("CICSLOAD.amblist"));
        assertThat(listed).hasSize(16);
        assertThat(listed).filteredOn(module -> !module.getName().equals(module.getEntry().getName()))
          .extracting(ModuleListing.Module::getName, module -> module.getEntry().getName())
          .containsExactly(tuple("CLMI010", "DLITCBL"));
        assertThat(listed).flatExtracting(ModuleListing.Module::getAliases)
          .extracting(ModuleListing.Name::getText).containsExactly("CLMRESV");
        assertThat(listed).extracting(ModuleListing.Module::getLibrary).containsOnly("LOADLIB", "CICSLOAD");

        // The 16 program CSECTs of the 13 modules that hold a program — 13 of their own and the three
        // subroutines bound in statically — plus the three map sets the assembler produced.
        assertThat(listed).flatExtracting(ModuleListing.Module::getCsects)
          .filteredOn(csect -> csect.getTranslator().getLanguage() == ModuleListing.Language.COBOL)
          .hasSize(16);
        assertThat(listed).filteredOn(module -> module.getCsects().size() == 1)
          .extracting(ModuleListing.Module::getName).containsExactly("CLMS01", "CLMS02", "CLMS03");

        TreeMap<String, List<String>> statics = new TreeMap<>();
        for (ModuleListing.Module module : listed) {
            for (ModuleListing.Csect csect : module.getCsects()) {
                if (csect.getName().startsWith("CLMU") && !csect.getName().equals(module.getName())) {
                    statics.computeIfAbsent(module.getName(), k -> new ArrayList<>()).add(csect.getName());
                }
            }
        }
        assertThat(statics).containsExactly(
          entry("CLMB020", singletonList("CLMU010")),
          entry("CLMC020", singletonList("CLMU020")),
          entry("CLMC030", singletonList("CLMU010")));

        // The size the summary reports is what the sections it lists add up to, which is arithmetic
        // anybody can redo and the one thing a hand-authored listing is easy to get wrong.
        assertThat(listed).allSatisfy(module -> {
            long sections = 0;
            for (ModuleListing.Csect csect : module.getCsects()) {
                sections += Long.parseLong(csect.getLength(), 16);
            }
            assertThat(Long.parseLong(module.getSize(), 16)).as("%s", module.getName()).isEqualTo(sections);
        });
    }

    /**
     * The binder printed a listing for each of the thirteen modules it built, and what it reports has
     * to agree with what AMBLIST found in the library afterwards.
     */
    @Test
    void whatTheBinderPrintedAgreesWithWhatAmblistFound() throws IOException {
        Path listload = Paths.get(System.getenv("JCL_CORPUS"), "mainframe-fixtures", "claims", "listload");
        TreeMap<String, ModuleListing.Module> listed = new TreeMap<>();
        TreeMap<String, ModuleListing.Module> printed = new TreeMap<>();

        for (Path member : Corpus.moduleListings(listload)) {
            PlainText cu = parse(member, new String(Files.readAllBytes(member)));
            assertThat(cu).as("%s", member).isNotNull();
            boolean binder = member.getFileName().toString().endsWith(".binder");
            for (ModuleListing.Module module : new ModuleListing.Matcher().require(cu, null).getModules()) {
                (binder ? printed : listed).put(module.getName(), module);
            }
        }

        assertThat(printed).hasSize(13);
        for (String module : printed.keySet()) {
            ModuleListing.Module fromBinder = printed.get(module);
            ModuleListing.Module fromAmblist = listed.get(module);
            assertThat(fromAmblist).as("%s is listed", module).isNotNull();
            assertThat(fromBinder.getEntry().getName()).as("%s entry", module)
              .isEqualTo(fromAmblist.getEntry().getName());
            assertThat(csectNames(fromBinder)).as("%s sections", module).isEqualTo(csectNames(fromAmblist));
            for (int i = 0; i < fromBinder.getCsects().size(); i++) {
                assertThat(fromBinder.getCsects().get(i).getOffset()).as("%s section %d", module, i)
                  .isEqualTo(fromAmblist.getCsects().get(i).getOffset());
            }
        }
    }

    /**
     * A control card library holds decks of every kind side by side and nothing in a member's name
     * says which it is, so a member typed as one thing must not also read as another. The two AMBLIST
     * request decks of {@code claims/ctlcard} are the ones this reader claims.
     */
    @Test
    void typesTheRequestDecksAndNothingElse() throws IOException {
        Path corpus = Paths.get(System.getenv("JCL_CORPUS"));
        List<String> claimedTwice = new ArrayList<>();
        for (Path repository : Corpus.repositories(corpus)) {
            List<Path> listings = Corpus.moduleListings(repository);
            for (Path other : Corpus.sortCards(repository)) {
                if (listings.contains(other)) {
                    claimedTwice.add(corpus.relativize(other) + ": listing and sort");
                }
            }
            for (Path other : Corpus.idcamsCards(repository)) {
                if (listings.contains(other)) {
                    claimedTwice.add(corpus.relativize(other) + ": listing and IDCAMS");
                }
            }
            for (Path other : Corpus.linkEditDecks(repository)) {
                if (listings.contains(other)) {
                    claimedTwice.add(corpus.relativize(other) + ": listing and link-edit");
                }
            }
        }
        assertThat(claimedTwice).isEmpty();

        Path ctlcard = Paths.get(System.getenv("JCL_CORPUS"), "mainframe-fixtures", "claims", "ctlcard");
        List<Path> decks = Corpus.moduleListings(ctlcard);
        assertThat(decks).extracting(p -> p.getFileName().toString())
          .containsExactly("LSTCLM01.ctl", "LSTCLM02.ctl");

        List<ModuleListing.Request> requests = new ArrayList<>();
        for (Path deck : decks) {
            PlainText cu = parse(deck, new String(Files.readAllBytes(deck)));
            assertThat(cu).as("%s", deck).isNotNull();
            ModuleListing listing = new ModuleListing.Matcher().require(cu, null);
            assertThat(listing.getModules()).isEmpty();
            requests.addAll(listing.getRequests());
        }
        assertThat(requests)
          .extracting(ModuleListing.Request::getFunction, ModuleListing.Request::getDdName)
          .containsExactly(
            tuple("LISTLOAD", "LOADLIB"), tuple("LISTIDR", "LOADLIB"),
            tuple("LISTLOAD", "CICSLOAD"), tuple("LISTIDR", "CICSLOAD"));
    }

    private static List<String> names(List<ModuleListing.Module> modules) {
        return modules.stream().map(ModuleListing.Module::getName).collect(Collectors.toList());
    }

    private static List<String> csectNames(ModuleListing.Module module) {
        return module.getCsects().stream().map(ModuleListing.Csect::getName).collect(Collectors.toList());
    }

    private static int count(Pattern pattern, String source) {
        Matcher matcher = pattern.matcher(source);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private static @Nullable PlainText parse(Path member, String source) {
        List<SourceFile> parsed = Corpus.plainTextReader()
          .parseInputs(singletonList(new Parser.Input(member, () -> new ByteArrayInputStream(source.getBytes()))),
            null, new InMemoryExecutionContext())
          .collect(Collectors.toList());
        return parsed.size() == 1 && parsed.get(0) instanceof PlainText ? (PlainText) parsed.get(0) : null;
    }
}
