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
package org.openrewrite.textmember.trait;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.textmember.tree.TextMember;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.textmember.Assertions.document;

class RunBookTest implements RewriteTest {

    @Test
    void readsTheJobADocjobDocuments() {
        rewriteRun(
          document(
            """
              DOCJOB   CLMJ010                                CASCADE MUTUAL - CLAIMS
              ========================================================================
              JOB          CLMJ010                    LIBRARY  CLM.PROD.JCL
              SCHEDULE     CONTROL-M TABLE CLMNIGHT, CALENDAR CLMWORK, FROM 22:00
              STEPS
                EXTRACT    PROC CLMBATCH  PGM CLMB010
              """,
            spec -> spec.afterRecipe(cu -> {
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
     * A {@code DOCFICH} is named for the last qualifier of its data set where that is a name and given
     * one where it is a word, so the {@code FILE} line and not the member name is what a data set
     * reference resolves through.
     */
    @Test
    void readsTheDataSetADocfichDocumentsRatherThanItsOwnName() {
        rewriteRun(
          document(
            """
              DOCFICH  EXTSORT                                CASCADE MUTUAL - CLAIMS
              ========================================================================
              FILE         CLM.PROD.EXTRACT.SORTED    APPLICATION  CLAIMS
              LAYOUT       COPYBOOK CLMEXTR
              """,
            spec -> spec.afterRecipe(cu -> {
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
          document(
            """
              DOCPGM   CLMB010
              PROGRAM      CLMB010                    LIBRARY  CLM.PROD.COBOL
              """,
            spec -> spec.path("doc/CLMB010.docjob")
              .afterRecipe(cu -> assertThat(book(cu).getShape()).isEqualTo(RunBook.Shape.DOCPGM)))
        );
        rewriteRun(
          document(
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
          document(
            """
              DOCJOB   CLMJ060
              RUNS AFTER   CLMJ030 (CONDITION CLMNIGHT_CLMJ030_OK) AND THE /DBR BY
                           IMS OPERATIONS (IMSP-DBR-CLMDBD01).
              OUTPUT
                CLMDB01    CLM.PROD.IMS.CLMDB01.LOG(+1)    IMS LOG, TAPE, GDG
              LAST CHANGE  05/18/1994 RJM  Written with the IMS load
              """,
            spec -> spec.afterRecipe(cu -> {
                List<String> names = texts(book(cu).getMentions());
                assertThat(names).contains("CLMJ030", "CLMNIGHT_CLMJ030_OK", "IMSP-DBR-CLMDBD01",
                  "CLMDB01", "CLM.PROD.IMS.CLMDB01.LOG");
                // Prose, a date and a line of rules are not names.
                assertThat(names).doesNotContain("Written", "OPERATIONS", "05", "1994");
            }))
        );
    }

    private static RunBook book(TextMember.CompilationUnit cu) {
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
