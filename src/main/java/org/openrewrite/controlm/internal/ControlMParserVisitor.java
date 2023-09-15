/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.controlm.internal;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.openrewrite.FileAttributes;
import org.openrewrite.controlm.internal.grammar.ControlMParser;
import org.openrewrite.controlm.internal.grammar.ControlMParserBaseVisitor;
import org.openrewrite.controlm.marker.Column;
import org.openrewrite.controlm.tree.ControlM;
import org.openrewrite.controlm.tree.ControlMLeftPadded;
import org.openrewrite.controlm.tree.Section;
import org.openrewrite.controlm.tree.Space;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.marker.Markers;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.openrewrite.Tree.randomId;

public class ControlMParserVisitor extends ControlMParserBaseVisitor<ControlM> {

    private final Path path;
    private final @Nullable FileAttributes fileAttributes;
    private final String source;
    private final Charset charset;
    private final boolean charsetBomMarked;

    private int cursor = 0;

    public ControlMParserVisitor(Path path,
                            @Nullable FileAttributes fileAttributes,
                            String source,
                            Charset charset,
                            boolean charsetBomMarked) {
        this.path = path;
        this.fileAttributes = fileAttributes;
        this.source = source;
        this.charset = charset;
        this.charsetBomMarked = charsetBomMarked;
    }

    public <T> T visitNullable(@Nullable ParseTree tree) {
        if (tree == null) {
            //noinspection ConstantConditions
            return null;
        }
        //noinspection unchecked
        return (T) super.visit(tree);
    }

    @Override
    public ControlM.CompilationUnit visitCompilationUnit(ControlMParser.CompilationUnitContext ctx) {
        Space prefix = whitespace();
        List<Section> sections = new ArrayList<>(6);
        if (ctx.definitionSection() != null) {
            sections.add(visitDefinitionSection(ctx.definitionSection()));
        }
        if (ctx.scheduleSection() != null) {
            sections.add(visitScheduleSection(ctx.scheduleSection()));
        }
        if (ctx.inputSection() != null) {
            sections.add(visitInputSection(ctx.inputSection()));
        }
        if (ctx.outputSection() != null) {
            sections.add(visitOutputSection(ctx.outputSection()));
        }
        if (ctx.applicationFormSection() != null) {
            sections.add(visitApplicationFormSection(ctx.applicationFormSection()));
        }
        return new ControlM.CompilationUnit(
                randomId(),
                path,
                fileAttributes,
                prefix,
                Markers.EMPTY,
                charset.name(),
                charsetBomMarked,
                null,
                sections,
                Space.build(source.substring(cursor))
        );
    }

    @Override
    public Section visitDefinitionSection(ControlMParser.DefinitionSectionContext ctx) {
        return new ControlM.DefinitionSection(
                randomId(),
                sourceBefore("+---------------------------------- BROWSE -----------------------------------+"),
                Markers.EMPTY,
                convertAll(ctx.definitionLine())
        );
    }

    @Override
    public ControlM visitDefinitionLine(ControlMParser.DefinitionLineContext ctx) {
        if (ctx.memLine() != null) {
            return visit(ctx.memLine());
        } else if (ctx.ownerLine() != null) {
            return visit(ctx.ownerLine());
        } else if (ctx.applLine() != null) {
            return visit(ctx.applLine());
        } else if (ctx.descLine() != null) {
            return visit(ctx.descLine());
        } else if (ctx.overlibLine() != null) {
            return visit(ctx.overlibLine());
        } else if (ctx.schenvLine() != null) {
            return visit(ctx.schenvLine());
        } else if (ctx.setVarLine() != null) {
            return visit(ctx.setVarLine());
        } else if (ctx.ctbSetLine() != null) {
            return visit(ctx.ctbSetLine());
        } else if (ctx.docLine() != null) {
            return visit(ctx.docLine());
        } else {
            Space prefix = whitespace();
            Markers markers = Markers.EMPTY;
            markers = markers.addIfAbsent(mapColumn(Column.Location.START));
            markers = markers.addIfAbsent(mapColumn(Column.Location.END));
            return new ControlM.Line(
                    randomId(),
                    prefix,
                    markers,
                    emptyList()
            );
        }
    }

    @Override
    public ControlM.Line visitMemLine(ControlMParser.MemLineContext ctx) {
        Space prefix = whitespace();
        Markers markers = Markers.EMPTY;
        markers = markers.addIfAbsent(mapColumn(Column.Location.START));
        List<ControlM> parameters = convertAll(Arrays.asList(ctx.memName(), ctx.memLib()));
        markers = markers.addIfAbsent(mapColumn(Column.Location.END));
        return new ControlM.Line(
                randomId(),
                prefix,
                markers,
                parameters
        );
    }

