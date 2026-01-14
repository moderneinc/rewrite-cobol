/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
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
