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
package org.openrewrite.mainframe.controlm.internal;

import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.PrintOutputCapture;
import org.openrewrite.mainframe.controlm.ControlMVisitor;
import org.openrewrite.mainframe.controlm.marker.Column;
import org.openrewrite.mainframe.controlm.tree.ControlM;
import org.openrewrite.mainframe.controlm.tree.ControlMLeftPadded;
import org.openrewrite.mainframe.controlm.tree.Space;
import org.openrewrite.marker.Marker;
import org.openrewrite.marker.Markers;

import java.util.function.UnaryOperator;

public class ControlMPrinter<P> extends ControlMVisitor<PrintOutputCapture<P>> {
    public static final UnaryOperator<String> CONTROL_M_MARKER_WRAPPER =
            out -> "~~" + out + (out.isEmpty() ? "" : "~~") + ">";

    @Override
    public ControlM visitCompilationUnit(ControlM.CompilationUnit cu, PrintOutputCapture<P> p) {
        beforeSyntax(cu, Space.Location.COMPILATION_UNIT_PREFIX, p);
        visit(cu.getSections(), p);
        afterSyntax(cu, p);
        visitSpace(cu.getEof(), Space.Location.COMPILATION_UNIT_EOF, p);
        return cu;
    }

    @Override
    public ControlM visitDefinitionSection(ControlM.DefinitionSection definitionSection, PrintOutputCapture<P> p) {
        beforeSyntax(definitionSection, Space.Location.DEFINITION_SECTION_PREFIX, p);
        p.append("+---------------------------------- BROWSE -----------------------------------+");
        visit(definitionSection.getLines(), p);
        afterSyntax(definitionSection, p);
        return definitionSection;
    }

    @Override
    public ControlM visitDescription(ControlM.Description description, PrintOutputCapture<P> p) {
        beforeSyntax(description, Space.Location.DESCRIPTION_PREFIX, p);
        p.append(description.getWord());
        visit(description.getDescription(), p);
        afterSyntax(description, p);
        return description;
    }

    @Override
    public ControlM visitSetVar(ControlM.SetVar setVar, PrintOutputCapture<P> p) {
        beforeSyntax(setVar, Space.Location.SET_VAR_PREFIX, p);
        p.append(setVar.getSetVar());
        visit(setVar.getVarName(), p);
        visitLeftPadded("=", setVar.getPadding().getValue(), ControlMLeftPadded.Location.SET_VAR_INITIALIZER, p);
        afterSyntax(setVar, p);
        return setVar;
    }

    @Override
    public ControlM visitScheduleSection(ControlM.ScheduleSection scheduleSection, PrintOutputCapture<P> p) {
        beforeSyntax(scheduleSection, Space.Location.SCHEDULE_SECTION_PREFIX, p);
        p.append("| =========================================================================== |");
        visit(scheduleSection.getLines(), p);
        afterSyntax(scheduleSection, p);
        return scheduleSection;
    }

    @Override
    public ControlM visitInputSection(ControlM.InputSection inputSection, PrintOutputCapture<P> p) {
        beforeSyntax(inputSection, Space.Location.INPUT_SECTION_PREFIX, p);
        p.append("| =========================================================================== |");
        visit(inputSection.getInputNames(), p);
        visit(inputSection.getLines(), p);
        afterSyntax(inputSection, p);
        return inputSection;
    }

    @Override
    public ControlM visitInput(ControlM.Input input, PrintOutputCapture<P> p) {
        beforeSyntax(input, Space.Location.INPUT_PREFIX, p);
        visit(input.getIn(), p);
        visit(input.getInput(), p);
        afterSyntax(input, p);
        return input;
    }

    @Override
    public ControlM visitInputNameParameter(ControlM.Input.NameParameter nameParameter, PrintOutputCapture<P> p) {
        beforeSyntax(nameParameter, Space.Location.PARAMETER_PREFIX, p);
        visit(nameParameter.getName(), p);
        visit(nameParameter.getDate(), p);
        afterSyntax(nameParameter, p);
        return nameParameter;
    }

    @Override
    public ControlM visitOutputSection(ControlM.OutputSection outputSection, PrintOutputCapture<P> p) {
        beforeSyntax(outputSection, Space.Location.OUTPUT_SECTION_PREFIX, p);
        p.append("| =========================================================================== |");
        visit(outputSection.getOutputNames(), p);
        visit(outputSection.getLines(), p);
        afterSyntax(outputSection, p);
        return outputSection;
    }

    @Override
    public ControlM visitOutput(ControlM.Output output, PrintOutputCapture<P> p) {
        beforeSyntax(output, Space.Location.OUTPUT_PREFIX, p);
        visit(output.getOut(), p);
        visit(output.getOutput(), p);
        afterSyntax(output, p);
        return output;
    }

    @Override
    public ControlM visitOutputNameParameter(ControlM.Output.NameParameter nameParameter, PrintOutputCapture<P> p) {
        beforeSyntax(nameParameter, Space.Location.PARAMETER_PREFIX, p);
        visit(nameParameter.getName(), p);
        visit(nameParameter.getDate(), p);
        afterSyntax(nameParameter, p);
        return nameParameter;
    }

