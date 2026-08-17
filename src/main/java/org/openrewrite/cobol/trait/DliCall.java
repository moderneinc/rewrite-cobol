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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.cobol.CobolIsoVisitor;
import org.openrewrite.cobol.tree.Cobol;
import org.openrewrite.cobol.tree.CobolPreprocessor;
import org.openrewrite.marker.SearchResult;
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
 * An IMS DL/I database or message call, however it was written: {@code CALL 'CBLTDLI'},
 * {@code CALL 'AIBTDLI'}, {@code CALL 'CEETDLI'} or {@code EXEC DLI}.
 * <p>
 * Call level DL/I is an ordinary COBOL {@code CALL}, so nothing about it stands out in an LST without
 * knowing the convention: the first argument is the function code, the second names a PCB, and the
 * trailing arguments are segment search arguments. This applies that convention, and reads the
 * command level form the same way so that a caller does not have to care which was written.
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DliCall implements Trait<Cobol> {

    /**
     * The {@code Cobol.Call} for a call level DL/I, or the stand-in word an {@code EXEC DLI} block
     * hangs off.
     */
    Cursor cursor;

    /**
     * The language interface called: {@code CBLTDLI}, {@code AIBTDLI}, {@code CEETDLI},
     * {@code PLITDLI}, or {@code EXEC DLI} for the command level interface.
     */
    String iface;

    /**
     * The DL/I function, upper cased and trimmed of the padding the four byte function code carries.
     * {@code GU}, {@code GHU}, {@code GN}, {@code ISRT}, {@code REPL}, {@code DLET}, {@code CHKP}.
     * <p>
     * Null when the function code is passed in a data item holding no single literal, which is the
     * common case: programs move {@code 'GU  '} into a working storage field and pass that, so it is
     * resolved through {@link LiteralAssignment} where it can be.
     */
    @Nullable
    String function;

    /**
     * The data name of the function code argument as written, whether or not {@link #function}
     * could be resolved from it.
     */
    String functionOperand;

    /**
     * The PCB argument's data name. Which database this reaches depends on the PCB's position in
     * the PSB, which {@link #getPcbPosition()} recovers from the program's entry point.
     */
    @Nullable
    String pcb;

    /**
     * The I/O area argument's data name, into or from which the segment is moved.
     */
    @Nullable
    String ioArea;

    /**
     * Segment search argument data names, in order. Qualification lives inside these at run time.
     */
    List<String> ssas;

    /**
     * Segment names resolved from the SSAs or, for {@code EXEC DLI}, from the {@code SEGMENT}
     * option. Empty when the call is unqualified or the segment name is not statically known.
     */
    List<String> segments;

    /**
     * What this call does to the database, derived from {@link #function}.
     */
    public Access getAccess() {
        return Access.of(function);
    }

    /**
     * The entry point that handed this program its PCBs, or null where it takes none.
     */
    public @Nullable ProgramEntry getEntry() {
        return ProgramEntry.of(cursor);
    }

    /**
     * The PCB's position in the PSB, which is what names the database, or -1 where the program was
     * handed no PCBs to count.
     */
    public int getPcbPosition() {
        ProgramEntry entry = getEntry();
        return entry == null ? -1 : entry.indexOf(pcb);
    }

    /**
     * Whether this call is against the I/O PCB, meaning it reads or writes the message queue rather
     * than a database. Resolving this needs the order the PCBs arrived in; see
     * {@link ProgramEntry#isIoPcb(String)}.
     */
    public boolean isMessageCall() {
        ProgramEntry entry = getEntry();
        return pcb != null && entry != null && entry.isIoPcb(pcb);
    }

    /**
     * This call with a search result on it, placed where it will print: on the called name for a
     * call level DL/I, and on the stand-in word for an {@code EXEC DLI}, whose own text prints from
     * the preprocessor statement.
     */
    public Cobol marked(@Nullable String description) {
        Cobol tree = getTree();
        if (tree instanceof Cobol.Call) {
            Cobol.Call call = (Cobol.Call) tree;
            return call.withIdentifier(SearchResult.found(call.getIdentifier(), description));
        }
        return SearchResult.found(tree, description);
    }

    public enum Access {
        READ,
        CREATE,
        UPDATE,
        DELETE,
        CHECKPOINT,
        SYSTEM,
        UNKNOWN;

        public static Access of(@Nullable String function) {
            if (function == null) {
                return UNKNOWN;
            }
            switch (function) {
                case "GU":
                case "GHU":
                case "GN":
                case "GHN":
                case "GNP":
                case "GHNP":
                    return READ;
                case "ISRT":
                    return CREATE;
                case "REPL":
                    return UPDATE;
                case "DLET":
                    return DELETE;
                case "CHKP":
                case "XRST":
                case "SYNC":
                case "ROLB":
                case "ROLL":
                case "ROLS":
                case "SETS":
                case "SETU":
                    return CHECKPOINT;
                case "PCB":
                case "TERM":
                case "APSB":
                case "DPSB":
                case "INIT":
                case "INQY":
                case "LOG":
                case "GSCD":
                case "STAT":
                    return SYSTEM;
                default:
                    return UNKNOWN;
            }
        }
    }

    /**
     * The DL/I language interfaces. {@code CBLTDLI} is the COBOL interface, {@code AIBTDLI} the AIB
     * interface that names a PCB rather than taking it positionally, {@code CEETDLI} the Language
     * Environment interface, and {@code PLITDLI} appears in COBOL that calls shared PL/I modules.
     */
    private static final Set<String> INTERFACES = new HashSet<>(Arrays.asList(
            "CBLTDLI", "AIBTDLI", "CEETDLI", "PLITDLI"));

    /**
     * Segment search argument suffixes never name a segment themselves, so an SSA data name that
     * follows the convention {@code <segment>-SSA} still yields the segment.
     */
    private static final String SSA_SUFFIX = "-SSA";

    public static boolean isDli(CobolPreprocessor.ExecStatement exec) {
        List<CobolPreprocessor.Word> words = exec.getWords();
        return words.size() >= 2 && "DLI".equalsIgnoreCase(words.get(1).getCobolWord().getWord());
    }

    public static class Matcher extends SimpleTraitMatcher<DliCall> {

        /**
         * An {@code EXEC DLI} is a procedure division statement, so the parser gives each block its
         * own stand-in word and there is one call per word — 26 words carrying 26 blocks over the
         * corpus on 2026-08-17, none carrying two.
         */
        @Override
        protected @Nullable DliCall test(Cursor cursor) {
            Object value = cursor.getValue();
            if (value instanceof Cobol.Call) {
                return fromCall(cursor, (Cobol.Call) value);
            }
            if (value instanceof Cobol.Word) {
                for (CobolPreprocessor ps : ((Cobol.Word) value).getPreprocessorStatements()) {
                    if (ps instanceof CobolPreprocessor.ExecStatement && isDli((CobolPreprocessor.ExecStatement) ps)) {
                        return fromExec(cursor, (CobolPreprocessor.ExecStatement) ps);
                    }
                }
            }
            return null;
        }
    }

    private static @Nullable DliCall fromCall(Cursor cursor, Cobol.Call call) {
        String target = Names.of(call.getIdentifier());
        String iface = target == null ? null : Literals.valueOf(target);
        if (iface == null || !INTERFACES.contains(iface.toUpperCase(Locale.ROOT))) {
            return null;
        }

        List<String> arguments = arguments(call);
        if (arguments.isEmpty()) {
            return null;
        }

        // CALL 'CBLTDLI' USING function, pcb, io-area, ssa... — AIBTDLI substitutes an AIB for the PCB.
        String functionOperand = arguments.get(0);
        String function = resolveFunction(functionOperand, cursor);
        String pcb = arguments.size() > 1 ? arguments.get(1) : null;
        String ioArea = arguments.size() > 2 ? arguments.get(2) : null;
        List<String> ssas = arguments.size() > 3 ?
                new ArrayList<>(arguments.subList(3, arguments.size())) : emptyList();

        return new DliCall(cursor, iface.toUpperCase(Locale.ROOT), function, functionOperand, pcb, ioArea,
                ssas, segmentsFromSsas(ssas, cursor));
    }

    /**
     * {@code EXEC DLI GU USING PCB(1) SEGMENT(ACCOUNT) INTO(WS-REC)} names its parts, so unlike the
     * call level form nothing has to be inferred from argument position.
     */
    private static @Nullable DliCall fromExec(Cursor cursor, CobolPreprocessor.ExecStatement exec) {
        List<String> tokens = CicsCommand.tokens(exec);
        if (tokens.isEmpty()) {
            return null;
        }

        String function = tokens.get(0).toUpperCase(Locale.ROOT);
        String pcb = null;
        String ioArea = null;
        List<String> segments = new ArrayList<>(1);
        for (int i = 1; i < tokens.size(); i++) {
            String option = tokens.get(i).toUpperCase(Locale.ROOT);
            String operand = operandAfter(tokens, i);
            if (operand == null) {
                continue;
            }
            if ("PCB".equals(option)) {
                pcb = operand;
            } else if ("SEGMENT".equals(option)) {
                segments.add(Literals.unquote(operand).toUpperCase(Locale.ROOT));
            } else if ("INTO".equals(option) || "FROM".equals(option)) {
                ioArea = operand;
            }
        }
        return new DliCall(cursor, "EXEC DLI", function, function, pcb, ioArea, emptyList(), segments);
    }

    private static @Nullable String operandAfter(List<String> tokens, int i) {
        if (i + 2 < tokens.size() && "(".equals(tokens.get(i + 1))) {
            return tokens.get(i + 2);
        }
        return null;
    }

    /**
     * The {@code USING} arguments of a call, in order. The grammar nests the argument list inside a
     * second {@code CallPhrase} and wraps each argument in a {@code CallBy}, so the names are read
     * back by walking for those.
     */
    private static List<String> arguments(Cobol.Call call) {
        if (call.getCallUsingPhrase() == null) {
            return emptyList();
        }
        List<String> arguments = new ArrayList<>(4);
        new CobolIsoVisitor<List<String>>() {
            @Override
            public Cobol.CallBy visitCallBy(Cobol.CallBy callBy, List<String> acc) {
                String name = Names.upperOf(callBy.getIdentifier());
                if (name != null) {
                    acc.add(name);
                }
                return callBy;
            }
        }.visit(call.getCallUsingPhrase(), arguments);
        return arguments;
    }

    /**
     * The function code a call passes. Programs almost never pass the literal directly; they move
     * {@code 'GU  '} into a field and pass the field, so the field is resolved through the literals
     * it is known to hold.
     */
    private static @Nullable String resolveFunction(String operand, Cursor cursor) {
        String literal = Literals.valueOf(operand);
        if (literal != null) {
            return literal.trim().toUpperCase(Locale.ROOT);
        }
        String resolved = LiteralAssignment.resolve(cursor, operand);
        return resolved == null ? null : resolved.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * Segment names carried by the SSAs. An SSA holds the segment name in its first eight bytes, so
     * the name is recoverable when the SSA is built from a literal, and otherwise from the
     * {@code <segment>-SSA} naming convention that IMS shops follow.
     */
    private static List<String> segmentsFromSsas(List<String> ssas, Cursor cursor) {
        if (ssas.isEmpty()) {
            return emptyList();
        }
        List<String> segments = new ArrayList<>(ssas.size());
        for (String ssa : ssas) {
            String resolved = LiteralAssignment.resolve(cursor, ssa);
            if (resolved != null && !resolved.isEmpty()) {
                // The segment name occupies the first eight bytes of an SSA.
                String name = resolved.length() > 8 ? resolved.substring(0, 8) : resolved;
                name = name.trim().toUpperCase(Locale.ROOT);
                if (!name.isEmpty()) {
                    segments.add(name);
                    continue;
                }
            }
            if (ssa.endsWith(SSA_SUFFIX) && ssa.length() > SSA_SUFFIX.length()) {
                segments.add(ssa.substring(0, ssa.length() - SSA_SUFFIX.length()));
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
