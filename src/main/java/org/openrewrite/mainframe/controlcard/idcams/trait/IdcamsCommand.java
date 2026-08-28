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
package org.openrewrite.mainframe.controlcard.idcams.trait;

import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.mainframe.controlcard.CardLines;
import org.openrewrite.mainframe.controlcard.idcams.IdcamsIsoVisitor;
import org.openrewrite.mainframe.controlcard.idcams.tree.Idcams;
import org.openrewrite.mainframe.controlcard.idcams.tree.Space;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyList;

/**
 * An access method services command, read for what it says about the data sets it makes rather than
 * for how it is written.
 * <p>
 * An IDCAMS {@code DEFINE} is the only place a VSAM file's existence is written down: the JCL that
 * reads the file names it on a DD and says nothing about its key, its record size or which of the
 * jobs in an estate created it, and the COBOL names only its DD. Everything else — the key length
 * that has to match the copybook, the components a listing shows, the GDG limit that decides how many
 * generations survive — is here and nowhere else.
 */
@Value
public class IdcamsCommand implements Trait<Idcams.Command> {

    /**
     * Each verb under every spelling the corpus writes it in. IDCAMS accepts an abbreviation of every
     * command it has and shops write both, so {@code DEF} and {@code DEFINE} have to read alike.
     */
    private static final Map<String, String> VERBS = new HashMap<>();

    /**
     * The kinds of object a {@code DEFINE} makes and a {@code DELETE} names, under every spelling.
     */
    private static final Map<String, String> OBJECT_TYPES = new HashMap<>();

    /**
     * The object types that are a data set — something a job can name on a DD. An {@code ALIAS} or a
     * {@code USERCATALOG} is a catalog entry and not a file.
     */
    private static final Set<String> DATA_SETS = new HashSet<>(Arrays.asList(
            "CLUSTER", "AIX", "PATH", "GDG", "NONVSAM", "PAGESPACE"));

    /**
     * The words a command may carry that take no value, so that the entry name can be told from them.
     */
    private static final Set<String> OPTIONS = new HashSet<>(Arrays.asList(
            "PURGE", "NOPURGE", "ERASE", "NOERASE", "SCRATCH", "NOSCRATCH", "FORCE", "RECOVERY",
            "REPLACE", "NOREPLACE", "REUSE", "NOREUSE", "ALL", "NAME", "HISTORY", "VOLUME",
            "ALLOCATION", "CHARACTERISTICS", "DUMP", "HEX", "COUNT", "SKIP"));

    static {
        verb("DEFINE", "DEF");
        verb("DELETE", "DEL");
        verb("ALTER", "ALT");
        verb("LISTCAT", "LISTC");
        verb("REPRO");
        verb("PRINT");
        verb("EXPORT");
        verb("IMPORT");
        verb("BLDINDEX", "BIX");
        verb("EXAMINE");
        verb("VERIFY");
        verb("SET");
        verb("IF");

        objectType("CLUSTER", "CL");
        objectType("AIX", "ALTERNATEINDEX", "IX");
        objectType("PATH");
        objectType("GDG", "GENERATIONDATAGROUP");
        objectType("ALIAS");
        objectType("NONVSAM", "NVSAM");
        objectType("PAGESPACE", "PGSPC");
        objectType("USERCATALOG", "UCAT");
        objectType("MASTERCATALOG", "MCAT");
        objectType("SPACE", "SPC");
    }

    private static void verb(String canonical, String... spellings) {
        VERBS.put(canonical, canonical);
        for (String spelling : spellings) {
            VERBS.put(spelling, canonical);
        }
    }

    private static void objectType(String canonical, String... spellings) {
        OBJECT_TYPES.put(canonical, canonical);
        for (String spelling : spellings) {
            OBJECT_TYPES.put(spelling, canonical);
        }
    }

    Cursor cursor;

