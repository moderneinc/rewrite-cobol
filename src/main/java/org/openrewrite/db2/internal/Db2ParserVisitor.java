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
import org.openrewrite.db2.tree.Db2;
import org.openrewrite.db2.tree.Space;
import org.openrewrite.db2.tree.Statement;
import org.openrewrite.marker.Markers;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static org.openrewrite.Tree.randomId;
import static org.openrewrite.db2.tree.Space.EMPTY;

/**
 * Builds the LST by walking the parse tree in source order and taking every prefix from the gap
 * between the cursor and the next token. A comment is not a node here, so it arrives in the prefix
 * of whatever follows it, and the file prints back byte for byte without comments being modelled.
 */
@RequiredArgsConstructor
public class Db2ParserVisitor extends DB2ParserBaseVisitor<Db2> {

    private final Path path;
    private final @Nullable FileAttributes fileAttributes;
    private final String source;
    private final Charset charset;
    private final boolean charsetBomMarked;

    private int cursor = 0;

    @Override
    public Db2.CompilationUnit visitCompilationUnit(DB2Parser.CompilationUnitContext ctx) {
        List<Statement> statements = new ArrayList<>(ctx.statement().size());
        for (DB2Parser.StatementContext statement : ctx.statement()) {
            statements.add(statement(statement));
        }
        return new Db2.CompilationUnit(
                randomId(),
                path,
                fileAttributes,
                EMPTY,
                Markers.EMPTY,
                charset.name(),
                charsetBomMarked,
                null,
                statements,
                Space.build(source.substring(cursor))
        );
    }

    private Statement statement(DB2Parser.StatementContext ctx) {
        if (ctx.createTable() != null) {
            return createTable(ctx.createTable());
        }
        if (ctx.createIndex() != null) {
            return createIndex(ctx.createIndex());
        }
        if (ctx.alterTable() != null) {
            return alterTable(ctx.alterTable());
        }
        return new Db2.Unknown(randomId(), prefix(ctx), Markers.EMPTY, words(ctx));
    }

    private Db2.CreateTable createTable(DB2Parser.CreateTableContext ctx) {
        Space prefix = prefix(ctx);
        List<Db2.Word> keywords = new ArrayList<>(2);
        Db2.Name name = null;
        Db2.Word lParen = null;
        Db2.Word rParen = null;
        List<Db2> elements = new ArrayList<>();
        List<Db2> options = new ArrayList<>();
        Db2.Word end = null;

        for (ParseTree child : ctx.children) {
            if (child instanceof DB2Parser.QualifiedNameContext) {
                name = name((DB2Parser.QualifiedNameContext) child);
            } else if (child instanceof DB2Parser.TableElementContext) {
                elements.add(tableElement((DB2Parser.TableElementContext) child));
            } else if (child instanceof DB2Parser.TableOptionContext) {
                options.addAll(tableOption((DB2Parser.TableOptionContext) child));
            } else if (child instanceof TerminalNode) {
                Token token = ((TerminalNode) child).getSymbol();
                switch (token.getType()) {
                    case DB2Parser.LPAREN:
                        lParen = word(token);
                        break;
                    case DB2Parser.RPAREN:
                        rParen = word(token);
                        break;
                    case DB2Parser.COMMA:
                        elements.add(word(token));
                        break;
                    case DB2Parser.SEMI:
                        end = word(token);
                        break;
                    default:
                        keywords.add(word(token));
                }
            }
        }
        return new Db2.CreateTable(randomId(), prefix, Markers.EMPTY, keywords,
                requireNonNull(name), requireNonNull(lParen), elements, requireNonNull(rParen),
                options, end);
    }

    private Db2 tableElement(DB2Parser.TableElementContext ctx) {
        return ctx.columnDefinition() != null ?
                columnDefinition(ctx.columnDefinition()) :
                constraint(ctx.tableConstraint());
    }

    private Db2.ColumnDefinition columnDefinition(DB2Parser.ColumnDefinitionContext ctx) {
        Space prefix = prefix(ctx);
        Db2.Name name = name(ctx.identifier());
        Db2.DataType type = dataType(ctx.dataType());
        List<Db2.Word> attributes = new ArrayList<>();
        for (DB2Parser.ColumnAttributeContext attribute : ctx.columnAttribute()) {
            attributes.addAll(words(attribute));
        }
        return new Db2.ColumnDefinition(randomId(), prefix, Markers.EMPTY, name, type, attributes);
    }

