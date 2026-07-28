/*
 * Copyright 2025 the original author or authors.
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
package org.openrewrite.cobol.search;

import lombok.Getter;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.cobol.CobolIsoVisitor;
import org.openrewrite.cobol.CobolPreprocessorIsoVisitor;
import org.openrewrite.cobol.marker.CopiedStatement;
import org.openrewrite.cobol.marker.MissingCopybook;
import org.openrewrite.cobol.table.CobolRelationships;
import org.openrewrite.cobol.tree.Cobol;
import org.openrewrite.cobol.tree.CobolPreprocessor;
import org.openrewrite.cobol.tree.Name;
import org.openrewrite.controlm.ControlMIsoVisitor;
import org.openrewrite.controlm.tree.ControlM;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.internal.StringUtils;
import org.openrewrite.marker.SearchResult;
import org.openrewrite.text.PlainText;
import org.openrewrite.text.PlainTextVisitor;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.MULTILINE;
import static org.openrewrite.cobol.table.CobolRelationships.ResourceAction.*;
import static org.openrewrite.cobol.table.CobolRelationships.ResourceType.*;

public class FindRelationships extends Recipe {
    transient CobolRelationships cobolRelationships = new CobolRelationships(this);

	@Getter
	final String displayName = "Find COBOL relationships";

	@Getter
	final String description = "Build a list of relationships for diagramming and exploration.";

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {

        CobolPreprocessorIsoVisitor<ExecutionContext> preprocessorVisitor = new CobolPreprocessorIsoVisitor<ExecutionContext>() {
            final Set<String> seenIncludes = new HashSet<>();
            final Set<String> seenCursorAccess = new HashSet<>();
            final Set<String> seenTableAccess = new HashSet<>();
            String sourceName = "UNKNOWN";
            boolean isSourceName;

            @Override
            public CobolPreprocessor.CompilationUnit visitCompilationUnit(CobolPreprocessor.CompilationUnit compilationUnit, ExecutionContext ctx) {
                sourceName = compilationUnit.getSourcePath().getFileName().toString();
                sourceName = sourceName.contains(".") ? sourceName.substring(0, sourceName.indexOf(".")) : sourceName;
                isSourceName = true;
                return super.visitCompilationUnit(compilationUnit, ctx);
            }

            @Override
            public CobolPreprocessor.Copybook visitCopybook(CobolPreprocessor.Copybook copybook, ExecutionContext ctx) {
                if (!isSourceName) {
                    sourceName = copybook.getSourcePath().getFileName().toString();
                    sourceName = sourceName.contains(".") ? sourceName.substring(0, sourceName.indexOf(".")) : sourceName;
                }
                return super.visitCopybook(copybook, ctx);
            }

            @Override
            public CobolPreprocessor.ExecSqlIncludeStatement visitExecSqlIncludeStatement(CobolPreprocessor.ExecSqlIncludeStatement execSqlIncludeStatement, ExecutionContext ctx) {
                String copybookName = execSqlIncludeStatement.getCopySource().getCobolWord().getWord();
                if (seenIncludes.add(copybookName)) {
                    cobolRelationships.insertRow(ctx,
                            new CobolRelationships.Row(
                                    sourceName,
                                    COPYBOOK,
                                    INCLUDE,
                                    copybookName,
                                    COPYBOOK,
                                    false,
                                    ""
                            )
                    );
                    return execSqlIncludeStatement.withCopySource(SearchResult.found(execSqlIncludeStatement.getCopySource()));
                }
                return super.visitExecSqlIncludeStatement(execSqlIncludeStatement, ctx);
            }

            @Override
            public CobolPreprocessor.CharDataSql visitCharDataSql(CobolPreprocessor.CharDataSql charDataSql, ExecutionContext ctx) {
                CobolPreprocessor.CharDataSql sql = super.visitCharDataSql(charDataSql, ctx);
                return getSqlRelationships(sql, sourceName, COPYBOOK, seenIncludes, seenCursorAccess, seenTableAccess, ctx);
            }
        };

        CobolIsoVisitor<ExecutionContext> cobolVisitor = new CobolIsoVisitor<ExecutionContext>() {
            final Set<String> seenCopies = new HashSet<>();
            final Set<String> seenCalls = new HashSet<>();
            final Set<String> seenIncludes = new HashSet<>();
            final Set<String> seenCursorAccess = new HashSet<>();
            final Set<String> seenTableAccess = new HashSet<>();
            String programName = "UNKNOWN";

            @Override
            public Cobol.ProgramIdParagraph visitProgramIdParagraph(Cobol.ProgramIdParagraph programIdParagraph, ExecutionContext ctx) {
                Name rawName = programIdParagraph.getProgramName();
                if (rawName instanceof Cobol.Word) {
                    programName = ((Cobol.Word) rawName).getWord();
                }
                return super.visitProgramIdParagraph(programIdParagraph, ctx);
            }

            @Override
            public Cobol.Word visitWord(Cobol.Word word, ExecutionContext ctx) {
                Cobol.Word w = super.visitWord(word, ctx);
                return w.withPreprocessorStatements(ListUtils.map(w.getPreprocessorStatements(), ps -> {
					if (ps instanceof CobolPreprocessor.CopyStatement) {
						CobolPreprocessor.CopyStatement copyStatement = (CobolPreprocessor.CopyStatement) ps;
						String copyName = copyStatement.getCopySource().getName().getCobolWord().getWord();
						if (seenCopies.add(copyName)) {
							Optional<CopiedStatement> cs = copyStatement.getMarkers().findFirst(CopiedStatement.class);
							cobolRelationships.insertRow(ctx,
								new CobolRelationships.Row(
									cs.isPresent() && StringUtils.isNotEmpty(cs.get().getSourceCopybook()) ? cs.get().getSourceCopybook() : programName,
									cs.isPresent() && StringUtils.isNotEmpty(cs.get().getSourceCopybook()) ? COPYBOOK : COBOL,
									COPY,
									copyStatement.getCopySource().getName().getCobolWord().getWord(),
									COPYBOOK,
									copyStatement.getMarkers().findFirst(MissingCopybook.class).isPresent(),
									""
								)
							);
						}
						return copyStatement.withCopySource(copyStatement.getCopySource().withName(
							SearchResult.found(copyStatement.getCopySource().getName())));
					}
					if (ps instanceof CobolPreprocessor.ExecSqlIncludeStatement) {
						CobolPreprocessor.ExecSqlIncludeStatement includeStatement = (CobolPreprocessor.ExecSqlIncludeStatement) ps;
						if (includeStatement.getCopybook() != null) {
							String copyName = includeStatement.getCopySource().getCobolWord().getWord();
							if (seenCopies.add(copyName)) {
								Optional<CopiedStatement> cs = includeStatement.getMarkers().findFirst(CopiedStatement.class);
								cobolRelationships.insertRow(ctx,
									new CobolRelationships.Row(
										cs.isPresent() && StringUtils.isNotEmpty(cs.get().getSourceCopybook()) ? cs.get().getSourceCopybook() : programName,
										cs.isPresent() && StringUtils.isNotEmpty(cs.get().getSourceCopybook()) ? COPYBOOK : COBOL,
										INCLUDE,
										includeStatement.getCopySource().getCobolWord().getWord(),
										COPYBOOK,
										includeStatement.getMarkers().findFirst(MissingCopybook.class).isPresent(),
										""
									)
								);
							}
							return includeStatement.withCopySource(SearchResult.found(includeStatement.getCopySource()));
						}
					}
					else if (ps instanceof CobolPreprocessor.ExecStatement) {
						CobolPreprocessor.ExecStatement execStatement = (CobolPreprocessor.ExecStatement) ps;
						if (execStatement.getCobol() instanceof CobolPreprocessor.CharDataSql &&
							!((CobolPreprocessor.CharDataSql) execStatement.getCobol()).getCobols().isEmpty()) {
							CobolPreprocessor.CharDataSql sql = (CobolPreprocessor.CharDataSql) execStatement.getCobol();
							return execStatement.withCobol(getSqlRelationships(sql, programName, COBOL, seenIncludes, seenCursorAccess, seenTableAccess, ctx));
						}
					}
					return ps;
				}));
            }

            @Override
            public Cobol.Call visitCall(Cobol.Call call, ExecutionContext ctx) {
                if (call.getIdentifier() instanceof Cobol.Word) {
                    Cobol.Word word = (Cobol.Word) call.getIdentifier();
                    if (word.getWord().startsWith("\"")) {
                        String callName = word.getWord().replace("\"", "");
                        if (seenCalls.add(callName)) {
                            cobolRelationships.insertRow(ctx,
                                    new CobolRelationships.Row(
                                            programName,
                                            COBOL,
                                            CALL,
                                            word.getWord().replace("\"", ""),
                                            COBOL,
                                            false,
                                            ""
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
                while (m.find()) {
                    String programName = m.group("include");
                    cobolRelationships.insertRow(ctx,
                            new CobolRelationships.Row(
                                    pt.getSourcePath().getFileName().toString(),
                                    LINKEDIT,
                                    INCLUDE,
                                    programName,
                                    COBOL,
                                    false,
                                    ""
                            )
                    );
                }
                return pt;
            }
        };

        PlainTextVisitor<ExecutionContext> bindCardVisitor = new PlainTextVisitor<ExecutionContext>() {
            final Pattern bindPattern = Pattern.compile("^BIND\\s+(?<keyword>PACKAGE|PLAN)\\((?<linkedit>[&.A-Z0-9]+)\\)\\s+OWNER\\(([&A-Z0-9]+)\\)", MULTILINE);
            final Pattern memberPattern = Pattern.compile("\\s+MEMBER\\((?<member>[A-Z0-9]+)\\)", MULTILINE);

            @Override
            public PlainText visitText(PlainText pt, ExecutionContext ctx) {
                String text = pt.getText();
                Matcher m = bindPattern.matcher(text);
                if (m.find()) {
                    String packageOrPlan = m.group("keyword");
                    if ("PLAN".equals(packageOrPlan)) {
                        String linkedit = m.group("linkedit");
                        cobolRelationships.insertRow(ctx,
                                new CobolRelationships.Row(
                                        pt.getSourcePath().getFileName().toString(),
                                        BINDPLAN,
                                        PLAN,
                                        linkedit,
                                        LINKEDIT,
                                        false,
                                        ""
                                )
                        );
                    } else {
                        Matcher memberMatcher = memberPattern.matcher(text);
                        while (memberMatcher.find()) {
                            String member = memberMatcher.group("member");
                            cobolRelationships.insertRow(ctx,
                                    new CobolRelationships.Row(
                                            pt.getSourcePath().getFileName().toString(),
                                            BINDPACKAGE,
                                            MEMBER,
                                            member,
                                            COBOL,
                                            false,
                                            ""
                                    )
                            );
                        }
                    }
                }
                return pt;
            }
        };

        ControlMIsoVisitor<ExecutionContext> controlMVisitor = new ControlMIsoVisitor<ExecutionContext>() {
            String sourceName = "UNKNOWN";
            @Override
            public ControlM.CompilationUnit visitCompilationUnit(ControlM.CompilationUnit compilationUnit, ExecutionContext ctx) {
                sourceName = compilationUnit.getSourcePath().getFileName().toString();
                sourceName = sourceName.contains(".") ? sourceName.substring(0, sourceName.indexOf(".")) : sourceName;
                return super.visitCompilationUnit(compilationUnit, ctx);
            }

            @Override
            public ControlM.DefinitionSection visitDefinitionSection(ControlM.DefinitionSection definitionSection, ExecutionContext ctx) {
                boolean isValid = isValidSchedule(definitionSection);
                if (!isValid) {
                    return definitionSection;
                }

                ControlM.DefinitionSection d = super.visitDefinitionSection(definitionSection, ctx);
                return d.withLines(ListUtils.map(d.getLines(), (i, it) -> {
                    if (i == 0 && it instanceof ControlM.Line) {
                        ControlM.Line line = (ControlM.Line) it;
                        return line.withParameters(ListUtils.map(line.getParameters(), (j, param) -> {
                            if (j == 0) {
                                ControlM.Parameter p = (ControlM.Parameter) param;
                                cobolRelationships.insertRow(ctx,
                                        new CobolRelationships.Row(
                                                sourceName,
                                                CONTROL_M_SCHEDULE,
                                                TRIGGERS,
                                                p.getValue().getText(),
                                                JCL,
                                                false,
                                                ""
                                        )
                                );
                                return p.withValue(SearchResult.found(p.getValue()));
                            }
                            return param;
                        }));
                    }
                    return it;
                }));
            }

            private boolean isValidSchedule(ControlM.DefinitionSection d) {
                return !d.getLines().isEmpty() && d.getLines().get(0) instanceof ControlM.Line &&
                        ((ControlM.Line) d.getLines().get(0)).getParameters().size() == 2 &&
                        ((ControlM.Line) d.getLines().get(0)).getParameters().get(1) instanceof ControlM.Parameter &&
                        ((ControlM.Parameter) ((ControlM.Line) d.getLines().get(0)).getParameters().get(0)).getValue() != null &&
                        ((ControlM.Parameter) ((ControlM.Line) d.getLines().get(0)).getParameters().get(1)).getValue() != null &&
                        !"DUMMY".equalsIgnoreCase(Objects.requireNonNull(((ControlM.Parameter) ((ControlM.Line) d.getLines().get(0)).getParameters().get(1)).getValue()).getText());
            }

            @Override
            public ControlM.Input visitInput(ControlM.Input input, ExecutionContext ctx) {
                ControlM.Input i = super.visitInput(input, ctx);
                if (!i.getInput().isEmpty() && i.getInput().get(0) instanceof ControlM.Input.NameParameter) {
                    i = i.withInput(ListUtils.map(i.getInput(), it -> {
                        if (it instanceof ControlM.Input.NameParameter) {
                            ControlM.Input.NameParameter nameParameter = (ControlM.Input.NameParameter) it;
                            if (nameParameter.getName() != null && nameParameter.getName().getText().contains("_")) {
                                String[] parts = nameParameter.getName().getText().split("_");
                                if (parts.length == 3) {
                                    cobolRelationships.insertRow(ctx,
                                            new CobolRelationships.Row(
                                                    parts[1],
                                                    CONTROL_M_SCHEDULE,
                                                    TRIGGERS,
                                                    sourceName,
                                                    CONTROL_M_SCHEDULE,
                                                    false,
                                                    ""
                                            )
                                    );
                                }
                                return nameParameter.withName(SearchResult.found(nameParameter.getName()));
                            }
                        }
                        return it;
                    }));
                }
                return i;
            }
        };

        return new TreeVisitor<Tree, ExecutionContext>() {
            @Override
            public Tree preVisit(Tree tree, ExecutionContext ctx) {
                stopAfterPreVisit();
                Tree t = tree;
                if (tree instanceof Cobol) {
                    t = cobolVisitor.visit(t, ctx);
                } else if (tree instanceof PlainText) {
                    t = linkEditVisitor.visit(t, ctx);
                    t = bindCardVisitor.visit(t, ctx);
                } else if (tree instanceof CobolPreprocessor) {
                    t = preprocessorVisitor.visit(t, ctx);
                } else if (tree instanceof ControlM) {
                    t = controlMVisitor.visit(t, ctx);
                }
                return t;
            }
        };
    }

    public CobolPreprocessor.CharDataSql getSqlRelationships(
            CobolPreprocessor.CharDataSql sql,
            String sourceName,
            CobolRelationships.ResourceType dependentType,
            Set<String> seenIncludes,
            Set<String> cursorNames,
            Set<String> seenTableAccess,
            ExecutionContext ctx) {
        return sql.withCobols(ListUtils.map(sql.getCobols(), (i, c) -> {
            if (c instanceof CobolPreprocessor.CharDataLine) {
                CobolPreprocessor.CharDataLine line = (CobolPreprocessor.CharDataLine) c;
                AtomicBoolean tableNameNext = new AtomicBoolean(false);
                AtomicReference<CobolRelationships.Row> cursorTo = new AtomicReference<>(null);
                return line.withWords(ListUtils.map(line.getWords(), (j, w) -> {
                    if (w instanceof CobolPreprocessor.Word) {
                        CobolPreprocessor.Word word = (CobolPreprocessor.Word) w;
                        // TODO: include condition is for backwards compatibility and may be removed after new LSTs are generated.
                        if ("include".equalsIgnoreCase(word.getCobolWord().getWord()) &&
                                j == 0 && 2 <= line.getWords().size() && line.getWords().get(1) instanceof CobolPreprocessor.Word) {
                            String copybookName = ((CobolPreprocessor.Word) line.getWords().get(1)).getCobolWord().getWord();
                            if (seenIncludes.add(copybookName)) {
                                cobolRelationships.insertRow(ctx,
                                        new CobolRelationships.Row(
                                                sourceName,
                                                dependentType,
                                                INCLUDE,
                                                copybookName,
                                                COPYBOOK,
                                                false,
                                                ""
                                        )
                                );
                                return SearchResult.found(word);
                            }
                        } else if ("update".equalsIgnoreCase(word.getCobolWord().getWord()) &&
                                j == 0 && 2 < line.getWords().size() && line.getWords().get(1) instanceof CobolPreprocessor.Word) {
                            String tableName = ((CobolPreprocessor.Word) line.getWords().get(1)).getCobolWord().getWord();
                            if (seenTableAccess.add(tableName + "_UPDATE") && !cursorNames.contains(tableName)) {
                                cobolRelationships.insertRow(ctx,
                                        new CobolRelationships.Row(
                                                sourceName,
                                                dependentType,
                                                ACCESS,
                                                tableName,
                                                SQL_TABLE,
                                                false,
                                                "UPDATE"));
                                return SearchResult.found(word);
                            }
                        } else if ("insert".equalsIgnoreCase(word.getCobolWord().getWord()) &&
                                j == 0 && 3 < line.getWords().size() && line.getWords().get(2) instanceof CobolPreprocessor.Word) {
                            String tableName = ((CobolPreprocessor.Word) line.getWords().get(2)).getCobolWord().getWord();
                            if (seenTableAccess.add(tableName + "_INSERT") && !cursorNames.contains(tableName)) {
                                cobolRelationships.insertRow(ctx,
                                        new CobolRelationships.Row(
                                                sourceName,
                                                dependentType,
                                                ACCESS,
                                                tableName,
                                                SQL_TABLE,
                                                false,
                                                "INSERT"
                                        )
                                );
                                return SearchResult.found(word);
                            }
                        } else if ("from".equalsIgnoreCase(word.getCobolWord().getWord()) || "join".equalsIgnoreCase(word.getCobolWord().getWord())) {
                            tableNameNext.set(true);
                        } else if (tableNameNext.get()) {
                            tableNameNext.set(false);
                            String metadata = j - 2 >= 0 && line.getWords().get(j - 2) instanceof CobolPreprocessor.Word &&
                                    "delete".equalsIgnoreCase(((CobolPreprocessor.Word) line.getWords().get(j - 2)).getCobolWord().getWord()) ?
                                    "DELETE" : "READ";
                            String tableName = word.getCobolWord().getWord();
                            if (cursorTo.get() != null) {
                                cursorTo.set(null);
                            } else if (seenTableAccess.add(tableName + "_" + metadata) && !cursorNames.contains(tableName)) {
                                cobolRelationships.insertRow(ctx,
                                        new CobolRelationships.Row(
                                                sourceName,
                                                dependentType,
                                                ACCESS,
                                                tableName,
                                                SQL_TABLE,
                                                false,
                                                metadata
                                        )
                                );
                                return SearchResult.found(word);
                            }
                        } else if ("declare".equalsIgnoreCase(word.getCobolWord().getWord()) &&
                                j + 2 < line.getWords().size() &&
                                line.getWords().get(j + 1) instanceof CobolPreprocessor.Word &&
                                line.getWords().get(j + 2) instanceof CobolPreprocessor.Word) {
                            String tableName = ((CobolPreprocessor.Word) line.getWords().get(j + 1)).getCobolWord().getWord();
                            if (seenTableAccess.add(tableName + "_CREATE")) {
                                if ("table".equalsIgnoreCase(((CobolPreprocessor.Word) line.getWords().get(j + 2)).getCobolWord().getWord())) {
                                    cobolRelationships.insertRow(ctx,
                                            new CobolRelationships.Row(
                                                    sourceName,
                                                    dependentType,
                                                    ACCESS,
                                                    tableName,
                                                    SQL_TABLE,
                                                    false,
                                                    "CREATE"
                                            )
                                    );
                                } else {
                                    cursorNames.add(tableName);
                                }
                                return SearchResult.found(word);
                            }
                        }
                    }
                    return w;
                }));
            }
            return c;
        }));
    }
}
