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

import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.PrintOutputCapture;
import org.openrewrite.mainframe.cobol.tree.*;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.internal.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.openrewrite.mainframe.cobol.CobolPrinterUtils.*;
import static org.openrewrite.mainframe.cobol.CobolStringUtils.isSubstituteCharacter;
import static org.openrewrite.mainframe.cobol.internal.CobolGrammarToken.*;

/**
 * Print the post processed COBOL AST with comments that act like a `JavaTemplate`.
 * The comments are used to link AST elements that contain the original source code to the COBOL AST.
 * <p>
 * Each key will be added as a comment that is formatted based on the current {@link CobolDialect}.
 * <p>
 * `printWithColumnAreas`:
 *      true: Print as source code with modifications to distinguish changes during preprocessing like COPY and REPLACE.
 *      false: Print as parser input for the CobolParserVisitor.
 */
public class CobolPreprocessorOutputSourcePrinter<P> extends CobolPreprocessorSourcePrinter<P> {
    public static final String COPY_START_KEY = "__COPY_START__";
    public static final String COPY_STOP_KEY = "__COPY_STOP__";
    public static final String COPY_UUID_KEY = "__COPY_UUID__";
    public static final String COPYBOOK_NOT_FOUND = "__COPYBOOK_NOT_FOUND__";

    public static final String REPLACE_START_KEY = "__REPLACE_START__";
    public static final String REPLACE_STOP_KEY = "__REPLACE_STOP__";
    public static final String REPLACE_UUID_KEY = "__REPLACE_UUID__";

    public static final String REPLACE_TYPE_ADDITIVE_START_KEY = "__REPLACE_TYPE_ADDITIVE_START__";
    public static final String REPLACE_TYPE_ADDITIVE_STOP_KEY = "__REPLACE_TYPE_ADDITIVE_STOP__";
    public static final String REPLACE_ADD_WORD_START_KEY = "__REPLACE_ADD_WORD_START__";
    public static final String REPLACE_ADD_WORD_STOP_KEY = "__REPLACE_ADD_WORD_STOP__";

    public static final String REPLACE_TYPE_REDUCTIVE_START_KEY = "__REPLACE_TYPE_REDUCTIVE_START__";
    public static final String REPLACE_TYPE_REDUCTIVE_STOP_KEY = "__REPLACE_TYPE_REDUCTIVE_STOP__";

    public static final String REPLACE_ADDED_WHITESPACE_KEY = "__REPLACE_ADDED_WHITESPACE__";

    public static final String COMPILER_OPTIONS_START_KEY = "__COMPILER_OPTIONS_START__";
    public static final String COMPILER_OPTIONS_STOP_KEY = "__COMPILER_OPTIONS_STOP__";

    public static final String PREPROCESSOR_START_KEY = "__PREPROCESSOR_START__";
    public static final String PREPROCESSOR_STOP_KEY = "__PREPROCESSOR_STOP__";

    public static final String UUID_KEY = "__UUID__";

    private final CobolSourcePrinter<P> cobolSourcePrinter;
    private final CobolDialect cobolDialect;
    private final boolean printColumns;

    // Lazily initialized Strings that are generated once with constraints based on the dialect.
    private String dialectSequenceArea = null;
    private String uuidEndOfLine = null;

    private String copyStartComment = null;
    private String copyStopComment = null;
    private String copyUuidComment = null;
    private String copybookNotFoundComment = null;

    private String replaceStartComment = null;
    private String replaceStopComment = null;
    private String replaceUuidComment = null;

    private String uuidComment = null;

    private String replaceTypeAdditiveStartComment = null;
    private String replaceTypeAdditiveStopComment = null;
    private String replaceAddWordStartComment = null;
    private String replaceAddWordStopComment = null;

    private String replaceTypeReductiveStartComment = null;
    private String replaceTypeReductiveStopComment = null;
    private Replacement replaceReductiveType = null;

    private String replaceAddedWhitespaceComment = null;

    private String compilerOptionsStartComment = null;
    private String compilerOptionsStopComment = null;

    private String preprocessorStartComment = null;
    private String preprocessorStopComment = null;

    private final CobolPreprocessorSourcePrinter<ExecutionContext> statementPrinter = new CobolPreprocessorSourcePrinter<>(false);
    private final CobolSourcePrinter<ExecutionContext> cobolStatementPrinter = new CobolSourcePrinter<>(false);

    private boolean inUnknownIndicator = false;

    private final Set<UUID> elidedExecs = new LinkedHashSet<>();
    private boolean inProcedureDivision = false;
    private String previousWord = "";

    public CobolPreprocessorOutputSourcePrinter(CobolDialect cobolDialect,
                                                boolean printColumns) {
        super(true);
        this.cobolSourcePrinter = new CobolSourcePrinter<>(printColumns);
        this.cobolDialect = cobolDialect;
        this.printColumns = printColumns;
    }

