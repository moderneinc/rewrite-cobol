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
package org.openrewrite.mainframe.trait;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.text.PlainText;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.openrewrite.test.SourceSpecs.text;

class ScriptTest implements RewriteTest {

    /**
     * The statements of a CLIST that reach a member, one of each shape. Everything else — the
     * conditions, the messages, the return codes — reaches nothing and is left alone.
     */
    @Test
    void readsWhatEachStatementOfAClistReaches() {
        rewriteRun(
          text(
            """
              PROC 1 JOB
              /*  %CLMSUB IN A COMMENT IS NOT A CALL  */
              %CLMSETUP ENV(PROD)
              IF &SYSDSN('CLM.PROD.JCL(CLMJ010)') NE &STR(OK) THEN EXIT CODE(8)
              ALLOC F(CLMMAST) DA('CLM.PROD.CLMMAST') SHR REUSE
              CALL 'CLM.PROD.LOADLIB(CLMB010)'
              DSN SYSTEM(DB2P)
              RUN PROGRAM(CLMD020) PLAN(CLMPLAN) LIB('CLM.PROD.LOADLIB')
              ISPEXEC EDIT DATASET('CLM.PROD.JCL(CLMJ010)')
              ISPEXEC SELECT PGM(ISRDSLST) PARM(LMD)
              EXEC 'CLM.PROD.CLIST(CLMNITE)'
              WRITENR CLMSUB: SUBMIT &JOB FROM CLM.PROD.JCL (Y/N)?
              SUBMIT 'CLM.PROD.JCL(CLMJ010)'
              """,
            spec -> spec.path("CLMDLG.clist").afterRecipe(cu -> assertThat(references(cu))
              .extracting(Script.Reference::getKind, Script.Reference::getName,
                Script.Reference::getDataSet, Script.Reference::getLine)
              .containsExactly(
                tuple(Script.Reference.Kind.EXEC, "CLMSETUP", null, 3),
                tuple(Script.Reference.Kind.CHECK, "CLMJ010", "CLM.PROD.JCL", 4),
                tuple(Script.Reference.Kind.ALLOCATE, "CLMMAST", "CLM.PROD.CLMMAST", 5),
                tuple(Script.Reference.Kind.CALL, "CLMB010", "CLM.PROD.LOADLIB", 6),
                tuple(Script.Reference.Kind.RUN, "CLMD020", null, 8),
                tuple(Script.Reference.Kind.EDIT, "CLMJ010", "CLM.PROD.JCL", 9),
                tuple(Script.Reference.Kind.SELECT, "ISRDSLST", null, 10),
                tuple(Script.Reference.Kind.EXEC, "CLMNITE", "CLM.PROD.CLIST", 11),
                tuple(Script.Reference.Kind.SUBMIT, "CLMJ010", "CLM.PROD.JCL", 13))))
        );
    }

    /**
     * Every script writes {@code WRITE ...: SUBMIT &JOB (Y/N)?} somewhere, so the verb of a statement
     * decides and a word in the middle of a message reaches nothing.
     */
    @Test
    void readsTheVerbOfTheStatementAndNotAWordInAMessage() {
        rewriteRun(
          text(
            """
              WRITE CLMNITE: SUBMIT CLMJ010 (Y/N/Q)?
              SET &ZEDLMSG = &STR(EDIT OF CLM.PROD.JCL(CLMJ010) ENDED RC=&RC)
              IF &JOB = CLMCMPC THEN SUBMIT 'CLM.PROD.JCL(CLMCMPC)'
              """,
            spec -> spec.path("CLMNITE.clist").afterRecipe(cu -> assertThat(references(cu))
              .extracting(Script.Reference::getKind, Script.Reference::getName)
              .containsExactly(tuple(Script.Reference.Kind.SUBMIT, "CLMCMPC"))))
        );
    }

