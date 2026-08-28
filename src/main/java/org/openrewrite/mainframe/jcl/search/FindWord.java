/*
 * Copyright 2026 the original author or authors.
 * <p>
 * Licensed under the Moderne Source Available License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://docs.moderne.io/licensing/moderne-source-available-license
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.mainframe.jcl.search;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.mainframe.jcl.JclIsoVisitor;
import org.openrewrite.mainframe.jcl.table.JclWordSearchResult;
import org.openrewrite.mainframe.jcl.tree.Jcl;
import org.openrewrite.marker.SearchResult;

import java.util.regex.Pattern;

@EqualsAndHashCode(callSuper = false)
@Value
public class FindWord extends Recipe {

    @Option(displayName = "Term to search for",
            description = "A word or regex pattern to find. By default the search term is case insensitive.",
            example = "SYSOUT or sys.*")
    String searchTerm;

    @Nullable
    @Option(displayName = "Only match exact word",
            description = "Search for a word based on an exact match of the search term.",
            example = "true")
    Boolean exactMatch;

    transient JclWordSearchResult wordSearchResult = new JclWordSearchResult(this);

    String displayName = "Find matching words in JCL source code";

    String description = "Search for JCL words based on a search term.";

    @Override
    public @NonNull TreeVisitor<?, ExecutionContext> getVisitor() {
        Pattern pattern = Boolean.TRUE.equals(exactMatch) ? null : Pattern.compile(searchTerm.toLowerCase());

        return new JclIsoVisitor<ExecutionContext>() {
            @Override
            public Jcl.@NonNull Word visitWord(Jcl.@NonNull Word word, ExecutionContext ctx) {
                Jcl.Word w = super.visitWord(word, ctx);
                if (matches(w.getText())) {
                    wordSearchResult.insertRow(ctx, new JclWordSearchResult.Row(
                            getCursor().firstEnclosingOrThrow(Jcl.CompilationUnit.class).getSourcePath().toString(),
                            w.getText()));
                    return SearchResult.found(w);
                }
                return w;
            }

            private boolean matches(String word) {
                return pattern != null && pattern.matcher(word.toLowerCase()).matches() ||
                       pattern == null && word.equals(searchTerm);
            }
        };
    }
}
