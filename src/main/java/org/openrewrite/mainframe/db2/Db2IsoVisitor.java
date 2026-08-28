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
package org.openrewrite.mainframe.db2;

import org.openrewrite.mainframe.db2.tree.Db2;

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
    public Db2.CreateTablespace visitCreateTablespace(Db2.CreateTablespace createTablespace, P p) {
        return (Db2.CreateTablespace) super.visitCreateTablespace(createTablespace, p);
    }

    @Override
    public Db2.CreateDatabase visitCreateDatabase(Db2.CreateDatabase createDatabase, P p) {
        return (Db2.CreateDatabase) super.visitCreateDatabase(createDatabase, p);
    }

    @Override
    public Db2.CreateStogroup visitCreateStogroup(Db2.CreateStogroup createStogroup, P p) {
        return (Db2.CreateStogroup) super.visitCreateStogroup(createStogroup, p);
    }

    @Override
    public Db2.CreateView visitCreateView(Db2.CreateView createView, P p) {
        return (Db2.CreateView) super.visitCreateView(createView, p);
    }

    @Override
    public Db2.CreateAlias visitCreateAlias(Db2.CreateAlias createAlias, P p) {
        return (Db2.CreateAlias) super.visitCreateAlias(createAlias, p);
    }

    @Override
    public Db2.CreateSynonym visitCreateSynonym(Db2.CreateSynonym createSynonym, P p) {
        return (Db2.CreateSynonym) super.visitCreateSynonym(createSynonym, p);
    }

    @Override
    public Db2.CreateSequence visitCreateSequence(Db2.CreateSequence createSequence, P p) {
        return (Db2.CreateSequence) super.visitCreateSequence(createSequence, p);
    }

    @Override
    public Db2.CreateRole visitCreateRole(Db2.CreateRole createRole, P p) {
        return (Db2.CreateRole) super.visitCreateRole(createRole, p);
    }

    @Override
    public Db2.CreateAuxiliaryTable visitCreateAuxiliaryTable(Db2.CreateAuxiliaryTable createAuxiliaryTable, P p) {
        return (Db2.CreateAuxiliaryTable) super.visitCreateAuxiliaryTable(createAuxiliaryTable, p);
    }

    @Override
    public Db2.CreateType visitCreateType(Db2.CreateType createType, P p) {
        return (Db2.CreateType) super.visitCreateType(createType, p);
    }

    @Override
    public Db2.CreateVariable visitCreateVariable(Db2.CreateVariable createVariable, P p) {
        return (Db2.CreateVariable) super.visitCreateVariable(createVariable, p);
    }

    @Override
    public Db2.CreateMask visitCreateMask(Db2.CreateMask createMask, P p) {
        return (Db2.CreateMask) super.visitCreateMask(createMask, p);
    }

    @Override
    public Db2.CreatePermission visitCreatePermission(Db2.CreatePermission createPermission, P p) {
        return (Db2.CreatePermission) super.visitCreatePermission(createPermission, p);
    }

    @Override
    public Db2.CreateTrustedContext visitCreateTrustedContext(Db2.CreateTrustedContext createTrustedContext, P p) {
        return (Db2.CreateTrustedContext) super.visitCreateTrustedContext(createTrustedContext, p);
    }

    @Override
    public Db2.CreateTrigger visitCreateTrigger(Db2.CreateTrigger createTrigger, P p) {
        return (Db2.CreateTrigger) super.visitCreateTrigger(createTrigger, p);
    }

    @Override
    public Db2.CreateProcedure visitCreateProcedure(Db2.CreateProcedure createProcedure, P p) {
        return (Db2.CreateProcedure) super.visitCreateProcedure(createProcedure, p);
    }

    @Override
    public Db2.CreateFunction visitCreateFunction(Db2.CreateFunction createFunction, P p) {
        return (Db2.CreateFunction) super.visitCreateFunction(createFunction, p);
    }

    @Override
    public Db2.DeclareGlobalTemporaryTable visitDeclareGlobalTemporaryTable(Db2.DeclareGlobalTemporaryTable declareGlobalTemporaryTable, P p) {
        return (Db2.DeclareGlobalTemporaryTable) super.visitDeclareGlobalTemporaryTable(declareGlobalTemporaryTable, p);
    }

    @Override
    public Db2.AlterTable visitAlterTable(Db2.AlterTable alterTable, P p) {
        return (Db2.AlterTable) super.visitAlterTable(alterTable, p);
    }

    @Override
    public Db2.AlterTablespace visitAlterTablespace(Db2.AlterTablespace alterTablespace, P p) {
        return (Db2.AlterTablespace) super.visitAlterTablespace(alterTablespace, p);
    }

    @Override
    public Db2.AlterIndex visitAlterIndex(Db2.AlterIndex alterIndex, P p) {
        return (Db2.AlterIndex) super.visitAlterIndex(alterIndex, p);
    }

    @Override
    public Db2.AlterDatabase visitAlterDatabase(Db2.AlterDatabase alterDatabase, P p) {
        return (Db2.AlterDatabase) super.visitAlterDatabase(alterDatabase, p);
    }

    @Override
    public Db2.AlterStogroup visitAlterStogroup(Db2.AlterStogroup alterStogroup, P p) {
        return (Db2.AlterStogroup) super.visitAlterStogroup(alterStogroup, p);
    }

    @Override
    public Db2.AlterSequence visitAlterSequence(Db2.AlterSequence alterSequence, P p) {
        return (Db2.AlterSequence) super.visitAlterSequence(alterSequence, p);
    }

    @Override
    public Db2.AlterView visitAlterView(Db2.AlterView alterView, P p) {
        return (Db2.AlterView) super.visitAlterView(alterView, p);
    }

    @Override
    public Db2.AlterProcedure visitAlterProcedure(Db2.AlterProcedure alterProcedure, P p) {
        return (Db2.AlterProcedure) super.visitAlterProcedure(alterProcedure, p);
    }

    @Override
    public Db2.AlterFunction visitAlterFunction(Db2.AlterFunction alterFunction, P p) {
        return (Db2.AlterFunction) super.visitAlterFunction(alterFunction, p);
    }

    @Override
    public Db2.AlterTrigger visitAlterTrigger(Db2.AlterTrigger alterTrigger, P p) {
        return (Db2.AlterTrigger) super.visitAlterTrigger(alterTrigger, p);
    }

    @Override
    public Db2.AlterMask visitAlterMask(Db2.AlterMask alterMask, P p) {
        return (Db2.AlterMask) super.visitAlterMask(alterMask, p);
    }

    @Override
    public Db2.AlterPermission visitAlterPermission(Db2.AlterPermission alterPermission, P p) {
        return (Db2.AlterPermission) super.visitAlterPermission(alterPermission, p);
    }

    @Override
    public Db2.AlterTrustedContext visitAlterTrustedContext(Db2.AlterTrustedContext alterTrustedContext, P p) {
        return (Db2.AlterTrustedContext) super.visitAlterTrustedContext(alterTrustedContext, p);
    }

    @Override
    public Db2.Drop visitDrop(Db2.Drop drop, P p) {
        return (Db2.Drop) super.visitDrop(drop, p);
    }

    @Override
    public Db2.Rename visitRename(Db2.Rename rename, P p) {
        return (Db2.Rename) super.visitRename(rename, p);
    }

    @Override
    public Db2.Comment visitComment(Db2.Comment comment, P p) {
        return (Db2.Comment) super.visitComment(comment, p);
    }

    @Override
    public Db2.Label visitLabel(Db2.Label label, P p) {
        return (Db2.Label) super.visitLabel(label, p);
    }

    @Override
    public Db2.Grant visitGrant(Db2.Grant grant, P p) {
        return (Db2.Grant) super.visitGrant(grant, p);
    }

    @Override
    public Db2.Revoke visitRevoke(Db2.Revoke revoke, P p) {
        return (Db2.Revoke) super.visitRevoke(revoke, p);
    }

    @Override
    public Db2.Set visitSet(Db2.Set set, P p) {
        return (Db2.Set) super.visitSet(set, p);
    }

    @Override
    public Db2.Commit visitCommit(Db2.Commit commit, P p) {
        return (Db2.Commit) super.visitCommit(commit, p);
    }

    @Override
    public Db2.Rollback visitRollback(Db2.Rollback rollback, P p) {
        return (Db2.Rollback) super.visitRollback(rollback, p);
    }

    @Override
    public Db2.Savepoint visitSavepoint(Db2.Savepoint savepoint, P p) {
        return (Db2.Savepoint) super.visitSavepoint(savepoint, p);
    }

    @Override
    public Db2.ReleaseSavepoint visitReleaseSavepoint(Db2.ReleaseSavepoint releaseSavepoint, P p) {
        return (Db2.ReleaseSavepoint) super.visitReleaseSavepoint(releaseSavepoint, p);
    }

    @Override
    public Db2.LockTable visitLockTable(Db2.LockTable lockTable, P p) {
        return (Db2.LockTable) super.visitLockTable(lockTable, p);
    }

    @Override
    public Db2.Insert visitInsert(Db2.Insert insert, P p) {
        return (Db2.Insert) super.visitInsert(insert, p);
    }

    @Override
    public Db2.Empty visitEmpty(Db2.Empty empty, P p) {
        return (Db2.Empty) super.visitEmpty(empty, p);
    }

    @Override
    public Db2.TableElements visitTableElements(Db2.TableElements tableElements, P p) {
        return (Db2.TableElements) super.visitTableElements(tableElements, p);
    }

    @Override
    public Db2.TableLike visitTableLike(Db2.TableLike tableLike, P p) {
        return (Db2.TableLike) super.visitTableLike(tableLike, p);
    }

    @Override
    public Db2.TableAsQuery visitTableAsQuery(Db2.TableAsQuery tableAsQuery, P p) {
        return (Db2.TableAsQuery) super.visitTableAsQuery(tableAsQuery, p);
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
    public Db2.DataType visitDataType(Db2.DataType dataType, P p) {
        return (Db2.DataType) super.visitDataType(dataType, p);
    }

    @Override
    public Db2.IndexKey visitIndexKey(Db2.IndexKey indexKey, P p) {
        return (Db2.IndexKey) super.visitIndexKey(indexKey, p);
    }

    @Override
    public Db2.Option visitOption(Db2.Option option, P p) {
        return (Db2.Option) super.visitOption(option, p);
    }

    @Override
    public Db2.Parameter visitParameter(Db2.Parameter parameter, P p) {
        return (Db2.Parameter) super.visitParameter(parameter, p);
    }

    @Override
    public Db2.Query visitQuery(Db2.Query query, P p) {
        return (Db2.Query) super.visitQuery(query, p);
    }

    @Override
    public Db2.Block visitBlock(Db2.Block block, P p) {
        return (Db2.Block) super.visitBlock(block, p);
    }

    @Override
    public Db2.Name visitName(Db2.Name name, P p) {
        return (Db2.Name) super.visitName(name, p);
    }

    @Override
    public Db2.Keyword visitKeyword(Db2.Keyword keyword, P p) {
        return (Db2.Keyword) super.visitKeyword(keyword, p);
    }

    @Override
    public Db2.Word visitWord(Db2.Word word, P p) {
        return (Db2.Word) super.visitWord(word, p);
    }
}
