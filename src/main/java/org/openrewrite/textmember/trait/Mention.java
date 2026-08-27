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
package org.openrewrite.textmember.trait;

import lombok.Value;
import org.openrewrite.textmember.TextMemberLineReader;
import org.openrewrite.textmember.tree.TextMember;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A name a member writes down, and the line it was written on.
 */
@Value
public class Mention {
    String text;

    /**
     * The one-based line the name was first written on.
     */
    int line;

    /**
     * Every name a member writes, distinct, in the order it first writes them.
     * <p>
     * This is what a search for a member name finds in the text, before any statement is understood.
     * It over-answers on purpose — an English word of eight letters or fewer is spelled exactly like a
     * member name — and which of these names is a component of the estate is answered by looking each
     * one up among the members a repository holds.
     */
    public static List<Mention> in(TextMember.CompilationUnit member) {
        List<Mention> names = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        List<TextMember.Line> lines = member.getLines();
        for (int i = 0; i < lines.size(); i++) {
            for (String name : TextMemberLineReader.names(lines.get(i).getText())) {
                if (seen.add(name)) {
                    names.add(new Mention(name, i + 1));
                }
            }
        }
        return names;
    }
}
