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
package org.openrewrite.mainframe.jcl.trait;

import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.mainframe.jcl.tree.Jcl;
import org.openrewrite.mainframe.jcl.tree.Statement;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A DD statement, read for what it says about data rather than for how it is written.
 * <p>
 * The DD name is the join to COBOL: {@code SELECT ACCT-FILE ASSIGN TO ACCTDD} in the program and
 * {@code //ACCTDD DD DSN=...} in the JCL are two halves of one fact, and neither half says what the
 * other knows. The program never learns the data set name; the JCL never learns what the program
 * does with it.
 */
@Value
public class DataDefinition implements Trait<Jcl.JobControlStatement> {

    Cursor cursor;

    /**
     * The DD name, which is what a program's {@code ASSIGN} clause names. Empty for a DD that
     * concatenates onto the one before it.
     */
    public String getName() {
        return getTree().getSimpleName();
    }

    /**
     * The data sets named, in order, including those concatenated on by the unnamed DD statements
     * that follow. A concatenation is one DD as far as the program reading it is concerned, so
     * reading them separately reports data sets that nothing names.
     */
    public List<DataSet> getDataSets() {
        List<DataSet> dataSets = new ArrayList<>(1);
        DataSet own = dataSetOf(getTree());
        if (own != null) {
            dataSets.add(own);
        }
        for (Statement statement : Steps.withinDataDefinition(cursor)) {
            if (Steps.isOperation(statement, "DD")) {
                DataSet concatenated = dataSetOf((Jcl.JobControlStatement) statement);
                if (concatenated != null) {
                    dataSets.add(concatenated);
                }
            }
        }
        return dataSets;
    }

    /**
     * The step this DD is written under, or null for one written before any step. A DD means nothing
     * on its own — the step is what says which program will open it.
     */
    public @Nullable Step getStep() {
        return Steps.stepOf(cursor);
    }

    /**
     * True for {@code DD *} and {@code DD DATA}, where the data follows in the job stream rather than
     * living in a data set. SYSIN control cards arrive this way.
     */
    public boolean isInStream() {
        return hasPositional("*") || hasPositional("DATA");
    }

    /**
     * True for {@code DD DUMMY}, where the program's I/O succeeds and goes nowhere. A step reading a
     * DUMMY file is doing nothing, which is worth seeing rather than reporting as a read.
     */
    public boolean isDummy() {
        return hasPositional("DUMMY");
    }

    /**
     * The {@code SYSOUT} class, or null. A SYSOUT DD produces printed output rather than a data set,
     * so it has no name the rest of the installation shares.
     */
    public @Nullable String getSysout() {
        return value("SYSOUT");
    }

    /**
     * The DD this one refers to, from {@code DSN=*.STEP.DDNAME}. Backward references are how a later
     * step names a data set an earlier one created without repeating its name.
     */
    public @Nullable String getBackwardReference() {
        String dsn = dataSetName(getTree());
        return dsn != null && dsn.startsWith("*.") ? dsn.substring(2) : null;
    }

    /**
     * Whether the data under this DD was written by the step that called the procedure it is in.
     * An override that supplies a DD's data is resolved into the procedure's step and also stands
     * where the caller wrote it, so the same cards are in the job twice and are one deck.
     */
    public boolean isDataOverridden() {
        Step step = getStep();
        Step caller = step == null ? null : step.getCallingStep();
        if (caller == null) {
            return false;
        }
        for (DataDefinition override : caller.getDataDefinitions()) {
            if (!override.getInStreamData().isEmpty() && getName().equalsIgnoreCase(override.getName()) &&
                overrides(override, step)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether an override was written for this step. {@code //SORT.SYSIN} replaces the SYSIN of the
     * procedure's {@code SORT} step and leaves every other step's alone; a procedure of one step may
     * be overridden without naming it.
     */
    private static boolean overrides(DataDefinition override, Step step) {
        String name = override.getTree().getName().getText();
        int dot = name.indexOf('.');
        return dot < 0 || name.substring(name.startsWith("//") ? 2 : 0, dot).equalsIgnoreCase(step.getName());
    }

    /**
     * The in-stream data this DD carries, if any.
     */
    public List<Jcl.DataDefinitionStream> getInStreamData() {
        List<Jcl.DataDefinitionStream> data = new ArrayList<>();
        for (Statement statement : Steps.withinDataDefinition(cursor)) {
            if (statement instanceof Jcl.DataDefinitionStream) {
                data.add((Jcl.DataDefinitionStream) statement);
            }
        }
        return data;
    }

    private boolean hasPositional(String value) {
        for (org.openrewrite.mainframe.jcl.tree.Parameter parameter : getTree().getParameters()) {
            if (parameter instanceof Jcl.PositionalParameter &&
                ((Jcl.PositionalParameter) parameter).getValueText().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    private @Nullable String value(String keyword) {
        Jcl.KeywordParameter parameter = getTree().getParameter(keyword);
        return parameter == null ? null : Steps.resolved(parameter);
    }

    /**
     * The data set named, with its symbols filled in: {@code &HLQ..CLMMAST} is a name no catalog
     * holds, and {@code CLM.PROD.CLMMAST} is the data set the step opens.
     */
    private static @Nullable String dataSetName(Jcl.JobControlStatement dd) {
        Jcl.KeywordParameter dsn = dd.getParameter("DSN");
        if (dsn == null) {
            dsn = dd.getParameter("DSNAME");
        }
        return dsn == null ? null : Steps.resolved(dsn);
    }

    private static @Nullable DataSet dataSetOf(Jcl.JobControlStatement dd) {
        String name = dataSetName(dd);
        if (name == null || name.startsWith("*.")) {
            return null;
        }
        Jcl.KeywordParameter disp = dd.getParameter("DISP");
        Disposition disposition = disp == null ? null : Disposition.parse(disp.getValueText());
        int open = name.indexOf('(');
        return open > 0 && name.endsWith(")") ?
                new DataSet(name.substring(0, open), name.substring(open + 1, name.length() - 1), disposition) :
                new DataSet(name, null, disposition);
    }

    public static class Matcher extends SimpleTraitMatcher<DataDefinition> {

        /**
         * An unnamed DD that continues the one above it does not match: it is part of that DD, and
         * matching it would report the same thing twice. An unnamed DD with nothing to continue does
         * match — it is a DD in its own right, and dropping it would lose a data set nothing else
         * names.
         */
        @Override
        protected @Nullable DataDefinition test(Cursor cursor) {
            Object value = cursor.getValue();
            if (!(value instanceof Jcl.JobControlStatement) ||
                !((Jcl.JobControlStatement) value).isOperation("DD")) {
                return null;
            }
            boolean named = !((Jcl.JobControlStatement) value).getSimpleName().isEmpty();
            return named || !Steps.continuesADataDefinition(cursor) ? new DataDefinition(cursor) : null;
        }
    }

    @Override
    public String toString() {
        return "DD " + getName().toUpperCase(Locale.ROOT);
    }
}
