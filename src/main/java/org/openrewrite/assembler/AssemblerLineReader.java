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
package org.openrewrite.assembler;

import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.openrewrite.assembler.marker.SequenceArea;
import org.openrewrite.assembler.tree.Assembler;
import org.openrewrite.assembler.tree.Space;
import org.openrewrite.assembler.tree.Statement;
import org.openrewrite.marker.Markers;

import java.util.ArrayList;
import java.util.List;

import static org.openrewrite.Tree.randomId;

/**
 * Reads an HLASM member statement by statement.
 * <p>
 * There is no grammar because the columns are the syntax. A name field begins in column 1, an
 * operation follows it, the operand field runs to the first blank outside quotes and parentheses, what
 * comes after that is remarks, a non-blank in column 72 carries the statement onto column 16 of the
 * next line, and columns 73-80 are an identification field the assembler never reads. None of that is
 * decidable by a lexer — whether a line is a continuation depends on a character of the line
 * <em>above</em> it — and a statement parser over the lines is both simpler and faster: it builds the
 * whole tree for the 1.2 MB {@code GVBMR95.asm} in about 25 ms, where an ANTLR lexer alone over the
 * same text takes three times that before a parser has run.
 * <p>
 * The reader stops at the statement. It does not evaluate an expression, resolve a symbol or expand a
 * macro; what a statement means is a trait's to say.
 */
public final class AssemblerLineReader {

    /**
     * The begin, end and continue columns before an {@code ICTL} says otherwise. The continuation
     * character sits in the column after {@code END_COLUMN}.
     */
    private static final int BEGIN_COLUMN = 1;
    private static final int END_COLUMN = 71;
    private static final int CONTINUE_COLUMN = 16;

    private final String source;

    private int begin = BEGIN_COLUMN;
    private int end = END_COLUMN;
    private int continueAt = CONTINUE_COLUMN;

    /**
     * Everything before this has been taken into a node, so it is where the next prefix starts.
     */
    private int cursor;

    @Getter
    private final List<Statement> statements = new ArrayList<>();

    @Getter
    private Space eof = Space.EMPTY;

    /**
     * The operand being built, which stays open across a line break when it ran to the end column.
     */
    private @Nullable List<Assembler> openOperand;
    private Space openPrefix = Space.EMPTY;
    private boolean quoted;
    private int depth;

    public AssemblerLineReader(String source) {
        this.source = source;
        read();
    }

    private void read() {
        List<int[]> lines = lines();
        int i = 0;
        while (i < lines.size()) {
            int[] line = lines.get(i);
            if (isComment(line)) {
                i = readComment(lines, i);
            } else if (isBlank(line)) {
                readUnknown(line);
                i++;
            } else {
                i = readInstruction(lines, i);
            }
        }
        eof = space(source.length());
    }

    /**
     * Each line as {@code {start, textEnd, nextLine}}, where the text stops before whatever ended the
     * line so that either ending prints back as it was written.
     */
    private List<int[]> lines() {
        List<int[]> lines = new ArrayList<>();
        int at = 0;
        while (at < source.length()) {
            int newline = source.indexOf('\n', at);
            int next = newline < 0 ? source.length() : newline + 1;
            int textEnd = newline < 0 ? source.length() : newline;
            if (textEnd > at && source.charAt(textEnd - 1) == '\r') {
                textEnd--;
            }
            lines.add(new int[]{at, textEnd, next});
            at = next;
        }
        return lines;
    }

    private int readComment(List<int[]> lines, int i) {
        Space prefix = space(lines.get(i)[0]);
        List<Assembler> parts = new ArrayList<>(1);
        SequenceArea trailing = null;

        int j = i;
        boolean continues;
        do {
            int[] line = lines.get(j);
            int bodyEnd = trimmed(line[0], body(line));
            if (bodyEnd > line[0]) {
                parts.add(word(line[0], bodyEnd));
            }
            continues = readContinuation(line, parts);
            trailing = readSequenceArea(line, parts);
            j++;
        } while (continues && j < lines.size());

        Markers markers = trailing == null ? Markers.EMPTY : Markers.EMPTY.addIfAbsent(trailing);
        statements.add(new Assembler.Comment(randomId(), prefix, markers, parts));
        return j;
    }

