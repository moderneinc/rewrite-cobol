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
import org.openrewrite.estate.Members;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.text.PlainText;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.openrewrite.jcl.Assertions.jcl;

class InstreamSasTest implements RewriteTest {

    /**
     * INTERLINKS 8.7. A job runs a SAS program by naming a member on {@code SYSIN} or by writing one
     * there, and the program written there has no member name at all. It is read detached from the
     * job: two source files cannot share a path, and the program is a fact about the estate rather
     * than about the job.
     */
    @DocumentExample
    @Test
    void readsTheProgramAJobWritesOnItsSysin() {
        rewriteRun(
          jcl(
            """
              //CLMSTAT  JOB (CLM,PROD),'CLAIM STATISTICS'
              //TOP20    EXEC CLMSAS
              //SAS.SYSIN    DD *
              %INCLUDE SASSRC(CLMSMAC);

              PROC SORT DATA=CLMSAS.CLMDAY OUT=BIGRSV;
                 BY DESCENDING AMTRSV;
              RUN;
              /*
              """,
            spec -> spec.path("jcl/CLMSTAT.jcl").afterRecipe(cu -> {
                List<InstreamSas> streams = new InstreamSas.Matcher().lower(cu).collect(Collectors.toList());
                assertThat(streams).extracting(InstreamSas::getName, InstreamSas::getLine)
                  .containsExactly(tuple("SYSIN", 4));

                PlainText program = streams.get(0).parse();
                // The text starts at the first card, so the program's own lines are the job's.
                assertThat(streams.get(0).getText()).isEqualTo(program.printAll());
                // The program has no member name of its own, so it is given the job's under .sas —
                // which is what says it is SAS to every trait that reads it.
                assertThat(program.getSourcePath()).hasToString("jcl/CLMSTAT.sas");
                assertThat(new Include.Matcher().require(program, null).getReferences())
                  .extracting(Include.Reference::getMember, Include.Reference::getLine)
                  .containsExactly(tuple("CLMSMAC", 1));
            })
          )
        );
    }

    /**
     * The rule the matcher asks for, which anything else holding a stream of text and no path to
     * type it from asks for by the same name: half a program is not one.
     */
    @Test
    void aStreamIsSasOnlyWhereItOpensAStepAndEndsOne() {
        assertThat(Members.isSasProgram("PROC PRINT DATA=CLMSAS.CLMDAY;\nRUN;\n")).isTrue();
        assertThat(Members.isSasProgram("PROC PRINT DATA=CLMSAS.CLMDAY;\n")).isFalse();
        assertThat(Members.isSasProgram("  SORT FIELDS=(53,4,CH,A)\n")).isFalse();
    }

    /**
     * A {@code SYSIN} carries sort cards, IDCAMS commands and TSO input under the same DD name, so a
     * stream is SAS only if it says so — a step opener and the statement that ends a step.
     */
    @Test
    void claimsNoStreamThatIsNotSas() {
        rewriteRun(
          jcl(
            """
              //CLMJ020  JOB (CLM,PROD),'SORT THE EXTRACT'
              //SORT     EXEC PGM=SORT
              //SYSIN    DD *
                SORT FIELDS=(53,4,CH,A,1,10,CH,A)
                INCLUDE COND=(57,1,CH,NE,C'D')
              /*
              """,
            spec -> spec.afterRecipe(cu ->
              assertThat(new InstreamSas.Matcher().lower(cu).collect(Collectors.toList())).isEmpty())
          )
        );
    }
}