    @Override
    public ControlM visitApplicationFormSection(ControlM.ApplicationFormSection applicationFormSection, PrintOutputCapture<P> p) {
        beforeSyntax(applicationFormSection, Space.Location.APPLICATION_FORM_SECTION_PREFIX, p);
        p.append("| =========================================================================== |");
        visit(applicationFormSection.getLines(), p);
        afterSyntax(applicationFormSection, p);
        return applicationFormSection;
    }

    @Override
    public ControlM visitLine(ControlM.Line line, PrintOutputCapture<P> p) {
        beforeSyntax(line, Space.Location.LINE_PREFIX, p);
        visit(line.getParameters(), p);
        afterSyntax(line, p);
        return line;
    }

    @Override
    public ControlM visitParameter(ControlM.Parameter parameter, PrintOutputCapture<P> p) {
        beforeSyntax(parameter, Space.Location.PARAMETER_PREFIX, p);
        p.append(parameter.getOption());
        visit(parameter.getValue(), p);
        afterSyntax(parameter, p);
        return parameter;
    }

    @Override
    public ControlM visitElement(ControlM.Element element, PrintOutputCapture<P> p) {
        beforeSyntax(element, Space.Location.ELEMENT_PREFIX, p);
        p.append("<").append(element.getName());
        visit(element.getAttributes(), p);
        visitSpace(element.getBeforeTagEnd(), Space.Location.ELEMENT_BEFORE_TAG_END, p);
        if (element.getElements() == null) {
            p.append("/>");
        } else {
            p.append(">");
            visit(element.getElements(), p);
            visitSpace(element.getBeforeEndTag(), Space.Location.ELEMENT_BEFORE_END_TAG, p);
            p.append("</").append(element.getName()).append(">");
        }
        afterSyntax(element, p);
        return element;
    }

    @Override
    public ControlM visitAttribute(ControlM.Attribute attribute, PrintOutputCapture<P> p) {
        beforeSyntax(attribute, Space.Location.ATTRIBUTE_PREFIX, p);
        p.append(attribute.getName());
        visitSpace(attribute.getBeforeEquals(), Space.Location.ATTRIBUTE_BEFORE_EQUALS, p);
        p.append("=");
        visit(attribute.getValue(), p);
        afterSyntax(attribute, p);
        return attribute;
    }

    @Override
    public ControlM visitDirective(ControlM.Directive directive, PrintOutputCapture<P> p) {
        beforeSyntax(directive, Space.Location.DIRECTIVE_PREFIX, p);
        p.append(directive.getText());
        afterSyntax(directive, p);
        return directive;
    }

    @Override
    public ControlM visitWord(ControlM.Word word, PrintOutputCapture<P> p) {
        beforeSyntax(word, Space.Location.WORD_PREFIX, p);
        p.append(word.getText());
        afterSyntax(word, p);
        return word;
    }

    protected void visitLeftPadded(@Nullable String prefix, @Nullable ControlMLeftPadded<? extends ControlM> leftPadded, ControlMLeftPadded.Location location, PrintOutputCapture<P> p) {
        if (leftPadded != null) {
            beforeSyntax(leftPadded.getBefore(), leftPadded.getMarkers(), location.getBeforeLocation(), p);
            if (prefix != null) {
                p.append(prefix);
            }
            visit(leftPadded.getElement(), p);
            afterSyntax(leftPadded.getMarkers(), p);
        }
    }

    @Override
    public Space visitSpace(Space space, Space.Location location, PrintOutputCapture<P> p) {
        p.append(space.getWhitespace());
        return space;
    }

    protected void beforeSyntax(ControlM c, Space.Location loc, PrintOutputCapture<P> p) {
        beforeSyntax(c.getPrefix(), c.getMarkers(), loc, p);
    }

    protected void beforeSyntax(Space prefix, Markers markers, Space.@Nullable Location loc, PrintOutputCapture<P> p) {
        for (Marker marker : markers.getMarkers()) {
            p.out.append(p.getMarkerPrinter().beforePrefix(marker, new Cursor(getCursor(), marker), CONTROL_M_MARKER_WRAPPER));
        }
        if (loc != null) {
            visitSpace(prefix, loc, p);
        }
        visitMarkers(markers, p);
        for (Marker marker : markers.getMarkers()) {
            p.out.append(p.getMarkerPrinter().beforeSyntax(marker, new Cursor(getCursor(), marker), CONTROL_M_MARKER_WRAPPER));
            if (marker instanceof Column && ((Column) marker).getLocation() == Column.Location.START) {
                visitSpace(((Column) marker).getPrefix(), Space.Location.COLUMN_START_PREFIX, p);
                p.out.append("|");
            }
        }
    }

    protected void afterSyntax(ControlM c, PrintOutputCapture<P> p) {
        afterSyntax(c.getMarkers(), p);
    }

    protected void afterSyntax(Markers markers, PrintOutputCapture<P> p) {
        for (Marker marker : markers.getMarkers()) {
            if (marker instanceof Column && ((Column) marker).getLocation() == Column.Location.END) {
                visitSpace(((Column) marker).getPrefix(), Space.Location.COLUMN_END_PREFIX, p);
                p.out.append("|");
            }
            p.out.append(p.getMarkerPrinter().afterSyntax(marker, new Cursor(getCursor(), marker), CONTROL_M_MARKER_WRAPPER));
        }
    }
}
