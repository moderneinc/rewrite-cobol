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
package org.openrewrite.jcl;

import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class JclParserTest {

    /**
     * A Jinja template of a job is still a job, and an installation that ships every member that way
     * would otherwise be invisible. What is left after dropping the {@code .j2} decides, so a
     * template of something else is not claimed — and a member with no extension at all is exactly
     * how a PDS holds a job.
     */
    @Test
    void acceptsTemplatedJcl() {
        JclParser parser = JclParser.builder().build();

        assertThat(parser.accept(Paths.get("cntl/DB2CREAT.jcl"))).isTrue();
        assertThat(parser.accept(Paths.get("cntl/PROCLIB.prc"))).isTrue();
        assertThat(parser.accept(Paths.get("deploy/ims_maclib.jcl.j2"))).isTrue();
        assertThat(parser.accept(Paths.get("jcl/cics/Db2-create.j2"))).isTrue();

        assertThat(parser.accept(Paths.get("deploy/zos_connect_app_config.xml.j2"))).isFalse();
        assertThat(parser.accept(Paths.get("build/datasets.yaml.j2"))).isFalse();
        assertThat(parser.accept(Paths.get("jmp/dfsjvmpr.props.j2"))).isFalse();
        assertThat(parser.accept(Paths.get("app/cbl/COACTVWC.cbl"))).isFalse();
    }

    /**
     * A directory named for something else must not decide the answer — only the file's own name
     * does, since that is all a build tool hands the parser.
     */
    @Test
    void onlyTheFileNameDecides() {
        JclParser parser = JclParser.builder().build();

        assertThat(parser.accept(Paths.get("some.xml.dir/PROCLIB.j2"))).isTrue();
        assertThat(parser.accept(Paths.get("jcl/config.xml.j2"))).isFalse();
    }
}