    /**
     * The command, under the spelling the manual gives it: {@code DEFINE}, {@code DELETE},
     * {@code REPRO}, {@code LISTCAT}, {@code PRINT}, {@code ALTER}. A verb IDCAMS does not know reads
     * as it was written.
     */
    public String getVerb() {
        String verb = getTree().getVerb().getText().toUpperCase(Locale.ROOT);
        return VERBS.getOrDefault(verb, verb);
    }

    public boolean isVerb(String verb) {
        return getVerb().equals(VERBS.getOrDefault(verb.toUpperCase(Locale.ROOT), verb.toUpperCase(Locale.ROOT)));
    }

    /**
     * What kind of object the command works on: {@code CLUSTER}, {@code AIX}, {@code PATH},
     * {@code GDG}, {@code ALIAS}, or null when the command does not say — a {@code DELETE} needs no
     * object type, since the catalog already knows what the entry is.
     */
    public @Nullable String getObjectType() {
        for (Idcams.Parameter parameter : getParameters()) {
            String type = objectTypeOf(parameter.getKeyword().getText());
            if (type != null) {
                return type;
            }
        }
        return null;
    }

    /**
     * The kind of object a word names, under the spelling the manual gives it, or null when the word
     * names no kind IDCAMS has. Shared with the parser, which types a member by whether its first
     * command is one IDCAMS could run: a CICS {@code DEFINE RESGROUP} is not.
     */
    public static @Nullable String objectTypeOf(String word) {
        return OBJECT_TYPES.get(word.toUpperCase(Locale.ROOT));
    }

    /**
     * The entry the command works on: what a {@code DEFINE} calls the object it makes, and the name a
     * {@code DELETE}, {@code ALTER} or {@code LISTCAT} was given. Null for the commands that name a DD
     * rather than an entry, such as {@code REPRO INFILE(x) OUTFILE(y)}.
     */
    public @Nullable String getObjectName() {
        if (isVerb("DEFINE")) {
            Idcams.Parameter group = objectGroup();
            return group == null ? null : nested(group.getValueText(), "NAME");
        }
        if (isVerb("LISTCAT")) {
            List<String> entries = names("ENTRIES", "ENT");
            return entries.isEmpty() ? null : entries.get(0);
        }
        if (isVerb("DELETE") || isVerb("ALTER") || isVerb("EXAMINE") || isVerb("VERIFY")) {
            for (Idcams.Parameter parameter : getParameters()) {
                String text = parameter.getKeyword().getText();
                String keyword = text.toUpperCase(Locale.ROOT);
                if (parameter.getValue().isEmpty() && !OBJECT_TYPES.containsKey(keyword) && !OPTIONS.contains(keyword)) {
                    return unquote(text);
                }
            }
        }
        return null;
    }

    /**
     * Whether the command creates a data set — something a job can name on a DD. A {@code DEFINE
     * ALIAS} or {@code DEFINE USERCATALOG} creates a catalog entry, which is not one.
     */
    public boolean definesDataSet() {
        String type = getObjectType();
        return isVerb("DEFINE") && type != null && DATA_SETS.contains(type) && getObjectName() != null;
    }

    /**
     * Every data set the command creates: the object itself, and the components a cluster or an
     * alternate index is made of, which are catalog entries under names of their own.
     */
    public List<String> getDefinedNames() {
        String object = getObjectName();
        if (object == null || !definesDataSet()) {
            return emptyList();
        }
        List<String> names = new ArrayList<>();
        names.add(object);
        for (Idcams.Parameter parameter : getParameters()) {
            String keyword = parameter.getKeyword().getText().toUpperCase(Locale.ROOT);
            if ("DATA".equals(keyword) || "INDEX".equals(keyword)) {
                String name = nested(parameter.getValueText(), "NAME");
                if (name != null) {
                    names.add(name);
                }
            }
        }
        return names;
    }

    public List<Idcams.Parameter> getParameters() {
        return getTree().getNamedParameters();
    }

    /**
     * The value written under any of these keywords, without the parentheses that hold it and the
     * quotes around a data set name.
     */
    public @Nullable String getParameter(String... keywords) {
        for (String keyword : keywords) {
            Idcams.Parameter parameter = getTree().getParameter(keyword);
            if (parameter != null) {
                String text = unwrap(parameter.getValueText());
                return text.isEmpty() ? null : text;
            }
        }
        return null;
    }