    @Override
    public ControlM.Parameter visitMemName(ControlMParser.MemNameContext ctx) {
        return new ControlM.Parameter(
                randomId(),
                sourceBefore(ctx.DEFINITION_MEMNAME().getText()),
                Markers.EMPTY,
                ctx.DEFINITION_MEMNAME().getText(),
                visitNullable(ctx.name())
        );
    }

    @Override
    public ControlM.Parameter visitMemLib(ControlMParser.MemLibContext ctx) {
        return new ControlM.Parameter(
                randomId(),
                sourceBefore(ctx.DEFINITION_MEMLIB().getText()),
                Markers.EMPTY,
                ctx.DEFINITION_MEMLIB().getText(),
                visitNullable(ctx.name())
        );
    }

    @Override
    public ControlM visitOwnerLine(ControlMParser.OwnerLineContext ctx) {
        Space prefix = whitespace();
        Markers markers = Markers.EMPTY;
        markers = markers.addIfAbsent(mapColumn(Column.Location.START));
        List<ControlM> parameters = convertAll(Arrays.asList(ctx.owner(), ctx.taskType(), ctx.preventNc2(), ctx.dflt()));
        markers = markers.addIfAbsent(mapColumn(Column.Location.END));
        return new ControlM.Line(
                randomId(),
                prefix,
                markers,
                parameters
        );
    }

    @Override
    public ControlM visitOwner(ControlMParser.OwnerContext ctx) {
        return new ControlM.Parameter(
                randomId(),
                sourceBefore(ctx.DEFINITION_OWNER().getText()),
                Markers.EMPTY,
                ctx.DEFINITION_OWNER().getText(),
                visitNullable(ctx.name())
        );
    }

    @Override
    public ControlM visitTaskType(ControlMParser.TaskTypeContext ctx) {
        return new ControlM.Parameter(
                randomId(),
                sourceBefore(ctx.DEFINITION_TASKTYPE().getText()),
                Markers.EMPTY,
                ctx.DEFINITION_TASKTYPE().getText(),
                visitNullable(ctx.name())
        );
    }

    @Override
    public ControlM visitPreventNc2(ControlMParser.PreventNc2Context ctx) {
        return new ControlM.Parameter(
                randomId(),
                sourceBefore(ctx.DEFINITION_PREVENT_NCT2().getText()),
                Markers.EMPTY,
                ctx.DEFINITION_PREVENT_NCT2().getText(),
                visitNullable(ctx.name())
        );
    }

    @Override
    public ControlM visitDflt(ControlMParser.DfltContext ctx) {
        return new ControlM.Parameter(
                randomId(),
                sourceBefore(ctx.DEFINITION_DFLT().getText()),
                Markers.EMPTY,
                ctx.DEFINITION_DFLT().getText(),
                visitNullable(ctx.name())
        );
    }

    @Override
    public ControlM visitApplLine(ControlMParser.ApplLineContext ctx) {
        Space prefix = whitespace();
        Markers markers = Markers.EMPTY;
        markers = markers.addIfAbsent(mapColumn(Column.Location.START));
        List<ControlM> parameters = convertAll(Arrays.asList(ctx.appl(), ctx.group()));
        markers = markers.addIfAbsent(mapColumn(Column.Location.END));
        return new ControlM.Line(
                randomId(),
                prefix,
                markers,
                parameters
        );
    }

    @Override
    public ControlM visitAppl(ControlMParser.ApplContext ctx) {
        return new ControlM.Parameter(
                randomId(),
                sourceBefore(ctx.DEFINITION_APPL().getText()),
                Markers.EMPTY,
                ctx.DEFINITION_APPL().getText(),
                visitNullable(ctx.name())
        );
    }

    @Override
    public ControlM visitGroup(ControlMParser.GroupContext ctx) {
        return new ControlM.Parameter(
                randomId(),
                sourceBefore(ctx.DEFINITION_GROUP().getText()),
                Markers.EMPTY,
                ctx.DEFINITION_GROUP().getText(),
                visitNullable(ctx.name())
        );
    }

    @Override
    public ControlM.Line visitDescLine(ControlMParser.DescLineContext ctx) {
        Space prefix = whitespace();
        Markers markers = Markers.EMPTY;
        markers = markers.addIfAbsent(mapColumn(Column.Location.START));

        ControlM.Description description = new ControlM.Description(
                randomId(),
                sourceBefore(ctx.DEFINITION_DESC().getText()),
                Markers.EMPTY,
                ctx.DEFINITION_DESC().getText(),
                convertAll(ctx.name())
        );

        markers = markers.addIfAbsent(mapColumn(Column.Location.END));
        return new ControlM.Line(
                randomId(),
                prefix,
                markers,
                singletonList(description)
        );
    }

