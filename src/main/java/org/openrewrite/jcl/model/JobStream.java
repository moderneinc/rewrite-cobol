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
package org.openrewrite.jcl.model;

import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.jcl.tree.Jcl;
import org.openrewrite.jcl.tree.Parameter;
import org.openrewrite.jcl.tree.Statement;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

/**
 * A job as a run of steps, each with the data sets it uses.
 * <p>
 * The tree already says what each statement is — {@link Jcl.JobControlStatement} carries its name field,
 * its operation and its typed parameters — so this adds only the shape above it: which DD statements
 * belong to which step, which data sets a concatenation names, and what a {@code DISP} means. It
 * reads the tree and holds no text of its own.
 * <p>
 * Not yet done: {@code PROC}/{@code PEND} resolution, symbolic substitution and step overrides
 * ({@code //STEP.DD}). Program to data set edges do not need them, and they are where this kind of
 * work historically overruns.
 */
@Value
public class JobStream {

    /**
     * The job name, or empty for a member with no {@code JOB} card — a procedure, or an included
     * fragment.
     */
    String jobName;

    /**
     * The {@code JOB} statement, or null when the member has none.
     */
    Jcl.@Nullable JobControlStatement job;

    List<Step> steps;

    public @Nullable Step step(String stepName) {
        for (Step step : steps) {
            if (step.getName().equalsIgnoreCase(stepName)) {
                return step;
            }
        }
        return null;
    }

    public static JobStream of(Jcl.CompilationUnit cu) {
        String jobName = "";
        Jcl.JobControlStatement job = null;
        List<Step> steps = new ArrayList<>();

        Jcl.JobControlStatement exec = null;
        List<DataDefinition> dds = new ArrayList<>();

        for (Statement statement : cu.getStatements()) {
            if (!(statement instanceof Jcl.JobControlStatement)) {
                continue;
            }
            Jcl.JobControlStatement jcl = (Jcl.JobControlStatement) statement;
            if (jcl.isOperation("JOB")) {
                job = jcl;
                jobName = jcl.getSimpleName();
            } else if (jcl.isOperation("EXEC")) {
                if (exec != null) {
                    steps.add(step(exec, dds));
                }
                exec = jcl;
                dds = new ArrayList<>();
            } else if (jcl.isOperation("DD") && exec != null) {
                addDataDefinition(dds, jcl);
            }
        }
        if (exec != null) {
            steps.add(step(exec, dds));
        }
        return new JobStream(jobName, job, unmodifiableList(steps));
    }

    private static Step step(Jcl.JobControlStatement exec, List<DataDefinition> dds) {
        String program = value(exec, "PGM");
        String procedure = value(exec, "PROC");
        if (program == null && procedure == null) {
            // EXEC MYPROC means the same as EXEC PROC=MYPROC.
            for (Parameter parameter : exec.getParameters()) {
                if (parameter instanceof Jcl.PositionalParameter) {
                    procedure = ((Jcl.PositionalParameter) parameter).getValueText();
                    break;
                }
            }
        }
        return new Step(exec.getSimpleName(), program, procedure, unmodifiableList(dds), exec);
    }

    /**
     * A DD with no name of its own continues the one before it, concatenating another data set onto
     * it. Read as separate statements, a concatenation reports data sets that nothing names — which
     * is the shape every STEPLIB in a portfolio takes.
     */
    private static void addDataDefinition(List<DataDefinition> dds, Jcl.JobControlStatement dd) {
        DataDefinition definition = definitionOf(dd);
        if (dd.getSimpleName().isEmpty() && !dds.isEmpty()) {
            DataDefinition previous = dds.remove(dds.size() - 1);
            List<DataSet> combined = new ArrayList<>(previous.getDataSets());
            combined.addAll(definition.getDataSets());
            List<Jcl.JobControlStatement> statements = new ArrayList<>(previous.getStatements());
            statements.add(dd);
            dds.add(new DataDefinition(previous.getName(), unmodifiableList(combined),
                    previous.isInStream(), previous.getSysout(), previous.isDummy(),
                    previous.getBackwardReference(), unmodifiableList(statements)));
            return;
        }
        dds.add(definition);
    }

    private static DataDefinition definitionOf(Jcl.JobControlStatement dd) {
        boolean inStream = false;
        boolean dummy = false;
        for (Parameter parameter : dd.getParameters()) {
            if (parameter instanceof Jcl.PositionalParameter) {
                String text = ((Jcl.PositionalParameter) parameter).getValueText().toUpperCase(Locale.ROOT);
                inStream |= "*".equals(text) || "DATA".equals(text);
                dummy |= "DUMMY".equals(text);
            }
        }

        String dsn = value(dd, "DSN");
        if (dsn == null) {
            dsn = value(dd, "DSNAME");
        }
        String backward = dsn != null && dsn.startsWith("*.") ? dsn.substring(2) : null;

        List<DataSet> dataSets = emptyList();
        if (dsn != null && backward == null) {
            String disp = value(dd, "DISP");
            Disposition disposition = disp == null ? null : Disposition.parse(disp);
            int open = dsn.indexOf('(');
            DataSet dataSet = open > 0 && dsn.endsWith(")") ?
                    new DataSet(dsn.substring(0, open), dsn.substring(open + 1, dsn.length() - 1), disposition) :
                    new DataSet(dsn, null, disposition);
            dataSets = java.util.Collections.singletonList(dataSet);
        }
        return new DataDefinition(dd.getSimpleName(), dataSets, inStream, value(dd, "SYSOUT"), dummy,
                backward, java.util.Collections.singletonList(dd));
    }

    /**
     * The value of a keyword parameter, without its {@code =}, or null when the statement has no such
     * parameter.
     */
    private static @Nullable String value(Jcl.JobControlStatement statement, String keyword) {
        Jcl.KeywordParameter parameter = statement.getParameter(keyword);
        return parameter == null ? null : parameter.getValueText();
    }

    /**
     * Every keyword parameter of a statement, keyed by upper cased keyword, for questions the model
     * does not name: {@code SPACE}, {@code RECFM}, {@code AMP}.
     */
    public static Map<String, String> parameters(Jcl.JobControlStatement statement) {
        Map<String, String> parameters = new LinkedHashMap<>();
        for (Parameter parameter : statement.getParameters()) {
            if (parameter instanceof Jcl.KeywordParameter) {
                Jcl.KeywordParameter keyword = (Jcl.KeywordParameter) parameter;
                parameters.putIfAbsent(keyword.getKeyword().getText().toUpperCase(Locale.ROOT),
                        keyword.getValueText());
            }
        }
        return parameters;
    }
}
