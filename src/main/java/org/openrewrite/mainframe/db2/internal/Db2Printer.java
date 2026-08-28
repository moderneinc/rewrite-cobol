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
package org.openrewrite.mainframe.db2.internal;

import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.PrintOutputCapture;
import org.openrewrite.mainframe.db2.Db2Visitor;
import org.openrewrite.mainframe.db2.marker.Semicolon;
import org.openrewrite.mainframe.db2.tree.*;
import org.openrewrite.marker.Marker;
import org.openrewrite.marker.Markers;

import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Writes back what the tree does not carry: the brackets and commas around a
 * {@link Db2Container}, the keyword a {@link Db2.Keyword} names, and the semicolon a statement's
 * {@link Semicolon} marker says it had.
 */
public class Db2Printer<P> extends Db2Visitor<PrintOutputCapture<P>> {
    public static final UnaryOperator<String> DB2_MARKER_WRAPPER =
            out -> "~~" + out + (out.isEmpty() ? "" : "~~") + ">";

    @Override
    public Db2 visitDdl(Db2.Ddl ddl, PrintOutputCapture<P> p) {
        beforeSyntax(ddl, Space.Location.DDL_PREFIX, p);
        for (Db2RightPadded<Statement> statement : ddl.getPadding().getStatements()) {
            printRightPadded(statement, p);
        }
        afterSyntax(ddl, p);
        visitSpace(ddl.getEof(), Space.Location.DDL_EOF, p);
        return ddl;
    }

    @Override
    public Db2 visitCreateTable(Db2.CreateTable createTable, PrintOutputCapture<P> p) {
        beforeSyntax(createTable, Space.Location.CREATE_TABLE_PREFIX, p);
        visit(createTable.getKeywords(), p);
        visit(createTable.getName(), p);
        visit(createTable.getContents(), p);
        visit(createTable.getOptions(), p);
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
        printContainer("(", createIndex.getKeys(), ",", ")", p);
        visit(createIndex.getOptions(), p);
        afterSyntax(createIndex, p);
        return createIndex;
    }

    @Override
    public Db2 visitCreateTablespace(Db2.CreateTablespace createTablespace, PrintOutputCapture<P> p) {
        beforeSyntax(createTablespace, Space.Location.CREATE_TABLESPACE_PREFIX, p);
        visit(createTablespace.getKeywords(), p);
        visit(createTablespace.getName(), p);
        visit(createTablespace.getIn(), p);
        visit(createTablespace.getDatabase(), p);
        visit(createTablespace.getOptions(), p);
        afterSyntax(createTablespace, p);
        return createTablespace;
    }

    @Override
    public Db2 visitCreateDatabase(Db2.CreateDatabase createDatabase, PrintOutputCapture<P> p) {
        beforeSyntax(createDatabase, Space.Location.CREATE_DATABASE_PREFIX, p);
        visit(createDatabase.getKeywords(), p);
        visit(createDatabase.getName(), p);
        visit(createDatabase.getOptions(), p);
        afterSyntax(createDatabase, p);
        return createDatabase;
    }

    @Override
    public Db2 visitCreateStogroup(Db2.CreateStogroup createStogroup, PrintOutputCapture<P> p) {
        beforeSyntax(createStogroup, Space.Location.CREATE_STOGROUP_PREFIX, p);
        visit(createStogroup.getKeywords(), p);
        visit(createStogroup.getName(), p);
        visit(createStogroup.getOptions(), p);
        afterSyntax(createStogroup, p);
        return createStogroup;
    }

    @Override
    public Db2 visitCreateView(Db2.CreateView createView, PrintOutputCapture<P> p) {
        beforeSyntax(createView, Space.Location.CREATE_VIEW_PREFIX, p);
        visit(createView.getKeywords(), p);
        visit(createView.getName(), p);
        printContainer("(", createView.getColumns(), ",", ")", p);
        printLeftPadded("AS", createView.getQuery(), p);
        visit(createView.getOptions(), p);
        afterSyntax(createView, p);
        return createView;
    }

    @Override
    public Db2 visitCreateAlias(Db2.CreateAlias createAlias, PrintOutputCapture<P> p) {
        beforeSyntax(createAlias, Space.Location.CREATE_ALIAS_PREFIX, p);
        visit(createAlias.getKeywords(), p);
        visit(createAlias.getName(), p);
        printLeftPadded("", createAlias.getTarget(), p);
        afterSyntax(createAlias, p);
        return createAlias;
    }

