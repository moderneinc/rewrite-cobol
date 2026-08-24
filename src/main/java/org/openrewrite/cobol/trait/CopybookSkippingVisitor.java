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
package org.openrewrite.cobol.trait;

import org.openrewrite.cobol.CobolIsoVisitor;
import org.openrewrite.cobol.CobolPreprocessorVisitor;
import org.openrewrite.cobol.marker.CopiedStatement;
import org.openrewrite.cobol.tree.CobolPreprocessor;

/**
 * A COBOL visitor that does not descend into a copybook. Its words are in the tree already, marked
 * as copied, and would be reached a second time through the copy statement they hang off; a copy
 * statement written inside one is the copybook's too. The words of an {@code EXEC} block are
 * reached the other way, by the preprocessor's visitor re-entering this one, which is why a trait
 * over them has to be a COBOL visitor.
 */
abstract class CopybookSkippingVisitor<P> extends CobolIsoVisitor<P> {

    @Override
    protected CobolPreprocessorVisitor<P> getCobolPreprocessorVisitor() {
        if (cobolPreprocessorVisitor == null) {
            cobolPreprocessorVisitor = new CobolPreprocessorVisitor<P>(this) {
                @Override
                public CobolPreprocessor visitCopybook(CobolPreprocessor.Copybook copybook, P p) {
                    return copybook;
                }

                @Override
                public CobolPreprocessor visitCopyStatement(CobolPreprocessor.CopyStatement copy, P p) {
                    return isCopied(copy) ? copy : super.visitCopyStatement(copy, p);
                }

                @Override
                public CobolPreprocessor visitExecSqlIncludeStatement(CobolPreprocessor.ExecSqlIncludeStatement include, P p) {
                    return isCopied(include) ? include : super.visitExecSqlIncludeStatement(include, p);
                }

                private boolean isCopied(CobolPreprocessor statement) {
                    return statement.getMarkers().findFirst(CopiedStatement.class).isPresent();
                }
            };
        }
        return cobolPreprocessorVisitor;
    }
}
