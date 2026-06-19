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
package org.openrewrite.jcl.marker;

import lombok.Value;
import lombok.With;
import org.openrewrite.marker.Marker;

import java.util.UUID;

/**
 * Marks a DD statement that references an external PDS member, e.g.
 * {@code //SYSIN DD DSN=DWL.PARMLIB(MGSLAP8F),DISP=SHR}. When {@link Status#EXPANDED}, the
 * member was resolved against the supplied set of {@code .prm} members and its content
 * grafted into the LST as {@link org.openrewrite.jcl.tree.Jcl.DataDefinitionStream} nodes;
 * when {@link Status#MISSING}, the DD qualified for expansion but the member was not
 * supplied. Either way the original DD statement is left untouched, so the source still
 * round-trips byte-for-byte.
 */
@With
@Value
public class ParmMember implements Marker {
    UUID id;

    Status status;

    /**
     * The DD name (e.g. {@code SYSIN}, {@code SYSTSIN}), or empty for an unnamed DD.
     */
    String ddName;

    /**
     * The referenced data set name, ignored for member resolution but retained for
     * diagnostics (e.g. {@code DWL.PARMLIB}).
     */
    String dataSetName;

    /**
     * The member name used for resolution (e.g. {@code MGSLAP8F}).
     */
    String memberName;

    public enum Status {
        /** The member was resolved and its content grafted into the LST. */
        EXPANDED,
        /** The DD qualified for expansion but the member was not supplied. */
        MISSING
    }
}
