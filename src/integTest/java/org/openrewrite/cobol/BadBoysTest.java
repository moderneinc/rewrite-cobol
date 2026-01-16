/*
 * Copyright 2024 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
