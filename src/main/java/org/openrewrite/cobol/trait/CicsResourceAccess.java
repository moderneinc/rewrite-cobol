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
package org.openrewrite.cobol.trait;

import lombok.Value;

/**
 * One resource a {@link CicsCommand} touches, and how it touches it.
 * <p>
 * A single command can produce more than one: {@code SEND MAP('DFH0M') MAPSET('DFH0MS')} names both
 * a map and a mapset. Separate from the command because the command is what was written and this is
 * what it reaches.
 */
@Value
public class CicsResourceAccess {

    Kind kind;

    /**
     * The resource name. A literal operand yields the name itself; a data name operand yields the
     * data name, with {@link #dynamic} set.
     */
    String name;

    Access access;

    /**
     * True when the name was supplied by a data item rather than a literal, so the resource is only
     * known at run time. {@link LiteralAssignment#resolve} is what a caller tries next; a dynamic
     * reference that stays dynamic is where automated migration needs human review.
     */
    boolean dynamic;

    public enum Kind {
        FILE,
        TS_QUEUE,
        TD_QUEUE,
        PROGRAM,
        TRANSACTION,
        MAP,
        MAPSET
    }

    public enum Access {
        READ,
        BROWSE,
        CREATE,
        UPDATE,
        DELETE,
        LINK,
        XCTL,
        LOAD,
        START,
        CANCEL,
        RETURN,
        SEND,
        RECEIVE
    }

    @Override
    public String toString() {
        return access + " " + kind + " " + name + (dynamic ? " (dynamic)" : "");
    }
}
