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
package org.openrewrite.db2.bind.trait;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.openrewrite.Cursor;
import org.openrewrite.db2.bind.BindIsoVisitor;
import org.openrewrite.db2.bind.tree.Bind;
import org.openrewrite.db2.bind.tree.Space;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Which line of the deck each word was written on.
 * <p>
 * A deck carries no positions of its own, so they are counted from the white space the words are
 * separated by — the same text printing puts back. Counted once per deck and kept on the cursor
 * root, because every command of a deck asks.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class Lines {

    static Map<UUID, Integer> of(Cursor cursor) {
        Bind.CompilationUnit cu = cursor.firstEnclosingOrThrow(Bind.CompilationUnit.class);
        return cursor.getRoot().computeMessageIfAbsent("bind.lines." + cu.getId(), k -> {
            Map<UUID, Integer> lines = new HashMap<>();
            new BindIsoVisitor<Map<UUID, Integer>>() {
                int line = 1;

                @Override
                public Space visitSpace(Space space, Space.Location location, Map<UUID, Integer> p) {
                    for (int i = 0; i < space.getWhitespace().length(); i++) {
                        if (space.getWhitespace().charAt(i) == '\n') {
                            line++;
                        }
                    }
                    return space;
                }

                @Override
                public Bind.Word visitWord(Bind.Word word, Map<UUID, Integer> p) {
                    Bind.Word w = super.visitWord(word, p);
                    p.put(w.getId(), line);
                    return w;
                }
            }.visit(cu, lines);
            return lines;
        });
    }
}
