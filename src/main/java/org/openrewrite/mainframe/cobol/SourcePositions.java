/*
 * Copyright 2025 the original author or authors.
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
package org.openrewrite.mainframe.cobol;

import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.PrintOutputCapture;
import org.openrewrite.Tree;
import org.openrewrite.mainframe.cobol.internal.CobolPreprocessorPrinter;
import org.openrewrite.mainframe.cobol.internal.CobolPreprocessorSourcePrinter;
import org.openrewrite.mainframe.cobol.internal.CobolPrinter;
import org.openrewrite.mainframe.cobol.marker.ElidedExec;
import org.openrewrite.mainframe.cobol.tree.Cobol;
import org.openrewrite.mainframe.cobol.tree.CobolLine;
import org.openrewrite.mainframe.cobol.tree.CobolPreprocessor;
import org.openrewrite.mainframe.cobol.tree.CommentArea;
import org.openrewrite.marker.Range;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

/**
 * Where each node of a program sits in its source.
 * <p>
 * A COBOL word carries the column areas it was read from but no offset of its own, so a position is
 * recovered the way OpenRewrite recovers anything else about how a tree reads: by printing it. A
 * node runs from the first character of its first word to the last character of its last, so a
 * statement begins at its verb rather than at the sequence number in front of it, and ends at its
 * final word rather than at the end of the line carrying it.
 * <p>
 * A node whose words all came from a copybook prints nothing of its own in the including program and
 * has no position there. That is the honest answer rather than a gap: the source it came from is the
 * copybook's, and it is the copybook that has somewhere to point at.
 * <p>
 * Comment lines and comment areas are not nodes and have no id, so they are found by identity: ask
 * about the line or area held by the tree that was printed, not a copy of it.
 * <p>
 * Lines and columns are one-based, as an editor reports them. Offsets are into {@link #getSource()},
 * which is the program as it was parsed.
 */
public class SourcePositions {

    private final String source;
    private final Map<UUID, int[]> spans;
    private final Map<Object, int[]> texts;
    private final Map<UUID, int[]> preprocessed;
    private final int[] wordStarts;
    private final int[] wordEnds;
    private final int[] preprocessedStarts;
    private final int[] preprocessedEnds;
    private final int[] lineStarts;

    private SourcePositions(String source, Map<UUID, int[]> spans, Map<Object, int[]> texts, List<int[]> words,
                            Map<UUID, int[]> preprocessed, List<int[]> preprocessedWords) {
        this.source = source;
        this.spans = spans;
        this.texts = texts;
        this.preprocessed = preprocessed;
        this.wordStarts = boundsOf(words, 0);
        this.wordEnds = boundsOf(words, 1);
        this.preprocessedStarts = boundsOf(preprocessedWords, 0);
        this.preprocessedEnds = boundsOf(preprocessedWords, 1);
        this.lineStarts = lineStartsOf(source);
    }

    public static SourcePositions of(Cobol.CompilationUnit cu) {
        Measure<Integer> measure = new Measure<>();
        PrintOutputCapture<Integer> out = new PrintOutputCapture<>(0);
        measure.visit(cu, out, new Cursor(null, Cursor.ROOT_VALUE));
        return new SourcePositions(out.getOut(), measure.spans, measure.texts, measure.words,
                measure.preprocessed, measure.preprocessedWords);
    }

    /**
     * A copybook, or any other source the preprocessor grammar owns outright. Everything in it prints
     * through the preprocessor, so ask for its nodes with {@link #get(CobolPreprocessor)}.
     */
    public static SourcePositions of(CobolPreprocessor sourceFile) {
        MeasurePreprocessed<Integer> measure = new MeasurePreprocessed<>();
        PrintOutputCapture<Integer> out = new PrintOutputCapture<>(0);
        measure.visit(sourceFile, out, new Cursor(null, Cursor.ROOT_VALUE));
        return new SourcePositions(out.getOut(), emptyMap(), measure.texts, emptyList(),
                measure.preprocessed, measure.preprocessedWords);
    }

    /**
     * The program as printed, which every offset here is an index into.
     */
    public String getSource() {
        return source;
    }

    public @Nullable Range get(@Nullable Cobol tree) {
        return tree == null ? null : get(tree.getId());
    }

    /**
     * Null when nothing in the program has this id, or when the node it names printed no words of
     * its own.
     */
    public @Nullable Range get(@Nullable UUID id) {
        return trim(id == null ? null : spans.get(id), wordStarts, wordEnds);
    }

