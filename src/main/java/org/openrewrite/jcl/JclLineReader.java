/*

 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.jcl;

import lombok.Getter;

import java.util.*;

public class JclLineReader {
    private static final Set<String> JCL_STATEMENT_NAMES = new HashSet<>(Arrays.asList("JOB", "JCLLIB", "CNTL", "ENDCNTL",
            "DD", "EXEC", "EXPORT", "IF", "INCLUDE", "NOTIFY", "OUTPUT", "PEND", "PROC", "SCHEDULE", "SET", "XMIT"));

    public static String readLines(String source) {
        StringBuilder p = new StringBuilder();

        int cursor = 0;
        JclLineContext jclLineContext = JclLineContext.NORM;
        JclLineContext prevLineContext = JclLineContext.NORM;
        boolean inCmIf = false;
        Scanner scanner = new Scanner(source);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            String trailingComment = null;
            String commentArea = null;
            LineType lineType = getLineType(line);
            if (line.length() > 72) {
                commentArea = line.substring(72);
                line = line.substring(0, 72);
            }

            String stepName = "";
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
                if (jclLineContext == JclLineContext.CONT || inCmIf && prevLineContext == JclLineContext.CONT) {
                    p.append("^^JCL_CONT^^");
                } else {
                    p.append("^^JCL^^");
                }
                jclLineContext = getLineContext(line);
            } else if (lineType == LineType.JCL_STATEMENT) {
                p.append("^^JCL_STATEMENT^^");
                String[] words = line.split("\\s+");
                stepName = words[0].substring(2);
                // Check for trailing comment.
                int i = line.indexOf(words[1]) + words[1].length();
                char prev = '~';
                for (; i < line.length(); i++) {
                    char c = line.charAt(i);
                    if (Character.isWhitespace(c)) {
                        continue;
                    }

                    if (c == '*' && (prev != '~' && prev != '=') && (i + 1 == line.length() || i + 1 < line.length() && line.charAt(i + 1) != '.')) {
                        trailingComment = line.substring(i);
                        line = line.substring(0, i);
                        break;
                    }

                    prev = c;
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
                    if (line.trim().startsWith("%%IF")) {
                        inCmIf = true;
                        prevLineContext = jclLineContext;
                    } else if (inCmIf && line.trim().startsWith("%%ENDIF")) {
                        inCmIf = false;
                        prevLineContext = JclLineContext.NORM;
                        jclLineContext = getLineContext(line);
                    }
                } else if (jclLineContext == JclLineContext.STREAM) {
                    p.append("^^STREAM^^");
                } else {
                    p.append("^^UNKNOWN^^");
                    if (jclLineContext != JclLineContext.CONT) {
                        jclLineContext = null;
                    }
                }
            }

            if (!stepName.isEmpty()) {
                p.append("//");
                p.append("^^STEP_NAME_START^^");
                p.append(stepName);
                p.append("^^STEP_NAME_END^^");
                line = line.substring(2 + stepName.length());
                cursor += 2 + stepName.length();
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
        } else if (c0 == '/' && c1 == '/' && c2 == '*') {
            return LineType.JES3;
        } else if (c0 == '/' && c1 == '/') {
            String[] words = line.split("\\s+");
            return words.length >=2 && JCL_STATEMENT_NAMES.contains(words[1]) ? LineType.JCL_STATEMENT : LineType.JCL;
        } else if (c0 == '/' && c1 == '*') {
            return LineType.JES2;
        }

        return LineType.UNKNOWN;
    }

    @Getter
    private enum LineType {
        JCL_STATEMENT("//"),
        JCL("//"),
        JES2("/*"),
        JES3("//*"),
        COMMENT("//* "),
        UNKNOWN("");

        private final String prefix;

        LineType(String prefix) {
            this.prefix = prefix;
        }
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