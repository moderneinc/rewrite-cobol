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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.db2.tree.Db2;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * Reading a constraint's parts, and finding the statement one was written in.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class Constraints {

    static List<String> columnNames(Db2.@Nullable ColumnList columns) {
        if (columns == null) {
            return emptyList();
        }
        List<Db2.Name> names = columns.getColumnNames();
        List<String> columnNames = new ArrayList<>(names.size());
        for (Db2.Name name : names) {
            columnNames.add(name.getSimpleName());
        }
        return columnNames;
    }

    /**
     * The {@code CREATE TABLE} a node was written inside, or null when it was not.
     */
    static @Nullable Table enclosingTable(Cursor cursor) {
        for (Cursor c = cursor.getParent(); c != null; c = c.getParent()) {
            if (c.getValue() instanceof Db2.CreateTable) {
                return new Table(c);
            }
        }
        return null;
    }

    /**
     * The name of the table a constraint constrains. A foreign key is written either in the
     * {@code CREATE TABLE} or in an {@code ALTER TABLE} of its own, and CardDemo uses both, so
     * neither statement can be assumed.
     */
    static Db2.@Nullable Name constrainedTable(Cursor cursor) {
        for (Cursor c = cursor.getParent(); c != null; c = c.getParent()) {
            Object value = c.getValue();
            if (value instanceof Db2.CreateTable) {
                return ((Db2.CreateTable) value).getName();
            }
            if (value instanceof Db2.AlterTable) {
                return ((Db2.AlterTable) value).getName();
            }
        }
        return null;
    }
}
