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
package org.openrewrite.controlcard.idcams.tree;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.With;
import org.jspecify.annotations.Nullable;
import org.openrewrite.*;
import org.openrewrite.controlcard.idcams.IdcamsVisitor;
import org.openrewrite.controlcard.idcams.internal.IdcamsPrinter;
import org.openrewrite.marker.Markers;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public interface Idcams extends Tree {

    @SuppressWarnings("unchecked")
    @Override
    default <R extends Tree, P> R accept(TreeVisitor<R, P> v, P p) {
        return (R) acceptIdcams(v.adapt(IdcamsVisitor.class), p);
    }

    default <P> @Nullable Idcams acceptIdcams(IdcamsVisitor<P> v, P p) {
        return v.defaultValue(this, p);
    }

    @Override
    default <P> boolean isAcceptable(TreeVisitor<?, P> v, P p) {
        return v.isAdaptableTo(IdcamsVisitor.class);
    }

    Space getPrefix();

    <P extends Idcams> P withPrefix(Space prefix);

    /**
     * A deck of access method services commands, as a control card member or as the in-stream data of
     * an IDCAMS step's {@code SYSIN} DD.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class CompilationUnit implements Idcams, SourceFile {

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
        public <P> Idcams acceptIdcams(IdcamsVisitor<P> v, P p) {
            return v.visitCompilationUnit(this, p);
        }

        @Override
        public <P> TreeVisitor<?, PrintOutputCapture<P>> printer(Cursor cursor) {
            return new IdcamsPrinter<>();
        }
    }

    /**
     * One command: a verb and the parameters belonging to it, however many cards they are written
     * over.
     * <p>
     * {@code DEFINE}, {@code DELETE}, {@code REPRO}, {@code LISTCAT}, {@code PRINT}, {@code ALTER}
     * and the modal {@code SET} and {@code IF} share this shape, so they share a node and are told
     * apart by {@link #getVerb()}.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Command implements Idcams, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        Word verb;

        /**
         * Everything after the verb, in source order: the {@link Parameter}s and the dash or plus
         * ending each continued card. Keeping them in one ordered list is what lets the command
         * print back exactly, however it was laid out.
         */
        List<Idcams> parameters;

        @Override
        public <P> Idcams acceptIdcams(IdcamsVisitor<P> v, P p) {
            return v.visitCommand(this, p);
        }

        public boolean isVerb(String verb) {
            return this.verb.getText().equalsIgnoreCase(verb);
        }

        /**
         * The parameters proper, without the characters that continued the command over several
         * cards.
         */
        public List<Parameter> getNamedParameters() {
            List<Parameter> named = new ArrayList<>(parameters.size());
            for (Idcams parameter : parameters) {
                if (parameter instanceof Parameter) {
                    named.add((Parameter) parameter);
                }
            }
            return named;
        }

        /**
         * The parameter written under this keyword, or null. A keyword is written once in a command,
         * so the first is the only one.
         */
        public @Nullable Parameter getParameter(String keyword) {
            for (Parameter parameter : getNamedParameters()) {
                if (parameter.getKeyword().getText().equalsIgnoreCase(keyword)) {
                    return parameter;
                }
            }
            return null;
        }
    }

    /**
     * A parameter written as {@code KEYWORD(value)}, as a parameter group whose value is itself a
     * list of parameters — {@code DATA (NAME(x) CISZ(512))} — or as a word on its own, which is
     * either an option that takes no value ({@code PURGE}, {@code INDEXED}) or the entry name the
     * command works on.
     * <p>
     * The value is a list of words rather than one, because the lexer breaks a quoted name out on its
     * own and a group may be written over as many cards as it has parameters. Reading it back means
     * joining them, and they are kept apart so that printing puts them back exactly as they were.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Parameter implements Idcams {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        /**
         * The keyword, without the parentheses: {@code CLUSTER}, {@code NAME}, {@code INFILE}, or the
         * entry name itself when the parameter is a bare word.
         */
        Word keyword;

        List<Word> value;

        @Override
        public <P> Idcams acceptIdcams(IdcamsVisitor<P> v, P p) {
            return v.visitParameter(this, p);
        }

        /**
         * What the parameter means: the parenthesised value with the dashes that continued it removed
         * and each break in it reduced to one blank, so that {@code (NAME(A) -} … {@code LIMIT(7))}
         * reads as {@code (NAME(A) LIMIT(7))}. Printing walks the words instead, so nothing here has
         * to reproduce the layout.
         */
        public String getValueText() {
            StringBuilder text = new StringBuilder();
            for (Word word : value) {
                if ("-".equals(word.getText()) || "+".equals(word.getText())) {
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
    class Word implements Idcams {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;
        String text;

        @Override
        public <P> Idcams acceptIdcams(IdcamsVisitor<P> v, P p) {
            return v.visitWord(this, p);
        }
    }
}
