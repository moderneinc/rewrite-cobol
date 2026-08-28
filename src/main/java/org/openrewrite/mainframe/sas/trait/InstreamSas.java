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
package org.openrewrite.mainframe.sas.trait;

import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.mainframe.Members;
import org.openrewrite.mainframe.jcl.SourcePositions;
import org.openrewrite.mainframe.jcl.trait.DataDefinition;
import org.openrewrite.mainframe.jcl.tree.Jcl;
import org.openrewrite.marker.Range;
import org.openrewrite.text.PlainText;
import org.openrewrite.trait.SimpleTraitMatcher;
import org.openrewrite.trait.Trait;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * A SAS program a job writes on a {@code SYSIN} stream rather than keeping as a member of its own.
 * <p>
 * Both shapes occur in a real estate and neither stands for the other: the member in the SAS library
 * is read by name from several jobs, while the program written in the job is the only place it
 * exists — and it has no member name at all, so nothing outside the job can name it. That is where a
 * listing somebody asked for last week lives until somebody puts it in the library.
 */
@Value
public class InstreamSas implements Trait<Jcl.JobControlStatement> {

    Cursor cursor;

    public DataDefinition getDataDefinition() {
        return new DataDefinition(cursor);
    }

    /**
     * The DD name the stream is written under, which is {@code SYSIN} whether the job wrote it that
     * way or as the {@code //SAS.SYSIN} override of a procedure step.
     */
    public String getName() {
        return getTree().getSimpleName();
    }

    /**
     * The one-based line of the job the program's first card is on.
     */
    public int getLine() {
        Range card = firstCard();
        return card == null ? 1 : card.getStart().getLine();
    }

    /**
     * The stream as the job holds it, from its first card. In-stream data is one LST node per word,
     * so the text is put back together from the words and the space between them; the line ending
     * the first prefix opens with belongs to the DD card and is left out, so the program's first
     * line is the job's line {@link #getLine()}.
     */
    public String getText() {
        StringBuilder text = new StringBuilder();
        boolean first = true;
        for (Jcl.DataDefinitionStream line : getDataDefinition().getInStreamData()) {
            String prefix = line.getPrefix().getWhitespace();
            if (first) {
                prefix = prefix.startsWith("\r\n") ? prefix.substring(2) :
                        prefix.startsWith("\n") ? prefix.substring(1) : prefix;
                first = false;
            }
            text.append(prefix).append(line.getWord().getText());
        }
        return text.toString();
    }

    /**
     * The stream held as the program it is. Detached from the JCL rather than grafted into it: the
     * program is a fact about the estate rather than about the job, and it is read by the same traits
     * a member of the SAS library is — which is why the path it is given says SAS.
     */
    public PlainText parse() {
        return PlainText.builder()
                .sourcePath(sourcePath())
                .text(getText())
                .build();
    }

    /**
     * The job's own path with {@code .sas} in place of its extension. The program has no member name
     * of its own, so there is no other name to give it.
     */
    private Path sourcePath() {
        Jcl.CompilationUnit cu = cursor.firstEnclosing(Jcl.CompilationUnit.class);
        if (cu == null) {
            return Paths.get(getName() + ".sas");
        }
        Path fileName = cu.getSourcePath().getFileName();
        String name = fileName == null ? getName() : fileName.toString();
        int dot = name.lastIndexOf('.');
        return cu.getSourcePath().resolveSibling((dot < 0 ? name : name.substring(0, dot)) + ".sas");
    }

    private @Nullable Range firstCard() {
        Jcl.CompilationUnit cu = cursor.firstEnclosing(Jcl.CompilationUnit.class);
        if (cu == null) {
            return null;
        }
        SourcePositions positions = SourcePositions.of(cu);
        for (Jcl.DataDefinitionStream line : getDataDefinition().getInStreamData()) {
            Range card = positions.card(line);
            if (card != null) {
                return card;
            }
        }
        return null;
    }

    public static class Matcher extends SimpleTraitMatcher<InstreamSas> {

        @Override
        protected @Nullable InstreamSas test(Cursor cursor) {
            Object value = cursor.getValue();
            if (!(value instanceof Jcl.JobControlStatement) ||
                !((Jcl.JobControlStatement) value).isOperation("DD")) {
                return null;
            }
            if (!new DataDefinition(cursor).isInStream()) {
                return null;
            }
            InstreamSas sas = new InstreamSas(cursor);
            return Members.isSasProgram(sas.getText()) ? sas : null;
        }
    }

    /**
     * Every SAS program written in a job, read.
     */
    public static List<PlainText> parseAll(Jcl.CompilationUnit cu) {
        return new Matcher().lower(cu)
                .map(InstreamSas::parse)
                .collect(toList());
    }

    @Override
    public String toString() {
        return "DD " + getName() + " (SAS)";
    }
}
