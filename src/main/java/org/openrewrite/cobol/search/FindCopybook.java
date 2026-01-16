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
        CobolPreprocessorIsoVisitor<ExecutionContext> preprocessorVisitor = new CobolPreprocessorIsoVisitor<ExecutionContext>() {
            @Override
            public CobolPreprocessor.CopyStatement visitCopyStatement(CobolPreprocessor.CopyStatement copyStatement, ExecutionContext ctx) {
				CobolPreprocessor.CopyStatement cs = super.visitCopyStatement(copyStatement, ctx);
				if (cs.getMarkers().findFirst(MissingCopybook.class).isPresent()) {
					copybookSource.insertRow(ctx,
						new CopybookSource.Row(
							getCursor().firstEnclosingOrThrow(Cobol.CompilationUnit.class).getSourcePath().toString(),
							cs.getCopySource().getName().getCobolWord().getWord(),
							"",
							CopybookSource.ResolutionStatus.MISSING_SOURCE,
							""));
					return cs.withCopySource(
						cs.getCopySource().withName(
							SearchResult.found(cs.getCopySource().getName())));
				}
				if (!Boolean.TRUE.equals(onlyMissingCopybooks)) {
					if (copybookName == null || copybookName.isEmpty() || copybookName.equals(cs.getCopySource().getName().getCobolWord().getWord())) {
						CobolPreprocessor.CopyStatement updated = cs.withCopySource(cs.getCopySource().withName(
							SearchResult.found(cs.getCopySource().getName(), null)));
						boolean copySourceResolved = cs.getCopybook() != null;
						copybookSource.insertRow(ctx,
							new CopybookSource.Row(
								getCursor().firstEnclosingOrThrow(Cobol.CompilationUnit.class).getSourcePath().toString(),
								cs.getCopySource().getName().getCobolWord().getWord(),
								copySourceResolved ? cs.getCopybook().getSourcePath().toString() : "",
								copySourceResolved ? CopybookSource.ResolutionStatus.RESOLVED : CopybookSource.ResolutionStatus.NO_SOURCE_PATH,
								""));
						return updated;
					}
				}
				return cs;
			}

            @Override
            public CobolPreprocessor.ExecSqlIncludeStatement visitExecSqlIncludeStatement(CobolPreprocessor.ExecSqlIncludeStatement execSqlIncludeStatement, ExecutionContext ctx) {
				CobolPreprocessor.ExecSqlIncludeStatement is = super.visitExecSqlIncludeStatement(execSqlIncludeStatement, ctx);
				if (is.getMarkers().findFirst(MissingCopybook.class).isPresent()) {
					copybookSource.insertRow(ctx,
						new CopybookSource.Row(
							getCursor().firstEnclosingOrThrow(Cobol.CompilationUnit.class).getSourcePath().toString(),
							is.getCopySource().getCobolWord().getWord(),
							"",
							CopybookSource.ResolutionStatus.MISSING_SOURCE,
							""));
					return is.withCopySource(SearchResult.found(is.getCopySource()));
				}
				if (!Boolean.TRUE.equals(onlyMissingCopybooks)) {
					if (copybookName == null || copybookName.isEmpty() || copybookName.equals(is.getCopySource().getCobolWord().getWord())) {
						CobolPreprocessor.ExecSqlIncludeStatement updated = is.withCopySource(SearchResult.found(is.getCopySource(), null));
						boolean copySourceResolved = is.getCopybook() != null;
						copybookSource.insertRow(ctx,
							new CopybookSource.Row(
								getCursor().firstEnclosingOrThrow(Cobol.CompilationUnit.class).getSourcePath().toString(),
								is.getCopySource().getCobolWord().getWord(),
								copySourceResolved ? is.getCopybook().getSourcePath().toString() : "",
								copySourceResolved ? CopybookSource.ResolutionStatus.RESOLVED : CopybookSource.ResolutionStatus.NO_SOURCE_PATH,
								""));
						return updated;
					}
				}
				return is;
			}
        };

        return Preconditions.check(new UsesCopybook(copybookName), new CobolIsoVisitor<ExecutionContext>() {
            @Override
            public Cobol.Word visitWord(Cobol.Word word, ExecutionContext ctx) {
                Cobol.Word w = super.visitWord(word, ctx);
                return w.withPreprocessorStatements(ListUtils.map(w.getPreprocessorStatements(), ps -> {
                    if (ps instanceof CobolPreprocessor.CopyStatement) {
						CobolPreprocessor.CopyStatement copyStatement = (CobolPreprocessor.CopyStatement) ps;
						if (copyStatement.getMarkers().findFirst(MissingCopybook.class).isPresent()) {
							copybookSource.insertRow(ctx,
								new CopybookSource.Row(
									getCursor().firstEnclosingOrThrow(Cobol.CompilationUnit.class).getSourcePath().toString(),
									copyStatement.getCopySource().getName().getCobolWord().getWord(),
									"",
									CopybookSource.ResolutionStatus.MISSING_SOURCE,
									""));
							return copyStatement.withCopySource(
								copyStatement.getCopySource().withName(
									SearchResult.found(copyStatement.getCopySource().getName())));
						}
						if (!Boolean.TRUE.equals(onlyMissingCopybooks)) {
							if (copybookName == null || copybookName.isEmpty() || copybookName.equals(copyStatement.getCopySource().getName().getCobolWord().getWord())) {
								CobolPreprocessor.CopyStatement updated = copyStatement.withCopySource(copyStatement.getCopySource().withName(
									SearchResult.found(copyStatement.getCopySource().getName(), null)));
								if (updated.getCopybook() != null) {
									CobolPreprocessor.Copybook newCopyBook = (CobolPreprocessor.Copybook) preprocessorVisitor
										.visit(updated.getCopybook(), ctx, getCursor().getParentTreeCursor());
									updated = updated.withCopybook(newCopyBook);
								}
								boolean copySourceResolved = updated.getCopybook() != null;
								copybookSource.insertRow(ctx,
									new CopybookSource.Row(
										getCursor().firstEnclosingOrThrow(Cobol.CompilationUnit.class).getSourcePath().toString(),
										updated.getCopySource().getName().getCobolWord().getWord(),
										copySourceResolved ? updated.getCopybook().getSourcePath().toString() : "",
										copySourceResolved ? CopybookSource.ResolutionStatus.RESOLVED : CopybookSource.ResolutionStatus.NO_SOURCE_PATH,
										word.getWord()));
								return updated;
							}
						}
					} else if (ps instanceof CobolPreprocessor.ExecSqlIncludeStatement) {
						CobolPreprocessor.ExecSqlIncludeStatement includeStatement = (CobolPreprocessor.ExecSqlIncludeStatement) ps;
						if (includeStatement.getMarkers().findFirst(MissingCopybook.class).isPresent()) {
							copybookSource.insertRow(ctx,
								new CopybookSource.Row(
									getCursor().firstEnclosingOrThrow(Cobol.CompilationUnit.class).getSourcePath().toString(),
									includeStatement.getCopySource().getCobolWord().getWord(),
									"",
									CopybookSource.ResolutionStatus.MISSING_SOURCE,
									""));
							return includeStatement.withCopySource(SearchResult.found(includeStatement.getCopySource()));
						}
						if (!Boolean.TRUE.equals(onlyMissingCopybooks)) {
							if (copybookName == null || copybookName.isEmpty() || copybookName.equals(includeStatement.getCopySource().getCobolWord().getWord())) {
								CobolPreprocessor.ExecSqlIncludeStatement updated = includeStatement.withCopySource(SearchResult.found(includeStatement.getCopySource(), null));
								if (updated.getCopybook() != null) {
									CobolPreprocessor.Copybook newCopyBook = (CobolPreprocessor.Copybook) preprocessorVisitor
										.visit(updated.getCopybook(), ctx, getCursor().getParentTreeCursor());
									updated = updated.withCopybook(newCopyBook);
								}
								boolean copySourceResolved = updated.getCopybook() != null;
								copybookSource.insertRow(ctx,
									new CopybookSource.Row(
										getCursor().firstEnclosingOrThrow(Cobol.CompilationUnit.class).getSourcePath().toString(),
										updated.getCopySource().getCobolWord().getWord(),
										copySourceResolved ? updated.getCopybook().getSourcePath().toString() : "",
										copySourceResolved ? CopybookSource.ResolutionStatus.RESOLVED : CopybookSource.ResolutionStatus.NO_SOURCE_PATH,
										word.getWord()));
								return updated;
							}
						}
					}
                    return ps;
                }));
            }
        });
    }
}
