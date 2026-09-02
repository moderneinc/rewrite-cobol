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
package org.openrewrite.mainframe.controlcard.utility.tree;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.With;
import org.jspecify.annotations.Nullable;
import org.openrewrite.*;
import org.openrewrite.mainframe.controlcard.utility.UtilityVisitor;
import org.openrewrite.mainframe.controlcard.utility.internal.UtilityPrinter;
import org.openrewrite.marker.Markers;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public interface Utility extends Tree {

    @SuppressWarnings("unchecked")
    @Override
    default <R extends Tree, P> R accept(TreeVisitor<R, P> v, P p) {
        return (R) acceptUtility(v.adapt(UtilityVisitor.class), p);
    }

    default <P> @Nullable Utility acceptUtility(UtilityVisitor<P> v, P p) {
        return v.defaultValue(this, p);
    }

    @Override
    default <P> boolean isAcceptable(TreeVisitor<?, P> v, P p) {
        return v.isAdaptableTo(UtilityVisitor.class);
    }

    Space getPrefix();

    <P extends Utility> P withPrefix(Space prefix);

    /**
     * A deck of Db2 utility control cards, as a control card member or as the in-stream data of a
     * {@code SYSIN} DD.
     * <p>
     * Which dialect the deck is written in is a {@link org.openrewrite.mainframe.controlcard.utility.marker.Dialect}
     * marker on the unit, because the program name on the {@code EXEC} card does not settle it: a shop
     * may alias the base utility's program to the unload product's compatibility entry point, and the
     * product reads three vendors' dialects through the one program name.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class CompilationUnit implements Utility, SourceFile {

        @EqualsAndHashCode.Include
        UUID id;

        Path sourcePath;

        @Nullable
        FileAttributes fileAttributes;

        Space prefix;
        Markers markers;

        @Nullable // for backwards compatibility
        @With(AccessLevel.PRIVATE)
        String charsetName;

        boolean charsetBomMarked;

        @Nullable
        Checksum checksum;

        @Override
        public Charset getCharset() {
            return charsetName == null ? StandardCharsets.UTF_8 : Charset.forName(charsetName);
        }

        @SuppressWarnings("unchecked")
        @Override
        public SourceFile withCharset(Charset charset) {
            return withCharsetName(charset.name());
        }

        List<Statement> statements;
        Space eof;

        @Override
        public <P> Utility acceptUtility(UtilityVisitor<P> v, P p) {
            return v.visitCompilationUnit(this, p);
        }

        @Override
        public <P> TreeVisitor<?, PrintOutputCapture<P>> printer(Cursor cursor) {
            return new UtilityPrinter<>();
        }
    }

    /**
     * A keyword and everything written under it.
     * <p>
     * A utility control statement and the blocks written inside one share this shape, so they share a
     * node and are told apart by {@link #getVerb()} and by what encloses them: {@code UNLOAD},
     * {@code GLOBAL}, {@code TEMPLATE}, {@code LOAD}, {@code RUNSTATS} open a statement, and
     * {@code SELECT}, {@code FROM TABLE}, {@code FORMAT} and {@code OPTIONS} open a block within one.
     * <p>
     * Blocks nest because the source nests them: an unload of one table space fans out to several
     * {@code SELECT}s, each with its own {@code OUTDDN} and its own {@code FORMAT}, and an
     * {@code OPTIONS} block means something different at each level it may be written at.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Block implements Utility, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        /**
         * The keyword the block opens with: {@code UNLOAD}, {@code SELECT}, {@code FORMAT}.
         */
        Word verb;

        /**
         * The words between the verb and the first keyword under it, which is where what the block is
         * about is written: the columns of a {@code SELECT}, the name of a {@code TEMPLATE}, the
         * layout of a {@code FORMAT}.
         */
        List<Word> value;

        /**
         * Everything after that, in source order: the {@link Operand}s, the blocks nested in this one
         * and the semicolon that ends it. Keeping them in one ordered list is what lets a block print
         * back exactly, however it was laid out.
         */
        List<Utility> contents;

        @Override
        public <P> Utility acceptUtility(UtilityVisitor<P> v, P p) {
            return v.visitBlock(this, p);
        }

        public boolean isVerb(String verb) {
            return this.verb.getText().equalsIgnoreCase(verb);
        }

        /**
         * The text between the verb and the first keyword, each break in it reduced to one blank.
         */
        public String getValueText() {
            return Words.textOf(value);
        }

        public List<Operand> getOperands() {
            List<Operand> operands = new ArrayList<>(contents.size());
            for (Utility content : contents) {
                if (content instanceof Operand) {
                    operands.add((Operand) content);
                }
            }
            return operands;
        }

        /**
         * The operand of this keyword, or null. A keyword is written once in a block, so the first is
         * the only one.
         */
        public @Nullable Operand getOperand(String keyword) {
            for (Operand operand : getOperands()) {
                if (operand.getKeyword().getText().equalsIgnoreCase(keyword)) {
                    return operand;
                }
            }
            return null;
        }

        /**
         * The blocks written inside this one.
         */
        public List<Block> getBlocks() {
            List<Block> blocks = new ArrayList<>(contents.size());
            for (Utility content : contents) {
                if (content instanceof Block) {
                    blocks.add((Block) content);
                }
            }
            return blocks;
        }

        /**
         * The blocks written inside this one under a given verb, in the order they were written.
         */
        public List<Block> getBlocks(String verb) {
            List<Block> blocks = new ArrayList<>();
            for (Block block : getBlocks()) {
                if (block.isVerb(verb)) {
                    blocks.add(block);
                }
            }
            return blocks;
        }
    }

    /**
     * An operand written as a keyword and the words belonging to it: {@code DB2 NO},
     * {@code OUTDDN (HSTUNL)}, {@code WHERE POSTED_DATE > '2001-12-31'}.
     * <p>
     * The value is a list of words rather than one, because the value of a keyword is whatever is
     * written before the next keyword of the block, a parenthesised list runs over as many cards as it
     * needs, and the lexer breaks a quoted string out on its own. Reading the value back means joining
     * them, and they are kept apart so that printing puts them back exactly as they were.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Operand implements Utility {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        /**
         * The keyword: {@code DB2}, {@code OUTDDN}, {@code FROM TABLE} is written as two words and
         * keyed under the first.
         */
        Word keyword;

        List<Word> value;

        @Override
        public <P> Utility acceptUtility(UtilityVisitor<P> v, P p) {
            return v.visitOperand(this, p);
        }

        /**
         * What the operand means: the value with each break in it reduced to one blank, so that a
         * {@code WHERE} written over three cards reads as one predicate. Printing walks the words
         * instead, so nothing here has to reproduce the layout.
         */
        public String getValueText() {
            return Words.textOf(value);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Word implements Utility {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;
        String text;

        @Override
        public <P> Utility acceptUtility(UtilityVisitor<P> v, P p) {
            return v.visitWord(this, p);
        }
    }
}
