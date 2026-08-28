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
package org.openrewrite.mainframe.ims.internal.grammar;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link IMSParser}.
 */
public interface IMSParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link IMSParser#compilationUnit}.
	 * @param ctx the parse tree
	 */
	void enterCompilationUnit(IMSParser.CompilationUnitContext ctx);
	/**
	 * Exit a parse tree produced by {@link IMSParser#compilationUnit}.
	 * @param ctx the parse tree
	 */
	void exitCompilationUnit(IMSParser.CompilationUnitContext ctx);
	/**
	 * Enter a parse tree produced by {@link IMSParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(IMSParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link IMSParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(IMSParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link IMSParser#ims}.
	 * @param ctx the parse tree
	 */
	void enterIms(IMSParser.ImsContext ctx);
	/**
	 * Exit a parse tree produced by {@link IMSParser#ims}.
	 * @param ctx the parse tree
	 */
	void exitIms(IMSParser.ImsContext ctx);
	/**
	 * Enter a parse tree produced by {@link IMSParser#imsWord}.
	 * @param ctx the parse tree
	 */
	void enterImsWord(IMSParser.ImsWordContext ctx);
	/**
	 * Exit a parse tree produced by {@link IMSParser#imsWord}.
	 * @param ctx the parse tree
	 */
	void exitImsWord(IMSParser.ImsWordContext ctx);
	/**
	 * Enter a parse tree produced by {@link IMSParser#imsSequenceArea}.
	 * @param ctx the parse tree
	 */
	void enterImsSequenceArea(IMSParser.ImsSequenceAreaContext ctx);
	/**
	 * Exit a parse tree produced by {@link IMSParser#imsSequenceArea}.
	 * @param ctx the parse tree
	 */
	void exitImsSequenceArea(IMSParser.ImsSequenceAreaContext ctx);
	/**
	 * Enter a parse tree produced by {@link IMSParser#comment}.
	 * @param ctx the parse tree
	 */
	void enterComment(IMSParser.CommentContext ctx);
	/**
	 * Exit a parse tree produced by {@link IMSParser#comment}.
	 * @param ctx the parse tree
	 */
	void exitComment(IMSParser.CommentContext ctx);
	/**
	 * Enter a parse tree produced by {@link IMSParser#commentWord}.
	 * @param ctx the parse tree
	 */
	void enterCommentWord(IMSParser.CommentWordContext ctx);
	/**
	 * Exit a parse tree produced by {@link IMSParser#commentWord}.
	 * @param ctx the parse tree
	 */
	void exitCommentWord(IMSParser.CommentWordContext ctx);
	/**
	 * Enter a parse tree produced by {@link IMSParser#commentSequenceArea}.
	 * @param ctx the parse tree
	 */
	void enterCommentSequenceArea(IMSParser.CommentSequenceAreaContext ctx);
	/**
	 * Exit a parse tree produced by {@link IMSParser#commentSequenceArea}.
	 * @param ctx the parse tree
	 */
	void exitCommentSequenceArea(IMSParser.CommentSequenceAreaContext ctx);
	/**
	 * Enter a parse tree produced by {@link IMSParser#unknown}.
	 * @param ctx the parse tree
	 */
	void enterUnknown(IMSParser.UnknownContext ctx);
	/**
	 * Exit a parse tree produced by {@link IMSParser#unknown}.
	 * @param ctx the parse tree
	 */
	void exitUnknown(IMSParser.UnknownContext ctx);
	/**
	 * Enter a parse tree produced by {@link IMSParser#unknownWord}.
	 * @param ctx the parse tree
	 */
	void enterUnknownWord(IMSParser.UnknownWordContext ctx);
	/**
	 * Exit a parse tree produced by {@link IMSParser#unknownWord}.
	 * @param ctx the parse tree
	 */
	void exitUnknownWord(IMSParser.UnknownWordContext ctx);
	/**
	 * Enter a parse tree produced by {@link IMSParser#unknownSequenceArea}.
	 * @param ctx the parse tree
	 */
	void enterUnknownSequenceArea(IMSParser.UnknownSequenceAreaContext ctx);
	/**
	 * Exit a parse tree produced by {@link IMSParser#unknownSequenceArea}.
	 * @param ctx the parse tree
	 */
	void exitUnknownSequenceArea(IMSParser.UnknownSequenceAreaContext ctx);
}