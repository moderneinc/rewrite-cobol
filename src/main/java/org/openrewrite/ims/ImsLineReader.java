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
package org.openrewrite.ims;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.openrewrite.cobol.LineEndings;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;

/**
 * Classifies IMS gen lines before they are lexed.
 * <p>
 * DBD, PSB, MFS and stage 1 source are all assembler macro source, so the layout is the assembler's:
 * a name field beginning in column 1, an operation, an operand field, and column 72 saying whether
 * the statement carries on. Nothing in the text of a continuation line marks it as one — it is a
 * continuation because of a character on the line <em>above</em> it — so the lexer cannot work this
 * out and the grouping has to be decided here.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ImsLineReader {

    /**
     * Column 72, where a non-blank says the next line continues this statement. Any character will
     * do; the corpus writes {@code X} and {@code C} interchangeably.
     */
    private static final int CONTINUATION_COLUMN = 71;

    /**
     * Columns 73-80, the identification-sequence field. Ignored by the assembler, so it is carried
     * as a marker rather than as operands.
     */
    private static final int SEQUENCE_AREA = 72;

    /**
     * The assembler listing controls, which say nothing about what a member is. CardDemo opens both
     * of its DBDs with a {@code TITLE}, so what a member gens has to be looked for past them.
     */
    private static final Set<String> LISTING_CONTROLS =
            new HashSet<>(Arrays.asList("TITLE", "EJECT", "SPACE", "PRINT"));

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
                p.append("^^IMS_CONT^^");
                continued = continues;
            } else {
                p.append(hasNameField(line) ? "^^IMS_NAMED^^" : "^^IMS^^");
                continued = continues;
            }

            p.append(line);
            if (sequenceArea != null) {
                p.append("^^CA_START^^");
                p.append(sequenceArea);
            }

            cursor = LineEndings.append(p, source,
                    cursor + line.length() + (sequenceArea == null ? 0 : sequenceArea.length()));
        }
        return p.toString();
    }

    /**
     * The operation of the first macro statement, or null for a member that has none. This is what a
     * gen member is known by: an {@code .asm} whose first operation is {@code DBD} is a DBD source
     * member, and the assembler reader has to leave it alone.
     */
    public static @Nullable String firstOperation(String source) {
        Scanner scanner = new Scanner(source);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (isComment(line) || line.trim().isEmpty()) {
                continue;
            }
            String[] words = line.trim().split("\\s+");
            int operation = hasNameField(line) ? 1 : 0;
            if (words.length <= operation) {
                continue;
            }
            String word = words[operation].toUpperCase(Locale.ROOT);
            if (!LISTING_CONTROLS.contains(word)) {
                return word;
            }
        }
        return null;
    }

    private static boolean isComment(String line) {
        return line.startsWith("*");
    }

    /**
     * Whether the line writes a name field, which begins in column 1. Most gen statements have no
     * name of its own; the ones that do are labelling a data set group or a PCB.
     */
    private static boolean hasNameField(String line) {
        char c = line.charAt(0);
        return c != ' ' && c != '\t';
    }

    private static boolean continuesOnNextLine(String line) {
        return line.length() > CONTINUATION_COLUMN && line.charAt(CONTINUATION_COLUMN) != ' ';
    }
}
