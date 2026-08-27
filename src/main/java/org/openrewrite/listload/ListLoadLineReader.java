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
package org.openrewrite.listload;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.openrewrite.cobol.LineEndings;
import org.openrewrite.listload.tree.ListLoad;
import org.openrewrite.marker.Markers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import static org.openrewrite.Tree.randomId;

/**
 * Splits a listing into the lines it was printed as.
 * <p>
 * Column 1 of a report is the ASA carriage control the printer acted on rather than something that
 * was printed, so it is held apart from the text: the report's own column 1 is column 2 of the file,
 * and a trait that reads a column of the module map would otherwise be one out. A request deck is a
 * deck of cards and has no carriage control, so there the whole card is text.
 * <p>
 * Everything else is left as it was printed. A report is not a language and its columns drift — the
 * fixture's listings are hand-authored in the format, and a real one differs by release — so what a
 * report says is read from its words rather than from where they fell.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ListLoadLineReader {

    /**
     * The ASA carriage control characters a listing uses: throw a page, skip a line, skip two, print
     * without spacing, print on the next line.
     */
    private static final String CARRIAGE_CONTROL = "10-+ ";

    /**
     * The AMBLIST functions that report on a load library. A request deck opens with one of them, and
     * a report echoes the request it was given.
     */
    private static final Set<String> FUNCTIONS = new HashSet<>(Arrays.asList(
            "LISTLOAD", "LISTIDR", "LISTOBJ", "LISTLPA"));

    /**
     * The headings AMBLIST and the binder print, with the blanks taken out: both spread a heading's
     * letters apart, and by how much is a matter of the release.
     */
    private static final Set<String> HEADINGS = new HashSet<>(Arrays.asList(
            "AMBLIST", "MODULESUMMARY", "CONTROLSECTIONSUMMARY", "MODULEMAP", "CROSSREFERENCETABLE",
            "IDENTIFICATIONRECORDDATA"));

    private static final Pattern BINDER_MESSAGE = Pattern.compile("IEW\\d{4}[A-Z]");

    /**
     * How far into a file a listing has to say what it is. Both reports open with a heading and a
     * request deck with its function, so nothing is gained by reading a 1.5 million line listing to
     * the end to type it.
     */
    private static final int HEADING_LINES = 50;

    /**
     * Whether text is something AMBLIST or the binder produced, or a request deck for AMBLIST. A
     * control card library holds these beside sort cards, IDCAMS cards and parm cards, and nothing in
     * a member's name says which it is.
     */
    public static boolean isModuleListing(String source) {
        return isReport(source) || isRequest(source);
    }

    /**
     * Whether text is a printed report rather than a deck, judged by the headings it opens with.
     */
    public static boolean isReport(String source) {
        int read = 0;
        for (String line : source.split("\n")) {
            if (BINDER_MESSAGE.matcher(line).find()) {
                return true;
            }
            String squeezed = squeeze(line);
            for (String heading : HEADINGS) {
                if (squeezed.contains(heading)) {
                    return true;
                }
            }
            if (!squeezed.isEmpty() && ++read >= HEADING_LINES) {
                return false;
            }
        }
        return false;
    }

    /**
     * Whether text is a deck of AMBLIST requests, judged by its first card.
     */
    public static boolean isRequest(String source) {
        for (String line : source.split("\n")) {
            String card = line.trim();
            if (card.isEmpty()) {
                continue;
            }
            return FUNCTIONS.contains(card.split("[\\s,=]+", 2)[0].toUpperCase(Locale.ROOT));
        }
        return false;
    }

    /**
     * Whether a card asks AMBLIST for a report, which is the one statement a request deck is made of.
     */
    public static boolean isRequestCard(String text) {
        String card = text.trim();
        return !card.isEmpty() && FUNCTIONS.contains(card.split("[\\s,=]+", 2)[0].toUpperCase(Locale.ROOT));
    }

    public static List<ListLoad.Line> readLines(String source) {
        boolean report = isReport(source);
        List<ListLoad.Line> lines = new ArrayList<>();
        LineEndings.split(source, (text, lineEnding) -> {
            String carriageControl = "";
            String body = text;
            if (report && !body.isEmpty() && CARRIAGE_CONTROL.indexOf(body.charAt(0)) >= 0) {
                carriageControl = body.substring(0, 1);
                body = body.substring(1);
            }
            lines.add(new ListLoad.Line(randomId(), Markers.EMPTY, carriageControl, body, lineEnding));
        });
        return lines;
    }

    /**
     * A line with its blanks taken out and folded to upper case, which is how a heading is recognised
     * however far apart the release spread its letters.
     */
    public static String squeeze(String text) {
        StringBuilder squeezed = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (!Character.isWhitespace(c)) {
                squeezed.append(Character.toUpperCase(c));
            }
        }
        return squeezed.toString();
    }
}
