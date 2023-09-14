/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.controlm.tree;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.openrewrite.*;
import org.openrewrite.controlm.ControlMVisitor;
import org.openrewrite.controlm.internal.ControlMPrinter;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.marker.Markers;

import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public interface ControlM extends Tree {

    @SuppressWarnings("unchecked")
    @Override
    default <R extends Tree, P> R accept(TreeVisitor<R, P> v, P p) {
        return (R) acceptControlM(v.adapt(ControlMVisitor.class), p);
    }

    @Nullable
    default <P> ControlM acceptControlM(ControlMVisitor<P> v, P p) {
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
        List<Line> lines;

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

        @Nullable
        Word description;

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

        @Nullable
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

            @Nullable
            public ControlMLeftPadded<Word> getValue() {
                return t.value;
            }

            public SetVar withValue(@Nullable ControlMLeftPadded<Word> value) {
                return t.value == value ? t : new SetVar(t.id, t.prefix, t.markers, t.setVar, value);
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
        List<Line> lines;

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
        List<Line> lines;

        @Override
        public <P> ControlM acceptControlM(ControlMVisitor<P> v, P p) {
            return v.visitInputSection(this, p);
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
        List<Line> lines;

        @Override
        public <P> ControlM acceptControlM(ControlMVisitor<P> v, P p) {
            return v.visitOutputSection(this, p);
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
        List<Line> lines;

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
