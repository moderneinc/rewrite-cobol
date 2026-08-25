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
// Generated from /Users/jon/Projects/github/openrewrite/rewrite-cobol/.claude/worktrees/ims-or-cics/src/main/antlr-db2/DB2Parser.g4 by ANTLR 4.13.2
package org.openrewrite.db2.internal.grammar;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link DB2Parser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface DB2ParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link DB2Parser#compilationUnit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompilationUnit(DB2Parser.CompilationUnitContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(DB2Parser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#queryStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQueryStatement(DB2Parser.QueryStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#terminator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerminator(DB2Parser.TerminatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#createTable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateTable(DB2Parser.CreateTableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#tableContents}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableContents(DB2Parser.TableContentsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#copyOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCopyOption(DB2Parser.CopyOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#tableElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableElement(DB2Parser.TableElementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#columnDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnDefinition(DB2Parser.ColumnDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#columnAttribute}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnAttribute(DB2Parser.ColumnAttributeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#generatedAs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGeneratedAs(DB2Parser.GeneratedAsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#defaultValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefaultValue(DB2Parser.DefaultValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#periodDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPeriodDefinition(DB2Parser.PeriodDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#tableConstraint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableConstraint(DB2Parser.TableConstraintContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#constraintBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstraintBody(DB2Parser.ConstraintBodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#referentialAction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReferentialAction(DB2Parser.ReferentialActionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#constraintOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstraintOption(DB2Parser.ConstraintOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#columnList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnList(DB2Parser.ColumnListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#tableOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableOption(DB2Parser.TableOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#hashSpace}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHashSpace(DB2Parser.HashSpaceContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#partitionKey}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitionKey(DB2Parser.PartitionKeyContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#partitionClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitionClause(DB2Parser.PartitionClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#partitionSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitionSpec(DB2Parser.PartitionSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#createIndex}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateIndex(DB2Parser.CreateIndexContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#indexModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexModifier(DB2Parser.IndexModifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#indexKey}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexKey(DB2Parser.IndexKeyContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#indexOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexOption(DB2Parser.IndexOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#createTablespace}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateTablespace(DB2Parser.CreateTablespaceContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#tablespaceOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTablespaceOption(DB2Parser.TablespaceOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#createDatabase}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateDatabase(DB2Parser.CreateDatabaseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#databaseOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDatabaseOption(DB2Parser.DatabaseOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#createStogroup}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateStogroup(DB2Parser.CreateStogroupContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#stogroupOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStogroupOption(DB2Parser.StogroupOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#createView}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateView(DB2Parser.CreateViewContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#viewOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitViewOption(DB2Parser.ViewOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#createAlias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateAlias(DB2Parser.CreateAliasContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#createSynonym}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateSynonym(DB2Parser.CreateSynonymContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#createSequence}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateSequence(DB2Parser.CreateSequenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#sequenceOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSequenceOption(DB2Parser.SequenceOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#createRole}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateRole(DB2Parser.CreateRoleContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#createAuxiliaryTable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateAuxiliaryTable(DB2Parser.CreateAuxiliaryTableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#createType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateType(DB2Parser.CreateTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#createVariable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateVariable(DB2Parser.CreateVariableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#createMask}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateMask(DB2Parser.CreateMaskContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#createPermission}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreatePermission(DB2Parser.CreatePermissionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#createTrustedContext}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateTrustedContext(DB2Parser.CreateTrustedContextContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#trustedContextOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrustedContextOption(DB2Parser.TrustedContextOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#trustedAttribute}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrustedAttribute(DB2Parser.TrustedAttributeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#trustedUser}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrustedUser(DB2Parser.TrustedUserContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#caseExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCaseExpression(DB2Parser.CaseExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#declareGlobalTemporaryTable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclareGlobalTemporaryTable(DB2Parser.DeclareGlobalTemporaryTableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#createTrigger}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateTrigger(DB2Parser.CreateTriggerContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#triggerEvent}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriggerEvent(DB2Parser.TriggerEventContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#triggerCorrelation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriggerCorrelation(DB2Parser.TriggerCorrelationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#triggerGranularity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriggerGranularity(DB2Parser.TriggerGranularityContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#createProcedure}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateProcedure(DB2Parser.CreateProcedureContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#createFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateFunction(DB2Parser.CreateFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#routineParameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoutineParameter(DB2Parser.RoutineParameterContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#routineClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoutineClause(DB2Parser.RoutineClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#compoundStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompoundStatement(DB2Parser.CompoundStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#bodyItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBodyItem(DB2Parser.BodyItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#triggeredStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriggeredStatement(DB2Parser.TriggeredStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#alterTable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterTable(DB2Parser.AlterTableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#alterTableAction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterTableAction(DB2Parser.AlterTableActionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#alterColumnAction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterColumnAction(DB2Parser.AlterColumnActionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#alterTablespace}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterTablespace(DB2Parser.AlterTablespaceContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#alterIndex}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterIndex(DB2Parser.AlterIndexContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#alterDatabase}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterDatabase(DB2Parser.AlterDatabaseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#alterStogroup}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterStogroup(DB2Parser.AlterStogroupContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#alterSequence}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterSequence(DB2Parser.AlterSequenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#alterView}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterView(DB2Parser.AlterViewContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#alterProcedure}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterProcedure(DB2Parser.AlterProcedureContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#alterFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterFunction(DB2Parser.AlterFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#alterTrigger}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterTrigger(DB2Parser.AlterTriggerContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#alterMask}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterMask(DB2Parser.AlterMaskContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#alterPermission}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterPermission(DB2Parser.AlterPermissionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#alterTrustedContext}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterTrustedContext(DB2Parser.AlterTrustedContextContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#alterStogroupAction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterStogroupAction(DB2Parser.AlterStogroupActionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#dropStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropStatement(DB2Parser.DropStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#droppedObject}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDroppedObject(DB2Parser.DroppedObjectContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#grantStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrantStatement(DB2Parser.GrantStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#revokeStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRevokeStatement(DB2Parser.RevokeStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#privilege}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrivilege(DB2Parser.PrivilegeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#privilegeObject}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrivilegeObject(DB2Parser.PrivilegeObjectContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#qualifiedNameList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualifiedNameList(DB2Parser.QualifiedNameListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#grantee}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrantee(DB2Parser.GranteeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#commentStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommentStatement(DB2Parser.CommentStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#commentTarget}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommentTarget(DB2Parser.CommentTargetContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#labelStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLabelStatement(DB2Parser.LabelStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#labelTarget}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLabelTarget(DB2Parser.LabelTargetContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#renameStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRenameStatement(DB2Parser.RenameStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#setStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetStatement(DB2Parser.SetStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#specialRegister}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSpecialRegister(DB2Parser.SpecialRegisterContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#commitStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommitStatement(DB2Parser.CommitStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#rollbackStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRollbackStatement(DB2Parser.RollbackStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#savepointStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSavepointStatement(DB2Parser.SavepointStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#releaseSavepointStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReleaseSavepointStatement(DB2Parser.ReleaseSavepointStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#lockStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLockStatement(DB2Parser.LockStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#insertStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsertStatement(DB2Parser.InsertStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#valuesRow}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValuesRow(DB2Parser.ValuesRowContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#queryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQueryExpression(DB2Parser.QueryExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#setOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetOperator(DB2Parser.SetOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#querySpecification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQuerySpecification(DB2Parser.QuerySpecificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#selectList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectList(DB2Parser.SelectListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#selectItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectItem(DB2Parser.SelectItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#tableReference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableReference(DB2Parser.TableReferenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#joinType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoinType(DB2Parser.JoinTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#sortKey}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSortKey(DB2Parser.SortKeyContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#searchCondition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSearchCondition(DB2Parser.SearchConditionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicate(DB2Parser.PredicateContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#comparisonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparisonOperator(DB2Parser.ComparisonOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(DB2Parser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#caseWhen}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCaseWhen(DB2Parser.CaseWhenContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#specialValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSpecialValue(DB2Parser.SpecialValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstant(DB2Parser.ConstantContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#signedNumber}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSignedNumber(DB2Parser.SignedNumberContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#qualifiedName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualifiedName(DB2Parser.QualifiedNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#identifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifier(DB2Parser.IdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#dataType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDataType(DB2Parser.DataTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#typeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeName(DB2Parser.TypeNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#typeAttribute}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeAttribute(DB2Parser.TypeAttributeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#storageOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStorageOption(DB2Parser.StorageOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#end}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnd(DB2Parser.EndContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#nonReserved}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNonReserved(DB2Parser.NonReservedContext ctx);
}