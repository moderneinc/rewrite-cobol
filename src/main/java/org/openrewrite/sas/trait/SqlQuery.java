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
import org.openrewrite.text.PlainText;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * The statements of a program's {@code PROC SQL} steps that read tables, and the names they read them
 * by.
 * <p>
 * This is a lexical read and nothing more: the names after a {@code FROM} or a {@code JOIN} are taken
 * as written, with no SQL parsed and no libref resolved. It is enough for the question a lineage
 * recipe asks — which tables this report touched — and it is not enough to answer anything about the
 * query itself.
 * <p>
 * Which side of the connection a name is on does matter, and the source says: a name inside the
 * parentheses of {@code SELECT * FROM CONNECTION TO DB2 (...)} is a DB2 table, and one outside is a
 * data set of a SAS library. The fixture reads {@code CLM.POLICY_ACTIVE} that way — a view, not the
 * table under it — and joins the result to two SAS data sets.
 */
@Value
public class SqlQuery implements Trait<PlainText> {

    /**
     * The words that end a {@code FROM} list. What stands between two of them is a table and its
     * alias.
     */
    private static final Set<String> ENDS_LIST = new HashSet<>(Arrays.asList(
            "SELECT", "WHERE", "GROUP", "HAVING", "ORDER", "ON", "UNION", "INTERSECT", "EXCEPT",
            "INTO", "SET", "VALUES", "USING"));

    Cursor cursor;

    /**
     * The statements that read a table, in the order the program writes them.
     */
    public List<Query> getQueries() {
        List<Query> queries = new ArrayList<>();
        List<Statements.Statement> statements = Statements.in(getTree().getText());
        for (int i = 0; i < statements.size(); i++) {
            Statements.Statement statement = statements.get(i);
            if (!Statements.isWithinProc(statements, i, "SQL")) {
                continue;
            }
            // CONNECT and DISCONNECT name the connection rather than a table, and DISCONNECT writes
            // its FROM in front of the DBMS.
            if (statement.isKeyword("CONNECT") || statement.isKeyword("DISCONNECT")) {
                continue;
            }
            if (namesAList(statement)) {
                queries.add(new Query(tables(statement), statement.getLine()));
            }
        }
        return queries;
    }

    private static boolean namesAList(Statements.Statement statement) {
        for (String word : statement.getWordTexts()) {
            String bare = bare(word);
            if ("FROM".equalsIgnoreCase(bare) || "JOIN".equalsIgnoreCase(bare)) {
                return true;
            }
        }
        return false;
    }

    /**
     * The tables one statement reads, in the order it names them.
     */
    private static List<Table> tables(Statements.Statement statement) {
        List<Table> tables = new ArrayList<>();
        List<Statements.Word> words = statement.getWords();
        String dbms = null;
        boolean inList = false;
        boolean expectName = false;
        for (int i = 0; i < words.size(); i++) {
            String text = words.get(i).getText();
            String bare = bare(text);
            String upper = bare.toUpperCase(Locale.ROOT);
            if ("FROM".equals(upper) || "JOIN".equals(upper)) {
                // FROM CONNECTION TO DB2 opens a passthrough rather than naming a table, and what
                // the DBMS is asked for is the parenthesised query after it — which is the rest of
                // the statement, since nothing may follow the expression the result is selected from.
                if (i + 3 < words.size() && "CONNECTION".equalsIgnoreCase(bare(words.get(i + 1).getText()))) {
                    dbms = bare(words.get(i + 3).getText()).toUpperCase(Locale.ROOT);
                    i += 3;
                    inList = false;
                    continue;
                }
                inList = true;
                expectName = true;
            } else if (inList && ENDS_LIST.contains(upper)) {
                inList = false;
            } else if (inList && !bare.isEmpty()) {
                if (expectName) {
                    tables.add(new Table(upper, dbms, words.get(i).getLine()));
                }
                expectName = text.endsWith(",");
            }
        }
        return tables;
    }

    /**
     * The punctuation a name is written among: the parenthesis a subquery opens with and the comma
     * that separates one name in a {@code FROM} list from the next.
     */
    private static String bare(String word) {
        int from = 0;
        int to = word.length();
        while (from < to && word.charAt(from) == '(') {
            from++;
        }
        while (to > from && (word.charAt(to - 1) == ')' || word.charAt(to - 1) == ',')) {
            to--;
        }
        return word.substring(from, to);
    }

    @Value
    public static class Query {
        List<Table> tables;

        int line;

        @Override
        public String toString() {
            return "SQL over " + tables;
        }
    }

    @Value
    public static class Table {

        /**
         * The name as the query writes it, which for a SAS data set is qualified by its libref and
         * for a DB2 table by its schema.
         */
        String name;

        /**
         * The DBMS the name was read out of, or null for a data set of a SAS library.
         */
        @Nullable
        String dbms;

        /**
         * The one-based line the name was written on, which for a query written over ten lines is
         * not the line the statement begins on.
         */
        int line;

        public boolean isPassthrough() {
            return dbms != null;
        }
    }

    public static class Matcher extends SimpleTraitMatcher<SqlQuery> {

        @Override
        protected @Nullable SqlQuery test(Cursor cursor) {
            return Statements.isProgram(cursor) ? new SqlQuery(cursor) : null;
        }
    }

    @Override
    public String toString() {
        return "SQL of " + getTree().getSourcePath();
    }
}
