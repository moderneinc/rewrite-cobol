/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
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
	 * Visit a parse tree produced by {@link JCLParser#jclStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJclStatement(JCLParser.JclStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#jobStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJobStatement(JCLParser.JobStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#parameterArgument}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterArgument(JCLParser.ParameterArgumentContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#jobName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJobName(JCLParser.JobNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#jclLibStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJclLibStatement(JCLParser.JclLibStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#jclLibName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJclLibName(JCLParser.JclLibNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#cntlStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCntlStatement(JCLParser.CntlStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#cntlName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCntlName(JCLParser.CntlNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#endcntlStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEndcntlStatement(JCLParser.EndcntlStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#endcntlName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEndcntlName(JCLParser.EndcntlNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#ddStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDdStatement(JCLParser.DdStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#ddStreamStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDdStreamStatement(JCLParser.DdStreamStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#ddName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDdName(JCLParser.DdNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#streamText}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStreamText(JCLParser.StreamTextContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#streamJclCommentArea}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStreamJclCommentArea(JCLParser.StreamJclCommentAreaContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#endifStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEndifStatement(JCLParser.EndifStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#endifName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEndifName(JCLParser.EndifNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#execStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExecStatement(JCLParser.ExecStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#execName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExecName(JCLParser.ExecNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#exportStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExportStatement(JCLParser.ExportStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#exportName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExportName(JCLParser.ExportNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#ifStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfStatement(JCLParser.IfStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#ifName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfName(JCLParser.IfNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#thenName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitThenName(JCLParser.ThenNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#elseStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElseStatement(JCLParser.ElseStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#elseName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElseName(JCLParser.ElseNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#includeStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIncludeStatement(JCLParser.IncludeStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#includeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIncludeName(JCLParser.IncludeNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#outputStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOutputStatement(JCLParser.OutputStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#outputName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOutputName(JCLParser.OutputNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#pendStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPendStatement(JCLParser.PendStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#pendName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPendName(JCLParser.PendNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#procStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProcStatement(JCLParser.ProcStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#procName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProcName(JCLParser.ProcNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#setStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetStatement(JCLParser.SetStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#setName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetName(JCLParser.SetNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#xmitStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmitStatement(JCLParser.XmitStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#xmitName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmitName(JCLParser.XmitNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#parameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameter(JCLParser.ParameterContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#parameterParentheses}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterParentheses(JCLParser.ParameterParenthesesContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#parameterAssignment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterAssignment(JCLParser.ParameterAssignmentContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitName(JCLParser.NameContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#jclWord}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJclWord(JCLParser.JclWordContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#jclName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJclName(JCLParser.JclNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link JCLParser#jclKeyword}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJclKeyword(JCLParser.JclKeywordContext ctx);
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