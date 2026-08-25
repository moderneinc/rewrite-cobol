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
 * One table pointing at another — the edge the relationship graph draws between two tables without
 * having to read a program to find it.
 * <p>
 * Matched over the whole file rather than reached from {@link Table}, because a foreign key is
 * written either in the {@code CREATE TABLE} or in an {@code ALTER TABLE} afterwards and CardDemo
 * does both.
 */
@Value
public class ForeignKey implements Trait<Db2.Constraint> {

    Cursor cursor;

    /**
     * The table the key is declared on, or null when the constraint was read outside a
     * {@code CREATE TABLE} or {@code ALTER TABLE}.
     */
    public @Nullable String getTable() {
        Db2.Name table = Constraints.constrainedTable(cursor);
        return table == null ? null : table.getFullName();
    }

    public List<String> getColumns() {
        return Constraints.columnNames(getTree().getColumns());
    }

    public @Nullable String getReferencedTable() {
        Db2.Name referenced = getTree().getReferencedTable();
        return referenced == null ? null : referenced.getFullName();
    }

    /**
     * The columns of the referenced table, or empty when the DDL leaves them to be its primary key.
     */
    public List<String> getReferencedColumns() {
        return Constraints.columnNames(getTree().getReferencedColumns());
    }

    /**
     * {@code RESTRICT}, {@code CASCADE}, {@code SET NULL} or {@code NO ACTION} — what DB2 does to
     * this table's rows when a referenced row goes away. Null when the DDL leaves it to the default
     * of {@code NO ACTION}.
     */
    /**
     * {@code RESTRICT}, {@code CASCADE}, {@code SET NULL} or {@code NO ACTION} — what DB2 does to
     * this table's rows when a referenced row goes away. Null when the DDL leaves it to the default
     * of {@code NO ACTION}.
     */
    public @Nullable String getDeleteRule() {
        for (Db2 option : getTree().getOptions()) {
            if (!(option instanceof Db2.Option)) {
                continue;
            }
            List<Db2.Keyword> keywords = ((Db2.Option) option).getKeywords();
            if (!Db2.has(keywords, Db2.Keyword.Type.Delete)) {
                continue;
            }
            StringBuilder rule = new StringBuilder();
            for (int i = 2; i < keywords.size(); i++) {
                if (rule.length() > 0) {
                    rule.append(' ');
                }
                rule.append(keywords.get(i).getType().getKeyword());
            }
            return rule.length() == 0 ? null : rule.toString();
        }
        return null;
    }

    public static class Matcher extends SimpleTraitMatcher<ForeignKey> {

        @Override
        protected @Nullable ForeignKey test(Cursor cursor) {
            Object value = cursor.getValue();
            return value instanceof Db2.Constraint &&
                   Db2.has(((Db2.Constraint) value).getKeywords(), Db2.Keyword.Type.Foreign) ?
                    new ForeignKey(cursor) : null;
        }
    }

    @Override
    public String toString() {
        return getTable() + " " + getColumns() + " -> " + getReferencedTable() +
               (getReferencedColumns().isEmpty() ? "" : " " + getReferencedColumns());
    }
}
