/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.cobol.search;

import org.openrewrite.*;
import org.openrewrite.cobol.CobolIsoVisitor;
import org.openrewrite.cobol.CobolPreprocessorIsoVisitor;
import org.openrewrite.cobol.markers.MissingCopybook;
import org.openrewrite.cobol.table.CobolRelationships;
import org.openrewrite.cobol.tree.Cobol;
import org.openrewrite.cobol.tree.CobolPreprocessor;
import org.openrewrite.cobol.tree.Name;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.marker.SearchResult;
import org.openrewrite.text.PlainText;
import org.openrewrite.text.PlainTextVisitor;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.MULTILINE;
import static org.openrewrite.cobol.table.CobolRelationships.ResourceAction.*;
import static org.openrewrite.cobol.table.CobolRelationships.ResourceType.*;

public class FindRelationships extends Recipe {
    transient CobolRelationships cobolRelationships = new CobolRelationships(this);

    @Override
    public String getDisplayName() {
        return "Find COBOL relationships";
    }

    @Override
    public String getDescription() {
        return "Build a list of relationships for diagramming and exploration.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {

        /*
         * Find relationships from CopyBooks.
         */
        class PreprocessorRelationshipVisitor extends CobolPreprocessorIsoVisitor<ExecutionContext> {
            String sourceName = "UNKNOWN";

            @Override
            public CobolPreprocessor.CompilationUnit visitCompilationUnit(CobolPreprocessor.CompilationUnit compilationUnit, ExecutionContext ctx) {
                sourceName = compilationUnit.getSourcePath().getFileName().toString();
                sourceName = sourceName.contains(".") ? sourceName.substring(0, sourceName.indexOf(".")) : sourceName;
                return super.visitCompilationUnit(compilationUnit, ctx);
            }

            @Override
            public CobolPreprocessor.CharDataSql visitCharDataSql(CobolPreprocessor.CharDataSql charDataSql, ExecutionContext ctx) {
                CobolPreprocessor.CharDataSql sql = super.visitCharDataSql(charDataSql, ctx);
                sql = getSqlRelationships(sql, sourceName, COPYBOOK, ctx);

                return sql;
            }

            public CobolPreprocessor.CharDataSql getSqlRelationships(CobolPreprocessor.CharDataSql sql, String sourceName, CobolRelationships.ResourceType dependentType, ExecutionContext ctx) {
                return sql.withCobols(ListUtils.map(sql.getCobols(), (i, c) -> {
                    if (c instanceof CobolPreprocessor.CharDataLine) {
                        CobolPreprocessor.CharDataLine line = (CobolPreprocessor.CharDataLine) c;
                        if (line.getWords().size() >= 4 &&
                                line.getWords().get(0) instanceof CobolPreprocessor.Word &&
                                "declare".equalsIgnoreCase(((CobolPreprocessor.Word) line.getWords().get(0)).getCobolWord().getWord())) {
                            return line.withWords(ListUtils.map(line.getWords(), (j, w) -> {
                                if (j == 1 && w instanceof CobolPreprocessor.Word &&
                                        !(line.getWords().get(2) instanceof CobolPreprocessor.Word &&
                                                "cursor".equalsIgnoreCase(((CobolPreprocessor.Word) line.getWords().get(2)).getCobolWord().getWord()))) {
                                    String tableName = ((CobolPreprocessor.Word) w).getCobolWord().getWord();
                                    cobolRelationships.insertRow(ctx,
                                            new CobolRelationships.Row(
                                                    sourceName,
                                                    dependentType,
                                                    DECLARE,
                                                    tableName,
                                                    SQL_TABLE,
                                                    false));
                                    return SearchResult.found(w);
                                }
                                return w;
                            }));
                        } else if (line.getWords().size() >= 2 &&
                                line.getWords().get(0) instanceof CobolPreprocessor.Word &&
                                "include".equalsIgnoreCase(((CobolPreprocessor.Word) line.getWords().get(0)).getCobolWord().getWord())) {
                            return line.withWords(ListUtils.map(line.getWords(), (j, w) -> {
                                if (j == 1 && w instanceof CobolPreprocessor.Word) {
                                    String tableName = ((CobolPreprocessor.Word) w).getCobolWord().getWord();
                                    cobolRelationships.insertRow(ctx,
                                            new CobolRelationships.Row(
                                                    sourceName,
                                                    dependentType,
                                                    INCLUDE,
                                                    tableName,
                                                    SQL_TABLE,
                                                    false));
                                    return SearchResult.found(w);
                                }
                                return w;
                            }));
                        }
                    }
                    return c;
                }));
            }
        }

        PreprocessorRelationshipVisitor preprocessorVisitor = new PreprocessorRelationshipVisitor();

        CobolIsoVisitor<ExecutionContext> cobolVisitor = new CobolIsoVisitor<ExecutionContext>() {
            final Set<String> seenCalls = new HashSet<>();
            final Set<String> seenCopies = new HashSet<>();

            String programName = "UNKNOWN";

            @Override
            public Cobol.ProgramIdParagraph visitProgramIdParagraph(Cobol.ProgramIdParagraph programIdParagraph, ExecutionContext executionContext) {
                Name rawName = programIdParagraph.getProgramName();
                if (rawName instanceof Cobol.Word) {
                    programName = ((Cobol.Word) rawName).getWord();
                }
                return super.visitProgramIdParagraph(programIdParagraph, executionContext);
            }

            @Override
            public Cobol.Word visitWord(Cobol.Word word, ExecutionContext ctx) {
                Cobol.Word w = super.visitWord(word, ctx);
                w = w.withPreprocessorStatements(ListUtils.map(w.getPreprocessorStatements(), ps -> {
                    if (ps instanceof CobolPreprocessor.CopyStatement) {
                        CobolPreprocessor.CopyStatement copyStatement = (CobolPreprocessor.CopyStatement) ps;
                        String copyName = copyStatement.getCopySource().getName().getCobolWord().getWord();
                        if(seenCopies.add(copyName)) {
                            cobolRelationships.insertRow(ctx,
                                    new CobolRelationships.Row(
                                            programName,
                                            COBOL,
                                            COPY,
                                            copyStatement.getCopySource().getName().getCobolWord().getWord(),
                                            COPYBOOK,
                                            copyStatement.getMarkers().findFirst(MissingCopybook.class).isPresent()));
                        }
                        return copyStatement.withCopySource(copyStatement.getCopySource().withName(
                                SearchResult.found(copyStatement.getCopySource().getName())));
                    } else if (ps instanceof CobolPreprocessor.ExecStatement) {
                        CobolPreprocessor.ExecStatement execStatement = (CobolPreprocessor.ExecStatement) ps;
                        if (execStatement.getCobol() instanceof CobolPreprocessor.CharDataSql &&
                                !((CobolPreprocessor.CharDataSql) execStatement.getCobol()).getCobols().isEmpty()) {
                            CobolPreprocessor.CharDataSql sql = (CobolPreprocessor.CharDataSql) execStatement.getCobol();
                            execStatement = execStatement.withCobol(preprocessorVisitor.getSqlRelationships(sql, programName, COBOL, ctx));
                            return execStatement;
                        }
                    }
                    return ps;
                }));
                return w;
            }

            @Override
            public Cobol.Call visitCall(Cobol.Call call, ExecutionContext ctx) {
                if (call.getIdentifier() instanceof Cobol.Word) {
                    Cobol.Word word = (Cobol.Word) call.getIdentifier();
                    if (word.getWord().startsWith("\"")) {
                        String callName = word.getWord().replace("\"", "");
                        if(seenCalls.add(callName)) {
                            cobolRelationships.insertRow(ctx,
                                    new CobolRelationships.Row(
                                            programName,
                                            COBOL,
                                            CALL,
                                            word.getWord().replace("\"", ""),
                                            COBOL,
                                            false
                                    )
                            );
                        }
                        return call.withIdentifier(SearchResult.found(call.getIdentifier()));
                    }
                }
                return super.visitCall(call, ctx);
            }
        };

        PlainTextVisitor<ExecutionContext> linkEditVisitor = new PlainTextVisitor<ExecutionContext>() {
            final Pattern includePattern = Pattern.compile("^INCLUDE\\s+(SYS|OBJ)LIB\\((?<include>[A-Z0-9]+)\\)", MULTILINE);
            @Override
            public PlainText visitText(PlainText pt, ExecutionContext ctx) {
                String text = pt.getText();
                Matcher m = includePattern.matcher(text);
                while(m.find()) {
                    String programName = m.group("include");
                    cobolRelationships.insertRow(ctx,
                            new CobolRelationships.Row(
                                    pt.getSourcePath().getFileName().toString(),
                                    LINKEDIT,
                                    INCLUDE,
                                    programName,
                                    COBOL,
                                    false));
                }
                return pt;
            }
        };

        PlainTextVisitor<ExecutionContext> bindCardVisitor = new PlainTextVisitor<ExecutionContext>() {
            final Pattern bindPattern = Pattern.compile("^BIND\\s+(?<keyword>PACKAGE|PLAN)\\((?<linkedit>[A-Z0-9]+)\\)\\s+OWNER\\(([A-Z0-9]+)\\)", MULTILINE);
            final Pattern memberPattern = Pattern.compile("\\s+MEMBER\\((?<member>[A-Z0-9]+)\\)", MULTILINE);
            @Override
            public PlainText visitText(PlainText pt, ExecutionContext ctx) {
                String text = pt.getText();
                Matcher m = bindPattern.matcher(text);
                if(m.find()) {
                    String packageOrPlan = m.group("keyword");
                    if("PLAN".equals(packageOrPlan)) {
                        String linkedit = m.group("linkedit");
                        cobolRelationships.insertRow(ctx,
                                new CobolRelationships.Row(
                                        pt.getSourcePath().getFileName().toString(),
                                        BINDPLAN,
                                        PLAN,
                                        linkedit,
                                        LINKEDIT,
                                        false));
                    } else {
                        Matcher memberMatcher = memberPattern.matcher(text);
                        while(memberMatcher.find()) {
                            String member = memberMatcher.group("member");
                            cobolRelationships.insertRow(ctx,
                                    new CobolRelationships.Row(
                                            pt.getSourcePath().getFileName().toString(),
                                            BINDPACKAGE,
                                            MEMBER,
                                            member,
                                            COBOL,
                                            false));
                        }
                    }
                }
                return pt;
            }
        };

        return new TreeVisitor<Tree, ExecutionContext>() {
            @Override
            public Tree preVisit(Tree tree, ExecutionContext ctx) {
                stopAfterPreVisit();
                Tree t = tree;
                if(tree instanceof Cobol) {
                    t = cobolVisitor.visit(t, ctx);
                } else if(tree instanceof PlainText) {
                    t = linkEditVisitor.visit(t, ctx);
                    t = bindCardVisitor.visit(t, ctx);
                } else if (tree instanceof CobolPreprocessor) {
                    t = preprocessorVisitor.visit(t, ctx);
                }
                return t;
            }
        };
    }

}
