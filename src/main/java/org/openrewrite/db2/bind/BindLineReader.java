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
package org.openrewrite.db2.bind;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;

/**
 * Classifies the lines of a bind deck before they are lexed.
 * <p>
 * A DSN subcommand runs to the end of its line unless that line ends in a dash, in which case the
 * next line carries on where it left off. Nothing in the text of a continuation line marks it as one
 * — it is a continuation because of the line <em>above</em> it — so the lexer cannot work this out
 * and the grouping has to be decided here.
 * <p>
 * A deck is a TSO command stream, so all 80 columns are data: there is no sequence area to take off
 * the end, and the dash a line continues with is simply its last non-blank character.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BindLineReader {

    /**
     * The subcommands a deck may open with. {@code DSN} is the usual one — it names the subsystem the
     * binds run against — and a deck read straight from {@code SYSTSIN} of a step already under DSN
     * opens with the bind itself.
     */
    private static final Set<String> OPENING_VERBS = new HashSet<>(Arrays.asList("DSN", "BIND", "REBIND"));

    /**
     * Whether text is a bind deck, judged by its first subcommand and by binding something. A
     * {@code CARDLIB} member kept without an extension is known by nothing else, and the library
     * holds catalog query decks and IDCAMS cards beside the binds.
     */
    public static boolean isBindDeck(String source) {
        boolean opens = false;
        for (String line : source.split("\n")) {
            String[] words = line.trim().split("[\\s(]+", 2);
            String verb = words[0].toUpperCase(Locale.ROOT);
            if (verb.isEmpty()) {
                continue;
            }
            if (!opens) {
                if (!OPENING_VERBS.contains(verb)) {
                    return false;
                }
                opens = true;
            }
            if ("BIND".equals(verb) || "REBIND".equals(verb)) {
                return true;
            }
        }
        return false;
    }

    public static String readLines(String source) {
        StringBuilder p = new StringBuilder();

        int cursor = 0;
        boolean continued = false;
        Scanner scanner = new Scanner(source);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.trim().isEmpty()) {
                // A blank line carries no operands, so it cannot be the continuation of anything.
                continued = false;
            } else {
                p.append(continued ? "^^CARD_CONT^^" : "^^CARD^^");
                continued = continuesOnNextLine(line);
            }
            p.append(line);
            cursor = appendEndOfLine(p, source, cursor + line.length());
        }
        return p.toString();
    }

    private static int appendEndOfLine(StringBuilder p, String source, int cursor) {
        String endOfLine = source.substring(cursor);
        if (endOfLine.startsWith("\r\n")) {
            p.append("\r\n");
            return cursor + 2;
        }
        if (endOfLine.startsWith("\n")) {
            p.append("\n");
            return cursor + 1;
        }
        return cursor;
    }

    private static boolean continuesOnNextLine(String line) {
        int last = line.length() - 1;
        while (last >= 0 && Character.isWhitespace(line.charAt(last))) {
            last--;
        }
        return last >= 0 && line.charAt(last) == '-';
    }
}
