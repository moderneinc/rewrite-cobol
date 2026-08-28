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
package org.openrewrite.mainframe.db2.bind.tree;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.With;
import org.jspecify.annotations.Nullable;
import org.openrewrite.*;
import org.openrewrite.mainframe.db2.bind.BindVisitor;
import org.openrewrite.mainframe.db2.bind.internal.BindPrinter;
import org.openrewrite.marker.Markers;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public interface Bind extends Tree {

    @SuppressWarnings("unchecked")
    @Override
    default <R extends Tree, P> R accept(TreeVisitor<R, P> v, P p) {
        return (R) acceptBind(v.adapt(BindVisitor.class), p);
    }

    default <P> @Nullable Bind acceptBind(BindVisitor<P> v, P p) {
        return v.defaultValue(this, p);
    }

    @Override
    default <P> boolean isAcceptable(TreeVisitor<?, P> v, P p) {
        return v.isAdaptableTo(BindVisitor.class);
    }

    Space getPrefix();

    <P extends Bind> P withPrefix(Space prefix);

    /**
     * A deck of DSN subcommands, as a {@code CARDLIB} member or as the in-stream data of a
     * {@code SYSTSIN} DD.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class CompilationUnit implements Bind, SourceFile {

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
        public <P> Bind acceptBind(BindVisitor<P> v, P p) {
            return v.visitCompilationUnit(this, p);
        }

        @Override
        public <P> TreeVisitor<?, PrintOutputCapture<P>> printer(Cursor cursor) {
            return new BindPrinter<>();
        }
    }

    /**
     * One DSN subcommand: a verb and the operands belonging to it, however many lines they are
     * written over.
     * <p>
     * {@code BIND}, {@code REBIND}, {@code FREE}, {@code DSN}, {@code RUN} and {@code END} share this
     * shape, so they share a node and are told apart by {@link #getVerb()}.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Command implements Bind, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        /**
         * The subcommand invoked: {@code BIND}, {@code REBIND}, {@code DSN}, {@code END}.
         */
        Word verb;

        /**
         * Everything after the verb, in source order: the {@link Operand}s and the dash ending each
         * continued line. Keeping them in one ordered list is what lets the command print back
         * exactly, however it was laid out.
         */
        List<Bind> operands;

        @Override
        public <P> Bind acceptBind(BindVisitor<P> v, P p) {
            return v.visitCommand(this, p);
        }

        public boolean isVerb(String verb) {
            return this.verb.getText().equalsIgnoreCase(verb);
        }

        public List<Operand> getParameters() {
            List<Operand> parameters = new ArrayList<>(operands.size());
            for (Bind operand : operands) {
                if (operand instanceof Operand) {
                    parameters.add((Operand) operand);
                }
            }
            return parameters;
        }

        /**
         * The operand of this keyword, or null. A keyword is written once in a subcommand, so the
         * first is the only one.
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
     * An operand written as {@code KEYWORD(value)}, or as a keyword on its own — {@code RETAIN} takes
     * no value.
     * <p>
     * The value is a list of words rather than one, because the lexer breaks a quoted string out on
     * its own and a parenthesised list may be written over several lines:
     * {@code LIBRARY('CLM.PROD.DBRMLIB')} arrives as {@code (}, {@code 'CLM.PROD.DBRMLIB'} and
     * {@code )}. Reading the value back means joining them, and they are kept apart so that printing
     * puts them back exactly as they were.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Operand implements Bind {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        /**
         * The keyword, without the parentheses: {@code PACKAGE}, {@code MEMBER}, {@code PKLIST}.
         */
        Word keyword;

        List<Word> value;

        @Override
        public <P> Bind acceptBind(BindVisitor<P> v, P p) {
            return v.visitOperand(this, p);
        }

        /**
         * What the operand means: the parenthesised value with the dashes that continued it removed
         * and each break in it reduced to one blank, so that {@code PKLIST(A.B -} … {@code A.C )}
         * reads as {@code (A.B A.C )}. Printing walks the words instead, so nothing here has to
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
    class Word implements Bind {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;
        String text;

        @Override
        public <P> Bind acceptBind(BindVisitor<P> v, P p) {
            return v.visitWord(this, p);
        }
    }
}