    @Override
    public Db2 visitCreateSynonym(Db2.CreateSynonym createSynonym, PrintOutputCapture<P> p) {
        beforeSyntax(createSynonym, Space.Location.CREATE_SYNONYM_PREFIX, p);
        visit(createSynonym.getKeywords(), p);
        visit(createSynonym.getName(), p);
        printLeftPadded("", createSynonym.getTarget(), p);
        afterSyntax(createSynonym, p);
        return createSynonym;
    }

    @Override
    public Db2 visitCreateSequence(Db2.CreateSequence createSequence, PrintOutputCapture<P> p) {
        beforeSyntax(createSequence, Space.Location.CREATE_SEQUENCE_PREFIX, p);
        visit(createSequence.getKeywords(), p);
        visit(createSequence.getName(), p);
        printLeftPadded("AS", createSequence.getType(), p);
        visit(createSequence.getOptions(), p);
        afterSyntax(createSequence, p);
        return createSequence;
    }

    @Override
    public Db2 visitCreateRole(Db2.CreateRole createRole, PrintOutputCapture<P> p) {
        beforeSyntax(createRole, Space.Location.CREATE_ROLE_PREFIX, p);
        visit(createRole.getKeywords(), p);
        visit(createRole.getName(), p);
        afterSyntax(createRole, p);
        return createRole;
    }

    @Override
    public Db2 visitCreateAuxiliaryTable(Db2.CreateAuxiliaryTable createAuxiliaryTable, PrintOutputCapture<P> p) {
        beforeSyntax(createAuxiliaryTable, Space.Location.CREATE_AUXILIARY_TABLE_PREFIX, p);
        visit(createAuxiliaryTable.getKeywords(), p);
        visit(createAuxiliaryTable.getName(), p);
        visit(createAuxiliaryTable.getOptions(), p);
        afterSyntax(createAuxiliaryTable, p);
        return createAuxiliaryTable;
    }

    @Override
    public Db2 visitCreateType(Db2.CreateType createType, PrintOutputCapture<P> p) {
        beforeSyntax(createType, Space.Location.CREATE_TYPE_PREFIX, p);
        visit(createType.getKeywords(), p);
        visit(createType.getName(), p);
        printLeftPadded("AS", createType.getType(), p);
        visit(createType.getOptions(), p);
        afterSyntax(createType, p);
        return createType;
    }

    @Override
    public Db2 visitCreateVariable(Db2.CreateVariable createVariable, PrintOutputCapture<P> p) {
        beforeSyntax(createVariable, Space.Location.CREATE_VARIABLE_PREFIX, p);
        visit(createVariable.getKeywords(), p);
        visit(createVariable.getName(), p);
        visit(createVariable.getType(), p);
        visit(createVariable.getOptions(), p);
        afterSyntax(createVariable, p);
        return createVariable;
    }

    @Override
    public Db2 visitCreateMask(Db2.CreateMask createMask, PrintOutputCapture<P> p) {
        beforeSyntax(createMask, Space.Location.CREATE_MASK_PREFIX, p);
        visit(createMask.getKeywords(), p);
        visit(createMask.getName(), p);
        visit(createMask.getTable(), p);
        visit(createMask.getOptions(), p);
        afterSyntax(createMask, p);
        return createMask;
    }

    @Override
    public Db2 visitCreatePermission(Db2.CreatePermission createPermission, PrintOutputCapture<P> p) {
        beforeSyntax(createPermission, Space.Location.CREATE_PERMISSION_PREFIX, p);
        visit(createPermission.getKeywords(), p);
        visit(createPermission.getName(), p);
        visit(createPermission.getTable(), p);
        visit(createPermission.getOptions(), p);
        afterSyntax(createPermission, p);
        return createPermission;
    }

    @Override
    public Db2 visitCreateTrustedContext(Db2.CreateTrustedContext createTrustedContext, PrintOutputCapture<P> p) {
        beforeSyntax(createTrustedContext, Space.Location.CREATE_TRUSTED_CONTEXT_PREFIX, p);
        visit(createTrustedContext.getKeywords(), p);
        visit(createTrustedContext.getName(), p);
        visit(createTrustedContext.getOptions(), p);
        afterSyntax(createTrustedContext, p);
        return createTrustedContext;
    }

