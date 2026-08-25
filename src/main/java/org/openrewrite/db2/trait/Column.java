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

import java.util.List;
import java.util.Locale;

/**
 * A column of a table: what it is called, what it holds, and whether it may be absent.
 */
@Value
public class Column implements Trait<Db2.ColumnDefinition> {

    Cursor cursor;

    public String getName() {
        return getTree().getName().getSimpleName();
    }

    /**
     * The type without its length: {@code CHAR}, {@code VARCHAR}, {@code DECIMAL}, {@code DATE}.
     */
    public String getTypeName() {
        return getTree().getType().getName().getSimpleName().toUpperCase(Locale.ROOT);
    }

    /**
     * The first parenthesised argument — a character count for the string types, a precision for the
     * numeric ones — or null for a type that takes none.
     */
    public @Nullable Integer getLength() {
        return argument(0);
    }

    /**
     * The digits after the decimal point of a {@code DECIMAL} or {@code NUMERIC}, or null.
     */
    public @Nullable Integer getScale() {
        return argument(1);
    }

    /**
     * Whether the column may hold no value. A primary key column may not, whether or not the DDL
     * says {@code NOT NULL} — DB2 requires it and supplies it when it is left out.
     */
    public boolean isNullable() {
        return !isNotNull() && !isPrimaryKey();
    }

    /**
     * Whether the column says {@code NOT NULL} among its attributes.
     */
    private boolean isNotNull() {
        for (Db2 attribute : getTree().getAttributes()) {
            if (attribute instanceof Db2.Option &&
                Db2.has(((Db2.Option) attribute).getKeywords(), Db2.Keyword.Type.Not) &&
                Db2.has(((Db2.Option) attribute).getKeywords(), Db2.Keyword.Type.Null)) {
                return true;
            }
        }
        return false;
    }

    public boolean isPrimaryKey() {
        Table table = getTable();
        if (table == null) {
            return false;
        }
        for (String key : table.getPrimaryKey()) {
            if (key.equalsIgnoreCase(getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Where the column sits in the table, counting from one. This is the order a {@code SELECT *} and
     * a host structure both take, so it is what aligns a column with the COBOL field receiving it.
     */
    public int getOrdinal() {
        Table table = getTable();
        if (table == null) {
            return -1;
        }
        List<Db2.ColumnDefinition> columns =
                Db2.elementsOf(Constraints.elementsOf(table.getTree()), Db2.ColumnDefinition.class);
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i) == getTree()) {
                return i + 1;
            }
        }
        return -1;
    }

    public @Nullable Table getTable() {
        return Constraints.enclosingTable(cursor);
    }

    private @Nullable Integer argument(int index) {
        int seen = 0;
        if (getTree().getType().getArguments() == null) {
            return null;
        }
        for (Db2.Word argument : getTree().getType().getArguments().getElements()) {
            String text = argument.getText();
            if (!text.isEmpty() && Character.isDigit(text.charAt(0))) {
                if (seen++ == index) {
                    return Integer.parseInt(text);
                }
            }
        }
        return null;
    }

    public static class Matcher extends SimpleTraitMatcher<Column> {

        @Override
        protected @Nullable Column test(Cursor cursor) {
            return cursor.getValue() instanceof Db2.ColumnDefinition ? new Column(cursor) : null;
        }
    }

    @Override
    public String toString() {
        Table table = getTable();
        return (table == null ? "" : table.getQualifiedName() + ".") + getName();
    }
}
