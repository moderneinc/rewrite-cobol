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
import org.openrewrite.ExecutionContext;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Parser;
import org.openrewrite.SourceFile;
import org.openrewrite.cobol.tree.CobolPreprocessor;
import org.openrewrite.test.SourceSpec;
import org.openrewrite.test.SourceSpecs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;

public class PreprocessorAssertions {
    private PreprocessorAssertions() {
    }

    static void customizeExecutionContext(ExecutionContext ctx) {
    }

    public static SourceSpecs cobolPreprocess(@Nullable String before) {
        return cobolPreprocess(before, s -> {
        });
    }

    public static SourceSpecs cobolPreprocess(@Nullable String before, Consumer<SourceSpec<CobolPreprocessor.CompilationUnit>> spec) {
        SourceSpec<CobolPreprocessor.CompilationUnit> cobol = new SourceSpec<>(
                CobolPreprocessor.CompilationUnit.class, null,
                CobolPreprocessorParser.builder(),
                before,
                SourceSpec.ValidateSource.noop,
                PreprocessorAssertions::customizeExecutionContext);
        acceptPreprocessorSpec(spec, cobol);
        return cobol;
    }

    public static SourceSpecs cobolPreprocess(@Nullable String before, @Nullable String after) {
        return cobolPreprocess(before, after, s -> {
        });
    }

    public static SourceSpecs cobolPreprocess(@Nullable String before, @Nullable String after,
                                              Consumer<SourceSpec<CobolPreprocessor.CompilationUnit>> spec) {
        SourceSpec<CobolPreprocessor.CompilationUnit> cobol = new SourceSpec<>(CobolPreprocessor.CompilationUnit.class, null,
                CobolPreprocessorParser.builder(),
                before,
                SourceSpec.ValidateSource.noop,
                PreprocessorAssertions::customizeExecutionContext).after(s -> after);
        acceptPreprocessorSpec(spec, cobol);
        return cobol;
    }

    public static SourceSpecs cobolPreprocessorCopy(@Nullable String before) {
        return cobolPreprocessorCopy(before, s -> {
        });
    }

    public static SourceSpecs cobolPreprocessorCopy(@Nullable String before, Consumer<SourceSpec<CobolPreprocessor.CompilationUnit>> spec) {
        List<SourceFile> copybooks = getCopybookSources();

        SourceSpec<CobolPreprocessor.CompilationUnit> cobol = new SourceSpec<>(CobolPreprocessor.CompilationUnit.class, null,
                CobolPreprocessorParser.builder()
                        .copybooks(copybooks),
                before,
                SourceSpec.ValidateSource.noop,
                PreprocessorAssertions::customizeExecutionContext);
        acceptPreprocessorSpec(spec, cobol);
        return cobol;
    }

    public static SourceSpecs cobolPreprocessorCopy(@Nullable String before, @Nullable String after) {
        return cobolPreprocessorCopy(before, after, s -> {
        });
    }

    public static SourceSpecs cobolPreprocessorCopy(@Nullable String before, @Nullable String after,
                                                    Consumer<SourceSpec<CobolPreprocessor.CompilationUnit>> spec) {
        List<SourceFile> copybooks = getCopybookSources();

        SourceSpec<CobolPreprocessor.CompilationUnit> cobol = new SourceSpec<>(CobolPreprocessor.CompilationUnit.class, null,
                CobolPreprocessorParser.builder()
                        .copybooks(copybooks),
                before,
                SourceSpec.ValidateSource.noop,
                PreprocessorAssertions::customizeExecutionContext).after(s -> after);
        acceptPreprocessorSpec(spec, cobol);
        return cobol;
    }

    private static void acceptPreprocessorSpec(Consumer<SourceSpec<CobolPreprocessor.CompilationUnit>> spec, SourceSpec<CobolPreprocessor.CompilationUnit> cobol) {
        Consumer<CobolPreprocessor.CompilationUnit> userSuppliedAfterRecipe = cobol.getAfterRecipe();
        cobol.afterRecipe(userSuppliedAfterRecipe::accept);
        spec.accept(cobol);
    }

    private static List<SourceFile> getCopybookSources() {
        try(ScanResult scan = new ClassGraph().scan()) {
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
