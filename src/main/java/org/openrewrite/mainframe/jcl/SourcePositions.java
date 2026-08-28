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
package org.openrewrite.mainframe.jcl;

import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.PrintOutputCapture;
import org.openrewrite.Tree;
import org.openrewrite.mainframe.jcl.internal.JclPrinter;
import org.openrewrite.mainframe.jcl.tree.Jcl;
import org.openrewrite.mainframe.jcl.tree.Statement;
import org.openrewrite.marker.Range;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Where each statement and parameter of a job sits in its source.
 * <p>
 * A JCL node carries no offset of its own, so a position is recovered the way OpenRewrite recovers
 * anything else about how a tree reads: by printing it. A node runs from the first character of its
 * first word to the last character of its last, so a statement begins at the {@code //} of its name
 * field and ends at its last operand rather than at the sequence number in columns 73 to 80.
 * <p>
 * Nothing brought in by an {@link Jcl.Expansion} is written in this member, so it has no range here.
 * It has {@link #expanded(Jcl)} instead: the {@code EXEC} or {@code INCLUDE} card that brought it in,
 * which is where this job can be opened, and where it sits in the member it was written in.
 * <p>
 * Lines and columns are one-based, as an editor reports them. Offsets are into {@link #getSource()},
 * which is the job as it was parsed.
 */
public class SourcePositions {

    private final String source;
    private final Map<UUID, int[]> spans;
    private final int[] wordStarts;
    private final int[] wordEnds;
    private final int[] lineStarts;
    private final Map<UUID, Expanded> expanded = new HashMap<>();

    private SourcePositions(String source, Map<UUID, int[]> spans, List<int[]> words) {
        this.source = source;
        this.spans = spans;
        this.wordStarts = boundsOf(words, 0);
        this.wordEnds = boundsOf(words, 1);
        this.lineStarts = lineStartsOf(source);
    }

    public static SourcePositions of(Jcl.CompilationUnit cu) {
        Measure<Integer> measure = new Measure<>();
        PrintOutputCapture<Integer> out = new PrintOutputCapture<>(0);
        measure.visit(cu, out, new Cursor(null, Cursor.ROOT_VALUE));
        SourcePositions positions = new SourcePositions(out.getOut(), measure.spans, measure.words);
        positions.placeExpansions(cu.getStatements(), null);
        return positions;
    }

    /**
     * The job as printed, which every offset here is an index into.
     */
    public String getSource() {
        return source;
    }

    public @Nullable Range get(@Nullable Jcl tree) {
        return tree == null ? null : get(tree.getId());
    }

    /**
     * Null when nothing in the job has this id, when the node it names printed no words of its own,
     * or when it was brought in by an expansion and written somewhere else.
     */
    public @Nullable Range get(@Nullable UUID id) {
        return trim(id == null ? null : spans.get(id));
    }

    /**
     * The whole cards a node was written on: column 1 through the end of the last line it reaches,
     * sequence numbers and all. A control card means something different in another column, so a
     * reader of one has to see the line rather than the words.
     */
    public @Nullable Range card(@Nullable Jcl tree) {
        Range range = get(tree);
        if (range == null) {
            return null;
        }
        int end = source.indexOf('\n', range.getEnd().getOffset());
        end = end < 0 ? source.length() : end;
        if (end > 0 && source.charAt(end - 1) == '\r') {
            end--;
        }
        return rangeOf(lineStarts[range.getStart().getLine() - 1], end);
    }

    /**
     * Where a node an expansion brought in was written, or null for one written in this member.
     */
    public @Nullable Expanded expanded(@Nullable Jcl tree) {
        return tree == null ? null : expanded.get(tree.getId());
    }

    /**
     * The text a range covers.
     */
    public String textOf(Range range) {
        return source.substring(range.getStart().getOffset(), range.getEnd().getOffset());
    }

    /**
     * A node of a procedure or INCLUDE member, placed both in the job that runs it and in the member
     * it was written in.
     */
    @Value
    public static class Expanded {

        /**
         * Where the {@code EXEC} or {@code INCLUDE} statement that brought the member in was written
         * in this job. A member that brings in another is reached through the card written here, so
         * this is always a position in the job's own source.
         */
        Range broughtInAt;

        /**
         * The member the node was written in, which is the innermost one when a member brings in
         * another: that is the file somebody changing the statement would open.
         */
        String memberName;

        /**
         * The member's cards as they were brought in, which {@link #getRange()} is a position into.
         * A procedure's body begins after its {@code PROC} card, and a DD the caller overrode is
         * written afresh on one line, so this is the member as the job runs it rather than the file
         * byte for byte.
         */
        String memberSource;

        /**
         * Where the node sits in {@link #getMemberSource()}.
         */
        Range range;

        public String getText() {
            return memberSource.substring(range.getStart().getOffset(), range.getEnd().getOffset());
        }
    }

    /**
     * An expansion is placed directly after the statement that named it, and its own statements
     * print nothing here, so that statement is what anything inside it is anchored to.
     */
    private void placeExpansions(List<Statement> statements, @Nullable Range broughtInAt) {
        for (int i = 0; i < statements.size(); i++) {
            if (!(statements.get(i) instanceof Jcl.Expansion)) {
                continue;
            }
            Jcl.Expansion expansion = (Jcl.Expansion) statements.get(i);
            Range at = broughtInAt != null ? broughtInAt : i == 0 ? null : get(statements.get(i - 1));
            if (at != null) {
                SourcePositions member = memberOf(expansion);
                for (UUID id : member.spans.keySet()) {
                    Range range = member.get(id);
                    if (range != null) {
                        expanded.put(id, new Expanded(at, expansion.getMemberName(), member.source, range));
                    }
                }
            }
            placeExpansions(expansion.getStatements(), at);
        }
    }

    /**
     * The member an expansion brought in, measured on its own. Its first card is put on a line of its
     * own where it is placed in the job, so the member's lines are counted from that card rather than
     * from the break in front of it.
     */
    private static SourcePositions memberOf(Jcl.Expansion expansion) {
        Measure<Integer> measure = new Measure<>();
        PrintOutputCapture<Integer> out = new PrintOutputCapture<>(0);
        for (Statement statement : expansion.getStatements()) {
            measure.visit(statement, out, new Cursor(null, Cursor.ROOT_VALUE));
        }
        String printed = out.getOut();
        int skip = 0;
        while (skip < printed.length() && (printed.charAt(skip) == '\n' || printed.charAt(skip) == '\r')) {
            skip++;
        }
        shift(measure.spans.values(), skip);
        shift(measure.words, skip);
        return new SourcePositions(printed.substring(skip), measure.spans, measure.words);
    }

    private @Nullable Range trim(int @Nullable [] span) {
        if (span == null) {
            return null;
        }
        int first = firstWordFrom(wordStarts, span[0]);
        int last = lastWordUntil(wordEnds, span[1]);
        return first > last ? null : rangeOf(wordStarts[first], wordEnds[last]);
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

    /**
     * A statement's span begins at the break in front of it, which is before where the member's own
     * text starts, so a shifted span is clamped rather than allowed to go negative.
     */
    private static void shift(Collection<int[]> spans, int by) {
        for (int[] span : spans) {
            span[0] = Math.max(0, span[0] - by);
            span[1] = Math.max(0, span[1] - by);
        }
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
     * Prints exactly what {@link Jcl.CompilationUnit#printer} prints, keeping the span each node
     * covered and the span of every word that carried characters of its own.
     */
    private static class Measure<P> extends JclPrinter<P> {

        private final Map<UUID, int[]> spans = new HashMap<>();
        private final List<int[]> words = new ArrayList<>();

        @Override
        public @Nullable Jcl visit(@Nullable Tree tree, PrintOutputCapture<P> p) {
            if (!(tree instanceof Jcl)) {
                return super.visit(tree, p);
            }
            int before = p.out.length();
            Jcl printed = super.visit(tree, p);
            spans.put(((Jcl) tree).getId(), new int[]{before, p.out.length()});
            return printed;
        }

        @Override
        public void wordPrinted(Jcl.Word word, int start, int end) {
            if (end > start) {
                words.add(new int[]{start, end});
            }
        }
    }
}
