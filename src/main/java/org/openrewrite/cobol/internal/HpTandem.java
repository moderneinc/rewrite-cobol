/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.cobol.internal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;

public final class HpTandem implements CobolDialect {

    private static final HpTandem INSTANCE = new HpTandem();

    public static HpTandem getInstance() {
        return INSTANCE;
    }

    private final Set<String> separators;
    private final Set<Character> commentIndicators;
    private final Columns columns;

    private HpTandem() {
        // Currently unknown.
        this.separators = emptySet();
        this.commentIndicators = new HashSet<>(singletonList('*'));
        this.columns = Columns.IBM_ANSI_85;
    }

    @Override
    public Collection<String> getSeparators() {
        return separators;
    }

    @Override
    public Collection<Character> getCommentIndicators() {
        return commentIndicators;
    }

    @Override
    public Columns getColumns() {
        return columns;
    }
}
