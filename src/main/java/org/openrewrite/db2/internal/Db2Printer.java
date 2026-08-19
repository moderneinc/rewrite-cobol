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
package org.openrewrite.db2.internal;

import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.PrintOutputCapture;
import org.openrewrite.db2.Db2Visitor;
import org.openrewrite.db2.tree.Db2;
import org.openrewrite.db2.tree.Space;
import org.openrewrite.marker.Marker;
import org.openrewrite.marker.Markers;

import java.util.function.UnaryOperator;

public class Db2Printer<P> extends Db2Visitor<PrintOutputCapture<P>> {
    public static final UnaryOperator<String> DB2_MARKER_WRAPPER =
            out -> "~~" + out + (out.isEmpty() ? "" : "~~") + ">";

    @Override
    public Db2 visitDdl(Db2.Ddl ddl, PrintOutputCapture<P> p) {
        beforeSyntax(ddl, Space.Location.DDL_PREFIX, p);
        visit(ddl.getStatements(), p);
        afterSyntax(ddl, p);
        visitSpace(ddl.getEof(), Space.Location.DDL_EOF, p);
        return ddl;
    }

    @Override
    public Db2 visitCreateTable(Db2.CreateTable createTable, PrintOutputCapture<P> p) {
        beforeSyntax(createTable, Space.Location.CREATE_TABLE_PREFIX, p);
        visit(createTable.getKeywords(), p);
        visit(createTable.getName(), p);
        visit(createTable.getLParen(), p);
        visit(createTable.getElements(), p);
        visit(createTable.getRParen(), p);
        visit(createTable.getOptions(), p);
        visit(createTable.getEnd(), p);
        afterSyntax(createTable, p);
        return createTable;
    }

    @Override
    public Db2 visitCreateIndex(Db2.CreateIndex createIndex, PrintOutputCapture<P> p) {
        beforeSyntax(createIndex, Space.Location.CREATE_INDEX_PREFIX, p);
        visit(createIndex.getKeywords(), p);
        visit(createIndex.getName(), p);
        visit(createIndex.getOn(), p);
        visit(createIndex.getTable(), p);
        visit(createIndex.getLParen(), p);
        visit(createIndex.getKeys(), p);
        visit(createIndex.getRParen(), p);
        visit(createIndex.getOptions(), p);
        visit(createIndex.getEnd(), p);
        afterSyntax(createIndex, p);
        return createIndex;
    }

    @Override
    public Db2 visitAlterTable(Db2.AlterTable alterTable, PrintOutputCapture<P> p) {
        beforeSyntax(alterTable, Space.Location.ALTER_TABLE_PREFIX, p);
        visit(alterTable.getKeywords(), p);
        visit(alterTable.getName(), p);
        visit(alterTable.getActions(), p);
        visit(alterTable.getEnd(), p);
        afterSyntax(alterTable, p);
        return alterTable;
    }

    @Override
    public Db2 visitUnknown(Db2.Unknown unknown, PrintOutputCapture<P> p) {
        beforeSyntax(unknown, Space.Location.UNKNOWN_PREFIX, p);
        visit(unknown.getWords(), p);
        afterSyntax(unknown, p);
        return unknown;
    }

    @Override
    public Db2 visitColumnDefinition(Db2.ColumnDefinition columnDefinition, PrintOutputCapture<P> p) {
        beforeSyntax(columnDefinition, Space.Location.COLUMN_DEFINITION_PREFIX, p);
        visit(columnDefinition.getName(), p);
        visit(columnDefinition.getType(), p);
        visit(columnDefinition.getAttributes(), p);
        afterSyntax(columnDefinition, p);
        return columnDefinition;
    }

    @Override
    public Db2 visitConstraint(Db2.Constraint constraint, PrintOutputCapture<P> p) {
        beforeSyntax(constraint, Space.Location.CONSTRAINT_PREFIX, p);
        visit(constraint.getKeywords(), p);
        visit(constraint.getColumns(), p);
        visit(constraint.getReferences(), p);
        visit(constraint.getReferencedTable(), p);
        visit(constraint.getReferencedColumns(), p);
        visit(constraint.getOptions(), p);
        afterSyntax(constraint, p);
        return constraint;
    }

    @Override
    public Db2 visitColumnList(Db2.ColumnList columnList, PrintOutputCapture<P> p) {
        beforeSyntax(columnList, Space.Location.COLUMN_LIST_PREFIX, p);
        visit(columnList.getLParen(), p);
        visit(columnList.getNames(), p);
        visit(columnList.getRParen(), p);
        afterSyntax(columnList, p);
        return columnList;
    }

    @Override
    public Db2 visitDataType(Db2.DataType dataType, PrintOutputCapture<P> p) {
        beforeSyntax(dataType, Space.Location.DATA_TYPE_PREFIX, p);
        visit(dataType.getName(), p);
        visit(dataType.getArguments(), p);
        afterSyntax(dataType, p);
        return dataType;
    }

    @Override
    public Db2 visitIndexKey(Db2.IndexKey indexKey, PrintOutputCapture<P> p) {
        beforeSyntax(indexKey, Space.Location.INDEX_KEY_PREFIX, p);
        visit(indexKey.getName(), p);
        visit(indexKey.getDirection(), p);
        afterSyntax(indexKey, p);
        return indexKey;
    }

    @Override
    public Db2 visitName(Db2.Name name, PrintOutputCapture<P> p) {
        beforeSyntax(name, Space.Location.NAME_PREFIX, p);
        visit(name.getParts(), p);
        afterSyntax(name, p);
        return name;
    }

    @Override
    public Db2 visitWord(Db2.Word word, PrintOutputCapture<P> p) {
        beforeSyntax(word, Space.Location.WORD_PREFIX, p);
        p.append(word.getText());
        afterSyntax(word, p);
        return word;
    }

    @Override
    public Space visitSpace(Space space, Space.Location location, PrintOutputCapture<P> p) {
        p.append(space.getWhitespace());
        return space;
    }

    protected void beforeSyntax(Db2 d, Space.Location loc, PrintOutputCapture<P> p) {
        beforeSyntax(d.getPrefix(), d.getMarkers(), loc, p);
    }

    protected void beforeSyntax(Space prefix, Markers markers, Space.@Nullable Location loc, PrintOutputCapture<P> p) {
        for (Marker marker : markers.getMarkers()) {
            p.out.append(p.getMarkerPrinter().beforePrefix(marker, new Cursor(getCursor(), marker), DB2_MARKER_WRAPPER));
        }
        if (loc != null) {
            visitSpace(prefix, loc, p);
        }
        visitMarkers(markers, p);
        for (Marker marker : markers.getMarkers()) {
            p.out.append(p.getMarkerPrinter().beforeSyntax(marker, new Cursor(getCursor(), marker), DB2_MARKER_WRAPPER));
        }
    }

    protected void afterSyntax(Db2 d, PrintOutputCapture<P> p) {
        afterSyntax(d.getMarkers(), p);
    }

    protected void afterSyntax(Markers markers, PrintOutputCapture<P> p) {
        for (Marker marker : markers.getMarkers()) {
            p.out.append(p.getMarkerPrinter().afterSyntax(marker, new Cursor(getCursor(), marker), DB2_MARKER_WRAPPER));
        }
    }
}
