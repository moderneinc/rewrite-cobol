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
package org.openrewrite.ims;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ImsLineReaderTest {

    @Test
    void classifiesLines() {
        String read = ImsLineReader.readLines(
          "* a comment\n" +
          "DSG001 DATASET DD1=CLMDB01\n" +
          "         SEGM  NAME=CLMROOT,PARENT=0\n");
        assertThat(read).isEqualTo(
          "^^COMMENT^^* a comment\n" +
          "^^IMS_NAMED^^DSG001 DATASET DD1=CLMDB01\n" +
          "^^IMS^^         SEGM  NAME=CLMROOT,PARENT=0\n");
    }

    /**
     * A line continues the one above it because of column 72, and nothing in its own text says so.
     * One column short and the operands become an operation of their own, which still prints back.
     */
    @Test
    void continuationComesFromColumnSeventyTwo() {
        String continued =
          "         DBD   NAME=CLMDBD01,ACCESS=(HDAM,VSAM)," + blanks(23) + "X\n" +
          "               RMNAME=(DFSHDC40,5,500,824)\n";
        assertThat(ImsLineReader.readLines(continued)).contains("^^IMS_CONT^^               RMNAME=");

        String oneColumnShort =
          "         DBD   NAME=CLMDBD01,ACCESS=(HDAM,VSAM)," + blanks(22) + "X\n" +
          "               RMNAME=(DFSHDC40,5,500,824)\n";
        assertThat(ImsLineReader.readLines(oneColumnShort)).contains("^^IMS^^               RMNAME=");
    }

    @Test
    void sequenceAreaIsSplitOffOnlyWhenItHoldsSomething() {
        String padded = "         SEGM  NAME=CLMROOT" + blanks(53) + "\n";
        assertThat(ImsLineReader.readLines(padded)).doesNotContain("^^CA_START^^");

        String numbered = "         SEGM  NAME=CLMROOT" + blanks(45) + "CLMDBD01\n";
        assertThat(ImsLineReader.readLines(numbered)).isEqualTo(
          "^^IMS^^         SEGM  NAME=CLMROOT" + blanks(45) + "^^CA_START^^CLMDBD01\n");
    }

    /**
     * What a gen member is, which is the only thing that tells a DBD kept as {@code .asm} from a
     * program kept beside it. CardDemo opens both of its DBDs with a listing control, so the answer
     * is not simply the first statement.
     */
    @Test
    void theFirstOperationLooksPastTheListingControls() {
        assertThat(ImsLineReader.firstOperation(
          "***********************\n" +
          "         TITLE   'ASSEMBLE OF DBDNAME=DBPAUTP0 '\n" +
          "       DBD     NAME=DBPAUTP0,ACCESS=(HIDAM,VSAM)\n")).isEqualTo("DBD");

        assertThat(ImsLineReader.firstOperation(
          "         COPY  CLMREGS\n" +
          "CLMROOTD DSECT\n")).isEqualTo("COPY");

        assertThat(ImsLineReader.firstOperation("* nothing but a comment\n")).isNull();
    }

    private static String blanks(int n) {
        StringBuilder blanks = new StringBuilder();
        for (int i = 0; i < n; i++) {
            blanks.append(' ');
        }
        return blanks.toString();
    }
}
