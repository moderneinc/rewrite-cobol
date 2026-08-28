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
package org.openrewrite.mainframe.listload.trait;

import org.junit.jupiter.api.Test;
import org.openrewrite.mainframe.Members;
import org.openrewrite.test.RewriteTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.openrewrite.test.SourceSpecs.text;

class ModuleListingTest implements RewriteTest {

    private static final String AMBLIST = String.join("\n",
      "1                                          A M B L I S T                                 PAGE     1",
      " ",
      "    LISTLOAD OUTPUT=MODLIST,DDN=LOADLIB",
      " ",
      "0                                          ** MODULE SUMMARY **",
      "0",
      "      MEMBER NAME:                  CLMI010",
      "      MAIN ENTRY POINT:             000000F8",
      "      LIBRARY:                      DDNAME=LOADLIB  DSNAME=CLM.PROD.LOADLIB",
      "      AMODE OF MAIN ENTRY POINT:    31",
      "      ALIAS(ES):                    CLMLOAD   ENTRY POINT 00000000  AMODE 31",
      "      MODULE ATTRIBUTES:            REENTERABLE, REUSABLE, REFRESHABLE, NOT OVERLAY,",
      "                                    EXECUTABLE, EDITABLE",
      "      MODULE SIZE (HEX):            00002970",
      "0",
      "0                                          ** CONTROL SECTION SUMMARY **",
      "0",
      "      CSECT NAME    ORIGIN    LENGTH    TYPE   AMODE   ENTRY NAME    LOCATION",
      "      CLMI010       00000000  00001C30  SD     31      DLITCBL       000000F8",
      "      DFSLI000      00001C30  00000D40  SD     31      ASMTDLI       00001C38",
      "                                                        CBLTDLI       00001C40",
      "1                                          A M B L I S T                                 PAGE     2",
      " ",
      "0                                  ** IDENTIFICATION RECORD DATA (IDR) FOR MEMBER CLMI010 **",
      "0",
      "                                   ***** TRANSLATOR IDENTIFICATION DATA *****",
      "0",
      "           TRANSLATOR   VER   MOD     DATE",
      " CSECT:    CLMI010",
      "           5648A25       02    02    02/03/2026",
      " CSECT:    DFSLI000",
      "           569623400     01    03    10/29/1999",
      "0",
      "                                   ***** ZAP IDENTIFICATION DATA *****",
      "0",
      "           NONE",
      "");

    private static final String BINDER = String.join("\n",
      "1z/OS V2 R5 BINDER     10:15:31 TUESDAY FEBRUARY 3, 2026",
      " BATCH EMULATOR  JOB(CLMCMPC ) STEP(CMPC020 ) PGM= IEWL      PROCEDURE(LKED    )",
      " IEW2278I B352 INVOCATION PARAMETERS - LIST,MAP,XREF,RENT",
      " IEW2322I 1220  1  *  CLMC020 - CLAIM INQUIRY.  CLMU020 IS CALLED STATICALLY.",
      " IEW2322I 1220  2    INCLUDE SYSLIB(DFHECI)",
      " IEW2322I 1220  3    INCLUDE OBJLIB(CLMU020)",
      " IEW2322I 1220  4    ENTRY CLMC020",
      " IEW2322I 1220  5    ALIAS CLMINQ",
      " IEW2322I 1220  6    NAME CLMC020(R)",
      " ",
      "1                                       *** M O D U L E  M A P ***",
      " ",
      "   SECTION    CLASS                                      ------- SOURCE --------",
      "    OFFSET   OFFSET  NAME                TYPE    LENGTH  DDNAME   SEQ  MEMBER",
      " ",
      "                  0  CLMC020            CSECT      2F10   SYSLIN    01  **NULL**",
      "               2F10  DFHECI             CSECT        58   SYSLIB    01  DFHECI",
      "         8              DFHEI1           LABEL",
      "               2F68  CLMU020            CSECT       6E8   OBJLIB    01  CLMU020",
      " ",
      "  *** E N D  O F  M O D U L E  M A P ***",
      " ",
      " IEW2008I 0F03 PROCESSING COMPLETED.  RETURN CODE =  0.",
      "");

