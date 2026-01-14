/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.cobol.cleanup;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.*;
import org.openrewrite.cobol.CobolIsoVisitor;
import org.openrewrite.cobol.format.RemoveWords;
import org.openrewrite.cobol.format.ShiftSequenceAreas;
import org.openrewrite.cobol.marker.CopiedWord;
import org.openrewrite.cobol.tree.Cobol;
import org.openrewrite.cobol.tree.CommentArea;
import org.openrewrite.cobol.tree.Space;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static java.util.Collections.singleton;

@EqualsAndHashCode(callSuper = false)
@Value
public class RemoveWithDebuggingMode extends Recipe {

    @Option(displayName = "Update sequence areas",
            description = "When set to `true` the existing sequence are updated to preserve ordering. " +
                    "This is default to false, and is used to prevent large diffs since COBOL has a line limit of 999k.",
            example = "true",
            required = false)
    @Nullable
    Boolean updateSequenceAreas;

    String displayName = "Remove with debugging mode";

    String description = "Remove debugging mode from SOURCE-COMPUTER paragraphs.";

    @Override
    public Set<String> getTags() {
        return singleton("RSPEC-4057");
    }

    @Override
    public Duration getEstimatedEffortPerOccurrence() {
        return Duration.ofMinutes(1_000_000);
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new CobolIsoVisitor<ExecutionContext>() {

			private Cobol.@Nullable Word endWord = null;

            @Override
            public Cobol.Word visitWord(Cobol.Word word, ExecutionContext ctx) {
                Cobol.Word w = super.visitWord(word, ctx);
                if (endWord != null) {
                    Cursor parent = getCursor().getParent();
                    // This covers an unlikely case, and requires the cursor to have access to the CU.
                    // Removes whitespace from the EOF on the CU if the SourceComputerDefinition is the last COBOL.
                    if (parent != null && parent.getValue() instanceof Cobol.CompilationUnit && ((Cobol.CompilationUnit) parent.getValue()).getEof() == word) {
                        w = w.withPrefix(Space.EMPTY);
                    }
                    endWord = null;
                }
                return w;
            }

            @Override
            public Cobol.SourceComputerDefinition visitSourceComputerDefinition(Cobol.SourceComputerDefinition sourceComputerDefinition,
                                                                                ExecutionContext ctx) {
                Cobol.SourceComputerDefinition s = super.visitSourceComputerDefinition(sourceComputerDefinition, ctx);
                if (s.getDebuggingMode() != null) {
                    // Do not change copied or replaced code until the transformations are understood.
                    boolean isSupported = true;
                    for (Cobol.Word word : s.getDebuggingMode()) {
                        if (word.getReplacement() != null || word.getMarkers().findFirst(CopiedWord.class).isPresent()) {
                            isSupported = false;
                            break;
                        }
                    }

                    if (isSupported) {
                        if (s.getComputerName().getCommentArea() != null && !s.getComputerName().getCommentArea().getPrefix().getWhitespace().isEmpty()) {
                            List<Cobol.Word> originalWords = s.getDebuggingMode();
                            CommentArea commentArea = s.getComputerName().getCommentArea();
                            commentArea = commentArea.withPrefix(
                                    commentArea.getPrefix().withWhitespace(
                                            commentArea.getPrefix().getWhitespace().substring(1)));
                            s = s.withDot(s.getDot().withCommentArea(commentArea));
                            s = s.withComputerName(s.getComputerName().withCommentArea(null));

                            Cobol.Word startWord = s.getComputerName();
                            if (Boolean.TRUE.equals(updateSequenceAreas)) {
                                doAfterVisit(new ShiftSequenceAreas(originalWords, startWord));
                            }

                            endWord = s.getDot();
                            s = s.withDebuggingMode(null);
                        } else {
                            // Interim safe replace until we have auto-formatting.
                            s = new RemoveWords(s.getDebuggingMode()).visitSourceComputerDefinition(s, ctx);
                        }
                    }
                }
                return s;
            }
        };
    }
}
