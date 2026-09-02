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
import org.openrewrite.mainframe.jcl.marker.GeneratedParmContent;
import org.openrewrite.mainframe.jcl.trait.DataDefinition;
import org.openrewrite.mainframe.jcl.tree.Jcl;
import org.openrewrite.mainframe.jcl.tree.Space;
import org.openrewrite.mainframe.jcl.tree.Statement;
import org.openrewrite.marker.Markers;
import org.openrewrite.marker.Range;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.openrewrite.Tree.randomId;

/**
 * The cards a DD hands the step: written in the job stream, or grafted in from the library member
 * the DD named.
 * <p>
 * Both shapes occur at a real shop and neither stands for the other: the deck a shop keeps as a
 * control card member is read once and run from several jobs, while the deck written in the job is
 * the only place its cards exist. Cards written in the stream are already in the job's own LST, so
 * they are read back from it by printing — a card's layout lives in the white space in front of each
 * word, and where the line ends is what decides whether the next one continues it. Cards of a member
 * print nowhere and are put back together from the words the graft left behind.
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
     * The DD statement the cards belong to, which is what says which step reads them: a job runs
     * {@code SYSIN} under as many steps as it has, and the name alone does not tell them apart.
     */
    UUID dataDefinition;

    /**
     * The one-based line of the job the deck is reached at: the first card for one written in the
     * job, and the {@code EXEC} that called the procedure for one written in a procedure.
     */
    int line;

    String text;

    /**
     * The in-stream statements the deck was read from, in the order they were written, where they are
     * cards of this member. Empty for a deck written in a cataloged procedure or grafted in from a
     * library member: those cards belong to another member, and changing them here would change
     * nothing this job prints.
     */
    List<UUID> cards;

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
        List<Jcl.DataDefinitionStream> data = dd.getInStreamData();
        if (!data.isEmpty() && data.get(0).getMarkers().findFirst(GeneratedParmContent.class).isPresent()) {
            return fromMember(dd, data, positions);
        }
        int start = -1;
        int end = -1;
        int line = 0;
        List<UUID> cards = new ArrayList<>();
        for (Jcl.DataDefinitionStream card : data) {
            Range range = cardOf(card, positions);
            if (range == null) {
                continue;
            }
            if (start < 0) {
                start = range.getStart().getOffset();
                line = range.getStart().getLine();
            }
            end = range.getEnd().getOffset();
            cards.add(card.getId());
        }
        if (start < 0) {
            return null;
        }
        SourcePositions.Expanded at = positions.expanded(dd.getTree());
        String source = at == null ? positions.getSource() : at.getMemberSource();
        return new InStreamCards(dd.getName(), dd.getTree().getId(),
                at == null ? line : at.getBroughtInAt().getStart().getLine(),
                source.substring(start, end),
                at == null ? cards : emptyList());
    }

    /**
     * A deck the DD named as a member of a library rather than writing out. Its cards are written in
     * that member and nowhere in this job, so there is no source to read them out of and the text is
     * put back together from the words and the white space in front of them. The line reported is
     * where the job reaches the deck: the DD that named the member, or the EXEC that called the
     * procedure the DD is written in.
     */
    private static InStreamCards fromMember(DataDefinition dd, List<Jcl.DataDefinitionStream> data,
                                            SourcePositions positions) {
        StringBuilder text = new StringBuilder();
        for (Jcl.DataDefinitionStream card : data) {
            text.append(card.getPrefix().getWhitespace()).append(card.getWord().getText());
        }
        SourcePositions.Expanded at = positions.expanded(dd.getTree());
        Range reached = at == null ? positions.card(dd.getTree()) : at.getBroughtInAt();
        return new InStreamCards(dd.getName(), dd.getTree().getId(),
                reached == null ? 0 : reached.getStart().getLine(),
                text.toString(), emptyList());
    }

    /**
     * The job with these cards replaced by the text given, which is how an edited deck goes back into
     * the job it was read from.
     * <p>
     * A deck is parsed on its own, detached from the job, because the two are different languages and
     * only one of them is in the LST. So writing one back is putting the printed deck through the
     * words the stream is held as — one word to a statement, the white space in front of it saying
     * where the card breaks are — rather than editing anything the island parsed.
     * <p>
     * The cards written are the ones this deck was read from, so a job that has already been written
     * to has to be read again before it is written to a second time.
     */
    public Jcl.CompilationUnit write(Jcl.CompilationUnit cu, String text) {
        if (cards.isEmpty()) {
            throw new IllegalStateException("The " + ddName + " deck is not written among this member's " +
                                            "own cards, so it cannot be written back through it.");
        }
        List<Statement> statements = new ArrayList<>(cu.getStatements().size());
        boolean replaced = false;
        for (Statement statement : cu.getStatements()) {
            if (!cards.contains(statement.getId())) {
                statements.add(statement);
            } else if (!replaced) {
                statements.addAll(split(text, statement.getPrefix().getWhitespace()));
                replaced = true;
            }
        }
        if (!replaced) {
            throw new IllegalStateException("The " + ddName + " deck was read from cards this job no " +
                                            "longer holds, so writing it back would change nothing.");
        }
        return cu.withStatements(statements);
    }

    /**
     * The cards of a deck, as the words a job stream is held as. The first word keeps whatever ended
     * the DD statement above it — a line ending, and on z/OS that may be two characters — and every
     * word after it is separated by the white space the deck itself was written with.
     * <p>
     * A quoted string with a blank in it becomes more than one word here where the parser would have
     * kept it as one. It prints the same either way, and the deck rather than the stream is the thing
     * anything reads.
     */
    private static List<Statement> split(String text, String firstPrefix) {
        String lineEnding = firstPrefix.substring(0, firstPrefix.lastIndexOf('\n') + 1);
        List<Statement> statements = new ArrayList<>();
        int i = 0;
        while (i < text.length()) {
            int start = i;
            while (i < text.length() && Character.isWhitespace(text.charAt(i))) {
                i++;
            }
            String prefix = text.substring(start, i);
            int word = i;
            while (i < text.length() && !Character.isWhitespace(text.charAt(i))) {
                i++;
            }
            if (i == word) {
                break;
            }
            statements.add(new Jcl.DataDefinitionStream(randomId(),
                    Space.build(statements.isEmpty() ? lineEnding + prefix : prefix), Markers.EMPTY,
                    new Jcl.Word(randomId(), Space.EMPTY, Markers.EMPTY, text.substring(word, i))));
        }
        return statements;
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
