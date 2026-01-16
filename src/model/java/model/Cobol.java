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
package model;

import generate.Skip;

public interface Cobol {

    @Skip
    class ArithmeticExpression {}
    @Skip
    class Subscript {}
    @Skip
    class CobolWord {}
    @Skip
    class QualifiedDataName {}
    @Skip
    class StatementPhrase {}
    @Skip
    class Parenthesized {}
    @Skip
    class Condition {}
    @Skip
    class ProcedureName {}
    @Skip
    class PictureString {}

    // We made the mistake of removing the model object code because code generation was very slow due to the number of AST elements.
    // Requires manually re-adding the model objects.
}