    private Db2.DataType dataType(DB2Parser.DataTypeContext ctx) {
        Space prefix = prefix(ctx);
        Db2.Name name = name(ctx.identifier());
        List<Db2.Word> arguments = new ArrayList<>(5);
        for (ParseTree child : ctx.children) {
            if (child instanceof TerminalNode) {
                arguments.add(word((TerminalNode) child));
            }
        }
        return new Db2.DataType(randomId(), prefix, Markers.EMPTY, name, arguments);
    }

    /**
     * The optional {@code CONSTRAINT name} and whichever of the four constraint bodies follows it,
     * read as one node. Walking the children in order is what keeps a check expression's words in
     * front of the options written after it.
     */
    private Db2.Constraint constraint(DB2Parser.TableConstraintContext ctx) {
        Space prefix = prefix(ctx);
        List<Db2.Word> keywords = new ArrayList<>(4);
        List<Db2.Word> references = new ArrayList<>(1);
        List<Db2.Word> options = new ArrayList<>();
        Db2.ColumnList columns = null;
        Db2.Name referencedTable = null;
        Db2.ColumnList referencedColumns = null;

        List<ParseTree> children = new ArrayList<>(ctx.children);
        DB2Parser.ConstraintBodyContext body = ctx.constraintBody();
        children.remove(body);
        children.addAll(body.children);

        for (ParseTree child : children) {
            if (child instanceof DB2Parser.ColumnListContext) {
                Db2.ColumnList columnList = columnList((DB2Parser.ColumnListContext) child);
                if (columns == null) {
                    columns = columnList;
                } else {
                    referencedColumns = columnList;
                }
            } else if (child instanceof DB2Parser.QualifiedNameContext) {
                referencedTable = name((DB2Parser.QualifiedNameContext) child);
            } else if (child instanceof DB2Parser.IdentifierContext) {
                keywords.add(word(((DB2Parser.IdentifierContext) child).getStart()));
            } else if (child instanceof TerminalNode) {
                Token token = ((TerminalNode) child).getSymbol();
                if (token.getType() == DB2Parser.REFERENCES) {
                    references.add(word(token));
                } else {
                    keywords.add(word(token));
                }
            } else {
                // A check expression's parentheses, and the words of ON DELETE and the rest.
                options.addAll(words(child));
            }
        }
        return new Db2.Constraint(randomId(), prefix, Markers.EMPTY, keywords, columns,
                references, referencedTable, referencedColumns, options);
    }

    private Db2.ColumnList columnList(DB2Parser.ColumnListContext ctx) {
        Space prefix = prefix(ctx);
        Db2.Word lParen = null;
        Db2.Word rParen = null;
        List<Db2> names = new ArrayList<>();
        for (ParseTree child : ctx.children) {
            if (child instanceof DB2Parser.IdentifierContext) {
                names.add(name((DB2Parser.IdentifierContext) child));
            } else {
                Token token = ((TerminalNode) child).getSymbol();
                if (token.getType() == DB2Parser.LPAREN) {
                    lParen = word(token);
                } else if (token.getType() == DB2Parser.RPAREN) {
                    rParen = word(token);
                } else {
                    names.add(word(token));
                }
            }
        }
        return new Db2.ColumnList(randomId(), prefix, Markers.EMPTY,
                requireNonNull(lParen), names, requireNonNull(rParen));
    }

    /**
     * {@code IN} names the tablespace, so its operand becomes a {@link Db2.Name} among the options
     * and everything else stays a word. That is the whole of what {@code CreateTable#getTablespace}
     * looks for.
     */
    private List<Db2> tableOption(DB2Parser.TableOptionContext ctx) {
        if (ctx.qualifiedName() == null) {
            return new ArrayList<>(words(ctx));
        }
        List<Db2> option = new ArrayList<>(2);
        option.add(word(ctx.IN()));
        option.add(name(ctx.qualifiedName()));
        return option;
    }

