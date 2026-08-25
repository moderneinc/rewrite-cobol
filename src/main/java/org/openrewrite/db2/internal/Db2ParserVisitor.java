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
package org.openrewrite.db2.internal;

import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jspecify.annotations.Nullable;
import org.openrewrite.FileAttributes;
import org.openrewrite.db2.internal.grammar.DB2Parser;
import org.openrewrite.db2.internal.grammar.DB2ParserBaseVisitor;
import org.openrewrite.db2.marker.Semicolon;
import org.openrewrite.db2.tree.*;
import org.openrewrite.marker.Markers;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.openrewrite.Tree.randomId;
import static org.openrewrite.db2.tree.Space.EMPTY;

/**
 * Builds the LST by walking the parse tree in source order, taking every prefix from the gap between
 * the cursor and the next token. A comment is not a node, so it arrives in the prefix of whatever
 * follows and the file prints back byte for byte without comments being modelled.
 */
@RequiredArgsConstructor
public class Db2ParserVisitor extends DB2ParserBaseVisitor<Db2> {

    private final Path path;
    private final @Nullable FileAttributes fileAttributes;
    private final String source;
    private final Charset charset;
    private final boolean charsetBomMarked;

    private int cursor = 0;

    /**
     * Rules that only say which of several shapes follows, and whose children belong to the
     * statement itself. Walking through them is what keeps a {@code DROP TABLE}'s name a name.
     */
    private static final Set<Class<?>> TRANSPARENT = new HashSet<>(Arrays.asList(
            DB2Parser.DroppedObjectContext.class,
            DB2Parser.CommentTargetContext.class,
            DB2Parser.LabelTargetContext.class,
            DB2Parser.SpecialRegisterContext.class,
            DB2Parser.PrivilegeObjectContext.class,
            DB2Parser.QualifiedNameListContext.class));

    @Override
    public Db2.Ddl visitCompilationUnit(DB2Parser.CompilationUnitContext ctx) {
        List<Db2RightPadded<Statement>> statements = new ArrayList<>(ctx.statement().size());
        for (DB2Parser.StatementContext statement : ctx.statement()) {
            statements.add(terminated((Statement) visit(statement.getChild(0))));
        }
        return new Db2.Ddl(randomId(), path, fileAttributes, EMPTY, Markers.EMPTY,
                charset.name(), charsetBomMarked, null, statements,
                Space.build(source.substring(cursor)));
    }

    /**
     * A statement with the space before its terminator, and a {@link Semicolon} marker when it has
     * one. Where it does not, the cursor goes back so the white space flows to the next statement's
     * prefix instead of being eaten here.
     */
    private Db2RightPadded<Statement> terminated(Statement statement) {
        Db2RightPadded<Statement> padded = Db2RightPadded.build(statement);
        int saveCursor = cursor;
        Space before = whitespace();
        if (cursor < source.length() && source.charAt(cursor) == ';') {
            cursor++;
            return padded.withAfter(before)
                    .withMarkers(padded.getMarkers().add(new Semicolon(randomId())));
        }
        cursor = saveCursor;
        return padded;
    }

    //
    // Statements. All but a handful are keywords, the names they apply to, and the options that
    // qualify them, so they are gathered once and assembled by the statement's own method.
    //

    private final class Parts {
        final Space prefix;
        final List<Db2.Keyword> keywords = new ArrayList<>();
        final List<Db2.Name> names = new ArrayList<>();
        final List<Db2> options = new ArrayList<>();

        Parts(ParserRuleContext ctx) {
            prefix = prefix(ctx);
            walk(ctx);
        }

        private void walk(ParseTree tree) {
            for (int i = 0; i < tree.getChildCount(); i++) {
                ParseTree child = tree.getChild(i);
                if (child instanceof DB2Parser.QualifiedNameContext) {
                    names.add(name((DB2Parser.QualifiedNameContext) child));
                } else if (child instanceof DB2Parser.IdentifierContext) {
                    names.add(name((DB2Parser.IdentifierContext) child));
                } else if (child instanceof TerminalNode) {
                    Token token = ((TerminalNode) child).getSymbol();
                    if (token.getType() == DB2Parser.SEMI) {
                        continue;
                    }
                    if (isKeywordToken(token)) {
                        keywords.add(keyword(token));
                    } else {
                        options.add(word(token));
                    }
                } else if (child instanceof DB2Parser.EndContext) {
                    // The terminator is read by `terminated`, not here.
                } else if (TRANSPARENT.contains(child.getClass())) {
                    walk(child);
                } else {
                    options.add(option((ParserRuleContext) child));
                }
            }
        }

        Db2.Name nameAt(int i) {
            return names.get(i);
        }

        /**
         * The options, plus any name past the one the statement itself named.
         */
        List<Db2> tail(int fromName) {
            List<Db2> tail = new ArrayList<>(options);
            for (int i = fromName; i < names.size(); i++) {
                tail.add(names.get(i));
            }
            return tail;
        }
    }

    @Override
    public Db2 visitCreateTable(DB2Parser.CreateTableContext ctx) {
        Space prefix = prefix(ctx);
        List<Db2.Keyword> keywords = keywords(ctx.CREATE(), ctx.TABLE());
        Db2.Name name = name(ctx.qualifiedName());
        TableContents contents = tableContents(ctx.tableContents());
        return new Db2.CreateTable(randomId(), prefix, Markers.EMPTY, keywords, name, contents,
                options(ctx.tableOption()));
    }

    private TableContents tableContents(DB2Parser.TableContentsContext ctx) {
        Space prefix = prefix(ctx);
        // Two of the three alternatives open with a bracket, so which one this is turns on LIKE
        // and AS rather than on the bracket being there.
        if (ctx.LIKE() != null) {
            return new Db2.TableLike(randomId(), prefix, Markers.EMPTY,
                    keywords(ctx.LIKE()), name(ctx.qualifiedName()), options(ctx.copyOption()));
        }
        if (ctx.AS() != null) {
            List<Db2.Keyword> as = keywords(ctx.AS());
            Space before = sourceBefore("(");
            Db2RightPadded<Db2.Query> query =
                    Db2RightPadded.build(query(ctx.queryExpression())).withAfter(sourceBefore(")"));
            return new Db2.TableAsQuery(randomId(), prefix, Markers.EMPTY, as,
                    Db2Container.build(before, singletonList(query), Markers.EMPTY),
                    options(ctx.copyOption()));
        }
        return new Db2.TableElements(randomId(), prefix, Markers.EMPTY,
                container(ctx.LPAREN(), ctx.tableElement(), ctx.COMMA(), ctx.RPAREN(),
                        this::tableElement));
    }

