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
package org.openrewrite.db2.trait;

import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.db2.tree.Db2;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * A table, and the columns a program's {@code EXEC SQL} names.
 * <p>
 * This is the other end of a column lineage row: a COBOL field on one side and, through here, a
 * column that really exists on the other.
 */
@Value
public class Table implements Trait<Db2.CreateTable> {

    Cursor cursor;

    /**
     * The schema, or null for a table created unqualified — which leaves it to whatever
     * {@code SET CURRENT SQLID} was in force.
     */
    public @Nullable String getSchema() {
        return getTree().getName().getQualifier();
    }

    public String getName() {
        return getTree().getName().getSimpleName();
    }

    /**
     * Schema and name, upper cased, as the COBOL that reads the table writes it.
     */
    public String getQualifiedName() {
        return getTree().getName().getFullName();
    }

    /**
     * The tablespace the table lives in, or null when the DDL leaves it to DB2.
     */
    public @Nullable String getTablespace() {
        Db2.Name tablespace = getTree().getTablespace();
        return tablespace == null ? null : tablespace.getFullName();
    }

    public List<Column> getColumns() {
        List<Db2.ColumnDefinition> definitions = getTree().getColumns();
        List<Column> columns = new ArrayList<>(definitions.size());
        for (Db2.ColumnDefinition definition : definitions) {
            columns.add(new Column(new Cursor(cursor, definition)));
        }
        return columns;
    }

    public @Nullable Column getColumn(String name) {
        for (Column column : getColumns()) {
            if (column.getName().equalsIgnoreCase(name)) {
                return column;
            }
        }
        return null;
    }

    /**
     * The primary key's columns in key order, or empty for a table declaring none.
     */
    public List<String> getPrimaryKey() {
        for (Db2.Constraint constraint : getTree().getConstraints()) {
            if (constraint.isKind("PRIMARY")) {
                return Constraints.columnNames(constraint.getColumns());
            }
        }
        return emptyList();
    }

    /**
     * The foreign keys declared in the {@code CREATE TABLE} itself. An {@code ALTER TABLE} elsewhere
     * can add more, which is why {@link ForeignKey} is matched over the whole file rather than only
     * reached from here.
     */
    public List<ForeignKey> getForeignKeys() {
        List<ForeignKey> foreignKeys = new ArrayList<>();
        for (Db2.Constraint constraint : getTree().getConstraints()) {
            if (constraint.isKind("FOREIGN")) {
                foreignKeys.add(new ForeignKey(new Cursor(cursor, constraint)));
            }
        }
        return foreignKeys;
    }

    public static class Matcher extends SimpleTraitMatcher<Table> {

        @Override
        protected @Nullable Table test(Cursor cursor) {
            return cursor.getValue() instanceof Db2.CreateTable ? new Table(cursor) : null;
        }
    }

    @Override
    public String toString() {
        return "TABLE " + getQualifiedName();
    }
}
