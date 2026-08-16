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
package org.openrewrite.cobol;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.SourceFile;
import org.openrewrite.cobol.marker.CopiedStatement;
import org.openrewrite.cobol.marker.MissingCopybook;
import org.openrewrite.cobol.tree.CobolPreprocessor;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.tree.ParseError;

import java.util.*;

import static org.openrewrite.Tree.randomId;

@EqualsAndHashCode(callSuper = true)
@Value
public class PreprocessCopyVisitor<P> extends CobolPreprocessorIsoVisitor<P> {

    Map<String, SourceFile> copybooks = new HashMap<>();
    Map<String, CobolPreprocessor> preprocessorMap;
    Deque<String> copyStack = new ArrayDeque<>();

    public PreprocessCopyVisitor(Map<String, CobolPreprocessor> preprocessorMap,
                                 List<SourceFile> copybooks) {
        this.preprocessorMap = preprocessorMap;
        copybooks.forEach(it -> {
            String fileName = it.getSourcePath().getFileName().toString();
            this.copybooks.putIfAbsent(memberName(fileName.substring(0, fileName.indexOf("."))), it);
        });
    }

    @Override
    public CobolPreprocessor.CopyStatement visitCopyStatement(CobolPreprocessor.CopyStatement copyStatement, P p) {
        copyStack.push(copyStatement.getCopySource().getName().getCobolWord().getWord());
        CobolPreprocessor.CopyStatement c = (CobolPreprocessor.CopyStatement) resolve(copyStatement, copyStatement.getCopySource().getName().getCobolWord().getWord(), p);
        c = (CobolPreprocessor.CopyStatement) inCopiedStatement(c);
        preprocessorMap.put(c.getId().toString(), c);
        return c;
    }

    @Override
    public CobolPreprocessor.ExecSqlIncludeStatement visitExecSqlIncludeStatement(CobolPreprocessor.ExecSqlIncludeStatement execSqlIncludeStatement, P p) {
        copyStack.push(execSqlIncludeStatement.getCopySource().getCobolWord().getWord());
        CobolPreprocessor.ExecSqlIncludeStatement e = (CobolPreprocessor.ExecSqlIncludeStatement) resolve(execSqlIncludeStatement, execSqlIncludeStatement.getCopySource().getCobolWord().getWord(), p);
        e = (CobolPreprocessor.ExecSqlIncludeStatement) inCopiedStatement(e);
        preprocessorMap.put(e.getId().toString(), e);
        return e;
    }

    private CobolPreprocessor inCopiedStatement(CobolPreprocessor c) {
        if (copyStack.size() > 1) {
            copyStack.pop();
            c = c.withMarkers(c.getMarkers().addIfAbsent(new CopiedStatement(randomId(), copyStack.peek())));
        } else {
            copyStack.pop();
        }
        return c;
    }

    // A copybook name is a PDS member name, which is case insensitive, and COPY takes it either as a
    // word or as a literal. On a distributed filesystem the same member is often a lower case file,
    // so COPY LGCMAREA has to find lgcmarea.cpy and COPY 'CSSTRPFY' has to find CSSTRPFY.cpy.
    private static String memberName(String name) {
        if (name.length() > 1 && (name.charAt(0) == '\'' || name.charAt(0) == '"') &&
                name.charAt(name.length() - 1) == name.charAt(0)) {
            name = name.substring(1, name.length() - 1);
        }
        return name.toUpperCase(Locale.ROOT);
    }

    private CobolPreprocessor resolve(CobolPreprocessor c, String copybookName, P p) {
        String member = memberName(copybookName);
        if (copybooks.containsKey(member)) {
            SourceFile sf = copybooks.get(member);
            if (sf instanceof ParseError) {
                return c.withMarkers(c.getMarkers().addIfAbsent(new MissingCopybook(randomId(), MissingCopybook.Status.PARSE_ERROR)));
            }

            CobolPreprocessor.Copybook cb = (CobolPreprocessor.Copybook) sf;
            cb = cb.withLst(ListUtils.map(cb.getLst(), l -> visit(l, p)));
            if (c instanceof CobolPreprocessor.CopyStatement) {
                c = ((CobolPreprocessor.CopyStatement) c).withCopybook(cb);
            } else {
                c = ((CobolPreprocessor.ExecSqlIncludeStatement) c).withCopybook(cb);
            }
        } else {
            c = c.withMarkers(c.getMarkers().addIfAbsent(new MissingCopybook(randomId(), MissingCopybook.Status.MISSING)));
        }

        return c;
    }

    // Add preprocessor elements to the map. This happens in the CopyVisitor since statements may occur in copybooks.
    @Override
    public CobolPreprocessor.CompilerOption visitCompilerOption(CobolPreprocessor.CompilerOption compilerOption, P p) {
        preprocessorMap.put(compilerOption.getId().toString(), compilerOption);
        return compilerOption;
    }

    @Override
    public CobolPreprocessor.CompilerOptions visitCompilerOptions(CobolPreprocessor.CompilerOptions compilerOptions, P p) {
        preprocessorMap.put(compilerOptions.getId().toString(), compilerOptions);
        return compilerOptions;
    }

    @Override
    public CobolPreprocessor.EjectStatement visitEjectStatement(CobolPreprocessor.EjectStatement ejectStatement, P p) {
        preprocessorMap.put(ejectStatement.getId().toString(), ejectStatement);
        return ejectStatement;
    }

    @Override
    public CobolPreprocessor.ExecStatement visitExecStatement(CobolPreprocessor.ExecStatement execStatement, P p) {
        preprocessorMap.put(execStatement.getId().toString(), execStatement);
        return execStatement;
    }

    @Override
    public CobolPreprocessor.ReplaceByStatement visitReplaceByStatement(CobolPreprocessor.ReplaceByStatement replaceByStatement, P p) {
        preprocessorMap.put(replaceByStatement.getId().toString(), replaceByStatement);
        return replaceByStatement;
    }

    @Override
    public CobolPreprocessor.ReplaceOffStatement visitReplaceOffStatement(CobolPreprocessor.ReplaceOffStatement replaceOffStatement, P p) {
        preprocessorMap.put(replaceOffStatement.getId().toString(), replaceOffStatement);
        return replaceOffStatement;
    }

    @Override
    public CobolPreprocessor.SkipStatement visitSkipStatement(CobolPreprocessor.SkipStatement skipStatement, P p) {
        preprocessorMap.put(skipStatement.getId().toString(), skipStatement);
        return skipStatement;
    }

    @Override
    public CobolPreprocessor.TitleStatement visitTitleStatement(CobolPreprocessor.TitleStatement titleStatement, P p) {
        preprocessorMap.put(titleStatement.getId().toString(), titleStatement);
        return titleStatement;
    }
}
