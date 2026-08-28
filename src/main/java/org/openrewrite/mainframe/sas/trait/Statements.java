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
package org.openrewrite.mainframe.sas.trait;

import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.mainframe.Members;
import org.openrewrite.text.PlainText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.util.Collections.emptyList;

/**
 * The statements of a SAS program, found in the text.
 * <p>
 * There is no grammar because there is almost no syntax to have one for: a statement runs to the
 * first semicolon written outside a quoted string or a comment, and SAS has no reserved words at all,
 * so what a statement is cannot be decided until its first word has been read. Everything that is not
 * a statement boundary stays a word, which is the depth a text search and a name reference need.
 * <p>
 * The three things that do have to be read are the ones that move a boundary: a
 * {@code /*...*}{@code /} comment, which may stand anywhere a blank may; a statement beginning
 * {@code *} or {@code %*}, which is a comment running to its own semicolon; and a quoted string,
 * where a semicolon means nothing. Data written after a {@code DATALINES} statement is read as words
 * like anything else — it is not source, but it is text somebody searches.
 * <p>
 * A member is a flat run of statements and means something structured: the statements between a
 * {@code PROC} or {@code DATA} and the {@code RUN;} or {@code QUIT;} under it are one step, and what
 * a statement means depends on which step it is in — {@code FILE} in a DATA step writes an external
 * file and {@code LIBRARY=} in {@code PROC FORMAT} names a libref. Nothing but position says so, so
 * the statements stay flat and the step is read from the ones before.
 */
public final class Statements {

    private final String text;

    private final List<Statement> statements = new ArrayList<>();

    /**
     * The words of the statement being read, or null between statements — which is also what says
     * that a {@code *} here opens a comment rather than multiplying.
     */
    private @Nullable List<Word> open;

    /**
     * How far newlines have been counted, and the line that offset falls on. Words are read in source
     * order, so counting forward is enough.
     */
    private int counted;
    private int line = 1;

    private Statements(String text) {
        this.text = text;
        read();
    }

    /**
     * Every statement of a program, in source order. A comment is not one: it carries nothing a
     * reference is read from, and where it stands has already moved the boundaries around it.
     */
    public static List<Statement> in(String text) {
        return new Statements(text).statements;
    }

    /**
     * Whether the cursor is on a SAS program, which is what every trait here is read from.
     */
    static boolean isProgram(Cursor cursor) {
        return cursor.getValue() instanceof PlainText &&
               Members.kindOf((PlainText) cursor.getValue()) == Members.Kind.SAS;
    }

    /**
     * The {@code PROC} or {@code DATA} statement that opened the step statement {@code index} is in,
     * or null where it is between steps. A {@code RUN;} or {@code QUIT;} ends a step, and so does the
     * next step opener: SAS runs the one before without being told to.
     */
    static @Nullable Statement stepOf(List<Statement> statements, int index) {
        for (int i = index; i >= 0; i--) {
            Statement statement = statements.get(i);
            if (statement.isKeyword("PROC") || statement.isKeyword("DATA")) {
                return statement;
            }
            if (statement.isKeyword("RUN") || statement.isKeyword("QUIT")) {
                return null;
            }
        }
        return null;
    }

    /**
     * Whether statement {@code index} is in a step {@code PROC} was given {@code procedure} on.
     */
    static boolean isWithinProc(List<Statement> statements, int index, String procedure) {
        Statement step = stepOf(statements, index);
        return step != null && step.isKeyword("PROC") && procedure.equalsIgnoreCase(step.getWordText(1));
    }

    /**
     * The first quoted word of a statement at or after {@code from}, without its quotes, or null
     * where every word was written bare. A {@code LIBNAME} or an {@code INFILE} that writes one is
     * naming a data set itself; one that does not is naming a DD the step allocated.
     */
    static @Nullable String literalIn(Statement statement, int from) {
        List<String> words = statement.getWordTexts();
        for (int i = Math.max(from, 0); i < words.size(); i++) {
            String word = words.get(i);
            if (word.length() > 1 && (word.charAt(0) == '\'' || word.charAt(0) == '"') &&
                word.charAt(word.length() - 1) == word.charAt(0)) {
                return word.substring(1, word.length() - 1);
            }
        }
        return null;
    }

