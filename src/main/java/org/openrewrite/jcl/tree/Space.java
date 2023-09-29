/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.jcl.tree;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.EqualsAndHashCode;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.marker.Markers;

import java.util.*;

import static java.util.Collections.emptyList;

/**
 * JCL white space.
 */
@EqualsAndHashCode
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@ref")
public class Space {
    public static final Space EMPTY = new Space("", emptyList());
    public static final Space SINGLE_SPACE = new Space(" ", emptyList());

    private final List<Comment> comments;

    @Nullable
    private final String whitespace;

    /*
     * Most occurrences of spaces will have no comments or markers and will be repeated frequently throughout a source file.
     * e.g.: a single space between keywords, or the common indentation of every line in a block.
     * So use flyweights to avoid storing many instances of functionally identical spaces
     */
    private static final Map<String, Space> flyweights = Collections.synchronizedMap(new WeakHashMap<>());

    private Space(@Nullable String whitespace, List<Comment> comments) {
        this.comments = comments;
        this.whitespace = whitespace == null || whitespace.isEmpty() ? null : whitespace;
    }

    @JsonCreator
    public static Space build(@Nullable String whitespace, List<Comment> comments) {
        if (comments.isEmpty()) {
            if (whitespace == null || whitespace.isEmpty()) {
                return Space.EMPTY;
            } else if (whitespace.length() <= 100) {
                //noinspection StringOperationCanBeSimplified
                return flyweights.computeIfAbsent(whitespace, k -> new Space(new String(whitespace), comments));
            }
        }
        return new Space(whitespace, comments);
    }

    public String getIndent() {
        if (!comments.isEmpty()) {
            return getWhitespaceIndent(comments.get(comments.size() - 1).getSuffix());
        }
        return getWhitespaceIndent(whitespace);
    }

