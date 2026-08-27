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
package org.openrewrite.sas;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.sas.tree.Sas;
import org.openrewrite.test.SourceSpec;
import org.openrewrite.test.SourceSpecs;

import java.util.function.Consumer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Assertions {

    static void customizeExecutionContext(ExecutionContext ctx) {
    }

    public static SourceSpecs sas(@Nullable String before) {
        return sas(before, s -> {
        });
    }

    public static SourceSpecs sas(@Nullable String before, Consumer<SourceSpec<Sas.CompilationUnit>> spec) {
        SourceSpec<Sas.CompilationUnit> sas = new SourceSpec<>(
                Sas.CompilationUnit.class, null, SasParser.builder(), before,
                SourceSpec.ValidateSource.noop,
                Assertions::customizeExecutionContext);
        acceptSpec(spec, sas);
        return sas;
    }

    public static SourceSpecs sas(@Nullable String before, @Nullable String after) {
        return sas(before, after, s -> {
        });
    }

    public static SourceSpecs sas(@Nullable String before, @Nullable String after,
                                  Consumer<SourceSpec<Sas.CompilationUnit>> spec) {
        SourceSpec<Sas.CompilationUnit> sas = new SourceSpec<>(
                Sas.CompilationUnit.class, null, SasParser.builder(), before,
                SourceSpec.ValidateSource.noop,
                Assertions::customizeExecutionContext).after(s -> after);
        acceptSpec(spec, sas);
        return sas;
    }

    private static void acceptSpec(Consumer<SourceSpec<Sas.CompilationUnit>> spec,
                                   SourceSpec<Sas.CompilationUnit> sas) {
        Consumer<Sas.CompilationUnit> userSuppliedAfterRecipe = sas.getAfterRecipe();
        sas.afterRecipe(userSuppliedAfterRecipe::accept);
        // An INPUT layout is written in columns, so a source is taken exactly as it was written
        // rather than with its common indentation taken off.
        sas.noTrim();
        spec.accept(sas);
    }
}
