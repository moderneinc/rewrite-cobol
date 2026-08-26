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
package org.openrewrite.cobol.trait;

import org.junit.jupiter.api.Test;
import org.openrewrite.ExecutionContext;
import org.openrewrite.test.RewriteTest;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.openrewrite.cobol.Assertions.cobol;
import static org.openrewrite.test.RewriteTest.toRecipe;

class DliCallTest implements RewriteTest {

    private List<DliCall> parse(String source) {
        List<DliCall> found = new ArrayList<>();
        rewriteRun(
          spec -> spec.recipe(toRecipe(() -> new DliCall.Matcher().<ExecutionContext>asVisitor((call, ctx) -> {
              found.add(call);
              return call.getTree();
          }))),
          cobol(source)
        );
        return found;
    }

    @Test
    void resolvesFunctionCodeFromValueClause() {
        List<DliCall> calls = parse(
          """
            000000 IDENTIFICATION DIVISION.                                        \s
            000000 PROGRAM-ID. IMSPGM.                                             \s
            000000 DATA DIVISION.                                                  \s
            000000 WORKING-STORAGE SECTION.                                        \s
            000000 77 GU-FUNC PIC X(4) VALUE 'GU  '.                               \s
            000000 PROCEDURE DIVISION.                                             \s
            000000     ENTRY 'DLITCBL' USING IO-PCB DB-PCB.                        \s
            000000     CALL 'CBLTDLI' USING GU-FUNC DB-PCB IO-AREA.                \s
            000000     GOBACK.                                                     \s
            """
        );

        assertThat(calls).hasSize(1);
        DliCall gu = calls.get(0);
        assertThat(gu.getIface()).isEqualTo("CBLTDLI");
        // The literal is padded to four bytes in the source and trimmed here.
        assertThat(gu.getFunction()).isEqualTo("GU");
        assertThat(gu.getFunctionOperand()).isEqualTo("GU-FUNC");
        assertThat(gu.getPcb()).isEqualTo("DB-PCB");
        assertThat(gu.getIoArea()).isEqualTo("IO-AREA");
        assertThat(gu.getAccess()).isEqualTo(DliCall.Access.READ);
    }

    @Test
    void resolvesFunctionCodeMovedIntoAField() {
        List<DliCall> calls = parse(
          """
            000000 IDENTIFICATION DIVISION.                                        \s
            000000 PROGRAM-ID. IMSPGM.                                             \s
            000000 DATA DIVISION.                                                  \s
            000000 WORKING-STORAGE SECTION.                                        \s
            000000 77 DLI-FUNC PIC X(4).                                           \s
            000000 PROCEDURE DIVISION.                                             \s
            000000     MOVE 'ISRT' TO DLI-FUNC.                                    \s
            000000     CALL 'CBLTDLI' USING DLI-FUNC DB-PCB IO-AREA.               \s
            000000     GOBACK.                                                     \s
            """
        );

        assertThat(calls).hasSize(1);
        assertThat(calls.get(0).getFunction()).isEqualTo("ISRT");
        assertThat(calls.get(0).getAccess()).isEqualTo(DliCall.Access.CREATE);
    }

    @Test
    void leavesTheFunctionUnresolvedWhenSeveralLiteralsReachIt() {
        List<DliCall> calls = parse(
          """
            000000 IDENTIFICATION DIVISION.                                        \s
            000000 PROGRAM-ID. IMSPGM.                                             \s
            000000 DATA DIVISION.                                                  \s
            000000 WORKING-STORAGE SECTION.                                        \s
            000000 77 DLI-FUNC PIC X(4).                                           \s
            000000 PROCEDURE DIVISION.                                             \s
            000000     MOVE 'ISRT' TO DLI-FUNC.                                    \s
            000000     MOVE 'DLET' TO DLI-FUNC.                                    \s
            000000     CALL 'CBLTDLI' USING DLI-FUNC DB-PCB IO-AREA.               \s
            000000     GOBACK.                                                     \s
            """
        );

        assertThat(calls).hasSize(1);
        assertThat(calls.get(0).getFunction()).isNull();
        assertThat(calls.get(0).getAccess()).isEqualTo(DliCall.Access.UNKNOWN);
    }