    private int readInstruction(List<int[]> lines, int i) {
        int[] first = lines.get(i);
        Space prefix = space(first[0]);

        int bodyEnd = body(first);
        Assembler.Word name = null;
        int at = first[0];
        if (source.charAt(at) == ' ') {
            at = nonBlankAfter(at, bodyEnd);
        } else {
            int nameEnd = blankAfter(at, bodyEnd);
            name = word(at, nameEnd);
            at = nonBlankAfter(nameEnd, bodyEnd);
        }
        if (at >= bodyEnd) {
            // A name field with no operation is not a statement the assembler could act on.
            statements.add(new Assembler.Unknown(randomId(), prefix, Markers.EMPTY,
                    name == null ? word(at, at) : name));
            return i + 1;
        }
        Assembler.Word operation = word(at, blankAfter(at, bodyEnd));

        List<Assembler> children = new ArrayList<>(2);
        reset();
        SequenceArea trailing = readOperandLine(first, nonBlankAfter(cursor, bodyEnd), children);
        boolean continues = continued(first);

        int j = i + 1;
        while (continues && j < lines.size() && !isComment(lines.get(j))) {
            int[] line = lines.get(j);
            int from = line[0] + continueAt - 1;
            // The operands carry on from the continue column. A line that leaves that column blank is
            // carrying on the remarks instead, so the operand it left open ends here.
            if (from >= body(line) || source.charAt(from) == ' ') {
                closeOperand(children);
                from = line[0];
            } else {
                // The columns before the continue column are ignored by the assembler and still have
                // to print back: text there is somebody having run the line above past the end column.
                readRemarks(line[0], from, children);
            }
            trailing = readOperandLine(line, from, children);
            continues = continued(line);
            j++;
        }
        closeOperand(children);

        Markers markers = trailing == null ? Markers.EMPTY : Markers.EMPTY.addIfAbsent(trailing);
        Assembler.Instruction instruction =
                new Assembler.Instruction(randomId(), prefix, markers, name, operation, children);
        statements.add(instruction);
        if ("ICTL".equals(operation.getUpperText())) {
            readColumns(instruction);
        }
        return j;
    }

    /**
     * The operand field, remarks, continuation character and identification field of one line. Answers
     * the identification field where it had no node of its own to hang on, which happens only on a
     * statement's last line.
     */
    private @Nullable SequenceArea readOperandLine(int[] line, int from, List<Assembler> children) {
        int bodyEnd = body(line);
        int at = readOperands(from, bodyEnd, children);
        readRemarks(at, bodyEnd, children);
        readContinuation(line, children);
        return readSequenceArea(line, children);
    }

