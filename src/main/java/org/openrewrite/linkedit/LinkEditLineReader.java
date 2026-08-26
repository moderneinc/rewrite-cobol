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
package org.openrewrite.linkedit;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.openrewrite.cobol.LineEndings;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;

/**
 * Classifies the cards of a link-edit deck before they are lexed.
 * <p>
 * A binder control statement runs to the end of its card unless column 72 holds something, in which
 * case the next card carries on where it left off. Nothing in the text of a continuation card marks
 * it as one — it is a continuation because of a column on the card <em>above</em> it — so the lexer
 * cannot work this out and the grouping has to be decided here.
 * <p>
 * Columns 72-80 are not data either way: the binder reads operands from columns 1-71 and ignores
 * what a shop numbers its cards with, so those columns are kept out of the text the grammar sees and
 * print back from the white space in front of the next word.
 * <p>
 * An asterisk in column 1 makes the whole card a comment.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LinkEditLineReader {

    /**
     * How many columns of a card carry operands. Column 72 says whether the next card continues this
     * statement, and columns 73-80 are the shop's own sequence numbers.
     */
    public static final int DATA_COLUMNS = 71;

    /**
     * The operators a deck may open with. A deck often opens with the options rather than with what it
     * is linking, which is why {@code SETOPT} and {@code MODE} are here.
     */
    private static final Set<String> OPERATORS = new HashSet<>(Arrays.asList(
            "INCLUDE", "ENTRY", "ALIAS", "NAME", "ORDER", "MODE", "SETCODE", "SETOPT", "SETSSI",
            "CHANGE", "REPLACE", "IDENTIFY", "PAGE", "LIBRARY", "EXPAND", "INSERT"));

    /**
     * The operators that make a deck a link-edit deck rather than a preamble of options. Every deck
     * that builds anything writes at least one of them.
     */
    private static final Set<String> BUILDS = new HashSet<>(Arrays.asList(
            "INCLUDE", "ENTRY", "ALIAS", "NAME"));

    /**
     * Whether text is a link-edit deck, judged by its first control statement and by building
     * something. A {@code LINKLIB} member kept without an extension is known by nothing else, and the
     * library holds sort cards, IDCAMS cards and parm cards beside the binder decks.
     * <p>
     * {@code INCLUDE} alone says nothing: it opens a DFSORT deck as {@code INCLUDE COND=} and a job
     * as {@code INCLUDE MEMBER=}, so it counts only when it names a DD the way the binder does.
     */
    public static boolean isLinkEditDeck(String source) {
        boolean opens = false;
        boolean builds = false;
        for (String line : source.split("\n")) {
            if (isComment(line)) {
                continue;
            }
            String[] words = data(line).trim().split("[\\s(]+", 2);
            String operator = words[0].toUpperCase(Locale.ROOT);
            if (operator.isEmpty()) {
                continue;
            }
            if (!opens) {
                if (!OPERATORS.contains(operator) || !namesADdName(operator, line)) {
                    return false;
                }
                opens = true;
            }
            builds |= BUILDS.contains(operator) && namesADdName(operator, line);
        }
        return builds;
    }

    private static boolean namesADdName(String operator, String line) {
        if (!"INCLUDE".equals(operator)) {
            return true;
        }
        String operands = data(line).trim().substring("INCLUDE".length()).trim().toUpperCase(Locale.ROOT);
        return operands.indexOf('(') > 0 && !operands.startsWith("COND") && !operands.startsWith("MEMBER");
    }

    public static String readLines(String source) {
        StringBuilder p = new StringBuilder();

        int cursor = 0;
        boolean continued = false;
        Scanner scanner = new Scanner(source);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String data = data(line);
            if (isComment(line)) {
                // A comment carries no operands, so it neither opens a statement nor ends one: a
                // statement continued over it goes on being continued.
                p.append("^^COMMENT^^");
            } else if (data.trim().isEmpty()) {
                // A blank card carries no operands, so it cannot be the continuation of anything.
                continued = false;
            } else {
                p.append(continued ? "^^CARD_CONT^^" : "^^CARD^^");
                continued = continuesOnNextCard(line);
            }
            p.append(data);
            cursor = LineEndings.append(p, source, cursor + line.length());
        }
        return p.toString();
    }

    private static String data(String line) {
        return line.length() > DATA_COLUMNS ? line.substring(0, DATA_COLUMNS) : line;
    }

    private static boolean isComment(String line) {
        return line.startsWith("*");
    }

    private static boolean continuesOnNextCard(String line) {
        return line.length() > DATA_COLUMNS && !Character.isWhitespace(line.charAt(DATA_COLUMNS));
    }
}
