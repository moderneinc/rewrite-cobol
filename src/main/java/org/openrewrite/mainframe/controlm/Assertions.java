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
package org.openrewrite.mainframe.controlm;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.mainframe.controlm.tree.ControlM;
import org.openrewrite.test.SourceSpec;
import org.openrewrite.test.SourceSpecs;

import java.util.function.Consumer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Assertions {

    static void customizeExecutionContext(ExecutionContext ctx) {
    }

    public static SourceSpecs controlM(@Nullable String before) {
        return controlM(before, s -> {
        });
    }

    public static SourceSpecs controlM(@Nullable String before, Consumer<SourceSpec<ControlM.CompilationUnit>> spec) {
        SourceSpec<ControlM.CompilationUnit> controlM = new SourceSpec<>(
                ControlM.CompilationUnit.class, null, ControlMParser.builder(), before,
                SourceSpec.ValidateSource.noop,
                Assertions::customizeExecutionContext);
        acceptSpec(spec, controlM);
        return controlM;
    }

    private static void acceptSpec(Consumer<SourceSpec<ControlM.CompilationUnit>> spec, SourceSpec<ControlM.CompilationUnit> controlM) {
        Consumer<ControlM.CompilationUnit> userSuppliedAfterRecipe = controlM.getAfterRecipe();
        controlM.afterRecipe(userSuppliedAfterRecipe::accept);
        spec.accept(controlM);
    }
}
