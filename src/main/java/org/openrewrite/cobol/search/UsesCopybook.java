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
package org.openrewrite.cobol.search;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.cobol.CobolIsoVisitor;
import org.openrewrite.cobol.tree.Cobol;
import org.openrewrite.cobol.tree.CobolPreprocessor;
import org.openrewrite.marker.SearchResult;

import java.util.concurrent.atomic.AtomicBoolean;

@EqualsAndHashCode(callSuper = true)
@Value
public class UsesCopybook extends CobolIsoVisitor<ExecutionContext> {

    @Nullable
    String copybookName;

    public UsesCopybook(@Nullable String copybookName) {
        this.copybookName = copybookName;
    }

    @Override
    public Cobol.CompilationUnit visitCompilationUnit(Cobol.CompilationUnit compilationUnit, ExecutionContext ctx) {
        Cobol.CompilationUnit cu = compilationUnit;
        if (FindCopySource.find(cu, copybookName)) {
            cu = SearchResult.found(cu);
        }
        return cu;
    }

    private static class FindCopySource extends CobolIsoVisitor<ExecutionContext> {

        public static boolean find(Cobol cobol, @Nullable String bookName) {
            CobolIsoVisitor<AtomicBoolean> visitor = new CobolIsoVisitor<AtomicBoolean>() {
                @Override
                public Cobol.Word visitWord(Cobol.Word word, AtomicBoolean found) {
                    Cobol.Word w = super.visitWord(word, found);
                    if (!found.get()) {
                        for (CobolPreprocessor preprocessorStatement : w.getPreprocessorStatements()) {
                            if (preprocessorStatement instanceof CobolPreprocessor.CopyStatement) {
                                CobolPreprocessor.CopyStatement copyStatement = (CobolPreprocessor.CopyStatement) preprocessorStatement;
                                if (bookName == null || bookName.isEmpty() || bookName.equals(copyStatement.getCopySource().getName().getCobolWord().getWord())) {
                                    found.set(true);
                                }
                            } else if (preprocessorStatement instanceof CobolPreprocessor.ExecSqlIncludeStatement) {
                                CobolPreprocessor.ExecSqlIncludeStatement execSqlIncludeStatement = (CobolPreprocessor.ExecSqlIncludeStatement) preprocessorStatement;
                                if (bookName == null || bookName.isEmpty() || bookName.equals(execSqlIncludeStatement.getCopySource().getCobolWord().getWord())) {
                                    found.set(true);
                                }
                            }
                        }
                    }
                    return w;
                }
            };

            AtomicBoolean found = new AtomicBoolean(false);
            visitor.visit(cobol, found);
            return found.get();
        }
    }
}
