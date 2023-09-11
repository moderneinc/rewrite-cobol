/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.jcl.tree;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.jcl.tree.ParserAssertions.jcl;

public class UnknownTest implements RewriteTest {

    @Test
    void unknown() {
        rewriteRun(
          jcl(
            """
              //* %%RANGE 1 42
                TRACE OFF
                MAXCOMMAND 42
                CALLMEM SET42
                CALLMEM SET42
                 SETOLOC %%MAXRC = 42
              %%RANGE 1 42
              CALLMEM ORDERJOB %%LIBRO LCOPYJCL ALL
              %%DATTR FORCE C
              %% RANGE 1 42
              """
          )
        );
    }

    @Test
    void unknownWithCommentAreas() {
        rewriteRun(
          jcl(
            """
              //* %%RANGE 1 42                                                        commentArea
                                                                                      commentArea
                TRACE OFF                                                             commentArea
                MAXCOMMAND 42                                                         commentArea
                CALLMEM SET42                                                         commentArea
                CALLMEM SET42                                                         commentArea
                 SETOLOC %%MAXRC = 42                                                 commentArea
              %%RANGE 1 42                                                            commentArea
              CALLMEM ORDERJOB %%LIBRO LCOPYJCL ALL                                   commentArea
              %%DATTR FORCE C                                                         commentArea
              %% RANGE 1 42                                                           commentArea
              """
          )
        );
    }
}
