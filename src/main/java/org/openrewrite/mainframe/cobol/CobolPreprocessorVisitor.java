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
package org.openrewrite.mainframe.cobol;

import lombok.AllArgsConstructor;
import org.openrewrite.Cursor;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.mainframe.cobol.tree.Cobol;
import org.openrewrite.mainframe.cobol.tree.CobolPreprocessor;
import org.openrewrite.mainframe.cobol.tree.Space;
import org.openrewrite.internal.ListUtils;

@AllArgsConstructor
public class CobolPreprocessorVisitor<P> extends TreeVisitor<CobolPreprocessor, P> {

    protected CobolVisitor<P> cobolVisitor;

    public CobolPreprocessorVisitor() {
        this.cobolVisitor = new CobolVisitor<>();
    }

    /**
     * Override adapt() to handle classloader isolation issues.
     * <p>
     * When recipes are loaded by a child-first classloader (e.g., Moderne CLI's RecipeClassLoader),
     * the {@code adaptTo} class parameter may be loaded by a different classloader than this visitor's
     * class. Since Java class identity includes the classloader, {@code adaptTo.isAssignableFrom(getClass())}
     * returns false even when the classes have the same name and this visitor IS a CobolPreprocessorVisitor.
     * <p>
     * This override checks class assignability by name to support cross-classloader scenarios.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <R extends Tree, V extends TreeVisitor<R, P>> V adapt(Class<? extends V> adaptTo) {
        if (isAssignableByName(getClass(), adaptTo)) {
            return (V) this;
        }
        return super.adapt(adaptTo);
    }

    /**
     * Check if {@code fromClass} is assignable to {@code toClass} by comparing class names
     * up the inheritance hierarchy. This is used instead of {@code Class.isAssignableFrom()}
     * to handle cross-classloader scenarios where the same class loaded by different
     * classloaders would otherwise be considered incompatible.
     */
    private static boolean isAssignableByName(Class<?> fromClass, Class<?> toClass) {
        String targetName = toClass.getName();
        Class<?> current = fromClass;
        while (current != null) {
            if (current.getName().equals(targetName)) {
                return true;
            }
            current = current.getSuperclass();
        }
        return false;
    }

    public CobolPreprocessor visitCharData(CobolPreprocessor.CharData charData, P p) {
        CobolPreprocessor.CharData c = charData;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.CHAR_DATA_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        return c.withCobols(ListUtils.map(c.getCobols(), it -> (CobolPreprocessor.CharDataLine) visit(it, p)));
    }

    public CobolPreprocessor visitCharDataLine(CobolPreprocessor.CharDataLine charDataLine, P p) {
        CobolPreprocessor.CharDataLine c = charDataLine;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.CHAR_DATA_LINE_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        return c.withWords(ListUtils.map(c.getWords(), it -> visit(it, p)));
    }

    public CobolPreprocessor visitCharDataSql(CobolPreprocessor.CharDataSql charDataSql, P p) {
        CobolPreprocessor.CharDataSql c = charDataSql;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.CHAR_DATA_SQL_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        return c.withCobols(ListUtils.map(c.getCobols(), it -> visit(it, p)));
    }

    public CobolPreprocessor visitCommentEntry(CobolPreprocessor.CommentEntry commentEntry, P p) {
        CobolPreprocessor.CommentEntry c = commentEntry;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.COMMENT_ENTRY_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        return c.withComments(ListUtils.map(c.getComments(), it -> (CobolPreprocessor.Word) visit(it, p)));
    }

    public CobolPreprocessor visitCompilationUnit(CobolPreprocessor.CompilationUnit compilationUnit, P p) {
        CobolPreprocessor.CompilationUnit c = compilationUnit;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.PREPROCESSOR_COMPILATION_UNIT_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withCobols(ListUtils.map(c.getCobols(), it -> visit(it, p)));
        return c.withEof((CobolPreprocessor.Word) visit(c.getEof(), p));
    }

    public CobolPreprocessor visitCompilerOption(CobolPreprocessor.CompilerOption compilerOption, P p) {
        CobolPreprocessor.CompilerOption c = compilerOption;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.COMPILER_OPTION_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        return c.withCobols(ListUtils.map(c.getCobols(), it -> visit(it, p)));
    }