    @Override
    public ControlM visitOverlibLine(ControlMParser.OverlibLineContext ctx) {
        Space prefix = whitespace();
        Markers markers = Markers.EMPTY;
        markers = markers.addIfAbsent(mapColumn(Column.Location.START));
        List<ControlM> parameters = convertAll(Arrays.asList(ctx.overlib(), ctx.statCal()));
        markers = markers.addIfAbsent(mapColumn(Column.Location.END));
        return new ControlM.Line(
                randomId(),
                prefix,
                markers,
                parameters
        );
    }

    @Override
    public ControlM visitOverlib(ControlMParser.OverlibContext ctx) {
        return new ControlM.Parameter(
                randomId(),
                sourceBefore(ctx.DEFINITION_OVERLIB().getText()),
                Markers.EMPTY,
                ctx.DEFINITION_OVERLIB().getText(),
                visitNullable(ctx.name())
        );
    }

    @Override
    public ControlM visitStatCal(ControlMParser.StatCalContext ctx) {
        return new ControlM.Parameter(
                randomId(),
                sourceBefore(ctx.DEFINITION_STAT_CAL().getText()),
                Markers.EMPTY,
                ctx.DEFINITION_STAT_CAL().getText(),
                visitNullable(ctx.name())
        );
    }

    @Override
    public ControlM visitSchenvLine(ControlMParser.SchenvLineContext ctx) {
        Space prefix = whitespace();
        Markers markers = Markers.EMPTY;
        markers = markers.addIfAbsent(mapColumn(Column.Location.START));
        List<ControlM> parameters = convertAll(Arrays.asList(ctx.schenv(), ctx.systemId(), ctx.njeNode()));
        markers = markers.addIfAbsent(mapColumn(Column.Location.END));
        return new ControlM.Line(
                randomId(),
                prefix,
                markers,
                parameters
        );
    }

    @Override
    public ControlM visitSchenv(ControlMParser.SchenvContext ctx) {
        return new ControlM.Parameter(
                randomId(),
                sourceBefore(ctx.DEFINITION_SCHENV().getText()),
                Markers.EMPTY,
                ctx.DEFINITION_SCHENV().getText(),
                visitNullable(ctx.name())
        );
    }

    @Override
    public ControlM visitSystemId(ControlMParser.SystemIdContext ctx) {
        return new ControlM.Parameter(
                randomId(),
                sourceBefore(ctx.DEFINITION_SYSTEM_ID().getText()),
                Markers.EMPTY,
                ctx.DEFINITION_SYSTEM_ID().getText(),
                visitNullable(ctx.name())
        );
    }

    @Override
    public ControlM visitNjeNode(ControlMParser.NjeNodeContext ctx) {
        return new ControlM.Parameter(
                randomId(),
                sourceBefore(ctx.DEFINITION_NJE_NODE().getText()),
                Markers.EMPTY,
                ctx.DEFINITION_NJE_NODE().getText(),
                visitNullable(ctx.name())
        );
    }

    @Override
    public ControlM visitSetVarLine(ControlMParser.SetVarLineContext ctx) {
        Space prefix = whitespace();
        Markers markers = Markers.EMPTY;
        markers = markers.addIfAbsent(mapColumn(Column.Location.START));
        ControlM set;
        if (ctx.DEFINITION_EQUALS_CHAR() != null) {
            set = new ControlM.SetVar(
                    randomId(),
                    sourceBefore(ctx.DEFINITION_SET_VAR().getText()),
                    Markers.EMPTY,
                    ctx.DEFINITION_SET_VAR().getText(),
                    (ControlM.Word) visit(ctx.name(0)),
                    padLeft(sourceBefore(ctx.DEFINITION_EQUALS_CHAR().getText()), (ControlM.Word) visit(ctx.name(1)))
            );
        } else {
            set = new ControlM.Parameter(
                    randomId(),
                    sourceBefore(ctx.DEFINITION_SET_VAR().getText()),
                    Markers.EMPTY,
                    ctx.DEFINITION_SET_VAR().getText(),
                    ctx.name().isEmpty() ? null : (ControlM.Word) visit(ctx.name(0))
            );
        }

        markers = markers.addIfAbsent(mapColumn(Column.Location.END));
        return new ControlM.Line(
                randomId(),
                prefix,
                markers,
                singletonList(set)
        );
    }

