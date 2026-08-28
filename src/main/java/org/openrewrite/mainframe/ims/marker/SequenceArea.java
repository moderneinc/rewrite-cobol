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
package org.openrewrite.mainframe.ims.marker;

import lombok.Value;
import lombok.With;
import org.openrewrite.mainframe.ims.tree.Space;
import org.openrewrite.marker.Marker;

import java.util.UUID;

/**
 * Columns 73-80, the identification-sequence field. The assembler ignores it, so it is carried
 * beside the statement rather than as operands of it.
 */
@With
@Value
public class SequenceArea implements Marker {
    UUID id;
    Space prefix;
    String text;
}