    @Override
    public CobolPreprocessor visitCompilerOptions(CobolPreprocessor.CompilerOptions compilerOptions, PrintOutputCapture<P> p) {
        if (printColumns) {
            int curIndex = getCurrentIndex(p.getOut());
            curIndex = curIndex == -1 ? 0 : curIndex;
            addStartKey(getCompilerOptionsStartComment(), curIndex, p);
            addUuidKey(getUuidComment(), compilerOptions.getId(), p);
            addStopComment(getCompilerOptionsStopComment(), null, curIndex, p);
        }
        return compilerOptions;
    }

    @Override
    public CobolPreprocessor visitCommentEntry(CobolPreprocessor.CommentEntry commentEntry, PrintOutputCapture<P> p) {
        if (printColumns) {
            super.visitCommentEntry(commentEntry, p);
        } else {
            visitSpace(commentEntry.getPrefix(), Space.Location.COMMENT_ENTRY_PREFIX, p);
            visitMarkers(commentEntry.getMarkers(), p);
            for (CobolPreprocessor.Word comment : commentEntry.getComments()) {
                p.append(COMMENT_ENTRY);
                visit(comment, p);
            }
        }

        return commentEntry;
    }

    @Override
    public CobolPreprocessor visitCopybook(CobolPreprocessor.Copybook copybook, PrintOutputCapture<P> p) {
        visitSpace(copybook.getPrefix(), Space.Location.COPY_BOOK_PREFIX, p);
        visitMarkers(copybook.getMarkers(), p);
        for (CobolPreprocessor cobolPreprocessor : copybook.getLst()) {
            if (!(cobolPreprocessor instanceof CobolPreprocessor.CompilerOptions ||
                    cobolPreprocessor instanceof CobolPreprocessor.EjectStatement ||
                    cobolPreprocessor instanceof CobolPreprocessor.ExecStatement ||
                    cobolPreprocessor instanceof CobolPreprocessor.SkipStatement ||
                    cobolPreprocessor instanceof CobolPreprocessor.TitleStatement)) {
                visit(cobolPreprocessor, p);
            }
        }

        visit(copybook.getEof(), p);
        return copybook;
    }

    @Override
    public CobolPreprocessor visitCopyStatement(CobolPreprocessor.CopyStatement copyStatement, PrintOutputCapture<P> p) {
        visitSpace(copyStatement.getPrefix(), Space.Location.COPY_STATEMENT_PREFIX, p);
        visitMarkers(copyStatement.getMarkers(), p);

        printCopybookSource(copyStatement, p);

        return copyStatement;
    }

    private void printCopybookSource(CopybookSource copybookSource, PrintOutputCapture<P> p) {
        if (!printColumns) {
            visit(copybookSource.getCopybook(), p);
            if (!p.getOut().endsWith("\n")) {
                p.append("\n");
            }
            return;
        }

        // Printing the COPY statement will add comments that work similar to JavaTemplate.
        // Comments are added before and after the template to provide context about which AST elements
        // are a product of a COPY statement.

        /*
         *  Before:
         *      |000001| |Some COBOL tokens|      COPY STATEMENT.           |
         *
         *  After:
         *      |      |*|__COPY_START______________________________________|=> Trigger search for end of line to detect whitespace added from the template.
         *      |000001| |Some COBOL tokens|                                |=> The index + 1 is the position of `|`.
         *      |      |*|__UUID____________________________________________|=> Detect the UUID section of the template.
         *      |      |*|263cd588-bdea-4c06-8ba1-177e515bded2              |=> UUID to the CopyStatement; a UUID will fit in the column area, but the copy statement might not.
         *      |~~~~~~| |Print the COPIED source AST. ~~~~~~~~~~~~~~~~~~~~~|=> Print the COPIED AST, which includes new column areas.
         *      |      |*|__COPY_END________________________________________|
         *      |      |*|33                                                |=> # of spaces added to align the column areas.
         *      |      | |[WS for tokens  ]|[WS for COPY        ]|=> White space is conditionally printer based on where the copy statement ends to ensure columns are aligned.
         *
         *  Alignment:
         *      |      | | COPY STATEMENT.                                  |=> Requires whitespace to replace the statement for correct alignment.
         *      |      | |                                   COPY STATEMENT.|=> The next line does not require any whitespace.
         */

        // Print markers like Lines, SequenceArea, and Indicator if the line starts with COPY.
        if (copybookSource instanceof CobolPreprocessor.CopyStatement) {
            CobolPreprocessor.CopyStatement cs = (CobolPreprocessor.CopyStatement) copybookSource;
            visit(cs.getWord(), p);
            p.out.delete(p.getOut().length() - cs.getWord().getCobolWord().getWord().length() -
                    cs.getWord().getPrefix().getWhitespace().length(), p.getOut().length());
        } else {
            CobolPreprocessor.ExecSqlIncludeStatement es = (CobolPreprocessor.ExecSqlIncludeStatement) copybookSource;
            visit(es.getWords().get(0), p);
            p.out.delete(p.getOut().length() - es.getWords().get(0).getCobolWord().getWord().length() -
                    es.getWords().get(0).getPrefix().getWhitespace().length(), p.getOut().length());
        }

        // Save the current index to ensure the text that follows the COPY will be aligned correctly.
        int curIndex = getCurrentIndex(p.getOut());

        addStartKey(getCopyStartComment(), curIndex, p);
        addUuidKey(getCopyUuidKey(), ((CobolPreprocessor) copybookSource).getId(), p);

        // Print copied source.
        if (copybookSource.getCopybook() == null) {
            // Assume the copy statement is not found, and the copybook is not sub grammatical.
            p.append(getCopybookNotFound());
        } else {
            visit(copybookSource.getCopybook(), p);
        }
        if (!p.getOut().endsWith("\n")) {
            // Add a new line character if the copied source does not end with one already.
            p.append("\n");
        }

        addStopComment(getCopyStopComment(), (CobolPreprocessor) copybookSource, curIndex, p);
    }

