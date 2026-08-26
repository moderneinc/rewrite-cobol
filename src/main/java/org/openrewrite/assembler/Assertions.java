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
package org.openrewrite.assembler;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.assembler.tree.Assembler;
import org.openrewrite.test.SourceSpec;
import org.openrewrite.test.SourceSpecs;

import java.util.function.Consumer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Assertions {

    static void customizeExecutionContext(ExecutionContext ctx) {
    }

    public static SourceSpecs assembler(@Nullable String before) {
        return assembler(before, s -> {
        });
    }

    public static SourceSpecs assembler(@Nullable String before, Consumer<SourceSpec<Assembler.CompilationUnit>> spec) {
        SourceSpec<Assembler.CompilationUnit> assembler = new SourceSpec<>(
                Assembler.CompilationUnit.class, null, AssemblerParser.builder(), before,
                SourceSpec.ValidateSource.noop,
                Assertions::customizeExecutionContext);
        acceptSpec(spec, assembler);
        return assembler;
    }

    public static SourceSpecs assembler(@Nullable String before, @Nullable String after) {
        return assembler(before, after, s -> {
        });
    }

    public static SourceSpecs assembler(@Nullable String before, @Nullable String after,
                                        Consumer<SourceSpec<Assembler.CompilationUnit>> spec) {
        SourceSpec<Assembler.CompilationUnit> assembler = new SourceSpec<>(
                Assembler.CompilationUnit.class, null, AssemblerParser.builder(), before,
                SourceSpec.ValidateSource.noop,
                Assertions::customizeExecutionContext).after(s -> after);
        acceptSpec(spec, assembler);
        return assembler;
    }

    private static void acceptSpec(Consumer<SourceSpec<Assembler.CompilationUnit>> spec,
                                   SourceSpec<Assembler.CompilationUnit> assembler) {
        Consumer<Assembler.CompilationUnit> userSuppliedAfterRecipe = assembler.getAfterRecipe();
        assembler.afterRecipe(userSuppliedAfterRecipe::accept);
        // The columns are the syntax, so a source is taken exactly as it was written rather than with
        // its common indentation taken off.
        assembler.noTrim();
        spec.accept(assembler);
    }
}
