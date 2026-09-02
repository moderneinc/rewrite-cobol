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
// Generated from /Users/jon/Projects/github/openrewrite/rewrite-cobol/.claude/worktrees/oraweb/src/main/antlr-utility/UtilityCardParser.g4 by ANTLR 4.13.2
package org.openrewrite.mainframe.controlcard.utility.internal.grammar;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link UtilityCardParser}.
 */
public interface UtilityCardParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link UtilityCardParser#compilationUnit}.
	 * @param ctx the parse tree
	 */
	void enterCompilationUnit(UtilityCardParser.CompilationUnitContext ctx);
	/**
	 * Exit a parse tree produced by {@link UtilityCardParser#compilationUnit}.
	 * @param ctx the parse tree
	 */
	void exitCompilationUnit(UtilityCardParser.CompilationUnitContext ctx);
	/**
	 * Enter a parse tree produced by {@link UtilityCardParser#word}.
	 * @param ctx the parse tree
	 */
	void enterWord(UtilityCardParser.WordContext ctx);
	/**
	 * Exit a parse tree produced by {@link UtilityCardParser#word}.
	 * @param ctx the parse tree
	 */
	void exitWord(UtilityCardParser.WordContext ctx);
}