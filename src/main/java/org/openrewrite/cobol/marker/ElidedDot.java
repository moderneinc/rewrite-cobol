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
package org.openrewrite.cobol.marker;

import lombok.Value;
import lombok.With;
import org.openrewrite.cobol.tree.CobolPreprocessor;
import org.openrewrite.marker.Marker;

import java.util.UUID;

/**
 * Preprocessing removes an {@link CobolPreprocessor.ExecStatement} from the text the COBOL grammar sees, which takes
 * the period that ended the sentence with it. The period is re-emitted into the parser input so that the sentence is
 * still terminated; this marks the resulting word as a placeholder whose text prints from the EXEC statement that is
 * attached to it, so that the period is only printed once.
 */
@With
@Value
public class ElidedDot implements Marker {
    UUID id;
}
