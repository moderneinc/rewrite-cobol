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
package org.openrewrite.mainframe.assembler.trait;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RewriteTest;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.tuple;
import static org.openrewrite.mainframe.assembler.Assertions.assembler;

class ReferencesTest implements RewriteTest {

    /**
     * INTERLINKS 20.2. A member of the macro library is reached two ways, and which library it lives
     * in says nothing: {@code CLMREGS} is copied, {@code CLMSAVE} is invoked, and both are
     * {@code CLM.PROD.MACLIB}.
     */
    @DocumentExample
    @Test
    void tellsAMemberThatIsCopiedFromOneThatIsInvoked() {
        rewriteRun(
          assembler(
            """
                       COPY  CLMREGS
                       COPY  CLMRECD
              CLMU040  CLMSAVE 12,SAVE=U40SAVE,ID=CLMU040
                       L     R2,0(,R1)           ADDRESS OF THE CLAIM RECORD
                       OPEN  (A10CRD,(INPUT))
              U40NUM   CLMRTRN 4
                       END   CLMU040
              """,
            spec -> spec.afterRecipe(cu -> {
                assertThat(new Copy.Matcher().lower(cu).collect(Collectors.toList()))
                  .extracting(Copy::getMember, Copy::getLine)
                  .containsExactly(tuple("CLMREGS", 1), tuple("CLMRECD", 2));

                List<MacroCall> macros = new MacroCall.Matcher().lower(cu).collect(Collectors.toList());
                assertThat(macros).extracting(MacroCall::getName, MacroCall::getLabel, MacroCall::getLine)
                  .containsExactly(
                    tuple("CLMSAVE", "CLMU040", 3),
                    tuple("OPEN", "", 5),
                    tuple("CLMRTRN", "U40NUM", 6));
                // Which of them the shop wrote is the library's answer, not the program's: OPEN comes
                // out of SYS1.MACLIB, which is nowhere in the repository.
                List<String> library = List.of("CLMSAVE", "CLMRTRN", "CLMREGS", "CLMRECD", "CLMPCBD");
                assertThat(macros).filteredOn(macro -> macro.isDefinedBy(library))
                  .extracting(MacroCall::getName).containsExactly("CLMSAVE", "CLMRTRN");
                assertThat(macros.get(0).getOperands()).containsExactly("12", "SAVE=U40SAVE", "ID=CLMU040");
                assertThat(macros.get(0).getOperand("SAVE")).isEqualTo("U40SAVE");
            })
          )
        );
    }

    /**
     * The statement after a {@code MACRO} names the macro being defined and says what a caller may
     * pass it. Read as an invocation instead, every macro library member reports one call of itself.
     */
    @Test
    void readsThePrototypeOfAMacroRatherThanAnInvocationOfIt() {
        rewriteRun(
          assembler(
            """
                       MACRO
              &NAME    CLMRTRN &RC,&REG=
              &NAME    DS    0H
                       L     13,4(,13)           BACK TO THE CALLER'S SAVE AREA
                       MEND
              """,
            spec -> spec.afterRecipe(cu -> {
                assertThat(new MacroCall.Matcher().lower(cu).collect(Collectors.toList())).isEmpty();
                assertThat(new MacroDefinition.Matcher().lower(cu).collect(Collectors.toList()))
                  .singleElement()
                  .satisfies(macro -> {
                      assertThat(macro.getName()).isEqualTo("CLMRTRN");
                      assertThat(macro.getLabelParameter()).isEqualTo("NAME");
                      assertThat(macro.getPositionalParameters()).containsExactly("RC");
                      assertThat(macro.getKeywordParameters()).containsExactly(entry("REG", ""));
                      assertThat(macro.getLine()).isEqualTo(2);
                  });
            })
          )
        );
    }

