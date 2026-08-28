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
package org.openrewrite.mainframe.sas.trait;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Where one statement ends and the next begins, which is the whole of the syntax a SAS program has.
 */
class StatementsTest {

    @Test
    void readsAStatementUpToItsSemicolon() {
        assertThat(Statements.in(
          """
            LIBNAME CLMSAS;

            DATA CLMSAS.CLMDAY;
               INFILE CLMEXTR LRECL=200 RECFM=FB;
            RUN;
            """))
          .extracting(Statements.Statement::getKeyword, Statements.Statement::getWordTexts,
            Statements.Statement::getLine)
          .containsExactly(
            tuple("LIBNAME", List.of("LIBNAME", "CLMSAS"), 1),
            tuple("DATA", List.of("DATA", "CLMSAS.CLMDAY"), 3),
            tuple("INFILE", List.of("INFILE", "CLMEXTR", "LRECL=200", "RECFM=FB"), 4),
            tuple("RUN", List.of("RUN"), 5));
    }

    /**
     * A comment may stand anywhere a blank may, and it carries nothing a reference is read from — so
     * what it must not do is end the statement it stands in the middle of.
     */
    @Test
    void keepsABlockCommentOutOfTheStatementItStandsIn() {
        assertThat(Statements.in(
          """
            /*  CLMSMAC - THE FORMATS AND THE TITLE MACRO.  */
            OPTIONS LS=132 /* AS THE ACTUARIES ASKED */ PS=60;
            """))
          .extracting(Statements.Statement::getWordTexts)
          .containsExactly(List.of("OPTIONS", "LS=132", "PS=60"));
    }

    /**
     * A statement beginning {@code *} is a comment that runs to its own semicolon, and the same
     * character in the middle of a statement multiplies. Only where the statement starts says which.
     */
    @Test
    void tellsACommentStatementFromAMultiplication() {
        assertThat(Statements.in(
          """
            * THE RESERVE; IS NOT WHAT IT WAS;
            RSVCHG = NEWRSV * OLDRSV;
            """))
          .extracting(Statements.Statement::getWordTexts)
          .containsExactly(
            List.of("IS", "NOT", "WHAT", "IT", "WAS"),
            List.of("RSVCHG", "=", "NEWRSV", "*", "OLDRSV"));
    }

    @Test
    void takesAQuotedStringWholeHoweverItIsPunctuated() {
        assertThat(Statements.in(
          """
            TITLE1 "CASCADE MUTUAL - CLAIMS; RESERVES - &SYSDATE";
            FOOTNOTE1 'CLM.PROD.SAS - PAGER 4142';
            """))
          .extracting(Statements.Statement::getWordTexts)
          .containsExactly(
            List.of("TITLE1", "\"CASCADE MUTUAL - CLAIMS; RESERVES - &SYSDATE\""),
            List.of("FOOTNOTE1", "'CLM.PROD.SAS - PAGER 4142'"));
    }

    /**
     * A statement is anchored at the line it begins on and every word carries its own, which for a
     * query written over ten lines are not the same line.
     */
    @Test
    void readsAStatementWrittenOverSeveralLines() {
        List<Statements.Statement> statements = Statements.in(
          """
            INPUT @001 CLAIMNO  $CHAR10.
                  @011 POLICYNO $CHAR12.
                  @023 CLMNAME  $CHAR30.;
            """);

        assertThat(statements).singleElement()
          .satisfies(input -> {
              assertThat(input.getWordTexts()).containsExactly(
                "INPUT", "@001", "CLAIMNO", "$CHAR10.", "@011", "POLICYNO", "$CHAR12.",
                "@023", "CLMNAME", "$CHAR30.");
              assertThat(input.getLine()).isEqualTo(1);
              assertThat(input.getWords().get(8).getLine()).isEqualTo(3);
          });
    }

    /**
     * A member that ends without a semicolon still reads. What is written is a statement; whether SAS
     * would have run it is the run's business.
     */
    @Test
    void readsALastStatementNobodyTerminated() {
        assertThat(Statements.in("%MEND CLMTITL")).singleElement()
          .satisfies(statement -> assertThat(statement.getKeyword()).isEqualTo("%MEND"));
    }

}
