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
// Generated from /Users/jon/Projects/github/openrewrite/rewrite-cobol/.claude/worktrees/ims-or-cics/src/main/antlr-bms/BMSParser.g4 by ANTLR 4.13.2
package org.openrewrite.bms.internal.grammar;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link BMSParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface BMSParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link BMSParser#compilationUnit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompilationUnit(BMSParser.CompilationUnitContext ctx);
	/**
	 * Visit a parse tree produced by {@link BMSParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(BMSParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BMSParser#bms}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBms(BMSParser.BmsContext ctx);
	/**
	 * Visit a parse tree produced by {@link BMSParser#bmsWord}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBmsWord(BMSParser.BmsWordContext ctx);
	/**
	 * Visit a parse tree produced by {@link BMSParser#bmsSequenceArea}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBmsSequenceArea(BMSParser.BmsSequenceAreaContext ctx);
	/**
	 * Visit a parse tree produced by {@link BMSParser#comment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComment(BMSParser.CommentContext ctx);
	/**
	 * Visit a parse tree produced by {@link BMSParser#commentWord}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommentWord(BMSParser.CommentWordContext ctx);
	/**
	 * Visit a parse tree produced by {@link BMSParser#commentSequenceArea}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommentSequenceArea(BMSParser.CommentSequenceAreaContext ctx);
	/**
	 * Visit a parse tree produced by {@link BMSParser#unknown}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnknown(BMSParser.UnknownContext ctx);
	/**
	 * Visit a parse tree produced by {@link BMSParser#unknownWord}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnknownWord(BMSParser.UnknownWordContext ctx);
	/**
	 * Visit a parse tree produced by {@link BMSParser#unknownSequenceArea}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnknownSequenceArea(BMSParser.UnknownSequenceAreaContext ctx);
}