    @Override
    public CobolPreprocessor visitEjectStatement(CobolPreprocessor.EjectStatement ejectStatement, PrintOutputCapture<P> p) {
        addPreprocessorStatementTemplate(ejectStatement, p);
        return ejectStatement;
    }

    @Override
    public CobolPreprocessor visitExecStatement(CobolPreprocessor.ExecStatement execStatement, PrintOutputCapture<P> p) {
        addPreprocessorStatementTemplate(execStatement, p);

        // Stand the elided block up as a tagged line so that the COBOL grammar still sees a statement, and re-emit the
        // period it took with it so that the sentence is still terminated. Outside the procedure division an EXEC is
        // not a statement and a period is not a sentence terminator, so there the block stays elided.
        if (!printColumns && inProcedureDivision) {
            String tag = execTag(execStatement);
            if (tag != null) {
                elidedExecs.add(execStatement.getId());
                p.append(tag).append("\n");
                if (execStatement.getDot() != null) {
                    p.append(".\n");
                }
            }
        }
        return execStatement;
    }

    private static @Nullable String execTag(CobolPreprocessor.ExecStatement execStatement) {
        if (execStatement.getWords().size() < 2) {
            return null;
        }
        switch (execStatement.getWords().get(1).getCobolWord().getWord().toUpperCase()) {
            case "CICS":
                return EXEC_CICS;
            case "DLI":
                return EXEC_DLI;
            case "SQL":
                return EXEC_SQL;
            case "SQLIMS":
                return EXEC_SQL_IMS;
            default:
                return null;
        }
    }

    /**
     * IDs of the EXEC statements that were re-emitted into the parser input as a tagged line. The words the COBOL
     * grammar creates for one of them, and for the period that followed it, consume no source.
     */
    public Set<UUID> getElidedExecs() {
        return elidedExecs;
    }

    @Override
    public CobolPreprocessor visitExecSqlIncludeStatement(CobolPreprocessor.ExecSqlIncludeStatement execSqlIncludeStatement, PrintOutputCapture<P> p) {
        visitSpace(execSqlIncludeStatement.getPrefix(), Space.Location.EXEC_SQL_INCLUDE_STATEMENT_PREFIX, p);
        visitMarkers(execSqlIncludeStatement.getMarkers(), p);

        printCopybookSource(execSqlIncludeStatement, p);

        return execSqlIncludeStatement;
    }

    @Override
    public CobolPreprocessor visitReplaceArea(CobolPreprocessor.ReplaceArea replaceArea, PrintOutputCapture<P> p) {
        visit(replaceArea.getReplaceByStatement(), p);

        if (replaceArea.getCobols() != null) {
            for (CobolPreprocessor cobol : replaceArea.getCobols()) {
                visit(cobol, p);
            }
        }

        visit(replaceArea.getReplaceOffStatement(), p);
        return replaceArea;
    }

    @Override
    public CobolPreprocessor visitReplaceByStatement(CobolPreprocessor.ReplaceByStatement replaceByStatement, PrintOutputCapture<P> p) {
        addPreprocessorStatementTemplate(replaceByStatement, p);
        return replaceByStatement;
    }

    @Override
    public CobolPreprocessor visitReplaceOffStatement(CobolPreprocessor.ReplaceOffStatement replaceOffStatement, PrintOutputCapture<P> p) {
        addPreprocessorStatementTemplate(replaceOffStatement, p);
        return replaceOffStatement;
    }

    @Override
    public CobolPreprocessor visitSkipStatement(CobolPreprocessor.SkipStatement skipStatement, PrintOutputCapture<P> p) {
        addPreprocessorStatementTemplate(skipStatement, p);
        return skipStatement;
    }

    @Override
    public CobolPreprocessor visitTitleStatement(CobolPreprocessor.TitleStatement titleStatement, PrintOutputCapture<P> p) {
        addPreprocessorStatementTemplate(titleStatement, p);
        return titleStatement;
    }

    private void addPreprocessorStatementTemplate(CobolPreprocessor cobolPreprocessor, PrintOutputCapture<P> p) {
        if (printColumns) {
            int curIndex = getCurrentIndex(p.getOut());
            curIndex = curIndex == -1 ? 0 : curIndex;
            addStartKey(getPreprocessorStartComment(), curIndex, p);
            addUuidKey(getUuidComment(), cobolPreprocessor.getId(), p);
            addStopComment(getPreprocessorStopComment(), null, curIndex, p);
        }
    }

