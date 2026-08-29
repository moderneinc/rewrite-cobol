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
package org.openrewrite.mainframe.ims.trait;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Parser;
import org.openrewrite.SourceFile;
import org.openrewrite.mainframe.ims.ImsParser;
import org.openrewrite.mainframe.ims.tree.Ims;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.openrewrite.mainframe.ims.trait.Psb.ProgramKind.BATCH;
import static org.openrewrite.mainframe.ims.trait.Psb.ProgramKind.MESSAGE_DRIVEN;

/**
 * What a PSB means, and above all which PCB a program's nth mask is.
 * <p>
 * The mask order is the whole point of a PSB reader: a DL/I call names a position and nothing else,
 * so until the position is turned into a database the segment a call reached says nothing. The cases
 * are {@code INTERLINKS.md} section 6.1, one per way of writing an IMS program.
 * <p>
 * These parse directly rather than through {@code Assertions.ims}: the operand field and column 72
 * are where the meaning is, and the test framework trims the common indentation off a source block.
 */
class PsbTest {

    /**
     * A conversational message processing program's PSB. The I/O PCB is coded nowhere and arrives
     * first all the same, so the alternate PCB written first here is the program's second mask.
     */
    private static final String CONVERSATIONAL =
      "*  CLMPSB02 - CONVERSATIONAL CLAIM INQUIRY, PROGRAM CLMI030\n" +
      "         PCB   TYPE=TP,NAME=CLMLTRM,MODIFY=YES\n" +
      "*\n" +
      "         PCB   TYPE=DB,DBDNAME=CLMDBD01,PROCOPT=G,KEYLEN=14\n" +
      "         SENSEG NAME=CLMROOT,PARENT=0,PROCOPT=G\n" +
      "         SENFLD NAME=CLMKEY,START=1\n" +
      "         SENFLD NAME=CLMPOL,START=11\n" +
      "         SENFLD NAME=CLMADJR,START=43\n" +
      "         SENSEG NAME=CLMDETL,PARENT=CLMROOT,PROCOPT=G\n" +
      "*\n" +
      "         PCB   TYPE=DB,DBDNAME=CLMDBD03,PROCOPT=G,KEYLEN=4\n" +
      "         SENSEG NAME=TYPROOT,PARENT=0,PROCOPT=G\n" +
      "*\n" +
      "         PSBGEN LANG=COBOL,PSBNAME=CLMPSB02,IOASIZE=600,SSASIZE=200\n" +
      "         END\n";

    /**
     * A BMP's PSB. {@code CMPAT=YES} is what gives it the I/O PCB its {@code CHKP} and {@code XRST}
     * need, and it is the only thing here that says so.
     */
    private static final String BATCH_MESSAGE_PROGRAM =
      "         PCB   TYPE=GSAM,DBDNAME=CLMDBG01,PROCOPT=GS\n" +
      "         PCB   TYPE=DB,DBDNAME=CLMDBD01,PROCOPT=AP,KEYLEN=14\n" +
      "         SENSEG NAME=CLMROOT,PARENT=0,PROCOPT=A\n" +
      "         PSBGEN LANG=COBOL,PSBNAME=CLMPSB04,CMPAT=YES,IOASIZE=200\n" +
      "         END\n";

    @Test
    void thePsbAndItsPcbs() {
        Psb psb = psb(CONVERSATIONAL);

        assertThat(psb.getName()).isEqualTo("CLMPSB02");
        assertThat(psb.getLanguage()).isEqualTo("COBOL");
        assertThat(psb.isCompatible()).isFalse();
        assertThat(psb.getIoAreaSize()).isEqualTo(600);
        assertThat(psb.getSsaSize()).isEqualTo(200);
        assertThat(psb.getLine()).isEqualTo(14);

        assertThat(psb.getPcbs()).extracting(Pcb::getPosition, Pcb::getType, Pcb::getDatabaseName,
            Pcb::getProcessingOptions, Pcb::getKeyLength, Pcb::getLine)
          .containsExactly(
            tuple(1, "TP", null, null, null, 2),
            tuple(2, "DB", "CLMDBD01", "G", 14, 4),
            tuple(3, "DB", "CLMDBD03", "G", 4, 11));

        Pcb alternate = psb.getPcbs().get(0);
        assertThat(alternate.isMessage()).isTrue();
        assertThat(alternate.isDatabase()).isFalse();
        assertThat(alternate.getDestination()).isEqualTo("CLMLTRM");
        assertThat(alternate.isModifiable()).isTrue();
    }

