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