    @Override
    public CobolPreprocessor visitWord(CobolPreprocessor.Word word, PrintOutputCapture<P> p) {
        String currentWord = word.getCobolWord().getWord();
        if ("DIVISION".equalsIgnoreCase(currentWord)) {
            inProcedureDivision = "PROCEDURE".equalsIgnoreCase(previousWord);
        }
        previousWord = currentWord;

        if (!printColumns) {
            // Do not print words on lines with an unknown indicator until we know how to handle them.
            // Note: Unknown indicators are treated as comments via source code in CobolParserVisitor#isCommentIndicator.
            if (word.getCobolWord().getIndicatorArea() != null) {
                String indicator = word.getCobolWord().getIndicatorArea().getIndicator();
                if ("G".equals(indicator) || "J".equals(indicator) || "P".equals(indicator)) {
                    // TODO: add a form of visibility for an unrecognized indicator.
                    inUnknownIndicator = true;
                }
            }

            if (!inUnknownIndicator) {
                if (word.getCobolWord().getReplacement() != null && word.getCobolWord().getReplacement().getType() == Replacement.Type.ADDITIVE) {
                    Replacement replaceAdditiveType = word.getCobolWord().getReplacement();
                    for (Replacement.OriginalWord additionalWord : replaceAdditiveType.getOriginalWords()) {
                        cobolSourcePrinter.visit(additionalWord.getOriginal(), p);
                    }
                    int curIndex = getCurrentIndex(p.getOut());
                    int contentEnd = cobolDialect.getColumns().getOtherArea();
                    int untilEndOfLine = curIndex >= contentEnd ? 0 : cobolDialect.getColumns().getOtherArea() - curIndex;
                    String whitespace = generateWhitespace(untilEndOfLine) + "\n";
                    p.append(whitespace);
                }

                visitSpace(word.getPrefix(), Space.Location.WORD_PREFIX, p);
                visitMarkers(word.getMarkers(), p);
                p.append(word.getCobolWord().getWord());

                if (word.getCobolWord().getCommentArea() != null) {
                    visitSpace(word.getCobolWord().getCommentArea().getPrefix(), Space.Location.COMMENT_AREA_PREFIX, p);
                    visitSpace(word.getCobolWord().getCommentArea().getEndOfLine(), Space.Location.COMMENT_AREA_EOL, p);
                }
            }

            if (inUnknownIndicator && word.getCobolWord().getCommentArea() != null) {
                inUnknownIndicator = false;
            }
            return word;
        }

        // Beware all who enter.
        // Replace contains many special cases due to changes in column alignment after a replacement.
        Replacement replacement = word.getCobolWord().getReplacement();
        if (replacement != null && replacement.getType() == Replacement.Type.EQUAL) {
            List<CobolLine> lines = replacement.getOriginalWords().get(0).getOriginal().getLines();
            if (lines != null) {
                ListUtils.map(lines, l -> cobolSourcePrinter.visitLine(l, p));
            }

            replaceTemplate(word, p, replacement);
        } else if (replacement != null && replacement.getType() == Replacement.Type.REDUCTIVE) {
            if (replaceReductiveType == null) {
                replaceReductiveType = replacement;
            }
        } else if (replaceReductiveType != null) {
            replaceReductiveTemplate(p);

            super.visitWord(word, p);
            replaceReductiveType = null;
        } else if (replacement != null && replacement.getType() == Replacement.Type.ADDITIVE) {
            replaceAdditiveTemplate(replacement, p);
            super.visitWord(word, p);
        } else if (!isSubstituteCharacter(word.getCobolWord().getWord())) {
            super.visitWord(word, p);
        }
        return word;
    }

    private void replaceAdditiveTemplate(Replacement replaceAdditiveType, PrintOutputCapture<P> p) {
        // Fill the remaining line with whitespace to align the column areas.
        int curIndex = getCurrentIndex(p.getOut());
        int contentEnd = cobolDialect.getColumns().getOtherArea();
        int untilEndOfLine = curIndex >= contentEnd ? 0 : cobolDialect.getColumns().getOtherArea() - curIndex;
        String whitespace = generateWhitespace(untilEndOfLine) + "\n";
        p.append(whitespace);

        int startKeyIndex = getCurrentIndex(p.getOut());
        addStartKey(getReplaceTypeAdditiveStartComment(), startKeyIndex, p);
        addUuidKey(getUuidComment(), replaceAdditiveType.getId(), p);
        for (Replacement.OriginalWord additionalWord : replaceAdditiveType.getOriginalWords()) {
            p.append(getReplaceAddWordStartComment());
            String addedWord = getDialectSequenceArea() + " " + additionalWord.getOriginal().getPrefix().getWhitespace() + additionalWord.getOriginal().getWord();
            p.append(addedWord);
            int addedIndex = getCurrentIndex(p.getOut());
            untilEndOfLine = addedIndex >= contentEnd ? 0 : cobolDialect.getColumns().getOtherArea() - addedIndex;
            String addWhitespace = generateWhitespace(untilEndOfLine) + "\n";
            p.append(addWhitespace);
            p.append(getReplaceAddWordStopComment());
        }
        addStopComment(getReplaceTypeAdditiveStopComment(), null, curIndex, p);
    }

