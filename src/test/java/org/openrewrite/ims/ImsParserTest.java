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
package org.openrewrite.ims;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.ParseExceptionResult;
import org.openrewrite.Parser;
import org.openrewrite.SourceFile;
import org.openrewrite.ims.marker.SequenceArea;
import org.openrewrite.ims.tree.Ims;
import org.openrewrite.tree.ParseError;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Printing back exactly, over the layouts a test fixture cannot express.
 * <p>
 * These parse directly rather than through {@code Assertions.ims}, because the test framework trims
 * the common indentation off a source block — which for a gen member would move the name field, the
 * operand field and column 72, and so change what is being tested.
 */
class ImsParserTest {

    @Test
    void aStatementWrittenOverThreeLines() {
        String source =
          "         DBD   NAME=CLMDBD01,ACCESS=(HDAM,VSAM)\n" +
          "         SEGM  NAME=CLMPLNK," + blanks(43) + "X\n" +
          "               PARENT=((CLMROOT),(POLROOT,PHYSICAL,CLMDBD02))," + blanks(9) + "X\n" +
          "               BYTES=12,POINTER=(LPARNT),RULES=(LLL,LAST)\n" +
          "         DBDGEN\n";

        Ims.CompilationUnit cu = parse(source);
        assertThat(cu.printAll()).isEqualTo(source);
        assertThat(cu.getStatements()).hasSize(3);

        Ims.MacroStatement segm = (Ims.MacroStatement) cu.getStatements().get(1);
        assertThat(segm.getOperation().getText()).isEqualTo("SEGM");
        assertThat(segm.getParameter("PARENT")).isNotNull();
        assertThat(segm.getParameter("BYTES")).isNotNull();
        assertThat(segm.getParameter("RULES")).isNotNull();
    }

    /**
     * The whole trick, and the way a DBD defect hides. The statement below prints back byte for byte
     * either way; only what it says changes.
     */
    @Test
    void aContinuationOneColumnShortIsNotAContinuation() {
        String source =
          "         DBD   NAME=CLMDBD01\n" +
          "         SEGM  NAME=CLMPLNK," + blanks(42) + "X\n" +
          "               PARENT=CLMROOT\n";

        Ims.CompilationUnit cu = parse(source);
        assertThat(cu.printAll()).isEqualTo(source);
        assertThat(cu.getStatements()).hasSize(3);
        assertThat(((Ims.MacroStatement) cu.getStatements().get(2)).getOperation().getText())
          .isEqualTo("PARENT=CLMROOT");
    }

    @Test
    void aNameFieldLabelsADataSetGroup() {
        String source =
          "       DBD     NAME=DBPAUTP0,ACCESS=(HIDAM,VSAM)\n" +
          "DSG001 DATASET DD1=DDPAUTP0,SIZE=(4096),SCAN=3\n" +
          "       SEGM    NAME=PAUTSUM0,PARENT=0,BYTES=100\n";

        Ims.CompilationUnit cu = parse(source);
        assertThat(cu.printAll()).isEqualTo(source);
        assertThat(((Ims.MacroStatement) cu.getStatements().get(1)).getSimpleName()).isEqualTo("DSG001");
        assertThat(((Ims.MacroStatement) cu.getStatements().get(2)).getSimpleName()).isEmpty();
    }

    @Test
    void aQuotedOperandHoldingBlanks() {
        String source =
          "      DBD   NAME=CUSTOMER\n" +
          "      DFSMARSH INTERNALTYPECONVERTER=CHAR," + blanks(29) + "C\n" +
          "               PATTERN='yyyy-MM-dd HH:mm:ss.SSS'\n";

        Ims.CompilationUnit cu = parse(source);
        assertThat(cu.printAll()).isEqualTo(source);
        Ims.MacroStatement marsh = (Ims.MacroStatement) cu.getStatements().get(1);
        assertThat(marsh.getParameter("PATTERN")).isNotNull();
        assertThat(marsh.getParameter("PATTERN").getValueText())
          .isEqualTo("'yyyy-MM-dd HH:mm:ss.SSS'");
    }

    @Test
    void aSequenceAreaIsKeptAndPrintedBack() {
        String source =
          "         DBD   NAME=CLMDBD01" + blanks(44) + "CLMDBD01\n" +
          "         SEGM  NAME=CLMROOT" + blanks(45) + "CLMDBD02\n";

        Ims.CompilationUnit cu = parse(source);
        assertThat(cu.printAll()).isEqualTo(source);
        assertThat(sequenceAreasIn(cu)).containsExactly("CLMDBD01", "CLMDBD02");
    }

    @Test
    void blankLinesBetweenStatements() {
        String source =
          "      DBD   NAME=CUSTOMER\n" +
          "\n" +
          "      DATASET  DD1=CUSTOMER\n" +
          "\n" +
          "      END\n";

        Ims.CompilationUnit cu = parse(source);
        assertThat(cu.printAll()).isEqualTo(source);
        assertThat(cu.getStatements().stream().filter(Ims.MacroStatement.class::isInstance)).hasSize(3);
    }

    @Test
    void windowsLineEndings() {
        String source =
          "         DBD   NAME=CLMDBD01,ACCESS=(HDAM,VSAM)," + blanks(23) + "X\r\n" +
          "               RMNAME=(DFSHDC40,5,500,824)\r\n" +
          "         END\r\n";

        Ims.CompilationUnit cu = parse(source);
        assertThat(cu.printAll()).isEqualTo(source);
        assertThat(cu.getStatements()).hasSize(2);
    }

    @Test
    void noTrailingNewline() {
        String source =
          "         DBD   NAME=CLMDBD01\n" +
          "         END";

        Ims.CompilationUnit cu = parse(source);
        assertThat(cu.printAll()).isEqualTo(source);
        assertThat(cu.getStatements()).hasSize(2);
    }

