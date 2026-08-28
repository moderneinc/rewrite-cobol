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
package org.openrewrite.mainframe.db2.bind.trait;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.openrewrite.mainframe.db2.bind.tree.Bind;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * Reading the value of a DSN operand, which is parenthesised and holds either one name or a list of
 * them separated by commas, blanks, or both.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class Operands {

    static @Nullable String textOf(Bind.Command command, String keyword) {
        Bind.Operand operand = command.getParameter(keyword);
        if (operand == null) {
            return null;
        }
        String text = unwrap(operand.getValueText());
        return text.isEmpty() ? null : text;
    }

    /**
     * The names in an operand: {@code PKLIST(CLMPKG.*)} has one, {@code PLAN(CLMPLAN,CLMCICS)} and a
     * {@code PKLIST} written one package to a line have several.
     */
    static List<String> listOf(Bind.Command command, String keyword) {
        Bind.Operand operand = command.getParameter(keyword);
        if (operand == null) {
            return emptyList();
        }
        List<String> names = new ArrayList<>();
        for (String name : unwrap(operand.getValueText()).split("[,\\s]+")) {
            if (!name.isEmpty()) {
                names.add(name);
            }
        }
        return names;
    }

    /**
     * The value without the parentheses that hold it, and without the quotes around a data set name.
     */
    private static String unwrap(String value) {
        String text = value.trim();
        if (text.startsWith("(")) {
            text = text.substring(1);
        }
        if (text.endsWith(")")) {
            text = text.substring(0, text.length() - 1);
        }
        text = text.trim();
        if (text.length() > 1 && text.startsWith("'") && text.endsWith("'")) {
            text = text.substring(1, text.length() - 1);
        }
        return text;
    }
}