    @Override
    public Db2 visitCreateTrigger(Db2.CreateTrigger createTrigger, PrintOutputCapture<P> p) {
        beforeSyntax(createTrigger, Space.Location.CREATE_TRIGGER_PREFIX, p);
        visit(createTrigger.getKeywords(), p);
        visit(createTrigger.getName(), p);
        visit(createTrigger.getEvents(), p);
        visit(createTrigger.getTable(), p);
        visit(createTrigger.getOptions(), p);
        visit(createTrigger.getBody(), p);
        afterSyntax(createTrigger, p);
        return createTrigger;
    }

    @Override
    public Db2 visitCreateProcedure(Db2.CreateProcedure createProcedure, PrintOutputCapture<P> p) {
        beforeSyntax(createProcedure, Space.Location.CREATE_PROCEDURE_PREFIX, p);
        visit(createProcedure.getKeywords(), p);
        visit(createProcedure.getName(), p);
        printContainer("(", createProcedure.getParameters(), ",", ")", p);
        visit(createProcedure.getClauses(), p);
        visit(createProcedure.getBody(), p);
        afterSyntax(createProcedure, p);
        return createProcedure;
    }

    @Override
    public Db2 visitCreateFunction(Db2.CreateFunction createFunction, PrintOutputCapture<P> p) {
        beforeSyntax(createFunction, Space.Location.CREATE_FUNCTION_PREFIX, p);
        visit(createFunction.getKeywords(), p);
        visit(createFunction.getName(), p);
        printContainer("(", createFunction.getParameters(), ",", ")", p);
        visit(createFunction.getClauses(), p);
        visit(createFunction.getBody(), p);
        afterSyntax(createFunction, p);
        return createFunction;
    }

    @Override
    public Db2 visitDeclareGlobalTemporaryTable(Db2.DeclareGlobalTemporaryTable declareGlobalTemporaryTable, PrintOutputCapture<P> p) {
        beforeSyntax(declareGlobalTemporaryTable, Space.Location.DECLARE_GLOBAL_TEMPORARY_TABLE_PREFIX, p);
        visit(declareGlobalTemporaryTable.getKeywords(), p);
        visit(declareGlobalTemporaryTable.getName(), p);
        visit(declareGlobalTemporaryTable.getContents(), p);
        visit(declareGlobalTemporaryTable.getOptions(), p);
        afterSyntax(declareGlobalTemporaryTable, p);
        return declareGlobalTemporaryTable;
    }

    @Override
    public Db2 visitAlterTable(Db2.AlterTable alterTable, PrintOutputCapture<P> p) {
        beforeSyntax(alterTable, Space.Location.ALTER_TABLE_PREFIX, p);
        visit(alterTable.getKeywords(), p);
        visit(alterTable.getName(), p);
        visit(alterTable.getActions(), p);
        afterSyntax(alterTable, p);
        return alterTable;
    }

    @Override
    public Db2 visitAlterTablespace(Db2.AlterTablespace alterTablespace, PrintOutputCapture<P> p) {
        beforeSyntax(alterTablespace, Space.Location.ALTER_TABLESPACE_PREFIX, p);
        visit(alterTablespace.getKeywords(), p);
        visit(alterTablespace.getName(), p);
        visit(alterTablespace.getIn(), p);
        visit(alterTablespace.getDatabase(), p);
        visit(alterTablespace.getOptions(), p);
        afterSyntax(alterTablespace, p);
        return alterTablespace;
    }

    @Override
    public Db2 visitAlterIndex(Db2.AlterIndex alterIndex, PrintOutputCapture<P> p) {
        beforeSyntax(alterIndex, Space.Location.ALTER_INDEX_PREFIX, p);
        visit(alterIndex.getKeywords(), p);
        visit(alterIndex.getName(), p);
        visit(alterIndex.getOptions(), p);
        afterSyntax(alterIndex, p);
        return alterIndex;
    }

    @Override
    public Db2 visitAlterDatabase(Db2.AlterDatabase alterDatabase, PrintOutputCapture<P> p) {
        beforeSyntax(alterDatabase, Space.Location.ALTER_DATABASE_PREFIX, p);
        visit(alterDatabase.getKeywords(), p);
        visit(alterDatabase.getName(), p);
        visit(alterDatabase.getOptions(), p);
        afterSyntax(alterDatabase, p);
        return alterDatabase;
    }

