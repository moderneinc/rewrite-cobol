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
package org.openrewrite.assembler.tree;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.With;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Checksum;
import org.openrewrite.Cursor;
import org.openrewrite.FileAttributes;
import org.openrewrite.PrintOutputCapture;
import org.openrewrite.SourceFile;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.assembler.AssemblerVisitor;
import org.openrewrite.assembler.internal.AssemblerPrinter;
import org.openrewrite.marker.Markers;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public interface Assembler extends Tree {

    @SuppressWarnings("unchecked")
    @Override
    default <R extends Tree, P> R accept(TreeVisitor<R, P> v, P p) {
        return (R) acceptAssembler(v.adapt(AssemblerVisitor.class), p);
    }

    default <P> @Nullable Assembler acceptAssembler(AssemblerVisitor<P> v, P p) {
        return v.defaultValue(this, p);
    }

    @Override
    default <P> boolean isAcceptable(TreeVisitor<?, P> v, P p) {
        return v.isAdaptableTo(AssemblerVisitor.class);
    }

    Space getPrefix();

    <P extends Assembler> P withPrefix(Space prefix);

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class CompilationUnit implements Assembler, SourceFile {

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

        List<Statement> statements;
        Space eof;

        @Override
        public <P> Assembler acceptAssembler(AssemblerVisitor<P> v, P p) {
            return v.visitCompilationUnit(this, p);
        }

        @Override
        public <P> TreeVisitor<?, PrintOutputCapture<P>> printer(Cursor cursor) {
            return new AssemblerPrinter<>();
        }
    }

    /**
     * A statement that does something: an optional name field beginning in column 1, an operation, and
     * everything written after it, over as many lines as column 72 carried it.
     * <p>
     * HLASM calls all three kinds of these an instruction — a machine instruction, an assembler
     * instruction such as {@code DSECT} or {@code DC}, and a macro instruction, which is an invocation
     * of a macro. Nothing in the source tells them apart; only a table of the mnemonics and the
     * directives does, and that reading is a trait's.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Instruction implements Assembler, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        /**
         * The name field, which begins in column 1. Null for a statement that writes none, which is
         * most of them; the ones that do are labelling a control section, a constant or a branch target.
         */
        @Nullable
        Word name;

        Word operation;

        /**
         * Everything after the operation, in source order: the {@link Operand}s of the operand field,
         * a {@link Word} for the remarks after them, and a {@link Continuation} for the character in
         * column 72 carrying the statement onto the next line. Keeping them in one ordered list is what
         * lets the statement print back exactly, however it was laid out.
         */
        List<Assembler> operands;

        @Override
        public <P> Assembler acceptAssembler(AssemblerVisitor<P> v, P p) {
            return v.visitInstruction(this, p);
        }

        public boolean isOperation(String operation) {
            return this.operation.getText().equalsIgnoreCase(operation);
        }

        /**
         * The name field, or empty for a statement with none.
         */
        public String getSimpleName() {
            return name == null ? "" : name.getText();
        }

        public List<Operand> getParameters() {
            List<Operand> parameters = new ArrayList<>(operands.size());
            for (Assembler operand : operands) {
                if (operand instanceof Operand) {
                    parameters.add((Operand) operand);
                }
            }
            return parameters;
        }

        /**
         * The text of each operand, in order, without the commas that separate them.
         */
        public List<String> getOperandTexts() {
            List<Operand> parameters = getParameters();
            List<String> texts = new ArrayList<>(parameters.size());
            for (Operand operand : parameters) {
                texts.add(operand.getText());
            }
            return texts;
        }

        /**
         * The nth operand's text, or null where the statement wrote fewer than that.
         */
        public @Nullable String getOperandText(int index) {
            List<Operand> parameters = getParameters();
            return index >= 0 && index < parameters.size() ? parameters.get(index).getText() : null;
        }

        /**
         * The operand written {@code KEYWORD=value}, or null. A macro takes each keyword once, so the
         * first is the only one.
         */
        public @Nullable Operand getParameter(String keyword) {
            for (Operand operand : getParameters()) {
                if (keyword.equalsIgnoreCase(operand.getKeyword())) {
                    return operand;
                }
            }
            return null;
        }

        public @Nullable String getParameterValue(String keyword) {
            Operand operand = getParameter(keyword);
            return operand == null ? null : operand.getValue();
        }
    }

    /**
     * A comment statement: a {@code *} in column 1, or the {@code .*} of a macro definition. It carries
     * on past column 72 like any other statement, which is how a commented out macro invocation keeps
     * its continuation lines from being read as statements of their own.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Comment implements Assembler, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        /**
         * The {@link Word} of each line and the {@link Continuation} that carried it to the next.
         */
        List<Assembler> parts;

        @Override
        public <P> Assembler acceptAssembler(AssemblerVisitor<P> v, P p) {
            return v.visitComment(this, p);
        }

        public String getText() {
            return textOf(parts);
        }
    }

    /**
     * A line with nothing in the statement area and something past column 72, which the assembler reads
     * as blank and a reader has to print back all the same.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Unknown implements Assembler, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;
        Word word;

        @Override
        public <P> Assembler acceptAssembler(AssemblerVisitor<P> v, P p) {
            return v.visitUnknown(this, p);
        }
    }

    /**
     * One operand of the operand field, up to the comma that ends it.
     * <p>
     * The parts are a list because an operand too long for column 71 carries on at column 16 of the
     * next line, with the continuation character standing between the two halves.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Operand implements Assembler {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Assembler> parts;

        @Override
        public <P> Assembler acceptAssembler(AssemblerVisitor<P> v, P p) {
            return v.visitOperand(this, p);
        }

        /**
         * What the operand says, without the comma that separates it from the next. Printing walks the
         * parts instead, so the punctuation is kept there and left out here.
         */
        public String getText() {
            String text = textOf(parts);
            return text.endsWith(",") ? text.substring(0, text.length() - 1) : text;
        }

        /**
         * The keyword of an operand written {@code KEYWORD=value}, or null for a positional one.
         */
        public @Nullable String getKeyword() {
            int equals = equalsSign(getText());
            return equals < 0 ? null : getText().substring(0, equals);
        }

        /**
         * The value of a keyword operand, or the whole of a positional one.
         */
        public String getValue() {
            String text = getText();
            int equals = equalsSign(text);
            return equals < 0 ? text : text.substring(equals + 1);
        }

        public boolean isKeyword(String keyword) {
            return keyword.equalsIgnoreCase(getKeyword());
        }

        /**
         * The members of a parenthesised value, or the single value written without parentheses. A
         * nested list stays whole, so {@code (A10GHN,(R11),A10ROOT)} yields the register as
         * {@code (R11)} and reading inside it is a second call.
         */
        public List<String> getMembers() {
            return membersOf(getValue());
        }

        /**
         * Where the {@code =} of a keyword operand is. Only one written outside quotes and parentheses
         * counts: {@code MF=(E,LIST)} is a keyword and {@code =C'CLM'} is a literal.
         */
        private static int equalsSign(String text) {
            int depth = 0;
            boolean quoted = false;
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '\'') {
                    quoted = !quoted;
                } else if (quoted) {
                    continue;
                } else if (c == '(') {
                    depth++;
                } else if (c == ')') {
                    depth--;
                } else if (c == '=' && depth == 0) {
                    return i > 0 && isName(text.substring(0, i)) ? i : -1;
                }
            }
            return -1;
        }

        private static boolean isName(String text) {
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (!Character.isLetterOrDigit(c) && "&$#@_".indexOf(c) < 0) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * The non-blank in column 72 saying that the statement carries on at column 16 of the next line.
     * Any character will do; the corpus writes {@code X}, {@code C} and {@code +} interchangeably.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Continuation implements Assembler {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;
        String text;

        @Override
        public <P> Assembler acceptAssembler(AssemblerVisitor<P> v, P p) {
            return v.visitContinuation(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Word implements Assembler {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;
        String text;

        @Override
        public <P> Assembler acceptAssembler(AssemblerVisitor<P> v, P p) {
            return v.visitWord(this, p);
        }

        public String getUpperText() {
            return text.toUpperCase(Locale.ROOT);
        }
    }

    /**
     * Splits a parenthesised list at the commas written outside quotes and nested parentheses.
     */
    static List<String> membersOf(String value) {
        String text = value.trim();
        if (text.startsWith("(") && text.endsWith(")")) {
            text = text.substring(1, text.length() - 1);
        }
        if (text.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> members = new ArrayList<>();
        int depth = 0;
        int start = 0;
        boolean quoted = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\'') {
                quoted = !quoted;
            } else if (quoted) {
                continue;
            } else if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
            } else if (c == ',' && depth == 0) {
                members.add(text.substring(start, i).trim());
                start = i + 1;
            }
        }
        members.add(text.substring(start).trim());
        return members;
    }

    /**
     * The words of a run of parts joined, leaving out the continuation characters that a line break put
     * between them.
     */
    static String textOf(List<Assembler> parts) {
        StringBuilder text = new StringBuilder();
        for (Assembler part : parts) {
            if (part instanceof Word) {
                text.append(((Word) part).getText());
            }
        }
        return text.toString();
    }
}
