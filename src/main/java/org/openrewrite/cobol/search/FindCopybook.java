/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.cobol.search;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.*;
import org.openrewrite.cobol.CobolIsoVisitor;
import org.openrewrite.cobol.CobolPreprocessorIsoVisitor;
import org.openrewrite.cobol.marker.MissingCopybook;
import org.openrewrite.cobol.table.CopybookSource;
import org.openrewrite.cobol.tree.Cobol;
import org.openrewrite.cobol.tree.CobolPreprocessor;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.marker.SearchResult;

@EqualsAndHashCode(callSuper = true)
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

    @Override
    public String getDisplayName() {
        return "Find copybook usage";
    }

    @Override
    public String getDescription() {
        return "Find all copy statements with the copybook name.";
    }

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
                } else if (!Boolean.TRUE.equals(onlyMissingCopybooks)) {
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
                } else if (!Boolean.TRUE.equals(onlyMissingCopybooks)) {
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
                w = w.withPreprocessorStatements(ListUtils.map(w.getPreprocessorStatements(), ps -> {
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
                        } else if (!Boolean.TRUE.equals(onlyMissingCopybooks)) {
                            if (copybookName == null || copybookName.isEmpty() || copybookName.equals(copyStatement.getCopySource().getName().getCobolWord().getWord())) {
                                CobolPreprocessor.CopyStatement updated = copyStatement.withCopySource(copyStatement.getCopySource().withName(
                                        SearchResult.found(copyStatement.getCopySource().getName(), null)));
                                if (updated.getCopybook() != null) {
                                    updated = updated.withCopybook(updated.getCopybook().withLst(ListUtils.map(updated.getCopybook().getLst(), l -> preprocessorVisitor.visit(l, ctx, getCursor()))));
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
                        } else if (!Boolean.TRUE.equals(onlyMissingCopybooks)) {
                            if (copybookName == null || copybookName.isEmpty() || copybookName.equals(includeStatement.getCopySource().getCobolWord().getWord())) {
                                CobolPreprocessor.ExecSqlIncludeStatement updated = includeStatement.withCopySource(SearchResult.found(includeStatement.getCopySource(), null));
                                if (updated.getCopybook() != null) {
                                    updated = updated.withCopybook(updated.getCopybook().withLst(ListUtils.map(updated.getCopybook().getLst(), l -> preprocessorVisitor.visit(l, ctx, getCursor()))));
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

                return w;
            }
        });
    }
}
