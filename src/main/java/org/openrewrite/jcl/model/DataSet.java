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
package org.openrewrite.jcl.model;

import lombok.Value;
import org.jspecify.annotations.Nullable;

/**
 * One data set a DD statement names.
 * <p>
 * A DD names more than one when data sets are concatenated, which is why this is separate from the
 * DD itself.
 */
@Value
public class DataSet {

    /**
     * The data set name as written, without any member in parentheses. This is the name the whole
     * installation knows the data set by, and so the only thing two jobs can be joined on.
     */
    String name;

    /**
     * The member of a partitioned data set, from {@code DSN=LIB(MEMBER)}. Null for a sequential data
     * set or a whole library.
     */
    @Nullable
    String member;

    @Nullable
    Disposition disposition;

    /**
     * A temporary data set, named {@code &&NAME}, which exists only for the life of the job. These
     * are how one step hands work to the next, so they are the edges within a job rather than
     * between jobs.
     */
    public boolean isTemporary() {
        return name.startsWith("&&");
    }

    /**
     * A generation data group reference, {@code MY.GDG(+1)} or {@code MY.GDG(0)}. The member syntax
     * means something different here: a relative generation, not a library member.
     */
    public boolean isGenerationDataGroup() {
        return member != null && (member.startsWith("+") || member.startsWith("-") || "0".equals(member));
    }
}
