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
package org.openrewrite.mainframe.trait;

import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.mainframe.Members;
import org.openrewrite.text.PlainText;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.util.Collections.singletonList;

/**
 * A run book member, read for the component it documents and the components it mentions.
 * <p>
 * A run book is prose, and it is worth reading anyway because of how it is written: upper case in a
 * fixed set of labelled fields, naming every component by its member name, so that what the text says
 * resolves the way code does. The header line says which of the five shapes it is and what it is about;
 * the rest names the jobs, programs, files and copybooks around it.
 * <p>
 * {@link #getMentions()} is every name-shaped token of the member and not a resolution: which of them is a
 * component of the estate is answered by looking each one up among the members a repository holds, and
 * two of the fixture's are wrong on purpose so that a name index has something to be wrong about.
 * {@link #getFields()} is the narrower reading — the labels and the names written under each — which is
 * where the member says a name is a component rather than merely writing it down.
 */
@Value
public class RunBook implements Trait<PlainText> {

    /**
     * A run book is written in fixed columns: a label stands in the first thirteen of a line and its
     * value in the rest, and the header block writes a second field from column forty one.
     */
    private static final int VALUE_COLUMN = 13;
    private static final int SECOND_LABEL_COLUMN = 40;

    Cursor cursor;

    /**
     * Which of the five a member is, from its first word — which is where the mainframe itself keeps
     * it, since a member's name says only what it is about. A member whose first word says nothing is
     * typed by its extension.
     */
    public Shape getShape() {
        String first = firstWord();
        for (Shape shape : Shape.values()) {
            if (shape.name().equals(first)) {
                return shape;
            }
        }
        String name = getTree().getSourcePath().getFileName().toString().toLowerCase(Locale.ROOT);
        for (Shape shape : Shape.values()) {
            if (name.endsWith("." + shape.name().toLowerCase(Locale.ROOT))) {
                return shape;
            }
        }
        // The reader takes only those five extensions, so the loop above answers for anything it read.
        return Shape.DOCJOB;
    }

    /**
     * The component the member documents: the job, the program, or the data set the {@code FILE} line
     * names. A {@code DOCFICH} is named for the last qualifier of its data set where that is a name and
     * given one where it is a word, so the field is what a reference resolves through and the member
     * name is not.
     */
    public @Nullable Mention getSubject() {
        for (Field field : getFields()) {
            if (field.getLabel().equalsIgnoreCase(getShape().label) && !field.getNames().isEmpty()) {
                return field.getNames().get(0);
            }
        }
        return headerName();
    }

    /**
     * Every name the member writes, which is what a search for a member name finds in it.
     */
    public List<Mention> getMentions() {
        return Mention.in(getTree());
    }

    /**
     * The labelled fields of the member, in the order it writes them.
     * <p>
     * This is where a run book says a name is a component of the estate: {@code STEPS} names the
     * programs a job runs, {@code INPUT} and {@code OUTPUT} and {@code FILES} the data sets, {@code SEE
     * ALSO} the other run books, and so on down whichever labels the shop writes. {@link #getMentions()}
     * is every name-shaped token of the member, prose and all, so a finding about a name that resolves
     * to nothing is sound only over the fields somebody chose and never over the whole text.
     * <p>
     * Which labels there are is the shop's and not this reader's: the fixture writes fifty one of
     * them, and nothing here knows what one means.
     */
    public List<Field> getFields() {
        List<Field> fields = new ArrayList<>();
        List<String> lines = Members.lines(getTree().getText());
        // Line one is the header, which getShape and getSubject read.
        for (int i = 1; i < lines.size(); i++) {
            String text = lines.get(i);
            if (!opensField(text)) {
                continue;
            }
            int second = secondFieldAt(text);
            List<String> written = new ArrayList<>();
            if (text.length() > VALUE_COLUMN) {
                written.add(text.substring(VALUE_COLUMN, second < 0 ? text.length() : second));
            }
            int below = i + 1;
            while (below < lines.size() && continuesField(lines.get(below))) {
                written.add(lines.get(below));
                below++;
            }
            fields.add(field(text.substring(0, Math.min(VALUE_COLUMN, text.length())).trim(),
                    written, i + 1));
            if (second >= 0) {
                int gap = gapIn(text, second);
                fields.add(field(text.substring(second, gap).trim(),
                        singletonList(text.substring(gap).trim()), i + 1));
            }
            i = below - 1;
        }
        return fields;
    }