    /**
     * A member the reader took but cannot read is a parse error, not a plausible tree.
     */
    @Test
    void aMemberThatGensNothing() {
        Parser.Input input = input("CLMU030.dbd",
          "         COPY  CLMREGS\n" +
          "CLMU030  CLMSAVE 12,SAVE=U30SAVE,ID=CLMU030\n");
        List<SourceFile> parsed = ImsParser.builder().build()
          .parseInputs(singletonList(input), null, new InMemoryExecutionContext())
          .collect(Collectors.toList());

        assertThat(parsed).hasSize(1);
        assertThat(parsed.get(0)).isInstanceOf(ParseError.class);
        ParseExceptionResult failure = parsed.get(0).getMarkers()
          .findFirst(ParseExceptionResult.class).orElseThrow(AssertionError::new);
        assertThat(failure.getExceptionType()).isEqualTo("WrongLanguageException");
        assertThat(failure.getMessage())
          .contains("CLMU030.dbd is not an IMS gen member: it opens with no gen macro.");
    }

    /**
     * Bank of Z keeps its DBDs beside its PSBs, both as {@code .asm}, so what a member gens is what
     * says which reader takes it. The assembler reader of a later item asks the same question.
     */
    @Test
    void anAssemblerMemberIsClaimedOnlyWhenItGens(@TempDir Path dir) throws IOException {
        Path database = write(dir, "CUSTOMER.asm",
          "***********************\n" +
          "* Friendly Bank - CUSTOMER DBD\n" +
          "***********************\n" +
          "      DBD   NAME=CUSTOMER,ACCESS=(HDAM,OSAM)\n");
        Path programSpecification = write(dir, "IBTRAN.asm",
          "      PCB   TYPE=DB,DBDNAME=CUSTOMER,PROCOPT=A\n");
        Path program = write(dir, "CLMU030.asm",
          "         COPY  CLMREGS\n");

        assertThat(ImsParser.builder().build().accept(database)).isTrue();
        assertThat(ImsParser.builder().build().accept(programSpecification)).isTrue();
        assertThat(ImsParser.builder().build().accept(program)).isFalse();
    }

    /**
     * A PSB is written the other way up from a DBD: the PCBs first, each with what belongs to it, and
     * the {@code PSBGEN} that names them all last.
     */
    @Test
    void aPsbWithAContinuedPcb() {
        String source =
          "         PCB   TYPE=DB,DBDNAME=CLMDBD01,PROCSEQ=CLMDBX01,PROCOPT=G," + blanks(4) + "X\n" +
          "               KEYLEN=14\n" +
          "         SENSEG NAME=CLMROOT,PARENT=0,PROCOPT=G\n" +
          "         PSBGEN LANG=COBOL,PSBNAME=CLMPSB03,IOASIZE=200\n" +
          "         END\n";

        Ims.CompilationUnit cu = parse("CLMPSB03.psb", source);
        assertThat(cu.printAll()).isEqualTo(source);
        assertThat(cu.getStatements()).hasSize(4);
        assertThat(((Ims.MacroStatement) cu.getStatements().get(0)).getParameter("KEYLEN")).isNotNull();
    }

    @Test
    void aStage1Deck() {
        String source =
          "         APPLCTN PSB=CLMPSB02,PGMTYPE=(TP,,2),SCHDTYP=PARALLEL\n" +
          "         TRANSACT CODE=CLMINQ,MODE=SNGL,SPA=(150,),EDIT=(,ULC)," + blanks(8) + "X\n" +
          "               MSGTYPE=(SNGLSEG,RESPONSE,1)\n" +
          "         DATABASE DBD=CLMDBD01,ACCESS=UP\n";

        Ims.CompilationUnit cu = parse("CLMGEN01.gen", source);
        assertThat(cu.printAll()).isEqualTo(source);
        assertThat(cu.getStatements()).hasSize(3);
    }

    private static Path write(Path dir, String name, String source) throws IOException {
        Path member = dir.resolve(name);
        Files.write(member, source.getBytes(StandardCharsets.UTF_8));
        return member;
    }

    private static List<String> sequenceAreasIn(Ims.CompilationUnit cu) {
        List<String> areas = new ArrayList<>();
        new ImsIsoVisitor<Integer>() {
            @Override
            public Ims.Word visitWord(Ims.Word word, Integer p) {
                word.getMarkers().findFirst(SequenceArea.class)
                  .ifPresent(area -> areas.add(area.getText()));
                return super.visitWord(word, p);
            }
        }.visit(cu, 0);
        return areas;
    }

    private static Ims.CompilationUnit parse(String source) {
        return parse("CLMDBD01.dbd", source);
    }

    private static Ims.CompilationUnit parse(String name, String source) {
        List<SourceFile> parsed = ImsParser.builder().build()
          .parseInputs(singletonList(input(name, source)), null, new InMemoryExecutionContext())
          .collect(Collectors.toList());
        assertThat(parsed).hasSize(1);
        assertThat(parsed.get(0)).isInstanceOf(Ims.CompilationUnit.class);
        return (Ims.CompilationUnit) parsed.get(0);
    }

    /**
     * A file rather than a string, because a synthetic input is taken on trust: what a member gens is
     * only asked of one that came off disk.
     */
    private static Parser.Input input(String name, String source) {
        return new Parser.Input(Paths.get(name),
          () -> new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8)));
    }

    private static String blanks(int n) {
        StringBuilder blanks = new StringBuilder();
        for (int i = 0; i < n; i++) {
            blanks.append(' ');
        }
        return blanks.toString();
    }
}
