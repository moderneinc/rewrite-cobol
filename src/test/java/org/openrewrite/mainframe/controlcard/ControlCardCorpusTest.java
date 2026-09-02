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
package org.openrewrite.mainframe.controlcard;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Parser;
import org.openrewrite.SourceFile;
import org.openrewrite.mainframe.cobol.Corpus;
import org.openrewrite.mainframe.controlcard.idcams.IdcamsLineReader;
import org.openrewrite.mainframe.controlcard.idcams.IdcamsParser;
import org.openrewrite.mainframe.controlcard.idcams.trait.IdcamsCommand;
import org.openrewrite.mainframe.controlcard.idcams.tree.Idcams;
import org.openrewrite.mainframe.controlcard.sort.SortLineReader;
import org.openrewrite.mainframe.controlcard.sort.SortParser;
import org.openrewrite.mainframe.controlcard.sort.trait.SortStatement;
import org.openrewrite.mainframe.controlcard.sort.tree.Sort;
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
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reads the control cards of real applications, as members of their own and as the in-stream data of
 * the jobs that run them, and reports what the traits found.
 * <p>
 * Gated on {@code JCL_CORPUS} rather than a variable of its own: a control card is reached through the
 * step that reads it, and it is the JCL half of an estate that holds both.
 * <p>
 * The measurement that matters is the count. A deck whose continuation is mishandled still parses and
 * still prints back — it just says something else — so every deck's statements are counted again off
 * the source text, and the fixture, whose every card is written down in its own INTERLINKS document,
 * is checked member by member.
 */
@EnabledIfEnvironmentVariable(named = "JCL_CORPUS", matches = ".+")
class ControlCardCorpusTest {

    @Test
    void readsRealControlCardMembers() throws IOException {
        Path corpus = Paths.get(System.getenv("JCL_CORPUS"));

        int members = 0;
        int statements = 0;
        int written = 0;
        int fields = 0;
        int defined = 0;
        List<String> failures = new ArrayList<>();
        boolean fixtureFound = false;

        System.out.println("control card members read, by application:");
        for (Path repository : Corpus.repositories(corpus)) {
            List<Path> sortCards = Corpus.sortCards(repository);
            List<Path> idcamsCards = Corpus.idcamsCards(repository);
            if (sortCards.isEmpty() && idcamsCards.isEmpty()) {
                continue;
            }
            fixtureFound |= Corpus.isFixture(repository);

            // A member is typed by what it says, so no member may be claimed by two parsers.
            Set<Path> both = new HashSet<>(sortCards);
            both.retainAll(idcamsCards);
            both.forEach(member -> failures.add(corpus.relativize(member) + ": read as both a sort and an IDCAMS deck"));

            for (Path member : sortCards) {
                String name = corpus.relativize(member).toString();
                String source = new String(Files.readAllBytes(member));
                Sort.CompilationUnit cu = parseSort(member, source);
                if (cu == null) {
                    failures.add(name + ": did not parse");
                    continue;
                }
                if (!source.equals(cu.printAll())) {
                    failures.add(name + ": did not print back");
                    continue;
                }
                List<SortStatement> read = new SortStatement.Matcher().lower(cu).collect(Collectors.toList());
                int count = countSortStatements(source);
                written += count;
                if (read.size() != count) {
                    failures.add(name + ": " + read.size() + " statements read, " + count + " written");
                    continue;
                }
                members++;
                statements += read.size();
                for (SortStatement statement : read) {
                    fields += statement.getFields().size();
                }
            }

            for (Path member : idcamsCards) {
                String name = corpus.relativize(member).toString();
                String source = new String(Files.readAllBytes(member));
                Idcams.CompilationUnit cu = parseIdcams(member, source);
                if (cu == null) {
                    failures.add(name + ": did not parse");
                    continue;
                }
                if (!source.equals(cu.printAll())) {
                    failures.add(name + ": did not print back");
                    continue;
                }
                List<IdcamsCommand> read = new IdcamsCommand.Matcher().lower(cu).collect(Collectors.toList());
                int count = countIdcamsCommands(source);
                written += count;
                if (named(read) != count) {
                    failures.add(name + ": " + named(read) + " commands read, " + count + " written");
                    continue;
                }
                members++;
                statements += read.size();
                for (IdcamsCommand command : read) {
                    defined += command.getDefinedNames().size();
                }
            }
            System.out.printf("  %-40s %3d sort, %3d IDCAMS%n", repository.getFileName(),
                    sortCards.size(), idcamsCards.size());
        }
        assertThat(members).as("no control card members found under %s", corpus).isPositive();

        int decks = 0;
        int inStream = 0;
        boolean applications = false;
        for (Path repository : Corpus.repositories(corpus)) {
            List<Path> jobs = Corpus.jobs(repository);
            applications |= !jobs.isEmpty() && !Corpus.isFixture(repository);
            for (Path job : jobs) {
                String name = corpus.relativize(job).toString();
                Jcl.CompilationUnit cu = parseJob(job, new String(Files.readAllBytes(job)));
                if (cu == null) {
                    continue;
                }
                for (InStreamCards cards : InStreamCards.of(cu)) {
                    if (SortLineReader.isSortDeck(cards.getText())) {
                        Sort.CompilationUnit deck = SortParser.parse(job, cards.getText());
                        int read = new SortStatement.Matcher().lower(deck).collect(Collectors.toList()).size();
                        int count = countSortStatements(cards.getText());
                        decks++;
                        inStream += read;
                        if (read != count) {
                            failures.add(name + " " + cards.getDdName() + ": " + read +
                                         " statements read in-stream, " + count + " written");
                        }
                    } else if (IdcamsLineReader.isIdcamsDeck(cards.getText())) {
                        Idcams.CompilationUnit deck = IdcamsParser.parse(job, cards.getText());
                        List<IdcamsCommand> read = new IdcamsCommand.Matcher().lower(deck).collect(Collectors.toList());
                        int count = countIdcamsCommands(cards.getText());
                        decks++;
                        inStream += read.size();
                        if (named(read) != count) {
                            failures.add(name + " " + cards.getDdName() + ": " + named(read) +
                                         " commands read in-stream, " + count + " written");
                        }
                    }
                }
            }
        }

        System.out.printf("control card corpus: %d members, %d statements (%d sort control fields, " +
                          "%d data sets defined); %d decks written in-stream, %d statements%n",
                members, statements, fields, defined, decks, inStream);
        if (!failures.isEmpty()) {
            System.out.println("failures:");
            failures.forEach(f -> System.out.println("  " + f));
        }

        assertThat(failures).isEmpty();
        assertThat(statements).isGreaterThanOrEqualTo(written);
        assertThat(fixtureFound).as("mainframe-fixtures under %s", corpus).isTrue();
        // A shop that keeps its cards in a library also writes them in the odd job, so real
        // applications reporting none of one shape mean that shape is not being found at all.
        // CLAIMS writes every card as a member, so a root holding the fixture alone has none.
        if (applications) {
            assertThat(decks).isPositive();
        }
    }

