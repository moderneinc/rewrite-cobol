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
package org.openrewrite.jcl;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class JclLineReader {
    /**
     * Every JCL statement there is. {@code ELSE}, {@code ENDIF} and {@code COMMAND} were missing, so
     * a line carrying one was read as a nameless statement rather than as the statement it is.
     * {@code THEN} is deliberately absent: it ends an {@code IF}, it is not an operation of its own.
     */
    private static final Set<String> JCL_STATEMENT_NAMES = new HashSet<>(Arrays.asList("JOB", "JCLLIB", "CNTL", "ENDCNTL",
            "COMMAND", "DD", "ELSE", "ENDIF", "EXEC", "EXPORT", "IF", "INCLUDE", "NOTIFY", "OUTPUT", "PEND", "PROC",
            "SCHEDULE", "SET", "XMIT"));

    public static String readLines(String source) {
        StringBuilder p = new StringBuilder();

        int cursor = 0;
        JclLineContext jclLineContext = JclLineContext.NORM;
        String streamDelimiter = null;
        Scanner scanner = new Scanner(source);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            String trailingComment = null;
            String commentArea = null;
            LineType lineType = getLineType(line);
            // Only when columns 73-80 hold something. A line merely padded out to 80 would otherwise
            // open a comment area with nothing in it, which `jclCommentArea` cannot match — it needs
            // a word, and blanks are hidden — so the parser would recover by swallowing the next
            // statement's name field.
            if (line.length() > 72 && !line.substring(72).trim().isEmpty()) {
                commentArea = line.substring(72);
                line = line.substring(0, 72);
            }

            // A DD with DLM= ends its data at a string of its own choosing rather than at /*, which
            // is the whole reason for saying so: it is how a member containing /* is passed through.
            if (streamDelimiter != null && jclLineContext == JclLineContext.STREAM &&
                line.startsWith(streamDelimiter)) {
                p.append("^^STREAM_END^^");
                p.append(line);
                streamDelimiter = null;
                jclLineContext = null;
                cursor = appendEndOfLine(p, source, cursor + line.length() +
                        (commentArea == null ? 0 : commentArea.length()));
                continue;
            }

            if (lineType == LineType.JES2) {
                if (jclLineContext == JclLineContext.STREAM) {
                    p.append("^^STREAM_END^^");
                } else if (jclLineContext == JclLineContext.CONT) {
                    p.append("^^JES2_CONT^^");
                } else {
                    p.append("^^JES2^^");
                }
                jclLineContext = getLineContext(line);
            } else if (lineType == LineType.JES3) {
                if (jclLineContext == JclLineContext.CONT) {
                    p.append("^^JES3_CONT^^");
                } else {
                    p.append("^^JES3^^");
                }
                jclLineContext = getLineContext(line);
            } else if (lineType == LineType.JCL) {
                if (jclLineContext == JclLineContext.CONT) {
                    p.append("^^JCL_CONT^^");
                } else {
                    p.append("^^JCL^^");
                }
                jclLineContext = getLineContext(line);
            } else if (lineType == LineType.JCL_STATEMENT) {
                p.append("^^JCL_STATEMENT^^");
                jclLineContext = getLineContext(line);
                if (jclLineContext == JclLineContext.STREAM) {
                    // Check for trailing comment.
                    int i = 0;
                    boolean inDD = false;
                    int asteriskCount = 0;
                    for (; i < line.length(); i++) {
                        char c = line.charAt(i);
                        if (inDD) {
                            if (c == '*') {
                                asteriskCount++;
                            }
                            if (asteriskCount == 2) {
                                break;
                            }
                        }
                        if (c == 'D' && i - 2 > 0 &&
                                line.charAt(i - 1) == 'D' &&
                                (line.charAt(i - 2) == ' ' || line.charAt(i - 2) == '\t') &&
                                i + 1 < line.length() && (line.charAt(i + 1) == ' ' || line.charAt(i + 1) == '\t')) {
                            inDD = true;
                        }
                    }
                    if (asteriskCount == 2) {
                        trailingComment = line.substring(i);
                        line = line.substring(0, i);
                    }
                }
                jclLineContext = getLineContext(line);
                if (jclLineContext == JclLineContext.STREAM) {
                    streamDelimiter = streamDelimiter(operandField(line));
                }
            } else if (lineType == LineType.COMMENT) {
                p.append("^^COMMENT^^");
                if (jclLineContext != JclLineContext.CONT) {
                    jclLineContext = null;
                }
            } else {
                if (line.trim().startsWith("%%")) {
                    p.append("^^CM^^");
                    jclLineContext = null;
                } else if (jclLineContext == JclLineContext.STREAM) {
                    p.append("^^STREAM^^");
                } else {
                    p.append("^^UNKNOWN^^");
                    if (jclLineContext != JclLineContext.CONT) {
                        jclLineContext = null;
                    }
                }
            }

            p.append(line);
            if (trailingComment != null) {
                p.append("^^TC_START^^");
                p.append(trailingComment);
                p.append("^^TC_STOP^^");
            }

            if (commentArea != null) {
                p.append("^^CA_START^^");
                p.append(commentArea);
            }

            cursor = appendEndOfLine(p, source,
                    cursor + line.length() + (trailingComment == null ? 0 : trailingComment.length()) +
                            (commentArea == null ? 0 : commentArea.length()));
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

    private static LineType getLineType(String line) {
		char c0 = !line.isEmpty() ? line.charAt(0) : '~';
		char c1 = line.length() > 1 ? line.charAt(1) : '~';
		char c2 = line.length() > 2 ? line.charAt(2) : '~';
		char c3 = line.length() > 3 ? line.charAt(3) : '~';

		if (c0 == '/' && c1 == '/' && c2 == '*' && (c3 == ' ' || c3 == '~' || c3 == '*' || c3 == '=' || c3 == '-' || c3 == '/' || c3 == '\r' || c3 == '\n')) {
			return LineType.COMMENT;
		}
		if (c0 == '/' && c1 == '/' && c2 == '*') {
			return LineType.JES3;
		}
		if (c0 == '/' && c1 == '/') {
			String[] words = line.split("\\s+");
			return words.length >= 2 && JCL_STATEMENT_NAMES.contains(words[1]) ? LineType.JCL_STATEMENT : LineType.JCL;
		}
		if (c0 == '/' && c1 == '*') {
			return LineType.JES2;
		}

		return LineType.UNKNOWN;
	}

	@Getter
	@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
	private enum LineType {
        JCL_STATEMENT("//"),
        JCL("//"),
        JES2("/*"),
        JES3("//*"),
        COMMENT("//* "),
        UNKNOWN("");

        private final String prefix;
    }

    private enum JclLineContext {
        NORM, CONT, STREAM, CM_CONT
    }

    /**
     * The operand field of a line, which is what says whether the statement continues on the next
     * one. Everything after it is the comment field — so a line ending in a comment continues just as
     * surely as one ending in a comma, and looking at the end of the line misses it.
     * <p>
     * The field runs to the first blank <em>outside quotes</em>. Splitting on whitespace would cut
     * {@code 'DAILY POST',CLASS=A,} in half and lose the comma that says the statement continues.
     */
    private static String operandField(String line) {
        String trimmed = line.trim();
        if (!trimmed.startsWith("//")) {
            return trimmed;
        }
        int i = endOfField(trimmed, 0);
        int operationStart = startOfField(trimmed, i);
        int operationEnd = endOfField(trimmed, operationStart);
        String operation = trimmed.substring(operationStart, operationEnd).toUpperCase(Locale.ROOT);
        if (!JCL_STATEMENT_NAMES.contains(operation)) {
            // No operation, so what follows the name field is already the operands — a continuation.
            return trimmed.substring(operationStart, operationEnd);
        }
        int start = startOfField(trimmed, operationEnd);
        return trimmed.substring(start, endOfField(trimmed, start));
    }

    private static int startOfField(String line, int from) {
        int i = from;
        while (i < line.length() && (line.charAt(i) == ' ' || line.charAt(i) == '\t')) {
            i++;
        }
        return i;
    }

    private static int endOfField(String line, int from) {
        boolean quoted = false;
        int i = from;
        for (; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\'') {
                quoted = !quoted;
            } else if (!quoted && (c == ' ' || c == '\t')) {
                break;
            }
        }
        return i;
    }

    private static JclLineContext getLineContext(String line) {
        String operands = operandField(line);
        if (operands.endsWith(",")) {
            return JclLineContext.CONT;
        }
        if (isDataDefinition(line) && beginsInStreamData(operands)) {
            return JclLineContext.STREAM;
        }

        int tickCount = 0;
        for (char c : line.trim().toCharArray()) {
            if (c == '\'') {
                tickCount++;
            }
        }
        return tickCount % 2 == 0 ? JclLineContext.NORM : JclLineContext.CONT;
    }

    private static boolean isDataDefinition(String line) {
        String trimmed = line.trim();
        if (!trimmed.startsWith("//")) {
            return false;
        }
        int start = startOfField(trimmed, endOfField(trimmed, 0));
        return "DD".equals(trimmed.substring(start, endOfField(trimmed, start)).toUpperCase(Locale.ROOT));
    }

    /**
     * Whether a DD statement's operands say the data follows in the job stream. The first positional
     * parameter is {@code *} or {@code DATA}; a keyword parameter cannot come first.
     */
    private static boolean beginsInStreamData(String operands) {
        String first = firstParameter(operands).toUpperCase(Locale.ROOT);
        return "*".equals(first) || "DATA".equals(first);
    }

    /**
     * The string that ends the in-stream data, from {@code DLM=}. Without it the data ends at the
     * delimiter statement {@code /*}; with it, anything at all can be the terminator, which is the
     * point — {@code DLM} is how a member containing {@code /*} is passed through untouched.
     */
    private static @Nullable String streamDelimiter(String operands) {
        for (String parameter : parameters(operands)) {
            if (parameter.toUpperCase(Locale.ROOT).startsWith("DLM=")) {
                String delimiter = parameter.substring(4);
                if (delimiter.length() > 1 && delimiter.charAt(0) == '\'' && delimiter.endsWith("'")) {
                    delimiter = delimiter.substring(1, delimiter.length() - 1);
                }
                return delimiter.isEmpty() ? null : delimiter;
            }
        }
        return null;
    }

    private static String firstParameter(String operands) {
        List<String> parameters = parameters(operands);
        return parameters.isEmpty() ? "" : parameters.get(0);
    }

    /**
     * Splits an operand field on commas outside parentheses and quotes.
     */
    private static List<String> parameters(String operands) {
        List<String> parts = new ArrayList<>();
        int depth = 0;
        int start = 0;
        boolean quoted = false;
        for (int i = 0; i < operands.length(); i++) {
            char c = operands.charAt(i);
            if (c == '\'') {
                quoted = !quoted;
            } else if (quoted) {
                continue;
            } else if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
            } else if (c == ',' && depth == 0) {
                parts.add(operands.substring(start, i));
                start = i + 1;
            }
        }
        if (start < operands.length()) {
            parts.add(operands.substring(start));
        }
        return parts;
    }
}
