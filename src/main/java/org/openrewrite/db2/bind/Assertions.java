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
package org.openrewrite.db2.bind;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.db2.bind.tree.Bind;
import org.openrewrite.test.SourceSpec;
import org.openrewrite.test.SourceSpecs;

import java.util.function.Consumer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Assertions {

    static void customizeExecutionContext(ExecutionContext ctx) {
    }

    public static SourceSpecs bind(@Nullable String before) {
        return bind(before, s -> {
        });
    }

    public static SourceSpecs bind(@Nullable String before, Consumer<SourceSpec<Bind.CompilationUnit>> spec) {
        SourceSpec<Bind.CompilationUnit> bind = new SourceSpec<>(
                Bind.CompilationUnit.class, null, BindParser.builder(), before,
                SourceSpec.ValidateSource.noop,
                Assertions::customizeExecutionContext);
        acceptSpec(spec, bind);
        return bind;
    }

    public static SourceSpecs bind(@Nullable String before, @Nullable String after) {
        return bind(before, after, s -> {
        });
    }

    public static SourceSpecs bind(@Nullable String before, @Nullable String after,
                                  Consumer<SourceSpec<Bind.CompilationUnit>> spec) {
        SourceSpec<Bind.CompilationUnit> bind = new SourceSpec<>(
                Bind.CompilationUnit.class, null, BindParser.builder(), before,
                SourceSpec.ValidateSource.noop,
                Assertions::customizeExecutionContext).after(s -> after);
        acceptSpec(spec, bind);
        return bind;
    }

    private static void acceptSpec(Consumer<SourceSpec<Bind.CompilationUnit>> spec, SourceSpec<Bind.CompilationUnit> bind) {
        Consumer<Bind.CompilationUnit> userSuppliedAfterRecipe = bind.getAfterRecipe();
        bind.afterRecipe(userSuppliedAfterRecipe::accept);
        spec.accept(bind);
    }
}
