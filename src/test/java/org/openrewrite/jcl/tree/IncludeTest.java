/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.jcl.tree;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.jcl.tree.ParserAssertions.jcl;

class IncludeTest implements RewriteTest {
    @ParameterizedTest
    @ValueSource(
      strings = {
        "NAME INCLUDE",
        "     INCLUDE",
        "     INCLUDE                                                          commentArea"
      }
    )
    void include(String input) {
        rewriteRun(
          jcl(
            "//%s".formatted(input)
          )
        );
    }

    @Test
    void member() {
        rewriteRun(
          jcl("//Name INCLUDE MEMBER=ALPHA.PGM")
        );
    }
}
