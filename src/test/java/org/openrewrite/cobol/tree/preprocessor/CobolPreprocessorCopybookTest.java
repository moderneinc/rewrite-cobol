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
package org.openrewrite.cobol.tree.preprocessor;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Issue;
import org.openrewrite.Parser;
import org.openrewrite.SourceFile;
import org.openrewrite.cobol.CobolParser;
import org.openrewrite.cobol.CobolTest;
import org.openrewrite.tree.ParseError;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.cobol.Assertions.copybook;

class CobolPreprocessorCopybookTest extends CobolTest {

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

    @Test
    void syntaxErrorProducesParseErrorWithoutStoppingStream() {
        CobolParser parser = CobolParser.builder()
                .copybooks(emptyList())
                .timeout(Duration.ofSeconds(10))
                .build();

        String validCobol = """
                000001 IDENTIFICATION DIVISION.
                000002 PROGRAM-ID. HELLO.
                000003 PROCEDURE DIVISION.
                000004     STOP RUN.
                """;

        String invalidCobol = """
                000001 BLAH BLAH THIS IS NOT A VALID COBOL PROGRAM.
                000002 IT SHOULD CAUSE A PARSE ERROR.
                """;

        List<Parser.Input> inputs = List.of(
                new Parser.Input(Path.of("valid1.cbl"), () -> new ByteArrayInputStream(validCobol.getBytes(StandardCharsets.UTF_8))),
                new Parser.Input(Path.of("invalid.cbl"), () -> new ByteArrayInputStream(invalidCobol.getBytes(StandardCharsets.UTF_8))),
                new Parser.Input(Path.of("valid2.cbl"), () -> new ByteArrayInputStream(validCobol.getBytes(StandardCharsets.UTF_8)))
        );

        List<Throwable> errors = new ArrayList<>();
        var ctx = new InMemoryExecutionContext(errors::add);

        List<SourceFile> results = parser.parseInputs(inputs, null, ctx)
                .toList();

        assertThat(results).hasSize(3);
        assertThat(results.get(1)).isInstanceOf(ParseError.class);
        assertThat(errors).isNotEmpty();
    }
}
