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

import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.sas.tree.Sas;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * The {@code INPUT} statement of a DATA step, which is a copybook written again in SAS.
 * <p>
 * {@code @001 CLAIMNO $CHAR10.} says the same thing as {@code 05 EXT-CLAIM-NO PIC X(10)} at offset
 * zero: the column is the offset the copybook gives and the informat is what the COBOL picture is on
 * the tape. The names are not the COBOL names — a SAS variable holds eight characters and
 * {@code AUD-OLD-RESERVE} is fifteen — so the join between the two is by position and never by name,
 * which is why the column and the width in bytes are what this trait answers.
 */
@Value
public class InputLayout implements Trait<Sas.Statement> {

    /**
     * An informat: an optional {@code $}, a name, the width, and the decimal places after the point.
     */
    private static final Pattern INFORMAT = Pattern.compile("\\$?[A-Za-z_]*\\d*\\.\\d*");

    private static final Pattern COLUMN = Pattern.compile("@(\\d+)");

    Cursor cursor;

    /**
     * The variables read, in the order the statement reads them.
     */
    public List<Field> getFields() {
        List<Field> fields = new ArrayList<>();
        List<String> words = getTree().getWordTexts();
        int column = 1;
        String pending = null;
        for (int i = 1; i < words.size(); i++) {
            String word = words.get(i);
            if (COLUMN.matcher(word).matches()) {
                if (pending != null) {
                    column += add(fields, column, pending, "");
                    pending = null;
                }
                column = Integer.parseInt(word.substring(1));
            } else if (pending != null && isInformat(word)) {
                column += add(fields, column, pending, word);
                pending = null;
            } else {
                if (pending != null) {
                    column += add(fields, column, pending, "");
                }
                pending = word;
            }
        }
        if (pending != null) {
            add(fields, column, pending, "");
        }
        return fields;
    }

    public int getLine() {
        return Statements.lineOf(cursor);
    }

    private static int add(List<Field> fields, int column, String name, String informat) {
        int bytes = bytesOf(informat);
        fields.add(new Field(column, name.toUpperCase(Locale.ROOT), informat, bytes));
        return bytes;
    }

    private static boolean isInformat(String word) {
        return word.length() > 1 && INFORMAT.matcher(word).matches();
    }

    /**
     * The width an informat reads, which is the number written before the point. An informat that
     * writes none lets SAS decide, and nothing here can say how wide that is.
     */
    private static int bytesOf(String informat) {
        int dot = informat.indexOf('.');
        int at = dot;
        while (at > 0 && Character.isDigit(informat.charAt(at - 1))) {
            at--;
        }
        return dot < 0 || at == dot ? 0 : Integer.parseInt(informat.substring(at, dot));
    }

    @Value
    public static class Field {

        /**
         * The one-based column of the record the variable begins in. Stated by the {@code @} in front
         * of it, or, where the statement writes none, where the variable before it ended.
         */
        int column;

        String name;

        /**
         * The informat as written, or empty for a variable read without one.
         */
        String informat;

        /**
         * How many bytes of the record the informat reads, or 0 where it left the width to SAS.
         */
        int bytes;
    }

    public static class Matcher extends SimpleTraitMatcher<InputLayout> {

        @Override
        protected @Nullable InputLayout test(Cursor cursor) {
            Object value = cursor.getValue();
            if (!(value instanceof Sas.Statement) || !((Sas.Statement) value).isKeyword("INPUT") ||
                ((Sas.Statement) value).getWordText(1) == null) {
                return null;
            }
            Sas.Statement step = Statements.stepOf(cursor);
            return step != null && step.isKeyword("DATA") ? new InputLayout(cursor) : null;
        }
    }

    @Override
    public String toString() {
        return "INPUT of " + getFields().size() + " variables";
    }
}
