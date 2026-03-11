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
package org.openrewrite.jcl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.jcl.tree.Jcl;
import org.openrewrite.test.SourceSpec;
import org.openrewrite.test.SourceSpecs;

import java.util.function.Consumer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Assertions {

    static void customizeExecutionContext(ExecutionContext ctx) {
    }

    public static SourceSpecs jcl(@Nullable String before) {
        return jcl(before, s -> {
        });
    }

    public static SourceSpecs jcl(@Nullable String before, Consumer<SourceSpec<Jcl.CompilationUnit>> spec) {
        SourceSpec<Jcl.CompilationUnit> jcl = new SourceSpec<>(
                Jcl.CompilationUnit.class, null, JclParser.builder(), before,
                SourceSpec.ValidateSource.noop,
                Assertions::customizeExecutionContext);
        acceptSpec(spec, jcl);
        return jcl;
    }

    public static SourceSpecs jcl(@Nullable String before, @Nullable String after) {
        return jcl(before, after, s -> {
        });
    }

    public static SourceSpecs jcl(@Nullable String before, @Nullable String after,
                                    Consumer<SourceSpec<Jcl.CompilationUnit>> spec) {
        SourceSpec<Jcl.CompilationUnit> jcl = new SourceSpec<>(
                Jcl.CompilationUnit.class, null, JclParser.builder(), before,
                        SourceSpec.ValidateSource.noop,
                        Assertions::customizeExecutionContext).after(s -> after);
        acceptSpec(spec, jcl);
        return jcl;
    }

    private static void acceptSpec(Consumer<SourceSpec<Jcl.CompilationUnit>> spec, SourceSpec<Jcl.CompilationUnit> kotlin) {
        Consumer<Jcl.CompilationUnit> userSuppliedAfterRecipe = kotlin.getAfterRecipe();
        kotlin.afterRecipe(userSuppliedAfterRecipe::accept);
        spec.accept(kotlin);
    }
}
