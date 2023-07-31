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

public class CopyBookSource extends DataTable<CopyBookSource.Row> {

    public CopyBookSource(Recipe recipe) {
        super(recipe, "CopyBook source information",
                "Information about copy book references in a COBOL source.");
    }
    public enum ResolutionStatus {
        MISSING_SOURCE,
        NO_SOURCE_PATH,
        RESOLVED
    }
    @Value
    public static class Row {
        @Option(displayName = "Source path",
                description = "The source path of the COBOL source file that contains the copy statement.")
        String sourcePath;

        @Column(displayName = "Copy book name",
                description = "The copy book name from a copy statement in a COBOL source.")
        String copyBookName;

        @Column(displayName = "Copy book Source path",
                description = "The source path of the copy book that was resolved during resolution of copy books.")
        String copyBookSourcePath;

        @Column(displayName = "Resolution status",
                description = "The status of the resolved copy book in a copy statement.")
        ResolutionStatus resolutionStatus;

        @Column(displayName = "Marked word",
                description = "The current word being visited from the post-processed LST.")
        String markedWord;


    }
}
