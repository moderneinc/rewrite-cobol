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
package org.openrewrite.controlcard.idcams.tree;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.EqualsAndHashCode;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

import static java.util.Collections.synchronizedMap;

/**
 * What separates one word of an IDCAMS deck from the next: white space, and the {@code /* … *}{@code /}
 * comments written among the commands, which carry no meaning of their own but have to print back
 * where they were.
 */
@EqualsAndHashCode
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@ref")
public class Space {
    public static final Space EMPTY = new Space("");

    @Nullable
    private final String whitespace;

    /*
     * Most occurrences of spaces will have no comments or markers and will be repeated frequently throughout a source file.
     * e.g.: a single space between keywords, or the common indentation of every line in a block.
     * So use flyweights to avoid storing many instances of functionally identical spaces
     */
    private static final Map<String, Space> flyweights = synchronizedMap(new WeakHashMap<>());

    private Space(@Nullable String whitespace) {
        this.whitespace = whitespace == null || whitespace.isEmpty() ? null : whitespace;
    }

    @JsonCreator
    public static Space build(@Nullable String whitespace) {
        if (whitespace == null || whitespace.isEmpty()) {
            return Space.EMPTY;
        }
        if (whitespace.length() <= 100) {
            return flyweights.computeIfAbsent(whitespace, k -> new Space(whitespace));
        }
        return new Space(whitespace);
    }

    public String getIndent() {
        return getWhitespaceIndent(whitespace);
    }

    private String getWhitespaceIndent(@Nullable String whitespace) {
        if (whitespace == null) {
            return "";
        }
        int lastNewline = whitespace.lastIndexOf('\n');
        if (lastNewline >= 0) {
            return whitespace.substring(lastNewline + 1);
        }
        if (lastNewline == whitespace.length() - 1) {
            return "";
        }
        return whitespace;
    }

    public String getWhitespace() {
        return whitespace == null ? "" : whitespace;
    }

    public Space withWhitespace(String whitespace) {
        if (whitespace.isEmpty()) {
            return Space.EMPTY;
        }

        if (this.whitespace == null || whitespace.equals(this.whitespace)) {
            return this;
        }
        return build(whitespace);
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    @Override
    public String toString() {
        return "Space(whitespace='" + getWhitespace().replace("\n", "\\n").replace("\r", "\\r") + "')";
    }

    public enum Location {
        COMPILATION_UNIT_PREFIX,
        COMPILATION_UNIT_EOF,
        COMMAND_PREFIX,
        PARAMETER_PREFIX,
        WORD_PREFIX
    }
}
