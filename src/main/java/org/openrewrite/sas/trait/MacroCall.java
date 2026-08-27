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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static java.util.Collections.emptyList;

/**
 * An invocation of a macro: {@code %CLMTITL(RESERVE CHANGE BY CLAIM TYPE);}.
 * <p>
 * A name after a {@code %} is a macro the shop wrote or one of the statements the macro language
 * itself has, and nothing in the source tells them apart — so the statements are listed here and
 * everything else is an invocation. Whether the macro is one the estate defines is a second
 * question, answered by {@link MacroDefinition} over the members that could define it: a macro comes
 * as often out of an autocall library nobody keeps as out of a member beside the program.
 */
@Value
public class MacroCall implements Trait<Sas.Statement> {

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
     * The macro's name, without the {@code %}.
     */
    public String getName() {
        String word = getTree().getKeyword();
        int paren = word.indexOf('(');
        return (paren < 0 ? word : word.substring(0, paren)).substring(1);
    }

    /**
     * The arguments, in order, or empty for a macro invoked without parentheses.
     */
    public List<String> getArguments() {
        String text = getTree().getText();
        int open = text.indexOf('(');
        int close = text.lastIndexOf(')');
        return open < 0 || close < open ? emptyList() :
                Statements.argumentsOf(text.substring(open + 1, close));
    }

    /**
     * Whether one of {@code members} defines this macro. Member names are compared without regard to
     * case, the way a mainframe library holds them.
     */
    public boolean isDefinedBy(Collection<String> members) {
        for (String member : members) {
            if (member.equalsIgnoreCase(getName())) {
                return true;
            }
        }
        return false;
    }

    public int getLine() {
        return Statements.lineOf(cursor);
    }

    public static class Matcher extends SimpleTraitMatcher<MacroCall> {

        @Override
        protected @Nullable MacroCall test(Cursor cursor) {
            Object value = cursor.getValue();
            if (!(value instanceof Sas.Statement)) {
                return null;
            }
            String keyword = ((Sas.Statement) value).getKeyword();
            if (!keyword.startsWith("%") || keyword.length() < 2) {
                return null;
            }
            int paren = keyword.indexOf('(');
            String name = paren < 0 ? keyword : keyword.substring(0, paren);
            return STATEMENTS.contains(name.toUpperCase(Locale.ROOT)) ? null : new MacroCall(cursor);
        }
    }

    @Override
    public String toString() {
        return "%" + getName();
    }
}
