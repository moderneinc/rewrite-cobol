/*
 * Copyright 2025 the original author or authors.
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
package org.openrewrite.cobol.internal;

import lombok.Getter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;

public class HpTandem implements CobolDialect {

    private static final HpTandem INSTANCE = new HpTandem();

    public static HpTandem getInstance() {
        return INSTANCE;
    }

    private final Set<String> separators;
    private final Set<Character> commentIndicators;
	@Getter
	private final Columns columns;

    private HpTandem() {
        // Currently unknown.
        this.separators = emptySet();
        this.commentIndicators = new HashSet<>(singletonList('*'));
        this.columns = Columns.IBM_ANSI_85;
    }

    @Override
    public Collection<String> getSeparators() {
        return separators;
    }

    @Override
    public Collection<Character> getCommentIndicators() {
        return commentIndicators;
    }
}
