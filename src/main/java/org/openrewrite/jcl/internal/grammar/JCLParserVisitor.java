/*
 * Copyright 2024 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// Generated from java-escape by ANTLR 4.11.1
package org.openrewrite.jcl.internal.grammar;
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
	 * Visit a parse tree produced by {@link JCLParser#jclCommentArea}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJclCommentArea(JCLParser.JclCommentAreaContext ctx);
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
	 * Visit a parse tree produced by {@link JCLParser#jes2CommentArea}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJes2CommentArea(JCLParser.Jes2CommentAreaContext ctx);
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
	 * Visit a parse tree produced by {@link JCLParser#jes3CommentArea}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJes3CommentArea(JCLParser.Jes3CommentAreaContext ctx);
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
	 * Visit a parse tree produced by {@link JCLParser#streamCommentArea}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStreamCommentArea(JCLParser.StreamCommentAreaContext ctx);
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
	 * Visit a parse tree produced by {@link JCLParser#controlMCommentArea}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitControlMCommentArea(JCLParser.ControlMCommentAreaContext ctx);
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
	 * Visit a parse tree produced by {@link JCLParser#commentCommentArea}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommentCommentArea(JCLParser.CommentCommentAreaContext ctx);
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
	 * Visit a parse tree produced by {@link JCLParser#unknownCommentArea}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnknownCommentArea(JCLParser.UnknownCommentAreaContext ctx);
}
