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
	 * Enter a parse tree produced by {@link ControlMParser#definitionSection}.
	 * @param ctx the parse tree
	 */
	void enterDefinitionSection(ControlMParser.DefinitionSectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#definitionSection}.
	 * @param ctx the parse tree
	 */
	void exitDefinitionSection(ControlMParser.DefinitionSectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ControlMParser#definitionLine}.
	 * @param ctx the parse tree
	 */
	void enterDefinitionLine(ControlMParser.DefinitionLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#definitionLine}.
	 * @param ctx the parse tree
	 */
	void exitDefinitionLine(ControlMParser.DefinitionLineContext ctx);
	/**
	 * Enter a parse tree produced by {@link ControlMParser#memLine}.
	 * @param ctx the parse tree
	 */
	void enterMemLine(ControlMParser.MemLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#memLine}.
	 * @param ctx the parse tree
	 */
	void exitMemLine(ControlMParser.MemLineContext ctx);
	/**
	 * Enter a parse tree produced by {@link ControlMParser#memName}.
	 * @param ctx the parse tree
	 */
	void enterMemName(ControlMParser.MemNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#memName}.
	 * @param ctx the parse tree
	 */
	void exitMemName(ControlMParser.MemNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link ControlMParser#memLib}.
	 * @param ctx the parse tree
	 */
	void enterMemLib(ControlMParser.MemLibContext ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#memLib}.
	 * @param ctx the parse tree
	 */
	void exitMemLib(ControlMParser.MemLibContext ctx);
	/**
	 * Enter a parse tree produced by {@link ControlMParser#ownerLine}.
	 * @param ctx the parse tree
	 */
	void enterOwnerLine(ControlMParser.OwnerLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#ownerLine}.
	 * @param ctx the parse tree
	 */
	void exitOwnerLine(ControlMParser.OwnerLineContext ctx);
	/**
	 * Enter a parse tree produced by {@link ControlMParser#owner}.
	 * @param ctx the parse tree
	 */
	void enterOwner(ControlMParser.OwnerContext ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#owner}.
	 * @param ctx the parse tree
	 */
	void exitOwner(ControlMParser.OwnerContext ctx);
	/**
	 * Enter a parse tree produced by {@link ControlMParser#taskType}.
	 * @param ctx the parse tree
	 */
	void enterTaskType(ControlMParser.TaskTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#taskType}.
	 * @param ctx the parse tree
	 */
	void exitTaskType(ControlMParser.TaskTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link ControlMParser#preventNc2}.
	 * @param ctx the parse tree
	 */
	void enterPreventNc2(ControlMParser.PreventNc2Context ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#preventNc2}.
	 * @param ctx the parse tree
	 */
	void exitPreventNc2(ControlMParser.PreventNc2Context ctx);
	/**
	 * Enter a parse tree produced by {@link ControlMParser#dflt}.
	 * @param ctx the parse tree
	 */
	void enterDflt(ControlMParser.DfltContext ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#dflt}.
	 * @param ctx the parse tree
	 */
	void exitDflt(ControlMParser.DfltContext ctx);
	/**
	 * Enter a parse tree produced by {@link ControlMParser#applLine}.
	 * @param ctx the parse tree
	 */
	void enterApplLine(ControlMParser.ApplLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#applLine}.
	 * @param ctx the parse tree
	 */
	void exitApplLine(ControlMParser.ApplLineContext ctx);
	/**
	 * Enter a parse tree produced by {@link ControlMParser#appl}.
	 * @param ctx the parse tree
	 */
	void enterAppl(ControlMParser.ApplContext ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#appl}.
	 * @param ctx the parse tree
	 */
	void exitAppl(ControlMParser.ApplContext ctx);
	/**
	 * Enter a parse tree produced by {@link ControlMParser#group}.
	 * @param ctx the parse tree
	 */
	void enterGroup(ControlMParser.GroupContext ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#group}.
	 * @param ctx the parse tree
	 */
	void exitGroup(ControlMParser.GroupContext ctx);
	/**
	 * Enter a parse tree produced by {@link ControlMParser#descLine}.
	 * @param ctx the parse tree
	 */
	void enterDescLine(ControlMParser.DescLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#descLine}.
	 * @param ctx the parse tree
	 */
	void exitDescLine(ControlMParser.DescLineContext ctx);
	/**
	 * Enter a parse tree produced by {@link ControlMParser#overlibLine}.
	 * @param ctx the parse tree
	 */
	void enterOverlibLine(ControlMParser.OverlibLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#overlibLine}.
	 * @param ctx the parse tree
	 */
	void exitOverlibLine(ControlMParser.OverlibLineContext ctx);
	/**
	 * Enter a parse tree produced by {@link ControlMParser#overlib}.
	 * @param ctx the parse tree
	 */
	void enterOverlib(ControlMParser.OverlibContext ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#overlib}.
	 * @param ctx the parse tree
	 */
	void exitOverlib(ControlMParser.OverlibContext ctx);
	/**
	 * Enter a parse tree produced by {@link ControlMParser#statCal}.
	 * @param ctx the parse tree
	 */
	void enterStatCal(ControlMParser.StatCalContext ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#statCal}.
	 * @param ctx the parse tree
	 */
	void exitStatCal(ControlMParser.StatCalContext ctx);
	/**
	 * Enter a parse tree produced by {@link ControlMParser#schenvLine}.
	 * @param ctx the parse tree
	 */
	void enterSchenvLine(ControlMParser.SchenvLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#schenvLine}.
	 * @param ctx the parse tree
	 */
	void exitSchenvLine(ControlMParser.SchenvLineContext ctx);
	/**
	 * Enter a parse tree produced by {@link ControlMParser#schenv}.
	 * @param ctx the parse tree
	 */
	void enterSchenv(ControlMParser.SchenvContext ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#schenv}.
	 * @param ctx the parse tree
	 */
	void exitSchenv(ControlMParser.SchenvContext ctx);
	/**
	 * Enter a parse tree produced by {@link ControlMParser#systemId}.
	 * @param ctx the parse tree
	 */
	void enterSystemId(ControlMParser.SystemIdContext ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#systemId}.
	 * @param ctx the parse tree
	 */
	void exitSystemId(ControlMParser.SystemIdContext ctx);
	/**
	 * Enter a parse tree produced by {@link ControlMParser#njeNode}.
	 * @param ctx the parse tree
	 */
	void enterNjeNode(ControlMParser.NjeNodeContext ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#njeNode}.
	 * @param ctx the parse tree
	 */
	void exitNjeNode(ControlMParser.NjeNodeContext ctx);
	/**
	 * Enter a parse tree produced by {@link ControlMParser#setVarLine}.
	 * @param ctx the parse tree
	 */
	void enterSetVarLine(ControlMParser.SetVarLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#setVarLine}.
	 * @param ctx the parse tree
	 */
	void exitSetVarLine(ControlMParser.SetVarLineContext ctx);
	/**
	 * Enter a parse tree produced by {@link ControlMParser#ctbSetLine}.
	 * @param ctx the parse tree
	 */
	void enterCtbSetLine(ControlMParser.CtbSetLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#ctbSetLine}.
	 * @param ctx the parse tree
	 */
	void exitCtbSetLine(ControlMParser.CtbSetLineContext ctx);
	/**
	 * Enter a parse tree produced by {@link ControlMParser#docLine}.
	 * @param ctx the parse tree
	 */
	void enterDocLine(ControlMParser.DocLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#docLine}.
	 * @param ctx the parse tree
	 */
	void exitDocLine(ControlMParser.DocLineContext ctx);
	/**
	 * Enter a parse tree produced by {@link ControlMParser#docMem}.
	 * @param ctx the parse tree
	 */
	void enterDocMem(ControlMParser.DocMemContext ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#docMem}.
	 * @param ctx the parse tree
	 */
	void exitDocMem(ControlMParser.DocMemContext ctx);
	/**
	 * Enter a parse tree produced by {@link ControlMParser#docLib}.
	 * @param ctx the parse tree
	 */
	void enterDocLib(ControlMParser.DocLibContext ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#docLib}.
	 * @param ctx the parse tree
	 */
	void exitDocLib(ControlMParser.DocLibContext ctx);
	/**
	 * Enter a parse tree produced by {@link ControlMParser#scheduleSection}.
	 * @param ctx the parse tree
	 */
	void enterScheduleSection(ControlMParser.ScheduleSectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#scheduleSection}.
	 * @param ctx the parse tree
	 */
	void exitScheduleSection(ControlMParser.ScheduleSectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ControlMParser#scheduleLine}.
	 * @param ctx the parse tree
	 */
	void enterScheduleLine(ControlMParser.ScheduleLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#scheduleLine}.
	 * @param ctx the parse tree
	 */
	void exitScheduleLine(ControlMParser.ScheduleLineContext ctx);
	/**
	 * Enter a parse tree produced by {@link ControlMParser#inputSection}.
	 * @param ctx the parse tree
	 */
	void enterInputSection(ControlMParser.InputSectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#inputSection}.
	 * @param ctx the parse tree
	 */
	void exitInputSection(ControlMParser.InputSectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ControlMParser#inputLine}.
	 * @param ctx the parse tree
	 */
	void enterInputLine(ControlMParser.InputLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#inputLine}.
	 * @param ctx the parse tree
	 */
	void exitInputLine(ControlMParser.InputLineContext ctx);
	/**
	 * Enter a parse tree produced by {@link ControlMParser#inLine}.
	 * @param ctx the parse tree
	 */
	void enterInLine(ControlMParser.InLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#inLine}.
	 * @param ctx the parse tree
	 */
	void exitInLine(ControlMParser.InLineContext ctx);
	/**
	 * Enter a parse tree produced by {@link ControlMParser#in}.
	 * @param ctx the parse tree
	 */
	void enterIn(ControlMParser.InContext ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#in}.
	 * @param ctx the parse tree
	 */
	void exitIn(ControlMParser.InContext ctx);
	/**
	 * Enter a parse tree produced by {@link ControlMParser#odat}.
	 * @param ctx the parse tree
	 */
	void enterOdat(ControlMParser.OdatContext ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#odat}.
	 * @param ctx the parse tree
	 */
	void exitOdat(ControlMParser.OdatContext ctx);
	/**
	 * Enter a parse tree produced by {@link ControlMParser#outputSection}.
	 * @param ctx the parse tree
	 */
	void enterOutputSection(ControlMParser.OutputSectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#outputSection}.
	 * @param ctx the parse tree
	 */
	void exitOutputSection(ControlMParser.OutputSectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ControlMParser#outputLine}.
	 * @param ctx the parse tree
	 */
	void enterOutputLine(ControlMParser.OutputLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#outputLine}.
	 * @param ctx the parse tree
	 */
	void exitOutputLine(ControlMParser.OutputLineContext ctx);
	/**
	 * Enter a parse tree produced by {@link ControlMParser#outLine}.
	 * @param ctx the parse tree
	 */
	void enterOutLine(ControlMParser.OutLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#outLine}.
	 * @param ctx the parse tree
	 */
	void exitOutLine(ControlMParser.OutLineContext ctx);
	/**
	 * Enter a parse tree produced by {@link ControlMParser#out}.
	 * @param ctx the parse tree
	 */
	void enterOut(ControlMParser.OutContext ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#out}.
	 * @param ctx the parse tree
	 */
	void exitOut(ControlMParser.OutContext ctx);
	/**
	 * Enter a parse tree produced by {@link ControlMParser#applicationFormSection}.
	 * @param ctx the parse tree
	 */
	void enterApplicationFormSection(ControlMParser.ApplicationFormSectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#applicationFormSection}.
	 * @param ctx the parse tree
	 */
	void exitApplicationFormSection(ControlMParser.ApplicationFormSectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ControlMParser#applicationFormLine}.
	 * @param ctx the parse tree
	 */
	void enterApplicationFormLine(ControlMParser.ApplicationFormLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#applicationFormLine}.
	 * @param ctx the parse tree
	 */
	void exitApplicationFormLine(ControlMParser.ApplicationFormLineContext ctx);
	/**
	 * Enter a parse tree produced by {@link ControlMParser#name}.
	 * @param ctx the parse tree
	 */
	void enterName(ControlMParser.NameContext ctx);
	/**
	 * Exit a parse tree produced by {@link ControlMParser#name}.
	 * @param ctx the parse tree
	 */
	void exitName(ControlMParser.NameContext ctx);
}