    /**
     * Splits the operand field at the commas written outside quotes and parentheses, and answers where
     * it ended. An operand that ran to the end column is left open, because column 16 of the next line
     * may be the rest of it.
     */
    private int readOperands(int from, int bodyEnd, List<Assembler> children) {
        int at = from;
        int start = from;
        while (at < bodyEnd) {
            char c = source.charAt(at);
            if (quoted) {
                if (c == '\'') {
                    quoted = false;
                }
            } else if (c == '\'' && !isAttribute(at)) {
                quoted = true;
            } else if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth = Math.max(0, depth - 1);
            } else if (c == ' ' && depth == 0) {
                break;
            } else if (c == ',' && depth == 0) {
                addPart(start, at + 1);
                closeOperand(children);
                start = at + 1;
            }
            at++;
        }
        if (at > start) {
            addPart(start, at);
        }
        if (at < bodyEnd) {
            closeOperand(children);
        }
        return at;
    }

    private void readRemarks(int at, int bodyEnd, List<Assembler> children) {
        int from = nonBlankAfter(at, bodyEnd);
        int to = trimmed(from, bodyEnd);
        if (to > from) {
            tail(children).add(word(from, to));
        }
    }

    private boolean readContinuation(int[] line, List<Assembler> parts) {
        if (!continued(line)) {
            return false;
        }
        int at = line[0] + end;
        tail(parts).add(new Assembler.Continuation(randomId(), space(at), Markers.EMPTY,
                source.substring(at, at + 1)));
        cursor = at + 1;
        return true;
    }

    /**
     * Columns 73-80, put on whichever node ended the line, or answered for the statement itself when
     * the line wrote nothing else at all.
     */
    private @Nullable SequenceArea readSequenceArea(int[] line, List<Assembler> parts) {
        int from = nonBlankAfter(line[0] + end + 1, line[1]);
        if (from >= line[1]) {
            return null;
        }
        SequenceArea sequenceArea = new SequenceArea(randomId(), space(from), source.substring(from, line[1]));
        cursor = line[1];
        List<Assembler> tail = tail(parts);
        if (tail.isEmpty()) {
            return sequenceArea;
        }
        tail.set(tail.size() - 1, marked(tail.get(tail.size() - 1), sequenceArea));
        return null;
    }

    /**
     * The begin, end and continue columns an {@code ICTL} moves them to, which apply to the statements
     * after it and not to itself.
     */
    private void readColumns(Assembler.Instruction ictl) {
        begin = number(ictl.getOperandText(0), BEGIN_COLUMN);
        end = number(ictl.getOperandText(1), END_COLUMN);
        // Left off, the continue column is the begin column plus fifteen; written 0, there is no
        // continuation at all, which this reader takes as a statement that never carries on.
        continueAt = number(ictl.getOperandText(2), begin + 15);
    }

    private static int number(@Nullable String text, int otherwise) {
        try {
            return text == null || text.trim().isEmpty() ? otherwise : Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return otherwise;
        }
    }

    private void addPart(int from, int to) {
        Space prefix = space(from);
        if (openOperand == null) {
            openOperand = new ArrayList<>(1);
            openPrefix = prefix;
            prefix = Space.EMPTY;
        }
        openOperand.add(new Assembler.Word(randomId(), prefix, Markers.EMPTY, source.substring(from, to)));
        cursor = to;
    }

    private void closeOperand(List<Assembler> children) {
        if (openOperand != null) {
            children.add(new Assembler.Operand(randomId(), openPrefix, Markers.EMPTY, openOperand));
            openOperand = null;
            openPrefix = Space.EMPTY;
        }
        reset();
    }

    private void reset() {
        quoted = false;
        depth = 0;
    }

    /**
     * Where the next node goes: inside the operand still being built, or beside it.
     */
    private List<Assembler> tail(List<Assembler> children) {
        return openOperand == null ? children : openOperand;
    }

    private Assembler.Word word(int from, int to) {
        Space prefix = space(from);
        cursor = to;
        return new Assembler.Word(randomId(), prefix, Markers.EMPTY, source.substring(from, to));
    }

    private Space space(int to) {
        Space prefix = Space.build(source.substring(cursor, to));
        cursor = to;
        return prefix;
    }

    private static Assembler marked(Assembler node, SequenceArea sequenceArea) {
        if (node instanceof Assembler.Word) {
            Assembler.Word word = (Assembler.Word) node;
            return word.withMarkers(word.getMarkers().addIfAbsent(sequenceArea));
        }
        if (node instanceof Assembler.Continuation) {
            Assembler.Continuation continuation = (Assembler.Continuation) node;
            return continuation.withMarkers(continuation.getMarkers().addIfAbsent(sequenceArea));
        }
        Assembler.Operand operand = (Assembler.Operand) node;
        return operand.withMarkers(operand.getMarkers().addIfAbsent(sequenceArea));
    }

    private boolean continued(int[] line) {
        int at = line[0] + end;
        return continueAt > 0 && at < line[1] && source.charAt(at) != ' ';
    }

    private boolean isComment(int[] line) {
        int at = line[0] + begin - 1;
        return at < line[1] && (source.charAt(at) == '*' ||
                                source.startsWith(".*", at));
    }

    /**
     * Whether the statement area of the line says nothing. What is left is white space, unless somebody
     * wrote past the end column, which the assembler ignores and a reader still has to print back.
     */
    private boolean isBlank(int[] line) {
        return nonBlankAfter(line[0], body(line)) >= body(line);
    }

    private void readUnknown(int[] line) {
        int from = nonBlankAfter(line[0], line[1]);
        if (from >= line[1]) {
            return;
        }
        Space prefix = space(from);
        statements.add(new Assembler.Unknown(randomId(), prefix, Markers.EMPTY, word(from, line[1])));
    }

    /**
     * Whether the apostrophe at {@code at} is the attribute operator of {@code L'SYMBOL} rather than
     * the start of a literal. The attribute letters that are also constant types — {@code L} and
     * {@code D} — are told apart by what follows: an attribute names a symbol, a constant holds a
     * number.
     */
    private boolean isAttribute(int at) {
        if (at == 0 || "LISKNTDO".indexOf(Character.toUpperCase(source.charAt(at - 1))) < 0) {
            return false;
        }
        if (at >= 2 && isSymbolCharacter(source.charAt(at - 2))) {
            return false;
        }
        return at + 1 < source.length() && isSymbolStart(source.charAt(at + 1));
    }

    private static boolean isSymbolStart(char c) {
        return Character.isLetter(c) || "$#@_&".indexOf(c) >= 0;
    }

    private static boolean isSymbolCharacter(char c) {
        return Character.isLetterOrDigit(c) || "$#@_&".indexOf(c) >= 0;
    }

    /**
     * Where the statement text of a line stops, which is the end column or the end of the line.
     */
    private int body(int[] line) {
        return Math.min(line[1], line[0] + end);
    }

    private int blankAfter(int from, int to) {
        int at = from;
        while (at < to && source.charAt(at) != ' ') {
            at++;
        }
        return at;
    }

    private int nonBlankAfter(int from, int to) {
        int at = from;
        while (at < to && source.charAt(at) == ' ') {
            at++;
        }
        return at;
    }

    private int trimmed(int from, int to) {
        int at = to;
        while (at > from && source.charAt(at - 1) == ' ') {
            at--;
        }
        return at;
    }

}
