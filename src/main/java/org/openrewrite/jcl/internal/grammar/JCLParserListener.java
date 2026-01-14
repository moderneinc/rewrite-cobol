/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
// Generated from java-escape by ANTLR 4.11.1
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
	 * Enter a parse tree produced by {@link JCLParser#jclCommentArea}.
	 * @param ctx the parse tree
	 */
	void enterJclCommentArea(JCLParser.JclCommentAreaContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#jclCommentArea}.
	 * @param ctx the parse tree
	 */
	void exitJclCommentArea(JCLParser.JclCommentAreaContext ctx);
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
	 * Enter a parse tree produced by {@link JCLParser#jes2CommentArea}.
	 * @param ctx the parse tree
	 */
	void enterJes2CommentArea(JCLParser.Jes2CommentAreaContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#jes2CommentArea}.
	 * @param ctx the parse tree
	 */
	void exitJes2CommentArea(JCLParser.Jes2CommentAreaContext ctx);
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
	 * Enter a parse tree produced by {@link JCLParser#jes3CommentArea}.
	 * @param ctx the parse tree
	 */
	void enterJes3CommentArea(JCLParser.Jes3CommentAreaContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#jes3CommentArea}.
	 * @param ctx the parse tree
	 */
	void exitJes3CommentArea(JCLParser.Jes3CommentAreaContext ctx);
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
	 * Enter a parse tree produced by {@link JCLParser#streamCommentArea}.
	 * @param ctx the parse tree
	 */
	void enterStreamCommentArea(JCLParser.StreamCommentAreaContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#streamCommentArea}.
	 * @param ctx the parse tree
	 */
	void exitStreamCommentArea(JCLParser.StreamCommentAreaContext ctx);
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
	 * Enter a parse tree produced by {@link JCLParser#controlMCommentArea}.
	 * @param ctx the parse tree
	 */
	void enterControlMCommentArea(JCLParser.ControlMCommentAreaContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#controlMCommentArea}.
	 * @param ctx the parse tree
	 */
	void exitControlMCommentArea(JCLParser.ControlMCommentAreaContext ctx);
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
	 * Enter a parse tree produced by {@link JCLParser#commentCommentArea}.
	 * @param ctx the parse tree
	 */
	void enterCommentCommentArea(JCLParser.CommentCommentAreaContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#commentCommentArea}.
	 * @param ctx the parse tree
	 */
	void exitCommentCommentArea(JCLParser.CommentCommentAreaContext ctx);
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
	 * Enter a parse tree produced by {@link JCLParser#unknownCommentArea}.
	 * @param ctx the parse tree
	 */
	void enterUnknownCommentArea(JCLParser.UnknownCommentAreaContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#unknownCommentArea}.
	 * @param ctx the parse tree
	 */
	void exitUnknownCommentArea(JCLParser.UnknownCommentAreaContext ctx);
}