    @Override
    public Db2 visitAlterStogroup(Db2.AlterStogroup alterStogroup, PrintOutputCapture<P> p) {
        beforeSyntax(alterStogroup, Space.Location.ALTER_STOGROUP_PREFIX, p);
        visit(alterStogroup.getKeywords(), p);
        visit(alterStogroup.getName(), p);
        visit(alterStogroup.getOptions(), p);
        afterSyntax(alterStogroup, p);
        return alterStogroup;
    }

    @Override
    public Db2 visitAlterSequence(Db2.AlterSequence alterSequence, PrintOutputCapture<P> p) {
        beforeSyntax(alterSequence, Space.Location.ALTER_SEQUENCE_PREFIX, p);
        visit(alterSequence.getKeywords(), p);
        visit(alterSequence.getName(), p);
        visit(alterSequence.getOptions(), p);
        afterSyntax(alterSequence, p);
        return alterSequence;
    }

    @Override
    public Db2 visitAlterView(Db2.AlterView alterView, PrintOutputCapture<P> p) {
        beforeSyntax(alterView, Space.Location.ALTER_VIEW_PREFIX, p);
        visit(alterView.getKeywords(), p);
        visit(alterView.getName(), p);
        visit(alterView.getOptions(), p);
        afterSyntax(alterView, p);
        return alterView;
    }

    @Override
    public Db2 visitAlterProcedure(Db2.AlterProcedure alterProcedure, PrintOutputCapture<P> p) {
        beforeSyntax(alterProcedure, Space.Location.ALTER_PROCEDURE_PREFIX, p);
        visit(alterProcedure.getKeywords(), p);
        visit(alterProcedure.getName(), p);
        printContainer("(", alterProcedure.getParameters(), ",", ")", p);
        visit(alterProcedure.getClauses(), p);
        visit(alterProcedure.getBody(), p);
        afterSyntax(alterProcedure, p);
        return alterProcedure;
    }

    @Override
    public Db2 visitAlterFunction(Db2.AlterFunction alterFunction, PrintOutputCapture<P> p) {
        beforeSyntax(alterFunction, Space.Location.ALTER_FUNCTION_PREFIX, p);
        visit(alterFunction.getKeywords(), p);
        visit(alterFunction.getName(), p);
        printContainer("(", alterFunction.getParameters(), ",", ")", p);
        visit(alterFunction.getClauses(), p);
        visit(alterFunction.getBody(), p);
        afterSyntax(alterFunction, p);
        return alterFunction;
    }

    @Override
    public Db2 visitAlterTrigger(Db2.AlterTrigger alterTrigger, PrintOutputCapture<P> p) {
        beforeSyntax(alterTrigger, Space.Location.ALTER_TRIGGER_PREFIX, p);
        visit(alterTrigger.getKeywords(), p);
        visit(alterTrigger.getName(), p);
        visit(alterTrigger.getOptions(), p);
        afterSyntax(alterTrigger, p);
        return alterTrigger;
    }

    @Override
    public Db2 visitAlterMask(Db2.AlterMask alterMask, PrintOutputCapture<P> p) {
        beforeSyntax(alterMask, Space.Location.ALTER_MASK_PREFIX, p);
        visit(alterMask.getKeywords(), p);
        visit(alterMask.getName(), p);
        visit(alterMask.getOptions(), p);
        afterSyntax(alterMask, p);
        return alterMask;
    }

    @Override
    public Db2 visitAlterPermission(Db2.AlterPermission alterPermission, PrintOutputCapture<P> p) {
        beforeSyntax(alterPermission, Space.Location.ALTER_PERMISSION_PREFIX, p);
        visit(alterPermission.getKeywords(), p);
        visit(alterPermission.getName(), p);
        visit(alterPermission.getOptions(), p);
        afterSyntax(alterPermission, p);
        return alterPermission;
    }

    @Override
    public Db2 visitAlterTrustedContext(Db2.AlterTrustedContext alterTrustedContext, PrintOutputCapture<P> p) {
        beforeSyntax(alterTrustedContext, Space.Location.ALTER_TRUSTED_CONTEXT_PREFIX, p);
        visit(alterTrustedContext.getKeywords(), p);
        visit(alterTrustedContext.getName(), p);
        visit(alterTrustedContext.getOptions(), p);
        afterSyntax(alterTrustedContext, p);
        return alterTrustedContext;
    }