    @Test
    void whatAModuleSummarySaysAboutTheModule() {
        rewriteRun(
          text(AMBLIST, spec -> spec.afterRecipe(cu -> {
              ModuleListing.Module module = new ModuleListing.Matcher().require(cu, null).getModules().get(0);
              assertThat(module.getName()).isEqualTo("CLMI010");
              assertThat(module.getLibrary()).isEqualTo("LOADLIB");
              assertThat(module.getDataSetName()).isEqualTo("CLM.PROD.LOADLIB");
              assertThat(module.getSize()).isEqualTo("2970");
              assertThat(module.getAliases()).extracting(ModuleListing.Name::getText).containsExactly("CLMLOAD");
              assertThat(module.getLine()).isEqualTo(5);
          }))
        );
    }

    /**
     * The summary gives the entry point as an offset and nothing else, so which name stands there is
     * a question only the control section summary answers. A DL/I program is entered at the
     * {@code DLITCBL} label it declares rather than at its own CSECT, which is the case worth being
     * sure of.
     */
    @Test
    void theEntryPointIsAnOffsetUntilTheSectionsResolveIt() {
        rewriteRun(
          text(AMBLIST, spec -> spec.afterRecipe(cu -> {
              ModuleListing.Entry entry = new ModuleListing.Matcher().require(cu, null)
                .getModules().get(0).getEntry();
              assertThat(entry.getName()).isEqualTo("DLITCBL");
              assertThat(entry.getOffset()).isEqualTo("F8");
              assertThat(entry.getLine()).isEqualTo(19);
          }))
        );
    }

    @Test
    void theSectionsTheModuleIsMadeOf() {
        rewriteRun(
          text(AMBLIST, spec -> spec.afterRecipe(cu -> {
              ModuleListing.Module module = new ModuleListing.Matcher().require(cu, null).getModules().get(0);
              assertThat(module.getCsects())
                .extracting(ModuleListing.Csect::getName, ModuleListing.Csect::getOffset,
                  ModuleListing.Csect::getLength, ModuleListing.Csect::getType)
                .containsExactly(
                  tuple("CLMI010", "0", "1C30", "SD"),
                  tuple("DFSLI000", "1C30", "D40", "SD"));
              assertThat(module.getCsects().get(1).getEntries())
                .extracting(ModuleListing.Entry::getName, ModuleListing.Entry::getOffset)
                .containsExactly(tuple("ASMTDLI", "1C38"), tuple("CBLTDLI", "1C40"));
          }))
        );
    }

    /**
     * Column 1 of a report is what the printer acted on rather than something it printed, so a row
     * whose carriage control stands against the name would otherwise take the character for part of
     * it.
     */
    @Test
    void columnOneOfAReportIsCarriageControlAndNotText() {
        String report = String.join("\n",
          "1                                          A M B L I S T                                 PAGE     1",
          "0                                          ** MODULE SUMMARY **",
          "      MEMBER NAME:                  CLMB010",
          "0                                          ** CONTROL SECTION SUMMARY **",
          "-CLMB010       00000000  00001C30  SD",
          "");

        rewriteRun(
          text(report, spec -> spec.afterRecipe(cu -> assertThat(
            new ModuleListing.Matcher().require(cu, null).getModules().get(0).getCsects())
            .extracting(ModuleListing.Csect::getName)
            .containsExactly("CLMB010")))
        );
    }

    @Test
    void whatCompiledEachSection() {
        rewriteRun(
          text(AMBLIST, spec -> spec.afterRecipe(cu -> assertThat(
            new ModuleListing.Matcher().require(cu, null).getModules().get(0).getCsects())
            .extracting(csect -> csect.getTranslator().getProductId(),
              csect -> csect.getTranslator().getLanguage(),
              csect -> csect.getTranslator().getVersion(),
              csect -> csect.getTranslator().getDate())
            .containsExactly(
              tuple("5648A25", ModuleListing.Language.COBOL, "02.02", "02/03/2026"),
              tuple("569623400", ModuleListing.Language.ASSEMBLER, "01.03", "10/29/1999"))))
        );
    }

    @Test
    void whatTheReportWasAskedFor() {
        rewriteRun(
          text("  LISTLOAD OUTPUT=MODLIST,DDN=LOADLIB\n  LISTIDR  DDN=CICSLOAD\n",
            spec -> spec.afterRecipe(cu -> assertThat(new ModuleListing.Matcher().require(cu, null).getRequests())
              .extracting(ModuleListing.Request::getFunction, ModuleListing.Request::getDdName,
                ModuleListing.Request::getOutput, ModuleListing.Request::getLine)
              .containsExactly(
                tuple("LISTLOAD", "LOADLIB", "MODLIST", 1),
                tuple("LISTIDR", "CICSLOAD", null, 2))))
        );
    }

