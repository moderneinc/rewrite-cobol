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
package org.openrewrite.mainframe.jcl;

import org.jspecify.annotations.Nullable;
import org.openrewrite.Tree;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.mainframe.jcl.marker.ExpandedMember;
import org.openrewrite.mainframe.jcl.marker.ResolvedText;
import org.openrewrite.mainframe.jcl.marker.Symbolic;
import org.openrewrite.mainframe.jcl.marker.Symbolics;
import org.openrewrite.mainframe.jcl.tree.Jcl;
import org.openrewrite.mainframe.jcl.tree.Parameter;
import org.openrewrite.mainframe.jcl.tree.Space;
import org.openrewrite.mainframe.jcl.tree.Statement;
import org.openrewrite.marker.Marker;
import org.openrewrite.marker.Markers;

import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.openrewrite.Tree.randomId;

/**
 * Resolves what a job actually runs: the procedures its {@code EXEC} statements call, the members
 * its {@code INCLUDE} statements pull in, and the values its symbols stand for.
 * <p>
 * A resolved procedure or INCLUDE body is placed after the statement that named it as a
 * {@link Jcl.Expansion}, with the caller's DD overrides already applied and the caller's symbol
 * values recorded. Nothing written in another member joins the statements of this one, and the
 * printer leaves an expansion out, so the source still prints back byte for byte;
 * {@link ExpandedPrinter} prints the resolved listing from the same tree.
 * <p>
 * Members are resolved by member name against the set supplied to
 * {@link JclParser.Builder#procedureLibrary}, in-stream {@code PROC}/{@code PEND} definitions
 * first. A {@code JCLLIB ORDER=} names data sets, which say nothing about where a member is checked
 * in, so the order is read and reported ({@code Job.getProcedureLibraries()}) rather than searched.
 * <p>
 * Three things are deliberately simpler than z/OS. A DD override merges by keyword, and of the
 * parameters that nullify each other it models only a data source replacing {@code DUMMY} or
 * in-stream data. Every keyword on an {@code EXEC} that names a procedure is a symbol, except the
 * EXEC keywords that cannot be symbol names. And a symbol nothing set is left as it was written
 * rather than resolving to the null string, so a job that would not resolve reads as one that did
 * not rather than as one that resolved to nothing.
 */
public class ExpandJobVisitor<P> extends JclIsoVisitor<P> {

    /**
     * EXEC keywords that apply to the steps of a called procedure rather than naming a symbol.
     * {@code PGM} is not among them: a step that names a procedure cannot also name a program, so
     * {@code EXEC CLMBATCH,PGM=CLMB010} is a symbol.
     */
    private static final Set<String> EXEC_KEYWORDS = new HashSet<>(Arrays.asList("ACCT", "ADDRSPC",
            "COND", "DYNAMNBR", "MEMLIMIT", "PARM", "PERFORM", "PROC", "RD", "REGION", "RLSTMOUT",
            "TIME"));

    /**
     * Symbols the system supplies at run time. Anything else beginning {@code SYS} is one too.
     */
    private static final Set<String> SYSTEM_SYMBOLS = new HashSet<>(Arrays.asList("JOBNAME",
            "DATE", "TIME", "YYMMDD", "LDAY", "LMON", "LYR2", "LYR4", "LYYMMDD"));

    private static final int MAX_DEPTH = 15;

    private final Map<String, Jcl.CompilationUnit> library;

    public ExpandJobVisitor(Map<String, Jcl.CompilationUnit> library) {
        this.library = library;
    }

    @Override
    public Jcl.CompilationUnit visitCompilationUnit(Jcl.CompilationUnit cu, P p) {
        for (Statement statement : cu.getStatements()) {
            if (statement instanceof Jcl.Expansion) {
                return cu;
            }
        }
        List<Statement> expanded = expand(cu.getStatements(), new Scope(), new ArrayDeque<>());
        // Compared by identity: a statement equals itself once it is marked, since identity is the
        // node id and marking keeps it.
        if (expanded.size() == cu.getStatements().size()) {
            for (int i = 0; i < expanded.size(); i++) {
                if (expanded.get(i) != cu.getStatements().get(i)) {
                    return cu.withStatements(expanded);
                }
            }
            return cu;
        }
        return cu.withStatements(expanded);
    }

