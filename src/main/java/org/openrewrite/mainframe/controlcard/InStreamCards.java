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
import org.openrewrite.mainframe.jcl.SourcePositions;
import org.openrewrite.mainframe.jcl.tree.Jcl;
import org.openrewrite.mainframe.jcl.tree.Statement;
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
     * The one-based line of the job the first card is written on.
     */
    int line;

    String text;

    public static List<InStreamCards> of(Jcl.CompilationUnit cu) {
        SourcePositions positions = SourcePositions.of(cu);
        List<Statement> statements = cu.getStatements();
        List<InStreamCards> decks = new ArrayList<>();
        for (int i = 0; i < statements.size(); i++) {
            if (!(statements.get(i) instanceof Jcl.JobControlStatement) ||
                !((Jcl.JobControlStatement) statements.get(i)).isOperation("DD")) {
                continue;
            }
            int start = -1;
            int end = -1;
            int line = 0;
            for (int j = i + 1; j < statements.size() &&
                                !(statements.get(j) instanceof Jcl.JobControlStatement); j++) {
                // Whole cards rather than words: a control card means something different in another
                // column. Content grafted in from an external member is not the job's own and has none.
                Range card = statements.get(j) instanceof Jcl.DataDefinitionStream ?
                        positions.card(statements.get(j)) : null;
                if (card != null) {
                    if (start < 0) {
                        start = card.getStart().getOffset();
                        line = card.getStart().getLine();
                    }
                    end = card.getEnd().getOffset();
                }
            }
            if (start >= 0) {
                decks.add(new InStreamCards(((Jcl.JobControlStatement) statements.get(i)).getSimpleName(),
                        line, positions.getSource().substring(start, end)));
            }
        }
        return decks;
    }
}
