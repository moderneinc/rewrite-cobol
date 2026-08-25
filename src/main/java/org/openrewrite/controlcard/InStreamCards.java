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
package org.openrewrite.controlcard;

import lombok.Value;
import org.openrewrite.Cursor;
import org.openrewrite.PrintOutputCapture;
import org.openrewrite.jcl.internal.JclPrinter;
import org.openrewrite.jcl.tree.Jcl;

import java.util.ArrayList;
import java.util.List;

/**
 * The cards written inside a job rather than in a library of their own — the in-stream data of a DD.
 * <p>
 * Both shapes occur in a real estate and neither stands for the other: the deck a shop keeps as a
 * control card member is read once and run from several jobs, while the deck written in the job is
 * the only place its cards exist. The cards are already in the job's own LST, so they are read back
 * from it by printing — a card's layout lives in the white space in front of each word, and where the
 * line ends is what decides whether the next one continues it.
 * <p>
 * Every DD is offered, not a list of the ones a shop is expected to use. A sort deck reaches DFSORT
 * on {@code SYSIN}, {@code DFSPARM}, {@code SORTCNTL} or a {@code xxxxCNTL} named by an ICETOOL
 * {@code USING}, and which of those it is is the job's business — the deck is typed by what it says.
 */
@Value
public class InStreamCards {

    /**
     * The DD the cards were written under.
     */
    String ddName;

    /**
     * The one-based line of the job the first card is written on.
     */
    int line;

    String text;

    public static List<InStreamCards> of(Jcl.CompilationUnit cu) {
        List<String> ddNames = new ArrayList<>();
        List<int[]> spans = new ArrayList<>();

        PrintOutputCapture<Integer> out = new PrintOutputCapture<>(0);
        new JclPrinter<Integer>() {
            int stream = -1;

            @Override
            public Jcl visitJobControlStatement(Jcl.JobControlStatement statement, PrintOutputCapture<Integer> p) {
                Jcl printed = super.visitJobControlStatement(statement, p);
                if (statement.isOperation("DD")) {
                    ddNames.add(statement.getSimpleName());
                    spans.add(new int[]{p.out.length(), p.out.length()});
                    stream = spans.size() - 1;
                } else {
                    stream = -1;
                }
                return printed;
            }

            @Override
            public Jcl visitDataDefinitionStream(Jcl.DataDefinitionStream ddStream, PrintOutputCapture<Integer> p) {
                Jcl printed = super.visitDataDefinitionStream(ddStream, p);
                if (stream >= 0) {
                    spans.get(stream)[1] = p.out.length();
                }
                return printed;
            }
        }.visit(cu, out, new Cursor(null, Cursor.ROOT_VALUE));

        String job = out.getOut();
        List<InStreamCards> decks = new ArrayList<>(ddNames.size());
        for (int i = 0; i < ddNames.size(); i++) {
            int start = spans.get(i)[0];
            int end = spans.get(i)[1];
            while (start < end && (job.charAt(start) == '\n' || job.charAt(start) == '\r')) {
                start++;
            }
            // A DD with no in-stream data leaves an empty run here, as does one naming an external
            // member: grafted content is not the job's own source and prints nothing.
            if (start < end) {
                decks.add(new InStreamCards(ddNames.get(i), lineOf(job, start), job.substring(start, end)));
            }
        }
        return decks;
    }

    private static int lineOf(String text, int offset) {
        int line = 1;
        for (int i = 0; i < offset; i++) {
            if (text.charAt(i) == '\n') {
                line++;
            }
        }
        return line;
    }
}