    private Db2 tableElement(DB2Parser.TableElementContext ctx) {
        if (ctx.columnDefinition() != null) {
            return columnDefinition(ctx.columnDefinition());
        }
        if (ctx.tableConstraint() != null) {
            return constraint(ctx.tableConstraint());
        }
        return option(ctx.periodDefinition());
    }

    private Db2.ColumnDefinition columnDefinition(DB2Parser.ColumnDefinitionContext ctx) {
        Space prefix = prefix(ctx);
        return new Db2.ColumnDefinition(randomId(), prefix, Markers.EMPTY,
                name(ctx.identifier()), dataType(ctx.dataType()), options(ctx.columnAttribute()));
    }

    private Db2.DataType dataType(DB2Parser.DataTypeContext ctx) {
        Space prefix = prefix(ctx);
        Db2.Name name = typeName(ctx.typeName());
        Db2Container<Db2.Word> arguments = null;
        if (ctx.LPAREN() != null) {
            List<Db2RightPadded<Db2.Word>> args = new ArrayList<>(2);
            Space before = sourceBefore("(");
            for (TerminalNode number : ctx.NUMBER()) {
                args.add(Db2RightPadded.build(word(number)).withAfter(whitespace()));
                if (args.size() < ctx.NUMBER().size()) {
                    skip(",");
                }
            }
            skip(")");
            arguments = Db2Container.build(before, args, Markers.EMPTY);
        }
        return new Db2.DataType(randomId(), prefix, Markers.EMPTY, name, arguments,
                options(ctx.typeAttribute()));
    }

    private Db2.Name typeName(DB2Parser.TypeNameContext ctx) {
        if (ctx.qualifiedName() != null) {
            return name(ctx.qualifiedName());
        }
        Space prefix = prefix(ctx);
        List<Db2.Word> parts = new ArrayList<>(2);
        for (ParseTree child : ctx.children) {
            parts.add(word((TerminalNode) child));
        }
        return new Db2.Name(randomId(), prefix, Markers.EMPTY, parts);
    }

    /**
     * A table level constraint, read in source order: the optional {@code CONSTRAINT name}, the
     * keywords naming the kind, its columns, and — for a foreign key — what it references. Each
     * keyword that sits between two other parts is a field of its own, because the printer writes
     * the fields in the order they are declared.
     */
    private Db2.Constraint constraint(DB2Parser.TableConstraintContext ctx) {
        Space prefix = prefix(ctx);
        List<Db2.Keyword> keywords = new ArrayList<>(4);
        List<Db2> options = new ArrayList<>();
        Db2.Keyword constraintKeyword = ctx.CONSTRAINT() == null ? null : keyword(ctx.CONSTRAINT());
        Db2.Name constraintName = ctx.identifier() == null ? null : name(ctx.identifier());
        Db2.Keyword references = null;
        Db2.Name keyName = null;
        Db2.Name referencedTable = null;
        Db2Container<Db2.Name> columns = null;
        Db2Container<Db2.Name> referencedColumns = null;

        for (ParseTree child : ctx.constraintBody().children) {
            if (child instanceof DB2Parser.ColumnListContext) {
                Db2Container<Db2.Name> list = columnList((DB2Parser.ColumnListContext) child);
                if (columns == null) {
                    columns = list;
                } else {
                    referencedColumns = list;
                }
            } else if (child instanceof DB2Parser.QualifiedNameContext) {
                referencedTable = name((DB2Parser.QualifiedNameContext) child);
            } else if (child instanceof DB2Parser.IdentifierContext) {
                // The name DB2 lets a foreign key carry between KEY and its columns. CardDemo
                // writes one, genapp does not.
                keyName = name((DB2Parser.IdentifierContext) child);
            } else if (child instanceof TerminalNode) {
                Token token = ((TerminalNode) child).getSymbol();
                if (token.getType() == DB2Parser.REFERENCES) {
                    references = keyword(token);
                } else if (isKeywordToken(token)) {
                    keywords.add(keyword(token));
                } else {
                    options.add(word(token));
                }
            } else {
                options.add(option((ParserRuleContext) child));
            }
        }
        return new Db2.Constraint(randomId(), prefix, Markers.EMPTY, constraintKeyword,
                constraintName, keywords, keyName, columns, references, referencedTable,
                referencedColumns, options);
    }

    private Db2Container<Db2.Name> columnList(DB2Parser.ColumnListContext ctx) {
        return container(ctx.LPAREN(), ctx.identifier(), ctx.COMMA(), ctx.RPAREN(), this::name);
    }

    @Override
    public Db2 visitCreateIndex(DB2Parser.CreateIndexContext ctx) {
        Space prefix = prefix(ctx);
        List<Db2.Keyword> keywords = new ArrayList<>(4);
        keywords.add(keyword(ctx.CREATE()));
        for (DB2Parser.IndexModifierContext modifier : ctx.indexModifier()) {
            for (ParseTree child : modifier.children) {
                keywords.add(keyword((TerminalNode) child));
            }
        }
        keywords.add(keyword(ctx.INDEX()));
        Db2.Name name = name(ctx.qualifiedName(0));
        Db2.Keyword on = keyword(ctx.ON());
        Db2.Name table = name(ctx.qualifiedName(1));
        Db2Container<Db2.IndexKey> keys =
                container(ctx.LPAREN(), ctx.indexKey(), ctx.COMMA(), ctx.RPAREN(), this::indexKey);
        return new Db2.CreateIndex(randomId(), prefix, Markers.EMPTY, keywords, name, on, table,
                keys, options(ctx.indexOption()));
    }

