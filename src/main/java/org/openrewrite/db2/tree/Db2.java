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
package org.openrewrite.db2.tree;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.With;
import org.jspecify.annotations.Nullable;
import org.openrewrite.*;
import org.openrewrite.db2.Db2Visitor;
import org.openrewrite.db2.internal.Db2Printer;
import org.openrewrite.marker.Markers;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static java.util.Collections.emptyList;

/**
 * DB2 for z/OS DDL, of which only some statements are modelled.
 * <p>
 * {@link CreateTable}, {@link CreateIndex} and {@link AlterTable} are modelled because they say what
 * the estate's tables and columns are and which of them point at which. Every other statement — a
 * {@code GRANT}, a {@code COMMIT}, a {@code CREATE TABLESPACE} — is an {@link Unknown} holding the
 * words it was written with, so a file prints back unchanged without DB2's DDL being modelled whole.
 */
public interface Db2 extends Tree {

    @SuppressWarnings("unchecked")
    @Override
    default <R extends Tree, P> R accept(TreeVisitor<R, P> v, P p) {
        return (R) acceptDb2(v.adapt(Db2Visitor.class), p);
    }

    default <P> @Nullable Db2 acceptDb2(Db2Visitor<P> v, P p) {
        return v.defaultValue(this, p);
    }

    @Override
    default <P> boolean isAcceptable(TreeVisitor<?, P> v, P p) {
        return v.isAdaptableTo(Db2Visitor.class);
    }

    Space getPrefix();

    <P extends Db2> P withPrefix(Space prefix);

    /**
     * A file of DDL, or the DDL of one {@code SYSIN} stream. Not a compilation unit: nothing here is
     * compiled, and DDL is what the domain calls it.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Ddl implements Db2, SourceFile {

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
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitDdl(this, p);
        }

        @Override
        public <P> TreeVisitor<?, PrintOutputCapture<P>> printer(Cursor cursor) {
            return new Db2Printer<>();
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class CreateTable implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        /**
         * {@code CREATE TABLE}, as written — two words, however much white space is between them.
         */
        List<Word> keywords;

        Name name;

        Word lParen;

        /**
         * The table body in source order: {@link ColumnDefinition}s, {@link Constraint}s, and the
         * commas between them. Keeping the punctuation here is what lets a column be added or
         * removed without reflowing the rest.
         */
        List<Db2> elements;

        Word rParen;

        /**
         * What follows the body. Mostly storage attributes nothing joins on and so kept as words;
         * the one exception is the {@link Name} after {@code IN}, which is the tablespace.
         */
        List<Db2> options;

