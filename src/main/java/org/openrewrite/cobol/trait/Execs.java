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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.cobol.CobolPreprocessorIsoVisitor;
import org.openrewrite.cobol.tree.Cobol;
import org.openrewrite.cobol.tree.CobolPreprocessor;

import java.util.ArrayList;
import java.util.List;

/**
 * Finding the one word an {@code EXEC} block hangs off.
 * <p>
 * Preprocessing takes the block out of the text the COBOL grammar reads and attaches it to a word
 * that stands in for it. The words of the block are then visited too — the preprocessor's own words
 * are handed back to the COBOL visitor, and they answer with the block they came from — so a visitor
 * that asks every word what block it carries hears about the same block a dozen times. This is what
 * tells the stand-in from the rest of the block.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class Execs {

    /**
     * The CICS, DLI or SQL block {@code word} carries, or null for a word carrying none.
     */
    static CobolPreprocessor.@Nullable ExecStatement blockOn(Cobol.Word word) {
        for (CobolPreprocessor ps : word.getPreprocessorStatements()) {
            if (ps instanceof CobolPreprocessor.ExecStatement) {
                return (CobolPreprocessor.ExecStatement) ps;
            }
        }
        return null;
    }

    /**
     * The words of the block body, flattened across continuation lines.
     */
    static List<Cobol.Word> wordsOf(CobolPreprocessor.ExecStatement exec) {
        List<Cobol.Word> words = new ArrayList<>();
        new CobolPreprocessorIsoVisitor<Integer>() {
            @Override
            public CobolPreprocessor.Word visitWord(CobolPreprocessor.Word word, Integer p) {
                words.add(word.getCobolWord());
                return word;
            }
        }.visit(exec.getCobol(), 0);
        return words;
    }

    /**
     * Whether {@code cursor}'s word is the stand-in for the block it carries rather than one of the
     * words within it.
     * <p>
     * A block in the procedure division parses to a statement whose lines are the stand-ins, so
     * membership of that list is the test. A block written anywhere else is elided onto the word
     * that follows it and has no statement to belong to, which is why anything with no such parent
     * counts.
     */
    static boolean isStandIn(Cursor cursor, Cobol.Word word) {
        Object parent = cursor.getParentTreeCursor().getValue();
        List<Cobol.Word> lines =
                parent instanceof Cobol.ExecCicsStatement ? ((Cobol.ExecCicsStatement) parent).getExecCicsLines() :
                parent instanceof Cobol.ExecDliStatement ? ((Cobol.ExecDliStatement) parent).getExecDliLines() :
                parent instanceof Cobol.ExecSqlStatement ? ((Cobol.ExecSqlStatement) parent).getExecSqlLines() :
                parent instanceof Cobol.ExecSqlImsStatement ? ((Cobol.ExecSqlImsStatement) parent).getExecSqlLmsLines() :
                        null;
        if (lines == null) {
            return true;
        }
        for (Cobol.Word line : lines) {
            if (line == word) {
                return true;
            }
        }
        return false;
    }
}
