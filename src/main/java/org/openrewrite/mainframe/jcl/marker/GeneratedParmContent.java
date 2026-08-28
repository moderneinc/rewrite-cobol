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

import java.util.UUID;

/**
 * Marks a {@code DataDefinitionStream} node that was grafted into the LST from an external
 * PDS member (see {@link ParmMember}), rather than read from the original source. The
 * {@code JclPrinter} skips nodes carrying this marker so the source still round-trips
 * byte-for-byte.
 */
@With
@Value
public class GeneratedParmContent implements Marker {
    UUID id;

    /**
     * The member the content was expanded from (e.g. {@code MGSLAP8F}).
     */
    String memberName;
}
