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
package org.openrewrite.controlcard.sort;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;

/**
 * Classifies the cards of a sort deck before they are lexed.
 * <p>
 * A control statement runs to the end of its card unless that card ends in a comma, in which case the
 * next one carries on where it left off. Nothing in the text of a continuation card marks it as one —
 * it is a continuation because of the card <em>above</em> it — so the lexer cannot work this out and
 * the grouping has to be decided here. An ICETOOL deck continues with a dash instead, and both are
 * read, since which utility is reading a deck is not written in the deck.
 * <p>
 * An asterisk in column 1 makes the whole card a comment. Everything else begins in column 2 or
 * later, which is why a statement is never found by looking at column 1.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SortLineReader {

    /**
     * The operators that can only be a sort deck. None of them opens an IDCAMS, IEBGENER, AMBLIST,
     * ADRDSSU or link-edit deck, which are the other things a {@code SYSIN} holds.
     */
    private static final Set<String> OPERATORS = new HashSet<>(Arrays.asList(
            "SORT", "MERGE", "OPTION", "INREC", "OUTREC", "OUTFIL", "SUM",
            "ALTSEQ", "MODS", "JOINKEYS", "REFORMAT", "DEBUG"));

    /**
     * {@code INCLUDE} also opens a link-edit deck and {@code OMIT} is rare enough to be worth
     * checking, so both are a sort deck only when they carry the operand DFSORT gives them.
     */
    private static final Set<String> FILTERS = new HashSet<>(Arrays.asList("INCLUDE", "OMIT"));

    /**
     * ICETOOL operators. Each names its input with {@code FROM}, which is what tells an ICETOOL
     * {@code COPY} from an ADRDSSU one and an ICETOOL {@code VERIFY} from an IDCAMS one.
     */
    private static final Set<String> TOOL_OPERATORS = new HashSet<>(Arrays.asList(
            "SELECT", "COPY", "COUNT", "DISPLAY", "OCCUR", "RANGE", "RESIZE", "SPLICE",
            "STATS", "SUBSET", "UNIQUE", "VERIFY"));

    /**
     * Whether text is a sort deck, judged by its first control statement. A control card member is
     * known by nothing else: the library holds IDCAMS cards, parm cards and DSN command decks beside
     * the sort cards, and none of them is told apart by its name.
     */
    public static boolean isSortDeck(String source) {
        for (String line : source.split("\n")) {
            if (isComment(line)) {
                continue;
            }
            String text = line.trim();
            if (text.isEmpty()) {
                continue;
            }
            return opensDeck(text);
        }
        return false;
    }

    private static boolean opensDeck(String statement) {
        String[] words = statement.split("\\s+", 2);
        String operator = words[0].toUpperCase(Locale.ROOT);
        String operands = words.length > 1 ? words[1].toUpperCase(Locale.ROOT) : "";
        if (OPERATORS.contains(operator)) {
            return true;
        }
        if (FILTERS.contains(operator)) {
            return operands.startsWith("COND=") || operands.startsWith("FORMAT=");
        }
        return TOOL_OPERATORS.contains(operator) && operands.startsWith("FROM(");
    }

    public static String readLines(String source) {
        StringBuilder p = new StringBuilder();

        int cursor = 0;
        boolean continued = false;
        Scanner scanner = new Scanner(source);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (isComment(line)) {
                // A comment carries no operands, so it neither opens a statement nor ends one: a
                // statement continued over it goes on being continued.
                p.append("^^COMMENT^^");
            } else if (line.trim().isEmpty()) {
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

    static boolean isComment(String line) {
        return line.startsWith("*");
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
        return last >= 0 && (line.charAt(last) == ',' || line.charAt(last) == '-');
    }
}
