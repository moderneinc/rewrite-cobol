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
package org.openrewrite.mainframe.cobol.table;

import lombok.Value;
import org.openrewrite.Column;
import org.openrewrite.DataTable;
import org.openrewrite.Recipe;

public class CopybookSource extends DataTable<CopybookSource.Row> {

    public CopybookSource(Recipe recipe) {
        super(recipe, "Copybook source information",
                "Information about copybook references in a COBOL source.");
    }

    @Value
    public static class Row {
        @Column(displayName = "Source path",
                description = "The source path of the file that contains the copy statement.")
        String source;

        @Column(displayName = "Copybook name",
                description = "The copybook name from a copy statement in a COBOL source.")
        String copybookName;

        @Column(displayName = "Copybook source path",
                description = "The source path of the copybook that was resolved during resolution of copybooks.")
        String copybookSourcePath;

        @Column(displayName = "Resolution status",
                description = "The status of the resolved copybook in a copy statement.")
        ResolutionStatus resolutionStatus;

        @Column(displayName = "Marked word",
                description = "The current word being visited from the post-processed LST.")
        String markedWord;
    }

    public enum ResolutionStatus {
        MISSING_SOURCE,
        NO_SOURCE_PATH,
        RESOLVED
    }
}
