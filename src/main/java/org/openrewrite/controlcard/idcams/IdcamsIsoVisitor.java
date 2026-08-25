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
package org.openrewrite.controlcard.idcams;

import org.openrewrite.controlcard.idcams.tree.Idcams;

public class IdcamsIsoVisitor<P> extends IdcamsVisitor<P> {

    @Override
    public Idcams.CompilationUnit visitCompilationUnit(Idcams.CompilationUnit compilationUnit, P p) {
        return (Idcams.CompilationUnit) super.visitCompilationUnit(compilationUnit, p);
    }

    @Override
    public Idcams.Command visitCommand(Idcams.Command command, P p) {
        return (Idcams.Command) super.visitCommand(command, p);
    }

    @Override
    public Idcams.Parameter visitParameter(Idcams.Parameter parameter, P p) {
        return (Idcams.Parameter) super.visitParameter(parameter, p);
    }

    @Override
    public Idcams.Word visitWord(Idcams.Word word, P p) {
        return (Idcams.Word) super.visitWord(word, p);
    }
}
