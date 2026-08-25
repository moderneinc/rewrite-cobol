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
 * Marks the {@code EXEC} or {@code INCLUDE} statement that named a member of the procedure library,
 * and says whether the member was found. Which of the two it is, is read from the statement's own
 * operation.
 * <p>
 * When the member was found its body follows as a {@link org.openrewrite.jcl.tree.Jcl.Expansion};
 * when it was not, this marker is all there is to say so, and a job whose procedures are missing is
 * exactly what a portfolio with a gap in it looks like.
 */
@With
@Value
public class ExpandedMember implements Marker {
    UUID id;

    Status status;

    /**
     * The member named, e.g. {@code CLMBATCH} or {@code @JOBCARD}.
     */
    String memberName;

    public enum Status {
        /** Found in the supplied procedure library and expanded. */
        EXPANDED,
        /** Written in this member as a {@code PROC}/{@code PEND} pair, and expanded. */
        IN_STREAM,
        /** Named, and nowhere in the supplied procedure library. */
        MISSING
    }
}
