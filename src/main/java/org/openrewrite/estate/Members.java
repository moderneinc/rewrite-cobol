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
package org.openrewrite.estate;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.openrewrite.cobol.LineEndings;
import org.openrewrite.text.PlainText;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;

/**
 * The members an estate keeps as plain text: which technologies they are, and the extensions each is
 * kept under.
 * <p>
 * There is no grammar here for any of them, and holding them anyway is the point — a shop's CLISTs
 * say how a job is started by hand, its run books say what a job is for, and its SAS says which
 * tables a report read. So an ingestion keeps the member as the text it was written as and the traits
 * read what it names: {@link org.openrewrite.estate.trait.Script} a CLIST or an exec,
 * {@link org.openrewrite.estate.trait.RunBook} a run book member,
 * {@link org.openrewrite.sas.trait.Include} and the rest a SAS program,
 * {@link org.openrewrite.listload.trait.ModuleListing} a load module listing.
 * <p>
 * Which technology a member is comes from its path, since a library holds members of every kind side
 * by side and a member's name says only what it is about. Two kinds are also known by what they open
 * with, which is how they are found where the path says nothing: an exec kept without an extension —
 * how a PDS member arrives when it is copied off as it stands — and an AMBLIST request deck in a
 * control card library.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Members {

    /**
     * Compared case-insensitively, as are all of these.
     */
    public static final List<String> CLIST_FILE_EXTENSIONS = unmodifiableList(asList(".clist", ".clst"));

    public static final List<String> REXX_FILE_EXTENSIONS = unmodifiableList(asList(".rexx", ".rex", ".rx"));

    public static final List<String> DOCUMENT_FILE_EXTENSIONS = unmodifiableList(
            asList(".docjob", ".docpgm", ".docfich", ".docappl", ".docoper"));

    public static final List<String> C_FILE_EXTENSIONS = unmodifiableList(asList(".c", ".h"));

    public static final List<String> PLI_FILE_EXTENSIONS = unmodifiableList(asList(".pli", ".pl1"));

    public static final List<String> SAS_FILE_EXTENSIONS = unmodifiableList(singletonList(".sas"));

    public static final List<String> LISTING_FILE_EXTENSIONS = unmodifiableList(
            asList(".amblist", ".binder", ".listload"));

    /**
     * The AMBLIST functions that report on a load library. A request deck opens with one of them, and
     * a report echoes the request it was given.
     */
    private static final Set<String> FUNCTIONS = new HashSet<>(
            asList("LISTLOAD", "LISTIDR", "LISTOBJ", "LISTLPA"));

    /**
     * The headings AMBLIST and the binder print, with the blanks taken out: both spread a heading's
     * letters apart, and by how much is a matter of the release.
     */
    private static final Set<String> HEADINGS = new HashSet<>(asList(
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
     * What technology a member kept as text is, and the extensions it is kept under.
     */
    public enum Kind {
        /**
         * A CLIST, run from {@code SYSPROC}. Nothing in one says what it is — {@code PROC 1 MEM} is a
         * statement of several other languages too — so only the extension types it.
         */
        CLIST(CLIST_FILE_EXTENSIONS),
        /**
         * A REXX exec, run from {@code SYSEXEC} or {@code SYSPROC}.
         */
        REXX(REXX_FILE_EXTENSIONS),
        /**
         * A run book member: Desjardins' {@code DOCJOB}, {@code DOCPGM}, {@code DOCFICH},
         * {@code DOCAPPL} and {@code DOCOPER}, one per job, program, file, application and operating
         * procedure. Which of the five a member is comes from its first word, not from this.
         */
        DOCUMENT(DOCUMENT_FILE_EXTENSIONS),
        /**
         * C, which on z/OS is the USS side of an application and the exits its products were written
         * with. Nothing here reads it.
         */
        C(C_FILE_EXTENSIONS),
        /**
         * PL/I, typed so that a shop's PL/I is not taken for something else. Nothing here reads it.
         */
        PLI(PLI_FILE_EXTENSIONS),
        SAS(SAS_FILE_EXTENSIONS),
        /**
         * What AMBLIST or the binder reported about a load library, or the deck that asked for it.
         */
        LISTING(LISTING_FILE_EXTENSIONS);

        private final List<String> extensions;

        Kind(List<String> extensions) {
            this.extensions = extensions;
        }

        public List<String> getExtensions() {
            return extensions;
        }
    }

    /**
     * What a member at this path is, or null for a path no kind claims.
     */
    public static @Nullable Kind kindOf(Path sourcePath) {
        Path fileName = sourcePath.getFileName();
        String name = (fileName == null ? sourcePath : fileName).toString().toLowerCase(Locale.ROOT);
        for (Kind kind : Kind.values()) {
            for (String extension : kind.extensions) {
                if (name.endsWith(extension)) {
                    return kind;
                }
            }
        }
        return null;
    }

    /**
     * What a member held as text is: its path, or where that says nothing, what it opens with.
     */
    public static @Nullable Kind kindOf(PlainText text) {
        Kind kind = kindOf(text.getSourcePath());
        if (kind != null) {
            return kind;
        }
        if (isRexxExec(text.getText())) {
            return Kind.REXX;
        }
        return isModuleListing(text.getText()) ? Kind.LISTING : null;
    }

    /**
     * The path globs an ingestion keeps these members as text by, one per extension.
     */
    public static List<String> masks() {
        List<String> masks = new ArrayList<>();
        for (Kind kind : Kind.values()) {
            for (String extension : kind.extensions) {
                masks.add("**/*" + extension);
            }
        }
        return masks;
    }

    /**
     * Whether text is a REXX exec, by the rule TSO/E itself uses: the first line is a comment holding
     * the word {@code REXX}.
     * <p>
     * A member whose first line does not say so is not an exec as far as TSO is concerned either —
     * {@code SYSEXEC} would refuse to run it.
     */
    public static boolean isRexxExec(String text) {
        String first = firstCard(text);
        return first.startsWith("/*") && first.toUpperCase(Locale.ROOT).contains("REXX");
    }

    /**
     * Whether text is something AMBLIST or the binder produced, or a request deck for AMBLIST. A
     * control card library holds these beside sort cards, IDCAMS cards and parm cards, and nothing in
     * a member's name says which it is.
     */
    public static boolean isModuleListing(String text) {
        return isReport(text) || isRequest(text);
    }

    /**
     * Whether text is a printed report rather than a deck, judged by the headings it opens with.
     */
    public static boolean isReport(String text) {
        int read = 0;
        int cursor = 0;
        while (cursor < text.length()) {
            int newline = text.indexOf('\n', cursor);
            String line = text.substring(cursor, newline < 0 ? text.length() : newline);
            cursor = newline < 0 ? text.length() : newline + 1;

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
    public static boolean isRequest(String text) {
        return isRequestCard(firstCard(text));
    }

    /**
     * The first card of a member holding anything, trimmed. A member is typed by what it opens with,
     * so there is no reason to read further — and a listing runs to a million lines.
     */
    private static String firstCard(String text) {
        int cursor = 0;
        while (cursor < text.length()) {
            int newline = text.indexOf('\n', cursor);
            String line = text.substring(cursor, newline < 0 ? text.length() : newline).trim();
            if (!line.isEmpty()) {
                return line;
            }
            if (newline < 0) {
                break;
            }
            cursor = newline + 1;
        }
        return "";
    }

    /**
     * Whether a card asks AMBLIST for a report, which is the one statement a request deck is made of.
     */
    public static boolean isRequestCard(String text) {
        String card = text.trim();
        return !card.isEmpty() && FUNCTIONS.contains(card.split("[\\s,=]+", 2)[0].toUpperCase(Locale.ROOT));
    }

    /**
     * A line with its blanks taken out and folded to upper case, which is how a report's heading is
     * recognised however far apart the release spread its letters.
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

    /**
     * The lines of a member as they were written, without the endings that separated them. Every
     * reference a trait reads carries the one-based position in this list.
     */
    public static List<String> lines(String text) {
        List<String> lines = new ArrayList<>();
        LineEndings.split(text, (line, ending) -> lines.add(line));
        return lines;
    }
}
