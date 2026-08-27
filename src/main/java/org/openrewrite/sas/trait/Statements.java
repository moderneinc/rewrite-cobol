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
package org.openrewrite.sas.trait;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.controlcard.CardLines;
import org.openrewrite.sas.SasIsoVisitor;
import org.openrewrite.sas.tree.Sas;
import org.openrewrite.sas.tree.Space;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * Walking the statements around one, which is how a SAS program says what belongs to what.
 * <p>
 * A member is a flat run of statements and means something structured: the statements between a
 * {@code PROC} or {@code DATA} and the {@code RUN;} or {@code QUIT;} under it are one step, and what
 * a statement means depends on which step it is in — {@code FILE} in a DATA step writes an external
 * file and {@code LIBRARY=} in {@code PROC FORMAT} names a libref. Nothing but position says so, so
 * the tree stays flat and the step is read from the cursor.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class Statements {

    static List<Sas> allIn(Cursor cursor) {
        Sas.CompilationUnit cu = cursor.firstEnclosing(Sas.CompilationUnit.class);
        return cu == null ? emptyList() : cu.getStatements();
    }

    /**
     * The {@code PROC} or {@code DATA} statement that opened the step {@code cursor}'s statement is
     * in, or null where it is between steps. A {@code RUN;} or {@code QUIT;} ends a step, and so does
     * the next step opener: SAS runs the one before without being told to.
     */
    static Sas.@Nullable Statement stepOf(Cursor cursor) {
        List<Sas> statements = allIn(cursor);
        for (int i = indexOf(statements, cursor.getValue()); i >= 0; i--) {
            if (!(statements.get(i) instanceof Sas.Statement)) {
                continue;
            }
            Sas.Statement statement = (Sas.Statement) statements.get(i);
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
     * Whether {@code cursor}'s statement is in a step {@code PROC} was given {@code procedure} on.
     */
    static boolean isWithinProc(Cursor cursor, String procedure) {
        Sas.Statement step = stepOf(cursor);
        return step != null && step.isKeyword("PROC") && procedure.equalsIgnoreCase(step.getWordText(1));
    }

    /**
     * The one-based line of the member a word was written on. Every row a member contributes is
     * anchored at the line of the statement that said it.
     */
    static int lineOf(Cursor cursor, Sas.Word word) {
        return CardLines.of(cursor, Sas.CompilationUnit.class, words()).getOrDefault(word.getId(), 1);
    }

    /**
     * The line the statement at {@code cursor} begins on.
     */
    static int lineOf(Cursor cursor) {
        Sas.Statement statement = (Sas.Statement) cursor.getValue();
        List<Sas.Word> words = statement.getWords();
        return words.isEmpty() ? 1 : lineOf(cursor, words.get(0));
    }

    /**
     * The first quoted word of a statement at or after {@code from}, without its quotes, or null
     * where every word was written bare. A {@code LIBNAME} or an {@code INFILE} that writes one is
     * naming a data set itself; one that does not is naming a DD the step allocated.
     */
    static @Nullable String literalIn(Sas.Statement statement, int from) {
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

    private static int indexOf(List<Sas> statements, Object statement) {
        for (int i = 0; i < statements.size(); i++) {
            if (statements.get(i) == statement) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Where the words of a member are, for {@link CardLines} to count the lines between them. A
     * comment carries lines of its own, so its text is counted as well as the space around it.
     */
    private static SasIsoVisitor<CardLines> words() {
        return new SasIsoVisitor<CardLines>() {
            @Override
            public Space visitSpace(Space space, Space.Location location, CardLines lines) {
                lines.space(space.getWhitespace());
                return space;
            }

            @Override
            public Sas.Comment visitComment(Sas.Comment comment, CardLines lines) {
                Sas.Comment c = super.visitComment(comment, lines);
                lines.space(c.getText());
                return c;
            }

            @Override
            public Sas.Word visitWord(Sas.Word word, CardLines lines) {
                Sas.Word w = super.visitWord(word, lines);
                lines.word(w.getId());
                lines.space(w.getText());
                return w;
            }
        };
    }
}
