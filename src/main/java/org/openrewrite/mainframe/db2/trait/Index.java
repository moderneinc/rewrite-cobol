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
package org.openrewrite.mainframe.db2.trait;

import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.mainframe.db2.tree.Db2;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.util.ArrayList;
import java.util.List;

/**
 * An index, which is how a batch job reaches a table cheaply and so which columns a query is
 * expected to be selective on.
 */
@Value
public class Index implements Trait<Db2.CreateIndex> {

    Cursor cursor;

    public String getName() {
        return getTree().getName().getSimpleName();
    }

    public String getQualifiedName() {
        return getTree().getName().getFullName();
    }

    /**
     * The table indexed, qualified as the DDL wrote it.
     */
    public String getTable() {
        return getTree().getTable().getFullName();
    }

    public boolean isUnique() {
        return Db2.has(getTree().getKeywords(), Db2.Keyword.Type.Unique);
    }

    public List<Key> getKeys() {
        List<Db2.IndexKey> indexKeys = getTree().getKeys().getElements();
        List<Key> keys = new ArrayList<>(indexKeys.size());
        for (Db2.IndexKey key : indexKeys) {
            Db2.Keyword direction = key.getDirection();
            keys.add(new Key(key.getName().getSimpleName(),
                    direction == null ? "ASC" : direction.getType().getKeyword()));
        }
        return keys;
    }

    public List<String> getKeyColumns() {
        List<Key> keys = getKeys();
        List<String> columns = new ArrayList<>(keys.size());
        for (Key key : keys) {
            columns.add(key.getColumn());
        }
        return columns;
    }

    /**
     * One column of the key, and the order it is held in. An index written without a direction is
     * ascending, so this says {@code ASC} where the DDL says nothing.
     */
    @Value
    public static class Key {
        String column;
        String direction;
    }

    public static class Matcher extends SimpleTraitMatcher<Index> {

        @Override
        protected @Nullable Index test(Cursor cursor) {
            return cursor.getValue() instanceof Db2.CreateIndex ? new Index(cursor) : null;
        }
    }

    @Override
    public String toString() {
        return (isUnique() ? "UNIQUE INDEX " : "INDEX ") + getQualifiedName() + " ON " + getTable();
    }
}
