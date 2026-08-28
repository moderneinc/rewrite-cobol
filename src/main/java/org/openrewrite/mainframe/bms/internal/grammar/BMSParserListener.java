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
package org.openrewrite.mainframe.bms.internal.grammar;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link BMSParser}.
 */
public interface BMSParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link BMSParser#compilationUnit}.
	 * @param ctx the parse tree
	 */
	void enterCompilationUnit(BMSParser.CompilationUnitContext ctx);
	/**
	 * Exit a parse tree produced by {@link BMSParser#compilationUnit}.
	 * @param ctx the parse tree
	 */
	void exitCompilationUnit(BMSParser.CompilationUnitContext ctx);
	/**
	 * Enter a parse tree produced by {@link BMSParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(BMSParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BMSParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(BMSParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BMSParser#bms}.
	 * @param ctx the parse tree
	 */
	void enterBms(BMSParser.BmsContext ctx);
	/**
	 * Exit a parse tree produced by {@link BMSParser#bms}.
	 * @param ctx the parse tree
	 */
	void exitBms(BMSParser.BmsContext ctx);
	/**
	 * Enter a parse tree produced by {@link BMSParser#bmsWord}.
	 * @param ctx the parse tree
	 */
	void enterBmsWord(BMSParser.BmsWordContext ctx);
	/**
	 * Exit a parse tree produced by {@link BMSParser#bmsWord}.
	 * @param ctx the parse tree
	 */
	void exitBmsWord(BMSParser.BmsWordContext ctx);
	/**
	 * Enter a parse tree produced by {@link BMSParser#bmsSequenceArea}.
	 * @param ctx the parse tree
	 */
	void enterBmsSequenceArea(BMSParser.BmsSequenceAreaContext ctx);
	/**
	 * Exit a parse tree produced by {@link BMSParser#bmsSequenceArea}.
	 * @param ctx the parse tree
	 */
	void exitBmsSequenceArea(BMSParser.BmsSequenceAreaContext ctx);
	/**
	 * Enter a parse tree produced by {@link BMSParser#comment}.
	 * @param ctx the parse tree
	 */
	void enterComment(BMSParser.CommentContext ctx);
	/**
	 * Exit a parse tree produced by {@link BMSParser#comment}.
	 * @param ctx the parse tree
	 */
	void exitComment(BMSParser.CommentContext ctx);
	/**
	 * Enter a parse tree produced by {@link BMSParser#commentWord}.
	 * @param ctx the parse tree
	 */
	void enterCommentWord(BMSParser.CommentWordContext ctx);
	/**
	 * Exit a parse tree produced by {@link BMSParser#commentWord}.
	 * @param ctx the parse tree
	 */
	void exitCommentWord(BMSParser.CommentWordContext ctx);
	/**
	 * Enter a parse tree produced by {@link BMSParser#commentSequenceArea}.
	 * @param ctx the parse tree
	 */
	void enterCommentSequenceArea(BMSParser.CommentSequenceAreaContext ctx);
	/**
	 * Exit a parse tree produced by {@link BMSParser#commentSequenceArea}.
	 * @param ctx the parse tree
	 */
	void exitCommentSequenceArea(BMSParser.CommentSequenceAreaContext ctx);
	/**
	 * Enter a parse tree produced by {@link BMSParser#unknown}.
	 * @param ctx the parse tree
	 */
	void enterUnknown(BMSParser.UnknownContext ctx);
	/**
	 * Exit a parse tree produced by {@link BMSParser#unknown}.
	 * @param ctx the parse tree
	 */
	void exitUnknown(BMSParser.UnknownContext ctx);
	/**
	 * Enter a parse tree produced by {@link BMSParser#unknownWord}.
	 * @param ctx the parse tree
	 */
	void enterUnknownWord(BMSParser.UnknownWordContext ctx);
	/**
	 * Exit a parse tree produced by {@link BMSParser#unknownWord}.
	 * @param ctx the parse tree
	 */
	void exitUnknownWord(BMSParser.UnknownWordContext ctx);
	/**
	 * Enter a parse tree produced by {@link BMSParser#unknownSequenceArea}.
	 * @param ctx the parse tree
	 */
	void enterUnknownSequenceArea(BMSParser.UnknownSequenceAreaContext ctx);
	/**
	 * Exit a parse tree produced by {@link BMSParser#unknownSequenceArea}.
	 * @param ctx the parse tree
	 */
	void exitUnknownSequenceArea(BMSParser.UnknownSequenceAreaContext ctx);
}