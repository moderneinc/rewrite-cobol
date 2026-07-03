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
package org.openrewrite.jcl;

import org.jspecify.annotations.Nullable;
import org.openrewrite.jcl.marker.GeneratedParmContent;
import org.openrewrite.jcl.marker.ParmMember;
import org.openrewrite.jcl.tree.Jcl;
import org.openrewrite.jcl.tree.Space;
import org.openrewrite.jcl.tree.Statement;
import org.openrewrite.marker.Markers;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.openrewrite.Tree.randomId;

/**
 * Resolves external SYSIN/SYSTSIN (and other input control) DD statements that reference a
 * PDS member, e.g. {@code //SYSIN DD DSN=DWL.PARMLIB(MGSLAP8F),DISP=SHR}, against a supplied
 * set of {@code .prm} members, and grafts the resolved member content into the LST.
 * <p>
 * Expansion is resolution-driven: a DD qualifies when (1) its {@code DSN}/{@code DSNAME}
 * references a specific {@code dataset(member)}, and (2) its {@code DISP} is an input
 * disposition ({@code SHR} or {@code OLD}). The supplied member set is the authority on what
 * is a control member — a qualifying DD is marked with {@link ParmMember}: when the member is
 * supplied its content is grafted as {@link Jcl.DataDefinitionStream} nodes
 * ({@link ParmMember.Status#EXPANDED}), otherwise it is marked {@link ParmMember.Status#MISSING}.
 * Output DDs and whole-data-set references are never touched.
 * <p>
 * The grafted stream nodes are tagged with {@link GeneratedParmContent} so the
 * {@code JclPrinter} skips them; the original DD statements are left untouched, so the source
 * round-trips byte-for-byte while the LST gains the expanded member content.
 */
public class ExpandExternalSysinVisitor<P> extends JclIsoVisitor<P> {

    private static final Pattern DSN_MEMBER = Pattern.compile("^(.+)\\(([^)]+)\\)$");

    /**
     * JCL operation keywords. Used to tell a continuation line ({@code //} followed by a
     * parameter) from a new unnamed statement ({@code //} followed by an operation).
     */
    private static final Set<String> OPERATIONS = new HashSet<>(Arrays.asList("JOB", "JCLLIB", "CNTL",
            "ENDCNTL", "DD", "EXEC", "EXPORT", "IF", "INCLUDE", "NOTIFY", "OUTPUT", "PEND", "PROC",
            "SCHEDULE", "SET", "XMIT"));

    private final Map<String, List<String>> parmMembers = new HashMap<>();

    public ExpandExternalSysinVisitor(Map<String, String> memberContents) {
        memberContents.forEach((memberName, content) ->
                parmMembers.putIfAbsent(memberName.toUpperCase(Locale.ROOT), tokenize(content)));
    }

    @Override
    public Jcl.CompilationUnit visitCompilationUnit(Jcl.CompilationUnit cu, P p) {
        List<Statement> in = cu.getStatements();
        List<Statement> out = new ArrayList<>(in.size());
        boolean changed = false;

        int i = 0;
        while (i < in.size()) {
            DdGroup group = matchDdGroup(in, i);
            if (group == null) {
                out.add(in.get(i));
                i++;
                continue;
            }

            ParmMember marker = group.dsnIndex < 0 ? null : evaluate(group.ddName, group.params);
            for (int k = group.start; k < group.end; k++) {
                Statement s = in.get(k);
                if (marker != null && k == group.dsnIndex) {
                    Jcl.JclStatement js = (Jcl.JclStatement) s;
                    s = js.withMarkers(js.getMarkers().addIfAbsent(marker));
                    changed = true;
                }
                out.add(s);
            }

            if (marker != null && marker.getStatus() == ParmMember.Status.EXPANDED) {
                String memberName = marker.getMemberName();
                List<Statement> grafted = buildStreamNodes(
                        parmMembers.get(memberName.toUpperCase(Locale.ROOT)), memberName);
                out.addAll(grafted);
                changed |= !grafted.isEmpty();
            }
            i = group.end;
        }

        return changed ? cu.withStatements(out) : cu;
    }

