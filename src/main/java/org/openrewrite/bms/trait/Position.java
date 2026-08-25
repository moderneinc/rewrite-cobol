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
package org.openrewrite.bms.trait;

import lombok.Value;
import org.jspecify.annotations.Nullable;

/**
 * Where a field sits on the screen, counted from one.
 */
@Value
public class Position {

    int line;
    int column;

    /**
     * Reads {@code POS} in either form BMS writes it: {@code (line,column)} counted from one, or the
     * scalar {@code n}, a displacement from the start of the map counted from zero, which becomes a
     * line and a column only against {@code width}. Null for anything else.
     */
    static @Nullable Position of(@Nullable String text, int width) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        if (!trimmed.startsWith("(") || !trimmed.endsWith(")")) {
            Integer offset = Operands.integerOf(trimmed);
            return offset == null || offset < 0 || width <= 0 ? null :
                    new Position(offset / width + 1, offset % width + 1);
        }
        String[] parts = trimmed.substring(1, trimmed.length() - 1).split(",", -1);
        if (parts.length != 2) {
            return null;
        }
        Integer line = Operands.integerOf(parts[0]);
        Integer column = Operands.integerOf(parts[1]);
        return line == null || column == null ? null : new Position(line, column);
    }

    @Override
    public String toString() {
        return "(" + line + "," + column + ")";
    }
}
