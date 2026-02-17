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
package org.openrewrite.cobol.search;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.*;
import org.openrewrite.cobol.CobolIsoVisitor;
import org.openrewrite.cobol.CobolPreprocessorIsoVisitor;
import org.openrewrite.cobol.marker.MissingCopybook;
import org.openrewrite.cobol.table.CopybookSource;
import org.openrewrite.cobol.tree.Cobol;
import org.openrewrite.cobol.tree.CobolPreprocessor;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.internal.StringUtils;
import org.openrewrite.marker.SearchResult;

@EqualsAndHashCode(callSuper = false)
@Value
public class FindCopybook extends Recipe {
    transient CopybookSource copybookSource = new CopybookSource(this);

    @Option(displayName = "Copybook name",
            description = "The copybook name to search for. If not provided, all copy statements will be returned.",
            example = "KP008",
            required = false)
    @Nullable
    String copybookName;

    @Option(displayName = "Only missing copybooks",
            description = "Only find copy statements and exec sql include statements that are missing copybooks.",
            example = "True",
            required = false)
    @Nullable
    Boolean onlyMissingCopybooks;

    String displayName = "Find copybook usage";

    String description = "Find all copy statements with the copybook name.";

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(new UsesCopybook(copybookName), new CobolIsoVisitor<ExecutionContext>() {
            @Override
            public Cobol.Word visitWord(Cobol.Word word, ExecutionContext ctx) {
                Cobol.Word w = super.visitWord(word, ctx);
                PreprocessorVisitor preprocessorVisitor = new PreprocessorVisitor(getCursor(), word.getWord());
                return w.withPreprocessorStatements(ListUtils.map(w.getPreprocessorStatements(),
                    ps -> preprocessorVisitor.visit(ps, ctx)));
            }

            /**
             * Visitor for CobolPreprocessor trees that finds copy statements and exec sql include statements.
             */
            class PreprocessorVisitor extends CobolPreprocessorIsoVisitor<ExecutionContext> {
                private final Cursor cursor;
                private final String wordContext;

                PreprocessorVisitor(Cursor cursor, String wordContext) {
                    this.cursor = cursor;
                    this.wordContext = wordContext;
                }

                @Override
                public CobolPreprocessor.CopyStatement visitCopyStatement(CobolPreprocessor.CopyStatement copyStatement, ExecutionContext ctx) {
                    CobolPreprocessor.CopyStatement c = super.visitCopyStatement(copyStatement, ctx);

                    if (c.getMarkers().findFirst(MissingCopybook.class).isPresent()) {
                        copybookSource.insertRow(ctx,
                            new CopybookSource.Row(
                                cursor.firstEnclosingOrThrow(Cobol.CompilationUnit.class).getSourcePath().toString(),
                                c.getCopySource().getName().getCobolWord().getWord(),
                                "",
                                CopybookSource.ResolutionStatus.MISSING_SOURCE,
                                ""));
                        return c.withCopySource(
                            c.getCopySource().withName(
                                SearchResult.found(c.getCopySource().getName())));
                    }

                    if (!Boolean.TRUE.equals(onlyMissingCopybooks)) {
                        if (StringUtils.isNullOrEmpty(copybookName) ||
                            copybookName.equals(c.getCopySource().getName().getCobolWord().getWord())) {
                            CobolPreprocessor.CopyStatement updated = c.withCopySource(
                                c.getCopySource().withName(
                                    SearchResult.found(c.getCopySource().getName(), null)));
                            if (updated.getCopybook() != null) {
                                updated = updated.withCopybook(visitCopybook(updated.getCopybook(), ctx));
                            }
                            boolean copySourceResolved = updated.getCopybook() != null;
                            copybookSource.insertRow(ctx,
                                new CopybookSource.Row(
                                    cursor.firstEnclosingOrThrow(Cobol.CompilationUnit.class).getSourcePath().toString(),
                                    updated.getCopySource().getName().getCobolWord().getWord(),
                                    copySourceResolved ? updated.getCopybook().getSourcePath().toString() : "",
                                    copySourceResolved ? CopybookSource.ResolutionStatus.RESOLVED : CopybookSource.ResolutionStatus.NO_SOURCE_PATH,
                                    wordContext));
                            return updated;
                        }
                    }
                    return c;
                }

                @Override
                public CobolPreprocessor.ExecSqlIncludeStatement visitExecSqlIncludeStatement(
                        CobolPreprocessor.ExecSqlIncludeStatement includeStatement, ExecutionContext ctx) {
                    CobolPreprocessor.ExecSqlIncludeStatement i = super.visitExecSqlIncludeStatement(includeStatement, ctx);

                    if (i.getMarkers().findFirst(MissingCopybook.class).isPresent()) {
                        copybookSource.insertRow(ctx,
                            new CopybookSource.Row(
                                cursor.firstEnclosingOrThrow(Cobol.CompilationUnit.class).getSourcePath().toString(),
                                i.getCopySource().getCobolWord().getWord(),
                                "",
                                CopybookSource.ResolutionStatus.MISSING_SOURCE,
                                ""));
                        return i.withCopySource(SearchResult.found(i.getCopySource()));
                    }

                    if (!Boolean.TRUE.equals(onlyMissingCopybooks)) {
                        if (StringUtils.isNullOrEmpty(copybookName) ||
                            copybookName.equals(i.getCopySource().getCobolWord().getWord())) {
                            CobolPreprocessor.ExecSqlIncludeStatement updated =
                                i.withCopySource(SearchResult.found(i.getCopySource(), null));
                            if (updated.getCopybook() != null) {
                                updated = updated.withCopybook(visitCopybook(updated.getCopybook(), ctx));
                            }
                            boolean copySourceResolved = updated.getCopybook() != null;
                            copybookSource.insertRow(ctx,
                                new CopybookSource.Row(
                                    cursor.firstEnclosingOrThrow(Cobol.CompilationUnit.class).getSourcePath().toString(),
                                    updated.getCopySource().getCobolWord().getWord(),
                                    copySourceResolved ? updated.getCopybook().getSourcePath().toString() : "",
                                    copySourceResolved ? CopybookSource.ResolutionStatus.RESOLVED : CopybookSource.ResolutionStatus.NO_SOURCE_PATH,
                                    wordContext));
                            return updated;
                        }
                    }
                    return i;
                }

                @Override
                public CobolPreprocessor.Copybook visitCopybook(CobolPreprocessor.Copybook copybook, ExecutionContext ctx) {
                    // Process nested copybooks by visiting their LST elements
                    return copybook.withLst(ListUtils.map(copybook.getLst(),
                        element -> visit(element, ctx)));
                }
            }
        });
    }
}