    @Override
    public Db2 visitDrop(Db2.Drop drop, PrintOutputCapture<P> p) {
        beforeSyntax(drop, Space.Location.DROP_PREFIX, p);
        visit(drop.getKeywords(), p);
        visit(drop.getName(), p);
        visit(drop.getOptions(), p);
        afterSyntax(drop, p);
        return drop;
    }

    @Override
    public Db2 visitRename(Db2.Rename rename, PrintOutputCapture<P> p) {
        beforeSyntax(rename, Space.Location.RENAME_PREFIX, p);
        visit(rename.getKeywords(), p);
        visit(rename.getName(), p);
        printLeftPadded("", rename.getNewName(), p);
        afterSyntax(rename, p);
        return rename;
    }

    @Override
    public Db2 visitComment(Db2.Comment comment, PrintOutputCapture<P> p) {
        beforeSyntax(comment, Space.Location.COMMENT_PREFIX, p);
        visit(comment.getKeywords(), p);
        visit(comment.getTarget(), p);
        printLeftPadded("IS", comment.getText(), p);
        afterSyntax(comment, p);
        return comment;
    }

    @Override
    public Db2 visitLabel(Db2.Label label, PrintOutputCapture<P> p) {
        beforeSyntax(label, Space.Location.LABEL_PREFIX, p);
        visit(label.getKeywords(), p);
        visit(label.getTarget(), p);
        printLeftPadded("IS", label.getText(), p);
        afterSyntax(label, p);
        return label;
    }

    @Override
    public Db2 visitGrant(Db2.Grant grant, PrintOutputCapture<P> p) {
        beforeSyntax(grant, Space.Location.GRANT_PREFIX, p);
        visit(grant.getKeywords(), p);
        printContainer("", grant.getPrivileges(), ",", "", p);
        visit(grant.getOn(), p);
        printContainer("", grant.getObjects(), ",", "", p);
        visit(grant.getTo(), p);
        printContainer("", grant.getGrantees(), ",", "", p);
        visit(grant.getOptions(), p);
        afterSyntax(grant, p);
        return grant;
    }

    @Override
    public Db2 visitRevoke(Db2.Revoke revoke, PrintOutputCapture<P> p) {
        beforeSyntax(revoke, Space.Location.REVOKE_PREFIX, p);
        visit(revoke.getKeywords(), p);
        printContainer("", revoke.getPrivileges(), ",", "", p);
        visit(revoke.getOn(), p);
        printContainer("", revoke.getObjects(), ",", "", p);
        visit(revoke.getTo(), p);
        printContainer("", revoke.getGrantees(), ",", "", p);
        visit(revoke.getOptions(), p);
        afterSyntax(revoke, p);
        return revoke;
    }

    @Override
    public Db2 visitSet(Db2.Set set, PrintOutputCapture<P> p) {
        beforeSyntax(set, Space.Location.SET_PREFIX, p);
        visit(set.getKeywords(), p);
        printLeftPadded("=", set.getValue(), p);
        afterSyntax(set, p);
        return set;
    }

    @Override
    public Db2 visitCommit(Db2.Commit commit, PrintOutputCapture<P> p) {
        beforeSyntax(commit, Space.Location.COMMIT_PREFIX, p);
        visit(commit.getKeywords(), p);
        afterSyntax(commit, p);
        return commit;
    }

    @Override
    public Db2 visitRollback(Db2.Rollback rollback, PrintOutputCapture<P> p) {
        beforeSyntax(rollback, Space.Location.ROLLBACK_PREFIX, p);
        visit(rollback.getKeywords(), p);
        printLeftPadded("", rollback.getSavepoint(), p);
        afterSyntax(rollback, p);
        return rollback;
    }

    @Override
    public Db2 visitSavepoint(Db2.Savepoint savepoint, PrintOutputCapture<P> p) {
        beforeSyntax(savepoint, Space.Location.SAVEPOINT_PREFIX, p);
        visit(savepoint.getKeywords(), p);
        visit(savepoint.getName(), p);
        visit(savepoint.getOptions(), p);
        afterSyntax(savepoint, p);
        return savepoint;
    }

    @Override
    public Db2 visitReleaseSavepoint(Db2.ReleaseSavepoint releaseSavepoint, PrintOutputCapture<P> p) {
        beforeSyntax(releaseSavepoint, Space.Location.RELEASE_SAVEPOINT_PREFIX, p);
        visit(releaseSavepoint.getKeywords(), p);
        visit(releaseSavepoint.getName(), p);
        afterSyntax(releaseSavepoint, p);
        return releaseSavepoint;
    }