    @Test
    void recoversPcbOrderFromTheEntryPoint() {
        List<DliCall> calls = parse(
          """
            000000 IDENTIFICATION DIVISION.                                        \s
            000000 PROGRAM-ID. IMSPGM.                                             \s
            000000 DATA DIVISION.                                                  \s
            000000 WORKING-STORAGE SECTION.                                        \s
            000000 77 GU-FUNC PIC X(4) VALUE 'GU  '.                               \s
            000000 PROCEDURE DIVISION.                                             \s
            000000     ENTRY 'DLITCBL' USING IO-PCB ACCT-DB-PCB HIST-DB-PCB.       \s
            000000     CALL 'CBLTDLI' USING GU-FUNC ACCT-DB-PCB IO-AREA.           \s
            000000     GOBACK.                                                     \s
            """
        );

        assertThat(calls).hasSize(1);
        ProgramEntry pcbs = calls.get(0).getEntry();
        assertThat(pcbs).isNotNull();
        assertThat(pcbs.getPcbNames()).containsExactly("IO-PCB", "ACCT-DB-PCB", "HIST-DB-PCB");
        assertThat(pcbs.indexOf("NOT-A-PCB")).isEqualTo(-1);
        assertThat(pcbs.isIoPcb("IO-PCB")).isTrue();
        assertThat(pcbs.isIoPcb("ACCT-DB-PCB")).isFalse();
        // The position is what a PSB listing has to be joined on to name the database.
        assertThat(calls.get(0).getPcbPosition()).isEqualTo(1);
    }

    @Test
    void distinguishesMessageCallsFromDatabaseCalls() {
        List<DliCall> calls = parse(
          """
            000000 IDENTIFICATION DIVISION.                                        \s
            000000 PROGRAM-ID. IMSPGM.                                             \s
            000000 DATA DIVISION.                                                  \s
            000000 WORKING-STORAGE SECTION.                                        \s
            000000 77 GU-FUNC PIC X(4) VALUE 'GU  '.                               \s
            000000 PROCEDURE DIVISION.                                             \s
            000000     ENTRY 'DLITCBL' USING IO-PCB ACCT-DB-PCB.                   \s
            000000     CALL 'CBLTDLI' USING GU-FUNC IO-PCB MSG-AREA.               \s
            000000     CALL 'CBLTDLI' USING GU-FUNC ACCT-DB-PCB IO-AREA.           \s
            000000     GOBACK.                                                     \s
            """
        );

        assertThat(calls).hasSize(2);
        assertThat(calls.get(0).isMessageCall()).isTrue();
        assertThat(calls.get(1).isMessageCall()).isFalse();
    }

    @Test
    void resolvesSegmentNamesFromSsas() {
        List<DliCall> calls = parse(
          """
            000000 IDENTIFICATION DIVISION.                                        \s
            000000 PROGRAM-ID. IMSPGM.                                             \s
            000000 DATA DIVISION.                                                  \s
            000000 WORKING-STORAGE SECTION.                                        \s
            000000 77 GU-FUNC PIC X(4) VALUE 'GU  '.                               \s
            000000 77 ACCT-SSA PIC X(9) VALUE 'ACCOUNT '.                          \s
            000000 PROCEDURE DIVISION.                                             \s
            000000     CALL 'CBLTDLI' USING GU-FUNC DB-PCB IO-AREA ACCT-SSA.       \s
            000000     GOBACK.                                                     \s
            """
        );

        assertThat(calls).hasSize(1);
        assertThat(calls.get(0).getSsas()).containsExactly("ACCT-SSA");
        assertThat(calls.get(0).getSegments()).containsExactly("ACCOUNT");
    }

    @Test
    void fallsBackToTheSsaNamingConvention() {
        List<DliCall> calls = parse(
          """
            000000 IDENTIFICATION DIVISION.                                        \s
            000000 PROGRAM-ID. IMSPGM.                                             \s
            000000 DATA DIVISION.                                                  \s
            000000 WORKING-STORAGE SECTION.                                        \s
            000000 77 GU-FUNC PIC X(4) VALUE 'GU  '.                               \s
            000000 PROCEDURE DIVISION.                                             \s
            000000     CALL 'CBLTDLI' USING GU-FUNC DB-PCB IO-AREA CUSTOMER-SSA.   \s
            000000     GOBACK.                                                     \s
            """
        );

        assertThat(calls.get(0).getSegments()).containsExactly("CUSTOMER");
    }