    @Test
    void theSegmentsAndFieldsAPcbIsSensitiveTo() {
        Pcb claims = psb(CONVERSATIONAL).getPcbs().get(1);

        assertThat(claims.getSensitiveSegments())
          .extracting(SensitiveSegment::getName, SensitiveSegment::getParentName,
            SensitiveSegment::getProcessingOptions, SensitiveSegment::getLine)
          .containsExactly(
            tuple("CLMROOT", null, "G", 5),
            tuple("CLMDETL", "CLMROOT", "G", 9));

        // The I/O area a field-sensitive PCB builds is not the segment: it is these fields, in this
        // order, and it is shorter.
        SensitiveSegment root = claims.getSensitiveSegments().get(0);
        assertThat(root.isRoot()).isTrue();
        assertThat(root.getSensitiveFields())
          .extracting(SensitiveField::getName, SensitiveField::getStart, SensitiveField::getLine)
          .containsExactly(
            tuple("CLMKEY", 1, 6),
            tuple("CLMPOL", 11, 7),
            tuple("CLMADJR", 43, 8));

        // A SENFLD belongs to the SENSEG above it and to no other, which is read from position.
        assertThat(root.getSensitiveFields().get(2).getSegment()).isNotNull()
          .extracting(SensitiveSegment::getName).isEqualTo("CLMROOT");
        assertThat(claims.getSensitiveSegments().get(1).getSensitiveFields()).isEmpty();
    }

    /**
     * Section 6.1, the message driven case: {@code CLMI030}'s four masks are the I/O PCB, the
     * alternate, {@code CLMDBD01} and {@code CLMDBD03}, and only the first of those is written here.
     */
    @Test
    void aMessageDrivenProgramIsHandedTheIoPcbFirst() {
        Psb psb = psb(CONVERSATIONAL);

        assertThat(psb.receivesIoPcb(MESSAGE_DRIVEN)).isTrue();
        assertThat(psb.getPcbAtMask(1, MESSAGE_DRIVEN)).isNull();
        assertThat(psb.getPcbAtMask(2, MESSAGE_DRIVEN)).isNotNull()
          .extracting(Pcb::getDestination).isEqualTo("CLMLTRM");
        assertThat(psb.getPcbAtMask(3, MESSAGE_DRIVEN)).isNotNull()
          .extracting(Pcb::getDatabaseName).isEqualTo("CLMDBD01");
        assertThat(psb.getPcbAtMask(4, MESSAGE_DRIVEN)).isNotNull()
          .extracting(Pcb::getDatabaseName).isEqualTo("CLMDBD03");
        assertThat(psb.getPcbAtMask(5, MESSAGE_DRIVEN)).isNull();
    }

    /**
     * The same PSB read as if a batch program ran under it would put every database one mask early,
     * which is the defect this rule exists to prevent.
     */
    @Test
    void theProgramKindMovesEveryMask() {
        assertThat(psb(CONVERSATIONAL).getPcbAtMask(3, BATCH)).isNotNull()
          .extracting(Pcb::getDatabaseName).isEqualTo("CLMDBD03");
    }

    @Test
    void aBmpIsHandedTheIoPcbBecauseOfCmpat() {
        Psb psb = psb(BATCH_MESSAGE_PROGRAM);

        assertThat(psb.isCompatible()).isTrue();
        assertThat(psb.receivesIoPcb(BATCH)).isTrue();
        assertThat(psb.getPcbAtMask(1, BATCH)).isNull();
        assertThat(psb.getPcbAtMask(2, BATCH)).isNotNull()
          .extracting(Pcb::getDatabaseName).isEqualTo("CLMDBG01");
        assertThat(psb.getPcbAtMask(3, BATCH)).isNotNull()
          .extracting(Pcb::getDatabaseName).isEqualTo("CLMDBD01");

        // A GSAM PCB has a database and is not one a command level program numbers, and it has no
        // sensitive segments because a GSAM database has no segments.
        Pcb gsam = psb.getPcbs().get(0);
        assertThat(gsam.isSequential()).isTrue();
        assertThat(gsam.isDatabase()).isFalse();
        assertThat(gsam.getSensitiveSegments()).isEmpty();
        assertThat(psb.getDatabasePcbs()).singleElement()
          .extracting(Pcb::getDatabaseName).isEqualTo("CLMDBD01");
    }

    @Test
    void aDliBatchProgramWithoutCmpatIsHandedItsDatabasePcbFirst() {
        Psb psb = psb(
          "         PCB   TYPE=DB,DBDNAME=CLMDBD01,PROCOPT=A,KEYLEN=14\n" +
          "         SENSEG NAME=CLMROOT,PARENT=0,PROCOPT=A\n" +
          "         PSBGEN LANG=COBOL,PSBNAME=CLMPSB01,IOASIZE=200\n" +
          "         END\n");

        assertThat(psb.receivesIoPcb(BATCH)).isFalse();
        assertThat(psb.getPcbAtMask(1, BATCH)).isNotNull()
          .extracting(Pcb::getDatabaseName).isEqualTo("CLMDBD01");
        assertThat(psb.getPcbAtMask(2, BATCH)).isNull();
    }

