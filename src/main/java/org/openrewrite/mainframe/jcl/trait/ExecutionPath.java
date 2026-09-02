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
package org.openrewrite.mainframe.jcl.trait;

import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Parser;
import org.openrewrite.SourceFile;
import org.openrewrite.mainframe.controlcard.InStreamCards;
import org.openrewrite.mainframe.controlcard.utility.UtilityLineReader;
import org.openrewrite.mainframe.controlcard.utility.UtilityParser;
import org.openrewrite.mainframe.controlcard.utility.trait.UnloadCommand;
import org.openrewrite.mainframe.controlcard.utility.tree.Utility;
import org.openrewrite.mainframe.jcl.JclIsoVisitor;
import org.openrewrite.mainframe.jcl.marker.ExpandedMember;
import org.openrewrite.mainframe.jcl.marker.ParmMember;
import org.openrewrite.mainframe.jcl.marker.ResolvedText;
import org.openrewrite.mainframe.jcl.marker.Symbolic;
import org.openrewrite.mainframe.jcl.tree.Jcl;
import org.openrewrite.mainframe.jcl.tree.Statement;
import org.openrewrite.marker.Markers;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;

/**
 * Whether everything a step does can be read from what was supplied, and every reason it cannot.
 * <p>
 * A step is a chain of names: the EXEC names a procedure, the procedure's cards name symbols, a
 * symbol names the member a DD reads, and the member holds the cards that say what the program
 * actually does. Each link is followed somewhere in the model already — {@link ExpandedMember.Status},
 * {@link Symbolic.Origin#UNDEFINED}, {@link ParmMember.Status}, and what a control card deck codes
 * rather than inherits — and each is worth nothing on its own, because a transformation may only be
 * applied to a step where every one of them held. This puts them together and says so once, with the
 * reasons kept as {@link Gap}s rather than written out as a sentence, so a report can count them.
 * <p>
 * Following every name is not the same as knowing what the step does, and the two are separate
 * answers here. A Db2 unload deck that codes no {@code FORMAT} takes it from the site parmlib on
 * {@code INFPLIB}, which is in no application library: every name in that job resolves, and the job
 * writes a different file the day the parmlib changes. That is {@link Verdict#INHERITED} rather than
 * {@link Verdict#UNRESOLVED}, and only {@link Verdict#RESOLVED} says a step is safe to rewrite
 * without asking anyone anything.
 * <p>
 * The answer is about what was supplied and says so plainly: a job parsed with no procedure library
 * has no symbols filled in and no procedures to miss, and reads as resolved because nothing in it was
 * asked to resolve. Supply the libraries the shop runs and the answer is about the shop.
 * <p>
 * What it does not answer is whether the step could be written another way. A deck that reads a table
 * space once and writes three files has no equivalent in the base utility, and every name in it
 * resolves; that is a finding about the translation, and this is a finding about the source.
 */
@Value
public class ExecutionPath implements Trait<Jcl.JobControlStatement> {

    Cursor cursor;

    public Step getStep() {
        return new Step(cursor);
    }

    /**
     * Whether every name the step is written with was followed. False leaves nothing to stand on:
     * what the step runs is not in what was read.
     */
    public boolean isResolved() {
        return getVerdict() != Verdict.UNRESOLVED;
    }

    public Verdict getVerdict() {
        return verdictOf(getGaps());
    }

    /**
     * The verdict a list of gaps adds up to, for a caller that already holds them — the worst one
     * any of them carries, and {@link Verdict#RESOLVED} for none at all.
     */
    public static Verdict verdictOf(Collection<Gap> gaps) {
        Verdict verdict = Verdict.RESOLVED;
        for (Gap gap : gaps) {
            if (gap.getKind().getVerdict().compareTo(verdict) > 0) {
                verdict = gap.getKind().getVerdict();
            }
        }
        return verdict;
    }

    /**
     * Every reason this step does not read, in the order they were found, the steps of a procedure it
     * calls included. The same reason found twice is reported once.
     */
    public List<Gap> getGaps() {
        Jcl.CompilationUnit cu = cursor.firstEnclosingOrThrow(Jcl.CompilationUnit.class);
        Set<Gap> gaps = new LinkedHashSet<>();
        collect(getStep(), cu, decksOf(cu), gaps);
        return new ArrayList<>(gaps);
    }

    private static void collect(Step step, Jcl.CompilationUnit cu, Map<UUID, InStreamCards> decks,
                                Set<Gap> gaps) {
        String name = step.getQualifiedName();
        step.getTree().getMarkers().findFirst(ExpandedMember.class)
                .filter(member -> member.getStatus() == ExpandedMember.Status.MISSING)
                .ifPresent(member -> gaps.add(new Gap(Gap.Kind.PROCEDURE_MISSING, name, "",
                        member.getMemberName())));
        statuses(step.getTree(), name, "", gaps);

        String ddName = "";
        for (Statement statement : Steps.withinStep(step.getCursor())) {
            // The procedure's cards are read as its own steps below. A card of a deck is read by the
            // program rather than by the job: an unload deck's &DB. is written like a JCL symbol,
            // resolves at run time, and nothing in the job was ever going to set it.
            if (statement instanceof Jcl.Expansion || statement instanceof Jcl.DataDefinitionStream) {
                continue;
            }
            if (Steps.isNamedDataDefinition(statement)) {
                ddName = ((Jcl.JobControlStatement) statement).getSimpleName();
            }
            statuses(statement, name, ddName, gaps);
        }

        cards(step, cu, decks, gaps);
        for (Step called : step.getProcedureSteps()) {
            collect(called, cu, decks, gaps);
        }
    }