    /**
     * Matches a logical DD statement starting at {@code start}: a name/blank field
     * ({@code //NAME} or bare {@code //}) followed by the {@code DD} operation and its
     * parameter words, walking across {@code //}-continuation lines. Returns {@code null}
     * when no DD statement starts at {@code start}.
     */
    private @Nullable DdGroup matchDdGroup(List<Statement> statements, int start) {
        Statement head = statements.get(start);
        if (!(head instanceof Jcl.JclStatement)) {
            return null;
        }
        String headText = text(head);
        if (!headText.startsWith("//") || isComment(headText)) {
            return null;
        }
        if (start + 1 >= statements.size() || !isWord(statements.get(start + 1), "DD")) {
            return null;
        }

        String ddName = headText.length() > 2 ? headText.substring(2) : "";
        StringBuilder params = new StringBuilder();
        int dsnIndex = -1;
        // The operand field is a single blank-free token per line segment; the first word
        // after the operation (or after a //-continuation) is the operand, and any further
        // words on that line are the comment field, which is ignored.
        boolean expectOperand = true;
        int j = start + 2;
        for (; j < statements.size(); j++) {
            Statement s = statements.get(j);
            if (!(s instanceof Jcl.JclStatement)) {
                break;
            }
            String t = text(s);
            if (t.startsWith("//")) {
                boolean continuation = t.equals("//") &&
                        j + 1 < statements.size() && !isOperation(statements.get(j + 1));
                if (continuation) {
                    expectOperand = true; // operands resume after the continuation marker
                    continue;
                }
                break; // a new statement begins
            }
            if (expectOperand) {
                params.append(t);
                if (dsnIndex < 0 && isDsnAssignment(t)) {
                    dsnIndex = j;
                }
                expectOperand = false;
            }
        }
        return new DdGroup(start, j, dsnIndex, ddName, params.toString());
    }

    /**
     * Returns the marker for a qualifying input DD that references a specific member, or
     * {@code null} when the DD does not qualify for expansion.
     */
    private @Nullable ParmMember evaluate(String ddName, String params) {
        String dsn = null;
        String disp = null;
        for (String param : splitParams(params)) {
            String upper = param.toUpperCase(Locale.ROOT);
            if (dsn == null && (upper.startsWith("DSN=") || upper.startsWith("DSNAME="))) {
                dsn = param.substring(param.indexOf('=') + 1);
            } else if (disp == null && upper.startsWith("DISP=")) {
                disp = param.substring(param.indexOf('=') + 1);
            }
        }
        if (dsn == null || !isInputDisposition(disp)) {
            return null;
        }
        Matcher m = DSN_MEMBER.matcher(dsn);
        if (!m.matches()) {
            return null; // whole-data-set reference, no specific member to resolve
        }
        String dataSetName = m.group(1);
        String memberName = m.group(2);
        if (memberName.indexOf('&') >= 0 || memberName.indexOf('%') >= 0) {
            // Unresolved symbolic member reference; left for symbolic substitution, not expanded.
            return null;
        }
        ParmMember.Status status = parmMembers.containsKey(memberName.toUpperCase(Locale.ROOT)) ?
                ParmMember.Status.EXPANDED : ParmMember.Status.MISSING;
        return new ParmMember(randomId(), status, ddName, dataSetName, memberName);
    }

    /**
     * Instantiates fresh {@link Jcl.DataDefinitionStream} nodes from the member's pre-tokenized
     * words — one per whitespace-delimited word, matching how an inline {@code DD *} stream is
     * represented — each tagged with {@link GeneratedParmContent}. New tree/marker ids are minted
     * per graft so the same member can be expanded into multiple JCL sources without id collisions.
     * <p>
     * The grafted nodes are never printed (the {@code JclPrinter} skips {@link GeneratedParmContent}),
     * so their whitespace is irrelevant and simply {@link Space#EMPTY}.
     */
    private static List<Statement> buildStreamNodes(@Nullable List<String> words, String memberName) {
        if (words == null) {
            return Collections.emptyList();
        }
        List<Statement> nodes = new ArrayList<>(words.size());
        for (String word : words) {
            nodes.add(new Jcl.DataDefinitionStream(
                    randomId(),
                    Space.EMPTY,
                    Markers.EMPTY.addIfAbsent(new GeneratedParmContent(randomId(), memberName)),
                    new Jcl.Word(randomId(), Space.EMPTY, Markers.EMPTY, word)));
        }
        return nodes;
    }

