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
package org.openrewrite.textmember.trait;

import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.textmember.tree.TextMember;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.util.ArrayList;
import java.util.List;

/**
 * A CLIST or a REXX exec, read for the members it reaches.
 * <p>
 * These are the operator's and the programmer's side of an application — the members that submit the
 * jobs, run the programs in the foreground and drive the dialogs — and nothing else in an estate says
 * how a job is started by hand. There is no grammar for either language here, so what is read is the
 * statements whose shape says what they reach, and only those: a {@code SUBMIT}, a {@code CALL}, a
 * {@code RUN} under DSN, a call of another script, an allocation, an edit and an existence check.
 * <p>
 * A name written as a variable is reported as it stands and marked {@link Reference#isSymbolic()}.
 * Nothing here resolves one: {@code CLMCOMP} picks a job into {@code &JOB} and hands it to
 * {@code CLMSUB}, which submits it, so the job a script really submits is a fact about two members and
 * a parameter and not one this can read off a statement.
 */
@Value
public class Script implements Trait<TextMember.CompilationUnit> {

    Cursor cursor;

    /**
     * The members the script reaches, in the order it names them.
     */
    public List<Reference> getReferences() {
        List<Reference> references = new ArrayList<>();
        List<TextMember.Line> lines = getTree().getLines();
        boolean rexx = getTree().getKind() == TextMember.Kind.REXX;
        for (int i = 0; i < lines.size(); i++) {
            String text = lines.get(i).getText();
            if (text.trim().startsWith("/*")) {
                continue;
            }
            read(new Card(text, rexx), i + 1, references);
        }
        return references;
    }

    /**
     * Every name the script writes, which is what a search for a member name finds in it.
     */
    public List<Mention> getMentions() {
        return Mention.in(getTree());
    }

    /**
     * What one card reaches. The verb decides: a name in the middle of a message is not a reference,
     * and every script writes {@code WRITE ...: SUBMIT &JOB (Y/N)?} somewhere.
     */
    private static void read(Card card, int line, List<Reference> references) {
        // A function, not a statement, so it is looked for wherever it was written.
        for (int at = card.find("SYSDSN", 0); at >= 0; at = card.find("SYSDSN", at + 1)) {
            add(references, Reference.Kind.CHECK, card, card.parenthesised(at + "SYSDSN".length()), null, line);
        }

        switch (card.getVerb()) {
            case "SUBMIT":
                add(references, Reference.Kind.SUBMIT, card, card.operandAt(card.getVerbEnd()), null, line);
                break;
            case "CALL":
                add(references, Reference.Kind.CALL, card, card.operandAt(card.getVerbEnd()), null, line);
                break;
            case "RUN":
                add(references, Reference.Kind.RUN, card, card.keywordOperand("PROGRAM"), null, line);
                break;
            case "EXEC":
                add(references, Reference.Kind.EXEC, card, card.operandAt(card.getVerbEnd()), null, line);
                break;
            case "ALLOC":
            case "ALLOCATE":
            case "ALTLIB":
            case "LMINIT":
                add(references, Reference.Kind.ALLOCATE, card,
                        card.keywordOperand("DA", "DSN", "DSNAME", "DATASET"), card.ddName(), line);
                break;
            case "EDIT":
            case "VIEW":
            case "BROWSE":
                Operand edited = card.keywordOperand("DA", "DSN", "DSNAME", "DATASET");
                add(references, Reference.Kind.EDIT, card,
                        edited == null ? card.quotedAt(card.getVerbEnd()) : edited, null, line);
                break;
            case "SELECT":
                add(references, Reference.Kind.SELECT, card, card.keywordOperand("PGM", "CMD"), null, line);
                break;
            case "LISTDS":
                add(references, Reference.Kind.CHECK, card, card.operandAt(card.getVerbEnd()), null, line);
                break;
            default:
                // The name is the statement itself, so there is nothing here a script could have
                // computed and no data set to take it out of.
                if (card.getVerb().startsWith("%")) {
                    String called = card.getVerbText().substring(1);
                    references.add(new Reference(Reference.Kind.EXEC, null, called, null, called,
                            false, line));
                }
                break;
        }
    }

    private static void add(List<Reference> references, Reference.Kind kind, Card card,
                            @Nullable Operand operand, @Nullable String ddName, int line) {
        String text = operand == null ? "" : operand.text;
        String member = null;
        String dataSet = null;
        // A member is written in parentheses after the data set that holds it; a name with a qualifier
        // and no parentheses is the data set itself; a bare word is a member of a library the statement
        // does not name.
        int open = text.indexOf('(');
        if (open > 0 && text.endsWith(")")) {
            member = text.substring(open + 1, text.length() - 1);
            dataSet = text.substring(0, open);
        } else if (text.indexOf('.') > 0) {
            dataSet = text;
        } else if (!text.isEmpty() && !"*".equals(text)) {
            member = text;
        }

        String name = member != null ? member : dataSet != null ? lastQualifier(dataSet) : ddName;
        if (name == null || name.isEmpty()) {
            return;
        }
        boolean symbolic = name.indexOf('&') >= 0 ||
                           operand != null && text.contains(name) &&
                           card.isVariable(operand.start + text.lastIndexOf(name), name.length());
        references.add(new Reference(kind, dataSet, member, ddName, name, symbolic, line));
    }

