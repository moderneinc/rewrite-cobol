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
package org.openrewrite.controlcard.idcams;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.openrewrite.controlcard.idcams.trait.IdcamsCommand;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;

/**
 * Classifies the cards of an IDCAMS deck before they are lexed.
 * <p>
 * A command runs to the end of its card unless that card ends in a dash, in which case the next one
 * carries on where it left off. Nothing in the text of a continuation card marks it as one — it is a
 * continuation because of the card <em>above</em> it — so the lexer cannot work this out and the
 * grouping has to be decided here.
 * <p>
 * A card holding nothing but a {@code /*}…{@code *}{@code /} comment is neither: it does not open a
 * command, and a command continued over it goes on being continued.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IdcamsLineReader {

    /**
     * The verbs that can only be an access method services command. None of them opens a sort,
     * IEBGENER, AMBLIST or link-edit deck.
     */
    private static final Set<String> VERBS = new HashSet<>(Arrays.asList(
            "DELETE", "DEL", "REPRO", "LISTCAT", "LISTC", "ALTER",
            "EXPORT", "IMPORT", "BLDINDEX", "BIX", "EXAMINE"));

    /**
     * The verbs that other utilities have too, each with the operand that tells IDCAMS from them:
     * ADRDSSU has a {@code PRINT} that names a data set or a volume rather than a file, ICETOOL has a
     * {@code VERIFY} that names its input with {@code FROM}, and CICS has a {@code DEFINE} whose
     * object is a resource group.
     */
    private static final Set<String> PRINT_OPERANDS = new HashSet<>(Arrays.asList(
            "INFILE(", "IFILE(", "INDATASET(", "IDS("));

    private static final Set<String> VERIFY_OPERANDS = new HashSet<>(Arrays.asList(
            "DATASET(", "DS(", "FILE(", "DSNAME("));

    /**
     * Whether text is an IDCAMS deck, judged by its first command. A control card member is known by
     * nothing else: the library holds sort cards, parm cards and DSN command decks beside the IDCAMS
     * cards, and none of them is told apart by its name.
     */
    public static boolean isIdcamsDeck(String source) {
        for (String line : source.split("\n")) {
            String text = withoutComments(line).trim();
            if (text.isEmpty()) {
                continue;
            }
            return opensDeck(text);
        }
        return false;
    }

    private static boolean opensDeck(String command) {
        String[] words = command.toUpperCase(Locale.ROOT).split("[\\s(]+", 3);
        String verb = words[0];
        if (VERBS.contains(verb)) {
            return true;
        }
        if ("DEFINE".equals(verb) || "DEF".equals(verb)) {
            // The object type may be written on the card after the verb, which is as far as a deck
            // can be typed by its opening card.
            String object = words.length > 1 ? words[1] : "";
            return IdcamsCommand.objectTypeOf(object) != null || "-".equals(object) || "+".equals(object);
        }
        String operands = command.substring(verb.length()).trim().toUpperCase(Locale.ROOT);
        if ("PRINT".equals(verb)) {
            return startsWithAny(operands, PRINT_OPERANDS);
        }
        return "VERIFY".equals(verb) && startsWithAny(operands, VERIFY_OPERANDS);
    }

    private static boolean startsWithAny(String operands, Set<String> keywords) {
        for (String keyword : keywords) {
            if (operands.startsWith(keyword)) {
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
            if (withoutComments(line).trim().isEmpty()) {
                if (line.trim().isEmpty()) {
                    // A blank card carries no parameters, so it cannot be the continuation of
                    // anything; a comment says nothing about the command it interrupts.
                    continued = false;
                } else {
                    p.append("^^COMMENT^^");
                }
            } else {
                p.append(continued ? "^^CARD_CONT^^" : "^^CARD^^");
                continued = continuesOnNextLine(line);
            }
            p.append(line);
            cursor = appendEndOfLine(p, source, cursor + line.length());
        }
        return p.toString();
    }

    /**
     * A card with its comments blanked out, which is how the reader decides whether the card says
     * anything at all. Only a comment closed on its own card counts, which is the rule the lexer
     * follows too.
     */
    static String withoutComments(String line) {
        StringBuilder text = new StringBuilder(line);
        int open = text.indexOf("/*");
        while (open >= 0) {
            int close = text.indexOf("*/", open + 2);
            if (close < 0) {
                break;
            }
            for (int i = open; i < close + 2; i++) {
                text.setCharAt(i, ' ');
            }
            open = text.indexOf("/*", close + 2);
        }
        return text.toString();
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

    /**
     * A dash continues an IDCAMS command and a plus continues a TSO one, and a {@code SYSTSIN} deck
     * is read by both — the terminal monitor program hands IDCAMS whatever it does not recognise
     * itself, so a shop writes its deletes in either dialect.
     */
    private static boolean continuesOnNextLine(String line) {
        String text = withoutComments(line);
        int last = text.length() - 1;
        while (last >= 0 && Character.isWhitespace(text.charAt(last))) {
            last--;
        }
        return last >= 0 && (text.charAt(last) == '-' || text.charAt(last) == '+');
    }
}
