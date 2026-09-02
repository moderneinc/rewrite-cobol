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
import org.openrewrite.mainframe.jcl.marker.ResolvedText;
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
        List<Statement> expanded = expand(cu.getStatements());
        return expanded == cu.getStatements() ? cu : cu.withStatements(expanded);
    }

    /**
     * One run of cards, each qualifying DD marked and its member's content grafted in after it. A
     * procedure's own cards are a run of their own: the DD only exists once
     * {@link ExpandJobVisitor} has expanded the job, and the member it names only once that has
     * filled in its symbols, which is why this runs after it and walks what it produced.
     */
    private List<Statement> expand(List<Statement> statements) {
        List<Statement> out = new ArrayList<>(statements.size());
        boolean changed = false;

        for (Statement statement : statements) {
            if (statement instanceof Jcl.Expansion) {
                Jcl.Expansion expansion = (Jcl.Expansion) statement;
                List<Statement> body = expand(expansion.getStatements());
                changed |= body != expansion.getStatements();
                out.add(body == expansion.getStatements() ? expansion : expansion.withStatements(body));
                continue;
            }
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

        return changed ? out : statements;
    }

    /**
     * Returns the marker for a qualifying input DD that references a specific member, or
     * {@code null} when the DD does not qualify for expansion.
     */
    private @Nullable ParmMember evaluate(Jcl.JobControlStatement dd, Jcl.KeywordParameter dsnParameter) {
        String dsn = resolved(dsnParameter);
        Jcl.KeywordParameter dispParameter = dd.getParameter("DISP");
        String disp = dispParameter == null ? null : resolved(dispParameter);
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
     * What a parameter says once its symbols are filled in, which is what it says at all when the
     * job runs — {@code DSN=&CTLLIB(&MEM)} names no member until then.
     */
    private static String resolved(Jcl.KeywordParameter parameter) {
        return parameter.getMarkers().findFirst(ResolvedText.class)
                .map(ResolvedText::getText)
                .orElseGet(parameter::getValueText);
    }

    /**
     * Instantiates fresh {@link Jcl.DataDefinitionStream} nodes from the member's pre-tokenized
     * words — one per whitespace-delimited word, matching how an inline {@code DD *} stream is
     * represented — each tagged with {@link GeneratedParmContent}. New tree/marker ids are minted
     * per graft so the same member can be expanded into multiple JCL sources without id collisions.
     * <p>
     * The grafted nodes are never printed (the {@code JclPrinter} skips {@link GeneratedParmContent}),
     * but the white space in front of each word is kept: a control card says something different in
     * another column, so a reader of the deck needs the member's lines and not only its words.
     */
    private static List<Statement> buildStreamNodes(@Nullable List<String> words, String memberName) {
        if (words == null) {
            return Collections.emptyList();
        }
        List<Statement> nodes = new ArrayList<>(words.size());
        for (String word : words) {
            int text = 0;
            while (text < word.length() && Character.isWhitespace(word.charAt(text))) {
                text++;
            }
            nodes.add(new Jcl.DataDefinitionStream(
                    randomId(),
                    Space.build(word.substring(0, text)),
                    Markers.EMPTY.addIfAbsent(new GeneratedParmContent(randomId(), memberName)),
                    new Jcl.Word(randomId(), Space.EMPTY, Markers.EMPTY, word.substring(text))));
        }
        return nodes;
    }

    /**
     * Tokenizes raw member content into its whitespace-delimited words, each carrying the white
     * space written in front of it — the blanks of its own line, and the line endings of any blank
     * line before it. Blanks written after the last word of a line are dropped, since no card means
     * anything by them.
     * <p>
     * Only columns 1–72 of each line are tokenized; the identification/sequence-number area
     * in columns 73–80 of fixed-form PDS members is ignored, consistent with how the JCL
     * line reader treats columns beyond 72.
     */
    private static List<String> tokenize(String content) {
        List<String> words = new ArrayList<>();
        StringBuilder prefix = new StringBuilder();
        boolean firstLine = true;
        for (String rawLine : content.split("\n", -1)) {
            String line = rawLine;
            if (line.endsWith("\r")) {
                line = line.substring(0, line.length() - 1);
            }
            if (line.length() > 72) {
                line = line.substring(0, 72);
            }
            if (!firstLine) {
                prefix.append('\n');
            }
            firstLine = false;
            int idx = 0;
            while (idx < line.length()) {
                int blanks = idx;
                while (idx < line.length() && Character.isWhitespace(line.charAt(idx))) {
                    idx++;
                }
                if (idx >= line.length()) {
                    break;
                }
                prefix.append(line, blanks, idx);
                int textStart = idx;
                while (idx < line.length() && !Character.isWhitespace(line.charAt(idx))) {
                    idx++;
                }
                words.add(prefix + line.substring(textStart, idx));
                prefix.setLength(0);
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
