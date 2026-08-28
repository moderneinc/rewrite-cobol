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
package org.openrewrite.mainframe.cobol;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.function.BiConsumer;

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

    /**
     * Hands each line of {@code source} to {@code line} as the text it was written as and whatever
     * ended it, which is empty on a last line the file does not end. For a reader that keeps a line as
     * a node of its own rather than handing text on to a grammar.
     */
    public static void split(String source, BiConsumer<String, String> line) {
        int cursor = 0;
        while (cursor < source.length()) {
            int newline = source.indexOf('\n', cursor);
            String text = newline < 0 ? source.substring(cursor) : source.substring(cursor, newline);
            cursor = newline < 0 ? source.length() : newline + 1;

            String ending = newline < 0 ? "" : "\n";
            if (text.endsWith("\r")) {
                text = text.substring(0, text.length() - 1);
                ending = "\r" + ending;
            }
            line.accept(text, ending);
        }
    }
}
