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
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RewriteTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.openrewrite.test.SourceSpecs.text;

class ReferencesTest implements RewriteTest {

    /**
     * INTERLINKS 21.2 and 21.3. Everything the program reaches, it reaches by a DD name the step
     * running it resolves: {@code SASSRC} is not a path, and a {@code LIBNAME} or an {@code INFILE}
     * with no data set after it names whatever the JCL allocated.
     */
    @DocumentExample
    @Test
    void readsWhatAProgramReachesByDdName() {
        rewriteRun(
          text(
            """
              %INCLUDE SASSRC(CLMSMAC);

              LIBNAME CLMSAS;

              DATA CLMSAS.CLMDAY;
                 INFILE CLMEXTR LRECL=200 RECFM=FB;
              RUN;
              """,
            spec -> spec.path("sas/CLMSEXTR.sas").afterRecipe(cu -> {
                assertThat(new Include.Matcher().require(cu, null).getReferences())
                  .extracting(Include.Reference::getDdName, Include.Reference::getMember,
                    Include.Reference::getLine)
                  .containsExactly(tuple("SASSRC", "CLMSMAC", 1));
                assertThat(new Library.Matcher().require(cu, null).getReferences())
                  .extracting(Library.Reference::getName, Library.Reference::getPath,
                    Library.Reference::getDdName, Library.Reference::getLine)
                  .containsExactly(tuple("CLMSAS", null, "CLMSAS", 3));
                assertThat(new FileReference.Matcher().require(cu, null).getReferences())
                  .extracting(FileReference.Reference::getKind, FileReference.Reference::getName,
                    FileReference.Reference::getDdName, FileReference.Reference::getLine)
                  .containsExactly(tuple(FileReference.Kind.INFILE, "CLMEXTR", "CLMEXTR", 6));
            })
          )
        );
    }

    /**
     * A program that names its own data set is naming one, and there is no DD for a job to allocate.
     * The two shapes are the same statement, so only the quoted literal tells them apart.
     */
    @Test
    void tellsADataSetTheProgramNamesFromOneTheStepAllocates() {
        rewriteRun(
          text(
            """
              LIBNAME CLMSAS V9 'CLM.PROD.SASLIB';
              FILENAME CLMRPT 'CLM.PROD.CLMRPT';
              DATA _NULL_;
                 FILE CLMRPT;
              RUN;
              """,
            spec -> spec.path("sas/CLMSTAT.sas").afterRecipe(cu -> {
                assertThat(new Library.Matcher().require(cu, null).getReferences())
                  .extracting(Library.Reference::getName, Library.Reference::getEngine,
                    Library.Reference::getPath, Library.Reference::getDdName)
                  .containsExactly(tuple("CLMSAS", "V9", "CLM.PROD.SASLIB", null));
                assertThat(new FileReference.Matcher().require(cu, null).getReferences())
                  .extracting(FileReference.Reference::getKind, FileReference.Reference::getName,
                    FileReference.Reference::getPath, FileReference.Reference::getDdName)
                  .containsExactly(
                    tuple(FileReference.Kind.FILENAME, "CLMRPT", "CLM.PROD.CLMRPT", null),
                    tuple(FileReference.Kind.FILE, "CLMRPT", null, "CLMRPT"));
            })
          )
        );
    }

    /**
     * INTERLINKS 21.3. A {@code PROC} names a libref two ways, and both are DD names where no
     * {@code LIBNAME} of the member gave the libref a data set: {@code LIBRARY=} for the library a
     * stored format outlives the step in, and the first qualifier of whatever {@code DATA=},
     * {@code OUT=} or {@code BASE=} was given. An unqualified data set is in {@code WORK} and reaches
     * nothing.
     */
    @Test
    void readsTheLibrefsAProcNames() {
        rewriteRun(
          text(
            """
              PROC FORMAT LIBRARY=LIBRARY;
                 VALUE $CLMSTA 'O' = 'OPEN';
              RUN;

              PROC APPEND BASE=CLMSAS.CLMYTD DATA=CLMSAS.CLMDAY;
              RUN;

              PROC SORT DATA = CLMSAS.CLMDAY(KEEP=AMTRSV) OUT=BIGRSV;
              RUN;
              """,
            spec -> spec.path("sas/CLMSMAC.sas").afterRecipe(cu ->
              assertThat(new Library.Matcher().require(cu, null).getReferences())
                .extracting(Library.Reference::getKind, Library.Reference::getName,
                  Library.Reference::getMember, Library.Reference::getDdName,
                  Library.Reference::getLine)
                .containsExactly(
                  tuple(Library.Kind.LIBRARY, "LIBRARY", null, "LIBRARY", 1),
                  tuple(Library.Kind.BASE, "CLMSAS", "CLMYTD", "CLMSAS", 5),
                  tuple(Library.Kind.DATA, "CLMSAS", "CLMDAY", "CLMSAS", 5),
                  tuple(Library.Kind.DATA, "CLMSAS", "CLMDAY", "CLMSAS", 8)))
          )
        );
    }

