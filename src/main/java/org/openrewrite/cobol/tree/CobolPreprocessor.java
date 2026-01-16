/*
 * Copyright 2024 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.cobol.tree;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.With;
import org.jspecify.annotations.Nullable;
import org.openrewrite.*;
import org.openrewrite.cobol.CobolPreprocessorVisitor;
import org.openrewrite.cobol.internal.CobolPreprocessorPrinter;
import org.openrewrite.marker.Markers;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface CobolPreprocessor extends Tree {

    @SuppressWarnings("unchecked")
    @Override
    default <R extends Tree, P> R accept(TreeVisitor<R, P> v, P p) {
        return (R) acceptCobolPreprocessor(v.adapt(CobolPreprocessorVisitor.class), p);
    }

	default <P> @Nullable CobolPreprocessor acceptCobolPreprocessor(CobolPreprocessorVisitor<P> v, P p) {
        return v.defaultValue(this, p);
    }

    @Override
    default <P> boolean isAcceptable(TreeVisitor<?, P> v, P p) {
        return v.isAdaptableTo(CobolPreprocessorVisitor.class);
    }

    Space getPrefix();

    <P extends CobolPreprocessor> P withPrefix(Space prefix);

	@Override default UUID getId() {
        return Cobol.CompilationUnit.id;
    }

	@Override default <T extends Tree> T withId(UUID id) {
        //noinspection unchecked
        return (T) this;
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    @With
    class CompilationUnit implements CobolPreprocessor, SourceFile {

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

        Map<String, CobolPreprocessor> preprocessorStatements;
        Map<String, Replacement> replacements;
        List<CobolPreprocessor> cobols;

        Word eof;

        @Override
        public <P> CobolPreprocessor acceptCobolPreprocessor(CobolPreprocessorVisitor<P> v, P p) {
            return v.visitCompilationUnit(this, p);
        }

        @Override
        public <P> TreeVisitor<?, PrintOutputCapture<P>> printer(Cursor cursor) {
            return new CobolPreprocessorPrinter<>(true, true);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    @With
    class CharData implements CobolPreprocessor {

        Space prefix;
        Markers markers;

        List<CharDataLine> cobols;

        @Override
        public <P> CobolPreprocessor acceptCobolPreprocessor(CobolPreprocessorVisitor<P> v, P p) {
            return v.visitCharData(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    @With
    class CharDataLine implements CobolPreprocessor {

        Space prefix;
        Markers markers;

        List<CobolPreprocessor> words;

        @Override
        public <P> CobolPreprocessor acceptCobolPreprocessor(CobolPreprocessorVisitor<P> v, P p) {
            return v.visitCharDataLine(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    @With
    class CharDataSql implements CobolPreprocessor {

        Space prefix;
        Markers markers;

        List<CobolPreprocessor> cobols;

        @Override
        public <P> CobolPreprocessor acceptCobolPreprocessor(CobolPreprocessorVisitor<P> v, P p) {
            return v.visitCharDataSql(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    @With
    class CommentEntry implements CobolPreprocessor, Comment {

        Space prefix;
        Markers markers;
        List<Word> comments;

        @Override
        public <P> CobolPreprocessor acceptCobolPreprocessor(CobolPreprocessorVisitor<P> v, P p) {
            return v.visitCommentEntry(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    @With
    class CompilerOption implements CobolPreprocessor {

        Space prefix;
        Markers markers;

        List<CobolPreprocessor> cobols;

        @Override
        public <P> CobolPreprocessor acceptCobolPreprocessor(CobolPreprocessorVisitor<P> v, P p) {
            return v.visitCompilerOption(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    @With
    class CompilerOptions implements CobolPreprocessor {

        UUID id;

        Space prefix;
        Markers markers;

        Word word;

        List<CobolPreprocessor> cobols;

        @Override
        public <P> CobolPreprocessor acceptCobolPreprocessor(CobolPreprocessorVisitor<P> v, P p) {
            return v.visitCompilerOptions(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    @With
    class CompilerXOpts implements CobolPreprocessor {

        Space prefix;
        Markers markers;

        Word word;
        Word leftParen;
        List<CobolPreprocessor> compilerOptions;
        Word rightParen;

        @Override
        public <P> CobolPreprocessor acceptCobolPreprocessor(CobolPreprocessorVisitor<P> v, P p) {
            return v.visitCompilerXOpts(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    @With
    class Copybook implements CobolPreprocessor, CobolSourceFile {

        UUID id;
        Space prefix;
        Markers markers;

        // ... verbose for quality assurance.
        Path sourcePath;

        @Nullable
        FileAttributes fileAttributes;

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

        List<CobolPreprocessor> lst;
        CobolPreprocessor.Word eof;

        @Override
        public <P> CobolPreprocessor acceptCobolPreprocessor(CobolPreprocessorVisitor<P> v, P p) {
            return v.visitCopybook(this, p);
        }

        @Override
        public <P> TreeVisitor<?, PrintOutputCapture<P>> printer(Cursor cursor) {
            return new CobolPreprocessorPrinter<>(true, true);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    @With
    class CopySource implements CobolPreprocessor {

        Space prefix;
        Markers markers;

        Word name;

        @Nullable
        Word word;

        @Nullable
        Word copyLibrary;

        @Override
        public <P> CobolPreprocessor acceptCobolPreprocessor(CobolPreprocessorVisitor<P> v, P p) {
            return v.visitCopySource(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    @With
    class CopyStatement implements CobolPreprocessor, CopybookSource {

        UUID id;

        Space prefix;
        Markers markers;

        Word word;
        CopySource copySource;

        List<CobolPreprocessor> cobols;

        Word dot;

		CobolPreprocessor.@Nullable Copybook copybook;

        @Override
        public <P> CobolPreprocessor acceptCobolPreprocessor(CobolPreprocessorVisitor<P> v, P p) {
            return v.visitCopyStatement(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    @With
    class DirectoryPhrase implements CobolPreprocessor {

        UUID id;

        Space prefix;
        Markers markers;

        Word word;
        Word name;

        @Override
        public <P> CobolPreprocessor acceptCobolPreprocessor(CobolPreprocessorVisitor<P> v, P p) {
            return v.visitDirectoryPhrase(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    @With
    class EjectStatement implements CobolPreprocessor {

        UUID id;

        Space prefix;
        Markers markers;

        Word word;

        @Nullable
        Word dot;

        @Override
        public <P> CobolPreprocessor acceptCobolPreprocessor(CobolPreprocessorVisitor<P> v, P p) {
            return v.visitEjectStatement(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    @With
    class ExecStatement implements CobolPreprocessor {

        UUID id;

        Space prefix;
        Markers markers;

        List<Word> words;

        CobolPreprocessor cobol;

        Word endExec;

        @Nullable
        Word dot;

        @Override
        public <P> CobolPreprocessor acceptCobolPreprocessor(CobolPreprocessorVisitor<P> v, P p) {
            return v.visitExecStatement(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    @With
    class ExecSqlIncludeStatement implements CobolPreprocessor, CopybookSource {

        UUID id;
        Space prefix;
        Markers markers;
        List<Word> words;
        Word copySource;
        Word endExec;

        @Nullable
        Word dot;

        @Nullable
        Copybook copybook;

        @Override
        public <P> CobolPreprocessor acceptCobolPreprocessor(CobolPreprocessorVisitor<P> v, P p) {
            return v.visitExecSqlIncludeStatement(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    @With
    class FamilyPhrase implements CobolPreprocessor {

        UUID id;

        Space prefix;
        Markers markers;

        Word word;
        Word name;

        @Override
        public <P> CobolPreprocessor acceptCobolPreprocessor(CobolPreprocessorVisitor<P> v, P p) {
            return v.visitFamilyPhrase(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    @With
    class PseudoText implements CobolPreprocessor {

        Space prefix;
        Markers markers;

        Word doubleEqualOpen;

        @Nullable
        CharData charData;

        Word doubleEqualClose;

        @Override
        public <P> CobolPreprocessor acceptCobolPreprocessor(CobolPreprocessorVisitor<P> v, P p) {
            return v.visitPseudoText(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    @With
    class ReplaceArea implements CobolPreprocessor {

        UUID id;

        Space prefix;
        Markers markers;

        ReplaceByStatement replaceByStatement;

        @Nullable
        List<CobolPreprocessor> cobols;

        @Nullable
        ReplaceOffStatement replaceOffStatement;

        @Override
        public <P> CobolPreprocessor acceptCobolPreprocessor(CobolPreprocessorVisitor<P> v, P p) {
            return v.visitReplaceArea(this, p);
        }
    }

    /**
     * Define the {@link ReplaceClause}s in a {@link ReplaceArea}.
     */
    @Value
    @EqualsAndHashCode(callSuper = false)
    @With
    class ReplaceByStatement implements CobolPreprocessor {

        UUID id;

        Space prefix;
        Markers markers;

        Word word;
        List<ReplaceClause> clauses;
        Word dot;

        @Override
        public <P> CobolPreprocessor acceptCobolPreprocessor(CobolPreprocessorVisitor<P> v, P p) {
            return v.visitReplaceByStatement(this, p);
        }
    }

    /**
     * A ReplaceClause is a rule to change COBOL words that is applied to code in either a {@link Copybook} or {@link ReplaceArea}.
     */
    @Value
    @EqualsAndHashCode(callSuper = false)
    @With
    class ReplaceClause implements CobolPreprocessor {

        UUID id;

        Space prefix;
        Markers markers;

        CobolPreprocessor replaceable;
        Word by;
        CobolPreprocessor replacement;

        @Nullable
        List<CobolPreprocessor> subscript;

        @Nullable
        List<DirectoryPhrase> directoryPhrases;

        @Nullable
        FamilyPhrase familyPhrase;

        @Override
        public <P> CobolPreprocessor acceptCobolPreprocessor(CobolPreprocessorVisitor<P> v, P p) {
            return v.visitReplaceClause(this, p);
        }
    }

    /**
     * A ReplaceOffStatement is a part of preprocessing and marks the end of a {@link ReplaceArea}.
     */
    @Value
    @EqualsAndHashCode(callSuper = false)
    @With
    class ReplaceOffStatement implements CobolPreprocessor {

        UUID id;

        Space prefix;
        Markers markers;

        List<Word> words;

        Word dot;

        @Override
        public <P> CobolPreprocessor acceptCobolPreprocessor(CobolPreprocessorVisitor<P> v, P p) {
            return v.visitReplaceOffStatement(this, p);
        }
    }

    /**
     * Define the {@link ReplaceClause}s in a {@link ReplacingPhrase}.
     */
    @Value
    @EqualsAndHashCode(callSuper = false)
    @With
    class ReplacingPhrase implements CobolPreprocessor {

        UUID id;

        Space prefix;
        Markers markers;

        Word word;
        List<ReplaceClause> clauses;

        @Override
        public <P> CobolPreprocessor acceptCobolPreprocessor(CobolPreprocessorVisitor<P> v, P p) {
            return v.visitReplacingPhrase(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    @With
    class SkipStatement implements CobolPreprocessor {

        UUID id;

        Space prefix;
        Markers markers;

        Word word;

        @Nullable
        Word dot;

        @Override
        public <P> CobolPreprocessor acceptCobolPreprocessor(CobolPreprocessorVisitor<P> v, P p) {
            return v.visitSkipStatement(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    @With
    class TitleStatement implements CobolPreprocessor {

        UUID id;

        Space prefix;
        Markers markers;

        Word first;
        Word second;

        @Nullable
        Word dot;

        @Override
        public <P> CobolPreprocessor acceptCobolPreprocessor(CobolPreprocessorVisitor<P> v, P p) {
            return v.visitTitleStatement(this, p);
        }
    }

    @SuppressWarnings("unchecked")
    @Value
    @EqualsAndHashCode(callSuper = false)
    class Word implements CobolPreprocessor {

        Space prefix;
        Markers markers;

        @With
        Cobol.Word cobolWord;

		@Override public Space getPrefix() {
            return cobolWord.getPrefix();
        }

		@Override public Word withPrefix(Space prefix) {
            return cobolWord.getPrefix() == prefix ? this : withCobolWord(cobolWord.withPrefix(prefix));
        }

		@Override public Markers getMarkers() {
            return cobolWord.getMarkers();
        }

		@Override public Word withMarkers(Markers markers) {
            return cobolWord.getMarkers() == markers ? this : withCobolWord(cobolWord.withMarkers(markers));
        }

        @Override
        public <P> CobolPreprocessor acceptCobolPreprocessor(CobolPreprocessorVisitor<P> v, P p) {
            return v.visitWord(this, p);
        }
    }
}
