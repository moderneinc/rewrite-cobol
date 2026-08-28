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
package org.openrewrite.mainframe.controlm.trait;

import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.mainframe.controlm.tree.ControlM;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

/**
 * A condition a job leaves behind when it ends: the panel's {@code OUT}, the export's {@code OUTCOND}.
 * <p>
 * The sign is what makes a chain a chain. A job adds the condition its successor waits for, and the
 * successor usually takes it away again so that tomorrow's run waits for tomorrow's predecessor
 * rather than starting on yesterday's leftovers.
 */
@Value
public class OutCondition implements Trait<ControlM> {

    Cursor cursor;

    /**
     * What the job does to the condition when it ends.
     */
    public enum Sign {
        ADD,
        DELETE
    }

    public String getName() {
        ControlM.Element element = element();
        if (element != null) {
            String name = element.getAttributeText("NAME");
            return name == null ? "" : name;
        }
        ControlM.Word name = ((ControlM.Output.NameParameter) cursor.getValue()).getName();
        return name == null ? "" : name.getText();
    }

    public @Nullable String getDate() {
        ControlM.Element element = element();
        if (element != null) {
            return element.getAttributeText("ODATE");
        }
        ControlM.Parameter date = ((ControlM.Output.NameParameter) cursor.getValue()).getDate();
        return date == null ? null : date.getOption();
    }

    /**
     * Adding is what a definition means when it writes no sign at all.
     */
    public Sign getSign() {
        return "-".equals(sign()) ? Sign.DELETE : Sign.ADD;
    }

    public boolean isAdded() {
        return getSign() == Sign.ADD;
    }

    public boolean isDeleted() {
        return getSign() == Sign.DELETE;
    }

    private @Nullable String sign() {
        ControlM.Element element = element();
        if (element != null) {
            return element.getAttributeText("SIGN");
        }
        ControlM.Parameter date = ((ControlM.Output.NameParameter) cursor.getValue()).getDate();
        return date == null || date.getValue() == null ? null : date.getValue().getText();
    }

    private ControlM.@Nullable Element element() {
        Object value = cursor.getValue();
        return value instanceof ControlM.Element ? (ControlM.Element) value : null;
    }

    public static class Matcher extends SimpleTraitMatcher<OutCondition> {

        @Override
        protected @Nullable OutCondition test(Cursor cursor) {
            Object value = cursor.getValue();
            if (value instanceof ControlM.Element && ((ControlM.Element) value).isName("OUTCOND")) {
                return new OutCondition(cursor);
            }
            return value instanceof ControlM.Output.NameParameter &&
                   ((ControlM.Output.NameParameter) value).getName() != null ? new OutCondition(cursor) : null;
        }
    }

    @Override
    public String toString() {
        return "OUT " + getName() + (getDate() == null ? "" : " " + getDate()) +
               (getSign() == Sign.ADD ? " +" : " -");
    }
}