    private static String lastQualifier(String dataSetName) {
        int dot = dataSetName.lastIndexOf('.');
        return dot < 0 ? dataSetName : dataSetName.substring(dot + 1);
    }

    /**
     * A member a script reaches, and the statement that reaches it.
     */
    @Value
    public static class Reference {
        Kind kind;

        /**
         * The data set as written, or null where the statement named a member without one.
         */
        @Nullable
        String dataSet;

        /**
         * The member in parentheses, or null where the statement named a data set and no member.
         */
        @Nullable
        String member;

        /**
         * The DD an {@code ALLOC} bound the data set to, which is the name the program that reads it
         * knows the file by.
         */
        @Nullable
        String ddName;

        /**
         * What the statement reaches: the member, or the last qualifier of the data set, or the DD.
         */
        String name;

        /**
         * Whether the name is a variable rather than something written down. A CLIST writes one with a
         * leading {@code &}; a REXX exec writes one outside the quotes.
         */
        boolean symbolic;

        int line;

        public enum Kind {
            /**
             * {@code SUBMIT}, which puts a member of a JCL library on the internal reader. A submit of
             * {@code *} is left out: the job went to the reader off the stack, so there is no member.
             */
            SUBMIT,
            /**
             * {@code CALL}, which loads a module and gives it control.
             */
            CALL,
            /**
             * {@code RUN PROGRAM} under DSN, which is how a DB2 program is run in the foreground.
             */
            RUN,
            /**
             * Another script: {@code %name}, or an {@code EXEC} of a member.
             */
            EXEC,
            /**
             * {@code ALLOC}, {@code ALTLIB} or {@code LMINIT}: a data set the session will read.
             */
            ALLOCATE,
            /**
             * {@code EDIT}, {@code VIEW} or {@code BROWSE}: a data set the script opens for somebody
             * to read or change.
             */
            EDIT,
            /**
             * {@code ISPEXEC SELECT}, which runs a program or a command under the dialog manager.
             */
            SELECT,
            /**
             * {@code &SYSDSN} or {@code LISTDS}: a data set named to ask whether it is there.
             */
            CHECK
        }
    }

    public static class Matcher extends SimpleTraitMatcher<Script> {

        @Override
        protected @Nullable Script test(Cursor cursor) {
            if (!(cursor.getValue() instanceof TextMember.CompilationUnit)) {
                return null;
            }
            TextMember.Kind kind = ((TextMember.CompilationUnit) cursor.getValue()).getKind();
            return kind == TextMember.Kind.CLIST || kind == TextMember.Kind.REXX ? new Script(cursor) : null;
        }
    }

    /**
     * One line of a script, ready to be read as a statement.
     * <p>
     * REXX builds a command out of strings and variables written side by side, so the double quotes come
     * off and what was between them joins up: {@code "SUBMIT '"HLQ".JCL("JOB")'"} is the one command
     * {@code SUBMIT 'HLQ.JCL(JOB)'}. What was written outside the quotes is what the exec computed, and
     * that is remembered, since it is the difference between the job {@code CLMJ010} and the job named
     * by the argument.
     */
    private static final class Card {
        private final String text;
        private final String upper;
        private final boolean[] variable;
        private final String verb;
        private final int verbAt;
        private final int verbEnd;

        Card(String line, boolean rexx) {
            StringBuilder built = new StringBuilder(line.length());
            boolean[] computed = new boolean[line.length()];
            if (rexx) {
                boolean quoted = false;
                boolean doubled = false;
                for (int i = 0; i < line.length(); i++) {
                    char c = line.charAt(i);
                    if (!quoted && c == '"') {
                        doubled = !doubled;
                        continue;
                    }
                    if (!doubled && c == '\'') {
                        quoted = !quoted;
                    }
                    computed[built.length()] = !quoted && !doubled;
                    built.append(c);
                }
            } else {
                boolean symbol = false;
                for (int i = 0; i < line.length(); i++) {
                    char c = line.charAt(i);
                    symbol = c == '&' || symbol && isNameCharacter(c);
                    computed[i] = symbol;
                    built.append(c);
                }
            }
            this.text = built.toString();
            // Folded character by character, since the two are read by the same index.
            StringBuilder folded = new StringBuilder(text.length());
            for (int i = 0; i < text.length(); i++) {
                folded.append(Character.toUpperCase(text.charAt(i)));
            }
            this.upper = folded.toString();
            this.variable = computed;

            int at = verbAt(upper);
            int end = at;
            while (end < text.length() && !Character.isWhitespace(text.charAt(end))) {
                end++;
            }
            this.verb = upper.substring(at, end);
            this.verbAt = at;
            this.verbEnd = end;
        }

