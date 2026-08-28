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
package org.openrewrite.mainframe.cobol.search;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.mainframe.cobol.table.CopybookSource;
import org.openrewrite.mainframe.cobol.trait.CopybookReference;
import org.openrewrite.mainframe.cobol.tree.Cobol;
import org.openrewrite.internal.StringUtils;

import java.nio.file.Path;

@EqualsAndHashCode(callSuper = false)
@Value
public class FindCopybook extends Recipe {
    transient CopybookSource copybookSource = new CopybookSource(this);

    @Option(displayName = "Copybook name",
            description = "The copybook name to search for. If not provided, all copy statements will be returned.",
            example = "KP008",
            required = false)
    @Nullable
    String copybookName;

    @Option(displayName = "Only missing copybooks",
            description = "Only find copy statements and exec sql include statements that are missing copybooks.",
            example = "True",
            required = false)
    @Nullable
    Boolean onlyMissingCopybooks;

    String displayName = "Find copybook usage";

    String description = "Find all copy statements with the copybook name.";

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(new UsesCopybook(copybookName),
                new CopybookReference.Matcher().asVisitor((reference, ctx) -> {
                    // A missing copybook is reported whatever was asked for: the program is being read
                    // without declarations it needs, which matters more than the name searched for.
                    if (reference.isMissing()) {
                        copybookSource.insertRow(ctx, row(reference, "",
                                CopybookSource.ResolutionStatus.MISSING_SOURCE, ""));
                        return reference.marked(null);
                    }
                    if (Boolean.TRUE.equals(onlyMissingCopybooks) ||
                        !(StringUtils.isNullOrEmpty(copybookName) || copybookName.equals(reference.getName()))) {
                        return reference.getTree();
                    }
                    Path sourcePath = reference.getSourcePath();
                    copybookSource.insertRow(ctx, row(reference,
                            sourcePath == null ? "" : sourcePath.toString(),
                            sourcePath == null ? CopybookSource.ResolutionStatus.NO_SOURCE_PATH :
                                    CopybookSource.ResolutionStatus.RESOLVED,
                            wordContext(reference)));
                    return reference.marked(null);
                }));
    }

    private static CopybookSource.Row row(CopybookReference reference, String sourcePath,
                                          CopybookSource.ResolutionStatus status, String wordContext) {
        return new CopybookSource.Row(
                reference.getCursor().firstEnclosingOrThrow(Cobol.CompilationUnit.class).getSourcePath().toString(),
                reference.getName(),
                sourcePath,
                status,
                wordContext);
    }

    private static String wordContext(CopybookReference reference) {
        Cobol.Word word = reference.getEnclosingWord();
        return word == null ? "" : word.getWord();
    }
}
