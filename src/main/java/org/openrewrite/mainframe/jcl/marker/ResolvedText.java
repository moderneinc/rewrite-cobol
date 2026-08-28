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
package org.openrewrite.mainframe.jcl.marker;

import lombok.Value;
import lombok.With;
import org.openrewrite.marker.Marker;

import java.util.List;
import java.util.UUID;

/**
 * What a parameter value or a line of in-stream data reads as once its symbols are filled in, on
 * the parameter or word as written. The tree text is left alone, so the source still prints back
 * byte for byte and the resolved reading is there for anything that needs it.
 * <p>
 * A symbol nothing set stays as it was written and is listed in {@link #getSymbolics()} with no
 * value, which is how a job that would fail to resolve is told from one that resolves to something.
 */
@With
@Value
public class ResolvedText implements Marker {
    UUID id;

    /**
     * The text with every symbol that has a value substituted.
     */
    String text;

    /**
     * The symbols referred to, in the order they were written.
     */
    List<Symbolic> symbolics;
}
