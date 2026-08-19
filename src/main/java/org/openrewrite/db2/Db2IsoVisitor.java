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
package org.openrewrite.db2;

import org.openrewrite.db2.tree.Db2;

public class Db2IsoVisitor<P> extends Db2Visitor<P> {

    @Override
    public Db2.Ddl visitDdl(Db2.Ddl ddl, P p) {
        return (Db2.Ddl) super.visitDdl(ddl, p);
    }

    @Override
    public Db2.CreateTable visitCreateTable(Db2.CreateTable createTable, P p) {
        return (Db2.CreateTable) super.visitCreateTable(createTable, p);
    }

    @Override
    public Db2.CreateIndex visitCreateIndex(Db2.CreateIndex createIndex, P p) {
        return (Db2.CreateIndex) super.visitCreateIndex(createIndex, p);
    }

    @Override
    public Db2.AlterTable visitAlterTable(Db2.AlterTable alterTable, P p) {
        return (Db2.AlterTable) super.visitAlterTable(alterTable, p);
    }

    @Override
    public Db2.Unknown visitUnknown(Db2.Unknown unknown, P p) {
        return (Db2.Unknown) super.visitUnknown(unknown, p);
    }

    @Override
    public Db2.ColumnDefinition visitColumnDefinition(Db2.ColumnDefinition columnDefinition, P p) {
        return (Db2.ColumnDefinition) super.visitColumnDefinition(columnDefinition, p);
    }

    @Override
    public Db2.Constraint visitConstraint(Db2.Constraint constraint, P p) {
        return (Db2.Constraint) super.visitConstraint(constraint, p);
    }

    @Override
    public Db2.ColumnList visitColumnList(Db2.ColumnList columnList, P p) {
        return (Db2.ColumnList) super.visitColumnList(columnList, p);
    }

    @Override
    public Db2.DataType visitDataType(Db2.DataType dataType, P p) {
        return (Db2.DataType) super.visitDataType(dataType, p);
    }

    @Override
    public Db2.IndexKey visitIndexKey(Db2.IndexKey indexKey, P p) {
        return (Db2.IndexKey) super.visitIndexKey(indexKey, p);
    }

    @Override
    public Db2.Name visitName(Db2.Name name, P p) {
        return (Db2.Name) super.visitName(name, p);
    }

    @Override
    public Db2.Word visitWord(Db2.Word word, P p) {
        return (Db2.Word) super.visitWord(word, p);
    }
}
