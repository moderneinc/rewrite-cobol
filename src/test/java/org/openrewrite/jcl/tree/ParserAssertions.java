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
package org.openrewrite.jcl.tree;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.jcl.JclIsoVisitor;
import org.openrewrite.jcl.JclParser;
import org.openrewrite.test.SourceSpec;
import org.openrewrite.test.SourceSpecs;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ParserAssertions {

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
                ParserAssertions::customizeExecutionContext);
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
                ParserAssertions::customizeExecutionContext).after(s -> after);
        acceptSpec(spec, jcl);
        return jcl;
    }

    private static void acceptSpec(Consumer<SourceSpec<Jcl.CompilationUnit>> spec, SourceSpec<Jcl.CompilationUnit> jcl) {
        Consumer<Jcl.CompilationUnit> userSuppliedAfterRecipe = jcl.getAfterRecipe();
        jcl.afterRecipe(userSuppliedAfterRecipe::accept);
        isFullyParsed().andThen(spec).accept(jcl);
    }

    public static Consumer<SourceSpec<Jcl.CompilationUnit>> isFullyParsed() {
        return spec -> spec.afterRecipe(cu -> new JclIsoVisitor<Integer>() {
            @Override
            public Space visitSpace(Space space, Space.Location loc, Integer integer) {
                assertThat(space.getWhitespace().trim()).isEmpty();
                return super.visitSpace(space, loc, integer);
            }
        }.visit(cu, 0));
    }
}
