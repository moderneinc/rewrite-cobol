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
package org.openrewrite.controlcard.idcams;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.controlcard.idcams.tree.Idcams;
import org.openrewrite.test.SourceSpec;
import org.openrewrite.test.SourceSpecs;

import java.util.function.Consumer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Assertions {

    static void customizeExecutionContext(ExecutionContext ctx) {
    }

    public static SourceSpecs idcamsCard(@Nullable String before) {
        return idcamsCard(before, s -> {
        });
    }

    public static SourceSpecs idcamsCard(@Nullable String before, Consumer<SourceSpec<Idcams.CompilationUnit>> spec) {
        SourceSpec<Idcams.CompilationUnit> idcams = new SourceSpec<>(
                Idcams.CompilationUnit.class, null, IdcamsParser.builder(), before,
                SourceSpec.ValidateSource.noop,
                Assertions::customizeExecutionContext);
        acceptSpec(spec, idcams);
        return idcams;
    }

    public static SourceSpecs idcamsCard(@Nullable String before, @Nullable String after) {
        return idcamsCard(before, after, s -> {
        });
    }

    public static SourceSpecs idcamsCard(@Nullable String before, @Nullable String after,
                                         Consumer<SourceSpec<Idcams.CompilationUnit>> spec) {
        SourceSpec<Idcams.CompilationUnit> idcams = new SourceSpec<>(
                Idcams.CompilationUnit.class, null, IdcamsParser.builder(), before,
                SourceSpec.ValidateSource.noop,
                Assertions::customizeExecutionContext).after(s -> after);
        acceptSpec(spec, idcams);
        return idcams;
    }

    private static void acceptSpec(Consumer<SourceSpec<Idcams.CompilationUnit>> spec, SourceSpec<Idcams.CompilationUnit> idcams) {
        Consumer<Idcams.CompilationUnit> userSuppliedAfterRecipe = idcams.getAfterRecipe();
        idcams.afterRecipe(userSuppliedAfterRecipe::accept);
        spec.accept(idcams);
    }
}
