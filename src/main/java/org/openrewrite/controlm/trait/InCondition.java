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
package org.openrewrite.controlm.trait;

import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.controlm.tree.ControlM;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

/**
 * A condition a job waits for: the panel's {@code IN}, the export's {@code INCOND}.
 * <p>
 * Conditions are the whole of the dependency graph. Nothing in a schedule names another job — a job
 * adds a condition and another requires it, and the two are joined only by the name, which is why a
 * chain crosses tables and applications without saying so.
 */
@Value
public class InCondition implements Trait<ControlM> {

    Cursor cursor;

    public String getName() {
        ControlM.Element element = element();
        if (element != null) {
            String name = element.getAttributeText("NAME");
            return name == null ? "" : name;
        }
        ControlM.Word name = ((ControlM.Input.NameParameter) cursor.getValue()).getName();
        return name == null ? "" : name.getText();
    }

    /**
     * The run the condition has to have been added on: {@code ODAT} for today's, {@code PREV} for the
     * one before, which is how a job waits on the work of the night before.
     */
    public @Nullable String getDate() {
        ControlM.Element element = element();
        if (element != null) {
            return element.getAttributeText("ODATE");
        }
        ControlM.Parameter date = ((ControlM.Input.NameParameter) cursor.getValue()).getDate();
        return date == null ? null : date.getOption();
    }

    private ControlM.@Nullable Element element() {
        Object value = cursor.getValue();
        return value instanceof ControlM.Element ? (ControlM.Element) value : null;
    }

    public static class Matcher extends SimpleTraitMatcher<InCondition> {

        @Override
        protected @Nullable InCondition test(Cursor cursor) {
            Object value = cursor.getValue();
            if (value instanceof ControlM.Element && ((ControlM.Element) value).isName("INCOND")) {
                return new InCondition(cursor);
            }
            // A name parameter with no name is a date the panel wrote on its own, which names nothing.
            return value instanceof ControlM.Input.NameParameter &&
                   ((ControlM.Input.NameParameter) value).getName() != null ? new InCondition(cursor) : null;
        }
    }

    @Override
    public String toString() {
        return "IN " + getName() + (getDate() == null ? "" : " " + getDate());
    }
}
