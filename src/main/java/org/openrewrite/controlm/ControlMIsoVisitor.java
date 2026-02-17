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
package org.openrewrite.controlm;

import org.openrewrite.controlm.tree.ControlM;

public class ControlMIsoVisitor<P> extends ControlMVisitor<P> {

    @Override
    public ControlM.CompilationUnit visitCompilationUnit(ControlM.CompilationUnit compilationUnit, P p) {
        return (ControlM.CompilationUnit) super.visitCompilationUnit(compilationUnit, p);
    }

    @Override
    public ControlM.DefinitionSection visitDefinitionSection(ControlM.DefinitionSection definitionSection, P p) {
        return (ControlM.DefinitionSection) super.visitDefinitionSection(definitionSection, p);
    }

    @Override
    public ControlM.Description visitDescription(ControlM.Description description, P p) {
        return (ControlM.Description) super.visitDescription(description, p);
    }

    @Override
    public ControlM.SetVar visitSetVar(ControlM.SetVar setVar, P p) {
        return (ControlM.SetVar) super.visitSetVar(setVar, p);
    }

    @Override
    public ControlM.ScheduleSection visitScheduleSection(ControlM.ScheduleSection scheduleSection, P p) {
        return (ControlM.ScheduleSection) super.visitScheduleSection(scheduleSection, p);
    }

    @Override
    public ControlM.Input visitInput(ControlM.Input input, P p) {
        return (ControlM.Input) super.visitInput(input, p);
    }

    @Override
    public ControlM.Input.NameParameter visitInputNameParameter(ControlM.Input.NameParameter nameParameter, P p) {
        return (ControlM.Input.NameParameter) super.visitInputNameParameter(nameParameter, p);
    }

    @Override
    public ControlM.InputSection visitInputSection(ControlM.InputSection inputSection, P p) {
        return (ControlM.InputSection) super.visitInputSection(inputSection, p);
    }

    @Override
    public ControlM.Output visitOutput(ControlM.Output output, P p) {
        return (ControlM.Output) super.visitOutput(output, p);
    }

    @Override
    public ControlM.Output.NameParameter visitOutputNameParameter(ControlM.Output.NameParameter nameParameter, P p) {
        return (ControlM.Output.NameParameter) super.visitOutputNameParameter(nameParameter, p);
    }

    @Override
    public ControlM.OutputSection visitOutputSection(ControlM.OutputSection outputSection, P p) {
        return (ControlM.OutputSection) super.visitOutputSection(outputSection, p);
    }

    @Override
    public ControlM.ApplicationFormSection visitApplicationFormSection(ControlM.ApplicationFormSection applicationFormSection, P p) {
        return (ControlM.ApplicationFormSection) super.visitApplicationFormSection(applicationFormSection, p);
    }

    @Override
    public ControlM.Line visitLine(ControlM.Line line, P p) {
        return (ControlM.Line) super.visitLine(line, p);
    }

    @Override
    public ControlM.Parameter visitParameter(ControlM.Parameter parameter, P p) {
        return (ControlM.Parameter) super.visitParameter(parameter, p);
    }
}
