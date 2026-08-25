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

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.marker.Markers;

import java.util.List;
import java.util.function.UnaryOperator;

import static java.util.Collections.emptyList;

/**
 * A separated list: a table's columns, an index's keys, a routine's parameters, the privileges of a
 * {@code GRANT}.
 * <p>
 * The brackets around it and the commas between it are not held here. They are the same for every
 * list of a given kind, so the printer supplies them and the LST carries only what varies — the
 * elements and the space around them. That is what lets a recipe add a column without also adding
 * a comma, and it is why nothing in the tree is a punctuation node.
 *
 * @param <T> what the list holds.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Db2Container<T> {
    private transient Padding<T> padding;

    private static final Db2Container<?> EMPTY =
            new Db2Container<>(Space.EMPTY, emptyList(), Markers.EMPTY);

    /**
     * What sits between the keyword that introduces the list and its opening bracket.
     */
    @Getter
    private final Space before;

    private final List<Db2RightPadded<T>> elements;

    @Getter
    private final Markers markers;

    public static <T> Db2Container<T> build(List<Db2RightPadded<T>> elements) {
        return build(Space.EMPTY, elements, Markers.EMPTY);
    }

    @JsonCreator
    public static <T> Db2Container<T> build(Space before, List<Db2RightPadded<T>> elements, Markers markers) {
        return before.isEmpty() && elements.isEmpty() ? empty() :
                new Db2Container<>(before, elements, markers);
    }

    @SuppressWarnings("unchecked")
    public static <T> Db2Container<T> empty() {
        return (Db2Container<T>) EMPTY;
    }

    public Db2Container<T> withBefore(Space before) {
        return this.before == before ? this : build(before, elements, markers);
    }

    public Db2Container<T> withMarkers(Markers markers) {
        return this.markers == markers ? this : build(before, elements, markers);
    }

    public List<T> getElements() {
        return Db2RightPadded.getElements(elements);
    }

    public Db2Container<T> withElements(List<T> elements) {
        return getPadding().withElements(Db2RightPadded.withElements(this.elements, elements));
    }

    public Db2Container<T> map(UnaryOperator<T> map) {
        return getPadding().withElements(ListUtils.map(elements, e -> e.map(map)));
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    /**
     * The space in front of the closing bracket, which is where a list written over several lines
     * keeps the indentation of the line the bracket sits on.
     */
    public Space getLastSpace() {
        return elements.isEmpty() ? Space.EMPTY : elements.get(elements.size() - 1).getAfter();
    }

    public Db2Container<T> withLastSpace(Space after) {
        return getPadding().withElements(ListUtils.mapLast(elements, e -> e.withAfter(after)));
    }

    public Padding<T> getPadding() {
        if (padding == null) {
            padding = new Padding<>(this);
        }
        return padding;
    }

    @RequiredArgsConstructor
    public static class Padding<T> {
        private final Db2Container<T> container;

        public List<Db2RightPadded<T>> getElements() {
            return container.elements;
        }

        public Db2Container<T> withElements(List<Db2RightPadded<T>> elements) {
            return container.elements == elements ? container :
                    build(container.before, elements, container.markers);
        }
    }

    public static <T> @Nullable Db2Container<T> withElementsNullable(@Nullable Db2Container<T> before,
                                                                    @Nullable List<T> elements) {
        if (elements == null || elements.isEmpty()) {
            return null;
        }
        return before == null ? build(Db2RightPadded.withElements(emptyList(), elements)) :
                before.withElements(elements);
    }

    @Override
    public String toString() {
        return "Db2Container(before=" + before + ", elementCount=" + elements.size() + ')';
    }
}
