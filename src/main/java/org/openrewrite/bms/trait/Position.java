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
 * Where a field sits on the screen, one-based, as {@code POS} writes it.
 */
@Value
public class Position {

    int line;
    int column;

    /**
     * Reads {@code (line,column)}. The scalar form {@code POS=n} — an offset from the start of the
     * map — is not read here, because turning it into a line and column needs the map's own width
     * and this is a value, not a lookup. Null for anything else.
     */
    static @Nullable Position of(@Nullable String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        if (!trimmed.startsWith("(") || !trimmed.endsWith(")")) {
            return null;
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
