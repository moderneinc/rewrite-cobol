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
package org.openrewrite.textmember;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Parser;
import org.openrewrite.test.SourceSpec;
import org.openrewrite.test.SourceSpecs;
import org.openrewrite.textmember.tree.TextMember;

import java.util.function.Consumer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Assertions {

    static void customizeExecutionContext(ExecutionContext ctx) {
    }

    public static SourceSpecs rexx(@Nullable String before) {
        return rexx(before, s -> {
        });
    }

    public static SourceSpecs rexx(@Nullable String before, Consumer<SourceSpec<TextMember.CompilationUnit>> spec) {
        return member(RexxParser.builder(), before, spec);
    }

    public static SourceSpecs clist(@Nullable String before) {
        return clist(before, s -> {
        });
    }

    public static SourceSpecs clist(@Nullable String before, Consumer<SourceSpec<TextMember.CompilationUnit>> spec) {
        return member(ClistParser.builder(), before, spec);
    }

    public static SourceSpecs document(@Nullable String before) {
        return document(before, s -> {
        });
    }

    public static SourceSpecs document(@Nullable String before, Consumer<SourceSpec<TextMember.CompilationUnit>> spec) {
        return member(DocumentParser.builder(), before, spec);
    }

    public static SourceSpecs cSource(@Nullable String before) {
        return member(CParser.builder(), before, s -> {
        });
    }

    public static SourceSpecs pliSource(@Nullable String before) {
        return member(PliParser.builder(), before, s -> {
        });
    }

    private static SourceSpecs member(Parser.Builder parser, @Nullable String before,
                                      Consumer<SourceSpec<TextMember.CompilationUnit>> spec) {
        SourceSpec<TextMember.CompilationUnit> member = new SourceSpec<>(
                TextMember.CompilationUnit.class, null, parser, before,
                SourceSpec.ValidateSource.noop,
                Assertions::customizeExecutionContext);
        Consumer<TextMember.CompilationUnit> userSuppliedAfterRecipe = member.getAfterRecipe();
        member.afterRecipe(userSuppliedAfterRecipe::accept);
        spec.accept(member);
        return member;
    }
}
