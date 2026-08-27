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

import org.openrewrite.textmember.tree.TextMember;

import java.util.Arrays;
import java.util.List;

/**
 * Types the C a shop keeps beside its COBOL, which on z/OS is the USS side of an application and the
 * exits its products were written with. There is no grammar for it here: a C source is typed so that it
 * is searchable as C, and nothing more is read from it.
 */
public class CParser extends TextMemberParser {

    /**
     * Compared case-insensitively.
     */
    public static final List<String> C_FILE_EXTENSIONS = Arrays.asList(".c", ".h");

    @Override
    public TextMember.Kind getKind() {
        return TextMember.Kind.C;
    }

    @Override
    public List<String> getExtensions() {
        return C_FILE_EXTENSIONS;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends org.openrewrite.Parser.Builder {

        public Builder() {
            super(TextMember.CompilationUnit.class);
        }

        @Override
        public CParser build() {
            return new CParser();
        }

        @Override
        public String getDslName() {
            return "c";
        }
    }
}
