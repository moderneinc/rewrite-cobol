/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.cobol;

import org.junit.jupiter.api.Test;

import static org.openrewrite.cobol.Assertions.cobol;

public class BadBoysTest extends CobolTest {

    @Test
    void NC246A() {
        rewriteRun(
          cobol(getNistResource("NC246A.CBL"))
        );
    }
}
