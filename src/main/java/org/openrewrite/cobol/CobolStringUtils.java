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
package org.openrewrite.cobol;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.openrewrite.cobol.internal.CobolDialect;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CobolStringUtils {

    private static final String EMPTY_STRING = "";

    /**
     * Indicator area characters that mark a debugging line.
     */
    public static final Set<Character> DEBUGGING_INDICATORS = unmodifiableSet(new HashSet<>(asList('D', 'd')));

    private static final Pattern DEBUGGING_MODE = Pattern.compile("\\bDEBUGGING\\s+MODE\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern PROCEDURE_DIVISION = Pattern.compile("\\bPROCEDURE\\s+DIVISION\\b", Pattern.CASE_INSENSITIVE);

    /**
     * Debugging lines are only compiled when the SOURCE-COMPUTER paragraph specifies WITH DEBUGGING MODE.
     * Without it they are compiled as comment lines and need not be syntactically valid COBOL.
     */
    public static boolean isDebuggingModeEnabled(String source, CobolDialect cobolDialect) {
        CobolDialect.Columns columns = cobolDialect.getColumns();
        StringBuilder configuration = new StringBuilder();
        Scanner scanner = new Scanner(source);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.length() <= columns.getContentArea() ||
                    cobolDialect.getCommentIndicators().contains(line.charAt(columns.getIndicatorArea()))) {
                continue;
            }
            String contentArea = line.substring(columns.getContentArea(), Math.min(line.length(), columns.getOtherArea()));
            // The clause may only appear in the Configuration Section.
            if (PROCEDURE_DIVISION.matcher(contentArea).find()) {
                break;
            }
            configuration.append(contentArea).append('\n');
        }
        return DEBUGGING_MODE.matcher(configuration).find();
    }

    public static String trimLeadingChar(String contentArea) {
        return contentArea.substring(1);
    }

    public static String trimLeadingWhitespace(String contentArea) {
        return contentArea.replaceAll("^\\s+", EMPTY_STRING);
    }

    public static String trimTrailingWhitespace(String contentArea) {
        return contentArea.replaceAll("\\s+$", EMPTY_STRING);
    }

    /**
     * Position of the COBOL 2002 floating comment indicator ({@code *>}) in a content area, or -1.
     * The indicator either starts the content area or is preceded by a space, and never occurs inside a literal.
     */
    public static int indexOfFloatingComment(String contentArea) {
        char delimiter = 0;
        for (int i = 0; i < contentArea.length() - 1; i++) {
            char c = contentArea.charAt(i);
            if (delimiter != 0) {
                if (c == delimiter) {
                    delimiter = 0;
                }
            } else if (c == '\'' || c == '"') {
                delimiter = c;
            } else if (c == '*' && contentArea.charAt(i + 1) == '>' && (i == 0 || contentArea.charAt(i - 1) == ' ')) {
                return i;
            }
        }
        return -1;
    }

    public static boolean isSubstituteCharacter(String text) {
        return text.length() == 1 && '\u001A' == text.charAt(0);
    }
}