    @Override
    public Db2 visitLockTable(Db2.LockTable lockTable, PrintOutputCapture<P> p) {
        beforeSyntax(lockTable, Space.Location.LOCK_TABLE_PREFIX, p);
        visit(lockTable.getKeywords(), p);
        visit(lockTable.getName(), p);
        afterSyntax(lockTable, p);
        return lockTable;
    }

    @Override
    public Db2 visitInsert(Db2.Insert insert, PrintOutputCapture<P> p) {
        beforeSyntax(insert, Space.Location.INSERT_PREFIX, p);
        visit(insert.getKeywords(), p);
        visit(insert.getTable(), p);
        printContainer("(", insert.getColumns(), ",", ")", p);
        printContainer("", insert.getValues(), ",", "", p);
        afterSyntax(insert, p);
        return insert;
    }

    @Override
    public Db2 visitEmpty(Db2.Empty empty, PrintOutputCapture<P> p) {
        beforeSyntax(empty, Space.Location.EMPTY_PREFIX, p);
        afterSyntax(empty, p);
        return empty;
    }

    @Override
    public Db2 visitTableElements(Db2.TableElements tableElements, PrintOutputCapture<P> p) {
        beforeSyntax(tableElements, Space.Location.TABLE_ELEMENTS_PREFIX, p);
        printContainer("(", tableElements.getElements(), ",", ")", p);
        afterSyntax(tableElements, p);
        return tableElements;
    }

    @Override
    public Db2 visitTableLike(Db2.TableLike tableLike, PrintOutputCapture<P> p) {
        beforeSyntax(tableLike, Space.Location.TABLE_LIKE_PREFIX, p);
        visit(tableLike.getKeywords(), p);
        visit(tableLike.getTable(), p);
        visit(tableLike.getOptions(), p);
        afterSyntax(tableLike, p);
        return tableLike;
    }

    @Override
    public Db2 visitTableAsQuery(Db2.TableAsQuery tableAsQuery, PrintOutputCapture<P> p) {
        beforeSyntax(tableAsQuery, Space.Location.TABLE_AS_QUERY_PREFIX, p);
        visit(tableAsQuery.getKeywords(), p);
        printContainer("(", tableAsQuery.getQuery(), ",", ")", p);
        visit(tableAsQuery.getOptions(), p);
        afterSyntax(tableAsQuery, p);
        return tableAsQuery;
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
        visit(constraint.getConstraintKeyword(), p);
        visit(constraint.getConstraintName(), p);
        visit(constraint.getKeywords(), p);
        visit(constraint.getKeyName(), p);
        printContainer("(", constraint.getColumns(), ",", ")", p);
        visit(constraint.getReferences(), p);
        visit(constraint.getReferencedTable(), p);
        printContainer("(", constraint.getReferencedColumns(), ",", ")", p);
        visit(constraint.getOptions(), p);
        afterSyntax(constraint, p);
        return constraint;
    }

