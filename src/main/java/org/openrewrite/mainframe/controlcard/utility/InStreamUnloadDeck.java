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
package org.openrewrite.mainframe.controlcard.utility;

import lombok.Value;
import lombok.With;
import org.openrewrite.mainframe.controlcard.InStreamCards;
import org.openrewrite.mainframe.controlcard.utility.tree.Utility;
import org.openrewrite.mainframe.jcl.tree.Jcl;

import java.util.ArrayList;
import java.util.List;

/**
 * A Db2 utility deck written inside a job rather than in a library of its own.
 * <p>
 * Both shapes occur at a real shop and neither stands for the other: the deck kept as a control card
 * member is read once and run from several jobs, while the deck written in the job is the only place
 * its unload exists.
 */
@Value
@With
public class InStreamUnloadDeck {

    /**
     * Where the deck was read from, and where {@link #write} puts it back.
     */
    InStreamCards cards;

    Utility.CompilationUnit deck;

    /**
     * The DD the deck was written under, usually {@code SYSIN}.
     */
    public String getDdName() {
        return cards.getDdName();
    }

    /**
     * The one-based line of the job the deck's first card is written on.
     */
    public int getLine() {
        return cards.getLine();
    }

    public static List<InStreamUnloadDeck> of(Jcl.CompilationUnit cu) {
        List<InStreamUnloadDeck> decks = new ArrayList<>();
        for (InStreamCards cards : InStreamCards.of(cu)) {
            if (UtilityLineReader.isUtilityDeck(cards.getText())) {
                decks.add(new InStreamUnloadDeck(cards,
                        UtilityParser.parse(cu.getSourcePath(), cards.getText())));
            }
        }
        return decks;
    }

    /**
     * The job with this deck, as it now reads, written back into the cards it came from.
     */
    public Jcl.CompilationUnit write(Jcl.CompilationUnit cu) {
        return cards.write(cu, deck.printAll());
    }
}
