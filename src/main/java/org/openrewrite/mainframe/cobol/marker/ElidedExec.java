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
import org.openrewrite.mainframe.cobol.tree.CobolPreprocessor;
import org.openrewrite.marker.Marker;

import java.util.UUID;

/**
 * Preprocessing removes an {@link CobolPreprocessor.ExecStatement} from the text the COBOL grammar sees, taking the
 * period that ended the sentence with it. A tagged line and a period stand in for them in the parser input, so that
 * the EXEC is still a statement and the sentence is still terminated. This marks each stand-in word; its text prints
 * from the EXEC statement attached to it rather than from the word itself.
 */
@With
@Value
public class ElidedExec implements Marker {
    UUID id;
}