    public CobolPreprocessor visitCompilerOptions(CobolPreprocessor.CompilerOptions compilerOptions, P p) {
        CobolPreprocessor.CompilerOptions c = compilerOptions;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.COMPILER_OPTIONS_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        return c.withCobols(ListUtils.map(c.getCobols(), it -> visit(it, p)));
    }

    public CobolPreprocessor visitCompilerXOpts(CobolPreprocessor.CompilerXOpts compilerXOpts, P p) {
        CobolPreprocessor.CompilerXOpts c = compilerXOpts;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.COMPILER_XOPTS_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withWord((CobolPreprocessor.Word) visit(c.getWord(), p));
        c = c.withLeftParen((CobolPreprocessor.Word) visit(c.getLeftParen(), p));
        c = c.withCompilerOptions(ListUtils.map(c.getCompilerOptions(), it -> visit(it, p)));
        return c.withRightParen((CobolPreprocessor.Word) visit(c.getRightParen(), p));
    }

    public CobolPreprocessor visitCopybook(CobolPreprocessor.Copybook copybook, P p) {
        CobolPreprocessor.Copybook c = copybook;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.COPY_BOOK_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withLst(ListUtils.map(c.getLst(), it -> visit(it, p)));
        return c.withEof((CobolPreprocessor.Word) visit(c.getEof(), p));
    }

    public CobolPreprocessor visitCopySource(CobolPreprocessor.CopySource copySource, P p) {
        CobolPreprocessor.CopySource c = copySource;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.COPY_SOURCE_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withName((CobolPreprocessor.Word) visit(c.getName(), p));
        c = c.withWord((CobolPreprocessor.Word) visit(c.getWord(), p));
        return c.withCopyLibrary((CobolPreprocessor.Word) visit(c.getCopyLibrary(), p));
    }

    public CobolPreprocessor visitCopyStatement(CobolPreprocessor.CopyStatement copyStatement, P p) {
        CobolPreprocessor.CopyStatement c = copyStatement;
        c = c.withPrefix(visitSpace(c.getPrefix(), Space.Location.COPY_STATEMENT_PREFIX, p));
        c = c.withMarkers(visitMarkers(c.getMarkers(), p));
        c = c.withWord((CobolPreprocessor.Word) visit(c.getWord(), p));
        c = c.withCopySource((CobolPreprocessor.CopySource) visit(c.getCopySource(), p));
        c = c.withCobols(ListUtils.map(c.getCobols(), it -> visit(it, p)));
        c = c.withDot((CobolPreprocessor.Word) visit(c.getDot(), p));
        return c.withCopybook((CobolPreprocessor.Copybook) visit(c.getCopybook(), p));
    }

    public CobolPreprocessor visitDirectoryPhrase(CobolPreprocessor.DirectoryPhrase directoryPhrase, P p) {
        CobolPreprocessor.DirectoryPhrase d = directoryPhrase;
        d = d.withPrefix(visitSpace(d.getPrefix(), Space.Location.DIRECTORY_PHRASE_PREFIX, p));
        d = d.withMarkers(visitMarkers(d.getMarkers(), p));
        d = d.withWord((CobolPreprocessor.Word) visit(d.getWord(), p));
        return d.withName((CobolPreprocessor.Word) visit(d.getName(), p));
    }

    public CobolPreprocessor visitEjectStatement(CobolPreprocessor.EjectStatement ejectStatement, P p) {
        CobolPreprocessor.EjectStatement e = ejectStatement;
        e = e.withPrefix(visitSpace(e.getPrefix(), Space.Location.EJECT_STATEMENT_PREFIX, p));
        e = e.withMarkers(visitMarkers(e.getMarkers(), p));
        e = e.withWord((CobolPreprocessor.Word) visit(e.getWord(), p));
        return e.withDot((CobolPreprocessor.Word) visit(e.getDot(), p));
    }

