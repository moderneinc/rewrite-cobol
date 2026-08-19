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
	 * Enter a parse tree produced by {@link DB2Parser#alterAction}.
	 * @param ctx the parse tree
	 */
	void enterAlterAction(DB2Parser.AlterActionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#alterAction}.
	 * @param ctx the parse tree
	 */
	void exitAlterAction(DB2Parser.AlterActionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#unknownStatement}.
	 * @param ctx the parse tree
	 */
	void enterUnknownStatement(DB2Parser.UnknownStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#unknownStatement}.
	 * @param ctx the parse tree
	 */
	void exitUnknownStatement(DB2Parser.UnknownStatementContext ctx);
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
	 * Enter a parse tree produced by {@link DB2Parser#water}.
	 * @param ctx the parse tree
	 */
	void enterWater(DB2Parser.WaterContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#water}.
	 * @param ctx the parse tree
	 */
	void exitWater(DB2Parser.WaterContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#elementWater}.
	 * @param ctx the parse tree
	 */
	void enterElementWater(DB2Parser.ElementWaterContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#elementWater}.
	 * @param ctx the parse tree
	 */
	void exitElementWater(DB2Parser.ElementWaterContext ctx);
	/**
	 * Enter a parse tree produced by {@link DB2Parser#parenGroup}.
	 * @param ctx the parse tree
	 */
	void enterParenGroup(DB2Parser.ParenGroupContext ctx);
	/**
	 * Exit a parse tree produced by {@link DB2Parser#parenGroup}.
	 * @param ctx the parse tree
	 */
	void exitParenGroup(DB2Parser.ParenGroupContext ctx);
}