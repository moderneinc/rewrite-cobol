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
package org.openrewrite.mainframe.ims;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.openrewrite.mainframe.cobol.MacroLines;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;

/**
 * Classifies IMS gen lines before they are lexed.
 * <p>
 * DBD, PSB, MFS and stage 1 source are all assembler macro source, so the layout is the assembler's
 * and {@link MacroLines} reads it. What is IMS's own is {@link #firstOperation}, which is how a gen
 * member kept as {@code .asm} is told from a program kept beside it.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ImsLineReader {

    /**
     * The assembler listing controls, which say nothing about what a member is. CardDemo opens both
     * of its DBDs with a {@code TITLE}, so what a member gens has to be looked for past them.
     */
    private static final Set<String> LISTING_CONTROLS =
            new HashSet<>(Arrays.asList("TITLE", "EJECT", "SPACE", "PRINT"));

    public static String readLines(String source) {
        return MacroLines.read(source, "IMS");
    }

    /**
     * The operation of the first macro statement, or null for a member that has none. This is what a
     * gen member is known by: an {@code .asm} whose first operation is {@code DBD} is a DBD source
     * member, and the assembler reader has to leave it alone.
     */
    public static @Nullable String firstOperation(String source) {
        Scanner scanner = new Scanner(source);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (MacroLines.isComment(line) || line.trim().isEmpty()) {
                continue;
            }
            String[] words = line.trim().split("\\s+");
            int operation = MacroLines.hasNameField(line) ? 1 : 0;
            if (words.length <= operation) {
                continue;
            }
            String word = words[operation].toUpperCase(Locale.ROOT);
            if (!LISTING_CONTROLS.contains(word)) {
                return word;
            }
        }
        return null;
    }
}
