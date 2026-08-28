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

/**
 * The {@code %INCLUDE} statements of a program, which read another member in where they stand — the
 * SAS answer to a COBOL {@code COPY}.
 * <p>
 * {@code %INCLUDE SASSRC(CLMSMAC);} names a DD and not a path: {@code SASSRC} is whatever the step
 * running the program allocated it to, so the reference closes only by reading the JCL. That is the
 * same indirection a COBOL {@code SELECT} has and a {@code COPY} does not, and it is why the DD name
 * is exposed here rather than resolved.
 */
@Value
public class Include implements Trait<PlainText> {

    Cursor cursor;

    /**
     * The members the program includes, in the order it includes them.
     */
    public List<Reference> getReferences() {
        List<Reference> references = new ArrayList<>();
        for (Statements.Statement statement : Statements.in(getTree().getText())) {
            if (statement.isKeyword("%INCLUDE") && statement.getWordText(1) != null) {
                references.add(new Reference(source(statement), statement.getLine()));
            }
        }
        return references;
    }

    /**
     * What was written after the keyword, as written. Options after a {@code /} are the run's
     * business and not the reference's.
     */
    private static @Nullable String source(Statements.Statement statement) {
        String source = statement.getWordText(1);
        return "/".equals(source) ? null : source;
    }

    @Value
    public static class Reference {

        /**
         * What the statement named, as written.
         */
        @Nullable
        String source;

        int line;

        /**
         * The DD the member is read from, or null for an include that names a member alone and leaves
         * the DD to the {@code SASAUTOS} search order.
         */
        public @Nullable String getDdName() {
            int paren = source == null ? -1 : source.indexOf('(');
            return paren > 0 && source.endsWith(")") ?
                    source.substring(0, paren).toUpperCase(Locale.ROOT) : null;
        }

        /**
         * The member included, or null for one written as a quoted path.
         */
        public @Nullable String getMember() {
            if (source == null || source.startsWith("'") || source.startsWith("\"")) {
                return null;
            }
            int paren = source.indexOf('(');
            String member = paren > 0 && source.endsWith(")") ?
                    source.substring(paren + 1, source.length() - 1) : source;
            return member.isEmpty() ? null : member.toUpperCase(Locale.ROOT);
        }

        @Override
        public String toString() {
            return "%INCLUDE " + source;
        }
    }

    public static class Matcher extends SimpleTraitMatcher<Include> {

        @Override
        protected @Nullable Include test(Cursor cursor) {
            return Statements.isProgram(cursor) ? new Include(cursor) : null;
        }
    }

    @Override
    public String toString() {
        return "%INCLUDE of " + getTree().getSourcePath();
    }
}
