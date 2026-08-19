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

import org.openrewrite.TreeVisitor;
import org.openrewrite.db2.tree.Db2;
import org.openrewrite.db2.tree.Space;
import org.openrewrite.internal.ListUtils;

public class Db2Visitor<P> extends TreeVisitor<Db2, P> {

    public Db2 visitCompilationUnit(Db2.CompilationUnit compilationUnit, P p) {
        Db2.CompilationUnit c = compilationUnit;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.COMPILATION_UNIT_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withStatements(ListUtils.map(c.getStatements(), s -> visitAndCast(s, p)));
        return c.withEof(visitSpace(c.getEof(), Space.Location.COMPILATION_UNIT_EOF, p));
    }

    public Db2 visitCreateTable(Db2.CreateTable createTable, P p) {
        Db2.CreateTable c = createTable;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.CREATE_TABLE_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withKeywords(ListUtils.map(c.getKeywords(), w -> visitAndCast(w, p)));
        c = c.withName(visitAndCast(c.getName(), p));
        c = c.withLParen(visitAndCast(c.getLParen(), p));
        c = c.withElements(ListUtils.map(c.getElements(), e -> visitAndCast(e, p)));
        c = c.withRParen(visitAndCast(c.getRParen(), p));
        c = c.withOptions(ListUtils.map(c.getOptions(), o -> visitAndCast(o, p)));
        if (c.getEnd() != null) {
            c = c.withEnd(visitAndCast(c.getEnd(), p));
        }
        return c;
    }

    public Db2 visitCreateIndex(Db2.CreateIndex createIndex, P p) {
        Db2.CreateIndex c = createIndex;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.CREATE_INDEX_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withKeywords(ListUtils.map(c.getKeywords(), w -> visitAndCast(w, p)));
        c = c.withName(visitAndCast(c.getName(), p));
        c = c.withOn(visitAndCast(c.getOn(), p));
        c = c.withTable(visitAndCast(c.getTable(), p));
        c = c.withLParen(visitAndCast(c.getLParen(), p));
        c = c.withKeys(ListUtils.map(c.getKeys(), k -> visitAndCast(k, p)));
        c = c.withRParen(visitAndCast(c.getRParen(), p));
        c = c.withOptions(ListUtils.map(c.getOptions(), o -> visitAndCast(o, p)));
        if (c.getEnd() != null) {
            c = c.withEnd(visitAndCast(c.getEnd(), p));
        }
        return c;
    }

    public Db2 visitAlterTable(Db2.AlterTable alterTable, P p) {
        Db2.AlterTable a = alterTable;
        a = a.withPrefix(visitSpace(a.getPrefix(), Space.Location.ALTER_TABLE_PREFIX, p));
        a = a.withMarkers(visitMarkers(a.getMarkers(), p));
        a = a.withKeywords(ListUtils.map(a.getKeywords(), w -> visitAndCast(w, p)));
        a = a.withName(visitAndCast(a.getName(), p));
        a = a.withActions(ListUtils.map(a.getActions(), action -> visitAndCast(action, p)));
        if (a.getEnd() != null) {
            a = a.withEnd(visitAndCast(a.getEnd(), p));
        }
        return a;
    }

    public Db2 visitUnknown(Db2.Unknown unknown, P p) {
        Db2.Unknown u = unknown;
        u = u.withPrefix(visitSpace(u.getPrefix(), Space.Location.UNKNOWN_PREFIX, p));
        u = u.withMarkers(visitMarkers(u.getMarkers(), p));
        return u.withWords(ListUtils.map(u.getWords(), w -> visitAndCast(w, p)));
    }

    public Db2 visitColumnDefinition(Db2.ColumnDefinition columnDefinition, P p) {
        Db2.ColumnDefinition c = columnDefinition;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.COLUMN_DEFINITION_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withName(visitAndCast(c.getName(), p));
        c = c.withType(visitAndCast(c.getType(), p));
        return c.withAttributes(ListUtils.map(c.getAttributes(), a -> visitAndCast(a, p)));
    }

    public Db2 visitConstraint(Db2.Constraint constraint, P p) {
        Db2.Constraint c = constraint;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.CONSTRAINT_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withKeywords(ListUtils.map(c.getKeywords(), w -> visitAndCast(w, p)));
        if (c.getColumns() != null) {
            c = c.withColumns(visitAndCast(c.getColumns(), p));
        }
        c = c.withReferences(ListUtils.map(c.getReferences(), w -> visitAndCast(w, p)));
        if (c.getReferencedTable() != null) {
            c = c.withReferencedTable(visitAndCast(c.getReferencedTable(), p));
        }
        if (c.getReferencedColumns() != null) {
            c = c.withReferencedColumns(visitAndCast(c.getReferencedColumns(), p));
        }
        return c.withOptions(ListUtils.map(c.getOptions(), o -> visitAndCast(o, p)));
    }

    public Db2 visitColumnList(Db2.ColumnList columnList, P p) {
        Db2.ColumnList c = columnList;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.COLUMN_LIST_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withLParen(visitAndCast(c.getLParen(), p));
        c = c.withNames(ListUtils.map(c.getNames(), n -> visitAndCast(n, p)));
        return c.withRParen(visitAndCast(c.getRParen(), p));
    }

    public Db2 visitDataType(Db2.DataType dataType, P p) {
        Db2.DataType d = dataType;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DATA_TYPE_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        d = d.withName(visitAndCast(d.getName(), p));
        return d.withArguments(ListUtils.map(d.getArguments(), a -> visitAndCast(a, p)));
    }

    public Db2 visitIndexKey(Db2.IndexKey indexKey, P p) {
        Db2.IndexKey i = indexKey;
        i = i.withPrefix(visitSpace(i.getPrefix(), Space.Location.INDEX_KEY_PREFIX, p));
        i = i.withMarkers(visitMarkers(i.getMarkers(), p));
        i = i.withName(visitAndCast(i.getName(), p));
        if (i.getDirection() != null) {
            i = i.withDirection(visitAndCast(i.getDirection(), p));
        }
        return i;
    }

    public Db2 visitName(Db2.Name name, P p) {
        Db2.Name n = name;
        n = n.withPrefix(visitSpace(n.getPrefix(), Space.Location.NAME_PREFIX, p));
        n = n.withMarkers(visitMarkers(n.getMarkers(), p));
        return n.withParts(ListUtils.map(n.getParts(), part -> visitAndCast(part, p)));
    }

    public Db2 visitWord(Db2.Word word, P p) {
        Db2.Word w = word;
        w = w.withPrefix(visitSpace(w.getPrefix(), Space.Location.WORD_PREFIX, p));
        return w.withMarkers(visitMarkers(w.getMarkers(), p));
    }

    public Space visitSpace(Space space, Space.Location location, P p) {
        return space;
    }
}
