/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.controlm.internal;

import org.openrewrite.FileAttributes;
import org.openrewrite.controlm.internal.grammar.ControlMParser;
import org.openrewrite.controlm.internal.grammar.ControlMParserBaseVisitor;
import org.openrewrite.controlm.tree.ControlM;
import org.openrewrite.controlm.tree.Space;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.marker.Markers;

import java.nio.charset.Charset;
import java.nio.file.Path;

import static java.util.Collections.emptyList;
import static org.openrewrite.Tree.randomId;
import static org.openrewrite.controlm.tree.Space.EMPTY;

public class ControlMParserVisitor extends ControlMParserBaseVisitor<ControlM> {

    private final Path path;
    private final @Nullable FileAttributes fileAttributes;
    private final String source;
    private final Charset charset;
    private final boolean charsetBomMarked;

    private int cursor = 0;

    public ControlMParserVisitor(Path path,
                            @Nullable FileAttributes fileAttributes,
                            String source,
                            Charset charset,
                            boolean charsetBomMarked) {
        this.path = path;
        this.fileAttributes = fileAttributes;
        this.source = source;
        this.charset = charset;
        this.charsetBomMarked = charsetBomMarked;
    }

    @Override
    public ControlM.CompilationUnit visitCompilationUnit(ControlMParser.CompilationUnitContext ctx) {
        return new ControlM.CompilationUnit(
                randomId(),
                path,
                fileAttributes,
                EMPTY, // FIXME
                Markers.EMPTY, // FIXME
                charset.name(),
                charsetBomMarked,
                null,
                emptyList(),
                Space.build(source.substring(cursor))
        );
    }
}
