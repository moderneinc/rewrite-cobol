/*
 * Copyright 2024 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
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
import org.jspecify.annotations.Nullable;
import org.openrewrite.*;
import org.openrewrite.cobol.internal.CobolPrinter;
import org.openrewrite.cobol.internal.IbmAnsi85;
import org.openrewrite.cobol.tree.Cobol;
import org.openrewrite.cobol.tree.CobolPreprocessor;
import org.openrewrite.cobol.tree.Space;
import org.openrewrite.internal.StringUtils;
import org.openrewrite.test.SourceSpec;
import org.openrewrite.test.SourceSpecs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;

public class Assertions {
    private Assertions() {
    }

    static void customizeExecutionContext(ExecutionContext ctx) {
    }

    public static SourceSpecs preprocessor(@Nullable String before) {
        return preprocessor(before, s -> {
        });
    }

    public static SourceSpecs preprocessor(@Nullable String before, Consumer<SourceSpec<CobolPreprocessor.CompilationUnit>> spec) {
        SourceSpec<CobolPreprocessor.CompilationUnit> cobol = new SourceSpec<>(
                CobolPreprocessor.CompilationUnit.class, null,
                CobolPreprocessorParser.builder().copybooks(getCopybookSources()),
                before,
                SourceSpec.ValidateSource.noop,
                Assertions::customizeExecutionContext);
        acceptPreprocessorSpec(spec, cobol);
        return cobol;
    }

    public static SourceSpecs preprocessor(@Nullable String before, @Nullable String after) {
        return preprocessor(before, after, s -> {
        });
    }

    public static SourceSpecs preprocessor(@Nullable String before, @Nullable String after,
                                           Consumer<SourceSpec<CobolPreprocessor.CompilationUnit>> spec) {
        SourceSpec<CobolPreprocessor.CompilationUnit> cobol = new SourceSpec<>(CobolPreprocessor.CompilationUnit.class, null,
                CobolPreprocessorParser.builder(),
                before,
                SourceSpec.ValidateSource.noop,
                Assertions::customizeExecutionContext).after(s -> after);
        acceptPreprocessorSpec(spec, cobol);
        return cobol;
    }

    private static void acceptPreprocessorSpec(Consumer<SourceSpec<CobolPreprocessor.CompilationUnit>> spec, SourceSpec<CobolPreprocessor.CompilationUnit> cobol) {
        Consumer<CobolPreprocessor.CompilationUnit> userSuppliedAfterRecipe = cobol.getAfterRecipe();
        cobol.afterRecipe(userSuppliedAfterRecipe::accept);
        isPreprocessorFullyParsed().andThen(spec).accept(cobol);
    }

    public static Consumer<SourceSpec<CobolPreprocessor.CompilationUnit>> isPreprocessorFullyParsed() {
        return spec -> spec.afterRecipe(cu -> new CobolPreprocessorIsoVisitor<Integer>() {
            @Override
            public Space visitSpace(Space space, Space.Location loc, Integer integer) {
                String whitespace = space.getWhitespace().trim();
                if (!(IbmAnsi85.getInstance().getSeparators().contains(whitespace + " ") || whitespace.isEmpty())) {
                    return space.withWhitespace("(~~>" + whitespace + "<~~)");
                }
                return super.visitSpace(space, loc, integer);
            }
        }.visit(cu, 0));
    }

    public static SourceSpecs copybook(@Nullable String before) {
        return copybook(before, s -> {
        });
    }

    public static SourceSpecs copybook(@Nullable String before, Consumer<SourceSpec<CobolPreprocessor.Copybook>> spec) {
        SourceSpec<CobolPreprocessor.Copybook> cobol = new SourceSpec<>(
                CobolPreprocessor.Copybook.class, null,
                CopybookParser.builder(),
                before,
                SourceSpec.ValidateSource.noop,
                Assertions::customizeExecutionContext);
        acceptCopybookSpec(spec, cobol);
        return cobol;
    }

    public static SourceSpecs copybook(@Nullable String before, @Nullable String after) {
        return copybook(before, after, s -> {
        });
    }

    public static SourceSpecs copybook(@Nullable String before, @Nullable String after,
                                           Consumer<SourceSpec<CobolPreprocessor.Copybook>> spec) {
        SourceSpec<CobolPreprocessor.Copybook> cobol = new SourceSpec<>(CobolPreprocessor.Copybook.class, null,
                CopybookParser.builder(),
                before,
                SourceSpec.ValidateSource.noop,
                Assertions::customizeExecutionContext).after(s -> after);
        acceptCopybookSpec(spec, cobol);
        return cobol;
    }

    private static void acceptCopybookSpec(Consumer<SourceSpec<CobolPreprocessor.Copybook>> spec, SourceSpec<CobolPreprocessor.Copybook> cobol) {
        Consumer<CobolPreprocessor.Copybook> userSuppliedAfterRecipe = cobol.getAfterRecipe();
        cobol.afterRecipe(userSuppliedAfterRecipe::accept);
        isCopybookFullyParsed().andThen(spec).accept(cobol);
    }

    public static Consumer<SourceSpec<CobolPreprocessor.Copybook>> isCopybookFullyParsed() {
        return spec -> spec.afterRecipe(cu -> new CobolPreprocessorIsoVisitor<Integer>() {
            @Override
            public Space visitSpace(Space space, Space.Location loc, Integer integer) {
                String whitespace = space.getWhitespace().trim();
                if (!(IbmAnsi85.getInstance().getSeparators().contains(whitespace + " ") || whitespace.isEmpty())) {
                    return space.withWhitespace("(~~>" + whitespace + "<~~)");
                }
                return super.visitSpace(space, loc, integer);
            }
        }.visit(cu, 0));
    }

    public static SourceSpecs cobol(@Nullable String before) {
        return cobol(before, s -> {
        });
    }

    public static SourceSpecs cobol(@Nullable String before, Consumer<SourceSpec<Cobol.CompilationUnit>> spec) {
        List<SourceFile> copybooks = getCopybookSources();
        SourceSpec<Cobol.CompilationUnit> cobol = new SourceSpec<>(
                Cobol.CompilationUnit.class, null,
                CobolParser.builder().copybooks(copybooks),
                before,
                SourceSpec.ValidateSource.noop,
                Assertions::customizeExecutionContext);
        acceptSpec(spec, cobol);
        return cobol;
    }

    public static SourceSpecs cobol(@Nullable String before, @Nullable String after) {
        return cobol(before, after, spec -> {
        });
    }

    public static SourceSpecs cobol(@Nullable String before, @Nullable String after,
                                    Consumer<SourceSpec<Cobol.CompilationUnit>> spec) {
        List<SourceFile> copybooks = getCopybookSources();
        SourceSpec<Cobol.CompilationUnit> cobol =
                new SourceSpec<>(Cobol.CompilationUnit.class,
                        null,
                        CobolParser.builder().copybooks(copybooks),
                        before,
                        SourceSpec.ValidateSource.noop,
                        Assertions::customizeExecutionContext).after(s -> after);
        acceptSpec(spec, cobol);
        return cobol;
    }

    public static SourceSpecs cobolPostProcess(@Nullable String before) {
        return cobolPostProcess(before, doesNotExist());
    }

    public static SourceSpecs cobolPostProcess(@Nullable String before, @Nullable String after) {
        return cobolPostProcess(before, after, s -> {
        });
    }

    public static SourceSpecs cobolPostProcess(@Nullable String before, @Nullable String after,
                                               Consumer<SourceSpec<Cobol.CompilationUnit>> spec) {
        List<SourceFile> copybooks = getCopybookSources();
        SourceSpec<Cobol.CompilationUnit> cobol = new SourceSpec<>(
                Cobol.CompilationUnit.class, null,
                CobolParser.builder().copybooks(copybooks),
                before,
                SourceSpec.ValidateSource.noop,
                Assertions::customizeExecutionContext);
        isFullyParsed().andThen(isPostProcessedLst(after)).andThen(spec).accept(cobol);
        return cobol;
    }

    public static @Nullable String doesNotExist() {
        return null;
    }

    private static void acceptSpec(Consumer<SourceSpec<Cobol.CompilationUnit>> spec, SourceSpec<Cobol.CompilationUnit> cobol) {
        Consumer<Cobol.CompilationUnit> userSuppliedAfterRecipe = cobol.getAfterRecipe();
        cobol.afterRecipe(userSuppliedAfterRecipe::accept);
        isFullyParsed().andThen(spec).accept(cobol);
    }

    public static Consumer<SourceSpec<Cobol.CompilationUnit>> isFullyParsed() {
        return spec -> spec.afterRecipe(cu -> new CobolPreprocessorIsoVisitor<Integer>() {
            @Override
            public Space visitSpace(Space space, Space.Location loc, Integer integer) {
                String whitespace = space.getWhitespace().trim();
                if (!(IbmAnsi85.getInstance().getSeparators().contains(whitespace + " ") || whitespace.isEmpty())) {
                    return space.withWhitespace("(~~>" + whitespace + "<~~)");
                }
                return super.visitSpace(space, loc, integer);
            }
        }.visit(cu, 0));
    }

    public static Consumer<SourceSpec<Cobol.CompilationUnit>> isPostProcessedLst(@Nullable String expectedLst) {
        return spec -> spec.afterRecipe(cu -> {
            CobolPrinter<ExecutionContext> printer = new CobolPrinter<>(false, false);
            PrintOutputCapture<ExecutionContext> outputCapture = new PrintOutputCapture<>(new InMemoryExecutionContext());
            printer.visit(cu, outputCapture);
            if (expectedLst != null && !trimTrailingSpaces(StringUtils.trimIndentPreserveCRLF(outputCapture.getOut())).equals(expectedLst)) {
                System.out.println("Expected LST of length " + expectedLst.length() + ":");
                System.out.println(expectedLst);
                System.out.println("Actual LST of length " + trimTrailingSpaces(StringUtils.trimIndentPreserveCRLF(outputCapture.getOut())).length() + ":");
                System.out.println(outputCapture.getOut());
            }
            assert expectedLst == null || trimTrailingSpaces(StringUtils.trimIndentPreserveCRLF(outputCapture.getOut())).equals(expectedLst);
        });
    }

    // Trim trailing whitespaces for each line
    public static String trimTrailingSpaces(String input) {
        StringBuilder result = new StringBuilder();
        String[] lines = input.split("\\r?\\n");
        for (String line : lines) {
            String trimmedLine = line.replaceAll("\\s+$", "");
            result.append(trimmedLine).append("\n");
        }
        return result.toString();
    }

    private static List<SourceFile> getCopybookSources() {
        try (ScanResult scan = new ClassGraph().scan()) {
            List<Parser.Input> copyInputs = scan.getResourcesWithExtension("cpy").stream()
                    .map(res -> new Parser.Input(Paths.get(res.getPath()), () -> {
                        try {
                            return new ByteArrayInputStream(res.getContentAsString().getBytes());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }))
                    .collect(toList());
            return CopybookParser.builder().build()
                    .parseInputs(copyInputs, null, new InMemoryExecutionContext())
                    .collect(toList());
        }
    }
}