    private String getWhitespaceIndent(@Nullable String whitespace) {
        if (whitespace == null) {
            return "";
        }
        int lastNewline = whitespace.lastIndexOf('\n');
        if (lastNewline >= 0) {
            return whitespace.substring(lastNewline + 1);
        } else if (lastNewline == whitespace.length() - 1) {
            return "";
        }
        return whitespace;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public String getWhitespace() {
        return whitespace == null ? "" : whitespace;
    }

    public Space withComments(List<Comment> comments) {
        if (comments == this.comments) {
            return this;
        }
        if (comments.isEmpty() && (whitespace == null || whitespace.isEmpty())) {
            return Space.EMPTY;
        }
        return build(whitespace, comments);
    }

    public Space withWhitespace(String whitespace) {
        if (comments.isEmpty() && whitespace.isEmpty()) {
            return Space.EMPTY;
        } else if (comments.isEmpty() && " ".equals(whitespace)) {
            return SINGLE_SPACE;
        }
        if ((whitespace.isEmpty() && this.whitespace == null) || whitespace.equals(this.whitespace)) {
            return this;
        }
        return build(whitespace, comments);
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    private static final String[] spaces = {
            "·₁", "·₂", "·₃", "·₄", "·₅", "·₆", "·₇", "·₈", "·₉", "·₊"
    };

    private static final String[] tabs = {
            "-₁", "-₂", "-₃", "-₄", "-₅", "-₆", "-₇", "-₈", "-₉", "-₊"
    };

    @Override
    public String toString() {
        StringBuilder printedWs = new StringBuilder();
        int lastNewline = 0;
        if (whitespace != null) {
            char[] charArray = whitespace.toCharArray();
            for (int i = 0; i < charArray.length; i++) {
                char c = charArray[i];
                if (c == '\n') {
                    printedWs.append("\\n");
                    lastNewline = i + 1;
                } else if (c == '\r') {
                    printedWs.append("\\r");
                    lastNewline = i + 1;
                } else if (c == ' ') {
                    printedWs.append(spaces[(i - lastNewline) % 10]);
                } else if (c == '\t') {
                    printedWs.append(tabs[(i - lastNewline) % 10]);
                }
            }
        }

        return "Space(whitespace='" + printedWs + "')";
    }

    public static Space format(String formatting, int beginIndex, int toIndex) {
        if (beginIndex == toIndex) {
            return Space.EMPTY;
        } else if (toIndex == beginIndex + 1 && ' ' == formatting.charAt(beginIndex)) {
            return Space.SINGLE_SPACE;
        } else {
            rangeCheck(formatting.length(), beginIndex, toIndex);
        }

        StringBuilder prefix = new StringBuilder();
        StringBuilder comment = new StringBuilder();
        List<Comment> comments = new ArrayList<>(1);

        boolean inSingleLineComment = false;

        int i = beginIndex;
        for (; i < toIndex; i++) {
            char c = formatting.charAt(i);
            switch (c) {
                case '/':
                    if (i + 3 <= toIndex) {
                        if (formatting.charAt(i + 1) == '/' && formatting.charAt(i + 2) == '*') {
                            if (i + 3 == toIndex) {
                                comments.add(new TextComment("", prefix.toString(), Markers.EMPTY));
                                prefix.setLength(0);
                                comment.setLength(0);
                                i += 2;
                            } else if (formatting.charAt(i + 3) == ' ' || formatting.charAt(i + 3) == '\n') {
                                inSingleLineComment = true;
                                i += 2;
                            }
                        }
                    }
                    break;
                case '\r':
                case '\n':
                    if (inSingleLineComment) {
                        inSingleLineComment = false;
                        comments.add(new TextComment(comment.toString(), prefix.toString(), Markers.EMPTY));
                        prefix.setLength(0);
                        comment.setLength(0);
                        prefix.append(c);
                    } else {
                        prefix.append(c);
                    }
                    break;
                default:
                    if (inSingleLineComment) {
                        comment.append(c);
                    } else {
                        prefix.append(c);
                    }
            }
        }
        // If a file ends with a single-line comment there may be no terminating newline
        if (comment.length() > 0) {
            comments.add(new TextComment(comment.toString(), prefix.toString(), Markers.EMPTY));
            prefix.setLength(0);
        }

        // Shift the whitespace on each comment forward to be a suffix of the comment before it, and the
        // whitespace on the first comment to be the whitespace of the tree element. The remaining prefix is the suffix
        // of the last comment.
        String whitespace = prefix.toString();
        if (!comments.isEmpty()) {
            for (i = comments.size() - 1; i >= 0; i--) {
                Comment c = comments.get(i);
                String next = c.getSuffix();
                comments.set(i, c.withSuffix(whitespace));
                whitespace = next;
            }
        }

        return build(whitespace, comments);
    }

    public enum Location {
        ASSIGNMENT,
        ASSIGNMENT_PREFIX,
        CNTL_STATEMENT_PREFIX,
        COMPILATION_UNIT_PREFIX,
        COMPILATION_UNIT_EOF,
        COMMENT_AREA_PREFIX,
        COMMENT_PREFIX,
        CONTROL_M_PREFIX,
        CONTROL_M_IF_PREFIX,
        CONTROL_M_ELSE_PREFIX,
        CONTROL_M_ENDIF_PREFIX,
        DATA_DEFINITION_STATEMENT_PREFIX,
        DATA_DEFINITION_STREAM_PREFIX,
        ELSE_STATEMENT_PREFIX,
        EMPTY_STATEMENT_PREFIX,
        END_CNTL_STATEMENT_PREFIX,
        END_IF_STATEMENT_PREFIX,
        EXEC_STATEMENT_PREFIX,
        EXPORT_STATEMENT_PREFIX,
        IDENTIFIER_PREFIX,
        IF_STATEMENT_PREFIX,
        INCLUDE_STATEMENT_PREFIX,
        JCL_LIB_STATEMENT_PREFIX,
        JCL_NAME_PREFIX,
        JCL_STATEMENT_PREFIX,
        JES2_PREFIX,
        JES3_PREFIX,
        JOB_STATEMENT_PREFIX,
        LITERAL_PREFIX,
        OUTPUT_STATEMENT_PREFIX,
        PARAMETERS,
        PARENTHESES,
        PARENTHESES_PREFIX,
        PEND_STATEMENT_PREFIX,
        PROC_STATEMENT_PREFIX,
        SET_STATEMENT_PREFIX,
        UNKNOWN_PREFIX,
        TRAILING_COMMENT_PREFIX,
        WORD_PREFIX,
        XMIT_STATEMENT_PREFIX
    }

    static void rangeCheck(int arrayLength, int fromIndex, int toIndex) {
        if (fromIndex > toIndex) {
            throw new IllegalArgumentException(
                    "fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
        }
        if (fromIndex < 0) {
            throw new StringIndexOutOfBoundsException(fromIndex);
        }
        if (toIndex > arrayLength) {
            throw new StringIndexOutOfBoundsException(toIndex);
        }
    }
}
