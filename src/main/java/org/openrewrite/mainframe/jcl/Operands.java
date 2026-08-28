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
package org.openrewrite.mainframe.jcl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Reading what an operand says, where the value is not a tree of its own.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Operands {

    /**
     * The elements of a value written as a list: {@code (A,B)} or the single {@code A}, apostrophes
     * taken off. This is how {@code SYMLIST=} and {@code ORDER=} are written, and either may be
     * written with one element and no parentheses.
     */
    public static List<String> list(String text) {
        String value = text.trim();
        if (value.startsWith("(") && value.endsWith(")")) {
            value = value.substring(1, value.length() - 1);
        }
        List<String> elements = new ArrayList<>();
        for (String element : value.split(",")) {
            String trimmed = element.trim();
            if (trimmed.length() > 1 && trimmed.startsWith("'") && trimmed.endsWith("'")) {
                trimmed = trimmed.substring(1, trimmed.length() - 1);
            }
            if (!trimmed.isEmpty()) {
                elements.add(trimmed);
            }
        }
        return elements;
    }
}
