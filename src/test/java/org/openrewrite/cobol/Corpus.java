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

import org.openrewrite.Parser;
import org.openrewrite.bms.BmsParser;
import org.openrewrite.db2.bind.BindParser;
import org.openrewrite.jcl.JclParser;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

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

    public static List<Path> jobs(Path repository) throws IOException {
        return files(repository, JclParser.builder().build());
    }

    public static List<Path> bindDecks(Path repository) throws IOException {
        return files(repository, BindParser.builder().build());
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
