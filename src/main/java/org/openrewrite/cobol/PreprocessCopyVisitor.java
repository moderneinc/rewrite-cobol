/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.cobol;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.SourceFile;
import org.openrewrite.cobol.markers.CopiedStatement;
import org.openrewrite.cobol.tree.CobolPreprocessor;
import org.openrewrite.internal.ListUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import static org.openrewrite.Tree.randomId;

@EqualsAndHashCode(callSuper = true)
@Value
public class PreprocessCopyVisitor<P> extends CobolPreprocessorIsoVisitor<P> {

    Map<String, SourceFile> copybooks = new HashMap<>();
    Stack<CobolPreprocessor.CopyStatement> copyStack = new Stack<>();

    public PreprocessCopyVisitor(List<SourceFile> copybooks) {
        copybooks.forEach(it -> {
            // Note: this implementation ASSUMES copybooks are resolved by FileName and will require changes.
            String fileName = it.getSourcePath().getFileName().toString();
            this.copybooks.putIfAbsent(fileName.substring(0, fileName.indexOf(".")), it);
        });
    }

    @Override
    public CobolPreprocessor.CopyStatement visitCopyStatement(CobolPreprocessor.CopyStatement copyStatement, P p) {
        CobolPreprocessor.CopyStatement c = super.visitCopyStatement(copyStatement, p);
        copyStack.push(c);

        if (copybooks.containsKey(copyStatement.getCopySource().getName().getCobolWord().getWord())) {
            CobolPreprocessor.Copybook cb = (CobolPreprocessor.Copybook) copybooks.get(copyStatement.getCopySource().getName().getCobolWord().getWord());
            cb = cb.withLst(ListUtils.map(cb.getLst(), l -> visit(l, p)));
            c = c.withCopybook(cb);
            if (copyStack.size() > 1) {
                c = c.withMarkers(c.getMarkers().addIfAbsent(new CopiedStatement(randomId())));
            }
        }
        copyStack.pop();
        return c;
    }
}