    /**
     * An extended mnemonic is a machine instruction and not a macro, however short its name. Leaving
     * the unconditional branch out of the table read 218 of them across the corpus as invocations of a
     * macro called {@code B}.
     */
    @Test
    void readsABranchAsAnInstructionRatherThanAMacro() {
        rewriteRun(
          assembler(
            """
              U40NUM   B     U40EXIT
                       NOP   U40EXIT
                       CLMRTRN 4
              """,
            spec -> spec.afterRecipe(cu -> assertThat(new MacroCall.Matcher().lower(cu)
              .collect(Collectors.toList())).extracting(MacroCall::getName)
              .containsExactly("CLMRTRN"))
          )
        );
    }

    /**
     * Most of a shop's macro library is DSECTs read by {@code COPY}, and only the prototype tells the
     * two apart: {@code CLMREGS} defines no macro, so an invocation of that name reaches nothing here.
     */
    @Test
    void readsNoMacroDefinitionInACopyMember() {
        rewriteRun(
          assembler(
            """
              R1       EQU   1
              R13      EQU   13
              """,
            spec -> spec.afterRecipe(cu -> assertThat(new MacroDefinition.Matcher().lower(cu)
              .collect(Collectors.toList())).isEmpty())
          )
        );
    }

    /**
     * INTERLINKS 20.6. A DCB is the assembler's {@code ASSIGN}: it names a DD and nothing else, and
     * only the JCL says what data set that DD is bound to.
     */
    @Test
    void readsTheDdNameADcbAssigns() {
        rewriteRun(
          assembler(
            "A10CRD   DCB   DDNAME=PURGCARD,DSORG=PS,MACRF=(GM),RECFM=FB,           X\n" +
            "               LRECL=80,EODAD=A10NOCD\n" +
            "A10RPT   DCB   DDNAME=PURGRPT,DSORG=PS,MACRF=(PM),RECFM=FBA,           X\n" +
            "               LRECL=133\n",
            spec -> spec.afterRecipe(cu -> assertThat(new DataControlBlock.Matcher().lower(cu)
              .collect(Collectors.toList()))
              .extracting(DataControlBlock::getName, DataControlBlock::getDdName,
                DataControlBlock::getOrganization, DataControlBlock::getAccess,
                DataControlBlock::getRecordFormat, DataControlBlock::getRecordLength,
                DataControlBlock::getEndOfDataLabel)
              .containsExactly(
                tuple("A10CRD", "PURGCARD", "PS", "(GM)", "FB", 80, "A10NOCD"),
                tuple("A10RPT", "PURGRPT", "PS", "(PM)", "FBA", 133, null)))
          )
        );
    }

    /**
     * The fixture's three programs have no {@code CSECT} statement at all — the entry macro generates
     * it — so the name a caller writes is the one on the {@code END}.
     */
    @Test
    void readsTheNamesAMemberOffersAndTheOnesItNeeds() {
        rewriteRun(
          assembler(
            """
              BASE64E  CSECT
                       ENTRY BASE64EA,BASE64D
                       EXTRN CEEENTRY
                       WXTRN IEANTRT
                       END   BASE64E
              """,
            spec -> spec.afterRecipe(cu -> {
                assertThat(new EntryPoint.Matcher().lower(cu).collect(Collectors.toList()))
                  .extracting(EntryPoint::getKind, EntryPoint::getNames)
                  .containsExactly(
                    tuple(EntryPoint.Kind.SECTION, List.of("BASE64E")),
                    tuple(EntryPoint.Kind.ENTRY, List.of("BASE64EA", "BASE64D")),
                    tuple(EntryPoint.Kind.END, List.of("BASE64E")));
                assertThat(new ExternalName.Matcher().lower(cu).collect(Collectors.toList()))
                  .extracting(ExternalName::getNames, ExternalName::isWeak)
                  .containsExactly(
                    tuple(List.of("CEEENTRY"), false),
                    tuple(List.of("IEANTRT"), true));
            })
          )
        );
    }
}
