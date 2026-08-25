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
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link DB2Parser}.
 */
public interface DB2ParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link DB2Parser#compilationUnit}.
	 * @param ctx the parse tree
	 */
	void enterCompilationUnit(DB2Parser.CompilationUnitContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#compilationUnit}.
	 * @param ctx the parse tree
	 */
	void exitCompilationUnit(DB2Parser.CompilationUnitContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(DB2Parser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(DB2Parser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#queryStatement}.
	 * @param ctx the parse tree
	 */
	void enterQueryStatement(DB2Parser.QueryStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#queryStatement}.
	 * @param ctx the parse tree
	 */
	void exitQueryStatement(DB2Parser.QueryStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#terminator}.
	 * @param ctx the parse tree
	 */
	void enterTerminator(DB2Parser.TerminatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#terminator}.
	 * @param ctx the parse tree
	 */
	void exitTerminator(DB2Parser.TerminatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#createTable}.
	 * @param ctx the parse tree
	 */
	void enterCreateTable(DB2Parser.CreateTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#createTable}.
	 * @param ctx the parse tree
	 */
	void exitCreateTable(DB2Parser.CreateTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#tableContents}.
	 * @param ctx the parse tree
	 */
	void enterTableContents(DB2Parser.TableContentsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#tableContents}.
	 * @param ctx the parse tree
	 */
	void exitTableContents(DB2Parser.TableContentsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#copyOption}.
	 * @param ctx the parse tree
	 */
	void enterCopyOption(DB2Parser.CopyOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#copyOption}.
	 * @param ctx the parse tree
	 */
	void exitCopyOption(DB2Parser.CopyOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#tableElement}.
	 * @param ctx the parse tree
	 */
	void enterTableElement(DB2Parser.TableElementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#tableElement}.
	 * @param ctx the parse tree
	 */
	void exitTableElement(DB2Parser.TableElementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#columnDefinition}.
	 * @param ctx the parse tree
	 */
	void enterColumnDefinition(DB2Parser.ColumnDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#columnDefinition}.
	 * @param ctx the parse tree
	 */
	void exitColumnDefinition(DB2Parser.ColumnDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#columnAttribute}.
	 * @param ctx the parse tree
	 */
	void enterColumnAttribute(DB2Parser.ColumnAttributeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#columnAttribute}.
	 * @param ctx the parse tree
	 */
	void exitColumnAttribute(DB2Parser.ColumnAttributeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#generatedAs}.
	 * @param ctx the parse tree
	 */
	void enterGeneratedAs(DB2Parser.GeneratedAsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#generatedAs}.
	 * @param ctx the parse tree
	 */
	void exitGeneratedAs(DB2Parser.GeneratedAsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#defaultValue}.
	 * @param ctx the parse tree
	 */
	void enterDefaultValue(DB2Parser.DefaultValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#defaultValue}.
	 * @param ctx the parse tree
	 */
	void exitDefaultValue(DB2Parser.DefaultValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#periodDefinition}.
	 * @param ctx the parse tree
	 */
	void enterPeriodDefinition(DB2Parser.PeriodDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#periodDefinition}.
	 * @param ctx the parse tree
	 */
	void exitPeriodDefinition(DB2Parser.PeriodDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#tableConstraint}.
	 * @param ctx the parse tree
	 */
	void enterTableConstraint(DB2Parser.TableConstraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#tableConstraint}.
	 * @param ctx the parse tree
	 */
	void exitTableConstraint(DB2Parser.TableConstraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#constraintBody}.
	 * @param ctx the parse tree
	 */
	void enterConstraintBody(DB2Parser.ConstraintBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#constraintBody}.
	 * @param ctx the parse tree
	 */
	void exitConstraintBody(DB2Parser.ConstraintBodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#referentialAction}.
	 * @param ctx the parse tree
	 */
	void enterReferentialAction(DB2Parser.ReferentialActionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#referentialAction}.
	 * @param ctx the parse tree
	 */
	void exitReferentialAction(DB2Parser.ReferentialActionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#constraintOption}.
	 * @param ctx the parse tree
	 */
	void enterConstraintOption(DB2Parser.ConstraintOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#constraintOption}.
	 * @param ctx the parse tree
	 */
	void exitConstraintOption(DB2Parser.ConstraintOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#columnList}.
	 * @param ctx the parse tree
	 */
	void enterColumnList(DB2Parser.ColumnListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#columnList}.
	 * @param ctx the parse tree
	 */
	void exitColumnList(DB2Parser.ColumnListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#tableOption}.
	 * @param ctx the parse tree
	 */
	void enterTableOption(DB2Parser.TableOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#tableOption}.
	 * @param ctx the parse tree
	 */
	void exitTableOption(DB2Parser.TableOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#hashSpace}.
	 * @param ctx the parse tree
	 */
	void enterHashSpace(DB2Parser.HashSpaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#hashSpace}.
	 * @param ctx the parse tree
	 */
	void exitHashSpace(DB2Parser.HashSpaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#partitionKey}.
	 * @param ctx the parse tree
	 */
	void enterPartitionKey(DB2Parser.PartitionKeyContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#partitionKey}.
	 * @param ctx the parse tree
	 */
	void exitPartitionKey(DB2Parser.PartitionKeyContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#partitionClause}.
	 * @param ctx the parse tree
	 */
	void enterPartitionClause(DB2Parser.PartitionClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#partitionClause}.
	 * @param ctx the parse tree
	 */
	void exitPartitionClause(DB2Parser.PartitionClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#partitionSpec}.
	 * @param ctx the parse tree
	 */
	void enterPartitionSpec(DB2Parser.PartitionSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#partitionSpec}.
	 * @param ctx the parse tree
	 */
	void exitPartitionSpec(DB2Parser.PartitionSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#createIndex}.
	 * @param ctx the parse tree
	 */
	void enterCreateIndex(DB2Parser.CreateIndexContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#createIndex}.
	 * @param ctx the parse tree
	 */
	void exitCreateIndex(DB2Parser.CreateIndexContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#indexModifier}.
	 * @param ctx the parse tree
	 */
	void enterIndexModifier(DB2Parser.IndexModifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#indexModifier}.
	 * @param ctx the parse tree
	 */
	void exitIndexModifier(DB2Parser.IndexModifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#indexKey}.
	 * @param ctx the parse tree
	 */
	void enterIndexKey(DB2Parser.IndexKeyContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#indexKey}.
	 * @param ctx the parse tree
	 */
	void exitIndexKey(DB2Parser.IndexKeyContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#indexOption}.
	 * @param ctx the parse tree
	 */
	void enterIndexOption(DB2Parser.IndexOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#indexOption}.
	 * @param ctx the parse tree
	 */
	void exitIndexOption(DB2Parser.IndexOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#createTablespace}.
	 * @param ctx the parse tree
	 */
	void enterCreateTablespace(DB2Parser.CreateTablespaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#createTablespace}.
	 * @param ctx the parse tree
	 */
	void exitCreateTablespace(DB2Parser.CreateTablespaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#tablespaceOption}.
	 * @param ctx the parse tree
	 */
	void enterTablespaceOption(DB2Parser.TablespaceOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#tablespaceOption}.
	 * @param ctx the parse tree
	 */
	void exitTablespaceOption(DB2Parser.TablespaceOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#createDatabase}.
	 * @param ctx the parse tree
	 */
	void enterCreateDatabase(DB2Parser.CreateDatabaseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#createDatabase}.
	 * @param ctx the parse tree
	 */
	void exitCreateDatabase(DB2Parser.CreateDatabaseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#databaseOption}.
	 * @param ctx the parse tree
	 */
	void enterDatabaseOption(DB2Parser.DatabaseOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#databaseOption}.
	 * @param ctx the parse tree
	 */
	void exitDatabaseOption(DB2Parser.DatabaseOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#createStogroup}.
	 * @param ctx the parse tree
	 */
	void enterCreateStogroup(DB2Parser.CreateStogroupContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#createStogroup}.
	 * @param ctx the parse tree
	 */
	void exitCreateStogroup(DB2Parser.CreateStogroupContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#stogroupOption}.
	 * @param ctx the parse tree
	 */
	void enterStogroupOption(DB2Parser.StogroupOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#stogroupOption}.
	 * @param ctx the parse tree
	 */
	void exitStogroupOption(DB2Parser.StogroupOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#createView}.
	 * @param ctx the parse tree
	 */
	void enterCreateView(DB2Parser.CreateViewContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#createView}.
	 * @param ctx the parse tree
	 */
	void exitCreateView(DB2Parser.CreateViewContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#viewOption}.
	 * @param ctx the parse tree
	 */
	void enterViewOption(DB2Parser.ViewOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#viewOption}.
	 * @param ctx the parse tree
	 */
	void exitViewOption(DB2Parser.ViewOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#createAlias}.
	 * @param ctx the parse tree
	 */
	void enterCreateAlias(DB2Parser.CreateAliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#createAlias}.
	 * @param ctx the parse tree
	 */
	void exitCreateAlias(DB2Parser.CreateAliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#createSynonym}.
	 * @param ctx the parse tree
	 */
	void enterCreateSynonym(DB2Parser.CreateSynonymContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#createSynonym}.
	 * @param ctx the parse tree
	 */
	void exitCreateSynonym(DB2Parser.CreateSynonymContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#createSequence}.
	 * @param ctx the parse tree
	 */
	void enterCreateSequence(DB2Parser.CreateSequenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#createSequence}.
	 * @param ctx the parse tree
	 */
	void exitCreateSequence(DB2Parser.CreateSequenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#sequenceOption}.
	 * @param ctx the parse tree
	 */
	void enterSequenceOption(DB2Parser.SequenceOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#sequenceOption}.
	 * @param ctx the parse tree
	 */
	void exitSequenceOption(DB2Parser.SequenceOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#createRole}.
	 * @param ctx the parse tree
	 */
	void enterCreateRole(DB2Parser.CreateRoleContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#createRole}.
	 * @param ctx the parse tree
	 */
	void exitCreateRole(DB2Parser.CreateRoleContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#createAuxiliaryTable}.
	 * @param ctx the parse tree
	 */
	void enterCreateAuxiliaryTable(DB2Parser.CreateAuxiliaryTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#createAuxiliaryTable}.
	 * @param ctx the parse tree
	 */
	void exitCreateAuxiliaryTable(DB2Parser.CreateAuxiliaryTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#createType}.
	 * @param ctx the parse tree
	 */
	void enterCreateType(DB2Parser.CreateTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#createType}.
	 * @param ctx the parse tree
	 */
	void exitCreateType(DB2Parser.CreateTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#createVariable}.
	 * @param ctx the parse tree
	 */
	void enterCreateVariable(DB2Parser.CreateVariableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#createVariable}.
	 * @param ctx the parse tree
	 */
	void exitCreateVariable(DB2Parser.CreateVariableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#createMask}.
	 * @param ctx the parse tree
	 */
	void enterCreateMask(DB2Parser.CreateMaskContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#createMask}.
	 * @param ctx the parse tree
	 */
	void exitCreateMask(DB2Parser.CreateMaskContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#createPermission}.
	 * @param ctx the parse tree
	 */
	void enterCreatePermission(DB2Parser.CreatePermissionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#createPermission}.
	 * @param ctx the parse tree
	 */
	void exitCreatePermission(DB2Parser.CreatePermissionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#createTrustedContext}.
	 * @param ctx the parse tree
	 */
	void enterCreateTrustedContext(DB2Parser.CreateTrustedContextContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#createTrustedContext}.
	 * @param ctx the parse tree
	 */
	void exitCreateTrustedContext(DB2Parser.CreateTrustedContextContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#trustedContextOption}.
	 * @param ctx the parse tree
	 */
	void enterTrustedContextOption(DB2Parser.TrustedContextOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#trustedContextOption}.
	 * @param ctx the parse tree
	 */
	void exitTrustedContextOption(DB2Parser.TrustedContextOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#trustedAttribute}.
	 * @param ctx the parse tree
	 */
	void enterTrustedAttribute(DB2Parser.TrustedAttributeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#trustedAttribute}.
	 * @param ctx the parse tree
	 */
	void exitTrustedAttribute(DB2Parser.TrustedAttributeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#trustedUser}.
	 * @param ctx the parse tree
	 */
	void enterTrustedUser(DB2Parser.TrustedUserContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#trustedUser}.
	 * @param ctx the parse tree
	 */
	void exitTrustedUser(DB2Parser.TrustedUserContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#caseExpression}.
	 * @param ctx the parse tree
	 */
	void enterCaseExpression(DB2Parser.CaseExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#caseExpression}.
	 * @param ctx the parse tree
	 */
	void exitCaseExpression(DB2Parser.CaseExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#declareGlobalTemporaryTable}.
	 * @param ctx the parse tree
	 */
	void enterDeclareGlobalTemporaryTable(DB2Parser.DeclareGlobalTemporaryTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#declareGlobalTemporaryTable}.
	 * @param ctx the parse tree
	 */
	void exitDeclareGlobalTemporaryTable(DB2Parser.DeclareGlobalTemporaryTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#createTrigger}.
	 * @param ctx the parse tree
	 */
	void enterCreateTrigger(DB2Parser.CreateTriggerContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#createTrigger}.
	 * @param ctx the parse tree
	 */
	void exitCreateTrigger(DB2Parser.CreateTriggerContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#triggerEvent}.
	 * @param ctx the parse tree
	 */
	void enterTriggerEvent(DB2Parser.TriggerEventContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#triggerEvent}.
	 * @param ctx the parse tree
	 */
	void exitTriggerEvent(DB2Parser.TriggerEventContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#triggerCorrelation}.
	 * @param ctx the parse tree
	 */
	void enterTriggerCorrelation(DB2Parser.TriggerCorrelationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#triggerCorrelation}.
	 * @param ctx the parse tree
	 */
	void exitTriggerCorrelation(DB2Parser.TriggerCorrelationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#triggerGranularity}.
	 * @param ctx the parse tree
	 */
	void enterTriggerGranularity(DB2Parser.TriggerGranularityContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#triggerGranularity}.
	 * @param ctx the parse tree
	 */
	void exitTriggerGranularity(DB2Parser.TriggerGranularityContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#createProcedure}.
	 * @param ctx the parse tree
	 */
	void enterCreateProcedure(DB2Parser.CreateProcedureContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#createProcedure}.
	 * @param ctx the parse tree
	 */
	void exitCreateProcedure(DB2Parser.CreateProcedureContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#createFunction}.
	 * @param ctx the parse tree
	 */
	void enterCreateFunction(DB2Parser.CreateFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#createFunction}.
	 * @param ctx the parse tree
	 */
	void exitCreateFunction(DB2Parser.CreateFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#routineParameter}.
	 * @param ctx the parse tree
	 */
	void enterRoutineParameter(DB2Parser.RoutineParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#routineParameter}.
	 * @param ctx the parse tree
	 */
	void exitRoutineParameter(DB2Parser.RoutineParameterContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#routineClause}.
	 * @param ctx the parse tree
	 */
	void enterRoutineClause(DB2Parser.RoutineClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#routineClause}.
	 * @param ctx the parse tree
	 */
	void exitRoutineClause(DB2Parser.RoutineClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#compoundStatement}.
	 * @param ctx the parse tree
	 */
	void enterCompoundStatement(DB2Parser.CompoundStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#compoundStatement}.
	 * @param ctx the parse tree
	 */
	void exitCompoundStatement(DB2Parser.CompoundStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#bodyItem}.
	 * @param ctx the parse tree
	 */
	void enterBodyItem(DB2Parser.BodyItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#bodyItem}.
	 * @param ctx the parse tree
	 */
	void exitBodyItem(DB2Parser.BodyItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#triggeredStatement}.
	 * @param ctx the parse tree
	 */
	void enterTriggeredStatement(DB2Parser.TriggeredStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#triggeredStatement}.
	 * @param ctx the parse tree
	 */
	void exitTriggeredStatement(DB2Parser.TriggeredStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#alterTable}.
	 * @param ctx the parse tree
	 */
	void enterAlterTable(DB2Parser.AlterTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#alterTable}.
	 * @param ctx the parse tree
	 */
	void exitAlterTable(DB2Parser.AlterTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#alterTableAction}.
	 * @param ctx the parse tree
	 */
	void enterAlterTableAction(DB2Parser.AlterTableActionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#alterTableAction}.
	 * @param ctx the parse tree
	 */
	void exitAlterTableAction(DB2Parser.AlterTableActionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#alterColumnAction}.
	 * @param ctx the parse tree
	 */
	void enterAlterColumnAction(DB2Parser.AlterColumnActionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#alterColumnAction}.
	 * @param ctx the parse tree
	 */
	void exitAlterColumnAction(DB2Parser.AlterColumnActionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#alterTablespace}.
	 * @param ctx the parse tree
	 */
	void enterAlterTablespace(DB2Parser.AlterTablespaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#alterTablespace}.
	 * @param ctx the parse tree
	 */
	void exitAlterTablespace(DB2Parser.AlterTablespaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#alterIndex}.
	 * @param ctx the parse tree
	 */
	void enterAlterIndex(DB2Parser.AlterIndexContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#alterIndex}.
	 * @param ctx the parse tree
	 */
	void exitAlterIndex(DB2Parser.AlterIndexContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#alterDatabase}.
	 * @param ctx the parse tree
	 */
	void enterAlterDatabase(DB2Parser.AlterDatabaseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#alterDatabase}.
	 * @param ctx the parse tree
	 */
	void exitAlterDatabase(DB2Parser.AlterDatabaseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#alterStogroup}.
	 * @param ctx the parse tree
	 */
	void enterAlterStogroup(DB2Parser.AlterStogroupContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#alterStogroup}.
	 * @param ctx the parse tree
	 */
	void exitAlterStogroup(DB2Parser.AlterStogroupContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#alterSequence}.
	 * @param ctx the parse tree
	 */
	void enterAlterSequence(DB2Parser.AlterSequenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#alterSequence}.
	 * @param ctx the parse tree
	 */
	void exitAlterSequence(DB2Parser.AlterSequenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#alterView}.
	 * @param ctx the parse tree
	 */
	void enterAlterView(DB2Parser.AlterViewContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#alterView}.
	 * @param ctx the parse tree
	 */
	void exitAlterView(DB2Parser.AlterViewContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#alterProcedure}.
	 * @param ctx the parse tree
	 */
	void enterAlterProcedure(DB2Parser.AlterProcedureContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#alterProcedure}.
	 * @param ctx the parse tree
	 */
	void exitAlterProcedure(DB2Parser.AlterProcedureContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#alterFunction}.
	 * @param ctx the parse tree
	 */
	void enterAlterFunction(DB2Parser.AlterFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#alterFunction}.
	 * @param ctx the parse tree
	 */
	void exitAlterFunction(DB2Parser.AlterFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#alterTrigger}.
	 * @param ctx the parse tree
	 */
	void enterAlterTrigger(DB2Parser.AlterTriggerContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#alterTrigger}.
	 * @param ctx the parse tree
	 */
	void exitAlterTrigger(DB2Parser.AlterTriggerContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#alterMask}.
	 * @param ctx the parse tree
	 */
	void enterAlterMask(DB2Parser.AlterMaskContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#alterMask}.
	 * @param ctx the parse tree
	 */
	void exitAlterMask(DB2Parser.AlterMaskContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#alterPermission}.
	 * @param ctx the parse tree
	 */
	void enterAlterPermission(DB2Parser.AlterPermissionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#alterPermission}.
	 * @param ctx the parse tree
	 */
	void exitAlterPermission(DB2Parser.AlterPermissionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#alterTrustedContext}.
	 * @param ctx the parse tree
	 */
	void enterAlterTrustedContext(DB2Parser.AlterTrustedContextContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#alterTrustedContext}.
	 * @param ctx the parse tree
	 */
	void exitAlterTrustedContext(DB2Parser.AlterTrustedContextContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#alterStogroupAction}.
	 * @param ctx the parse tree
	 */
	void enterAlterStogroupAction(DB2Parser.AlterStogroupActionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#alterStogroupAction}.
	 * @param ctx the parse tree
	 */
	void exitAlterStogroupAction(DB2Parser.AlterStogroupActionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#dropStatement}.
	 * @param ctx the parse tree
	 */
	void enterDropStatement(DB2Parser.DropStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#dropStatement}.
	 * @param ctx the parse tree
	 */
	void exitDropStatement(DB2Parser.DropStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#droppedObject}.
	 * @param ctx the parse tree
	 */
	void enterDroppedObject(DB2Parser.DroppedObjectContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#droppedObject}.
	 * @param ctx the parse tree
	 */
	void exitDroppedObject(DB2Parser.DroppedObjectContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#grantStatement}.
	 * @param ctx the parse tree
	 */
	void enterGrantStatement(DB2Parser.GrantStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#grantStatement}.
	 * @param ctx the parse tree
	 */
	void exitGrantStatement(DB2Parser.GrantStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#revokeStatement}.
	 * @param ctx the parse tree
	 */
	void enterRevokeStatement(DB2Parser.RevokeStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#revokeStatement}.
	 * @param ctx the parse tree
	 */
	void exitRevokeStatement(DB2Parser.RevokeStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#privilege}.
	 * @param ctx the parse tree
	 */
	void enterPrivilege(DB2Parser.PrivilegeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#privilege}.
	 * @param ctx the parse tree
	 */
	void exitPrivilege(DB2Parser.PrivilegeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#privilegeObject}.
	 * @param ctx the parse tree
	 */
	void enterPrivilegeObject(DB2Parser.PrivilegeObjectContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#privilegeObject}.
	 * @param ctx the parse tree
	 */
	void exitPrivilegeObject(DB2Parser.PrivilegeObjectContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#qualifiedNameList}.
	 * @param ctx the parse tree
	 */
	void enterQualifiedNameList(DB2Parser.QualifiedNameListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#qualifiedNameList}.
	 * @param ctx the parse tree
	 */
	void exitQualifiedNameList(DB2Parser.QualifiedNameListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#grantee}.
	 * @param ctx the parse tree
	 */
	void enterGrantee(DB2Parser.GranteeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#grantee}.
	 * @param ctx the parse tree
	 */
	void exitGrantee(DB2Parser.GranteeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#commentStatement}.
	 * @param ctx the parse tree
	 */
	void enterCommentStatement(DB2Parser.CommentStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#commentStatement}.
	 * @param ctx the parse tree
	 */
	void exitCommentStatement(DB2Parser.CommentStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#commentTarget}.
	 * @param ctx the parse tree
	 */
	void enterCommentTarget(DB2Parser.CommentTargetContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#commentTarget}.
	 * @param ctx the parse tree
	 */
	void exitCommentTarget(DB2Parser.CommentTargetContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#labelStatement}.
	 * @param ctx the parse tree
	 */
	void enterLabelStatement(DB2Parser.LabelStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#labelStatement}.
	 * @param ctx the parse tree
	 */
	void exitLabelStatement(DB2Parser.LabelStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#labelTarget}.
	 * @param ctx the parse tree
	 */
	void enterLabelTarget(DB2Parser.LabelTargetContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#labelTarget}.
	 * @param ctx the parse tree
	 */
	void exitLabelTarget(DB2Parser.LabelTargetContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#renameStatement}.
	 * @param ctx the parse tree
	 */
	void enterRenameStatement(DB2Parser.RenameStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#renameStatement}.
	 * @param ctx the parse tree
	 */
	void exitRenameStatement(DB2Parser.RenameStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#setStatement}.
	 * @param ctx the parse tree
	 */
	void enterSetStatement(DB2Parser.SetStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#setStatement}.
	 * @param ctx the parse tree
	 */
	void exitSetStatement(DB2Parser.SetStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#specialRegister}.
	 * @param ctx the parse tree
	 */
	void enterSpecialRegister(DB2Parser.SpecialRegisterContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#specialRegister}.
	 * @param ctx the parse tree
	 */
	void exitSpecialRegister(DB2Parser.SpecialRegisterContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#commitStatement}.
	 * @param ctx the parse tree
	 */
	void enterCommitStatement(DB2Parser.CommitStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#commitStatement}.
	 * @param ctx the parse tree
	 */
	void exitCommitStatement(DB2Parser.CommitStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#rollbackStatement}.
	 * @param ctx the parse tree
	 */
	void enterRollbackStatement(DB2Parser.RollbackStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#rollbackStatement}.
	 * @param ctx the parse tree
	 */
	void exitRollbackStatement(DB2Parser.RollbackStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#savepointStatement}.
	 * @param ctx the parse tree
	 */
	void enterSavepointStatement(DB2Parser.SavepointStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#savepointStatement}.
	 * @param ctx the parse tree
	 */
	void exitSavepointStatement(DB2Parser.SavepointStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#releaseSavepointStatement}.
	 * @param ctx the parse tree
	 */
	void enterReleaseSavepointStatement(DB2Parser.ReleaseSavepointStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#releaseSavepointStatement}.
	 * @param ctx the parse tree
	 */
	void exitReleaseSavepointStatement(DB2Parser.ReleaseSavepointStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#lockStatement}.
	 * @param ctx the parse tree
	 */
	void enterLockStatement(DB2Parser.LockStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#lockStatement}.
	 * @param ctx the parse tree
	 */
	void exitLockStatement(DB2Parser.LockStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#insertStatement}.
	 * @param ctx the parse tree
	 */
	void enterInsertStatement(DB2Parser.InsertStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#insertStatement}.
	 * @param ctx the parse tree
	 */
	void exitInsertStatement(DB2Parser.InsertStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#valuesRow}.
	 * @param ctx the parse tree
	 */
	void enterValuesRow(DB2Parser.ValuesRowContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#valuesRow}.
	 * @param ctx the parse tree
	 */
	void exitValuesRow(DB2Parser.ValuesRowContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#queryExpression}.
	 * @param ctx the parse tree
	 */
	void enterQueryExpression(DB2Parser.QueryExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#queryExpression}.
	 * @param ctx the parse tree
	 */
	void exitQueryExpression(DB2Parser.QueryExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#setOperator}.
	 * @param ctx the parse tree
	 */
	void enterSetOperator(DB2Parser.SetOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#setOperator}.
	 * @param ctx the parse tree
	 */
	void exitSetOperator(DB2Parser.SetOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#querySpecification}.
	 * @param ctx the parse tree
	 */
	void enterQuerySpecification(DB2Parser.QuerySpecificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#querySpecification}.
	 * @param ctx the parse tree
	 */
	void exitQuerySpecification(DB2Parser.QuerySpecificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#selectList}.
	 * @param ctx the parse tree
	 */
	void enterSelectList(DB2Parser.SelectListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#selectList}.
	 * @param ctx the parse tree
	 */
	void exitSelectList(DB2Parser.SelectListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#selectItem}.
	 * @param ctx the parse tree
	 */
	void enterSelectItem(DB2Parser.SelectItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#selectItem}.
	 * @param ctx the parse tree
	 */
	void exitSelectItem(DB2Parser.SelectItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#tableReference}.
	 * @param ctx the parse tree
	 */
	void enterTableReference(DB2Parser.TableReferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#tableReference}.
	 * @param ctx the parse tree
	 */
	void exitTableReference(DB2Parser.TableReferenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#joinType}.
	 * @param ctx the parse tree
	 */
	void enterJoinType(DB2Parser.JoinTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#joinType}.
	 * @param ctx the parse tree
	 */
	void exitJoinType(DB2Parser.JoinTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#sortKey}.
	 * @param ctx the parse tree
	 */
	void enterSortKey(DB2Parser.SortKeyContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#sortKey}.
	 * @param ctx the parse tree
	 */
	void exitSortKey(DB2Parser.SortKeyContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#searchCondition}.
	 * @param ctx the parse tree
	 */
	void enterSearchCondition(DB2Parser.SearchConditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#searchCondition}.
	 * @param ctx the parse tree
	 */
	void exitSearchCondition(DB2Parser.SearchConditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#predicate}.
	 * @param ctx the parse tree
	 */
	void enterPredicate(DB2Parser.PredicateContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#predicate}.
	 * @param ctx the parse tree
	 */
	void exitPredicate(DB2Parser.PredicateContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void enterComparisonOperator(DB2Parser.ComparisonOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void exitComparisonOperator(DB2Parser.ComparisonOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(DB2Parser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(DB2Parser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#caseWhen}.
	 * @param ctx the parse tree
	 */
	void enterCaseWhen(DB2Parser.CaseWhenContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#caseWhen}.
	 * @param ctx the parse tree
	 */
	void exitCaseWhen(DB2Parser.CaseWhenContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#specialValue}.
	 * @param ctx the parse tree
	 */
	void enterSpecialValue(DB2Parser.SpecialValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#specialValue}.
	 * @param ctx the parse tree
	 */
	void exitSpecialValue(DB2Parser.SpecialValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#constant}.
	 * @param ctx the parse tree
	 */
	void enterConstant(DB2Parser.ConstantContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#constant}.
	 * @param ctx the parse tree
	 */
	void exitConstant(DB2Parser.ConstantContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#signedNumber}.
	 * @param ctx the parse tree
	 */
	void enterSignedNumber(DB2Parser.SignedNumberContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#signedNumber}.
	 * @param ctx the parse tree
	 */
	void exitSignedNumber(DB2Parser.SignedNumberContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#qualifiedName}.
	 * @param ctx the parse tree
	 */
	void enterQualifiedName(DB2Parser.QualifiedNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#qualifiedName}.
	 * @param ctx the parse tree
	 */
	void exitQualifiedName(DB2Parser.QualifiedNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#identifier}.
	 * @param ctx the parse tree
	 */
	void enterIdentifier(DB2Parser.IdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#identifier}.
	 * @param ctx the parse tree
	 */
	void exitIdentifier(DB2Parser.IdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterDataType(DB2Parser.DataTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitDataType(DB2Parser.DataTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#typeName}.
	 * @param ctx the parse tree
	 */
	void enterTypeName(DB2Parser.TypeNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#typeName}.
	 * @param ctx the parse tree
	 */
	void exitTypeName(DB2Parser.TypeNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#typeAttribute}.
	 * @param ctx the parse tree
	 */
	void enterTypeAttribute(DB2Parser.TypeAttributeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#typeAttribute}.
	 * @param ctx the parse tree
	 */
	void exitTypeAttribute(DB2Parser.TypeAttributeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#storageOption}.
	 * @param ctx the parse tree
	 */
	void enterStorageOption(DB2Parser.StorageOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#storageOption}.
	 * @param ctx the parse tree
	 */
	void exitStorageOption(DB2Parser.StorageOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#end}.
	 * @param ctx the parse tree
	 */
	void enterEnd(DB2Parser.EndContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#end}.
	 * @param ctx the parse tree
	 */
	void exitEnd(DB2Parser.EndContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#nonReserved}.
	 * @param ctx the parse tree
	 */
	void enterNonReserved(DB2Parser.NonReservedContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#nonReserved}.
	 * @param ctx the parse tree
	 */
	void exitNonReserved(DB2Parser.NonReservedContext ctx);
}