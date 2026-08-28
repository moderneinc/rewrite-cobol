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
package org.openrewrite.mainframe.jcl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.ParseExceptionResult;
import org.openrewrite.Parser;
import org.openrewrite.SourceFile;
import org.openrewrite.mainframe.jcl.tree.Jcl;
import org.openrewrite.tree.ParseError;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class JclParserTest {

    private final JclParser parser = JclParser.builder().build();

    @ParameterizedTest
    @ValueSource(strings = {"POST.jcl", "POST.JCL", "POST.prc", "POST.PRC", "POST.proc", "jobs/POST.Jcl"})
    void acceptsAJobByExtension(String name) {
        assertThat(parser.accept(Paths.get(name))).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST.cbl", "POST.cpy", "POST.bms", "POST.ctl", "POST.prm", "POST.txt.bak"})
    void rejectsOtherExtensions(String name) {
        assertThat(parser.accept(Paths.get(name))).isFalse();
    }

    /**
     * A member copied off a PDS keeps no extension, or {@code .txt}; nothing but its first card
     * says what it is.
     */
    @ParameterizedTest
    @ValueSource(strings = {"IEFBR14DE", "VTOCPRNT.txt", "ZWESECKG"})
    void acceptsAMemberByItsFirstCard(String name, @TempDir Path dir) throws IOException {
        Path member = Files.write(dir.resolve(name), "//AACCDELA JOB (AACC,SCHE),'PROD',CLASS=A\n//S1 EXEC PGM=IEFBR14\n".getBytes(StandardCharsets.UTF_8));
        assertThat(parser.accept(member)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"J00ADCDP", "notes.txt", "LICENSE"})
    void rejectsAMemberWhoseFirstCardIsNotJcl(String name, @TempDir Path dir) throws IOException {
        Path member = Files.write(dir.resolve(name), "/* REXX */\nsay 'hello'\n".getBytes(StandardCharsets.UTF_8));
        assertThat(parser.accept(member)).isFalse();
    }

    @Test
    void anExtensionlessPathThatIsNotAFileIsNotAccepted() {
        assertThat(parser.accept(Paths.get("nowhere/IEFBR14DE"))).isFalse();
    }

    /**
     * A CICS parameter member kept as {@code .jcl} is not a grammar gap. It is reported under its
     * own type so a parse-quality report can tell the two apart.
     */
    @Test
    void aMemberThatIsNotJclIsSaidSo() {
        SourceFile parsed = parse("DFH$SIP1.jcl", "*\n* Copyright IBM Corp. 2023\n*\nSIT=6$,\nAPPLID=CICSTS61,\n");

        assertThat(parsed).isInstanceOf(ParseError.class);
        ParseExceptionResult failure = parsed.getMarkers().findFirst(ParseExceptionResult.class).orElseThrow();
        assertThat(failure.getParserType()).isEqualTo("JclParser");
        assertThat(failure.getExceptionType()).isEqualTo("WrongLanguageException");
        assertThat(failure.getMessage()).contains("DFH$SIP1.jcl is not JCL: no card begins with //.");
        assertThat(parsed.printAll()).startsWith("*\n* Copyright IBM Corp. 2023");
    }

    /**
     * A string has no name to have promised anything, so it is read as whatever it is.
     */
    @Test
    void aStringIsNeverTheWrongLanguage() {
        SourceFile parsed = parser.parse(new InMemoryExecutionContext(), "SIT=6$,\nAPPLID=CICSTS61,\n").findFirst().orElseThrow();

        assertThat(parsed).isInstanceOf(Jcl.CompilationUnit.class);
    }

    /**
     * A Jinja template of a job is still a job, and Bank-of-Z ships its whole installation that way.
     * What is left after dropping the {@code .j2} decides, so a template of something else is not
     * claimed.
     */
    @ParameterizedTest
    @ValueSource(strings = {"deploy/ims_maclib.jcl.j2", "cntl/PROCLIB.prc.j2", "some.xml.dir/POST.jcl.j2"})
    void acceptsATemplatedJob(String name) {
        assertThat(parser.accept(Paths.get(name))).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"deploy/zos_connect_app_config.xml.j2", "build/datasets.yaml.j2", "jmp/dfsjvmpr.props.j2"})
    void rejectsATemplateOfSomethingElse(String name) {
        assertThat(parser.accept(Paths.get(name))).isFalse();
    }

    /**
     * Nothing is left of a templated PDS member's name once the {@code .j2} is gone, so its first
     * card decides, the same as an untemplated one. Bank-of-Z creates every one of its tables from
     * a member named this way.
     */
    @Test
    void acceptsATemplatedMemberByItsFirstCard(@TempDir Path dir) throws IOException {
        Path member = Files.write(dir.resolve("Db2-create.j2"),
                "//DB2CREAT JOB 'DB2',NOTIFY=&SYSUID,CLASS=A\n//GRANT EXEC PGM=IKJEFT01\n".getBytes(StandardCharsets.UTF_8));
        assertThat(parser.accept(member)).isTrue();
    }

    private SourceFile parse(String name, String source) {
        Parser.Input input = new Parser.Input(Paths.get(name),
                () -> new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8)));
        return parser.parseInputs(singletonList(input), null, new InMemoryExecutionContext()).findFirst().orElseThrow();
    }
}
