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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class JclLineReader {
    private static final Set<String> JCL_STATEMENT_NAMES = new HashSet<>(Arrays.asList("JOB", "JCLLIB", "CNTL", "ENDCNTL",
            "DD", "EXEC", "EXPORT", "IF", "INCLUDE", "NOTIFY", "OUTPUT", "PEND", "PROC", "SCHEDULE", "SET", "XMIT"));

    public static String readLines(String source) {
        StringBuilder p = new StringBuilder();

        int cursor = 0;
        JclLineContext jclLineContext = JclLineContext.NORM;
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

            cursor += line.length() + (trailingComment == null ? 0 : trailingComment.length()) + (commentArea == null ? 0 : commentArea.length());
            String endOfLine = source.substring(cursor);
            if (endOfLine.startsWith("\r\n")) {
                p.append("\r\n");
                cursor += 2;
            } else if (endOfLine.startsWith("\n")) {
                p.append("\n");
                cursor += 1;
            }
        }
        return p.toString();
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

    private static JclLineContext getLineContext(String line) {
        if (line.trim().endsWith(",")) {
            return JclLineContext.CONT;
        }

        int tickCount = 0;
        boolean checkDD = false;
        char[] charArray = line.trim().toCharArray();
        char prev = '~';
        for (int i = charArray.length - 1; i >= 0; i--) {
            char c = charArray[i];
            if (checkDD) {
                if (Character.isWhitespace(c)) {
                    prev = c;
                    continue;
                }
                if (c == 'D' && prev == 'D' && i - 1 >= 0 &&
                        (charArray[i - 1] == ' ' || charArray[i - 1] == '\t')) {
                    return JclLineContext.STREAM;
                }
                if (c != 'D') {
                    checkDD = false;
                }
            }

            if (c == '*') {
                checkDD = true;
            }
            if (c == '\'') {
                tickCount++;
            }
            prev = c;
        }

        return tickCount % 2 == 0 ? JclLineContext.NORM : JclLineContext.CONT;
    }
}
