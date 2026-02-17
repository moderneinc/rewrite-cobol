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
package org.openrewrite.controlm.tree;

import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.controlm.ControlMIsoVisitor;
import org.openrewrite.controlm.ControlMParser;
import org.openrewrite.test.SourceSpec;
import org.openrewrite.test.SourceSpecs;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

public class ParserAssertions {

    private ParserAssertions() {
    }

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
                ParserAssertions::customizeExecutionContext);
        acceptSpec(spec, controlM);
        return controlM;
    }

    private static void acceptSpec(Consumer<SourceSpec<ControlM.CompilationUnit>> spec, SourceSpec<ControlM.CompilationUnit> controlM) {
        Consumer<ControlM.CompilationUnit> userSuppliedAfterRecipe = controlM.getAfterRecipe();
        controlM.afterRecipe(userSuppliedAfterRecipe::accept);
        isFullyParsed().andThen(spec).accept(controlM);
    }

    public static Consumer<SourceSpec<ControlM.CompilationUnit>> isFullyParsed() {
        return spec -> spec.afterRecipe(cu -> new ControlMIsoVisitor<Integer>() {
            @Override
            public Space visitSpace(Space space, Space.Location loc, Integer integer) {
                assertThat(space.getWhitespace().trim()).isEmpty();
                return super.visitSpace(space, loc, integer);
            }
        }.visit(cu, 0));
    }
}
