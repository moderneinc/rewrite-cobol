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
import org.openrewrite.jcl.tree.Statement;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

/**
 * What a JCL member says, read out of the lossless tree.
 * <p>
 * The tree is a flat run of words — {@code //STEP1 EXEC PGM=IEFBR14} is three unrelated statements —
 * because that is what lets it be printed back exactly. Everything above it needs the other view:
 * jobs made of steps, steps made of DD statements, DD statements naming data sets. This builds that
 * view without disturbing the tree, the same way {@code DataItems} builds a record hierarchy out of
 * COBOL level numbers.
 * <p>
 * It is a reading, not an editing model. Nothing here can be changed and printed back; a recipe that
 * rewrites JCL still works on the tree.
 * <p>
 * Not yet done: {@code PROC}/{@code PEND} resolution, symbolic substitution and step overrides
 * ({@code //STEP.DD}). Program to data set edges do not need them, and they are where this kind of
 * work historically overruns.
 */
@Value
public class JobStream {

    /**
     * The job name, from the {@code JOB} statement, or empty for a member with no {@code JOB} card —
     * a procedure or an included fragment.
     */
    String jobName;

    /**
     * Every EXEC parameter of the {@code JOB} statement, keyed by upper cased keyword:
     * {@code CLASS}, {@code MSGCLASS}, {@code NOTIFY}, {@code REGION}.
     */
    Map<String, String> jobParameters;

    List<Step> steps;

    /**
     * The step of this job with the given name, or null.
     */
    public @Nullable Step step(String stepName) {
        for (Step step : steps) {
            if (step.getName().equalsIgnoreCase(stepName)) {
                return step;
            }
        }
        return null;
    }

    public static JobStream of(Jcl.CompilationUnit cu) {
        List<Card> cards = cardsOf(cu.getStatements());

        String jobName = "";
        Map<String, String> jobParameters = new LinkedHashMap<>();
        List<Step> steps = new ArrayList<>();

        String stepName = "";
        String program = null;
        String procedure = null;
        Map<String, String> stepParameters = new LinkedHashMap<>();
        List<DataDefinition> dds = new ArrayList<>();
        boolean inStep = false;

        for (Card card : cards) {
            switch (card.operation) {
                case "JOB":
                    jobName = card.name;
                    jobParameters = keywords(card.operands);
                    break;
                case "EXEC":
                    if (inStep) {
                        steps.add(new Step(stepName, program, procedure, unmodifiableList(dds), stepParameters));
                    }
                    inStep = true;
                    stepName = card.name;
                    stepParameters = keywords(card.operands);
                    program = stepParameters.get("PGM");
                    procedure = procedureOf(card.operands, stepParameters);
                    dds = new ArrayList<>();
                    break;
                case "DD":
                    if (inStep) {
                        addDd(dds, card);
                    }
                    break;
                default:
                    break;
            }
        }
        if (inStep) {
            steps.add(new Step(stepName, program, procedure, unmodifiableList(dds), stepParameters));
        }
        return new JobStream(jobName, jobParameters, unmodifiableList(steps));
    }

    /**
     * A DD with no name of its own continues the one before it, concatenating another data set onto
     * it. A concatenation read as separate DD statements would report data sets nothing names.
     */
    private static void addDd(List<DataDefinition> dds, Card card) {
        DataDefinition dd = definitionOf(card);
        if (card.name.isEmpty() && !dds.isEmpty()) {
            DataDefinition previous = dds.remove(dds.size() - 1);
            List<DataSet> combined = new ArrayList<>(previous.getDataSets());
            combined.addAll(dd.getDataSets());
            dds.add(new DataDefinition(previous.getName(), unmodifiableList(combined),
                    previous.getParameters(), previous.isInStream(), previous.getSysout(),
                    previous.isDummy(), previous.getBackwardReference()));
            return;
        }
        dds.add(dd);
    }

    private static DataDefinition definitionOf(Card card) {
        Map<String, String> parameters = keywords(card.operands);
        List<String> positional = positionals(card.operands);

        boolean inStream = positional.contains("*") || positional.contains("DATA");
        boolean dummy = positional.contains("DUMMY");
        String sysout = parameters.get("SYSOUT");

        String dsn = parameters.containsKey("DSN") ? parameters.get("DSN") : parameters.get("DSNAME");
        String backward = dsn != null && dsn.startsWith("*.") ? dsn.substring(2) : null;

        List<DataSet> dataSets = emptyList();
        if (dsn != null && backward == null) {
            Disposition disposition = parameters.containsKey("DISP") ?
                    Disposition.parse(parameters.get("DISP")) : null;
            dataSets = new ArrayList<>(1);
            int open = dsn.indexOf('(');
            if (open > 0 && dsn.endsWith(")")) {
                dataSets.add(new DataSet(dsn.substring(0, open),
                        dsn.substring(open + 1, dsn.length() - 1), disposition));
            } else {
                dataSets.add(new DataSet(dsn, null, disposition));
            }
            dataSets = unmodifiableList(dataSets);
        }
        return new DataDefinition(card.name, dataSets, parameters, inStream, sysout, dummy, backward);
    }

