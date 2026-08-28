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

import lombok.Value;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Where a field sits on the screen, counted from one.
 */
@Value
public class Position {

    int line;
    int column;

    /**
     * Reads {@code (line,column)}, and a one member list holding one: a {@code DPAGE} writes its
     * cursor as {@code CURSOR=((line,column))}, a position per physical page. A third value, the
     * physical page a {@code POS} is on, is ignored.
     */
    static @Nullable Position of(@Nullable String text) {
        List<String> parts = Operands.membersOf(text);
        if (parts.size() == 1) {
            parts = Operands.membersOf(parts.get(0));
        }
        if (parts.size() < 2) {
            return null;
        }
        Integer line = Operands.integerOf(parts.get(0));
        Integer column = Operands.integerOf(parts.get(1));
        return line == null || column == null ? null : new Position(line, column);
    }

    @Override
    public String toString() {
        return "(" + line + "," + column + ")";
    }
}
