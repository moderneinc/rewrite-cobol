/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
// Generated from java-escape by ANTLR 4.11.1
package org.openrewrite.controlm.internal.grammar;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link ControlMParser}.
 */
public interface ControlMParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link ControlMParser#compilationUnit}.
	 * @param ctx the parse tree
	 */
	void enterCompilationUnit(ControlMParser.CompilationUnitContext ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#compilationUnit}.
	 * @param ctx the parse tree
	 */
	void exitCompilationUnit(ControlMParser.CompilationUnitContext ctx);
	/**
	 * Enter a parse tree produced by {@link ControlMParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(ControlMParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(ControlMParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ControlMParser#controlM}.
	 * @param ctx the parse tree
	 */
	void enterControlM(ControlMParser.ControlMContext ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#controlM}.
	 * @param ctx the parse tree
	 */
	void exitControlM(ControlMParser.ControlMContext ctx);
	/**
	 * Enter a parse tree produced by {@link ControlMParser#controlMWord}.
	 * @param ctx the parse tree
	 */
	void enterControlMWord(ControlMParser.ControlMWordContext ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#controlMWord}.
	 * @param ctx the parse tree
	 */
	void exitControlMWord(ControlMParser.ControlMWordContext ctx);
	/**
	 * Enter a parse tree produced by {@link ControlMParser#controlMCommentArea}.
	 * @param ctx the parse tree
	 */
	void enterControlMCommentArea(ControlMParser.ControlMCommentAreaContext ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#controlMCommentArea}.
	 * @param ctx the parse tree
	 */
	void exitControlMCommentArea(ControlMParser.ControlMCommentAreaContext ctx);
}