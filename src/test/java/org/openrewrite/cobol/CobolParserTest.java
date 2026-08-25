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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.ParseExceptionResult;
import org.openrewrite.Parser;
import org.openrewrite.SourceFile;
import org.openrewrite.cobol.tree.Cobol;
import org.openrewrite.tree.ParseError;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class CobolParserTest {

    private final CobolParser parser = CobolParser.builder().build();

    @ParameterizedTest
    @ValueSource(strings = {"ACCTPOST.cbl", "ACCTPOST.CBL", "ACCTPOST.cob", "ACCTPOST.COB", "ACCTPOST.cobol", "cobol/ACCTPOST.Cbl"})
    void acceptsAProgramByExtension(String name) {
        assertThat(parser.accept(Paths.get(name))).isTrue();
    }

    /**
     * A copybook is not a program, and a parser that accepted both would try to read a record
     * layout as one.
     */
    @ParameterizedTest
    @ValueSource(strings = {"ACCTREC.cpy", "ACCTREC.copy", "ACCTREC.dcl", "ACCTPOST.jcl", "ACCTPOST"})
    void rejectsOtherExtensions(String name) {
        assertThat(parser.accept(Paths.get(name))).isFalse();
    }

    /**
     * A control card kept as {@code .cbl} fails the grammar at its first word. That is not a
     * grammar gap, and it is reported under its own type so a parse-quality report can tell it from
     * one.
     */
    @Test
    void aFileThatIsNotAProgramIsSaidSo() {
        SourceFile parsed = parse("IEFBR14.cbl", " DELETE PROD.ACCOUNT.MASTER\n SET MAXCC=0\n");

        assertThat(parsed).isInstanceOf(ParseError.class);
        ParseExceptionResult failure = parsed.getMarkers().findFirst(ParseExceptionResult.class).orElseThrow();
        assertThat(failure.getParserType()).isEqualTo("CobolParser");
        assertThat(failure.getExceptionType()).isEqualTo("WrongLanguageException");
        assertThat(failure.getMessage()).contains("IEFBR14.cbl is not a COBOL program: it has no IDENTIFICATION DIVISION.");
    }

    @Test
    void aGrammarGapIsAParsingException() {
        SourceFile parsed = parse("HELLO.cbl", """
                     IDENTIFICATION DIVISION.
                     PROGRAM-ID. HELLO.
                     PROCEDURE DIVISION.
                         DISPLAY 'HELLO' UPON NOWHERE AT ALL.
              """);

        assertThat(parsed).isInstanceOf(ParseError.class);
        ParseExceptionResult failure = parsed.getMarkers().findFirst(ParseExceptionResult.class).orElseThrow();
        assertThat(failure.getParserType()).isEqualTo("CobolParser");
        assertThat(failure.getExceptionType()).isEqualTo("CobolParsingException");
    }

    /**
     * A string has no name to have promised anything, so a failure to read one is a grammar
     * failure whatever it holds.
     */
    @Test
    void aStringIsNeverTheWrongLanguage() {
        SourceFile parsed = parser.parse(new InMemoryExecutionContext(), " DELETE PROD.ACCOUNT.MASTER\n").findFirst().orElseThrow();

        assertThat(parsed).isInstanceOf(ParseError.class);
        assertThat(parsed.getMarkers().findFirst(ParseExceptionResult.class).orElseThrow().getExceptionType())
          .isEqualTo("CobolParsingException");
    }

    @Test
    void readsAProgramByPath() {
        SourceFile parsed = parse("HELLO.COB", "       IDENTIFICATION DIVISION.\n       PROGRAM-ID. HELLO.\n       PROCEDURE DIVISION.\n           STOP RUN.\n");

        assertThat(parsed).isInstanceOf(Cobol.CompilationUnit.class);
    }

    private SourceFile parse(String name, String source) {
        Parser.Input input = new Parser.Input(Paths.get(name),
                () -> new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8)));
        return parser.parseInputs(singletonList(input), null, new InMemoryExecutionContext()).findFirst().orElseThrow();
    }
}
