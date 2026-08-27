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
package org.openrewrite.textmember;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.openrewrite.cobol.LineEndings;
import org.openrewrite.marker.Markers;
import org.openrewrite.textmember.tree.TextMember;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import static org.openrewrite.Tree.randomId;

/**
 * Splits a member into the lines it was written as, and says what a line names.
 * <p>
 * Nothing else is taken out of the text. There is no grammar for any of these members here, so a line
 * is kept as it was written and what it says is read by the traits.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextMemberLineReader {

    /**
     * The longest a data set name may be, and the longest a member name may be. Both are what says a
     * token could be a name at all.
     */
    private static final int DATA_SET_NAME_LENGTH = 44;
    private static final int MEMBER_NAME_LENGTH = 8;

    /**
     * Two joiners running together end a name and start the next. CLIST writes {@code &CLMHLQ..JCL} to
     * end the symbol before the dot, so the qualifier and the library are two names and not one.
     */
    private static final Pattern JOINERS = Pattern.compile("[.\\-_]{2,}");

    /**
     * Whether text is a REXX exec, by the rule TSO/E itself uses: the first line is a comment holding
     * the word {@code REXX}.
     * <p>
     * This is what types an exec kept in a library without an extension, which is how a PDS member
     * arrives when it is copied off as it stands. A member whose first line does not say so is not an
     * exec as far as TSO is concerned either — {@code SYSEXEC} would refuse to run it.
     */
    public static boolean isRexxExec(String source) {
        for (String line : source.split("\n")) {
            if (line.trim().isEmpty()) {
                continue;
            }
            String first = line.trim();
            return first.startsWith("/*") && first.toUpperCase(Locale.ROOT).contains("REXX");
        }
        return false;
    }

    public static List<TextMember.Line> readLines(String source) {
        List<TextMember.Line> lines = new ArrayList<>();
        LineEndings.split(source, (text, lineEnding) ->
                lines.add(new TextMember.Line(randomId(), Markers.EMPTY, text, lineEnding)));
        return lines;
    }

    /**
     * Every token of a line that is spelled the way a name of the estate is spelled: a member name of
     * at most eight characters, a data set name of such qualifiers, or a condition name written with
     * hyphens or underscores.
     * <p>
     * This says only how a name may be spelled, and deliberately over-answers: an English word of eight
     * letters or fewer is shaped exactly like a member name, and no rule tells {@code MASTER} in a
     * sentence from {@code MASTER} in a library. Which of these tokens is a component is answered by
     * looking each one up among the members a repository holds, which is a join and not a lexical rule.
     * A token holding a lower case letter is not a name: the estate's names are upper case, so a word
     * that is not is prose.
     */
    public static List<String> names(String text) {
        List<String> names = new ArrayList<>();
        int cursor = 0;
        while (cursor < text.length()) {
            if (!isNameCharacter(text.charAt(cursor))) {
                cursor++;
                continue;
            }
            int end = cursor;
            while (end < text.length() && isNameCharacter(text.charAt(end))) {
                end++;
            }
            for (String token : JOINERS.split(text.substring(cursor, end))) {
                String name = trimJoiners(token);
                if (isName(name)) {
                    names.add(name);
                }
            }
            cursor = end;
        }
        return names;
    }

    private static boolean isNameCharacter(char c) {
        return c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z' || c >= '0' && c <= '9' ||
               c == '@' || c == '#' || c == '$' || c == '.' || c == '-' || c == '_';
    }

    private static boolean isJoiner(char c) {
        return c == '.' || c == '-' || c == '_';
    }

    private static String trimJoiners(String token) {
        int start = 0;
        int end = token.length();
        while (start < end && isJoiner(token.charAt(start))) {
            start++;
        }
        while (end > start && isJoiner(token.charAt(end - 1))) {
            end--;
        }
        return token.substring(start, end);
    }

    private static boolean isName(String token) {
        if (token.isEmpty() || token.length() > DATA_SET_NAME_LENGTH) {
            return false;
        }
        int part = 0;
        boolean first = true;
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if (c >= 'a' && c <= 'z') {
                return false;
            }
            if (isJoiner(c)) {
                if (part == 0) {
                    return false;
                }
                part = 0;
                first = true;
                continue;
            }
            if (first && !(c >= 'A' && c <= 'Z' || c == '@' || c == '#' || c == '$')) {
                return false;
            }
            if (++part > MEMBER_NAME_LENGTH) {
                return false;
            }
            first = false;
        }
        return part > 0;
    }
}
