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
package org.openrewrite.cobol;

import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.PrintOutputCapture;
import org.openrewrite.Tree;
import org.openrewrite.cobol.internal.CobolPrinter;
import org.openrewrite.cobol.tree.Cobol;
import org.openrewrite.marker.Range;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
 * Lines and columns are one-based, as an editor reports them. Offsets are into {@link #getSource()},
 * which is the program as it was parsed.
 */
public class SourcePositions {

    private final String source;
    private final Map<UUID, int[]> spans;
    private final int[] wordStarts;
    private final int[] wordEnds;
    private final int[] lineStarts;

    private SourcePositions(String source, Map<UUID, int[]> spans, List<int[]> words) {
        this.source = source;
        this.spans = spans;
        this.wordStarts = new int[words.size()];
        this.wordEnds = new int[words.size()];
        for (int i = 0; i < words.size(); i++) {
            wordStarts[i] = words.get(i)[0];
            wordEnds[i] = words.get(i)[1];
        }
        this.lineStarts = lineStartsOf(source);
    }

    public static SourcePositions of(Cobol.CompilationUnit cu) {
        Measure<Integer> measure = new Measure<>();
        PrintOutputCapture<Integer> out = new PrintOutputCapture<>(0);
        measure.visit(cu, out, new Cursor(null, Cursor.ROOT_VALUE));
        return new SourcePositions(out.getOut(), measure.spans, measure.words);
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
        int[] span = id == null ? null : spans.get(id);
        if (span == null) {
            return null;
        }
        int first = firstWordFrom(span[0]);
        int last = lastWordUntil(span[1]);
        if (first > last) {
            return null;
        }
        return new Range(Tree.randomId(), positionAt(wordStarts[first]), positionAt(wordEnds[last]));
    }

    /**
     * The text a range covers.
     */
    public String textOf(Range range) {
        return source.substring(range.getStart().getOffset(), range.getEnd().getOffset());
    }

    private Range.Position positionAt(int offset) {
        int line = Arrays.binarySearch(lineStarts, offset);
        line = line < 0 ? -line - 2 : line;
        return new Range.Position(offset, line + 1, offset - lineStarts[line] + 1);
    }

    private int firstWordFrom(int offset) {
        int i = Arrays.binarySearch(wordStarts, offset);
        return i < 0 ? -i - 1 : i;
    }

    private int lastWordUntil(int offset) {
        int i = Arrays.binarySearch(wordEnds, offset);
        return i < 0 ? -i - 2 : i;
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
        private final List<int[]> words = new ArrayList<>();

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
        public void wordPrinted(Cobol.Word word, int start, int end) {
            // The word ending a compilation unit carries the trailing whitespace and no text of its own.
            // Left in, its empty span at the end of the file would extend the last statement over the newline.
            if (end > start) {
                words.add(new int[]{start, end});
            }
        }
    }
}
