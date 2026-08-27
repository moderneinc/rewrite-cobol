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

import java.util.Locale;

/**
 * A {@code LIBNAME}, which gives a SAS library a libref — the name every data set in it is qualified
 * by, as {@code CLMSAS.CLMDAY} is.
 * <p>
 * {@code LIBNAME CLMSAS;} writes no data set name at all, and that is the shape that matters here:
 * the libref is then a DD, and what it points at is whatever the step running the program allocated.
 * A shop runs the same program against test and production by changing the JCL, so the library the
 * report read is a fact about the job and not about the member.
 */
@Value
public class Library implements Trait<Sas.Statement> {

    Cursor cursor;

    /**
     * The libref.
     */
    public String getName() {
        String name = getTree().getWordText(1);
        return name == null ? "" : name.toUpperCase(Locale.ROOT);
    }

    /**
     * The data set the statement names, or null where it names none.
     */
    public @Nullable String getPath() {
        return Statements.literalIn(getTree(), 2);
    }

    /**
     * The DD the step has to allocate for this libref to resolve, or null for a library the program
     * names itself.
     */
    public @Nullable String getDdName() {
        return getPath() == null && !getName().isEmpty() ? getName() : null;
    }

    /**
     * The engine written between the libref and the data set, or null where the statement leaves it
     * to SAS.
     */
    public @Nullable String getEngine() {
        String engine = getTree().getWordText(2);
        return engine == null || engine.startsWith("'") || engine.startsWith("\"") ||
               engine.indexOf('=') >= 0 || engine.startsWith("(") ?
                null : engine.toUpperCase(Locale.ROOT);
    }

    public int getLine() {
        return Statements.lineOf(cursor);
    }

    public static class Matcher extends SimpleTraitMatcher<Library> {

        @Override
        protected @Nullable Library test(Cursor cursor) {
            Object value = cursor.getValue();
            return value instanceof Sas.Statement && ((Sas.Statement) value).isKeyword("LIBNAME") &&
                   ((Sas.Statement) value).getWordText(1) != null ? new Library(cursor) : null;
        }
    }

    @Override
    public String toString() {
        return "LIBNAME " + getName();
    }
}
