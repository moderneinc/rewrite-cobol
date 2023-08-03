/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.cobol.tree.preprocessor;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.junit.jupiter.api.Test;
import org.openrewrite.*;
import org.openrewrite.cobol.CobolPreprocessorVisitor;
import org.openrewrite.cobol.CobolTest;
import org.openrewrite.cobol.internal.CobolDialect;
import org.openrewrite.cobol.internal.CobolPreprocessorOutputSourcePrinter;
import org.openrewrite.cobol.internal.CobolPreprocessorPrinter;
import org.openrewrite.cobol.tree.CobolPreprocessor;
import org.openrewrite.cobol.tree.Space;
import org.openrewrite.test.RecipeSpec;


import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.cobol.Assertions.preprocessor;
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
                assertThat(copyStatement.getCopyBook()).isNotNull();
                assertThat(copyStatement.getCopyBook().getSourcePath()).isNotNull();
                CobolPreprocessor.CopyBook copyBook = copyStatement.getCopyBook();

                PrintOutputCapture<ExecutionContext> output = new PrintOutputCapture<>(new InMemoryExecutionContext());
                CobolPreprocessorOutputSourcePrinter<ExecutionContext> printer =
                  new CobolPreprocessorOutputSourcePrinter<>(CobolDialect.ibmAnsi85(), true);
                printer.visit(copyBook, output);

                String source = getSource(copyBook.getSourcePath().toString());
                assertThat(source).isEqualTo(output.getOut());

                return super.visitCopyStatement(copyStatement, p);
            }
        }));
    }

    private String getSource(String copyBook) {
        String searchPath = PathUtils.separatorsToUnix(copyBook);
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
          preprocessor(getNistResource("SM101A.CBL"), true)
        );
    }

    @Test
    void sm103A() {
        rewriteRun(
          preprocessor(getNistResource("SM103A.CBL"), true)
        );
    }

    @Test
    void sm105A() {
        rewriteRun(
          preprocessor(getNistResource("SM105A.CBL"), true)
        );
    }

    @Test
    void sm106A() {
        rewriteRun(
          preprocessor(getNistResource("SM106A.CBL"), true)
        );
    }

    @Test
    void sm107A() {
        rewriteRun(
          preprocessor(getNistResource("SM107A.CBL"), true)
        );
    }

    @Test
    void sm207A() {
        rewriteRun(
          preprocessor(getNistResource("SM207A.CBL"), true)
        );
    }

    @Test
    void sm301M() {
        rewriteRun(
          preprocessor(getNistResource("SM301M.CBL"), true)
        );
    }

    @Test
    void sm401M() {
        rewriteRun(
          preprocessor(getNistResource("SM401M.CBL"), true)
        );
    }
}
