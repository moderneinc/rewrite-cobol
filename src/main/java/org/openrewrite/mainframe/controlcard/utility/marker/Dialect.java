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
package org.openrewrite.mainframe.controlcard.utility.marker;

import lombok.Value;
import lombok.With;
import org.openrewrite.marker.Marker;
import org.openrewrite.mainframe.controlcard.utility.tree.Statement;
import org.openrewrite.mainframe.controlcard.utility.tree.Utility;
import org.openrewrite.marker.Markers;

import java.util.List;
import java.util.UUID;

/**
 * Which utility's language a deck is written in, on the deck.
 * <p>
 * The program name on the {@code EXEC} card does not settle it and must not be used to: a shop may
 * define {@code DSNUTILB} as an alias for the unload product's compatibility entry point, in which
 * case a step that reads as the base utility runs the product, and the product reads three vendors'
 * dialects through the one program name of its own. What does settle it is the deck. After
 * {@code UNLOAD TABLESPACE}, a {@code FROM TABLE} is the base utility and a {@code SELECT} with an
 * {@code OUTDDN} is the product.
 */
@With
@Value
public class Dialect implements Marker {
    UUID id;
    Kind kind;

    public enum Kind {
        /**
         * Db2 High Performance Unload's own language: {@code GLOBAL}, {@code UNLOAD} blocks holding
         * {@code SELECT}s, {@code OUTDDN}, {@code DB2}/{@code LOCK}/{@code QUIESCE}.
         */
        HIGH_PERFORMANCE_UNLOAD,

        /**
         * The language the Db2 utilities themselves read: {@code UNLOAD ... FROM TABLE} with
         * {@code SHRLEVEL}, and {@code LOAD}, {@code COPY}, {@code REORG} and {@code RUNSTATS}.
         */
        BASE_UTILITY
    }

    /**
     * What the statements of a deck say it is. The product's own blocks and the base utility's own
     * keywords each decide it outright; a deck that is only {@code LOAD}, {@code COPY},
     * {@code REORG} or {@code RUNSTATS} is the base utility, since the product unloads and nothing
     * else.
     */
    public static Kind of(List<Statement> statements) {
        for (Statement statement : statements) {
            if (!(statement instanceof Utility.Block)) {
                continue;
            }
            Utility.Block block = (Utility.Block) statement;
            if (block.isVerb("GLOBAL") || block.isVerb("PROCESS_OPTIONS") || block.isVerb("LISTDEFTBV")) {
                return Kind.HIGH_PERFORMANCE_UNLOAD;
            }
            if (block.isVerb("UNLOAD")) {
                if (!block.getBlocks("SELECT").isEmpty() || codesAny(block, PRODUCT_ONLY)) {
                    return Kind.HIGH_PERFORMANCE_UNLOAD;
                }
                if (!block.getBlocks("FROM").isEmpty() || codesAny(block, BASE_ONLY)) {
                    return Kind.BASE_UTILITY;
                }
            }
        }
        return Kind.BASE_UTILITY;
    }

    /**
     * The dialect of the deck a tree belongs to.
     */
    public static Kind of(Markers markers) {
        for (Marker marker : markers.getMarkers()) {
            if (marker instanceof Dialect) {
                return ((Dialect) marker).getKind();
            }
        }
        return Kind.BASE_UTILITY;
    }

    private static final String[] PRODUCT_ONLY = {"OUTDDN", "DB2", "LOCK", "QUIESCE", "INDEXSCAN",
            "COPYDDN", "PARALLELISM", "UNLMAXROWS", "UNLFREQROWS", "EXECUTE", "INTERNAL_FORMAT"};

    private static final String[] BASE_ONLY = {"SHRLEVEL", "PUNCHDDN", "FROMCOPY", "FROMCOPYDDN",
            "FROMVOLUME", "FROMSEQNO", "PARALLEL", "LIMIT", "SAMPLE", "HEADER", "NOPAD", "NOSUBS"};

    private static boolean codesAny(Utility.Block block, String[] keywords) {
        for (String keyword : keywords) {
            if (block.getOperand(keyword) != null) {
                return true;
            }
        }
        return false;
    }
}
