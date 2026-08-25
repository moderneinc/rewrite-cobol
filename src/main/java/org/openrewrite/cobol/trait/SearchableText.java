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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.cobol.SourcePositions;
import org.openrewrite.cobol.marker.CopiedWord;
import org.openrewrite.cobol.marker.ElidedExec;
import org.openrewrite.cobol.tree.Cobol;
import org.openrewrite.cobol.tree.CobolLine;
import org.openrewrite.cobol.tree.ColumnArea;
import org.openrewrite.cobol.tree.CommentArea;
import org.openrewrite.marker.Range;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;
import org.openrewrite.trait.VisitFunction2;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Pattern;

import static java.util.Collections.singletonList;

/**
 * The text of a word as a search reads it.
 * <p>
 * A search over COBOL asks three things the tree does not say directly. Which layer a word is in: a
 * hit in a comment, in a literal and in code are different results, and Ora-Web lets a user ask for
 * one layer at a time. What the tokens are: {@code ACCT} finds {@code WS-ACCT-ID}, because a hyphen,
 * a period and an underscore each end a token and a match is a whole token or nothing. And what a
 * literal continued across a column-7 break says: {@code 'OPEN INP} on one line and {@code UT
 * FAILED'} on the next are one literal saying {@code OPEN INPUT FAILED}, which a phrase has to match
 * as one while a highlight lands on each line.
 * <p>
 * A comment is a line rather than a word, so it reaches a search through the word it is attached to:
 * the comment lines before a word hang off it, and a floating {@code *>} comment off the word it
 * follows. The asterisks and dashes a box is drawn with are not text, and are stripped.
 * <p>
 * What is described is the program's own text, as printed. A word a copybook supplied is the
 * copybook's to report, and the stand-in for an {@code EXEC} block prints nothing, the block's own
 * words being reached on their own.
 */
@Value
public class SearchableText implements Trait<Cobol.Word> {

    private static final Pattern NUMERIC = Pattern.compile("[+-]?(\\d+|\\d*[.,]\\d+)([Ee][+-]?\\d+)?");
    private static final String BORDERS = "*-=+|#/\\>~_";
    private static final String FLOATING_COMMENT = "*>";

    Cursor cursor;

    public enum Layer {
        CODE,
        COMMENT,
        STRING_LITERAL,
        NUMERIC_LITERAL
    }

    public Layer getLayer() {
        String text = getTree().getWord();
        Object parent = cursor.getParentTreeCursor().getValue();
        if (parent instanceof Cobol.CommentEntry) {
            return Layer.COMMENT;
        }
        if (isStringLiteral(text)) {
            return Layer.STRING_LITERAL;
        }
        // A level number is structure rather than a value, and the 99 in a picture is not a number.
        if (NUMERIC.matcher(text).matches() &&
            !(parent instanceof Cobol.DataDescriptionEntry) &&
            cursor.firstEnclosing(Cobol.PictureString.class) == null) {
            return Layer.NUMERIC_LITERAL;
        }
        return Layer.CODE;
    }

    /**
     * The word as written, joined across any continuation. A literal keeps its quotes, which is
     * where the tokens end anyway; {@link Literals#valueOf} reads the value. A comment entry — the
     * text of {@code AUTHOR} or {@code DATE-WRITTEN}, and any comment line the paragraph swallowed
     * before the next header — reads with its borders stripped, as a comment line does.
     */
    public String getText() {
        String word = getTree().getWord();
        return getLayer() == Layer.COMMENT ? stripBorders(word) : word;
    }

    public List<Token> getTokens() {
        return tokens(getText());
    }

