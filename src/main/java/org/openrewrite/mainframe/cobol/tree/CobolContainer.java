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
 * AST elements that contain lists of trees with some delimiter like function call arguments.
 *
 * @param <T> The type of the inner list of elements.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CobolContainer<T> {
    private transient Padding<T> padding;

    private static final CobolContainer<?> EMPTY = new CobolContainer<>(Space.EMPTY, null, emptyList(), Markers.EMPTY);

	@Getter
	private final Space before;

	@Getter
	@Nullable
	private final CobolLeftPadded<String> preposition;

    private final List<CobolRightPadded<T>> elements;
	@Getter
	private final Markers markers;

    public static <T> CobolContainer<T> build(List<CobolRightPadded<T>> elements) {
        return build(Space.EMPTY, null, elements, Markers.EMPTY);
    }

    @JsonCreator
    public static <T> CobolContainer<T> build(Space before, @Nullable CobolLeftPadded<String> preposition,
                                              List<CobolRightPadded<T>> elements, Markers markers) {
        if (before.isEmpty() && elements.isEmpty()) {
            return empty();
        }
        return new CobolContainer<>(before, preposition, elements, markers);
    }

    @SuppressWarnings("unchecked")
    public static <T> CobolContainer<T> empty() {
        return (CobolContainer<T>) EMPTY;
    }

    public CobolContainer<T> withPreposition(@Nullable CobolLeftPadded<String> preposition) {
        return this.preposition == preposition ? this : build(before, preposition, elements, markers);
    }

    public CobolContainer<T> withBefore(Space before) {
        return this.before == before ? this : build(before, preposition, elements, markers);
    }

    public CobolContainer<T> withElements(List<CobolRightPadded<T>> elements) {
        return this.elements == elements ? this : build(before, preposition, elements, markers);
    }

    public CobolContainer<T> withMarkers(Markers markers) {
        return this.markers == markers ? this : build(before, preposition, elements, markers);
    }

    public List<T> getElements() {
        return CobolRightPadded.getElements(elements);
    }

    public CobolContainer<T> map(UnaryOperator<T> map) {
        return getPadding().withElements(ListUtils.map(elements, t -> t.map(map)));
    }

    public Space getLastSpace() {
        return elements.isEmpty() ? Space.EMPTY : elements.get(elements.size() - 1).getAfter();
    }

    public CobolContainer<T> withLastSpace(Space after) {
        return withElements(ListUtils.mapLast(elements, elem -> elem.withAfter(after)));
    }

    public Padding<T> getPadding() {
        if (padding == null) {
            this.padding = new Padding<>(this);
        }
        return padding;
    }

    @RequiredArgsConstructor
    public static class Padding<T> {
        private final CobolContainer<T> c;

        public List<CobolRightPadded<T>> getElements() {
            return c.elements;
        }

        public CobolContainer<T> withElements(List<CobolRightPadded<T>> elements) {
            return c.elements == elements ? c : build(c.before, c.preposition, elements, c.markers);
        }
    }

	public static <P extends Cobol> @Nullable CobolContainer<P> withElementsNullable(@Nullable CobolContainer<P> before, @Nullable List<P> elements) {
        if (before == null) {
            if (elements == null || elements.isEmpty()) {
                return null;
            }
            return CobolContainer.build(Space.EMPTY, null, CobolRightPadded.withElements(emptyList(), elements), Markers.EMPTY);
        }
        if (elements == null || elements.isEmpty()) {
            return null;
        }
        return before.getPadding().withElements(CobolRightPadded.withElements(before.elements, elements));
    }

    public static <P extends Cobol> CobolContainer<P> withElements(CobolContainer<P> before, @Nullable List<P> elements) {
        if (elements == null) {
            return before.getPadding().withElements(emptyList());
        }
        return before.getPadding().withElements(CobolRightPadded.withElements(before.elements, elements));
    }

    @Override
    public String toString() {
        return "CobolContainer(before=" + before + ", elementCount=" + elements.size() + ')';
    }
}
