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

import org.openrewrite.jcl.tree.Jcl;

import java.util.List;

/**
 * One DD statement, with the data sets it names.
 * <p>
 * The DD name is the join to COBOL: {@code SELECT ACCT-FILE ASSIGN TO ACCTDD} in the program and
 * {@code //ACCTDD DD DSN=...} in the JCL are the two halves of the same fact, and neither half says
 * what the other knows. The program never learns the data set name and the JCL never learns what the
 * program does with it.
 */
@Value
public class DataDefinition {

    /**
     * The DD name, which is what the program's {@code ASSIGN} clause names.
     */
    String name;

    /**
     * The data sets named, in order. More than one means a concatenation, where the step reads them
     * end to end as though they were one.
     */
    List<DataSet> dataSets;

    /**
     * True for {@code DD *} and {@code DD DATA}, where the data follows in the job stream rather than
     * living in a data set. SYSIN control cards arrive this way.
     */
    boolean inStream;

    /**
     * The {@code SYSOUT} class, or null. A SYSOUT DD produces printed output rather than a data set,
     * so it has no name the rest of the installation shares.
     */
    @Nullable
    String sysout;

    /**
     * True for {@code DD DUMMY}, where the program's I/O succeeds and goes nowhere. A step reading a
     * DUMMY file is doing nothing, which is worth seeing rather than reporting as a read.
     */
    boolean dummy;

    /**
     * The DD this one refers to, from {@code DSN=*.STEP.DDNAME}. Backward references are how a later
     * step names a data set an earlier one created without repeating its name.
     */
    @Nullable
    String backwardReference;

    /**
     * The DD statements this came from — more than one for a concatenation. Anything the model does
     * not name is on them, and a recipe that wants to mark or rewrite the DD works through them.
     */
    List<Jcl.JobControlStatement> statements;
}
