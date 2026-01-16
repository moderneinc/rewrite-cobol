/*
 * Copyright 2024 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
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
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.cobol.CobolPreprocessorIsoVisitor;
import org.openrewrite.cobol.NameVisitor;
import org.openrewrite.cobol.marker.CopiedWord;
import org.openrewrite.cobol.tree.Cobol;
import org.openrewrite.cobol.tree.CobolPreprocessor;
import org.openrewrite.cobol.tree.Replacement;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.marker.SearchResult;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

@EqualsAndHashCode(callSuper = false)
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

    String displayName = "Find matching words in the source code";

    String description = "Search for COBOL words based on a search term.";

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
        public Cobol.Word visitWord(Cobol.Word word, ExecutionContext ctx) {
            Cobol.Word w = super.visitWord(word, ctx);
            AtomicBoolean hasCopyStatement = new AtomicBoolean(false);
            w = w.withPreprocessorStatements(ListUtils.map(w.getPreprocessorStatements(), it -> {
                if (it instanceof CobolPreprocessor.CopyStatement && !hasCopyStatement.get()) {
                    hasCopyStatement.set(true);
                }
                return preprocessorSearch.visit(it, ctx);
            }));

            if (hasCopyStatement.get() || w.getMarkers().findFirst(CopiedWord.class).isPresent()) {
                return w;
            }

            if (w.getReplacement() != null) {
                if (w.getReplacement().getType() == Replacement.Type.EQUAL || w.getReplacement().getType() == Replacement.Type.REDUCTIVE) {
                    w = w.withReplacement(w.getReplacement().withOriginalWords(
                            ListUtils.map(w.getReplacement().getOriginalWords(), it -> it.withOriginal(visitAndCast(it.getOriginal(), ctx)))));
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
            public CobolPreprocessor.Word visitWord(CobolPreprocessor.Word word, ExecutionContext ctx) {
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