        /**
         * Where the statement itself begins: past the conditions, the {@code THEN} and the subcommand
         * environment a command was addressed to.
         */
        private static int verbAt(String upper) {
            int at = upper.lastIndexOf(" THEN ");
            at = at < 0 ? 0 : at + " THEN ".length();
            while (true) {
                while (at < upper.length() && Character.isWhitespace(upper.charAt(at))) {
                    at++;
                }
                int end = at;
                while (end < upper.length() && !Character.isWhitespace(upper.charAt(end))) {
                    end++;
                }
                String word = upper.substring(at, end);
                if ("ELSE".equals(word) || "OTHERWISE".equals(word) || "DO".equals(word) ||
                    "ISPEXEC".equals(word) || "ADDRESS".equals(word)) {
                    at = end;
                    continue;
                }
                if ("WHEN".equals(word)) {
                    at = end;
                    while (at < upper.length() && Character.isWhitespace(upper.charAt(at))) {
                        at++;
                    }
                    if (at < upper.length() && upper.charAt(at) == '(') {
                        int close = close(upper, at);
                        if (close < 0) {
                            return at;
                        }
                        at = close + 1;
                    }
                    continue;
                }
                return at;
            }
        }

        String getVerb() {
            return verb;
        }

        String getVerbText() {
            return text.substring(verbAt, verbEnd);
        }

        int getVerbEnd() {
            return verbEnd;
        }

        /**
         * Where {@code word} is written as a word of its own, at or after {@code from}.
         */
        int find(String word, int from) {
            for (int at = upper.indexOf(word, from); at >= 0; at = upper.indexOf(word, at + 1)) {
                int after = at + word.length();
                if ((at == 0 || !isNameCharacter(upper.charAt(at - 1))) &&
                    (after == upper.length() || !isNameCharacter(upper.charAt(after)))) {
                    return at;
                }
            }
            return -1;
        }

        /**
         * The operand at or after {@code from}: a quoted data set name, or a word with its parentheses.
         */
        @Nullable
        Operand operandAt(int from) {
            int at = from;
            while (at < text.length() && Character.isWhitespace(text.charAt(at))) {
                at++;
            }
            if (at >= text.length()) {
                return null;
            }
            if (text.charAt(at) == '\'') {
                int close = text.indexOf('\'', at + 1);
                return close < 0 ? null : new Operand(text.substring(at + 1, close), at + 1);
            }
            int end = at;
            int depth = 0;
            while (end < text.length() && (depth > 0 || !Character.isWhitespace(text.charAt(end)))) {
                char c = text.charAt(end);
                depth += c == '(' ? 1 : c == ')' ? -1 : 0;
                end++;
            }
            return new Operand(text.substring(at, end), at);
        }

        /**
         * The operand at or after {@code from} only where it is quoted, which is how a command names a
         * data set among words that are not one.
         */
        @Nullable
        Operand quotedAt(int from) {
            Operand operand = operandAt(from);
            return operand != null && operand.start > 0 && text.charAt(operand.start - 1) == '\'' ?
                    operand : null;
        }

        /**
         * What one of {@code keywords} was given, as {@code KEYWORD(value)}.
         */
        @Nullable
        Operand keywordOperand(String... keywords) {
            for (String keyword : keywords) {
                int at = find(keyword, verbEnd);
                if (at >= 0 && at + keyword.length() < text.length() &&
                    text.charAt(at + keyword.length()) == '(') {
                    Operand operand = parenthesised(at + keyword.length());
                    if (operand != null) {
                        return operand;
                    }
                }
            }
            return null;
        }

        /**
         * The contents of the parenthesised group at {@code from}, with any quotes taken off.
         */
        @Nullable
        Operand parenthesised(int from) {
            if (from >= text.length() || text.charAt(from) != '(') {
                return null;
            }
            int close = close(text, from);
            if (close < 0) {
                return null;
            }
            int start = from + 1;
            int end = close;
            if (end > start && text.charAt(start) == '\'' && text.charAt(end - 1) == '\'') {
                start++;
                end--;
            }
            return new Operand(text.substring(start, end), start);
        }

        /**
         * The DD an allocation binds the data set to.
         */
        @Nullable
        String ddName() {
            Operand operand = keywordOperand("F", "FI", "FILE", "DDNAME", "DD");
            return operand == null ? null : operand.text.trim();
        }

        boolean isVariable(int start, int length) {
            for (int i = start; i < start + length && i < variable.length; i++) {
                if (variable[i]) {
                    return true;
                }
            }
            return false;
        }

        private static int close(String text, int open) {
            int depth = 0;
            for (int i = open; i < text.length(); i++) {
                char c = text.charAt(i);
                depth += c == '(' ? 1 : c == ')' ? -1 : 0;
                if (depth == 0) {
                    return i;
                }
            }
            return -1;
        }

        private static boolean isNameCharacter(char c) {
            return Character.isLetterOrDigit(c) || c == '@' || c == '#' || c == '$';
        }
    }

    /**
     * What a statement was given, and where in the card it was written.
     */
    private static final class Operand {
        private final String text;
        private final int start;

        Operand(String text, int start) {
            this.text = text;
            this.start = start;
        }
    }

    @Override
    public String toString() {
        return getTree().getKind() + " " + getTree().getSourcePath();
    }
}