    private Db2.IndexKey indexKey(DB2Parser.IndexKeyContext ctx) {
        Space prefix = prefix(ctx);
        Db2.Name name = name(ctx.identifier());
        Db2.Keyword direction = null;
        for (ParseTree child : ctx.children) {
            if (child instanceof TerminalNode) {
                direction = keyword((TerminalNode) child);
            }
        }
        return new Db2.IndexKey(randomId(), prefix, Markers.EMPTY, name, direction);
    }

    @Override
    public Db2 visitAlterTable(DB2Parser.AlterTableContext ctx) {
        Space prefix = prefix(ctx);
        List<Db2.Keyword> keywords = keywords(ctx.ALTER(), ctx.TABLE());
        Db2.Name name = name(ctx.qualifiedName());
        List<Db2> actions = new ArrayList<>();
        for (DB2Parser.AlterTableActionContext action : ctx.alterTableAction()) {
            if (action.tableConstraint() != null && action.ADD() == null) {
                actions.add(constraint(action.tableConstraint()));
            } else if (action.columnDefinition() != null) {
                actions.add(columnDefinition(action.columnDefinition()));
            } else {
                actions.add(option(action));
            }
        }
        return new Db2.AlterTable(randomId(), prefix, Markers.EMPTY, keywords, name, actions);
    }

    //
    // Everything else assembles from the same walk.
    //

    @Override
    public Db2 visitCreateTablespace(DB2Parser.CreateTablespaceContext ctx) {
        Space prefix = prefix(ctx);
        List<Db2.Keyword> keywords = keywords(ctx.CREATE(), ctx.LOB(), ctx.LARGE(), ctx.TABLESPACE());
        Db2.Name name = name(ctx.identifier(0));
        Db2.Keyword in = ctx.IN() == null ? null : keyword(ctx.IN());
        Db2.Name database = ctx.IN() == null ? null : name(ctx.identifier(1));
        return new Db2.CreateTablespace(randomId(), prefix, Markers.EMPTY, keywords, name, in,
                database, options(ctx.tablespaceOption()));
    }

    @Override
    public Db2 visitCreateDatabase(DB2Parser.CreateDatabaseContext ctx) {
        Parts p = new Parts(ctx);
        return new Db2.CreateDatabase(randomId(), p.prefix, Markers.EMPTY, p.keywords,
                p.nameAt(0), p.tail(1));
    }

    @Override
    public Db2 visitCreateStogroup(DB2Parser.CreateStogroupContext ctx) {
        Parts p = new Parts(ctx);
        return new Db2.CreateStogroup(randomId(), p.prefix, Markers.EMPTY, p.keywords,
                p.nameAt(0), p.tail(1));
    }

    @Override
    public Db2 visitCreateRole(DB2Parser.CreateRoleContext ctx) {
        Parts p = new Parts(ctx);
        return new Db2.CreateRole(randomId(), p.prefix, Markers.EMPTY, p.keywords, p.nameAt(0));
    }

    @Override
    public Db2 visitCreateAuxiliaryTable(DB2Parser.CreateAuxiliaryTableContext ctx) {
        Parts p = new Parts(ctx);
        return new Db2.CreateAuxiliaryTable(randomId(), p.prefix, Markers.EMPTY, p.keywords,
                p.nameAt(0), p.tail(1));
    }

    @Override
    public Db2 visitCreateTrustedContext(DB2Parser.CreateTrustedContextContext ctx) {
        Parts p = new Parts(ctx);
        return new Db2.CreateTrustedContext(randomId(), p.prefix, Markers.EMPTY, p.keywords,
                p.nameAt(0), p.tail(1));
    }

    @Override
    public Db2 visitCreateMask(DB2Parser.CreateMaskContext ctx) {
        Parts p = new Parts(ctx);
        return new Db2.CreateMask(randomId(), p.prefix, Markers.EMPTY, p.keywords,
                p.nameAt(0), p.nameAt(1), p.tail(2));
    }

    @Override
    public Db2 visitCreatePermission(DB2Parser.CreatePermissionContext ctx) {
        Parts p = new Parts(ctx);
        return new Db2.CreatePermission(randomId(), p.prefix, Markers.EMPTY, p.keywords,
                p.nameAt(0), p.nameAt(1), p.tail(2));
    }

    @Override
    public Db2 visitAlterTablespace(DB2Parser.AlterTablespaceContext ctx) {
        Space prefix = prefix(ctx);
        List<Db2.Keyword> keywords = keywords(ctx.ALTER(), ctx.LOB(), ctx.LARGE(), ctx.TABLESPACE());
        Db2.Name name = name(ctx.qualifiedName());
        return new Db2.AlterTablespace(randomId(), prefix, Markers.EMPTY, keywords, name, null,
                null, options(ctx.tablespaceOption()));
    }

    @Override
    public Db2 visitAlterIndex(DB2Parser.AlterIndexContext ctx) {
        Parts p = new Parts(ctx);
        return new Db2.AlterIndex(randomId(), p.prefix, Markers.EMPTY, p.keywords, p.nameAt(0), p.tail(1));
    }

    @Override
    public Db2 visitAlterDatabase(DB2Parser.AlterDatabaseContext ctx) {
        Parts p = new Parts(ctx);
        return new Db2.AlterDatabase(randomId(), p.prefix, Markers.EMPTY, p.keywords, p.nameAt(0), p.tail(1));
    }

    @Override
    public Db2 visitAlterStogroup(DB2Parser.AlterStogroupContext ctx) {
        Parts p = new Parts(ctx);
        return new Db2.AlterStogroup(randomId(), p.prefix, Markers.EMPTY, p.keywords, p.nameAt(0), p.tail(1));
    }

    @Override
    public Db2 visitAlterSequence(DB2Parser.AlterSequenceContext ctx) {
        Parts p = new Parts(ctx);
        return new Db2.AlterSequence(randomId(), p.prefix, Markers.EMPTY, p.keywords, p.nameAt(0), p.tail(1));
    }

