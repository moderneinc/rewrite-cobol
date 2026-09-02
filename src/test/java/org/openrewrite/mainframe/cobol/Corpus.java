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
package org.openrewrite.mainframe.cobol;

import org.openrewrite.Parser;
import org.openrewrite.mainframe.assembler.AssemblerParser;
import org.openrewrite.mainframe.bms.BmsParser;
import org.openrewrite.mainframe.controlcard.idcams.IdcamsParser;
import org.openrewrite.mainframe.controlm.ControlMParser;
import org.openrewrite.mainframe.controlcard.sort.SortParser;
import org.openrewrite.mainframe.controlcard.utility.UtilityParser;
import org.openrewrite.mainframe.db2.bind.BindParser;
import org.openrewrite.mainframe.Members;
import org.openrewrite.mainframe.ims.ImsParser;
import org.openrewrite.mainframe.jcl.JclParser;
import org.openrewrite.mainframe.linkedit.LinkEditParser;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

/**
 * Shared by the corpus tests, which each walk a directory of real applications cloned side by side.
 * <p>
 * Each application is measured on its own. A copybook is found by member name, so two applications
 * that share one — Bank of Z and CBSA share 36 — would otherwise have the first one walked supply
 * the copybook for both.
 * <p>
 * Files are found the way the parsers themselves accept them, so the tests measure what a build
 * would read and nothing else.
 */
public final class Corpus {

    private Corpus() {
    }

    /**
     * The applications under the corpus root, one directory each, in name order.
     */
    public static List<Path> repositories(Path root) throws IOException {
        try (Stream<Path> children = Files.list(root)) {
            return children
              .filter(Files::isDirectory)
              .filter(p -> !p.getFileName().toString().startsWith("."))
              .sorted()
              .collect(toList());
        }
    }

    /**
     * moderneinc/mainframe-fixtures, cloned beside the real applications. It is written so that every
     * member of it parses, so the tests require that of it where they only measure the rest.
     */
    public static boolean isFixture(Path repository) {
        return "mainframe-fixtures".equals(repository.getFileName().toString());
    }

    public static List<Path> programs(Path repository) throws IOException {
        return files(repository, CobolParser.builder().build());
    }

    public static List<Path> copybooks(Path repository) throws IOException {
        return files(repository, CopybookParser.builder().build());
    }

    public static List<Path> mapsets(Path repository) throws IOException {
        return files(repository, BmsParser.builder().build());
    }

    /**
     * DBDs, PSBs, format sets and stage 1 decks together: one reader takes all four gen libraries,
     * since a member kept as {@code .asm} is typed by what it gens rather than by where it lives.
     */
    public static List<Path> imsDefinitions(Path repository) throws IOException {
        return files(repository, ImsParser.builder().build());
    }

    /**
     * HLASM programs and macro library members together, which are one language: an {@code .asm} the
     * IMS reader claims is a gen deck and is not among them.
     */
    public static List<Path> assemblerMembers(Path repository) throws IOException {
        return files(repository, AssemblerParser.builder().build());
    }

    /**
     * SAS members. The member name is the only name a SAS program has, so a program a job writes
     * in-stream is not among them and is reached through the job.
     */
    public static List<Path> sasPrograms(Path repository) throws IOException {
        return members(repository, Members.Kind.SAS);
    }

    /**
     * CLISTs and REXX execs together, which are one library to an application even where TSO keeps
     * them in two: they call each other, and either kind submits the same jobs.
     */
    public static List<Path> scripts(Path repository) throws IOException {
        return members(repository, Members.Kind.CLIST, Members.Kind.REXX);
    }

    public static List<Path> runBooks(Path repository) throws IOException {
        return members(repository, Members.Kind.DOCUMENT);
    }

    public static List<Path> cSources(Path repository) throws IOException {
        return members(repository, Members.Kind.C);
    }

    public static List<Path> pliSources(Path repository) throws IOException {
        return members(repository, Members.Kind.PLI);
    }

    public static List<Path> jobs(Path repository) throws IOException {
        return files(repository, JclParser.builder().build());
    }

    public static List<Path> schedules(Path repository) throws IOException {
        return files(repository, ControlMParser.builder().build());
    }

    public static List<Path> bindDecks(Path repository) throws IOException {
        return files(repository, BindParser.builder().build());
    }

    public static List<Path> linkEditDecks(Path repository) throws IOException {
        return files(repository, LinkEditParser.builder().build());
    }

    public static List<Path> moduleListings(Path repository) throws IOException {
        return members(repository, Members.Kind.LISTING);
    }

    public static List<Path> sortCards(Path repository) throws IOException {
        return files(repository, SortParser.builder().build());
    }

    public static List<Path> idcamsCards(Path repository) throws IOException {
        return files(repository, IdcamsParser.builder().build());
    }

    public static List<Path> utilityCards(Path repository) throws IOException {
        return files(repository, UtilityParser.builder().build());
    }

    /**
     * The members an application keeps as text, of the kinds asked for.
     */
    public static List<Path> members(Path repository, Members.Kind... kinds) throws IOException {
        Set<Members.Kind> wanted = EnumSet.copyOf(asList(kinds));
        try (Stream<Path> paths = Files.walk(repository)) {
            return paths
              .filter(Files::isRegularFile)
              .filter(p -> isSource(repository.relativize(p)))
              .filter(p -> wanted.contains(Members.kindOfFile(p)))
              .sorted()
              .collect(toList());
        }
    }

    /**
     * The members an estate keeps as text, read the way a build reads them.
     */
    public static Parser plainTextReader() {
        return Members.parser();
    }

    public static List<Parser.Input> inputs(List<Path> paths) {
        return paths.stream()
          .map(p -> new Parser.Input(p, () -> {
              try {
                  return Files.newInputStream(p);
              } catch (IOException e) {
                  throw new UncheckedIOException(e);
              }
          }))
          .collect(toList());
    }

    /**
     * Whether a file is corpus source rather than something a tool left behind.
     * <p>
     * The Moderne CLI writes each run's output under {@code <repo>/.moderne/run/<id>/after-fenced/},
     * keeping the original directory layout — so once anybody has run a recipe over the corpus,
     * walking it finds every program several times over, and the extra copies are recipe output with
     * search markers printed into them. Measured on 2026-08-17 that was 734 of 847 {@code .cbl}
     * files, and nothing failed: the corpus tests went on passing against four times the input they
     * were written for, quietly reporting four times as much of everything.
     * <p>
     * The CLI itself is not exposed to this — {@code RepositoryDirectory.directoryExcludedByDefault}
     * skips any directory whose name begins with a dot. Only these hand-rolled walks are, so they
     * skip the same directories.
     */
    public static boolean isSource(Path path) {
        for (Path element : path) {
            if (element.toString().startsWith(".")) {
                return false;
            }
        }
        return true;
    }

    private static List<Path> files(Path repository, Parser parser) throws IOException {
        try (Stream<Path> paths = Files.walk(repository)) {
            return paths
              .filter(Files::isRegularFile)
              .filter(p -> isSource(repository.relativize(p)))
              .filter(parser::accept)
              .sorted()
              .collect(toList());
        }
    }
}
