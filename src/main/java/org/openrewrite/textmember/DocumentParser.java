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
 * Reads the documentation library an application keeps: one run book member per job, program, file,
 * application and operating procedure — Desjardins' {@code DOCJOB}, {@code DOCPGM}, {@code DOCFICH},
 * {@code DOCAPPL} and {@code DOCOPER}.
 * <p>
 * A run book is prose in a fixed layout, and it names every component by its member name, so what the
 * text says resolves the way code does. Which of the five a member is comes from its first word, which
 * is read by {@link org.openrewrite.textmember.trait.RunBook}.
 */
public class DocumentParser extends TextMemberParser {

    /**
     * Compared case-insensitively.
     */
    public static final List<String> DOCUMENT_FILE_EXTENSIONS =
            Arrays.asList(".docjob", ".docpgm", ".docfich", ".docappl", ".docoper");

    @Override
    public TextMember.Kind getKind() {
        return TextMember.Kind.DOCUMENT;
    }

    @Override
    public List<String> getExtensions() {
        return DOCUMENT_FILE_EXTENSIONS;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends org.openrewrite.Parser.Builder {

        public Builder() {
            super(TextMember.CompilationUnit.class);
        }

        @Override
        public DocumentParser build() {
            return new DocumentParser();
        }

        @Override
        public String getDslName() {
            return "document";
        }
    }
}
