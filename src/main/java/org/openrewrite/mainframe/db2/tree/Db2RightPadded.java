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
package org.openrewrite.mainframe.db2.tree;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;
import lombok.experimental.FieldDefaults;
import org.openrewrite.marker.Markers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * An element and the space between it and whatever separates it from the next — the comma in a
 * column list, or the blank before a closing paren.
 * <p>
 * The separator itself is not here. A comma is not a sibling of the column in front of it, and a
 * recipe adding a column should not have to add punctuation with it.
 */
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Data
public class Db2RightPadded<T> {
    @With
    T element;

    @With
    Space after;

    @With
    Markers markers;

    public Db2RightPadded<T> map(UnaryOperator<T> map) {
        return withElement(map.apply(element));
    }

    public static <T> List<T> getElements(List<Db2RightPadded<T>> padded) {
        List<T> elements = new ArrayList<>(padded.size());
        for (Db2RightPadded<T> p : padded) {
            elements.add(p.getElement());
        }
        return elements;
    }

    /**
     * Puts {@code elements} back behind the padding they had, keeping the space of any that stayed
     * and giving a new one none.
     */
    public static <T> List<Db2RightPadded<T>> withElements(List<Db2RightPadded<T>> before, List<T> elements) {
        if (elements.size() == before.size()) {
            boolean changed = false;
            for (int i = 0; i < before.size(); i++) {
                if (before.get(i).getElement() != elements.get(i)) {
                    changed = true;
                    break;
                }
            }
            if (!changed) {
                return before;
            }
        }

        List<Db2RightPadded<T>> after = new ArrayList<>(elements.size());
        for (int i = 0; i < elements.size(); i++) {
            after.add(i < before.size() ?
                    before.get(i).withElement(elements.get(i)) :
                    build(elements.get(i)));
        }
        return after;
    }

    public static <T> Db2RightPadded<T> build(T element) {
        return new Db2RightPadded<>(element, Space.EMPTY, Markers.EMPTY);
    }

    @Override
    public String toString() {
        return "Db2RightPadded(element=" + element.getClass().getSimpleName() + ", after=" + after + ')';
    }
}
