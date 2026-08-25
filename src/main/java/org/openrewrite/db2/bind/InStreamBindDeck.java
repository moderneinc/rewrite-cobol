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
package org.openrewrite.db2.bind;

import lombok.Value;
import org.openrewrite.Cursor;
import org.openrewrite.PrintOutputCapture;
import org.openrewrite.db2.bind.tree.Bind;
import org.openrewrite.jcl.internal.JclPrinter;
import org.openrewrite.jcl.tree.Jcl;

import java.util.ArrayList;
import java.util.List;

/**
 * A bind deck written inside a job rather than in a library of its own, as the in-stream data of a
 * {@code SYSTSIN} DD.
 * <p>
 * Both shapes occur in a real estate and neither stands for the other: the deck a shop keeps as a
 * {@code CARDLIB} member is read once and run from several jobs, while the deck written in the job is
 * the only place its binds exist. The cards are already in the job's own LST, so they are read back
 * from it by printing — a card's layout lives in the white space in front of each word, and where the
 * line ends is what decides whether the next one continues it.
 */
@Value
public class InStreamBindDeck {

    /**
     * The DD the deck was written under. Always {@code SYSTSIN}, since that is the DD the terminal
     * monitor program reads its commands from, but carried so a caller can say where it came from.
     */
    String ddName;

    /**
     * The one-based line of the job the deck's first card is written on.
     */
    int line;

    Bind.CompilationUnit deck;

    public static List<InStreamBindDeck> of(Jcl.CompilationUnit cu) {
        List<String> ddNames = new ArrayList<>();
        List<int[]> spans = new ArrayList<>();

        PrintOutputCapture<Integer> out = new PrintOutputCapture<>(0);
        new JclPrinter<Integer>() {
            int stream = -1;

            @Override
            public Jcl visitJobControlStatement(Jcl.JobControlStatement statement, PrintOutputCapture<Integer> p) {
                Jcl printed = super.visitJobControlStatement(statement, p);
                if (isSystsin(statement)) {
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
        List<InStreamBindDeck> decks = new ArrayList<>(ddNames.size());
        for (int i = 0; i < ddNames.size(); i++) {
            int start = spans.get(i)[0];
            int end = spans.get(i)[1];
            while (start < end && (job.charAt(start) == '\n' || job.charAt(start) == '\r')) {
                start++;
            }
            String source = job.substring(start, end);
            // A DD naming an external member prints nothing — grafted content is not the job's own
            // source — so it leaves an empty run here and is read as the deck it is instead.
            if (BindLineReader.isBindDeck(source)) {
                decks.add(new InStreamBindDeck(ddNames.get(i), lineOf(job, start),
                        BindParser.parse(cu.getSourcePath(), source)));
            }
        }
        return decks;
    }

    /**
     * {@code getSimpleName} drops the procedure step an override names, so {@code //BIND.SYSTSIN}
     * reads the same as {@code //SYSTSIN}.
     */
    private static boolean isSystsin(Jcl.JobControlStatement dd) {
        return dd.isOperation("DD") && "SYSTSIN".equalsIgnoreCase(dd.getSimpleName());
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
