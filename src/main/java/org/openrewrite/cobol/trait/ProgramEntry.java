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
package org.openrewrite.cobol.trait;

import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.cobol.tree.Cobol;
import org.openrewrite.cobol.tree.Identifier;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * The point at which a program is handed its PCBs.
 * <p>
 * An IMS program receives one PCB per database or destination its PSB declares, and the binding is
 * positional: the PSB's first PCB arrives as the first argument, and nothing in the COBOL names the
 * database. The order is what lets a DL/I call be attributed to a database rather than to a
 * variable, which is why an IMS analysis has to read the entry point before it reads any call.
 * <p>
 * Mapping a position onto a database name additionally needs the PSB, which lives outside the COBOL.
 */
@Value
public class ProgramEntry implements Trait<Cobol> {

    Cursor cursor;

    /**
     * PCB names in the order the PSB supplies them.
     */
    public List<String> getPcbNames() {
        List<String> names = new ArrayList<>();
        Cobol tree = getTree();
        if (tree instanceof Cobol.Entry) {
            List<Identifier> identifiers = ((Cobol.Entry) tree).getIdentifiers();
            if (identifiers != null) {
                for (Identifier identifier : identifiers) {
                    add(names, identifier);
                }
            }
        } else {
            for (Cobol parameter :
                    ((Cobol.ProcedureDivisionUsingClause) tree).getProcedureDivisionUsingParameter()) {
                add(names, parameter);
            }
        }
        return names;
    }

    /**
     * The zero based position of {@code pcbName} in the PSB, or -1 when the name is not a PCB the
     * program was handed. The position is what a PSB listing has to be joined on to name the
     * database.
     */
    public int indexOf(@Nullable String pcbName) {
        return pcbName == null ? -1 : getPcbNames().indexOf(pcbName.toUpperCase(Locale.ROOT));
    }

    /**
     * Whether {@code pcbName} is the I/O PCB, meaning calls against it read and write the message
     * queue rather than a database.
     * <p>
     * Message driven programs receive the I/O PCB first, which is the signal used here alongside the
     * naming conventions the field has settled on. A batch DL/I program with no message queue has no
     * I/O PCB at all, so a first position match is only trusted when the name does not look like a
     * database PCB.
     */
    public boolean isIoPcb(@Nullable String pcbName) {
        if (pcbName == null) {
            return false;
        }
        String upper = pcbName.toUpperCase(Locale.ROOT);
        if (upper.contains("IO-PCB") || upper.contains("IOPCB") ||
            upper.contains("TP-PCB") || upper.contains("TPPCB")) {
            return true;
        }
        List<String> names = getPcbNames();
        return !names.isEmpty() && names.get(0).equals(upper) &&
               !upper.contains("DB-PCB") && !upper.contains("DBPCB");
    }

    /**
     * Whether {@code pcbName} names a message destination rather than a database: the I/O PCB the
     * reply goes back on, or an alternate PCB, which sends to some other logical terminal — a branch
     * printer, say. Both are TP PCBs, and what an {@code ISRT} against one names in its fourth
     * argument is a MOD and not a segment search argument.
     * <p>
     * Only the PSB says which a mask really is, so an alternate PCB is recognised the way the I/O PCB
     * is, by the naming conventions the field has settled on.
     */
    public boolean isMessagePcb(@Nullable String pcbName) {
        if (isIoPcb(pcbName)) {
            return true;
        }
        String upper = pcbName == null ? "" : pcbName.toUpperCase(Locale.ROOT);
        return upper.contains("ALT-PCB") || upper.contains("ALTPCB");
    }

    private static void add(List<String> names, Cobol tree) {
        String pcb = Names.upperOf(tree);
        if (pcb != null) {
            names.add(pcb);
        }
    }

    public static class Matcher extends SimpleTraitMatcher<ProgramEntry> {

        @Override
        protected @Nullable ProgramEntry test(Cursor cursor) {
            Object value = cursor.getValue();
            return value instanceof Cobol.Entry || value instanceof Cobol.ProcedureDivisionUsingClause ?
                    new ProgramEntry(cursor) : null;
        }
    }

    /**
     * The entry point of the program {@code cursor} is inside, or null for one that takes nothing.
     * <p>
     * {@code PROCEDURE DIVISION USING} is the entry point where a program has one, and it hangs off
     * an ancestor of every statement, so no search is needed to reach it. Only an IMS batch program,
     * which is entered at {@code ENTRY 'DLITCBL' USING} instead, has to be looked through — and the
     * answer is kept on the division's cursor, because every DL/I call in the program asks for it.
     */
    public static @Nullable ProgramEntry of(Cursor cursor) {
        Cursor division = enclosing(cursor);
        if (division == null) {
            return null;
        }
        Cobol.ProcedureDivision procedureDivision = division.getValue();
        if (procedureDivision.getProcedureDivisionUsingClause() != null) {
            return new ProgramEntry(new Cursor(division,
                    procedureDivision.getProcedureDivisionUsingClause()));
        }
        Optional<ProgramEntry> entry = division.computeMessageIfAbsent("cobol.programEntry",
                k -> new Matcher().lower(division).findFirst());
        return entry.orElse(null);
    }

    private static @Nullable Cursor enclosing(Cursor cursor) {
        for (Iterator<Cursor> path = cursor.getPathAsCursors(); path.hasNext(); ) {
            Cursor enclosing = path.next();
            if (enclosing.getValue() instanceof Cobol.ProcedureDivision) {
                return enclosing;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "ENTRY " + getPcbNames();
    }
}
