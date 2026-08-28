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

import org.jspecify.annotations.Nullable;

import java.util.Locale;

/**
 * What {@code ATTRB} says about a field: whether the operator can type into it, and how it looks
 * when they do.
 * <p>
 * The vocabulary is closed — these are keywords of the {@code DFHMDF} macro, not a dialect — so a
 * token that is not one of these is a typo rather than something to carry through.
 */
public enum Attribute {
    /**
     * The operator cannot type into the field and the cursor skips over it.
     */
    ASKIP,
    /**
     * The operator cannot type into the field.
     */
    PROT,
    /**
     * The operator can type into the field. This is what makes a field an input.
     */
    UNPROT,
    /**
     * Numeric: the keyboard is shifted and only digits, minus and full stop are accepted.
     */
    NUM,
    BRT,
    NORM,
    /**
     * Not displayed — how a password field is written.
     */
    DRK,
    /**
     * The cursor is placed here when the map is sent.
     */
    IC,
    /**
     * The modified data tag is set before the operator types anything, so the field is returned
     * whether or not it was changed.
     */
    FSET,
    /**
     * Detectable by a light pen.
     */
    DET;

    public static @Nullable Attribute from(String token) {
        for (Attribute attribute : values()) {
            if (attribute.name().equals(token.trim().toUpperCase(Locale.ROOT))) {
                return attribute;
            }
        }
        return null;
    }
}