        @Nullable
        Word end;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitCreateTable(this, p);
        }

        public List<ColumnDefinition> getColumns() {
            return elementsOf(elements, ColumnDefinition.class);
        }

        public List<Constraint> getConstraints() {
            return elementsOf(elements, Constraint.class);
        }

        /**
         * The tablespace the table is created in, or null when the DDL leaves it to DB2. The grammar
         * only builds a {@link Name} among the options for {@code IN}, so this is structural rather
         * than a search for the keyword.
         */
        public @Nullable Name getTablespace() {
            for (Db2 option : options) {
                if (option instanceof Name) {
                    return (Name) option;
                }
            }
            return null;
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class CreateIndex implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        /**
         * {@code CREATE INDEX}, with whatever qualifies it: {@code UNIQUE}, or the four words of
         * {@code UNIQUE WHERE NOT NULL}.
         */
        List<Word> keywords;

        Name name;
        Word on;
        Name table;
        Word lParen;

        /**
         * The key columns and the commas between them.
         */
        List<Db2> keys;

        Word rParen;

        /**
         * {@code USING STOGROUP}, {@code CLUSTER}, {@code COPY YES} — storage and clustering, kept
         * as words.
         */
        List<Db2> options;

        @Nullable
        Word end;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitCreateIndex(this, p);
        }

        public boolean isUnique() {
            for (Word keyword : keywords) {
                if ("UNIQUE".equalsIgnoreCase(keyword.getText())) {
                    return true;
                }
            }
            return false;
        }

        public List<IndexKey> getIndexKeys() {
            return elementsOf(keys, IndexKey.class);
        }
    }

    /**
     * Only the {@code ALTER TABLE} that adds a constraint is read. CardDemo writes its foreign key
     * this way rather than in the {@code CREATE TABLE}, so an estate's referential integrity is not
     * all in one place.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class AlterTable implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Word> keywords;
        Name name;

        /**
         * {@link Constraint}s the alter adds, and the words of anything else it does.
         */
        List<Db2> actions;

        @Nullable
        Word end;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitAlterTable(this, p);
        }

        public List<Constraint> getConstraints() {
            return elementsOf(actions, Constraint.class);
        }
    }

    /**
     * A statement this grammar does not read, kept as the words it was written with.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Unknown implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;
        List<Word> words;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitUnknown(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class ColumnDefinition implements Db2 {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        Name name;
        DataType type;

        /**
         * {@code NOT NULL}, {@code DEFAULT}, {@code GENERATED ALWAYS AS IDENTITY} and the rest, kept
         * as words. Nullability is read off them by {@link #isNotNull()}; nothing else in a column
         * attribute names something the relationship graph joins on.
         */
        List<Word> attributes;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitColumnDefinition(this, p);
        }

        public boolean isNotNull() {
            for (int i = 0; i < attributes.size() - 1; i++) {
                if ("NOT".equalsIgnoreCase(attributes.get(i).getText()) &&
                    "NULL".equalsIgnoreCase(attributes.get(i + 1).getText())) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * A table level constraint: {@code PRIMARY KEY}, {@code FOREIGN KEY}, {@code UNIQUE} or
     * {@code CHECK}.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Constraint implements Db2 {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        /**
         * Everything up to the constrained columns: the optional {@code CONSTRAINT name}, the kind,
         * and — for a foreign key — the name DB2 lets you write between {@code KEY} and the columns.
         */
        List<Word> keywords;

        @Nullable
        ColumnList columns;

        /**
         * The {@code REFERENCES} keyword, or empty. A list rather than a nullable word so that a
         * constraint with no reference has nothing to print.
         */
        List<Word> references;

        @Nullable
        Name referencedTable;

        @Nullable
        ColumnList referencedColumns;

        /**
         * {@code ON DELETE RESTRICT}, {@code ENFORCED}, and a check constraint's expression.
         */
        List<Word> options;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitConstraint(this, p);
        }

        /**
         * {@code PRIMARY}, {@code FOREIGN}, {@code UNIQUE} or {@code CHECK} — the first keyword that
         * is not the optional {@code CONSTRAINT name} in front of it.
         */
        public @Nullable String getKind() {
            for (int i = 0; i < keywords.size(); i++) {
                String text = keywords.get(i).getText();
                if ("CONSTRAINT".equalsIgnoreCase(text)) {
                    i++;
                    continue;
                }
                return text.toUpperCase(Locale.ROOT);
            }
            return null;
        }

        public boolean isKind(String kind) {
            return kind.equalsIgnoreCase(getKind());
        }
    }

    /**
     * A parenthesised list of column names, as a key or a reference writes it.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class ColumnList implements Db2 {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        Word lParen;

        /**
         * The names and the commas between them.
         */
        List<Db2> names;

        Word rParen;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitColumnList(this, p);
        }

        public List<Name> getColumnNames() {
            return elementsOf(names, Name.class);
        }
    }

    /**
     * A column's type: one word and, when the type takes them, a length and a scale.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class DataType implements Db2 {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        Name name;

        /**
         * The parenthesised arguments in source order — parentheses, numbers and the comma between
         * them — or empty for a type that takes none.
         */
        List<Word> arguments;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitDataType(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class IndexKey implements Db2 {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        Name name;

        /**
         * {@code ASC}, {@code DESC} or {@code RANDOM}, or null when the DDL leaves it to DB2's
         * default of ascending.
         */
        @Nullable
        Word direction;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitIndexKey(this, p);
        }
    }

    /**
     * An identifier, qualified or not: {@code CARDDEMO.TRANSACTION_TYPE}, {@code TR_TYPE}, or the
     * {@code <DB2DBID>} a templated file writes where a schema belongs.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Name implements Db2 {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        /**
         * The parts and the dots between them, so that the name prints back however it was spaced.
         */
        List<Word> parts;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitName(this, p);
        }

        public String getSimpleName() {
            return undelimit(parts.get(parts.size() - 1).getText());
        }

        /**
         * The schema, or null for an unqualified name. For a three part name this is still the part
         * in front of the last, DB2's first part being a location rather than a schema.
         */
        public @Nullable String getQualifier() {
            return parts.size() < 3 ? null : undelimit(parts.get(parts.size() - 3).getText());
        }

        /**
         * The name as DB2 catalogues it: qualifier and simple name, undelimited and upper cased.
         * Case matters because genapp writes {@code <DB2DBID>.customer} in the DDL and
         * {@code CUSTOMER} in the COBOL that reads it.
         */
        public String getFullName() {
            String qualifier = getQualifier();
            return ((qualifier == null ? "" : qualifier + ".") + getSimpleName()).toUpperCase(Locale.ROOT);
        }

        private static String undelimit(String text) {
            return text.length() > 1 && text.charAt(0) == '"' && text.endsWith("\"") ?
                    text.substring(1, text.length() - 1).replace("\"\"", "\"") : text;
        }
    }

    /**
     * One token, with what preceded it. Keywords, punctuation and the text of an unmodelled
     * statement are all words; an identifier is a {@link Name}, so the two are told apart by type.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Word implements Db2 {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;
        String text;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitWord(this, p);
        }
    }

    static <T> List<T> elementsOf(List<Db2> elements, Class<T> type) {
        List<T> of = null;
        for (Db2 element : elements) {
            if (type.isInstance(element)) {
                if (of == null) {
                    of = new ArrayList<>(elements.size());
                }
                of.add(type.cast(element));
            }
        }
        return of == null ? emptyList() : of;
    }
}