    /**
     * The names listed under a keyword, which may be separated by commas, blanks, or both:
     * {@code ENTRIES(A B C)}.
     */
    public List<String> names(String... keywords) {
        String value = getParameter(keywords);
        if (value == null) {
            return emptyList();
        }
        List<String> names = new ArrayList<>();
        for (String name : value.split("[,\\s]+")) {
            if (!name.isEmpty()) {
                names.add(unquote(name));
            }
        }
        return names;
    }

    /**
     * The one-based line of the deck the command was written on.
     */
    public int getLine() {
        return CardLines.of(cursor, Idcams.CompilationUnit.class, words())
                .getOrDefault(getTree().getVerb().getId(), 1);
    }

    /**
     * Where the words of a deck are, for {@link CardLines} to count the cards between them.
     */
    private static IdcamsIsoVisitor<CardLines> words() {
        return new IdcamsIsoVisitor<CardLines>() {
            @Override
            public Space visitSpace(Space space, Space.Location location, CardLines lines) {
                lines.space(space.getWhitespace());
                return space;
            }

            @Override
            public Idcams.Word visitWord(Idcams.Word word, CardLines lines) {
                Idcams.Word w = super.visitWord(word, lines);
                lines.word(w.getId());
                return w;
            }
        };
    }

    private Idcams.@Nullable Parameter objectGroup() {
        for (Idcams.Parameter parameter : getParameters()) {
            if (OBJECT_TYPES.containsKey(parameter.getKeyword().getText().toUpperCase(Locale.ROOT))) {
                return parameter;
            }
        }
        return null;
    }

    /**
     * The value of a parameter written inside a group: the {@code NAME} of a
     * {@code CLUSTER (NAME(x) …)}. Read from the group's text rather than from its words, because the
     * lexer splits a group wherever the layout put a blank and a group holds no words of its own.
     */
    private static @Nullable String nested(String group, String keyword) {
        String upper = group.toUpperCase(Locale.ROOT);
        int i = upper.indexOf(keyword + "(");
        while (i >= 0) {
            if (i == 0 || !isNamePart(group.charAt(i - 1))) {
                int open = i + keyword.length();
                int close = matching(group, open);
                if (close > open) {
                    return unquote(group.substring(open + 1, close).trim());
                }
            }
            i = upper.indexOf(keyword + "(", i + keyword.length());
        }
        return null;
    }

    /**
     * Where the parenthesis at {@code open} closes, or -1. Quoted text is skipped, since a data set
     * name may hold either character.
     */
    private static int matching(String text, int open) {
        int depth = 0;
        boolean quoted = false;
        for (int i = open; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\'') {
                quoted = !quoted;
            } else if (quoted) {
                continue;
            } else if (c == '(') {
                depth++;
            } else if (c == ')' && --depth == 0) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isNamePart(char c) {
        return Character.isLetterOrDigit(c) || c == '.' || c == '-' || c == '_' ||
               c == '$' || c == '#' || c == '@';
    }

    private static String unwrap(String value) {
        String text = value.trim();
        if (text.startsWith("(")) {
            text = text.substring(1);
        }
        if (text.endsWith(")")) {
            text = text.substring(0, text.length() - 1);
        }
        return unquote(text.trim());
    }

    private static String unquote(String value) {
        return value.length() > 1 && value.startsWith("'") && value.endsWith("'") ?
                value.substring(1, value.length() - 1) : value;
    }

    public static class Matcher extends SimpleTraitMatcher<IdcamsCommand> {

        @Override
        protected @Nullable IdcamsCommand test(Cursor cursor) {
            return cursor.getValue() instanceof Idcams.Command ? new IdcamsCommand(cursor) : null;
        }
    }

    @Override
    public String toString() {
        String type = getObjectType();
        String name = getObjectName();
        return (getVerb() + (type == null ? "" : " " + type) + (name == null ? "" : " " + name)).trim();
    }
}
