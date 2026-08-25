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
package org.openrewrite.linkedit;

import lombok.Value;
import org.openrewrite.controlcard.InStreamCards;
import org.openrewrite.jcl.tree.Jcl;
import org.openrewrite.linkedit.tree.LinkEdit;

import java.util.ArrayList;
import java.util.List;

/**
 * A link-edit deck written inside a job rather than in a library of its own, as the in-stream data of
 * the DD the link-edit step reads its control statements from.
 * <p>
 * Both shapes occur in a real estate and neither stands for the other: the deck a shop keeps as a
 * {@code LINKLIB} member is read once and run from several jobs, while the deck written in the job is
 * the only place its composition exists. The cards are already in the job's own LST, so they are read
 * back from it by printing — a card's layout lives in the white space in front of each word, and
 * column 72 of each card is what decides whether the next one continues it.
 */
@Value
public class InStreamLinkEditDeck {

    /**
     * The DD the deck was written under: {@code SYSLIN}, which the binder reads its object modules and
     * control statements from, or the {@code SYSIN} that a compile-and-link procedure concatenates to
     * it.
     */
    String ddName;

    /**
     * The one-based line of the job the deck's first card is written on.
     */
    int line;

    LinkEdit.CompilationUnit deck;

    public static List<InStreamLinkEditDeck> of(Jcl.CompilationUnit cu) {
        List<InStreamLinkEditDeck> decks = new ArrayList<>();
        for (InStreamCards cards : InStreamCards.of(cu)) {
            // A DD name reads without the procedure step an override names, so //LKED.SYSIN is the
            // same DD as //SYSIN.
            if (("SYSLIN".equalsIgnoreCase(cards.getDdName()) || "SYSIN".equalsIgnoreCase(cards.getDdName())) &&
                LinkEditLineReader.isLinkEditDeck(cards.getText())) {
                decks.add(new InStreamLinkEditDeck(cards.getDdName(), cards.getLine(),
                        LinkEditParser.parse(cu.getSourcePath(), cards.getText())));
            }
        }
        return decks;
    }
}
