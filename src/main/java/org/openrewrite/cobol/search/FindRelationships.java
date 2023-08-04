/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.cobol.search;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.cobol.CobolIsoVisitor;
import org.openrewrite.cobol.markers.MissingCopybook;
import org.openrewrite.cobol.table.CobolRelationships;
import org.openrewrite.cobol.tree.Cobol;
import org.openrewrite.cobol.tree.CobolPreprocessor;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.marker.SearchResult;

import static org.openrewrite.cobol.table.CobolRelationships.ResourceAction.CALL;
import static org.openrewrite.cobol.table.CobolRelationships.ResourceAction.COPY;
import static org.openrewrite.cobol.table.CobolRelationships.ResourceType.COBOL;
import static org.openrewrite.cobol.table.CobolRelationships.ResourceType.COPYBOOK;

public class FindRelationships extends Recipe {
    transient CobolRelationships cobolRelationships = new CobolRelationships(this);

    @Override
    public String getDisplayName() {
        return "Find COBOL relationships";
    }

    @Override
    public String getDescription() {
        return "Build a list of relationships for diagramming and exploration.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new CobolIsoVisitor<ExecutionContext>() {
            @Override
            public Cobol.Word visitWord(Cobol.Word word, ExecutionContext ctx) {
                Cobol.Word w = super.visitWord(word, ctx);
                w = w.withPreprocessorStatements(ListUtils.map(w.getPreprocessorStatements(), ps -> {
                    if (ps instanceof CobolPreprocessor.CopyStatement) {
                        CobolPreprocessor.CopyStatement copyStatement = (CobolPreprocessor.CopyStatement) ps;
                        cobolRelationships.insertRow(ctx,
                                new CobolRelationships.Row(
                                        getCursor().firstEnclosingOrThrow(Cobol.CompilationUnit.class).getSourcePath().toString(),
                                        COBOL,
                                        COPY,
                                        copyStatement.getCopySource().getName().getCobolWord().getWord(),
                                        COPYBOOK,
                                        copyStatement.getMarkers().findFirst(MissingCopybook.class).isPresent()));
                        return copyStatement.withCopySource(copyStatement.getCopySource().withName(
                                SearchResult.found(copyStatement.getCopySource().getName())));
                    }
                    return ps;
                }));
                return w;
            }

            @Override
            public Cobol.Call visitCall(Cobol.Call call, ExecutionContext ctx) {
                if (call.getIdentifier() instanceof Cobol.Word) {
                    Cobol.Word word = (Cobol.Word) call.getIdentifier();
                    if (word.getWord().startsWith("\"")) {
                        cobolRelationships.insertRow(ctx,
                                new CobolRelationships.Row(
                                        getCursor().firstEnclosingOrThrow(Cobol.CompilationUnit.class).getSourcePath().toString(),
                                        COBOL,
                                        CALL,
                                        word.getWord().replace("\"", ""),
                                        COBOL,
                                        false
                                )
                        );
                        return call.withIdentifier(SearchResult.found(call.getIdentifier()));
                    }
                }
                return super.visitCall(call, ctx);
            }
        };
    }
}
