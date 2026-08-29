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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.mainframe.cobol.tree.Cobol;
import org.openrewrite.mainframe.cobol.tree.CobolPreprocessor;
import org.openrewrite.mainframe.cobol.tree.CommentArea;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.*;

/**
 * A single {@code EXEC CICS} command, decomposed into its verb, qualifier and keyword options.
 * <p>
 * Preprocessing takes an EXEC block out of the text the COBOL grammar is handed, so the LST holds it
 * as an opaque run of words hanging off the word that stands in for it. This gives that run a shape,
 * so an analysis can ask "which programs does this LINK to" rather than pattern match text.
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CicsCommand implements Trait<Cobol.Word> {

    /**
     * The stand-in word the block hangs off. That word is what a search result marks — the block
     * itself prints from the preprocessor statement, so a marker on it would not show.
     */
    Cursor cursor;

    /**
     * The block this command was decomposed from.
     */
    CobolPreprocessor.ExecStatement exec;

    /**
     * The command verb, upper cased. For example {@code READ}, {@code LINK}, {@code SEND}.
     */
    String verb;

    /**
     * The qualifier following the verb for commands whose meaning depends on it, upper cased:
     * {@code TS}/{@code TD} for queue commands, {@code MAP}/{@code TEXT} for BMS,
     * {@code CONDITION}/{@code ABEND}/{@code AID} for HANDLE. Null when the verb stands alone.
     */
    @Nullable
    String qualifier;

    /**
     * Options in source order, keyed by upper cased option name. The value is the operand exactly as
     * written, so {@code FILE('ACCTFILE')} yields {@code 'ACCTFILE'} and {@code FILE(WS-FILE)}
     * yields {@code WS-FILE}. Options with no operand map to an empty string.
     */
    Map<String, String> options;

    public boolean isVerb(String candidate) {
        return verb.equalsIgnoreCase(candidate);
    }

    public @Nullable String getOption(String name) {
        return options.get(name.toUpperCase(Locale.ROOT));
    }

    public boolean hasOption(String name) {
        return options.containsKey(name.toUpperCase(Locale.ROOT));
    }

    /**
     * The operand of the first of {@code names} that is present, with literal quotes stripped.
     * Null when none are present.
     */
    public @Nullable String operand(String... names) {
        for (String name : names) {
            String value = getOption(name);
            if (value != null) {
                return Literals.unquote(value);
            }
        }
        return null;
    }

    /**
     * The command as it would be written, without operands. {@code SEND MAP}, {@code READQ TS}.
     */
    public String getCommand() {
        return qualifier == null ? verb : verb + " " + qualifier;
    }

    /**
     * The options as they were written, for reporting.
     */
    public String getOptionsText() {
        StringBuilder rendered = new StringBuilder();
        for (Map.Entry<String, String> option : options.entrySet()) {
            if (rendered.length() > 0) {
                rendered.append(' ');
            }
            rendered.append(option.getKey());
            if (!option.getValue().isEmpty()) {
                rendered.append('(').append(option.getValue()).append(')');
            }
        }
        return rendered.toString();
    }

    /**
     * What this command touches, and how. Commands that reach no named resource return nothing.
     */
    public List<CicsResourceAccess> getResources() {
        switch (verb) {
            case "READ":
                // READ ... UPDATE reserves the record for a following REWRITE or DELETE.
                return file(hasOption("UPDATE") ? CicsResourceAccess.Access.UPDATE : CicsResourceAccess.Access.READ);
            case "WRITE":
                return file(CicsResourceAccess.Access.CREATE);
            case "REWRITE":
                return file(CicsResourceAccess.Access.UPDATE);
            case "DELETE":
                return file(CicsResourceAccess.Access.DELETE);
            case "STARTBR":
            case "READNEXT":
            case "READPREV":
            case "ENDBR":
            case "RESETBR":
                return file(CicsResourceAccess.Access.BROWSE);
            case "UNLOCK":
                return file(CicsResourceAccess.Access.UPDATE);
            case "READQ":
                return queue(CicsResourceAccess.Access.READ);
            case "WRITEQ":
                return queue(hasOption("REWRITE") ? CicsResourceAccess.Access.UPDATE : CicsResourceAccess.Access.CREATE);
            case "DELETEQ":
                return queue(CicsResourceAccess.Access.DELETE);
            case "LINK":
                return single(CicsResourceAccess.Kind.PROGRAM, CicsResourceAccess.Access.LINK, "PROGRAM");
            case "XCTL":
                return single(CicsResourceAccess.Kind.PROGRAM, CicsResourceAccess.Access.XCTL, "PROGRAM");
            case "LOAD":
                return single(CicsResourceAccess.Kind.PROGRAM, CicsResourceAccess.Access.LOAD, "PROGRAM");
            case "START":
                return single(CicsResourceAccess.Kind.TRANSACTION, CicsResourceAccess.Access.START, "TRANSID");
            case "CANCEL":
                return single(CicsResourceAccess.Kind.TRANSACTION, CicsResourceAccess.Access.CANCEL, "TRANSID");
            case "RETURN":
                return single(CicsResourceAccess.Kind.TRANSACTION, CicsResourceAccess.Access.RETURN, "TRANSID");
            case "SEND":
                return bms(CicsResourceAccess.Access.SEND);
            case "RECEIVE":
                return bms(CicsResourceAccess.Access.RECEIVE);
            default:
                return emptyList();
        }
    }

    private List<CicsResourceAccess> file(CicsResourceAccess.Access access) {
        return single(CicsResourceAccess.Kind.FILE, access, "FILE", "DATASET");
    }

    private List<CicsResourceAccess> queue(CicsResourceAccess.Access access) {
        // An unqualified queue command defaults to temporary storage.
        CicsResourceAccess.Kind kind = "TD".equals(qualifier) ?
                CicsResourceAccess.Kind.TD_QUEUE : CicsResourceAccess.Kind.TS_QUEUE;
        return single(kind, access, "QUEUE", "QNAME");
    }

    private List<CicsResourceAccess> bms(CicsResourceAccess.Access access) {
        if (!"MAP".equals(qualifier)) {
            return emptyList();
        }
        List<CicsResourceAccess> accesses = new ArrayList<>(2);
        accesses.addAll(single(CicsResourceAccess.Kind.MAP, access, "MAP"));
        accesses.addAll(single(CicsResourceAccess.Kind.MAPSET, access, "MAPSET"));
        return accesses;
    }

    private List<CicsResourceAccess> single(CicsResourceAccess.Kind kind, CicsResourceAccess.Access access,
                                            String... optionNames) {
        for (String option : optionNames) {
            String raw = getOption(option);
            if (raw != null && !raw.isEmpty()) {
                return singletonList(new CicsResourceAccess(kind, Literals.unquote(raw), access,
                        !Literals.isLiteral(raw)));
            }
        }
        return emptyList();
    }

    /**
     * Verbs whose meaning is determined by the word that follows them, mapped to the qualifiers that
     * word is allowed to take. Anything else following the verb is parsed as an option.
     */
    private static final Map<String, Set<String>> QUALIFIERS = qualifiers();

    private static Map<String, Set<String>> qualifiers() {
        Map<String, Set<String>> m = new HashMap<>();
        Set<String> tsTd = new HashSet<>(Arrays.asList("TS", "TD"));
        m.put("READQ", tsTd);
        m.put("WRITEQ", tsTd);
        m.put("DELETEQ", tsTd);
        m.put("SEND", new HashSet<>(Arrays.asList("MAP", "TEXT", "CONTROL", "PAGE")));
        m.put("RECEIVE", new HashSet<>(Arrays.asList("MAP", "PARTN")));
        m.put("HANDLE", new HashSet<>(Arrays.asList("CONDITION", "ABEND", "AID")));
        m.put("IGNORE", singleton("CONDITION"));
        m.put("PUSH", singleton("HANDLE"));
        m.put("POP", singleton("HANDLE"));
        m.put("WAIT", new HashSet<>(Arrays.asList("EVENT", "TERMINAL", "JOURNALNAME", "EXTERNAL")));
        return m;
    }

    /**
     * Whether {@code exec} is an {@code EXEC CICS} block, as opposed to SQL, SQLIMS or DLI.
     */
    public static boolean isCics(CobolPreprocessor.ExecStatement exec) {
        List<CobolPreprocessor.Word> words = exec.getWords();
        return words.size() >= 2 && "CICS".equalsIgnoreCase(words.get(1).getCobolWord().getWord());
    }

    public static class Matcher extends SimpleTraitMatcher<CicsCommand> {

        /**
         * One command per block, matched on the word that stands in for it. Every word within the
         * block answers with the block too — see {@link Execs#isStandIn} — so matching on any word
         * that carries one reports a single command a dozen times over.
         */
        @Override
        protected @Nullable CicsCommand test(Cursor cursor) {
            Object value = cursor.getValue();
            if (!(value instanceof Cobol.Word)) {
                return null;
            }
            CobolPreprocessor.ExecStatement exec = Execs.blockOn((Cobol.Word) value);
            return exec != null && isCics(exec) && Execs.isStandIn(cursor, (Cobol.Word) value) ?
                    parse(cursor, exec) : null;
        }
    }

    private static CicsCommand parse(Cursor cursor, CobolPreprocessor.ExecStatement exec) {
        List<Cobol.Word> words = Execs.wordsOf(exec);
        List<String> tokens = textOf(words);
        if (tokens.isEmpty()) {
            return new CicsCommand(cursor, exec, "", null, emptyMap());
        }

        String verb = tokens.get(0).toUpperCase(Locale.ROOT);
        int i = 1;

        String qualifier = null;
        Set<String> allowed = QUALIFIERS.get(verb);
        if (allowed != null && i < tokens.size()) {
            String candidate = tokens.get(i).toUpperCase(Locale.ROOT);
            if (allowed.contains(candidate)) {
                qualifier = candidate;
                // SEND MAP('ACCTM') writes the qualifier and an option of the same name once. Left
                // consumed, the map name went with it and no BMS map was ever reported.
                if (i + 1 >= tokens.size() || !"(".equals(tokens.get(i + 1))) {
                    i++;
                }
            }
        }

        Map<String, String> options = new LinkedHashMap<>();
        while (i < tokens.size()) {
            String name = tokens.get(i++);
            if ("(".equals(name) || ")".equals(name)) {
                continue;
            }
            if (i < tokens.size() && "(".equals(tokens.get(i))) {
                StringBuilder operand = new StringBuilder();
                int depth = 0;
                do {
                    int at = i++;
                    String token = tokens.get(at);
                    if ("(".equals(token)) {
                        depth++;
                        if (depth == 1) {
                            continue;
                        }
                    } else if (")".equals(token)) {
                        depth--;
                        if (depth == 0) {
                            break;
                        }
                    }
                    if (operand.length() > 0) {
                        operand.append(separator(words, at));
                    }
                    operand.append(token);
                } while (i < tokens.size());
                options.put(name.toUpperCase(Locale.ROOT), operand.toString());
            } else {
                options.put(name.toUpperCase(Locale.ROOT), "");
            }
        }
        return new CicsCommand(cursor, exec, verb, qualifier, options);
    }

    /**
     * What was written between this word and the one before it. Neither a blank nor a comma is a
     * word — both are kept in the whitespace around one — so an operand read off the words alone
     * has {@code APPLID(APPLIDO OF COSGN0AO)} arriving as {@code APPLIDOOFCOSGN0AO}, and a field a
     * program names only inside an {@code EXEC CICS} then belongs to no data item at all.
     * <p>
     * A separator is kept as it was written and everything else becomes one blank, so an operand
     * broken over two cards reads as one line. What follows a word up to column 73 separates it from
     * the next just as a blank before that one would, so both are read.
     */
    private static String separator(List<Cobol.Word> words, int at) {
        CommentArea trailing = words.get(at - 1).getCommentArea();
        String between = (trailing == null ? "" : trailing.getPrefix().getWhitespace()) +
                         words.get(at).getPrefix().getWhitespace();
        if (between.isEmpty()) {
            return "";
        }
        StringBuilder separator = new StringBuilder();
        for (int i = 0; i < between.length(); i++) {
            if (!Character.isWhitespace(between.charAt(i))) {
                separator.append(between.charAt(i));
            }
        }
        return separator.append(' ').toString();
    }

    /**
     * The words of an {@code EXEC} block body, flattened across continuation lines.
     */
    static List<String> tokens(CobolPreprocessor.ExecStatement exec) {
        return textOf(Execs.wordsOf(exec));
    }

    private static List<String> textOf(List<Cobol.Word> words) {
        List<String> tokens = new ArrayList<>(words.size());
        for (Cobol.Word word : words) {
            tokens.add(word.getWord());
        }
        return tokens;
    }

    @Override
    public String toString() {
        String optionsText = getOptionsText();
        return "EXEC CICS " + getCommand() + (optionsText.isEmpty() ? "" : " " + optionsText);
    }
}
