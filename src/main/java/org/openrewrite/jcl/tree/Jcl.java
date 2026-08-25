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
package org.openrewrite.jcl.tree;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.With;
import org.jspecify.annotations.Nullable;
import org.openrewrite.*;
import org.openrewrite.jcl.JclVisitor;
import org.openrewrite.jcl.internal.JclPrinter;
import org.openrewrite.marker.Markers;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public interface Jcl extends Tree {

    @SuppressWarnings("unchecked")
    @Override
    default <R extends Tree, P> R accept(TreeVisitor<R, P> v, P p) {
        return (R) acceptJcl(v.adapt(JclVisitor.class), p);
    }

	default <P> @Nullable Jcl acceptJcl(JclVisitor<P> v, P p) {
        return v.defaultValue(this, p);
    }

    @Override
    default <P> boolean isAcceptable(TreeVisitor<?, P> v, P p) {
        return v.isAdaptableTo(JclVisitor.class);
    }

    Space getPrefix();

    <P extends Jcl> P withPrefix(Space prefix);


    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class CompilationUnit implements Jcl, SourceFile {

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
        public <P> Jcl acceptJcl(JclVisitor<P> v, P p) {
            return v.visitCompilationUnit(this, p);
        }

        @Override
        public <P> TreeVisitor<?, PrintOutputCapture<P>> printer(Cursor cursor) {
            return new JclPrinter<>();
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Comment implements Jcl, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;
        Word word;

        @Override
        public <P> Jcl acceptJcl(JclVisitor<P> v, P p) {
            return v.visitComment(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class ControlM implements Jcl, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;
        Word word;

        @Override
        public <P> Jcl acceptJcl(JclVisitor<P> v, P p) {
            return v.visitControlM(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class DataDefinitionStream implements Jcl, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;
        Word word;

        @Override
        public <P> Jcl acceptJcl(JclVisitor<P> v, P p) {
            return v.visitDataDefinitionStream(this, p);
        }
    }

    /**
     * A job control statement: a name field, an operation, and the operands belonging to it, however
     * many lines they are written over.
     * <p>
     * All of {@code JOB}, {@code EXEC}, {@code DD}, {@code PROC} and the rest share this shape, so
     * they share a node and are told apart by {@link #getOperation()}. Twelve near identical classes
     * would say nothing this does not.
     * <p>
     * Named for what IBM calls it rather than simply {@code Statement}, because {@link Statement} is
     * the interface every entry in a job stream implements — comments, JES2 and JES3 control
     * statements and in-stream data among them, all of which are statements too.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class JobControlStatement implements Jcl, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        /**
         * The name field as written, including its leading slashes: {@code //ACCTDD}, or {@code //}
         * for a statement with no name of its own — an unnamed step, or a DD continuing a
         * concatenation.
         */
        Word name;

        /**
         * The operation. Null for a name field with nothing after it.
         */
        @Nullable
        Word operation;

        /**
         * Everything after the operation, in source order: the {@link Parameter}s of the operand
         * field, the words of any comment field, the {@code //} beginning each continuation line,
         * and any {@link Comment} card written between two of its lines. Keeping them in one
         * ordered list is what lets the statement print back exactly, however it was laid out.
         */
        List<Jcl> operands;

        @Override
        public <P> Jcl acceptJcl(JclVisitor<P> v, P p) {
            return v.visitJobControlStatement(this, p);
        }

        public boolean isOperation(String operation) {
            return this.operation != null && this.operation.getText().equalsIgnoreCase(operation);
        }

        /**
         * The name field without its slashes, and without the procedure step qualifier of an
         * override like {@code //STEP1.SORTIN}.
         */
        public String getSimpleName() {
            String text = name.getText();
            String unqualified = text.startsWith("//") ? text.substring(2) : text;
            int dot = unqualified.indexOf('.');
            return dot < 0 ? unqualified : unqualified.substring(dot + 1);
        }

        public List<Parameter> getParameters() {
            List<Parameter> parameters = new java.util.ArrayList<>(operands.size());
            for (Jcl operand : operands) {
                if (operand instanceof Parameter) {
                    parameters.add((Parameter) operand);
                }
            }
            return parameters;
        }

        /**
         * The first parameter, when it was written without a keyword: the procedure name on
         * {@code EXEC MYPROC}, the accounting information on a JOB card, the {@code *} of a
         * {@code DD *}. Null when the statement leads with a keyword.
         * <p>
         * Which positional field it is depends on the operation, and only the first one can be told
         * from the others by position alone, so only the first is offered here.
         */
        public Jcl.@Nullable PositionalParameter getPositionalParameter() {
            List<Parameter> parameters = getParameters();
            return parameters.isEmpty() || !(parameters.get(0) instanceof PositionalParameter) ? null :
                    (PositionalParameter) parameters.get(0);
        }

        /**
         * The keyword parameter of this name, or null. Keyword parameters are unique within a
         * statement, so the first is the only one.
         */
        public Jcl.@Nullable KeywordParameter getParameter(String keyword) {
            for (Parameter parameter : getParameters()) {
                if (parameter instanceof KeywordParameter &&
                    ((KeywordParameter) parameter).getKeyword().getText().equalsIgnoreCase(keyword)) {
                    return (KeywordParameter) parameter;
                }
            }
            return null;
        }
    }

    /**
     * The body of a procedure or an INCLUDE group, resolved and placed after the statement that
     * named it. Nothing here was written in this member, so the printer leaves it out and the
     * source still prints back byte for byte; the expanded listing is printed from it instead.
     * <p>
     * A step written inside one is a step the job runs, and the DD statements under it already have
     * the caller's overrides applied. Nested expansion — a procedure that includes a member, or
     * runs another procedure — is an expansion inside this one.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Expansion implements Jcl, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        /**
         * The member expanded, e.g. {@code CLMBATCH} or {@code @JOBCARD}.
         */
        String memberName;

        Kind kind;

        List<Statement> statements;

        @Override
        public <P> Jcl acceptJcl(JclVisitor<P> v, P p) {
            return v.visitExpansion(this, p);
        }

        public enum Kind {
            PROCEDURE,
            INCLUDE
        }
    }

    /**
     * The delimiter statement, which ends in-stream data: {@code /*}, or whatever the DD's
     * {@code DLM} said instead.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Delimiter implements Jcl, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;
        Word delimiter;

        /**
         * A delimiter statement may carry a comment after it.
         */
        List<Word> comment;

        @Override
        public <P> Jcl acceptJcl(JclVisitor<P> v, P p) {
            return v.visitDelimiter(this, p);
        }
    }

    /**
     * The null statement, {@code //} alone, which marks the end of a job.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class NullStatement implements Jcl, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;
        Word marker;

        @Override
        public <P> Jcl acceptJcl(JclVisitor<P> v, P p) {
            return v.visitNullStatement(this, p);
        }
    }

    /**
     * A parameter written as {@code KEYWORD=value}.
     * <p>
     * The value is a list of words rather than one, because the lexer breaks a quoted string out on
     * its own: {@code ORDER=('SYS1.PROCLIB')} arrives as {@code ORDER=(}, {@code 'SYS1.PROCLIB'} and
     * {@code )}. Reading the value back means joining them, and they are kept apart so that printing
     * puts them back exactly as they were.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class KeywordParameter implements Jcl, Parameter {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        /**
         * The keyword, without the {@code =}: {@code DSN}, {@code DISP}, {@code PGM}.
         */
        Word keyword;

        List<Word> value;

        @Override
        public <P> Jcl acceptJcl(JclVisitor<P> v, P p) {
            return v.visitKeywordParameter(this, p);
        }

        /**
         * What the parameter means: the value without the {@code =} that introduces it or the comma
         * that separates it from the next. Printing walks the words instead, so the punctuation is
         * kept there and left out here.
         */
        public String getValueText() {
            StringBuilder text = new StringBuilder();
            for (Word word : value) {
                text.append(word.getText());
            }
            return trimSeparators(text.toString().substring(1));
        }

        /**
         * This parameter with a different value, keeping the {@code =} that introduces it and the
         * comma that separates it from the next. Changing the value directly is easy to get wrong,
         * because the separators live in the same words.
         */
        public KeywordParameter withValueText(String text) {
            StringBuilder raw = new StringBuilder();
            for (Word word : value) {
                raw.append(word.getText());
            }
            String separator = raw.toString().endsWith(",") ? "," : "";
            Word last = value.get(value.size() - 1);
            return withValue(java.util.Collections.singletonList(
                    new Word(last.getId(), Space.EMPTY, last.getMarkers(), "=" + text + separator)));
        }
    }

    /**
     * A parameter with no keyword: {@code *} and {@code DATA} on a DD, {@code DUMMY}, the accounting
     * information on a JOB card, the procedure name on {@code EXEC MYPROC}.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class PositionalParameter implements Jcl, Parameter {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Word> value;

        @Override
        public <P> Jcl acceptJcl(JclVisitor<P> v, P p) {
            return v.visitPositionalParameter(this, p);
        }

        /**
         * The value without the comma separating it from the next parameter.
         */
        public String getValueText() {
            StringBuilder text = new StringBuilder();
            for (Word word : value) {
                text.append(word.getText());
            }
            return trimSeparators(text.toString());
        }
    }

    static String trimSeparators(String text) {
        return text.endsWith(",") ? text.substring(0, text.length() - 1) : text;
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Jes2 implements Jcl, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;
        Word word;

        @Override
        public <P> Jcl acceptJcl(JclVisitor<P> v, P p) {
            return v.visitJes2(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Jes3 implements Jcl, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;
        Word word;

        @Override
        public <P> Jcl acceptJcl(JclVisitor<P> v, P p) {
            return v.visitJes3(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Unknown implements Jcl, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;
        Word word;

        @Override
        public <P> Jcl acceptJcl(JclVisitor<P> v, P p) {
            return v.visitUnknown(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Word implements Jcl {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;
        String text;

        @Override
        public <P> Jcl acceptJcl(JclVisitor<P> v, P p) {
            return v.visitWord(this, p);
        }
    }
}
