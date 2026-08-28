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
package org.openrewrite.mainframe.db2.bind;

import lombok.Value;
import org.openrewrite.mainframe.controlcard.InStreamCards;
import org.openrewrite.mainframe.db2.bind.tree.Bind;
import org.openrewrite.mainframe.jcl.tree.Jcl;

import java.util.ArrayList;
import java.util.List;

/**
 * A bind deck written inside a job rather than in a library of its own, as the in-stream data of a
 * {@code SYSTSIN} DD.
 * <p>
 * Both shapes occur in a real estate and neither stands for the other: the deck a shop keeps as a
 * {@code CARDLIB} member is read once and run from several jobs, while the deck written in the job is
 * the only place its binds exist. The cards are already in the job's own LST, so they are read back
 * from it by printing — a card's layout lives in the white space in front of each word, and where the
 * line ends is what decides whether the next one continues it.
 */
@Value
public class InStreamBindDeck {

    /**
     * The DD the deck was written under. Always {@code SYSTSIN}, since that is the DD the terminal
     * monitor program reads its commands from, but carried so a caller can say where it came from.
     */
    String ddName;

    /**
     * The one-based line of the job the deck's first card is written on.
     */
    int line;

    Bind.CompilationUnit deck;

    public static List<InStreamBindDeck> of(Jcl.CompilationUnit cu) {
        List<InStreamBindDeck> decks = new ArrayList<>();
        for (InStreamCards cards : InStreamCards.of(cu)) {
            // A DD name reads without the procedure step an override names, so //BIND.SYSTSIN is the
            // same DD as //SYSTSIN.
            if ("SYSTSIN".equalsIgnoreCase(cards.getDdName()) && BindLineReader.isBindDeck(cards.getText())) {
                decks.add(new InStreamBindDeck(cards.getDdName(), cards.getLine(),
                        BindParser.parse(cu.getSourcePath(), cards.getText())));
            }
        }
        return decks;
    }
}
