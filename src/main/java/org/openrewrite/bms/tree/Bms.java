/*
 * Copyright 2025 the original author or authors.
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
package org.openrewrite.bms.tree;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.With;
import org.jspecify.annotations.Nullable;
import org.openrewrite.*;
import org.openrewrite.bms.BmsVisitor;
import org.openrewrite.bms.internal.BmsPrinter;
import org.openrewrite.marker.Markers;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public interface Bms extends Tree {

    @SuppressWarnings("unchecked")
    @Override
    default <R extends Tree, P> R accept(TreeVisitor<R, P> v, P p) {
        return (R) acceptBms(v.adapt(BmsVisitor.class), p);
    }

    default <P> @Nullable Bms acceptBms(BmsVisitor<P> v, P p) {
        return v.defaultValue(this, p);
    }

    @Override
    default <P> boolean isAcceptable(TreeVisitor<?, P> v, P p) {
        return v.isAdaptableTo(BmsVisitor.class);
    }

    Space getPrefix();

    <P extends Bms> P withPrefix(Space prefix);

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class CompilationUnit implements Bms, SourceFile {

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
        public <P> Bms acceptBms(BmsVisitor<P> v, P p) {
            return v.visitCompilationUnit(this, p);
        }

        @Override
        public <P> TreeVisitor<?, PrintOutputCapture<P>> printer(Cursor cursor) {
            return new BmsPrinter<>();
        }
    }

    /**
     * A macro statement: an optional name field, an operation, and the operands belonging to it,
     * however many lines they are written over.
     * <p>
     * {@code DFHMSD}, {@code DFHMDI}, {@code DFHMDF} and {@code END} share this shape, so they share
     * a node and are told apart by {@link #getOperation()}. Named for the assembler construct rather
     * than simply {@code Statement}, because {@link Statement} is what every line of a mapset is,
     * comments among them.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class MacroStatement implements Bms, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        /**
         * The name field, which begins in column 1. Null when the statement has none — every
         * {@code DFHMDF} writing a screen literal rather than a field a program can name.
         */
        @Nullable
        Word name;

        /**
         * The macro invoked: {@code DFHMSD}, {@code DFHMDI}, {@code DFHMDF}, {@code END}.
         */
        Word operation;

        /**
         * Everything after the operation, in source order: the {@link Operand}s of the operand
         * field, the words of any comment field, and the character in column 72 ending each
         * continued line. Keeping them in one ordered list is what lets the statement print back
         * exactly, however it was laid out.
         */
        List<Bms> operands;

        @Override
        public <P> Bms acceptBms(BmsVisitor<P> v, P p) {
            return v.visitMacroStatement(this, p);
        }

        public boolean isOperation(String operation) {
            return this.operation.getText().equalsIgnoreCase(operation);
        }

        /**
         * The name field, or empty for a statement with none.
         */
        public String getSimpleName() {
            return name == null ? "" : name.getText();
        }

        public List<Operand> getParameters() {
            List<Operand> parameters = new ArrayList<>(operands.size());
            for (Bms operand : operands) {
                if (operand instanceof Operand) {
                    parameters.add((Operand) operand);
                }
            }
            return parameters;
        }

        /**
         * The keyword operand of this name, or null. Keyword operands are unique within a macro, so
         * the first is the only one.
         */
        public Bms.@Nullable KeywordOperand getParameter(String keyword) {
            for (Operand operand : getParameters()) {
                if (operand instanceof KeywordOperand &&
                    ((KeywordOperand) operand).getKeyword().getText().equalsIgnoreCase(keyword)) {
                    return (KeywordOperand) operand;
                }
            }
            return null;
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Comment implements Bms, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;
        Word word;

        @Override
        public <P> Bms acceptBms(BmsVisitor<P> v, P p) {
            return v.visitComment(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Unknown implements Bms, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;
        Word word;

        @Override
        public <P> Bms acceptBms(BmsVisitor<P> v, P p) {
            return v.visitUnknown(this, p);
        }
    }

    /**
     * An operand written as {@code KEYWORD=value}.
     * <p>
     * The value is a list of words rather than one, because the lexer breaks a quoted string out on
     * its own: {@code INITIAL='Tran :'} arrives as {@code INITIAL=} and {@code 'Tran :'}. Reading
     * the value back means joining them, and they are kept apart so that printing puts them back
     * exactly as they were.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class KeywordOperand implements Bms, Operand {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        /**
         * The keyword, without the {@code =}: {@code POS}, {@code LENGTH}, {@code ATTRB}.
         */
        Word keyword;

        List<Word> value;

        @Override
        public <P> Bms acceptBms(BmsVisitor<P> v, P p) {
            return v.visitKeywordOperand(this, p);
        }

        /**
         * What the operand means: the value without the {@code =} that introduces it or the comma
         * that separates it from the next. Printing walks the words instead, so the punctuation is
         * kept there and left out here.
         */
        public String getValueText() {
            StringBuilder text = new StringBuilder();
            for (Word word : value) {
                text.append(word.getText());
            }
            return trimSeparators(text.toString().substring(1));
        }
    }

    /**
     * An operand with no keyword. The BMS macros take keyword operands throughout, so this is what
     * anything else in the operand field becomes rather than a shape the macros ask for.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class PositionalOperand implements Bms, Operand {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Word> value;

        @Override
        public <P> Bms acceptBms(BmsVisitor<P> v, P p) {
            return v.visitPositionalOperand(this, p);
        }

        /**
         * The value without the comma separating it from the next operand.
         */
        public String getValueText() {
            StringBuilder text = new StringBuilder();
            for (Word word : value) {
                text.append(word.getText());
            }
            return trimSeparators(text.toString());
        }
    }

    static String trimSeparators(String text) {
        return text.endsWith(",") ? text.substring(0, text.length() - 1) : text;
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Word implements Bms {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;
        String text;

        @Override
        public <P> Bms acceptBms(BmsVisitor<P> v, P p) {
            return v.visitWord(this, p);
        }
    }
}