    /**
     * A libref the member itself gave a data set to needs nothing of the step, so a {@code PROC} that
     * uses it reaches no DD and is not reported twice.
     */
    @Test
    void leavesOutALibrefALibnameAlreadyResolved() {
        rewriteRun(
          text(
            """
              LIBNAME CLMSAS V9 'CLM.PROD.SASLIB';
              PROC PRINT DATA=CLMSAS.CLMDAY NOOBS LABEL;
              RUN;
              """,
            spec -> spec.path("sas/CLMSPOL.sas").afterRecipe(cu ->
              assertThat(new Library.Matcher().require(cu, null).getReferences())
                .extracting(Library.Reference::getKind, Library.Reference::getName,
                  Library.Reference::getPath)
                .containsExactly(tuple(Library.Kind.LIBNAME, "CLMSAS", "CLM.PROD.SASLIB")))
          )
        );
    }

    /**
     * INTERLINKS 21.2. A name after a {@code %} is either a macro somebody wrote or a statement of
     * the macro language, and nothing in the source tells them apart — so the statements are listed
     * and everything else is an invocation. Read the other way, every {@code %LET} reports a call.
     */
    @Test
    void tellsAMacroInvocationFromAStatementOfTheMacroLanguage() {
        rewriteRun(
          text(
            """
              %MACRO CLMTITL(SUBTTL);
                 TITLE1 "CASCADE MUTUAL - CLAIMS - &SYSDATE";
                 TITLE2 "&SUBTTL";
              %MEND CLMTITL;

              %LET DB2SSN = &SYSPARM;

              %CLMTITL(RESERVE CHANGE BY CLAIM TYPE);
              """,
            spec -> spec.path("sas/CLMSMAC.sas").afterRecipe(cu -> {
                assertThat(new MacroDefinition.Matcher().require(cu, null).getMacros())
                  .extracting(MacroDefinition.Macro::getName, MacroDefinition.Macro::getParameters,
                    MacroDefinition.Macro::getLine)
                  .containsExactly(tuple("CLMTITL", List.of("SUBTTL"), 1));

                List<MacroCall.Reference> macros =
                  new MacroCall.Matcher().require(cu, null).getReferences();
                assertThat(macros).extracting(MacroCall.Reference::getName,
                    MacroCall.Reference::getArguments, MacroCall.Reference::getLine)
                  .containsExactly(
                    tuple("CLMTITL", List.of("RESERVE CHANGE BY CLAIM TYPE"), 8));
                // Whether a macro is one the estate keeps is the library's answer: SAS finds most of
                // them in an autocall library nobody checks in.
                assertThat(macros.get(0).isDefinedBy(List.of("CLMSMAC"))).isFalse();
                assertThat(macros.get(0).isDefinedBy(List.of("CLMTITL"))).isTrue();
            })
          )
        );
    }

    /**
     * A member reached by a quoted path is not reached by a DD, and a member name with no library in
     * front of it is left to the {@code SASAUTOS} search order.
     */
    @Test
    void readsAnIncludeWrittenWithoutALibrary() {
        rewriteRun(
          text(
            """
              %INCLUDE CLMSMAC;
              %INCLUDE '/u/clm/clmsmac.sas';
              """,
            spec -> spec.path("sas/CLMSAUD.sas").afterRecipe(cu ->
              assertThat(new Include.Matcher().require(cu, null).getReferences())
                .extracting(Include.Reference::getDdName, Include.Reference::getMember)
                .containsExactly(tuple(null, "CLMSMAC"), tuple(null, null)))
          )
        );
    }
}
