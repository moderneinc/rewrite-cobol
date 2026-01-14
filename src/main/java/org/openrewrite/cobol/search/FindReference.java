/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.cobol.search;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.*;
import org.openrewrite.cobol.CobolPreprocessorVisitor;
import org.openrewrite.cobol.NameVisitor;
import org.openrewrite.cobol.tree.Cobol;
import org.openrewrite.cobol.tree.CobolPreprocessor;
import org.openrewrite.jcl.JclIsoVisitor;
import org.openrewrite.jcl.tree.Jcl;
import org.openrewrite.marker.SearchResult;

import java.util.regex.Pattern;

@EqualsAndHashCode(callSuper = false)
@Value
public class FindReference extends Recipe {

    @Option(displayName = "Term to search for",
            description = "A word or regex pattern to find. By default the search term is case insensitive.",
            example = "CM102M or cm1.*")
    String searchTerm;

    @Nullable
    @Option(displayName = "Only match exact word",
            description = "Search for a word based on an exact match of the search term.",
            example = "true")
    Boolean exactMatch;

    String displayName = "Find matching identifiers in COBOL, copybooks, and JCL";

    String description = "Finds an identifier by an exact match or regex pattern in COBOL, copybooks, and/or JCL.";

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new TreeVisitor<Tree, ExecutionContext>() {
            private final CobolReference cobolReference = new CobolReference();
            private final CopybookReference copybookReference = new CopybookReference();
            private final JclReference jclReference = new JclReference();

            @Nullable
            private final Pattern pattern = Boolean.TRUE.equals(exactMatch) ? null : Pattern.compile(searchTerm.toLowerCase());

            @Override
            public boolean isAcceptable(SourceFile sourceFile, ExecutionContext ctx) {
                return cobolReference.isAcceptable(sourceFile, ctx) ||
                       copybookReference.isAcceptable(sourceFile, ctx) ||
                       jclReference.isAcceptable(sourceFile, ctx);
            }

            @Override
            public @Nullable Tree visit(@Nullable Tree tree, ExecutionContext ctx) {
				if (tree instanceof Cobol) {
					return cobolReference.visit(tree, ctx);
				}
				if (tree instanceof CobolPreprocessor.Copybook) {
					return copybookReference.visit(tree, ctx);
				}
				if (tree instanceof Jcl.CompilationUnit) {
					return jclReference.visit(tree, ctx);
				}
				return super.visit(tree, ctx);
			}

            class CobolReference extends NameVisitor<ExecutionContext> {
                @Override
                public Cobol.Word visitWord(Cobol.Word word, ExecutionContext ctx) {
                    if (matches(word.getWord())) {
                        return SearchResult.found(word);
                    }
                    return super.visitWord(word, ctx);
                }
            }

            class CopybookReference extends CobolPreprocessorVisitor<ExecutionContext> {
                @Override
                public CobolPreprocessor visitWord(CobolPreprocessor.Word word, ExecutionContext ctx) {
                    if (matches(word.getCobolWord().getWord())) {
                        return SearchResult.found(word);
                    }
                    return super.visitWord(word, ctx);
                }
            }

            class JclReference extends JclIsoVisitor<ExecutionContext> {
                // TODO: FIXME.
            }

            private boolean matches(String word) {
                return pattern != null && pattern.matcher(word.toLowerCase()).matches() || pattern == null && word.equals(searchTerm);
            }
        };
    }
}
