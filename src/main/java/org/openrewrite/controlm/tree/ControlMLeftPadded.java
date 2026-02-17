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
package org.openrewrite.controlm.tree;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.With;
import org.jspecify.annotations.Nullable;
import org.openrewrite.marker.Markers;

import java.util.function.UnaryOperator;

@Value
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@With
public class ControlMLeftPadded<T> {

    Space before;
    T element;
    Markers markers;

    public ControlMLeftPadded<T> map(UnaryOperator<T> map) {
        return withElement(map.apply(element));
    }

    public enum Location {
        SET_VAR_INITIALIZER(Space.Location.SET_VAR_INITIALIZER);

        private final Space.Location beforeLocation;

        Location(Space.Location beforeLocation) {
            this.beforeLocation = beforeLocation;
        }

        public Space.Location getBeforeLocation() {
            return beforeLocation;
        }
    }

	public static <T> @Nullable ControlMLeftPadded<T> withElement(@Nullable ControlMLeftPadded<T> before, @Nullable T element) {
        if (before == null) {
            if (element == null) {
                return null;
            }
            return new ControlMLeftPadded<>(Space.EMPTY, element, Markers.EMPTY);
        }
        if (element == null) {
            return null;
        }
        return before.withElement(element);
    }

    @Override
    public String toString() {
        return "ControlMLeftPadded(before=" + before + ", element=" + element + ')';
    }

    public static <T> ControlMLeftPadded<T> build(T element) {
        return new ControlMLeftPadded<>(Space.EMPTY, element, Markers.EMPTY);
    }
}
