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
package org.openrewrite.mainframe.cobol.trait;

import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.mainframe.cobol.tree.Cobol;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.*;

/**
 * A literal landing in a data item: a {@code VALUE} clause, or a {@code MOVE} of a literal.
 * <p>
 * Mainframe programs name their resources at one remove. {@code MOVE 'PAYROLL' TO WS-PGM} precedes
 * {@code EXEC CICS LINK PROGRAM(WS-PGM)}, and {@code MOVE 'GU  ' TO DLI-FUNC} precedes a DL/I call,
 * so an operand is only as resolvable as the literals that reach it. This is the statement that put
 * a value there; {@link #resolve} is the question a call site asks.
 */
@Value
public class LiteralAssignment implements Trait<Cobol> {

    Cursor cursor;

    /**
     * The data items the literal lands in. A {@code MOVE} names more than one.
     */
    public List<String> getTargets() {
        Cobol tree = getTree();
        if (tree instanceof Cobol.MoveToStatement) {
            List<String> targets = new ArrayList<>(1);
            for (Cobol to : ((Cobol.MoveToStatement) tree).getNames()) {
                String name = Names.of(to);
                if (name != null) {
                    targets.add(name);
                }
            }
            return targets;
        }
        // A VALUE clause names nothing. The item it belongs to is the one it hangs off, which is
        // what the cursor is for.
        Object entry = cursor.getParentTreeCursor().getValue();
        String name = entry instanceof Cobol.DataDescriptionEntry &&
                      ((Cobol.DataDescriptionEntry) entry).getName() != null ?
                ((Cobol.DataDescriptionEntry) entry).getName().getWord() : null;
        return name == null ? emptyList() : singletonList(name);
    }

    /**
     * The literals, unquoted. A condition name is written {@code VALUE 'Y' 'N'} and holds either.
     * Figurative constants are absent: {@code VALUE ZERO} says nothing a resource name can be
     * resolved to.
     */
    public List<String> getValues() {
        List<String> values = new ArrayList<>(1);
        Cobol tree = getTree();
        if (tree instanceof Cobol.MoveToStatement) {
            add(values, Names.of(((Cobol.MoveToStatement) tree).getFrom()));
        } else {
            for (Cobol value : ((Cobol.DataValueClause) tree).getCobols()) {
                add(values, Names.of(value));
            }
        }
        return values;
    }

    private static void add(List<String> values, @Nullable String word) {
        String literal = word == null ? null : Literals.valueOf(word);
        if (literal != null) {
            values.add(literal);
        }
    }

    public static class Matcher extends SimpleTraitMatcher<LiteralAssignment> {

        /**
         * A {@code MOVE} only says what a field holds when it moves a literal: {@code MOVE A TO B}
         * says nothing about B that can be read off the source.
         */
        @Override
        protected @Nullable LiteralAssignment test(Cursor cursor) {
            Object value = cursor.getValue();
            if (value instanceof Cobol.DataValueClause) {
                return new LiteralAssignment(cursor);
            }
            if (value instanceof Cobol.MoveToStatement) {
                String from = Names.of(((Cobol.MoveToStatement) value).getFrom());
                return from != null && Literals.isLiteral(from) ? new LiteralAssignment(cursor) : null;
            }
            return null;
        }
    }

    /**
     * The single literal {@code dataName} is known to hold, or null when it holds none or more than
     * one. A null answer means the operand is genuinely dynamic and a person has to look.
     */
    public static @Nullable String resolve(Cursor cursor, @Nullable String dataName) {
        Set<String> candidates = candidates(cursor, dataName);
        return candidates.size() == 1 ? candidates.iterator().next() : null;
    }

    /**
     * Every literal {@code dataName} is known to hold anywhere in the program.
     * <p>
     * Flow insensitive by choice: this collects what is assigned anywhere rather than what holds at
     * a point. WORKING-STORAGE is static and there are no aliases to chase, so unlike the equivalent
     * analysis in Java this needs no heap model to be worth having.
     */
    public static Set<String> candidates(Cursor cursor, @Nullable String dataName) {
        if (dataName == null) {
            return emptySet();
        }
        Set<String> candidates = valuesIn(cursor).get(dataName.toUpperCase(Locale.ROOT));
        return candidates == null ? emptySet() : unmodifiableSet(candidates);
    }

    /**
     * Kept on the program's cursor rather than gathered again for each question. Every call site in
     * a program resolves its operands, and a program is large enough that walking it once per
     * operand is the difference between a portfolio scan finishing and not.
     */
    private static Map<String, Set<String>> valuesIn(Cursor cursor) {
        Cursor program = enclosing(cursor);
        if (program == null) {
            return emptyMap();
        }
        return program.computeMessageIfAbsent("cobol.literalAssignments", k -> {
            Map<String, Set<String>> values = new HashMap<>();
            new Matcher().lower(program).forEach(assignment -> {
                List<String> literals = assignment.getValues();
                for (String target : assignment.getTargets()) {
                    values.computeIfAbsent(target.toUpperCase(Locale.ROOT), t -> new HashSet<>(2))
                            .addAll(literals);
                }
            });
            return values;
        });
    }

    private static @Nullable Cursor enclosing(Cursor cursor) {
        for (Iterator<Cursor> path = cursor.getPathAsCursors(); path.hasNext(); ) {
            Cursor enclosing = path.next();
            if (enclosing.getValue() instanceof Cobol.ProgramUnit) {
                return enclosing;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return getTargets() + " = " + getValues();
    }
}
