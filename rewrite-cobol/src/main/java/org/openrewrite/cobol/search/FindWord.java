/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.cobol.search;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.cobol.CobolPreprocessorIsoVisitor;
import org.openrewrite.cobol.NameVisitor;
import org.openrewrite.cobol.markers.CopiedWord;
import org.openrewrite.cobol.tree.Cobol;
import org.openrewrite.cobol.tree.CobolPreprocessor;
import org.openrewrite.cobol.tree.Replacement;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.marker.SearchResult;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

@EqualsAndHashCode(callSuper = true)
@Value
public class FindWord extends Recipe {

    @Option(displayName = "Term to search for",
            description = "A word or regex pattern to find. By default the search term is case insensitive.",
            example = "CM102M or cm1.*")
    String searchTerm;

    @Nullable
    @Option(displayName = "Only match exact word",
            description = "Search for a word based on an exact match of the search term.",
            example = "true")
    Boolean exactMatch;

    @Override
    public String getDisplayName() {
        return "Find matching words in the source code";
    }

    @Override
    public String getDescription() {
        return "Search for COBOL words based on a search term.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new SearchForWord(searchTerm, exactMatch);
    }

    private static class SearchForWord extends NameVisitor<ExecutionContext> {
        private final PreprocessorSearch preprocessorSearch;
        private final String searchTerm;

        @Nullable
        private final Pattern pattern;

        public SearchForWord(String searchTerm, @Nullable Boolean exactMatch) {
            this.searchTerm = searchTerm;
            pattern = Boolean.TRUE.equals(exactMatch) ? null : Pattern.compile(searchTerm.toLowerCase());
            preprocessorSearch = new PreprocessorSearch();
        }

        @Override
        public Cobol.Word visitWord(Cobol.Word word, ExecutionContext executionContext) {
            Cobol.Word w = super.visitWord(word, executionContext);
            AtomicBoolean hasCopyStatement = new AtomicBoolean(false);
            w = w.withPreprocessorStatements(ListUtils.map(w.getPreprocessorStatements(), it -> {
                if (it instanceof CobolPreprocessor.CopyStatement && !hasCopyStatement.get()) {
                    hasCopyStatement.set(true);
                }
                return preprocessorSearch.visit(it, executionContext);
            }));

            if (hasCopyStatement.get() || w.getMarkers().findFirst(CopiedWord.class).isPresent()) {
                return w;
            }

            if (w.getReplacement() != null) {
                if (w.getReplacement().getType() == Replacement.Type.EQUAL || w.getReplacement().getType() == Replacement.Type.REDUCTIVE) {
                    w = w.withReplacement(w.getReplacement().withOriginalWords(
                            ListUtils.map(w.getReplacement().getOriginalWords(), it -> it.withOriginal(visitAndCast(it.getOriginal(), executionContext)))));
                }
                return w;
            }

            if (matches(w.getWord())) {
                return SearchResult.found(word);
            }
            return w;
        }

        private class PreprocessorSearch extends CobolPreprocessorIsoVisitor<ExecutionContext> {
            @Override
            public CobolPreprocessor.Word visitWord(CobolPreprocessor.Word word, ExecutionContext executionContext) {
                if (matches(word.getCobolWord().getWord())) {
                    return SearchResult.found(word);
                }
                return word;
            }
        }

        private boolean matches(String word) {
            return pattern != null && pattern.matcher(word.toLowerCase()).matches() || pattern == null && word.equals(searchTerm);
        }
    }
}
