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
package org.openrewrite.mainframe.cobol.tree.preprocessor;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.junit.jupiter.api.Test;
import org.openrewrite.ExecutionContext;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.PathUtils;
import org.openrewrite.PrintOutputCapture;
import org.openrewrite.mainframe.cobol.CobolPreprocessorVisitor;
import org.openrewrite.mainframe.cobol.CobolTest;
import org.openrewrite.mainframe.cobol.internal.CobolDialect;
import org.openrewrite.mainframe.cobol.internal.CobolPreprocessorOutputSourcePrinter;
import org.openrewrite.mainframe.cobol.internal.CobolPreprocessorPrinter;
import org.openrewrite.mainframe.cobol.tree.CobolPreprocessor;
import org.openrewrite.mainframe.cobol.tree.Space;
import org.openrewrite.test.RecipeSpec;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.mainframe.cobol.Assertions.preprocessor;
import static org.openrewrite.test.RewriteTest.toRecipe;

public class CobolPreprocessorCopyTest extends CobolTest {
    private static final CobolDialect DIALECT = CobolDialect.ibmAnsi85();
    public static CobolPreprocessorPrinter<ExecutionContext> printer = new CobolPreprocessorPrinter<>(false, true);

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(toRecipe(context -> new CobolPreprocessorVisitor<>() {
            @Override
            public Space visitSpace(Space space, Space.Location location, ExecutionContext p) {
                String whitespace = space.getWhitespace().trim();
                if (!(DIALECT.getSeparators().contains(whitespace + " ") || whitespace.isEmpty())) {
                    return space.withWhitespace("(~~>" + space.getWhitespace() + "<~~)");
                }
                return space;
            }

            @Override
            public CobolPreprocessor visitCopyStatement(CobolPreprocessor.CopyStatement copyStatement, ExecutionContext p) {
                assertThat(copyStatement.getCopybook()).isNotNull();
                assertThat(copyStatement.getCopybook().getSourcePath()).isNotNull();
                CobolPreprocessor.Copybook copybook = copyStatement.getCopybook();

                var output = new PrintOutputCapture<ExecutionContext>(new InMemoryExecutionContext());
                var printer =
                  new CobolPreprocessorOutputSourcePrinter<ExecutionContext>(CobolDialect.ibmAnsi85(), true);
                printer.visit(copybook, output);

                String source = getSource(copybook.getSourcePath().toString());
                assertThat(source).isEqualTo(output.getOut());

                return super.visitCopyStatement(copyStatement, p);
            }
        }));
    }

    private String getSource(String copybook) {
        String searchPath = PathUtils.separatorsToUnix(copybook);
        try(ScanResult scan = new ClassGraph().scan()) {
            //noinspection OptionalGetWithoutIsPresent
            return scan.getResourcesWithExtension("cpy").stream()
              .filter(it -> {
                  String path = PathUtils.separatorsToUnix(it.getPath());
                  return path.endsWith(searchPath);
              })
              .findFirst()
              .get()
              .getContentAsString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void sm101A() {
        rewriteRun(
          preprocessor(getNistResource("SM101A.CBL"))
        );
    }

    @Test
    void sm103A() {
        rewriteRun(
          preprocessor(getNistResource("SM103A.CBL"))
        );
    }

    @Test
    void sm105A() {
        rewriteRun(
          preprocessor(getNistResource("SM105A.CBL"))
        );
    }

    @Test
    void sm106A() {
        rewriteRun(
          preprocessor(getNistResource("SM106A.CBL"))
        );
    }

    @Test
    void sm107A() {
        rewriteRun(
          preprocessor(getNistResource("SM107A.CBL"))
        );
    }

    @Test
    void sm207A() {
        rewriteRun(
          preprocessor(getNistResource("SM207A.CBL"))
        );
    }

    @Test
    void sm301M() {
        rewriteRun(
          preprocessor(getNistResource("SM301M.CBL"))
        );
    }

    @Test
    void sm401M() {
        rewriteRun(
          preprocessor(getNistResource("SM401M.CBL"))
        );
    }
}