    /**
     * The text one line at a time. A word written on one line is one piece; a literal or name
     * continued across column-7 breaks has one per line, each knowing where in {@link #getText()} it
     * starts, so a match found in the joined text can be highlighted on the lines it spans.
     */
    public List<Piece> getPieces() {
        Cobol.Word word = getTree();
        String text = word.getWord();
        if (getLayer() == Layer.COMMENT) {
            return singletonList(new Piece(stripBorders(text), 0, word, 0, leadingBorder(text)));
        }
        List<Piece> pieces = new ArrayList<>();
        int start = 0;
        if (word.getContinuation() != null) {
            for (int at : new TreeSet<>(word.getContinuation().getContinuations().keySet())) {
                if (at > 0 && at < text.length()) {
                    pieces.add(new Piece(text.substring(start, at), start, word, pieces.size(), 0));
                    start = at;
                }
            }
        }
        pieces.add(new Piece(text.substring(start), start, word, pieces.size(), 0));
        return pieces;
    }

    /**
     * The comments around this word: the comment lines before it, in order, then any floating
     * comment on its line. A line that says nothing once its borders are gone is left out, as is a
     * line a copybook supplied.
     */
    public List<Comment> getComments() {
        Cobol.Word word = getTree();
        List<Comment> comments = new ArrayList<>();
        if (word.getLines() != null) {
            for (CobolLine line : word.getLines()) {
                if (line.isCopiedSource()) {
                    continue;
                }
                add(comments, Comment.of(line));
                add(comments, Comment.of(line.getCommentArea()));
            }
        }
        if (word.getContinuation() != null) {
            for (int at : new TreeSet<>(word.getContinuation().getContinuations().keySet())) {
                for (ColumnArea area : word.getContinuation().getContinuations().get(at)) {
                    if (area instanceof CommentArea) {
                        add(comments, Comment.of((CommentArea) area));
                    }
                }
            }
        }
        add(comments, Comment.of(word.getCommentArea()));
        return comments;
    }

    private static void add(List<Comment> comments, @Nullable Comment comment) {
        if (comment != null) {
            comments.add(comment);
        }
    }

    /**
     * The tokens of {@code text}: each run of letters and digits. Ora-Web's rule is that {@code .},
     * {@code -} and {@code _} end a token, which is what lets {@code ACCT} find {@code WS-ACCT-ID};
     * every other character that is not a letter or digit ends one too, since a comment or a literal
     * is free text and {@code width} has to find {@code width:70%}.
     */
    public static List<Token> tokens(String text) {
        List<Token> tokens = new ArrayList<>();
        int start = -1;
        for (int i = 0; i <= text.length(); i++) {
            boolean inToken = i < text.length() && Character.isLetterOrDigit(text.charAt(i));
            if (inToken && start < 0) {
                start = i;
            } else if (!inToken && start >= 0) {
                tokens.add(new Token(text.substring(start, i), start));
                start = -1;
            }
        }
        return tokens;
    }

    /**
     * A comment without the asterisks, dashes and rules a box is drawn with at either end, or the
     * whitespace around them. Only the ends are touched: a dash inside a sentence is text.
     */
    public static String stripBorders(String comment) {
        int from = leadingBorder(comment);
        return comment.substring(from, trailingBorder(comment, from));
    }

    private static int leadingBorder(String text) {
        int i = 0;
        while (i < text.length() && isBorder(text.charAt(i))) {
            i++;
        }
        return i;
    }

    private static int trailingBorder(String text, int from) {
        int i = text.length();
        while (i > from && isBorder(text.charAt(i - 1))) {
            i--;
        }
        return i;
    }

    private static boolean isBorder(char c) {
        return Character.isWhitespace(c) || BORDERS.indexOf(c) >= 0;
    }

    private static boolean isStringLiteral(String text) {
        return Literals.isLiteral(text) ||
               text.length() > 2 && "XZGNxzgn".indexOf(text.charAt(0)) >= 0 && Literals.isLiteral(text.substring(1));
    }

    /**
     * The part of a printed range that is text, once {@code skipped} characters of border or tag are
     * dropped from its start. Nothing here spans a line, so the line is the printed one.
     */
    private static Range within(Range printed, int skipped, int length) {
        Range.Position start = printed.getStart();
        Range.Position from = new Range.Position(start.getOffset() + skipped, start.getLine(), start.getColumn() + skipped);
        Range.Position to = new Range.Position(from.getOffset() + length, from.getLine(), from.getColumn() + length);
        return new Range(Tree.randomId(), from, to);
    }

