/*
 * Copyright 2025 the original author or authors.
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
package org.openrewrite.bms;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Scanner;

/**
 * Classifies BMS lines before they are lexed.
 * <p>
 * BMS map definitions are assembler macro source, so the layout is the assembler's: a name field
 * beginning in column 1, an operation, an operand field, and column 72 saying whether the statement
 * carries on. Nothing in the text of a continuation line marks it as one — it is a continuation
 * because of a character on the line <em>above</em> it — so the lexer cannot work this out and the
 * grouping has to be decided here.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BmsLineReader {

    /**
     * Column 72, where a non-blank says the next line continues this statement. Any character will
     * do; the corpus writes {@code -}, {@code X} and {@code *} interchangeably.
     */
    private static final int CONTINUATION_COLUMN = 71;

    /**
     * Columns 73-80, the identification-sequence field. Ignored by the assembler, so it is carried
     * as a marker rather than as operands.
     */
    private static final int SEQUENCE_AREA = 72;

    public static String readLines(String source) {
        StringBuilder p = new StringBuilder();

        int cursor = 0;
        boolean continued = false;
        Scanner scanner = new Scanner(source);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            // Only when columns 73-80 hold something. A line merely padded out to 80 would otherwise
            // open a sequence area with nothing in it, which the parser cannot match — it needs a
            // word, and blanks are hidden.
            String sequenceArea = null;
            if (line.length() > SEQUENCE_AREA && !line.substring(SEQUENCE_AREA).trim().isEmpty()) {
                sequenceArea = line.substring(SEQUENCE_AREA);
                line = line.substring(0, SEQUENCE_AREA);
            }

            boolean continues = continuesOnNextLine(line);
            if (isComment(line) || line.trim().isEmpty()) {
                p.append(isComment(line) ? "^^COMMENT^^" : "^^UNKNOWN^^");
                // Neither carries operands, so neither can be the continuation of anything.
                continued = false;
            } else if (continued) {
                p.append("^^BMS_CONT^^");
                continued = continues;
            } else {
                p.append(hasNameField(line) ? "^^BMS_NAMED^^" : "^^BMS^^");
                continued = continues;
            }

            p.append(line);
            if (sequenceArea != null) {
                p.append("^^CA_START^^");
                p.append(sequenceArea);
            }

            cursor = appendEndOfLine(p, source,
                    cursor + line.length() + (sequenceArea == null ? 0 : sequenceArea.length()));
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

    private static boolean isComment(String line) {
        return line.startsWith("*");
    }

    /**
     * Whether the line writes a name field, which begins in column 1. A macro with no name of its
     * own — every {@code DFHMDF} holding a screen literal rather than a field a program can read —
     * starts its operation further along instead.
     */
    private static boolean hasNameField(String line) {
        char c = line.charAt(0);
        return c != ' ' && c != '\t';
    }

    private static boolean continuesOnNextLine(String line) {
        return line.length() > CONTINUATION_COLUMN && line.charAt(CONTINUATION_COLUMN) != ' ';
    }
}