    /**
     * A name a script computes is reported as it stands and said to be by argument. A CLIST writes one
     * with a leading {@code &}, and the qualifier being a variable does not make the library one.
     */
    @Test
    void saysWhichNamesTheScriptComputedAndWhichItWroteDown() {
        rewriteRun(
          text(
            """
              SUBMIT '&CLMHLQ..JCL(&JOB)'
              CALL '&CLMHLQ..LOADLIB(CLMB010)'
              ALTLIB ACTIVATE APPLICATION(CLIST) DATASET('&CLMHLQ..CLIST') QUIET
              """,
            spec -> spec.path("CLMSUB.clist").afterRecipe(cu -> assertThat(references(cu))
              .extracting(Script.Reference::getName, Script.Reference::isSymbolic)
              .containsExactly(
                tuple("&JOB", true),
                tuple("CLMB010", false),
                tuple("CLIST", false))))
        );
    }

    /**
     * REXX builds a command out of strings and variables written side by side, so the double quotes
     * come off and what was between them joins up. What was written outside them is what the exec
     * computed, which is the difference between the job {@code CLMJ010} and the job named by the
     * argument.
     */
    @Test
    void joinsUpTheStringsARexxExecBuildsACommandFrom() {
        rewriteRun(
          text(
            """
              /* REXX */
              HLQ = 'CLM.PROD'
              "LISTDS '"HLQ".JCL' MEMBERS"
              "ALLOC F(JCLIN) DA('"HLQ".JCL("MEM")') SHR REUSE"
              "SUBMIT '"HLQ".JCL(CLMJ010)'"
              "SUBMIT * END(@@)"
              """,
            spec -> spec.path("CLMPICK.rexx").afterRecipe(cu -> assertThat(references(cu))
              .extracting(Script.Reference::getKind, Script.Reference::getName,
                Script.Reference::getDataSet, Script.Reference::getDdName,
                Script.Reference::isSymbolic)
              .containsExactly(
                tuple(Script.Reference.Kind.CHECK, "JCL", "HLQ.JCL", null, false),
                tuple(Script.Reference.Kind.ALLOCATE, "MEM", "HLQ.JCL", "JCLIN", true),
                tuple(Script.Reference.Kind.SUBMIT, "CLMJ010", "HLQ.JCL", null, false))))
        );
    }

    /**
     * An exec kept without an extension — which is how a PDS member arrives when it is copied off as
     * it stands — is known by the comment TSO itself reads, and by nothing else.
     */
    @Test
    void readsAnExecKeptWithoutAnExtension() {
        rewriteRun(
          text(
            """
              /* REXX - SUBMIT THE NIGHTLY STREAM */
              "SUBMIT 'CLM.PROD.JCL(CLMJ010)'"
              """,
            spec -> spec.path("rexx/CLMNITE").afterRecipe(cu -> assertThat(references(cu))
              .extracting(Script.Reference::getKind, Script.Reference::getName)
              .containsExactly(tuple(Script.Reference.Kind.SUBMIT, "CLMJ010"))))
        );
    }

    /**
     * Every name a script writes, which is what a search for a member name finds in it. It
     * over-answers on purpose: an English word of eight letters or fewer is spelled exactly like a
     * member name, and which of these is a component is a join and not a lexical rule.
     */
    @Test
    void findsEveryNameAScriptWritesDown() {
        rewriteRun(
          text(
            """
              /*  RUN THE NIGHTLY STREAM, TABLE CLMNIGHT  */
              SET &JOB1 = CLMJ010
              SET &JOB2 = CLMJ020
              %CLMSUB &JOB NOASK
              """,
            spec -> spec.path("CLMNITE.clist").afterRecipe(cu -> {
                List<String> names = new ArrayList<>();
                new Script.Matcher().require(cu, null).getMentions().forEach(name -> names.add(name.getText()));
                assertThat(names).contains("CLMNIGHT", "CLMJ010", "CLMJ020", "CLMSUB");
                // A name is distinct and carries the line it was first written on.
                assertThat(new Script.Matcher().require(cu, null).getMentions())
                  .filteredOn(name -> "CLMJ020".equals(name.getText()))
                  .singleElement()
                  .satisfies(name -> assertThat(name.getLine()).isEqualTo(3));
                // Not a name: a word with a lower case letter is prose, and a word of more than
                // eight characters is longer than a member name may be.
                assertThat(names).doesNotContain("NOASK1234", "Claims");
            }))
        );
    }

