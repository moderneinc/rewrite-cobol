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
import org.openrewrite.cobol.tree.Cobol;
import org.openrewrite.marker.SearchResult;

@EqualsAndHashCode(callSuper = true)
@Value
public class FindMissingCopyBooks extends Recipe {

    @Override
    public String getDisplayName() {
        return "Find missing copybooks";
    }

    @Override
    public String getDescription() {
        return "Finds copy statements that are missing a reference to a copy book.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new CobolIsoVisitor<ExecutionContext>() {
            @Override
            public Cobol.Word visitWord(Cobol.Word word, ExecutionContext executionContext) {
                if (word.getCopyStatement() != null &&
                        word.getCopyStatement().getMarkers().findFirst(MissingCopyBook.class).isPresent()) {
                    return word.withCopyStatement(word.getCopyStatement().withCopySource(
                            word.getCopyStatement().getCopySource().withName(
                                    SearchResult.found(word.getCopyStatement().getCopySource().getName()))));
                }
                return super.visitWord(word, executionContext);
            }
        };
    }
}
