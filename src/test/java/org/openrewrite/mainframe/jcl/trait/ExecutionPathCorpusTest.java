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
package org.openrewrite.mainframe.jcl.trait;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.SourceFile;
import org.openrewrite.mainframe.cobol.Corpus;
import org.openrewrite.mainframe.jcl.JclParser;
import org.openrewrite.mainframe.jcl.tree.Jcl;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The five Db2 unload jobs of the fixture, put through the verdict with the libraries the shop runs
 * supplied — the procedures they call and the control card members their SYSIN names.
 * <p>
 * They are written to be five different answers and the fixture says so in {@code docs/INTERLINKS}
 * section 22.6 before any recipe existed: one already in the base utility's dialect, three that run
 * the unload product, and one of those three reached only through a symbolic. What the verdict adds
 * to that is the line between a name nobody supplied and a keyword nobody wrote down, and the jobs
 * here are all of the second kind: every name in this repository resolves.
 */
@EnabledIfEnvironmentVariable(named = "JCL_CORPUS", matches = ".+")
class ExecutionPathCorpusTest {

    private static final List<String> JOBS =
            List.of("CLMUNLB", "CLMUNLD", "CLMUNLF", "CLMUNLH", "CLMUNLP");

    @Test
    void readsTheUnloadJobsOfTheFixture() throws IOException {
        Path fixture = fixture();
        List<Path> jobs = Corpus.jobs(fixture);
        List<SourceFile> parsed = JclParser.builder()
                .procedureLibrary(jobs)
                .parmMembers(Corpus.utilityCards(fixture))
                .build()
                .parseInputs(Corpus.inputs(jobs), fixture, new InMemoryExecutionContext())
                .collect(toList());

        Map<String, List<ExecutionPath>> unloads = new LinkedHashMap<>();
        for (SourceFile source : parsed) {
            String member = source instanceof Jcl.CompilationUnit ?
                    memberName(source.getSourcePath()) : "";
            // The procedure they call is a member of the library and not a job of its own.
            if (JOBS.contains(member)) {
                unloads.put(member, new ExecutionPath.Matcher()
                        .lower((Jcl.CompilationUnit) source).collect(toList()));
            }
        }

        assertThat(unloads).containsOnlyKeys(JOBS.toArray(new String[0]));

        // The base utility's defaults are published and the same everywhere, so a deck written in its
        // dialect says everything it does. Nothing here has to be asked of the shop.
        assertThat(verdicts(unloads.get("CLMUNLB")))
                .containsExactly(ExecutionPath.Verdict.RESOLVED);

        // The other four run the unload product, whose parmlib member on INFPLIB answers the four
        // keywords that decide what an unload writes and how it reads. Three of them code all four
        // and read as clean as the base utility job; ctlcard/UNLCLM04 codes none of them, and
        // jcl/CLMUNLD is the one job here nobody may translate without asking the shop.
        assertThat(verdicts(unloads.get("CLMUNLP")))
                .containsExactly(ExecutionPath.Verdict.RESOLVED);
        assertThat(verdicts(unloads.get("CLMUNLH")))
                .containsExactly(ExecutionPath.Verdict.RESOLVED);
        assertThat(verdicts(unloads.get("CLMUNLD")))
                .containsExactly(ExecutionPath.Verdict.INHERITED);
        // Two steps: the fan out reads the table space, the second step an image copy of it, which
        // may code neither LOCK nor QUIESCE and is not asked for either.
        assertThat(verdicts(unloads.get("CLMUNLF")))
                .containsExactly(ExecutionPath.Verdict.RESOLVED, ExecutionPath.Verdict.RESOLVED);

        // No line of CLMUNLD says which table it unloads: the member is the value of DECK=, the
        // SYSIN that reads it is written in proc CLMUNL, and the gap is reported against that step.
        assertThat(unloads.get("CLMUNLD").get(0).getGaps())
                .isNotEmpty()
                .allSatisfy(gap -> assertThat(gap.getStep()).isEqualTo("UNLPOL.UNL"));

        // CLMUNLD is the one whose four load bearing keywords are all uncoded, which is what the
        // fixture wrote it for; CLMUNLP codes them and leaves only the rest to the site.
        assertThat(names(unloads.get("CLMUNLD").get(0)))
                .contains("FORMAT", "DB2", "LOCK", "QUIESCE");
        assertThat(names(unloads.get("CLMUNLP").get(0))).isEmpty();
    }

    private static List<ExecutionPath.Verdict> verdicts(List<ExecutionPath> paths) {
        List<ExecutionPath.Verdict> verdicts = new ArrayList<>(paths.size());
        for (ExecutionPath path : paths) {
            verdicts.add(path.getVerdict());
        }
        return verdicts;
    }

    private static List<String> names(ExecutionPath path) {
        List<String> names = new ArrayList<>();
        for (Gap gap : path.getGaps()) {
            names.add(gap.getName());
        }
        return names;
    }

    private static String memberName(Path path) {
        String fileName = path.getFileName().toString();
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? fileName : fileName.substring(0, dot);
    }

    private static Path fixture() throws IOException {
        for (Path repository : Corpus.repositories(Paths.get(System.getenv("JCL_CORPUS")))) {
            if (Corpus.isFixture(repository)) {
                return repository;
            }
        }
        throw new IllegalStateException("mainframe-fixtures is not checked out under JCL_CORPUS.");
    }
}