    /**
     * Tokenizes raw member content into its whitespace-delimited words. The grafted content is not
     * printed, so only the words are kept — their surrounding whitespace is discarded.
     * <p>
     * Only columns 1–72 of each line are tokenized; the identification/sequence-number area
     * in columns 73–80 of fixed-form PDS members is ignored, consistent with how the JCL
     * line reader treats columns beyond 72.
     */
    private static List<String> tokenize(String content) {
        List<String> words = new ArrayList<>();
        for (String rawLine : content.split("\n", -1)) {
            String line = rawLine;
            if (line.endsWith("\r")) {
                line = line.substring(0, line.length() - 1);
            }
            if (line.length() > 72) {
                line = line.substring(0, 72);
            }
            int idx = 0;
            while (idx < line.length()) {
                while (idx < line.length() && Character.isWhitespace(line.charAt(idx))) {
                    idx++;
                }
                if (idx >= line.length()) {
                    break;
                }
                int textStart = idx;
                while (idx < line.length() && !Character.isWhitespace(line.charAt(idx))) {
                    idx++;
                }
                words.add(line.substring(textStart, idx));
            }
        }
        return words;
    }

    /**
     * Splits a JCL parameter string on commas at parenthesis depth zero, so that commas
     * inside sub-parameter lists such as {@code DISP=(NEW,CATLG,DELETE)} do not split.
     */
    private static List<String> splitParams(String params) {
        List<String> result = new ArrayList<>();
        int depth = 0;
        int start = 0;
        for (int i = 0; i < params.length(); i++) {
            char c = params.charAt(i);
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
            } else if (c == ',' && depth == 0) {
                result.add(params.substring(start, i));
                start = i + 1;
            }
        }
        result.add(params.substring(start));
        return result;
    }

    private static boolean isInputDisposition(@Nullable String disp) {
        if (disp == null) {
            return false;
        }
        // Reduce to the status (first positional) sub-parameter, e.g. DISP=(SHR,KEEP) -> SHR.
        String status = disp.trim();
        if (status.startsWith("(")) {
            status = status.substring(1);
        }
        int end = status.length();
        for (int i = 0; i < status.length(); i++) {
            char c = status.charAt(i);
            if (c == ',' || c == ')') {
                end = i;
                break;
            }
        }
        status = status.substring(0, end).trim().toUpperCase(Locale.ROOT);
        return "SHR".equals(status) || "OLD".equals(status);
    }

    private static boolean isDsnAssignment(String text) {
        String upper = text.toUpperCase(Locale.ROOT);
        return upper.startsWith("DSN=") || upper.startsWith("DSNAME=");
    }

    private static boolean isComment(String headText) {
        return headText.length() > 2 && headText.charAt(2) == '*';
    }

    private static boolean isOperation(Statement statement) {
        return statement instanceof Jcl.JclStatement &&
                OPERATIONS.contains(text(statement).toUpperCase(Locale.ROOT));
    }

    private static boolean isWord(Statement statement, String word) {
        return statement instanceof Jcl.JclStatement && text(statement).equalsIgnoreCase(word);
    }

    private static String text(Statement statement) {
        return ((Jcl.JclStatement) statement).getWord().getText();
    }

    /**
     * The span of a logical DD statement within the flat statement list.
     */
    private static final class DdGroup {
        final int start;
        final int end;
        final int dsnIndex;
        final String ddName;
        final String params;

        DdGroup(int start, int end, int dsnIndex, String ddName, String params) {
            this.start = start;
            this.end = end;
            this.dsnIndex = dsnIndex;
            this.ddName = ddName;
            this.params = params;
        }
    }
}