    /**
     * The other half of a submit by argument: what the member sets, what it calls and what it takes.
     * The job is written down under the {@code WHEN} that chooses it and handed on as a variable, so
     * nothing but the caller and the callee together says which job is submitted.
     */
    @Test
    void readsWhatAClistSetsCallsAndDeclares() {
        rewriteRun(
          text(
            """
              PROC 1 MEM ENV(PROD)
              /*  SET &JOB = CLMCMPX IN A COMMENT IS NOT AN ASSIGNMENT  */
              %CLMSETUP ENV(&ENV)
              SET &KIND = &SUBSTR(4,&MEM)
              SELECT (&KIND)
                WHEN (B) SET &JOB = CLMCMPB
                WHEN (C) SET &JOB = CLMCMPC
              END
              SET &CARD = +
                &STR(//COMPILE EXEC &PROC,MEM=&MEM)
              %CLMSUB &JOB NOASK
              """,
            spec -> spec.path("CLMCOMP.clist").afterRecipe(cu -> {
                Script script = new Script.Matcher().require(cu, null);
                assertThat(script.getParameters())
                  .extracting(Script.Parameter::getName, Script.Parameter::getDefaultValue,
                    Script.Parameter::isPositional)
                  .containsExactly(tuple("MEM", null, true), tuple("ENV", "PROD", false));
                assertThat(script.getAssignments())
                  .extracting(Script.Assignment::getVariable, Script.Assignment::getValue,
                    Script.Assignment::isLiteral, Script.Assignment::getLine)
                  .containsExactly(
                    tuple("KIND", "&SUBSTR(4,&MEM)", false, 4),
                    tuple("JOB", "CLMCMPB", true, 6),
                    tuple("JOB", "CLMCMPC", true, 7),
                    // The continuation is joined, so the value is what was set and not a plus sign.
                    tuple("CARD", "&STR(//COMPILE EXEC &PROC,MEM=&MEM)", false, 9));
                assertThat(script.getInvocations())
                  .extracting(Script.Invocation::getName, Script.Invocation::getArguments,
                    Script.Invocation::getLine)
                  .containsExactly(
                    tuple("CLMSETUP", List.of("ENV(&ENV)"), 3),
                    tuple("CLMSUB", List.of("&JOB", "NOASK"), 11));
            }))
        );
    }

    /**
     * An exec takes its arguments with {@code PARSE ARG}, assigns without a verb and calls another
     * exec by writing its name where a command goes, so none of the three readings is the same
     * reading there and none of them is done.
     */
    @Test
    void readsNeitherAssignmentNorParameterOfARexxExec() {
        rewriteRun(
          text(
            """
              /*  REXX  */
              PARSE ARG MEM
              PROC = 'CLMCLB'
              "SUBMIT '"HLQ".JCL("JOB")'"
              """,
            spec -> spec.path("CLMCMPX.rexx").afterRecipe(cu -> {
                Script script = new Script.Matcher().require(cu, null);
                assertThat(script.getAssignments()).isEmpty();
                assertThat(script.getInvocations()).isEmpty();
                assertThat(script.getParameters()).isEmpty();
                // What it reaches is read as it always was.
                assertThat(references(cu))
                  .extracting(Script.Reference::getKind, Script.Reference::getName)
                  .containsExactly(tuple(Script.Reference.Kind.SUBMIT, "JOB"));
            }))
        );
    }

    /**
     * A run book is not a script, so the matcher leaves it alone: the trait would report the
     * {@code CALLS} and {@code RUN BY} lines of a run book as statements, and they are prose.
     */
    @Test
    void doesNotReadARunBookAsAScript() {
        rewriteRun(
          text(
            """
              DOCPGM   CLMB010
              CALLS        NONE
              """,
            spec -> spec.path("CLMB010.docpgm").afterRecipe(cu ->
              assertThat(new Script.Matcher().lower(cu).collect(Collectors.toList())).isEmpty()))
        );
    }

    private static List<Script.Reference> references(PlainText cu) {
        return new Script.Matcher().require(cu, null).getReferences();
    }
}
