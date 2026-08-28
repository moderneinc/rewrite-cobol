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
package org.openrewrite.mainframe.ims.trait;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.openrewrite.mainframe.ims.tree.Ims;
import org.openrewrite.mainframe.ims.tree.Operand;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * Reading the value of an operand, which the gen macros write in three shapes: a word, a number, and
 * a parenthesised list of either — nested one level for a logical parent.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class Operands {

    static @Nullable String textOf(Ims.MacroStatement statement, String keyword) {
        Ims.KeywordOperand operand = statement.getParameter(keyword);
        return operand == null ? null : operand.getValueText();
    }

    static @Nullable Integer integerOf(Ims.MacroStatement statement, String keyword) {
        List<String> members = listOf(statement, keyword);
        return members.isEmpty() ? null : integerOf(members.get(0));
    }

    static @Nullable Integer integerOf(@Nullable String text) {
        if (text == null) {
            return null;
        }
        try {
            return Integer.valueOf(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    static List<String> listOf(Ims.MacroStatement statement, String keyword) {
        return membersOf(textOf(statement, keyword));
    }

    /**
     * The members of a parenthesised list, or the single value written without parentheses.
     * {@code ACCESS=(HDAM,VSAM)} and {@code ACCESS=INDEX} both say how the database is organised.
     * <p>
     * A nested list stays whole, so {@code PARENT=((CLMROOT),(POLROOT,PHYSICAL,CLMDBD02))} yields
     * the two parents and reading either one is a second call.
     */
    static List<String> membersOf(@Nullable String value) {
        if (value == null) {
            return emptyList();
        }
        String text = unwrap(value);
        if (text.isEmpty()) {
            return emptyList();
        }
        List<String> members = new ArrayList<>();
        int depth = 0;
        int start = 0;
        boolean quoted = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\'') {
                quoted = !quoted;
            } else if (quoted) {
                continue;
            } else if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
            } else if (c == ',' && depth == 0) {
                add(members, text.substring(start, i));
                start = i + 1;
            }
        }
        add(members, text.substring(start));
        return members;
    }

    /**
     * A quoted literal's text, blanks and all, or the value unchanged when it is not quoted. The
     * padding is what a length is counted from, so this does not trim.
     */
    static @Nullable String unquote(@Nullable String value) {
        if (value == null) {
            return null;
        }
        String text = value.trim();
        return isQuoted(text) ? text.substring(1, text.length() - 1) : text;
    }

    static boolean isQuoted(@Nullable String value) {
        String text = value == null ? "" : value.trim();
        return text.length() >= 2 && text.charAt(0) == '\'' && text.charAt(text.length() - 1) == '\'';
    }

    /**
     * The first operand written without a keyword, or null where the macro writes none. MFS puts the
     * one thing a {@code DFLD} or an {@code MFLD} is about there: the label of a device field, or the
     * literal to be written on the screen.
     */
    static @Nullable String positionalOf(Ims.MacroStatement statement) {
        for (Operand operand : statement.getParameters()) {
            if (operand instanceof Ims.PositionalOperand) {
                return ((Ims.PositionalOperand) operand).getValueText();
            }
        }
        return null;
    }

    /**
     * The first member, which for the shapes that name one thing is the whole of it.
     */
    static @Nullable String firstOf(Ims.MacroStatement statement, String keyword) {
        List<String> members = listOf(statement, keyword);
        return members.isEmpty() ? null : members.get(0);
    }

    /**
     * The value without the parentheses that hold it. {@code BYTES=(279)} says the same as
     * {@code BYTES=279}.
     */
    private static String unwrap(String value) {
        String text = value.trim();
        return text.startsWith("(") && text.endsWith(")") ?
                text.substring(1, text.length() - 1).trim() : text;
    }

    private static void add(List<String> members, String member) {
        if (!member.trim().isEmpty()) {
            members.add(member.trim());
        }
    }
}