    /**
     * The binder echoes the deck it was given, so the deck is read by the deck reader and the lines
     * reported are the listing's own.
     */
    @Test
    void theDeckABinderListingEchoes() {
        rewriteRun(
          text(BINDER, spec -> spec.afterRecipe(cu -> {
              ModuleListing.Module module = new ModuleListing.Matcher().require(cu, null).getModules().get(0);
              assertThat(module.getName()).isEqualTo("CLMC020");
              assertThat(module.getLine()).isEqualTo(9);
              assertThat(module.getEntry().getName()).isEqualTo("CLMC020");
              assertThat(module.getEntry().getOffset()).isEqualTo("0");
              assertThat(module.getAliases())
                .extracting(ModuleListing.Name::getText, ModuleListing.Name::getLine)
                .containsExactly(tuple("CLMINQ", 8));
          }))
        );
    }

    /**
     * A binder map prints a label's offset within its own section and AMBLIST prints it within the
     * module, so the two only reconcile if one of them is put on the other's footing.
     */
    @Test
    void theSectionsABinderMapPlaces() {
        rewriteRun(
          text(BINDER, spec -> spec.afterRecipe(cu -> {
              ModuleListing.Module module = new ModuleListing.Matcher().require(cu, null).getModules().get(0);
              assertThat(module.getCsects())
                .extracting(ModuleListing.Csect::getName, ModuleListing.Csect::getOffset,
                  ModuleListing.Csect::getLength, ModuleListing.Csect::getDdName, ModuleListing.Csect::getMember)
                .containsExactly(
                  tuple("CLMC020", "0", "2F10", "SYSLIN", null),
                  tuple("DFHECI", "2F10", "58", "SYSLIB", "DFHECI"),
                  tuple("CLMU020", "2F68", "6E8", "OBJLIB", "CLMU020"));
              assertThat(module.getCsects().get(1).getEntries())
                .extracting(ModuleListing.Entry::getName, ModuleListing.Entry::getOffset)
                .containsExactly(tuple("DFHEI1", "2F18"));
          }))
        );
    }

    /**
     * The cross-reference table under the map has rows of the same shape, and they are references out
     * of the module rather than sections in it.
     */
    @Test
    void readsNothingPastTheEndOfTheMap() {
        String listing = BINDER +
          String.join("\n",
            "1                                       *** C R O S S  R E F E R E N C E  T A B L E ***",
            " ",
            "        C0       C0  IGZCBSO            V-CON   IGZCBSO              3650   B_TEXT",
            "");

        rewriteRun(
          text(listing, spec -> spec.afterRecipe(cu -> assertThat(
            new ModuleListing.Matcher().require(cu, null).getModules().get(0).getCsects())
            .extracting(ModuleListing.Csect::getName)
            .containsExactly("CLMC020", "DFHECI", "CLMU020")))
        );
    }

    @Test
    void typesAReportByItsHeadingAndADeckByItsFunction() {
        assertThat(Members.isReport("1                     A M B L I S T          PAGE 1\n")).isTrue();
        assertThat(Members.isReport("1z/OS V2 R5 BINDER\n IEW2278I B352 INVOCATION PARAMETERS - LIST,MAP\n")).isTrue();
        assertThat(Members.isRequest("  LISTLOAD OUTPUT=MODLIST,DDN=LOADLIB\n")).isTrue();

        assertThat(Members.isModuleListing("  SORT FIELDS=(1,8,CH,A)\n")).isFalse();
        assertThat(Members.isModuleListing("  DELETE CLM.PROD.EXTRACT\n")).isFalse();
    }

    /**
     * A member that says nothing about itself must not cost a full read to type: a report announces
     * itself in its heading and a deck in its first card.
     */
    @Test
    void doesNotReadAWholeFileToRefuseIt() {
        StringBuilder parms = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            parms.append("  RECFM=FB,LRECL=80\n");
        }
        parms.append("1                     A M B L I S T\n");
        assertThat(Members.isModuleListing(parms.toString())).isFalse();
    }
}
