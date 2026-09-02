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
package org.openrewrite.mainframe.controlcard.utility;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.openrewrite.mainframe.cobol.LineEndings;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;

/**
 * Classifies the cards of a Db2 utility deck before they are lexed.
 * <p>
 * There is no continuation character to look for: a control statement is free form and runs over as
 * many cards as it needs, which is why the words are grouped by the vocabulary in {@link Keywords}
 * rather than by where the cards end. What is left for a line reader is the comments, and both forms
 * are comments only in column one: {@code --} anywhere in the deck, and {@code *} before the first
 * keyword of it. A {@code *} after that is the {@code SELECT *} of an unload.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UtilityLineReader {

    /**
     * The verbs that need nothing after them to be a utility deck. Nothing else opens a deck with any
     * of them: the sort, IDCAMS, link-edit and AMBLIST readers each open with a verb of their own.
     */
    private static final Set<String> OPENING_VERBS = new HashSet<>(Arrays.asList(
            "GLOBAL", "TEMPLATE", "LISTDEF", "LISTDEFTBV", "PROCESS_OPTIONS"));

    /**
     * The verbs a Db2 utility shares with another card language, which open a deck only when the word
     * after them names the object the utility works on. {@code COPY} is ICETOOL's and ADRDSSU's too,
     * and a deck opening {@code LOAD} could be anything until {@code DATA} follows it.
     */
    private static final Set<String> OBJECT_VERBS = new HashSet<>(Arrays.asList(
            "UNLOAD", "LOAD", "COPY", "COPYTOCOPY", "MERGECOPY", "REORG", "RUNSTATS", "CHECK",
            "QUIESCE", "REBUILD", "RECOVER", "REPAIR", "MODIFY", "STOSPACE"));

    private static final Set<String> OBJECTS = new HashSet<>(Arrays.asList(
            "DATA", "TABLESPACE", "TABLESPACES", "TABLESPACESET", "INDEXSPACE", "INDEXSPACES",
            "INDEX", "TABLE", "DATABASE", "LIST", "STOGROUP", "RECOVERY", "STATISTICS", "LOB",
            "OBJECT", "LOCATE", "SET", "DBD"));

    /**
     * Whether text is a Db2 utility deck, judged by its first control statement. A control card
     * member is known by nothing else: the library holds sort cards, IDCAMS cards and parm cards
     * beside these, and none of them is told apart by its name.
     */
    public static boolean isUtilityDeck(String source) {
        boolean beforeFirstKeyword = true;
        for (String line : source.split("\n")) {
            if (isComment(line, 0, beforeFirstKeyword)) {
                continue;
            }
            String text = line.trim();
            if (text.isEmpty()) {
                continue;
            }
            beforeFirstKeyword = false;
            return opensDeck(text);
        }
        return false;
    }

    private static boolean opensDeck(String statement) {
        String[] words = statement.split("[\\s(]+", 3);
        String verb = words[0].toUpperCase(Locale.ROOT);
        if (OPENING_VERBS.contains(verb)) {
            return true;
        }
        String object = words.length > 1 ? words[1].toUpperCase(Locale.ROOT) : "";
        return OBJECT_VERBS.contains(verb) && OBJECTS.contains(object);
    }

    public static String readLines(String source) {
        StringBuilder p = new StringBuilder();

        int cursor = 0;
        boolean beforeFirstKeyword = true;
        Scanner scanner = new Scanner(source);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (isComment(line, 0, beforeFirstKeyword)) {
                p.append("^^COMMENT^^");
            } else if (!line.trim().isEmpty()) {
                beforeFirstKeyword = false;
            }
            p.append(line);
            cursor = LineEndings.append(p, source, cursor + line.length());
        }
        return p.toString();
    }

    /**
     * Whether the card beginning at {@code start} is a comment. Both forms are read in column one
     * only, so a {@code --} written as an operand and the {@code *} of {@code SELECT *} are not
     * comments.
     */
    public static boolean isComment(CharSequence source, int start, boolean beforeFirstKeyword) {
        char first = start < source.length() ? source.charAt(start) : ' ';
        if (beforeFirstKeyword && first == '*') {
            return true;
        }
        return first == '-' && start + 1 < source.length() && source.charAt(start + 1) == '-';
    }
}
