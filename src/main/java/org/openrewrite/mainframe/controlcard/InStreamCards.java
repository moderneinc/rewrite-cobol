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
package org.openrewrite.mainframe.controlcard;

import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.mainframe.jcl.SourcePositions;
import org.openrewrite.mainframe.jcl.trait.DataDefinition;
import org.openrewrite.mainframe.jcl.tree.Jcl;
import org.openrewrite.marker.Range;

import java.util.ArrayList;
import java.util.List;

/**
 * The cards written inside a job rather than in a library of their own — the in-stream data of a DD.
 * <p>
 * Both shapes occur at a real shop and neither stands for the other: the deck a shop keeps as a
 * control card member is read once and run from several jobs, while the deck written in the job is
 * the only place its cards exist. The cards are already in the job's own LST, so they are read back
 * from it by printing — a card's layout lives in the white space in front of each word, and where the
 * line ends is what decides whether the next one continues it.
 * <p>
 * Every DD is offered, not a list of the ones a shop is expected to use. A sort deck reaches DFSORT
 * on {@code SYSIN}, {@code DFSPARM}, {@code SORTCNTL} or a {@code xxxxCNTL} named by an ICETOOL
 * {@code USING}, and which of those it is is the job's business — the deck is typed by what it says.
 */
@Value
public class InStreamCards {

    /**
     * The DD the cards were written under.
     */
    String ddName;

    /**
     * The one-based line of the job the deck is reached at: the first card for one written in the
     * job, and the {@code EXEC} that called the procedure for one written in a procedure.
     */
    int line;

    String text;

    public static List<InStreamCards> of(Jcl.CompilationUnit cu) {
        SourcePositions positions = SourcePositions.of(cu);
        List<InStreamCards> decks = new ArrayList<>();
        // Every DD the job runs, not only the ones written among its own cards: a SYSIN written in a
        // cataloged procedure is a deck this job runs.
        new DataDefinition.Matcher().lower(cu).forEach(dd -> {
            InStreamCards cards = read(dd, positions);
            if (cards != null) {
                decks.add(cards);
            }
        });
        return decks;
    }

    private static @Nullable InStreamCards read(DataDefinition dd, SourcePositions positions) {
        if (dd.isDataOverridden()) {
            return null;
        }
        int start = -1;
        int end = -1;
        int line = 0;
        for (Jcl.DataDefinitionStream data : dd.getInStreamData()) {
            Range card = cardOf(data, positions);
            if (card == null) {
                continue;
            }
            if (start < 0) {
                start = card.getStart().getOffset();
                line = card.getStart().getLine();
            }
            end = card.getEnd().getOffset();
        }
        if (start < 0) {
            return null;
        }
        SourcePositions.Expanded at = positions.expanded(dd.getTree());
        String source = at == null ? positions.getSource() : at.getMemberSource();
        return new InStreamCards(dd.getName(),
                at == null ? line : at.getBroughtInAt().getStart().getLine(),
                source.substring(start, end));
    }

    /**
     * Whole cards rather than words: a control card means something different in another column. A
     * card written in a procedure is placed in that member; content grafted in from an external
     * member is written nowhere and has no card at all.
     */
    private static @Nullable Range cardOf(Jcl.DataDefinitionStream data, SourcePositions positions) {
        Range card = positions.card(data);
        if (card != null) {
            return card;
        }
        SourcePositions.Expanded at = positions.expanded(data);
        return at == null ? null : at.getCard();
    }
}