    /**
     * A step's procedure can be named by {@code PROC=} or by a bare positional operand, since
     * {@code EXEC MYPROC} and {@code EXEC PROC=MYPROC} mean the same thing.
     */
    private static @Nullable String procedureOf(String operands, Map<String, String> parameters) {
        if (parameters.containsKey("PROC")) {
            return parameters.get("PROC");
        }
        if (parameters.containsKey("PGM")) {
            return null;
        }
        for (String positional : positionals(operands)) {
            return positional;
        }
        return null;
    }

    private static Map<String, String> keywords(String operands) {
        Map<String, String> parameters = new LinkedHashMap<>();
        for (String parameter : split(operands)) {
            int equals = indexOfAssignment(parameter);
            if (equals > 0) {
                parameters.putIfAbsent(parameter.substring(0, equals).trim().toUpperCase(Locale.ROOT),
                        parameter.substring(equals + 1).trim());
            }
        }
        return parameters;
    }

    private static List<String> positionals(String operands) {
        List<String> positional = new ArrayList<>();
        for (String parameter : split(operands)) {
            if (indexOfAssignment(parameter) <= 0 && !parameter.trim().isEmpty()) {
                positional.add(parameter.trim().toUpperCase(Locale.ROOT));
            }
        }
        return positional;
    }

    /**
     * The first {@code =} outside parentheses. Anything inside them belongs to a sub-parameter, so
     * {@code AMP=('BUFND=5')} is one parameter rather than two.
     */
    private static int indexOfAssignment(String parameter) {
        int depth = 0;
        for (int i = 0; i < parameter.length(); i++) {
            char c = parameter.charAt(i);
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
            } else if (c == '=' && depth == 0) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Splits on commas at parenthesis depth zero, so the commas inside
     * {@code DISP=(NEW,CATLG,DELETE)} do not split it into three parameters.
     */
    private static List<String> split(String operands) {
        List<String> parts = new ArrayList<>();
        int depth = 0;
        int start = 0;
        boolean quoted = false;
        for (int i = 0; i < operands.length(); i++) {
            char c = operands.charAt(i);
            if (c == '\'') {
                quoted = !quoted;
            } else if (quoted) {
                continue;
            } else if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
            } else if (c == ',' && depth == 0) {
                parts.add(operands.substring(start, i));
                start = i + 1;
            }
        }
        parts.add(operands.substring(start));
        return parts;
    }

    /**
     * One logical JCL statement: its name field, its operation, and its operands with continuations
     * joined.
     */
    private static final class Card {
        final String name;
        final String operation;
        final String operands;

        Card(String name, String operation, String operands) {
            this.name = name;
            this.operation = operation;
            this.operands = operands;
        }
    }

    /**
     * Groups the flat words into logical statements.
     * <p>
     * A statement begins with {@code //NAME} or a bare {@code //} and runs until the next one. A
     * bare {@code //} with something other than an operation after it marks a continuation, which is
     * how a long operand list is spread over several lines; one with an operation after it starts a
     * new statement, which is how an unnamed DD continuing a concatenation is written.
     * <p>
     * The operand field ends at the first blank on the line, and what follows is the comment field.
     * It is not one word, though, because the lexer breaks a quoted string out on its own:
     * {@code (ACCT),'DAILY POST',CLASS=A} arrives as three words with nothing between them. So words
     * are taken while they run on from the one before, and the first word separated by a blank is
     * where the comment field starts.
     */
    private static List<Card> cardsOf(List<Statement> statements) {
        List<Card> cards = new ArrayList<>();
        String name = null;
        String operation = null;
        StringBuilder operands = new StringBuilder();
        boolean expectOperation = false;
        boolean expectOperand = false;
        boolean inOperand = false;

        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);
            if (!(statement instanceof Jcl.JclStatement)) {
                continue;
            }
            Jcl.JclStatement jcl = (Jcl.JclStatement) statement;
            String text = jcl.getWord().getText();

            if (text.startsWith("//")) {
                if (text.length() > 2 && text.charAt(2) == '*') {
                    continue;
                }
                if ("//".equals(text) && i + 1 < statements.size() && !isOperation(statements.get(i + 1))) {
                    expectOperand = true;
                    inOperand = false;
                    continue;
                }
                if (operation != null) {
                    cards.add(new Card(name, operation, operands.toString()));
                }
                name = text.length() > 2 ? text.substring(2) : "";
                operation = null;
                operands = new StringBuilder();
                expectOperation = true;
                expectOperand = false;
                inOperand = false;
                continue;
            }

            if (expectOperation) {
                operation = text.toUpperCase(Locale.ROOT);
                expectOperation = false;
                expectOperand = true;
                continue;
            }
            if (expectOperand) {
                operands.append(text);
                expectOperand = false;
                inOperand = true;
                continue;
            }
            if (inOperand && jcl.getPrefix().getWhitespace().isEmpty()) {
                operands.append(text);
                continue;
            }
            inOperand = false;
        }
        if (operation != null) {
            cards.add(new Card(name, operation, operands.toString()));
        }
        return cards;
    }

    private static boolean isOperation(Statement statement) {
        if (!(statement instanceof Jcl.JclStatement)) {
            return false;
        }
        switch (((Jcl.JclStatement) statement).getWord().getText().toUpperCase(Locale.ROOT)) {
            case "JOB":
            case "EXEC":
            case "DD":
            case "PROC":
            case "PEND":
            case "SET":
            case "INCLUDE":
            case "JCLLIB":
            case "OUTPUT":
            case "IF":
            case "ELSE":
            case "ENDIF":
                return true;
            default:
                return false;
        }
    }
}
