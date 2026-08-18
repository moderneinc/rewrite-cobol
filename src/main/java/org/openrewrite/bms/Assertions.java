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
package org.openrewrite.bms;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.bms.tree.Bms;
import org.openrewrite.test.SourceSpec;
import org.openrewrite.test.SourceSpecs;

import java.util.function.Consumer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Assertions {

    static void customizeExecutionContext(ExecutionContext ctx) {
    }

    public static SourceSpecs bms(@Nullable String before) {
        return bms(before, s -> {
        });
    }

    public static SourceSpecs bms(@Nullable String before, Consumer<SourceSpec<Bms.CompilationUnit>> spec) {
        SourceSpec<Bms.CompilationUnit> bms = new SourceSpec<>(
                Bms.CompilationUnit.class, null, BmsParser.builder(), before,
                SourceSpec.ValidateSource.noop,
                Assertions::customizeExecutionContext);
        acceptSpec(spec, bms);
        return bms;
    }

    public static SourceSpecs bms(@Nullable String before, @Nullable String after) {
        return bms(before, after, s -> {
        });
    }

    public static SourceSpecs bms(@Nullable String before, @Nullable String after,
                                  Consumer<SourceSpec<Bms.CompilationUnit>> spec) {
        SourceSpec<Bms.CompilationUnit> bms = new SourceSpec<>(
                Bms.CompilationUnit.class, null, BmsParser.builder(), before,
                SourceSpec.ValidateSource.noop,
                Assertions::customizeExecutionContext).after(s -> after);
        acceptSpec(spec, bms);
        return bms;
    }

    private static void acceptSpec(Consumer<SourceSpec<Bms.CompilationUnit>> spec, SourceSpec<Bms.CompilationUnit> bms) {
        Consumer<Bms.CompilationUnit> userSuppliedAfterRecipe = bms.getAfterRecipe();
        bms.afterRecipe(userSuppliedAfterRecipe::accept);
        spec.accept(bms);
    }
}
