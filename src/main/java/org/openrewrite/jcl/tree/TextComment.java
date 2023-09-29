/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.jcl.tree;

import lombok.Value;
import lombok.With;
import org.openrewrite.Cursor;
import org.openrewrite.PrintOutputCapture;
import org.openrewrite.marker.Marker;
import org.openrewrite.marker.Markers;

import java.util.function.UnaryOperator;

@Value
public class TextComment implements Comment {

    String text;

    public TextComment withText(String text) {
        if(!text.equals(this.text)) {
            return new TextComment(text, suffix, markers);
        }
        return this;
    }

    String suffix;

    @SuppressWarnings("unchecked")
    public TextComment withSuffix(String suffix) {
        if(!suffix.equals(this.suffix)) {
            return new TextComment(text, suffix, markers);
        }
        return this;
    }

    @With
    Markers markers;

    private static final UnaryOperator<String> JAVA_MARKER_WRAPPER =
            out -> "/*~~" + out + (out.isEmpty() ? "" : "~~") + ">*/";

    @Override
    public <P> void printComment(Cursor cursor, PrintOutputCapture<P> p) {
        for (Marker marker : markers.getMarkers()) {
            p.append(p.getMarkerPrinter().beforeSyntax(marker, new Cursor(cursor, this), JAVA_MARKER_WRAPPER));
        }
        p.append("//*" + text);
        for (Marker marker : markers.getMarkers()) {
            p.append(p.getMarkerPrinter().afterSyntax(marker, new Cursor(cursor, this), JAVA_MARKER_WRAPPER));
        }
    }
}
