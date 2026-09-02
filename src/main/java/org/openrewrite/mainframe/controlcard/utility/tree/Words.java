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
package org.openrewrite.mainframe.controlcard.utility.tree;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * What a run of words says, as against how it was laid out.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class Words {

    /**
     * The words joined, each break between them reduced to one blank. Two words written against each
     * other are one — {@code X} and {@code '6F'} are the hexadecimal constant {@code X'6F'}, since the
     * lexer breaks a quoted string out on its own.
     */
    static String textOf(List<Utility.Word> words) {
        StringBuilder text = new StringBuilder();
        for (Utility.Word word : words) {
            if (text.length() > 0 && !word.getPrefix().getWhitespace().isEmpty()) {
                text.append(' ');
            }
            text.append(word.getText());
        }
        return text.toString();
    }
}
