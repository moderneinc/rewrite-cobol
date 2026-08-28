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
package org.openrewrite.mainframe.trait;

import lombok.Value;
import org.openrewrite.mainframe.Members;
import org.openrewrite.text.PlainText;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A name a member writes down, and the line it was written on.
 */
@Value
public class Mention {

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

    String text;

    /**
     * The one-based line the name was first written on.
     */
    int line;

    /**
     * Every name a member writes, distinct, in the order it first writes them.
     * <p>
     * This is what a search for a member name finds in the text, before any statement is understood.
     * It over-answers on purpose — an English word of eight letters or fewer is spelled exactly like a
     * member name — and which of these names is a component of the estate is answered by looking each
     * one up among the members a repository holds.
     */
    public static List<Mention> in(PlainText member) {
        List<Mention> names = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        List<String> lines = Members.lines(member.getText());
        for (int i = 0; i < lines.size(); i++) {
            for (String name : namesIn(lines.get(i))) {
                if (seen.add(name)) {
                    names.add(new Mention(name, i + 1));
                }
            }
        }
        return names;
    }

    /**
     * Every token of a line that is spelled the way a name of the estate is spelled: a member name of
     * at most eight characters, a data set name of such qualifiers, or a condition name written with
     * hyphens or underscores.
     * <p>
     * This says only how a name may be spelled, and deliberately over-answers: no rule tells
     * {@code MASTER} in a sentence from {@code MASTER} in a library. A token holding a lower case
     * letter is not a name: the estate's names are upper case, so a word that is not is prose.
     */
    private static List<String> namesIn(String text) {
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
