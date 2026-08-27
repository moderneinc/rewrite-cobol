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
package org.openrewrite.textmember.tree;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.With;
import org.jspecify.annotations.Nullable;
import org.openrewrite.*;
import org.openrewrite.marker.Markers;
import org.openrewrite.textmember.TextMemberVisitor;
import org.openrewrite.textmember.internal.TextMemberPrinter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public interface TextMember extends Tree {

    @SuppressWarnings("unchecked")
    @Override
    default <R extends Tree, P> R accept(TreeVisitor<R, P> v, P p) {
        return (R) acceptTextMember(v.adapt(TextMemberVisitor.class), p);
    }

    default <P> @Nullable TextMember acceptTextMember(TextMemberVisitor<P> v, P p) {
        return v.defaultValue(this, p);
    }

    @Override
    default <P> boolean isAcceptable(TreeVisitor<?, P> v, P p) {
        return v.isAdaptableTo(TextMemberVisitor.class);
    }

    /**
     * A member this repository types but does not parse: a REXX exec, a CLIST, a run book member, a C
     * source, a PL/I source.
     * <p>
     * There is no grammar for any of them here, and typing them anyway is the point: a member that no
     * parser claims reaches a repository as a text file, and a text file is not searchable as the
     * technology it is. So the member is held as the lines it was written as, {@link #getKind()} says
     * which technology it is, and what a member says is read by the traits — {@link
     * org.openrewrite.textmember.trait.Script} for a CLIST or an exec, {@link
     * org.openrewrite.textmember.trait.RunBook} for a run book member.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class CompilationUnit implements TextMember, SourceFile {

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

        Kind kind;

        List<Line> lines;

        @Override
        public <P> TextMember acceptTextMember(TextMemberVisitor<P> v, P p) {
            return v.visitCompilationUnit(this, p);
        }

        @Override
        public <P> TreeVisitor<?, PrintOutputCapture<P>> printer(Cursor cursor) {
            return new TextMemberPrinter<>();
        }
    }

    /**
     * One line as it was written, and what ended it.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Line implements TextMember {

        @EqualsAndHashCode.Include
        UUID id;

        Markers markers;

        String text;

        /**
         * The line terminator, which is empty on a last line the file does not end.
         */
        String lineEnding;

        @Override
        public <P> TextMember acceptTextMember(TextMemberVisitor<P> v, P p) {
            return v.visitLine(this, p);
        }
    }

    /**
     * What technology a member is. A library holds members of every kind side by side, so this is the
     * answer to "what am I looking at" that the member's name alone does not give.
     */
    enum Kind {
        /**
         * A REXX exec, run from {@code SYSEXEC} or {@code SYSPROC}.
         */
        REXX,
        /**
         * A CLIST, run from {@code SYSPROC}.
         */
        CLIST,
        /**
         * A run book member: Desjardins' {@code DOCJOB}, {@code DOCPGM}, {@code DOCFICH},
         * {@code DOCAPPL} and {@code DOCOPER}, one per job, program, file, application and operating
         * procedure. Which of the five a member is comes from its first word, not from this.
         */
        DOCUMENT,
        C,
        /**
         * PL/I, typed so that a shop's PL/I is not taken for something else. Nothing here reads it.
         */
        PLI
    }
}
