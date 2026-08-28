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
package org.openrewrite.mainframe.sas.trait;

import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.text.PlainText;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.util.Collections.emptyList;

/**
 * The {@code %MACRO} statements of a program, which are what make a name an invocation rather than a
 * statement of the macro language. A shop's macro library is one member per macro, so a definition is
 * also the member's own name.
 */
@Value
public class MacroDefinition implements Trait<PlainText> {

    Cursor cursor;

    /**
     * The macros the program defines, in the order it defines them.
     */
    public List<Macro> getMacros() {
        List<Macro> macros = new ArrayList<>();
        for (Statements.Statement statement : Statements.in(getTree().getText())) {
            if (statement.isKeyword("%MACRO") && statement.getWordText(1) != null) {
                macros.add(new Macro(statement.getWordText(1), statement.getText(),
                        statement.getLine()));
            }
        }
        return macros;
    }

    @Value
    public static class Macro {

        /**
         * The prototype as written: the name, and the parameters where the macro takes any.
         */
        @Nullable
        String prototype;

        /**
         * The statement as written, which is where the parameters are read from.
         */
        String text;

        int line;

        public String getName() {
            String written = prototype == null ? "" : prototype;
            int paren = written.indexOf('(');
            return (paren < 0 ? written : written.substring(0, paren)).toUpperCase(Locale.ROOT);
        }

        /**
         * The parameter names, positional and keyword alike, in the order the prototype writes them.
         * A keyword parameter's default is left off: {@code SUBTTL=} is a parameter that defaults to
         * nothing.
         */
        public List<String> getParameters() {
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

        @Override
        public String toString() {
            return "%MACRO " + getName();
        }
    }

    public static class Matcher extends SimpleTraitMatcher<MacroDefinition> {

        @Override
        protected @Nullable MacroDefinition test(Cursor cursor) {
            return Statements.isProgram(cursor) ? new MacroDefinition(cursor) : null;
        }
    }

    @Override
    public String toString() {
        return "%MACRO of " + getTree().getSourcePath();
    }
}
