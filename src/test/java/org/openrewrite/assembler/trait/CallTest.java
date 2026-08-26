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
package org.openrewrite.assembler.trait;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.cobol.trait.DliCall.Access;
import org.openrewrite.test.RewriteTest;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.openrewrite.assembler.Assertions.assembler;

class CallTest implements RewriteTest {

    @DocumentExample
    @Test
    void readsTheCallMacroAndItsParameterList() {
        rewriteRun(
          assembler(
            """
              CLMU040  CSECT
                       LA    R3,CLMRCLM
                       ST    R3,U40PLST          ONE PARAMETER FOR CLMU030
                       CALL  CLMU030,MF=(E,U40PLST)
                       END   CLMU040
              """,
            spec -> spec.afterRecipe(cu -> assertThat(new Call.Matcher().lower(cu)
              .collect(Collectors.toList())).singleElement().satisfies(call -> {
                assertThat(call.getTarget()).isEqualTo("CLMU030");
                assertThat(call.getKind()).isEqualTo(Call.Kind.CALL_MACRO);
                assertThat(call.getParameterList()).isEqualTo("U40PLST");
                assertThat(call.getLine()).isEqualTo(4);
            }))
          )
        );
    }

    @Test
    void readsTheArgumentsOfAVariableLengthCall() {
        rewriteRun(
          assembler(
            """
              A10LOOP  CALL  ASMTDLI,(A10GHN,(R11),A10ROOT,A10SSA),VL
              """,
            spec -> spec.afterRecipe(cu -> assertThat(new Call.Matcher().lower(cu)
              .collect(Collectors.toList())).singleElement().satisfies(call -> {
                assertThat(call.getTarget()).isEqualTo("ASMTDLI");
                assertThat(call.getArguments())
                  .containsExactly("A10GHN", "(R11)", "A10ROOT", "A10SSA");
                assertThat(call.isVariableLength()).isTrue();
            }))
          )
        );
    }

    /**
     * A V-con call says nothing on its own. It is a call because a load of the constant into R15 is
     * followed by a branch that comes back through R14, and that shape is the whole of the evidence.
     */
    @Test
    void readsAVConLoadedIntoRegister15AndBranchedTo() {
        rewriteRun(
          assembler(
            """
                       LAY   R1,MSG_AREA        Address parm list area
                       L     15,=V(GVBUTMSG)
                       BASSM 14,15
                       XC    MSGS2PTR,MSGS2PTR
                       L     15,=V(GVBUTMSG)
                       LR    R2,R3
              """,
            spec -> spec.afterRecipe(cu -> assertThat(new Call.Matcher().lower(cu)
              .collect(Collectors.toList()))
              // The second load is never branched to, so it is not a call.
              .singleElement().satisfies(call -> {
                  assertThat(call.getTarget()).isEqualTo("GVBUTMSG");
                  assertThat(call.getKind()).isEqualTo(Call.Kind.V_CON);
              }))
          )
        );
    }

    @Test
    void followsALoadThroughTheVConTheSymbolNames() {
        rewriteRun(
          assembler(
            """
                       L     R15,GVBDL96A
                       BALR  R14,R15
              GVBDL96A DC    V(GVBDL96)         ADDRESS OF "GVBDL96"
              """,
            spec -> spec.afterRecipe(cu -> assertThat(new Call.Matcher().lower(cu)
              .collect(Collectors.toList())).singleElement()
              .extracting(Call::getTarget).isEqualTo("GVBDL96"))
          )
        );
    }

    /**
     * INTERLINKS 20.5. The function is not written on the call: the first argument names a constant,
     * and only the constant says which DL/I function was asked for.
     */
    @Test
    void readsADliCallThroughTheConstantsItsArgumentsName() {
        rewriteRun(
          assembler(
            """
              A10LOOP  CALL  ASMTDLI,(A10GHN,(R11),A10ROOT,A10SSA),VL
                       CLC   PCBSTAT,=C'  '
                       BNE   A10EOD
                       CALL  ASMTDLI,(A10DLET,(R11),A10ROOT),VL
              A10GHN   DC    CL4'GHN '
              A10DLET  DC    CL4'DLET'
              A10SSA   DC    CL9'CLMROOT'        UNQUALIFIED, ROOTS ONLY
              A10ROOT  DS    CL65
              """,
            spec -> spec.afterRecipe(cu -> {
                List<DliCall> calls = new DliCall.Matcher().lower(cu).collect(Collectors.toList());
                assertThat(calls).extracting(DliCall::getIface, DliCall::getFunction,
                    DliCall::getPcbRegister, DliCall::getIoArea, DliCall::getSsas,
                    DliCall::getSegments, DliCall::getAccess)
                  .containsExactly(
                    tuple("ASMTDLI", "GHN", "R11", "A10ROOT",
                      List.of("A10SSA"), List.of("CLMROOT"), Access.READ),
                    // The delete passes no SSA at all: it deletes the segment the get held.
                    tuple("ASMTDLI", "DLET", "R11", "A10ROOT",
                      List.of(), List.of(), Access.DELETE));
            })
          )
        );
    }
}
