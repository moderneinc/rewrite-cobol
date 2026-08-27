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
 * An external file the program reads or writes: {@code INFILE} in a DATA step, {@code FILE} in one,
 * and the {@code FILENAME} that declares a fileref for either.
 * <p>
 * The three are one trait because the name they carry is one thing — a fileref, which on z/OS is a
 * DD name. {@code INFILE CLMEXTR} is the same join a COBOL {@code SELECT ... ASSIGN TO CLMEXTR} is:
 * the program never learns the data set name, the JCL never learns what is done with it, and the DD
 * name is the only word both halves write. The join itself belongs to a recipe that has the JCL as
 * well, so the DD name is exposed here and resolved nowhere.
 */
@Value
public class FileReference implements Trait<Sas.Statement> {

    public enum Kind {
        /**
         * The file a DATA step reads.
         */
        INFILE,
        /**
         * The file a DATA step writes.
         */
        FILE,
        /**
         * A declaration, which binds a fileref to a data set without reading or writing it.
         */
        FILENAME
    }

    Cursor cursor;

    public Kind getKind() {
        return Kind.valueOf(getTree().getKeyword());
    }

    /**
     * The fileref, or empty for a statement that names a data set directly.
     */
    public String getName() {
        String name = getTree().getWordText(1);
        return name == null || name.startsWith("'") || name.startsWith("\"") ?
                "" : name.toUpperCase(Locale.ROOT);
    }

    /**
     * The data set the statement names, or null where it names none.
     */
    public @Nullable String getPath() {
        return Statements.literalIn(getTree(), 1);
    }

    /**
     * The DD the step has to allocate for this reference to resolve, or null for a file the program
     * names itself.
     */
    public @Nullable String getDdName() {
        return getPath() == null && !getName().isEmpty() ? getName() : null;
    }

    public int getLine() {
        return Statements.lineOf(cursor);
    }

    public static class Matcher extends SimpleTraitMatcher<FileReference> {

        @Override
        protected @Nullable FileReference test(Cursor cursor) {
            Object value = cursor.getValue();
            if (!(value instanceof Sas.Statement) || ((Sas.Statement) value).getWordText(1) == null) {
                return null;
            }
            for (Kind kind : Kind.values()) {
                if (((Sas.Statement) value).isKeyword(kind.name())) {
                    return new FileReference(cursor);
                }
            }
            return null;
        }
    }

    @Override
    public String toString() {
        return getKind() + " " + (getPath() == null ? getName() : "'" + getPath() + "'");
    }
}
