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
package org.openrewrite.cobol;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Parser;
import org.openrewrite.SourceFile;
import org.openrewrite.bms.BmsParser;
import org.openrewrite.controlcard.idcams.IdcamsParser;
import org.openrewrite.controlcard.sort.SortParser;
import org.openrewrite.controlm.ControlMParser;
import org.openrewrite.db2.Db2Parser;
import org.openrewrite.db2.bind.BindParser;
import org.openrewrite.ims.ImsParser;
import org.openrewrite.jcl.JclParser;
import org.openrewrite.linkedit.LinkEditParser;
import org.openrewrite.listload.ListLoadParser;
import org.openrewrite.tree.ParseError;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

/**
 * Every member of the fixture, against every reader this repository has.
 * <p>
 * The fixture is written so that all of it reads, so a member a reader takes must parse and print
 * back byte for byte. The other half matters as much. A mainframe library holds members of every kind
 * side by side and nothing in a member's name says which it is, so a member must be claimed by one
 * reader and no more, and the shapes there is no reader for — IMS format sets, HLASM, SAS, CLISTs,
 * REXX execs, the documentation members — must be claimed by none. A reader that quietly takes one of
 * those reports something plausible about a file it cannot read.
 * <p>
 * Gated on {@code JCL_CORPUS}, the variable the readers reached through a job already use, since the
 * fixture is one repository of that estate.
 */
@EnabledIfEnvironmentVariable(named = "JCL_CORPUS", matches = ".+")
class FixtureCoverageTest {

    @Test
    void everyMemberIsReadByOneReaderOrNone() throws IOException {
        Path fixture = Paths.get(System.getenv("JCL_CORPUS")).resolve("mainframe-fixtures");
        assertThat(Files.isDirectory(fixture)).as("%s", fixture).isTrue();

        Map<String, Parser> readers = new LinkedHashMap<>();
        readers.put("COBOL", CobolParser.builder().build());
        readers.put("copybook", CopybookParser.builder().build());
        readers.put("BMS", BmsParser.builder().build());
        readers.put("JCL", JclParser.builder().build());
        readers.put("Control-M", ControlMParser.builder().build());
        readers.put("bind", BindParser.builder().build());
        readers.put("sort", SortParser.builder().build());
        readers.put("IDCAMS", IdcamsParser.builder().build());
        readers.put("link-edit", LinkEditParser.builder().build());
        readers.put("load module listing", ListLoadParser.builder().build());
        readers.put("DB2 DDL", Db2Parser.builder().build());
        readers.put("IMS gen", ImsParser.builder().build());

        List<String> claimedTwice = new ArrayList<>();
        List<String> failures = new ArrayList<>();
        Map<String, Integer> read = new TreeMap<>();
        Map<String, Integer> unread = new TreeMap<>();
        for (Path member : members(fixture)) {
            String name = fixture.relativize(member).toString();
            List<String> claims = readers.entrySet().stream()
              .filter(reader -> reader.getValue().accept(member))
              .map(Map.Entry::getKey)
              .collect(toList());
            if (claims.isEmpty()) {
                unread.merge(extension(member), 1, Integer::sum);
                continue;
            }
            if (claims.size() > 1) {
                claimedTwice.add(name + ": " + String.join(" and ", claims));
                continue;
            }
            String reader = claims.get(0);
            read.merge(reader, 1, Integer::sum);
            String failure = readBack(readers.get(reader), fixture, member);
            if (failure != null) {
                failures.add(name + " [" + reader + "]: " + failure);
            }
        }

        System.out.println("fixture members read, by reader:");
        read.forEach((reader, count) -> System.out.printf("  %-24s %3d%n", reader, count));
        System.out.println("no reader:");
        unread.forEach((extension, count) -> System.out.printf("  %-24s %3d%n", extension, count));
        claimedTwice.forEach(member -> System.out.println("  claimed twice: " + member));
        failures.forEach(failure -> System.out.println("  " + failure));

        assertThat(claimedTwice).isEmpty();
        assertThat(failures).isEmpty();
        // A reader that stops accepting the members written for it fails nothing else here.
        assertThat(read.keySet()).containsExactlyInAnyOrderElementsOf(readers.keySet());
        assertThat(unread).containsExactly(
          // The repository's own licence, which is not a member of any library.
          entry("(none)", 1),
          // HLASM programs. The IMS reader looks at an .asm too, since a DBD or a PSB is often kept
          // as one, and declines these because the first macro they invoke gens nothing.
          entry(".asm", 3),
          entry(".clist", 8),
          // The IEBGENER, DSN, RUNSTATS and parm cards of claims/ctlcard, beside the sort, IDCAMS and
          // AMBLIST decks that do have readers: a member opening with nothing recognisable stays plain.
          entry(".ctl", 9),
          entry(".docfich", 7),
          entry(".docjob", 10),
          entry(".docpgm", 14),
          entry(".mac", 5),
          entry(".md", 2),
          entry(".mfs", 6),
          entry(".rexx", 3),
          entry(".sas", 4));
    }

    private static List<Path> members(Path fixture) throws IOException {
        try (Stream<Path> paths = Files.walk(fixture)) {
            return paths
              .filter(Files::isRegularFile)
              .filter(p -> Corpus.isSource(fixture.relativize(p)))
              .sorted()
              .collect(toList());
        }
    }

    private static String extension(Path member) {
        String name = member.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "(none)" : name.substring(dot);
    }

    /**
     * Why a member a reader took did not read, or null when it did.
     */
    private static @Nullable String readBack(Parser reader, Path fixture, Path member) throws IOException {
        List<SourceFile> parsed = reader
          .parseInputs(Corpus.inputs(singletonList(member)), fixture, new InMemoryExecutionContext())
          .collect(toList());
        if (parsed.size() != 1) {
            return parsed.size() + " source files from one member";
        }
        if (parsed.get(0) instanceof ParseError) {
            return "did not parse";
        }
        return new String(Files.readAllBytes(member)).equals(parsed.get(0).printAll()) ?
          null : "did not print back";
    }
}
