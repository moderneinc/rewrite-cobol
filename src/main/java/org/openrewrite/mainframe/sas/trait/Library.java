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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * The librefs a program names — the name every data set in a SAS library is qualified by, as
 * {@code CLMSAS.CLMDAY} is. A {@code LIBNAME} declares one; a {@code PROC} uses one, on
 * {@code LIBRARY=} or as the first qualifier of the data set it was given.
 * <p>
 * {@code LIBNAME CLMSAS;} writes no data set name at all, and that is the shape that matters here:
 * the libref is then a DD, and what it points at is whatever the step running the program allocated.
 * A shop runs the same program against test and production by changing the JCL, so the library the
 * report read is a fact about the job and not about the member.
 * <p>
 * A libref a {@code PROC} uses is reported only where no {@code LIBNAME} of the same program gave it
 * a data set name, since then it is a DD the step has to allocate too — {@code PROC FORMAT
 * LIBRARY=LIBRARY} names a DD no statement of the member declares, and it is how a stored format
 * outlives the step that built it.
 */
@Value
public class Library implements Trait<PlainText> {

    /**
     * The keyword that named the libref.
     */
    public enum Kind {
        /**
         * The declaration, which gives the libref a data set or leaves it to the step.
         */
        LIBNAME,
        /**
         * {@code LIBRARY=} on a {@code PROC}, which names a libref and no data set in it.
         */
        LIBRARY,
        /**
         * {@code DATA=} on a {@code PROC}: the data set it reads, qualified by its libref.
         */
        DATA,
        /**
         * {@code OUT=} on a {@code PROC}: the data set it writes.
         */
        OUT,
        /**
         * {@code BASE=} on {@code PROC APPEND}: the data set it appends to.
         */
        BASE
    }

    Cursor cursor;

    /**
     * The libraries the program names, in the order it names them.
     */
    public List<Reference> getReferences() {
        List<Statements.Statement> statements = Statements.in(getTree().getText());
        Set<String> declared = new HashSet<>();
        for (Statements.Statement statement : statements) {
            String libref = statement.getWordText(1);
            if (statement.isKeyword("LIBNAME") && libref != null &&
                Statements.literalIn(statement, 2) != null) {
                declared.add(libref.toUpperCase(Locale.ROOT));
            }
        }

        List<Reference> references = new ArrayList<>();
        for (Statements.Statement statement : statements) {
            if (statement.isKeyword("LIBNAME") && statement.getWordText(1) != null) {
                references.add(new Reference(Kind.LIBNAME, statement.getWordText(1),
                        statement.getWordText(2), null, Statements.literalIn(statement, 2),
                        statement.getLine()));
            } else if (statement.isKeyword("PROC")) {
                read(statement, declared, references);
            }
        }
        return references;
    }

    /**
     * The librefs one {@code PROC} statement uses.
     */
    private static void read(Statements.Statement statement, Set<String> declared,
                             List<Reference> references) {
        for (String option : optionsOf(statement)) {
            int equals = option.indexOf('=');
            if (equals < 0) {
                continue;
            }
            Kind kind = kindOf(option.substring(0, equals));
            if (kind == null) {
                continue;
            }
            // The parenthesised part is what to do with the data set, not part of its name.
            String value = option.substring(equals + 1);
            int open = value.indexOf('(');
            value = open < 0 ? value : value.substring(0, open);
            if (value.isEmpty() || value.charAt(0) == '\'' || value.charAt(0) == '"') {
                continue;
            }

            int dot = value.indexOf('.');
            String libref = kind == Kind.LIBRARY ? value : dot < 0 ? null : value.substring(0, dot);
            String member = kind == Kind.LIBRARY || dot < 0 ? null : value.substring(dot + 1);
            if (libref != null && !declared.contains(libref.toUpperCase(Locale.ROOT))) {
                references.add(new Reference(kind, libref, null, member, null, statement.getLine()));
            }
        }
    }

    private static @Nullable Kind kindOf(String keyword) {
        for (Kind kind : Kind.values()) {
            if (kind != Kind.LIBNAME && kind.name().equalsIgnoreCase(keyword)) {
                return kind;
            }
        }
        return null;
    }

    /**
     * The {@code KEYWORD=value} options a statement writes, glued back together where the blanks SAS
     * allows around the equals sign left them in words of their own.
     */
    private static List<String> optionsOf(Statements.Statement statement) {
        List<String> words = statement.getWordTexts();
        List<String> options = new ArrayList<>(words.size());
        for (int i = 0; i < words.size(); i++) {
            StringBuilder option = new StringBuilder(words.get(i));
            if (option.indexOf("=") < 0 && i + 1 < words.size() && words.get(i + 1).startsWith("=")) {
                option.append(words.get(++i));
            }
            if (option.charAt(option.length() - 1) == '=' && i + 1 < words.size()) {
                option.append(words.get(++i));
            }
            options.add(option.toString());
        }
        return options;
    }

    @Value
    public static class Reference {

        Kind kind;

        @Nullable
        String libref;

        /**
         * The word between the libref and the data set, which is the engine where it is one.
         */
        @Nullable
        String engineWord;

        /**
         * The data set inside the library, for a {@code PROC} that named one — {@code CLMDAY} of
         * {@code DATA=CLMSAS.CLMDAY}. Nothing outside the library has heard of it: it is not a z/OS
         * data set of its own.
         */
        @Nullable
        String member;

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
            return kind + " " + getName() + (member == null ? "" : "." + member);
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
        return "librefs of " + getTree().getSourcePath();
    }
}
