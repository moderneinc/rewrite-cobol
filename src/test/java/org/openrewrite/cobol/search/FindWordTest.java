/*
 * Copyright 2025 the original author or authors.
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
package org.openrewrite.cobol.search;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.cobol.CobolTest;
import org.openrewrite.cobol.table.WordSearchResult;
import org.openrewrite.marker.Marker;
import org.openrewrite.marker.SearchResult;
import org.openrewrite.test.RecipeSpec;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.cobol.Assertions.cobol;

class FindWordTest extends CobolTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new FindWord("CM102M", true));
    }

    private final TreeVisitor<Tree, List<SearchResult>> visitor = new TreeVisitor<>() {
        @Override
        public <M extends Marker> M visitMarker(Marker marker, List<SearchResult> p) {
            if (marker instanceof SearchResult result) {
                p.add(result);
            }
            return super.visitMarker(marker, p);
        }
    };

	@DocumentExample @Test void cm102mExactMatch() {
        rewriteRun(
          spec -> spec.dataTable(WordSearchResult.Row.class, rows ->
              assertThat(rows).singleElement()
                  .extracting(WordSearchResult.Row::getMatchedWord)
                  .isEqualTo("CM102M")),
          cobol(
            """
              000100 IDENTIFICATION DIVISION.                                         CM1024.2
              000200 PROGRAM-ID.                                                      CM1024.2
              000300     CM102M.                                                      CM1024.2
              000400 AUTHOR.                                                          CM1024.2
              000500     FEDERAL COMPILER TESTING CENTER.                             CM1024.2
              000600 INSTALLATION.                                                    CM1024.2
              000700     GENERAL SERVICES ADMINISTRATION                              CM1024.2
              000800     AUTOMATED DATA AND TELECOMMUNICATION SERVICE.                CM1024.2
              000900     SOFTWARE DEVELOPMENT OFFICE.                                 CM1024.2
              001000     5203 LEESBURG PIKE  SUITE 1100                               CM1024.2
              001100     FALLS CHURCH VIRGINIA 22041.                                 CM1024.2
              001200                                                                  CM1024.2
              001300     PHONE   (703) 756-6153                                       CM1024.2
              001400                                                                  CM1024.2
              001500     " HIGH       ".                                              CM1024.2
              """,
            """
              000100 IDENTIFICATION DIVISION.                                         CM1024.2
              000200 PROGRAM-ID.                                                      CM1024.2
              000300     ~~>CM102M.                                                      CM1024.2
              000400 AUTHOR.                                                          CM1024.2
              000500     FEDERAL COMPILER TESTING CENTER.                             CM1024.2
              000600 INSTALLATION.                                                    CM1024.2
              000700     GENERAL SERVICES ADMINISTRATION                              CM1024.2
              000800     AUTOMATED DATA AND TELECOMMUNICATION SERVICE.                CM1024.2
              000900     SOFTWARE DEVELOPMENT OFFICE.                                 CM1024.2
              001000     5203 LEESBURG PIKE  SUITE 1100                               CM1024.2
              001100     FALLS CHURCH VIRGINIA 22041.                                 CM1024.2
              001200                                                                  CM1024.2
              001300     PHONE   (703) 756-6153                                       CM1024.2
              001400                                                                  CM1024.2
              001500     " HIGH       ".                                              CM1024.2
              """,
            spec -> spec.afterRecipe(cu -> {
                var searchResults = new ArrayList<SearchResult>(1);
                visitor.visit(cu, searchResults);
                assertThat(searchResults).hasSize(1);
            }))
        );
    }

    @Test
    void wordIsNotUsed() {
        rewriteRun(
          cobol(getNistResource("DB101A.CBL"))
        );
    }

    @Test
    void sm101A() {
        rewriteRun(
          spec -> spec.recipe(new FindWord("PROC-2", true))
              .dataTable(WordSearchResult.Row.class, rows ->
                  assertThat(rows).hasSize(3)
                      .extracting(WordSearchResult.Row::getMatchedWord)
                      .containsOnly("PROC-2")),
          cobol(
            getNistResource("SM101A.CBL"),
            sm101A, spec -> spec.afterRecipe(cu -> {
                var searchResults = new ArrayList<SearchResult>(3);
                visitor.visit(cu, searchResults);
                assertThat(searchResults).hasSize(3);
            }))
        );
    }

    @Test
    void cm102mPartialMatch() {
        rewriteRun(
          spec -> spec.recipe(new FindWord("cm.*", false))
              .dataTable(WordSearchResult.Row.class, rows ->
                  assertThat(rows).hasSize(39)),
          cobol(
            """
              000100 IDENTIFICATION DIVISION.                                         CM1024.2
              000200 PROGRAM-ID.                                                      CM1024.2
              000300     CM102M.                                                      CM1024.2
              003200 DATA DIVISION.                                                   CM1024.2
              027700 COMMUNICATION SECTION.                                           CM1024.2
              027800 CD  CM-OUTQUE-1 FOR OUTPUT                                       CM1024.2
              027900     DESTINATION COUNT IS ONE                                     CM1024.2
              028000     TEXT LENGTH IS MSG-LENGTH                                    CM1024.2
              028100     STATUS KEY IS STATUS-KEY                                     CM1024.2
              028200     ERROR KEY IS ERR-KEY                                         CM1024.2
              028300     SYMBOLIC DESTINATION IS SYM-DEST.                            CM1024.2
              028400 PROCEDURE    DIVISION.                                           CM1024.2
              028500 SECT-CM102M-0001 SECTION.                                        CM1024.2
              028600 CM102M-INIT.                                                     CM1024.2
              028700     OPEN     OUTPUT PRINT-FILE.                                  CM1024.2
              028800     MOVE "CM102M     " TO TEST-ID.                               CM1024.2
              028900     MOVE     TEST-ID TO ID-AGAIN.                                CM1024.2
              029000     MOVE    SPACE TO TEST-RESULTS.                               CM1024.2
              029100     PERFORM HEAD-ROUTINE.                                        CM1024.2
              029200     PERFORM COLUMN-NAMES-ROUTINE.                                CM1024.2
              029300     MOVE "MCS STATUS WORD" TO FEATURE.                           CM1024.2
              029400 DISAB-STATUS-TEST-01.                                            CM1024.2
              029500     MOVE "INITIAL DISABLE TO OUTPUT CD" TO RE-MARK.              CM1024.2
              029600     MOVE "9" TO STATUS-KEY ERR-KEY.                              CM1024.2
              029700     MOVE 1 TO ONE.                                               CM1024.2
              029800     MOVE                                                         CM1024.2
              029900     XXXXX032                                                     CM1024.2
              030000         TO SYM-DEST.                                             CM1024.2
              030100     DISABLE OUTPUT CM-OUTQUE-1 WITH KEY                          CM1024.2
              030200     XXXXX033.                                                    CM1024.2
              030300     MOVE "INFO" TO P-OR-F.                                       CM1024.2
              030400     MOVE STATUS-KEY TO COMPUTED-STATUS.                          CM1024.2
              030500     MOVE "/" TO SLASH.                                           CM1024.2
              030600     MOVE ERR-KEY TO COMPUTED-ERR-KEY.                            CM1024.2
              030700     MOVE "       INFO TEST FOR" TO CORRECT-A.                    CM1024.2
              030800     GO TO DISAB-STATUS-WRITE-01.                                 CM1024.2
              031400 DISAB-STATUS-TEST-02.                                            CM1024.2
              031500     MOVE "NO DESTINATION SPECIFIED" TO RE-MARK.                  CM1024.2
              031600     MOVE "9" TO STATUS-KEY ERR-KEY.                              CM1024.2
              031700     MOVE "GARBAGE" TO SYM-DEST.                                  CM1024.2
              031800     MOVE 1 TO ONE.                                               CM1024.2
              031900     DISABLE OUTPUT CM-OUTQUE-1 WITH KEY                          CM1024.2
              032000     XXXXX033.                                                    CM1024.2
              032100     IF STATUS-KEY IS EQUAL TO "20"                               CM1024.2
              032200         AND ERR-KEY IS EQUAL TO "1"                              CM1024.2
              032300         PERFORM PASS GO TO DISAB-STATUS-WRITE-02.                CM1024.2
              032400     MOVE 201 TO CORRECT-2SLASH1.                                 CM1024.2
              032500     MOVE STATUS-KEY TO COMPUTED-STATUS.                          CM1024.2
              032600     MOVE "/" TO SLASH.                                           CM1024.2
              032700     MOVE ERR-KEY TO COMPUTED-ERR-KEY.                            CM1024.2
              032800     PERFORM FAIL.                                                CM1024.2
              032900     GO TO DISAB-STATUS-WRITE-02.                                 CM1024.2
              033500 DISAB-STATUS-TEST-03.                                            CM1024.2
              033600     MOVE "INVALID PASSWORD USED" TO RE-MARK.                     CM1024.2
              033700     MOVE "9" TO STATUS-KEY ERR-KEY.                              CM1024.2
              033800     MOVE 1 TO ONE.                                               CM1024.2
              033900     MOVE                                                         CM1024.2
              034000     XXXXX032                                                     CM1024.2
              034100         TO SYM-DEST.                                             CM1024.2
              034200     DISABLE OUTPUT CM-OUTQUE-1 WITH KEY                          CM1024.2
              034300         "GARBAGE".                                               CM1024.2
              034400     IF STATUS-KEY IS EQUAL TO "40"                               CM1024.2
              034500         PERFORM PASS GO TO DISAB-STATUS-WRITE-03.                CM1024.2
              034600     MOVE 400 TO CORRECT-2SLASH1.                                 CM1024.2
              034700     MOVE STATUS-KEY TO COMPUTED-STATUS.                          CM1024.2
              034800     MOVE "/" TO SLASH.                                           CM1024.2
              034900     MOVE ERR-KEY TO COMPUTED-ERR-KEY.                            CM1024.2
              035000     PERFORM FAIL.                                                CM1024.2
              035100     GO TO DISAB-STATUS-WRITE-03.                                 CM1024.2
              035700 DISAB-STATUS-TEST-04.                                            CM1024.2
              035800     MOVE "INVALID DESTINATION COUNT (0)" TO RE-MARK.             CM1024.2
              035900     MOVE "9" TO STATUS-KEY ERR-KEY.                              CM1024.2
              036000     MOVE                                                         CM1024.2
              036100     XXXXX032                                                     CM1024.2
              036200         TO SYM-DEST.                                             CM1024.2
              036300     MOVE 0 TO ONE.                                               CM1024.2
              036400     DISABLE OUTPUT CM-OUTQUE-1 WITH KEY                          CM1024.2
              036500     XXXXX033.                                                    CM1024.2
              036600     IF STATUS-KEY IS EQUAL TO "30"                               CM1024.2
              036700         PERFORM PASS GO TO DISAB-STATUS-WRITE-04.                CM1024.2
              036800     MOVE 300 TO CORRECT-2SLASH1.                                 CM1024.2
              036900     MOVE STATUS-KEY TO COMPUTED-STATUS.                          CM1024.2
              037000     MOVE "/" TO SLASH.                                           CM1024.2
              037100     MOVE ERR-KEY TO COMPUTED-ERR-KEY.                            CM1024.2
              037200     PERFORM FAIL.                                                CM1024.2
              037300     GO TO DISAB-STATUS-WRITE-04.                                 CM1024.2
              037900 DISAB-STATUS-TEST-05.                                            CM1024.2
              038000     MOVE "COMBINATION ERROR" TO RE-MARK.                         CM1024.2
              038100     MOVE "9" TO STATUS-KEY ERR-KEY.                              CM1024.2
              038200     MOVE SPACES TO SYM-DEST.                                     CM1024.2
              038300     MOVE 0 TO ONE.                                               CM1024.2
              038400     DISABLE OUTPUT CM-OUTQUE-1 WITH KEY                          CM1024.2
              038500         "GARBAGE".                                               CM1024.2
              038600     MOVE "INFO" TO P-OR-F.                                       CM1024.2
              038700     MOVE STATUS-KEY TO COMPUTED-STATUS.                          CM1024.2
              038800     MOVE "/" TO SLASH.                                           CM1024.2
              038900     MOVE ERR-KEY TO COMPUTED-ERR-KEY.                            CM1024.2
              039000     GO TO DISAB-STATUS-WRITE-05.                                 CM1024.2
              039600 SEND-STATUS-TEST-01.                                             CM1024.2
              039700     MOVE "DESTINATION DISABLED" TO RE-MARK.                      CM1024.2
              039800     MOVE "CM102M- I AM THE FIRST MESSAGE IN QUEUE;" TO MSG-70.   CM1024.2
              039900     MOVE "9" TO STATUS-KEY ERR-KEY.                              CM1024.2
              040000     MOVE                                                         CM1024.2
              040100     XXXXX032                                                     CM1024.2
              040200         TO SYM-DEST.                                             CM1024.2
              040300     MOVE 1 TO ONE.                                               CM1024.2
              040400     MOVE 45 TO MSG-LENGTH.                                       CM1024.2
              040500     SEND CM-OUTQUE-1 FROM MSG-70 WITH EMI                        CM1024.2
              040600         AFTER ADVANCING PAGE.                                    CM1024.2
              040700     MOVE "THOU SHALT HAVE NO OTHER MESSAGES BEFORE ME." TO MSG-70CM1024.2
              040800     SEND CM-OUTQUE-1 FROM MSG-70 WITH EMI.                       CM1024.2
              040900     MOVE SPACES TO MSG-70.                                       CM1024.2
              041000     MOVE 1 TO MSG-LENGTH.                                        CM1024.2
              041100     SEND CM-OUTQUE-1 FROM MSG-70 WITH EGI.                       CM1024.2
              041200     IF STATUS-KEY IS EQUAL TO "10"                               CM1024.2
              041300         PERFORM PASS GO TO SEND-STATUS-WRITE-01.                 CM1024.2
              041400     MOVE 100 TO CORRECT-2SLASH1.                                 CM1024.2
              041500     MOVE STATUS-KEY TO COMPUTED-STATUS.                          CM1024.2
              041600     MOVE "/" TO SLASH.                                           CM1024.2
              041700     MOVE ERR-KEY TO COMPUTED-ERR-KEY.                            CM1024.2
              041800     PERFORM FAIL.                                                CM1024.2
              041900     GO TO SEND-STATUS-WRITE-01.                                  CM1024.2
              042500 SEND-STATUS-TEST-02.                                             CM1024.2
              042600     MOVE "COMBINATION ERROR" TO RE-MARK.                         CM1024.2
              042700     MOVE SPACES TO SYM-DEST.                                     CM1024.2
              042800     MOVE 0 TO ONE.                                               CM1024.2
              042900     MOVE 100 TO MSG-LENGTH.                                      CM1024.2
              043000     MOVE "S-02" TO TEST-IND.                                     CM1024.2
              043100     SEND CM-OUTQUE-1 FROM ERR-MSG WITH EMI.                      CM1024.2
              043200     MOVE "INFO" TO P-OR-F.                                       CM1024.2
              043300     MOVE STATUS-KEY TO COMPUTED-STATUS.                          CM1024.2
              043400     MOVE "/" TO SLASH.                                           CM1024.2
              043500     MOVE ERR-KEY TO COMPUTED-ERR-KEY.                            CM1024.2
              043600     GO TO SEND-STATUS-WRITE-02.                                  CM1024.2
              044200 ENABL-STATUS-TEST-01.                                            CM1024.2
              044300     MOVE "DESTINATION NOT SPECIFIED" TO RE-MARK.                 CM1024.2
              044400     MOVE SPACES TO SYM-DEST.                                     CM1024.2
              044500     MOVE "9" TO STATUS-KEY ERR-KEY.                              CM1024.2
              044600     MOVE 1 TO ONE.                                               CM1024.2
              044700     ENABLE OUTPUT CM-OUTQUE-1 WITH KEY                           CM1024.2
              044800     XXXXX033.                                                    CM1024.2
              044900     IF STATUS-KEY IS EQUAL TO "20"                               CM1024.2
              045000         AND ERR-KEY IS EQUAL TO "1"                              CM1024.2
              045100         PERFORM PASS GO TO ENABL-STATUS-WRITE-01.                CM1024.2
              045200     MOVE 201 TO CORRECT-2SLASH1.                                 CM1024.2
              045300     MOVE STATUS-KEY TO COMPUTED-STATUS.                          CM1024.2
              045400     MOVE "/" TO SLASH.                                           CM1024.2
              045500     MOVE ERR-KEY TO COMPUTED-ERR-KEY.                            CM1024.2
              045600     PERFORM FAIL.                                                CM1024.2
              045700     GO TO ENABL-STATUS-WRITE-01.                                 CM1024.2
              046300 ENABL-STATUS-TEST-02.                                            CM1024.2
              046400     MOVE "INVALID DESTINATION COUNT (0)" TO RE-MARK.             CM1024.2
              046500     MOVE                                                         CM1024.2
              046600     XXXXX032                                                     CM1024.2
              046700         TO SYM-DEST.                                             CM1024.2
              046800     MOVE "9" TO STATUS-KEY ERR-KEY.                              CM1024.2
              046900     MOVE 0 TO ONE.                                               CM1024.2
              047000     ENABLE OUTPUT CM-OUTQUE-1 WITH KEY                           CM1024.2
              047100     XXXXX033.                                                    CM1024.2
              047200     IF STATUS-KEY IS EQUAL TO "30"                               CM1024.2
              047300         PERFORM PASS GO TO ENABL-STATUS-WRITE-02.                CM1024.2
              047400     MOVE 300 TO CORRECT-2SLASH1.                                 CM1024.2
              047500     MOVE STATUS-KEY TO COMPUTED-STATUS.                          CM1024.2
              047600     MOVE "/" TO SLASH.                                           CM1024.2
              047700     MOVE ERR-KEY TO COMPUTED-ERR-KEY.                            CM1024.2
              047800     PERFORM FAIL.                                                CM1024.2
              047900     GO TO ENABL-STATUS-WRITE-02.                                 CM1024.2
              048500 ENABL-STATUS-TEST-03.                                            CM1024.2
              048600     MOVE "INVALID PASSWORD USED" TO RE-MARK.                     CM1024.2
              048700     MOVE                                                         CM1024.2
              048800     XXXXX032                                                     CM1024.2
              048900         TO SYM-DEST.                                             CM1024.2
              049000     MOVE "9" TO STATUS-KEY ERR-KEY.                              CM1024.2
              049100     MOVE 1 TO ONE.                                               CM1024.2
              049200     ENABLE OUTPUT CM-OUTQUE-1 WITH KEY                           CM1024.2
              049300         "GARBAGE".                                               CM1024.2
              049400     IF STATUS-KEY IS EQUAL TO "40"                               CM1024.2
              049500         PERFORM PASS GO TO ENABL-STATUS-WRITE-03.                CM1024.2
              049600     MOVE 400 TO CORRECT-2SLASH1.                                 CM1024.2
              049700     MOVE STATUS-KEY TO COMPUTED-STATUS.                          CM1024.2
              049800     MOVE "/" TO SLASH.                                           CM1024.2
              049900     MOVE ERR-KEY TO COMPUTED-ERR-KEY.                            CM1024.2
              050000     PERFORM FAIL.                                                CM1024.2
              050100     GO TO ENABL-STATUS-WRITE-03.                                 CM1024.2
              050700 ENABL-STATUS-TEST-04.                                            CM1024.2
              050800     MOVE "VALID ENABLE/NO ERROR EXPECTED" TO RE-MARK.            CM1024.2
              050900     MOVE                                                         CM1024.2
              051000     XXXXX032                                                     CM1024.2
              051100         TO SYM-DEST.                                             CM1024.2
              051200     MOVE "9" TO STATUS-KEY ERR-KEY.                              CM1024.2
              051300     MOVE 1 TO ONE.                                               CM1024.2
              051400     ENABLE OUTPUT CM-OUTQUE-1 WITH KEY                           CM1024.2
              051500     XXXXX033.                                                    CM1024.2
              051600     IF STATUS-KEY IS EQUAL TO ZERO                               CM1024.2
              051700         PERFORM PASS GO TO ENABL-STATUS-WRITE-04.                CM1024.2
              051800     MOVE 0 TO CORRECT-2SLASH1.                                   CM1024.2
              051900     MOVE STATUS-KEY TO COMPUTED-STATUS.                          CM1024.2
              052000     MOVE "/" TO SLASH.                                           CM1024.2
              052100     MOVE ERR-KEY TO COMPUTED-ERR-KEY.                            CM1024.2
              052200     PERFORM FAIL.                                                CM1024.2
              052300     GO TO ENABL-STATUS-WRITE-04.                                 CM1024.2
              052900 SEND-STATUS-TEST-03.                                             CM1024.2
              053000     MOVE "DESTINATION UNKNOWN" TO RE-MARK.                       CM1024.2
              053100     MOVE "GARBAGE" TO SYM-DEST.                                  CM1024.2
              053200     MOVE "9" TO STATUS-KEY ERR-KEY.                              CM1024.2
              053300     MOVE 1 TO ONE.                                               CM1024.2
              053400     MOVE 37 TO MSG-LENGTH.                                       CM1024.2
              053500     MOVE "S-03" TO TEST-IND.                                     CM1024.2
              053600     SEND CM-OUTQUE-1 FROM ERR-MSG WITH EMI.                      CM1024.2
              053700     IF STATUS-KEY IS EQUAL TO "20"                               CM1024.2
              053800         AND ERR-KEY IS EQUAL TO "1"                              CM1024.2
              053900         PERFORM PASS GO TO SEND-STATUS-WRITE-03.                 CM1024.2
              054000     MOVE 201 TO CORRECT-2SLASH1.                                 CM1024.2
              054100     MOVE STATUS-KEY TO COMPUTED-STATUS.                          CM1024.2
              054200     MOVE "/" TO SLASH.                                           CM1024.2
              054300     MOVE ERR-KEY TO COMPUTED-ERR-KEY.                            CM1024.2
              054400     PERFORM FAIL.                                                CM1024.2
              054500     GO TO SEND-STATUS-WRITE-03.                                  CM1024.2
              055100 SEND-STATUS-TEST-04.                                             CM1024.2
              055200     MOVE "DESTINATION COUNT INVALID (0)" TO RE-MARK.             CM1024.2
              055300     MOVE                                                         CM1024.2
              055400     XXXXX032                                                     CM1024.2
              055500         TO SYM-DEST.                                             CM1024.2
              055600     MOVE "9" TO STATUS-KEY ERR-KEY.                              CM1024.2
              055700     MOVE 0 TO ONE.                                               CM1024.2
              055800     MOVE 37 TO MSG-LENGTH.                                       CM1024.2
              055900     MOVE "S-04" TO TEST-IND.                                     CM1024.2
              056000     SEND CM-OUTQUE-1 FROM ERR-MSG WITH EMI.                      CM1024.2
              056100     IF STATUS-KEY IS EQUAL TO "30"                               CM1024.2
              056200         PERFORM PASS GO TO SEND-STATUS-WRITE-04.                 CM1024.2
              056300     MOVE 300 TO CORRECT-2SLASH1.                                 CM1024.2
              056400     MOVE STATUS-KEY TO COMPUTED-STATUS.                          CM1024.2
              056500     MOVE "/" TO SLASH.                                           CM1024.2
              056600     MOVE ERR-KEY TO COMPUTED-ERR-KEY.                            CM1024.2
              056700     PERFORM FAIL.                                                CM1024.2
              056800     GO TO SEND-STATUS-WRITE-04.                                  CM1024.2
              057400 SEND-STATUS-TEST-05.                                             CM1024.2
              057500     MOVE "CHARACTER COUNT EXCESSIVE" TO RE-MARK.                 CM1024.2
              057600     MOVE                                                         CM1024.2
              057700     XXXXX032                                                     CM1024.2
              057800         TO SYM-DEST.                                             CM1024.2
              057900     MOVE "9" TO STATUS-KEY ERR-KEY.                              CM1024.2
              058000     MOVE 1 TO ONE.                                               CM1024.2
              058100     MOVE 38 TO MSG-LENGTH.                                       CM1024.2
              058200     MOVE "S-05" TO TEST-IND.                                     CM1024.2
              058300     SEND CM-OUTQUE-1 FROM ERR-MSG WITH EMI.                      CM1024.2
              058400     IF STATUS-KEY IS EQUAL TO "50"                               CM1024.2
              058500         PERFORM PASS GO TO SEND-STATUS-WRITE-05.                 CM1024.2
              058600     MOVE 500 TO CORRECT-2SLASH1.                                 CM1024.2
              058700     MOVE STATUS-KEY TO COMPUTED-STATUS.                          CM1024.2
              058800     MOVE "/" TO SLASH.                                           CM1024.2
              058900     MOVE ERR-KEY TO COMPUTED-ERR-KEY.                            CM1024.2
              059000     PERFORM FAIL.                                                CM1024.2
              059100     GO TO SEND-STATUS-WRITE-05.                                  CM1024.2
              082200 SEND-EMI-A1.                                                     CM1024.2
              082300     SEND CM-OUTQUE-1 FROM MSG-OUT WITH EMI.                      CM1024.2
              082400     GO TO UNIFORM-SEND-EXIT.                                     CM1024.2
              082500 SEND-EGI-A1.                                                     CM1024.2
              082600     SEND CM-OUTQUE-1 FROM MSG-OUT WITH EGI.                      CM1024.2
              082700     GO TO UNIFORM-SEND-EXIT.                                     CM1024.2
              082800 SEND-EMI-AP.                                                     CM1024.2
              082900     SEND CM-OUTQUE-1 FROM MSG-OUT WITH EMI AFTER PAGE.           CM1024.2
              083000     GO TO UNIFORM-SEND-EXIT.                                     CM1024.2
              083100 SEND-EMI-A3-01.                                                  CM1024.2
              083200     SEND CM-OUTQUE-1 FROM MSG-OUT WITH EMI AFTER ADVANCING 3     CM1024.2
              083300         LINES.                                                   CM1024.2
              083400     GO TO UNIFORM-SEND-EXIT.                                     CM1024.2
              083500 SEND-EMI-A3-02.                                                  CM1024.2
              083600     SEND CM-OUTQUE-1 FROM MSG-OUT WITH EMI                       CM1024.2
              083700         AFTER ADVANCING THREE LINES.                             CM1024.2
              083800     GO TO UNIFORM-SEND-EXIT.                                     CM1024.2
              083900 SEND-EMI-A3-03.                                                  CM1024.2
              084000     SEND CM-OUTQUE-1 FROM MSG-OUT WITH EMI                       CM1024.2
              084100         AFTER 3 LINE.                                            CM1024.2
              084200     GO TO UNIFORM-SEND-EXIT.                                     CM1024.2
              084300 SEND-EMI-A3-04.                                                  CM1024.2
              084400     SEND CM-OUTQUE-1 FROM MSG-OUT WITH EMI                       CM1024.2
              084500         AFTER COMP-THREE.                                        CM1024.2
              084600     GO TO UNIFORM-SEND-EXIT.                                     CM1024.2
              084700 SEND-EMI-A3-05.                                                  CM1024.2
              084800     SEND CM-OUTQUE-1 FROM MSG-OUT WITH EMI                       CM1024.2
              084900         AFTER 3.                                                 CM1024.2
              085000     GO TO UNIFORM-SEND-EXIT.                                     CM1024.2
              085100 SEND-EGI-ONLY.                                                   CM1024.2
              085200     SEND CM-OUTQUE-1 WITH EGI.                                   CM1024.2
              085300     GO TO UNIFORM-SEND-EXIT.                                     CM1024.2
              085400 SEND-EMI-BP.                                                     CM1024.2
              085500     SEND CM-OUTQUE-1 FROM MSG-OUT WITH EMI                       CM1024.2
              085600         BEFORE ADVANCING PAGE.                                   CM1024.2
              085700     GO TO UNIFORM-SEND-EXIT.                                     CM1024.2
              085800 SEND-EMI-B2-01.                                                  CM1024.2
              085900     SEND CM-OUTQUE-1 FROM MSG-OUT WITH EMI                       CM1024.2
              086000         BEFORE ADVANCING 2 LINES.                                CM1024.2
              086100     GO TO UNIFORM-SEND-EXIT.                                     CM1024.2
              086200 SEND-EMI-B2-02.                                                  CM1024.2
              086300     SEND CM-OUTQUE-1 FROM MSG-OUT WITH EMI                       CM1024.2
              086400         BEFORE ADVANCING TWO LINES.                              CM1024.2
              086500     GO TO UNIFORM-SEND-EXIT.                                     CM1024.2
              086600 SEND-EMI-B2-03.                                                  CM1024.2
              086700     SEND CM-OUTQUE-1 FROM MSG-OUT WITH EMI                       CM1024.2
              086800         BEFORE 2 LINE.                                           CM1024.2
              086900     GO TO UNIFORM-SEND-EXIT.                                     CM1024.2
              087000 SEND-EMI-B2-04.                                                  CM1024.2
              087100     SEND CM-OUTQUE-1 FROM MSG-OUT WITH EMI                       CM1024.2
              087200         BEFORE COMP-TWO.                                         CM1024.2
              087300     GO TO UNIFORM-SEND-EXIT.                                     CM1024.2
              087400 SEND-EMI-B2-05.                                                  CM1024.2
              087500     SEND CM-OUTQUE-1 FROM MSG-OUT WITH EMI                       CM1024.2
              087600         BEFORE 2.                                                CM1024.2
              087700     GO TO UNIFORM-SEND-EXIT.                                     CM1024.2
              087800 SEND-EMI-A0.                                                     CM1024.2
              087900     SEND CM-OUTQUE-1 FROM MSG-OUT WITH EMI                       CM1024.2
              088000         AFTER 0 LINES.                                           CM1024.2
              088100     GO TO UNIFORM-SEND-EXIT.                                     CM1024.2
              088200 SEND-EMI-B0.                                                     CM1024.2
              088300     SEND CM-OUTQUE-1 FROM MSG-OUT WITH EMI                       CM1024.2
              088400         BEFORE ZERO LINES.                                       CM1024.2
              088500     GO TO UNIFORM-SEND-EXIT.                                     CM1024.2
              088600 SEND-LONG-MSG.                                                   CM1024.2
              088700     SEND CM-OUTQUE-1 FROM LONG-MSG WITH EMI AFTER PAGE.          CM1024.2
              088800     GO TO UNIFORM-SEND-EXIT.                                     CM1024.2
              088900 DISABLE-OUTQUE.                                                  CM1024.2
              089000     DISABLE OUTPUT CM-OUTQUE-1 KEY                               CM1024.2
              089100     PASSWORD1.                                                   CM1024.2
              089200     GO TO UNIFORM-SEND-EXIT.                                     CM1024.2
              089300 ENABLE-OUTQUE.                                                   CM1024.2
              089400     ENABLE OUTPUT CM-OUTQUE-1 WITH KEY                           CM1024.2
              089500     XXXXX033.                                                    CM1024.2
              """,
            """
              000100 IDENTIFICATION DIVISION.                                         CM1024.2
              000200 PROGRAM-ID.                                                      CM1024.2
              000300     ~~>CM102M.                                                      CM1024.2
              003200 DATA DIVISION.                                                   CM1024.2
              027700 COMMUNICATION SECTION.                                           CM1024.2
              027800 CD  ~~>CM-OUTQUE-1 FOR OUTPUT                                       CM1024.2
              027900     DESTINATION COUNT IS ONE                                     CM1024.2
              028000     TEXT LENGTH IS MSG-LENGTH                                    CM1024.2
              028100     STATUS KEY IS STATUS-KEY                                     CM1024.2
              028200     ERROR KEY IS ERR-KEY                                         CM1024.2
              028300     SYMBOLIC DESTINATION IS SYM-DEST.                            CM1024.2
              028400 PROCEDURE    DIVISION.                                           CM1024.2
              028500 SECT-CM102M-0001 SECTION.                                        CM1024.2
              028600 ~~>CM102M-INIT.                                                     CM1024.2
              028700     OPEN     OUTPUT PRINT-FILE.                                  CM1024.2
              028800     MOVE "CM102M     " TO TEST-ID.                               CM1024.2
              028900     MOVE     TEST-ID TO ID-AGAIN.                                CM1024.2
              029000     MOVE    SPACE TO TEST-RESULTS.                               CM1024.2
              029100     PERFORM HEAD-ROUTINE.                                        CM1024.2
              029200     PERFORM COLUMN-NAMES-ROUTINE.                                CM1024.2
              029300     MOVE "MCS STATUS WORD" TO FEATURE.                           CM1024.2
              029400 DISAB-STATUS-TEST-01.                                            CM1024.2
              029500     MOVE "INITIAL DISABLE TO OUTPUT CD" TO RE-MARK.              CM1024.2
              029600     MOVE "9" TO STATUS-KEY ERR-KEY.                              CM1024.2
              029700     MOVE 1 TO ONE.                                               CM1024.2
              029800     MOVE                                                         CM1024.2
              029900     XXXXX032                                                     CM1024.2
              030000         TO SYM-DEST.                                             CM1024.2
              030100     DISABLE OUTPUT ~~>CM-OUTQUE-1 WITH KEY                          CM1024.2
              030200     XXXXX033.                                                    CM1024.2
              030300     MOVE "INFO" TO P-OR-F.                                       CM1024.2
              030400     MOVE STATUS-KEY TO COMPUTED-STATUS.                          CM1024.2
              030500     MOVE "/" TO SLASH.                                           CM1024.2
              030600     MOVE ERR-KEY TO COMPUTED-ERR-KEY.                            CM1024.2
              030700     MOVE "       INFO TEST FOR" TO CORRECT-A.                    CM1024.2
              030800     GO TO DISAB-STATUS-WRITE-01.                                 CM1024.2
              031400 DISAB-STATUS-TEST-02.                                            CM1024.2
              031500     MOVE "NO DESTINATION SPECIFIED" TO RE-MARK.                  CM1024.2
              031600     MOVE "9" TO STATUS-KEY ERR-KEY.                              CM1024.2
              031700     MOVE "GARBAGE" TO SYM-DEST.                                  CM1024.2
              031800     MOVE 1 TO ONE.                                               CM1024.2
              031900     DISABLE OUTPUT ~~>CM-OUTQUE-1 WITH KEY                          CM1024.2
              032000     XXXXX033.                                                    CM1024.2
              032100     IF STATUS-KEY IS EQUAL TO "20"                               CM1024.2
              032200         AND ERR-KEY IS EQUAL TO "1"                              CM1024.2
              032300         PERFORM PASS GO TO DISAB-STATUS-WRITE-02.                CM1024.2
              032400     MOVE 201 TO CORRECT-2SLASH1.                                 CM1024.2
              032500     MOVE STATUS-KEY TO COMPUTED-STATUS.                          CM1024.2
              032600     MOVE "/" TO SLASH.                                           CM1024.2
              032700     MOVE ERR-KEY TO COMPUTED-ERR-KEY.                            CM1024.2
              032800     PERFORM FAIL.                                                CM1024.2
              032900     GO TO DISAB-STATUS-WRITE-02.                                 CM1024.2
              033500 DISAB-STATUS-TEST-03.                                            CM1024.2
              033600     MOVE "INVALID PASSWORD USED" TO RE-MARK.                     CM1024.2
              033700     MOVE "9" TO STATUS-KEY ERR-KEY.                              CM1024.2
              033800     MOVE 1 TO ONE.                                               CM1024.2
              033900     MOVE                                                         CM1024.2
              034000     XXXXX032                                                     CM1024.2
              034100         TO SYM-DEST.                                             CM1024.2
              034200     DISABLE OUTPUT ~~>CM-OUTQUE-1 WITH KEY                          CM1024.2
              034300         "GARBAGE".                                               CM1024.2
              034400     IF STATUS-KEY IS EQUAL TO "40"                               CM1024.2
              034500         PERFORM PASS GO TO DISAB-STATUS-WRITE-03.                CM1024.2
              034600     MOVE 400 TO CORRECT-2SLASH1.                                 CM1024.2
              034700     MOVE STATUS-KEY TO COMPUTED-STATUS.                          CM1024.2
              034800     MOVE "/" TO SLASH.                                           CM1024.2
              034900     MOVE ERR-KEY TO COMPUTED-ERR-KEY.                            CM1024.2
              035000     PERFORM FAIL.                                                CM1024.2
              035100     GO TO DISAB-STATUS-WRITE-03.                                 CM1024.2
              035700 DISAB-STATUS-TEST-04.                                            CM1024.2
              035800     MOVE "INVALID DESTINATION COUNT (0)" TO RE-MARK.             CM1024.2
              035900     MOVE "9" TO STATUS-KEY ERR-KEY.                              CM1024.2
              036000     MOVE                                                         CM1024.2
              036100     XXXXX032                                                     CM1024.2
              036200         TO SYM-DEST.                                             CM1024.2
              036300     MOVE 0 TO ONE.                                               CM1024.2
              036400     DISABLE OUTPUT ~~>CM-OUTQUE-1 WITH KEY                          CM1024.2
              036500     XXXXX033.                                                    CM1024.2
              036600     IF STATUS-KEY IS EQUAL TO "30"                               CM1024.2
              036700         PERFORM PASS GO TO DISAB-STATUS-WRITE-04.                CM1024.2
              036800     MOVE 300 TO CORRECT-2SLASH1.                                 CM1024.2
              036900     MOVE STATUS-KEY TO COMPUTED-STATUS.                          CM1024.2
              037000     MOVE "/" TO SLASH.                                           CM1024.2
              037100     MOVE ERR-KEY TO COMPUTED-ERR-KEY.                            CM1024.2
              037200     PERFORM FAIL.                                                CM1024.2
              037300     GO TO DISAB-STATUS-WRITE-04.                                 CM1024.2
              037900 DISAB-STATUS-TEST-05.                                            CM1024.2
              038000     MOVE "COMBINATION ERROR" TO RE-MARK.                         CM1024.2
              038100     MOVE "9" TO STATUS-KEY ERR-KEY.                              CM1024.2
              038200     MOVE SPACES TO SYM-DEST.                                     CM1024.2
              038300     MOVE 0 TO ONE.                                               CM1024.2
              038400     DISABLE OUTPUT ~~>CM-OUTQUE-1 WITH KEY                          CM1024.2
              038500         "GARBAGE".                                               CM1024.2
              038600     MOVE "INFO" TO P-OR-F.                                       CM1024.2
              038700     MOVE STATUS-KEY TO COMPUTED-STATUS.                          CM1024.2
              038800     MOVE "/" TO SLASH.                                           CM1024.2
              038900     MOVE ERR-KEY TO COMPUTED-ERR-KEY.                            CM1024.2
              039000     GO TO DISAB-STATUS-WRITE-05.                                 CM1024.2
              039600 SEND-STATUS-TEST-01.                                             CM1024.2
              039700     MOVE "DESTINATION DISABLED" TO RE-MARK.                      CM1024.2
              039800     MOVE "CM102M- I AM THE FIRST MESSAGE IN QUEUE;" TO MSG-70.   CM1024.2
              039900     MOVE "9" TO STATUS-KEY ERR-KEY.                              CM1024.2
              040000     MOVE                                                         CM1024.2
              040100     XXXXX032                                                     CM1024.2
              040200         TO SYM-DEST.                                             CM1024.2
              040300     MOVE 1 TO ONE.                                               CM1024.2
              040400     MOVE 45 TO MSG-LENGTH.                                       CM1024.2
              040500     SEND ~~>CM-OUTQUE-1 FROM MSG-70 WITH EMI                        CM1024.2
              040600         AFTER ADVANCING PAGE.                                    CM1024.2
              040700     MOVE "THOU SHALT HAVE NO OTHER MESSAGES BEFORE ME." TO MSG-70CM1024.2
              040800     SEND ~~>CM-OUTQUE-1 FROM MSG-70 WITH EMI.                       CM1024.2
              040900     MOVE SPACES TO MSG-70.                                       CM1024.2
              041000     MOVE 1 TO MSG-LENGTH.                                        CM1024.2
              041100     SEND ~~>CM-OUTQUE-1 FROM MSG-70 WITH EGI.                       CM1024.2
              041200     IF STATUS-KEY IS EQUAL TO "10"                               CM1024.2
              041300         PERFORM PASS GO TO SEND-STATUS-WRITE-01.                 CM1024.2
              041400     MOVE 100 TO CORRECT-2SLASH1.                                 CM1024.2
              041500     MOVE STATUS-KEY TO COMPUTED-STATUS.                          CM1024.2
              041600     MOVE "/" TO SLASH.                                           CM1024.2
              041700     MOVE ERR-KEY TO COMPUTED-ERR-KEY.                            CM1024.2
              041800     PERFORM FAIL.                                                CM1024.2
              041900     GO TO SEND-STATUS-WRITE-01.                                  CM1024.2
              042500 SEND-STATUS-TEST-02.                                             CM1024.2
              042600     MOVE "COMBINATION ERROR" TO RE-MARK.                         CM1024.2
              042700     MOVE SPACES TO SYM-DEST.                                     CM1024.2
              042800     MOVE 0 TO ONE.                                               CM1024.2
              042900     MOVE 100 TO MSG-LENGTH.                                      CM1024.2
              043000     MOVE "S-02" TO TEST-IND.                                     CM1024.2
              043100     SEND ~~>CM-OUTQUE-1 FROM ERR-MSG WITH EMI.                      CM1024.2
              043200     MOVE "INFO" TO P-OR-F.                                       CM1024.2
              043300     MOVE STATUS-KEY TO COMPUTED-STATUS.                          CM1024.2
              043400     MOVE "/" TO SLASH.                                           CM1024.2
              043500     MOVE ERR-KEY TO COMPUTED-ERR-KEY.                            CM1024.2
              043600     GO TO SEND-STATUS-WRITE-02.                                  CM1024.2
              044200 ENABL-STATUS-TEST-01.                                            CM1024.2
              044300     MOVE "DESTINATION NOT SPECIFIED" TO RE-MARK.                 CM1024.2
              044400     MOVE SPACES TO SYM-DEST.                                     CM1024.2
              044500     MOVE "9" TO STATUS-KEY ERR-KEY.                              CM1024.2
              044600     MOVE 1 TO ONE.                                               CM1024.2
              044700     ENABLE OUTPUT ~~>CM-OUTQUE-1 WITH KEY                           CM1024.2
              044800     XXXXX033.                                                    CM1024.2
              044900     IF STATUS-KEY IS EQUAL TO "20"                               CM1024.2
              045000         AND ERR-KEY IS EQUAL TO "1"                              CM1024.2
              045100         PERFORM PASS GO TO ENABL-STATUS-WRITE-01.                CM1024.2
              045200     MOVE 201 TO CORRECT-2SLASH1.                                 CM1024.2
              045300     MOVE STATUS-KEY TO COMPUTED-STATUS.                          CM1024.2
              045400     MOVE "/" TO SLASH.                                           CM1024.2
              045500     MOVE ERR-KEY TO COMPUTED-ERR-KEY.                            CM1024.2
              045600     PERFORM FAIL.                                                CM1024.2
              045700     GO TO ENABL-STATUS-WRITE-01.                                 CM1024.2
              046300 ENABL-STATUS-TEST-02.                                            CM1024.2
              046400     MOVE "INVALID DESTINATION COUNT (0)" TO RE-MARK.             CM1024.2
              046500     MOVE                                                         CM1024.2
              046600     XXXXX032                                                     CM1024.2
              046700         TO SYM-DEST.                                             CM1024.2
              046800     MOVE "9" TO STATUS-KEY ERR-KEY.                              CM1024.2
              046900     MOVE 0 TO ONE.                                               CM1024.2
              047000     ENABLE OUTPUT ~~>CM-OUTQUE-1 WITH KEY                           CM1024.2
              047100     XXXXX033.                                                    CM1024.2
              047200     IF STATUS-KEY IS EQUAL TO "30"                               CM1024.2
              047300         PERFORM PASS GO TO ENABL-STATUS-WRITE-02.                CM1024.2
              047400     MOVE 300 TO CORRECT-2SLASH1.                                 CM1024.2
              047500     MOVE STATUS-KEY TO COMPUTED-STATUS.                          CM1024.2
              047600     MOVE "/" TO SLASH.                                           CM1024.2
              047700     MOVE ERR-KEY TO COMPUTED-ERR-KEY.                            CM1024.2
              047800     PERFORM FAIL.                                                CM1024.2
              047900     GO TO ENABL-STATUS-WRITE-02.                                 CM1024.2
              048500 ENABL-STATUS-TEST-03.                                            CM1024.2
              048600     MOVE "INVALID PASSWORD USED" TO RE-MARK.                     CM1024.2
              048700     MOVE                                                         CM1024.2
              048800     XXXXX032                                                     CM1024.2
              048900         TO SYM-DEST.                                             CM1024.2
              049000     MOVE "9" TO STATUS-KEY ERR-KEY.                              CM1024.2
              049100     MOVE 1 TO ONE.                                               CM1024.2
              049200     ENABLE OUTPUT ~~>CM-OUTQUE-1 WITH KEY                           CM1024.2
              049300         "GARBAGE".                                               CM1024.2
              049400     IF STATUS-KEY IS EQUAL TO "40"                               CM1024.2
              049500         PERFORM PASS GO TO ENABL-STATUS-WRITE-03.                CM1024.2
              049600     MOVE 400 TO CORRECT-2SLASH1.                                 CM1024.2
              049700     MOVE STATUS-KEY TO COMPUTED-STATUS.                          CM1024.2
              049800     MOVE "/" TO SLASH.                                           CM1024.2
              049900     MOVE ERR-KEY TO COMPUTED-ERR-KEY.                            CM1024.2
              050000     PERFORM FAIL.                                                CM1024.2
              050100     GO TO ENABL-STATUS-WRITE-03.                                 CM1024.2
              050700 ENABL-STATUS-TEST-04.                                            CM1024.2
              050800     MOVE "VALID ENABLE/NO ERROR EXPECTED" TO RE-MARK.            CM1024.2
              050900     MOVE                                                         CM1024.2
              051000     XXXXX032                                                     CM1024.2
              051100         TO SYM-DEST.                                             CM1024.2
              051200     MOVE "9" TO STATUS-KEY ERR-KEY.                              CM1024.2
              051300     MOVE 1 TO ONE.                                               CM1024.2
              051400     ENABLE OUTPUT ~~>CM-OUTQUE-1 WITH KEY                           CM1024.2
              051500     XXXXX033.                                                    CM1024.2
              051600     IF STATUS-KEY IS EQUAL TO ZERO                               CM1024.2
              051700         PERFORM PASS GO TO ENABL-STATUS-WRITE-04.                CM1024.2
              051800     MOVE 0 TO CORRECT-2SLASH1.                                   CM1024.2
              051900     MOVE STATUS-KEY TO COMPUTED-STATUS.                          CM1024.2
              052000     MOVE "/" TO SLASH.                                           CM1024.2
              052100     MOVE ERR-KEY TO COMPUTED-ERR-KEY.                            CM1024.2
              052200     PERFORM FAIL.                                                CM1024.2
              052300     GO TO ENABL-STATUS-WRITE-04.                                 CM1024.2
              052900 SEND-STATUS-TEST-03.                                             CM1024.2
              053000     MOVE "DESTINATION UNKNOWN" TO RE-MARK.                       CM1024.2
              053100     MOVE "GARBAGE" TO SYM-DEST.                                  CM1024.2
              053200     MOVE "9" TO STATUS-KEY ERR-KEY.                              CM1024.2
              053300     MOVE 1 TO ONE.                                               CM1024.2
              053400     MOVE 37 TO MSG-LENGTH.                                       CM1024.2
              053500     MOVE "S-03" TO TEST-IND.                                     CM1024.2
              053600     SEND ~~>CM-OUTQUE-1 FROM ERR-MSG WITH EMI.                      CM1024.2
              053700     IF STATUS-KEY IS EQUAL TO "20"                               CM1024.2
              053800         AND ERR-KEY IS EQUAL TO "1"                              CM1024.2
              053900         PERFORM PASS GO TO SEND-STATUS-WRITE-03.                 CM1024.2
              054000     MOVE 201 TO CORRECT-2SLASH1.                                 CM1024.2
              054100     MOVE STATUS-KEY TO COMPUTED-STATUS.                          CM1024.2
              054200     MOVE "/" TO SLASH.                                           CM1024.2
              054300     MOVE ERR-KEY TO COMPUTED-ERR-KEY.                            CM1024.2
              054400     PERFORM FAIL.                                                CM1024.2
              054500     GO TO SEND-STATUS-WRITE-03.                                  CM1024.2
              055100 SEND-STATUS-TEST-04.                                             CM1024.2
              055200     MOVE "DESTINATION COUNT INVALID (0)" TO RE-MARK.             CM1024.2
              055300     MOVE                                                         CM1024.2
              055400     XXXXX032                                                     CM1024.2
              055500         TO SYM-DEST.                                             CM1024.2
              055600     MOVE "9" TO STATUS-KEY ERR-KEY.                              CM1024.2
              055700     MOVE 0 TO ONE.                                               CM1024.2
              055800     MOVE 37 TO MSG-LENGTH.                                       CM1024.2
              055900     MOVE "S-04" TO TEST-IND.                                     CM1024.2
              056000     SEND ~~>CM-OUTQUE-1 FROM ERR-MSG WITH EMI.                      CM1024.2
              056100     IF STATUS-KEY IS EQUAL TO "30"                               CM1024.2
              056200         PERFORM PASS GO TO SEND-STATUS-WRITE-04.                 CM1024.2
              056300     MOVE 300 TO CORRECT-2SLASH1.                                 CM1024.2
              056400     MOVE STATUS-KEY TO COMPUTED-STATUS.                          CM1024.2
              056500     MOVE "/" TO SLASH.                                           CM1024.2
              056600     MOVE ERR-KEY TO COMPUTED-ERR-KEY.                            CM1024.2
              056700     PERFORM FAIL.                                                CM1024.2
              056800     GO TO SEND-STATUS-WRITE-04.                                  CM1024.2
              057400 SEND-STATUS-TEST-05.                                             CM1024.2
              057500     MOVE "CHARACTER COUNT EXCESSIVE" TO RE-MARK.                 CM1024.2
              057600     MOVE                                                         CM1024.2
              057700     XXXXX032                                                     CM1024.2
              057800         TO SYM-DEST.                                             CM1024.2
              057900     MOVE "9" TO STATUS-KEY ERR-KEY.                              CM1024.2
              058000     MOVE 1 TO ONE.                                               CM1024.2
              058100     MOVE 38 TO MSG-LENGTH.                                       CM1024.2
              058200     MOVE "S-05" TO TEST-IND.                                     CM1024.2
              058300     SEND ~~>CM-OUTQUE-1 FROM ERR-MSG WITH EMI.                      CM1024.2
              058400     IF STATUS-KEY IS EQUAL TO "50"                               CM1024.2
              058500         PERFORM PASS GO TO SEND-STATUS-WRITE-05.                 CM1024.2
              058600     MOVE 500 TO CORRECT-2SLASH1.                                 CM1024.2
              058700     MOVE STATUS-KEY TO COMPUTED-STATUS.                          CM1024.2
              058800     MOVE "/" TO SLASH.                                           CM1024.2
              058900     MOVE ERR-KEY TO COMPUTED-ERR-KEY.                            CM1024.2
              059000     PERFORM FAIL.                                                CM1024.2
              059100     GO TO SEND-STATUS-WRITE-05.                                  CM1024.2
              082200 SEND-EMI-A1.                                                     CM1024.2
              082300     SEND ~~>CM-OUTQUE-1 FROM MSG-OUT WITH EMI.                      CM1024.2
              082400     GO TO UNIFORM-SEND-EXIT.                                     CM1024.2
              082500 SEND-EGI-A1.                                                     CM1024.2
              082600     SEND ~~>CM-OUTQUE-1 FROM MSG-OUT WITH EGI.                      CM1024.2
              082700     GO TO UNIFORM-SEND-EXIT.                                     CM1024.2
              082800 SEND-EMI-AP.                                                     CM1024.2
              082900     SEND ~~>CM-OUTQUE-1 FROM MSG-OUT WITH EMI AFTER PAGE.           CM1024.2
              083000     GO TO UNIFORM-SEND-EXIT.                                     CM1024.2
              083100 SEND-EMI-A3-01.                                                  CM1024.2
              083200     SEND ~~>CM-OUTQUE-1 FROM MSG-OUT WITH EMI AFTER ADVANCING 3     CM1024.2
              083300         LINES.                                                   CM1024.2
              083400     GO TO UNIFORM-SEND-EXIT.                                     CM1024.2
              083500 SEND-EMI-A3-02.                                                  CM1024.2
              083600     SEND ~~>CM-OUTQUE-1 FROM MSG-OUT WITH EMI                       CM1024.2
              083700         AFTER ADVANCING THREE LINES.                             CM1024.2
              083800     GO TO UNIFORM-SEND-EXIT.                                     CM1024.2
              083900 SEND-EMI-A3-03.                                                  CM1024.2
              084000     SEND ~~>CM-OUTQUE-1 FROM MSG-OUT WITH EMI                       CM1024.2
              084100         AFTER 3 LINE.                                            CM1024.2
              084200     GO TO UNIFORM-SEND-EXIT.                                     CM1024.2
              084300 SEND-EMI-A3-04.                                                  CM1024.2
              084400     SEND ~~>CM-OUTQUE-1 FROM MSG-OUT WITH EMI                       CM1024.2
              084500         AFTER COMP-THREE.                                        CM1024.2
              084600     GO TO UNIFORM-SEND-EXIT.                                     CM1024.2
              084700 SEND-EMI-A3-05.                                                  CM1024.2
              084800     SEND ~~>CM-OUTQUE-1 FROM MSG-OUT WITH EMI                       CM1024.2
              084900         AFTER 3.                                                 CM1024.2
              085000     GO TO UNIFORM-SEND-EXIT.                                     CM1024.2
              085100 SEND-EGI-ONLY.                                                   CM1024.2
              085200     SEND ~~>CM-OUTQUE-1 WITH EGI.                                   CM1024.2
              085300     GO TO UNIFORM-SEND-EXIT.                                     CM1024.2
              085400 SEND-EMI-BP.                                                     CM1024.2
              085500     SEND ~~>CM-OUTQUE-1 FROM MSG-OUT WITH EMI                       CM1024.2
              085600         BEFORE ADVANCING PAGE.                                   CM1024.2
              085700     GO TO UNIFORM-SEND-EXIT.                                     CM1024.2
              085800 SEND-EMI-B2-01.                                                  CM1024.2
              085900     SEND ~~>CM-OUTQUE-1 FROM MSG-OUT WITH EMI                       CM1024.2
              086000         BEFORE ADVANCING 2 LINES.                                CM1024.2
              086100     GO TO UNIFORM-SEND-EXIT.                                     CM1024.2
              086200 SEND-EMI-B2-02.                                                  CM1024.2
              086300     SEND ~~>CM-OUTQUE-1 FROM MSG-OUT WITH EMI                       CM1024.2
              086400         BEFORE ADVANCING TWO LINES.                              CM1024.2
              086500     GO TO UNIFORM-SEND-EXIT.                                     CM1024.2
              086600 SEND-EMI-B2-03.                                                  CM1024.2
              086700     SEND ~~>CM-OUTQUE-1 FROM MSG-OUT WITH EMI                       CM1024.2
              086800         BEFORE 2 LINE.                                           CM1024.2
              086900     GO TO UNIFORM-SEND-EXIT.                                     CM1024.2
              087000 SEND-EMI-B2-04.                                                  CM1024.2
              087100     SEND ~~>CM-OUTQUE-1 FROM MSG-OUT WITH EMI                       CM1024.2
              087200         BEFORE COMP-TWO.                                         CM1024.2
              087300     GO TO UNIFORM-SEND-EXIT.                                     CM1024.2
              087400 SEND-EMI-B2-05.                                                  CM1024.2
              087500     SEND ~~>CM-OUTQUE-1 FROM MSG-OUT WITH EMI                       CM1024.2
              087600         BEFORE 2.                                                CM1024.2
              087700     GO TO UNIFORM-SEND-EXIT.                                     CM1024.2
              087800 SEND-EMI-A0.                                                     CM1024.2
              087900     SEND ~~>CM-OUTQUE-1 FROM MSG-OUT WITH EMI                       CM1024.2
              088000         AFTER 0 LINES.                                           CM1024.2
              088100     GO TO UNIFORM-SEND-EXIT.                                     CM1024.2
              088200 SEND-EMI-B0.                                                     CM1024.2
              088300     SEND ~~>CM-OUTQUE-1 FROM MSG-OUT WITH EMI                       CM1024.2
              088400         BEFORE ZERO LINES.                                       CM1024.2
              088500     GO TO UNIFORM-SEND-EXIT.                                     CM1024.2
              088600 SEND-LONG-MSG.                                                   CM1024.2
              088700     SEND ~~>CM-OUTQUE-1 FROM LONG-MSG WITH EMI AFTER PAGE.          CM1024.2
              088800     GO TO UNIFORM-SEND-EXIT.                                     CM1024.2
              088900 DISABLE-OUTQUE.                                                  CM1024.2
              089000     DISABLE OUTPUT ~~>CM-OUTQUE-1 KEY                               CM1024.2
              089100     PASSWORD1.                                                   CM1024.2
              089200     GO TO UNIFORM-SEND-EXIT.                                     CM1024.2
              089300 ENABLE-OUTQUE.                                                   CM1024.2
              089400     ENABLE OUTPUT ~~>CM-OUTQUE-1 WITH KEY                           CM1024.2
              089500     XXXXX033.                                                    CM1024.2
              """,
            spec -> spec.afterRecipe(cu -> {
                var searchResults = new ArrayList<SearchResult>(39);
                visitor.visit(cu, searchResults);
                assertThat(searchResults).hasSize(39);
            })
          )
        );
    }

    private final String sm101A =
      """
        000100 IDENTIFICATION DIVISION.                                         SM1014.2
        000200 PROGRAM-ID.                                                      SM1014.2
        000300     SM101A.                                                      SM1014.2
        000400****************************************************************  SM1014.2
        000500*                                                              *  SM1014.2
        000600*    VALIDATION FOR:-                                          *  SM1014.2
        000700*                                                              *  SM1014.2
        000800*    "ON-SITE VALIDATION, NATIONAL INSTITUTE OF STD & TECH.     ".SM1014.2
        000900*                                                              *  SM1014.2
        001000*    "COBOL 85 VERSION 4.2, Apr  1993 SSVG                      ".SM1014.2
        001100*                                                              *  SM1014.2
        001200*                                                              *  SM1014.2
        001300*      X-CARDS USED BY THIS PROGRAM ARE :-                     *  SM1014.2
        001400*                                                              *  SM1014.2
        001500*        X-55  - SYSTEM PRINTER NAME.                          *  SM1014.2
        001600*        X-82  - SOURCE COMPUTER NAME.                         *  SM1014.2
        001700*        X-83  - OBJECT COMPUTER NAME.                         *  SM1014.2
        001800*                                                              *  SM1014.2
        001900****************************************************************  SM1014.2
        002000*                                                              *  SM1014.2
        002100*    PROGRAM SM101A TESTS THE USE OF THE "COPY" STATEMENT      *  SM1014.2
        002200*    IN A FILE DESCRIPTION WITH ITS RELATED 01 ENTRIES IN THE  *  SM1014.2
        002300*    WORKING-STORAGE SECTION AND IN THE PROCEDURE DIVISION.    *  SM1014.2
        002400*    IT CREATES A SEQUENTIAL FILE WHICH IS INPUT TO SM102A TO  *  SM1014.2
        002500*    CHECK THE PROPER EXECUTION OF THE "COPY" STATEMENT IN     *  SM1014.2
        002600*    SM101A.  IT ALSO TESTS THE EFFECT OF A "COPY" STATEMENT   *  SM1014.2
        002700*    APPEARING ON A DEBUGGING LINE.                            *  SM1014.2
        002800*                                                              *  SM1014.2
        002900****************************************************************  SM1014.2
        003000 ENVIRONMENT DIVISION.                                            SM1014.2
        003100 CONFIGURATION SECTION.                                           SM1014.2
        003200 SOURCE-COMPUTER.                                                 SM1014.2
        003300     XXXXX082.                                                    SM1014.2
        003400 OBJECT-COMPUTER.                                                 SM1014.2
        003500     XXXXX083.                                                    SM1014.2
        003600 INPUT-OUTPUT SECTION.                                            SM1014.2
        003700 FILE-CONTROL.                                                    SM1014.2
        003800     SELECT PRINT-FILE ASSIGN TO                                  SM1014.2
        003900     XXXXX055.                                                    SM1014.2
        004000     SELECT TEST-FILE ASSIGN TO                                   SM1014.2
        004100     XXXXP001.                                                    SM1014.2
        004200 DATA DIVISION.                                                   SM1014.2
        004300 FILE SECTION.                                                    SM1014.2
        004400 FD  PRINT-FILE.                                                  SM1014.2
        004500 01  PRINT-REC PICTURE X(120).                                    SM1014.2
        004600 01  DUMMY-RECORD PICTURE X(120).                                 SM1014.2
        004700                                                                  SM1014.2
        004800                                                                  SM1014.2
        004900                                                                  SM1014.2
        005000                                                                  SM1014.2
        005100                                                                  SM1014.2
        005200*                                                                 SM1014.2
        005300*********************** COPY STATEMENT USED **********************SM1014.2
        005400*                                                                 SM1014.2
        005500*FD  TEST-FILE                                        COPY K1FDA. SM1014.2
        005600*                                                                 SM1014.2
        005700******************** COPIED TEXT BEGINS BELOW ********************SM1014.2
        005800 FD  TEST-FILE                                         COPY K1FDA.SM1014.2
        005900*********************** END OF COPIED TEXT ***********************SM1014.2
        006000                                                                  SM1014.2
        006100                                                                  SM1014.2
        006200                                                                  SM1014.2
        006300                                                                  SM1014.2
        006400                                                                  SM1014.2
        006500*                                                                 SM1014.2
        006600*********************** COPY STATEMENT USED **********************SM1014.2
        006700*                                                                 SM1014.2
        006800*01  TST-TEST                                         COPY K101A. SM1014.2
        006900*                                                                 SM1014.2
        007000******************** COPIED TEXT BEGINS BELOW ********************SM1014.2
        007100 01  TST-TEST                                          COPY K101A.SM1014.2
        007200*********************** END OF COPIED TEXT ***********************SM1014.2
        007300 WORKING-STORAGE SECTION.                                         SM1014.2
        007400*                                                                 SM1014.2
        007500*********************** COPY STATEMENT USED **********************SM1014.2
        007600*                                                                 SM1014.2
        007700*77  RCD-1                                            COPY K1W01. SM1014.2
        007800*                                                                 SM1014.2
        007900******************** COPIED TEXT BEGINS BELOW ********************SM1014.2
        008000 77  RCD-1                                             COPY K1W01.SM1014.2
        008100*********************** END OF COPIED TEXT ***********************SM1014.2
        008200 77  RCD-3 PICTURE 9(5) VALUE 10901.                              SM1014.2
        008300*                                                                 SM1014.2
        008400*********************** COPY STATEMENT USED **********************SM1014.2
        008500*                                                                 SM1014.2
        008600*77  COPY K1W02.                                                  SM1014.2
        008700*                                                                 SM1014.2
        008800******************** COPIED TEXT BEGINS BELOW ********************SM1014.2
        008900 77  COPY K1W02.                                                  SM1014.2
        009000*********************** END OF COPIED TEXT ***********************SM1014.2
        009100                       14003.                                     SM1014.2
        009200 77  RCD-6 PICTURE 9(5) VALUE 19922.                              SM1014.2
        009300*                                                                 SM1014.2
        009400*********************** COPY STATEMENT USED **********************SM1014.2
        009500*                                                                 SM1014.2
        009600*77  COPY K1W03.   VALUE 3543.                                    SM1014.2
        009700*                                                                 SM1014.2
        009800******************** COPIED TEXT BEGINS BELOW ********************SM1014.2
        009900 77  COPY K1W03.   VALUE 3543.                                    SM1014.2
        010000*********************** END OF COPIED TEXT ***********************SM1014.2
        010100 77  COPYSECT-1 PICTURE 9(5) VALUE 72459.                         SM1014.2
        010200 77  COPYSECT-2 PICTURE 9(5) VALUE 12132.                         SM1014.2
        010300 77  COPYSECT-3 PICTURE X(5) VALUE "TSTLI".                       SM1014.2
        010400 77  COPYSECT-4 PICTURE X(5) VALUE "BCOPY".                       SM1014.2
        010500*                                                                 SM1014.2
        010600*********************** COPY STATEMENT USED **********************SM1014.2
        010700*                                                                 SM1014.2
        010800*COPY  K1W04.                                                     SM1014.2
        010900*                                                                 SM1014.2
        011000******************** COPIED TEXT BEGINS BELOW ********************SM1014.2
        011100 COPY  K1W04.                                                     SM1014.2
        011200*********************** END OF COPIED TEXT ***********************SM1014.2
        011300 77  PROC-1 PICTURE 999 VALUE 123.                                SM1014.2
        011400 77  ~~>PROC-2 PICTURE 999 VALUE 456.                                SM1014.2
        011500 77  WSTR-1  PICTURE X(3) VALUE "ABC".                            SM1014.2
        011600                                                                  SM1014.2
        011700                                                                  SM1014.2
        011800                                                                  SM1014.2
        011900                                                                  SM1014.2
        012000                                                                  SM1014.2
        012100 01  WSTR-2.                                                      SM1014.2
        012200*                                                                 SM1014.2
        012300*********************** COPY STATEMENT USED **********************SM1014.2
        012400*                                                                 SM1014.2
        012500*                                            COPY K1WKA.          SM1014.2
        012600*                                                                 SM1014.2
        012700******************** COPIED TEXT BEGINS BELOW ********************SM1014.2
        012800                                             COPY K1WKA.          SM1014.2
        012900*********************** END OF COPIED TEXT ***********************SM1014.2
        013000 01  TEST-RESULTS.                                                SM1014.2
        013100     02 FILLER                   PIC X      VALUE SPACE.          SM1014.2
        013200     02 FEATURE                  PIC X(20)  VALUE SPACE.          SM1014.2
        013300     02 FILLER                   PIC X      VALUE SPACE.          SM1014.2
        013400     02 P-OR-F                   PIC X(5)   VALUE SPACE.          SM1014.2
        013500     02 FILLER                   PIC X      VALUE SPACE.          SM1014.2
        013600     02  PAR-NAME.                                                SM1014.2
        013700       03 FILLER                 PIC X(19)  VALUE SPACE.          SM1014.2
        013800       03  PARDOT-X              PIC X      VALUE SPACE.          SM1014.2
        013900       03 DOTVALUE               PIC 99     VALUE ZERO.           SM1014.2
        014000     02 FILLER                   PIC X(8)   VALUE SPACE.          SM1014.2
        014100     02 RE-MARK                  PIC X(61).                       SM1014.2
        014200 01  TEST-COMPUTED.                                               SM1014.2
        014300     02 FILLER                   PIC X(30)  VALUE SPACE.          SM1014.2
        014400     02 FILLER                   PIC X(17)  VALUE                 SM1014.2
        014500            "       COMPUTED=".                                   SM1014.2
        014600     02 COMPUTED-X.                                               SM1014.2
        014700     03 COMPUTED-A               PIC X(20)  VALUE SPACE.          SM1014.2
        014800     03 COMPUTED-N               REDEFINES COMPUTED-A             SM1014.2
        014900                                 PIC -9(9).9(9).                  SM1014.2
        015000     03 COMPUTED-0V18 REDEFINES COMPUTED-A   PIC -.9(18).         SM1014.2
        015100     03 COMPUTED-4V14 REDEFINES COMPUTED-A   PIC -9(4).9(14).     SM1014.2
        015200     03 COMPUTED-14V4 REDEFINES COMPUTED-A   PIC -9(14).9(4).     SM1014.2
        015300     03       CM-18V0 REDEFINES COMPUTED-A.                       SM1014.2
        015400         04 COMPUTED-18V0                    PIC -9(18).          SM1014.2
        015500         04 FILLER                           PIC X.               SM1014.2
        015600     03 FILLER PIC X(50) VALUE SPACE.                             SM1014.2
        015700 01  TEST-CORRECT.                                                SM1014.2
        015800     02 FILLER PIC X(30) VALUE SPACE.                             SM1014.2
        015900     02 FILLER PIC X(17) VALUE "       CORRECT =".                SM1014.2
        016000     02 CORRECT-X.                                                SM1014.2
        016100     03 CORRECT-A                  PIC X(20) VALUE SPACE.         SM1014.2
        016200     03 CORRECT-N    REDEFINES CORRECT-A     PIC -9(9).9(9).      SM1014.2
        016300     03 CORRECT-0V18 REDEFINES CORRECT-A     PIC -.9(18).         SM1014.2
        016400     03 CORRECT-4V14 REDEFINES CORRECT-A     PIC -9(4).9(14).     SM1014.2
        016500     03 CORRECT-14V4 REDEFINES CORRECT-A     PIC -9(14).9(4).     SM1014.2
        016600     03      CR-18V0 REDEFINES CORRECT-A.                         SM1014.2
        016700         04 CORRECT-18V0                     PIC -9(18).          SM1014.2
        016800         04 FILLER                           PIC X.               SM1014.2
        016900     03 FILLER PIC X(2) VALUE SPACE.                              SM1014.2
        017000     03 COR-ANSI-REFERENCE             PIC X(48) VALUE SPACE.     SM1014.2
        017100 01  CCVS-C-1.                                                    SM1014.2
        017200     02 FILLER  PIC IS X(99)    VALUE IS " FEATURE              PASM1014.2
        017300-    "SS  PARAGRAPH-NAME                                          SM1014.2
        017400-    "       REMARKS".                                            SM1014.2
        017500     02 FILLER                     PIC X(20)    VALUE SPACE.      SM1014.2
        017600 01  CCVS-C-2.                                                    SM1014.2
        017700     02 FILLER                     PIC X        VALUE SPACE.      SM1014.2
        017800     02 FILLER                     PIC X(6)     VALUE "TESTED".   SM1014.2
        017900     02 FILLER                     PIC X(15)    VALUE SPACE.      SM1014.2
        018000     02 FILLER                     PIC X(4)     VALUE "FAIL".     SM1014.2
        018100     02 FILLER                     PIC X(94)    VALUE SPACE.      SM1014.2
        018200 01  REC-SKL-SUB                   PIC 9(2)     VALUE ZERO.       SM1014.2
        018300 01  REC-CT                        PIC 99       VALUE ZERO.       SM1014.2
        018400 01  DELETE-COUNTER                PIC 999      VALUE ZERO.       SM1014.2
        018500 01  ERROR-COUNTER                 PIC 999      VALUE ZERO.       SM1014.2
        018600 01  INSPECT-COUNTER               PIC 999      VALUE ZERO.       SM1014.2
        018700 01  PASS-COUNTER                  PIC 999      VALUE ZERO.       SM1014.2
        018800 01  TOTAL-ERROR                   PIC 999      VALUE ZERO.       SM1014.2
        018900 01  ERROR-HOLD                    PIC 999      VALUE ZERO.       SM1014.2
        019000 01  DUMMY-HOLD                    PIC X(120)   VALUE SPACE.      SM1014.2
        019100 01  RECORD-COUNT                  PIC 9(5)     VALUE ZERO.       SM1014.2
        019200 01  ANSI-REFERENCE                PIC X(48)    VALUE SPACES.     SM1014.2
        019300 01  CCVS-H-1.                                                    SM1014.2
        019400     02  FILLER                    PIC X(39)    VALUE SPACES.     SM1014.2
        019500     02  FILLER                    PIC X(42)    VALUE             SM1014.2
        019600     "OFFICIAL COBOL COMPILER VALIDATION SYSTEM".                 SM1014.2
        019700     02  FILLER                    PIC X(39)    VALUE SPACES.     SM1014.2
        019800 01  CCVS-H-2A.                                                   SM1014.2
        019900   02  FILLER                        PIC X(40)  VALUE SPACE.      SM1014.2
        020000   02  FILLER                        PIC X(7)   VALUE "CCVS85 ".  SM1014.2
        020100   02  FILLER                        PIC XXXX   VALUE             SM1014.2
        020200     "4.2 ".                                                      SM1014.2
        020300   02  FILLER                        PIC X(28)  VALUE             SM1014.2
        020400            " COPY - NOT FOR DISTRIBUTION".                       SM1014.2
        020500   02  FILLER                        PIC X(41)  VALUE SPACE.      SM1014.2
        020600                                                                  SM1014.2
        020700 01  CCVS-H-2B.                                                   SM1014.2
        020800   02  FILLER                        PIC X(15)  VALUE             SM1014.2
        020900            "TEST RESULT OF ".                                    SM1014.2
        021000   02  TEST-ID                       PIC X(9).                    SM1014.2
        021100   02  FILLER                        PIC X(4)   VALUE             SM1014.2
        021200            " IN ".                                               SM1014.2
        021300   02  FILLER                        PIC X(12)  VALUE             SM1014.2
        021400     " HIGH       ".                                              SM1014.2
        021500   02  FILLER                        PIC X(22)  VALUE             SM1014.2
        021600            " LEVEL VALIDATION FOR ".                             SM1014.2
        021700   02  FILLER                        PIC X(58)  VALUE             SM1014.2
        021800     "ON-SITE VALIDATION, NATIONAL INSTITUTE OF STD & TECH.     ".SM1014.2
        021900 01  CCVS-H-3.                                                    SM1014.2
        022000     02  FILLER                      PIC X(34)  VALUE             SM1014.2
        022100            " FOR OFFICIAL USE ONLY    ".                         SM1014.2
        022200     02  FILLER                      PIC X(58)  VALUE             SM1014.2
        022300     "COBOL 85 VERSION 4.2, Apr  1993 SSVG                      ".SM1014.2
        022400     02  FILLER                      PIC X(28)  VALUE             SM1014.2
        022500            "  COPYRIGHT   1985 ".                                SM1014.2
        022600 01  CCVS-E-1.                                                    SM1014.2
        022700     02 FILLER                       PIC X(52)  VALUE SPACE.      SM1014.2
        022800     02 FILLER  PIC X(14) VALUE IS "END OF TEST-  ".              SM1014.2
        022900     02 ID-AGAIN                     PIC X(9).                    SM1014.2
        023000     02 FILLER                       PIC X(45)  VALUE SPACES.     SM1014.2
        023100 01  CCVS-E-2.                                                    SM1014.2
        023200     02  FILLER                      PIC X(31)  VALUE SPACE.      SM1014.2
        023300     02  FILLER                      PIC X(21)  VALUE SPACE.      SM1014.2
        023400     02 CCVS-E-2-2.                                               SM1014.2
        023500         03 ERROR-TOTAL              PIC XXX    VALUE SPACE.      SM1014.2
        023600         03 FILLER                   PIC X      VALUE SPACE.      SM1014.2
        023700         03 ENDER-DESC               PIC X(44)  VALUE             SM1014.2
        023800            "ERRORS ENCOUNTERED".                                 SM1014.2
        023900 01  CCVS-E-3.                                                    SM1014.2
        024000     02  FILLER                      PIC X(22)  VALUE             SM1014.2
        024100            " FOR OFFICIAL USE ONLY".                             SM1014.2
        024200     02  FILLER                      PIC X(12)  VALUE SPACE.      SM1014.2
        024300     02  FILLER                      PIC X(58)  VALUE             SM1014.2
        024400     "ON-SITE VALIDATION, NATIONAL INSTITUTE OF STD & TECH.     ".SM1014.2
        024500     02  FILLER                      PIC X(13)  VALUE SPACE.      SM1014.2
        024600     02 FILLER                       PIC X(15)  VALUE             SM1014.2
        024700             " COPYRIGHT 1985".                                   SM1014.2
        024800 01  CCVS-E-4.                                                    SM1014.2
        024900     02 CCVS-E-4-1                   PIC XXX    VALUE SPACE.      SM1014.2
        025000     02 FILLER                       PIC X(4)   VALUE " OF ".     SM1014.2
        025100     02 CCVS-E-4-2                   PIC XXX    VALUE SPACE.      SM1014.2
        025200     02 FILLER                       PIC X(40)  VALUE             SM1014.2
        025300      "  TESTS WERE EXECUTED SUCCESSFULLY".                       SM1014.2
        025400 01  XXINFO.                                                      SM1014.2
        025500     02 FILLER                       PIC X(19)  VALUE             SM1014.2
        025600            "*** INFORMATION ***".                                SM1014.2
        025700     02 INFO-TEXT.                                                SM1014.2
        025800       04 FILLER                     PIC X(8)   VALUE SPACE.      SM1014.2
        025900       04 XXCOMPUTED                 PIC X(20).                   SM1014.2
        026000       04 FILLER                     PIC X(5)   VALUE SPACE.      SM1014.2
        026100       04 XXCORRECT                  PIC X(20).                   SM1014.2
        026200     02 INF-ANSI-REFERENCE           PIC X(48).                   SM1014.2
        026300 01  HYPHEN-LINE.                                                 SM1014.2
        026400     02 FILLER  PIC IS X VALUE IS SPACE.                          SM1014.2
        026500     02 FILLER  PIC IS X(65)    VALUE IS "************************SM1014.2
        026600-    "*****************************************".                 SM1014.2
        026700     02 FILLER  PIC IS X(54)    VALUE IS "************************SM1014.2
        026800-    "******************************".                            SM1014.2
        026900 01  CCVS-PGM-ID                     PIC X(9)   VALUE             SM1014.2
        027000     "SM101A".                                                    SM1014.2
        027100 PROCEDURE DIVISION.                                              SM1014.2
        027200 CCVS1 SECTION.                                                   SM1014.2
        027300 OPEN-FILES.                                                      SM1014.2
        027400     OPEN     OUTPUT PRINT-FILE.                                  SM1014.2
        027500     MOVE CCVS-PGM-ID TO TEST-ID. MOVE CCVS-PGM-ID TO ID-AGAIN.   SM1014.2
        027600     MOVE    SPACE TO TEST-RESULTS.                               SM1014.2
        027700     PERFORM  HEAD-ROUTINE THRU COLUMN-NAMES-ROUTINE.             SM1014.2
        027800     GO TO CCVS1-EXIT.                                            SM1014.2
        027900 CLOSE-FILES.                                                     SM1014.2
        028000     PERFORM END-ROUTINE THRU END-ROUTINE-13. CLOSE PRINT-FILE.   SM1014.2
        028100 TERMINATE-CCVS.                                                  SM1014.2
        028200S    EXIT PROGRAM.                                                SM1014.2
        028300STERMINATE-CALL.                                                  SM1014.2
        028400     STOP     RUN.                                                SM1014.2
        028500 INSPT. MOVE "INSPT" TO P-OR-F. ADD 1 TO INSPECT-COUNTER.         SM1014.2
        028600 PASS.  MOVE "PASS " TO P-OR-F.  ADD 1 TO PASS-COUNTER.           SM1014.2
        028700 FAIL.  MOVE "FAIL*" TO P-OR-F.  ADD 1 TO ERROR-COUNTER.          SM1014.2
        028800 DE-LETE.  MOVE "*****" TO P-OR-F.  ADD 1 TO DELETE-COUNTER.      SM1014.2
        028900     MOVE "****TEST DELETED****" TO RE-MARK.                      SM1014.2
        029000 PRINT-DETAIL.                                                    SM1014.2
        029100     IF REC-CT NOT EQUAL TO ZERO                                  SM1014.2
        029200             MOVE "." TO PARDOT-X                                 SM1014.2
        029300             MOVE REC-CT TO DOTVALUE.                             SM1014.2
        029400     MOVE     TEST-RESULTS TO PRINT-REC. PERFORM WRITE-LINE.      SM1014.2
        029500     IF P-OR-F EQUAL TO "FAIL*"  PERFORM WRITE-LINE               SM1014.2
        029600        PERFORM FAIL-ROUTINE THRU FAIL-ROUTINE-EX                 SM1014.2
        029700          ELSE PERFORM BAIL-OUT THRU BAIL-OUT-EX.                 SM1014.2
        029800     MOVE SPACE TO P-OR-F. MOVE SPACE TO COMPUTED-X.              SM1014.2
        029900     MOVE SPACE TO CORRECT-X.                                     SM1014.2
        030000     IF     REC-CT EQUAL TO ZERO  MOVE SPACE TO PAR-NAME.         SM1014.2
        030100     MOVE     SPACE TO RE-MARK.                                   SM1014.2
        030200 HEAD-ROUTINE.                                                    SM1014.2
        030300     MOVE CCVS-H-1  TO DUMMY-RECORD. PERFORM WRITE-LINE 2 TIMES.  SM1014.2
        030400     MOVE CCVS-H-2A TO DUMMY-RECORD. PERFORM WRITE-LINE 2 TIMES.  SM1014.2
        030500     MOVE CCVS-H-2B TO DUMMY-RECORD. PERFORM WRITE-LINE 3 TIMES.  SM1014.2
        030600     MOVE CCVS-H-3  TO DUMMY-RECORD. PERFORM WRITE-LINE 3 TIMES.  SM1014.2
        030700 COLUMN-NAMES-ROUTINE.                                            SM1014.2
        030800     MOVE CCVS-C-1 TO DUMMY-RECORD. PERFORM WRITE-LINE.           SM1014.2
        030900     MOVE CCVS-C-2 TO DUMMY-RECORD. PERFORM WRITE-LINE 2 TIMES.   SM1014.2
        031000     MOVE HYPHEN-LINE TO DUMMY-RECORD. PERFORM WRITE-LINE.        SM1014.2
        031100 END-ROUTINE.                                                     SM1014.2
        031200     MOVE HYPHEN-LINE TO DUMMY-RECORD. PERFORM WRITE-LINE 5 TIMES.SM1014.2
        031300 END-RTN-EXIT.                                                    SM1014.2
        031400     MOVE CCVS-E-1 TO DUMMY-RECORD. PERFORM WRITE-LINE 2 TIMES.   SM1014.2
        031500 END-ROUTINE-1.                                                   SM1014.2
        031600      ADD ERROR-COUNTER TO ERROR-HOLD ADD INSPECT-COUNTER TO      SM1014.2
        031700      ERROR-HOLD. ADD DELETE-COUNTER TO ERROR-HOLD.               SM1014.2
        031800      ADD PASS-COUNTER TO ERROR-HOLD.                             SM1014.2
        031900*     IF PASS-COUNTER EQUAL TO ERROR-HOLD GO TO END-ROUTINE-12.   SM1014.2
        032000      MOVE PASS-COUNTER TO CCVS-E-4-1.                            SM1014.2
        032100      MOVE ERROR-HOLD TO CCVS-E-4-2.                              SM1014.2
        032200      MOVE CCVS-E-4 TO CCVS-E-2-2.                                SM1014.2
        032300      MOVE CCVS-E-2 TO DUMMY-RECORD PERFORM WRITE-LINE.           SM1014.2
        032400  END-ROUTINE-12.                                                 SM1014.2
        032500      MOVE "TEST(S) FAILED" TO ENDER-DESC.                        SM1014.2
        032600     IF       ERROR-COUNTER IS EQUAL TO ZERO                      SM1014.2
        032700         MOVE "NO " TO ERROR-TOTAL                                SM1014.2
        032800         ELSE                                                     SM1014.2
        032900         MOVE ERROR-COUNTER TO ERROR-TOTAL.                       SM1014.2
        033000     MOVE     CCVS-E-2 TO DUMMY-RECORD.                           SM1014.2
        033100     PERFORM WRITE-LINE.                                          SM1014.2
        033200 END-ROUTINE-13.                                                  SM1014.2
        033300     IF DELETE-COUNTER IS EQUAL TO ZERO                           SM1014.2
        033400         MOVE "NO " TO ERROR-TOTAL  ELSE                          SM1014.2
        033500         MOVE DELETE-COUNTER TO ERROR-TOTAL.                      SM1014.2
        033600     MOVE "TEST(S) DELETED     " TO ENDER-DESC.                   SM1014.2
        033700     MOVE CCVS-E-2 TO DUMMY-RECORD. PERFORM WRITE-LINE.           SM1014.2
        033800      IF   INSPECT-COUNTER EQUAL TO ZERO                          SM1014.2
        033900          MOVE "NO " TO ERROR-TOTAL                               SM1014.2
        034000      ELSE MOVE INSPECT-COUNTER TO ERROR-TOTAL.                   SM1014.2
        034100      MOVE "TEST(S) REQUIRE INSPECTION" TO ENDER-DESC.            SM1014.2
        034200      MOVE CCVS-E-2 TO DUMMY-RECORD. PERFORM WRITE-LINE.          SM1014.2
        034300     MOVE CCVS-E-3 TO DUMMY-RECORD. PERFORM WRITE-LINE.           SM1014.2
        034400 WRITE-LINE.                                                      SM1014.2
        034500     ADD 1 TO RECORD-COUNT.                                       SM1014.2
        034600Y    IF RECORD-COUNT GREATER 50                                   SM1014.2
        034700Y        MOVE DUMMY-RECORD TO DUMMY-HOLD                          SM1014.2
        034800Y        MOVE SPACE TO DUMMY-RECORD                               SM1014.2
        034900Y        WRITE DUMMY-RECORD AFTER ADVANCING PAGE                  SM1014.2
        035000Y        MOVE CCVS-C-1 TO DUMMY-RECORD PERFORM WRT-LN             SM1014.2
        035100Y        MOVE CCVS-C-2 TO DUMMY-RECORD PERFORM WRT-LN 2 TIMES     SM1014.2
        035200Y        MOVE HYPHEN-LINE TO DUMMY-RECORD PERFORM WRT-LN          SM1014.2
        035300Y        MOVE DUMMY-HOLD TO DUMMY-RECORD                          SM1014.2
        035400Y        MOVE ZERO TO RECORD-COUNT.                               SM1014.2
        035500     PERFORM WRT-LN.                                              SM1014.2
        035600 WRT-LN.                                                          SM1014.2
        035700     WRITE    DUMMY-RECORD AFTER ADVANCING 1 LINES.               SM1014.2
        035800     MOVE SPACE TO DUMMY-RECORD.                                  SM1014.2
        035900 BLANK-LINE-PRINT.                                                SM1014.2
        036000     PERFORM WRT-LN.                                              SM1014.2
        036100 FAIL-ROUTINE.                                                    SM1014.2
        036200     IF   COMPUTED-X NOT EQUAL TO SPACE GO TO FAIL-ROUTINE-WRITE. SM1014.2
        036300     IF     CORRECT-X NOT EQUAL TO SPACE GO TO FAIL-ROUTINE-WRITE.SM1014.2
        036400     MOVE   ANSI-REFERENCE TO INF-ANSI-REFERENCE.                 SM1014.2
        036500     MOVE  "NO FURTHER INFORMATION, SEE PROGRAM." TO INFO-TEXT.   SM1014.2
        036600     MOVE   XXINFO TO DUMMY-RECORD. PERFORM WRITE-LINE 2 TIMES.   SM1014.2
        036700     MOVE   SPACES TO INF-ANSI-REFERENCE.                         SM1014.2
        036800     GO TO  FAIL-ROUTINE-EX.                                      SM1014.2
        036900 FAIL-ROUTINE-WRITE.                                              SM1014.2
        037000     MOVE   TEST-COMPUTED TO PRINT-REC PERFORM WRITE-LINE         SM1014.2
        037100     MOVE   ANSI-REFERENCE TO COR-ANSI-REFERENCE.                 SM1014.2
        037200     MOVE   TEST-CORRECT TO PRINT-REC PERFORM WRITE-LINE 2 TIMES. SM1014.2
        037300     MOVE   SPACES TO COR-ANSI-REFERENCE.                         SM1014.2
        037400 FAIL-ROUTINE-EX. EXIT.                                           SM1014.2
        037500 BAIL-OUT.                                                        SM1014.2
        037600     IF     COMPUTED-A NOT EQUAL TO SPACE GO TO BAIL-OUT-WRITE.   SM1014.2
        037700     IF     CORRECT-A EQUAL TO SPACE GO TO BAIL-OUT-EX.           SM1014.2
        037800 BAIL-OUT-WRITE.                                                  SM1014.2
        037900     MOVE CORRECT-A TO XXCORRECT. MOVE COMPUTED-A TO XXCOMPUTED.  SM1014.2
        038000     MOVE   ANSI-REFERENCE TO INF-ANSI-REFERENCE.                 SM1014.2
        038100     MOVE   XXINFO TO DUMMY-RECORD. PERFORM WRITE-LINE 2 TIMES.   SM1014.2
        038200     MOVE   SPACES TO INF-ANSI-REFERENCE.                         SM1014.2
        038300 BAIL-OUT-EX. EXIT.                                               SM1014.2
        038400 CCVS1-EXIT.                                                      SM1014.2
        038500     EXIT.                                                        SM1014.2
        038600 INITIALIZATION SECTION.                                          SM1014.2
        038700 SM101A-INIT.                                                     SM1014.2
        038800     OPEN     OUTPUT TEST-FILE.                                   SM1014.2
        038900     MOVE     "OUTPUT OF SM101A IS USED AS" TO RE-MARK.           SM1014.2
        039000     PERFORM  PRINT-DETAIL.                                       SM1014.2
        039100     MOVE     "INPUT FOR SM102A."           TO RE-MARK.           SM1014.2
        039200     PERFORM  PRINT-DETAIL.                                       SM1014.2
        039300     MOVE     "COPY ---" TO FEATURE.                              SM1014.2
        039400     PERFORM  PRINT-DETAIL.                                       SM1014.2
        039500 WORKING-STORAGE-TEST SECTION.                                    SM1014.2
        039600 COPY-TEST-1.                                                     SM1014.2
        039700     IF       WSTR-1 EQUAL TO WSTR-2                              SM1014.2
        039800              PERFORM PASS GO TO COPY-WRITE-1.                    SM1014.2
        039900*        NOTE TESTS COPYING OF WORKING-STORAGE ENTRIES.           SM1014.2
        040000     GO       TO COPY-FAIL-1.                                     SM1014.2
        040100 COPY-DELETE-1.                                                   SM1014.2
        040200     PERFORM  DE-LETE.                                            SM1014.2
        040300     GO       TO COPY-WRITE-1.                                    SM1014.2
        040400 COPY-FAIL-1.                                                     SM1014.2
        040500     MOVE     WSTR-2 TO COMPUTED-A.                               SM1014.2
        040600     MOVE     "ABC" TO CORRECT-A                                  SM1014.2
        040700     PERFORM  FAIL.                                               SM1014.2
        040800 COPY-WRITE-1.                                                    SM1014.2
        040900     MOVE     "  WKNG-STORAGE ENTRY" TO FEATURE                   SM1014.2
        041000     MOVE     "COPY-TEST-1 " TO PAR-NAME.                         SM1014.2
        041100     PERFORM  PRINT-DETAIL.                                       SM1014.2
        041200 PARAGRAPH-TEST SECTION.                                          SM1014.2
        041300 COPY-TEST-2.                                                     SM1014.2
        041400                                                                  SM1014.2
        041500                                                                  SM1014.2
        041600                                                                  SM1014.2
        041700                                                                  SM1014.2
        041800                                                                  SM1014.2
        041900*                                                                 SM1014.2
        042000*********************** COPY STATEMENT USED **********************SM1014.2
        042100*                                                                 SM1014.2
        042200*                                                     COPY K1PRA. SM1014.2
        042300*                                                                 SM1014.2
        042400******************** COPIED TEXT BEGINS BELOW ********************SM1014.2
        042500                                                       COPY K1PRA.SM1014.2
        042600*********************** END OF COPIED TEXT ***********************SM1014.2
        042700 COPY-TESTT-2.                                                    SM1014.2
        042800     IF       PROC-1 EQUAL TO ~~>PROC-2                              SM1014.2
        042900              PERFORM PASS GO TO COPY-WRITE-2.                    SM1014.2
        043000*        NOTE TESTS COPYING OF A PROCEDURE DIVISION STATEMENT.    SM1014.2
        043100     GO       TO COPY-FAIL-2.                                     SM1014.2
        043200 COPY-DELETE-2.                                                   SM1014.2
        043300     PERFORM  DE-LETE.                                            SM1014.2
        043400     GO       TO COPY-WRITE-2.                                    SM1014.2
        043500 COPY-FAIL-2.                                                     SM1014.2
        043600     MOVE     ~~>PROC-2 TO COMPUTED-N.                               SM1014.2
        043700     MOVE     123 TO CORRECT-N.                                   SM1014.2
        043800     PERFORM  FAIL.                                               SM1014.2
        043900 COPY-WRITE-2.                                                    SM1014.2
        044000     MOVE     "  PROCEDURE" TO FEATURE                            SM1014.2
        044100     MOVE     "COPY-TEST-2 " TO PAR-NAME.                         SM1014.2
        044200     PERFORM  PRINT-DETAIL.                                       SM1014.2
        044300 SECTION-TEST SECTION.                                            SM1014.2
        044400                                                                  SM1014.2
        044500                                                                  SM1014.2
        044600                                                                  SM1014.2
        044700                                                                  SM1014.2
        044800                                                                  SM1014.2
        044900*                                                                 SM1014.2
        045000*********************** COPY STATEMENT USED **********************SM1014.2
        045100*                                                                 SM1014.2
        045200*                                                     COPY K1SEA. SM1014.2
        045300*                                                                 SM1014.2
        045400******************** COPIED TEXT BEGINS BELOW ********************SM1014.2
        045500                                                       COPY K1SEA.SM1014.2
        045600D                                                      COPY K1SEA.SM1014.2
        045700*********************** END OF COPIED TEXT ***********************SM1014.2
        045800 COPY-INIT-A.                                                     SM1014.2
        045900     MOVE     "  SECTION" TO FEATURE.                             SM1014.2
        046000 COPY-TEST-3.                                                     SM1014.2
        046100     IF       COPYSECT-1 EQUAL TO 95427                           SM1014.2
        046200              PERFORM PASS GO TO COPY-WRITE-3.                    SM1014.2
        046300*        NOTE COPY-TEST-3, 4, 5, 6 TEST THE COPYING OF AN         SM1014.2
        046400*             ENTIRE SECTION.                                     SM1014.2
        046500     GO       TO COPY-FAIL-3.                                     SM1014.2
        046600 COPY-DELETE-3.                                                   SM1014.2
        046700     PERFORM  DE-LETE.                                            SM1014.2
        046800     GO       TO COPY-WRITE-3.                                    SM1014.2
        046900 COPY-FAIL-3.                                                     SM1014.2
        047000     MOVE     COPYSECT-1 TO COMPUTED-N.                           SM1014.2
        047100     MOVE     95427   TO CORRECT-N.                               SM1014.2
        047200     PERFORM  FAIL.                                               SM1014.2
        047300 COPY-WRITE-3.                                                    SM1014.2
        047400     MOVE     "COPY-TEST-3 " TO PAR-NAME.                         SM1014.2
        047500     PERFORM  PRINT-DETAIL.                                       SM1014.2
        047600 COPY-TEST-4.                                                     SM1014.2
        047700     IF       COPYSECT-2 EQUAL TO 23121                           SM1014.2
        047800              PERFORM PASS GO TO COPY-WRITE-4.                    SM1014.2
        047900     GO       TO COPY-FAIL-4.                                     SM1014.2
        048000 COPY-DELETE-4.                                                   SM1014.2
        048100     PERFORM  DE-LETE.                                            SM1014.2
        048200     GO       TO COPY-WRITE-4.                                    SM1014.2
        048300 COPY-FAIL-4.                                                     SM1014.2
        048400     MOVE     COPYSECT-2 TO COMPUTED-N.                           SM1014.2
        048500     MOVE     23121   TO CORRECT-N.                               SM1014.2
        048600     PERFORM  FAIL.                                               SM1014.2
        048700 COPY-WRITE-4.                                                    SM1014.2
        048800     MOVE     "COPY-TEST-4 " TO PAR-NAME.                         SM1014.2
        048900     PERFORM  PRINT-DETAIL.                                       SM1014.2
        049000 COPY-TEST-5.                                                     SM1014.2
        049100     IF       COPYSECT-3 EQUAL TO "LIBCO"                         SM1014.2
        049200              PERFORM PASS GO TO COPY-WRITE-5.                    SM1014.2
        049300     GO       TO COPY-FAIL-5.                                     SM1014.2
        049400 COPY-DELETE-5.                                                   SM1014.2
        049500     PERFORM  DE-LETE.                                            SM1014.2
        049600     GO       TO COPY-WRITE-5.                                    SM1014.2
        049700 COPY-FAIL-5.                                                     SM1014.2
        049800     MOVE     COPYSECT-3 TO COMPUTED-A.                           SM1014.2
        049900     MOVE     "LIBCO" TO CORRECT-A.                               SM1014.2
        050000     PERFORM  FAIL.                                               SM1014.2
        050100 COPY-WRITE-5.                                                    SM1014.2
        050200     MOVE     "COPY-TEST-5 " TO PAR-NAME.                         SM1014.2
        050300     PERFORM  PRINT-DETAIL.                                       SM1014.2
        050400 COPY-TEST-6.                                                     SM1014.2
        050500     IF       COPYSECT-4 EQUAL TO "PYTST"                         SM1014.2
        050600              PERFORM PASS GO TO COPY-WRITE-6.                    SM1014.2
        050700     GO       TO COPY-FAIL-6.                                     SM1014.2
        050800 COPY-DELETE-6.                                                   SM1014.2
        050900     PERFORM  DE-LETE.                                            SM1014.2
        051000     GO       TO COPY-WRITE-6.                                    SM1014.2
        051100 COPY-FAIL-6.                                                     SM1014.2
        051200     MOVE     COPYSECT-4 TO COMPUTED-A.                           SM1014.2
        051300     MOVE     "PYTST" TO CORRECT-A.                               SM1014.2
        051400     PERFORM  FAIL.                                               SM1014.2
        051500 COPY-WRITE-6.                                                    SM1014.2
        051600     MOVE     "COPY-TEST-6 " TO PAR-NAME.                         SM1014.2
        051700     PERFORM  PRINT-DETAIL.                                       SM1014.2
        051800 BUILD SECTION.                                                   SM1014.2
        051900 COPY-TEST-7.                                                     SM1014.2
        052000     MOVE     RCD-1 TO TST-FLD-1.                                 SM1014.2
        052100     WRITE    TST-TEST.                                           SM1014.2
        052200     MOVE     RCD-2 TO TST-FLD-1.                                 SM1014.2
        052300     WRITE    TST-TEST.                                           SM1014.2
        052400     MOVE     RCD-3 TO TST-FLD-1.                                 SM1014.2
        052500     WRITE    TST-TEST.                                           SM1014.2
        052600     MOVE     RCD-4 TO TST-FLD-1.                                 SM1014.2
        052700     WRITE    TST-TEST.                                           SM1014.2
        052800     MOVE     RCD-5 TO TST-FLD-1.                                 SM1014.2
        052900     WRITE    TST-TEST.                                           SM1014.2
        053000     MOVE     RCD-6 TO TST-FLD-1.                                 SM1014.2
        053100     WRITE    TST-TEST.                                           SM1014.2
        053200     MOVE     RCD-7 TO TST-FLD-1.                                 SM1014.2
        053300     WRITE    TST-TEST.                                           SM1014.2
        053400     PERFORM  PASS.                                               SM1014.2
        053500     GO       TO COPY-WRITE-7.                                    SM1014.2
        053600 COPY-DELETE-7.                                                   SM1014.2
        053700     PERFORM  DE-LETE.                                            SM1014.2
        053800 COPY-WRITE-7.                                                    SM1014.2
        053900     MOVE     "  FILE DESCRIPTION" TO FEATURE.                    SM1014.2
        054000     MOVE     "COPY-TEST-7" TO PAR-NAME.                          SM1014.2
        054100     MOVE     "OUTPUT CHECKED IN SM102A" TO RE-MARK.              SM1014.2
        054200     PERFORM  PRINT-DETAIL.                                       SM1014.2
        054300 COPY-TEST-8.                                                     SM1014.2
        054400*                                                                 SM1014.2
        054500*********************** COPY STATEMENT USED **********************SM1014.2
        054600*                                                                 SM1014.2
        054700*    ADD     COPY K1P01. TO WRK-DS-05V00.                         SM1014.2
        054800*                                                                 SM1014.2
        054900******************** COPIED TEXT BEGINS BELOW ********************SM1014.2
        055000     ADD     COPY K1P01. TO WRK-DS-05V00.                         SM1014.2
        055100*********************** END OF COPIED TEXT ***********************SM1014.2
        055200     IF       WRK-DS-05V00 EQUAL TO 97523                         SM1014.2
        055300             PERFORM PASS                                         SM1014.2
        055400             GO TO COPY-WRITE-8.                                  SM1014.2
        055500     GO TO    COPY-FAIL-8.                                        SM1014.2
        055600 COPY-DELETE-8.                                                   SM1014.2
        055700     PERFORM DE-LETE.                                             SM1014.2
        055800     GO TO    COPY-WRITE-8.                                       SM1014.2
        055900 COPY-FAIL-8.                                                     SM1014.2
        056000     MOVE    WRK-DS-05V00 TO COMPUTED-N.                          SM1014.2
        056100     MOVE    97523        TO CORRECT-N.                           SM1014.2
        056200     PERFORM FAIL.                                                SM1014.2
        056300 COPY-WRITE-8.                                                    SM1014.2
        056400     MOVE     "COPY-TEST-8" TO PAR-NAME.                          SM1014.2
        056500     PERFORM PRINT-DETAIL.                                        SM1014.2
        056600     CLOSE    TEST-FILE.                                          SM1014.2
        056700 CCVS-EXIT SECTION.                                               SM1014.2
        056800 CCVS-999999.                                                     SM1014.2
        056900     GO TO CLOSE-FILES.                                           SM1014.2
        """;
}
