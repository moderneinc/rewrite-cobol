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
package org.openrewrite.mainframe.cobol;

import org.junit.jupiter.api.Test;

import static org.openrewrite.mainframe.cobol.Assertions.cobol;

/**
 * A copybook of nothing but a DB2 table declaration contributes no COBOL words: preprocessing elides the EXEC block,
 * and comment lines are not words either. Everything that follows the copy still belongs to the including program.
 * <p>
 * The copied-word bookkeeping used to assume a copybook always yields at least one word — it opened on the word that
 * carried the copybook rather than on the copy template itself, so a copybook with no words opened after its own stop
 * comment closed, and every word in the rest of the program was taken for copied source and printed as nothing. Over
 * the corpus that lost about 80% of the text of 15 programs.
 */
class CopybookWithoutWordsTest extends CobolTest {

    @Test
    void copyOfADeclarationOnlyCopybook() {
        rewriteRun(
          cobol(
            """
              000000 IDENTIFICATION DIVISION.
              000000 PROGRAM-ID. DECLONLY.
              000000 DATA DIVISION.
              000000 WORKING-STORAGE SECTION.
              000000     COPY DECLONLY.
              000000 01 AFTER-COPY.
              000000    03 FIELD-A PIC X(4).
              000000 PROCEDURE DIVISION.
              000000     GOBACK.
              """
          )
        );
    }

    @Test
    void includeOfADeclarationOnlyCopybook() {
        rewriteRun(
          cobol(
            """
              000000 IDENTIFICATION DIVISION.
              000000 PROGRAM-ID. DECLONLY.
              000000 DATA DIVISION.
              000000 WORKING-STORAGE SECTION.
              000000     EXEC SQL INCLUDE DECLONLY END-EXEC.
              000000 01 AFTER-INCLUDE.
              000000    03 FIELD-A PIC X(4).
              000000 PROCEDURE DIVISION.
              000000     GOBACK.
              """
          )
        );
    }

    /**
     * The comment belongs to the program, and the copy statement it precedes prints it. Keeping it on the word that
     * carries the copybook as well printed it twice.
     */
    @Test
    void commentBeforeADeclarationOnlyCopy() {
        rewriteRun(
          cobol(
            """
              000000 IDENTIFICATION DIVISION.
              000000 PROGRAM-ID. CMTDECL.
              000000 DATA DIVISION.
              000000 WORKING-STORAGE SECTION.
              000000* Get the table
              000000     EXEC SQL INCLUDE DECLONLY END-EXEC.
              000000 01 AFTER-INCLUDE.
              000000    03 FIELD-A PIC X(4).
              000000 PROCEDURE DIVISION.
              000000     GOBACK.
              """
          )
        );
    }

    /**
     * The genapp programs write the include over three lines rather than one.
     */
    @Test
    void includeSplitOverLines() {
        rewriteRun(
          cobol(
            """
              000000 IDENTIFICATION DIVISION.
              000000 PROGRAM-ID. SPLIT.
              000000 DATA DIVISION.
              000000 WORKING-STORAGE SECTION.
              000000     EXEC SQL
              000000        INCLUDE DECLONLY
              000000     END-EXEC.
              000000 01 AFTER-INCLUDE.
              000000    03 FIELD-A PIC X(4).
              000000 PROCEDURE DIVISION.
              000000     GOBACK.
              """
          )
        );
    }

    /**
     * A copybook that does contribute words takes the other path, where the word carrying the copybook is the first
     * copied one and the copybook has already printed it.
     */
    @Test
    void copybookThatContributesWords() {
        rewriteRun(
          cobol(
            """
              000000 IDENTIFICATION DIVISION.
              000000 PROGRAM-ID. WITHWORD.
              000000 DATA DIVISION.
              000000 WORKING-STORAGE SECTION.
              000000* Get the table
              000000     EXEC SQL INCLUDE MGSACATG END-EXEC.
              000000 01 AFTER-INCLUDE.
              000000    03 FIELD-A PIC X(4).
              000000 PROCEDURE DIVISION.
              000000     GOBACK.
              """
          )
        );
    }
}