    @Override
    public Db2 visitAlterView(DB2Parser.AlterViewContext ctx) {
        Parts p = new Parts(ctx);
        return new Db2.AlterView(randomId(), p.prefix, Markers.EMPTY, p.keywords, p.nameAt(0), p.tail(1));
    }

    @Override
    public Db2 visitAlterTrigger(DB2Parser.AlterTriggerContext ctx) {
        Parts p = new Parts(ctx);
        return new Db2.AlterTrigger(randomId(), p.prefix, Markers.EMPTY, p.keywords, p.nameAt(0), p.tail(1));
    }

    @Override
    public Db2 visitAlterMask(DB2Parser.AlterMaskContext ctx) {
        Parts p = new Parts(ctx);
        return new Db2.AlterMask(randomId(), p.prefix, Markers.EMPTY, p.keywords, p.nameAt(0), p.tail(1));
    }

    @Override
    public Db2 visitAlterPermission(DB2Parser.AlterPermissionContext ctx) {
        Parts p = new Parts(ctx);
        return new Db2.AlterPermission(randomId(), p.prefix, Markers.EMPTY, p.keywords, p.nameAt(0), p.tail(1));
    }

    @Override
    public Db2 visitAlterTrustedContext(DB2Parser.AlterTrustedContextContext ctx) {
        Parts p = new Parts(ctx);
        return new Db2.AlterTrustedContext(randomId(), p.prefix, Markers.EMPTY, p.keywords, p.nameAt(0), p.tail(1));
    }

    @Override
    public Db2 visitDropStatement(DB2Parser.DropStatementContext ctx) {
        Parts p = new Parts(ctx);
        return new Db2.Drop(randomId(), p.prefix, Markers.EMPTY, p.keywords, p.nameAt(0), p.tail(1));
    }

    @Override
    public Db2 visitSavepointStatement(DB2Parser.SavepointStatementContext ctx) {
        Parts p = new Parts(ctx);
        return new Db2.Savepoint(randomId(), p.prefix, Markers.EMPTY, p.keywords, p.nameAt(0), p.tail(1));
    }

    @Override
    public Db2 visitReleaseSavepointStatement(DB2Parser.ReleaseSavepointStatementContext ctx) {
        Parts p = new Parts(ctx);
        return new Db2.ReleaseSavepoint(randomId(), p.prefix, Markers.EMPTY, p.keywords, p.nameAt(0));
    }

    @Override
    public Db2 visitLockStatement(DB2Parser.LockStatementContext ctx) {
        Parts p = new Parts(ctx);
        return new Db2.LockTable(randomId(), p.prefix, Markers.EMPTY, p.keywords, p.nameAt(0));
    }

    @Override
    public Db2 visitCommitStatement(DB2Parser.CommitStatementContext ctx) {
        Parts p = new Parts(ctx);
        return new Db2.Commit(randomId(), p.prefix, Markers.EMPTY, p.keywords);
    }

    @Override
    public Db2 visitCreateVariable(DB2Parser.CreateVariableContext ctx) {
        Space prefix = prefix(ctx);
        List<Db2.Keyword> keywords = keywords(ctx.CREATE(), ctx.VARIABLE());
        Db2.Name name = name(ctx.qualifiedName());
        Db2.DataType type = dataType(ctx.dataType());
        List<Db2> options = new ArrayList<>();
        if (ctx.DEFAULT() != null) {
            options.add(option(ctx));
        }
        return new Db2.CreateVariable(randomId(), prefix, Markers.EMPTY, keywords, name, type, options);
    }

    @Override
    public Db2 visitTerminator(DB2Parser.TerminatorContext ctx) {
        return new Db2.Empty(randomId(), prefix(ctx), Markers.EMPTY);
    }


    @Override
    public Db2 visitCreateView(DB2Parser.CreateViewContext ctx) {
        Space prefix = prefix(ctx);
        List<Db2.Keyword> keywords = keywords(ctx.CREATE(), ctx.VIEW());
        Db2.Name name = name(ctx.qualifiedName());
        Db2Container<Db2.Name> columns = ctx.columnList() == null ? null : columnList(ctx.columnList());
        Db2LeftPadded<Db2.Query> query = leftPadded(ctx.AS(), () -> query(ctx.queryExpression()));
        return new Db2.CreateView(randomId(), prefix, Markers.EMPTY, keywords, name, columns, query,
                options(ctx.viewOption()));
    }

    @Override
    public Db2 visitCreateAlias(DB2Parser.CreateAliasContext ctx) {
        Space prefix = prefix(ctx);
        List<Db2.Keyword> keywords = keywords(ctx.CREATE(), ctx.PUBLIC(), ctx.ALIAS());
        Db2.Name name = name(ctx.qualifiedName(0));
        // FOR, and the TABLE or SEQUENCE that may qualify it, introduce the target.
        List<Db2.Keyword> forKeywords = keywords(ctx.FOR(), ctx.TABLE(), ctx.SEQUENCE());
        keywords.addAll(forKeywords);
        return new Db2.CreateAlias(randomId(), prefix, Markers.EMPTY, keywords, name,
                Db2LeftPadded.build(name(ctx.qualifiedName(1))));
    }

    @Override
    public Db2 visitCreateSynonym(DB2Parser.CreateSynonymContext ctx) {
        Space prefix = prefix(ctx);
        List<Db2.Keyword> keywords = keywords(ctx.CREATE(), ctx.SYNONYM());
        Db2.Name name = name(ctx.identifier());
        keywords.add(keyword(ctx.FOR()));
        return new Db2.CreateSynonym(randomId(), prefix, Markers.EMPTY, keywords, name,
                Db2LeftPadded.build(name(ctx.qualifiedName())));
    }

    @Override
    public Db2 visitCreateSequence(DB2Parser.CreateSequenceContext ctx) {
        Space prefix = prefix(ctx);
        List<Db2.Keyword> keywords = keywords(ctx.CREATE(), ctx.SEQUENCE());
        Db2.Name name = name(ctx.qualifiedName());
        Db2LeftPadded<Db2.DataType> type = ctx.dataType() == null ? null :
                leftPadded(ctx.AS(), () -> dataType(ctx.dataType()));
        return new Db2.CreateSequence(randomId(), prefix, Markers.EMPTY, keywords, name, type,
                options(ctx.sequenceOption()));
    }