    private void replaceReductiveTemplate(PrintOutputCapture<P> p) {
        // Print the markers from the original words and replace the original words with whitespace.
        for (Replacement.OriginalWord replace : replaceReductiveType.getOriginalWords()) {
            Cobol.Word originalWord = replace.getOriginal();
            if (originalWord.getContinuation() != null) {
                throw new UnsupportedOperationException("Implement continuation lines for a reductive replacement.");
            }

            if (originalWord.getSequenceArea() != null) {
                originalWord.getSequenceArea().printColumnArea(this, getCursor(), printColumns, p);
            }

            if (originalWord.getIndicatorArea() != null) {
                originalWord.getIndicatorArea().printColumnArea(this, getCursor(), printColumns, p);
            }

            visitSpace(originalWord.getPrefix(), Space.Location.WORD_PREFIX, p);

            String replaceWithWhitespace = generateWhitespace(originalWord.getWord().length());
            p.append(replaceWithWhitespace);

            if (originalWord.getCommentArea() != null) {
                originalWord.getCommentArea().printColumnArea(this, getCursor(), printColumns, p);
            }
        }

        // Save the current index to ensure the text that follows the REPLACE will be aligned correctly.
        int curIndex = getCurrentIndex(p.getOut());
        if (curIndex == -1) {
            throw new UnsupportedOperationException("Unknown case: Detected a ReplaceTypeReductive at the start of the source code.");
        }

        // Fill the remaining line with whitespace to align the column areas.
        // Reductive changes require printing the original column areas with the original word replaced by whitespace.
        // The last replaced word might end the line, so the current index might be greater than (CommentArea) or
        // equal to the end of the content area.
        int contentEnd = cobolDialect.getColumns().getOtherArea();
        int untilEndOfLine = curIndex >= contentEnd ? 0 : cobolDialect.getColumns().getOtherArea() - curIndex;
        String whitespace = generateWhitespace(untilEndOfLine) + "\n";
        p.append(whitespace);

        // Add Start key.
        p.append(getReplaceTypeReductiveStartComment());

        // Add UUID key.
        addUuidKey(getUuidComment(), replaceReductiveType.getId(), p);

        // Add Stop key.
        p.append(getReplaceTypeReductiveStopComment());

        // Add whitespace until the next token will be aligned with the column area.
        String afterStop = getColumnAlignmentAfterStop(curIndex);
        p.append(afterStop);
        p.append(StringUtils.repeat(" ", curIndex >= contentEnd ? 0 : curIndex));
    }

