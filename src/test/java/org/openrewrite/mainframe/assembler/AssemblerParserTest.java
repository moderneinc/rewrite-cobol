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
package org.openrewrite.mainframe.assembler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openrewrite.DocumentExample;
import org.openrewrite.mainframe.assembler.marker.SequenceArea;
import org.openrewrite.mainframe.assembler.tree.Assembler;
import org.openrewrite.test.RewriteTest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.openrewrite.mainframe.assembler.Assertions.assembler;

class AssemblerParserTest implements RewriteTest {

    @DocumentExample
    @Test
    void readsALabelAnOperationAndItsOperands() {
        rewriteRun(
          assembler(
            """
              CLMU030  CSECT
                       L     R2,0(,R1)           ADDRESS OF THE CLAIM NUMBER
                       CLC   0(3,R2),=C'CLM'     PREFIX
                       BNE   U30FMT
                       END   CLMU030
              """,
            spec -> spec.afterRecipe(cu -> {
                List<Assembler.Instruction> instructions = instructions(cu);
                assertThat(instructions).extracting(Assembler.Instruction::getSimpleName,
                    i -> i.getOperation().getText(), Assembler.Instruction::getOperandTexts)
                  .containsExactly(
                    tuple("CLMU030", "CSECT", List.of()),
                    tuple("", "L", List.of("R2", "0(,R1)")),
                    tuple("", "CLC", List.of("0(3,R2)", "=C'CLM'")),
                    tuple("", "BNE", List.of("U30FMT")),
                    tuple("", "END", List.of("CLMU030")));
            })
          )
        );
    }

    @Test
    void keepsTheRemarksAfterTheOperandField() {
        rewriteRun(
          assembler(
            "         LA    R3,3(,R2)           FIRST SERIAL DIGIT\n",
            spec -> spec.afterRecipe(cu -> {
                Assembler.Instruction instruction = instructions(cu).get(0);
                assertThat(instruction.getOperandTexts()).containsExactly("R3", "3(,R2)");
                assertThat(instruction.getOperands()).last()
                  .isInstanceOfSatisfying(Assembler.Word.class,
                    word -> assertThat(word.getText()).isEqualTo("FIRST SERIAL DIGIT"));
            })
          )
        );
    }

    @Test
    void joinsTheOperandsOfAContinuedStatement() {
        rewriteRun(
          assembler(
            """
              A10CRD   DCB   DDNAME=PURGCARD,DSORG=PS,MACRF=(GM),RECFM=FB,           X
                             LRECL=80,EODAD=A10NOCD
              """,
            spec -> spec.afterRecipe(cu -> {
                List<Assembler.Instruction> instructions = instructions(cu);
                assertThat(instructions).singleElement()
                  .extracting(Assembler.Instruction::getOperandTexts)
                  .isEqualTo(List.of("DDNAME=PURGCARD", "DSORG=PS", "MACRF=(GM)", "RECFM=FB",
                    "LRECL=80", "EODAD=A10NOCD"));
                assertThat(instructions.get(0).getParameterValue("DDNAME")).isEqualTo("PURGCARD");
                assertThat(instructions.get(0).getParameterValue("MACRF")).isEqualTo("(GM)");
            })
          )
        );
    }

    @Test
    void carriesOneOperandOverTheLineBreakItWasSplitBy() {
        rewriteRun(
          assembler(
            "         UNPK  DTEMP((mdsdigits*2)-1),DTEMP+L'DTEMP-mdsdigits+1(mdsdigi+\n" +
            "               ts)\n",
            spec -> spec.afterRecipe(cu -> assertThat(instructions(cu)).singleElement()
              .extracting(Assembler.Instruction::getOperandTexts)
              .isEqualTo(List.of("DTEMP((mdsdigits*2)-1)", "DTEMP+L'DTEMP-mdsdigits+1(mdsdigits)")))
          )
        );
    }

