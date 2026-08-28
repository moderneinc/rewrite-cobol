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
package org.openrewrite.mainframe.cobol.internal;

/**
 * Collection keys required by the COBOL antlr grammar.
 */
public class CobolGrammarToken {
    public static final String COMMENT_ENTRY = "*>CE ";
    public static final String COMMENT = "*> ";
    public static final String END_OF_FILE = "<EOF>";

    /**
     * Preprocessing removes an EXEC block from the text the COBOL grammar sees. A tagged line takes its place so
     * that the block is still a statement; the grammar matches one of these tags per EXEC dialect.
     */
    public static final String EXEC_TAG = "*>EXEC";
    public static final String EXEC_CICS = EXEC_TAG + "CICS ";
    public static final String EXEC_DLI = EXEC_TAG + "DLI ";
    public static final String EXEC_SQL = EXEC_TAG + "SQL ";
    public static final String EXEC_SQL_IMS = EXEC_TAG + "SQLIMS ";
}
