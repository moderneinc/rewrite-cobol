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
package org.openrewrite.mainframe.controlcard;

import org.openrewrite.Cursor;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Which line of a deck each word was written on.
 * <p>
 * A deck carries no positions of its own, so they are counted from the white space and comment cards
 * the words are separated by — the same text printing puts back. Counted once per deck and kept on the
 * cursor root, because every statement of a deck asks.
 * <p>
 * Every deck has words and white space of its own kind, so the walk belongs to the language that
 * declares them; what is counted along the way is the same for all of them and is here.
 */
public final class CardLines {

    private final Map<UUID, Integer> lines = new HashMap<>();
    private int line = 1;

    /**
     * Counts the card breaks in the white space in front of the next word.
     */
    public void space(String whitespace) {
        for (int i = 0; i < whitespace.length(); i++) {
            if (whitespace.charAt(i) == '\n') {
                line++;
            }
        }
    }

    /**
     * Puts the word standing here on the card the count has reached.
     */
    public void word(UUID id) {
        lines.put(id, line);
    }

    public static <T extends Tree> Map<UUID, Integer> of(Cursor cursor, Class<T> deckType,
                                                         TreeVisitor<?, CardLines> words) {
        T deck = cursor.firstEnclosingOrThrow(deckType);
        return cursor.getRoot().computeMessageIfAbsent("cardLines." + deck.getId(), k -> {
            CardLines lines = new CardLines();
            words.visit(deck, lines);
            return lines.lines;
        });
    }
}