    /**
     * What the statuses left on a card say. A symbol nothing set and a member nobody supplied are
     * both written on the parameter that named them, so one walk finds both.
     */
    private static void statuses(Statement statement, String step, String ddName, Set<Gap> gaps) {
        new JclIsoVisitor<Integer>() {
            @Override
            public Markers visitMarkers(@Nullable Markers markers, Integer p) {
                if (markers != null) {
                    markers.findFirst(ResolvedText.class).ifPresent(resolved -> {
                        for (Symbolic symbolic : resolved.getSymbolics()) {
                            if (symbolic.getOrigin() == Symbolic.Origin.UNDEFINED) {
                                gaps.add(new Gap(Gap.Kind.SYMBOL_UNDEFINED, step, ddName,
                                        symbolic.getName()));
                            }
                        }
                    });
                    markers.findFirst(ParmMember.class)
                            .filter(member -> member.getStatus() == ParmMember.Status.MISSING)
                            .ifPresent(member -> gaps.add(new Gap(Gap.Kind.MEMBER_MISSING, step,
                                    ddName, member.getMemberName())));
                }
                return super.visitMarkers(markers, p);
            }
        }.visit(statement, 0);
    }

    /**
     * What the cards the step is handed say about themselves. A Db2 utility deck is read here because
     * it is the one control card language whose keywords may be answered somewhere else; a deck of
     * another language is left to the reader that claims it.
     */
    private static void cards(Step step, Jcl.CompilationUnit cu, Map<UUID, InStreamCards> decks,
                              Set<Gap> gaps) {
        String name = step.getQualifiedName();
        for (DataDefinition dd : step.getDataDefinitions()) {
            InStreamCards cards = decks.get(dd.getTree().getId());
            if (cards == null || !UtilityLineReader.isUtilityDeck(cards.getText())) {
                continue;
            }
            Utility.CompilationUnit deck = read(cu, cards);
            if (deck == null) {
                gaps.add(new Gap(Gap.Kind.CARDS_NOT_READ, name, cards.getDdName(), ""));
                continue;
            }
            new UnloadCommand.Matcher().lower(deck).forEach(unload -> {
                for (String keyword : unload.getInheritedKeywords()) {
                    gaps.add(new Gap(Gap.Kind.KEYWORD_INHERITED, name, cards.getDdName(), keyword));
                }
            });
        }
    }

    /**
     * The deck, or null where it does not read as one. It goes through the parser's inputs rather
     * than its plain read of a deck, because that one reports a syntax error to an execution context
     * that throws it away, and a deck read into half a tree would be reported here as one that says
     * nothing is missing.
     */
    private static Utility.@Nullable CompilationUnit read(Jcl.CompilationUnit cu, InStreamCards cards) {
        ExecutionContext ctx = new InMemoryExecutionContext(t -> {
            throw new IllegalStateException(t);
        });
        SourceFile deck = UtilityParser.builder().build()
                .parseInputs(singletonList(Parser.Input.fromString(cu.getSourcePath(), cards.getText())),
                        null, ctx)
                .findFirst().orElse(null);
        return deck instanceof Utility.CompilationUnit ? (Utility.CompilationUnit) deck : null;
    }

    /**
     * The decks of the job by the DD each belongs to. Reading them costs a print of the job, so a job
     * that hands no step any cards does not pay for it.
     */
    private static Map<UUID, InStreamCards> decksOf(Jcl.CompilationUnit cu) {
        if (!hasCards(cu.getStatements())) {
            return emptyMap();
        }
        Map<UUID, InStreamCards> decks = new HashMap<>();
        for (InStreamCards cards : InStreamCards.of(cu)) {
            decks.put(cards.getDataDefinition(), cards);
        }
        return decks;
    }

    private static boolean hasCards(List<Statement> statements) {
        for (Statement statement : statements) {
            if (statement instanceof Jcl.DataDefinitionStream ||
                (statement instanceof Jcl.Expansion &&
                 hasCards(((Jcl.Expansion) statement).getStatements()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * How far a step reads, worst last: the order is what {@link #verdictOf} compares.
     */
    public enum Verdict {

        /** Every name was followed and everything the step does is written in what was read. */
        RESOLVED,

        /**
         * Every name was followed, and something the step does is a setting written outside the
         * source. The job runs; what it writes is not decided here.
         */
        INHERITED,

        /** A name the step is written with was not followed, so what it runs is not known. */
        UNRESOLVED
    }

    /**
     * Only the steps written in the member being read. A step of a procedure is part of the path of
     * the step that called it and is reported there, so matching both would report one execution
     * twice.
     */
    public static class Matcher extends SimpleTraitMatcher<ExecutionPath> {

        @Override
        protected @Nullable ExecutionPath test(Cursor cursor) {
            Object value = cursor.getValue();
            return value instanceof Jcl.JobControlStatement &&
                   ((Jcl.JobControlStatement) value).isOperation("EXEC") &&
                   Steps.expansionOf(cursor) == null ? new ExecutionPath(cursor) : null;
        }
    }

    @Override
    public String toString() {
        return getVerdict() + " " + getStep().getName();
    }
}