    /**
     * Where a preprocessor node was written: a {@code COPY} or {@code EXEC SQL INCLUDE} statement, or a
     * word of an {@code EXEC} block. What preprocessing takes out of the text the grammar sees prints
     * through its own printer, so it is measured apart from the words of the program. Null for a
     * statement copied in from a copybook, which prints nothing where it was copied to.
     */
    public @Nullable Range get(@Nullable CobolPreprocessor node) {
        return trim(node == null ? null : preprocessed.get(node.getId()), preprocessedStarts, preprocessedEnds);
    }

    /**
     * The content area of a comment or blank line, or null for a line that printed nothing.
     */
    public @Nullable Range get(CobolLine line) {
        int[] span = texts.get(line);
        return span == null ? null : rangeOf(span[0], span[1]);
    }

    /**
     * The text of a comment area, or null for one that printed nothing.
     */
    public @Nullable Range get(CommentArea commentArea) {
        int[] span = texts.get(commentArea);
        return span == null ? null : rangeOf(span[0], span[1]);
    }

    /**
     * A word's characters one line at a time: one range for a word written on one line, and one per
     * line for a literal or name continued across column-7 breaks, so a highlight can land on each.
     * Empty for a word that printed nothing.
     */
    public List<Range> pieces(Cobol.Word word) {
        int[] span = spans.get(word.getId());
        if (span == null) {
            return emptyList();
        }
        List<Range> pieces = new ArrayList<>();
        for (int i = firstWordFrom(wordStarts, span[0]), last = lastWordUntil(wordEnds, span[1]); i <= last; i++) {
            pieces.add(rangeOf(wordStarts[i], wordEnds[i]));
        }
        return pieces;
    }

    /**
     * The text a range covers.
     */
    public String textOf(Range range) {
        return source.substring(range.getStart().getOffset(), range.getEnd().getOffset());
    }

    private @Nullable Range trim(int @Nullable [] span, int[] starts, int[] ends) {
        if (span == null) {
            return null;
        }
        int first = firstWordFrom(starts, span[0]);
        int last = lastWordUntil(ends, span[1]);
        return first > last ? null : rangeOf(starts[first], ends[last]);
    }

    private Range rangeOf(int start, int end) {
        return new Range(Tree.randomId(), positionAt(start), positionAt(end));
    }

    private Range.Position positionAt(int offset) {
        int line = Arrays.binarySearch(lineStarts, offset);
        line = line < 0 ? -line - 2 : line;
        return new Range.Position(offset, line + 1, offset - lineStarts[line] + 1);
    }

    private static int firstWordFrom(int[] starts, int offset) {
        int i = Arrays.binarySearch(starts, offset);
        return i < 0 ? -i - 1 : i;
    }

    private static int lastWordUntil(int[] ends, int offset) {
        int i = Arrays.binarySearch(ends, offset);
        return i < 0 ? -i - 2 : i;
    }

    private static int[] boundsOf(List<int[]> words, int bound) {
        int[] bounds = new int[words.size()];
        for (int i = 0; i < bounds.length; i++) {
            bounds[i] = words.get(i)[bound];
        }
        return bounds;
    }

    private static int[] lineStartsOf(String source) {
        List<Integer> starts = new ArrayList<>();
        starts.add(0);
        for (int i = 0; i < source.length(); i++) {
            if (source.charAt(i) == '\n') {
                starts.add(i + 1);
            }
        }
        int[] lines = new int[starts.size()];
        for (int i = 0; i < lines.length; i++) {
            lines[i] = starts.get(i);
        }
        return lines;
    }

    /**
     * Prints exactly what {@link Cobol.CompilationUnit#printer} prints, keeping the span each node
     * covered and the span of every word that carried characters of its own.
     */
    private static class Measure<P> extends CobolPrinter<P> {

        private final Map<UUID, int[]> spans = new HashMap<>();
        private final Map<Object, int[]> texts = new IdentityHashMap<>();
        private final List<int[]> words = new ArrayList<>();
        private final Map<UUID, int[]> preprocessed = new HashMap<>();
        private final List<int[]> preprocessedWords = new ArrayList<>();
        private Cobol.@Nullable Word carrying;

        Measure() {
            super(true, true);
        }

        @Override
        public @Nullable Cobol visit(@Nullable Tree tree, PrintOutputCapture<P> p) {
            if (!(tree instanceof Cobol)) {
                return super.visit(tree, p);
            }
            int before = p.out.length();
            Cobol printed = super.visit(tree, p);
            spans.put(((Cobol) tree).getId(), new int[]{before, p.out.length()});
            return printed;
        }