    private void replaceTemplate(CobolPreprocessor.Word word, PrintOutputCapture<P> p, Replacement replacement) {
        // Save the current index to ensure the text that follows the REPLACE will be aligned correctly.
        int curIndex = getCurrentIndex(p.getOut());
        if (curIndex == -1) {
            throw new UnsupportedOperationException("Unknown case: Detected a Replace at the start of the source code.");
        }

        Cobol.Word originalWord = replacement.getOriginalWords().get(0).getOriginal();
        boolean isLongerWord = word.getCobolWord().getWord().length() > originalWord.getWord().length();
        String replacedWord = isLongerWord ? " " + word.getCobolWord().getWord() : word.getCobolWord().getWord();

        boolean isLiteral = word.getCobolWord().getWord().startsWith("\"") || word.getCobolWord().getWord().startsWith("'");
        int contentAreaLength = getContentAreaLength(cobolDialect);
        boolean isContinuedLiteral = isLiteral && (curIndex + replacedWord.length()) > contentAreaLength;

        // Add Start key.
        int insertIndex = getInsertIndex(p.getOut());
        p.out.insert(insertIndex, getReplaceStartComment());

        if (isLongerWord && !isContinuedLiteral) {
            insertIndex = getInsertIndex(p.getOut());
            p.out.insert(insertIndex, getReplaceAddedWhitespaceComment());
        }

        if (curIndex == 0) {
            if (originalWord.getSequenceArea() != null) {
                originalWord.getSequenceArea().printColumnArea(this, getCursor(), printColumns, p);
            }
            if (originalWord.getIndicatorArea() != null) {
                originalWord.getIndicatorArea().printColumnArea(this, getCursor(), printColumns, p);
            }
        }

        // Fill in the rest of the content area with whitespace.
        int untilEndOfLine =  cobolDialect.getColumns().getOtherArea() - (curIndex == 0 ? cobolDialect.getColumns().getContentArea() : curIndex);
        String whitespace = generateWhitespace(untilEndOfLine) + "\n";
        p.append(whitespace);

        // Add UUID key.
        addUuidKey(getReplaceUuidComment(), replacement.getId(), p);

        // Add Stop key.
        p.append(getReplaceStopComment());

        // Additive replacement like PIC to PICTURE.
        if (isLongerWord) {
            if (isContinuedLiteral) {
                int numberOfSpaces = curIndex == 0 ? cobolDialect.getColumns().getContentArea() : curIndex;
                String afterStop = getColumnAlignmentAfterStop(numberOfSpaces);
                p.append(afterStop);
                p.append(StringUtils.repeat(" ", numberOfSpaces));

                // The current word must be a literal.
                /*
                 *  I.E. "Z" replaced by """""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""
                 *  Before:
                 *      |036800| |    MOVE "Z"| TO WRK-XN-00322.         |SM2084.2
                 *
                 *  After:
                 *      |      | |REPLACE_START__________________________|
                 *      |036800| |    MOVE """"""""""""""""""""""""""""""|
                 *      |      |-|"""""""""""""""""""""""""""""""""""""""|
                 *      |      |-|""""""""""""| TO WRK-XN-00322.         |SM2084.2
                 */

                // Predetermine the length of the end of the literal to align the column areas with the next token.
                String dialectSequenceArea = getDialectSequenceArea();
                int originalLength = originalWord.getPrefix().getWhitespace().length() + originalWord.getWord().length();
                int endPos = replacedWord.length() - curIndex + getDialectSequenceArea().length() + 1 - originalLength;
                String end = dialectSequenceArea + "-" + replacedWord.substring(endPos);
                replacedWord = replacedWord.substring(0, replacedWord.length() - end.length());

                // Split the rest of the literal into continuable parts.
                int remainder = replacedWord.length() % contentAreaLength;
                int size = replacedWord.length() / contentAreaLength + (remainder == 0 ? 0 : 1);
                List<String> parts = new ArrayList<>(size);
                int total = replacedWord.length();
                for (int i = 0; i < size; i++) {
                    if (total - contentAreaLength >= 0) {
                        String part = replacedWord.substring(total - contentAreaLength, total);
                        parts.add(part);
                        total -= part.length();
                    } else {
                        if (total != remainder) {
                            throw new IllegalStateException("Unexpected remained calculating replacement end position.");
                        }
                        parts.add(replacedWord.substring(0, remainder));
                    }
                }

                for (int i = parts.size() - 1; i >= 0; i--) {
                    String part = parts.get(i);
                    if (i != parts.size() - 1) {
                        p.append(getDialectSequenceArea());
                        p.append("-");
                    }
                    p.append(part);
                    if (i == parts.size() - 1 && part.length() < contentAreaLength) {
                        // // Total area to be filled - existing characters.
                        untilEndOfLine = (getDialectSequenceArea().length() + 1 + contentAreaLength) - (curIndex + part.length());
                        whitespace = generateWhitespace(untilEndOfLine);
                        p.append(whitespace);
                    }
                    p.append("\n");
                }
                p.append(end);
            } else {
                int difference = word.getCobolWord().getWord().length() - originalWord.getWord().length();
                int alignColumn = curIndex == 0 ? cobolDialect.getColumns().getContentArea() : curIndex;
                int total = alignColumn + word.getPrefix().getWhitespace().length() - difference;
                if (total > cobolDialect.getColumns().getOtherArea()) {
                    throw new UnsupportedOperationException("The position of the replaced word exceeds the column area.");
                }
                String prefix = generateWhitespace(total);
                p.append(prefix);
                p.append(word.getCobolWord().getWord());
            }
        } else {
            PrintOutputCapture<ExecutionContext> outputCapture = new PrintOutputCapture<>(new InMemoryExecutionContext());
            cobolStatementPrinter.visit(originalWord, outputCapture);

            String statement = outputCapture.getOut();
            boolean isEndOfLine = statement.endsWith("\n");
            boolean isCRLF = statement.endsWith("\r\n");

            int totalChars = statement.length() + curIndex - cobolDialect.getColumns().getContentArea() - (isEndOfLine ? (isCRLF ? 2 : 1) : 0);

            int numberOfSpaces;
            if (!isEndOfLine && totalChars > contentAreaLength) {
                String replaced = word.print(getCursor());
                numberOfSpaces = getCurrentIndex(replaced);
            } else {
                numberOfSpaces = curIndex == 0 ? cobolDialect.getColumns().getContentArea() : curIndex;
            }

            String afterStop = getColumnAlignmentAfterStop(numberOfSpaces);
            p.append(afterStop);
            p.append(StringUtils.repeat(" ", numberOfSpaces));

            /*  The original word is <= the length of the replaced word.
             *  To retain column alignment, the prefix is shifted left with whitespace equal to the difference between the original word and the replaced word.
             *
             *  I.E. PICTURE replaced by PIC.
             *  Before:
             *      |000001| | firstWord PICTURE secondWord         |
             *  After:
             *      |000001| | firstWord     PIC secondWord         |
             */
            int difference = originalWord.getWord().length() - word.getCobolWord().getWord().length();
            // The difference exceeds the content area.
            if (curIndex + difference > cobolDialect.getColumns().getOtherArea()) {
                String fullWord = word.print(getCursor());
                int lastIndex = fullWord.lastIndexOf("\n");
                fullWord = fullWord.substring(lastIndex + 1);

                if (fullWord.length() - word.getCobolWord().getWord().length() < curIndex) {
                    p.out.delete(p.getOut().length() - (curIndex - (fullWord.length() - word.getCobolWord().getWord().length())), p.getOut().length());
                } else {
                    p.append(StringUtils.repeat(" ", fullWord.length() - word.getCobolWord().getWord().length() - curIndex));
                }

                p.append(word.getCobolWord().getWord());
            } else {
                String additionalPrefix = StringUtils.repeat(" ", difference);
                p.append(additionalPrefix);
                visitSpace(word.getPrefix(), Space.Location.WORD_PREFIX, p);
                p.append(word.getCobolWord().getWord());
            }
        }

        if (word.getCobolWord().getCommentArea() != null) {
            word.getCobolWord().getCommentArea().printColumnArea(this, getCursor(), printColumns, p);
        }
    }