    @Override
    public Db2 visitCreateType(DB2Parser.CreateTypeContext ctx) {
        Space prefix = prefix(ctx);
        List<Db2.Keyword> keywords = keywords(ctx.CREATE(), ctx.DISTINCT(), ctx.TYPE());
        Db2.Name name = name(ctx.qualifiedName());
        Db2LeftPadded<Db2.DataType> type = leftPadded(ctx.AS(), () -> dataType(ctx.dataType()));
        List<Db2> options = new ArrayList<>();
        if (ctx.WITH() != null) {
            options.add(option(ctx));
        }
        return new Db2.CreateType(randomId(), prefix, Markers.EMPTY, keywords, name, type, options);
    }

    @Override
    public Db2 visitDeclareGlobalTemporaryTable(DB2Parser.DeclareGlobalTemporaryTableContext ctx) {
        Space prefix = prefix(ctx);
        List<Db2.Keyword> keywords = keywords(ctx.DECLARE(), ctx.GLOBAL(), ctx.TEMPORARY(), ctx.TABLE());
        Db2.Name name = name(ctx.qualifiedName());
        return new Db2.DeclareGlobalTemporaryTable(randomId(), prefix, Markers.EMPTY, keywords, name,
                tableContents(ctx.tableContents()), options(ctx.tableOption()));
    }

    @Override
    public Db2 visitCreateTrigger(DB2Parser.CreateTriggerContext ctx) {
        Space prefix = prefix(ctx);
        List<Db2.Keyword> keywords = new ArrayList<>();
        Db2.Name name = null;
        Db2.Name table = null;
        List<Db2> events = new ArrayList<>();
        List<Db2> options = new ArrayList<>();
        Db2.Block body = null;

        for (ParseTree child : ctx.children) {
            if (child instanceof DB2Parser.QualifiedNameContext) {
                Db2.Name qualified = name((DB2Parser.QualifiedNameContext) child);
                if (name == null) {
                    name = qualified;
                } else {
                    table = qualified;
                }
            } else if (child instanceof DB2Parser.TriggerEventContext) {
                events.add(option((ParserRuleContext) child));
            } else if (child instanceof DB2Parser.CompoundStatementContext) {
                body = block((DB2Parser.CompoundStatementContext) child);
            } else if (child instanceof DB2Parser.TriggeredStatementContext) {
                // A trigger whose body is a single statement has no BEGIN and no END.
                body = new Db2.Block(randomId(), prefix((ParserRuleContext) child), Markers.EMPTY,
                        emptyList(), new ArrayList<>(words((ParserRuleContext) child)), null, null);
            } else if (child instanceof DB2Parser.EndContext) {
                // read by `terminated`
            } else if (child instanceof TerminalNode) {
                Token token = ((TerminalNode) child).getSymbol();
                if (isKeywordToken(token)) {
                    keywords.add(keyword(token));
                } else {
                    options.add(word(token));
                }
            } else {
                options.add(option((ParserRuleContext) child));
            }
        }
        return new Db2.CreateTrigger(randomId(), prefix, Markers.EMPTY, keywords, name, events,
                table, options, body);
    }

    @Override
    public Db2 visitCreateProcedure(DB2Parser.CreateProcedureContext ctx) {
        return routine(ctx, true, true);
    }

    @Override
    public Db2 visitCreateFunction(DB2Parser.CreateFunctionContext ctx) {
        return routine(ctx, false, true);
    }

    @Override
    public Db2 visitAlterProcedure(DB2Parser.AlterProcedureContext ctx) {
        return routine(ctx, true, false);
    }

    @Override
    public Db2 visitAlterFunction(DB2Parser.AlterFunctionContext ctx) {
        return routine(ctx, false, false);
    }

    private Db2 routine(ParserRuleContext ctx, boolean procedure, boolean creating) {
        Space prefix = prefix(ctx);
        List<Db2.Keyword> keywords = new ArrayList<>();
        Db2.Name name = null;
        List<Db2RightPadded<Db2.Parameter>> parameters = new ArrayList<>();
        Space beforeParameters = Space.EMPTY;
        List<Db2> clauses = new ArrayList<>();
        Db2.Block body = null;

        for (ParseTree child : ctx.children) {
            if (child instanceof DB2Parser.QualifiedNameContext && name == null) {
                name = name((DB2Parser.QualifiedNameContext) child);
            } else if (child instanceof DB2Parser.RoutineParameterContext) {
                Db2.Parameter parameter = parameter((DB2Parser.RoutineParameterContext) child);
                parameters.add(Db2RightPadded.build(parameter).withAfter(whitespace()));
            } else if (child instanceof DB2Parser.RoutineClauseContext) {
                clauses.add(option((ParserRuleContext) child));
            } else if (child instanceof DB2Parser.CompoundStatementContext) {
                body = block((DB2Parser.CompoundStatementContext) child);
            } else if (child instanceof DB2Parser.EndContext) {
                // read by `terminated`
            } else if (child instanceof TerminalNode) {
                Token token = ((TerminalNode) child).getSymbol();
                if (token.getType() == DB2Parser.LPAREN) {
                    beforeParameters = Space.build(source.substring(cursor, token.getStartIndex()));
                    cursor = token.getStopIndex() + 1;
                } else if (token.getType() == DB2Parser.COMMA || token.getType() == DB2Parser.RPAREN) {
                    skip(token.getText());
                } else {
                    keywords.add(keyword(token));
                }
            }
        }
        Db2Container<Db2.Parameter> container =
                Db2Container.build(beforeParameters, parameters, Markers.EMPTY);
        if (creating) {
            return procedure ?
                    new Db2.CreateProcedure(randomId(), prefix, Markers.EMPTY, keywords, name, container, clauses, body) :
                    new Db2.CreateFunction(randomId(), prefix, Markers.EMPTY, keywords, name, container, clauses, body);
        }
        return procedure ?
                new Db2.AlterProcedure(randomId(), prefix, Markers.EMPTY, keywords, name, container, clauses, body) :
                new Db2.AlterFunction(randomId(), prefix, Markers.EMPTY, keywords, name, container, clauses, body);
    }

