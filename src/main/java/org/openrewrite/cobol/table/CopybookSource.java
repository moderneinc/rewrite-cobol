/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.cobol.table;

import lombok.Value;
import org.openrewrite.Column;
import org.openrewrite.DataTable;
import org.openrewrite.Option;
import org.openrewrite.Recipe;

public class CopybookSource extends DataTable<CopybookSource.Row> {

    public CopybookSource(Recipe recipe) {
        super(recipe, "Copybook source information",
                "Information about copybook references in a COBOL source.");
    }

    @Value
    public static class Row {
        @Option(displayName = "COBOL source path",
                description = "The source path of the COBOL source file that contains the copy statement.")
        String cobolSourcePath;

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