    @Value
    public static class Token {
        String text;

        /**
         * Where the token starts in the text it was read from.
         */
        int offset;
    }

    @Value
    public static class Piece {
        String text;

        /**
         * Where this piece starts in the joined text.
         */
        int offset;

        @Getter(AccessLevel.NONE)
        Cobol.Word word;

        @Getter(AccessLevel.NONE)
        int index;

        @Getter(AccessLevel.NONE)
        int skipped;

        /**
         * Where this piece sits, or null when the word printed nothing.
         */
        public @Nullable Range range(SourcePositions positions) {
            List<Range> printed = positions.pieces(word);
            return index < printed.size() ? within(printed.get(index), skipped, text.length()) : null;
        }
    }

    @Value
    public static class Comment {
        /**
         * The comment with its borders stripped.
         */
        String text;

        @Getter(AccessLevel.NONE)
        @Nullable
        CobolLine line;

        @Getter(AccessLevel.NONE)
        @Nullable
        CommentArea commentArea;

        @Getter(AccessLevel.NONE)
        int skipped;

        public List<Token> getTokens() {
            return tokens(text);
        }

        /**
         * Where the text sits, or null when the line printed nothing.
         */
        public @Nullable Range range(SourcePositions positions) {
            Range printed = line != null ? positions.get(line) : commentArea != null ? positions.get(commentArea) : null;
            return printed == null ? null : within(printed, skipped, text.length());
        }

        /**
         * A line the grammar did not read is a comment, whatever its indicator says; a blank one says
         * nothing.
         */
        static @Nullable Comment of(CobolLine line) {
            return of(line.getContentArea(), 0, line, null);
        }

        /**
         * A comment area is a floating {@code *>} comment or the identification area in columns 73
         * to 80, and only the first is text.
         */
        static @Nullable Comment of(@Nullable CommentArea commentArea) {
            if (commentArea == null || !commentArea.getComment().startsWith(FLOATING_COMMENT)) {
                return null;
            }
            return of(commentArea.getComment(), FLOATING_COMMENT.length(), null, commentArea);
        }

        private static @Nullable Comment of(String printed, int after, @Nullable CobolLine line, @Nullable CommentArea commentArea) {
            int from = after + leadingBorder(printed.substring(after));
            String text = printed.substring(from, trailingBorder(printed, from));
            return text.isEmpty() ? null : new Comment(text, line, commentArea, from);
        }
    }

    public static class Matcher extends SimpleTraitMatcher<SearchableText> {

        @Override
        protected @Nullable SearchableText test(Cursor cursor) {
            Object value = cursor.getValue();
            if (!(value instanceof Cobol.Word) || !prints((Cobol.Word) value)) {
                return null;
            }
            SearchableText text = new SearchableText(cursor);
            // The word ending a file carries only whitespace, and a comment of nothing but border says nothing.
            return text.getText().isEmpty() ? null : text;
        }

        /**
         * Whether the word has text of its own in the program. A copied word is the copybook's, the
         * stand-in for an {@code EXEC} block prints nothing, and a replaced word prints its original,
         * which is visited on its own.
         */
        private static boolean prints(Cobol.Word word) {
            return word.getReplacement() == null &&
                   !word.getMarkers().findFirst(CopiedWord.class).isPresent() &&
                   !word.getMarkers().findFirst(ElidedExec.class).isPresent();
        }

        @Override
        public <P> TreeVisitor<? extends Tree, P> asVisitor(VisitFunction2<SearchableText, P> visitor) {
            return new CopybookSkippingVisitor<P>() {
                @Override
                public Cobol.Word visitWord(Cobol.Word word, P p) {
                    Cobol.Word visited = super.visitWord(word, p);
                    SearchableText text = test(new Cursor(getCursor().getParentOrThrow(), visited));
                    return text == null ? visited : (Cobol.Word) visitor.visit(text, p);
                }
            };
        }
    }

    @Override
    public String toString() {
        return getLayer() + " " + getText();
    }
}
