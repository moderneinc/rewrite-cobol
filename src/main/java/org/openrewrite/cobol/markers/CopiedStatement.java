/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.cobol.markers;

import lombok.Value;
import lombok.With;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.marker.Marker;

import java.util.UUID;

@Deprecated
@With
@Value
public class CopiedStatement implements Marker {
    UUID id;

    @Nullable
    String sourceCopybook;

    public String getSourceCopybook() {
        return sourceCopybook == null ? "" : sourceCopybook;
    }
}
