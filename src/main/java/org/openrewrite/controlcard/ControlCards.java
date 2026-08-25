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
package org.openrewrite.controlcard;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;

/**
 * How a control card member is typed.
 * <p>
 * A control card library holds sort cards, IDCAMS cards, bind decks, parm cards and plain data side
 * by side, and nothing in a member's name says which it is — the shops that name them
 * {@code SRTxxxxx} and {@code DEFxxxxx} are following a convention, not a rule, and the same library
 * holds members that follow no convention at all. So a member is typed by reading its first
 * statement, and a member that opens with nothing recognisable stays plain text.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ControlCards {

    /**
     * Whether a parser that reads decks satisfying {@code opensDeck} should take this file. The
     * extension only narrows what is worth opening; the content decides.
     */
    public static boolean accept(Path path, List<String> extensions, Predicate<String> opensDeck) {
        String name = path.getFileName().toString().toLowerCase();
        for (String extension : extensions) {
            if (name.endsWith(extension)) {
                return Files.isRegularFile(path) && opensDeck.test(head(path));
            }
        }
        return name.indexOf('.') < 0 && Files.isRegularFile(path) && opensDeck.test(head(path));
    }

    /**
     * The opening cards of a member. A deck is typed by its first statement, so there is no reason to
     * read further, and a control card library holds members big enough that it matters.
     */
    public static String head(Path path) {
        try (InputStream in = Files.newInputStream(path)) {
            byte[] bytes = new byte[4096];
            int read = in.read(bytes);
            return read < 0 ? "" : new String(bytes, 0, read, StandardCharsets.ISO_8859_1);
        } catch (IOException e) {
            return "";
        }
    }
}
