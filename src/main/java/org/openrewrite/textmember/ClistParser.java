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
 * Reads the CLIST library a shop keeps beside its COBOL: the members put in front of {@code SYSPROC},
 * which are the operator's and the programmer's side of an application.
 * <p>
 * A CLIST has no extension on the mainframe and takes one here only so that a repository can hold the
 * library. Unlike a REXX exec, nothing in a CLIST says what it is: {@code PROC 1 MEM} is a statement of
 * several other languages too, so an extensionless member is left to the readers that can tell.
 */
public class ClistParser extends TextMemberParser {

    /**
     * Compared case-insensitively.
     */
    public static final List<String> CLIST_FILE_EXTENSIONS = Arrays.asList(".clist", ".clst");

    @Override
    public TextMember.Kind getKind() {
        return TextMember.Kind.CLIST;
    }

    @Override
    public List<String> getExtensions() {
        return CLIST_FILE_EXTENSIONS;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends org.openrewrite.Parser.Builder {

        public Builder() {
            super(TextMember.CompilationUnit.class);
        }

        @Override
        public ClistParser build() {
            return new ClistParser();
        }

        @Override
        public String getDslName() {
            return "clist";
        }
    }
}
