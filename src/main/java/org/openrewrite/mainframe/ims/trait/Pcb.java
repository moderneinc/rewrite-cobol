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
package org.openrewrite.mainframe.ims.trait;

import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.mainframe.ims.tree.Ims;
import org.openrewrite.mainframe.ims.tree.Statement;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A program communication block: one mask the program is handed, and the database or destination
 * behind it.
 * <p>
 * This is where a DL/I call stops being a position and becomes a database. The call names a mask, the
 * PSB says which PCB that is, and {@link #getDatabaseName()} is the DBD the PCB opens.
 */
@Value
public class Pcb implements Trait<Ims.MacroStatement> {

    Cursor cursor;

    /**
     * The name a program may reach the PCB by, from {@code PCBNAME=} or, where a shop labels the
     * macro instead, the label in column 1. Null for the PCBs a program has to count.
     */
    public @Nullable String getName() {
        String name = Operands.firstOf(getTree(), "PCBNAME");
        if (name != null) {
            return name;
        }
        String label = getTree().getSimpleName();
        return label.isEmpty() ? null : label;
    }

    /**
     * What the PCB opens, from {@code TYPE=}: {@code DB}, {@code TP} or {@code GSAM}.
     */
    public @Nullable String getType() {
        return Operands.firstOf(getTree(), "TYPE");
    }

    /**
     * Whether the PCB opens a database, which is the default where no {@code TYPE=} is written.
     */
    public boolean isDatabase() {
        String type = getType();
        return type == null || "DB".equalsIgnoreCase(type);
    }

    /**
     * Whether the PCB opens a sequential file rather than a hierarchy. A GSAM PCB has no
     * {@code SENSEG}, because a GSAM database has no segments.
     */
    public boolean isSequential() {
        return "GSAM".equalsIgnoreCase(getType());
    }

    /**
     * Whether the PCB opens the message queue rather than a database.
     */
    public boolean isMessage() {
        return "TP".equalsIgnoreCase(getType());
    }

    /**
     * The DBD the PCB opens, from {@code DBDNAME=}. Null for a TP PCB, which names a destination
     * instead.
     */
    public @Nullable String getDatabaseName() {
        return Operands.firstOf(getTree(), "DBDNAME");
    }

    /**
     * The logical terminal or transaction a TP PCB sends to, from {@code NAME=}.
     */
    public @Nullable String getDestination() {
        return Operands.firstOf(getTree(), "NAME");
    }

    /**
     * What the program may do through this PCB, from {@code PROCOPT=}: {@code G} to read,
     * {@code A} for all of it, {@code GS} for a forward-only GSAM read, and the rest.
     */
    public @Nullable String getProcessingOptions() {
        return Operands.firstOf(getTree(), "PROCOPT");
    }

    /**
     * The secondary index the database is walked through, from {@code PROCSEQ=}. This is a second
     * database the PCB names, and a {@code GN} under it returns roots in the index's order rather
     * than in the order the randomizer left them in.
     */
    public @Nullable String getProcessingSequence() {
        return Operands.firstOf(getTree(), "PROCSEQ");
    }

    /**
     * How long the longest concatenated key the program sees is, from {@code KEYLEN=}. It is also the
     * length of the key feedback area a database mask ends in.
     */
    public @Nullable Integer getKeyLength() {
        return Operands.integerOf(getTree(), "KEYLEN");
    }

    /**
     * Whether a TP PCB may be sent to, from {@code MODIFY=YES}.
     */
    public boolean isModifiable() {
        return "YES".equalsIgnoreCase(Operands.firstOf(getTree(), "MODIFY"));
    }

    public List<SensitiveSegment> getSensitiveSegments() {
        List<SensitiveSegment> segments = new ArrayList<>();
        for (Statement statement : Definitions.withinPcb(cursor)) {
            new SensitiveSegment.Matcher().get(new Cursor(cursor.getParentOrThrow(), statement))
                    .ifPresent(segments::add);
        }
        return segments;
    }

    /**
     * Where the PCB comes in the PSB, one based. This is not the mask the program sees: an I/O PCB
     * the PSB never codes comes before it, which is what {@link Psb#getPcbAtMask} settles.
     */
    public int getPosition() {
        int position = 1;
        for (Statement statement : Definitions.withinPsb(cursor)) {
            if (Definitions.isOperation(statement, "PCB")) {
                position++;
            }
        }
        return position;
    }

    public @Nullable Psb getPsb() {
        return Definitions.psbOf(cursor);
    }

    public int getLine() {
        return Definitions.lineOf(cursor, getTree().getOperation());
    }

    public static class Matcher extends SimpleTraitMatcher<Pcb> {

        @Override
        protected @Nullable Pcb test(Cursor cursor) {
            Object value = cursor.getValue();
            return value instanceof Ims.MacroStatement && ((Ims.MacroStatement) value).isOperation("PCB") ?
                    new Pcb(cursor) : null;
        }
    }

    @Override
    public String toString() {
        String name = getName();
        return "PCB " + (name == null ? getPosition() : name.toUpperCase(Locale.ROOT));
    }
}
