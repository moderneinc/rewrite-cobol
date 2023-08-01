/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.cobol;

import org.openrewrite.cobol.internal.CobolDialect;

import java.util.*;

import static org.openrewrite.cobol.CobolStringUtils.*;

/**
 * Trim COBOL column areas, remove comments, mark comment entries, and concatenate String literals continued on new lines.
 */
public class CobolLineReader {

    private static final List<String> triggersStart = Arrays.asList("AUTHOR", "INSTALLATION", "DATE-WRITTEN",
            "DATE-COMPILED", "SECURITY", "REMARKS");

    private static final List<String> triggersEnd = Arrays.asList("PROGRAM-ID", "AUTHOR", "INSTALLATION",
            "DATE-WRITTEN", "DATE-COMPILED", "SECURITY", "IDENTIFICATION", "ID", "ENVIRONMENT", "DATA", "PROCEDURE", "END");

    private boolean inCommentEntry = false;

    public String readLines(String source, CobolDialect cobolDialect) {
        int indicatorArea = cobolDialect.getColumns().getIndicatorArea();
        int contentAreaAStart = cobolDialect.getColumns().getContentArea();
        int contentAreaBEnd = cobolDialect.getColumns().getOtherArea();

        StringBuilder processedSource = new StringBuilder();

        boolean inLiteralOrHexnumber = false;
        int previousNewLineLength = 0;
        int trailingWhitespaceLength = 0;
        int cursor = 0;
        Scanner scanner = new Scanner(source);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            if (isSubstituteCharacter(line)) {
                processedSource.append(line);
                continue;
            }

            String indicator;
            if (line.length() < indicatorArea + 1) {
                indicator = "";
            } else if (line.length() == indicatorArea + 1) {
                indicator = line.substring(indicatorArea);
            } else {
                indicator = line.substring(indicatorArea, contentAreaAStart);
            }

            String contentArea;
            if (line.length() < contentAreaAStart + 1) {
                contentArea = "";
            } else if (line.length() == contentAreaAStart + 1) {
                contentArea = line.substring(contentAreaAStart);
            } else {
                contentArea = line.substring(contentAreaAStart, Math.min(line.length(), contentAreaBEnd));
            }
            boolean isValidText = !(" ".equals(indicator) && contentArea.trim().isEmpty());

            boolean isCommentLine = !indicator.isEmpty() && cobolDialect.getCommentIndicators().contains(indicator.charAt(0));
            if (inCommentEntry && !contentArea.isEmpty()) {
                if (!isCommentLine && startsWithTrigger(contentArea, triggersEnd)) {
                    inCommentEntry = false;
                } else {
                    // Mark the comment entry.
                    processedSource.append("*>CE ");
                }
            }

            // Comment entries are a specific type of comment that occur in the Identification Division.
            // Each comment entry needs to be marked uniquely to be recognized by the grammar.
            if (!isCommentLine && startsWithTrigger(contentArea, triggersStart)) {
                inCommentEntry = true;
                String firstWords = getFirstWords(contentArea);
                // Comment entries in older COBOL dialects may start in line with the paragraph start.
                if (!contentArea.substring(firstWords.length()).trim().isEmpty()) {
                    contentArea = firstWords + " *>CE " + contentArea.substring(firstWords.length());
                }
            }

            if (!inCommentEntry && isCommentLine) {
                // Mark in-line comments.
                processedSource.append("*> ");
            }

            if (isValidText) {
                String trimmedContentArea = trimLeadingWhitespace(contentArea);
                if ("-".equals(indicator)) {
                    if (trimmedContentArea.startsWith("\"") || trimmedContentArea.startsWith("'")) {
                        if (inLiteralOrHexnumber) {
                            // Remove the previous newline character to concatenate the literal.
                            processedSource.delete(processedSource.length() - previousNewLineLength, processedSource.length());
                            // Remove the leading delimiter.
                            trimmedContentArea = trimLeadingChar(trimmedContentArea);
                        }
                    } else {
                        // Remove the previous newline character to concatenate the literal.
                        processedSource.delete(processedSource.length() - previousNewLineLength, processedSource.length());

                        // Remove trailing whitespace to concatenate the word or identifier.
                        processedSource.delete(processedSource.length() - trailingWhitespaceLength, processedSource.length());
                    }
                    inLiteralOrHexnumber = startOfLiteralOrHexNumber(contentArea);
                    processedSource.append(trimmedContentArea);
                } else {
                    inLiteralOrHexnumber = startOfLiteralOrHexNumber(trimmedContentArea);
                    processedSource.append(contentArea);
                }
            }

            cursor += line.length();
            trailingWhitespaceLength = contentArea.length() - trimTrailingWhitespace(contentArea).length();
            String endOfLine = source.substring(cursor).startsWith("\r\n") ? "\r\n" :
                    source.substring(cursor).startsWith("\n") ? "\n" : null;
            previousNewLineLength = endOfLine == null ? 0 : endOfLine.length();

            if (endOfLine != null) {
                cursor += endOfLine.length();
                if (inCommentEntry || isValidText) {
                    processedSource.append(endOfLine);
                }
            }
        }
        return processedSource.toString();
    }

    /**
     * Detect whether the previous content area may be continued as a literal or hex number.
     */
    private static boolean startOfLiteralOrHexNumber(String contentArea) {
        int ticks = 0;
        int quotes = 0;
        for (char c : contentArea.toCharArray()) {
            if (c == '\'') {
                ticks++;
            } else if (c == '"') {
                quotes++;
            }
        }
        return ticks % 2 != 0 || quotes % 2 != 0 || contentArea.endsWith("'") || contentArea.endsWith("\"");
    }

    private static String getFirstWords(String line) {
        int i = 0;
        char[] charArray = line.toCharArray();
        for (; i < charArray.length; i++) {
            char c = charArray[i];
            if (Character.isWhitespace(c)) {
                break;
            }
        }
        return line.substring(0, i);
    }

    private static boolean startsWithTrigger(String line, List<String> triggers) {
        String firstWordsUpper = getFirstWords(line).toUpperCase();
        boolean isTrigger = false;
        for (String trigger : triggers) {
            if (firstWordsUpper.equals(trigger) || firstWordsUpper.startsWith(trigger + ".")) {
                isTrigger = true;
                break;
            }
        }
        return isTrigger;
    }
}
