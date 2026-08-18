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
package org.openrewrite.cobol.trait;

import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.cobol.CobolIsoVisitor;
import org.openrewrite.cobol.CobolPreprocessorIsoVisitor;
import org.openrewrite.cobol.marker.MissingCopybook;
import org.openrewrite.cobol.tree.Cobol;
import org.openrewrite.cobol.tree.CobolPreprocessor;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.marker.SearchResult;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;
import org.openrewrite.trait.VisitFunction2;

import java.nio.file.Path;

/**
 * A program pulling in a copybook, written either way: {@code COPY ACCTREC} or {@code EXEC SQL
 * INCLUDE ACCTREC END-EXEC}.
 * <p>
 * The two are unrelated node types holding the name in different places, and every caller that cared
 * about copybooks had to know both. They mean one thing — this program's record layout comes from
 * that library member — so they read as one thing here.
 */
@Value
public class CopybookReference implements Trait<CobolPreprocessor> {

    Cursor cursor;

    /**
     * The copybook name as written, which is what the library is searched for.
     */
    public String getName() {
        return nameOf(getTree());
    }

    /**
     * The copybook a {@code COPY} or {@code EXEC SQL INCLUDE} names, or an empty string for anything
     * else. Static because the parser needs it before there is a cursor to build a trait from.
     */
    public static String nameOf(CobolPreprocessor tree) {
        if (tree instanceof CobolPreprocessor.CopyStatement) {
            return ((CobolPreprocessor.CopyStatement) tree).getCopySource().getName().getCobolWord().getWord();
        }
        if (tree instanceof CobolPreprocessor.ExecSqlIncludeStatement) {
            return ((CobolPreprocessor.ExecSqlIncludeStatement) tree).getCopySource().getCobolWord().getWord();
        }
        return "";
    }

    /**
     * The copybook this resolved to, or null when the parse never found a file for it.
     */
    public CobolPreprocessor.@Nullable Copybook getCopybook() {
        CobolPreprocessor tree = getTree();
        return tree instanceof CobolPreprocessor.CopyStatement ?
                ((CobolPreprocessor.CopyStatement) tree).getCopybook() :
                ((CobolPreprocessor.ExecSqlIncludeStatement) tree).getCopybook();
    }

    /**
     * Whether the library was searched and the member found. A member that was not there still gets
     * a copybook node of its own, so being missing has to be ruled out rather than inferred from
     * there being something attached.
     */
    public boolean isResolved() {
        return !isMissing() && getCopybook() != null;
    }

    /**
     * The file the copybook was read from, or null when it resolved to none. A program whose
     * copybooks are missing parses, and reads as though the fields it uses were never declared, so
     * this is the difference between an analysis that is complete and one that only looks it.
     */
    public @Nullable Path getSourcePath() {
        CobolPreprocessor.Copybook copybook = getCopybook();
        return isResolved() && copybook != null ? copybook.getSourcePath() : null;
    }

    /**
     * Whether the library was searched and the member was not there.
     */
    public boolean isMissing() {
        return getTree().getMarkers().findFirst(MissingCopybook.class).isPresent();
    }

    /**
     * The word the reference hangs off, which is the program text around it. Null for a reference
     * inside a copybook that was itself copied in.
     */
    public Cobol.@Nullable Word getEnclosingWord() {
        return cursor.firstEnclosing(Cobol.Word.class);
    }

    /**
     * This reference with a search result on the copybook name, which is where it prints. The
     * statement's own text comes from the preprocessor, so a marker anywhere else would not show.
     */
    public CobolPreprocessor marked(@Nullable String description) {
        CobolPreprocessor tree = getTree();
        if (tree instanceof CobolPreprocessor.CopyStatement) {
            CobolPreprocessor.CopyStatement copy = (CobolPreprocessor.CopyStatement) tree;
            return copy.withCopySource(copy.getCopySource()
                    .withName(SearchResult.found(copy.getCopySource().getName(), description)));
        }
        CobolPreprocessor.ExecSqlIncludeStatement include = (CobolPreprocessor.ExecSqlIncludeStatement) tree;
        return include.withCopySource(SearchResult.found(include.getCopySource(), description));
    }

    public static class Matcher extends SimpleTraitMatcher<CopybookReference> {

        @Override
        protected @Nullable CopybookReference test(Cursor cursor) {
            Object value = cursor.getValue();
            return value instanceof CobolPreprocessor.CopyStatement ||
                   value instanceof CobolPreprocessor.ExecSqlIncludeStatement ?
                    new CopybookReference(cursor) : null;
        }

        /**
         * A copy statement is reached through the word it hangs off rather than through the tree, so
         * the default visitor never sees one: {@code CobolVisitor.visitWord} hands the preprocessor
         * statements to a second visitor with a cursor of its own. This walks them with the word's
         * cursor as the root, so a caller can still ask what program and paragraph it is in.
         */
        @Override
        public <P> TreeVisitor<? extends Tree, P> asVisitor(VisitFunction2<CopybookReference, P> visitor) {
            return new CobolIsoVisitor<P>() {
                @Override
                public Cobol.Word visitWord(Cobol.Word word, P p) {
                    if (word.getPreprocessorStatements().isEmpty()) {
                        return word;
                    }
                    Cursor wordCursor = getCursor();
                    CobolPreprocessorIsoVisitor<P> references = new CobolPreprocessorIsoVisitor<P>() {
                        @Override
                        public CobolPreprocessor.CopyStatement visitCopyStatement(
                                CobolPreprocessor.CopyStatement copyStatement, P p) {
                            return (CobolPreprocessor.CopyStatement) found(
                                    super.visitCopyStatement(copyStatement, p), p);
                        }

                        @Override
                        public CobolPreprocessor.ExecSqlIncludeStatement visitExecSqlIncludeStatement(
                                CobolPreprocessor.ExecSqlIncludeStatement include, P p) {
                            return (CobolPreprocessor.ExecSqlIncludeStatement) found(
                                    super.visitExecSqlIncludeStatement(include, p), p);
                        }

                        /**
                         * A copybook that copies another is not descended into. Preprocessing gives
                         * the nested copy statement a word of the including program to hang off, so
                         * it arrives here on its own; walking in as well reports it twice, and once
                         * per level of nesting above it.
                         */
                        @Override
                        public CobolPreprocessor.Copybook visitCopybook(CobolPreprocessor.Copybook copybook, P p) {
                            return copybook;
                        }

                        private CobolPreprocessor found(CobolPreprocessor visited, P p) {
                            return (CobolPreprocessor) visitor.visit(
                                    new CopybookReference(new Cursor(getCursor().getParentOrThrow(), visited)), p);
                        }
                    };
                    return word.withPreprocessorStatements(ListUtils.map(word.getPreprocessorStatements(),
                            ps -> references.visit(ps, p, wordCursor)));
                }
            };
        }
    }

    @Override
    public String toString() {
        return "COPY " + getName() + (isResolved() ? "" : isMissing() ? " (missing)" : " (unresolved)");
    }
}
