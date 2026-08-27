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
package org.openrewrite.textmember.trait;

import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.textmember.tree.TextMember;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

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
 */
@Value
public class RunBook implements Trait<TextMember.CompilationUnit> {

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
        Mention subject = field(getShape().label);
        return subject == null ? headerName() : subject;
    }

    /**
     * Every name the member writes, which is what a search for a member name finds in it.
     */
    public List<Mention> getMentions() {
        return Mention.in(getTree());
    }

    /**
     * The value of a labelled field: the second word of the first line the label opens.
     */
    private @Nullable Mention field(String label) {
        List<TextMember.Line> lines = getTree().getLines();
        for (int i = 0; i < lines.size(); i++) {
            String[] words = lines.get(i).getText().trim().split("\\s+");
            if (words.length >= 2 && label.equalsIgnoreCase(words[0])) {
                return new Mention(words[1], i + 1);
            }
        }
        return null;
    }

    /**
     * The name on the header line, which every shape writes after its own word.
     */
    private @Nullable Mention headerName() {
        List<TextMember.Line> lines = getTree().getLines();
        for (int i = 0; i < lines.size(); i++) {
            String[] words = lines.get(i).getText().trim().split("\\s+");
            if (words.length >= 2 && !words[0].isEmpty()) {
                return new Mention(words[1], i + 1);
            }
        }
        return null;
    }

    private String firstWord() {
        for (TextMember.Line line : getTree().getLines()) {
            String text = line.getText().trim();
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
            return cursor.getValue() instanceof TextMember.CompilationUnit &&
                   ((TextMember.CompilationUnit) cursor.getValue()).getKind() == TextMember.Kind.DOCUMENT ?
                    new RunBook(cursor) : null;
        }
    }

    @Override
    public String toString() {
        return getShape() + " " + getName();
    }
}
