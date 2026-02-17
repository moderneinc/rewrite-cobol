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
import org.openrewrite.cobol.tree.CommentArea;
import org.openrewrite.cobol.tree.Space;
import org.openrewrite.marker.Marker;

import java.util.UUID;

/**
 * COBOL word transformations that increase the length of the current line may cause misalignment in the content area.
 * Whitespace is added in two places to simplify column alignment.
 *  1. The {@link CommentArea} contains whitespace until the end of the line.
 *  2. TemplateWhitespace marker contains whitespace until the next word starts in its current position.
 * <p>
 * I.E. PIC replaced by PICTURE
 * Before:
 *  |000001| | firstWord PIC [some words]         |
 * <p>
 * After:
 *  |000001| | firstWord PICTURE                  |
 *  |      | |               [some words]         |
 * <p>
 * The markers enable the ability to print the original word in place of the transformed word to print either the
 * original word or the transformed word.
 */
@With
@Value
public class ReplaceAdditiveWhitespace implements Marker {
    UUID id;
    Space prefix;
}
