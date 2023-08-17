/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.cobol.tree.preprocessor;

import org.junit.jupiter.api.Test;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Issue;
import org.openrewrite.cobol.CobolTest;
import org.openrewrite.cobol.internal.CobolPreprocessorPrinter;

import static org.openrewrite.cobol.Assertions.copybook;

public class CobolPreprocessorCopybookTest extends CobolTest {
    private final CobolPreprocessorPrinter<ExecutionContext> printer = new CobolPreprocessorPrinter<>(false, true);

    @Test
    void newLineInContentArea() {
        rewriteRun(
          copybook(
            """
              000001 IDENTIFICATION  DIVISION  .
              000002 PROGRAM-ID    . HELLO     .
              000003 PROCEDURE DIVISION        .
              000004 DISPLAY 'Hello world!'    .
              000005 STOP RUN                  .
              """
          )
        );
    }

    @Issue("https://github.com/openrewrite/rewrite-cobol/issues/24")
    @Test
    void emptyIndicatorArea() {
        rewriteRun(
          copybook(
            """
              000100*******************************************************************
              000200
              000300     PERFORM FAIL.
              000400     SUBTRACT 1 FROM ERROR-COUNTER.
              000500     MOVE "TEXT COPIED FROM WRONG LIBRARY" TO RE-MARK.
              """
          )
        );
    }

    @Issue("https://github.com/openrewrite/rewrite-cobol/issues/27")
    @Test
    void newLineInContentAreaBeforeCopybook() {
        rewriteRun(
          copybook(getNistResource("ISSUE_27.CBL"))
        );
    }

    @Test
    void altl1() {
        rewriteRun(
          copybook(getNistResource("ALTL1.CPY"))
        );
    }

    @Test
    void altlb() {
        rewriteRun(
          copybook(getNistResource("ALTLB.CPY"))
        );
    }

    @Test
    void k1daa() {
        rewriteRun(
          copybook(getNistResource("K1DAA.CPY"))
        );
    }

    @Test
    void k1fda() {
        rewriteRun(
          copybook(getNistResource("K1FDA.CPY"))
        );
    }

    @Test
    void k1p01() {
        rewriteRun(
          copybook(getNistResource("K1P01.CPY"))
        );
    }

    @Test
    void k1pra() {
        rewriteRun(
          copybook(getNistResource("K1PRA.CPY"))
        );
    }

    @Test
    void k1prb() {
        rewriteRun(
          copybook(getNistResource("K1PRB.CPY"))
        );
    }

    @Test
    void k1prc() {
        rewriteRun(
          copybook(getNistResource("K1PRC.CPY"))
        );
    }

    @Test
    void k1sea() {
        rewriteRun(
          copybook(getNistResource("K1SEA.CPY"))
        );
    }

    @Test
    void k1w01() {
        rewriteRun(
          copybook(getNistResource("K1W01.CPY"))
        );
    }

    @Test
    void k1w02() {
        rewriteRun(
          copybook(getNistResource("K1W02.CPY"))
        );
    }

    @Test
    void k1w03() {
        rewriteRun(
          copybook(getNistResource("K1W03.CPY"))
        );
    }

    @Test
    void k1w04() {
        rewriteRun(
          copybook(getNistResource("K1W04.CPY"))
        );
    }

    @Test
    void k1wka() {
        rewriteRun(
          copybook(getNistResource("K1WKA.CPY"))
        );
    }

    @Test
    void k1wkb() {
        rewriteRun(
          copybook(getNistResource("K1WKB.CPY"))
        );
    }

    @Test
    void k1wkc() {
        rewriteRun(
          copybook(getNistResource("K1WKC.CPY"))
        );
    }

    @Test
    void k1wky() {
        rewriteRun(
          copybook(getNistResource("K1WKY.CPY"))
        );
    }

    @Test
    void k1wkz() {
        rewriteRun(
          copybook(getNistResource("K1WKZ.CPY"))
        );
    }

    @Test
    void k2pra() {
        rewriteRun(
          copybook(getNistResource("K2PRA.CPY"))
        );
    }

    @Test
    void k3fca() {
        rewriteRun(
          copybook(getNistResource("K3FCA.CPY"))
        );
    }

    @Test
    void k3fcb() {
        rewriteRun(
          copybook(getNistResource("K3FCB.CPY"))
        );
    }

    @Test
    void k3ioa() {
        rewriteRun(
          copybook(getNistResource("K3IOA.CPY"))
        );
    }

    @Test
    void k3iob() {
        rewriteRun(
          copybook(getNistResource("K3IOB.CPY"))
        );
    }

    @Test
    void k3lge() {
        rewriteRun(
          copybook(getNistResource("K3LGE.CPY"))
        );
    }

    @Test
    void k3oca() {
        rewriteRun(
          copybook(getNistResource("K3OCA.CPY"))
        );
    }

    @Test
    void k3sml() {
        rewriteRun(
          copybook(getNistResource("K3SML.CPY"))
        );
    }

    @Test
    void k3sna() {
        rewriteRun(
          copybook(getNistResource("K3SNA.CPY"))
        );
    }

    @Test
    void k3snb() {
        rewriteRun(
          copybook(getNistResource("K3SNB.CPY"))
        );
    }

    @Test
    void k5sda() {
        rewriteRun(
          copybook(getNistResource("K5SDA.CPY"))
        );
    }

    @Test
    void k6sca() {
        rewriteRun(
          copybook(getNistResource("K6SCA.CPY"))
        );
    }

    @Test
    void k7sea() {
        rewriteRun(
          copybook(getNistResource("K7SEA.CPY"))
        );
    }

    @Test
    void k501A() {
        rewriteRun(
          copybook(getNistResource("K501A.CPY"))
        );
    }

    @Test
    void k501B() {
        rewriteRun(
          copybook(getNistResource("K501B.CPY"))
        );
    }

    @Test
    void kk208A() {
        rewriteRun(
          copybook(getNistResource("KK208A.CPY"))
        );
    }

    @Test
    void kp001() {
        rewriteRun(
          copybook(getNistResource("KP001.CPY"))
        );
    }

    @Test
    void kp002() {
        rewriteRun(
          copybook(getNistResource("KP002.CPY"))
        );
    }

    @Test
    void kp003() {
        rewriteRun(
          copybook(getNistResource("KP003.CPY"))
        );
    }

    @Test
    void kp004() {
        rewriteRun(
          copybook(getNistResource("KP004.CPY"))
        );
    }

    @Test
    void kp005() {
        rewriteRun(
          copybook(getNistResource("KP005.CPY"))
        );
    }

    @Test
    void kp006() {
        rewriteRun(
          copybook(getNistResource("KP006.CPY"))
        );
    }

    @Test
    void kp007() {
        rewriteRun(
          copybook(getNistResource("KP007.CPY"))
        );
    }

    @Test
    void kp008() {
        rewriteRun(
          copybook(getNistResource("KP008.CPY"))
        );
    }

    @Test
    void kp009() {
        rewriteRun(
          copybook(getNistResource("KP009.CPY"))
        );
    }

    @Test
    void kp010() {
        rewriteRun(
          copybook(getNistResource("KP010.CPY"))
        );
    }

    @Test
    void ksm31() {
        rewriteRun(
          copybook(getNistResource("KSM31.CPY"))
        );
    }

    @Test
    void ksm41() {
        rewriteRun(
          copybook(getNistResource("KSM41.CPY"))
        );
    }

    @Test
    void trailingSub() {
        rewriteRun(
          copybook(getNistResource("K1WKA_TRAILING_SUB.CPY"))
        );
    }
}
