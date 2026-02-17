/*
 * Copyright 2025 the original author or authors.
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
	 * Visit a parse tree produced by {@link ControlMParser#definitionSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefinitionSection(ControlMParser.DefinitionSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ControlMParser#definitionLine}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefinitionLine(ControlMParser.DefinitionLineContext ctx);
	/**
	 * Visit a parse tree produced by {@link ControlMParser#memLine}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMemLine(ControlMParser.MemLineContext ctx);
	/**
	 * Visit a parse tree produced by {@link ControlMParser#memName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMemName(ControlMParser.MemNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link ControlMParser#memLib}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMemLib(ControlMParser.MemLibContext ctx);
	/**
	 * Visit a parse tree produced by {@link ControlMParser#ownerLine}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOwnerLine(ControlMParser.OwnerLineContext ctx);
	/**
	 * Visit a parse tree produced by {@link ControlMParser#owner}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOwner(ControlMParser.OwnerContext ctx);
	/**
	 * Visit a parse tree produced by {@link ControlMParser#taskType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTaskType(ControlMParser.TaskTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link ControlMParser#preventNc2}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPreventNc2(ControlMParser.PreventNc2Context ctx);
	/**
	 * Visit a parse tree produced by {@link ControlMParser#dflt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDflt(ControlMParser.DfltContext ctx);
	/**
	 * Visit a parse tree produced by {@link ControlMParser#applLine}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitApplLine(ControlMParser.ApplLineContext ctx);
	/**
	 * Visit a parse tree produced by {@link ControlMParser#appl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAppl(ControlMParser.ApplContext ctx);
	/**
	 * Visit a parse tree produced by {@link ControlMParser#group}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroup(ControlMParser.GroupContext ctx);
	/**
	 * Visit a parse tree produced by {@link ControlMParser#descLine}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDescLine(ControlMParser.DescLineContext ctx);
	/**
	 * Visit a parse tree produced by {@link ControlMParser#overlibLine}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOverlibLine(ControlMParser.OverlibLineContext ctx);
	/**
	 * Visit a parse tree produced by {@link ControlMParser#overlib}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOverlib(ControlMParser.OverlibContext ctx);
	/**
	 * Visit a parse tree produced by {@link ControlMParser#statCal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatCal(ControlMParser.StatCalContext ctx);
	/**
	 * Visit a parse tree produced by {@link ControlMParser#schenvLine}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchenvLine(ControlMParser.SchenvLineContext ctx);
	/**
	 * Visit a parse tree produced by {@link ControlMParser#schenv}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchenv(ControlMParser.SchenvContext ctx);
	/**
	 * Visit a parse tree produced by {@link ControlMParser#systemId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSystemId(ControlMParser.SystemIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link ControlMParser#njeNode}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNjeNode(ControlMParser.NjeNodeContext ctx);
	/**
	 * Visit a parse tree produced by {@link ControlMParser#setVarLine}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetVarLine(ControlMParser.SetVarLineContext ctx);
	/**
	 * Visit a parse tree produced by {@link ControlMParser#ctbSetLine}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCtbSetLine(ControlMParser.CtbSetLineContext ctx);
	/**
	 * Visit a parse tree produced by {@link ControlMParser#docLine}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDocLine(ControlMParser.DocLineContext ctx);
	/**
	 * Visit a parse tree produced by {@link ControlMParser#docMem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDocMem(ControlMParser.DocMemContext ctx);
	/**
	 * Visit a parse tree produced by {@link ControlMParser#docLib}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDocLib(ControlMParser.DocLibContext ctx);
	/**
	 * Visit a parse tree produced by {@link ControlMParser#scheduleSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitScheduleSection(ControlMParser.ScheduleSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ControlMParser#scheduleLine}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitScheduleLine(ControlMParser.ScheduleLineContext ctx);
	/**
	 * Visit a parse tree produced by {@link ControlMParser#inputSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInputSection(ControlMParser.InputSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ControlMParser#inputNamesLine}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInputNamesLine(ControlMParser.InputNamesLineContext ctx);
	/**
	 * Visit a parse tree produced by {@link ControlMParser#input}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInput(ControlMParser.InputContext ctx);
	/**
	 * Visit a parse tree produced by {@link ControlMParser#date}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDate(ControlMParser.DateContext ctx);
	/**
	 * Visit a parse tree produced by {@link ControlMParser#dateParam}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateParam(ControlMParser.DateParamContext ctx);
	/**
	 * Visit a parse tree produced by {@link ControlMParser#inputLine}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInputLine(ControlMParser.InputLineContext ctx);
	/**
	 * Visit a parse tree produced by {@link ControlMParser#outputSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOutputSection(ControlMParser.OutputSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ControlMParser#outputNamesLine}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOutputNamesLine(ControlMParser.OutputNamesLineContext ctx);
	/**
	 * Visit a parse tree produced by {@link ControlMParser#output}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOutput(ControlMParser.OutputContext ctx);
	/**
	 * Visit a parse tree produced by {@link ControlMParser#outputLine}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOutputLine(ControlMParser.OutputLineContext ctx);
	/**
	 * Visit a parse tree produced by {@link ControlMParser#applicationFormSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitApplicationFormSection(ControlMParser.ApplicationFormSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ControlMParser#applicationFormLine}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitApplicationFormLine(ControlMParser.ApplicationFormLineContext ctx);
	/**
	 * Visit a parse tree produced by {@link ControlMParser#name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitName(ControlMParser.NameContext ctx);
}
