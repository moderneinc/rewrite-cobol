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
	 * Enter a parse tree produced by {@link JCLParser#jclStatement}.
	 * @param ctx the parse tree
	 */
	void enterJclStatement(JCLParser.JclStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#jclStatement}.
	 * @param ctx the parse tree
	 */
	void exitJclStatement(JCLParser.JclStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link JCLParser#jobStatement}.
	 * @param ctx the parse tree
	 */
	void enterJobStatement(JCLParser.JobStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#jobStatement}.
	 * @param ctx the parse tree
	 */
	void exitJobStatement(JCLParser.JobStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link JCLParser#parameterArgument}.
	 * @param ctx the parse tree
	 */
	void enterParameterArgument(JCLParser.ParameterArgumentContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#parameterArgument}.
	 * @param ctx the parse tree
	 */
	void exitParameterArgument(JCLParser.ParameterArgumentContext ctx);
	/**
	 * Enter a parse tree produced by {@link JCLParser#jobName}.
	 * @param ctx the parse tree
	 */
	void enterJobName(JCLParser.JobNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#jobName}.
	 * @param ctx the parse tree
	 */
	void exitJobName(JCLParser.JobNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link JCLParser#jclLibStatement}.
	 * @param ctx the parse tree
	 */
	void enterJclLibStatement(JCLParser.JclLibStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#jclLibStatement}.
	 * @param ctx the parse tree
	 */
	void exitJclLibStatement(JCLParser.JclLibStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link JCLParser#jclLibName}.
	 * @param ctx the parse tree
	 */
	void enterJclLibName(JCLParser.JclLibNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#jclLibName}.
	 * @param ctx the parse tree
	 */
	void exitJclLibName(JCLParser.JclLibNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link JCLParser#ddStatement}.
	 * @param ctx the parse tree
	 */
	void enterDdStatement(JCLParser.DdStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#ddStatement}.
	 * @param ctx the parse tree
	 */
	void exitDdStatement(JCLParser.DdStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link JCLParser#ddStreamStatement}.
	 * @param ctx the parse tree
	 */
	void enterDdStreamStatement(JCLParser.DdStreamStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#ddStreamStatement}.
	 * @param ctx the parse tree
	 */
	void exitDdStreamStatement(JCLParser.DdStreamStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link JCLParser#ddName}.
	 * @param ctx the parse tree
	 */
	void enterDdName(JCLParser.DdNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#ddName}.
	 * @param ctx the parse tree
	 */
	void exitDdName(JCLParser.DdNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link JCLParser#streamText}.
	 * @param ctx the parse tree
	 */
	void enterStreamText(JCLParser.StreamTextContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#streamText}.
	 * @param ctx the parse tree
	 */
	void exitStreamText(JCLParser.StreamTextContext ctx);
	/**
	 * Enter a parse tree produced by {@link JCLParser#streamJclCommentArea}.
	 * @param ctx the parse tree
	 */
	void enterStreamJclCommentArea(JCLParser.StreamJclCommentAreaContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#streamJclCommentArea}.
	 * @param ctx the parse tree
	 */
	void exitStreamJclCommentArea(JCLParser.StreamJclCommentAreaContext ctx);
	/**
	 * Enter a parse tree produced by {@link JCLParser#execStatement}.
	 * @param ctx the parse tree
	 */
	void enterExecStatement(JCLParser.ExecStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#execStatement}.
	 * @param ctx the parse tree
	 */
	void exitExecStatement(JCLParser.ExecStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link JCLParser#execName}.
	 * @param ctx the parse tree
	 */
	void enterExecName(JCLParser.ExecNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#execName}.
	 * @param ctx the parse tree
	 */
	void exitExecName(JCLParser.ExecNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link JCLParser#outputStatement}.
	 * @param ctx the parse tree
	 */
	void enterOutputStatement(JCLParser.OutputStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#outputStatement}.
	 * @param ctx the parse tree
	 */
	void exitOutputStatement(JCLParser.OutputStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link JCLParser#outputName}.
	 * @param ctx the parse tree
	 */
	void enterOutputName(JCLParser.OutputNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#outputName}.
	 * @param ctx the parse tree
	 */
	void exitOutputName(JCLParser.OutputNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link JCLParser#pendStatement}.
	 * @param ctx the parse tree
	 */
	void enterPendStatement(JCLParser.PendStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#pendStatement}.
	 * @param ctx the parse tree
	 */
	void exitPendStatement(JCLParser.PendStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link JCLParser#pendName}.
	 * @param ctx the parse tree
	 */
	void enterPendName(JCLParser.PendNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#pendName}.
	 * @param ctx the parse tree
	 */
	void exitPendName(JCLParser.PendNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link JCLParser#procStatement}.
	 * @param ctx the parse tree
	 */
	void enterProcStatement(JCLParser.ProcStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#procStatement}.
	 * @param ctx the parse tree
	 */
	void exitProcStatement(JCLParser.ProcStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link JCLParser#procName}.
	 * @param ctx the parse tree
	 */
	void enterProcName(JCLParser.ProcNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#procName}.
	 * @param ctx the parse tree
	 */
	void exitProcName(JCLParser.ProcNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link JCLParser#setStatement}.
	 * @param ctx the parse tree
	 */
	void enterSetStatement(JCLParser.SetStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#setStatement}.
	 * @param ctx the parse tree
	 */
	void exitSetStatement(JCLParser.SetStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link JCLParser#setName}.
	 * @param ctx the parse tree
	 */
	void enterSetName(JCLParser.SetNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#setName}.
	 * @param ctx the parse tree
	 */
	void exitSetName(JCLParser.SetNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link JCLParser#xmitStatement}.
	 * @param ctx the parse tree
	 */
	void enterXmitStatement(JCLParser.XmitStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#xmitStatement}.
	 * @param ctx the parse tree
	 */
	void exitXmitStatement(JCLParser.XmitStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link JCLParser#xmitName}.
	 * @param ctx the parse tree
	 */
	void enterXmitName(JCLParser.XmitNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#xmitName}.
	 * @param ctx the parse tree
	 */
	void exitXmitName(JCLParser.XmitNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link JCLParser#parameter}.
	 * @param ctx the parse tree
	 */
	void enterParameter(JCLParser.ParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#parameter}.
	 * @param ctx the parse tree
	 */
	void exitParameter(JCLParser.ParameterContext ctx);
	/**
	 * Enter a parse tree produced by {@link JCLParser#parameterParentheses}.
	 * @param ctx the parse tree
	 */
	void enterParameterParentheses(JCLParser.ParameterParenthesesContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#parameterParentheses}.
	 * @param ctx the parse tree
	 */
	void exitParameterParentheses(JCLParser.ParameterParenthesesContext ctx);
	/**
	 * Enter a parse tree produced by {@link JCLParser#parameterAssignment}.
	 * @param ctx the parse tree
	 */
	void enterParameterAssignment(JCLParser.ParameterAssignmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#parameterAssignment}.
	 * @param ctx the parse tree
	 */
	void exitParameterAssignment(JCLParser.ParameterAssignmentContext ctx);
	/**
	 * Enter a parse tree produced by {@link JCLParser#name}.
	 * @param ctx the parse tree
	 */
	void enterName(JCLParser.NameContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#name}.
	 * @param ctx the parse tree
	 */
	void exitName(JCLParser.NameContext ctx);
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
	 * Enter a parse tree produced by {@link JCLParser#jclName}.
	 * @param ctx the parse tree
	 */
	void enterJclName(JCLParser.JclNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#jclName}.
	 * @param ctx the parse tree
	 */
	void exitJclName(JCLParser.JclNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link JCLParser#jclKeyword}.
	 * @param ctx the parse tree
	 */
	void enterJclKeyword(JCLParser.JclKeywordContext ctx);
	/**
	 * Exit a parse tree produced by {@link JCLParser#jclKeyword}.
	 * @param ctx the parse tree
	 */
	void exitJclKeyword(JCLParser.JclKeywordContext ctx);
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