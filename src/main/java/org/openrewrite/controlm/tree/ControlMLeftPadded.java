/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.controlm.tree;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.With;
import org.openrewrite.internal.lang.Nullable;
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

    @Nullable
    public static <T> ControlMLeftPadded<T> withElement(@Nullable ControlMLeftPadded<T> before, @Nullable T element) {
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
