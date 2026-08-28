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
package org.openrewrite.mainframe.cobol.marker;

import lombok.Value;
import lombok.With;
import org.openrewrite.marker.Marker;

import java.util.UUID;

/**
 * A word that came from a copybook rather than from the program's own source.
 * <p>
 * The copybook is named rather than pointed at. This used to carry the id of the statement that
 * copied the word, and an id does not survive being stored: the Moderne CLI writes build LSTs with
 * node ids omitted and regenerates them on read, so the reference pointed at nothing and every field
 * declared by a copybook read as though it had been declared in the program.
 */
@With
@Value
public class CopiedWord implements Marker {
    UUID id;

    /**
     * The copybook name as the {@code COPY} or {@code EXEC SQL INCLUDE} wrote it.
     */
    String copybook;
}
