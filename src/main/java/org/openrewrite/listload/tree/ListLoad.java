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
package org.openrewrite.listload.tree;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.With;
import org.jspecify.annotations.Nullable;
import org.openrewrite.*;
import org.openrewrite.listload.ListLoadVisitor;
import org.openrewrite.listload.internal.ListLoadPrinter;
import org.openrewrite.marker.Markers;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public interface ListLoad extends Tree {

    @SuppressWarnings("unchecked")
    @Override
    default <R extends Tree, P> R accept(TreeVisitor<R, P> v, P p) {
        return (R) acceptListLoad(v.adapt(ListLoadVisitor.class), p);
    }

    default <P> @Nullable ListLoad acceptListLoad(ListLoadVisitor<P> v, P p) {
        return v.defaultValue(this, p);
    }

    @Override
    default <P> boolean isAcceptable(TreeVisitor<?, P> v, P p) {
        return v.isAdaptableTo(ListLoadVisitor.class);
    }

    /**
     * What AMBLIST or the binder printed about a load library: the {@code LISTLOAD}/{@code LISTIDR}
     * output of an AMBLIST run, the {@code SYSPRINT} of a link-edit step, or the request deck either
     * one was given.
     * <p>
     * A report is not a language, so it is held as the lines it was printed as. Nothing of it is
     * dropped — the page headings, the banners and the message summary are lines like any other — and
     * what the report says is read by {@link org.openrewrite.listload.trait.ModuleListing}.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class CompilationUnit implements ListLoad, SourceFile {

        @EqualsAndHashCode.Include
        UUID id;

        Path sourcePath;

        @Nullable
        FileAttributes fileAttributes;

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

        List<Line> lines;

        @Override
        public <P> ListLoad acceptListLoad(ListLoadVisitor<P> v, P p) {
            return v.visitCompilationUnit(this, p);
        }

        @Override
        public <P> TreeVisitor<?, PrintOutputCapture<P>> printer(Cursor cursor) {
            return new ListLoadPrinter<>();
        }
    }

    /**
     * One printed line.
     * <p>
     * Column 1 of a report is the ASA carriage control the printer acted on and not part of what was
     * printed — {@code 1} threw a page, {@code 0} skipped a line, a blank printed one — so it is held
     * apart from the text, and {@link #getText()} starts where the report's own column 1 does. A
     * request deck has no carriage control, and there the whole card is the text.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Line implements ListLoad {

        @EqualsAndHashCode.Include
        UUID id;

        Markers markers;

        /**
         * The ASA carriage control character, or empty for a line that carries none.
         */
        String carriageControl;

        String text;

        /**
         * The line terminator, which is empty on a last line the file does not end.
         */
        String lineEnding;

        @Override
        public <P> ListLoad acceptListLoad(ListLoadVisitor<P> v, P p) {
            return v.visitLine(this, p);
        }

        /**
         * Whether the printer threw a page before this line, which is how a report divides itself.
         */
        public boolean isPageBreak() {
            return "1".equals(carriageControl);
        }
    }
}
