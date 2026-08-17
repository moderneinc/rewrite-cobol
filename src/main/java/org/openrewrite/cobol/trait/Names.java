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
package org.openrewrite.cobol.trait;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.openrewrite.cobol.CobolIsoVisitor;
import org.openrewrite.cobol.tree.Cobol;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Reading a name back out of the tree. A data reference reaches the LST through several wrappers —
 * {@code QualifiedDataName}, {@code QualifiedDataNameFormat1}, {@code Identifier} — and every trait
 * here wants the plain name underneath.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Names {

    /**
     * The first word of {@code tree}, which for a data reference is the name itself and for a
     * qualified reference like {@code BALANCE OF ACCOUNT} is the qualified name. Null when the
     * subtree holds no word.
     */
    public static @Nullable String of(@Nullable Cobol tree) {
        if (tree == null) {
            return null;
        }
        if (tree instanceof Cobol.Word) {
            return ((Cobol.Word) tree).getWord();
        }
        AtomicReference<String> found = new AtomicReference<>();
        new CobolIsoVisitor<AtomicReference<String>>() {
            @Override
            public Cobol.Word visitWord(Cobol.Word word, AtomicReference<String> first) {
                first.compareAndSet(null, word.getWord());
                return word;
            }
        }.visit(tree, found);
        return found.get();
    }

    /**
     * As {@link #of}, upper cased. COBOL names are not case sensitive but source is written both ways.
     */
    public static @Nullable String upperOf(@Nullable Cobol tree) {
        String name = of(tree);
        return name == null ? null : name.toUpperCase(Locale.ROOT);
    }
}
