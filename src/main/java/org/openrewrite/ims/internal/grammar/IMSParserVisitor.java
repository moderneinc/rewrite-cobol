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
// Generated from /Users/jon/Projects/github/openrewrite/rewrite-cobol/.claude/worktrees/oraweb/src/main/antlr-ims/IMSParser.g4 by ANTLR 4.13.2
package org.openrewrite.ims.internal.grammar;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link IMSParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface IMSParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link IMSParser#compilationUnit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompilationUnit(IMSParser.CompilationUnitContext ctx);
	/**
	 * Visit a parse tree produced by {@link IMSParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(IMSParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link IMSParser#ims}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIms(IMSParser.ImsContext ctx);
	/**
	 * Visit a parse tree produced by {@link IMSParser#imsWord}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImsWord(IMSParser.ImsWordContext ctx);
	/**
	 * Visit a parse tree produced by {@link IMSParser#imsSequenceArea}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImsSequenceArea(IMSParser.ImsSequenceAreaContext ctx);
	/**
	 * Visit a parse tree produced by {@link IMSParser#comment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComment(IMSParser.CommentContext ctx);
	/**
	 * Visit a parse tree produced by {@link IMSParser#commentWord}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommentWord(IMSParser.CommentWordContext ctx);
	/**
	 * Visit a parse tree produced by {@link IMSParser#commentSequenceArea}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommentSequenceArea(IMSParser.CommentSequenceAreaContext ctx);
	/**
	 * Visit a parse tree produced by {@link IMSParser#unknown}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnknown(IMSParser.UnknownContext ctx);
	/**
	 * Visit a parse tree produced by {@link IMSParser#unknownWord}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnknownWord(IMSParser.UnknownWordContext ctx);
	/**
	 * Visit a parse tree produced by {@link IMSParser#unknownSequenceArea}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnknownSequenceArea(IMSParser.UnknownSequenceAreaContext ctx);
}