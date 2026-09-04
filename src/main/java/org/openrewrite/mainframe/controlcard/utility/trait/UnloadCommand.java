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
package org.openrewrite.mainframe.controlcard.utility.trait;

import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.mainframe.controlcard.utility.Keywords;
import org.openrewrite.mainframe.controlcard.utility.marker.Dialect;
import org.openrewrite.mainframe.controlcard.utility.tree.Statement;
import org.openrewrite.mainframe.controlcard.utility.tree.Utility;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * One unload, in either dialect, read for what it unloads and where it puts it.
 * <p>
 * The two dialects say the same things in different places. The unload product writes a
 * {@code SELECT} per output file inside the {@code UNLOAD} block, each with its own {@code OUTDDN}
 * and {@code FORMAT}; the base utility writes one {@code FROM TABLE} with a field list and sends the
 * rows to {@code UNLDDN}, defaulting to {@code SYSREC}. This trait answers both, and
 * {@link #getDialect()} says which was written, since the program name on the {@code EXEC} card
 * cannot.
 * <p>
 * What it cannot answer is as important. An omitted keyword is not a default a reader may assume:
 * the unload product resolves {@code FORMAT}, {@code DB2}, {@code LOCK}, {@code QUIESCE} and the
 * date and null options from the site parmlib on the {@code INFPLIB} DD, which is not in any
 * application library. {@link #isCoded(String)} and {@link #getInheritedKeywords()} are what tells a
 * job whose behaviour is written down from one whose behaviour is a site setting.
 */
@Value
public class UnloadCommand implements Trait<Utility.Block> {

    Cursor cursor;

    public Dialect.Kind getDialect() {
        return Operands.dialectOf(cursor);
    }

    /**
     * The table space unloaded, or null where the deck leaves it to be worked out from the tables the
     * {@code SELECT}s name — which the product allows, and which is what an image copy unload does.
     */
    public @Nullable String getTableSpace() {
        return Operands.textOf(getTree(), "TABLESPACE");
    }

    /**
     * The tables the rows come from: one per {@code SELECT} in the product's dialect, one per
     * {@code FROM TABLE} in the base utility's.
     */
    public List<String> getTables() {
        List<String> tables = new ArrayList<>();
        for (Utility.Block select : getTree().getBlocks("SELECT")) {
            String from = Operands.textOf(select, "FROM");
            if (from != null) {
                tables.add(from);
            }
        }
        for (Utility.Block from : getTree().getBlocks("FROM")) {
            // The base utility writes FROM TABLE, so the object of the block is what follows TABLE.
            String[] words = from.getValueText().split("\\s+", 3);
            if (words.length > 1 && "TABLE".equalsIgnoreCase(words[0])) {
                tables.add(words[1]);
            }
        }
        return tables;
    }

    /**
     * The {@code SELECT} blocks, in the order they were written. Several in one unload is one pass
     * over the table space fanned out to several files, which the base utility has no answer to.
     */
    public List<Utility.Block> getSelects() {
        return getTree().getBlocks("SELECT");
    }

    /**
     * Every DD the rows are written to: the {@code OUTDDN} of each {@code SELECT}, and the
     * {@code UNLDDN} of a physical or base utility unload.
     */
    public List<String> getOutputDdNames() {
        List<String> names = new ArrayList<>(Operands.namesOf(getTree(), "UNLDDN"));
        for (Utility.Block select : getSelects()) {
            names.addAll(Operands.namesOf(select, "OUTDDN"));
        }
        return names;
    }

    /**
     * Every DD the generated {@code LOAD} cards are written to: {@code LOADDDN} in the product's
     * dialect, {@code PUNCHDDN} in the base utility's. The name may be a {@code TEMPLATE} of the deck
     * rather than a DD of the job.
     */
    public List<String> getLoadDdNames() {
        List<String> names = new ArrayList<>(Operands.namesOf(getTree(), "PUNCHDDN"));
        for (Utility.Block select : getSelects()) {
            names.addAll(Operands.namesOf(select, "LOADDDN"));
            for (Utility.Block format : select.getBlocks("FORMAT")) {
                names.addAll(Operands.namesOf(format, "LOADDDN"));
            }
        }
        return names;
    }

    /**
     * The DD the image copy is read from, where the unload reads a copy rather than the table space.
     */
    public @Nullable String getCopyDdName() {
        String copy = Operands.textOf(getTree(), "COPYDDN");
        if (copy == null) {
            copy = Operands.textOf(getTree(), "FROMCOPYDDN");
        }
        return copy;
    }

    /**
     * The layout each {@code SELECT} writes, one per format block written. A {@code SELECT} that
     * codes none is not in the list, because what it writes is a site setting rather than something
     * the deck says.
     */
    public List<String> getFormats() {
        List<String> formats = new ArrayList<>();
        for (Utility.Block select : getSelects()) {
            for (Utility.Block format : select.getBlocks("FORMAT")) {
                formats.add(format.getValueText());
            }
        }
        String coded = Operands.textOf(getTree(), "FORMAT");
        if (coded != null) {
            formats.add(coded);
        }
        return formats;
    }

    /**
     * How the rows are read: {@code NO} for the product's native mode, straight off the data sets
     * outside Db2, {@code FORCE} for a dynamic cursor through Db2, {@code YES} for native where the
     * SQL allows it. The bytes written are not the same across the three.
     */
    public @Nullable String getDb2() {
        return getOperand("DB2");
    }

    public @Nullable String getLock() {
        return getOperand("LOCK");
    }

    public @Nullable String getQuiesce() {
        return getOperand("QUIESCE");
    }

    /**
     * The base utility's one dial for what the product writes as {@code LOCK} and {@code QUIESCE}
     * together.
     */
    public @Nullable String getShrLevel() {
        return getOperand("SHRLEVEL");
    }

    /**
     * The value written under a keyword of the unload itself, or of the deck's {@code GLOBAL} block,
     * where the same keywords may be written for every unload after them.
     */
    public @Nullable String getOperand(String keyword) {
        String value = Operands.textOf(getTree(), keyword);
        if (value != null) {
            return value;
        }
        for (Utility.Block global : globals()) {
            value = Operands.textOf(global, keyword);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    /**
     * Whether the deck writes this keyword anywhere that governs this unload: on the unload itself,
     * in one of its {@code SELECT}s or their format and options blocks, or in a {@code GLOBAL} block
     * of the deck. A keyword that is not written is not a default — it is whatever the site parmlib
     * says today.
     */
    public boolean isCoded(String keyword) {
        for (Utility.Block global : globals()) {
            if (codes(global, keyword)) {
                return true;
            }
        }
        return codes(getTree(), keyword);
    }

    /**
     * The keywords this unload leaves to the site parmlib, in the order the product's documentation
     * lists them. An empty list for a base utility unload, whose defaults are published and the same
     * everywhere.
     * <p>
     * {@code LOCK} and {@code QUIESCE} are not asked of an unload of an image copy: a copy is
     * neither locked nor quiesced and the product rejects both keywords there, so leaving them out
     * is the deck saying so rather than the deck saying nothing.
     */
    public List<String> getInheritedKeywords() {
        List<String> inherited = new ArrayList<>();
        if (getDialect() != Dialect.Kind.HIGH_PERFORMANCE_UNLOAD) {
            return inherited;
        }
        boolean copy = getCopyDdName() != null;
        for (String keyword : Keywords.inherited()) {
            if (copy && ("LOCK".equals(keyword) || "QUIESCE".equals(keyword))) {
                continue;
            }
            if (!isCoded(keyword)) {
                inherited.add(keyword);
            }
        }
        return inherited;
    }

    /**
     * The one-based line of the deck the unload was written on.
     */
    public int getLine() {
        return Operands.lineOf(cursor, getTree());
    }

    /**
     * Whether a block or anything written inside it codes a keyword. {@code FORMAT} and
     * {@code OPTIONS} are blocks rather than operands, so a deck codes them by opening one.
     */
    private static boolean codes(Utility.Block block, String keyword) {
        if (block.getOperand(keyword) != null || !block.getBlocks(keyword).isEmpty()) {
            return true;
        }
        for (Utility.Block nested : block.getBlocks()) {
            if (codes(nested, keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * The {@code GLOBAL} blocks of the deck. They are written once and govern every unload after
     * them, so a keyword written there is written for this unload too.
     */
    private List<Utility.Block> globals() {
        List<Utility.Block> globals = new ArrayList<>();
        for (Statement statement : cursor.firstEnclosingOrThrow(Utility.CompilationUnit.class).getStatements()) {
            if (statement == getTree()) {
                break;
            }
            if (statement instanceof Utility.Block && ((Utility.Block) statement).isVerb("GLOBAL")) {
                globals.add((Utility.Block) statement);
            }
        }
        return globals;
    }

    public static class Matcher extends SimpleTraitMatcher<UnloadCommand> {

        @Override
        protected @Nullable UnloadCommand test(Cursor cursor) {
            Object value = cursor.getValue();
            if (!(value instanceof Utility.Block)) {
                return null;
            }
            return ((Utility.Block) value).isVerb("UNLOAD") &&
                   cursor.getParentTreeCursor().getValue() instanceof Utility.CompilationUnit ?
                    new UnloadCommand(cursor) : null;
        }
    }

    @Override
    public String toString() {
        String tableSpace = getTableSpace();
        return ("UNLOAD " + (tableSpace == null ? String.join(",", getTables()) : tableSpace))
                .toUpperCase(Locale.ROOT);
    }
}
