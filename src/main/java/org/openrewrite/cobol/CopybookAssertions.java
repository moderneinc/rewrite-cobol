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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.cobol.tree.CobolPreprocessor;
import org.openrewrite.test.SourceSpec;
import org.openrewrite.test.SourceSpecs;

import java.util.function.Consumer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CopybookAssertions {

    static void customizeExecutionContext(ExecutionContext ctx) {
    }

    public static SourceSpecs copybook(@Nullable String before) {
        return copybook(before, s -> {
        });
    }

    public static SourceSpecs copybook(@Nullable String before, Consumer<SourceSpec<CobolPreprocessor.Copybook>> spec) {
        SourceSpec<CobolPreprocessor.Copybook> copybook = new SourceSpec<>(CobolPreprocessor.Copybook.class, null,
                CopybookParser.builder(),
                before,
                SourceSpec.ValidateSource.noop,
                CopybookAssertions::customizeExecutionContext);
        acceptSpec(spec, copybook);
        return copybook;
    }

    public static SourceSpecs copybook(@Nullable String before, @Nullable String after) {
        return copybook(before, after, s -> {
        });
    }

    public static SourceSpecs copybook(@Nullable String before, @Nullable String after,
                                       Consumer<SourceSpec<CobolPreprocessor.Copybook>> spec) {
        SourceSpec<CobolPreprocessor.Copybook> copybook = new SourceSpec<>(CobolPreprocessor.Copybook.class, null,
                CopybookParser.builder(),
                before,
                SourceSpec.ValidateSource.noop,
                CopybookAssertions::customizeExecutionContext).after(s -> after);
        acceptSpec(spec, copybook);
        return copybook;
    }

    private static void acceptSpec(Consumer<SourceSpec<CobolPreprocessor.Copybook>> spec, SourceSpec<CobolPreprocessor.Copybook> cobol) {
        Consumer<CobolPreprocessor.Copybook> userSuppliedAfterRecipe = cobol.getAfterRecipe();
        cobol.afterRecipe(userSuppliedAfterRecipe::accept);
        spec.accept(cobol);
    }
}
