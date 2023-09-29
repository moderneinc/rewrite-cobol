/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.jcl.tree;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.openrewrite.Cursor;
import org.openrewrite.PrintOutputCapture;
import org.openrewrite.marker.Markers;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@c")
public interface Comment {

    Markers getMarkers();
    <C extends Comment> C withMarkers(Markers markers);

    String getSuffix();
    <C extends Comment> C withSuffix(String margin);

    default String printComment(Cursor cursor) {
        PrintOutputCapture<Integer> p = new PrintOutputCapture<>(0);
        printComment(cursor, p);
        return p.getOut();
    }

    <P> void printComment(Cursor cursor, PrintOutputCapture<P> p);
}