    @Override
    public Db2 visitDataType(Db2.DataType dataType, PrintOutputCapture<P> p) {
        beforeSyntax(dataType, Space.Location.DATA_TYPE_PREFIX, p);
        visit(dataType.getName(), p);
        printContainer("(", dataType.getArguments(), ",", ")", p);
        visit(dataType.getAttributes(), p);
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
    public Db2 visitOption(Db2.Option option, PrintOutputCapture<P> p) {
        beforeSyntax(option, Space.Location.OPTION_PREFIX, p);
        visit(option.getKeywords(), p);
        visit(option.getValues(), p);
        afterSyntax(option, p);
        return option;
    }

    @Override
    public Db2 visitParameter(Db2.Parameter parameter, PrintOutputCapture<P> p) {
        beforeSyntax(parameter, Space.Location.PARAMETER_PREFIX, p);
        visit(parameter.getMode(), p);
        visit(parameter.getName(), p);
        visit(parameter.getType(), p);
        afterSyntax(parameter, p);
        return parameter;
    }

    @Override
    public Db2 visitQuery(Db2.Query query, PrintOutputCapture<P> p) {
        beforeSyntax(query, Space.Location.QUERY_PREFIX, p);
        visit(query.getParts(), p);
        afterSyntax(query, p);
        return query;
    }

    @Override
    public Db2 visitBlock(Db2.Block block, PrintOutputCapture<P> p) {
        beforeSyntax(block, Space.Location.BLOCK_PREFIX, p);
        visit(block.getKeywords(), p);
        visit(block.getBody(), p);
        visit(block.getEnd(), p);
        visit(block.getLabel(), p);
        afterSyntax(block, p);
        return block;
    }

    @Override
    public Db2 visitName(Db2.Name name, PrintOutputCapture<P> p) {
        beforeSyntax(name, Space.Location.NAME_PREFIX, p);
        visit(name.getParts(), p);
        afterSyntax(name, p);
        return name;
    }

    @Override
    public Db2 visitKeyword(Db2.Keyword keyword, PrintOutputCapture<P> p) {
        beforeSyntax(keyword, Space.Location.KEYWORD_PREFIX, p);
        p.append(keyword.getText());
        afterSyntax(keyword, p);
        return keyword;
    }

    @Override
    public Db2 visitWord(Db2.Word word, PrintOutputCapture<P> p) {
        beforeSyntax(word, Space.Location.WORD_PREFIX, p);
        p.append(word.getText());
        afterSyntax(word, p);
        return word;
    }

    protected void printContainer(String open, @Nullable Db2Container<? extends Db2> container,
                                  String between,
                                  String close, PrintOutputCapture<P> p) {
        if (container == null) {
            return;
        }
        visitSpace(container.getBefore(), Space.Location.CONTAINER_BEFORE, p);
        p.append(open);
        printRightPadded(container.getPadding().getElements(), between, p);
        p.append(close);
    }

    protected void printRightPadded(List<? extends Db2RightPadded<? extends Db2>> elements,
                                    String between, PrintOutputCapture<P> p) {
        for (int i = 0; i < elements.size(); i++) {
            Db2RightPadded<? extends Db2> element = elements.get(i);
            visit(element.getElement(), p);
            visitSpace(element.getAfter(), Space.Location.RIGHT_PADDED_AFTER, p);
            visitMarkers(element.getMarkers(), p);
            if (i < elements.size() - 1) {
                p.append(between);
            }
        }
    }

    protected void printRightPadded(Db2RightPadded<? extends Db2> padded, PrintOutputCapture<P> p) {
        visit(padded.getElement(), p);
        visitSpace(padded.getAfter(), Space.Location.RIGHT_PADDED_AFTER, p);
        visitMarkers(padded.getMarkers(), p);
    }

    protected void printLeftPadded(String operator, @Nullable Db2LeftPadded<? extends Db2> padded,
                                   PrintOutputCapture<P> p) {
        if (padded == null) {
            return;
        }
        visitSpace(padded.getBefore(), Space.Location.LEFT_PADDED_BEFORE, p);
        p.append(operator);
        visit(padded.getElement(), p);
    }

    /**
     * A statement's terminator, written from the marker its padding carries — the same hook Groovy
     * uses for an optional semicolon, so no statement has to handle it.
     */
    @Override
    public <M extends Marker> M visitMarker(Marker marker, PrintOutputCapture<P> p) {
        if (marker instanceof Semicolon) {
            p.append(';');
        }
        return super.visitMarker(marker, p);
    }

    @Override
    public Space visitSpace(Space space, Space.Location location, PrintOutputCapture<P> p) {
        p.append(space.getWhitespace());
        return space;
    }

    protected void beforeSyntax(Db2 d, Space.Location loc, PrintOutputCapture<P> p) {
        for (Marker marker : d.getMarkers().getMarkers()) {
            p.out.append(p.getMarkerPrinter().beforePrefix(marker, new Cursor(getCursor(), marker), DB2_MARKER_WRAPPER));
        }
        visitSpace(d.getPrefix(), loc, p);
        visitMarkers(d.getMarkers(), p);
        for (Marker marker : d.getMarkers().getMarkers()) {
            p.out.append(p.getMarkerPrinter().beforeSyntax(marker, new Cursor(getCursor(), marker), DB2_MARKER_WRAPPER));
        }
    }

    protected void afterSyntax(Db2 d, PrintOutputCapture<P> p) {
        for (Marker marker : d.getMarkers().getMarkers()) {
            p.out.append(p.getMarkerPrinter().afterSyntax(marker, new Cursor(getCursor(), marker), DB2_MARKER_WRAPPER));
        }
    }
}
