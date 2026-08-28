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
package org.openrewrite.mainframe.jcl.tree;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.mainframe.jcl.JclIsoVisitor;
import org.openrewrite.mainframe.jcl.JclParser;
import org.openrewrite.test.SourceSpec;
import org.openrewrite.test.SourceSpecs;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ParserAssertions {

    static void customizeExecutionContext(ExecutionContext ctx) {
    }

    /**
     * Writes an external PDS member (e.g. a {@code .prm} member) to a temp file named by member
     * name and returns its path, for resolution by
     * {@link org.openrewrite.mainframe.jcl.ExpandExternalSysinVisitor}.
     */
    public static Path parmMember(String memberName, String content) {
        try {
            Path dir = Files.createTempDirectory("jcl-parm");
            dir.toFile().deleteOnExit();
            Path file = dir.resolve(memberName + ".prm");
            Files.write(file, content.getBytes(StandardCharsets.UTF_8));
            file.toFile().deleteOnExit();
            return file;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static SourceSpecs jcl(@Nullable String before, List<Path> parmMembers,
                                  Consumer<SourceSpec<Jcl.CompilationUnit>> spec) {
        SourceSpec<Jcl.CompilationUnit> jcl = new SourceSpec<>(
                Jcl.CompilationUnit.class, null, JclParser.builder().parmMembers(parmMembers), before,
                SourceSpec.ValidateSource.noop,
                ParserAssertions::customizeExecutionContext);
        acceptSpec(spec, jcl);
        return jcl;
    }

    /**
     * Writes a member of the procedure library — a procedure or an INCLUDE group — to a temp file
     * named by member name and returns its path, for resolution by
     * {@link org.openrewrite.mainframe.jcl.ExpandJobVisitor}.
     */
    public static Path procedureMember(String memberName, String content) {
        try {
            Path dir = Files.createTempDirectory("jcl-proc");
            dir.toFile().deleteOnExit();
            Path file = dir.resolve(memberName + ".prc");
            Files.write(file, content.getBytes(StandardCharsets.UTF_8));
            file.toFile().deleteOnExit();
            return file;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static SourceSpecs jclWithProcedures(@Nullable String before, List<Path> procedureLibrary,
                                                Consumer<SourceSpec<Jcl.CompilationUnit>> spec) {
        SourceSpec<Jcl.CompilationUnit> jcl = new SourceSpec<>(
                Jcl.CompilationUnit.class, null, JclParser.builder().procedureLibrary(procedureLibrary),
                before, SourceSpec.ValidateSource.noop,
                ParserAssertions::customizeExecutionContext);
        acceptSpec(spec, jcl);
        return jcl;
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
