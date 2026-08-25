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
package org.openrewrite.db2.tree;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.jspecify.annotations.Nullable;
import org.openrewrite.*;
import org.openrewrite.db2.Db2Visitor;
import org.openrewrite.db2.internal.Db2Printer;
import org.openrewrite.marker.Markers;

import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * DB2 for z/OS DDL. Every statement DB2 documents is a node of its own, so nothing a file holds is
 * left unread — what this grammar cannot parse is a syntax error rather than a node that says
 * nothing.
 * <p>
 * No node holds a delimiter, a separator or a terminator. Brackets and commas belong to
 * {@link Db2Container}, an operator's space to {@link Db2LeftPadded}, and a semicolon to the
 * {@code Semicolon} marker on a statement's padding — so a recipe adding a column adds a column.
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
    @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @RequiredArgsConstructor
    class Ddl implements Db2, SourceFile {

        @Nullable
        @NonFinal
        transient WeakReference<Padding> padding;

        @With
        @EqualsAndHashCode.Include
        @Getter
        UUID id;

        @With
        @Getter
        Path sourcePath;

        @With
        @Nullable
        @Getter
        FileAttributes fileAttributes;

        @With
        @Getter
        Space prefix;

        @With
        @Getter
        Markers markers;

        @Nullable // for backwards compatibility
        @With(AccessLevel.PRIVATE)
        @Getter
        String charsetName;

        @With
        @Getter
        boolean charsetBomMarked;

        @With
        @Nullable
        @Getter
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

        /**
         * Each statement with the space before its terminator. Whether it has one at all is the
         * {@code Semicolon} marker on the padding, the way Groovy records an optional semicolon.
         */
        List<Db2RightPadded<Statement>> statements;

        public List<Statement> getStatements() {
            return Db2RightPadded.getElements(statements);
        }

        public Ddl withStatements(List<Statement> statements) {
            return getPadding().withStatements(Db2RightPadded.withElements(this.statements, statements));
        }

        @With
        @Getter
        Space eof;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitDdl(this, p);
        }

        @Override
        public <P> TreeVisitor<?, PrintOutputCapture<P>> printer(Cursor cursor) {
            return new Db2Printer<>();
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
            private final Ddl t;

            public List<Db2RightPadded<Statement>> getStatements() {
                return t.statements;
            }

            public Ddl withStatements(List<Db2RightPadded<Statement>> statements) {
                return t.statements == statements ? t : new Ddl(t.padding, t.id, t.sourcePath,
                        t.fileAttributes, t.prefix, t.markers, t.charsetName, t.charsetBomMarked,
                        t.checksum, statements, t.eof);
            }
        }
    }

    /**
     * A table and the columns a program's EXEC SQL names.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class CreateTable implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Name name;

        TableContents contents;

        List<Db2> options;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitCreateTable(this, p);
        }
    }

    /**
     * An index, which is how a batch job reaches a table cheaply.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class CreateIndex implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Name name;

        Keyword on;

        Name table;

        Db2Container<IndexKey> keys;

        List<Db2> options;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitCreateIndex(this, p);
        }
    }

    /**
     * The physical space a table lives in.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class CreateTablespace implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Name name;

        @Nullable
        Keyword in;

        @Nullable
        Name database;

        List<Db2> options;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitCreateTablespace(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class CreateDatabase implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Name name;

        List<Db2> options;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitCreateDatabase(this, p);
        }
    }

    /**
     * The volumes and catalog a tablespace is allocated from.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class CreateStogroup implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Name name;

        List<Db2> options;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitCreateStogroup(this, p);
        }
    }

    /**
     * A query given a name, so its query names the tables it reads.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class CreateView implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Name name;

        @Nullable
        Db2Container<Name> columns;

        Db2LeftPadded<Query> query;

        List<Db2> options;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitCreateView(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class CreateAlias implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Name name;

        Db2LeftPadded<Name> target;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitCreateAlias(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class CreateSynonym implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Name name;

        Db2LeftPadded<Name> target;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitCreateSynonym(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class CreateSequence implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Name name;

        @Nullable
        Db2LeftPadded<DataType> type;

        List<Db2> options;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitCreateSequence(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class CreateRole implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Name name;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitCreateRole(this, p);
        }
    }

    /**
     * The table a LOB column's data actually lives in.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class CreateAuxiliaryTable implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Name name;

        List<Db2> options;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitCreateAuxiliaryTable(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class CreateType implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Name name;

        Db2LeftPadded<DataType> type;

        List<Db2> options;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitCreateType(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class CreateVariable implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Name name;

        DataType type;

        List<Db2> options;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitCreateVariable(this, p);
        }
    }

    /**
     * What a user without the privilege sees instead of the value.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class CreateMask implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Name name;

        Name table;

        List<Db2> options;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitCreateMask(this, p);
        }
    }

    /**
     * Which rows a user is allowed to see at all.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class CreatePermission implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Name name;

        Name table;

        List<Db2> options;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitCreatePermission(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class CreateTrustedContext implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Name name;

        List<Db2> options;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitCreateTrustedContext(this, p);
        }
    }

    /**
     * Application logic living in the database.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class CreateTrigger implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Name name;

        List<Db2> events;

        Name table;

        List<Db2> options;

        @Nullable
        Block body;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitCreateTrigger(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class CreateProcedure implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Name name;

        Db2Container<Parameter> parameters;

        List<Db2> clauses;

        @Nullable
        Block body;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitCreateProcedure(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class CreateFunction implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Name name;

        Db2Container<Parameter> parameters;

        List<Db2> clauses;

        @Nullable
        Block body;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitCreateFunction(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class DeclareGlobalTemporaryTable implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Name name;

        TableContents contents;

        List<Db2> options;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitDeclareGlobalTemporaryTable(this, p);
        }
    }

    /**
     * CardDemo declares its foreign key here rather than in the CREATE TABLE.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class AlterTable implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Name name;

        List<Db2> actions;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitAlterTable(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class AlterTablespace implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Name name;

        @Nullable
        Keyword in;

        @Nullable
        Name database;

        List<Db2> options;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitAlterTablespace(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class AlterIndex implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Name name;

        List<Db2> options;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitAlterIndex(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class AlterDatabase implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Name name;

        List<Db2> options;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitAlterDatabase(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class AlterStogroup implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Name name;

        List<Db2> options;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitAlterStogroup(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class AlterSequence implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Name name;

        List<Db2> options;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitAlterSequence(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class AlterView implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Name name;

        List<Db2> options;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitAlterView(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class AlterProcedure implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Name name;

        Db2Container<Parameter> parameters;

        List<Db2> clauses;

        @Nullable
        Block body;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitAlterProcedure(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class AlterFunction implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Name name;

        Db2Container<Parameter> parameters;

        List<Db2> clauses;

        @Nullable
        Block body;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitAlterFunction(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class AlterTrigger implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Name name;

        List<Db2> options;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitAlterTrigger(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class AlterMask implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Name name;

        List<Db2> options;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitAlterMask(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class AlterPermission implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Name name;

        List<Db2> options;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitAlterPermission(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class AlterTrustedContext implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Name name;

        List<Db2> options;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitAlterTrustedContext(this, p);
        }
    }

    /**
     * DB2 documents one DROP over many object types, and the kind says which.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Drop implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Name name;

        List<Db2> options;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitDrop(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Rename implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Name name;

        Db2LeftPadded<Name> newName;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitRename(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Comment implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Name target;

        Db2LeftPadded<Word> text;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitComment(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Label implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Name target;

        Db2LeftPadded<Word> text;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitLabel(this, p);
        }
    }

    /**
     * Who may do what to which object.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Grant implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Db2Container<Db2> privileges;

        List<Keyword> on;

        Db2Container<Name> objects;

        @Nullable
        Keyword to;

        Db2Container<Db2> grantees;

        List<Db2> options;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitGrant(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Revoke implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Db2Container<Db2> privileges;

        List<Keyword> on;

        Db2Container<Name> objects;

        @Nullable
        Keyword to;

        Db2Container<Db2> grantees;

        List<Db2> options;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitRevoke(this, p);
        }
    }

    /**
     * A special register, of which CURRENT SQLID is the one a DDL script always sets.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Set implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Db2LeftPadded<Db2> value;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitSet(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Commit implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitCommit(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Rollback implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        @Nullable
        Db2LeftPadded<Name> savepoint;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitRollback(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Savepoint implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Name name;

        List<Db2> options;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitSavepoint(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class ReleaseSavepoint implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Name name;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitReleaseSavepoint(this, p);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class LockTable implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Name name;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitLockTable(this, p);
        }
    }

    /**
     * Seed data, which a DDL script carries as often as not.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Insert implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Name table;

        @Nullable
        Db2Container<Name> columns;

        Db2Container<Db2> values;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitInsert(this, p);
        }
    }

    /**
     * A statement with nothing in it. The semicolon is the marker on its padding, so a bare terminator has no field at all.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Empty implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitEmpty(this, p);
        }
    }

    /**
     * A table defined by its columns.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class TableElements implements Db2, TableContents {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        Db2Container<Db2> elements;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitTableElements(this, p);
        }
    }

    /**
     * A table defined by another table.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class TableLike implements Db2, TableContents {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Name table;

        List<Db2> options;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitTableLike(this, p);
        }
    }

    /**
     * A table defined by a query. The query is in a container because DB2 brackets it, and brackets belong to the container rather than to a node.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class TableAsQuery implements Db2, TableContents {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        Db2Container<Query> query;

        List<Db2> options;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitTableAsQuery(this, p);
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

        List<Db2> attributes;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitColumnDefinition(this, p);
        }
    }

    /**
     * A table level key or check. The kind is a field, not words to match text against.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Constraint implements Db2 {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        @Nullable
        Keyword constraintKeyword;

        @Nullable
        Name constraintName;

        List<Keyword> keywords;

        @Nullable
        Name keyName;

        @Nullable
        Db2Container<Name> columns;

        @Nullable
        Keyword references;

        @Nullable
        Name referencedTable;

        @Nullable
        Db2Container<Name> referencedColumns;

        List<Db2> options;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitConstraint(this, p);
        }
    }

    /**
     * A column's type, and the length and scale it takes.
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

        @Nullable
        Db2Container<Word> arguments;

        List<Db2> attributes;

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

        @Nullable
        Keyword direction;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitIndexKey(this, p);
        }
    }

    /**
     * DB2's option syntax is a keyword and what follows it. This is the one place a keyword is data, because which keyword it is varies.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Option implements Db2 {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        List<Db2> values;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitOption(this, p);
        }
    }

    /**
     * A routine's parameter.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Parameter implements Db2 {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        @Nullable
        Keyword mode;

        @Nullable
        Name name;

        DataType type;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitParameter(this, p);
        }
    }

    /**
     * A SELECT, kept as its words plus the names it reads, so a view says which tables feed it. A statement in its own right too: a catalog query is what a .sql file next to the DDL usually holds.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Query implements Db2, Statement {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Db2> parts;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitQuery(this, p);
        }
    }

    /**
     * A SQL PL body. Only its nesting is modelled - so that an inner END cannot close the routine early - and not the statements between BEGIN and END.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Block implements Db2 {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Keyword> keywords;

        List<Db2> body;

        @Nullable
        Keyword end;

        @Nullable
        Name label;

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitBlock(this, p);
        }
    }

    /**
     * An identifier, qualified or not, or the placeholder a templated file writes.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Name implements Db2 {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        List<Word> parts;

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
         * The name as DB2 catalogues it, upper cased. Case matters because genapp writes
         * {@code <DB2DBID>.customer} in the DDL and {@code CUSTOMER} in the COBOL that reads it.
         */
        public String getFullName() {
            String qualifier = getQualifier();
            return ((qualifier == null ? "" : qualifier + ".") + getSimpleName())
                    .toUpperCase(Locale.ROOT);
        }

        private static String undelimit(String text) {
            return text.length() > 1 && text.charAt(0) == '"' && text.endsWith("\"") ?
                    text.substring(1, text.length() - 1).replace("\"\"", "\"") : text;
        }

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitName(this, p);
        }
    }

    /**
     * One of DB2's own words. A node rather than a bare enum because the space in front of it is real - genapp writes CREATE   TABLESPACE - and typed rather than free text so that matching is on the enum. The text is kept alongside because DB2 is case insensitive: genapp writes `commit` where CardDemo writes `COMMIT`, and only the source says which.
     */
    @Value
    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @With
    class Keyword implements Db2 {

        @EqualsAndHashCode.Include
        UUID id;

        Space prefix;
        Markers markers;

        Type type;

        String text;

        /**
         * Every word DB2 reserves or gives meaning to, taken from the lexer so the two cannot
         * drift apart. The printer writes {@link #getKeyword()}, so no node carries the spelling.
         */
        public enum Type {
            Absolute,
            Access,
            Action,
            Add,
            Admin,
            After,
            Alias,
            All,
            Allocate,
            Allow,
            Alter,
            Always,
            And,
            Any,
            Append,
            As,
            Asc,
            Asensitive,
            Associate,
            Asutime,
            At,
            Atomic,
            Attributes,
            Audit,
            Authentication,
            Authid,
            Aux,
            Auxiliary,
            Based,
            Before,
            Begin,
            Between,
            Bit,
            Bufferpool,
            By,
            Cache,
            Call,
            Called,
            Capture,
            Cardinality,
            Cascade,
            Cascaded,
            Case,
            Cast,
            Ccsid,
            Change,
            Changed,
            Changes,
            Char,
            Character,
            Check,
            Clone,
            Close,
            Cluster,
            Collection,
            Collid,
            Column,
            Comment,
            Commit,
            Comparisons,
            Compress,
            Concat,
            Condition,
            Connect,
            Connection,
            Constraint,
            Contains,
            Context,
            Continue,
            Control,
            Copy,
            Create,
            Cube,
            Current,
            Cursor,
            Cursors,
            Cycle,
            Data,
            Database,
            Dataclas,
            Day,
            Days,
            Db2,
            Db2sql,
            Dbinfo,
            Debug,
            Declare,
            Default,
            Defer,
            Define,
            Definer,
            Delete,
            Desc,
            Descriptor,
            Deterministic,
            Disable,
            Disallow,
            Distinct,
            Do,
            Double,
            Drop,
            Dssize,
            Dynamic,
            Each,
            Editproc,
            Else,
            Elseif,
            Enable,
            Encoding,
            End,
            Ending,
            Enforced,
            Environment,
            Erase,
            Escape,
            Except,
            Exception,
            Exclude,
            Excluding,
            Exclusive,
            Exec,
            Execute,
            Exists,
            Exit,
            Explain,
            External,
            Fenced,
            Fetch,
            Fieldproc,
            Final,
            First,
            For,
            Foreign,
            Free,
            Freepage,
            From,
            Full,
            Function,
            Gbpcache,
            Generate,
            Generated,
            Get,
            Global,
            Go,
            Goto,
            Grant,
            Group,
            Handler,
            Hash,
            Having,
            HexString,
            HiddenKw,
            History,
            Hold,
            Hour,
            Hours,
            Id,
            Identity,
            If,
            Immediate,
            Implicitly,
            In,
            Include,
            Including,
            Inclusive,
            Increment,
            Index,
            Indexbp,
            Inherit,
            Inline,
            Inner,
            Inout,
            Input,
            Insensitive,
            Insert,
            Instead,
            Intersect,
            Into,
            Is,
            Isobid,
            Iterate,
            Jar,
            Join,
            Key,
            Keys,
            Label,
            Language,
            Large,
            Last,
            LcCtype,
            Leave,
            Left,
            Length,
            Like,
            Limit,
            Load,
            Lob,
            Local,
            Locale,
            Locator,
            Locators,
            Lock,
            Lockmax,
            Locksize,
            Logged,
            Long,
            Loop,
            Main,
            Maintained,
            Mask,
            Materialized,
            Maxpartitions,
            Maxrows,
            Maxvalue,
            Member,
            Mgmtclas,
            Microsecond,
            Microseconds,
            Minute,
            Minutes,
            Minvalue,
            Mixed,
            Mode,
            Modifies,
            Month,
            Months,
            Name,
            New,
            NewTable,
            Next,
            No,
            None,
            Not,
            Null,
            Nulls,
            Numparts,
            Obid,
            Of,
            Old,
            OldTable,
            On,
            Only,
            Open,
            Optimization,
            Optimize,
            Option,
            Options,
            Or,
            Order,
            Organization,
            Organize,
            Out,
            Outer,
            Package,
            Padded,
            Page,
            Pagenum,
            Parameter,
            Part,
            Partition,
            Partitioned,
            Partitioning,
            Path,
            Pctfree,
            Period,
            Permission,
            Piecesize,
            Plan,
            Precision,
            Prepare,
            Prevval,
            Primary,
            Priqty,
            Privileges,
            Procedure,
            Program,
            Psid,
            Public,
            Qualifier,
            Query,
            Queryno,
            Random,
            Range,
            Reads,
            References,
            Referencing,
            Refresh,
            Regenerate,
            Registers,
            Relative,
            Release,
            Remove,
            Rename,
            Repeat,
            Replace,
            Reset,
            Resident,
            Resignal,
            Restart,
            Restrict,
            Result,
            Retain,
            Return,
            Returns,
            Revoke,
            Right,
            Role,
            Rollback,
            Rollup,
            Rotate,
            RoundCeiling,
            RoundDown,
            RoundFloor,
            RoundHalfDown,
            RoundHalfEven,
            RoundHalfUp,
            RoundUp,
            Row,
            Rows,
            Rowset,
            Run,
            Savepoint,
            Sbcs,
            Schema,
            Scratchpad,
            Second,
            Seconds,
            Secqty,
            Secured,
            Security,
            Segsize,
            Select,
            Sensitive,
            Sequence,
            Set,
            Sets,
            Share,
            Signal,
            Simple,
            Size,
            Some,
            Source,
            Space,
            Special,
            Specific,
            Sql,
            Sqlid,
            Standard,
            Start,
            Statement,
            Static,
            Stay,
            Stogroup,
            Storclas,
            Stores,
            Style,
            Sub,
            Summary,
            Synonym,
            Sysfun,
            Sysibm,
            Sysproc,
            System,
            Table,
            Tablespace,
            Temporary,
            Then,
            Time,
            Timestamp,
            To,
            Trackmod,
            Transaction,
            Trigger,
            Truncate,
            Trusted,
            Type,
            Undo,
            Union,
            Unique,
            Unload,
            Until,
            Update,
            Upon,
            Usage,
            Use,
            User,
            Using,
            Validproc,
            Value,
            Values,
            Varchar,
            Vargraphic,
            Variable,
            Variant,
            Varying,
            Vcat,
            Versioning,
            View,
            Volatile,
            Volumes,
            When,
            Whenever,
            Where,
            While,
            With,
            Without,
            Wlm,
            Work,
            Workfile,
            Xmlcast,
            Xmlexists,
            Xmlnamespaces,
            Xmlpattern,
            Year,
            Years,
            Yes,
            Zone;

            /**
             * DB2's own spelling, which is what a generated statement should be written with.
             * A parsed one prints {@link Keyword#getText()} instead, to keep the file's own case.
             */
            public String getKeyword() {
                // Type.Locale shadows java.util.Locale inside this enum.
                return name().toUpperCase(java.util.Locale.ROOT);
            }
        }

        @Override
        public <P> Db2 acceptDb2(Db2Visitor<P> v, P p) {
            return v.visitKeyword(this, p);
        }
    }

    /**
     * One token, with what preceded it. An identifier is a {@link Name}, so the two are told apart
     * by type rather than by looking at the text.
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

    static <T> List<T> elementsOf(List<? extends Db2> elements, Class<T> type) {
        List<T> of = new ArrayList<>(elements.size());
        for (Db2 element : elements) {
            if (type.isInstance(element)) {
                of.add(type.cast(element));
            }
        }
        return of;
    }

    /**
     * Whether a keyword run holds this word. Reading a statement's shape means asking the typed
     * keywords, never matching text.
     */
    static boolean has(List<Keyword> keywords, Keyword.Type type) {
        for (Keyword keyword : keywords) {
            if (keyword.getType() == type) {
                return true;
            }
        }
        return false;
    }

    /**
     * The word a statement's keywords name it by — the object of a DROP, the kind of a constraint.
     * The first keyword is the verb, so the one after it is what the statement is about.
     */
    static Keyword.@Nullable Type subjectOf(List<Keyword> keywords) {
        return keywords.size() < 2 ? null : keywords.get(1).getType();
    }

    static String textOf(List<Word> words) {
        StringBuilder text = new StringBuilder();
        for (Word word : words) {
            if (text.length() > 0) {
                text.append(' ');
            }
            text.append(word.getText());
        }
        return text.toString().toUpperCase(Locale.ROOT);
    }
}
