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
package org.openrewrite.mainframe.assembler.trait;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.mainframe.assembler.tree.Assembler;
import org.openrewrite.mainframe.cobol.trait.DliCall.Access;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static java.util.Collections.emptyList;

/**
 * An IMS DL/I call written in assembler: {@code CALL ASMTDLI,(function,pcb,io-area,ssa...),VL}.
 * <p>
 * {@code ASMTDLI} is the assembler entry point of the same {@code DFSLI000} a COBOL program reaches as
 * {@code CBLTDLI}, so the convention is the one {@code org.openrewrite.mainframe.cobol.trait.DliCall} applies and
 * this reports the same things by the same names: a recipe that builds a table of everything reaching
 * IMS puts both in it without caring which language asked.
 * <p>
 * What differs is where the answers come from. A COBOL program moves a literal into working storage
 * and passes the field; an assembler program names a constant, so the function comes from the
 * {@code DC CL4'GHN '} the first argument points at and the segment from the first eight bytes of the
 * SSA's. And a PCB is often an address already in a register — {@code (R11)} — which is a position no
 * count of the entry parameters can recover, so the register is reported as it was written.
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DliCall implements Trait<Assembler.Instruction> {

    Cursor cursor;

    /**
     * The language interface called: {@code ASMTDLI}, {@code CBLTDLI}, {@code AIBTDLI},
     * {@code CEETDLI} or {@code PLITDLI}.
     */
    String iface;

    /**
     * The DL/I function, upper cased and trimmed of the padding the four byte code carries:
     * {@code GHN}, {@code DLET}, {@code ISRT}. Null where the constant the first argument names is not
     * declared in this member.
     */
    @Nullable
    String function;

    /**
     * The name of the first argument as written, whether or not {@link #function} could be read
     * from it.
     */
    String functionOperand;

    /**
     * The PCB argument as written: a name, or {@code (R11)} for a mask already addressed by a
     * register.
     */
    @Nullable
    String pcb;

    /**
     * The I/O area argument, into or out of which the segment is moved.
     */
    @Nullable
    String ioArea;

    /**
     * Segment search argument names, in order.
     */
    List<String> ssas;

    /**
     * Segment names read from the SSAs, which hold the name in their first eight bytes. Empty for a
     * call that passes none — a {@code DLET} takes the segment the {@code GHN} before it held.
     */
    List<String> segments;

    /**
     * The register the PCB is addressed by, without the parentheses, or null where the call names the
     * mask.
     */
    public @Nullable String getPcbRegister() {
        return pcb != null && pcb.startsWith("(") && pcb.endsWith(")") ?
                pcb.substring(1, pcb.length() - 1) : null;
    }

    /**
     * What this call does to the database, by the same reading COBOL's DL/I calls get.
     */
    public Access getAccess() {
        return Access.of(function);
    }

    public int getLine() {
        return Statements.lineOf(cursor, getTree().getOperation());
    }

    /**
     * The DL/I language interfaces. A program written in assembler calls {@code ASMTDLI}; the others
     * appear where assembler is sharing a load module with another language.
     */
    private static final Set<String> INTERFACES = new HashSet<>(Arrays.asList(
            "ASMTDLI", "CBLTDLI", "AIBTDLI", "CEETDLI", "PLITDLI"));

    /**
     * Whether a called name is the DL/I language interface rather than a program of the estate.
     */
    public static boolean isInterface(String name) {
        return INTERFACES.contains(name.toUpperCase(Locale.ROOT));
    }

    /**
     * The bytes of an SSA the segment name occupies.
     */
    private static final int SEGMENT_NAME_LENGTH = 8;

    public static class Matcher extends SimpleTraitMatcher<DliCall> {

        @Override
        protected @Nullable DliCall test(Cursor cursor) {
            Object value = cursor.getValue();
            if (!(value instanceof Assembler.Instruction)) {
                return null;
            }
            Assembler.Instruction call = (Assembler.Instruction) value;
            if (!call.isOperation("CALL")) {
                return null;
            }
            String target = call.getOperandText(0);
            if (target == null || !isInterface(target)) {
                return null;
            }

            List<String> arguments = argumentsOf(call);
            if (arguments.isEmpty()) {
                return null;
            }
            String functionOperand = arguments.get(0);
            String function = Statements.constantOf(cursor, functionOperand);
            List<String> ssas = arguments.size() > 3 ?
                    new ArrayList<>(arguments.subList(3, arguments.size())) : emptyList();
            return new DliCall(cursor, target.toUpperCase(Locale.ROOT),
                    function == null ? null : function.trim().toUpperCase(Locale.ROOT),
                    functionOperand,
                    arguments.size() > 1 ? arguments.get(1) : null,
                    arguments.size() > 2 ? arguments.get(2) : null,
                    ssas, segmentsOf(cursor, ssas));
        }
    }

    /**
     * The parenthesised argument list, which is the one operand of the call that is a list.
     */
    private static List<String> argumentsOf(Assembler.Instruction call) {
        for (Assembler.Operand operand : call.getParameters()) {
            String text = operand.getText();
            if (text.startsWith("(") && text.endsWith(")")) {
                return operand.getMembers();
            }
        }
        return emptyList();
    }

    /**
     * The segment each SSA names, from the constant it is built out of. An SSA holds the segment name
     * in its first eight bytes, so a member that declares the SSA says which segment the call asked
     * for.
     */
    private static List<String> segmentsOf(Cursor cursor, List<String> ssas) {
        List<String> segments = new ArrayList<>(ssas.size());
        for (String ssa : ssas) {
            String constant = Statements.constantOf(cursor, ssa);
            if (constant == null) {
                continue;
            }
            String name = constant.length() > SEGMENT_NAME_LENGTH ?
                    constant.substring(0, SEGMENT_NAME_LENGTH) : constant;
            name = name.trim().toUpperCase(Locale.ROOT);
            if (!name.isEmpty()) {
                segments.add(name);
            }
        }
        return segments;
    }

    @Override
    public String toString() {
        return iface + " " + (function == null ? functionOperand : function) +
               (pcb == null ? "" : " via " + pcb) + (segments.isEmpty() ? "" : " " + segments);
    }
}
