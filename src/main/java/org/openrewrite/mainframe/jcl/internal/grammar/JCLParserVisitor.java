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
// Generated from /Users/jon/Projects/github/openrewrite/rewrite-cobol/.claude/worktrees/ims-or-cics/src/main/antlr-jcl/JCLParser.g4 by ANTLR 4.13.2
package org.openrewrite.mainframe.jcl.internal.grammar;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link JCLParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface JCLParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link JCLParser#compilationUnit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompilationUnit(JCLParser.CompilationUnitContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(JCLParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#jcl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJcl(JCLParser.JclContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#jclWord}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJclWord(JCLParser.JclWordContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#jclTrailingComment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJclTrailingComment(JCLParser.JclTrailingCommentContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#jes2}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJes2(JCLParser.Jes2Context ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#jes2Word}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJes2Word(JCLParser.Jes2WordContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#jes3}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJes3(JCLParser.Jes3Context ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#jes3Word}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJes3Word(JCLParser.Jes3WordContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#stream}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStream(JCLParser.StreamContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#streamWord}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStreamWord(JCLParser.StreamWordContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#controlM}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitControlM(JCLParser.ControlMContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#controlMWord}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitControlMWord(JCLParser.ControlMWordContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#comment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComment(JCLParser.CommentContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#commentWord}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommentWord(JCLParser.CommentWordContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#unknown}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnknown(JCLParser.UnknownContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#unknownWord}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnknownWord(JCLParser.UnknownWordContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#commentArea}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommentArea(JCLParser.CommentAreaContext ctx);
}