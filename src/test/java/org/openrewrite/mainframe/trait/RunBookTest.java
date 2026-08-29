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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.openrewrite.test.SourceSpecs.text;

class RunBookTest implements RewriteTest {

    @Test
    void readsTheJobADocjobDocuments() {
        rewriteRun(
          text(
            """
              DOCJOB   CLMJ010                                CASCADE MUTUAL - CLAIMS
              ========================================================================
              JOB          CLMJ010                    LIBRARY  CLM.PROD.JCL
              SCHEDULE     CONTROL-M TABLE CLMNIGHT, CALENDAR CLMWORK, FROM 22:00
              STEPS
                EXTRACT    PROC CLMBATCH  PGM CLMB010
              """,
            spec -> spec.path("doc/CLMJ010.docjob").afterRecipe(cu -> {
                RunBook book = book(cu);
                assertThat(book.getShape()).isEqualTo(RunBook.Shape.DOCJOB);
                assertThat(book.getSubject()).isNotNull()
                  .satisfies(subject -> {
                      assertThat(subject.getText()).isEqualTo("CLMJ010");
                      assertThat(subject.getLine()).isEqualTo(3);
                  });
                assertThat(texts(book.getMentions())).contains("CLMNIGHT", "CLMWORK", "CLMBATCH",
                  "CLMB010", "CLM.PROD.JCL");
            }))
        );
    }

    /**
     * The labelled fields, which is where the member says a name is a component. A label stands in
     * the first thirteen columns and its value in the rest of the line and every line indented under
     * it; the header block writes a second field from column forty one, so the job and the library it
     * is in are two fields and not one field with a stray word in it.
     */
    @Test
    void readsTheLabelledFieldsAndNotTheProseAsOne() {
        rewriteRun(
          text(
            """
              DOCJOB   CLMJ010                                CASCADE MUTUAL - CLAIMS
              ========================================================================
              JOB          CLMJ010                    LIBRARY  CLM.PROD.JCL
              ------------------------------------------------------------------------
              DESCRIPTION
                FIRST JOB OF THE NIGHTLY STREAM.  RUN IT BY HAND WITH CLMFXTR.
              STEPS
                EXTRACT    PROC CLMBATCH  PGM CLMB010
              OUTPUT
                CLMEXTR    CLM.PROD.EXTRACT(+1)            GDG, LAYOUT CLMEXTR
              SEE ALSO     DOCPGM CLMB010   DOCFICH CLMMAST
              """,
            spec -> spec.path("doc/CLMJ010.docjob").afterRecipe(cu -> {
                List<RunBook.Field> fields = book(cu).getFields();
                assertThat(fields).extracting(RunBook.Field::getLabel, RunBook.Field::getLine)
                  .containsExactly(
                    tuple("JOB", 3), tuple("LIBRARY", 3), tuple("DESCRIPTION", 5), tuple("STEPS", 7),
                    tuple("OUTPUT", 9), tuple("SEE ALSO", 11));
                assertThat(fields).extracting(field -> texts(field.getNames()))
                  .containsExactly(
                    List.of("CLMJ010"),
                    List.of("CLM.PROD.JCL"),
                    List.of("FIRST", "JOB", "OF", "THE", "NIGHTLY", "STREAM", "RUN", "IT", "BY",
                      "HAND", "WITH", "CLMFXTR"),
                    List.of("EXTRACT", "PROC", "CLMBATCH", "PGM", "CLMB010"),
                    // The DD and the copybook the record is laid out by, both written CLMEXTR.
                    List.of("CLMEXTR", "CLM.PROD.EXTRACT", "GDG", "LAYOUT", "CLMEXTR"),
                    List.of("DOCPGM", "CLMB010", "DOCFICH", "CLMMAST"));
                // The value as written, which a field laid out in columns is read from.
                assertThat(fields.get(3).getLines())
                  .containsExactly("  EXTRACT    PROC CLMBATCH  PGM CLMB010");
            }))
        );
    }