    private Db2.CreateIndex createIndex(DB2Parser.CreateIndexContext ctx) {
        Space prefix = prefix(ctx);
        List<Db2.Word> keywords = new ArrayList<>(3);
        Db2.Name name = null;
        Db2.Word on = null;
        Db2.Name table = null;
        Db2.Word lParen = null;
        Db2.Word rParen = null;
        List<Db2> keys = new ArrayList<>();
        List<Db2> options = new ArrayList<>();
        Db2.Word end = null;

        for (ParseTree child : ctx.children) {
            if (child instanceof DB2Parser.QualifiedNameContext) {
                Db2.Name qualified = name((DB2Parser.QualifiedNameContext) child);
                if (name == null) {
                    name = qualified;
                } else {
                    table = qualified;
                }
            } else if (child instanceof DB2Parser.IndexModifierContext) {
                keywords.add(word(((DB2Parser.IndexModifierContext) child).getStart()));
            } else if (child instanceof DB2Parser.IndexKeyContext) {
                keys.add(indexKey((DB2Parser.IndexKeyContext) child));
            } else if (child instanceof DB2Parser.IndexOptionContext) {
                options.addAll(words(child));
            } else if (child instanceof TerminalNode) {
                Token token = ((TerminalNode) child).getSymbol();
                switch (token.getType()) {
                    case DB2Parser.ON:
                        on = word(token);
                        break;
                    case DB2Parser.LPAREN:
                        lParen = word(token);
                        break;
                    case DB2Parser.RPAREN:
                        rParen = word(token);
                        break;
                    case DB2Parser.COMMA:
                        keys.add(word(token));
                        break;
                    case DB2Parser.SEMI:
                        end = word(token);
                        break;
                    default:
                        keywords.add(word(token));
                }
            }
        }
        return new Db2.CreateIndex(randomId(), prefix, Markers.EMPTY, keywords, requireNonNull(name),
                requireNonNull(on), requireNonNull(table), requireNonNull(lParen), keys,
                requireNonNull(rParen), options, end);
    }

    private Db2.IndexKey indexKey(DB2Parser.IndexKeyContext ctx) {
        Space prefix = prefix(ctx);
        Db2.Name name = name(ctx.identifier());
        Db2.Word direction = null;
        for (ParseTree child : ctx.children) {
            if (child instanceof TerminalNode) {
                direction = word((TerminalNode) child);
            }
        }
        return new Db2.IndexKey(randomId(), prefix, Markers.EMPTY, name, direction);
    }

    private Db2.AlterTable alterTable(DB2Parser.AlterTableContext ctx) {
        Space prefix = prefix(ctx);
        List<Db2.Word> keywords = new ArrayList<>(2);
        Db2.Name name = null;
        List<Db2> actions = new ArrayList<>();
        Db2.Word end = null;

        for (ParseTree child : ctx.children) {
            if (child instanceof DB2Parser.QualifiedNameContext) {
                name = name((DB2Parser.QualifiedNameContext) child);
            } else if (child instanceof DB2Parser.AlterActionContext) {
                actions.addAll(alterAction((DB2Parser.AlterActionContext) child));
            } else if (child instanceof TerminalNode) {
                Token token = ((TerminalNode) child).getSymbol();
                if (token.getType() == DB2Parser.SEMI) {
                    end = word(token);
                } else {
                    keywords.add(word(token));
                }
            }
        }
        return new Db2.AlterTable(randomId(), prefix, Markers.EMPTY, keywords,
                requireNonNull(name), actions, end);
    }

    private List<Db2> alterAction(DB2Parser.AlterActionContext ctx) {
        if (ctx.tableConstraint() == null) {
            return new ArrayList<>(words(ctx));
        }
        List<Db2> action = new ArrayList<>(2);
        if (ctx.ADD() != null) {
            action.add(word(ctx.ADD()));
        }
        action.add(constraint(ctx.tableConstraint()));
        return action;
    }

    private Db2.Name name(DB2Parser.QualifiedNameContext ctx) {
        return new Db2.Name(randomId(), prefix(ctx), Markers.EMPTY, words(ctx));
    }

    private Db2.Name name(DB2Parser.IdentifierContext ctx) {
        Space prefix = prefix(ctx);
        List<Db2.Word> parts = new ArrayList<>(1);
        parts.add(word(ctx.getStart()));
        return new Db2.Name(randomId(), prefix, Markers.EMPTY, parts);
    }

    /**
     * Every token under {@code tree}, in source order. This is how water becomes words: a rule this
     * grammar does not read is flattened rather than shaped.
     */
    private List<Db2.Word> words(ParseTree tree) {
        List<Db2.Word> words = new ArrayList<>();
        collect(tree, words);
        return words.isEmpty() ? emptyList() : words;
    }

    private void collect(ParseTree tree, List<Db2.Word> into) {
        if (tree instanceof TerminalNode) {
            into.add(word((TerminalNode) tree));
            return;
        }
        for (int i = 0; i < tree.getChildCount(); i++) {
            collect(tree.getChild(i), into);
        }
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
     * Takes the white space and comments in front of a rule without taking its first token, so that
     * the node carries the indentation and its first word carries nothing.
     */
    private Space prefix(ParserRuleContext ctx) {
        int start = ctx.getStart().getStartIndex();
        Space prefix = Space.build(source.substring(cursor, start));
        cursor = start;
        return prefix;
    }
}
