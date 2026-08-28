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
package org.openrewrite.mainframe.cobol.search;

import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.PrintOutputCapture;
import org.openrewrite.ScanningRecipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.mainframe.assembler.AssemblerIsoVisitor;
import org.openrewrite.mainframe.assembler.AssemblerParser;
import org.openrewrite.mainframe.assembler.trait.Call;
import org.openrewrite.mainframe.assembler.trait.ControlSection;
import org.openrewrite.mainframe.assembler.trait.Copy;
import org.openrewrite.mainframe.assembler.trait.EntryPoint;
import org.openrewrite.mainframe.assembler.trait.MacroCall;
import org.openrewrite.mainframe.assembler.trait.MacroDefinition;
import org.openrewrite.mainframe.assembler.tree.Assembler;
import org.openrewrite.mainframe.cobol.CobolIsoVisitor;
import org.openrewrite.mainframe.cobol.CobolPreprocessorIsoVisitor;
import org.openrewrite.mainframe.cobol.SourcePositions;
import org.openrewrite.mainframe.cobol.marker.CopiedStatement;
import org.openrewrite.mainframe.cobol.marker.MissingCopybook;
import org.openrewrite.mainframe.cobol.table.CobolRelationships;
import org.openrewrite.mainframe.cobol.trait.DliCall;
import org.openrewrite.mainframe.cobol.trait.Literals;
import org.openrewrite.mainframe.cobol.tree.Cobol;
import org.openrewrite.mainframe.cobol.tree.CobolPreprocessor;
import org.openrewrite.mainframe.cobol.tree.Name;
import org.openrewrite.mainframe.controlcard.InStreamCards;
import org.openrewrite.mainframe.controlcard.idcams.IdcamsIsoVisitor;
import org.openrewrite.mainframe.controlcard.idcams.IdcamsLineReader;
import org.openrewrite.mainframe.controlcard.idcams.IdcamsParser;
import org.openrewrite.mainframe.controlcard.idcams.trait.IdcamsCommand;
import org.openrewrite.mainframe.controlcard.idcams.tree.Idcams;
import org.openrewrite.mainframe.controlm.ControlMIsoVisitor;
import org.openrewrite.mainframe.controlm.internal.ControlMPrinter;
import org.openrewrite.mainframe.controlm.tree.ControlM;
import org.openrewrite.mainframe.db2.bind.BindIsoVisitor;
import org.openrewrite.mainframe.db2.bind.InStreamBindDeck;
import org.openrewrite.mainframe.db2.bind.trait.BindCommand;
import org.openrewrite.mainframe.db2.bind.tree.Bind;
import org.openrewrite.mainframe.Members;
import org.openrewrite.mainframe.trait.Mention;
import org.openrewrite.mainframe.trait.RunBook;
import org.openrewrite.mainframe.trait.Script;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.internal.StringUtils;
import org.openrewrite.mainframe.ims.ImsIsoVisitor;
import org.openrewrite.mainframe.ims.trait.Application;
import org.openrewrite.mainframe.ims.trait.Database;
import org.openrewrite.mainframe.ims.trait.DatabaseAccess;
import org.openrewrite.mainframe.ims.trait.FormatSet;
import org.openrewrite.mainframe.ims.trait.Message;
import org.openrewrite.mainframe.ims.trait.Pcb;
import org.openrewrite.mainframe.ims.trait.Psb;
import org.openrewrite.mainframe.ims.trait.Segment;
import org.openrewrite.mainframe.ims.trait.SensitiveSegment;
import org.openrewrite.mainframe.ims.trait.Transaction;
import org.openrewrite.mainframe.ims.tree.Ims;
import org.openrewrite.mainframe.jcl.JclIsoVisitor;
import org.openrewrite.mainframe.jcl.tree.Jcl;
import org.openrewrite.mainframe.linkedit.InStreamLinkEditDeck;
import org.openrewrite.mainframe.linkedit.LinkEditIsoVisitor;
import org.openrewrite.mainframe.linkedit.trait.LinkEditDeck;
import org.openrewrite.mainframe.linkedit.tree.LinkEdit;
import org.openrewrite.mainframe.listload.trait.ModuleListing;
import org.openrewrite.marker.Range;
import org.openrewrite.marker.SearchResult;
import org.openrewrite.mainframe.sas.trait.Include;
import org.openrewrite.mainframe.sas.trait.InstreamSas;
import org.openrewrite.mainframe.sas.trait.SqlQuery;
import org.openrewrite.text.PlainText;
import org.openrewrite.text.PlainTextVisitor;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.openrewrite.mainframe.cobol.table.CobolRelationships.ResourceAction.*;
import static org.openrewrite.mainframe.cobol.table.CobolRelationships.ResourceType.*;

