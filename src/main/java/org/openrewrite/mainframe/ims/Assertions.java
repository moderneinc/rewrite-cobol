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
package org.openrewrite.mainframe.ims;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.mainframe.ims.tree.Ims;
import org.openrewrite.test.SourceSpec;
import org.openrewrite.test.SourceSpecs;

import java.util.function.Consumer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Assertions {

    static void customizeExecutionContext(ExecutionContext ctx) {
    }

    public static SourceSpecs ims(@Nullable String before) {
        return ims(before, s -> {
        });
    }

    public static SourceSpecs ims(@Nullable String before, Consumer<SourceSpec<Ims.CompilationUnit>> spec) {
        SourceSpec<Ims.CompilationUnit> ims = new SourceSpec<>(
                Ims.CompilationUnit.class, null, ImsParser.builder(), before,
                SourceSpec.ValidateSource.noop,
                Assertions::customizeExecutionContext);
        acceptSpec(spec, ims);
        return ims;
    }

    public static SourceSpecs ims(@Nullable String before, @Nullable String after) {
        return ims(before, after, s -> {
        });
    }

    public static SourceSpecs ims(@Nullable String before, @Nullable String after,
                                  Consumer<SourceSpec<Ims.CompilationUnit>> spec) {
        SourceSpec<Ims.CompilationUnit> ims = new SourceSpec<>(
                Ims.CompilationUnit.class, null, ImsParser.builder(), before,
                SourceSpec.ValidateSource.noop,
                Assertions::customizeExecutionContext).after(s -> after);
        acceptSpec(spec, ims);
        return ims;
    }

    private static void acceptSpec(Consumer<SourceSpec<Ims.CompilationUnit>> spec, SourceSpec<Ims.CompilationUnit> ims) {
        Consumer<Ims.CompilationUnit> userSuppliedAfterRecipe = ims.getAfterRecipe();
        ims.afterRecipe(userSuppliedAfterRecipe::accept);
        spec.accept(ims);
    }
}
