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
package org.openrewrite.linkedit.trait;

import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.controlcard.CardLines;
import org.openrewrite.linkedit.LinkEditIsoVisitor;
import org.openrewrite.linkedit.tree.LinkEdit;
import org.openrewrite.linkedit.tree.Space;
import org.openrewrite.linkedit.tree.Statement;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.util.ArrayList;
import java.util.List;

/**
 * A deck of binder control statements, read for the load module it builds rather than for how it is
 * written.
 * <p>
 * A link-edit deck is the only place a load module's composition is written down: {@code NAME} says
 * what the module is called, {@code ENTRY} where control arrives, {@code ALIAS} what else the module
 * answers to, and each {@code INCLUDE} names an object the module is built from. Nothing in the COBOL
 * or in the JCL says any of it — a step names the module, and the module names its programs here.
 */
@Value
public class LinkEditDeck implements Trait<LinkEdit.CompilationUnit> {

    Cursor cursor;

    /**
     * The load module the deck builds, from {@code NAME}. Null for a deck that only adds to a module
     * built elsewhere.
     */
    public @Nullable Name getModule() {
        LinkEdit.Operand name = firstOperand("NAME");
        return name == null ? null : named(name);
    }

    /**
     * Whether the module replaces one already in the library, from the {@code (R)} a {@code NAME}
     * carries.
     */
    public boolean isReplacing() {
        LinkEdit.Operand name = firstOperand("NAME");
        return name != null && "R".equalsIgnoreCase(unwrap(name.getValueText()));
    }

    /**
     * Where control arrives when the module is given control, from {@code ENTRY}. This is a CSECT in
     * one of the included objects, and for a DL/I program the {@code DLITCBL} label it declares rather
     * than the program's own name.
     */
    public @Nullable Name getEntry() {
        LinkEdit.Operand entry = firstOperand("ENTRY");
        return entry == null ? null : named(entry);
    }

    /**
     * The other names the module answers to, from {@code ALIAS}. An alias is a directory entry of its
     * own, so a step or a {@code CALL} naming one finds this module.
     */
    public List<Name> getAliases() {
        List<Name> aliases = new ArrayList<>();
        for (LinkEdit.ControlStatement statement : statements("ALIAS")) {
            for (LinkEdit.Operand operand : statement.getParameters()) {
                aliases.add(named(operand));
            }
        }
        return aliases;
    }

    /**
     * The members the module is built from, from {@code INCLUDE ddname(member,...)}.
     * <p>
     * Inclusion is what static binding means: a subroutine included here is part of this module and
     * its caller reaches it without the system ever looking the name up. A subroutine the deck leaves
     * out is called dynamically and is a module of its own, so a deck says as much by what it omits.
     * The same statement reaches a shop's own object library and an IBM stub, so the DD name is
     * carried rather than judged.
     */
    public List<Include> getIncludes() {
        List<Include> includes = new ArrayList<>();
        for (LinkEdit.ControlStatement statement : statements("INCLUDE")) {
            for (LinkEdit.Operand operand : statement.getParameters()) {
                int line = lineOf(operand);
                for (String member : split(unwrap(operand.getValueText()))) {
                    includes.add(new Include(operand.getKeyword().getText(), member, line));
                }
            }
        }
        return includes;
    }

    private List<LinkEdit.ControlStatement> statements(String operator) {
        List<LinkEdit.ControlStatement> statements = new ArrayList<>();
        for (Statement statement : getTree().getStatements()) {
            if (statement instanceof LinkEdit.ControlStatement &&
                ((LinkEdit.ControlStatement) statement).isOperator(operator)) {
                statements.add((LinkEdit.ControlStatement) statement);
            }
        }
        return statements;
    }

    /**
     * The first operand of the first statement written under this operator.
     */
    private LinkEdit.@Nullable Operand firstOperand(String operator) {
        for (LinkEdit.ControlStatement statement : statements(operator)) {
            if (!statement.getParameters().isEmpty()) {
                return statement.getParameters().get(0);
            }
        }
        return null;
    }

    private Name named(LinkEdit.Operand operand) {
        return new Name(operand.getKeyword().getText(), lineOf(operand));
    }

    private int lineOf(LinkEdit.Operand operand) {
        return CardLines.of(cursor, LinkEdit.CompilationUnit.class, words())
                .getOrDefault(operand.getKeyword().getId(), 1);
    }

    /**
     * Where the words of a deck are, for {@link CardLines} to count the cards between them.
     */
    private static LinkEditIsoVisitor<CardLines> words() {
        return new LinkEditIsoVisitor<CardLines>() {
            @Override
            public Space visitSpace(Space space, Space.Location location, CardLines lines) {
                lines.space(space.getWhitespace());
                return space;
            }

            @Override
            public LinkEdit.Word visitWord(LinkEdit.Word word, CardLines lines) {
                LinkEdit.Word w = super.visitWord(word, lines);
                lines.word(w.getId());
                return w;
            }
        };
    }

    /**
     * The value without the parentheses that hold it.
     */
    private static String unwrap(String value) {
        String text = value.trim();
        if (text.startsWith("(")) {
            text = text.substring(1);
        }
        if (text.endsWith(")")) {
            text = text.substring(0, text.length() - 1);
        }
        return text.trim();
    }

    private static List<String> split(String value) {
        List<String> names = new ArrayList<>();
        for (String name : value.split("[,\\s]+")) {
            if (!name.isEmpty()) {
                names.add(name);
            }
        }
        return names;
    }

    /**
     * A name the deck writes down, and the one-based line of the deck it was written on.
     */
    @Value
    public static class Name {
        String text;
        int line;
    }

    /**
     * A member the module is built from: the DD it is read from, the member itself, and the one-based
     * line of the deck the statement was written on.
     */
    @Value
    public static class Include {
        String ddName;
        String member;
        int line;
    }

    public static class Matcher extends SimpleTraitMatcher<LinkEditDeck> {

        @Override
        protected @Nullable LinkEditDeck test(Cursor cursor) {
            return cursor.getValue() instanceof LinkEdit.CompilationUnit ? new LinkEditDeck(cursor) : null;
        }
    }

    @Override
    public String toString() {
        Name module = getModule();
        return module == null ? "LINKEDIT" : "LINKEDIT " + module.getText();
    }
}