    private Db2.Parameter parameter(DB2Parser.RoutineParameterContext ctx) {
        Space prefix = prefix(ctx);
        Db2.Keyword mode = null;
        for (ParseTree child : ctx.children) {
            if (child instanceof TerminalNode) {
                mode = keyword((TerminalNode) child);
            }
        }
        Db2.Name name = ctx.identifier() == null ? null : name(ctx.identifier());
        return new Db2.Parameter(randomId(), prefix, Markers.EMPTY, mode, name, dataType(ctx.dataType()));
    }

    private Db2.Block block(DB2Parser.CompoundStatementContext ctx) {
        Space prefix = prefix(ctx);
        List<Db2.Keyword> keywords = keywords(ctx.BEGIN(), ctx.ATOMIC());
        List<Db2> body = new ArrayList<>();
        for (DB2Parser.BodyItemContext item : ctx.bodyItem()) {
            if (item.compoundStatement() != null) {
                body.add(block(item.compoundStatement()));
            } else {
                body.addAll(words(item));
            }
        }
        Db2.Keyword end = keyword(ctx.END());
        Db2.Name label = ctx.identifier() == null ? null : name(ctx.identifier());
        return new Db2.Block(randomId(), prefix, Markers.EMPTY, keywords, body, end, label);
    }

    @Override
    public Db2 visitRenameStatement(DB2Parser.RenameStatementContext ctx) {
        Space prefix = prefix(ctx);
        List<Db2.Keyword> keywords = keywords(ctx.RENAME(), ctx.TABLE(), ctx.INDEX());
        Db2.Name name = name(ctx.qualifiedName());
        keywords.add(keyword(ctx.TO()));
        return new Db2.Rename(randomId(), prefix, Markers.EMPTY, keywords, name,
                Db2LeftPadded.build(name(ctx.identifier())));
    }

    @Override
    public Db2 visitCommentStatement(DB2Parser.CommentStatementContext ctx) {
        Space prefix = prefix(ctx);
        List<Db2.Keyword> keywords = keywords(ctx.COMMENT(), ctx.ON());
        Db2.Name target = commentTarget(ctx.commentTarget(), keywords);
        Db2LeftPadded<Db2.Word> text = leftPadded(ctx.IS(), () -> word(ctx.STRING()));
        return new Db2.Comment(randomId(), prefix, Markers.EMPTY, keywords, target, text);
    }

    @Override
    public Db2 visitLabelStatement(DB2Parser.LabelStatementContext ctx) {
        Space prefix = prefix(ctx);
        List<Db2.Keyword> keywords = keywords(ctx.LABEL(), ctx.ON());
        Db2.Name target = commentTarget(ctx.labelTarget(), keywords);
        Db2LeftPadded<Db2.Word> text = leftPadded(ctx.IS(), () -> word(ctx.STRING()));
        return new Db2.Label(randomId(), prefix, Markers.EMPTY, keywords, target, text);
    }

    /**
     * The object a COMMENT or LABEL is about. Which kind of object it is arrives as keywords, so
     * those join the statement's run and the name comes back on its own.
     */
    private Db2.@Nullable Name commentTarget(ParserRuleContext ctx, List<Db2.Keyword> keywords) {
        Db2.Name target = null;
        for (ParseTree child : ctx.children) {
            if (child instanceof DB2Parser.QualifiedNameContext) {
                target = name((DB2Parser.QualifiedNameContext) child);
            } else if (child instanceof DB2Parser.IdentifierContext) {
                target = name((DB2Parser.IdentifierContext) child);
            } else if (child instanceof TerminalNode) {
                keywords.add(keyword((TerminalNode) child));
            }
        }
        return target;
    }

    @Override
    public Db2 visitGrantStatement(DB2Parser.GrantStatementContext ctx) {
        return grant(ctx, true);
    }

    @Override
    public Db2 visitRevokeStatement(DB2Parser.RevokeStatementContext ctx) {
        return grant(ctx, false);
    }

    /**
     * {@code GRANT <privileges> ON <object> <names> TO <grantees>}. ON and TO sit between the lists,
     * so each is a field of its own rather than part of the leading keyword run.
     */
    private Db2 grant(ParserRuleContext ctx, boolean granting) {
        Space prefix = prefix(ctx);
        List<Db2.Keyword> keywords = new ArrayList<>();
        List<Db2.Keyword> on = new ArrayList<>();
        List<Db2RightPadded<Db2>> privileges = new ArrayList<>();
        List<Db2RightPadded<Db2.Name>> objects = new ArrayList<>();
        List<Db2RightPadded<Db2>> grantees = new ArrayList<>();
        List<Db2> options = new ArrayList<>();
        Db2.Keyword to = null;
        int direction = granting ? DB2Parser.TO : DB2Parser.FROM;
        boolean pastOn = false;

        for (ParseTree child : ctx.children) {
            if (child instanceof DB2Parser.PrivilegeContext) {
                privileges.add(Db2RightPadded.build((Db2) option((ParserRuleContext) child))
                        .withAfter(whitespace()));
            } else if (child instanceof DB2Parser.PrivilegeObjectContext) {
                collectKeywords(child, on);
            } else if (child instanceof DB2Parser.QualifiedNameListContext) {
                for (DB2Parser.QualifiedNameContext q :
                        ((DB2Parser.QualifiedNameListContext) child).qualifiedName()) {
                    objects.add(Db2RightPadded.build(name(q)).withAfter(whitespace()));
                    skip(",");
                }
            } else if (child instanceof DB2Parser.GranteeContext) {
                grantees.add(Db2RightPadded.build((Db2) option((ParserRuleContext) child))
                        .withAfter(whitespace()));
            } else if (child instanceof DB2Parser.EndContext) {
                // read by `terminated`
            } else if (child instanceof TerminalNode) {
                Token token = ((TerminalNode) child).getSymbol();
                if (token.getType() == DB2Parser.COMMA) {
                    skip(",");
                } else if (token.getType() == direction) {
                    to = keyword(token);
                } else if (token.getType() == DB2Parser.ON || token.getType() == DB2Parser.IN) {
                    on.add(keyword(token));
                    pastOn = true;
                } else if (pastOn && to == null) {
                    on.add(keyword(token));
                } else {
                    keywords.add(keyword(token));
                }
            } else {
                options.add(option((ParserRuleContext) child));
            }
        }
        return granting ?
                new Db2.Grant(randomId(), prefix, Markers.EMPTY, keywords,
                        Db2Container.build(privileges), on, Db2Container.build(objects), to,
                        Db2Container.build(grantees), options) :
                new Db2.Revoke(randomId(), prefix, Markers.EMPTY, keywords,
                        Db2Container.build(privileges), on, Db2Container.build(objects), to,
                        Db2Container.build(grantees), options);
    }