    @Test
    void recognisesTheCommandLevelInterface() {
        List<DliCall> calls = parse(
          """
            000000 IDENTIFICATION DIVISION.                                        \s
            000000 PROGRAM-ID. DLIPGM.                                             \s
            000000 PROCEDURE DIVISION.                                             \s
            000000     EXEC DLI GU USING PCB(1) SEGMENT(ACCOUNT) INTO(WS-REC)      \s
            000000          END-EXEC.                                              \s
            000000     GOBACK.                                                     \s
            """
        );

        assertThat(calls).hasSize(1);
        DliCall gu = calls.get(0);
        assertThat(gu.getIface()).isEqualTo("EXEC DLI");
        assertThat(gu.getFunction()).isEqualTo("GU");
        assertThat(gu.getPcb()).isEqualTo("1");
        assertThat(gu.getSegments()).containsExactly("ACCOUNT");
        assertThat(gu.getAccess()).isEqualTo(DliCall.Access.READ);
    }

    /**
     * A program's whole say about a screen under IMS: the MOD's name, passed as the fourth argument
     * of an {@code ISRT} against a message PCB and kept in working storage. Read as a segment search
     * argument it resolves to a plausible segment name no database has, which is the defect this
     * exists to prevent.
     */
    @Test
    void resolvesTheModAnIsrtNames() {
        List<DliCall> calls = parse(
          """
            000000 IDENTIFICATION DIVISION.                                        \s
            000000 PROGRAM-ID. CLMI030.                                            \s
            000000 DATA DIVISION.                                                  \s
            000000 WORKING-STORAGE SECTION.                                        \s
            000000 01 WS-MOD-NAMES.                                                \s
            000000     05 MOD-INQ PIC X(08) VALUE 'CLMI1O  '.                      \s
            000000     05 MOD-ACK PIC X(08) VALUE 'CLMI6O  '.                      \s
            000000 77 ISRT-FUNC PIC X(4) VALUE 'ISRT'.                             \s
            000000 PROCEDURE DIVISION.                                             \s
            000000     ENTRY 'DLITCBL' USING IO-PCB ALT-PCB DB-PCB.                \s
            000000     CALL 'CBLTDLI' USING ISRT-FUNC IO-PCB OUT-MSG MOD-INQ.      \s
            000000     CALL 'CBLTDLI' USING ISRT-FUNC ALT-PCB ACK-MSG MOD-ACK.     \s
            000000     CALL 'CBLTDLI' USING ISRT-FUNC DB-PCB IO-AREA CLAIM-SSA.    \s
            000000     CALL 'CBLTDLI' USING ISRT-FUNC IO-PCB SPA-AREA.             \s
            000000     GOBACK.                                                     \s
            """
        );

        assertThat(calls).hasSize(4);
        assertThat(calls).extracting(DliCall::getMod, DliCall::getModOperand, DliCall::getSsas,
            DliCall::getSegments)
          .containsExactly(
            tuple("CLMI1O", "MOD-INQ", emptyList(), emptyList()),
            // An alternate PCB sends to another terminal and names a MOD the same way.
            tuple("CLMI6O", "MOD-ACK", emptyList(), emptyList()),
            tuple(null, null, singletonList("CLAIM-SSA"), singletonList("CLAIM")),
            // The SPA goes back with no format at all.
            tuple(null, null, emptyList(), emptyList()));
    }

    @Test
    void ignoresOrdinaryProgramCalls() {
        List<DliCall> calls = parse(
          """
            000000 IDENTIFICATION DIVISION.                                        \s
            000000 PROGRAM-ID. IMSPGM.                                             \s
            000000 PROCEDURE DIVISION.                                             \s
            000000     CALL 'SUBPGM' USING WS-PARM.                                \s
            000000     GOBACK.                                                     \s
            """
        );

        assertThat(calls).isEmpty();
    }
}
