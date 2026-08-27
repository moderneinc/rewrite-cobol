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
import org.openrewrite.text.PlainText;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static java.util.Collections.emptyList;

/**
 * The macros a program invokes: {@code %CLMTITL(RESERVE CHANGE BY CLAIM TYPE);}.
 * <p>
 * A name after a {@code %} is a macro the shop wrote or one of the statements the macro language
 * itself has, and nothing in the source tells them apart — so the statements are listed here and
 * everything else is an invocation. Whether the macro is one the estate defines is a second question,
 * answered by {@link MacroDefinition} over the members that could define it: a macro comes as often
 * out of an autocall library nobody keeps as out of a member beside the program.
 */
@Value
public class MacroCall implements Trait<PlainText> {

    /**
     * The macro language's own statements, which begin with a {@code %} and invoke nothing.
     */
    private static final Set<String> STATEMENTS = new HashSet<>(Arrays.asList(
            "%ABORT", "%COPY", "%DISPLAY", "%DO", "%ELSE", "%END", "%GLOBAL", "%GOTO", "%IF",
            "%INCLUDE", "%INPUT", "%LET", "%LIST", "%LOCAL", "%MACRO", "%MEND", "%PUT", "%RETURN",
            "%RUN", "%SYMDEL", "%SYSCALL", "%SYSEXEC", "%SYSLPUT", "%SYSMACDELETE", "%SYSRPUT",
            "%THEN", "%WINDOW"));

    Cursor cursor;

    /**
     * The macros the program invokes, in the order it invokes them.
     */
    public List<Reference> getReferences() {
        List<Reference> references = new ArrayList<>();
        for (Statements.Statement statement : Statements.in(getTree().getText())) {
            String keyword = statement.getKeyword();
            if (!keyword.startsWith("%") || keyword.length() < 2) {
                continue;
            }
            int paren = keyword.indexOf('(');
            String name = paren < 0 ? keyword : keyword.substring(0, paren);
            if (!STATEMENTS.contains(name.toUpperCase(Locale.ROOT))) {
                references.add(new Reference(name.substring(1), statement.getText(),
                        statement.getLine()));
            }
        }
        return references;
    }

    @Value
    public static class Reference {

        /**
         * The macro's name, without the {@code %}.
         */
        String name;

        /**
         * The statement as written, which is where the arguments are read from.
         */
        String text;

        int line;

        /**
         * The arguments, in order, or empty for a macro invoked without parentheses.
         */
        public List<String> getArguments() {
            int open = text.indexOf('(');
            int close = text.lastIndexOf(')');
            return open < 0 || close < open ? emptyList() :
                    Statements.argumentsOf(text.substring(open + 1, close));
        }

        /**
         * Whether one of {@code members} defines this macro. Member names are compared without regard
         * to case, the way a mainframe library holds them.
         */
        public boolean isDefinedBy(Collection<String> members) {
            for (String member : members) {
                if (member.equalsIgnoreCase(name)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String toString() {
            return "%" + name;
        }
    }

    public static class Matcher extends SimpleTraitMatcher<MacroCall> {

        @Override
        protected @Nullable MacroCall test(Cursor cursor) {
            return Statements.isProgram(cursor) ? new MacroCall(cursor) : null;
        }
    }

    @Override
    public String toString() {
        return "macros invoked by " + getTree().getSourcePath();
    }
}