    /**
     * A {@code DOCFICH} is named for the last qualifier of its data set where that is a name and given
     * one where it is a word, so the {@code FILE} line and not the member name is what a data set
     * reference resolves through.
     */
    @Test
    void readsTheDataSetADocfichDocumentsRatherThanItsOwnName() {
        rewriteRun(
          text(
            """
              DOCFICH  EXTSORT                                CASCADE MUTUAL - CLAIMS
              ========================================================================
              FILE         CLM.PROD.EXTRACT.SORTED    APPLICATION  CLAIMS
              LAYOUT       COPYBOOK CLMEXTR
              """,
            spec -> spec.path("doc/EXTSORT.docfich").afterRecipe(cu -> {
                RunBook book = book(cu);
                assertThat(book.getShape()).isEqualTo(RunBook.Shape.DOCFICH);
                assertThat(book.getSubject()).isNotNull()
                  .satisfies(subject -> assertThat(subject.getText())
                    .isEqualTo("CLM.PROD.EXTRACT.SORTED"));
            }))
        );
    }

    /**
     * The first word is where the mainframe keeps the shape, since a member's name says only what it
     * is about. A member that opens with something else is typed by the extension it reached the
     * repository with.
     */
    @Test
    void readsTheShapeFromTheFirstWordAndFallsBackToTheExtension() {
        rewriteRun(
          text(
            """
              DOCPGM   CLMB010
              PROGRAM      CLMB010                    LIBRARY  CLM.PROD.COBOL
              """,
            spec -> spec.path("doc/CLMB010.docjob")
              .afterRecipe(cu -> assertThat(book(cu).getShape()).isEqualTo(RunBook.Shape.DOCPGM)))
        );
        rewriteRun(
          text(
            """
              CLAIMS APPLICATION
              APPLICATION  CLAIMS
              """,
            spec -> spec.path("doc/CLAIMS.docappl")
              .afterRecipe(cu -> {
                  assertThat(book(cu).getShape()).isEqualTo(RunBook.Shape.DOCAPPL);
                  assertThat(book(cu).getSubject()).isNotNull()
                    .satisfies(subject -> assertThat(subject.getText()).isEqualTo("CLAIMS"));
              }))
        );
    }

    /**
     * Every name a run book mentions, which is what a name index joins on. It over-answers on purpose,
     * and what it will not answer with is prose: a word holding a lower case letter, and a word longer
     * than a member name may be.
     */
    @Test
    void findsEveryNameARunBookMentions() {
        rewriteRun(
          text(
            """
              DOCJOB   CLMJ060
              RUNS AFTER   CLMJ030 (CONDITION CLMNIGHT_CLMJ030_OK) AND THE /DBR BY
                           IMS OPERATIONS (IMSP-DBR-CLMDBD01).
              OUTPUT
                CLMDB01    CLM.PROD.IMS.CLMDB01.LOG(+1)    IMS LOG, TAPE, GDG
              LAST CHANGE  05/18/1994 RJM  Written with the IMS load
              """,
            spec -> spec.path("doc/CLMJ060.docjob").afterRecipe(cu -> {
                List<String> names = texts(book(cu).getMentions());
                assertThat(names).contains("CLMJ030", "CLMNIGHT_CLMJ030_OK", "IMSP-DBR-CLMDBD01",
                  "CLMDB01", "CLM.PROD.IMS.CLMDB01.LOG");
                // Prose, a date and a line of rules are not names.
                assertThat(names).doesNotContain("Written", "OPERATIONS", "05", "1994");
            }))
        );
    }

    private static RunBook book(PlainText cu) {
        return new RunBook.Matcher().require(cu, null);
    }

    private static List<String> texts(List<Mention> names) {
        List<String> texts = new ArrayList<>(names.size());
        for (Mention name : names) {
            texts.add(name.getText());
        }
        return texts;
    }
}