    @Override
    public Db2 visitSetStatement(DB2Parser.SetStatementContext ctx) {
        Space prefix = prefix(ctx);
        List<Db2.Keyword> keywords = new ArrayList<>();
        keywords.add(keyword(ctx.SET()));
        // A special register's words arrive as terminals or as `nonReserved` rules, depending on
        // which alternative matched, so they are collected rather than cast.
        collectKeywords(ctx.specialRegister(), keywords);
        Db2LeftPadded<Db2> value = leftPadded(ctx.EQ(), () -> (Db2) query(ctx.expression()));
        return new Db2.Set(randomId(), prefix, Markers.EMPTY, keywords, value);
    }

    @Override
    public Db2 visitRollbackStatement(DB2Parser.RollbackStatementContext ctx) {
        Space prefix = prefix(ctx);
        List<Db2.Keyword> keywords = keywords(ctx.ROLLBACK(), ctx.WORK(), ctx.TO(), ctx.SAVEPOINT());
        Db2LeftPadded<Db2.Name> savepoint = ctx.identifier() == null ? null :
                Db2LeftPadded.build(name(ctx.identifier()));
        return new Db2.Rollback(randomId(), prefix, Markers.EMPTY, keywords, savepoint);
    }

    @Override
    public Db2 visitQueryStatement(DB2Parser.QueryStatementContext ctx) {
        return query(ctx.queryExpression());
    }

    @Override
    public Db2 visitInsertStatement(DB2Parser.InsertStatementContext ctx) {
        Space prefix = prefix(ctx);
        List<Db2.Keyword> keywords = keywords(ctx.INSERT(), ctx.INTO());
        Db2.Name table = name(ctx.qualifiedName());
        Db2Container<Db2.Name> columns = ctx.columnList() == null ? null : columnList(ctx.columnList());
        if (ctx.VALUES() != null) {
            keywords.add(keyword(ctx.VALUES()));
        }
        List<Db2RightPadded<Db2>> values = new ArrayList<>();
        for (DB2Parser.ValuesRowContext row : ctx.valuesRow()) {
            values.add(Db2RightPadded.build((Db2) query(row)).withAfter(whitespace()));
            skip(",");
        }
        if (ctx.queryExpression() != null) {
            values.add(Db2RightPadded.build((Db2) query(ctx.queryExpression())));
        }
        return new Db2.Insert(randomId(), prefix, Markers.EMPTY, keywords, table, columns,
                Db2Container.build(values));
    }

    /**
     * A value and the space before the operator that introduces it. The operator is printed, not
     * stored, so only its prefix is kept.
     * <p>
     * The element arrives as a supplier because it must be read <em>after</em> the operator: Java
     * evaluates arguments left to right, so passing it directly would consume the value first and
     * leave the cursor past an operator still waiting to be read.
     */
    private <T extends Db2> Db2LeftPadded<T> leftPadded(@Nullable TerminalNode operator,
                                                        Supplier<T> element) {
        if (operator == null) {
            return Db2LeftPadded.build(element.get());
        }
        Space before = Space.build(source.substring(cursor, operator.getSymbol().getStartIndex()));
        cursor = operator.getSymbol().getStopIndex() + 1;
        return new Db2LeftPadded<>(before, element.get(), Markers.EMPTY);
    }

    private void collectKeywords(ParseTree tree, List<Db2.Keyword> into) {
        if (tree instanceof TerminalNode) {
            into.add(keyword((TerminalNode) tree));
            return;
        }
        for (int i = 0; i < tree.getChildCount(); i++) {
            collectKeywords(tree.getChild(i), into);
        }
    }

    private List<Db2.Word> words(ParseTree tree) {
        List<Db2.Word> words = new ArrayList<>();
        collectWords(tree, words);
        return words;
    }

    //
    // Shared
    //

    private Db2.Query query(ParserRuleContext ctx) {
        Space prefix = prefix(ctx);
        List<Db2> parts = new ArrayList<>();
        collectQuery(ctx, parts);
        return new Db2.Query(randomId(), prefix, Markers.EMPTY, parts);
    }

    private void collectQuery(ParseTree tree, List<Db2> parts) {
        for (int i = 0; i < tree.getChildCount(); i++) {
            ParseTree child = tree.getChild(i);
            if (child instanceof DB2Parser.QualifiedNameContext) {
                parts.add(name((DB2Parser.QualifiedNameContext) child));
            } else if (child instanceof TerminalNode) {
                parts.add(word((TerminalNode) child));
            } else {
                collectQuery(child, parts);
            }
        }
    }

    /**
     * DB2 writes an option as a keyword and what follows it, so that is what one becomes.
     */
    private Db2.Option option(ParserRuleContext ctx) {
        Space prefix = prefix(ctx);
        List<Db2.Keyword> keywords = new ArrayList<>();
        List<Db2> values = new ArrayList<>();
        collectOption(ctx, keywords, values);
        return new Db2.Option(randomId(), prefix, Markers.EMPTY, keywords, values);
    }