    /**
     * Splits what a macro was invoked or defined with at the commas written outside quotes and nested
     * parentheses.
     */
    static List<String> argumentsOf(String text) {
        List<String> arguments = new ArrayList<>();
        int depth = 0;
        int start = 0;
        char quote = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (quote != 0) {
                if (c == quote) {
                    quote = 0;
                }
            } else if (c == '\'' || c == '"') {
                quote = c;
            } else if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
            } else if (c == ',' && depth == 0) {
                arguments.add(text.substring(start, i).trim());
                start = i + 1;
            }
        }
        arguments.add(text.substring(start).trim());
        return arguments;
    }

    private void read() {
        int at = 0;
        while (at < text.length()) {
            char c = text.charAt(at);
            if (Character.isWhitespace(c)) {
                at++;
            } else if (text.startsWith("/*", at)) {
                at = closeOf(at);
            } else if (open == null && (c == '*' || text.startsWith("%*", at))) {
                at = endOf(at);
            } else if (c == ';') {
                closeStatement(lineAt(at), true);
                at++;
            } else {
                at = readWord(at);
            }
        }
        closeStatement(line, false);
    }

    /**
     * Where a word ends: the next blank, semicolon or comment. A quoted string is taken whole wherever
     * it begins, so {@code 'CLM.PROD.SAS - PAGER 4142'} is one word and the blanks and the semicolons
     * in it are none of this reader's business.
     */
    private int readWord(int from) {
        int at = from;
        while (at < text.length()) {
            char c = text.charAt(at);
            if (c == '\'' || c == '"') {
                at = quoteEnd(at, c);
            } else if (Character.isWhitespace(c) || c == ';' || text.startsWith("/*", at)) {
                break;
            } else {
                at++;
            }
        }
        if (open == null) {
            open = new ArrayList<>(4);
        }
        open.add(new Word(text.substring(from, at), lineAt(from)));
        return at;
    }

    /**
     * Ends the statement being read at a semicolon on {@code line}, or without one where the member
     * ran out first. A semicolon with no statement in front of it is the null statement, which SAS
     * allows and which means nothing — it is kept so that the statements read and the semicolons
     * written can be counted against each other.
     */
    private void closeStatement(int line, boolean terminated) {
        if (open == null && !terminated) {
            return;
        }
        List<Word> words = open == null ? emptyList() : open;
        statements.add(new Statement(words, words.isEmpty() ? line : words.get(0).getLine(), terminated));
        open = null;
    }

    /**
     * Where the closing quote of the string opening at {@code at} is, one past it. A doubled quote
     * inside the string is an escaped one and does not close it.
     */
    private int quoteEnd(int at, char quote) {
        for (int i = at + 1; i < text.length(); i++) {
            if (text.charAt(i) == quote) {
                if (i + 1 < text.length() && text.charAt(i + 1) == quote) {
                    i++;
                } else {
                    return i + 1;
                }
            }
        }
        return text.length();
    }

    /**
     * One past the {@code *}{@code /} closing the comment opening at {@code at}, or the end of the
     * member for one nobody closed.
     */
    private int closeOf(int at) {
        int close = text.indexOf("*/", at + 2);
        return close < 0 ? text.length() : close + 2;
    }

    /**
     * One past the semicolon ending the comment statement opening at {@code at}. A quoted string
     * inside it holds no semicolon of consequence, the same as anywhere else.
     */
    private int endOf(int at) {
        int i = at + 1;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (c == '\'' || c == '"') {
                i = quoteEnd(i, c);
            } else if (c == ';') {
                return i + 1;
            } else {
                i++;
            }
        }
        return text.length();
    }

    private int lineAt(int offset) {
        while (counted < offset) {
            if (text.charAt(counted++) == '\n') {
                line++;
            }
        }
        return line;
    }

    /**
     * A statement: every word written up to the semicolon that ends it, however many lines that took.
     * <p>
     * Nothing but the first word says what kind of statement it is, and SAS has no reserved words —
     * {@code DATA} names a step here and a variable there — so every word is kept as it was written
     * and the reading is left to the traits.
     */
    @Value
    public static class Statement {
        List<Word> words;

        /**
         * The one-based line the statement begins on.
         */
        int line;

        /**
         * Whether a semicolon ended it, which the last statement of a member nobody terminated has
         * none of.
         */
        boolean terminated;

        public List<String> getWordTexts() {
            List<String> texts = new ArrayList<>(words.size());
            for (Word word : words) {
                texts.add(word.getText());
            }
            return texts;
        }

        /**
         * The first word upper cased, which is what says what the statement is.
         */
        public String getKeyword() {
            return words.isEmpty() ? "" : words.get(0).getUpperText();
        }

        public boolean isKeyword(String keyword) {
            return keyword.equalsIgnoreCase(getKeyword());
        }

        /**
         * The nth word's text, or null where the statement wrote fewer than that.
         */
        public @Nullable String getWordText(int index) {
            return index >= 0 && index < words.size() ? words.get(index).getText() : null;
        }

        /**
         * The words joined by a single blank, which is what a trait reading a statement lexically
         * asks for.
         */
        public String getText() {
            return String.join(" ", getWordTexts());
        }
    }

    /**
     * A word as it was written, and the line it was written on — which for a query written over ten
     * lines is not the line its statement begins on.
     */
    @Value
    public static class Word {
        String text;
        int line;

        public String getUpperText() {
            return text.toUpperCase(Locale.ROOT);
        }
    }
}