public class FindRelationships extends ScanningRecipe<FindRelationships.Assemblers> {
    transient CobolRelationships cobolRelationships = new CobolRelationships(this);

	@Getter
	final String displayName = "Find COBOL relationships";

	@Getter
	final String description = "Build a list of relationships for diagramming and exploration.";

    /**
     * The names the assembler members of the estate offer, gathered before any edge is written.
     * <p>
     * A {@code CALL} says a name and nothing else, so which language is on the other end of it can
     * only be answered by looking at every member first: {@code CALL 'CLMU030'} in COBOL reaches an
     * assembler subroutine, and there is nothing in the COBOL that says so. A macro invocation is the
     * same problem, answered by the prototypes the macro library writes.
     */
    public static class Assemblers {
        final Set<String> entryPoints = new HashSet<>();
        final Set<String> macroLibrary = new HashSet<>();

        boolean isEntryPoint(String name) {
            return entryPoints.contains(name.toUpperCase(Locale.ROOT));
        }

        boolean isMacro(String name) {
            return macroLibrary.contains(name.toUpperCase(Locale.ROOT));
        }

        CobolRelationships.ResourceType typeOf(String name) {
            return isEntryPoint(name) ? ASSEMBLER : COBOL;
        }
    }

    @Override
    public Assemblers getInitialValue(ExecutionContext ctx) {
        return new Assemblers();
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getScanner(Assemblers acc) {
        return new TreeVisitor<Tree, ExecutionContext>() {
            @Override
            public Tree preVisit(Tree tree, ExecutionContext ctx) {
                stopAfterPreVisit();
                if (tree instanceof Assembler.CompilationUnit) {
                    Assembler.CompilationUnit cu = (Assembler.CompilationUnit) tree;
                    if (!AssemblerParser.isMacroLibraryMember(cu.getSourcePath())) {
                        // A program offers its own name even where it writes neither a CSECT nor an
                        // END operand, since that is the name the binder gives the object deck.
                        acc.entryPoints.add(memberName(cu.getSourcePath()).toUpperCase(Locale.ROOT));
                    }
                    new EntryPoint.Matcher().lower(cu).forEach(entry -> {
                        for (String name : entry.getNames()) {
                            acc.entryPoints.add(name.toUpperCase(Locale.ROOT));
                        }
                    });
                    new MacroDefinition.Matcher().lower(cu).forEach(macro ->
                            acc.macroLibrary.add(macro.getName().toUpperCase(Locale.ROOT)));
                }
                return tree;
            }
        };
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor(Assemblers acc) {

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
            final Set<String> seenMods = new HashSet<>();
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
                DliCall dli = new DliCall.Matcher().get(getCursor()).orElse(null);
                if (dli != null) {
                    // The language interface is not a program of the estate; what the call reached is
                    // the database, and that edge is the PSB's to draw. The one thing the COBOL says
                    // for itself is the screen it sends.
                    if (dli.getMod() == null) {
                        return super.visitCall(call, ctx);
                    }
                    if (seenMods.add(dli.getMod())) {
                        cobolRelationships.insertRow(ctx,
                                new CobolRelationships.Row(
                                        programName,
                                        COBOL,
                                        SEND,
                                        dli.getMod(),
                                        MFS_MAP,
                                        false,
                                        "",
                                        sourcePath,
                                        lineOf(positions, call),
                                        null,
                                        null
                                )
                        );
                    }
                    return (Cobol.Call) dli.marked(null);
                }
                if (call.getIdentifier() instanceof Cobol.Word) {
                    Cobol.Word word = (Cobol.Word) call.getIdentifier();
                    // Either quote character. A shop writes CALL 'CLMU030' as readily as CALL "CLMU030",
                    // and a program calling a name that is not a literal decides it at run time.
                    String callName = Literals.valueOf(word.getWord());
                    if (callName != null) {
                        if (seenCalls.add(callName)) {
                            cobolRelationships.insertRow(ctx,
                                    new CobolRelationships.Row(
                                            programName,
                                            COBOL,
                                            CALL,
                                            callName,
                                            // The callee is only COBOL because nothing else claimed
                                            // the name: an assembler subroutine is called the same way.
                                            acc.typeOf(callName),
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

        AssemblerIsoVisitor<ExecutionContext> assemblerVisitor = new AssemblerIsoVisitor<ExecutionContext>() {
            @Override
            public Assembler.CompilationUnit visitCompilationUnit(Assembler.CompilationUnit cu, ExecutionContext ctx) {
                assemblerRelationships(cu, acc, ctx);
                return cu;
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

        ImsIsoVisitor<ExecutionContext> genVisitor = new ImsIsoVisitor<ExecutionContext>() {
            @Override
            public Ims.CompilationUnit visitCompilationUnit(Ims.CompilationUnit cu, ExecutionContext ctx) {
                // One reader takes all four gen libraries, so which of these a member has anything to
                // say to is what it gens.
                databaseRelationships(cu, ctx);
                psbRelationships(cu, ctx);
                systemDefinitionRelationships(cu, ctx);
                formatSetRelationships(cu, ctx);
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
                new InstreamSas.Matcher().lower(cu).forEach(stream ->
                        sasRelationships(stream.parse(), memberName(cu.getSourcePath()), JCL,
                                cu.getSourcePath().toString(), stream.getLine() - 1, ctx));
                return cu;
            }
        };

        // Every member kept as plain text arrives as the same tree, so what one is has to be asked.
        PlainTextVisitor<ExecutionContext> textVisitor = new PlainTextVisitor<ExecutionContext>() {
            @Override
            public PlainText visitText(PlainText text, ExecutionContext ctx) {
                Members.Kind kind = Members.kindOf(text);
                if (kind == null) {
                    return text;
                }
                switch (kind) {
                    case CLIST:
                    case REXX:
                        new Script.Matcher().lower(text)
                                .forEach(script -> scriptRelationships(script, text, acc, ctx));
                        break;
                    case DOCUMENT:
                        new RunBook.Matcher().lower(text)
                                .forEach(book -> runBookRelationships(book, text, ctx));
                        break;
                    case SAS:
                        sasRelationships(text, memberName(text.getSourcePath()), SAS,
                                text.getSourcePath().toString(), 0, ctx);
                        break;
                    case LISTING:
                        listingRelationships(text, ctx);
                        break;
                    default:
                        break;
                }
                return text;
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
                } else if (tree instanceof Assembler) {
                    t = assemblerVisitor.visit(t, ctx);
                } else if (tree instanceof LinkEdit) {
                    t = linkEditVisitor.visit(t, ctx);
                } else if (tree instanceof Ims) {
                    t = genVisitor.visit(t, ctx);
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
                } else if (tree instanceof PlainText) {
                    t = textVisitor.visit(t, ctx);
                }
                return t;
            }
        };
    }

    /**
     * What an assembler member reaches, which is written the way the assembler writes everything: a
     * name and no word saying what kind of name it is.
     * <p>
     * The macro library is what tells a shop macro from IBM's. {@code CALL}, {@code DCB}, {@code OPEN}
     * and the rest come out of {@code SYS1.MACLIB} and are nowhere in the repository, so an invocation
     * is an edge only when a member of the estate defines the name — otherwise every {@code OPEN} in
     * the corpus is a dependency on something nobody keeps.
     */
    private void assemblerRelationships(Assembler.CompilationUnit member, Assemblers acc, ExecutionContext ctx) {
        Set<String> seen = new HashSet<>();
        String memberName = memberName(member.getSourcePath());
        String sourcePath = member.getSourcePath().toString();

        for (ControlSection section : new ControlSection.Matcher().lower(member).collect(Collectors.toList())) {
            if (!section.isDummy() && !section.getName().isEmpty()) {
                insertDeckRow(seen, memberName, ASSEMBLER, DEFINES, section.getName(), CSECT,
                        sourcePath, section.getLine(), ctx);
            }
        }
        for (EntryPoint entry : new EntryPoint.Matcher().lower(member).collect(Collectors.toList())) {
            if (entry.getKind() == EntryPoint.Kind.SECTION) {
                continue;
            }
            for (String name : entry.getNames()) {
                insertDeckRow(seen, memberName, ASSEMBLER, ENTRY, name, CSECT, sourcePath,
                        entry.getLine(), ctx);
            }
        }
        for (Copy copy : new Copy.Matcher().lower(member).collect(Collectors.toList())) {
            insertDeckRow(seen, memberName, ASSEMBLER, COPY, copy.getMember(), ASSEMBLER, sourcePath,
                    copy.getLine(), ctx);
        }
        for (MacroCall macro : new MacroCall.Matcher().lower(member).collect(Collectors.toList())) {
            if (acc.isMacro(macro.getName())) {
                insertDeckRow(seen, memberName, ASSEMBLER, INCLUDE, macro.getName(), ASSEMBLER,
                        sourcePath, macro.getLine(), ctx);
            }
        }
        for (Call call : new Call.Matcher().lower(member).collect(Collectors.toList())) {
            // A DL/I interface is not a program of the estate; what the call reached is the database.
            if (!call.isDli()) {
                insertDeckRow(seen, memberName, ASSEMBLER, CALL, call.getTarget(),
                        acc.typeOf(call.getTarget()), sourcePath, call.getLine(),
                        call.getKind().name(), ctx);
            }
        }
        // Qualified because COBOL has a DliCall of its own, which this one deliberately mirrors.
        new org.openrewrite.mainframe.assembler.trait.DliCall.Matcher().lower(member).forEach(dli -> {
            String function = dli.getFunction() == null ? dli.getFunctionOperand() : dli.getFunction();
            if (dli.getPcb() != null) {
                // An assembler program addresses the mask by register rather than naming it, so the
                // dependency is the operand as written; which PCB that register holds is the PSB's answer.
                insertDeckRow(seen, memberName, ASSEMBLER, ACCESS, dli.getPcb(), IMS_PCB, sourcePath,
                        dli.getLine(), function, ctx);
            }
            for (String segment : dli.getSegments()) {
                insertDeckRow(seen, memberName, ASSEMBLER, ACCESS, segment, IMS_SEGMENT, sourcePath,
                        dli.getLine(), function, ctx);
            }
        });
    }

    /**
     * What a SAS program reaches: the members it includes and the tables it reads.
     * <p>
     * What it reaches by DD name — the library a {@code LIBNAME} allocates, the file an
     * {@code INFILE} reads, the library the {@code %INCLUDE} names — is left on the traits. A DD name
     * closes only against the job that ran the program, which is a join across two languages and
     * belongs to a recipe that has both.
     *
     * @param memberType what the program is, which differs by where it was written: a member of the
     *                   SAS library is SAS, a program written on a {@code SYSIN} stream is the job,
     *                   since it has no member name of its own.
     * @param lineOffset how many lines of the source come before the program's first statement,
     *                   which is nothing for a member of its own and the SYSIN's position for an
     *                   in-stream one.
     */
    private void sasRelationships(PlainText program, String memberName,
                                  CobolRelationships.ResourceType memberType, String sourcePath,
                                  int lineOffset, ExecutionContext ctx) {
        Set<String> seen = new HashSet<>();
        for (Include.Reference include : new Include.Matcher().require(program, null).getReferences()) {
            if (include.getMember() != null) {
                insertDeckRow(seen, memberName, memberType, INCLUDE, include.getMember(), SAS,
                        sourcePath, lineOffset + include.getLine(), ctx);
            }
        }
        for (SqlQuery.Query query : new SqlQuery.Matcher().require(program, null).getQueries()) {
            for (SqlQuery.Table table : query.getTables()) {
                // A name read out of a SAS library is a data set of that library and not a table any
                // DB2 catalog has heard of; only what came through the connection is.
                if (table.isPassthrough()) {
                    insertDeckRow(seen, memberName, memberType, ACCESS, table.getName(), SQL_TABLE,
                            sourcePath, lineOffset + table.getLine(), table.getDbms(), ctx);
                }
            }
        }
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
     * What a CLIST or a REXX exec reaches: the jobs it submits, the programs it runs and the other
     * scripts it calls.
     * <p>
     * Only a name the script writes down is an edge. A name it computed is left out — {@code CLMCOMP}
     * picks a compile job into {@code &JOB} and hands it to {@code CLMSUB}, which submits it, so the job
     * a script really submits is a fact about two members and a parameter — and so are the data sets it
     * allocates, which close against the DD names of the program it runs and belong to a recipe that
     * has both.
     */
    private void scriptRelationships(Script script, PlainText member, Assemblers acc,
                                     ExecutionContext ctx) {
        Set<String> seen = new HashSet<>();
        String memberName = memberName(member.getSourcePath());
        String sourcePath = member.getSourcePath().toString();
        CobolRelationships.ResourceType scriptType =
                Members.kindOf(member) == Members.Kind.REXX ? REXX : CLIST;

        for (Script.Reference reference : script.getReferences()) {
            if (reference.isSymbolic()) {
                continue;
            }
            switch (reference.getKind()) {
                case SUBMIT:
                    insertDeckRow(seen, memberName, scriptType, SUBMITS, reference.getName(), JCL,
                            sourcePath, reference.getLine(), reference.getKind().name(), ctx);
                    break;
                case EXEC:
                    // Which library the name is found in is the session's answer and not the
                    // statement's, so a script reaches one of its own kind: SYSPROC first for a CLIST,
                    // SYSEXEC for an exec.
                    insertDeckRow(seen, memberName, scriptType, CALL, reference.getName(), scriptType,
                            sourcePath, reference.getLine(), reference.getKind().name(), ctx);
                    break;
                case CALL:
                case RUN:
                    insertDeckRow(seen, memberName, scriptType, CALL, reference.getName(),
                            acc.typeOf(reference.getName()), sourcePath, reference.getLine(),
                            reference.getKind().name(), ctx);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * What a run book documents, which is the one edge its text draws by itself: a {@code DOCJOB} is
     * about a job, a {@code DOCPGM} about a program, a {@code DOCFICH} about the data set on its
     * {@code FILE} line.
     * <p>
     * The other names a run book mentions are not rows here. They are names and not references — a run
     * book names a component in a sentence, and whether a name is a component at all is answered by
     * looking it up among the members a repository holds, which is a join and not something one member
     * says.
     */
    private void runBookRelationships(RunBook book, PlainText member, ExecutionContext ctx) {
        Mention subject = book.getSubject();
        CobolRelationships.ResourceType documented = documented(book.getShape());
        if (subject == null || documented == null) {
            return;
        }
        insertDeckRow(new HashSet<>(), book.getName(), DOCUMENT, REFERENCES, subject.getText(),
                documented, member.getSourcePath().toString(), subject.getLine(),
                book.getShape().name(), ctx);
    }

    /**
     * What a run book of each shape is about. An application and an operating procedure are neither,
     * so those two draw no edge until there is something for them to be an edge to.
     */
    private static CobolRelationships.@Nullable ResourceType documented(RunBook.Shape shape) {
        switch (shape) {
            case DOCJOB:
                return JCL;
            case DOCPGM:
                return COBOL;
            case DOCFICH:
                return DATA_SET;
            default:
                return null;
        }
    }

    /**
     * What a load module actually holds, which only a listing says. A link-edit deck says what a
     * module was meant to be built from; the listing says what the binder put there — the runtime and
     * the language interface the autocall pulled in as well as the objects the deck asked for — so
     * reconciling the two finds a module built from something other than the source a shop keeps.
     */
    private void listingRelationships(PlainText listing, ExecutionContext ctx) {
        Set<String> seen = new HashSet<>();
        String sourcePath = listing.getSourcePath().toString();
        for (ModuleListing.Module module : new ModuleListing.Matcher().require(listing, null).getModules()) {
            ModuleListing.Entry entry = module.getEntry();
            if (entry != null) {
                insertDeckRow(seen, module.getName(), LOAD_MODULE, ENTRY, entry.getName(), COBOL,
                        sourcePath, entry.getLine(), ctx);
            }
            for (ModuleListing.Csect csect : module.getCsects()) {
                insertDeckRow(seen, module.getName(), LOAD_MODULE, CONTAINS, csect.getName(), CSECT,
                        sourcePath, csect.getLine(), ctx);
            }
        }
    }

    /**
     * What a DBD defines and what it reaches, which nothing else in an estate writes down: a PSB says
     * which databases a program may use and a DL/I call says which segment it asked for, but only the
     * DBD says what the segments are and which other database this one is tied to.
     * <p>
     * The member is typed as assembler because that is what a gen library holds — a deck of macro
     * invocations the gen job assembles. An {@code XDFLD} yields nothing here: its {@code SRCH=} names
     * a field of this same database, so it is not an edge between databases.
     */
    private void databaseRelationships(Ims.CompilationUnit member, ExecutionContext ctx) {
        Set<String> seen = new HashSet<>();
        String memberName = memberName(member.getSourcePath());
        String sourcePath = member.getSourcePath().toString();
        for (Database database : new Database.Matcher().lower(member).collect(Collectors.toList())) {
            String name = database.getName();
            insertDeckRow(seen, memberName, ASSEMBLER, DEFINES, name, IMS_DATABASE, sourcePath,
                    database.getLine(), ctx);
            for (Segment segment : database.getSegments()) {
                insertDeckRow(seen, name, IMS_DATABASE, CONTAINS, segment.getName(), IMS_SEGMENT,
                        sourcePath, segment.getLine(), ctx);
            }
            for (Database.Reference reference : database.getReferences()) {
                if (reference.getKind() != Database.Reference.Kind.INDEX_SOURCE) {
                    insertDeckRow(seen, name, IMS_DATABASE, REFERENCES, reference.getDatabase(),
                            IMS_DATABASE, sourcePath, reference.getLine(), reference.getMember(), ctx);
                }
            }
        }
    }

    /**
     * What a PSB lets a program reach: the PCBs in the order the program is handed them, the database
     * each opens, and the segments of it the program may see.
     * <p>
     * A DL/I call names a position and nothing else, so these rows are what turn that position into a
     * database. The {@code PROCOPT} rides along as the action metadata, since it is what says whether
     * the reach is a read or a write.
     */
    private void psbRelationships(Ims.CompilationUnit member, ExecutionContext ctx) {
        Set<String> seen = new HashSet<>();
        String memberName = memberName(member.getSourcePath());
        String sourcePath = member.getSourcePath().toString();
        for (Psb psb : new Psb.Matcher().lower(member).collect(Collectors.toList())) {
            String psbName = psb.getName();
            insertDeckRow(seen, memberName, ASSEMBLER, DEFINES, psbName, IMS_PSB, sourcePath,
                    psb.getLine(), ctx);
            for (Pcb pcb : psb.getPcbs()) {
                String pcbName = pcbName(psbName, pcb);
                insertDeckRow(seen, psbName, IMS_PSB, CONTAINS, pcbName, IMS_PCB, sourcePath,
                        pcb.getLine(), String.valueOf(pcb.getPosition()), ctx);
                String procopt = pcb.getProcessingOptions() == null ? "" : pcb.getProcessingOptions();
                if (pcb.getDatabaseName() != null) {
                    insertDeckRow(seen, pcbName, IMS_PCB, ACCESS, pcb.getDatabaseName(), IMS_DATABASE,
                            sourcePath, pcb.getLine(), procopt, ctx);
                }
                // A PROCSEQ is a second database the PCB opens: the index the roots are walked in.
                if (pcb.getProcessingSequence() != null) {
                    insertDeckRow(seen, pcbName, IMS_PCB, ACCESS, pcb.getProcessingSequence(),
                            IMS_DATABASE, sourcePath, pcb.getLine(), "PROCSEQ", ctx);
                }
                for (SensitiveSegment segment : pcb.getSensitiveSegments()) {
                    insertDeckRow(seen, pcbName, IMS_PCB, ACCESS, segment.getName(), IMS_SEGMENT,
                            sourcePath, segment.getLine(),
                            segment.getProcessingOptions() == null ? procopt :
                                    segment.getProcessingOptions(), ctx);
                }
            }
        }
    }

    /**
     * What the stage 1 deck ties together: the transaction a terminal types, the PSB that answers it,
     * and the databases the control region is told about.
     * <p>
     * This is the only place a transaction code is written down, so the online side of an IMS estate
     * is unreachable without it. What it does not say is the program — a message driven application is
     * loaded by the name its PSB has, and which program was link-edited under that name is for the
     * link-edit deck to say.
     */
    private void systemDefinitionRelationships(Ims.CompilationUnit member, ExecutionContext ctx) {
        Set<String> seen = new HashSet<>();
        String memberName = memberName(member.getSourcePath());
        String sourcePath = member.getSourcePath().toString();
        for (Application application : new Application.Matcher().lower(member).collect(Collectors.toList())) {
            String psb = application.getPsbName();
            if (psb == null) {
                continue;
            }
            insertDeckRow(seen, memberName, ASSEMBLER, REFERENCES, psb, IMS_PSB, sourcePath,
                    application.getLine(),
                    application.getProgramType() == null ? "" : application.getProgramType(), ctx);
            for (Transaction transaction : application.getTransactions()) {
                insertDeckRow(seen, transaction.getCode(), IMS_TRANSACTION, SCHEDULES, psb, IMS_PSB,
                        sourcePath, transaction.getLine(), ctx);
            }
        }
        for (DatabaseAccess database : new DatabaseAccess.Matcher().lower(member).collect(Collectors.toList())) {
            insertDeckRow(seen, memberName, ASSEMBLER, REFERENCES, database.getName(), IMS_DATABASE,
                    sourcePath, database.getLine(),
                    database.getAccess() == null ? "" : database.getAccess(), ctx);
        }
    }

    /**
     * What a format set ties together: the messages laid out on it, and the message each one is
     * answered by.
     * <p>
     * A program names a MOD and nothing else, so these rows are what carry a screen from the name in
     * working storage to the fields it draws. The {@code NXT=} chain is the other half: it is the only
     * place the MID a reply arrives on is written, since the call that reads the reply names no format
     * at all.
     */
    private void formatSetRelationships(Ims.CompilationUnit member, ExecutionContext ctx) {
        Set<String> seen = new HashSet<>();
        String memberName = memberName(member.getSourcePath());
        String sourcePath = member.getSourcePath().toString();
        for (FormatSet format : new FormatSet.Matcher().lower(member).collect(Collectors.toList())) {
            insertDeckRow(seen, memberName, ASSEMBLER, DEFINES, format.getName(), MFS_FORMAT, sourcePath,
                    format.getLine(), ctx);
        }
        for (Message message : new Message.Matcher().lower(member).collect(Collectors.toList())) {
            String type = message.getType() == null ? "" : message.getType();
            insertDeckRow(seen, memberName, ASSEMBLER, DEFINES, message.getName(), MFS_MAP, sourcePath,
                    message.getLine(), type, ctx);
            if (message.getFormatName() != null) {
                insertDeckRow(seen, message.getName(), MFS_MAP, REFERENCES, message.getFormatName(),
                        MFS_FORMAT, sourcePath, message.getLine(), "SOR", ctx);
            }
            if (message.getNextName() != null) {
                insertDeckRow(seen, message.getName(), MFS_MAP, REFERENCES, message.getNextName(),
                        MFS_MAP, sourcePath, message.getLine(), "NXT", ctx);
            }
        }
    }

    /**
     * What to call a PCB. Most are named by nothing but where they come, so an unnamed one is the
     * PSB and its position — which is what a program counting masks has to work from anyway.
     */
    private static String pcbName(String psbName, Pcb pcb) {
        return pcb.getName() == null ? psbName + '(' + pcb.getPosition() + ')' : pcb.getName();
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
