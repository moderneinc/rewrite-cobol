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
package org.openrewrite.sas.trait;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RewriteTest;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.openrewrite.sas.Assertions.sas;

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
          sas(
            """
              %INCLUDE SASSRC(CLMSMAC);

              LIBNAME CLMSAS;

              DATA CLMSAS.CLMDAY;
                 INFILE CLMEXTR LRECL=200 RECFM=FB;
              RUN;
              """,
            spec -> spec.afterRecipe(cu -> {
                assertThat(new Include.Matcher().lower(cu).collect(Collectors.toList()))
                  .extracting(Include::getDdName, Include::getMember, Include::getLine)
                  .containsExactly(tuple("SASSRC", "CLMSMAC", 1));
                assertThat(new Library.Matcher().lower(cu).collect(Collectors.toList()))
                  .extracting(Library::getName, Library::getPath, Library::getDdName, Library::getLine)
                  .containsExactly(tuple("CLMSAS", null, "CLMSAS", 3));
                assertThat(new FileReference.Matcher().lower(cu).collect(Collectors.toList()))
                  .extracting(FileReference::getKind, FileReference::getName,
                    FileReference::getDdName, FileReference::getLine)
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
          sas(
            """
              LIBNAME CLMSAS V9 'CLM.PROD.SASLIB';
              FILENAME CLMRPT 'CLM.PROD.CLMRPT';
              DATA _NULL_;
                 FILE CLMRPT;
              RUN;
              """,
            spec -> spec.afterRecipe(cu -> {
                assertThat(new Library.Matcher().lower(cu).collect(Collectors.toList()))
                  .extracting(Library::getName, Library::getEngine, Library::getPath, Library::getDdName)
                  .containsExactly(tuple("CLMSAS", "V9", "CLM.PROD.SASLIB", null));
                assertThat(new FileReference.Matcher().lower(cu).collect(Collectors.toList()))
                  .extracting(FileReference::getKind, FileReference::getName,
                    FileReference::getPath, FileReference::getDdName)
                  .containsExactly(
                    tuple(FileReference.Kind.FILENAME, "CLMRPT", "CLM.PROD.CLMRPT", null),
                    tuple(FileReference.Kind.FILE, "CLMRPT", null, "CLMRPT"));
            })
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
          sas(
            """
              %MACRO CLMTITL(SUBTTL);
                 TITLE1 "CASCADE MUTUAL - CLAIMS - &SYSDATE";
                 TITLE2 "&SUBTTL";
              %MEND CLMTITL;

              %LET DB2SSN = &SYSPARM;

              %CLMTITL(RESERVE CHANGE BY CLAIM TYPE);
              """,
            spec -> spec.afterRecipe(cu -> {
                assertThat(new MacroDefinition.Matcher().lower(cu).collect(Collectors.toList()))
                  .extracting(MacroDefinition::getName, MacroDefinition::getParameters,
                    MacroDefinition::getLine)
                  .containsExactly(tuple("CLMTITL", List.of("SUBTTL"), 1));

                List<MacroCall> macros = new MacroCall.Matcher().lower(cu).collect(Collectors.toList());
                assertThat(macros).extracting(MacroCall::getName, MacroCall::getArguments,
                    MacroCall::getLine)
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
          sas(
            """
              %INCLUDE CLMSMAC;
              %INCLUDE '/u/clm/clmsmac.sas';
              """,
            spec -> spec.afterRecipe(cu ->
              assertThat(new Include.Matcher().lower(cu).collect(Collectors.toList()))
                .extracting(Include::getDdName, Include::getMember)
                .containsExactly(tuple(null, "CLMSMAC"), tuple(null, null)))
          )
        );
    }
}
