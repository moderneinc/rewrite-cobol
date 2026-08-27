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
import java.util.List;
import java.util.Locale;

/**
 * The {@code LIBNAME} statements of a program, each giving a SAS library a libref — the name every
 * data set in it is qualified by, as {@code CLMSAS.CLMDAY} is.
 * <p>
 * {@code LIBNAME CLMSAS;} writes no data set name at all, and that is the shape that matters here:
 * the libref is then a DD, and what it points at is whatever the step running the program allocated.
 * A shop runs the same program against test and production by changing the JCL, so the library the
 * report read is a fact about the job and not about the member.
 */
@Value
public class Library implements Trait<PlainText> {

    Cursor cursor;

    /**
     * The libraries the program names, in the order it names them.
     */
    public List<Reference> getReferences() {
        List<Reference> references = new ArrayList<>();
        for (Statements.Statement statement : Statements.in(getTree().getText())) {
            if (statement.isKeyword("LIBNAME") && statement.getWordText(1) != null) {
                references.add(new Reference(statement.getWordText(1),
                        statement.getWordText(2), Statements.literalIn(statement, 2),
                        statement.getLine()));
            }
        }
        return references;
    }

    @Value
    public static class Reference {

        @Nullable
        String libref;

        /**
         * The word between the libref and the data set, which is the engine where it is one.
         */
        @Nullable
        String engineWord;

        /**
         * The data set the statement names, or null where it names none.
         */
        @Nullable
        String path;

        int line;

        /**
         * The libref.
         */
        public String getName() {
            return libref == null ? "" : libref.toUpperCase(Locale.ROOT);
        }

        /**
         * The DD the step has to allocate for this libref to resolve, or null for a library the
         * program names itself.
         */
        public @Nullable String getDdName() {
            return path == null && !getName().isEmpty() ? getName() : null;
        }

        /**
         * The engine written between the libref and the data set, or null where the statement leaves
         * it to SAS.
         */
        public @Nullable String getEngine() {
            return engineWord == null || engineWord.startsWith("'") || engineWord.startsWith("\"") ||
                   engineWord.indexOf('=') >= 0 || engineWord.startsWith("(") ?
                    null : engineWord.toUpperCase(Locale.ROOT);
        }

        @Override
        public String toString() {
            return "LIBNAME " + getName();
        }
    }

    public static class Matcher extends SimpleTraitMatcher<Library> {

        @Override
        protected @Nullable Library test(Cursor cursor) {
            return Statements.isProgram(cursor) ? new Library(cursor) : null;
        }
    }

    @Override
    public String toString() {
        return "LIBNAME of " + getTree().getSourcePath();
    }
}