    /**
     * Add a templates START comment.
     * @param startComment start comment for the template type.
     * @param curIndex the position of the current index.
     */
    private void addStartKey(String startComment, int curIndex, PrintOutputCapture<P> p) {
        if (curIndex == -1) {
            throw new UnsupportedOperationException("Negative index detected for: " + startComment);
        }

        int insertIndex = getInsertIndex(p.getOut());
        p.out.insert(insertIndex, startComment);

        // Fill the remaining line with whitespace to align the column areas.
        int untilEndOfLine = cobolDialect.getColumns().getOtherArea() - curIndex;
        String whitespace = generateWhitespace(untilEndOfLine) + "\n";
        p.append(whitespace);
    }

    /**
     * Add a templates UUID comment.
     * @param uuidComment uuid comment for the template type.
     * @param uuid uuid from an AST element to retrieve.
     */
    private void addUuidKey(String uuidComment, UUID uuid, PrintOutputCapture<P> p) {
        p.append(uuidComment);
        String replaceUuidLine = getDialectSequenceArea() + "*" + uuid + getUuidEndOfLine();
        p.append(replaceUuidLine);
    }

    /**
     * Add a template STOP comment and align the whitespace for the next Word.
     * @param stopComment STOP comment for the template type.
     * @param statement current preprocessor element being processed.
     * @param curIndex the cursor position before the current statement.
     */
    private void addStopComment(String stopComment, @Nullable CobolPreprocessor statement, int curIndex, PrintOutputCapture<P> p) {
        p.append(stopComment);

        PrintOutputCapture<ExecutionContext> outputCapture = new PrintOutputCapture<>(new InMemoryExecutionContext());
        statementPrinter.visit(statement, outputCapture);

        String output = outputCapture.getOut();
        boolean isEndOfLine = output.endsWith("\n");
        boolean isCRLF = output.endsWith("\r\n");

        int totalChars = output.length() + curIndex - cobolDialect.getColumns().getContentArea() - (isEndOfLine ? (isCRLF ? 2 : 1) : 0);
        int contentAreaLength = getContentAreaLength(cobolDialect);

        int numberOfSpaces;
        if (!isEndOfLine && totalChars > contentAreaLength) {
            numberOfSpaces = 0;
        } else {
            numberOfSpaces = isEndOfLine ? 0 : output.length() + curIndex;
        }

        String afterStop = getColumnAlignmentAfterStop(numberOfSpaces);
        p.append(afterStop);
        if (numberOfSpaces != cobolDialect.getColumns().getOtherArea()) {
            p.append(StringUtils.repeat(" ", numberOfSpaces));
        }
    }

    /**
     * Calculate the whitespace required to align the column area based on the position of the previous word.
     * Then return a Template Comment with the calculated information.
     */
    private String getColumnAlignmentAfterStop(int lengthOfPrefix) {
        if (lengthOfPrefix > 0 && lengthOfPrefix - cobolDialect.getColumns().getContentArea() < 0) {
            throw new IllegalStateException("Negative index detected.");
        }

        int startOfContentArea = cobolDialect.getColumns().getContentArea();
        int endOfContentArea = cobolDialect.getColumns().getOtherArea();

        int prefixLength = lengthOfPrefix == 0 || lengthOfPrefix >= endOfContentArea ? 0 : (lengthOfPrefix - startOfContentArea);
        prefixLength = prefixLength == endOfContentArea - startOfContentArea ? 0 : prefixLength;

        String alignmentKey = getDialectSequenceArea() + "*" + prefixLength;
        String whitespace = generateWhitespace(endOfContentArea - alignmentKey.length());
        return alignmentKey + whitespace + "\n";
    }

    /**
     * Generate a comment based on the Key and the length of the ContentArea in the COBOL dialect.
     */
    private String getTemplateComment(String key) {
        String start = getDialectSequenceArea() + "*" + key;
        return start + StringUtils.repeat("_", cobolDialect.getColumns().getOtherArea() - start.length()) + "\n";
    }