    /**
     * A continuation line that leaves column 16 blank is carrying on the remarks and not the operands.
     * Read as operands, the text lands nowhere and prints back out of the white space in front of the
     * next word.
     */
    @Test
    void carriesRemarksOverALineBreakToo() {
        rewriteRun(
          assembler(
            "dayof001_s equ  60*60*24*100     create number of hundreths in a day   +\n" +
            "                ss mm hh .01\n",
            spec -> spec.afterRecipe(cu -> {
                Assembler.Instruction instruction = instructions(cu).get(0);
                assertThat(instruction.getOperandTexts()).containsExactly("60*60*24*100");
                assertThat(instruction.getOperands()).last()
                  .isInstanceOfSatisfying(Assembler.Word.class,
                    word -> assertThat(word.getText()).isEqualTo("ss mm hh .01"));
            })
          )
        );
    }

    /**
     * Columns 1 to 15 of a continuation line are ignored by the assembler, which is what happens to a
     * statement whose remarks ran past column 71 by accident.
     */
    @Test
    void keepsWhatAContinuationLineWritesBeforeColumn16() {
        rewriteRun(
          assembler(
            "         MVI   12(13),X'FF'            *ML purpose of this? - can we remove it?\n" +
            "         SR    15,15                   ZERO R15 BEFORE RETURN\n",
            spec -> spec.afterRecipe(cu -> assertThat(instructions(cu)).singleElement()
              .extracting(Assembler.Instruction::getOperandTexts)
              .isEqualTo(List.of("12(13)", "X'FF'", "15", "15")))
          )
        );
    }

    @Test
    void doesNotSplitAtACommaInsideQuotesOrParentheses() {
        rewriteRun(
          assembler(
            """
              A10LOOP  CALL  ASMTDLI,(A10GHN,(R11),A10ROOT,A10SSA),VL
              A10WGET  WTO   'CLMA010 - DL/I STATUS .. ON GHN, PURGE ENDED',         X
                             ROUTCDE=(11),DESC=(6),MF=L
              """,
            spec -> spec.afterRecipe(cu -> assertThat(instructions(cu))
              .extracting(Assembler.Instruction::getOperandTexts)
              .containsExactly(
                List.of("ASMTDLI", "(A10GHN,(R11),A10ROOT,A10SSA)", "VL"),
                List.of("'CLMA010 - DL/I STATUS .. ON GHN, PURGE ENDED'", "ROUTCDE=(11)",
                  "DESC=(6)", "MF=L")))
          )
        );
    }

    /**
     * {@code L'SYMBOL} is the length attribute of a symbol and not the start of a literal, so the
     * apostrophe in it does not open a quote. Read as one, the operand field runs to the end of the
     * line and swallows the remarks.
     */
    @Test
    void readsALengthAttributeRatherThanAQuote() {
        rewriteRun(
          assembler(
            "         oi    alias_found,l'alias_found  set the flag\n",
            spec -> spec.afterRecipe(cu -> assertThat(instructions(cu)).singleElement()
              .extracting(Assembler.Instruction::getOperandTexts)
              .isEqualTo(List.of("alias_found", "l'alias_found")))
          )
        );
    }

    /**
     * A logical expression in parentheses may hold blanks, which is the one place the operand field
     * does not end at the first of them.
     */
    @Test
    void readsAConditionalAssemblyExpressionWhole() {
        rewriteRun(
          assembler(
            "         AIF   ('&BASE' NE '').BASEOK\n",
            spec -> spec.afterRecipe(cu -> assertThat(instructions(cu)).singleElement()
              .extracting(Assembler.Instruction::getOperandTexts)
              .isEqualTo(List.of("('&BASE' NE '').BASEOK")))
          )
        );
    }

    @Test
    void readsACommentStatementAndTheLinesItCarriesOnTo() {
        rewriteRun(
          assembler(
            """
              *---------------------------------------------------------------------
              *UMPARM  SNAP  SDATA=(CB,DM,IO),                                       X
                             PDATA=(PSW,REGS,SA,JPA,SUBTASKS),SUBPLST=SPLADDR,MF=L
                       LTORG
              """,
            spec -> spec.afterRecipe(cu -> {
                assertThat(cu.getStatements()).hasSize(3);
                // The second comment carries its continuation line, so nothing reads PDATA= as an
                // operation of a statement of its own.
                assertThat(cu.getStatements().get(1)).isInstanceOf(Assembler.Comment.class);
                assertThat(instructions(cu)).singleElement()
                  .extracting(i -> i.getOperation().getText()).isEqualTo("LTORG");
            })
          )
        );
    }

