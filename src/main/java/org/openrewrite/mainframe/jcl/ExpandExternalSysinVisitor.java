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
package org.openrewrite.mainframe.jcl;

import org.jspecify.annotations.Nullable;
import org.openrewrite.mainframe.jcl.marker.GeneratedParmContent;
import org.openrewrite.mainframe.jcl.marker.ParmMember;
import org.openrewrite.mainframe.jcl.tree.Jcl;
import org.openrewrite.mainframe.jcl.tree.Space;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.mainframe.jcl.tree.Statement;
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
        List<Statement> out = new ArrayList<>(cu.getStatements().size());
        boolean changed = false;

        for (Statement statement : cu.getStatements()) {
            if (!(statement instanceof Jcl.JobControlStatement) || !((Jcl.JobControlStatement) statement).isOperation("DD")) {
                out.add(statement);
                continue;
            }
            Jcl.JobControlStatement dd = (Jcl.JobControlStatement) statement;
            Jcl.KeywordParameter dsn = dd.getParameter("DSN");
            if (dsn == null) {
                dsn = dd.getParameter("DSNAME");
            }
            ParmMember marker = dsn == null ? null : evaluate(dd, dsn);
            if (marker == null) {
                out.add(dd);
                continue;
            }

            Jcl.KeywordParameter marked = dsn;
            out.add(dd.withOperands(ListUtils.map(dd.getOperands(),
                    o -> o == marked ? marked.withMarkers(marked.getMarkers().addIfAbsent(marker)) : o)));
            changed = true;

            if (marker.getStatus() == ParmMember.Status.EXPANDED) {
                String memberName = marker.getMemberName();
                out.addAll(buildStreamNodes(parmMembers.get(memberName.toUpperCase(Locale.ROOT)), memberName));
            }
        }

        return changed ? cu.withStatements(out) : cu;
    }

    /**
     * Returns the marker for a qualifying input DD that references a specific member, or
     * {@code null} when the DD does not qualify for expansion.
     */
    private @Nullable ParmMember evaluate(Jcl.JobControlStatement dd, Jcl.KeywordParameter dsnParameter) {
        // The value keeps its leading = so that printing puts it back; the data set name is what
        // follows it.
        String dsn = dsnParameter.getValueText();
        Jcl.KeywordParameter dispParameter = dd.getParameter("DISP");
        String disp = dispParameter == null ? null : dispParameter.getValueText();
        String ddName = dd.getSimpleName();
        if (!isInputDisposition(disp)) {
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

}