    public String getCopyStartComment() {
        if (copyStartComment == null) {
            copyStartComment = getTemplateComment(COPY_START_KEY);
        }
        return copyStartComment;
    }

    public String getCopyStopComment() {
        if (copyStopComment == null) {
            copyStopComment = getTemplateComment(COPY_STOP_KEY);
        }
        return copyStopComment;
    }

    public String getCopyUuidKey() {
        if (copyUuidComment == null) {
            copyUuidComment = getTemplateComment(COPY_UUID_KEY);
        }
        return copyUuidComment;
    }

    public String getCopybookNotFound() {
        if (copybookNotFoundComment == null) {
            copybookNotFoundComment = getTemplateComment(COPYBOOK_NOT_FOUND);
        }
        return copybookNotFoundComment;
    }

    public String getReplaceStartComment() {
        if (replaceStartComment == null) {
            replaceStartComment = getTemplateComment(REPLACE_START_KEY);
        }
        return replaceStartComment;
    }

    public String getReplaceStopComment() {
        if (replaceStopComment == null) {
            replaceStopComment = getTemplateComment(REPLACE_STOP_KEY);
        }
        return replaceStopComment;
    }

    public String getReplaceUuidComment() {
        if (replaceUuidComment == null) {
            replaceUuidComment = getTemplateComment(REPLACE_UUID_KEY);
        }
        return replaceUuidComment;
    }

    public String getReplaceAddedWhitespaceComment() {
        if (replaceAddedWhitespaceComment == null) {
            replaceAddedWhitespaceComment = getTemplateComment(REPLACE_ADDED_WHITESPACE_KEY);
        }
        return replaceAddedWhitespaceComment;
    }

    public String getReplaceAddWordStartComment() {
        if (replaceAddWordStartComment == null) {
            replaceAddWordStartComment = getTemplateComment(REPLACE_ADD_WORD_START_KEY);
        }
        return replaceAddWordStartComment;
    }

    public String getReplaceAddWordStopComment() {
        if (replaceAddWordStopComment == null) {
            replaceAddWordStopComment = getTemplateComment(REPLACE_ADD_WORD_STOP_KEY);
        }
        return replaceAddWordStopComment;
    }

    public String getReplaceTypeAdditiveStartComment() {
        if (replaceTypeAdditiveStartComment == null) {
            replaceTypeAdditiveStartComment = getTemplateComment(REPLACE_TYPE_ADDITIVE_START_KEY);
        }
        return replaceTypeAdditiveStartComment;
    }

    public String getReplaceTypeAdditiveStopComment() {
        if (replaceTypeAdditiveStopComment == null) {
            replaceTypeAdditiveStopComment = getTemplateComment(REPLACE_TYPE_ADDITIVE_STOP_KEY);
        }
        return replaceTypeAdditiveStopComment;
    }

    public String getReplaceTypeReductiveStartComment() {
        if (replaceTypeReductiveStartComment == null) {
            replaceTypeReductiveStartComment = getTemplateComment(REPLACE_TYPE_REDUCTIVE_START_KEY);
        }
        return replaceTypeReductiveStartComment;
    }

    public String getReplaceTypeReductiveStopComment() {
        if (replaceTypeReductiveStopComment == null) {
            replaceTypeReductiveStopComment = getTemplateComment(REPLACE_TYPE_REDUCTIVE_STOP_KEY);
        }
        return replaceTypeReductiveStopComment;
    }

    public String getCompilerOptionsStartComment() {
        if (compilerOptionsStartComment == null) {
            compilerOptionsStartComment = getTemplateComment(COMPILER_OPTIONS_START_KEY);
        }
        return compilerOptionsStartComment;
    }

    public String getCompilerOptionsStopComment() {
        if (compilerOptionsStopComment == null) {
            compilerOptionsStopComment = getTemplateComment(COMPILER_OPTIONS_STOP_KEY);
        }
        return compilerOptionsStopComment;
    }

    public String getPreprocessorStartComment() {
        if (preprocessorStartComment == null) {
            preprocessorStartComment = getTemplateComment(PREPROCESSOR_START_KEY);
        }
        return preprocessorStartComment;
    }

    public String getPreprocessorStopComment() {
        if (preprocessorStopComment == null) {
            preprocessorStopComment = getTemplateComment(PREPROCESSOR_STOP_KEY);
        }
        return preprocessorStopComment;
    }

    public String getUuidComment() {
        if (uuidComment == null) {
            uuidComment = getTemplateComment(UUID_KEY);
        }
        return uuidComment;
    }

    private String getUuidEndOfLine() {
        if (uuidEndOfLine == null) {
            uuidEndOfLine = StringUtils.repeat(" ", cobolDialect.getColumns().getOtherArea() - cobolDialect.getColumns().getContentArea() - 36) + "\n";
        }
        return uuidEndOfLine;
    }

    private String getDialectSequenceArea() {
        if (dialectSequenceArea == null) {
            dialectSequenceArea = StringUtils.repeat(" ", cobolDialect.getColumns().getContentArea() - 1);
        }
        return dialectSequenceArea;
    }
}
