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
package org.openrewrite.mainframe.cobol.table;

import lombok.Value;
import org.openrewrite.Column;
import org.openrewrite.DataTable;
import org.openrewrite.Recipe;

public class ReferenceSearchResult extends DataTable<ReferenceSearchResult.Row> {

    public ReferenceSearchResult(Recipe recipe) {
        super(recipe, "COBOL reference search results",
                "Identifier references found in COBOL, copybook, and JCL sources.");
    }

    @Value
    public static class Row {
        @Column(displayName = "Source path",
                description = "The source path of the file that contains the matching reference.")
        String sourcePath;

        @Column(displayName = "Source type",
                description = "The type of source where the reference was found.")
        SourceType sourceType;

        @Column(displayName = "Reference",
                description = "The identifier text that matched the search criteria.")
        String reference;
    }

    public enum SourceType {
        COBOL,
        COPYBOOK,
        JCL
    }
}
