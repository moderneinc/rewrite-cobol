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
package org.openrewrite.mainframe.controlcard.utility.trait;

import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.mainframe.controlcard.utility.tree.Statement;
import org.openrewrite.mainframe.controlcard.utility.tree.Utility;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * A {@code TEMPLATE} statement: a file the deck allocates for itself instead of leaving it to a DD.
 * <p>
 * A template data set name is written with two kinds of substitution that look exactly alike, and
 * they are not the same kind. {@code &DB.} and {@code &PART.} are the utility's own variables, filled
 * in when it runs, one file per table space or per partition; {@code &HLQ.} is a JCL symbol, which
 * JES fills in before the step starts and only when the DD says {@code SYMBOLS=JCLONLY}. Nothing in
 * how they are written tells them apart, so they are told apart by name: the utility's list is
 * closed, and a deck adds to it only through {@code OPTIONS TEMPLATESET}.
 */
@Value
public class Template implements Trait<Utility.Block> {

    Cursor cursor;

    /**
     * The name the template is known by, which is the DD name a keyword of the deck writes to.
     */
    public String getName() {
        return getTree().getValueText();
    }

    /**
     * The data set name pattern, with the apostrophes it was written in taken off.
     */
    public @Nullable String getDataSetName() {
        String dsn = Operands.textOf(getTree(), "DSN");
        return dsn == null ? Operands.textOf(getTree(), "PATH") : dsn;
    }

    /**
     * The utility's own variables in the data set name, in the order they were written. These are
     * still in the text when the utility reads the deck, and are what makes one template stand for a
     * file per table space or per partition.
     */
    public List<String> getVariables() {
        List<String> variables = new ArrayList<>();
        for (String name : references()) {
            if (VARIABLES.contains(name) || declared().contains(name)) {
                variables.add(name);
            }
        }
        return variables;
    }

    /**
     * The JCL symbols in the data set name, in the order they were written. These are the job's, not
     * the deck's: JES has already replaced them by the time the utility reads the card, and only when
     * the DD carrying the deck asked it to.
     */
    public List<String> getSymbols() {
        List<String> symbols = new ArrayList<>();
        for (String name : references()) {
            if (!VARIABLES.contains(name) && !declared().contains(name)) {
                symbols.add(name);
            }
        }
        return symbols;
    }

    /**
     * Every {@code &NAME.} in the data set name. A reference ends at the period, which both kinds are
     * written with.
     */
    private List<String> references() {
        String dsn = getDataSetName();
        List<String> names = new ArrayList<>();
        if (dsn == null) {
            return names;
        }
        for (int i = dsn.indexOf('&'); i >= 0; i = dsn.indexOf('&', i + 1)) {
            int end = i + 1;
            while (end < dsn.length() && (Character.isLetterOrDigit(dsn.charAt(end)) ||
                                          dsn.charAt(end) == '@' || dsn.charAt(end) == '#' ||
                                          dsn.charAt(end) == '$')) {
                end++;
            }
            if (end > i + 1) {
                names.add(dsn.substring(i + 1, end).toUpperCase(Locale.ROOT));
            }
            i = end - 1;
        }
        return names;
    }

    /**
     * The variables the deck declares for itself, from {@code OPTIONS TEMPLATESET(name =: value)}.
     */
    private Set<String> declared() {
        Set<String> declared = new HashSet<>();
        for (Statement statement : cursor.firstEnclosingOrThrow(Utility.CompilationUnit.class).getStatements()) {
            if (statement instanceof Utility.Block) {
                collectDeclared((Utility.Block) statement, declared);
            }
        }
        return declared;
    }

    private static void collectDeclared(Utility.Block block, Set<String> declared) {
        Utility.Operand templateSet = block.getOperand("TEMPLATESET");
        if (templateSet != null) {
            for (String pair : Operands.unwrapped(templateSet.getValueText()).split(",")) {
                String name = pair.split("=", 2)[0].trim();
                if (!name.isEmpty()) {
                    declared.add(name.toUpperCase(Locale.ROOT));
                }
            }
        }
        for (Utility.Block nested : block.getBlocks()) {
            collectDeclared(nested, declared);
        }
    }

    /**
     * The variables the utility publishes, long form and short, from the template block's
     * documentation.
     */
    private static final Set<String> VARIABLES = new HashSet<>(Arrays.asList(
            "JOBNAME", "JO", "STEPNAME", "ST", "USERID", "US", "UTILID", "UT", "SSID", "SS",
            "UTILNAME", "UN", "SEQ", "SQ", "LIST", "LI", "DB", "TS", "SN", "PART", "PA",
            "DATE", "DT", "TIME", "TI", "JDATE", "JU", "YEAR", "YE", "MONTH", "MO", "DAY", "DA",
            "JDAY", "JD", "HOUR", "HO", "MINUTE", "MI", "SECOND", "SC", "UNIQ", "UQ"));

    public static class Matcher extends SimpleTraitMatcher<Template> {

        @Override
        protected @Nullable Template test(Cursor cursor) {
            Object value = cursor.getValue();
            return value instanceof Utility.Block && ((Utility.Block) value).isVerb("TEMPLATE") ?
                    new Template(cursor) : null;
        }
    }

    @Override
    public String toString() {
        return "TEMPLATE " + getName();
    }
}