    private void collectOption(ParseTree tree, List<Db2.Keyword> keywords, List<Db2> values) {
        for (int i = 0; i < tree.getChildCount(); i++) {
            ParseTree child = tree.getChild(i);
            if (child instanceof DB2Parser.QualifiedNameContext) {
                values.add(name((DB2Parser.QualifiedNameContext) child));
            } else if (child instanceof DB2Parser.IdentifierContext) {
                values.add(name((DB2Parser.IdentifierContext) child));
            } else if (child instanceof TerminalNode) {
                Token token = ((TerminalNode) child).getSymbol();
                if (values.isEmpty() && isKeyword(token)) {
                    keywords.add(keyword(token));
                } else {
                    values.add(word(token));
                }
            } else {
                collectOption(child, keywords, values);
            }
        }
    }

    private static boolean isKeyword(Token token) {
        return isKeywordToken(token);
    }

    private <T extends ParserRuleContext, R extends Db2> Db2Container<R> container(
            @Nullable TerminalNode open, List<T> elements, List<TerminalNode> separators,
            @Nullable TerminalNode close, java.util.function.Function<T, R> map) {
        Space before = whitespace();
        if (open != null) {
            skip("(");
        }
        List<Db2RightPadded<R>> padded = new ArrayList<>(elements.size());
        for (int i = 0; i < elements.size(); i++) {
            R element = map.apply(elements.get(i));
            Space after = whitespace();
            if (i < separators.size()) {
                skip(",");
            }
            padded.add(Db2RightPadded.build(element).withAfter(after));
        }
        if (close != null) {
            skip(")");
        }
        return Db2Container.build(before, padded, Markers.EMPTY);
    }

    private List<Db2> options(List<? extends ParserRuleContext> contexts) {
        List<Db2> options = new ArrayList<>(contexts.size());
        for (ParserRuleContext ctx : contexts) {
            options.add(option(ctx));
        }
        return options;
    }

    private List<Db2.Keyword> keywords(TerminalNode... nodes) {
        List<Db2.Keyword> keywords = new ArrayList<>(nodes.length);
        for (TerminalNode node : nodes) {
            if (node != null) {
                keywords.add(keyword(node));
            }
        }
        return keywords;
    }

    private Db2.Name name(DB2Parser.QualifiedNameContext ctx) {
        Space prefix = prefix(ctx);
        List<Db2.Word> parts = new ArrayList<>();
        collectWords(ctx, parts);
        return new Db2.Name(randomId(), prefix, Markers.EMPTY, parts);
    }

    private Db2.Name name(DB2Parser.IdentifierContext ctx) {
        Space prefix = prefix(ctx);
        List<Db2.Word> parts = new ArrayList<>(1);
        parts.add(word(ctx.getStart()));
        return new Db2.Name(randomId(), prefix, Markers.EMPTY, parts);
    }

    private void collectWords(ParseTree tree, List<Db2.Word> into) {
        if (tree instanceof TerminalNode) {
            into.add(word((TerminalNode) tree));
            return;
        }
        for (int i = 0; i < tree.getChildCount(); i++) {
            collectWords(tree.getChild(i), into);
        }
    }

    /**
     * The keyword a token names. ANTLR's own vocabulary supplies the symbolic name, so the enum and
     * the lexer cannot drift apart.
     */
    private Db2.Keyword keyword(TerminalNode node) {
        return keyword(node.getSymbol());
    }

    private Db2.Keyword keyword(Token token) {
        Space prefix = Space.build(source.substring(cursor, token.getStartIndex()));
        cursor = token.getStopIndex() + 1;
        return new Db2.Keyword(randomId(), prefix, Markers.EMPTY, typeOf(token), token.getText());
    }

    private static final Map<Integer, Db2.Keyword.Type> KEYWORDS = keywordTypes();

    /**
     * Token type to keyword, built once from ANTLR's own vocabulary so the enum and the lexer cannot
     * drift. Punctuation and literals have symbolic names too and are not keywords, so they are
     * simply absent.
     */
    private static Map<Integer, Db2.Keyword.Type> keywordTypes() {
        Map<String, Db2.Keyword.Type> byName = new HashMap<>();
        for (Db2.Keyword.Type type : Db2.Keyword.Type.values()) {
            byName.put(type.name().toUpperCase(Locale.ROOT), type);
        }
        Map<Integer, Db2.Keyword.Type> byToken = new HashMap<>();
        for (int token = 0; token <= DB2Parser.VOCABULARY.getMaxTokenType(); token++) {
            String symbolic = DB2Parser.VOCABULARY.getSymbolicName(token);
            if (symbolic != null) {
                Db2.Keyword.Type type = byName.get(symbolic.replace("_", ""));
                if (type != null) {
                    byToken.put(token, type);
                }
            }
        }
        return byToken;
    }

    private static Db2.Keyword.Type typeOf(Token token) {
        Db2.Keyword.Type type = KEYWORDS.get(token.getType());
        if (type == null) {
            throw new IllegalStateException("Not a keyword: " + token.getText());
        }
        return type;
    }

    private static boolean isKeywordToken(Token token) {
        return KEYWORDS.containsKey(token.getType());
    }

    private Db2.Word word(TerminalNode node) {
        return word(node.getSymbol());
    }

    private Db2.Word word(Token token) {
        Space prefix = Space.build(source.substring(cursor, token.getStartIndex()));
        cursor = token.getStopIndex() + 1;
        return new Db2.Word(randomId(), prefix, Markers.EMPTY, token.getText());
    }

    /**
     * The white space and comments in front of a rule, without taking its first token, so the node
     * carries the indentation and its first child carries nothing.
     */
    private Space prefix(ParserRuleContext ctx) {
        int start = ctx.getStart().getStartIndex();
        Space prefix = Space.build(source.substring(cursor, start));
        cursor = start;
        return prefix;
    }

    private Space whitespace() {
        int end = cursor;
        while (end < source.length() && Character.isWhitespace(source.charAt(end))) {
            end++;
        }
        Space space = Space.build(source.substring(cursor, end));
        cursor = end;
        return space;
    }

    private Space sourceBefore(String token) {
        Space prefix = whitespace();
        skip(token);
        return prefix;
    }

    private void skip(String token) {
        if (source.startsWith(token, cursor)) {
            cursor += token.length();
        }
    }
}
