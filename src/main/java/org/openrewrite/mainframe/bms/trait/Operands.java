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
package org.openrewrite.mainframe.bms.trait;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.openrewrite.mainframe.bms.tree.Bms;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * Reading the value of an operand, which BMS writes in four shapes: a word, a number, a quoted
 * string, and a parenthesised list of any of those.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class Operands {

    static @Nullable String textOf(Bms.MacroStatement statement, String keyword) {
        Bms.KeywordOperand operand = statement.getParameter(keyword);
        return operand == null ? null : operand.getValueText();
    }

    static @Nullable Integer integerOf(Bms.MacroStatement statement, String keyword) {
        return integerOf(textOf(statement, keyword));
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

    /**
     * The members of a parenthesised list, or the single value written without parentheses.
     * {@code ATTRB=(ASKIP,NORM)} and {@code ATTRB=ASKIP} both say what the field is like.
     */
    static List<String> listOf(Bms.MacroStatement statement, String keyword) {
        String text = textOf(statement, keyword);
        if (text == null) {
            return emptyList();
        }
        String trimmed = text.trim();
        if (!trimmed.startsWith("(") || !trimmed.endsWith(")")) {
            return singletonList(trimmed);
        }
        List<String> members = new ArrayList<>();
        for (String member : trimmed.substring(1, trimmed.length() - 1).split(",", -1)) {
            if (!member.trim().isEmpty()) {
                members.add(member.trim());
            }
        }
        return members;
    }

    /**
     * A quoted string without its quotes, with a doubled quote and a doubled ampersand each reduced
     * to one, since inside a quoted string the assembler reads both as the character itself.
     * Anything unquoted is returned as it stands — {@code INITIAL} is usually quoted and need not be.
     */
    static @Nullable String unquote(@Nullable String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        if (trimmed.length() < 2 || !trimmed.startsWith("'") || !trimmed.endsWith("'")) {
            return trimmed;
        }
        return trimmed.substring(1, trimmed.length() - 1).replace("''", "'").replace("&&", "&");
    }
}
