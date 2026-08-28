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
package org.openrewrite.mainframe.jcl;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.jspecify.annotations.Nullable;

import org.openrewrite.mainframe.cobol.LineEndings;

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

    /**
     * The JES3 control statements, which begin {@code //*} like a comment. Anything else after
     * {@code //*} is a comment — most often a card somebody commented out, {@code //*STEPLIB DD ...}.
     */
    private static final Set<String> JES3_STATEMENT_NAMES = new HashSet<>(Arrays.asList("DATASET", "ENDDATASET",
            "ENDPROCESS", "FORMAT", "MAIN", "NET", "NETACCT", "OPERATOR", "PAUSE", "PROCESS", "ROUTE", "SIGNOFF",
            "SIGNON"));

    /**
     * Whether text is JCL, judged by its first card: a member kept without an extension, or as
     * {@code .txt}, is known by nothing else. The card must begin {@code //} and be a comment, a name
     * on its own, or a statement — so a C++ comment line does not pass, and a job whose first card is
     * a {@code //JOBCARD} placeholder does.
     */
    public static boolean isJcl(String source) {
        String first = firstCard(source);
        if (!first.startsWith("//")) {
            return false;
        }
        if (first.length() == 2 || first.charAt(2) == '*') {
            return true;
        }
        String[] words = first.trim().split("\\s+");
        return words.length == 1 || JCL_STATEMENT_NAMES.contains(words[1].toUpperCase(Locale.ROOT));
    }

    /**
     * Whether any card at all begins {@code //}. A member named {@code .jcl} that has none is not
     * JCL, whatever else it is; one that has some is JCL the parser may or may not read.
     */
    public static boolean hasJcl(String source) {
        Scanner scanner = new Scanner(source);
        while (scanner.hasNextLine()) {
            if (scanner.nextLine().startsWith("//")) {
                return true;
            }
        }
        return false;
    }

    private static String firstCard(String source) {
        Scanner scanner = new Scanner(source);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (!line.trim().isEmpty()) {
                return line;
            }
        }
        return "";
    }

    public static String readLines(String source) {
        StringBuilder p = new StringBuilder();

        int cursor = 0;
        JclLineContext jclLineContext = JclLineContext.NORM;
        LineType lastType = null;
        boolean quoteOpen = false;
        String streamDelimiter = null;
        boolean streamIsData = false;
        Scanner scanner = new Scanner(source);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            // A data line is data in every column; under DLM /* is data too, and // is data in a DD DATA.
            if (jclLineContext == JclLineContext.STREAM && !line.startsWith(streamDelimiter == null ? "/*" : streamDelimiter) &&
                (streamIsData || !line.startsWith("//"))) {
                p.append("^^STREAM^^");
                p.append(line);
                cursor = LineEndings.append(p, source, cursor + line.length());
                continue;
            }

            String trailingComment = null;
            String commentArea = null;
            LineType lineType = getLineType(line, lastType == LineType.JES3 && jclLineContext == JclLineContext.CONT);
            if (lineType == LineType.JCL_STATEMENT) {
                quoteOpen = false;
            }
            // Only when columns 73-80 hold something. A line merely padded out to 80 would otherwise
            // open a comment area with nothing in it, which `commentArea` cannot match — it needs
            // a word, and blanks are hidden — so the parser would recover by swallowing the next
            // statement's name field. Nor when a literal open at column 72 closes after it, nor when
            // column 72 itself holds something: either way the line was never held to 72 columns,
            // and splitting it cuts a literal or a word in half that nothing downstream can put
            // back together.
            if (line.length() > 72 && line.charAt(71) == ' ' && !line.substring(72).trim().isEmpty() &&
                !(line.substring(72).indexOf('\'') >= 0 && quoteOpenAfter(operandField(line.substring(0, 72), quoteOpen), quoteOpen))) {
                // The blanks in front stay with the statement: `CA_START` takes the whole rest of
                // the line, so a comment area beginning with them is found twice in the source.
                int start = 72;
                while (line.charAt(start) == ' ') {
                    start++;
                }
                commentArea = line.substring(start);
                line = line.substring(0, start);
            }

            // A DD with DLM= ends its data at a string of its own choosing rather than at /*, which
            // is the whole reason for saying so: it is how a member containing /* is passed through.
            if (streamDelimiter != null && jclLineContext == JclLineContext.STREAM &&
                line.startsWith(streamDelimiter)) {
                p.append("^^STREAM_END^^");
                p.append(line);
                streamDelimiter = null;
                jclLineContext = null;
                cursor = LineEndings.append(p, source, cursor + line.length() +
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
                jclLineContext = getLineContext(line, operandField(line, false), false);
            } else if (lineType == LineType.JES3) {
                if (jclLineContext == JclLineContext.CONT) {
                    p.append("^^JES3_CONT^^");
                } else {
                    p.append("^^JES3^^");
                }
                jclLineContext = getLineContext(line, operandField(line, false), false);
            } else if (lineType == LineType.JCL) {
                if (jclLineContext == JclLineContext.CONT) {
                    p.append("^^JCL_CONT^^");
                } else {
                    p.append("^^JCL^^");
                }
                String operands = operandField(line, quoteOpen);
                quoteOpen = quoteOpenAfter(operands, quoteOpen);
                jclLineContext = getLineContext(line, operands, quoteOpen);
            } else if (lineType == LineType.JCL_STATEMENT) {
                p.append("^^JCL_STATEMENT^^");
                jclLineContext = getLineContext(line, operandField(line, false), false);
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
                String operands = operandField(line, false);
                quoteOpen = quoteOpenAfter(operands, false);
                jclLineContext = getLineContext(line, operands, quoteOpen);
                if (jclLineContext == JclLineContext.STREAM) {
                    streamDelimiter = streamDelimiter(operands);
                    streamIsData = "DATA".equalsIgnoreCase(firstParameter(operands));
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
            if (lineType != LineType.COMMENT) {
                lastType = lineType;
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

            cursor = LineEndings.append(p, source,
                    cursor + line.length() + (trailingComment == null ? 0 : trailingComment.length()) +
                            (commentArea == null ? 0 : commentArea.length()));
        }
        return p.toString();
    }

    /**
     * @param continuesJes3 whether the line before was a JES3 statement that continues, since a JES3
     *                      continuation card begins {@code //*} followed by the operands and nothing
     *                      else says what it is.
     */
    private static LineType getLineType(String line, boolean continuesJes3) {
		char c0 = !line.isEmpty() ? line.charAt(0) : '~';
		char c1 = line.length() > 1 ? line.charAt(1) : '~';
		char c2 = line.length() > 2 ? line.charAt(2) : '~';

		if (c0 == '/' && c1 == '/' && c2 == '*') {
			return continuesJes3 || JES3_STATEMENT_NAMES.contains(jes3Statement(line)) ? LineType.JES3 : LineType.COMMENT;
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

    private static String jes3Statement(String line) {
        return line.substring(3, endOfField(line, 3, false)).toUpperCase(Locale.ROOT);
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
     *
     * @param quoteOpen whether the line before ended inside a literal, in which case this line is
     *                  the rest of it and has no name or operation of its own.
     */
    private static String operandField(String line, boolean quoteOpen) {
        String trimmed = line.trim();
        if (!trimmed.startsWith("//")) {
            return trimmed;
        }
        if (quoteOpen) {
            int start = startOfField(trimmed, 2);
            return trimmed.substring(start, endOfField(trimmed, start, true));
        }
        int i = endOfField(trimmed, 0, false);
        int operationStart = startOfField(trimmed, i);
        int operationEnd = endOfField(trimmed, operationStart, false);
        String operation = trimmed.substring(operationStart, operationEnd).toUpperCase(Locale.ROOT);
        if (!JCL_STATEMENT_NAMES.contains(operation)) {
            // No operation, so what follows the name field is already the operands — a continuation.
            return trimmed.substring(operationStart, operationEnd);
        }
        int start = startOfField(trimmed, operationEnd);
        return trimmed.substring(start, endOfField(trimmed, start, false));
    }

    private static int startOfField(String line, int from) {
        int i = from;
        while (i < line.length() && (line.charAt(i) == ' ' || line.charAt(i) == '\t')) {
            i++;
        }
        return i;
    }

    private static int endOfField(String line, int from, boolean quoted) {
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

    /**
     * Whether a literal is still open at the end of the operand field. A literal too long for one
     * card carries on at column 16 of the next, and may carry on again from there.
     */
    private static boolean quoteOpenAfter(String operands, boolean quoteOpen) {
        for (char c : operands.toCharArray()) {
            if (c == '\'') {
                quoteOpen = !quoteOpen;
            }
        }
        return quoteOpen;
    }

    private static JclLineContext getLineContext(String line, String operands, boolean quoteOpen) {
        if (quoteOpen || operands.endsWith(",")) {
            return JclLineContext.CONT;
        }
        if (isDataDefinition(line) && beginsInStreamData(operands)) {
            return JclLineContext.STREAM;
        }
        return JclLineContext.NORM;
    }

    private static boolean isDataDefinition(String line) {
        String trimmed = line.trim();
        if (!trimmed.startsWith("//")) {
            return false;
        }
        int start = startOfField(trimmed, endOfField(trimmed, 0, false));
        return "DD".equals(trimmed.substring(start, endOfField(trimmed, start, false)).toUpperCase(Locale.ROOT));
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
