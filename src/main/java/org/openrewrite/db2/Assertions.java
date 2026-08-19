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
package org.openrewrite.db2;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.db2.tree.Db2;
import org.openrewrite.test.SourceSpec;
import org.openrewrite.test.SourceSpecs;

import java.util.function.Consumer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Assertions {

    static void customizeExecutionContext(ExecutionContext ctx) {
    }

    public static SourceSpecs db2(@Nullable String before) {
        return db2(before, s -> {
        });
    }

    public static SourceSpecs db2(@Nullable String before, Consumer<SourceSpec<Db2.CompilationUnit>> spec) {
        SourceSpec<Db2.CompilationUnit> db2 = new SourceSpec<>(
                Db2.CompilationUnit.class, null, Db2Parser.builder(), before,
                SourceSpec.ValidateSource.noop,
                Assertions::customizeExecutionContext);
        acceptSpec(spec, db2);
        return db2;
    }

    public static SourceSpecs db2(@Nullable String before, @Nullable String after) {
        return db2(before, after, s -> {
        });
    }

    public static SourceSpecs db2(@Nullable String before, @Nullable String after,
                                  Consumer<SourceSpec<Db2.CompilationUnit>> spec) {
        SourceSpec<Db2.CompilationUnit> db2 = new SourceSpec<>(
                Db2.CompilationUnit.class, null, Db2Parser.builder(), before,
                SourceSpec.ValidateSource.noop,
                Assertions::customizeExecutionContext).after(s -> after);
        acceptSpec(spec, db2);
        return db2;
    }

    private static void acceptSpec(Consumer<SourceSpec<Db2.CompilationUnit>> spec, SourceSpec<Db2.CompilationUnit> db2) {
        Consumer<Db2.CompilationUnit> userSuppliedAfterRecipe = db2.getAfterRecipe();
        db2.afterRecipe(userSuppliedAfterRecipe::accept);
        spec.accept(db2);
    }
}