    public CobolPreprocessor visitExecStatement(CobolPreprocessor.ExecStatement execStatement, P p) {
        CobolPreprocessor.ExecStatement e = execStatement;
        e = e.withPrefix(visitSpace(e.getPrefix(), Space.Location.EXEC_STATEMENT_PREFIX, p));
        e = e.withMarkers(visitMarkers(e.getMarkers(), p));
        e = e.withWords(ListUtils.map(e.getWords(), it -> (CobolPreprocessor.Word) visit(it, p)));
        e = e.withCobol(visit(e.getCobol(), p));
        e = e.withEndExec((CobolPreprocessor.Word) visit(e.getEndExec(), p));
        return e.withDot((CobolPreprocessor.Word) visit(e.getDot(), p));
    }

    public CobolPreprocessor visitExecSqlIncludeStatement(CobolPreprocessor.ExecSqlIncludeStatement execSqlIncludeStatement, P p) {
        CobolPreprocessor.ExecSqlIncludeStatement e = execSqlIncludeStatement;
        e = e.withPrefix(visitSpace(e.getPrefix(), Space.Location.EXEC_SQL_INCLUDE_STATEMENT_PREFIX, p));
        e = e.withMarkers(visitMarkers(e.getMarkers(), p));
        e = e.withWords(ListUtils.map(e.getWords(), it -> (CobolPreprocessor.Word) visit(it, p)));
        e = e.withCopySource((CobolPreprocessor.Word) visit(e.getCopySource(), p));
        e = e.withEndExec((CobolPreprocessor.Word) visit(e.getEndExec(), p));
        e = e.withDot((CobolPreprocessor.Word) visit(e.getDot(), p));
        return e.withCopybook((CobolPreprocessor.Copybook) visit(e.getCopybook(), p));
    }

    public CobolPreprocessor visitFamilyPhrase(CobolPreprocessor.FamilyPhrase familyPhrase, P p) {
        CobolPreprocessor.FamilyPhrase f = familyPhrase;
        f = f.withPrefix(visitSpace(f.getPrefix(), Space.Location.FAMILY_PHRASE_PREFIX, p));
        f = f.withMarkers(visitMarkers(f.getMarkers(), p));
        f = f.withWord((CobolPreprocessor.Word) visit(f.getWord(), p));
        return f.withName((CobolPreprocessor.Word) visit(f.getName(), p));
    }

    public CobolPreprocessor visitPseudoText(CobolPreprocessor.PseudoText pseudoText, P p) {
        CobolPreprocessor.PseudoText pp = pseudoText;
        pp = pp.withPrefix(visitSpace(pp.getPrefix(), Space.Location.PSEUDO_TEXT_PREFIX, p));
        pp = pp.withMarkers(visitMarkers(pp.getMarkers(), p));
        pp = pp.withDoubleEqualOpen((CobolPreprocessor.Word) visit(pp.getDoubleEqualOpen(), p));
        pp = pp.withCharData((CobolPreprocessor.CharData) visit(pp.getCharData(), p));
        return pp.withDoubleEqualClose((CobolPreprocessor.Word) visit(pp.getDoubleEqualClose(), p));
    }

    public CobolPreprocessor visitReplaceArea(CobolPreprocessor.ReplaceArea replaceArea, P p) {
        CobolPreprocessor.ReplaceArea r = replaceArea;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REPLACE_AREA_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withReplaceByStatement((CobolPreprocessor.ReplaceByStatement) visit(r.getReplaceByStatement(), p));
        r = r.withCobols(ListUtils.map(r.getCobols(), it -> visit(it, p)));
        return r.withReplaceOffStatement((CobolPreprocessor.ReplaceOffStatement) visit(r.getReplaceOffStatement(), p));
    }

    public CobolPreprocessor visitReplaceByStatement(CobolPreprocessor.ReplaceByStatement replaceByStatement, P p) {
        CobolPreprocessor.ReplaceByStatement r = replaceByStatement;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REPLACE_BY_STATEMENT_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withWord((CobolPreprocessor.Word) visit(r.getWord(), p));
        r = r.withClauses(ListUtils.map(r.getClauses(), it -> (CobolPreprocessor.ReplaceClause) visit(it, p)));
        return r.withDot((CobolPreprocessor.Word) visit(r.getDot(), p));
    }

