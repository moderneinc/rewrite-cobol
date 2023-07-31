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
import org.openrewrite.cobol.table.CopyBookSource;
import org.openrewrite.cobol.tree.Cobol;
import org.openrewrite.cobol.tree.CobolPreprocessor;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.marker.SearchResult;

@EqualsAndHashCode(callSuper = true)
@Value
public class FindCopyBook extends Recipe {
    transient CopyBookSource copyBookSource = new CopyBookSource(this);

    @Option(displayName = "Copy book name",
            description = "The copy book name to search for. If not provided, all copy statements will be returned.",
            example = "KP008",
            required = false)
    @Nullable
    String copyBookName;

    @Override
    public String getDisplayName() {
        return "Find copy book usage";
    }

    @Override
    public String getDescription() {
        return "Find all copy statements with the copybook name.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(new UsesCopyBook(copyBookName), new CobolIsoVisitor<ExecutionContext>() {

            @Override
            public Cobol.Word visitWord(Cobol.Word word, ExecutionContext executionContext) {
                if (word.getCopyStatement() != null) {
                    if (word.getCopyStatement().getMarkers().findFirst(MissingCopyBook.class).isPresent()) {
                            //noinspection DataFlowIssue
                            copyBookSource.insertRow(executionContext,
                                    new CopyBookSource.Row(
                                            getCursor().firstEnclosing(Cobol.CompilationUnit.class).getSourcePath().toString(),
                                            word.getCopyStatement().getCopySource().getName().getCobolWord().getWord(),
                                            "",
                                            CopyBookSource.ResolutionStatus.MISSING_SOURCE,
                                            ""));
                            return word.withCopyStatement(word.getCopyStatement().withCopySource(
                                    word.getCopyStatement().getCopySource().withName(
                                            SearchResult.found(word.getCopyStatement().getCopySource().getName()))));
                    } else {
                        if (copyBookName == null || copyBookName.isEmpty() || copyBookName.equals(word.getCopyStatement().getCopySource().getName().getCobolWord().getWord())) {
                            CobolPreprocessor.CopyStatement updated = word.getCopyStatement().withCopySource(word.getCopyStatement().getCopySource().withName(
                                    SearchResult.found(word.getCopyStatement().getCopySource().getName(), null)));
                            boolean copySourceResolved = word.getCopyStatement().getCopyBook() != null;
                            //noinspection DataFlowIssue
                            copyBookSource.insertRow(executionContext,
                                    new CopyBookSource.Row(
                                            getCursor().firstEnclosing(Cobol.CompilationUnit.class).getSourcePath().toString(),
                                            word.getCopyStatement().getCopySource().getName().getCobolWord().getWord(),
                                            copySourceResolved ? word.getCopyStatement().getCopyBook().getSourcePath().toString() : "",
                                            copySourceResolved ? CopyBookSource.ResolutionStatus.RESOLVED : CopyBookSource.ResolutionStatus.NO_SOURCE_PATH,
                                            word.getWord()));
                            return word.withCopyStatement(updated);
                        }
                    }
                }
                return super.visitWord(word, executionContext);
            }
        });
    }
}