    /**
     * Section 6.1's first exception: the AIB interface puts a name in {@code AIBRSNM1} and counts no
     * positions at all, so the PSB has to give the PCB one.
     */
    @Test
    void aPcbReachedByName() {
        Psb psb = psb(
          "         PCB   TYPE=DB,DBDNAME=CLMDBD01,PROCOPT=A,KEYLEN=14," + blanks(11) + "X\n" +
          "               PCBNAME=CLMPCB1\n" +
          "         SENSEG NAME=CLMROOT,PARENT=0,PROCOPT=A\n" +
          "         PSBGEN LANG=COBOL,PSBNAME=CLMPSB05,IOASIZE=200\n" +
          "         END\n");

        assertThat(psb.getPcb("CLMPCB1")).isNotNull()
          .extracting(Pcb::getDatabaseName).isEqualTo("CLMDBD01");
        assertThat(psb.getPcb("CLMPCB9")).isNull();
    }

    /**
     * Section 6.1's second exception: a command level program writes {@code PCB(n)}, and n counts the
     * database PCBs rather than the masks.
     */
    @Test
    void aPcbReachedByItsNumberAmongTheDatabasePcbs() {
        Psb psb = psb(
          "         PCB   TYPE=DB,DBDNAME=CLMDBD01,PROCOPT=G,KEYLEN=14\n" +
          "         SENSEG NAME=CLMROOT,PARENT=0,PROCOPT=G\n" +
          "         PCB   TYPE=DB,DBDNAME=CLMDBD03,PROCOPT=G,KEYLEN=4\n" +
          "         SENSEG NAME=TYPROOT,PARENT=0,PROCOPT=G\n" +
          "         PSBGEN LANG=COBOL,PSBNAME=CLMPSB06,IOASIZE=200\n" +
          "         END\n");

        assertThat(psb.getDatabasePcb(1)).isNotNull()
          .extracting(Pcb::getDatabaseName).isEqualTo("CLMDBD01");
        assertThat(psb.getDatabasePcb(2)).isNotNull()
          .extracting(Pcb::getDatabaseName).isEqualTo("CLMDBD03");
        assertThat(psb.getDatabasePcb(3)).isNull();
    }

    /**
     * A {@code PROCSEQ} is a second database the PCB names: the index the roots are walked in.
     */
    @Test
    void aPcbProcessedThroughASecondaryIndex() {
        Psb psb = psb(
          "         PCB   TYPE=DB,DBDNAME=CLMDBD01,PROCSEQ=CLMDBX01,PROCOPT=G," + blanks(4) + "X\n" +
          "               KEYLEN=14\n" +
          "         SENSEG NAME=CLMROOT,PARENT=0,PROCOPT=G\n" +
          "         PSBGEN LANG=COBOL,PSBNAME=CLMPSB03,IOASIZE=200\n" +
          "         END\n");

        Pcb pcb = psb.getPcbs().get(0);
        assertThat(pcb.getDatabaseName()).isEqualTo("CLMDBD01");
        assertThat(pcb.getProcessingSequence()).isEqualTo("CLMDBX01");
        assertThat(pcb.getKeyLength()).isEqualTo(14);
    }

    /**
     * CardDemo labels the macro in column 1 instead of writing {@code PCBNAME=}, and a program that
     * reaches a PCB by name reaches that one.
     */
    @Test
    void aPcbNamedByTheLabelInColumnOne() {
        Psb psb = psb(
          "PAUTBPCB PCB   TYPE=DB,DBDNAME=DBPAUTP0,PROCOPT=AP,KEYLEN=14\n" +
          "         SENSEG  NAME=PAUTSUM0,PARENT=0\n" +
          "         PSBGEN  LANG=COBOL,PSBNAME=PSBPAUTB,CMPAT=YES\n" +
          "         END\n");

        assertThat(psb.getPcbs()).singleElement().extracting(Pcb::getName).isEqualTo("PAUTBPCB");
        assertThat(psb.getPcb("PAUTBPCB")).isNotNull();
    }

    /**
     * Every PCB knows the PSB that closes it, which is the only place the PSB's name is written.
     */
    @Test
    void aPcbKnowsItsPsb() {
        assertThat(psb(CONVERSATIONAL).getPcbs().get(2).getPsb()).isNotNull()
          .extracting(Psb::getName).isEqualTo("CLMPSB02");
    }

    private static Psb psb(String source) {
        List<SourceFile> parsed = ImsParser.builder().build()
          .parseInputs(singletonList(input(source)), null, new InMemoryExecutionContext())
          .collect(Collectors.toList());
        assertThat(parsed).hasSize(1);
        assertThat(parsed.get(0)).isInstanceOf(Ims.CompilationUnit.class);
        assertThat(parsed.get(0).printAll()).isEqualTo(source);

        List<Psb> psbs = new Psb.Matcher().lower(parsed.get(0)).collect(Collectors.toList());
        assertThat(psbs).hasSize(1);
        return psbs.get(0);
    }

    private static Parser.Input input(String source) {
        return new Parser.Input(Paths.get("CLMPSB01.psb"),
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