    /**
     * Walks one run of statements in order, because that is the only order the answers exist in: a
     * {@code SET} means nothing to the cards above it, and a procedure is called with the values in
     * effect where the call is written.
     */
    private List<Statement> expand(List<Statement> statements, Scope scope, Deque<String> open) {
        Map<String, Procedure> inStream = inStreamProcedures(statements);
        List<Statement> out = new ArrayList<>(statements.size());
        boolean substituteData = false;
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);
            if (statement instanceof Jcl.DataDefinitionStream) {
                out.add(substituteData ? resolveStream((Jcl.DataDefinitionStream) statement, scope) : statement);
                continue;
            }
            if (!(statement instanceof Jcl.JobControlStatement)) {
                substituteData &= !(statement instanceof Jcl.Delimiter);
                out.add(statement);
                continue;
            }
            Jcl.JobControlStatement resolved = resolveParameters((Jcl.JobControlStatement) statement, scope);
            if (resolved.isOperation("DD")) {
                substituteData = resolved.getParameter("SYMBOLS") != null;
                out.add(resolved);
                continue;
            }
            substituteData = false;

            if (resolved.isOperation("SET")) {
                out.add(resolved);
                set(resolved, scope);
            } else if (resolved.isOperation("EXPORT")) {
                out.add(resolved);
                export(resolved, scope);
            } else if (resolved.isOperation("INCLUDE")) {
                include(resolved, scope, open, out);
            } else if (resolved.isOperation("EXEC")) {
                exec(resolved, statements, i, scope, inStream, open, out);
            } else {
                out.add(resolved);
            }
        }
        return out;
    }

    /**
     * An {@code EXEC} statement, and the expansion of the procedure it calls. The DD overrides
     * written under the EXEC go into the expansion and stay where they were written as well, since
     * they are cards of this member.
     */
    private void exec(Jcl.JobControlStatement exec, List<Statement> statements, int index, Scope scope,
                      Map<String, Procedure> inStream, Deque<String> open, List<Statement> out) {
        String procedureName = procedureNameOf(exec, scope);
        if (procedureName == null) {
            out.add(mark(exec, symbolics(scope.symbolics.values())));
            return;
        }

        String key = procedureName.toUpperCase(Locale.ROOT);
        Procedure procedure = inStream.get(key);
        ExpandedMember.Status status = procedure == null ?
                ExpandedMember.Status.EXPANDED : ExpandedMember.Status.IN_STREAM;
        if (procedure == null) {
            procedure = catalogued(procedureName);
        }
        if (procedure == null || open.contains(key) || open.size() >= MAX_DEPTH) {
            out.add(mark(exec, new ExpandedMember(randomId(), ExpandedMember.Status.MISSING, procedureName),
                    symbolics(scope.symbolics.values())));
            return;
        }

        Scope inner = scope.copy();
        for (Symbolic symbolic : defaultsOf(procedure.statement, scope)) {
            inner.symbolics.put(symbolic.getName().toUpperCase(Locale.ROOT), symbolic);
        }
        for (Symbolic symbolic : overridesOf(exec, scope)) {
            inner.symbolics.put(symbolic.getName().toUpperCase(Locale.ROOT), symbolic);
        }
        out.add(mark(exec, new ExpandedMember(randomId(), status, procedureName),
                symbolics(inner.symbolics.values())));

        List<Statement> body = applyOverrides(freshen(procedure.body), overridesFollowing(statements, index));
        open.push(key);
        List<Statement> resolved = expand(body, inner, open);
        open.pop();
        out.add(new Jcl.Expansion(randomId(), Space.EMPTY, Markers.EMPTY, procedureName,
                Jcl.Expansion.Kind.PROCEDURE, onNewLine(resolved)));
    }

    private void include(Jcl.JobControlStatement statement, Scope scope, Deque<String> open,
                         List<Statement> out) {
        Jcl.KeywordParameter member = statement.getParameter("MEMBER");
        if (member == null) {
            out.add(statement);
            return;
        }
        String memberName = valueOf(member, scope);
        String key = memberName.toUpperCase(Locale.ROOT);
        Jcl.CompilationUnit included = library.get(key);
        if (included == null || open.contains(key) || open.size() >= MAX_DEPTH) {
            out.add(mark(statement, new ExpandedMember(randomId(), ExpandedMember.Status.MISSING, memberName)));
            return;
        }
        out.add(mark(statement, new ExpandedMember(randomId(), ExpandedMember.Status.EXPANDED, memberName)));
        // An included member sets symbols for the cards after it, which is what a shared job card
        // member is for, so it is expanded into the caller's scope rather than a copy of it.
        open.push(key);
        List<Statement> body = expand(freshen(included.getStatements()), scope, open);
        open.pop();
        out.add(new Jcl.Expansion(randomId(), Space.EMPTY, Markers.EMPTY, memberName,
                Jcl.Expansion.Kind.INCLUDE, onNewLine(body)));
    }

    private void set(Jcl.JobControlStatement statement, Scope scope) {
        for (Parameter parameter : statement.getParameters()) {
            if (parameter instanceof Jcl.KeywordParameter) {
                Jcl.KeywordParameter keyword = (Jcl.KeywordParameter) parameter;
                String name = keyword.getKeyword().getText();
                scope.symbolics.put(name.toUpperCase(Locale.ROOT),
                        new Symbolic(name, assignedValue(keyword, scope), Symbolic.Origin.SET));
            }
        }
    }

    private void export(Jcl.JobControlStatement statement, Scope scope) {
        Jcl.KeywordParameter symlist = statement.getParameter("SYMLIST");
        if (symlist == null) {
            return;
        }
        for (String name : Operands.list(valueOf(symlist, scope))) {
            if ("*".equals(name)) {
                scope.exportAll = true;
            } else {
                scope.exported.add(name.toUpperCase(Locale.ROOT));
            }
        }
    }

    private static @Nullable Symbolics symbolics(Collection<Symbolic> inEffect) {
        return inEffect.isEmpty() ? null : new Symbolics(randomId(), new ArrayList<>(inEffect));
    }

    private static Jcl.JobControlStatement resolveParameters(Jcl.JobControlStatement statement, Scope scope) {
        return statement.withOperands(ListUtils.map(statement.getOperands(), operand -> {
            if (operand instanceof Jcl.KeywordParameter) {
                Jcl.KeywordParameter keyword = (Jcl.KeywordParameter) operand;
                ResolvedText resolved = resolve(keyword.getValueText(), scope, false);
                return resolved == null ? operand : keyword.withMarkers(keyword.getMarkers().addIfAbsent(resolved));
            }
            if (operand instanceof Jcl.PositionalParameter) {
                Jcl.PositionalParameter positional = (Jcl.PositionalParameter) operand;
                ResolvedText resolved = resolve(positional.getValueText(), scope, false);
                return resolved == null ? operand : positional.withMarkers(positional.getMarkers().addIfAbsent(resolved));
            }
            return operand;
        }));
    }

    private static Jcl.DataDefinitionStream resolveStream(Jcl.DataDefinitionStream data, Scope scope) {
        ResolvedText resolved = resolve(data.getWord().getText(), scope, true);
        return resolved == null ? data : data.withMarkers(data.getMarkers().addIfAbsent(resolved));
    }

    /**
     * Substitutes the symbols in one piece of text, or returns null when it names none.
     * <p>
     * In-stream data is only substituted for symbols an {@code EXPORT SYMLIST} put in the exported
     * list, which is what z/OS does and what tells a control card that reads {@code &HLQ} from one
     * that is meant to say it.
     */
    private static @Nullable ResolvedText resolve(String text, Scope scope, boolean inStreamData) {
        if (text.indexOf('&') < 0) {
            return null;
        }
        StringBuilder out = new StringBuilder(text.length());
        List<Symbolic> referred = new ArrayList<>(2);
        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (c != '&') {
                out.append(c);
                i++;
                continue;
            }
            if (i + 1 < text.length() && text.charAt(i + 1) == '&') {
                // && is a temporary data set name, not a symbol.
                out.append("&&");
                i += 2;
                continue;
            }
            int end = i + 1;
            while (end < text.length() && end - i <= 8 && isSymbolCharacter(text.charAt(end), end == i + 1)) {
                end++;
            }
            if (end == i + 1) {
                out.append('&');
                i++;
                continue;
            }
            String name = text.substring(i + 1, end);
            boolean terminated = end < text.length() && text.charAt(end) == '.';
            i = terminated ? end + 1 : end;

            Symbolic symbolic = scope.symbolics.get(name.toUpperCase(Locale.ROOT));
            if (symbolic == null) {
                symbolic = new Symbolic(name, null, isSystemSymbol(name) ?
                        Symbolic.Origin.SYSTEM : Symbolic.Origin.UNDEFINED);
            }
            referred.add(symbolic);
            boolean substitute = symbolic.getValue() != null && (!inStreamData || scope.isExported(name));
            out.append(substitute ? symbolic.getValue() : "&" + name + (terminated ? "." : ""));
        }
        return referred.isEmpty() ? null : new ResolvedText(randomId(), out.toString(), referred);
    }

    private static boolean isSymbolCharacter(char c, boolean first) {
        return c == '@' || c == '#' || c == '$' || Character.isLetter(c) ||
               (!first && Character.isDigit(c));
    }

    private static boolean isSystemSymbol(String name) {
        String upper = name.toUpperCase(Locale.ROOT);
        return upper.startsWith("SYS") || SYSTEM_SYMBOLS.contains(upper);
    }

    /**
     * The value a {@code SET}, {@code PROC} or override assigns to a symbol, or null when it
     * assigns something that does not itself resolve. Apostrophes around a value are how one with
     * special characters in it is written and are not part of it, so a procedure that defaults
     * {@code CBLOPTS='APOST,RENT'} and uses it as {@code PARM='&CBLOPTS'} runs with one pair of
     * apostrophes rather than two.
     */
    private static @Nullable String assignedValue(Jcl.KeywordParameter parameter, Scope scope) {
        ResolvedText resolved = resolve(parameter.getValueText(), scope, false);
        if (resolved == null) {
            return unquoted(parameter.getValueText());
        }
        for (Symbolic symbolic : resolved.getSymbolics()) {
            if (symbolic.getValue() == null) {
                // Assigned from a symbol nothing set, so this one stands for nothing either, and
                // every use of it is left as it was written rather than filled in with half a name.
                return null;
            }
        }
        return unquoted(resolved.getText());
    }

    private static String unquoted(String value) {
        return value.length() > 1 && value.startsWith("'") && value.endsWith("'") ?
                value.substring(1, value.length() - 1) : value;
    }

    private static String valueOf(Jcl.KeywordParameter parameter, Scope scope) {
        return valueOf(parameter.getValueText(), scope);
    }

    private static String valueOf(String text, Scope scope) {
        ResolvedText resolved = resolve(text, scope, false);
        return resolved == null ? text : resolved.getText();
    }

    /**
     * The procedure a step calls, or null when it names a program. {@code EXEC MYPROC} and
     * {@code EXEC PROC=MYPROC} say the same thing.
     */
    private static @Nullable String procedureNameOf(Jcl.JobControlStatement exec, Scope scope) {
        Jcl.KeywordParameter proc = exec.getParameter("PROC");
        if (proc != null) {
            return valueOf(proc, scope);
        }
        Jcl.PositionalParameter positional = exec.getPositionalParameter();
        return positional == null ? null : valueOf(positional.getValueText(), scope);
    }

    private static List<Symbolic> overridesOf(Jcl.JobControlStatement exec, Scope scope) {
        List<Symbolic> overrides = new ArrayList<>();
        for (Parameter parameter : exec.getParameters()) {
            if (!(parameter instanceof Jcl.KeywordParameter)) {
                continue;
            }
            Jcl.KeywordParameter keyword = (Jcl.KeywordParameter) parameter;
            String name = keyword.getKeyword().getText();
            if (name.indexOf('.') >= 0 || EXEC_KEYWORDS.contains(name.toUpperCase(Locale.ROOT))) {
                continue;
            }
            overrides.add(new Symbolic(name, assignedValue(keyword, scope), Symbolic.Origin.OVERRIDE));
        }
        return overrides;
    }

    /**
     * The procedures written in this member between a {@code PROC} statement and its {@code PEND}.
     * They are searched before the library, the way z/OS searches them.
     */
    private static Map<String, Procedure> inStreamProcedures(List<Statement> statements) {
        Map<String, Procedure> procedures = new HashMap<>();
        for (int i = 0; i < statements.size(); i++) {
            if (!isOperation(statements.get(i), "PROC")) {
                continue;
            }
            Jcl.JobControlStatement proc = (Jcl.JobControlStatement) statements.get(i);
            if (proc.getSimpleName().isEmpty()) {
                continue;
            }
            List<Statement> body = new ArrayList<>();
            int end = i + 1;
            while (end < statements.size() && !isOperation(statements.get(end), "PEND")) {
                body.add(statements.get(end));
                end++;
            }
            procedures.put(proc.getSimpleName().toUpperCase(Locale.ROOT),
                    new Procedure(proc, body));
            i = end;
        }
        return procedures;
    }

    /**
     * A member of the supplied procedure library, read as a procedure: its {@code PROC} statement
     * supplies the defaults and the cards up to {@code PEND} are the body. A member written without
     * a {@code PROC} statement is all body, which is what a procedure with no symbols of its own
     * looks like.
     */
    private @Nullable Procedure catalogued(String name) {
        Jcl.CompilationUnit member = library.get(name.toUpperCase(Locale.ROOT));
        if (member == null) {
            return null;
        }
        List<Statement> statements = member.getStatements();
        Procedure own = inStreamProcedures(statements).get(name.toUpperCase(Locale.ROOT));
        if (own != null) {
            return own;
        }
        for (int i = 0; i < statements.size(); i++) {
            if (isOperation(statements.get(i), "PROC")) {
                List<Statement> body = new ArrayList<>();
                for (int j = i + 1; j < statements.size() && !isOperation(statements.get(j), "PEND"); j++) {
                    body.add(statements.get(j));
                }
                return new Procedure((Jcl.JobControlStatement) statements.get(i), body);
            }
        }
        return new Procedure(null, statements);
    }

    /**
     * The symbols a {@code PROC} statement defaults, read in the scope of the call. A default
     * written as the symbol itself — {@code HLQ=&HLQ} — is how a procedure says to take the
     * caller's value.
     */
    private static List<Symbolic> defaultsOf(Jcl.@Nullable JobControlStatement proc, Scope scope) {
        if (proc == null) {
            return emptyList();
        }
        List<Symbolic> defaults = new ArrayList<>();
        for (Parameter parameter : proc.getParameters()) {
            if (parameter instanceof Jcl.KeywordParameter) {
                Jcl.KeywordParameter keyword = (Jcl.KeywordParameter) parameter;
                defaults.add(new Symbolic(keyword.getKeyword().getText(),
                        assignedValue(keyword, scope), Symbolic.Origin.PROCEDURE));
            }
        }
        return defaults;
    }

    /**
     * The DD statements written under an {@code EXEC} that calls a procedure. Each is kept with the
     * cards that belong to it — the DDs concatenated onto it and any data written in the stream —
     * because an override that supplies a data set supplies its data too.
     */
    private static List<DdOverride> overridesFollowing(List<Statement> statements, int index) {
        List<DdOverride> overrides = new ArrayList<>();
        DdOverride current = null;
        for (int i = index + 1; i < statements.size(); i++) {
            Statement statement = statements.get(i);
            if (isOperation(statement, "EXEC") || isOperation(statement, "JOB") ||
                isOperation(statement, "PEND") || statement instanceof Jcl.NullStatement) {
                break;
            }
            if (isOperation(statement, "DD") &&
                !((Jcl.JobControlStatement) statement).getSimpleName().isEmpty()) {
                String name = ((Jcl.JobControlStatement) statement).getName().getText();
                name = name.startsWith("//") ? name.substring(2) : name;
                int dot = name.indexOf('.');
                current = new DdOverride(dot < 0 ? null : name.substring(0, dot),
                        dot < 0 ? name : name.substring(dot + 1), (Jcl.JobControlStatement) statement);
                overrides.add(current);
            } else if (current != null && !(statement instanceof Jcl.Expansion)) {
                current.following.add(statement);
            }
        }
        return overrides;
    }

    /**
     * The procedure's own cards with the caller's overrides applied: a DD the caller names again is
     * merged parameter by parameter, and one the procedure does not have is added to the step the
     * caller named. An unqualified override belongs to the one step of a one-step procedure, as
     * z/OS allows.
     */
    private static List<Statement> applyOverrides(List<Statement> body, List<DdOverride> overrides) {
        if (overrides.isEmpty()) {
            return body;
        }
        List<String> steps = stepNames(body);
        String onlyStep = steps.size() == 1 ? steps.get(0) : null;
        Set<DdOverride> applied = new HashSet<>();

        List<Statement> out = new ArrayList<>(body.size());
        String step = "";
        for (int i = 0; i < body.size(); i++) {
            Statement statement = body.get(i);
            if (isOperation(statement, "EXEC")) {
                addTo(out, step, overrides, applied, onlyStep);
                step = ((Jcl.JobControlStatement) statement).getSimpleName();
                out.add(statement);
                continue;
            }
            if (!isOperation(statement, "DD")) {
                out.add(statement);
                continue;
            }
            Jcl.JobControlStatement dd = (Jcl.JobControlStatement) statement;
            DdOverride override = find(overrides, step, dd.getSimpleName(), onlyStep);
            if (override == null) {
                out.add(dd);
                continue;
            }
            applied.add(override);
            out.add(merge(dd, override.statement));
            if (!override.following.isEmpty()) {
                // The caller supplied this DD's data, so the procedure's own is not read.
                out.addAll(freshen(override.following));
                while (i + 1 < body.size() && belongsToDataDefinition(body.get(i + 1))) {
                    i++;
                }
            }
        }
        addTo(out, step, overrides, applied, onlyStep);
        return out;
    }

    private static boolean belongsToDataDefinition(Statement statement) {
        return statement instanceof Jcl.DataDefinitionStream || statement instanceof Jcl.Delimiter ||
               (isOperation(statement, "DD") &&
                ((Jcl.JobControlStatement) statement).getSimpleName().isEmpty());
    }

    /**
     * The overrides for a step that named no DD the procedure has: z/OS adds them to the step.
     */
    private static void addTo(List<Statement> out, String step, List<DdOverride> overrides,
                              Set<DdOverride> applied, @Nullable String onlyStep) {
        if (step.isEmpty()) {
            return;
        }
        for (DdOverride override : overrides) {
            if (applied.contains(override) || !step.equalsIgnoreCase(named(override, onlyStep))) {
                continue;
            }
            applied.add(override);
            out.add(freshen(override.statement.withName(
                    override.statement.getName().withText("//" + override.ddName))));
            out.addAll(freshen(override.following));
        }
    }

    private static @Nullable DdOverride find(List<DdOverride> overrides, String step, String ddName,
                                           @Nullable String onlyStep) {
        for (DdOverride override : overrides) {
            if (step.equalsIgnoreCase(named(override, onlyStep)) &&
                override.ddName.equalsIgnoreCase(ddName)) {
                return override;
            }
        }
        return null;
    }

    private static @Nullable String named(DdOverride override, @Nullable String onlyStep) {
        return override.step == null ? onlyStep : override.step;
    }

    private static List<String> stepNames(List<Statement> body) {
        List<String> names = new ArrayList<>();
        for (Statement statement : body) {
            if (isOperation(statement, "EXEC")) {
                names.add(((Jcl.JobControlStatement) statement).getSimpleName());
            }
        }
        return names;
    }

    /**
     * The procedure's DD with the caller's parameters merged in: the procedure's order is kept, a
     * keyword the caller wrote takes the caller's value, and a keyword only the caller wrote is
     * added at the end. A caller that supplies a data source replaces the procedure's positional
     * parameter, which is how a {@code DD DUMMY} is turned into a real data set.
     * <p>
     * The merged card is written afresh on one line, so a procedure DD written over several lines
     * reads as one in the resolved listing.
     */
    private static Jcl.JobControlStatement merge(Jcl.JobControlStatement procedure,
                                                 Jcl.JobControlStatement override) {
        boolean replacesSource = false;
        List<String> values = new ArrayList<>();
        for (Parameter parameter : override.getParameters()) {
            replacesSource |= parameter instanceof Jcl.PositionalParameter;
        }
        replacesSource |= override.getParameter("DSN") != null || override.getParameter("DSNAME") != null ||
                          override.getParameter("SYSOUT") != null;

        for (Parameter parameter : (replacesSource ? override : procedure).getParameters()) {
            if (parameter instanceof Jcl.PositionalParameter) {
                values.add(((Jcl.PositionalParameter) parameter).getValueText());
            }
        }
        List<String> keywords = new ArrayList<>();
        for (Parameter parameter : procedure.getParameters()) {
            if (parameter instanceof Jcl.KeywordParameter) {
                Jcl.KeywordParameter keyword = (Jcl.KeywordParameter) parameter;
                Jcl.KeywordParameter replacement = override.getParameter(keyword.getKeyword().getText());
                keywords.add(keyword.getKeyword().getText());
                values.add(replacement == null ? keyword.getValueText() : replacement.getValueText());
            }
        }
        for (Parameter parameter : override.getParameters()) {
            if (parameter instanceof Jcl.KeywordParameter) {
                Jcl.KeywordParameter keyword = (Jcl.KeywordParameter) parameter;
                if (procedure.getParameter(keyword.getKeyword().getText()) == null) {
                    keywords.add(keyword.getKeyword().getText());
                    values.add(keyword.getValueText());
                }
            }
        }

        int positionals = values.size() - keywords.size();
        List<Jcl> operands = new ArrayList<>(values.size());
        for (int i = 0; i < values.size(); i++) {
            String text = values.get(i) + (i == values.size() - 1 ? "" : ",");
            Space prefix = operands.isEmpty() ? Space.build(" ") : Space.EMPTY;
            if (i < positionals) {
                operands.add(new Jcl.PositionalParameter(randomId(), prefix, Markers.EMPTY,
                        singletonList(word(text))));
            } else {
                operands.add(new Jcl.KeywordParameter(randomId(), prefix, Markers.EMPTY,
                        word(keywords.get(i - positionals)), singletonList(word("=" + text))));
            }
        }
        return freshen(procedure.withOperands(operands));
    }

    private static Jcl.Word word(String text) {
        return new Jcl.Word(randomId(), Space.EMPTY, Markers.EMPTY, text);
    }

    private static Statement mark(Statement statement, @Nullable Marker... markers) {
        Markers marked = statement.getMarkers();
        for (Marker marker : markers) {
            if (marker != null) {
                marked = marked.addIfAbsent(marker);
            }
        }
        return statement.withMarkers(marked);
    }

    /**
     * The first card of an expansion begins a line of its own, so the resolved listing reads as JCL
     * rather than running onto the card that called it.
     */
    private static List<Statement> onNewLine(List<Statement> statements) {
        return ListUtils.mapFirst(statements, s -> s.getPrefix().getWhitespace().indexOf('\n') < 0 ?
                s.withPrefix(Space.build("\n" + s.getPrefix().getWhitespace())) : s);
    }

    /**
     * A copy with new ids throughout. The same procedure is expanded into every job that calls it,
     * and two nodes with one id put a change on the wrong file.
     */
    private static <T extends Jcl> T freshen(T tree) {
        //noinspection unchecked
        return (T) new JclVisitor<Integer>() {
            @Override
            public @Nullable Jcl visit(@Nullable Tree tree, Integer p) {
                Jcl visited = super.visit(tree, p);
                return visited == null ? null : visited.withId(randomId());
            }

            @Override
            public <M extends Marker> M visitMarker(Marker marker, Integer p) {
                //noinspection unchecked
                return (M) marker.withId(randomId());
            }
        }.visit(tree, 0);
    }

    private static List<Statement> freshen(List<Statement> statements) {
        List<Statement> copies = new ArrayList<>(statements.size());
        for (Statement statement : statements) {
            copies.add(freshen(statement));
        }
        return copies;
    }

    private static boolean isOperation(Statement statement, String operation) {
        return statement instanceof Jcl.JobControlStatement &&
               ((Jcl.JobControlStatement) statement).isOperation(operation);
    }

    private static final class Scope {
        final Map<String, Symbolic> symbolics = new LinkedHashMap<>();
        final Set<String> exported = new LinkedHashSet<>();
        boolean exportAll;

        boolean isExported(String name) {
            return exportAll || exported.contains(name.toUpperCase(Locale.ROOT));
        }

        Scope copy() {
            Scope copy = new Scope();
            copy.symbolics.putAll(symbolics);
            copy.exported.addAll(exported);
            copy.exportAll = exportAll;
            return copy;
        }
    }

    private static final class Procedure {
        final Jcl.@Nullable JobControlStatement statement;
        final List<Statement> body;

        Procedure(Jcl.@Nullable JobControlStatement statement, List<Statement> body) {
            this.statement = statement;
            this.body = body;
        }
    }

    private static final class DdOverride {
        final @Nullable String step;
        final String ddName;
        final Jcl.JobControlStatement statement;
        final List<Statement> following = new ArrayList<>();

        DdOverride(@Nullable String step, String ddName, Jcl.JobControlStatement statement) {
            this.step = step;
            this.ddName = ddName;
            this.statement = statement;
        }
    }
}
