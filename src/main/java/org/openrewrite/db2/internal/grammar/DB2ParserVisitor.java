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
	 * Visit a parse tree produced by {@link DB2Parser#createTable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateTable(DB2Parser.CreateTableContext ctx);
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
	 * Visit a parse tree produced by {@link DB2Parser#dataType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDataType(DB2Parser.DataTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#columnAttribute}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnAttribute(DB2Parser.ColumnAttributeContext ctx);
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
	 * Visit a parse tree produced by {@link DB2Parser#alterTable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterTable(DB2Parser.AlterTableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#alterAction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterAction(DB2Parser.AlterActionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#unknownStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnknownStatement(DB2Parser.UnknownStatementContext ctx);
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
	 * Visit a parse tree produced by {@link DB2Parser#water}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWater(DB2Parser.WaterContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#elementWater}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElementWater(DB2Parser.ElementWaterContext ctx);
	/**
	 * Visit a parse tree produced by {@link DB2Parser#parenGroup}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParenGroup(DB2Parser.ParenGroupContext ctx);
}