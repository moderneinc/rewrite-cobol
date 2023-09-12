/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
// Generated from java-escape by ANTLR 4.11.1
package org.openrewrite.controlm.internal.grammar;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link ControlMParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface ControlMParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link ControlMParser#compilationUnit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompilationUnit(ControlMParser.CompilationUnitContext ctx);
	/**
	 * Visit a parse tree produced by {@link ControlMParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(ControlMParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link ControlMParser#controlM}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitControlM(ControlMParser.ControlMContext ctx);
	/**
	 * Visit a parse tree produced by {@link ControlMParser#controlMWord}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitControlMWord(ControlMParser.ControlMWordContext ctx);
	/**
	 * Visit a parse tree produced by {@link ControlMParser#controlMCommentArea}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitControlMCommentArea(ControlMParser.ControlMCommentAreaContext ctx);
}