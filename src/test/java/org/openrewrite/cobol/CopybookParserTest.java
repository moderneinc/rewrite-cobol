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
package org.openrewrite.cobol;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class CopybookParserTest {

    private final CopybookParser parser = CopybookParser.builder().build();

    @ParameterizedTest
    @ValueSource(strings = {"ACCTREC.cpy", "ACCTREC.CPY", "ACCTREC.copy", "ACCTREC.COPY", "ACCTREC.dcl", "ACCTREC.DCL", "copy/ACCTREC.Cpy"})
    void acceptsACopybookByExtension(String name) {
        assertThat(parser.accept(Paths.get(name))).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ACCTPOST.cbl", "ACCTPOST.cob", "ACCTPOST.cobol", "ACCTPOST.jcl", "ACCTREC.inc", "ACCTREC"})
    void rejectsOtherExtensions(String name) {
        assertThat(parser.accept(Paths.get(name))).isFalse();
    }
}
