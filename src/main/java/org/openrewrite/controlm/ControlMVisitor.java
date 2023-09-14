/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.controlm;

import org.openrewrite.Cursor;
import org.openrewrite.TreeVisitor;
import org.openrewrite.controlm.tree.ControlM;
import org.openrewrite.controlm.tree.ControlMLeftPadded;
import org.openrewrite.controlm.tree.Space;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.internal.lang.Nullable;

public class ControlMVisitor<P> extends TreeVisitor<ControlM, P> {

    public ControlM visitCompilationUnit(ControlM.CompilationUnit compilationUnit, P p) {
        ControlM.CompilationUnit c = compilationUnit;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.COMPILATION_UNIT_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withSections(ListUtils.map(c.getSections(), e -> visitAndCast(e, p)));
        c = c.withEof(visitSpace(c.getEof(), Space.Location.COMPILATION_UNIT_EOF, p));
        return c;
    }

    public ControlM visitDefinitionSection(ControlM.DefinitionSection definitionSection, P p) {
        ControlM.DefinitionSection d = definitionSection;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DEFINITION_SECTION_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        d = d.withLines(ListUtils.map(d.getLines(), e -> visitAndCast(e, p)));
        return d;
    }

    public ControlM visitDescription(ControlM.Description description, P p) {
        ControlM.Description d = description;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DESCRIPTION_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        d = d.withDescription(visitAndCast(d.getDescription(), p));
        return d;
    }

    public ControlM visitSetVar(ControlM.SetVar setVar, P p) {
        ControlM.SetVar s = setVar;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SET_VAR_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.getPadding().withValue(visitLeftPadded(s.getPadding().getValue(),
                ControlMLeftPadded.Location.SET_VAR_INITIALIZER, p));
        return s;
    }

    public ControlM visitScheduleSection(ControlM.ScheduleSection scheduleSection, P p) {
        ControlM.ScheduleSection s = scheduleSection;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.DEFINITION_SECTION_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withLines(ListUtils.map(s.getLines(), e -> visitAndCast(e, p)));
        return s;
    }

    public ControlM visitInputSection(ControlM.InputSection inputSection, P p) {
        ControlM.InputSection i = inputSection;
        i = i.withPrefix(visitSpace(i.getPrefix(), Space.Location.INPUT_SECTION_PREFIX, p));
        i = i.withMarkers(visitMarkers(i.getMarkers(), p));
        i = i.withLines(ListUtils.map(i.getLines(), e -> visitAndCast(e, p)));
        return i;
    }

    public ControlM visitOutputSection(ControlM.OutputSection outputSection, P p) {
        ControlM.OutputSection o = outputSection;
        o = o.withPrefix(visitSpace(o.getPrefix(), Space.Location.OUTPUT_SECTION_PREFIX, p));
        o = o.withMarkers(visitMarkers(o.getMarkers(), p));
        o = o.withLines(ListUtils.map(o.getLines(), e -> visitAndCast(e, p)));
        return o;
    }

    public ControlM visitApplicationFormSection(ControlM.ApplicationFormSection applicationFormSection, P p) {
        ControlM.ApplicationFormSection a = applicationFormSection;
        a = a.withPrefix(visitSpace(a.getPrefix(), Space.Location.DEFINITION_SECTION_PREFIX, p));
        a = a.withMarkers(visitMarkers(a.getMarkers(), p));
        a = a.withLines(ListUtils.map(a.getLines(), e -> visitAndCast(e, p)));
        return a;
    }

    public ControlM visitLine(ControlM.Line line, P p) {
        ControlM.Line l = line;
        l = l.withPrefix(visitSpace(l.getPrefix(), Space.Location.LINE_PREFIX, p));
        l = l.withMarkers(visitMarkers(l.getMarkers(), p));
        l = l.withParameters(ListUtils.map(l.getParameters(), e -> visitAndCast(e, p)));
        return l;
    }

    public ControlM visitParameter(ControlM.Parameter parameter, P p) {
        ControlM.Parameter pa = parameter;
        pa = pa.withPrefix(visitSpace(pa.getPrefix(), Space.Location.PARAMETER_PREFIX, p));
        pa = pa.withMarkers(visitMarkers(pa.getMarkers(), p));
        pa = pa.withValue(visitAndCast(pa.getValue(), p));
        return pa;
    }

    public ControlM visitWord(ControlM.Word word, P p) {
        ControlM.Word w = word;
        w = w.withPrefix(visitSpace(w.getPrefix(), Space.Location.WORD_PREFIX, p));
        w = w.withMarkers(visitMarkers(w.getMarkers(), p));
        return w;
    }

    public <T> ControlMLeftPadded<T> visitLeftPadded(@Nullable ControlMLeftPadded<T> left, ControlMLeftPadded.Location loc, P p) {
        if (left == null) {
            //noinspection ConstantConditions
            return null;
        }

        setCursor(new Cursor(getCursor(), left));

        Space before = visitSpace(left.getBefore(), loc.getBeforeLocation(), p);
        T t = left.getElement();

        if (t instanceof ControlM) {
            //noinspection unchecked
            t = visitAndCast((ControlM) left.getElement(), p);
        }

        setCursor(getCursor().getParent());
        if (t == null) {
            // If nothing changed leave AST node the same
            if (left.getElement() == null && before == left.getBefore()) {
                return left;
            }
            //noinspection ConstantConditions
            return null;
        }

        return (before == left.getBefore() && t == left.getElement()) ? left : new ControlMLeftPadded<>(before, t, left.getMarkers());
    }


    public Space visitSpace(Space space, Space.Location location, P p) {
        return space;
    }
}
