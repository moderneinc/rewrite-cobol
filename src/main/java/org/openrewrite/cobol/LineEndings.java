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
package org.openrewrite.cobol;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * The line endings of a source a line reader is splitting into cards.
 * <p>
 * A member reaches a repository written with either ending, and one that was never a file ends without
 * one at all. Every line reader hands the grammar text that has to print back byte for byte, so what
 * ended a line is copied across rather than assumed.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LineEndings {

    /**
     * Copies the ending of the line at {@code cursor}, and answers with the offset past it.
     */
    public static int append(StringBuilder p, String source, int cursor) {
        if (source.startsWith("\r\n", cursor)) {
            p.append("\r\n");
            return cursor + 2;
        }
        if (source.startsWith("\n", cursor)) {
            p.append("\n");
            return cursor + 1;
        }
        return cursor;
    }
}