    public CobolPreprocessor visitReplaceClause(CobolPreprocessor.ReplaceClause replaceClause, P p) {
        CobolPreprocessor.ReplaceClause r = replaceClause;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REPLACE_CLAUSE_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withReplaceable(visit(r.getReplaceable(), p));
        r = r.withBy((CobolPreprocessor.Word) visit(r.getBy(), p));
        r = r.withReplacement(visit(r.getReplacement(), p));
        r = r.withSubscript(ListUtils.map(r.getSubscript(), it -> visit(it, p)));
        r = r.withDirectoryPhrases(ListUtils.map(r.getDirectoryPhrases(), it -> (CobolPreprocessor.DirectoryPhrase) visit(it, p)));
        return r.withFamilyPhrase((CobolPreprocessor.FamilyPhrase) visit(r.getFamilyPhrase(), p));
    }

    public CobolPreprocessor visitReplaceOffStatement(CobolPreprocessor.ReplaceOffStatement replaceOffStatement, P p) {
        CobolPreprocessor.ReplaceOffStatement r = replaceOffStatement;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REPLACE_OFF_STATEMENT_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withWords(ListUtils.map(r.getWords(), it -> (CobolPreprocessor.Word) visit(it, p)));
        return r.withDot((CobolPreprocessor.Word) visit(r.getDot(), p));
    }

    public CobolPreprocessor visitReplacingPhrase(CobolPreprocessor.ReplacingPhrase replacingPhrase, P p) {
        CobolPreprocessor.ReplacingPhrase r = replacingPhrase;
        r = r.withPrefix(visitSpace(r.getPrefix(), Space.Location.REPLACING_PHRASE_PREFIX, p));
        r = r.withMarkers(visitMarkers(r.getMarkers(), p));
        r = r.withWord((CobolPreprocessor.Word) visit(r.getWord(), p));
        return r.withClauses(ListUtils.map(r.getClauses(), it -> (CobolPreprocessor.ReplaceClause) visit(it, p)));
    }

    public CobolPreprocessor visitSkipStatement(CobolPreprocessor.SkipStatement skipStatement, P p) {
        CobolPreprocessor.SkipStatement s = skipStatement;
        s = s.withPrefix(visitSpace(s.getPrefix(), Space.Location.SKIP_STATEMENT_PREFIX, p));
        s = s.withMarkers(visitMarkers(s.getMarkers(), p));
        s = s.withWord((CobolPreprocessor.Word) visit(s.getWord(), p));
        return s.withDot((CobolPreprocessor.Word) visit(s.getDot(), p));
    }

    public CobolPreprocessor visitTitleStatement(CobolPreprocessor.TitleStatement titleStatement, P p) {
        CobolPreprocessor.TitleStatement t = titleStatement;
        t = t.withPrefix(visitSpace(t.getPrefix(), Space.Location.TITLE_STATEMENT_PREFIX, p));
        t = t.withMarkers(visitMarkers(t.getMarkers(), p));
        t = t.withFirst((CobolPreprocessor.Word) visit(t.getFirst(), p));
        t = t.withSecond((CobolPreprocessor.Word) visit(t.getSecond(), p));
        return t.withDot((CobolPreprocessor.Word) visit(t.getDot(), p));
    }

    public CobolPreprocessor visitWord(CobolPreprocessor.Word word, P p) {
        CobolPreprocessor.Word w = word;
        w = w.withPrefix(visitSpace(w.getPrefix(), Space.Location.WORD_PREFIX, p));
        w = w.withMarkers(visitMarkers(w.getMarkers(), p));

        // The COBOL visitor is re-entered here for a word it never descended to, so it is given a
        // cursor for it. Without one, everything the word's own visitWord asks the cursor is answered
        // about the word that carries this block: a visitor reading what an EXEC statement is
        // attached to heard the same block once for every word inside it.
        Cursor parent = cobolVisitor.getCursor();
        cobolVisitor.setCursor(new Cursor(parent, w.getCobolWord()));
        try {
            return w.withCobolWord((Cobol.Word) cobolVisitor.visitWord(w.getCobolWord(), p));
        } finally {
            cobolVisitor.setCursor(parent);
        }
    }

    public Space visitSpace(Space space, Space.Location location, P p) {
        return space;
    }
}
