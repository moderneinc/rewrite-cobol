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
 * Types the PL/I of an estate. There is no grammar for it here and none is planned — the shops this
 * serves are decommissioning theirs — so a PL/I source is typed to keep it from being taken for
 * something else, and nothing is read from it.
 */
public class PliParser extends TextMemberParser {

    /**
     * Compared case-insensitively.
     */
    public static final List<String> PLI_FILE_EXTENSIONS = Arrays.asList(".pli", ".pl1");

    @Override
    public TextMember.Kind getKind() {
        return TextMember.Kind.PLI;
    }

    @Override
    public List<String> getExtensions() {
        return PLI_FILE_EXTENSIONS;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends org.openrewrite.Parser.Builder {

        public Builder() {
            super(TextMember.CompilationUnit.class);
        }

        @Override
        public PliParser build() {
            return new PliParser();
        }

        @Override
        public String getDslName() {
            return "pli";
        }
    }
}
