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
package org.openrewrite.listload;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.listload.tree.ListLoad;
import org.openrewrite.test.SourceSpec;
import org.openrewrite.test.SourceSpecs;

import java.util.function.Consumer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Assertions {

    static void customizeExecutionContext(ExecutionContext ctx) {
    }

    public static SourceSpecs listLoad(@Nullable String before) {
        return listLoad(before, s -> {
        });
    }

    public static SourceSpecs listLoad(@Nullable String before, Consumer<SourceSpec<ListLoad.CompilationUnit>> spec) {
        SourceSpec<ListLoad.CompilationUnit> listLoad = new SourceSpec<>(
                ListLoad.CompilationUnit.class, null, ListLoadParser.builder(), before,
                SourceSpec.ValidateSource.noop,
                Assertions::customizeExecutionContext);
        acceptSpec(spec, listLoad);
        return listLoad;
    }

    private static void acceptSpec(Consumer<SourceSpec<ListLoad.CompilationUnit>> spec, SourceSpec<ListLoad.CompilationUnit> listLoad) {
        Consumer<ListLoad.CompilationUnit> userSuppliedAfterRecipe = listLoad.getAfterRecipe();
        listLoad.afterRecipe(userSuppliedAfterRecipe::accept);
        spec.accept(listLoad);
    }
}