    private static Field field(String label, List<String> written, int line) {
        List<Mention> names = new ArrayList<>();
        for (int i = 0; i < written.size(); i++) {
            names.addAll(Mention.in(written.get(i), line + i));
        }
        return new Field(label, written, names, line);
    }

    /**
     * Whether the line opens a field, which a label does by standing in column one.
     */
    private static boolean opensField(String line) {
        char first = line.isEmpty() ? ' ' : line.charAt(0);
        return first >= 'A' && first <= 'Z';
    }

    private static boolean continuesField(String line) {
        return !line.isEmpty() && Character.isWhitespace(line.charAt(0)) && !line.trim().isEmpty();
    }

    /**
     * Where a second field begins on the line, or -1 for a line that writes only one. The header
     * block writes two — the job and the library it is in, the application and its owner — and
     * nothing else does, so a second label has to stand in its column after a gap and be parted from
     * its own value by another.
     */
    private static int secondFieldAt(String line) {
        return line.length() > SECOND_LABEL_COLUMN &&
               line.charAt(SECOND_LABEL_COLUMN) != ' ' &&
               line.charAt(SECOND_LABEL_COLUMN - 1) == ' ' &&
               line.charAt(SECOND_LABEL_COLUMN - 2) == ' ' &&
               gapIn(line, SECOND_LABEL_COLUMN) >= 0 ? SECOND_LABEL_COLUMN : -1;
    }

    /**
     * Where the blanks that part a label from its value begin at or after {@code from}, or -1 where
     * what is left is one run of words with nothing after it.
     */
    private static int gapIn(String line, int from) {
        for (int at = from; at + 1 < line.length(); at++) {
            if (line.charAt(at) == ' ' && line.charAt(at + 1) == ' ') {
                return line.substring(at).trim().isEmpty() ? -1 : at;
            }
        }
        return -1;
    }

    /**
     * The name on the header line, which every shape writes after its own word.
     */
    private @Nullable Mention headerName() {
        List<String> lines = Members.lines(getTree().getText());
        for (int i = 0; i < lines.size(); i++) {
            String[] words = lines.get(i).trim().split("\\s+");
            if (words.length >= 2 && !words[0].isEmpty()) {
                return new Mention(words[1], i + 1);
            }
        }
        return null;
    }

    private String firstWord() {
        for (String line : Members.lines(getTree().getText())) {
            String text = line.trim();
            if (!text.isEmpty()) {
                return text.split("\\s+")[0].toUpperCase(Locale.ROOT);
            }
        }
        return "";
    }

    /**
     * The name of the member itself, which is the name every other member refers to it by.
     */
    public String getName() {
        Path path = getTree().getSourcePath().getFileName();
        String name = path == null ? "" : path.toString();
        return name.contains(".") ? name.substring(0, name.indexOf('.')) : name;
    }

    /**
     * A labelled field of a run book: what the member says under one heading.
     */
    @Value
    public static class Field {

        /**
         * The label as written, without the blanks that pad its column out.
         */
        String label;

        /**
         * The value as written: the rest of the label's own line, then every line indented under it.
         */
        List<String> lines;

        /**
         * The names written under the label, in the order and as often as they are written. A field
         * writes one twice where it means two things, as a {@code FILES} line does with the DD name
         * and the layout it is read by.
         */
        List<Mention> names;

        /**
         * The one-based line the label was written on.
         */
        int line;

        @Override
        public String toString() {
            return label + " " + String.join(" ", lines).trim();
        }
    }

    /**
     * The five shapes a documentation library holds, one per kind of thing an application is made of,
     * each with the field its subject is written on.
     */
    public enum Shape {
        DOCJOB("JOB"),
        DOCPGM("PROGRAM"),
        DOCFICH("FILE"),
        DOCAPPL("APPLICATION"),
        DOCOPER("PROCEDURE");

        public final String label;

        Shape(String label) {
            this.label = label;
        }
    }

    public static class Matcher extends SimpleTraitMatcher<RunBook> {

        @Override
        protected @Nullable RunBook test(Cursor cursor) {
            return cursor.getValue() instanceof PlainText &&
                   Members.kindOf((PlainText) cursor.getValue()) == Members.Kind.DOCUMENT ?
                    new RunBook(cursor) : null;
        }
    }

    @Override
    public String toString() {
        return getShape() + " " + getName();
    }
}
