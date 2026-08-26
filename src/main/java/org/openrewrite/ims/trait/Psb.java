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
package org.openrewrite.ims.trait;

import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.ims.tree.Ims;
import org.openrewrite.ims.tree.Statement;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A program specification block: the {@code PSBGEN} and the PCBs written above it.
 * <p>
 * A program is handed one mask per PCB and the binding is positional — nothing in the COBOL names a
 * database — so this is what turns the position a {@code DliCall} reports into the database it
 * reached. {@link #getPcbAtMask} is that answer.
 */
@Value
public class Psb implements Trait<Ims.MacroStatement> {

    Cursor cursor;

    public String getName() {
        String name = Operands.firstOf(getTree(), "PSBNAME");
        return name == null ? "" : name;
    }

    /**
     * The language the program is written in, from {@code LANG=}: {@code COBOL}, {@code PLI},
     * {@code ASSEM} or {@code PASCAL}. DL/I hands every one of them the same list, so this says how
     * the masks are declared and not which they are.
     */
    public @Nullable String getLanguage() {
        return Operands.firstOf(getTree(), "LANG");
    }

    /**
     * Whether the PSB is generated {@code CMPAT=YES}, which is what gives a batch program the I/O PCB
     * no {@code PCB} statement codes. A BMP needs one for {@code CHKP} and {@code XRST}, so this is
     * how a BMP is told from a DL/I batch program.
     */
    public boolean isCompatible() {
        return "YES".equalsIgnoreCase(Operands.firstOf(getTree(), "CMPAT"));
    }

    /**
     * The size of the largest I/O area the program moves a segment into, from {@code IOASIZE=}.
     */
    public @Nullable Integer getIoAreaSize() {
        return Operands.integerOf(getTree(), "IOASIZE");
    }

    /**
     * The room the program's segment search arguments need, from {@code SSASIZE=}.
     */
    public @Nullable Integer getSsaSize() {
        return Operands.integerOf(getTree(), "SSASIZE");
    }

    /**
     * The PCBs in the order they are written, which is the order the program is handed them.
     */
    public List<Pcb> getPcbs() {
        List<Pcb> pcbs = new ArrayList<>();
        for (Statement statement : Definitions.withinPsb(cursor)) {
            new Pcb.Matcher().get(new Cursor(cursor.getParentOrThrow(), statement)).ifPresent(pcbs::add);
        }
        return pcbs;
    }

    /**
     * The PCB a program reaches by name, which is what the AIB interface does: the name goes in
     * {@code AIBRSNM1} and the position is never counted.
     */
    public @Nullable Pcb getPcb(String name) {
        for (Pcb pcb : getPcbs()) {
            if (name.equalsIgnoreCase(pcb.getName())) {
                return pcb;
            }
        }
        return null;
    }

    /**
     * The database PCBs alone, which is what a command level program numbers: {@code EXEC DLI} names
     * a PCB by its place among these and not by its place among the masks.
     */
    public List<Pcb> getDatabasePcbs() {
        List<Pcb> pcbs = new ArrayList<>();
        for (Pcb pcb : getPcbs()) {
            if (pcb.isDatabase()) {
                pcbs.add(pcb);
            }
        }
        return pcbs;
    }

    /**
     * The PCB an {@code EXEC DLI ... PCB(number)} names, one based, or null where the PSB has no such
     * database PCB.
     */
    public @Nullable Pcb getDatabasePcb(int number) {
        List<Pcb> pcbs = getDatabasePcbs();
        return number >= 1 && number <= pcbs.size() ? pcbs.get(number - 1) : null;
    }

    /**
     * The PCB a program's {@code mask}th argument is, one based, or null where the mask is the I/O
     * PCB or the PSB codes no such PCB.
     * <p>
     * {@code ProgramEntry.indexOf} gives the position of a mask among the ones the program is handed,
     * zero based, so a caller passes that plus one. Which PCB it is depends on the program: a message
     * driven program and a BMP are handed the I/O PCB first though no statement here codes it, so
     * every mask after it is one place along.
     */
    public @Nullable Pcb getPcbAtMask(int mask, ProgramKind kind) {
        int index = mask - 1 - (receivesIoPcb(kind) ? 1 : 0);
        List<Pcb> pcbs = getPcbs();
        return index >= 0 && index < pcbs.size() ? pcbs.get(index) : null;
    }

    /**
     * Whether the program is handed an I/O PCB the PSB never codes, which makes mask 1 the message
     * queue rather than a database.
     */
    public boolean receivesIoPcb(ProgramKind kind) {
        return kind == ProgramKind.MESSAGE_DRIVEN || isCompatible();
    }

    public int getLine() {
        return Definitions.lineOf(cursor, getTree().getOperation());
    }

    /**
     * What kind of program runs under the PSB, which is the one thing the mask order needs that the
     * PSB does not say.
     */
    public enum ProgramKind {
        /**
         * A DL/I batch program or a BMP, entered at {@code ENTRY 'DLITCBL'} from a job step. It is
         * handed an I/O PCB only where the PSB is generated {@code CMPAT=YES}, which is what a BMP
         * needs and a DL/I batch program does not have.
         */
        BATCH,
        /**
         * A program the message region schedules, which stage 1 declares {@code PGMTYPE=TP}. It is
         * handed the I/O PCB first whatever {@code CMPAT} says.
         */
        MESSAGE_DRIVEN
    }

    public static class Matcher extends SimpleTraitMatcher<Psb> {

        @Override
        protected @Nullable Psb test(Cursor cursor) {
            Object value = cursor.getValue();
            if (!(value instanceof Ims.MacroStatement)) {
                return null;
            }
            Ims.MacroStatement statement = (Ims.MacroStatement) value;
            return statement.isOperation("PSBGEN") && Operands.firstOf(statement, "PSBNAME") != null ?
                    new Psb(cursor) : null;
        }
    }

    @Override
    public String toString() {
        return "PSBGEN " + getName().toUpperCase(Locale.ROOT);
    }
}
