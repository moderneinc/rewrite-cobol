/*
 * Copyright 2025 the original author or authors.
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
package org.openrewrite.cobol.table;

import lombok.Value;
import org.openrewrite.Column;
import org.openrewrite.DataTable;
import org.openrewrite.Recipe;

public class WordSearchResult extends DataTable<WordSearchResult.Row> {

    public WordSearchResult(Recipe recipe) {
        super(recipe, "COBOL word search results",
                "Words in COBOL source code that match the search criteria.");
    }

    @Value
    public static class Row {
        @Column(displayName = "Source path",
                description = "The source path of the file that contains the matching word.")
        String sourcePath;

        @Column(displayName = "Matched word",
                description = "The word text that matched the search criteria.")
        String matchedWord;
    }
}
