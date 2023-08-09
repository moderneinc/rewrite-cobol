/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.cobol.search;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.cobol.CobolIsoVisitor;
import org.openrewrite.cobol.markers.MissingCopybook;
import org.openrewrite.cobol.table.CobolRelationships;
import org.openrewrite.cobol.tree.Cobol;
import org.openrewrite.cobol.tree.CobolPreprocessor;
import org.openrewrite.cobol.tree.Name;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.marker.SearchResult;
import org.openrewrite.text.PlainText;
import org.openrewrite.text.PlainTextVisitor;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.MULTILINE;
import static org.openrewrite.cobol.table.CobolRelationships.ResourceAction.*;
import static org.openrewrite.cobol.table.CobolRelationships.ResourceType.*;

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
        CobolIsoVisitor<ExecutionContext> cobolVisitor = new CobolIsoVisitor<ExecutionContext>() {

            final Set<String> seenCalls = new HashSet<>();
            final Set<String> seenCopies = new HashSet<>();

            String programName = "UNKNOWN";

            @Override
            public Cobol.ProgramIdParagraph visitProgramIdParagraph(Cobol.ProgramIdParagraph programIdParagraph, ExecutionContext executionContext) {
                Name rawName = programIdParagraph.getProgramName();
                if (rawName instanceof Cobol.Word) {
                    programName = ((Cobol.Word) rawName).getWord();
                }
                return super.visitProgramIdParagraph(programIdParagraph, executionContext);
            }

            @Override
            public Cobol.Word visitWord(Cobol.Word word, ExecutionContext ctx) {
                Cobol.Word w = super.visitWord(word, ctx);
                w = w.withPreprocessorStatements(ListUtils.map(w.getPreprocessorStatements(), ps -> {
                    if (ps instanceof CobolPreprocessor.CopyStatement) {
                        CobolPreprocessor.CopyStatement copyStatement = (CobolPreprocessor.CopyStatement) ps;
                        String copyName = copyStatement.getCopySource().getName().getCobolWord().getWord();
                        if(seenCopies.add(copyName)) {
                            cobolRelationships.insertRow(ctx,
                                    new CobolRelationships.Row(
                                            programName,
                                            COBOL,
                                            COPY,
                                            copyStatement.getCopySource().getName().getCobolWord().getWord(),
                                            COPYBOOK,
                                            copyStatement.getMarkers().findFirst(MissingCopybook.class).isPresent()));
                        }
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
                        String callName = word.getWord().replace("\"", "");
                        if(seenCalls.add(callName)) {
                            cobolRelationships.insertRow(ctx,
                                    new CobolRelationships.Row(
                                            programName,
                                            COBOL,
                                            CALL,
                                            word.getWord().replace("\"", ""),
                                            COBOL,
                                            false
                                    )
                            );
                        }
                        return call.withIdentifier(SearchResult.found(call.getIdentifier()));
                    }
                }
                return super.visitCall(call, ctx);
            }
        };

        PlainTextVisitor<ExecutionContext> linkEditVisitor = new PlainTextVisitor<ExecutionContext>() {
            final Pattern includeMatcher = Pattern.compile("^INCLUDE\\s+(?:SYS|OBJ)LIB\\(([A-Z0-9]+)\\)", MULTILINE);
            @Override
            public PlainText visitText(PlainText pt, ExecutionContext ctx) {
                String text = pt.getText();
                Matcher m = includeMatcher.matcher(text);
                while(m.find()) {
                    String programName = m.group(1);
                    cobolRelationships.insertRow(ctx,
                            new CobolRelationships.Row(
                                    pt.getSourcePath().toString(),
                                    LINKEDIT,
                                    INCLUDE,
                                    programName,
                                    COBOL,
                                    false));
                }
                return pt;
            }
        };

        return new TreeVisitor<Tree, ExecutionContext>() {
            @Override
            public @Nullable Tree preVisit(Tree tree, ExecutionContext ctx) {
                stopAfterPreVisit();
                if(tree instanceof Cobol) {
                    tree = cobolVisitor.visit(tree, ctx);
                } else if(tree instanceof PlainText) {
                    tree = linkEditVisitor.visit(tree, ctx);
                }
                return tree;
            }
        };
    }

}