    /**
     * The fixture's own oracle: INTERLINKS sections 8.3 and 22 write down all twenty three members
     * of {@code claims/ctlcard} and which deck each one is.
     */
    @Test
    void typesTheFixtureMembersByWhatTheySay() throws IOException {
        Path ctlcard = Paths.get(System.getenv("JCL_CORPUS"), "mainframe-fixtures", "claims", "ctlcard");
        assertThat(Files.isDirectory(ctlcard)).as("%s", ctlcard).isTrue();

        assertThat(names(Corpus.idcamsCards(ctlcard)))
                .containsExactly("DEFCLM01", "DEFCLM02", "DEFGDG01", "REPCLM01", "REPCLM02", "REPCLM03");
        assertThat(names(Corpus.sortCards(ctlcard))).containsExactly("SRTCLM01", "SRTCLM02");
        assertThat(names(Corpus.utilityCards(ctlcard)))
                .containsExactly("STACLM01", "UNLCLM01", "UNLCLM02", "UNLCLM03", "UNLCLM04");

        // Of the other ten, the two AMBLIST decks are typed by the listload reader and the eight
        // IEBGENER, DSN and parm cards stay plain parameters.
        try (Stream<Path> paths = Files.list(ctlcard)) {
            assertThat(paths.filter(Files::isRegularFile)).hasSize(23);
        }
    }

    /**
     * The claims stream defines six GDG bases and two clusters, and the extract it sorts is the one
     * {@code cpy/CLMEXTR} describes: the type code at 53 for four bytes, the claim number at 1 for
     * ten, the status at 57 for one.
     */
    @Test
    void readsTheFixtureCardsNameByName() throws IOException {
        Path ctlcard = Paths.get(System.getenv("JCL_CORPUS"), "mainframe-fixtures", "claims", "ctlcard");

        List<IdcamsCommand> defines = new ArrayList<>();
        for (Path member : Corpus.idcamsCards(ctlcard)) {
            Idcams.CompilationUnit cu = parseIdcams(member, new String(Files.readAllBytes(member)));
            assertThat(cu).as("%s", member).isNotNull();
            for (IdcamsCommand command : new IdcamsCommand.Matcher().lower(cu).collect(Collectors.toList())) {
                if (command.definesDataSet()) {
                    defines.add(command);
                }
            }
        }

        TreeSet<String> names = new TreeSet<>();
        defines.forEach(define -> names.addAll(define.getDefinedNames()));
        assertThat(names).contains("CLM.PROD.CLMMAST", "CLM.PROD.CLMMAST.DATA", "CLM.PROD.CLMMAST.INDEX",
                "CLM.PROD.CLMTYPE", "CLM.PROD.EXTRACT", "CLM.PROD.CLMRPT", "CLM.PROD.CLMAUDIT",
                "CLM.PROD.CLMMAST.BACKUP", "CLM.PROD.IMS.CLMDB01.LOG");
        assertThat(defines.stream().filter(d -> "GDG".equals(d.getObjectType()))).hasSize(6);
        assertThat(defines.stream().filter(d -> "CLUSTER".equals(d.getObjectType()))).hasSize(2);

        Sort.CompilationUnit sortExtract = parseSort(ctlcard.resolve("SRTCLM01.ctl"),
                new String(Files.readAllBytes(ctlcard.resolve("SRTCLM01.ctl"))));
        assertThat(sortExtract).isNotNull();
        List<SortStatement> statements = new SortStatement.Matcher().lower(sortExtract).collect(Collectors.toList());
        assertThat(statements.get(0).getFields()).containsExactly(
                new SortStatement.Field(53, 4, "CH", "A"),
                new SortStatement.Field(1, 10, "CH", "A"));
        assertThat(statements.get(1).getOperand("COND")).contains("57,1,CH,EQ,C'O'");
    }

