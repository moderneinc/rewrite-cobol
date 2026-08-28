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
package org.openrewrite.mainframe.controlcard.sort.tree;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.With;
import org.jspecify.annotations.Nullable;
import org.openrewrite.*;
import org.openrewrite.mainframe.controlcard.sort.SortVisitor;
import org.openrewrite.mainframe.controlcard.sort.internal.SortPrinter;
import org.openrewrite.marker.Markers;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public interface Sort extends Tree {

    @SuppressWarnings("unchecked")
    @Override
    default <R extends Tree, P> R accept(TreeVisitor<R, P> v, P p) {
        return (R) acceptSort(v.adapt(SortVisitor.class), p);
    }

    default <P> @Nullable Sort acceptSort(SortVisitor<P> v, P p) {
        return v.defaultValue(this, p);
    }

    @Override
    default <P> boolean isAcceptable(TreeVisitor<?, P> v, P p) {
        return v.isAdaptableTo(SortVisitor.class);
    }

    Space getPrefix();

    <P extends Sort> P withPrefix(Space prefix);

    /**
     * A deck of DFSORT or ICETOOL control statements, as a control card member or as the in-stream
     * data of a {@code SYSIN}, {@code TOOLIN}, {@code DFSPARM} or {@code xxxxCNTL} DD.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class CompilationUnit implements Sort, SourceFile {

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
        public <P> Sort acceptSort(SortVisitor<P> v, P p) {
            return v.visitCompilationUnit(this, p);
        }

        @Override
        public <P> TreeVisitor<?, PrintOutputCapture<P>> printer(Cursor cursor) {
            return new SortPrinter<>();
        }
    }

    /**
     * One control statement: an operator and the operands belonging to it, however many cards they
     * are written over.
     * <p>
     * {@code SORT}, {@code MERGE}, {@code INCLUDE}, {@code OMIT}, {@code INREC}, {@code OUTREC},
     * {@code OUTFIL}, {@code SUM} and {@code OPTION} share this shape, as do the ICETOOL operators,
     * so they share a node and are told apart by {@link #getOperator()}.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class ControlStatement implements Sort, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        Word operator;

        /**
         * Everything after the operator, in source order: the {@link Operand}s and, in an ICETOOL
         * deck, the dash ending each continued card. Keeping them in one ordered list is what lets
         * the statement print back exactly, however it was laid out.
         */
        List<Sort> operands;

        @Override
        public <P> Sort acceptSort(SortVisitor<P> v, P p) {
            return v.visitControlStatement(this, p);
        }

        public boolean isOperator(String operator) {
            return this.operator.getText().equalsIgnoreCase(operator);
        }

        public List<Operand> getParameters() {
            List<Operand> parameters = new ArrayList<>(operands.size());
            for (Sort operand : operands) {
                if (operand instanceof Operand) {
                    parameters.add((Operand) operand);
                }
            }
            return parameters;
        }

        /**
         * The operand written under this keyword, or null. A keyword is written once in a statement,
         * so the first is the only one.
         */
        public @Nullable Operand getParameter(String keyword) {
            for (Operand operand : getParameters()) {
                if (operand.getKeyword().getText().equalsIgnoreCase(keyword)) {
                    return operand;
                }
            }
            return null;
        }
    }

    /**
     * An operand written as {@code KEYWORD=value}, as {@code KEYWORD(value)} the way ICETOOL writes
     * one, or as a keyword on its own — {@code EQUALS} and {@code NODUPS} take no value.
     * <p>
     * The value is a list of words rather than one, because the lexer breaks a quoted literal out on
     * its own and a parenthesised value may be written over several cards:
     * {@code COND=(57,1,CH,EQ,C'O')} arrives as {@code COND=(57,1,CH,EQ,C}, {@code 'O'} and
     * {@code )}. Reading it back means joining them, and they are kept apart so that printing puts
     * them back exactly as they were.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Operand implements Sort {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        /**
         * The keyword, without the {@code =} or the parentheses: {@code FIELDS}, {@code COND},
         * {@code BUILD}, {@code FROM}.
         */
        Word keyword;

        List<Word> value;

        @Override
        public <P> Sort acceptSort(SortVisitor<P> v, P p) {
            return v.visitOperand(this, p);
        }

        /**
         * What the operand means: the value with the dashes that continued it removed and each break
         * in it reduced to one blank. Printing walks the words instead, so nothing here has to
         * reproduce the layout.
         */
        public String getValueText() {
            StringBuilder text = new StringBuilder();
            for (Word word : value) {
                if ("-".equals(word.getText())) {
                    continue;
                }
                if (text.length() > 0 && !word.getPrefix().getWhitespace().isEmpty()) {
                    text.append(' ');
                }
                text.append(word.getText());
            }
            return text.toString();
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Word implements Sort {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;
        String text;

        @Override
        public <P> Sort acceptSort(SortVisitor<P> v, P p) {
            return v.visitWord(this, p);
        }
    }
}
