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
import org.openrewrite.cobol.markers.MissingCopyBook;
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
        return Preconditions.check(new UsesCopybook(copybookName), new CobolIsoVisitor<ExecutionContext>() {

            @Override
            public Cobol.Word visitWord(Cobol.Word word, ExecutionContext executionContext) {
                Cobol.Word w = super.visitWord(word, executionContext);
                w = w.withPreprocessorStatements(ListUtils.map(w.getPreprocessorStatements(), ps -> {
                    if (ps instanceof CobolPreprocessor.CopyStatement) {
                        CobolPreprocessor.CopyStatement copyStatement = (CobolPreprocessor.CopyStatement) ps;
                        if (copyStatement.getMarkers().findFirst(MissingCopyBook.class).isPresent()) {
                            //noinspection DataFlowIssue
                            copybookSource.insertRow(executionContext,
                                    new CopybookSource.Row(
                                            getCursor().firstEnclosing(Cobol.CompilationUnit.class).getSourcePath().toString(),
                                            copyStatement.getCopySource().getName().getCobolWord().getWord(),
                                            "",
                                            CopybookSource.ResolutionStatus.MISSING_SOURCE,
                                            ""));
                            return copyStatement.withCopySource(
                                    copyStatement.getCopySource().withName(
                                            SearchResult.found(copyStatement.getCopySource().getName())));
                        } else {
                            if (copybookName == null || copybookName.isEmpty() || copybookName.equals(copyStatement.getCopySource().getName().getCobolWord().getWord())) {
                                CobolPreprocessor.CopyStatement updated = copyStatement.withCopySource(copyStatement.getCopySource().withName(
                                        SearchResult.found(copyStatement.getCopySource().getName(), null)));
                                boolean copySourceResolved = copyStatement.getCopyBook() != null;
                                //noinspection DataFlowIssue
                                copybookSource.insertRow(executionContext,
                                        new CopybookSource.Row(
                                                getCursor().firstEnclosing(Cobol.CompilationUnit.class).getSourcePath().toString(),
                                                copyStatement.getCopySource().getName().getCobolWord().getWord(),
                                                copySourceResolved ? copyStatement.getCopyBook().getSourcePath().toString() : "",
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
