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
package org.openrewrite.sas.tree;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.With;
import org.jspecify.annotations.Nullable;
import org.openrewrite.*;
import org.openrewrite.marker.Markers;
import org.openrewrite.sas.SasVisitor;
import org.openrewrite.sas.internal.SasPrinter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public interface Sas extends Tree {

    @SuppressWarnings("unchecked")
    @Override
    default <R extends Tree, P> R accept(TreeVisitor<R, P> v, P p) {
        return (R) acceptSas(v.adapt(SasVisitor.class), p);
    }

    default <P> @Nullable Sas acceptSas(SasVisitor<P> v, P p) {
        return v.defaultValue(this, p);
    }

    @Override
    default <P> boolean isAcceptable(TreeVisitor<?, P> v, P p) {
        return v.isAdaptableTo(SasVisitor.class);
    }

    Space getPrefix();

    <P extends Sas> P withPrefix(Space prefix);

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class CompilationUnit implements Sas, SourceFile {

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

        /**
         * The {@link Statement}s and {@link Comment}s of the program, in source order. A SAS program
         * is a flat run of both and means something structured — the statements between a
         * {@code PROC} and the {@code RUN;} under it are one step — so what belongs to what is read
         * from the cursor rather than nested here.
         */
        List<Sas> statements;

        Space eof;

        @Override
        public <P> Sas acceptSas(SasVisitor<P> v, P p) {
            return v.visitCompilationUnit(this, p);
        }

        @Override
        public <P> TreeVisitor<?, PrintOutputCapture<P>> printer(Cursor cursor) {
            return new SasPrinter<>();
        }
    }

    /**
     * A statement: everything written up to the semicolon that ends it, however many lines that took.
     * <p>
     * Nothing but the first word says what kind of statement it is, and SAS has no reserved words —
     * {@code DATA} names a step here and a variable there — so the reader keeps every word as it was
     * written and leaves the reading to the traits.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Statement implements Sas {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        /**
         * The {@link Word}s of the statement and any {@link Comment} written between them, in source
         * order.
         */
        List<Sas> parts;

        /**
         * The space in front of the terminating semicolon, or null for a statement the member ends
         * without terminating. The semicolon itself is the printer's to write: it is the only
         * character a statement can end with, so a node for it would carry nothing.
         */
        @Nullable
        Space end;

        @Override
        public <P> Sas acceptSas(SasVisitor<P> v, P p) {
            return v.visitStatement(this, p);
        }

        public List<Word> getWords() {
            List<Word> words = new ArrayList<>(parts.size());
            for (Sas part : parts) {
                if (part instanceof Word) {
                    words.add((Word) part);
                }
            }
            return words;
        }

        public List<String> getWordTexts() {
            List<Word> words = getWords();
            List<String> texts = new ArrayList<>(words.size());
            for (Word word : words) {
                texts.add(word.getText());
            }
            return texts;
        }

        /**
         * The first word upper cased, which is what says what the statement is. Empty for a statement
         * of nothing but a semicolon, which SAS allows and means nothing.
         */
        public String getKeyword() {
            List<Word> words = getWords();
            return words.isEmpty() ? "" : words.get(0).getUpperText();
        }

        public boolean isKeyword(String keyword) {
            return keyword.equalsIgnoreCase(getKeyword());
        }

        /**
         * The nth word's text, or null where the statement wrote fewer than that.
         */
        public @Nullable String getWordText(int index) {
            List<Word> words = getWords();
            return index >= 0 && index < words.size() ? words.get(index).getText() : null;
        }

        /**
         * The words joined by a single blank, which is what a trait reading a statement lexically
         * asks for. Printing walks the parts instead, so the layout is kept there and left out here.
         */
        public String getText() {
            return String.join(" ", getWordTexts());
        }
    }

    /**
     * A comment, in either of the two shapes SAS writes: {@code /*...*}{@code /} anywhere a blank may
     * go, and a statement beginning {@code *} or {@code %*} that runs to its semicolon.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Comment implements Sas {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        /**
         * The comment as written, delimiters and all.
         */
        String text;

        @Override
        public <P> Sas acceptSas(SasVisitor<P> v, P p) {
            return v.visitComment(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Word implements Sas {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;
        String text;

        @Override
        public <P> Sas acceptSas(SasVisitor<P> v, P p) {
            return v.visitWord(this, p);
        }

        public String getUpperText() {
            return text.toUpperCase(Locale.ROOT);
        }
    }
}
