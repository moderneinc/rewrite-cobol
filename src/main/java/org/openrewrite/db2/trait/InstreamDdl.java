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
package org.openrewrite.db2.trait;

import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.db2.Db2Parser;
import org.openrewrite.db2.tree.Db2;
import org.openrewrite.jcl.tree.Jcl;
import org.openrewrite.jcl.trait.DataDefinition;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

/**
 * DDL a job runs from a {@code SYSIN DD *} stream rather than from a member of its own.
 * <p>
 * This is where most of an estate's tables are actually created: a {@code .ddl} file is the
 * exception, and genapp and Bank-of-Z between them create every one of their tables from inside a
 * JCL job that runs DSNTIAD or DSNTEP2. Reading only files of DDL would find a fraction of the
 * schema.
 */
@Value
public class InstreamDdl implements Trait<Jcl.JobControlStatement> {

    /**
     * A stream is DDL if it says so. Streams carry IDCAMS commands, sort cards and TSO input too,
     * and parsing those as DB2 would cost more than looking first — the water rules would swallow
     * them whole and report nothing, but slowly.
     */
    private static final Pattern DDL = Pattern.compile(
            "(?i)\\b(CREATE|ALTER|DROP)\\s+(TABLE|INDEX)\\b");

    Cursor cursor;

    public DataDefinition getDataDefinition() {
        return new DataDefinition(cursor);
    }

    /**
     * The DD name the stream is written under — {@code SYSIN} for the DB2 processors.
     */
    public String getName() {
        return getTree().getSimpleName();
    }

    /**
     * The stream as the job holds it. In-stream data is one LST node per word, so the text is put
     * back together from the words and the space between them.
     */
    public String getText() {
        StringBuilder text = new StringBuilder();
        for (Jcl.DataDefinitionStream line : getDataDefinition().getInStreamData()) {
            text.append(line.getPrefix().getWhitespace()).append(line.getWord().getText());
        }
        return text.toString();
    }

    /**
     * The stream read as DDL. Detached from the JCL rather than grafted into it: two source files
     * cannot share a path, and the DDL is a fact about the estate rather than about the job.
     */
    public Db2.CompilationUnit parse(ExecutionContext ctx) {
        return new Db2Parser().parseFragment(sourcePath(), getText(), ctx);
    }

    private Path sourcePath() {
        Jcl.CompilationUnit cu = cursor.firstEnclosing(Jcl.CompilationUnit.class);
        return cu == null ? Paths.get(getName()) : cu.getSourcePath();
    }

    public static class Matcher extends SimpleTraitMatcher<InstreamDdl> {

        @Override
        protected @Nullable InstreamDdl test(Cursor cursor) {
            Object value = cursor.getValue();
            if (!(value instanceof Jcl.JobControlStatement) ||
                !((Jcl.JobControlStatement) value).isOperation("DD")) {
                return null;
            }
            DataDefinition dd = new DataDefinition(cursor);
            if (!dd.isInStream()) {
                return null;
            }
            InstreamDdl ddl = new InstreamDdl(cursor);
            return DDL.matcher(ddl.getText()).find() ? ddl : null;
        }
    }

    /**
     * Every stream of DDL in a job, read.
     */
    public static List<Db2.CompilationUnit> parseAll(Jcl.CompilationUnit cu, ExecutionContext ctx) {
        return new Matcher().lower(cu)
                .map(ddl -> ddl.parse(ctx))
                .collect(toList());
    }

    @Override
    public String toString() {
        return "DD " + getName() + " (DDL)";
    }
}
