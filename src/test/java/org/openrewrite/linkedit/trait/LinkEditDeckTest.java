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
package org.openrewrite.linkedit.trait;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.openrewrite.linkedit.Assertions.linkEdit;

class LinkEditDeckTest implements RewriteTest {

    @Test
    void moduleEntryAndTheObjectsItIsBuiltFrom() {
        rewriteRun(
          linkEdit(
            """
              *  CLMC020 - CLAIM INQUIRY.  CLMU020 IS CALLED STATICALLY.
                INCLUDE SYSLIB(DFHECI)
                INCLUDE OBJLIB(CLMU020)
                ENTRY CLMC020
                NAME CLMC020(R)
              """,
            spec -> spec.afterRecipe(cu -> {
                LinkEditDeck deck = new LinkEditDeck.Matcher().require(cu, null);
                assertThat(deck.getModule().getText()).isEqualTo("CLMC020");
                assertThat(deck.getModule().getLine()).isEqualTo(5);
                assertThat(deck.isReplacing()).isTrue();
                assertThat(deck.getEntry().getText()).isEqualTo("CLMC020");
                assertThat(deck.getEntry().getLine()).isEqualTo(4);
                assertThat(deck.getIncludes())
                  .extracting(LinkEditDeck.Include::getDdName, LinkEditDeck.Include::getMember, LinkEditDeck.Include::getLine)
                  .containsExactly(tuple("SYSLIB", "DFHECI", 2), tuple("OBJLIB", "CLMU020", 3));
            })
          )
        );
    }

    /**
     * A DL/I program's entry is the {@code DLITCBL} label it declares, not the program's own name, so
     * the entry and the module are two different answers and both are worth having.
     */
    @Test
    void entryThatIsALabelRatherThanTheProgram() {
        rewriteRun(
          linkEdit(
            """
                INCLUDE RESLIB(DFSLI000)
                ENTRY DLITCBL
                NAME CLMI010(R)
              """,
            spec -> spec.afterRecipe(cu -> {
                LinkEditDeck deck = new LinkEditDeck.Matcher().require(cu, null);
                assertThat(deck.getEntry().getText()).isEqualTo("DLITCBL");
                assertThat(deck.getModule().getText()).isEqualTo("CLMI010");
            })
          )
        );
    }

    @Test
    void aliasesAreDirectoryEntriesOfTheirOwn() {
        rewriteRun(
          linkEdit(
            """
                ENTRY GVBMR95
                ALIAS GVBMR95E,GVBMR95R
                NAME GVBMR95(R)
              """,
            spec -> spec.afterRecipe(cu -> {
                LinkEditDeck deck = new LinkEditDeck.Matcher().require(cu, null);
                assertThat(deck.getAliases()).extracting(LinkEditDeck.Name::getText)
                  .containsExactly("GVBMR95E", "GVBMR95R");
                assertThat(deck.getAliases()).allSatisfy(alias -> assertThat(alias.getLine()).isEqualTo(2));
            })
          )
        );
    }

    /**
     * What a deck leaves out is as much of an answer as what it names: the reserve calculation is
     * included here and so is bound in, while the one the program calls through a data name is a load
     * module of its own and appears in no {@code INCLUDE} at all.
     */
    @Test
    void aModuleThatBindsSomeOfWhatItCalls() {
        rewriteRun(
          linkEdit(
            """
                INCLUDE OBJLIB(CLMU010,CLMU030)
                ENTRY CLMB020
                NAME CLMB020(R)
              """,
            spec -> spec.afterRecipe(cu -> {
                LinkEditDeck deck = new LinkEditDeck.Matcher().require(cu, null);
                assertThat(deck.getIncludes()).extracting(LinkEditDeck.Include::getMember)
                  .containsExactly("CLMU010", "CLMU030");
                assertThat(deck.getIncludes()).allSatisfy(include ->
                  assertThat(include.getDdName()).isEqualTo("OBJLIB"));
            })
          )
        );
    }

    @Test
    void aDeckThatOnlyAddsToAModuleBuiltElsewhere() {
        rewriteRun(
          linkEdit(
            """
                INCLUDE SYSLMOD(EPSMPMT)
                INCLUDE SYSLIB(EPSMLIST)
                NAME EPSMLIST(R)
              """,
            spec -> spec.afterRecipe(cu -> {
                LinkEditDeck deck = new LinkEditDeck.Matcher().require(cu, null);
                assertThat(deck.getEntry()).isNull();
                assertThat(deck.getModule().getText()).isEqualTo("EPSMLIST");
                assertThat(deck.getIncludes()).extracting(LinkEditDeck.Include::getMember)
                  .containsExactly("EPSMPMT", "EPSMLIST");
            })
          )
        );
    }
}
