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
package org.openrewrite.jcl.internal.grammar;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link JCLParser}.
 */
public interface JCLParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link JCLParser#compilationUnit}.
	 * @param ctx the parse tree
	 */
	void enterCompilationUnit(JCLParser.CompilationUnitContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#compilationUnit}.
	 * @param ctx the parse tree
	 */
	void exitCompilationUnit(JCLParser.CompilationUnitContext ctx);
	/**
	 * Enter a parse tree produced by {@link JCLParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(JCLParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(JCLParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link JCLParser#jcl}.
	 * @param ctx the parse tree
	 */
	void enterJcl(JCLParser.JclContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#jcl}.
	 * @param ctx the parse tree
	 */
	void exitJcl(JCLParser.JclContext ctx);
	/**
	 * Enter a parse tree produced by {@link JCLParser#jclWord}.
	 * @param ctx the parse tree
	 */
	void enterJclWord(JCLParser.JclWordContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#jclWord}.
	 * @param ctx the parse tree
	 */
	void exitJclWord(JCLParser.JclWordContext ctx);
	/**
	 * Enter a parse tree produced by {@link JCLParser#jclTrailingComment}.
	 * @param ctx the parse tree
	 */
	void enterJclTrailingComment(JCLParser.JclTrailingCommentContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#jclTrailingComment}.
	 * @param ctx the parse tree
	 */
	void exitJclTrailingComment(JCLParser.JclTrailingCommentContext ctx);
	/**
	 * Enter a parse tree produced by {@link JCLParser#jes2}.
	 * @param ctx the parse tree
	 */
	void enterJes2(JCLParser.Jes2Context ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#jes2}.
	 * @param ctx the parse tree
	 */
	void exitJes2(JCLParser.Jes2Context ctx);
	/**
	 * Enter a parse tree produced by {@link JCLParser#jes2Word}.
	 * @param ctx the parse tree
	 */
	void enterJes2Word(JCLParser.Jes2WordContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#jes2Word}.
	 * @param ctx the parse tree
	 */
	void exitJes2Word(JCLParser.Jes2WordContext ctx);
	/**
	 * Enter a parse tree produced by {@link JCLParser#jes3}.
	 * @param ctx the parse tree
	 */
	void enterJes3(JCLParser.Jes3Context ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#jes3}.
	 * @param ctx the parse tree
	 */
	void exitJes3(JCLParser.Jes3Context ctx);
	/**
	 * Enter a parse tree produced by {@link JCLParser#jes3Word}.
	 * @param ctx the parse tree
	 */
	void enterJes3Word(JCLParser.Jes3WordContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#jes3Word}.
	 * @param ctx the parse tree
	 */
	void exitJes3Word(JCLParser.Jes3WordContext ctx);
	/**
	 * Enter a parse tree produced by {@link JCLParser#stream}.
	 * @param ctx the parse tree
	 */
	void enterStream(JCLParser.StreamContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#stream}.
	 * @param ctx the parse tree
	 */
	void exitStream(JCLParser.StreamContext ctx);
	/**
	 * Enter a parse tree produced by {@link JCLParser#streamWord}.
	 * @param ctx the parse tree
	 */
	void enterStreamWord(JCLParser.StreamWordContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#streamWord}.
	 * @param ctx the parse tree
	 */
	void exitStreamWord(JCLParser.StreamWordContext ctx);
	/**
	 * Enter a parse tree produced by {@link JCLParser#controlM}.
	 * @param ctx the parse tree
	 */
	void enterControlM(JCLParser.ControlMContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#controlM}.
	 * @param ctx the parse tree
	 */
	void exitControlM(JCLParser.ControlMContext ctx);
	/**
	 * Enter a parse tree produced by {@link JCLParser#controlMWord}.
	 * @param ctx the parse tree
	 */
	void enterControlMWord(JCLParser.ControlMWordContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#controlMWord}.
	 * @param ctx the parse tree
	 */
	void exitControlMWord(JCLParser.ControlMWordContext ctx);
	/**
	 * Enter a parse tree produced by {@link JCLParser#comment}.
	 * @param ctx the parse tree
	 */
	void enterComment(JCLParser.CommentContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#comment}.
	 * @param ctx the parse tree
	 */
	void exitComment(JCLParser.CommentContext ctx);
	/**
	 * Enter a parse tree produced by {@link JCLParser#commentWord}.
	 * @param ctx the parse tree
	 */
	void enterCommentWord(JCLParser.CommentWordContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#commentWord}.
	 * @param ctx the parse tree
	 */
	void exitCommentWord(JCLParser.CommentWordContext ctx);
	/**
	 * Enter a parse tree produced by {@link JCLParser#unknown}.
	 * @param ctx the parse tree
	 */
	void enterUnknown(JCLParser.UnknownContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#unknown}.
	 * @param ctx the parse tree
	 */
	void exitUnknown(JCLParser.UnknownContext ctx);
	/**
	 * Enter a parse tree produced by {@link JCLParser#unknownWord}.
	 * @param ctx the parse tree
	 */
	void enterUnknownWord(JCLParser.UnknownWordContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#unknownWord}.
	 * @param ctx the parse tree
	 */
	void exitUnknownWord(JCLParser.UnknownWordContext ctx);
	/**
	 * Enter a parse tree produced by {@link JCLParser#commentArea}.
	 * @param ctx the parse tree
	 */
	void enterCommentArea(JCLParser.CommentAreaContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#commentArea}.
	 * @param ctx the parse tree
	 */
	void exitCommentArea(JCLParser.CommentAreaContext ctx);
}