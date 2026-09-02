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
package org.openrewrite.mainframe.controlcard.utility.trait;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.mainframe.controlcard.CardLines;
import org.openrewrite.mainframe.controlcard.utility.UtilityIsoVisitor;
import org.openrewrite.mainframe.controlcard.utility.marker.Dialect;
import org.openrewrite.mainframe.controlcard.utility.tree.Space;
import org.openrewrite.mainframe.controlcard.utility.tree.Utility;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * What the operands of a utility control statement say, as against how they are written.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class Operands {

    /**
     * The value written under a keyword, or null when the keyword is not written.
     */
    static @Nullable String textOf(Utility.Block block, String keyword) {
        Utility.Operand operand = block.getOperand(keyword);
        return operand == null ? null : unwrapped(operand.getValueText());
    }

    /**
     * The names written under a keyword. A DD name list is written in parentheses and separated by
     * commas — {@code OUTDDN (OPENCLM, CLOSCLM)} — and a single name is written either way.
     */
    static List<String> namesOf(Utility.Block block, String keyword) {
        String text = textOf(block, keyword);
        if (text == null || text.isEmpty()) {
            return emptyList();
        }
        List<String> names = new ArrayList<>();
        for (String name : text.split(",")) {
            String trimmed = name.trim();
            if (!trimmed.isEmpty()) {
                names.add(trimmed);
            }
        }
        return names;
    }

    /**
     * A value with the parentheses or apostrophes it was written in taken off.
     */
    static String unwrapped(String text) {
        String value = text.trim();
        if (value.length() > 1 && (value.startsWith("(") && value.endsWith(")") ||
                                   value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1).trim();
        }
        return value;
    }

    /**
     * The one-based line of the deck a block was written on.
     */
    static int lineOf(Cursor cursor, Utility.Block block) {
        return CardLines.of(cursor, Utility.CompilationUnit.class, words())
                .getOrDefault(block.getVerb().getId(), 1);
    }

    /**
     * Which utility's language the deck is written in, from the marker the parser left on it.
     */
    static Dialect.Kind dialectOf(Cursor cursor) {
        return Dialect.of(cursor.firstEnclosingOrThrow(Utility.CompilationUnit.class).getMarkers());
    }

    /**
     * Where the words of a deck are, for {@link CardLines} to count the cards between them.
     */
    private static UtilityIsoVisitor<CardLines> words() {
        return new UtilityIsoVisitor<CardLines>() {
            @Override
            public Space visitSpace(Space space, Space.Location location, CardLines lines) {
                lines.space(space.getWhitespace());
                return space;
            }

            @Override
            public Utility.Word visitWord(Utility.Word word, CardLines lines) {
                Utility.Word w = super.visitWord(word, lines);
                lines.word(w.getId());
                return w;
            }
        };
    }
}
