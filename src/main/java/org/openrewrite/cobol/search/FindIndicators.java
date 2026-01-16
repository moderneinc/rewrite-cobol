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
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.cobol.CobolIsoVisitor;
import org.openrewrite.cobol.tree.Cobol;
import org.openrewrite.marker.SearchResult;

import static org.openrewrite.Tree.randomId;

@EqualsAndHashCode(callSuper = false)
@Value
public class FindIndicators extends Recipe {

    @Option(displayName = "Indicator character",
            description = "Indicator to search for.",
            example = "D")
    String indicator;

    String displayName = "Find indicators";

    String description = "Find matching indicators. Currently, this recipe will not mark indicators on copybook code.";

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new AddSearchResult(indicator);
    }

    private static class AddSearchResult extends CobolIsoVisitor<ExecutionContext> {
        private final String indicator;

        public AddSearchResult(String indicator) {
            this.indicator = indicator;
        }


        @Override
        public Cobol.Word visitWord(Cobol.Word word, ExecutionContext ctx) {
            if (word.getIndicatorArea() != null && word.getIndicatorArea().getIndicator().equals(indicator)) {
                word = word.withIndicatorArea(
                        word.getIndicatorArea().withMarkers(
                                word.getIndicatorArea().getMarkers().addIfAbsent(new SearchResult(randomId(), null))));
            }
            return word;
        }
    }
}
