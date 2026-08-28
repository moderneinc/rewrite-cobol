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

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Incubating;
import org.openrewrite.mainframe.cobol.CobolIsoVisitor;
import org.openrewrite.mainframe.cobol.CobolPrinterUtils;
import org.openrewrite.mainframe.cobol.marker.CopiedWord;
import org.openrewrite.mainframe.cobol.search.FindWords;
import org.openrewrite.mainframe.cobol.tree.Cobol;
import org.openrewrite.mainframe.cobol.tree.ColumnArea;
import org.openrewrite.mainframe.cobol.tree.IndicatorArea;
import org.openrewrite.internal.ListUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@AllArgsConstructor
@Incubating(since = "0.0")
@EqualsAndHashCode(callSuper = true)
@Value
public class RemoveWords extends CobolIsoVisitor<ExecutionContext> {

    List<Cobol.Word> removeWords;

    public RemoveWords(Cobol tree) {
        this.removeWords = FindWords.find(tree);
    }

    @Override
    public Cobol.Word visitWord(Cobol.Word word, ExecutionContext executionContext) {
        Cobol.Word w = super.visitWord(word, executionContext);
        if (removeWords.contains(w) && !w.getWord().trim().isEmpty()) {
            if (word.getReplacement() != null || word.getMarkers().findFirst(CopiedWord.class).isPresent()) {
                // The NIST test does not provide examples of these types of transformation, so it hasn't been implemented yet.
                throw new UnsupportedOperationException("RemoveWords does not support changes on copied sources or replaced words.");
            }

            if (word.getContinuation() != null) {
                org.openrewrite.mainframe.cobol.tree.Continuation continuation = word.getContinuation();
                Map<Integer, List<ColumnArea>> continuations = new HashMap<>(continuation.getContinuations().size());
                AtomicBoolean changed = new AtomicBoolean(false);
                for (Map.Entry<Integer, List<ColumnArea>> entry : continuation.getContinuations().entrySet()) {
                    List<ColumnArea> columnAreas = ListUtils.map(entry.getValue(), it -> {
                        if (it instanceof IndicatorArea && "-".equals(((IndicatorArea) it).getIndicator())) {
                            it = ((IndicatorArea) it).withIndicator(" ");
                            changed.set(true);
                        }
                        return it;
                    });
                    continuations.put(entry.getKey(), columnAreas);
                }

                if (changed.get()) {
                    continuation = continuation.withContinuations(continuations);
                    w = w.withContinuation(continuation);
                }
            }
            w = w.withWord(CobolPrinterUtils.fillArea(' ', w.getWord().length()));
        }
        return w;
    }
}