    private static TreeSet<String> names(List<Path> members) {
        TreeSet<String> names = new TreeSet<>();
        for (Path member : members) {
            String name = member.getFileName().toString();
            int dot = name.lastIndexOf('.');
            names.add(dot < 0 ? name : name.substring(0, dot));
        }
        return names;
    }

    /**
     * How many commands the trait read under a verb the source count also recognises, so that the two
     * measure the same thing: a deck's {@code SET} and {@code IF} cards are commands too, but nothing
     * outside the tree can tell them from a continuation.
     */
    private static int named(List<IdcamsCommand> commands) {
        int count = 0;
        for (IdcamsCommand command : commands) {
            if (IDCAMS_VERBS.contains(command.getVerb())) {
                count++;
            }
        }
        return count;
    }

    private static final Set<String> IDCAMS_VERBS = new HashSet<>(Arrays.asList(
            "DEFINE", "DEF", "DELETE", "DEL", "REPRO", "LISTCAT", "LISTC", "PRINT", "ALTER",
            "EXPORT", "IMPORT", "BLDINDEX", "BIX", "EXAMINE"));

    private static final Set<String> SORT_OPERATORS = new HashSet<>(Arrays.asList(
            "SORT", "MERGE", "OPTION", "INREC", "OUTREC", "OUTFIL", "SUM",
            "ALTSEQ", "MODS", "JOINKEYS", "REFORMAT", "DEBUG"));

    private static final Set<String> TOOL_OPERATORS = new HashSet<>(Arrays.asList(
            "SELECT", "COPY", "COUNT", "DISPLAY", "OCCUR", "RANGE", "RESIZE", "SPLICE",
            "STATS", "SUBSET", "UNIQUE", "VERIFY"));

    /**
     * Counted off the source rather than off the tree: a card whose first word is an operator opens a
     * statement, and nothing a continuation card can begin with is one.
     */
    private static int countSortStatements(String source) {
        int count = 0;
        for (String line : source.split("\n", -1)) {
            if (line.startsWith("*")) {
                continue;
            }
            String[] words = line.trim().split("\\s+", 2);
            String operator = words[0].toUpperCase(Locale.ROOT);
            String operands = words.length > 1 ? words[1].toUpperCase(Locale.ROOT) : "";
            if (SORT_OPERATORS.contains(operator) ||
                (("INCLUDE".equals(operator) || "OMIT".equals(operator)) &&
                 (operands.startsWith("COND=") || operands.startsWith("FORMAT="))) ||
                (TOOL_OPERATORS.contains(operator) && operands.startsWith("FROM("))) {
                count++;
            }
        }
        return count;
    }

    private static int countIdcamsCommands(String source) {
        int count = 0;
        for (String line : source.split("\n", -1)) {
            String[] words = line.replaceAll("/\\*[^\r\n]*?\\*/", " ").trim().split("[\\s(]+", 2);
            if (IDCAMS_VERBS.contains(words[0].toUpperCase(Locale.ROOT))) {
                count++;
            }
        }
        return count;
    }

    private static Sort.@Nullable CompilationUnit parseSort(Path member, String source) {
        SourceFile parsed = parse(SortParser.builder().build(), member, source);
        return parsed instanceof Sort.CompilationUnit ? (Sort.CompilationUnit) parsed : null;
    }

    private static Idcams.@Nullable CompilationUnit parseIdcams(Path member, String source) {
        SourceFile parsed = parse(IdcamsParser.builder().build(), member, source);
        return parsed instanceof Idcams.CompilationUnit ? (Idcams.CompilationUnit) parsed : null;
    }

    private static Jcl.@Nullable CompilationUnit parseJob(Path job, String source) {
        SourceFile parsed = parse(JclParser.builder().build(), job, source);
        return parsed instanceof Jcl.CompilationUnit ? (Jcl.CompilationUnit) parsed : null;
    }

    /**
     * Parsed by path rather than from the string: a member is typed by content and refused by the
     * parser that does not want it, and a string has no path to be offered under.
     */
    private static @Nullable SourceFile parse(Parser parser, Path path, String source) {
        List<SourceFile> parsed = parser
                .parseInputs(singletonList(new Parser.Input(path, () -> new ByteArrayInputStream(source.getBytes()))),
                        null, new InMemoryExecutionContext())
                .collect(Collectors.toList());
        return parsed.size() == 1 ? parsed.get(0) : null;
    }
}
