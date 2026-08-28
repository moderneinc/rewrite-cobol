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
package org.openrewrite.mainframe.db2.marker;

import lombok.Value;
import lombok.With;
import org.openrewrite.marker.Marker;

import java.util.UUID;

/**
 * The terminating semicolon of a statement that has one, carried on the statement's padding rather
 * than as a node — the same shape as {@code org.openrewrite.java.marker.Semicolon}, which Groovy
 * uses for the same job. Defined here rather than imported so that this module goes on depending
 * only on rewrite-core.
 * <p>
 * It marks presence, not omission. That is the ecosystem's polarity, and it is what lets the
 * printer emit the character from {@code visitMarker} once instead of in every statement.
 */
@Value
@With
public class Semicolon implements Marker {
    UUID id;
}
