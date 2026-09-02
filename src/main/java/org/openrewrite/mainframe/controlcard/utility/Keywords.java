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
package org.openrewrite.mainframe.controlcard.utility;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * The words a Db2 utility control card is written out of.
 * <p>
 * A control statement has no punctuation to group it by — operands are separated by blanks, an
 * operand's value runs to the next keyword, and a statement runs to the next statement. So the
 * vocabulary is what says where one operand ends and the next begins, and it is written down here
 * rather than guessed at from the layout: the same deck reads the same whether it was written one
 * keyword to a card or all of it on one.
 * <p>
 * Both dialects are in one vocabulary. The unload product and the base utility share the
 * {@code UNLOAD} verb and almost nothing else under it, so keeping the two sets apart would mean
 * knowing which dialect a deck is in before reading it, and the dialect is only known once it is
 * read. Nothing is lost by the union: {@code SHRLEVEL} is never written in the product's dialect and
 * {@code DB2} never in the base utility's.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Keywords {

    /**
     * The verbs that open a utility control statement.
     */
    private static final Set<String> STATEMENTS = upper(
            "UNLOAD", "GLOBAL", "TEMPLATE", "LISTDEF", "LISTDEFTBV", "PROCESS_OPTIONS", "OPTIONS",
            "LOAD", "COPY", "COPYTOCOPY", "MERGECOPY", "REORG", "RUNSTATS", "CHECK", "QUIESCE",
            "REBUILD", "RECOVER", "REPAIR", "MODIFY", "STOSPACE", "DIAGNOSE");

    private static final Map<String, Set<String>> OPERANDS = new HashMap<>();
    private static final Map<String, Set<String>> BLOCKS = new HashMap<>();

    static {
        // The unload product's UNLOAD block, and the base utility's UNLOAD statement, in one set.
        block("UNLOAD",
                asList("SELECT", "FROM", "OPTIONS"),
                asList("TABLESPACE", "TABLE", "LIST", "CLONE", "UNLDDN", "PUNCHDDN", "INTERNAL_FORMAT",
                        "UNLMAXROWS", "UNLFREQROWS", "PART", "DB2", "EXECUTE", "COPYDDN", "DDLDDN",
                        "SIZE", "OBID_REPORT", "IC_MAXPARTITIONS", "LOCK", "QUIESCE", "INDEXSCAN",
                        "PARALLELISM", "SORT", "QSAM-BUFFERS", "VSAM-BUFFERS", "MAXPART", "SQLID",
                        "APPLCOMPAT", "TAPEUNIT", "TAPEREPORT", "PROGRESS_MSG_FREQ", "DATE_DELIM",
                        "NULL_DATE_DELIM", "ONE_GDS_TEMPLATE", "CHECK_INTEGRITY", "MAXERR",
                        // The base utility's own, which its one statement carries where the product
                        // spreads them over UNLOAD, SELECT and FORMAT.
                        "SHRLEVEL", "ISOLATION", "REGISTER", "FROMCOPY", "FROMCOPYDDN", "FROMVOLUME",
                        "FROMSEQNO", "PARALLEL", "EBCDIC", "ASCII", "UNICODE", "CCSID", "FORMAT",
                        "DELIMITED", "CHARDEL", "COLDEL", "DECPT", "NOSUBS", "NOPAD", "SPANNED",
                        "HEADER", "LIMIT", "SAMPLE", "NOSYSREC", "FLOAT"));

        // The product's SELECT block: SQL first, then where the rows go and how they are laid out.
        block("SELECT",
                asList("FORMAT", "OPTIONS"),
                asList("INTO", "FROM", "WHERE", "ORDER", "GROUP", "HAVING", "ORIGINOBID", "PART",
                        "SQLPART", "WITH", "OUTDDN", "MAX_EXPECTED_ROWS", "OUTMAXROWS", "OUTFREQROWS",
                        "OUTEXIT", "EBCDIC", "ASCII", "UNICODE", "ASIS", "CCSID", "LOADDDN", "LOADOPT",
                        "LOADDDL"));

        // FROM TABLE, the base utility's answer to a SELECT: a table, a field list and a condition.
        block("FROM",
                Collections.emptyList(),
                asList("WHEN", "HEADER", "LIMIT", "SAMPLE", "PART"));

        // INTO TABLE, the same shape on a LOAD.
        block("INTO",
                Collections.emptyList(),
                asList("WHEN", "PART", "RESUME", "REPLACE", "IGNOREFIELDS", "PRESORTED", "NUMRECS"));

        block("FORMAT",
                Collections.emptyList(),
                asList("LIKE", "SEP", "DELIM", "NULLVAL", "TITLE", "ALTERNATE", "COL", "TYPE",
                        "LOADDDN", "LOADOPT", "LOADDDL"));

        block("OPTIONS",
                Collections.emptyList(),
                asList("NULL", "DATE", "DATEDELIM", "TIME", "TIMEDELIM", "TIMESTAMP", "PIC", "LOADOPT",
                        "LOADDDL", "LENGTHBYTE", "LENGTH", "NULLID", "NULLPOS", "LOADINDDN", "FLOAT",
                        "UNLROWSET", "NULLPAD", "AUTOTAG", "IFERROR", "TRIM", "PADDING", "REFORMAT",
                        "TEMPLATESET", "HIDDEN", "SPANNED", "XMLSET", "OPTIMIZATION_HINT",
                        "QUERY_ACCELERATION", "CHARACTER_LOSS", "NUMERIC_LOSS", "ZONED_DEC_SIGN",
                        "UNICODE_EXPANSION_RATIO", "CONVERSION_TRUNCATION_ALLOWED",
                        // The base utility's common OPTIONS statement, which is where the two meet.
                        "TEMPLATEDD", "LISTDEFDD", "PREVIEW", "EVENT", "FILSZ", "KEY", "OFF"));

        block("GLOBAL",
                Collections.singletonList("OPTIONS"),
                asList("DB2", "LOCK", "QUIESCE", "QUIESCECAT", "INDEXSCAN", "EXECUTE", "PROCMSG",
                        "SELMSG", "PARALLELISM", "SORT", "QSAM-BUFFERS", "VSAM-BUFFERS", "MAXPART",
                        "TAPEUNIT", "TAPEREPORT", "SQLID", "APPLCOMPAT", "PROGRESS_MSG_FREQ",
                        "HANDLE_RECORD_ID", "DATE_DELIM", "NULL_DATE_DELIM", "ONE_GDS_TEMPLATE",
                        "CHECK_INTEGRITY", "INTO_RULES", "LOWMEM", "MAXSORT", "SORTCLASS", "WRKSPACE",
                        "SRTVSMIN", "SRTVSMAX", "LIMUNIT", "MAXTUNIT", "MAXUNIT", "WRKMXPR",
                        "WRKUNTSW", "WRKTVCNT", "WRKUNIT", "SORTDEVT", "WRKTUNIT", "VBUFSIZE",
                        "PGDFIN", "SQLACCES", "SQLACCESS", "DFSIGDCB", "ULDEGREE", "CONCURRENT_ACCESS",
                        "SORTUTIL", "UNICODE_EXPANSION_RATIO", "CONVERSION_TRUNCATION_ALLOWED",
                        "MAXERR", "UNLOAD_BUFFERS", "OPTIMIZE_PART_RANGE", "ZIIP", "ZIIPMXPL",
                        "ZIIPMXPU", "UNLLDTBB", "ULRLDRC", "DISPLAY_PARMLIB", "CHECK_MEMORY",
                        "SQL_LOCK_TIMEOUT", "USE_RTS_FOR_ESTIMATION", "SEQ_IO_METHOD", "SRTNBVRE"));

        block("PROCESS_OPTIONS",
                Collections.emptyList(),
                asList("LISTDEFDD", "TEMPLATEDD", "PATH_VARIABLES", "OFF"));

        block("TEMPLATE",
                Collections.emptyList(),
                asList("DSN", "PATH", "SUBSYS", "LRECL", "RECFM", "UNIT", "MODELDCB", "BUFNO",
                        "DATACLAS", "MGMTCLAS", "STORCLAS", "RETPD", "EXPDL", "VOLUMES", "VOLCNT",
                        "UNCNT", "GDGLIMIT", "LIMIT", "TIME", "DISP", "SPACE", "PCTPRIME", "MAXPRIME",
                        "NBRSECND", "DIR", "DSNTYPE", "KEYLABEL", "STACK", "TRTCH", "FILEDATA",
                        "PATHOPTS", "PATHMODE", "PATHDISP"));

        block("LISTDEF",
                Collections.emptyList(),
                asList("INCLUDE", "EXCLUDE", "TABLESPACES", "INDEXSPACES", "COPY", "LIST", "DATABASE",
                        "TABLESPACE", "INDEXSPACE", "TABLE", "INDEX", "PARTLEVEL", "CLONED", "RI"));
        BLOCKS.put("LISTDEFTBV", BLOCKS.get("LISTDEF"));
        OPERANDS.put("LISTDEFTBV", OPERANDS.get("LISTDEF"));

        block("LOAD",
                Collections.singletonList("INTO"),
                asList("DATA", "INDDN", "INCURSOR", "RESUME", "REPLACE", "SHRLEVEL", "LOG", "ENFORCE",
                        "SORTKEYS", "ERRDDN", "MAPDDN", "DISCARDDN", "DISCARDS", "WORKDDN", "SORTDEVT",
                        "SORTNUM", "COPYDDN", "RECOVERYDDN", "STATISTICS", "FORMAT", "DELIMITED",
                        "EBCDIC", "ASCII", "UNICODE", "CCSID", "NUMRECS", "PRESORTED",
                        "KEEPDICTIONARY", "REUSE", "NOCOPYPEND", "INDEXDEFER", "PREFORMAT",
                        "FLOAT", "NOSUBS", "IDENTITYOVERRIDE"));

        block("COPY",
                Collections.emptyList(),
                asList("TABLESPACE", "INDEXSPACE", "LIST", "COPYDDN", "RECOVERYDDN", "SHRLEVEL",
                        "FULL", "DSNUM", "FILTERDDN", "CHECKPAGE", "PARALLEL", "CONCURRENT",
                        "SYSTEMPAGES", "SCOPE", "FLASHCOPY", "CLONE"));

        block("REORG",
                Collections.emptyList(),
                asList("TABLESPACE", "INDEX", "INDEXSPACE", "LIST", "PART", "SHRLEVEL", "SORTDATA",
                        "SORTKEYS", "COPYDDN", "RECOVERYDDN", "UNLDDN", "PUNCHDDN", "STATISTICS",
                        "KEEPDICTIONARY", "LOG", "DEADLINE", "MAXRO", "LONGLOG", "DELAY", "DRAIN",
                        "TIMEOUT", "REUSE", "UNLOAD", "DISCARD", "DISCARDDN", "FROM", "WHEN",
                        "ROWFORMAT", "NOSYSREC", "SORTDEVT", "SORTNUM", "CLONE", "SCOPE",
                        "AUX", "PREFORMAT"));

        block("RUNSTATS",
                Collections.emptyList(),
                asList("TABLESPACE", "INDEX", "INDEXSPACE", "LIST", "TABLE", "COLUMN", "SAMPLE",
                        "SHRLEVEL", "REPORT", "UPDATE", "HISTORY", "KEYCARD", "FREQVAL", "COUNT",
                        "NUMCOLS", "SORTDEVT", "SORTNUM", "FORCEROLLUP", "COLGROUP", "TABLESPACES"));

        block("QUIESCE",
                Collections.emptyList(),
                asList("TABLESPACE", "LIST", "TABLESPACESET", "WRITE", "RANGE", "CLONE"));

        block("CHECK",
                Collections.emptyList(),
                asList("DATA", "INDEX", "LOB", "TABLESPACE", "INDEXSPACE", "LIST", "PART", "SHRLEVEL",
                        "SCOPE", "ERRDDN", "WORKDDN", "SORTDEVT", "SORTNUM", "EXCEPTIONS", "AUXERROR",
                        "FOR", "DELETE", "YES", "CLONE"));

        block("MERGECOPY",
                Collections.emptyList(),
                asList("TABLESPACE", "LIST", "DSNUM", "NEWCOPY", "COPYDDN", "RECOVERYDDN", "WORKDDN"));

        block("MODIFY",
                Collections.emptyList(),
                asList("RECOVERY", "STATISTICS", "TABLESPACE", "INDEXSPACE", "LIST", "DSNUM", "DELETE",
                        "AGE", "DATE", "RETAIN"));

        block("REBUILD",
                Collections.emptyList(),
                asList("INDEX", "INDEXSPACE", "LIST", "PART", "SHRLEVEL", "SORTDEVT", "SORTNUM",
                        "WORKDDN", "STATISTICS", "REUSE", "SCOPE", "CLONE"));

        block("RECOVER",
                Collections.emptyList(),
                asList("TABLESPACE", "INDEX", "INDEXSPACE", "LIST", "DSNUM", "TOCOPY", "TORBA",
                        "TOLOGPOINT", "TOLASTCOPY", "TOLASTFULLCOPY", "LOGONLY", "ERROR", "PARALLEL",
                        "CURRENTCOPYONLY", "RESTOREBEFORE", "CLONE"));

        block("REPAIR",
                Collections.emptyList(),
                asList("OBJECT", "LOCATE", "TABLESPACE", "INDEX", "INDEXSPACE", "DBD", "SET", "PAGE",
                        "DSNUM", "VERIFY", "REPLACE", "DELETE", "DUMP", "LOG", "CLONE"));

        block("COPYTOCOPY",
                Collections.emptyList(),
                asList("TABLESPACE", "INDEXSPACE", "LIST", "DSNUM", "FROMLASTCOPY", "FROMLASTFULLCOPY",
                        "FROMLASTINCRCOPY", "FROMCOPY", "FROMCOPYDDN", "FROMSEQNO", "FROMVOLUME",
                        "COPYDDN", "RECOVERYDDN", "CLONE"));

        block("STOSPACE", Collections.emptyList(), asList("STOGROUP", "LIST"));
        block("DIAGNOSE", Collections.emptyList(), asList("TYPE", "DISPLAY", "ALLDUMPS", "NODUMPS",
                "DATABASE", "TABLESPACE", "INDEX", "MEPL", "AVAILABLE", "WAIT", "ABEND", "END"));
    }

    /**
     * The keywords the site parmlib answers when a deck leaves them out, so that what the deck says
     * and what the job does are not the same question. Each one is a documented {@code VUUnnn}
     * parameter of the {@code INZUTIL} member the {@code INFPLIB} DD points at.
     */
    private static final List<String> INHERITED = Collections.unmodifiableList(asList(
            "FORMAT", "DB2", "QUIESCE", "LOCK", "NULLPOS", "DATE", "TIME", "TIMESTAMP", "HIDDEN",
            "PARALLELISM", "MAXERR"));

    private static void block(String verb, List<String> blocks, List<String> operands) {
        BLOCKS.put(verb, upper(blocks.toArray(new String[0])));
        OPERANDS.put(verb, upper(operands.toArray(new String[0])));
    }

    private static Set<String> upper(String... words) {
        Set<String> set = new HashSet<>(Arrays.asList(words));
        return Collections.unmodifiableSet(set);
    }

    /**
     * Whether a word opens a control statement of its own.
     */
    public static boolean opensStatement(String word) {
        return STATEMENTS.contains(word.toUpperCase(Locale.ROOT));
    }

    /**
     * Whether a word is a keyword of the block opened by {@code verb}, and so ends the operand before
     * it.
     */
    public static boolean isOperandOf(String verb, String word) {
        Set<String> operands = OPERANDS.get(verb.toUpperCase(Locale.ROOT));
        return operands != null && operands.contains(word.toUpperCase(Locale.ROOT));
    }

    /**
     * Whether a word opens a block written inside the block opened by {@code verb}.
     */
    public static boolean opensBlockIn(String verb, String word) {
        Set<String> blocks = BLOCKS.get(verb.toUpperCase(Locale.ROOT));
        return blocks != null && blocks.contains(word.toUpperCase(Locale.ROOT));
    }

    public static List<String> inherited() {
        return INHERITED;
    }
}
