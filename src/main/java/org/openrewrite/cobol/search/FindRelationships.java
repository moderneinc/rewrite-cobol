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
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.PrintOutputCapture;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.cobol.CobolIsoVisitor;
import org.openrewrite.cobol.CobolPreprocessorIsoVisitor;
import org.openrewrite.cobol.SourcePositions;
import org.openrewrite.cobol.marker.CopiedStatement;
import org.openrewrite.cobol.marker.MissingCopybook;
import org.openrewrite.cobol.table.CobolRelationships;
import org.openrewrite.cobol.tree.Cobol;
import org.openrewrite.cobol.tree.CobolPreprocessor;
import org.openrewrite.cobol.tree.Name;
import org.openrewrite.controlcard.InStreamCards;
import org.openrewrite.controlcard.idcams.IdcamsIsoVisitor;
import org.openrewrite.controlcard.idcams.IdcamsLineReader;
import org.openrewrite.controlcard.idcams.IdcamsParser;
import org.openrewrite.controlcard.idcams.trait.IdcamsCommand;
import org.openrewrite.controlcard.idcams.tree.Idcams;
import org.openrewrite.controlm.ControlMIsoVisitor;
import org.openrewrite.controlm.internal.ControlMPrinter;
import org.openrewrite.controlm.tree.ControlM;
import org.openrewrite.db2.bind.BindIsoVisitor;
import org.openrewrite.db2.bind.InStreamBindDeck;
import org.openrewrite.db2.bind.trait.BindCommand;
import org.openrewrite.db2.bind.tree.Bind;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.internal.StringUtils;
import org.openrewrite.jcl.JclIsoVisitor;
import org.openrewrite.jcl.tree.Jcl;
import org.openrewrite.linkedit.InStreamLinkEditDeck;
import org.openrewrite.linkedit.LinkEditIsoVisitor;
import org.openrewrite.linkedit.trait.LinkEditDeck;
import org.openrewrite.linkedit.tree.LinkEdit;
import org.openrewrite.marker.Range;
import org.openrewrite.marker.SearchResult;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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
            String sourcePath = "";
            boolean isSourceName = false;
            @Nullable SourcePositions positions;

            @Override
            public CobolPreprocessor.CompilationUnit visitCompilationUnit(CobolPreprocessor.CompilationUnit compilationUnit, ExecutionContext ctx) {
                sourceName = memberName(compilationUnit.getSourcePath());
                sourcePath = compilationUnit.getSourcePath().toString();
                positions = SourcePositions.of(compilationUnit);
                isSourceName = true;
                return super.visitCompilationUnit(compilationUnit, ctx);
            }

            @Override
            public CobolPreprocessor.Copybook visitCopybook(CobolPreprocessor.Copybook copybook, ExecutionContext ctx) {
                if (!isSourceName) {
                    sourceName = memberName(copybook.getSourcePath());
                    sourcePath = copybook.getSourcePath().toString();
                    positions = SourcePositions.of(copybook);
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
                                    "",
                                    sourcePath,
                                    lineOf(positions, execSqlIncludeStatement),
                                    pathOf(execSqlIncludeStatement.getCopybook()),
                                    null
                            )
                    );
                    return execSqlIncludeStatement.withCopySource(SearchResult.found(execSqlIncludeStatement.getCopySource()));
                }
                return super.visitExecSqlIncludeStatement(execSqlIncludeStatement, ctx);
            }

            @Override
            public CobolPreprocessor.CharDataSql visitCharDataSql(CobolPreprocessor.CharDataSql charDataSql, ExecutionContext ctx) {
                CobolPreprocessor.CharDataSql sql = super.visitCharDataSql(charDataSql, ctx);
                return getSqlRelationships(sql, sourceName, COPYBOOK, sourcePath, positions,
                        seenIncludes, seenCursorAccess, seenTableAccess, ctx);
            }
        };

        CobolIsoVisitor<ExecutionContext> cobolVisitor = new CobolIsoVisitor<ExecutionContext>() {
            final Set<String> seenCopies = new HashSet<>();
            final Set<String> seenCalls = new HashSet<>();
            final Set<String> seenIncludes = new HashSet<>();
            final Set<String> seenCursorAccess = new HashSet<>();
            final Set<String> seenTableAccess = new HashSet<>();
            String programName = "UNKNOWN";
            String sourcePath = "";
            @Nullable SourcePositions positions;

            @Override
            public Cobol.CompilationUnit visitCompilationUnit(Cobol.CompilationUnit compilationUnit, ExecutionContext ctx) {
                sourcePath = compilationUnit.getSourcePath().toString();
                positions = SourcePositions.of(compilationUnit);
                return super.visitCompilationUnit(compilationUnit, ctx);
            }

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
							String fromCopybook = cs.map(CopiedStatement::getSourceCopybook).orElse("");
							boolean copied = StringUtils.isNotEmpty(fromCopybook);
							boolean missing = copyStatement.getMarkers().findFirst(MissingCopybook.class).isPresent();
							cobolRelationships.insertRow(ctx,
								new CobolRelationships.Row(
									copied ? fromCopybook : programName,
									copied ? COPYBOOK : COBOL,
									COPY,
									copyStatement.getCopySource().getName().getCobolWord().getWord(),
									COPYBOOK,
									missing,
									"",
									copied ? null : sourcePath,
									lineOf(positions, copyStatement),
									missing ? null : pathOf(copyStatement.getCopybook()),
									null
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
								String fromCopybook = cs.map(CopiedStatement::getSourceCopybook).orElse("");
								boolean copied = StringUtils.isNotEmpty(fromCopybook);
								boolean missing = includeStatement.getMarkers().findFirst(MissingCopybook.class).isPresent();
								cobolRelationships.insertRow(ctx,
									new CobolRelationships.Row(
										copied ? fromCopybook : programName,
										copied ? COPYBOOK : COBOL,
										INCLUDE,
										includeStatement.getCopySource().getCobolWord().getWord(),
										COPYBOOK,
										missing,
										"",
										copied ? null : sourcePath,
										lineOf(positions, includeStatement),
										missing ? null : pathOf(includeStatement.getCopybook()),
										null
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
							return execStatement.withCobol(getSqlRelationships(sql, programName, COBOL, sourcePath, positions, seenIncludes, seenCursorAccess, seenTableAccess, ctx));
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
                                            "",
                                            sourcePath,
                                            lineOf(positions, call),
                                            null,
                                            null
                                    )
                            );
                        }
                        return call.withIdentifier(SearchResult.found(call.getIdentifier()));
                    }
                }
                return super.visitCall(call, ctx);
            }
        };

        LinkEditIsoVisitor<ExecutionContext> linkEditVisitor = new LinkEditIsoVisitor<ExecutionContext>() {
            @Override
            public LinkEdit.CompilationUnit visitCompilationUnit(LinkEdit.CompilationUnit cu, ExecutionContext ctx) {
                linkEditRelationships(cu, memberName(cu.getSourcePath()), LINKEDIT,
                        cu.getSourcePath().toString(), 0, ctx);
                return cu;
            }
        };

        BindIsoVisitor<ExecutionContext> bindCardVisitor = new BindIsoVisitor<ExecutionContext>() {
            @Override
            public Bind.CompilationUnit visitCompilationUnit(Bind.CompilationUnit cu, ExecutionContext ctx) {
                bindRelationships(cu, memberName(cu.getSourcePath()), CONTROL_CARD,
                        cu.getSourcePath().toString(), 0, ctx);
                return cu;
            }
        };

        IdcamsIsoVisitor<ExecutionContext> idcamsCardVisitor = new IdcamsIsoVisitor<ExecutionContext>() {
            @Override
            public Idcams.CompilationUnit visitCompilationUnit(Idcams.CompilationUnit cu, ExecutionContext ctx) {
                idcamsRelationships(cu, memberName(cu.getSourcePath()), CONTROL_CARD,
                        cu.getSourcePath().toString(), 0, ctx);
                return cu;
            }
        };

        JclIsoVisitor<ExecutionContext> jclVisitor = new JclIsoVisitor<ExecutionContext>() {
            @Override
            public Jcl.CompilationUnit visitCompilationUnit(Jcl.CompilationUnit cu, ExecutionContext ctx) {
                for (InStreamBindDeck stream : InStreamBindDeck.of(cu)) {
                    bindRelationships(stream.getDeck(), memberName(cu.getSourcePath()), JCL,
                            cu.getSourcePath().toString(), stream.getLine() - 1, ctx);
                }
                for (InStreamLinkEditDeck stream : InStreamLinkEditDeck.of(cu)) {
                    linkEditRelationships(stream.getDeck(), memberName(cu.getSourcePath()), JCL,
                            cu.getSourcePath().toString(), stream.getLine() - 1, ctx);
                }
                for (InStreamCards cards : InStreamCards.of(cu)) {
                    if (IdcamsLineReader.isIdcamsDeck(cards.getText())) {
                        idcamsRelationships(IdcamsParser.parse(cu.getSourcePath(), cards.getText()),
                                memberName(cu.getSourcePath()), JCL, cu.getSourcePath().toString(),
                                cards.getLine() - 1, ctx);
                    }
                }
                return cu;
            }
        };

        ControlMIsoVisitor<ExecutionContext> controlMVisitor = new ControlMIsoVisitor<ExecutionContext>() {
            final Map<UUID, Integer> wordLines = new HashMap<>();
            String sourceName = "UNKNOWN";
            String sourcePath = "";

            @Override
            public ControlM.CompilationUnit visitCompilationUnit(ControlM.CompilationUnit compilationUnit, ExecutionContext ctx) {
                sourceName = memberName(compilationUnit.getSourcePath());
                sourcePath = compilationUnit.getSourcePath().toString();
                wordLines.clear();
                wordLines.putAll(wordLines(compilationUnit));
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
                                                "",
                                                sourcePath,
                                                wordLines.get(p.getValue().getId()),
                                                null,
                                                null
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
                                                    "",
                                                    null,
                                                    null,
                                                    sourcePath,
                                                    wordLines.get(nameParameter.getName().getId())
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
                } else if (tree instanceof LinkEdit) {
                    t = linkEditVisitor.visit(t, ctx);
                } else if (tree instanceof Bind) {
                    t = bindCardVisitor.visit(t, ctx);
                } else if (tree instanceof Idcams) {
                    t = idcamsCardVisitor.visit(t, ctx);
                } else if (tree instanceof Jcl) {
                    t = jclVisitor.visit(t, ctx);
                } else if (tree instanceof CobolPreprocessor) {
                    t = preprocessorVisitor.visit(t, ctx);
                } else if (tree instanceof ControlM) {
                    t = controlMVisitor.visit(t, ctx);
                }
                return t;
            }
        };
    }

    /**
     * The chain a bind deck writes down, which nothing else in an estate does: the deck declares a
     * plan or a package, the plan lists the packages it may run, and a package is bound from the DBRM
     * a DB2 precompile left under the program's own name. A {@code REBIND} declares nothing — it names
     * objects that already exist, so it is a reference to them.
     *
     * @param deckType   what the deck is, which differs by where it was written: a {@code CARDLIB}
     *                   member is a control card, a deck written in-stream is the job.
     * @param lineOffset how many lines of the source come before the deck's first card, which is
     *                   nothing for a member of its own and the SYSTSIN's position for an in-stream one.
     */
    private void bindRelationships(Bind.CompilationUnit deck, String deckName,
                                   CobolRelationships.ResourceType deckType, String sourcePath,
                                   int lineOffset, ExecutionContext ctx) {
        Set<String> seen = new HashSet<>();
        for (BindCommand command : new BindCommand.Matcher().lower(deck).collect(Collectors.toList())) {
            int line = lineOffset + command.getLine();
            boolean rebind = command.getKind() == BindCommand.Kind.REBIND;
            String collection = command.getCollection();

            for (String plan : command.getPlans()) {
                if (rebind) {
                    insertDeckRow(seen, deckName, deckType, REFERENCES, plan, BINDPLAN, sourcePath, line, ctx);
                    continue;
                }
                insertDeckRow(seen, deckName, deckType, DEFINES, plan, BINDPLAN, sourcePath, line, ctx);
                for (String packageName : command.getPackageList()) {
                    insertDeckRow(seen, plan, BINDPLAN, BINDS, packageName, BINDPACKAGE, sourcePath, line, ctx);
                }
                for (String member : command.getMembers()) {
                    insertDeckRow(seen, plan, BINDPLAN, BINDS, member, DBRM, sourcePath, line, ctx);
                }
            }

            for (String packageName : command.getPackages()) {
                String qualified = collection == null ? packageName : collection + '.' + packageName;
                if (rebind) {
                    insertDeckRow(seen, deckName, deckType, REFERENCES, qualified, BINDPACKAGE, sourcePath, line, ctx);
                    continue;
                }
                insertDeckRow(seen, deckName, deckType, DEFINES, qualified, BINDPACKAGE, sourcePath, line, ctx);
                insertDeckRow(seen, qualified, BINDPACKAGE, BINDS, packageName, DBRM, sourcePath, line, ctx);
            }
        }
    }

    /**
     * The load module a link-edit deck builds and what it is built from, which nothing else in an
     * estate writes down: a step names a module and the module names its programs here. A subroutine
     * an {@code INCLUDE} names is bound into the module and reached without the system ever looking
     * the name up; one the deck leaves out is called dynamically and is a module of its own.
     *
     * @param deckType   what the deck is, which differs by where it was written: a {@code LINKLIB}
     *                   member is a link-edit deck, a deck written in-stream is the job.
     * @param lineOffset how many lines of the source come before the deck's first card, which is
     *                   nothing for a member of its own and the DD's position for an in-stream one.
     */
    private void linkEditRelationships(LinkEdit.CompilationUnit deck, String deckName,
                                       CobolRelationships.ResourceType deckType, String sourcePath,
                                       int lineOffset, ExecutionContext ctx) {
        Set<String> seen = new HashSet<>();
        LinkEditDeck linkEdit = new LinkEditDeck.Matcher().require(deck, null);
        LinkEditDeck.Name module = linkEdit.getModule();

        if (module != null) {
            int line = lineOffset + module.getLine();
            insertDeckRow(seen, deckName, deckType, DEFINES, module.getText(), LOAD_MODULE, sourcePath, line, ctx);
            for (LinkEditDeck.Name alias : linkEdit.getAliases()) {
                insertDeckRow(seen, deckName, deckType, DEFINES, alias.getText(), LOAD_MODULE, sourcePath,
                        lineOffset + alias.getLine(), ctx);
            }
            LinkEditDeck.Name entry = linkEdit.getEntry();
            if (entry != null) {
                insertDeckRow(seen, module.getText(), LOAD_MODULE, ENTRY, entry.getText(), COBOL, sourcePath,
                        lineOffset + entry.getLine(), ctx);
            }
        }

        for (LinkEditDeck.Include include : linkEdit.getIncludes()) {
            int line = lineOffset + include.getLine();
            insertDeckRow(seen, deckName, deckType, INCLUDE, include.getMember(), COBOL, sourcePath, line,
                    include.getDdName(), ctx);
            if (module != null) {
                insertDeckRow(seen, module.getText(), LOAD_MODULE, CONTAINS, include.getMember(), COBOL,
                        sourcePath, line, ctx);
            }
        }
    }

    /**
     * What an IDCAMS deck creates, which nothing else in an estate says: a VSAM file exists because a
     * {@code DEFINE} made it, and the JCL that reads it names it on a DD without saying where it came
     * from. The components of a cluster are catalog entries of their own, so a job may name either.
     * <p>
     * A DFSORT deck yields nothing here on purpose: {@code SORTIN} and {@code SORTOUT} are DD names,
     * and only the JCL says what they are bound to.
     *
     * @param deckType   what the deck is, which differs by where it was written: a control card
     *                   library member is a control card, a deck written in-stream is the job.
     * @param lineOffset how many lines of the source come before the deck's first card, which is
     *                   nothing for a member of its own and the DD's position for an in-stream one.
     */
    private void idcamsRelationships(Idcams.CompilationUnit deck, String deckName,
                                     CobolRelationships.ResourceType deckType, String sourcePath,
                                     int lineOffset, ExecutionContext ctx) {
        Set<String> seen = new HashSet<>();
        for (IdcamsCommand command : new IdcamsCommand.Matcher().lower(deck).collect(Collectors.toList())) {
            int line = lineOffset + command.getLine();
            for (String name : command.getDefinedNames()) {
                insertDeckRow(seen, deckName, deckType, DEFINES, name, DATA_SET, sourcePath, line, ctx);
            }
        }
    }

    private void insertDeckRow(Set<String> seen, String dependent, CobolRelationships.ResourceType dependentType,
                               CobolRelationships.ResourceAction action, String dependency,
                               CobolRelationships.ResourceType dependencyType, String sourcePath, int line,
                               ExecutionContext ctx) {
        insertDeckRow(seen, dependent, dependentType, action, dependency, dependencyType, sourcePath, line, "", ctx);
    }

    private void insertDeckRow(Set<String> seen, String dependent, CobolRelationships.ResourceType dependentType,
                               CobolRelationships.ResourceAction action, String dependency,
                               CobolRelationships.ResourceType dependencyType, String sourcePath, int line,
                               String actionMetadata, ExecutionContext ctx) {
        if (seen.add(dependent + ':' + action + ':' + dependency + ':' + line)) {
            cobolRelationships.insertRow(ctx, new CobolRelationships.Row(dependent, dependentType, action,
                    dependency, dependencyType, false, actionMetadata, sourcePath, line, null, null));
        }
    }

    public CobolPreprocessor.CharDataSql getSqlRelationships(
            CobolPreprocessor.CharDataSql sql,
            String sourceName,
            CobolRelationships.ResourceType dependentType,
            String sourcePath,
            @Nullable SourcePositions positions,
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
                                                "",
                                                sourcePath,
                                                lineOf(positions, word),
                                                null,
                                                null
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
                                                "UPDATE",
                                                sourcePath,
                                                lineOf(positions, word),
                                                null,
                                                null));
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
                                                "INSERT",
                                                sourcePath,
                                                lineOf(positions, word),
                                                null,
                                                null
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
                                                metadata,
                                                sourcePath,
                                                lineOf(positions, word),
                                                null,
                                                null
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
                                                    "CREATE",
                                                    sourcePath,
                                                    lineOf(positions, word),
                                                    null,
                                                    null
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

    private static String memberName(Path sourcePath) {
        String name = sourcePath.getFileName().toString();
        return name.contains(".") ? name.substring(0, name.indexOf(".")) : name;
    }

    private static @Nullable String pathOf(CobolPreprocessor.@Nullable Copybook copybook) {
        return copybook == null ? null : copybook.getSourcePath().toString();
    }

    private static @Nullable Integer lineOf(@Nullable SourcePositions positions, Cobol node) {
        return positions == null ? null : lineOf(positions.get(node));
    }

    private static @Nullable Integer lineOf(@Nullable SourcePositions positions, CobolPreprocessor node) {
        return positions == null ? null : lineOf(positions.get(node));
    }

    private static @Nullable Integer lineOf(@Nullable Range range) {
        return range == null ? null : range.getStart().getLine();
    }

    /**
     * Which line each word of a Control-M export was written on. The export carries no positions of its
     * own, so they are recovered the way anything else about how a tree reads is: by printing it.
     */
    private static Map<UUID, Integer> wordLines(ControlM.CompilationUnit cu) {
        Map<UUID, Integer> offsets = new LinkedHashMap<>();
        PrintOutputCapture<Integer> out = new PrintOutputCapture<>(0);
        new ControlMPrinter<Integer>() {
            @Override
            public ControlM visitWord(ControlM.Word word, PrintOutputCapture<Integer> p) {
                int before = p.out.length();
                ControlM printed = super.visitWord(word, p);
                // A column marker prints after the word, so find the word's own text rather than measuring back.
                int at = p.out.indexOf(word.getText(), before);
                if (at >= 0) {
                    offsets.put(word.getId(), at);
                }
                return printed;
            }
        }.visit(cu, out, new Cursor(null, Cursor.ROOT_VALUE));

        String source = out.getOut();
        Map<UUID, Integer> lines = new HashMap<>();
        int line = 1;
        int at = 0;
        for (Map.Entry<UUID, Integer> word : offsets.entrySet()) {
            for (; at < word.getValue(); at++) {
                if (source.charAt(at) == '\n') {
                    line++;
                }
            }
            lines.put(word.getKey(), line);
        }
        return lines;
    }
}
