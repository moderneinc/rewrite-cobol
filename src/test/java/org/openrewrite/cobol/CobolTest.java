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

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.openrewrite.ExecutionContext;
import org.openrewrite.cobol.internal.CobolDialect;
import org.openrewrite.cobol.tree.Space;
import org.openrewrite.internal.StringUtils;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.test.RewriteTest.toRecipe;

public abstract class CobolTest implements RewriteTest {
    private volatile List<String> nistResourcePaths;

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(toRecipe(() -> new CobolIsoVisitor<>() {
            @Override
            public Space visitSpace(Space space, Space.Location location, ExecutionContext ctx) {
                String whitespace = space.getWhitespace().trim();
                if (!(whitespace.isEmpty() || CobolDialect.ibmAnsi85().getSeparators().contains(whitespace + " "))) {
                    return space.withWhitespace("(~~>${space.whitespace}<~~)");
                }
                return space;
            }
        }));
    }

    private List<String> getNistResourcePaths() {
        if (nistResourcePaths == null) {
            try (ScanResult scanResult = new ClassGraph().acceptPaths("/gov/nist").scan()) {
                nistResourcePaths = scanResult.getAllResources().getPaths();
            }
        }
        return nistResourcePaths;
    }

    public String getNistResource(String sourceName) {
        Optional<String> source = getNistResourcePaths().stream()
          .filter(it -> it.toLowerCase().endsWith(sourceName.toLowerCase()))
          .findFirst();
        assertThat(source).isPresent();
        return StringUtils.readFully(getClass().getClassLoader().getResourceAsStream(source.get()));
    }
}
