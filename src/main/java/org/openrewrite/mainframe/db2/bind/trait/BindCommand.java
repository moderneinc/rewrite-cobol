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
package org.openrewrite.mainframe.db2.bind.trait;

import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.mainframe.controlcard.CardLines;
import org.openrewrite.mainframe.db2.bind.BindIsoVisitor;
import org.openrewrite.mainframe.db2.bind.tree.Bind;
import org.openrewrite.mainframe.db2.bind.tree.Space;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.util.Collections.emptyList;

/**
 * A {@code BIND} or {@code REBIND} subcommand, read for what it says about the DB2 objects it makes
 * rather than for how it is written.
 * <p>
 * A bind card is the only place the chain from a plan to the program that runs under it is written
 * down: the plan lists collections in its {@code PKLIST}, a package is bound from a DBRM named in
 * {@code MEMBER}, and the DBRM carries the name of the program the DB2 precompile read. Nothing in
 * the COBOL or in the JCL says any of it.
 */
@Value
public class BindCommand implements Trait<Bind.Command> {

    Cursor cursor;

    /**
     * Whether the command makes the object or rebinds one that exists. A {@code REBIND} names what it
     * rebinds and nothing else, so it is a reference to a plan or package rather than a definition of
     * one.
     */
    public Kind getKind() {
        return getTree().isVerb("REBIND") ? Kind.REBIND : Kind.BIND;
    }

    public boolean bindsPlan() {
        return hasOperand("PLAN");
    }

    public boolean bindsPackage() {
        return hasOperand("PACKAGE");
    }

    /**
     * The plans named: one for a {@code BIND}, as many as are listed for a {@code REBIND}.
     */
    public List<String> getPlans() {
        return Operands.listOf(getTree(), "PLAN");
    }

    /**
     * The packages named.
     * <p>
     * A {@code BIND PACKAGE} names the collection and leaves DB2 to name the package after the DBRM
     * it is bound from, so the packages are the {@link #getMembers()}; a {@code REBIND PACKAGE} names
     * {@code collection.package} outright, and the package is the last part of it.
     */
    public List<String> getPackages() {
        if (!bindsPackage()) {
            return emptyList();
        }
        if (getKind() == Kind.BIND) {
            return getMembers();
        }
        List<String> packages = new ArrayList<>();
        for (String name : Operands.listOf(getTree(), "PACKAGE")) {
            packages.add(lastPart(name));
        }
        return packages;
    }

    /**
     * The collection the packages are bound into, or null for a plan bind. Written either on its own
     * or qualified by the location the packages live at.
     */
    public @Nullable String getCollection() {
        List<String> names = Operands.listOf(getTree(), "PACKAGE");
        if (names.isEmpty()) {
            return null;
        }
        String[] parts = names.get(0).split("\\.");
        // BIND PACKAGE names [location.]collection; REBIND PACKAGE names [location.]collection.package.
        int collection = getKind() == Kind.BIND ? parts.length - 1 : parts.length - 2;
        return collection < 0 ? null : parts[collection];
    }

    /**
     * The DBRM members bound, from {@code MEMBER}. A DBRM is written under the name of the program it
     * was precompiled from, so these are program names as well.
     */
    public List<String> getMembers() {
        return Operands.listOf(getTree(), "MEMBER");
    }

    /**
     * The packages a plan may run, from {@code PKLIST}. An entry names a collection and either one
     * package or {@code *} for all of them.
     */
    public List<String> getPackageList() {
        return Operands.listOf(getTree(), "PKLIST");
    }

    /**
     * The library the DBRMs are read from, from {@code LIBRARY}. Null when the job supplies it on a
     * {@code DBRMLIB} DD instead, which is the usual arrangement.
     */
    public @Nullable String getLibrary() {
        return Operands.textOf(getTree(), "LIBRARY");
    }

    /**
     * The authorization identifier the objects are owned by, from {@code OWNER}.
     */
    public @Nullable String getOwner() {
        return Operands.textOf(getTree(), "OWNER");
    }

    /**
     * What unqualified table names in the packages resolve to, from {@code QUALIFIER}. This is the
     * schema the SQL in the program actually reads, which the program itself never names.
     */
    public @Nullable String getQualifier() {
        return Operands.textOf(getTree(), "QUALIFIER");
    }

    public @Nullable String getIsolation() {
        return operand("ISOLATION", "ISO");
    }

    public @Nullable String getRelease() {
        return operand("RELEASE", "REL");
    }

    public @Nullable String getValidate() {
        return operand("VALIDATE", "VAL");
    }

    public @Nullable String getAction() {
        return operand("ACTION", "ACT");
    }

    /**
     * The value of an operand written under any of these keywords. DSN accepts an abbreviation of
     * every keyword it has, and the corpus writes both — {@code ISOLATION(CS)} and {@code ISO(CS)}
     * are the same operand.
     */
    public @Nullable String operand(String... keywords) {
        for (String keyword : keywords) {
            String text = Operands.textOf(getTree(), keyword);
            if (text != null) {
                return text;
            }
        }
        return null;
    }

    /**
     * Whether the command writes an operand under any of these keywords. {@code RETAIN} and
     * {@code NOREOPT} take no value, so asking for their text answers nothing.
     */
    public boolean hasOperand(String... keywords) {
        for (String keyword : keywords) {
            if (getTree().getParameter(keyword) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * The one-based line of the deck the command was written on.
     */
    public int getLine() {
        return CardLines.of(cursor, Bind.CompilationUnit.class, words())
                .getOrDefault(getTree().getVerb().getId(), 1);
    }

    /**
     * Where the words of a deck are, for {@link CardLines} to count the cards between them.
     */
    private static BindIsoVisitor<CardLines> words() {
        return new BindIsoVisitor<CardLines>() {
            @Override
            public Space visitSpace(Space space, Space.Location location, CardLines lines) {
                lines.space(space.getWhitespace());
                return space;
            }

            @Override
            public Bind.Word visitWord(Bind.Word word, CardLines lines) {
                Bind.Word w = super.visitWord(word, lines);
                lines.word(w.getId());
                return w;
            }
        };
    }

    private static String lastPart(String name) {
        int dot = name.lastIndexOf('.');
        return dot < 0 ? name : name.substring(dot + 1);
    }

    public enum Kind {
        BIND,
        REBIND
    }

    public static class Matcher extends SimpleTraitMatcher<BindCommand> {

        @Override
        protected @Nullable BindCommand test(Cursor cursor) {
            Object value = cursor.getValue();
            if (!(value instanceof Bind.Command)) {
                return null;
            }
            Bind.Command command = (Bind.Command) value;
            return command.isVerb("BIND") || command.isVerb("REBIND") ? new BindCommand(cursor) : null;
        }
    }

    @Override
    public String toString() {
        String object = bindsPlan() ? "PLAN " + String.join(",", getPlans()) :
                bindsPackage() ? "PACKAGE " + String.join(",", getPackages()) : "";
        return (getKind() + " " + object).trim().toUpperCase(Locale.ROOT);
    }
}
