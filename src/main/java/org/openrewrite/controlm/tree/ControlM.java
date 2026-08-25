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
package org.openrewrite.controlm.tree;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.jspecify.annotations.Nullable;
import org.openrewrite.*;
import org.openrewrite.controlm.ControlMVisitor;
import org.openrewrite.controlm.internal.ControlMPrinter;
import org.openrewrite.marker.Markers;

import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public interface ControlM extends Tree {

    @SuppressWarnings("unchecked")
    @Override
    default <R extends Tree, P> R accept(TreeVisitor<R, P> v, P p) {
        return (R) acceptControlM(v.adapt(ControlMVisitor.class), p);
    }

	default <P> @Nullable ControlM acceptControlM(ControlMVisitor<P> v, P p) {
        return v.defaultValue(this, p);
    }

    @Override
    default <P> boolean isAcceptable(TreeVisitor<?, P> v, P p) {
        return v.isAdaptableTo(ControlMVisitor.class);
    }

    Space getPrefix();

    <P extends ControlM> P withPrefix(Space prefix);


    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class CompilationUnit implements ControlM, SourceFile {

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

        List<Section> sections;
        Space eof;

        @Override
        public <P> ControlM acceptControlM(ControlMVisitor<P> v, P p) {
            return v.visitCompilationUnit(this, p);
        }

        @Override
        public <P> TreeVisitor<?, PrintOutputCapture<P>> printer(Cursor cursor) {
            return new ControlMPrinter<>();
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class DefinitionSection implements ControlM, Section {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;
        List<ControlM> lines;

        @Override
        public <P> ControlM acceptControlM(ControlMVisitor<P> v, P p) {
            return v.visitDefinitionSection(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Description implements ControlM {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;
        String word;

        List<Word> description;

        @Override
        public <P> ControlM acceptControlM(ControlMVisitor<P> v, P p) {
            return v.visitDescription(this, p);
        }
    }

    @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @RequiredArgsConstructor
    class SetVar implements ControlM {
        @Nullable
        @NonFinal
        transient WeakReference<SetVar.Padding> padding;

        @With
        @EqualsAndHashCode.Include
        @Getter
        UUID id;

        @With
        @Getter
        Space prefix;

        @With
        @Getter
        Markers markers;

        @With
        @Getter
        String setVar;

        @With
        @Getter
        Word varName;

        @With
        @Getter
        ControlMLeftPadded<Word> value;

        @Override
        public <P> ControlM acceptControlM(ControlMVisitor<P> v, P p) {
            return v.visitSetVar(this, p);
        }

        public Padding getPadding() {
            Padding p;
            if (this.padding == null) {
                p = new Padding(this);
                this.padding = new WeakReference<>(p);
            } else {
                p = this.padding.get();
                if (p == null || p.t != this) {
                    p = new Padding(this);
                    this.padding = new WeakReference<>(p);
                }
            }
            return p;
        }

        @RequiredArgsConstructor
        public static class Padding {
            private final SetVar t;

			public @Nullable ControlMLeftPadded<Word> getValue() {
                return t.value;
            }

            public SetVar withValue(@Nullable ControlMLeftPadded<Word> value) {
                return t.value == value ? t : new SetVar(t.id, t.prefix, t.markers, t.setVar, t.varName, value);
            }
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class ScheduleSection implements ControlM, Section {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;
        List<ControlM> lines;

        @Override
        public <P> ControlM acceptControlM(ControlMVisitor<P> v, P p) {
            return v.visitScheduleSection(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class InputSection implements ControlM, Section {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;
        List<ControlM> inputNames;
        List<ControlM> lines;

        @Override
        public <P> ControlM acceptControlM(ControlMVisitor<P> v, P p) {
            return v.visitInputSection(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Input implements ControlM {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        @Nullable
        Word in;

        List<ControlM> input;

        @Override
        public <P> ControlM acceptControlM(ControlMVisitor<P> v, P p) {
            return v.visitInput(this, p);
        }

        @Value
        @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
        @With
        public static class NameParameter implements ControlM {

            @EqualsAndHashCode.Include
            UUID id;

            Space prefix;
            Markers markers;

            @Nullable
            Word name;

            @Nullable
            Parameter date;

            @Override
            public <P> ControlM acceptControlM(ControlMVisitor<P> v, P p) {
                return v.visitInputNameParameter(this, p);
            }
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class OutputSection implements ControlM, Section {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;
        List<ControlM> outputNames;
        List<ControlM> lines;

        @Override
        public <P> ControlM acceptControlM(ControlMVisitor<P> v, P p) {
            return v.visitOutputSection(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Output implements ControlM {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        @Nullable
        Word out;

        List<ControlM> output;

        @Override
        public <P> ControlM acceptControlM(ControlMVisitor<P> v, P p) {
            return v.visitOutput(this, p);
        }

        @Value
        @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
        @With
        public static class NameParameter implements ControlM {

            @EqualsAndHashCode.Include
            UUID id;

            Space prefix;
            Markers markers;

            @Nullable
            Word name;

            @Nullable
            Parameter date;

            @Override
            public <P> ControlM acceptControlM(ControlMVisitor<P> v, P p) {
                return v.visitOutputNameParameter(this, p);
            }
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class ApplicationFormSection implements ControlM, Section {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;
        List<ControlM> lines;

        @Override
        public <P> ControlM acceptControlM(ControlMVisitor<P> v, P p) {
            return v.visitApplicationFormSection(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Line implements ControlM {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;
        List<ControlM> parameters;

        @Override
        public <P> ControlM acceptControlM(ControlMVisitor<P> v, P p) {
            return v.visitLine(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Parameter implements ControlM {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;
        String option;

        @Nullable
        Word value;

        @Override
        public <P> ControlM acceptControlM(ControlMVisitor<P> v, P p) {
            return v.visitParameter(this, p);
        }
    }

    /**
     * One element of an {@code exportdeftable} or {@code exportdefcal} file: the table, a job, a
     * condition, a calendar. The XML dialect writes as elements and attributes what the panel dialect
     * writes as sections of lines, so the two meet in the traits rather than in the tree.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Element implements ControlM, Section {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;
        String name;
        List<Attribute> attributes;
        Space beforeTagEnd;

        /**
         * Null for an element written closed, {@code <INCOND ... />}, which is not the same as one
         * written with an empty body.
         */
        @Nullable
        List<ControlM> elements;

        Space beforeEndTag;

        public boolean isName(String name) {
            return this.name.equalsIgnoreCase(name);
        }

        public @Nullable Attribute getAttribute(String name) {
            for (Attribute attribute : attributes) {
                if (attribute.getName().equalsIgnoreCase(name)) {
                    return attribute;
                }
            }
            return null;
        }

        /**
         * The value of an attribute, or null when the element does not write it. An attribute written
         * empty answers the empty string, which a Control-M export uses to mean "not set".
         */
        public @Nullable String getAttributeText(String name) {
            Attribute attribute = getAttribute(name);
            return attribute == null ? null : attribute.getValueText();
        }

        public List<Element> getElements(String name) {
            List<Element> matching = new ArrayList<>();
            if (elements != null) {
                for (ControlM element : elements) {
                    if (element instanceof Element && ((Element) element).isName(name)) {
                        matching.add((Element) element);
                    }
                }
            }
            return matching;
        }

        @Override
        public <P> ControlM acceptControlM(ControlMVisitor<P> v, P p) {
            return v.visitElement(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Attribute implements ControlM {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;
        String name;
        Space beforeEquals;

        /**
         * The value as written, quotes and entity references included, so that it prints back as it
         * came. {@link #getValueText()} is what it says.
         */
        Word value;

        public String getValueText() {
            String text = value.getText();
            if (text.length() > 1 && (text.charAt(0) == '"' || text.charAt(0) == '\'') &&
                text.charAt(text.length() - 1) == text.charAt(0)) {
                text = text.substring(1, text.length() - 1);
            }
            return text.indexOf('&') < 0 ? text : text
                    .replace("&lt;", "<")
                    .replace("&gt;", ">")
                    .replace("&quot;", "\"")
                    .replace("&apos;", "'")
                    .replace("&amp;", "&");
        }

        @Override
        public <P> ControlM acceptControlM(ControlMVisitor<P> v, P p) {
            return v.visitAttribute(this, p);
        }
    }

    /**
     * Markup that defines nothing: the XML declaration an export opens with, a doctype, a comment.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Directive implements ControlM, Section {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;
        String text;

        @Override
        public <P> ControlM acceptControlM(ControlMVisitor<P> v, P p) {
            return v.visitDirective(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Word implements ControlM {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;
        String text;

        @Override
        public <P> ControlM acceptControlM(ControlMVisitor<P> v, P p) {
            return v.visitWord(this, p);
        }
    }
}
