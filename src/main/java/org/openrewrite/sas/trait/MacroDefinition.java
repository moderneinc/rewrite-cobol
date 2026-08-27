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
package org.openrewrite.sas.trait;

import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.sas.tree.Sas;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.util.Collections.emptyList;

/**
 * A {@code %MACRO}, which is what makes a name an invocation rather than a statement of the macro
 * language. A shop's macro library is one member per macro, so this is also the member's own name.
 */
@Value
public class MacroDefinition implements Trait<Sas.Statement> {

    Cursor cursor;

    public String getName() {
        String prototype = prototype();
        int paren = prototype.indexOf('(');
        return (paren < 0 ? prototype : prototype.substring(0, paren)).toUpperCase(Locale.ROOT);
    }

    /**
     * The parameter names, positional and keyword alike, in the order the prototype writes them. A
     * keyword parameter's default is left on it: {@code SUBTTL=} is a parameter that defaults to
     * nothing.
     */
    public List<String> getParameters() {
        String text = getTree().getText();
        int open = text.indexOf('(');
        int close = text.lastIndexOf(')');
        if (open < 0 || close < open) {
            return emptyList();
        }
        List<String> parameters = new ArrayList<>();
        for (String argument : Statements.argumentsOf(text.substring(open + 1, close))) {
            int equals = argument.indexOf('=');
            String name = equals < 0 ? argument : argument.substring(0, equals);
            if (!name.trim().isEmpty()) {
                parameters.add(name.trim().toUpperCase(Locale.ROOT));
            }
        }
        return parameters;
    }

    public int getLine() {
        return Statements.lineOf(cursor);
    }

    private String prototype() {
        String prototype = getTree().getWordText(1);
        return prototype == null ? "" : prototype;
    }

    public static class Matcher extends SimpleTraitMatcher<MacroDefinition> {

        @Override
        protected @Nullable MacroDefinition test(Cursor cursor) {
            Object value = cursor.getValue();
            return value instanceof Sas.Statement && ((Sas.Statement) value).isKeyword("%MACRO") &&
                   ((Sas.Statement) value).getWordText(1) != null ? new MacroDefinition(cursor) : null;
        }
    }

    @Override
    public String toString() {
        return "%MACRO " + getName();
    }
}
