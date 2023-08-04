/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.cobol;

import org.junit.jupiter.api.Test;
import org.openrewrite.ExecutionContext;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.PrintOutputCapture;
import org.openrewrite.cobol.internal.CobolDialect;
import org.openrewrite.cobol.internal.CobolPreprocessorOutputSourcePrinter;

import static org.openrewrite.cobol.Assertions.preprocessor;

public class BadBoysTest extends CobolTest {

    @Test
    void NC246A() {
        rewriteRun(
//          cobol(getNistResource("NC246A.CBL")),
          preprocessor(
            getNistResource("NC246A.CBL"),
            spec -> spec.beforeRecipe(cu -> {
                PrintOutputCapture<ExecutionContext> cobolParserOutput = new PrintOutputCapture<>(new InMemoryExecutionContext());
                CobolPreprocessorOutputSourcePrinter<ExecutionContext> printWithoutColumns = new CobolPreprocessorOutputSourcePrinter<>(CobolDialect.ibmAnsi85(), false);
                printWithoutColumns.visit(cu, cobolParserOutput);
                System.out.println(cobolParserOutput.getOut());
            }))
        );
    }
}