    @Test
    void readsAMacroCommentAsAComment() {
        rewriteRun(
          assembler(
            "         MACRO\n" +
            "&NAME    CLMSAVE &BASE,&SAVE=,&ID=\n" +
            ".*  ENTRY LINKAGE\n" +
            ".BASEOK  ANOP\n" +
            "         MEND\n",
            spec -> spec.afterRecipe(cu -> {
                assertThat(cu.getStatements().get(2)).isInstanceOf(Assembler.Comment.class);
                assertThat(instructions(cu)).extracting(Assembler.Instruction::getSimpleName,
                    i -> i.getOperation().getText(), Assembler.Instruction::getOperandTexts)
                  .containsExactly(
                    tuple("", "MACRO", List.of()),
                    tuple("&NAME", "CLMSAVE", List.of("&BASE", "&SAVE=", "&ID=")),
                    tuple(".BASEOK", "ANOP", List.of()),
                    tuple("", "MEND", List.of()));
            })
          )
        );
    }

    @Test
    void keepsColumns73To80OutOfTheStatement() {
        rewriteRun(
          assembler(
            "MVSWAIT  START 0                                                        00010000\n",
            spec -> spec.afterRecipe(cu -> {
                Assembler.Instruction instruction = instructions(cu).get(0);
                assertThat(instruction.getOperandTexts()).containsExactly("0");
                assertThat(instruction.getOperands()).singleElement()
                  .extracting(o -> o.getMarkers().findFirst(SequenceArea.class).orElseThrow().getText())
                  .isEqualTo("00010000");
            })
          )
        );
    }

    /**
     * {@code ICTL} moves the begin, end and continue columns, so what carries a statement onto the
     * next line is column 61 here and not column 72.
     */
    @Test
    void honoursTheColumnsIctlMovesThemTo() {
        rewriteRun(
          assembler(
            "         ICTL  1,60,16\n" +
            "         DCB   DDNAME=PURGCARD,DSORG=PS,                    X\n" +
            "               LRECL=80\n",
            spec -> spec.afterRecipe(cu -> assertThat(instructions(cu)).last()
              .extracting(Assembler.Instruction::getOperandTexts)
              .isEqualTo(List.of("DDNAME=PURGCARD", "DSORG=PS", "LRECL=80")))
          )
        );
    }

    /**
     * Bank of Z keeps its DBDs and PSBs as {@code .asm}, so an extension is not enough to say which
     * reader takes a member: what it gens is, and that is the IMS reader's rule to answer.
     */
    @Test
    void declinesAnImsGenMemberKeptAsAssembler(@TempDir Path dir) throws IOException {
        Path database = Files.write(dir.resolve("CUSTOMER.asm"),
          "      DBD   NAME=CUSTOMER,ACCESS=(HDAM,OSAM)\n".getBytes(StandardCharsets.UTF_8));
        Path program = Files.write(dir.resolve("CLMU030.asm"),
          "CLMU030  CSECT\n".getBytes(StandardCharsets.UTF_8));

        assertThat(AssemblerParser.builder().build().accept(database)).isFalse();
        assertThat(AssemblerParser.builder().build().accept(program)).isTrue();
        assertThat(AssemblerParser.builder().build().accept(Paths.get("CLMDBD01.dbd"))).isFalse();
        assertThat(AssemblerParser.builder().build().accept(Paths.get("CLMSAVE.mac"))).isTrue();
    }

    private static List<Assembler.Instruction> instructions(Assembler.CompilationUnit cu) {
        return cu.getStatements().stream()
          .filter(Assembler.Instruction.class::isInstance)
          .map(Assembler.Instruction.class::cast)
          .collect(Collectors.toList());
    }
}
