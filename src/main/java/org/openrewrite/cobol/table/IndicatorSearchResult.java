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

public class IndicatorSearchResult extends DataTable<IndicatorSearchResult.Row> {

    public IndicatorSearchResult(Recipe recipe) {
        super(recipe, "COBOL indicator search results",
                "Indicator area matches found in COBOL source code.");
    }

    @Value
    public static class Row {
        @Column(displayName = "Source path",
                description = "The source path of the file that contains the matching indicator.")
        String sourcePath;

        @Column(displayName = "Indicator",
                description = "The indicator character found in the indicator area.")
        String indicator;

        @Column(displayName = "Word",
                description = "The word text on the line where the indicator was found.")
        String word;
    }
}
