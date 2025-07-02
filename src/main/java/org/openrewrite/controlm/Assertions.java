/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.controlm;

import org.openrewrite.ExecutionContext;
import org.openrewrite.controlm.tree.ControlM;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.test.SourceSpec;
import org.openrewrite.test.SourceSpecs;

import java.util.function.Consumer;

public final class Assertions {
    private Assertions() {
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
