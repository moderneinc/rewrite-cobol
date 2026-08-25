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
package org.openrewrite.db2.tree;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.With;
import org.jspecify.annotations.Nullable;
import org.openrewrite.marker.Markers;

import java.util.function.UnaryOperator;

/**
 * An element and the space in front of the operator that introduces it — the {@code =} of a
 * {@code SET}, the {@code AS} of a sequence's type. The operator is printed rather than stored, for
 * the same reason a comma is.
 */
@Value
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@With
public class Db2LeftPadded<T> {

    Space before;
    T element;
    Markers markers;

    public Db2LeftPadded<T> map(UnaryOperator<T> map) {
        return withElement(map.apply(element));
    }

    public static <T> Db2LeftPadded<T> build(T element) {
        return new Db2LeftPadded<>(Space.EMPTY, element, Markers.EMPTY);
    }

    public static <T> @Nullable Db2LeftPadded<T> withElement(@Nullable Db2LeftPadded<T> before,
                                                            @Nullable T element) {
        if (element == null) {
            return null;
        }
        return before == null ? build(element) : before.withElement(element);
    }

    @Override
    public String toString() {
        return "Db2LeftPadded(before=" + before + ", element=" + element.getClass().getSimpleName() + ')';
    }
}