    @Override
    public ControlM visitCtbSetLine(ControlMParser.CtbSetLineContext ctx) {
        Space prefix = whitespace();
        Markers markers = Markers.EMPTY;
        markers = markers.addIfAbsent(mapColumn(Column.Location.START));
        List<ControlM> words = convertAll(Arrays.asList(ctx.DEFINITION_CTB_STEP(), ctx.DEFINITION_AT(), ctx.name(), ctx.DEFINITION_TYPE()));
        markers = markers.addIfAbsent(mapColumn(Column.Location.END));
        return new ControlM.Line(
                randomId(),
                prefix,
                markers,
                words
        );
    }

    @Override
    public ControlM visitDocLine(ControlMParser.DocLineContext ctx) {
        Space prefix = whitespace();
        Markers markers = Markers.EMPTY;
        markers = markers.addIfAbsent(mapColumn(Column.Location.START));
        List<ControlM> parameters = convertAll(Arrays.asList(ctx.docMem(), ctx.docLib()));
        markers = markers.addIfAbsent(mapColumn(Column.Location.END));
        return new ControlM.Line(
                randomId(),
                prefix,
                markers,
                parameters
        );
    }

    @Override
    public ControlM visitDocMem(ControlMParser.DocMemContext ctx) {
        return new ControlM.Parameter(
                randomId(),
                sourceBefore(ctx.DEFINITION_DOCMEM().getText()),
                Markers.EMPTY,
                ctx.DEFINITION_DOCMEM().getText(),
                visitNullable(ctx.name())
        );
    }

    @Override
    public ControlM visitDocLib(ControlMParser.DocLibContext ctx) {
        return new ControlM.Parameter(
                randomId(),
                sourceBefore(ctx.DEFINITION_DOCLIB().getText()),
                Markers.EMPTY,
                ctx.DEFINITION_DOCLIB().getText(),
                visitNullable(ctx.name())
        );
    }

    @Override
    public Section visitScheduleSection(ControlMParser.ScheduleSectionContext ctx) {
        return new ControlM.ScheduleSection(
                randomId(),
                sourceBefore("| =========================================================================== |"),
                Markers.EMPTY,
                convertAll(ctx.scheduleLine())
        );
    }

    @Override
    public ControlM visitScheduleLine(ControlMParser.ScheduleLineContext ctx) {
        Space prefix = whitespace();
        Markers markers = Markers.EMPTY;
        markers = markers.addIfAbsent(mapColumn(Column.Location.START));

        List<ControlM> words = convertAll(ctx.name());

        markers = markers.addIfAbsent(mapColumn(Column.Location.END));
        return new ControlM.Line(
                randomId(),
                prefix,
                markers,
                words
        );
    }

    @Override
    public Section visitInputSection(ControlMParser.InputSectionContext ctx) {
        return new ControlM.InputSection(
                randomId(),
                sourceBefore("| =========================================================================== |"),
                Markers.EMPTY,
                convertAll(ctx.inputNamesLine()),
                convertAll(ctx.inputLine())
        );
    }

    @Override
    public ControlM visitInputNamesLine(ControlMParser.InputNamesLineContext ctx) {
        Space prefix = whitespace();
        Markers markers = Markers.EMPTY;
        markers = markers.addIfAbsent(mapColumn(Column.Location.START));

        ControlM.Word in = visitNullable(ctx.INPUT_NAMES_IN());
        List<ControlM> inputs = convertAll(ctx.input());

        markers = markers.addIfAbsent(mapColumn(Column.Location.END));
        return new ControlM.Input(
                randomId(),
                prefix,
                markers,
                in,
                inputs
        );
    }

    @Override
    public ControlM visitInput(ControlMParser.InputContext ctx) {
        return new ControlM.Input.NameParameter(
                randomId(),
                whitespace(),
                Markers.EMPTY,
                visitNullable(ctx.name()),
                (ControlM.Parameter) visit(ctx.date())
        );
    }

    @Override
    public ControlM visitDate(ControlMParser.DateContext ctx) {
        String text = ctx.ODAT() != null ? ctx.ODAT().getText() : ctx.DATE_WILDCARD().getText();
        return new ControlM.Parameter(
                randomId(),
                sourceBefore(text),
                Markers.EMPTY,
                text,
                visitNullable(ctx.name())
        );
    }

    @Override
    public ControlM visitInputLine(ControlMParser.InputLineContext ctx) {
        Space prefix = whitespace();
        Markers markers = Markers.EMPTY;
        markers = markers.addIfAbsent(mapColumn(Column.Location.START));

        List<ControlM> words = convertAll(ctx.name());

        markers = markers.addIfAbsent(mapColumn(Column.Location.END));
        return new ControlM.Line(
                randomId(),
                prefix,
                markers,
                words
        );
    }

