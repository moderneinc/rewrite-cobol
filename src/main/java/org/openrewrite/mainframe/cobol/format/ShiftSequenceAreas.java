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
package org.openrewrite.mainframe.cobol.format;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Incubating;
import org.openrewrite.mainframe.cobol.CobolIsoVisitor;
import org.openrewrite.mainframe.cobol.tree.Cobol;
import org.openrewrite.mainframe.cobol.tree.SequenceArea;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toCollection;

@Incubating(since = "0.0")
@EqualsAndHashCode(callSuper = true)
@Value
public class ShiftSequenceAreas extends CobolIsoVisitor<ExecutionContext> {

    LinkedList<SequenceArea> originalSequenceAreas;
    Cobol.Word startAfter;

    @NonFinal
    Cobol.Word previousWord = null;

    @NonFinal
    boolean startShift = false;

    public ShiftSequenceAreas(List<Cobol.Word> originalWords,
                              Cobol.Word startAfter) {

        this.originalSequenceAreas = originalWords.stream()
                .map(Cobol.Word::getSequenceArea)
                .filter(Objects::nonNull)
                .collect(toCollection(LinkedList::new));
        this.startAfter = startAfter;
    }

    @Override
    public Cobol.Word visitWord(Cobol.Word word, ExecutionContext executionContext) {
        Cobol.Word w = super.visitWord(word, executionContext);
        if (startShift && word.getSequenceArea() != null) {
            originalSequenceAreas.add(w.getSequenceArea());
            w = w.withSequenceArea(originalSequenceAreas.removeFirst());
        }

        if (previousWord == startAfter) {
            startShift = true;
        }
        previousWord = w;
        return w;
    }
}