        @Override
        public Cobol visitWord(Cobol.Word word, PrintOutputCapture<P> p) {
            Cobol.Word outer = carrying;
            carrying = word;
            Cobol printed = super.visitWord(word, p);
            carrying = outer;
            return printed;
        }

        @Override
        public void wordPrinted(Cobol.Word word, int start, int end) {
            // The word ending a compilation unit carries the trailing whitespace and no text of its own.
            // Left in, its empty span at the end of the file would extend the last statement over the newline.
            if (end > start) {
                words.add(new int[]{start, end});
            }
        }

        @Override
        public void contentPrinted(CobolLine line, int start, int end) {
            texts.put(line, new int[]{start, end});
        }

        @Override
        public void commentPrinted(CommentArea commentArea, int start, int end) {
            if (end > start) {
                texts.put(commentArea, new int[]{start, end});
            }
        }

        private boolean carryingAnElidedExec() {
            return carrying != null && carrying.getMarkers().findFirst(ElidedExec.class).isPresent();
        }

        /**
         * An {@code EXEC} block is taken out of the text the COBOL grammar sees, so its words arrive here
         * rather than through {@link #wordPrinted}. Those are the statements a lineage edge crosses the
         * program boundary at, and without this they would be the one kind with nowhere to point.
         * <p>
         * Only the block a stand-in word was left in place of counts. Copybooks and the declarations a
         * precompiler eats print through the same visitor while the word carrying them still has its own
         * text to print, and taking their words would start that word at something written before it.
         */
        @Override
        protected CobolPreprocessorVisitor<PrintOutputCapture<P>> getCobolPreprocessorVisitor() {
            return new CobolPreprocessorSourcePrinter<P>(true) {
                @Override
                public @Nullable CobolPreprocessor visit(@Nullable Tree tree, PrintOutputCapture<P> p) {
                    if (!(tree instanceof CobolPreprocessor)) {
                        return super.visit(tree, p);
                    }
                    int before = p.out.length();
                    CobolPreprocessor printed = super.visit(tree, p);
                    int[] span = {before, p.out.length()};
                    preprocessed.put(((CobolPreprocessor) tree).getId(), span);
                    // A block's word shares its id with the COBOL word it wraps, so the block's words are placed too.
                    if (tree instanceof CobolPreprocessor.Word && carryingAnElidedExec()) {
                        spans.put(((CobolPreprocessor) tree).getId(), span);
                    }
                    return printed;
                }

                @Override
                public void wordPrinted(CobolPreprocessor.Word word, int start, int end) {
                    if (end > start) {
                        preprocessedWords.add(new int[]{start, end});
                        if (carryingAnElidedExec()) {
                            words.add(new int[]{start, end});
                        }
                    }
                }

                @Override
                public void contentPrinted(CobolLine line, int start, int end) {
                    texts.put(line, new int[]{start, end});
                }

                @Override
                public void commentPrinted(CommentArea commentArea, int start, int end) {
                    if (end > start) {
                        texts.put(commentArea, new int[]{start, end});
                    }
                }
            };
        }
    }

    /**
     * Prints exactly what {@link CobolPreprocessor.Copybook#printer} prints, keeping the span each node
     * covered and the span of every word that carried characters of its own.
     */
    private static class MeasurePreprocessed<P> extends CobolPreprocessorPrinter<P> {

        private final Map<Object, int[]> texts = new IdentityHashMap<>();
        private final Map<UUID, int[]> preprocessed = new HashMap<>();
        private final List<int[]> preprocessedWords = new ArrayList<>();

        MeasurePreprocessed() {
            super(true, true);
        }

        @Override
        public @Nullable CobolPreprocessor visit(@Nullable Tree tree, PrintOutputCapture<P> p) {
            if (!(tree instanceof CobolPreprocessor)) {
                return super.visit(tree, p);
            }
            int before = p.out.length();
            CobolPreprocessor printed = super.visit(tree, p);
            preprocessed.put(((CobolPreprocessor) tree).getId(), new int[]{before, p.out.length()});
            return printed;
        }

        @Override
        public void wordPrinted(CobolPreprocessor.Word word, int start, int end) {
            if (end > start) {
                preprocessedWords.add(new int[]{start, end});
            }
        }

        @Override
        public void contentPrinted(CobolLine line, int start, int end) {
            texts.put(line, new int[]{start, end});
        }

        @Override
        public void commentPrinted(CommentArea commentArea, int start, int end) {
            if (end > start) {
                texts.put(commentArea, new int[]{start, end});
            }
        }
    }
}
