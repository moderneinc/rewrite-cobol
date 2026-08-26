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
package org.openrewrite.ims.trait;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Parser;
import org.openrewrite.SourceFile;
import org.openrewrite.ims.ImsParser;
import org.openrewrite.ims.tree.Ims;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * What a stage 1 deck ties together, which is the only place an IMS transaction code is written down.
 * <p>
 * These parse directly rather than through {@code Assertions.ims}: the operand field and column 72
 * are where the meaning is, and the test framework trims the common indentation off a source block.
 */
class ApplicationTest {

    private static final String STAGE_1 =
      "*  CLMGEN01 - THE CLAIMS APPLICATION'S CONTRIBUTION TO THE STAGE 1 DECK\n" +
      "         APPLCTN PSB=CLMPSB02,PGMTYPE=(TP,,2),SCHDTYP=PARALLEL\n" +
      "         TRANSACT CODE=CLMINQ,MODE=SNGL,SPA=(150,),EDIT=(,ULC)," + blanks(8) + "X\n" +
      "               MSGTYPE=(SNGLSEG,RESPONSE,1)\n" +
      "         TRANSACT CODE=CLMDTL,MODE=SNGL,SPA=(150,)\n" +
      "*\n" +
      "         APPLCTN PSB=CLMPSB01,PGMTYPE=BATCH\n" +
      "*\n" +
      "         DATABASE DBD=CLMDBD01,ACCESS=UP\n" +
      "         DATABASE DBD=CLMDBX01,ACCESS=UP\n" +
      "         DATABASE DBD=CLMDBD03,ACCESS=RD\n";

    @Test
    void theApplicationsAndTheirTransactions() {
        Ims.CompilationUnit deck = parse(STAGE_1);

        List<Application> applications = new Application.Matcher().lower(deck).collect(Collectors.toList());
        assertThat(applications).extracting(Application::getPsbName, Application::getProgramType,
            Application::isMessageDriven, Application::getLine)
          .containsExactly(
            tuple("CLMPSB02", "TP", true, 2),
            tuple("CLMPSB01", "BATCH", false, 7));

        // Both transactions are under the one APPLCTN, so both reach the one PSB and the one program
        // answers them, telling them apart by the code it reads out of the message.
        assertThat(applications.get(0).getTransactions())
          .extracting(Transaction::getCode, Transaction::getScratchpadSize, Transaction::getLine)
          .containsExactly(
            tuple("CLMINQ", 150, 3),
            tuple("CLMDTL", 150, 5));

        // A batch application has none: its job names the PSB itself.
        assertThat(applications.get(1).getTransactions()).isEmpty();
    }

    @Test
    void aTransactionKnowsItsApplication() {
        List<Transaction> transactions = new Transaction.Matcher().lower(parse(STAGE_1))
          .collect(Collectors.toList());

        assertThat(transactions).hasSize(2);
        assertThat(transactions.get(1).getApplication()).isNotNull()
          .extracting(Application::getPsbName).isEqualTo("CLMPSB02");
    }

    /**
     * A database missing from here is one the online system cannot open, whatever its DBD says.
     */
    @Test
    void theDatabasesTheControlRegionIsToldAbout() {
        assertThat(new DatabaseAccess.Matcher().lower(parse(STAGE_1)).collect(Collectors.toList()))
          .extracting(DatabaseAccess::getName, DatabaseAccess::getAccess, DatabaseAccess::getLine)
          .containsExactly(
            tuple("CLMDBD01", "UP", 9),
            tuple("CLMDBX01", "UP", 10),
            tuple("CLMDBD03", "RD", 11));
    }

    /**
     * The databases are written after the applications, so a deck read as one flat run would hand the
     * last application whatever followed it.
     */
    @Test
    void aDatabaseEndsTheApplicationAboveIt() {
        Ims.CompilationUnit deck = parse(
          "         APPLCTN PSB=CLMPSB01,PGMTYPE=BATCH\n" +
          "         DATABASE DBD=CLMDBD01,ACCESS=UP\n" +
          "         TRANSACT CODE=CLMINQ,SPA=(150,)\n");

        assertThat(new Application.Matcher().lower(deck).collect(Collectors.toList()))
          .singleElement().satisfies(application ->
            assertThat(application.getTransactions()).isEmpty());
    }

    private static Ims.CompilationUnit parse(String source) {
        List<SourceFile> parsed = ImsParser.builder().build()
          .parseInputs(singletonList(input(source)), null, new InMemoryExecutionContext())
          .collect(Collectors.toList());
        assertThat(parsed).hasSize(1);
        assertThat(parsed.get(0)).isInstanceOf(Ims.CompilationUnit.class);
        assertThat(parsed.get(0).printAll()).isEqualTo(source);
        return (Ims.CompilationUnit) parsed.get(0);
    }

    private static Parser.Input input(String source) {
        return new Parser.Input(Paths.get("CLMGEN01.gen"),
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
