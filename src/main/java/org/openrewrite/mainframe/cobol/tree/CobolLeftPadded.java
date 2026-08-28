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
package org.openrewrite.mainframe.cobol.tree;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.With;
import org.jspecify.annotations.Nullable;
import org.openrewrite.marker.Markers;

import java.util.function.UnaryOperator;

@Value
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@With
public class CobolLeftPadded<T> {
    Space before;
    T element;
    Markers markers;

    public CobolLeftPadded<T> map(UnaryOperator<T> map) {
        return withElement(map.apply(element));
    }

	public static <T> @Nullable CobolLeftPadded<T> withElement(@Nullable CobolLeftPadded<T> before, @Nullable T elements) {
        if (before == null) {
            if (elements == null) {
                return null;
            }
            return new CobolLeftPadded<>(Space.EMPTY, elements, Markers.EMPTY);
        }
        if (elements == null) {
            return null;
        }
        return before.withElement(elements);
    }

    @Override
    public String toString() {
        return "ProtoLeftPadded(before=" + before + ", element=" + element.getClass().getSimpleName() + ')';
    }

    public static <T> CobolLeftPadded<T> build(T element) {
        return new CobolLeftPadded<>(Space.EMPTY, element, Markers.EMPTY);
    }
}
