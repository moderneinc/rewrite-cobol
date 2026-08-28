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
package org.openrewrite.mainframe.cobol.internal;

import org.openrewrite.PrintOutputCapture;
import org.openrewrite.mainframe.cobol.tree.CobolPreprocessor;
import org.openrewrite.mainframe.cobol.tree.Space;

/**
 * Print the original preprocessed COBOL.
 * Note: All the logic to print column areas exists in visitWord.
 */
public class CobolPreprocessorPrinter<P> extends CobolPreprocessorSourcePrinter<P> {

    private final CobolPrinter<P> cobolPrinter;
    private final boolean printOriginalSource;

    public CobolPreprocessorPrinter(boolean printOriginalSource,
                                    boolean printColumns) {
        super(printColumns);
        this.cobolPrinter = new CobolPrinter<>(printColumns, printOriginalSource);
        this.printOriginalSource = printOriginalSource;
    }

    @Override
    public CobolPreprocessor visitCopybook(CobolPreprocessor.Copybook copybook, PrintOutputCapture<P> p) {
        beforeSyntax(copybook, Space.Location.COPY_BOOK_PREFIX, p);
        visit(copybook.getLst(), p);
        // Comment lines after the last word hang off EOF, and a DCLGEN book ends with three of them.
        visit(copybook.getEof(), p);
        afterSyntax(copybook, p);
        return copybook;
    }

    @Override
    public CobolPreprocessor visitCopyStatement(CobolPreprocessor.CopyStatement copyStatement, PrintOutputCapture<P> p) {
        if (printOriginalSource) {
            return super.visitCopyStatement(copyStatement, p);
        }
        if (copyStatement.getCopybook() != null) {
            beforeSyntax(copyStatement, Space.Location.COPY_STATEMENT_PREFIX, p);
            visit(copyStatement.getCopybook(), p);
            if (!p.getOut().endsWith("\n")) {
                p.append("\n");
            }
            afterSyntax(copyStatement, p);
        }
        return copyStatement;
    }

    @Override
    public CobolPreprocessor visitExecSqlIncludeStatement(CobolPreprocessor.ExecSqlIncludeStatement execSqlIncludeStatement, PrintOutputCapture<P> p) {
        if (printOriginalSource) {
            return super.visitExecSqlIncludeStatement(execSqlIncludeStatement, p);
        }
        if (execSqlIncludeStatement.getCopybook() != null) {
            beforeSyntax(execSqlIncludeStatement, Space.Location.EXEC_SQL_INCLUDE_STATEMENT_PREFIX, p);
            visit(execSqlIncludeStatement.getCopybook(), p);
            if (!p.getOut().endsWith("\n")) {
                p.append("\n");
            }
            afterSyntax(execSqlIncludeStatement, p);
        }
        return execSqlIncludeStatement;
    }

    @Override
    public CobolPreprocessor visitWord(CobolPreprocessor.Word word, PrintOutputCapture<P> p) {
        if (printOriginalSource) {
            return super.visitWord(word, p);
        }

        cobolPrinter.visitWord(word.getCobolWord(), p);

        afterSyntax(word, p);
        return word;
    }
}
