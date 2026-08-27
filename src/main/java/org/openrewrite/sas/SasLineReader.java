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
package org.openrewrite.sas;

import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.openrewrite.marker.Markers;
import org.openrewrite.sas.tree.Sas;
import org.openrewrite.sas.tree.Space;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.openrewrite.Tree.randomId;

/**
 * Reads a SAS member statement by statement.
 * <p>
 * There is no grammar because there is almost no syntax to have one for: a statement runs to the
 * first semicolon written outside a quoted string or a comment, and SAS has no reserved words at
 * all, so what a statement is cannot be decided until its first word has been read. Everything that
 * is not a statement boundary stays a word, which is the depth a text search and a name reference
 * need and is all this reader claims to give.
 * <p>
 * The three things that do have to be read are the ones that move a boundary: a
 * {@code /*...*}{@code /} comment, which may stand anywhere a blank may; a statement beginning
 * {@code *} or {@code %*}, which is a comment running to its own semicolon; and a quoted string,
 * where a semicolon means nothing. Data written after a {@code DATALINES} statement is read as
 * words like anything else — it is not source, but it is text somebody searches.
 */
public final class SasLineReader {

    private static final Pattern OPENS_STEP = Pattern.compile(
            "(?im)^\\s*(DATA|PROC|%INCLUDE|%LET|%MACRO|LIBNAME|FILENAME|OPTIONS|TITLE\\d?)\\b");

    private static final Pattern ENDS_STEP = Pattern.compile("(?im)^\\s*(RUN|QUIT)\\s*;");

    private final String source;

    /**
     * Everything before this has been taken into a node, so it is where the next prefix starts.
     */
    private int cursor;

    @Getter
    private final List<Sas> statements = new ArrayList<>();

    @Getter
    private Space eof = Space.EMPTY;

    /**
     * The parts of the statement being read, or null between statements — which is also what says
     * that a {@code *} here opens a comment rather than multiplying.
     */
    private @Nullable List<Sas> open;
    private Space openPrefix = Space.EMPTY;

    public SasLineReader(String source) {
        this.source = source;
        read();
    }

    /**
     * Whether a stream of text is a SAS program: something that opens a step and something that ends
     * one. Both are asked for because a job's {@code SYSIN} carries sort cards, IDCAMS commands and
     * TSO input under the same DD name.
     */
    public static boolean isSasProgram(String source) {
        return OPENS_STEP.matcher(source).find() && ENDS_STEP.matcher(source).find();
    }

    private void read() {
        int at = 0;
        while (at < source.length()) {
            char c = source.charAt(at);
            if (Character.isWhitespace(c)) {
                at++;
            } else if (source.startsWith("/*", at)) {
                at = readComment(at, closeOf(at));
            } else if (open == null && (c == '*' || source.startsWith("%*", at))) {
                at = readComment(at, endOf(at));
            } else if (c == ';') {
                at = closeStatement(at);
            } else {
                at = readWord(at);
            }
        }
        closeStatement(-1);
        eof = space(source.length());
    }

    private int readComment(int from, int to) {
        Space prefix = space(from);
        cursor = to;
        Sas.Comment comment = new Sas.Comment(randomId(), prefix, Markers.EMPTY, source.substring(from, to));
        if (open == null) {
            statements.add(comment);
        } else {
            open.add(comment);
        }
        return to;
    }

    /**
     * Where a word ends: the next blank, semicolon or comment. A quoted string is taken whole
     * wherever it begins, so {@code 'CLM.PROD.SAS - PAGER 4142'} is one word and the blanks and the
     * semicolons in it are none of the reader's business.
     */
    private int readWord(int from) {
        int at = from;
        while (at < source.length()) {
            char c = source.charAt(at);
            if (c == '\'' || c == '"') {
                at = quoteEnd(at, c);
            } else if (Character.isWhitespace(c) || c == ';' || source.startsWith("/*", at)) {
                break;
            } else {
                at++;
            }
        }
        Space prefix = space(from);
        cursor = at;
        if (open == null) {
            open = new ArrayList<>(4);
            openPrefix = prefix;
            prefix = Space.EMPTY;
        }
        open.add(new Sas.Word(randomId(), prefix, Markers.EMPTY, source.substring(from, at)));
        return at;
    }

    /**
     * Ends the statement being read at the semicolon standing at {@code at}, or without one when the
     * member ran out first. A semicolon with no statement in front of it is the null statement, which
     * SAS allows and which means nothing.
     */
    private int closeStatement(int at) {
        if (open == null && at < 0) {
            return at;
        }
        Space prefix = openPrefix;
        List<Sas> parts = open;
        if (parts == null) {
            prefix = space(at);
            parts = new ArrayList<>(0);
        }
        Sas.Word end = null;
        if (at >= 0) {
            end = new Sas.Word(randomId(), space(at), Markers.EMPTY, ";");
            cursor = at + 1;
        }
        statements.add(new Sas.Statement(randomId(), prefix, Markers.EMPTY, parts, end));
        open = null;
        openPrefix = Space.EMPTY;
        return at < 0 ? at : at + 1;
    }

    /**
     * Where the closing quote of the string opening at {@code at} is, one past it. A doubled quote
     * inside the string is an escaped one and does not close it.
     */
    private int quoteEnd(int at, char quote) {
        for (int i = at + 1; i < source.length(); i++) {
            if (source.charAt(i) == quote) {
                if (i + 1 < source.length() && source.charAt(i + 1) == quote) {
                    i++;
                } else {
                    return i + 1;
                }
            }
        }
        return source.length();
    }

    /**
     * One past the {@code *}{@code /} closing the comment opening at {@code at}, or the end of the
     * member for one nobody closed.
     */
    private int closeOf(int at) {
        int close = source.indexOf("*/", at + 2);
        return close < 0 ? source.length() : close + 2;
    }

    /**
     * One past the semicolon ending the comment statement opening at {@code at}. A quoted string
     * inside it holds no semicolon of consequence, the same as anywhere else.
     */
    private int endOf(int at) {
        int i = at + 1;
        while (i < source.length()) {
            char c = source.charAt(i);
            if (c == '\'' || c == '"') {
                i = quoteEnd(i, c);
            } else if (c == ';') {
                return i + 1;
            } else {
                i++;
            }
        }
        return source.length();
    }

    private Space space(int to) {
        Space prefix = Space.build(source.substring(cursor, to));
        cursor = to;
        return prefix;
    }
}
