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
package org.openrewrite.textmember;

import org.openrewrite.controlcard.ControlCards;
import org.openrewrite.textmember.tree.TextMember;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * Reads the REXX library a shop keeps beside its COBOL: the execs the operators and the programmers
 * run, which submit the jobs, read the members and drive the dialogs of the application.
 */
public class RexxParser extends TextMemberParser {

    /**
     * Compared case-insensitively. An exec kept without an extension, which is how a PDS member arrives
     * when it is copied off as it stands, is accepted by its first line instead — see
     * {@link TextMemberLineReader#isRexxExec}.
     */
    public static final List<String> REXX_FILE_EXTENSIONS = Arrays.asList(".rexx", ".rex", ".rx");

    @Override
    public TextMember.Kind getKind() {
        return TextMember.Kind.REXX;
    }

    @Override
    public List<String> getExtensions() {
        return REXX_FILE_EXTENSIONS;
    }

    @Override
    public boolean accept(Path path) {
        // An extensionless member is claimed by whichever reader its first line answers to: JCL by its
        // first card, a bind deck by its first subcommand, an exec by the comment TSO itself reads.
        return super.accept(path) ||
               ControlCards.accept(path, emptyList(), TextMemberLineReader::isRexxExec);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends org.openrewrite.Parser.Builder {

        public Builder() {
            super(TextMember.CompilationUnit.class);
        }

        @Override
        public RexxParser build() {
            return new RexxParser();
        }

        @Override
        public String getDslName() {
            return "rexx";
        }
    }
}
