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
import org.openrewrite.mainframe.controlcard.utility.marker.Dialect;
import org.openrewrite.mainframe.controlcard.utility.tree.Utility;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * One Db2 utility control statement, read for what it does rather than for how it is written.
 * <p>
 * The statement is where a batch step's real work is written down. The JCL says which program runs
 * and where the files go; which table space is copied, reorganised or unloaded, at what
 * {@code SHRLEVEL} and into which DD, is written only here — which is why a reader that stops at the
 * JCL learns almost nothing about a utility job.
 */
@Value
public class UtilityStatement implements Trait<Utility.Block> {

    Cursor cursor;

    /**
     * The utility the statement runs: {@code UNLOAD}, {@code LOAD}, {@code COPY}, {@code REORG},
     * {@code RUNSTATS}, or the {@code GLOBAL}, {@code TEMPLATE} and {@code LISTDEF} statements that
     * set up the ones after them.
     */
    public String getVerb() {
        return getTree().getVerb().getText().toUpperCase(Locale.ROOT);
    }

    public boolean isVerb(String verb) {
        return getTree().isVerb(verb);
    }

    /**
     * The object the statement works on: the table space of a {@code COPY}, the name of a
     * {@code TEMPLATE} or a {@code LISTDEF}, or empty where the statement names none.
     */
    public String getObject() {
        String named = Operands.textOf(getTree(), "TABLESPACE");
        if (named == null) {
            named = Operands.textOf(getTree(), "INDEXSPACE");
        }
        if (named == null) {
            named = Operands.textOf(getTree(), "LIST");
        }
        return named == null ? Operands.unwrapped(getTree().getValueText()) : named;
    }

    /**
     * The value written under a keyword of this statement, or null when it is not written.
     */
    public @Nullable String getOperand(String keyword) {
        return Operands.textOf(getTree(), keyword);
    }

    /**
     * The keywords the statement writes, in the order they were written.
     */
    public List<String> getKeywords() {
        List<String> keywords = new ArrayList<>();
        for (Utility.Operand operand : getTree().getOperands()) {
            keywords.add(operand.getKeyword().getText().toUpperCase(Locale.ROOT));
        }
        return keywords;
    }

    /**
     * The blocks written inside the statement: the {@code SELECT}s of an unload, the
     * {@code FROM TABLE}s of a base utility one, an {@code OPTIONS} block.
     */
    public List<Utility.Block> getBlocks() {
        return getTree().getBlocks();
    }

    public Dialect.Kind getDialect() {
        return Operands.dialectOf(cursor);
    }

    /**
     * The one-based line of the deck the statement was written on.
     */
    public int getLine() {
        return Operands.lineOf(cursor, getTree());
    }

    /**
     * Every statement of a deck, and not the blocks written inside them: a {@code SELECT} is part of
     * the unload that holds it, not a statement of its own.
     */
    public static class Matcher extends SimpleTraitMatcher<UtilityStatement> {

        @Override
        protected @Nullable UtilityStatement test(Cursor cursor) {
            Object value = cursor.getValue();
            if (!(value instanceof Utility.Block)) {
                return null;
            }
            return cursor.getParentTreeCursor().getValue() instanceof Utility.CompilationUnit ?
                    new UtilityStatement(cursor) : null;
        }
    }

    @Override
    public String toString() {
        return (getVerb() + " " + getObject()).trim();
    }
}
