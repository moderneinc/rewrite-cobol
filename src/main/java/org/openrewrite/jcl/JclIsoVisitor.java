/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.jcl;

import org.openrewrite.jcl.tree.Jcl;

public class JclIsoVisitor<P> extends JclVisitor<P> {

    @Override
    public Jcl.CompilationUnit visitCompilationUnit(Jcl.CompilationUnit compilationUnit, P p) {
        return (Jcl.CompilationUnit) super.visitCompilationUnit(compilationUnit, p);
    }

    @Override
    public Jcl.Comment visitComment(Jcl.Comment comment, P p) {
        return (Jcl.Comment) super.visitComment(comment, p);
    }

    @Override
    public Jcl.ControlM visitControlM(Jcl.ControlM controlM, P p) {
        return (Jcl.ControlM) super.visitControlM(controlM, p);
    }

    @Override
    public Jcl.JclStatement visitJclStatement(Jcl.JclStatement jclStatement, P p) {
        return (Jcl.JclStatement) super.visitJclStatement(jclStatement, p);
    }

    @Override
    public Jcl.Jes2 visitJes2(Jcl.Jes2 jes2, P p) {
        return (Jcl.Jes2) super.visitJes2(jes2, p);
    }

    @Override
    public Jcl.Jes3 visitJes3(Jcl.Jes3 jes3, P p) {
        return (Jcl.Jes3) super.visitJes3(jes3, p);
    }

    @Override
    public Jcl.Unknown visitUnknown(Jcl.Unknown unknown, P p) {
        return (Jcl.Unknown) super.visitUnknown(unknown, p);
    }

    @Override
    public Jcl.Word visitWord(Jcl.Word word, P p) {
        return (Jcl.Word) super.visitWord(word, p);
    }
}
