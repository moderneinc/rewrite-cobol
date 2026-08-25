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
package org.openrewrite.linkedit.tree;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.With;
import org.jspecify.annotations.Nullable;
import org.openrewrite.*;
import org.openrewrite.linkedit.LinkEditVisitor;
import org.openrewrite.linkedit.internal.LinkEditPrinter;
import org.openrewrite.marker.Markers;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public interface LinkEdit extends Tree {

    @SuppressWarnings("unchecked")
    @Override
    default <R extends Tree, P> R accept(TreeVisitor<R, P> v, P p) {
        return (R) acceptLinkEdit(v.adapt(LinkEditVisitor.class), p);
    }

    default <P> @Nullable LinkEdit acceptLinkEdit(LinkEditVisitor<P> v, P p) {
        return v.defaultValue(this, p);
    }

    @Override
    default <P> boolean isAcceptable(TreeVisitor<?, P> v, P p) {
        return v.isAdaptableTo(LinkEditVisitor.class);
    }

    Space getPrefix();

    <P extends LinkEdit> P withPrefix(Space prefix);

    /**
     * A deck of binder control statements, as a {@code LINKLIB} member or as the in-stream data of the
     * {@code SYSLIN} the link-edit step reads.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class CompilationUnit implements LinkEdit, SourceFile {

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
        public <P> LinkEdit acceptLinkEdit(LinkEditVisitor<P> v, P p) {
            return v.visitCompilationUnit(this, p);
        }

        @Override
        public <P> TreeVisitor<?, PrintOutputCapture<P>> printer(Cursor cursor) {
            return new LinkEditPrinter<>();
        }
    }

    /**
     * One control statement: an operator and the operands belonging to it, however many cards they are
     * written over.
     * <p>
     * {@code INCLUDE}, {@code ENTRY}, {@code ALIAS}, {@code NAME}, {@code ORDER}, {@code MODE},
     * {@code SETCODE}, {@code SETOPT}, {@code CHANGE}, {@code REPLACE}, {@code IDENTIFY} and
     * {@code PAGE} share this shape, so they share a node and are told apart by {@link #getOperator()}.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class ControlStatement implements LinkEdit, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        Word operator;

        /**
         * Everything after the operator, in source order: the {@link Operand}s and the commas between
         * them. Keeping them in one ordered list is what lets the statement print back exactly,
         * however it was laid out.
         */
        List<LinkEdit> operands;

        @Override
        public <P> LinkEdit acceptLinkEdit(LinkEditVisitor<P> v, P p) {
            return v.visitControlStatement(this, p);
        }

        public boolean isOperator(String operator) {
            return this.operator.getText().equalsIgnoreCase(operator);
        }

        public List<Operand> getParameters() {
            List<Operand> parameters = new ArrayList<>(operands.size());
            for (LinkEdit operand : operands) {
                if (operand instanceof Operand) {
                    parameters.add((Operand) operand);
                }
            }
            return parameters;
        }
    }

    /**
     * An operand written as {@code KEYWORD(value)} — the {@code ddname(member)} of an {@code INCLUDE},
     * the {@code (R)} of a {@code NAME} — or as a name on its own, which is how {@code ENTRY},
     * {@code ALIAS} and {@code ORDER} write theirs.
     * <p>
     * The value is a list of words rather than one, because the lexer breaks a quoted literal out on
     * its own and a parenthesised list may be written over several cards:
     * {@code OBJLIB(CLMU010,} … {@code CLMU020)} arrives as two words. Reading it back means joining
     * them, and they are kept apart so that printing puts them back exactly as they were.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Operand implements LinkEdit {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        /**
         * The keyword, without the parentheses: the DD name of an {@code INCLUDE}, the module name of
         * a {@code NAME}, the entry point of an {@code ENTRY}.
         */
        Word keyword;

        List<Word> value;

        @Override
        public <P> LinkEdit acceptLinkEdit(LinkEditVisitor<P> v, P p) {
            return v.visitOperand(this, p);
        }

        /**
         * What the operand means: the parenthesised value with each break in it reduced to one blank,
         * so that {@code OBJLIB(CLMU010,} … {@code CLMU020)} reads as {@code (CLMU010, CLMU020)}.
         * Printing walks the words instead, so nothing here has to reproduce the layout.
         */
        public String getValueText() {
            StringBuilder text = new StringBuilder();
            for (Word word : value) {
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
    class Word implements LinkEdit {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;
        String text;

        @Override
        public <P> LinkEdit acceptLinkEdit(LinkEditVisitor<P> v, P p) {
            return v.visitWord(this, p);
        }
    }
}
