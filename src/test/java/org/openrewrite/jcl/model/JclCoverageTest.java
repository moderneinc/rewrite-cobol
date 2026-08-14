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
package org.openrewrite.jcl.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.SourceFile;
import org.openrewrite.jcl.JclParser;
import org.openrewrite.jcl.tree.Jcl;
import org.openrewrite.jcl.tree.Statement;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * What the tree actually says about real JCL.
 * <p>
 * A parser that never fails is not the same as one that understands what it read. This counts what
 * lands in each node type, and asserts the two things that say coverage is real: nothing falls to
 * {@link Jcl.Unknown}, and every job control statement has an operation that is a JCL statement
 * rather than a fragment of the line before it.
 * <p>
 * Gated on {@code JCL_CORPUS} pointing at a checkout, since the corpus is not redistributed here.
 */
@EnabledIfEnvironmentVariable(named = "JCL_CORPUS", matches = ".+")
class JclCoverageTest {

    @Test
    void everythingIsUnderstood() throws Exception {
        Path corpus = Paths.get(System.getenv("JCL_CORPUS"));
        List<Path> members = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(corpus)) {
            walk.filter(Files::isRegularFile)
                    .filter(p -> p.toString().toLowerCase().endsWith(".jcl"))
                    .forEach(members::add);
        }

        Map<String, Integer> byType = new LinkedHashMap<>();
        Map<String, Integer> byOperation = new LinkedHashMap<>();
        Map<String, Integer> unknownTexts = new LinkedHashMap<>();

        for (Path member : members) {
            List<SourceFile> parsed = JclParser.builder().build()
                    .parse(new InMemoryExecutionContext(), new String(Files.readAllBytes(member)))
                    .collect(Collectors.toList());
            if (parsed.isEmpty() || !(parsed.get(0) instanceof Jcl.CompilationUnit)) {
                continue;
            }
            for (Statement s : ((Jcl.CompilationUnit) parsed.get(0)).getStatements()) {
                byType.merge(s.getClass().getSimpleName(), 1, Integer::sum);
                if (s instanceof Jcl.JobControlStatement) {
                    Jcl.JobControlStatement j = (Jcl.JobControlStatement) s;
                    byOperation.merge(j.getOperation() == null ? "(none)" :
                            j.getOperation().getText().toUpperCase(), 1, Integer::sum);
                } else if (s instanceof Jcl.Unknown) {
                    String text = ((Jcl.Unknown) s).getWord().getText();
                    unknownTexts.merge(text.length() > 30 ? text.substring(0, 30) : text, 1, Integer::sum);
                }
            }
        }

        System.out.println("=== node types");
        byType.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> System.out.printf("%8d  %s%n", e.getValue(), e.getKey()));
        System.out.println("=== operations");
        byOperation.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> System.out.printf("%8d  %s%n", e.getValue(), e.getKey()));
        System.out.println("=== unknown, most common 25");
        unknownTexts.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(25).forEach(e -> System.out.printf("%8d  [%s]%n", e.getValue(), e.getKey()));

        org.assertj.core.api.Assertions.assertThat(unknownTexts).as("words the parser could not place").isEmpty();
        org.assertj.core.api.Assertions.assertThat(byOperation).as("operations read").isNotEmpty();
        org.assertj.core.api.Assertions.assertThat(byOperation.keySet())
                .as("every operation should be a JCL statement, not a fragment of the line before it")
                .allSatisfy(operation -> org.assertj.core.api.Assertions.assertThat(operation)
                        .matches("[A-Z]+"));
    }
}