    @Override
    public Section visitOutputSection(ControlMParser.OutputSectionContext ctx) {
        return new ControlM.OutputSection(
                randomId(),
                sourceBefore("| =========================================================================== |"),
                Markers.EMPTY,
                convertAll(ctx.outputNamesLine()),
                convertAll(ctx.outputLine())
        );
    }

    @Override
    public ControlM visitOutputNamesLine(ControlMParser.OutputNamesLineContext ctx) {
        Space prefix = whitespace();
        Markers markers = Markers.EMPTY;
        markers = markers.addIfAbsent(mapColumn(Column.Location.START));

        ControlM.Word out = visitNullable(ctx.OUTPUT_NAMES_OUT());
        List<ControlM> outputs = convertAll(ctx.output());

        markers = markers.addIfAbsent(mapColumn(Column.Location.END));
        return new ControlM.Output(
                randomId(),
                prefix,
                markers,
                out,
                outputs
        );
    }

    @Override
    public ControlM visitOutput(ControlMParser.OutputContext ctx) {
        return new ControlM.Output.NameParameter(
                randomId(),
                whitespace(),
                Markers.EMPTY,
                visitNullable(ctx.name()),
                (ControlM.Parameter) visit(ctx.date())
        );
    }

    @Override
    public ControlM visitOutputLine(ControlMParser.OutputLineContext ctx) {
        Space prefix = whitespace();
        Markers markers = Markers.EMPTY;
        markers = markers.addIfAbsent(mapColumn(Column.Location.START));

        List<ControlM> words = convertAll(ctx.name());

        markers = markers.addIfAbsent(mapColumn(Column.Location.END));
        return new ControlM.Line(
                randomId(),
                prefix,
                markers,
                words
        );
    }

    @Override
    public Section visitApplicationFormSection(ControlMParser.ApplicationFormSectionContext ctx) {
        return new ControlM.ApplicationFormSection(
                randomId(),
                sourceBefore("| =========================================================================== |"),
                Markers.EMPTY,
                convertAll(ctx.applicationFormLine())
        );
    }

    @Override
    public ControlM visitApplicationFormLine(ControlMParser.ApplicationFormLineContext ctx) {
        Space prefix = whitespace();
        Markers markers = Markers.EMPTY;
        markers = markers.addIfAbsent(mapColumn(Column.Location.START));

        List<ControlM> words = convertAll(ctx.name());

        markers = markers.addIfAbsent(mapColumn(Column.Location.END));
        return new ControlM.Line(
                randomId(),
                prefix,
                markers,
                words
        );
    }

    @Override
    public ControlM visitTerminal(TerminalNode node) {
        return new ControlM.Word(
                randomId(),
                sourceBefore(node.getText().trim()),
                Markers.EMPTY,
                node.getText().trim()
        );
    }

    private Column mapColumn(Column.Location location) {
        return new Column(
                randomId(),
                sourceBefore("|"),
                location
        );
    }

    private <C, T extends ParseTree> List<C> convertAll(List<T> trees, Function<T, C> convert) {
        List<C> converted = new ArrayList<>(trees.size());
        for (T tree : trees) {
            converted.add(convert.apply(tree));
        }
        return converted.isEmpty() ? emptyList() : converted;
    }

    private <C extends ControlM, T extends ParseTree> List<C> convertAll(List<T> trees) {
        //noinspection unchecked
        return convertAll(trees, t -> (C) visit(t));
    }

    private <T> ControlMLeftPadded<T>  padLeft(Space left, T tree) {
        return new ControlMLeftPadded<>(left, tree, Markers.EMPTY);
    }

    private void skip(@Nullable String token) {
        if (token != null && source.startsWith(token, cursor)) {
            cursor += token.length();
        }
    }

    private Space whitespace() {
        int endIndex = indexOfNextNonWhitespace(cursor, source);
        String prefix = source.substring(cursor, endIndex);
        cursor += prefix.length();
        return Space.build(prefix);
    }

    private Space sourceBefore(String untilDelim) {
        Space prefix = whitespace();
        skip(untilDelim);
        return Space.build(prefix.getWhitespace());
    }

    public static int indexOfNextNonWhitespace(int cursor, String source) {
        int delimIndex = cursor;
        for (; delimIndex < source.length(); delimIndex++) {
            if (!Character.isWhitespace(source.charAt(delimIndex))) {
                break;
            }
        }
        return delimIndex;
    }
}
