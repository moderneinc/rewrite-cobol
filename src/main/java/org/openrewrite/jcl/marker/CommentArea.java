/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.jcl.marker;

import lombok.Value;
import lombok.With;
import org.openrewrite.jcl.tree.Space;
import org.openrewrite.marker.Marker;

import java.util.UUID;

@With
@Value
public class CommentArea implements Marker {
    UUID id;
    Space prefix;
    String comment;
}
