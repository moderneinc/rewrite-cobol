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
package org.openrewrite.mainframe.controlcard.sort.trait;

import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.mainframe.controlcard.CardLines;
import org.openrewrite.mainframe.controlcard.sort.SortIsoVisitor;
import org.openrewrite.mainframe.controlcard.sort.tree.Sort;
import org.openrewrite.mainframe.controlcard.sort.tree.Space;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.util.Collections.emptyList;

/**
 * A DFSORT or ICETOOL control statement, read for what it says about the records it moves rather than
 * for how it is written.
 * <p>
 * A sort card is the only place a batch stream says what its records are sorted on and which of them
 * survive: the JCL names the data sets, the COBOL either side of the sort names its own fields, and
 * neither says that a step keeps claims of status {@code O} and {@code P} ordered by type code. The
 * positions the card counts in are byte offsets into the record, so they join to a copybook and to
 * nothing else.
 * <p>
 * The data sets a sort reads and writes are not here: {@code SORTIN} and {@code SORTOUT} are DD
 * names, and only the JCL says what they are bound to.
 */
@Value
public class SortStatement implements Trait<Sort.ControlStatement> {

    Cursor cursor;

    /**
     * The operator: {@code SORT}, {@code MERGE}, {@code INCLUDE}, {@code OMIT}, {@code INREC},
     * {@code OUTREC}, {@code OUTFIL}, {@code SUM}, {@code OPTION}, or an ICETOOL one.
     */
    public String getOperator() {
        return getTree().getOperator().getText().toUpperCase(Locale.ROOT);
    }

    public boolean isOperator(String... operators) {
        for (String operator : operators) {
            if (getTree().isOperator(operator)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether the statement copies rather than orders. {@code SORT FIELDS=COPY} is how a step that
     * only reformats records is written, and it has no control fields at all.
     */
    public boolean isCopy() {
        return "COPY".equalsIgnoreCase(getOperand("FIELDS"));
    }

    /**
     * The control fields: what a {@code SORT} or {@code MERGE} orders on, and what a {@code SUM}
     * totals. Empty for every other operator, whose {@code FIELDS} is a reformatting list rather than
     * a list of keys — read those with {@link #getOperand} instead.
     */
    public List<Field> getFields() {
        if (!isOperator("SORT", "MERGE", "SUM")) {
            return emptyList();
        }
        String value = getOperand("FIELDS");
        return value == null ? emptyList() : fields(value);
    }

    /**
     * The value written under any of these keywords, without the parentheses that hold it. DFSORT
     * accepts an abbreviation of most keywords and the corpus writes both — {@code FIELDS} and
     * {@code BUILD} name the same reformatting list on an {@code OUTREC}.
     */
    public @Nullable String getOperand(String... keywords) {
        for (String keyword : keywords) {
            Sort.Operand operand = getTree().getParameter(keyword);
            if (operand != null) {
                String text = unwrap(operand.getValueText());
                return text.isEmpty() ? null : text;
            }
        }
        return null;
    }

    /**
     * Whether the statement writes any of these keywords. {@code EQUALS} and {@code NODUPS} take no
     * value, so asking for their text answers nothing.
     */
    public boolean hasOperand(String... keywords) {
        for (String keyword : keywords) {
            if (getTree().getParameter(keyword) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * The one-based line of the deck the statement was written on.
     */
    public int getLine() {
        return CardLines.of(cursor, Sort.CompilationUnit.class, words())
                .getOrDefault(getTree().getOperator().getId(), 1);
    }

    /**
     * Where the words of a deck are, for {@link CardLines} to count the cards between them.
     */
    private static SortIsoVisitor<CardLines> words() {
        return new SortIsoVisitor<CardLines>() {
            @Override
            public Space visitSpace(Space space, Space.Location location, CardLines lines) {
                lines.space(space.getWhitespace());
                return space;
            }

            @Override
            public Sort.Word visitWord(Sort.Word word, CardLines lines) {
                Sort.Word w = super.visitWord(word, lines);
                lines.word(w.getId());
                return w;
            }
        };
    }

    /**
     * Reads {@code (53,4,CH,A,1,10,CH,A)} as two fields.
     * <p>
     * Which parts a field is written with varies: a {@code SUM} field has no order, and a
     * {@code SORT} whose statement carries a {@code FORMAT} operand leaves the format off each field.
     * Rather than work out which shape was used, each part is recognised by what it looks like — a
     * position and a length are numbers, and only {@code A}, {@code D} and {@code E} are orders.
     */
    private static List<Field> fields(String value) {
        List<Field> fields = new ArrayList<>();
        String[] parts = value.split(",", -1);
        int i = 0;
        while (i + 1 < parts.length && isNumber(parts[i]) && isNumber(parts[i + 1])) {
            int position = Integer.parseInt(parts[i].trim());
            int length = Integer.parseInt(parts[i + 1].trim());
            i += 2;

            String format = null;
            if (i < parts.length && !isNumber(parts[i]) && !isOrder(parts[i])) {
                format = parts[i++].trim().toUpperCase(Locale.ROOT);
            }
            String order = null;
            if (i < parts.length && isOrder(parts[i])) {
                order = parts[i++].trim().toUpperCase(Locale.ROOT);
            }
            fields.add(new Field(position, length, format, order));
        }
        return fields;
    }

    private static boolean isNumber(String part) {
        String text = part.trim();
        if (text.isEmpty()) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            if (!Character.isDigit(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isOrder(String part) {
        String text = part.trim();
        return "A".equalsIgnoreCase(text) || "D".equalsIgnoreCase(text) || "E".equalsIgnoreCase(text);
    }

    private static String unwrap(String value) {
        String text = value.trim();
        if (text.startsWith("=")) {
            text = text.substring(1).trim();
        }
        if (text.startsWith("(") && text.endsWith(")")) {
            text = text.substring(1, text.length() - 1);
        }
        return text.trim();
    }

    /**
     * One control field: where in the record it begins, how long it is, how it is encoded, and which
     * way it orders. The position is a one-based byte offset, which is what joins a sort card to the
     * copybook that describes the record.
     */
    @Value
    public static class Field {
        int position;
        int length;

        @Nullable
        String format;

        /**
         * {@code A}, {@code D} or {@code E}, or null on a {@code SUM} field, which orders nothing.
         */
        @Nullable
        String order;
    }

    public static class Matcher extends SimpleTraitMatcher<SortStatement> {

        @Override
        protected @Nullable SortStatement test(Cursor cursor) {
            return cursor.getValue() instanceof Sort.ControlStatement ? new SortStatement(cursor) : null;
        }
    }

    @Override
    public String toString() {
        String fields = getOperand("FIELDS", "COND", "BUILD", "OVERLAY", "FROM");
        return (getOperator() + (fields == null ? "" : " " + fields)).trim();
    }
}
