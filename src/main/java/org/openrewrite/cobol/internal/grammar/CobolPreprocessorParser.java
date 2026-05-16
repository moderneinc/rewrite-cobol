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
// Generated from /home/sam/code/moderne/rewrite-cobol/src/main/antlr-cobol/CobolPreprocessor.g4 by ANTLR 4.13.2
package org.openrewrite.cobol.internal.grammar;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class CobolPreprocessorParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		ADATA=1, ADV=2, ALIAS=3, ANSI=4, ANY=5, APOST=6, AR=7, ARITH=8, AUTO=9,
		AWO=10, BIN=11, BLOCK0=12, BUF=13, BUFSIZE=14, BY=15, CBL=16, CBLCARD=17,
		CICS=18, CO=19, COBOL2=20, COBOL3=21, CODEPAGE=22, COMPAT=23, COMPILE=24,
		COPY=25, CP=26, CPP=27, CPSM=28, CS=29, CURR=30, CURRENCY=31, DATA=32,
		DATEPROC=33, DBCS=34, DD=35, DEBUG=36, DECK=37, DIAGTRUNC=38, DLI=39,
		DLL=40, DP=41, DTR=42, DU=43, DUMP=44, DYN=45, DYNAM=46, EDF=47, EJECT=48,
		EJPD=49, EN=50, ENGLISH=51, END_EXEC=52, EPILOG=53, EXCI=54, EXEC=55,
		EXIT=56, EXP=57, EXPORTALL=58, EXTEND=59, FASTSRT=60, FEPI=61, FLAG=62,
		FLAGSTD=63, FSRT=64, FULL=65, GDS=66, GRAPHIC=67, HOOK=68, IN=69, INCLUDE=70,
		INTDATE=71, JA=72, JP=73, KA=74, LANG=75, LANGUAGE=76, LC=77, LEASM=78,
		LENGTH=79, LIB=80, LILIAN=81, LIN=82, LINECOUNT=83, LINKAGE=84, LIST=85,
		LM=86, LONGMIXED=87, LONGUPPER=88, LPARENCHAR=89, LU=90, MAP=91, MARGINS=92,
		MAX=93, MD=94, MDECK=95, MIG=96, MIXED=97, NAME=98, NAT=99, NATIONAL=100,
		NATLANG=101, NN=102, NO=103, NOADATA=104, NOADV=105, NOALIAS=106, NOAWO=107,
		NOBLOCK0=108, NOC=109, NOCBLCARD=110, NOCICS=111, NOCMPR2=112, NOCOMPILE=113,
		NOCPSM=114, NOCURR=115, NOCURRENCY=116, NOD=117, NODATEPROC=118, NODBCS=119,
		NODE=120, NODEBUG=121, NODECK=122, NODIAGTRUNC=123, NODLL=124, NODU=125,
		NODUMP=126, NODP=127, NODTR=128, NODYN=129, NODYNAM=130, NOEDF=131, NOEJPD=132,
		NOEPILOG=133, NOEXIT=134, NOEXP=135, NOEXPORTALL=136, NOF=137, NOFASTSRT=138,
		NOFEPI=139, NOFLAG=140, NOFLAGMIG=141, NOFLAGSTD=142, NOFSRT=143, NOGRAPHIC=144,
		NOHOOK=145, NOLENGTH=146, NOLIB=147, NOLINKAGE=148, NOLIST=149, NOMAP=150,
		NOMD=151, NOMDECK=152, NONAME=153, NONUM=154, NONUMBER=155, NOOBJ=156,
		NOOBJECT=157, NOOFF=158, NOOFFSET=159, NOOPSEQUENCE=160, NOOPT=161, NOOPTIMIZE=162,
		NOOPTIONS=163, NOP=164, NOPFD=165, NOPROLOG=166, NORENT=167, NOS=168,
		NOSEP=169, NOSEPARATE=170, NOSEQ=171, NOSOURCE=172, NOSPIE=173, NOSQL=174,
		NOSQLC=175, NOSQLCCSID=176, NOSSR=177, NOSSRANGE=178, NOSTDTRUNC=179,
		NOSEQUENCE=180, NOTERM=181, NOTERMINAL=182, NOTEST=183, NOTHREAD=184,
		NOTRIG=185, NOVBREF=186, NOWD=187, NOWORD=188, NOX=189, NOXREF=190, NOZWB=191,
		NS=192, NSEQ=193, NSYMBOL=194, NUM=195, NUMBER=196, NUMPROC=197, OBJ=198,
		OBJECT=199, OF=200, OFF=201, OFFSET=202, ON=203, OP=204, OPMARGINS=205,
		OPSEQUENCE=206, OPT=207, OPTFILE=208, OPTIMIZE=209, OPTIONS=210, OUT=211,
		OUTDD=212, PFD=213, PPTDBG=214, PGMN=215, PGMNAME=216, PROCESS=217, PROLOG=218,
		QUOTE=219, RENT=220, REPLACE=221, REPLACING=222, RMODE=223, RPARENCHAR=224,
		SEP=225, SEPARATE=226, SEQ=227, SEQUENCE=228, SHORT=229, SIZE=230, SOURCE=231,
		SP=232, SPACE=233, SPIE=234, SQL=235, SQLC=236, SQLCCSID=237, SQLIMS=238,
		SKIP1=239, SKIP2=240, SKIP3=241, SS=242, SSR=243, SSRANGE=244, STD=245,
		SUPPRESS=246, SYSEIB=247, SZ=248, TERM=249, TERMINAL=250, TEST=251, THREAD=252,
		TITLE=253, TRIG=254, TRUNC=255, UE=256, UPPER=257, VBREF=258, WD=259,
		WORD=260, XMLPARSE=261, XMLSS=262, XOPTS=263, XP=264, XREF=265, YEARWINDOW=266,
		YW=267, ZWB=268, C_CHAR=269, D_CHAR=270, E_CHAR=271, F_CHAR=272, H_CHAR=273,
		I_CHAR=274, M_CHAR=275, N_CHAR=276, Q_CHAR=277, S_CHAR=278, U_CHAR=279,
		W_CHAR=280, X_CHAR=281, COMMENTENTRYTAG=282, COMMENTTAG=283, COMMACHAR=284,
		DOT=285, DOUBLEEQUALCHAR=286, NONNUMERICLITERAL=287, INTEGERLITERAL=288,
		NUMERICLITERAL=289, IDENTIFIER=290, FILENAME=291, SEPARATOR=292, NEWLINE=293,
		COMMENTENTRYLINE=294, COMMENTLINE=295, WS=296, TEXT=297;
	public static final int
		RULE_compilationUnit = 0, RULE_compilerOptions = 1, RULE_compilerXOpts = 2,
		RULE_compilerOption = 3, RULE_execCicsStatement = 4, RULE_execSqlStatement = 5,
		RULE_execSqlIncludeStatement = 6, RULE_execSqlImsStatement = 7, RULE_copyStatement = 8,
		RULE_copySource = 9, RULE_copyLibrary = 10, RULE_replacingPhrase = 11,
		RULE_replaceArea = 12, RULE_replaceByStatement = 13, RULE_replaceOffStatement = 14,
		RULE_replaceClause = 15, RULE_directoryPhrase = 16, RULE_familyPhrase = 17,
		RULE_replaceable = 18, RULE_replacement = 19, RULE_ejectStatement = 20,
		RULE_skipStatement = 21, RULE_titleStatement = 22, RULE_pseudoText = 23,
		RULE_charData = 24, RULE_charDataSql = 25, RULE_charDataLineNoDot = 26,
		RULE_charDataLine = 27, RULE_subscript = 28, RULE_cobolWord = 29, RULE_literal = 30,
		RULE_filename = 31, RULE_commentEntry = 32, RULE_charDataKeyword = 33;
	private static String[] makeRuleNames() {
		return new String[] {
			"compilationUnit", "compilerOptions", "compilerXOpts", "compilerOption",
			"execCicsStatement", "execSqlStatement", "execSqlIncludeStatement", "execSqlImsStatement",
			"copyStatement", "copySource", "copyLibrary", "replacingPhrase", "replaceArea",
			"replaceByStatement", "replaceOffStatement", "replaceClause", "directoryPhrase",
			"familyPhrase", "replaceable", "replacement", "ejectStatement", "skipStatement",
			"titleStatement", "pseudoText", "charData", "charDataSql", "charDataLineNoDot",
			"charDataLine", "subscript", "cobolWord", "literal", "filename", "commentEntry",
			"charDataKeyword"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'ADATA'", "'ADV'", "'ALIAS'", "'ANSI'", "'ANY'", "'APOST'", "'AR'",
			"'ARITH'", "'AUTO'", "'AWO'", "'BIN'", "'BLOCK0'", "'BUF'", "'BUFSIZE'",
			"'BY'", "'CBL'", "'CBLCARD'", "'CICS'", "'CO'", "'COBOL2'", "'COBOL3'",
			"'CODEPAGE'", "'COMPAT'", "'COMPILE'", "'COPY'", "'CP'", "'CPP'", "'CPSM'",
			"'CS'", "'CURR'", "'CURRENCY'", "'DATA'", "'DATEPROC'", "'DBCS'", "'DD'",
			"'DEBUG'", "'DECK'", "'DIAGTRUNC'", "'DLI'", "'DLL'", "'DP'", "'DTR'",
			"'DU'", "'DUMP'", "'DYN'", "'DYNAM'", "'EDF'", "'EJECT'", "'EJPD'", "'EN'",
			"'ENGLISH'", "'END-EXEC'", "'EPILOG'", "'EXCI'", "'EXEC'", "'EXIT'",
			"'EXP'", "'EXPORTALL'", "'EXTEND'", "'FASTSRT'", "'FEPI'", "'FLAG'",
			"'FLAGSTD'", "'FSRT'", "'FULL'", "'GDS'", "'GRAPHIC'", "'HOOK'", "'IN'",
			"'INCLUDE'", "'INTDATE'", "'JA'", "'JP'", "'KA'", "'LANG'", "'LANGUAGE'",
			"'LC'", "'LEASM'", "'LENGTH'", "'LIB'", "'LILIAN'", "'LIN'", "'LINECOUNT'",
			"'LINKAGE'", "'LIST'", "'LM'", "'LONGMIXED'", "'LONGUPPER'", "'('", "'LU'",
			"'MAP'", "'MARGINS'", "'MAX'", "'MD'", "'MDECK'", "'MIG'", "'MIXED'",
			"'NAME'", "'NAT'", "'NATIONAL'", "'NATLANG'", "'NN'", "'NO'", "'NOADATA'",
			"'NOADV'", "'NOALIAS'", "'NOAWO'", "'NOBLOCK0'", "'NOC'", "'NOCBLCARD'",
			"'NOCICS'", "'NOCMPR2'", "'NOCOMPILE'", "'NOCPSM'", "'NOCURR'", "'NOCURRENCY'",
			"'NOD'", "'NODATEPROC'", "'NODBCS'", "'NODE'", "'NODEBUG'", "'NODECK'",
			"'NODIAGTRUNC'", "'NODLL'", "'NODU'", "'NODUMP'", "'NODP'", "'NODTR'",
			"'NODYN'", "'NODYNAM'", "'NOEDF'", "'NOEJPD'", "'NOEPILOG'", "'NOEXIT'",
			"'NOEXP'", "'NOEXPORTALL'", "'NOF'", "'NOFASTSRT'", "'NOFEPI'", "'NOFLAG'",
			"'NOFLAGMIG'", "'NOFLAGSTD'", "'NOFSRT'", "'NOGRAPHIC'", "'NOHOOK'",
			"'NOLENGTH'", "'NOLIB'", "'NOLINKAGE'", "'NOLIST'", "'NOMAP'", "'NOMD'",
			"'NOMDECK'", "'NONAME'", "'NONUM'", "'NONUMBER'", "'NOOBJ'", "'NOOBJECT'",
			"'NOOFF'", "'NOOFFSET'", "'NOOPSEQUENCE'", "'NOOPT'", "'NOOPTIMIZE'",
			"'NOOPTIONS'", "'NOP'", "'NOPFD'", "'NOPROLOG'", "'NORENT'", "'NOS'",
			"'NOSEP'", "'NOSEPARATE'", "'NOSEQ'", "'NOSOURCE'", "'NOSPIE'", "'NOSQL'",
			"'NOSQLC'", "'NOSQLCCSID'", "'NOSSR'", "'NOSSRANGE'", "'NOSTDTRUNC'",
			"'NOSEQUENCE'", "'NOTERM'", "'NOTERMINAL'", "'NOTEST'", "'NOTHREAD'",
			"'NOTRIG'", "'NOVBREF'", "'NOWD'", "'NOWORD'", "'NOX'", "'NOXREF'", "'NOZWB'",
			"'NS'", "'NSEQ'", "'NSYMBOL'", "'NUM'", "'NUMBER'", "'NUMPROC'", "'OBJ'",
			"'OBJECT'", "'OF'", "'OFF'", "'OFFSET'", "'ON'", "'OP'", "'OPMARGINS'",
			"'OPSEQUENCE'", "'OPT'", "'OPTFILE'", "'OPTIMIZE'", "'OPTIONS'", "'OUT'",
			"'OUTDD'", "'PFD'", "'PPTDBG'", "'PGMN'", "'PGMNAME'", "'PROCESS'", "'PROLOG'",
			"'QUOTE'", "'RENT'", "'REPLACE'", "'REPLACING'", "'RMODE'", "')'", "'SEP'",
			"'SEPARATE'", "'SEQ'", "'SEQUENCE'", "'SHORT'", "'SIZE'", "'SOURCE'",
			"'SP'", "'SPACE'", "'SPIE'", "'SQL'", "'SQLC'", "'SQLCCSID'", "'SQLIMS'",
			"'SKIP1'", "'SKIP2'", "'SKIP3'", "'SS'", "'SSR'", "'SSRANGE'", "'STD'",
			"'SUPPRESS'", "'SYSEIB'", "'SZ'", "'TERM'", "'TERMINAL'", "'TEST'", "'THREAD'",
			"'TITLE'", "'TRIG'", "'TRUNC'", "'UE'", "'UPPER'", "'VBREF'", "'WD'",
			"'WORD'", "'XMLPARSE'", "'XMLSS'", "'XOPTS'", "'XP'", "'XREF'", "'YEARWINDOW'",
			"'YW'", "'ZWB'", "'C'", "'D'", "'E'", "'F'", "'H'", "'I'", "'M'", "'N'",
			"'Q'", "'S'", "'U'", "'W'", "'X'", "'*>CE'", "'*>'", "','", "'.'", "'=='"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "ADATA", "ADV", "ALIAS", "ANSI", "ANY", "APOST", "AR", "ARITH",
			"AUTO", "AWO", "BIN", "BLOCK0", "BUF", "BUFSIZE", "BY", "CBL", "CBLCARD",
			"CICS", "CO", "COBOL2", "COBOL3", "CODEPAGE", "COMPAT", "COMPILE", "COPY",
			"CP", "CPP", "CPSM", "CS", "CURR", "CURRENCY", "DATA", "DATEPROC", "DBCS",
			"DD", "DEBUG", "DECK", "DIAGTRUNC", "DLI", "DLL", "DP", "DTR", "DU",
			"DUMP", "DYN", "DYNAM", "EDF", "EJECT", "EJPD", "EN", "ENGLISH", "END_EXEC",
			"EPILOG", "EXCI", "EXEC", "EXIT", "EXP", "EXPORTALL", "EXTEND", "FASTSRT",
			"FEPI", "FLAG", "FLAGSTD", "FSRT", "FULL", "GDS", "GRAPHIC", "HOOK",
			"IN", "INCLUDE", "INTDATE", "JA", "JP", "KA", "LANG", "LANGUAGE", "LC",
			"LEASM", "LENGTH", "LIB", "LILIAN", "LIN", "LINECOUNT", "LINKAGE", "LIST",
			"LM", "LONGMIXED", "LONGUPPER", "LPARENCHAR", "LU", "MAP", "MARGINS",
			"MAX", "MD", "MDECK", "MIG", "MIXED", "NAME", "NAT", "NATIONAL", "NATLANG",
			"NN", "NO", "NOADATA", "NOADV", "NOALIAS", "NOAWO", "NOBLOCK0", "NOC",
			"NOCBLCARD", "NOCICS", "NOCMPR2", "NOCOMPILE", "NOCPSM", "NOCURR", "NOCURRENCY",
			"NOD", "NODATEPROC", "NODBCS", "NODE", "NODEBUG", "NODECK", "NODIAGTRUNC",
			"NODLL", "NODU", "NODUMP", "NODP", "NODTR", "NODYN", "NODYNAM", "NOEDF",
			"NOEJPD", "NOEPILOG", "NOEXIT", "NOEXP", "NOEXPORTALL", "NOF", "NOFASTSRT",
			"NOFEPI", "NOFLAG", "NOFLAGMIG", "NOFLAGSTD", "NOFSRT", "NOGRAPHIC",
			"NOHOOK", "NOLENGTH", "NOLIB", "NOLINKAGE", "NOLIST", "NOMAP", "NOMD",
			"NOMDECK", "NONAME", "NONUM", "NONUMBER", "NOOBJ", "NOOBJECT", "NOOFF",
			"NOOFFSET", "NOOPSEQUENCE", "NOOPT", "NOOPTIMIZE", "NOOPTIONS", "NOP",
			"NOPFD", "NOPROLOG", "NORENT", "NOS", "NOSEP", "NOSEPARATE", "NOSEQ",
			"NOSOURCE", "NOSPIE", "NOSQL", "NOSQLC", "NOSQLCCSID", "NOSSR", "NOSSRANGE",
			"NOSTDTRUNC", "NOSEQUENCE", "NOTERM", "NOTERMINAL", "NOTEST", "NOTHREAD",
			"NOTRIG", "NOVBREF", "NOWD", "NOWORD", "NOX", "NOXREF", "NOZWB", "NS",
			"NSEQ", "NSYMBOL", "NUM", "NUMBER", "NUMPROC", "OBJ", "OBJECT", "OF",
			"OFF", "OFFSET", "ON", "OP", "OPMARGINS", "OPSEQUENCE", "OPT", "OPTFILE",
			"OPTIMIZE", "OPTIONS", "OUT", "OUTDD", "PFD", "PPTDBG", "PGMN", "PGMNAME",
			"PROCESS", "PROLOG", "QUOTE", "RENT", "REPLACE", "REPLACING", "RMODE",
			"RPARENCHAR", "SEP", "SEPARATE", "SEQ", "SEQUENCE", "SHORT", "SIZE",
			"SOURCE", "SP", "SPACE", "SPIE", "SQL", "SQLC", "SQLCCSID", "SQLIMS",
			"SKIP1", "SKIP2", "SKIP3", "SS", "SSR", "SSRANGE", "STD", "SUPPRESS",
			"SYSEIB", "SZ", "TERM", "TERMINAL", "TEST", "THREAD", "TITLE", "TRIG",
			"TRUNC", "UE", "UPPER", "VBREF", "WD", "WORD", "XMLPARSE", "XMLSS", "XOPTS",
			"XP", "XREF", "YEARWINDOW", "YW", "ZWB", "C_CHAR", "D_CHAR", "E_CHAR",
			"F_CHAR", "H_CHAR", "I_CHAR", "M_CHAR", "N_CHAR", "Q_CHAR", "S_CHAR",
			"U_CHAR", "W_CHAR", "X_CHAR", "COMMENTENTRYTAG", "COMMENTTAG", "COMMACHAR",
			"DOT", "DOUBLEEQUALCHAR", "NONNUMERICLITERAL", "INTEGERLITERAL", "NUMERICLITERAL",
			"IDENTIFIER", "FILENAME", "SEPARATOR", "NEWLINE", "COMMENTENTRYLINE",
			"COMMENTLINE", "WS", "TEXT"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "CobolPreprocessor.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public CobolPreprocessorParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CompilationUnitContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(CobolPreprocessorParser.EOF, 0); }
		public List<CompilerOptionsContext> compilerOptions() {
			return getRuleContexts(CompilerOptionsContext.class);
		}
		public CompilerOptionsContext compilerOptions(int i) {
			return getRuleContext(CompilerOptionsContext.class,i);
		}
		public List<CopyStatementContext> copyStatement() {
			return getRuleContexts(CopyStatementContext.class);
		}
		public CopyStatementContext copyStatement(int i) {
			return getRuleContext(CopyStatementContext.class,i);
		}
		public List<ExecCicsStatementContext> execCicsStatement() {
			return getRuleContexts(ExecCicsStatementContext.class);
		}
		public ExecCicsStatementContext execCicsStatement(int i) {
			return getRuleContext(ExecCicsStatementContext.class,i);
		}
		public List<ExecSqlStatementContext> execSqlStatement() {
			return getRuleContexts(ExecSqlStatementContext.class);
		}
		public ExecSqlStatementContext execSqlStatement(int i) {
			return getRuleContext(ExecSqlStatementContext.class,i);
		}
		public List<ExecSqlIncludeStatementContext> execSqlIncludeStatement() {
			return getRuleContexts(ExecSqlIncludeStatementContext.class);
		}
		public ExecSqlIncludeStatementContext execSqlIncludeStatement(int i) {
			return getRuleContext(ExecSqlIncludeStatementContext.class,i);
		}
		public List<ExecSqlImsStatementContext> execSqlImsStatement() {
			return getRuleContexts(ExecSqlImsStatementContext.class);
		}
		public ExecSqlImsStatementContext execSqlImsStatement(int i) {
			return getRuleContext(ExecSqlImsStatementContext.class,i);
		}
		public List<ReplaceOffStatementContext> replaceOffStatement() {
			return getRuleContexts(ReplaceOffStatementContext.class);
		}
		public ReplaceOffStatementContext replaceOffStatement(int i) {
			return getRuleContext(ReplaceOffStatementContext.class,i);
		}
		public List<ReplaceAreaContext> replaceArea() {
			return getRuleContexts(ReplaceAreaContext.class);
		}
		public ReplaceAreaContext replaceArea(int i) {
			return getRuleContext(ReplaceAreaContext.class,i);
		}
		public List<EjectStatementContext> ejectStatement() {
			return getRuleContexts(EjectStatementContext.class);
		}
		public EjectStatementContext ejectStatement(int i) {
			return getRuleContext(EjectStatementContext.class,i);
		}
		public List<SkipStatementContext> skipStatement() {
			return getRuleContexts(SkipStatementContext.class);
		}
		public SkipStatementContext skipStatement(int i) {
			return getRuleContext(SkipStatementContext.class,i);
		}
		public List<TitleStatementContext> titleStatement() {
			return getRuleContexts(TitleStatementContext.class);
		}
		public TitleStatementContext titleStatement(int i) {
			return getRuleContext(TitleStatementContext.class,i);
		}
		public List<CharDataLineContext> charDataLine() {
			return getRuleContexts(CharDataLineContext.class);
		}
		public CharDataLineContext charDataLine(int i) {
			return getRuleContext(CharDataLineContext.class,i);
		}
		public CompilationUnitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_compilationUnit; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).enterCompilationUnit(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).exitCompilationUnit(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof CobolPreprocessorVisitor) return ((CobolPreprocessorVisitor<? extends T>) visitor).visitCompilationUnit(this);
			return visitor.visitChildren(this);
		}
	}

	public final CompilationUnitContext compilationUnit() throws RecognitionException {
		CompilationUnitContext _localctx = new CompilationUnitContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_compilationUnit);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(82);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((_la & ~0x3f) == 0 && ((1L << _la) & -2310346608841326594L) != 0) || (((_la - 64) & ~0x3f) == 0 && ((1L << (_la - 64)) & -16449L) != 0) || (((_la - 128) & ~0x3f) == 0 && ((1L << (_la - 128)) & -576460752303423489L) != 0) || (((_la - 192) & ~0x3f) == 0 && ((1L << (_la - 192)) & -18084767253659649L) != 0) || (((_la - 256) & ~0x3f) == 0 && ((1L << (_la - 256)) & 2541345570543L) != 0)) {
				{
				setState(80);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
				case 1:
					{
					setState(68);
					compilerOptions();
					}
					break;
				case 2:
					{
					setState(69);
					copyStatement();
					}
					break;
				case 3:
					{
					setState(70);
					execCicsStatement();
					}
					break;
				case 4:
					{
					setState(71);
					execSqlStatement();
					}
					break;
				case 5:
					{
					setState(72);
					execSqlIncludeStatement();
					}
					break;
				case 6:
					{
					setState(73);
					execSqlImsStatement();
					}
					break;
				case 7:
					{
					setState(74);
					replaceOffStatement();
					}
					break;
				case 8:
					{
					setState(75);
					replaceArea();
					}
					break;
				case 9:
					{
					setState(76);
					ejectStatement();
					}
					break;
				case 10:
					{
					setState(77);
					skipStatement();
					}
					break;
				case 11:
					{
					setState(78);
					titleStatement();
					}
					break;
				case 12:
					{
					setState(79);
					charDataLine();
					}
					break;
				}
				}
				setState(84);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(85);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CompilerOptionsContext extends ParserRuleContext {
		public TerminalNode PROCESS() { return getToken(CobolPreprocessorParser.PROCESS, 0); }
		public TerminalNode CBL() { return getToken(CobolPreprocessorParser.CBL, 0); }
		public List<CompilerOptionContext> compilerOption() {
			return getRuleContexts(CompilerOptionContext.class);
		}
		public CompilerOptionContext compilerOption(int i) {
			return getRuleContext(CompilerOptionContext.class,i);
		}
		public List<CompilerXOptsContext> compilerXOpts() {
			return getRuleContexts(CompilerXOptsContext.class);
		}
		public CompilerXOptsContext compilerXOpts(int i) {
			return getRuleContext(CompilerXOptsContext.class,i);
		}
		public List<TerminalNode> COMMACHAR() { return getTokens(CobolPreprocessorParser.COMMACHAR); }
		public TerminalNode COMMACHAR(int i) {
			return getToken(CobolPreprocessorParser.COMMACHAR, i);
		}
		public CompilerOptionsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_compilerOptions; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).enterCompilerOptions(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).exitCompilerOptions(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof CobolPreprocessorVisitor) return ((CobolPreprocessorVisitor<? extends T>) visitor).visitCompilerOptions(this);
			return visitor.visitChildren(this);
		}
	}

	public final CompilerOptionsContext compilerOptions() throws RecognitionException {
		CompilerOptionsContext _localctx = new CompilerOptionsContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_compilerOptions);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(87);
			_la = _input.LA(1);
			if ( !(_la==CBL || _la==PROCESS) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(93);
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					setState(93);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case ADATA:
					case ADV:
					case APOST:
					case AR:
					case ARITH:
					case AWO:
					case BLOCK0:
					case BUF:
					case BUFSIZE:
					case CBLCARD:
					case CICS:
					case COBOL2:
					case COBOL3:
					case CODEPAGE:
					case COMPILE:
					case CP:
					case CPP:
					case CPSM:
					case CURR:
					case CURRENCY:
					case DATA:
					case DATEPROC:
					case DBCS:
					case DEBUG:
					case DECK:
					case DIAGTRUNC:
					case DLL:
					case DP:
					case DTR:
					case DU:
					case DUMP:
					case DYN:
					case DYNAM:
					case EDF:
					case EPILOG:
					case EXIT:
					case EXP:
					case EXPORTALL:
					case FASTSRT:
					case FEPI:
					case FLAG:
					case FLAGSTD:
					case FSRT:
					case GDS:
					case GRAPHIC:
					case INTDATE:
					case LANG:
					case LANGUAGE:
					case LC:
					case LEASM:
					case LENGTH:
					case LIB:
					case LIN:
					case LINECOUNT:
					case LINKAGE:
					case LIST:
					case MAP:
					case MARGINS:
					case MD:
					case MDECK:
					case NAME:
					case NATLANG:
					case NOADATA:
					case NOADV:
					case NOAWO:
					case NOBLOCK0:
					case NOC:
					case NOCBLCARD:
					case NOCICS:
					case NOCMPR2:
					case NOCOMPILE:
					case NOCPSM:
					case NOCURR:
					case NOCURRENCY:
					case NOD:
					case NODATEPROC:
					case NODBCS:
					case NODE:
					case NODEBUG:
					case NODECK:
					case NODIAGTRUNC:
					case NODLL:
					case NODU:
					case NODUMP:
					case NODP:
					case NODTR:
					case NODYN:
					case NODYNAM:
					case NOEDF:
					case NOEPILOG:
					case NOEXIT:
					case NOEXP:
					case NOEXPORTALL:
					case NOF:
					case NOFASTSRT:
					case NOFEPI:
					case NOFLAG:
					case NOFLAGMIG:
					case NOFLAGSTD:
					case NOFSRT:
					case NOGRAPHIC:
					case NOLENGTH:
					case NOLIB:
					case NOLINKAGE:
					case NOLIST:
					case NOMAP:
					case NOMD:
					case NOMDECK:
					case NONAME:
					case NONUM:
					case NONUMBER:
					case NOOBJ:
					case NOOBJECT:
					case NOOFF:
					case NOOFFSET:
					case NOOPSEQUENCE:
					case NOOPT:
					case NOOPTIMIZE:
					case NOOPTIONS:
					case NOP:
					case NOPROLOG:
					case NORENT:
					case NOS:
					case NOSEQ:
					case NOSOURCE:
					case NOSPIE:
					case NOSQL:
					case NOSQLC:
					case NOSQLCCSID:
					case NOSSR:
					case NOSSRANGE:
					case NOSTDTRUNC:
					case NOSEQUENCE:
					case NOTERM:
					case NOTERMINAL:
					case NOTEST:
					case NOTHREAD:
					case NOVBREF:
					case NOWD:
					case NOWORD:
					case NOX:
					case NOXREF:
					case NOZWB:
					case NS:
					case NSEQ:
					case NSYMBOL:
					case NUM:
					case NUMBER:
					case NUMPROC:
					case OBJ:
					case OBJECT:
					case OFF:
					case OFFSET:
					case OP:
					case OPMARGINS:
					case OPSEQUENCE:
					case OPT:
					case OPTFILE:
					case OPTIMIZE:
					case OPTIONS:
					case OUT:
					case OUTDD:
					case PGMN:
					case PGMNAME:
					case PROLOG:
					case QUOTE:
					case RENT:
					case RMODE:
					case SEQ:
					case SEQUENCE:
					case SIZE:
					case SOURCE:
					case SP:
					case SPACE:
					case SPIE:
					case SQL:
					case SQLC:
					case SQLCCSID:
					case SSR:
					case SSRANGE:
					case SYSEIB:
					case SZ:
					case TERM:
					case TERMINAL:
					case TEST:
					case THREAD:
					case TRUNC:
					case VBREF:
					case WD:
					case WORD:
					case XMLPARSE:
					case XP:
					case XREF:
					case YEARWINDOW:
					case YW:
					case ZWB:
					case C_CHAR:
					case D_CHAR:
					case F_CHAR:
					case Q_CHAR:
					case S_CHAR:
					case X_CHAR:
					case COMMACHAR:
						{
						setState(89);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==COMMACHAR) {
							{
							setState(88);
							match(COMMACHAR);
							}
						}

						setState(91);
						compilerOption();
						}
						break;
					case XOPTS:
						{
						setState(92);
						compilerXOpts();
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(95);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CompilerXOptsContext extends ParserRuleContext {
		public TerminalNode XOPTS() { return getToken(CobolPreprocessorParser.XOPTS, 0); }
		public TerminalNode LPARENCHAR() { return getToken(CobolPreprocessorParser.LPARENCHAR, 0); }
		public List<CompilerOptionContext> compilerOption() {
			return getRuleContexts(CompilerOptionContext.class);
		}
		public CompilerOptionContext compilerOption(int i) {
			return getRuleContext(CompilerOptionContext.class,i);
		}
		public TerminalNode RPARENCHAR() { return getToken(CobolPreprocessorParser.RPARENCHAR, 0); }
		public List<TerminalNode> COMMACHAR() { return getTokens(CobolPreprocessorParser.COMMACHAR); }
		public TerminalNode COMMACHAR(int i) {
			return getToken(CobolPreprocessorParser.COMMACHAR, i);
		}
		public CompilerXOptsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_compilerXOpts; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).enterCompilerXOpts(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).exitCompilerXOpts(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof CobolPreprocessorVisitor) return ((CobolPreprocessorVisitor<? extends T>) visitor).visitCompilerXOpts(this);
			return visitor.visitChildren(this);
		}
	}

	public final CompilerXOptsContext compilerXOpts() throws RecognitionException {
		CompilerXOptsContext _localctx = new CompilerXOptsContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_compilerXOpts);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(97);
			match(XOPTS);
			setState(98);
			match(LPARENCHAR);
			setState(99);
			compilerOption();
			setState(106);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((_la & ~0x3f) == 0 && ((1L << _la) & -639230256804891194L) != 0) || (((_la - 64) & ~0x3f) == 0 && ((1L << (_la - 64)) & -5339311376243L) != 0) || (((_la - 128) & ~0x3f) == 0 && ((1L << (_la - 128)) & -144121922584707089L) != 0) || (((_la - 192) & ~0x3f) == 0 && ((1L << (_la - 192)) & -6946732225628997889L) != 0) || (((_la - 258) & ~0x3f) == 0 && ((1L << (_la - 258)) & 77094863L) != 0)) {
				{
				{
				setState(101);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMACHAR) {
					{
					setState(100);
					match(COMMACHAR);
					}
				}

				setState(103);
				compilerOption();
				}
				}
				setState(108);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(109);
			match(RPARENCHAR);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CompilerOptionContext extends ParserRuleContext {
		public TerminalNode ADATA() { return getToken(CobolPreprocessorParser.ADATA, 0); }
		public TerminalNode ADV() { return getToken(CobolPreprocessorParser.ADV, 0); }
		public TerminalNode APOST() { return getToken(CobolPreprocessorParser.APOST, 0); }
		public TerminalNode LPARENCHAR() { return getToken(CobolPreprocessorParser.LPARENCHAR, 0); }
		public TerminalNode RPARENCHAR() { return getToken(CobolPreprocessorParser.RPARENCHAR, 0); }
		public TerminalNode ARITH() { return getToken(CobolPreprocessorParser.ARITH, 0); }
		public TerminalNode AR() { return getToken(CobolPreprocessorParser.AR, 0); }
		public TerminalNode EXTEND() { return getToken(CobolPreprocessorParser.EXTEND, 0); }
		public List<TerminalNode> E_CHAR() { return getTokens(CobolPreprocessorParser.E_CHAR); }
		public TerminalNode E_CHAR(int i) {
			return getToken(CobolPreprocessorParser.E_CHAR, i);
		}
		public TerminalNode COMPAT() { return getToken(CobolPreprocessorParser.COMPAT, 0); }
		public TerminalNode C_CHAR() { return getToken(CobolPreprocessorParser.C_CHAR, 0); }
		public TerminalNode AWO() { return getToken(CobolPreprocessorParser.AWO, 0); }
		public TerminalNode BLOCK0() { return getToken(CobolPreprocessorParser.BLOCK0, 0); }
		public List<LiteralContext> literal() {
			return getRuleContexts(LiteralContext.class);
		}
		public LiteralContext literal(int i) {
			return getRuleContext(LiteralContext.class,i);
		}
		public TerminalNode BUFSIZE() { return getToken(CobolPreprocessorParser.BUFSIZE, 0); }
		public TerminalNode BUF() { return getToken(CobolPreprocessorParser.BUF, 0); }
		public TerminalNode CBLCARD() { return getToken(CobolPreprocessorParser.CBLCARD, 0); }
		public TerminalNode CICS() { return getToken(CobolPreprocessorParser.CICS, 0); }
		public TerminalNode COBOL2() { return getToken(CobolPreprocessorParser.COBOL2, 0); }
		public TerminalNode COBOL3() { return getToken(CobolPreprocessorParser.COBOL3, 0); }
		public TerminalNode CODEPAGE() { return getToken(CobolPreprocessorParser.CODEPAGE, 0); }
		public TerminalNode CP() { return getToken(CobolPreprocessorParser.CP, 0); }
		public TerminalNode COMPILE() { return getToken(CobolPreprocessorParser.COMPILE, 0); }
		public TerminalNode CPP() { return getToken(CobolPreprocessorParser.CPP, 0); }
		public TerminalNode CPSM() { return getToken(CobolPreprocessorParser.CPSM, 0); }
		public TerminalNode CURRENCY() { return getToken(CobolPreprocessorParser.CURRENCY, 0); }
		public TerminalNode CURR() { return getToken(CobolPreprocessorParser.CURR, 0); }
		public TerminalNode DATA() { return getToken(CobolPreprocessorParser.DATA, 0); }
		public TerminalNode DATEPROC() { return getToken(CobolPreprocessorParser.DATEPROC, 0); }
		public TerminalNode DP() { return getToken(CobolPreprocessorParser.DP, 0); }
		public List<TerminalNode> COMMACHAR() { return getTokens(CobolPreprocessorParser.COMMACHAR); }
		public TerminalNode COMMACHAR(int i) {
			return getToken(CobolPreprocessorParser.COMMACHAR, i);
		}
		public TerminalNode FLAG() { return getToken(CobolPreprocessorParser.FLAG, 0); }
		public TerminalNode NOFLAG() { return getToken(CobolPreprocessorParser.NOFLAG, 0); }
		public TerminalNode TRIG() { return getToken(CobolPreprocessorParser.TRIG, 0); }
		public TerminalNode NOTRIG() { return getToken(CobolPreprocessorParser.NOTRIG, 0); }
		public TerminalNode DBCS() { return getToken(CobolPreprocessorParser.DBCS, 0); }
		public TerminalNode DECK() { return getToken(CobolPreprocessorParser.DECK, 0); }
		public TerminalNode D_CHAR() { return getToken(CobolPreprocessorParser.D_CHAR, 0); }
		public TerminalNode DEBUG() { return getToken(CobolPreprocessorParser.DEBUG, 0); }
		public TerminalNode DIAGTRUNC() { return getToken(CobolPreprocessorParser.DIAGTRUNC, 0); }
		public TerminalNode DTR() { return getToken(CobolPreprocessorParser.DTR, 0); }
		public TerminalNode DLL() { return getToken(CobolPreprocessorParser.DLL, 0); }
		public TerminalNode DUMP() { return getToken(CobolPreprocessorParser.DUMP, 0); }
		public TerminalNode DU() { return getToken(CobolPreprocessorParser.DU, 0); }
		public TerminalNode DYNAM() { return getToken(CobolPreprocessorParser.DYNAM, 0); }
		public TerminalNode DYN() { return getToken(CobolPreprocessorParser.DYN, 0); }
		public TerminalNode EDF() { return getToken(CobolPreprocessorParser.EDF, 0); }
		public TerminalNode EPILOG() { return getToken(CobolPreprocessorParser.EPILOG, 0); }
		public TerminalNode EXIT() { return getToken(CobolPreprocessorParser.EXIT, 0); }
		public TerminalNode EXPORTALL() { return getToken(CobolPreprocessorParser.EXPORTALL, 0); }
		public TerminalNode EXP() { return getToken(CobolPreprocessorParser.EXP, 0); }
		public TerminalNode FASTSRT() { return getToken(CobolPreprocessorParser.FASTSRT, 0); }
		public TerminalNode FSRT() { return getToken(CobolPreprocessorParser.FSRT, 0); }
		public TerminalNode FEPI() { return getToken(CobolPreprocessorParser.FEPI, 0); }
		public TerminalNode F_CHAR() { return getToken(CobolPreprocessorParser.F_CHAR, 0); }
		public List<TerminalNode> I_CHAR() { return getTokens(CobolPreprocessorParser.I_CHAR); }
		public TerminalNode I_CHAR(int i) {
			return getToken(CobolPreprocessorParser.I_CHAR, i);
		}
		public List<TerminalNode> S_CHAR() { return getTokens(CobolPreprocessorParser.S_CHAR); }
		public TerminalNode S_CHAR(int i) {
			return getToken(CobolPreprocessorParser.S_CHAR, i);
		}
		public List<TerminalNode> U_CHAR() { return getTokens(CobolPreprocessorParser.U_CHAR); }
		public TerminalNode U_CHAR(int i) {
			return getToken(CobolPreprocessorParser.U_CHAR, i);
		}
		public List<TerminalNode> W_CHAR() { return getTokens(CobolPreprocessorParser.W_CHAR); }
		public TerminalNode W_CHAR(int i) {
			return getToken(CobolPreprocessorParser.W_CHAR, i);
		}
		public TerminalNode FLAGSTD() { return getToken(CobolPreprocessorParser.FLAGSTD, 0); }
		public TerminalNode M_CHAR() { return getToken(CobolPreprocessorParser.M_CHAR, 0); }
		public TerminalNode H_CHAR() { return getToken(CobolPreprocessorParser.H_CHAR, 0); }
		public TerminalNode DD() { return getToken(CobolPreprocessorParser.DD, 0); }
		public TerminalNode N_CHAR() { return getToken(CobolPreprocessorParser.N_CHAR, 0); }
		public TerminalNode NN() { return getToken(CobolPreprocessorParser.NN, 0); }
		public TerminalNode SS() { return getToken(CobolPreprocessorParser.SS, 0); }
		public TerminalNode GDS() { return getToken(CobolPreprocessorParser.GDS, 0); }
		public TerminalNode GRAPHIC() { return getToken(CobolPreprocessorParser.GRAPHIC, 0); }
		public TerminalNode INTDATE() { return getToken(CobolPreprocessorParser.INTDATE, 0); }
		public TerminalNode ANSI() { return getToken(CobolPreprocessorParser.ANSI, 0); }
		public TerminalNode LILIAN() { return getToken(CobolPreprocessorParser.LILIAN, 0); }
		public TerminalNode LANGUAGE() { return getToken(CobolPreprocessorParser.LANGUAGE, 0); }
		public TerminalNode LANG() { return getToken(CobolPreprocessorParser.LANG, 0); }
		public TerminalNode ENGLISH() { return getToken(CobolPreprocessorParser.ENGLISH, 0); }
		public TerminalNode CS() { return getToken(CobolPreprocessorParser.CS, 0); }
		public TerminalNode EN() { return getToken(CobolPreprocessorParser.EN, 0); }
		public TerminalNode JA() { return getToken(CobolPreprocessorParser.JA, 0); }
		public TerminalNode JP() { return getToken(CobolPreprocessorParser.JP, 0); }
		public TerminalNode KA() { return getToken(CobolPreprocessorParser.KA, 0); }
		public TerminalNode UE() { return getToken(CobolPreprocessorParser.UE, 0); }
		public TerminalNode LEASM() { return getToken(CobolPreprocessorParser.LEASM, 0); }
		public TerminalNode LENGTH() { return getToken(CobolPreprocessorParser.LENGTH, 0); }
		public TerminalNode LIB() { return getToken(CobolPreprocessorParser.LIB, 0); }
		public TerminalNode LIN() { return getToken(CobolPreprocessorParser.LIN, 0); }
		public TerminalNode LINECOUNT() { return getToken(CobolPreprocessorParser.LINECOUNT, 0); }
		public TerminalNode LC() { return getToken(CobolPreprocessorParser.LC, 0); }
		public TerminalNode LINKAGE() { return getToken(CobolPreprocessorParser.LINKAGE, 0); }
		public TerminalNode LIST() { return getToken(CobolPreprocessorParser.LIST, 0); }
		public TerminalNode MAP() { return getToken(CobolPreprocessorParser.MAP, 0); }
		public TerminalNode MARGINS() { return getToken(CobolPreprocessorParser.MARGINS, 0); }
		public TerminalNode MDECK() { return getToken(CobolPreprocessorParser.MDECK, 0); }
		public TerminalNode MD() { return getToken(CobolPreprocessorParser.MD, 0); }
		public TerminalNode NOC() { return getToken(CobolPreprocessorParser.NOC, 0); }
		public TerminalNode NOCOMPILE() { return getToken(CobolPreprocessorParser.NOCOMPILE, 0); }
		public TerminalNode NAME() { return getToken(CobolPreprocessorParser.NAME, 0); }
		public TerminalNode ALIAS() { return getToken(CobolPreprocessorParser.ALIAS, 0); }
		public TerminalNode NOALIAS() { return getToken(CobolPreprocessorParser.NOALIAS, 0); }
		public TerminalNode NATLANG() { return getToken(CobolPreprocessorParser.NATLANG, 0); }
		public TerminalNode NOADATA() { return getToken(CobolPreprocessorParser.NOADATA, 0); }
		public TerminalNode NOADV() { return getToken(CobolPreprocessorParser.NOADV, 0); }
		public TerminalNode NOAWO() { return getToken(CobolPreprocessorParser.NOAWO, 0); }
		public TerminalNode NOBLOCK0() { return getToken(CobolPreprocessorParser.NOBLOCK0, 0); }
		public TerminalNode NOCBLCARD() { return getToken(CobolPreprocessorParser.NOCBLCARD, 0); }
		public TerminalNode NOCICS() { return getToken(CobolPreprocessorParser.NOCICS, 0); }
		public TerminalNode NOCMPR2() { return getToken(CobolPreprocessorParser.NOCMPR2, 0); }
		public TerminalNode NOCPSM() { return getToken(CobolPreprocessorParser.NOCPSM, 0); }
		public TerminalNode NOCURRENCY() { return getToken(CobolPreprocessorParser.NOCURRENCY, 0); }
		public TerminalNode NOCURR() { return getToken(CobolPreprocessorParser.NOCURR, 0); }
		public TerminalNode NODATEPROC() { return getToken(CobolPreprocessorParser.NODATEPROC, 0); }
		public TerminalNode NODP() { return getToken(CobolPreprocessorParser.NODP, 0); }
		public TerminalNode NODBCS() { return getToken(CobolPreprocessorParser.NODBCS, 0); }
		public TerminalNode NODEBUG() { return getToken(CobolPreprocessorParser.NODEBUG, 0); }
		public TerminalNode NODECK() { return getToken(CobolPreprocessorParser.NODECK, 0); }
		public TerminalNode NOD() { return getToken(CobolPreprocessorParser.NOD, 0); }
		public TerminalNode NODLL() { return getToken(CobolPreprocessorParser.NODLL, 0); }
		public TerminalNode NODE() { return getToken(CobolPreprocessorParser.NODE, 0); }
		public TerminalNode NODUMP() { return getToken(CobolPreprocessorParser.NODUMP, 0); }
		public TerminalNode NODU() { return getToken(CobolPreprocessorParser.NODU, 0); }
		public TerminalNode NODIAGTRUNC() { return getToken(CobolPreprocessorParser.NODIAGTRUNC, 0); }
		public TerminalNode NODTR() { return getToken(CobolPreprocessorParser.NODTR, 0); }
		public TerminalNode NODYNAM() { return getToken(CobolPreprocessorParser.NODYNAM, 0); }
		public TerminalNode NODYN() { return getToken(CobolPreprocessorParser.NODYN, 0); }
		public TerminalNode NOEDF() { return getToken(CobolPreprocessorParser.NOEDF, 0); }
		public TerminalNode NOEPILOG() { return getToken(CobolPreprocessorParser.NOEPILOG, 0); }
		public TerminalNode NOEXIT() { return getToken(CobolPreprocessorParser.NOEXIT, 0); }
		public TerminalNode NOEXPORTALL() { return getToken(CobolPreprocessorParser.NOEXPORTALL, 0); }
		public TerminalNode NOEXP() { return getToken(CobolPreprocessorParser.NOEXP, 0); }
		public TerminalNode NOFASTSRT() { return getToken(CobolPreprocessorParser.NOFASTSRT, 0); }
		public TerminalNode NOFSRT() { return getToken(CobolPreprocessorParser.NOFSRT, 0); }
		public TerminalNode NOFEPI() { return getToken(CobolPreprocessorParser.NOFEPI, 0); }
		public TerminalNode NOF() { return getToken(CobolPreprocessorParser.NOF, 0); }
		public TerminalNode NOFLAGMIG() { return getToken(CobolPreprocessorParser.NOFLAGMIG, 0); }
		public TerminalNode NOFLAGSTD() { return getToken(CobolPreprocessorParser.NOFLAGSTD, 0); }
		public TerminalNode NOGRAPHIC() { return getToken(CobolPreprocessorParser.NOGRAPHIC, 0); }
		public TerminalNode NOLENGTH() { return getToken(CobolPreprocessorParser.NOLENGTH, 0); }
		public TerminalNode NOLIB() { return getToken(CobolPreprocessorParser.NOLIB, 0); }
		public TerminalNode NOLINKAGE() { return getToken(CobolPreprocessorParser.NOLINKAGE, 0); }
		public TerminalNode NOLIST() { return getToken(CobolPreprocessorParser.NOLIST, 0); }
		public TerminalNode NOMAP() { return getToken(CobolPreprocessorParser.NOMAP, 0); }
		public TerminalNode NOMDECK() { return getToken(CobolPreprocessorParser.NOMDECK, 0); }
		public TerminalNode NOMD() { return getToken(CobolPreprocessorParser.NOMD, 0); }
		public TerminalNode NONAME() { return getToken(CobolPreprocessorParser.NONAME, 0); }
		public TerminalNode NONUMBER() { return getToken(CobolPreprocessorParser.NONUMBER, 0); }
		public TerminalNode NONUM() { return getToken(CobolPreprocessorParser.NONUM, 0); }
		public TerminalNode NOOBJECT() { return getToken(CobolPreprocessorParser.NOOBJECT, 0); }
		public TerminalNode NOOBJ() { return getToken(CobolPreprocessorParser.NOOBJ, 0); }
		public TerminalNode NOOFFSET() { return getToken(CobolPreprocessorParser.NOOFFSET, 0); }
		public TerminalNode NOOFF() { return getToken(CobolPreprocessorParser.NOOFF, 0); }
		public TerminalNode NOOPSEQUENCE() { return getToken(CobolPreprocessorParser.NOOPSEQUENCE, 0); }
		public TerminalNode NOOPTIMIZE() { return getToken(CobolPreprocessorParser.NOOPTIMIZE, 0); }
		public TerminalNode NOOPT() { return getToken(CobolPreprocessorParser.NOOPT, 0); }
		public TerminalNode NOOPTIONS() { return getToken(CobolPreprocessorParser.NOOPTIONS, 0); }
		public TerminalNode NOP() { return getToken(CobolPreprocessorParser.NOP, 0); }
		public TerminalNode NOPROLOG() { return getToken(CobolPreprocessorParser.NOPROLOG, 0); }
		public TerminalNode NORENT() { return getToken(CobolPreprocessorParser.NORENT, 0); }
		public TerminalNode NOSEQUENCE() { return getToken(CobolPreprocessorParser.NOSEQUENCE, 0); }
		public TerminalNode NOSEQ() { return getToken(CobolPreprocessorParser.NOSEQ, 0); }
		public TerminalNode NOSOURCE() { return getToken(CobolPreprocessorParser.NOSOURCE, 0); }
		public TerminalNode NOS() { return getToken(CobolPreprocessorParser.NOS, 0); }
		public TerminalNode NOSPIE() { return getToken(CobolPreprocessorParser.NOSPIE, 0); }
		public TerminalNode NOSQL() { return getToken(CobolPreprocessorParser.NOSQL, 0); }
		public TerminalNode NOSQLCCSID() { return getToken(CobolPreprocessorParser.NOSQLCCSID, 0); }
		public TerminalNode NOSQLC() { return getToken(CobolPreprocessorParser.NOSQLC, 0); }
		public TerminalNode NOSSRANGE() { return getToken(CobolPreprocessorParser.NOSSRANGE, 0); }
		public TerminalNode NOSSR() { return getToken(CobolPreprocessorParser.NOSSR, 0); }
		public TerminalNode NOSTDTRUNC() { return getToken(CobolPreprocessorParser.NOSTDTRUNC, 0); }
		public TerminalNode NOTERMINAL() { return getToken(CobolPreprocessorParser.NOTERMINAL, 0); }
		public TerminalNode NOTERM() { return getToken(CobolPreprocessorParser.NOTERM, 0); }
		public TerminalNode NOTEST() { return getToken(CobolPreprocessorParser.NOTEST, 0); }
		public TerminalNode NOTHREAD() { return getToken(CobolPreprocessorParser.NOTHREAD, 0); }
		public TerminalNode NOVBREF() { return getToken(CobolPreprocessorParser.NOVBREF, 0); }
		public TerminalNode NOWORD() { return getToken(CobolPreprocessorParser.NOWORD, 0); }
		public TerminalNode NOWD() { return getToken(CobolPreprocessorParser.NOWD, 0); }
		public TerminalNode NSEQ() { return getToken(CobolPreprocessorParser.NSEQ, 0); }
		public TerminalNode NSYMBOL() { return getToken(CobolPreprocessorParser.NSYMBOL, 0); }
		public TerminalNode NS() { return getToken(CobolPreprocessorParser.NS, 0); }
		public TerminalNode NATIONAL() { return getToken(CobolPreprocessorParser.NATIONAL, 0); }
		public TerminalNode NAT() { return getToken(CobolPreprocessorParser.NAT, 0); }
		public TerminalNode NOXREF() { return getToken(CobolPreprocessorParser.NOXREF, 0); }
		public TerminalNode NOX() { return getToken(CobolPreprocessorParser.NOX, 0); }
		public TerminalNode NOZWB() { return getToken(CobolPreprocessorParser.NOZWB, 0); }
		public TerminalNode NUMBER() { return getToken(CobolPreprocessorParser.NUMBER, 0); }
		public TerminalNode NUM() { return getToken(CobolPreprocessorParser.NUM, 0); }
		public TerminalNode NUMPROC() { return getToken(CobolPreprocessorParser.NUMPROC, 0); }
		public TerminalNode MIG() { return getToken(CobolPreprocessorParser.MIG, 0); }
		public TerminalNode NOPFD() { return getToken(CobolPreprocessorParser.NOPFD, 0); }
		public TerminalNode PFD() { return getToken(CobolPreprocessorParser.PFD, 0); }
		public TerminalNode OBJECT() { return getToken(CobolPreprocessorParser.OBJECT, 0); }
		public TerminalNode OBJ() { return getToken(CobolPreprocessorParser.OBJ, 0); }
		public TerminalNode OFFSET() { return getToken(CobolPreprocessorParser.OFFSET, 0); }
		public TerminalNode OFF() { return getToken(CobolPreprocessorParser.OFF, 0); }
		public TerminalNode OPMARGINS() { return getToken(CobolPreprocessorParser.OPMARGINS, 0); }
		public TerminalNode OPSEQUENCE() { return getToken(CobolPreprocessorParser.OPSEQUENCE, 0); }
		public TerminalNode OPTIMIZE() { return getToken(CobolPreprocessorParser.OPTIMIZE, 0); }
		public TerminalNode OPT() { return getToken(CobolPreprocessorParser.OPT, 0); }
		public TerminalNode FULL() { return getToken(CobolPreprocessorParser.FULL, 0); }
		public TerminalNode STD() { return getToken(CobolPreprocessorParser.STD, 0); }
		public TerminalNode OPTFILE() { return getToken(CobolPreprocessorParser.OPTFILE, 0); }
		public TerminalNode OPTIONS() { return getToken(CobolPreprocessorParser.OPTIONS, 0); }
		public TerminalNode OP() { return getToken(CobolPreprocessorParser.OP, 0); }
		public CobolWordContext cobolWord() {
			return getRuleContext(CobolWordContext.class,0);
		}
		public TerminalNode OUTDD() { return getToken(CobolPreprocessorParser.OUTDD, 0); }
		public TerminalNode OUT() { return getToken(CobolPreprocessorParser.OUT, 0); }
		public TerminalNode PGMNAME() { return getToken(CobolPreprocessorParser.PGMNAME, 0); }
		public TerminalNode PGMN() { return getToken(CobolPreprocessorParser.PGMN, 0); }
		public TerminalNode CO() { return getToken(CobolPreprocessorParser.CO, 0); }
		public TerminalNode LM() { return getToken(CobolPreprocessorParser.LM, 0); }
		public TerminalNode LONGMIXED() { return getToken(CobolPreprocessorParser.LONGMIXED, 0); }
		public TerminalNode LONGUPPER() { return getToken(CobolPreprocessorParser.LONGUPPER, 0); }
		public TerminalNode LU() { return getToken(CobolPreprocessorParser.LU, 0); }
		public TerminalNode MIXED() { return getToken(CobolPreprocessorParser.MIXED, 0); }
		public TerminalNode UPPER() { return getToken(CobolPreprocessorParser.UPPER, 0); }
		public TerminalNode PROLOG() { return getToken(CobolPreprocessorParser.PROLOG, 0); }
		public TerminalNode QUOTE() { return getToken(CobolPreprocessorParser.QUOTE, 0); }
		public TerminalNode Q_CHAR() { return getToken(CobolPreprocessorParser.Q_CHAR, 0); }
		public TerminalNode RENT() { return getToken(CobolPreprocessorParser.RENT, 0); }
		public TerminalNode RMODE() { return getToken(CobolPreprocessorParser.RMODE, 0); }
		public TerminalNode ANY() { return getToken(CobolPreprocessorParser.ANY, 0); }
		public TerminalNode AUTO() { return getToken(CobolPreprocessorParser.AUTO, 0); }
		public TerminalNode SEQUENCE() { return getToken(CobolPreprocessorParser.SEQUENCE, 0); }
		public TerminalNode SEQ() { return getToken(CobolPreprocessorParser.SEQ, 0); }
		public TerminalNode SIZE() { return getToken(CobolPreprocessorParser.SIZE, 0); }
		public TerminalNode SZ() { return getToken(CobolPreprocessorParser.SZ, 0); }
		public TerminalNode MAX() { return getToken(CobolPreprocessorParser.MAX, 0); }
		public TerminalNode SOURCE() { return getToken(CobolPreprocessorParser.SOURCE, 0); }
		public TerminalNode SP() { return getToken(CobolPreprocessorParser.SP, 0); }
		public TerminalNode SPACE() { return getToken(CobolPreprocessorParser.SPACE, 0); }
		public TerminalNode SPIE() { return getToken(CobolPreprocessorParser.SPIE, 0); }
		public TerminalNode SQL() { return getToken(CobolPreprocessorParser.SQL, 0); }
		public TerminalNode SQLCCSID() { return getToken(CobolPreprocessorParser.SQLCCSID, 0); }
		public TerminalNode SQLC() { return getToken(CobolPreprocessorParser.SQLC, 0); }
		public TerminalNode SSRANGE() { return getToken(CobolPreprocessorParser.SSRANGE, 0); }
		public TerminalNode SSR() { return getToken(CobolPreprocessorParser.SSR, 0); }
		public TerminalNode SYSEIB() { return getToken(CobolPreprocessorParser.SYSEIB, 0); }
		public TerminalNode TERMINAL() { return getToken(CobolPreprocessorParser.TERMINAL, 0); }
		public TerminalNode TERM() { return getToken(CobolPreprocessorParser.TERM, 0); }
		public TerminalNode TEST() { return getToken(CobolPreprocessorParser.TEST, 0); }
		public TerminalNode HOOK() { return getToken(CobolPreprocessorParser.HOOK, 0); }
		public TerminalNode NOHOOK() { return getToken(CobolPreprocessorParser.NOHOOK, 0); }
		public TerminalNode SEP() { return getToken(CobolPreprocessorParser.SEP, 0); }
		public TerminalNode SEPARATE() { return getToken(CobolPreprocessorParser.SEPARATE, 0); }
		public TerminalNode NOSEP() { return getToken(CobolPreprocessorParser.NOSEP, 0); }
		public TerminalNode NOSEPARATE() { return getToken(CobolPreprocessorParser.NOSEPARATE, 0); }
		public TerminalNode EJPD() { return getToken(CobolPreprocessorParser.EJPD, 0); }
		public TerminalNode NOEJPD() { return getToken(CobolPreprocessorParser.NOEJPD, 0); }
		public TerminalNode THREAD() { return getToken(CobolPreprocessorParser.THREAD, 0); }
		public TerminalNode TRUNC() { return getToken(CobolPreprocessorParser.TRUNC, 0); }
		public TerminalNode BIN() { return getToken(CobolPreprocessorParser.BIN, 0); }
		public TerminalNode VBREF() { return getToken(CobolPreprocessorParser.VBREF, 0); }
		public TerminalNode WORD() { return getToken(CobolPreprocessorParser.WORD, 0); }
		public TerminalNode WD() { return getToken(CobolPreprocessorParser.WD, 0); }
		public TerminalNode XMLPARSE() { return getToken(CobolPreprocessorParser.XMLPARSE, 0); }
		public TerminalNode XP() { return getToken(CobolPreprocessorParser.XP, 0); }
		public TerminalNode XMLSS() { return getToken(CobolPreprocessorParser.XMLSS, 0); }
		public TerminalNode X_CHAR() { return getToken(CobolPreprocessorParser.X_CHAR, 0); }
		public TerminalNode XREF() { return getToken(CobolPreprocessorParser.XREF, 0); }
		public TerminalNode SHORT() { return getToken(CobolPreprocessorParser.SHORT, 0); }
		public TerminalNode YEARWINDOW() { return getToken(CobolPreprocessorParser.YEARWINDOW, 0); }
		public TerminalNode YW() { return getToken(CobolPreprocessorParser.YW, 0); }
		public TerminalNode ZWB() { return getToken(CobolPreprocessorParser.ZWB, 0); }
		public CompilerOptionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_compilerOption; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).enterCompilerOption(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).exitCompilerOption(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof CobolPreprocessorVisitor) return ((CobolPreprocessorVisitor<? extends T>) visitor).visitCompilerOption(this);
			return visitor.visitChildren(this);
		}
	}

	public final CompilerOptionContext compilerOption() throws RecognitionException {
		CompilerOptionContext _localctx = new CompilerOptionContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_compilerOption);
		int _la;
		try {
			setState(453);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,32,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(111);
				match(ADATA);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(112);
				match(ADV);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(113);
				match(APOST);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(114);
				_la = _input.LA(1);
				if ( !(_la==AR || _la==ARITH) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(115);
				match(LPARENCHAR);
				setState(116);
				_la = _input.LA(1);
				if ( !(_la==COMPAT || _la==EXTEND || _la==C_CHAR || _la==E_CHAR) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(117);
				match(RPARENCHAR);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(118);
				match(AWO);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(119);
				match(BLOCK0);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(120);
				_la = _input.LA(1);
				if ( !(_la==BUF || _la==BUFSIZE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(121);
				match(LPARENCHAR);
				setState(122);
				literal();
				setState(123);
				match(RPARENCHAR);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(125);
				match(CBLCARD);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(126);
				match(CICS);
				setState(131);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
				case 1:
					{
					setState(127);
					match(LPARENCHAR);
					setState(128);
					literal();
					setState(129);
					match(RPARENCHAR);
					}
					break;
				}
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(133);
				match(COBOL2);
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(134);
				match(COBOL3);
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(135);
				_la = _input.LA(1);
				if ( !(_la==CODEPAGE || _la==CP) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(136);
				match(LPARENCHAR);
				setState(137);
				literal();
				setState(138);
				match(RPARENCHAR);
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 13);
				{
				setState(140);
				_la = _input.LA(1);
				if ( !(_la==COMPILE || _la==C_CHAR) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 14:
				enterOuterAlt(_localctx, 14);
				{
				setState(141);
				match(CPP);
				}
				break;
			case 15:
				enterOuterAlt(_localctx, 15);
				{
				setState(142);
				match(CPSM);
				}
				break;
			case 16:
				enterOuterAlt(_localctx, 16);
				{
				setState(143);
				_la = _input.LA(1);
				if ( !(_la==CURR || _la==CURRENCY) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(144);
				match(LPARENCHAR);
				setState(145);
				literal();
				setState(146);
				match(RPARENCHAR);
				}
				break;
			case 17:
				enterOuterAlt(_localctx, 17);
				{
				setState(148);
				match(DATA);
				setState(149);
				match(LPARENCHAR);
				setState(150);
				literal();
				setState(151);
				match(RPARENCHAR);
				}
				break;
			case 18:
				enterOuterAlt(_localctx, 18);
				{
				setState(153);
				_la = _input.LA(1);
				if ( !(_la==DATEPROC || _la==DP) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(165);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,11,_ctx) ) {
				case 1:
					{
					setState(154);
					match(LPARENCHAR);
					setState(156);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==FLAG || _la==NOFLAG) {
						{
						setState(155);
						_la = _input.LA(1);
						if ( !(_la==FLAG || _la==NOFLAG) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						}
					}

					setState(159);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==COMMACHAR) {
						{
						setState(158);
						match(COMMACHAR);
						}
					}

					setState(162);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==NOTRIG || _la==TRIG) {
						{
						setState(161);
						_la = _input.LA(1);
						if ( !(_la==NOTRIG || _la==TRIG) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						}
					}

					setState(164);
					match(RPARENCHAR);
					}
					break;
				}
				}
				break;
			case 19:
				enterOuterAlt(_localctx, 19);
				{
				setState(167);
				match(DBCS);
				}
				break;
			case 20:
				enterOuterAlt(_localctx, 20);
				{
				setState(168);
				_la = _input.LA(1);
				if ( !(_la==DECK || _la==D_CHAR) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 21:
				enterOuterAlt(_localctx, 21);
				{
				setState(169);
				match(DEBUG);
				}
				break;
			case 22:
				enterOuterAlt(_localctx, 22);
				{
				setState(170);
				_la = _input.LA(1);
				if ( !(_la==DIAGTRUNC || _la==DTR) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 23:
				enterOuterAlt(_localctx, 23);
				{
				setState(171);
				match(DLL);
				}
				break;
			case 24:
				enterOuterAlt(_localctx, 24);
				{
				setState(172);
				_la = _input.LA(1);
				if ( !(_la==DU || _la==DUMP) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 25:
				enterOuterAlt(_localctx, 25);
				{
				setState(173);
				_la = _input.LA(1);
				if ( !(_la==DYN || _la==DYNAM) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 26:
				enterOuterAlt(_localctx, 26);
				{
				setState(174);
				match(EDF);
				}
				break;
			case 27:
				enterOuterAlt(_localctx, 27);
				{
				setState(175);
				match(EPILOG);
				}
				break;
			case 28:
				enterOuterAlt(_localctx, 28);
				{
				setState(176);
				match(EXIT);
				}
				break;
			case 29:
				enterOuterAlt(_localctx, 29);
				{
				setState(177);
				_la = _input.LA(1);
				if ( !(_la==EXP || _la==EXPORTALL) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 30:
				enterOuterAlt(_localctx, 30);
				{
				setState(178);
				_la = _input.LA(1);
				if ( !(_la==FASTSRT || _la==FSRT) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 31:
				enterOuterAlt(_localctx, 31);
				{
				setState(179);
				match(FEPI);
				}
				break;
			case 32:
				enterOuterAlt(_localctx, 32);
				{
				setState(180);
				_la = _input.LA(1);
				if ( !(_la==FLAG || _la==F_CHAR) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(181);
				match(LPARENCHAR);
				setState(182);
				_la = _input.LA(1);
				if ( !(((_la - 271) & ~0x3f) == 0 && ((1L << (_la - 271)) & 905L) != 0) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(185);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMACHAR) {
					{
					setState(183);
					match(COMMACHAR);
					setState(184);
					_la = _input.LA(1);
					if ( !(((_la - 271) & ~0x3f) == 0 && ((1L << (_la - 271)) & 905L) != 0) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
				}

				setState(187);
				match(RPARENCHAR);
				}
				break;
			case 33:
				enterOuterAlt(_localctx, 33);
				{
				setState(188);
				match(FLAGSTD);
				setState(189);
				match(LPARENCHAR);
				setState(190);
				_la = _input.LA(1);
				if ( !(((_la - 273) & ~0x3f) == 0 && ((1L << (_la - 273)) & 7L) != 0) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(193);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMACHAR) {
					{
					setState(191);
					match(COMMACHAR);
					setState(192);
					_la = _input.LA(1);
					if ( !(_la==DD || _la==NN || (((_la - 242) & ~0x3f) == 0 && ((1L << (_la - 242)) & 86167781377L) != 0)) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
				}

				setState(195);
				match(RPARENCHAR);
				}
				break;
			case 34:
				enterOuterAlt(_localctx, 34);
				{
				setState(196);
				match(GDS);
				}
				break;
			case 35:
				enterOuterAlt(_localctx, 35);
				{
				setState(197);
				match(GRAPHIC);
				}
				break;
			case 36:
				enterOuterAlt(_localctx, 36);
				{
				setState(198);
				match(INTDATE);
				setState(199);
				match(LPARENCHAR);
				setState(200);
				_la = _input.LA(1);
				if ( !(_la==ANSI || _la==LILIAN) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(201);
				match(RPARENCHAR);
				}
				break;
			case 37:
				enterOuterAlt(_localctx, 37);
				{
				setState(202);
				_la = _input.LA(1);
				if ( !(_la==LANG || _la==LANGUAGE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(203);
				match(LPARENCHAR);
				setState(204);
				_la = _input.LA(1);
				if ( !((((_la - 29) & ~0x3f) == 0 && ((1L << (_la - 29)) & 61572657446913L) != 0) || _la==UE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(205);
				match(RPARENCHAR);
				}
				break;
			case 38:
				enterOuterAlt(_localctx, 38);
				{
				setState(206);
				match(LEASM);
				}
				break;
			case 39:
				enterOuterAlt(_localctx, 39);
				{
				setState(207);
				match(LENGTH);
				}
				break;
			case 40:
				enterOuterAlt(_localctx, 40);
				{
				setState(208);
				match(LIB);
				}
				break;
			case 41:
				enterOuterAlt(_localctx, 41);
				{
				setState(209);
				match(LIN);
				}
				break;
			case 42:
				enterOuterAlt(_localctx, 42);
				{
				setState(210);
				_la = _input.LA(1);
				if ( !(_la==LC || _la==LINECOUNT) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(211);
				match(LPARENCHAR);
				setState(212);
				literal();
				setState(213);
				match(RPARENCHAR);
				}
				break;
			case 43:
				enterOuterAlt(_localctx, 43);
				{
				setState(215);
				match(LINKAGE);
				}
				break;
			case 44:
				enterOuterAlt(_localctx, 44);
				{
				setState(216);
				match(LIST);
				}
				break;
			case 45:
				enterOuterAlt(_localctx, 45);
				{
				setState(217);
				match(MAP);
				}
				break;
			case 46:
				enterOuterAlt(_localctx, 46);
				{
				setState(218);
				match(MARGINS);
				setState(219);
				match(LPARENCHAR);
				setState(220);
				literal();
				setState(221);
				match(COMMACHAR);
				setState(222);
				literal();
				setState(225);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMACHAR) {
					{
					setState(223);
					match(COMMACHAR);
					setState(224);
					literal();
					}
				}

				setState(227);
				match(RPARENCHAR);
				}
				break;
			case 47:
				enterOuterAlt(_localctx, 47);
				{
				setState(229);
				_la = _input.LA(1);
				if ( !(_la==MD || _la==MDECK) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(233);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
				case 1:
					{
					setState(230);
					match(LPARENCHAR);
					setState(231);
					_la = _input.LA(1);
					if ( !(_la==COMPILE || _la==NOC || _la==NOCOMPILE || _la==C_CHAR) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(232);
					match(RPARENCHAR);
					}
					break;
				}
				}
				break;
			case 48:
				enterOuterAlt(_localctx, 48);
				{
				setState(235);
				match(NAME);
				setState(239);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,16,_ctx) ) {
				case 1:
					{
					setState(236);
					match(LPARENCHAR);
					setState(237);
					_la = _input.LA(1);
					if ( !(_la==ALIAS || _la==NOALIAS) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(238);
					match(RPARENCHAR);
					}
					break;
				}
				}
				break;
			case 49:
				enterOuterAlt(_localctx, 49);
				{
				setState(241);
				match(NATLANG);
				setState(242);
				match(LPARENCHAR);
				setState(243);
				_la = _input.LA(1);
				if ( !(((_la - 29) & ~0x3f) == 0 && ((1L << (_la - 29)) & 35184374185985L) != 0) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(244);
				match(RPARENCHAR);
				}
				break;
			case 50:
				enterOuterAlt(_localctx, 50);
				{
				setState(245);
				match(NOADATA);
				}
				break;
			case 51:
				enterOuterAlt(_localctx, 51);
				{
				setState(246);
				match(NOADV);
				}
				break;
			case 52:
				enterOuterAlt(_localctx, 52);
				{
				setState(247);
				match(NOAWO);
				}
				break;
			case 53:
				enterOuterAlt(_localctx, 53);
				{
				setState(248);
				match(NOBLOCK0);
				}
				break;
			case 54:
				enterOuterAlt(_localctx, 54);
				{
				setState(249);
				match(NOCBLCARD);
				}
				break;
			case 55:
				enterOuterAlt(_localctx, 55);
				{
				setState(250);
				match(NOCICS);
				}
				break;
			case 56:
				enterOuterAlt(_localctx, 56);
				{
				setState(251);
				match(NOCMPR2);
				}
				break;
			case 57:
				enterOuterAlt(_localctx, 57);
				{
				setState(252);
				_la = _input.LA(1);
				if ( !(_la==NOC || _la==NOCOMPILE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(256);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,17,_ctx) ) {
				case 1:
					{
					setState(253);
					match(LPARENCHAR);
					setState(254);
					_la = _input.LA(1);
					if ( !(((_la - 271) & ~0x3f) == 0 && ((1L << (_la - 271)) & 641L) != 0) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(255);
					match(RPARENCHAR);
					}
					break;
				}
				}
				break;
			case 58:
				enterOuterAlt(_localctx, 58);
				{
				setState(258);
				match(NOCPSM);
				}
				break;
			case 59:
				enterOuterAlt(_localctx, 59);
				{
				setState(259);
				_la = _input.LA(1);
				if ( !(_la==NOCURR || _la==NOCURRENCY) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 60:
				enterOuterAlt(_localctx, 60);
				{
				setState(260);
				_la = _input.LA(1);
				if ( !(_la==NODATEPROC || _la==NODP) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 61:
				enterOuterAlt(_localctx, 61);
				{
				setState(261);
				match(NODBCS);
				}
				break;
			case 62:
				enterOuterAlt(_localctx, 62);
				{
				setState(262);
				match(NODEBUG);
				}
				break;
			case 63:
				enterOuterAlt(_localctx, 63);
				{
				setState(263);
				_la = _input.LA(1);
				if ( !(_la==NOD || _la==NODECK) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 64:
				enterOuterAlt(_localctx, 64);
				{
				setState(264);
				match(NODLL);
				}
				break;
			case 65:
				enterOuterAlt(_localctx, 65);
				{
				setState(265);
				match(NODE);
				}
				break;
			case 66:
				enterOuterAlt(_localctx, 66);
				{
				setState(266);
				_la = _input.LA(1);
				if ( !(_la==NODU || _la==NODUMP) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 67:
				enterOuterAlt(_localctx, 67);
				{
				setState(267);
				_la = _input.LA(1);
				if ( !(_la==NODIAGTRUNC || _la==NODTR) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 68:
				enterOuterAlt(_localctx, 68);
				{
				setState(268);
				_la = _input.LA(1);
				if ( !(_la==NODYN || _la==NODYNAM) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 69:
				enterOuterAlt(_localctx, 69);
				{
				setState(269);
				match(NOEDF);
				}
				break;
			case 70:
				enterOuterAlt(_localctx, 70);
				{
				setState(270);
				match(NOEPILOG);
				}
				break;
			case 71:
				enterOuterAlt(_localctx, 71);
				{
				setState(271);
				match(NOEXIT);
				}
				break;
			case 72:
				enterOuterAlt(_localctx, 72);
				{
				setState(272);
				_la = _input.LA(1);
				if ( !(_la==NOEXP || _la==NOEXPORTALL) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 73:
				enterOuterAlt(_localctx, 73);
				{
				setState(273);
				_la = _input.LA(1);
				if ( !(_la==NOFASTSRT || _la==NOFSRT) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 74:
				enterOuterAlt(_localctx, 74);
				{
				setState(274);
				match(NOFEPI);
				}
				break;
			case 75:
				enterOuterAlt(_localctx, 75);
				{
				setState(275);
				_la = _input.LA(1);
				if ( !(_la==NOF || _la==NOFLAG) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 76:
				enterOuterAlt(_localctx, 76);
				{
				setState(276);
				match(NOFLAGMIG);
				}
				break;
			case 77:
				enterOuterAlt(_localctx, 77);
				{
				setState(277);
				match(NOFLAGSTD);
				}
				break;
			case 78:
				enterOuterAlt(_localctx, 78);
				{
				setState(278);
				match(NOGRAPHIC);
				}
				break;
			case 79:
				enterOuterAlt(_localctx, 79);
				{
				setState(279);
				match(NOLENGTH);
				}
				break;
			case 80:
				enterOuterAlt(_localctx, 80);
				{
				setState(280);
				match(NOLIB);
				}
				break;
			case 81:
				enterOuterAlt(_localctx, 81);
				{
				setState(281);
				match(NOLINKAGE);
				}
				break;
			case 82:
				enterOuterAlt(_localctx, 82);
				{
				setState(282);
				match(NOLIST);
				}
				break;
			case 83:
				enterOuterAlt(_localctx, 83);
				{
				setState(283);
				match(NOMAP);
				}
				break;
			case 84:
				enterOuterAlt(_localctx, 84);
				{
				setState(284);
				_la = _input.LA(1);
				if ( !(_la==NOMD || _la==NOMDECK) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 85:
				enterOuterAlt(_localctx, 85);
				{
				setState(285);
				match(NONAME);
				}
				break;
			case 86:
				enterOuterAlt(_localctx, 86);
				{
				setState(286);
				_la = _input.LA(1);
				if ( !(_la==NONUM || _la==NONUMBER) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 87:
				enterOuterAlt(_localctx, 87);
				{
				setState(287);
				_la = _input.LA(1);
				if ( !(_la==NOOBJ || _la==NOOBJECT) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 88:
				enterOuterAlt(_localctx, 88);
				{
				setState(288);
				_la = _input.LA(1);
				if ( !(_la==NOOFF || _la==NOOFFSET) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 89:
				enterOuterAlt(_localctx, 89);
				{
				setState(289);
				match(NOOPSEQUENCE);
				}
				break;
			case 90:
				enterOuterAlt(_localctx, 90);
				{
				setState(290);
				_la = _input.LA(1);
				if ( !(_la==NOOPT || _la==NOOPTIMIZE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 91:
				enterOuterAlt(_localctx, 91);
				{
				setState(291);
				match(NOOPTIONS);
				}
				break;
			case 92:
				enterOuterAlt(_localctx, 92);
				{
				setState(292);
				match(NOP);
				}
				break;
			case 93:
				enterOuterAlt(_localctx, 93);
				{
				setState(293);
				match(NOPROLOG);
				}
				break;
			case 94:
				enterOuterAlt(_localctx, 94);
				{
				setState(294);
				match(NORENT);
				}
				break;
			case 95:
				enterOuterAlt(_localctx, 95);
				{
				setState(295);
				_la = _input.LA(1);
				if ( !(_la==NOSEQ || _la==NOSEQUENCE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 96:
				enterOuterAlt(_localctx, 96);
				{
				setState(296);
				_la = _input.LA(1);
				if ( !(_la==NOS || _la==NOSOURCE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 97:
				enterOuterAlt(_localctx, 97);
				{
				setState(297);
				match(NOSPIE);
				}
				break;
			case 98:
				enterOuterAlt(_localctx, 98);
				{
				setState(298);
				match(NOSQL);
				}
				break;
			case 99:
				enterOuterAlt(_localctx, 99);
				{
				setState(299);
				_la = _input.LA(1);
				if ( !(_la==NOSQLC || _la==NOSQLCCSID) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 100:
				enterOuterAlt(_localctx, 100);
				{
				setState(300);
				_la = _input.LA(1);
				if ( !(_la==NOSSR || _la==NOSSRANGE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 101:
				enterOuterAlt(_localctx, 101);
				{
				setState(301);
				match(NOSTDTRUNC);
				}
				break;
			case 102:
				enterOuterAlt(_localctx, 102);
				{
				setState(302);
				_la = _input.LA(1);
				if ( !(_la==NOTERM || _la==NOTERMINAL) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 103:
				enterOuterAlt(_localctx, 103);
				{
				setState(303);
				match(NOTEST);
				}
				break;
			case 104:
				enterOuterAlt(_localctx, 104);
				{
				setState(304);
				match(NOTHREAD);
				}
				break;
			case 105:
				enterOuterAlt(_localctx, 105);
				{
				setState(305);
				match(NOVBREF);
				}
				break;
			case 106:
				enterOuterAlt(_localctx, 106);
				{
				setState(306);
				_la = _input.LA(1);
				if ( !(_la==NOWD || _la==NOWORD) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 107:
				enterOuterAlt(_localctx, 107);
				{
				setState(307);
				match(NSEQ);
				}
				break;
			case 108:
				enterOuterAlt(_localctx, 108);
				{
				setState(308);
				_la = _input.LA(1);
				if ( !(_la==NS || _la==NSYMBOL) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(309);
				match(LPARENCHAR);
				setState(310);
				_la = _input.LA(1);
				if ( !(_la==DBCS || _la==NAT || _la==NATIONAL) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(311);
				match(RPARENCHAR);
				}
				break;
			case 109:
				enterOuterAlt(_localctx, 109);
				{
				setState(312);
				match(NOVBREF);
				}
				break;
			case 110:
				enterOuterAlt(_localctx, 110);
				{
				setState(313);
				_la = _input.LA(1);
				if ( !(_la==NOX || _la==NOXREF) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 111:
				enterOuterAlt(_localctx, 111);
				{
				setState(314);
				match(NOZWB);
				}
				break;
			case 112:
				enterOuterAlt(_localctx, 112);
				{
				setState(315);
				_la = _input.LA(1);
				if ( !(_la==NUM || _la==NUMBER) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 113:
				enterOuterAlt(_localctx, 113);
				{
				setState(316);
				match(NUMPROC);
				setState(317);
				match(LPARENCHAR);
				setState(318);
				_la = _input.LA(1);
				if ( !(_la==MIG || _la==NOPFD || _la==PFD) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(319);
				match(RPARENCHAR);
				}
				break;
			case 114:
				enterOuterAlt(_localctx, 114);
				{
				setState(320);
				_la = _input.LA(1);
				if ( !(_la==OBJ || _la==OBJECT) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 115:
				enterOuterAlt(_localctx, 115);
				{
				setState(321);
				_la = _input.LA(1);
				if ( !(_la==OFF || _la==OFFSET) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 116:
				enterOuterAlt(_localctx, 116);
				{
				setState(322);
				match(OPMARGINS);
				setState(323);
				match(LPARENCHAR);
				setState(324);
				literal();
				setState(325);
				match(COMMACHAR);
				setState(326);
				literal();
				setState(329);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMACHAR) {
					{
					setState(327);
					match(COMMACHAR);
					setState(328);
					literal();
					}
				}

				setState(331);
				match(RPARENCHAR);
				}
				break;
			case 117:
				enterOuterAlt(_localctx, 117);
				{
				setState(333);
				match(OPSEQUENCE);
				setState(334);
				match(LPARENCHAR);
				setState(335);
				literal();
				setState(336);
				match(COMMACHAR);
				setState(337);
				literal();
				setState(338);
				match(RPARENCHAR);
				}
				break;
			case 118:
				enterOuterAlt(_localctx, 118);
				{
				setState(340);
				_la = _input.LA(1);
				if ( !(_la==OPT || _la==OPTIMIZE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(344);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,19,_ctx) ) {
				case 1:
					{
					setState(341);
					match(LPARENCHAR);
					setState(342);
					_la = _input.LA(1);
					if ( !(_la==FULL || _la==STD) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(343);
					match(RPARENCHAR);
					}
					break;
				}
				}
				break;
			case 119:
				enterOuterAlt(_localctx, 119);
				{
				setState(346);
				match(OPTFILE);
				}
				break;
			case 120:
				enterOuterAlt(_localctx, 120);
				{
				setState(347);
				match(OPTIONS);
				}
				break;
			case 121:
				enterOuterAlt(_localctx, 121);
				{
				setState(348);
				match(OP);
				}
				break;
			case 122:
				enterOuterAlt(_localctx, 122);
				{
				setState(349);
				_la = _input.LA(1);
				if ( !(_la==OUT || _la==OUTDD) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(350);
				match(LPARENCHAR);
				setState(351);
				cobolWord();
				setState(352);
				match(RPARENCHAR);
				}
				break;
			case 123:
				enterOuterAlt(_localctx, 123);
				{
				setState(354);
				_la = _input.LA(1);
				if ( !(_la==PGMN || _la==PGMNAME) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(355);
				match(LPARENCHAR);
				setState(356);
				_la = _input.LA(1);
				if ( !(_la==CO || _la==COMPAT || (((_la - 86) & ~0x3f) == 0 && ((1L << (_la - 86)) & 2071L) != 0) || (((_la - 257) & ~0x3f) == 0 && ((1L << (_la - 257)) & 4456449L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(357);
				match(RPARENCHAR);
				}
				break;
			case 124:
				enterOuterAlt(_localctx, 124);
				{
				setState(358);
				match(PROLOG);
				}
				break;
			case 125:
				enterOuterAlt(_localctx, 125);
				{
				setState(359);
				_la = _input.LA(1);
				if ( !(_la==QUOTE || _la==Q_CHAR) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 126:
				enterOuterAlt(_localctx, 126);
				{
				setState(360);
				match(RENT);
				}
				break;
			case 127:
				enterOuterAlt(_localctx, 127);
				{
				setState(361);
				match(RMODE);
				setState(362);
				match(LPARENCHAR);
				setState(366);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case ANY:
					{
					setState(363);
					match(ANY);
					}
					break;
				case AUTO:
					{
					setState(364);
					match(AUTO);
					}
					break;
				case NONNUMERICLITERAL:
				case INTEGERLITERAL:
				case NUMERICLITERAL:
					{
					setState(365);
					literal();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(368);
				match(RPARENCHAR);
				}
				break;
			case 128:
				enterOuterAlt(_localctx, 128);
				{
				setState(369);
				_la = _input.LA(1);
				if ( !(_la==SEQ || _la==SEQUENCE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(376);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,21,_ctx) ) {
				case 1:
					{
					setState(370);
					match(LPARENCHAR);
					setState(371);
					literal();
					setState(372);
					match(COMMACHAR);
					setState(373);
					literal();
					setState(374);
					match(RPARENCHAR);
					}
					break;
				}
				}
				break;
			case 129:
				enterOuterAlt(_localctx, 129);
				{
				setState(378);
				_la = _input.LA(1);
				if ( !(_la==SIZE || _la==SZ) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(379);
				match(LPARENCHAR);
				setState(382);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case MAX:
					{
					setState(380);
					match(MAX);
					}
					break;
				case NONNUMERICLITERAL:
				case INTEGERLITERAL:
				case NUMERICLITERAL:
					{
					setState(381);
					literal();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(384);
				match(RPARENCHAR);
				}
				break;
			case 130:
				enterOuterAlt(_localctx, 130);
				{
				setState(385);
				_la = _input.LA(1);
				if ( !(_la==SOURCE || _la==S_CHAR) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 131:
				enterOuterAlt(_localctx, 131);
				{
				setState(386);
				match(SP);
				}
				break;
			case 132:
				enterOuterAlt(_localctx, 132);
				{
				setState(387);
				match(SPACE);
				setState(388);
				match(LPARENCHAR);
				setState(389);
				literal();
				setState(390);
				match(RPARENCHAR);
				}
				break;
			case 133:
				enterOuterAlt(_localctx, 133);
				{
				setState(392);
				match(SPIE);
				}
				break;
			case 134:
				enterOuterAlt(_localctx, 134);
				{
				setState(393);
				match(SQL);
				setState(398);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,23,_ctx) ) {
				case 1:
					{
					setState(394);
					match(LPARENCHAR);
					setState(395);
					literal();
					setState(396);
					match(RPARENCHAR);
					}
					break;
				}
				}
				break;
			case 135:
				enterOuterAlt(_localctx, 135);
				{
				setState(400);
				_la = _input.LA(1);
				if ( !(_la==SQLC || _la==SQLCCSID) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 136:
				enterOuterAlt(_localctx, 136);
				{
				setState(401);
				_la = _input.LA(1);
				if ( !(_la==SSR || _la==SSRANGE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 137:
				enterOuterAlt(_localctx, 137);
				{
				setState(402);
				match(SYSEIB);
				}
				break;
			case 138:
				enterOuterAlt(_localctx, 138);
				{
				setState(403);
				_la = _input.LA(1);
				if ( !(_la==TERM || _la==TERMINAL) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 139:
				enterOuterAlt(_localctx, 139);
				{
				setState(404);
				match(TEST);
				setState(422);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,29,_ctx) ) {
				case 1:
					{
					setState(405);
					match(LPARENCHAR);
					setState(407);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==HOOK || _la==NOHOOK) {
						{
						setState(406);
						_la = _input.LA(1);
						if ( !(_la==HOOK || _la==NOHOOK) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						}
					}

					setState(410);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,25,_ctx) ) {
					case 1:
						{
						setState(409);
						match(COMMACHAR);
						}
						break;
					}
					setState(413);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (((_la - 169) & ~0x3f) == 0 && ((1L << (_la - 169)) & 216172782113783811L) != 0) {
						{
						setState(412);
						_la = _input.LA(1);
						if ( !(((_la - 169) & ~0x3f) == 0 && ((1L << (_la - 169)) & 216172782113783811L) != 0) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						}
					}

					setState(416);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==COMMACHAR) {
						{
						setState(415);
						match(COMMACHAR);
						}
					}

					setState(419);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==EJPD || _la==NOEJPD) {
						{
						setState(418);
						_la = _input.LA(1);
						if ( !(_la==EJPD || _la==NOEJPD) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						}
					}

					setState(421);
					match(RPARENCHAR);
					}
					break;
				}
				}
				break;
			case 140:
				enterOuterAlt(_localctx, 140);
				{
				setState(424);
				match(THREAD);
				}
				break;
			case 141:
				enterOuterAlt(_localctx, 141);
				{
				setState(425);
				match(TRUNC);
				setState(426);
				match(LPARENCHAR);
				setState(427);
				_la = _input.LA(1);
				if ( !(_la==BIN || _la==OPT || _la==STD) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(428);
				match(RPARENCHAR);
				}
				break;
			case 142:
				enterOuterAlt(_localctx, 142);
				{
				setState(429);
				match(VBREF);
				}
				break;
			case 143:
				enterOuterAlt(_localctx, 143);
				{
				setState(430);
				_la = _input.LA(1);
				if ( !(_la==WD || _la==WORD) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(431);
				match(LPARENCHAR);
				setState(432);
				cobolWord();
				setState(433);
				match(RPARENCHAR);
				}
				break;
			case 144:
				enterOuterAlt(_localctx, 144);
				{
				setState(435);
				_la = _input.LA(1);
				if ( !(_la==XMLPARSE || _la==XP) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(436);
				match(LPARENCHAR);
				setState(437);
				_la = _input.LA(1);
				if ( !(_la==COMPAT || (((_la - 262) & ~0x3f) == 0 && ((1L << (_la - 262)) & 524417L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(438);
				match(RPARENCHAR);
				}
				break;
			case 145:
				enterOuterAlt(_localctx, 145);
				{
				setState(439);
				_la = _input.LA(1);
				if ( !(_la==XREF || _la==X_CHAR) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(445);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,31,_ctx) ) {
				case 1:
					{
					setState(440);
					match(LPARENCHAR);
					setState(442);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==FULL || _la==SHORT) {
						{
						setState(441);
						_la = _input.LA(1);
						if ( !(_la==FULL || _la==SHORT) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						}
					}

					setState(444);
					match(RPARENCHAR);
					}
					break;
				}
				}
				break;
			case 146:
				enterOuterAlt(_localctx, 146);
				{
				setState(447);
				_la = _input.LA(1);
				if ( !(_la==YEARWINDOW || _la==YW) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(448);
				match(LPARENCHAR);
				setState(449);
				literal();
				setState(450);
				match(RPARENCHAR);
				}
				break;
			case 147:
				enterOuterAlt(_localctx, 147);
				{
				setState(452);
				match(ZWB);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExecCicsStatementContext extends ParserRuleContext {
		public TerminalNode EXEC() { return getToken(CobolPreprocessorParser.EXEC, 0); }
		public TerminalNode CICS() { return getToken(CobolPreprocessorParser.CICS, 0); }
		public CharDataContext charData() {
			return getRuleContext(CharDataContext.class,0);
		}
		public TerminalNode END_EXEC() { return getToken(CobolPreprocessorParser.END_EXEC, 0); }
		public TerminalNode DOT() { return getToken(CobolPreprocessorParser.DOT, 0); }
		public ExecCicsStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_execCicsStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).enterExecCicsStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).exitExecCicsStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof CobolPreprocessorVisitor) return ((CobolPreprocessorVisitor<? extends T>) visitor).visitExecCicsStatement(this);
			return visitor.visitChildren(this);
		}
	}

	public final ExecCicsStatementContext execCicsStatement() throws RecognitionException {
		ExecCicsStatementContext _localctx = new ExecCicsStatementContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_execCicsStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(455);
			match(EXEC);
			setState(456);
			match(CICS);
			setState(457);
			charData();
			setState(458);
			match(END_EXEC);
			setState(460);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,33,_ctx) ) {
			case 1:
				{
				setState(459);
				match(DOT);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExecSqlStatementContext extends ParserRuleContext {
		public TerminalNode EXEC() { return getToken(CobolPreprocessorParser.EXEC, 0); }
		public TerminalNode SQL() { return getToken(CobolPreprocessorParser.SQL, 0); }
		public CharDataSqlContext charDataSql() {
			return getRuleContext(CharDataSqlContext.class,0);
		}
		public TerminalNode END_EXEC() { return getToken(CobolPreprocessorParser.END_EXEC, 0); }
		public TerminalNode DOT() { return getToken(CobolPreprocessorParser.DOT, 0); }
		public ExecSqlStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_execSqlStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).enterExecSqlStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).exitExecSqlStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof CobolPreprocessorVisitor) return ((CobolPreprocessorVisitor<? extends T>) visitor).visitExecSqlStatement(this);
			return visitor.visitChildren(this);
		}
	}

	public final ExecSqlStatementContext execSqlStatement() throws RecognitionException {
		ExecSqlStatementContext _localctx = new ExecSqlStatementContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_execSqlStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(462);
			match(EXEC);
			setState(463);
			match(SQL);
			setState(464);
			charDataSql();
			setState(465);
			match(END_EXEC);
			setState(467);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,34,_ctx) ) {
			case 1:
				{
				setState(466);
				match(DOT);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExecSqlIncludeStatementContext extends ParserRuleContext {
		public TerminalNode EXEC() { return getToken(CobolPreprocessorParser.EXEC, 0); }
		public TerminalNode SQL() { return getToken(CobolPreprocessorParser.SQL, 0); }
		public TerminalNode INCLUDE() { return getToken(CobolPreprocessorParser.INCLUDE, 0); }
		public TerminalNode END_EXEC() { return getToken(CobolPreprocessorParser.END_EXEC, 0); }
		public LiteralContext literal() {
			return getRuleContext(LiteralContext.class,0);
		}
		public CobolWordContext cobolWord() {
			return getRuleContext(CobolWordContext.class,0);
		}
		public FilenameContext filename() {
			return getRuleContext(FilenameContext.class,0);
		}
		public TerminalNode DOT() { return getToken(CobolPreprocessorParser.DOT, 0); }
		public ExecSqlIncludeStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_execSqlIncludeStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).enterExecSqlIncludeStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).exitExecSqlIncludeStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof CobolPreprocessorVisitor) return ((CobolPreprocessorVisitor<? extends T>) visitor).visitExecSqlIncludeStatement(this);
			return visitor.visitChildren(this);
		}
	}

	public final ExecSqlIncludeStatementContext execSqlIncludeStatement() throws RecognitionException {
		ExecSqlIncludeStatementContext _localctx = new ExecSqlIncludeStatementContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_execSqlIncludeStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(469);
			match(EXEC);
			setState(470);
			match(SQL);
			setState(471);
			match(INCLUDE);
			setState(475);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NONNUMERICLITERAL:
			case INTEGERLITERAL:
			case NUMERICLITERAL:
				{
				setState(472);
				literal();
				}
				break;
			case ADATA:
			case ADV:
			case ALIAS:
			case ANSI:
			case ANY:
			case APOST:
			case AR:
			case ARITH:
			case AUTO:
			case AWO:
			case BIN:
			case BLOCK0:
			case BUF:
			case BUFSIZE:
			case BY:
			case CBL:
			case CBLCARD:
			case CO:
			case COBOL2:
			case COBOL3:
			case CODEPAGE:
			case COMPAT:
			case COMPILE:
			case CP:
			case CPP:
			case CPSM:
			case CS:
			case CURR:
			case CURRENCY:
			case DATA:
			case DATEPROC:
			case DBCS:
			case DD:
			case DEBUG:
			case DECK:
			case DIAGTRUNC:
			case DLI:
			case DLL:
			case DP:
			case DTR:
			case DU:
			case DUMP:
			case DYN:
			case DYNAM:
			case EDF:
			case EJPD:
			case EN:
			case ENGLISH:
			case EPILOG:
			case EXCI:
			case EXIT:
			case EXP:
			case EXPORTALL:
			case EXTEND:
			case FASTSRT:
			case FLAG:
			case FLAGSTD:
			case FSRT:
			case FULL:
			case GDS:
			case GRAPHIC:
			case HOOK:
			case IN:
			case INTDATE:
			case JA:
			case JP:
			case KA:
			case LANG:
			case LANGUAGE:
			case LC:
			case LENGTH:
			case LIB:
			case LILIAN:
			case LIN:
			case LINECOUNT:
			case LINKAGE:
			case LIST:
			case LM:
			case LONGMIXED:
			case LONGUPPER:
			case LU:
			case MAP:
			case MARGINS:
			case MAX:
			case MD:
			case MDECK:
			case MIG:
			case MIXED:
			case NAME:
			case NAT:
			case NATIONAL:
			case NATLANG:
			case NN:
			case NO:
			case NOADATA:
			case NOADV:
			case NOALIAS:
			case NOAWO:
			case NOBLOCK0:
			case NOC:
			case NOCBLCARD:
			case NOCICS:
			case NOCMPR2:
			case NOCOMPILE:
			case NOCPSM:
			case NOCURR:
			case NOCURRENCY:
			case NOD:
			case NODATEPROC:
			case NODBCS:
			case NODE:
			case NODEBUG:
			case NODECK:
			case NODIAGTRUNC:
			case NODLL:
			case NODU:
			case NODUMP:
			case NODP:
			case NODTR:
			case NODYN:
			case NODYNAM:
			case NOEDF:
			case NOEJPD:
			case NOEPILOG:
			case NOEXIT:
			case NOEXP:
			case NOEXPORTALL:
			case NOF:
			case NOFASTSRT:
			case NOFEPI:
			case NOFLAG:
			case NOFLAGMIG:
			case NOFLAGSTD:
			case NOFSRT:
			case NOGRAPHIC:
			case NOHOOK:
			case NOLENGTH:
			case NOLIB:
			case NOLINKAGE:
			case NOLIST:
			case NOMAP:
			case NOMD:
			case NOMDECK:
			case NONAME:
			case NONUM:
			case NONUMBER:
			case NOOBJ:
			case NOOBJECT:
			case NOOFF:
			case NOOFFSET:
			case NOOPSEQUENCE:
			case NOOPT:
			case NOOPTIMIZE:
			case NOOPTIONS:
			case NOP:
			case NOPFD:
			case NOPROLOG:
			case NORENT:
			case NOS:
			case NOSEP:
			case NOSEPARATE:
			case NOSEQ:
			case NOSOURCE:
			case NOSPIE:
			case NOSQL:
			case NOSQLC:
			case NOSQLCCSID:
			case NOSSR:
			case NOSSRANGE:
			case NOSTDTRUNC:
			case NOSEQUENCE:
			case NOTERM:
			case NOTERMINAL:
			case NOTEST:
			case NOTHREAD:
			case NOTRIG:
			case NOVBREF:
			case NOWORD:
			case NOX:
			case NOXREF:
			case NOZWB:
			case NS:
			case NSEQ:
			case NSYMBOL:
			case NUM:
			case NUMBER:
			case NUMPROC:
			case OBJ:
			case OBJECT:
			case OF:
			case OFF:
			case OFFSET:
			case ON:
			case OP:
			case OPMARGINS:
			case OPSEQUENCE:
			case OPT:
			case OPTFILE:
			case OPTIMIZE:
			case OPTIONS:
			case OUT:
			case OUTDD:
			case PFD:
			case PPTDBG:
			case PGMN:
			case PGMNAME:
			case PROCESS:
			case PROLOG:
			case QUOTE:
			case RENT:
			case REPLACING:
			case RMODE:
			case SEP:
			case SEPARATE:
			case SEQ:
			case SEQUENCE:
			case SHORT:
			case SIZE:
			case SOURCE:
			case SP:
			case SPACE:
			case SPIE:
			case SQL:
			case SQLC:
			case SQLCCSID:
			case SS:
			case SSR:
			case SSRANGE:
			case STD:
			case SYSEIB:
			case SZ:
			case TERM:
			case TERMINAL:
			case TEST:
			case THREAD:
			case TRIG:
			case TRUNC:
			case UE:
			case UPPER:
			case VBREF:
			case WD:
			case XMLPARSE:
			case XMLSS:
			case XOPTS:
			case XREF:
			case YEARWINDOW:
			case YW:
			case ZWB:
			case C_CHAR:
			case D_CHAR:
			case E_CHAR:
			case F_CHAR:
			case H_CHAR:
			case I_CHAR:
			case M_CHAR:
			case N_CHAR:
			case Q_CHAR:
			case S_CHAR:
			case U_CHAR:
			case W_CHAR:
			case X_CHAR:
			case COMMACHAR:
			case IDENTIFIER:
				{
				setState(473);
				cobolWord();
				}
				break;
			case FILENAME:
				{
				setState(474);
				filename();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(477);
			match(END_EXEC);
			setState(479);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,36,_ctx) ) {
			case 1:
				{
				setState(478);
				match(DOT);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExecSqlImsStatementContext extends ParserRuleContext {
		public TerminalNode EXEC() { return getToken(CobolPreprocessorParser.EXEC, 0); }
		public TerminalNode SQLIMS() { return getToken(CobolPreprocessorParser.SQLIMS, 0); }
		public CharDataContext charData() {
			return getRuleContext(CharDataContext.class,0);
		}
		public TerminalNode END_EXEC() { return getToken(CobolPreprocessorParser.END_EXEC, 0); }
		public TerminalNode DOT() { return getToken(CobolPreprocessorParser.DOT, 0); }
		public ExecSqlImsStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_execSqlImsStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).enterExecSqlImsStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).exitExecSqlImsStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof CobolPreprocessorVisitor) return ((CobolPreprocessorVisitor<? extends T>) visitor).visitExecSqlImsStatement(this);
			return visitor.visitChildren(this);
		}
	}

	public final ExecSqlImsStatementContext execSqlImsStatement() throws RecognitionException {
		ExecSqlImsStatementContext _localctx = new ExecSqlImsStatementContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_execSqlImsStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(481);
			match(EXEC);
			setState(482);
			match(SQLIMS);
			setState(483);
			charData();
			setState(484);
			match(END_EXEC);
			setState(486);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,37,_ctx) ) {
			case 1:
				{
				setState(485);
				match(DOT);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CopyStatementContext extends ParserRuleContext {
		public TerminalNode COPY() { return getToken(CobolPreprocessorParser.COPY, 0); }
		public CopySourceContext copySource() {
			return getRuleContext(CopySourceContext.class,0);
		}
		public TerminalNode DOT() { return getToken(CobolPreprocessorParser.DOT, 0); }
		public List<DirectoryPhraseContext> directoryPhrase() {
			return getRuleContexts(DirectoryPhraseContext.class);
		}
		public DirectoryPhraseContext directoryPhrase(int i) {
			return getRuleContext(DirectoryPhraseContext.class,i);
		}
		public List<FamilyPhraseContext> familyPhrase() {
			return getRuleContexts(FamilyPhraseContext.class);
		}
		public FamilyPhraseContext familyPhrase(int i) {
			return getRuleContext(FamilyPhraseContext.class,i);
		}
		public List<ReplacingPhraseContext> replacingPhrase() {
			return getRuleContexts(ReplacingPhraseContext.class);
		}
		public ReplacingPhraseContext replacingPhrase(int i) {
			return getRuleContext(ReplacingPhraseContext.class,i);
		}
		public List<TerminalNode> SUPPRESS() { return getTokens(CobolPreprocessorParser.SUPPRESS); }
		public TerminalNode SUPPRESS(int i) {
			return getToken(CobolPreprocessorParser.SUPPRESS, i);
		}
		public CopyStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_copyStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).enterCopyStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).exitCopyStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof CobolPreprocessorVisitor) return ((CobolPreprocessorVisitor<? extends T>) visitor).visitCopyStatement(this);
			return visitor.visitChildren(this);
		}
	}

	public final CopyStatementContext copyStatement() throws RecognitionException {
		CopyStatementContext _localctx = new CopyStatementContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_copyStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(488);
			match(COPY);
			setState(489);
			copySource();
			setState(498);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==IN || (((_la - 200) & ~0x3f) == 0 && ((1L << (_la - 200)) & 70368748371977L) != 0)) {
				{
				{
				setState(494);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case IN:
				case OF:
					{
					setState(490);
					directoryPhrase();
					}
					break;
				case ON:
					{
					setState(491);
					familyPhrase();
					}
					break;
				case REPLACING:
					{
					setState(492);
					replacingPhrase();
					}
					break;
				case SUPPRESS:
					{
					setState(493);
					match(SUPPRESS);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				}
				setState(500);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(501);
			match(DOT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CopySourceContext extends ParserRuleContext {
		public LiteralContext literal() {
			return getRuleContext(LiteralContext.class,0);
		}
		public CobolWordContext cobolWord() {
			return getRuleContext(CobolWordContext.class,0);
		}
		public FilenameContext filename() {
			return getRuleContext(FilenameContext.class,0);
		}
		public CopyLibraryContext copyLibrary() {
			return getRuleContext(CopyLibraryContext.class,0);
		}
		public TerminalNode OF() { return getToken(CobolPreprocessorParser.OF, 0); }
		public TerminalNode IN() { return getToken(CobolPreprocessorParser.IN, 0); }
		public CopySourceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_copySource; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).enterCopySource(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).exitCopySource(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof CobolPreprocessorVisitor) return ((CobolPreprocessorVisitor<? extends T>) visitor).visitCopySource(this);
			return visitor.visitChildren(this);
		}
	}

	public final CopySourceContext copySource() throws RecognitionException {
		CopySourceContext _localctx = new CopySourceContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_copySource);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(506);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NONNUMERICLITERAL:
			case INTEGERLITERAL:
			case NUMERICLITERAL:
				{
				setState(503);
				literal();
				}
				break;
			case ADATA:
			case ADV:
			case ALIAS:
			case ANSI:
			case ANY:
			case APOST:
			case AR:
			case ARITH:
			case AUTO:
			case AWO:
			case BIN:
			case BLOCK0:
			case BUF:
			case BUFSIZE:
			case BY:
			case CBL:
			case CBLCARD:
			case CO:
			case COBOL2:
			case COBOL3:
			case CODEPAGE:
			case COMPAT:
			case COMPILE:
			case CP:
			case CPP:
			case CPSM:
			case CS:
			case CURR:
			case CURRENCY:
			case DATA:
			case DATEPROC:
			case DBCS:
			case DD:
			case DEBUG:
			case DECK:
			case DIAGTRUNC:
			case DLI:
			case DLL:
			case DP:
			case DTR:
			case DU:
			case DUMP:
			case DYN:
			case DYNAM:
			case EDF:
			case EJPD:
			case EN:
			case ENGLISH:
			case EPILOG:
			case EXCI:
			case EXIT:
			case EXP:
			case EXPORTALL:
			case EXTEND:
			case FASTSRT:
			case FLAG:
			case FLAGSTD:
			case FSRT:
			case FULL:
			case GDS:
			case GRAPHIC:
			case HOOK:
			case IN:
			case INTDATE:
			case JA:
			case JP:
			case KA:
			case LANG:
			case LANGUAGE:
			case LC:
			case LENGTH:
			case LIB:
			case LILIAN:
			case LIN:
			case LINECOUNT:
			case LINKAGE:
			case LIST:
			case LM:
			case LONGMIXED:
			case LONGUPPER:
			case LU:
			case MAP:
			case MARGINS:
			case MAX:
			case MD:
			case MDECK:
			case MIG:
			case MIXED:
			case NAME:
			case NAT:
			case NATIONAL:
			case NATLANG:
			case NN:
			case NO:
			case NOADATA:
			case NOADV:
			case NOALIAS:
			case NOAWO:
			case NOBLOCK0:
			case NOC:
			case NOCBLCARD:
			case NOCICS:
			case NOCMPR2:
			case NOCOMPILE:
			case NOCPSM:
			case NOCURR:
			case NOCURRENCY:
			case NOD:
			case NODATEPROC:
			case NODBCS:
			case NODE:
			case NODEBUG:
			case NODECK:
			case NODIAGTRUNC:
			case NODLL:
			case NODU:
			case NODUMP:
			case NODP:
			case NODTR:
			case NODYN:
			case NODYNAM:
			case NOEDF:
			case NOEJPD:
			case NOEPILOG:
			case NOEXIT:
			case NOEXP:
			case NOEXPORTALL:
			case NOF:
			case NOFASTSRT:
			case NOFEPI:
			case NOFLAG:
			case NOFLAGMIG:
			case NOFLAGSTD:
			case NOFSRT:
			case NOGRAPHIC:
			case NOHOOK:
			case NOLENGTH:
			case NOLIB:
			case NOLINKAGE:
			case NOLIST:
			case NOMAP:
			case NOMD:
			case NOMDECK:
			case NONAME:
			case NONUM:
			case NONUMBER:
			case NOOBJ:
			case NOOBJECT:
			case NOOFF:
			case NOOFFSET:
			case NOOPSEQUENCE:
			case NOOPT:
			case NOOPTIMIZE:
			case NOOPTIONS:
			case NOP:
			case NOPFD:
			case NOPROLOG:
			case NORENT:
			case NOS:
			case NOSEP:
			case NOSEPARATE:
			case NOSEQ:
			case NOSOURCE:
			case NOSPIE:
			case NOSQL:
			case NOSQLC:
			case NOSQLCCSID:
			case NOSSR:
			case NOSSRANGE:
			case NOSTDTRUNC:
			case NOSEQUENCE:
			case NOTERM:
			case NOTERMINAL:
			case NOTEST:
			case NOTHREAD:
			case NOTRIG:
			case NOVBREF:
			case NOWORD:
			case NOX:
			case NOXREF:
			case NOZWB:
			case NS:
			case NSEQ:
			case NSYMBOL:
			case NUM:
			case NUMBER:
			case NUMPROC:
			case OBJ:
			case OBJECT:
			case OF:
			case OFF:
			case OFFSET:
			case ON:
			case OP:
			case OPMARGINS:
			case OPSEQUENCE:
			case OPT:
			case OPTFILE:
			case OPTIMIZE:
			case OPTIONS:
			case OUT:
			case OUTDD:
			case PFD:
			case PPTDBG:
			case PGMN:
			case PGMNAME:
			case PROCESS:
			case PROLOG:
			case QUOTE:
			case RENT:
			case REPLACING:
			case RMODE:
			case SEP:
			case SEPARATE:
			case SEQ:
			case SEQUENCE:
			case SHORT:
			case SIZE:
			case SOURCE:
			case SP:
			case SPACE:
			case SPIE:
			case SQL:
			case SQLC:
			case SQLCCSID:
			case SS:
			case SSR:
			case SSRANGE:
			case STD:
			case SYSEIB:
			case SZ:
			case TERM:
			case TERMINAL:
			case TEST:
			case THREAD:
			case TRIG:
			case TRUNC:
			case UE:
			case UPPER:
			case VBREF:
			case WD:
			case XMLPARSE:
			case XMLSS:
			case XOPTS:
			case XREF:
			case YEARWINDOW:
			case YW:
			case ZWB:
			case C_CHAR:
			case D_CHAR:
			case E_CHAR:
			case F_CHAR:
			case H_CHAR:
			case I_CHAR:
			case M_CHAR:
			case N_CHAR:
			case Q_CHAR:
			case S_CHAR:
			case U_CHAR:
			case W_CHAR:
			case X_CHAR:
			case COMMACHAR:
			case IDENTIFIER:
				{
				setState(504);
				cobolWord();
				}
				break;
			case FILENAME:
				{
				setState(505);
				filename();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(510);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,41,_ctx) ) {
			case 1:
				{
				setState(508);
				_la = _input.LA(1);
				if ( !(_la==IN || _la==OF) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(509);
				copyLibrary();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CopyLibraryContext extends ParserRuleContext {
		public LiteralContext literal() {
			return getRuleContext(LiteralContext.class,0);
		}
		public CobolWordContext cobolWord() {
			return getRuleContext(CobolWordContext.class,0);
		}
		public CopyLibraryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_copyLibrary; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).enterCopyLibrary(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).exitCopyLibrary(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof CobolPreprocessorVisitor) return ((CobolPreprocessorVisitor<? extends T>) visitor).visitCopyLibrary(this);
			return visitor.visitChildren(this);
		}
	}

	public final CopyLibraryContext copyLibrary() throws RecognitionException {
		CopyLibraryContext _localctx = new CopyLibraryContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_copyLibrary);
		try {
			setState(514);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NONNUMERICLITERAL:
			case INTEGERLITERAL:
			case NUMERICLITERAL:
				enterOuterAlt(_localctx, 1);
				{
				setState(512);
				literal();
				}
				break;
			case ADATA:
			case ADV:
			case ALIAS:
			case ANSI:
			case ANY:
			case APOST:
			case AR:
			case ARITH:
			case AUTO:
			case AWO:
			case BIN:
			case BLOCK0:
			case BUF:
			case BUFSIZE:
			case BY:
			case CBL:
			case CBLCARD:
			case CO:
			case COBOL2:
			case COBOL3:
			case CODEPAGE:
			case COMPAT:
			case COMPILE:
			case CP:
			case CPP:
			case CPSM:
			case CS:
			case CURR:
			case CURRENCY:
			case DATA:
			case DATEPROC:
			case DBCS:
			case DD:
			case DEBUG:
			case DECK:
			case DIAGTRUNC:
			case DLI:
			case DLL:
			case DP:
			case DTR:
			case DU:
			case DUMP:
			case DYN:
			case DYNAM:
			case EDF:
			case EJPD:
			case EN:
			case ENGLISH:
			case EPILOG:
			case EXCI:
			case EXIT:
			case EXP:
			case EXPORTALL:
			case EXTEND:
			case FASTSRT:
			case FLAG:
			case FLAGSTD:
			case FSRT:
			case FULL:
			case GDS:
			case GRAPHIC:
			case HOOK:
			case IN:
			case INTDATE:
			case JA:
			case JP:
			case KA:
			case LANG:
			case LANGUAGE:
			case LC:
			case LENGTH:
			case LIB:
			case LILIAN:
			case LIN:
			case LINECOUNT:
			case LINKAGE:
			case LIST:
			case LM:
			case LONGMIXED:
			case LONGUPPER:
			case LU:
			case MAP:
			case MARGINS:
			case MAX:
			case MD:
			case MDECK:
			case MIG:
			case MIXED:
			case NAME:
			case NAT:
			case NATIONAL:
			case NATLANG:
			case NN:
			case NO:
			case NOADATA:
			case NOADV:
			case NOALIAS:
			case NOAWO:
			case NOBLOCK0:
			case NOC:
			case NOCBLCARD:
			case NOCICS:
			case NOCMPR2:
			case NOCOMPILE:
			case NOCPSM:
			case NOCURR:
			case NOCURRENCY:
			case NOD:
			case NODATEPROC:
			case NODBCS:
			case NODE:
			case NODEBUG:
			case NODECK:
			case NODIAGTRUNC:
			case NODLL:
			case NODU:
			case NODUMP:
			case NODP:
			case NODTR:
			case NODYN:
			case NODYNAM:
			case NOEDF:
			case NOEJPD:
			case NOEPILOG:
			case NOEXIT:
			case NOEXP:
			case NOEXPORTALL:
			case NOF:
			case NOFASTSRT:
			case NOFEPI:
			case NOFLAG:
			case NOFLAGMIG:
			case NOFLAGSTD:
			case NOFSRT:
			case NOGRAPHIC:
			case NOHOOK:
			case NOLENGTH:
			case NOLIB:
			case NOLINKAGE:
			case NOLIST:
			case NOMAP:
			case NOMD:
			case NOMDECK:
			case NONAME:
			case NONUM:
			case NONUMBER:
			case NOOBJ:
			case NOOBJECT:
			case NOOFF:
			case NOOFFSET:
			case NOOPSEQUENCE:
			case NOOPT:
			case NOOPTIMIZE:
			case NOOPTIONS:
			case NOP:
			case NOPFD:
			case NOPROLOG:
			case NORENT:
			case NOS:
			case NOSEP:
			case NOSEPARATE:
			case NOSEQ:
			case NOSOURCE:
			case NOSPIE:
			case NOSQL:
			case NOSQLC:
			case NOSQLCCSID:
			case NOSSR:
			case NOSSRANGE:
			case NOSTDTRUNC:
			case NOSEQUENCE:
			case NOTERM:
			case NOTERMINAL:
			case NOTEST:
			case NOTHREAD:
			case NOTRIG:
			case NOVBREF:
			case NOWORD:
			case NOX:
			case NOXREF:
			case NOZWB:
			case NS:
			case NSEQ:
			case NSYMBOL:
			case NUM:
			case NUMBER:
			case NUMPROC:
			case OBJ:
			case OBJECT:
			case OF:
			case OFF:
			case OFFSET:
			case ON:
			case OP:
			case OPMARGINS:
			case OPSEQUENCE:
			case OPT:
			case OPTFILE:
			case OPTIMIZE:
			case OPTIONS:
			case OUT:
			case OUTDD:
			case PFD:
			case PPTDBG:
			case PGMN:
			case PGMNAME:
			case PROCESS:
			case PROLOG:
			case QUOTE:
			case RENT:
			case REPLACING:
			case RMODE:
			case SEP:
			case SEPARATE:
			case SEQ:
			case SEQUENCE:
			case SHORT:
			case SIZE:
			case SOURCE:
			case SP:
			case SPACE:
			case SPIE:
			case SQL:
			case SQLC:
			case SQLCCSID:
			case SS:
			case SSR:
			case SSRANGE:
			case STD:
			case SYSEIB:
			case SZ:
			case TERM:
			case TERMINAL:
			case TEST:
			case THREAD:
			case TRIG:
			case TRUNC:
			case UE:
			case UPPER:
			case VBREF:
			case WD:
			case XMLPARSE:
			case XMLSS:
			case XOPTS:
			case XREF:
			case YEARWINDOW:
			case YW:
			case ZWB:
			case C_CHAR:
			case D_CHAR:
			case E_CHAR:
			case F_CHAR:
			case H_CHAR:
			case I_CHAR:
			case M_CHAR:
			case N_CHAR:
			case Q_CHAR:
			case S_CHAR:
			case U_CHAR:
			case W_CHAR:
			case X_CHAR:
			case COMMACHAR:
			case IDENTIFIER:
				enterOuterAlt(_localctx, 2);
				{
				setState(513);
				cobolWord();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ReplacingPhraseContext extends ParserRuleContext {
		public TerminalNode REPLACING() { return getToken(CobolPreprocessorParser.REPLACING, 0); }
		public List<ReplaceClauseContext> replaceClause() {
			return getRuleContexts(ReplaceClauseContext.class);
		}
		public ReplaceClauseContext replaceClause(int i) {
			return getRuleContext(ReplaceClauseContext.class,i);
		}
		public ReplacingPhraseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_replacingPhrase; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).enterReplacingPhrase(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).exitReplacingPhrase(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof CobolPreprocessorVisitor) return ((CobolPreprocessorVisitor<? extends T>) visitor).visitReplacingPhrase(this);
			return visitor.visitChildren(this);
		}
	}

	public final ReplacingPhraseContext replacingPhrase() throws RecognitionException {
		ReplacingPhraseContext _localctx = new ReplacingPhraseContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_replacingPhrase);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(516);
			match(REPLACING);
			setState(517);
			replaceClause();
			setState(521);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,43,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(518);
					replaceClause();
					}
					}
				}
				setState(523);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,43,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ReplaceAreaContext extends ParserRuleContext {
		public ReplaceByStatementContext replaceByStatement() {
			return getRuleContext(ReplaceByStatementContext.class,0);
		}
		public List<CopyStatementContext> copyStatement() {
			return getRuleContexts(CopyStatementContext.class);
		}
		public CopyStatementContext copyStatement(int i) {
			return getRuleContext(CopyStatementContext.class,i);
		}
		public List<CharDataContext> charData() {
			return getRuleContexts(CharDataContext.class);
		}
		public CharDataContext charData(int i) {
			return getRuleContext(CharDataContext.class,i);
		}
		public ReplaceOffStatementContext replaceOffStatement() {
			return getRuleContext(ReplaceOffStatementContext.class,0);
		}
		public ReplaceAreaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_replaceArea; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).enterReplaceArea(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).exitReplaceArea(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof CobolPreprocessorVisitor) return ((CobolPreprocessorVisitor<? extends T>) visitor).visitReplaceArea(this);
			return visitor.visitChildren(this);
		}
	}

	public final ReplaceAreaContext replaceArea() throws RecognitionException {
		ReplaceAreaContext _localctx = new ReplaceAreaContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_replaceArea);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(524);
			replaceByStatement();
			setState(529);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,45,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					setState(527);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case COPY:
						{
						setState(525);
						copyStatement();
						}
						break;
					case ADATA:
					case ADV:
					case ALIAS:
					case ANSI:
					case ANY:
					case APOST:
					case AR:
					case ARITH:
					case AUTO:
					case AWO:
					case BIN:
					case BLOCK0:
					case BUF:
					case BUFSIZE:
					case BY:
					case CBL:
					case CBLCARD:
					case CO:
					case COBOL2:
					case COBOL3:
					case CODEPAGE:
					case COMPAT:
					case COMPILE:
					case CP:
					case CPP:
					case CPSM:
					case CS:
					case CURR:
					case CURRENCY:
					case DATA:
					case DATEPROC:
					case DBCS:
					case DD:
					case DEBUG:
					case DECK:
					case DIAGTRUNC:
					case DLI:
					case DLL:
					case DP:
					case DTR:
					case DU:
					case DUMP:
					case DYN:
					case DYNAM:
					case EDF:
					case EJPD:
					case EN:
					case ENGLISH:
					case EPILOG:
					case EXCI:
					case EXIT:
					case EXP:
					case EXPORTALL:
					case EXTEND:
					case FASTSRT:
					case FLAG:
					case FLAGSTD:
					case FSRT:
					case FULL:
					case GDS:
					case GRAPHIC:
					case HOOK:
					case IN:
					case INTDATE:
					case JA:
					case JP:
					case KA:
					case LANG:
					case LANGUAGE:
					case LC:
					case LENGTH:
					case LIB:
					case LILIAN:
					case LIN:
					case LINECOUNT:
					case LINKAGE:
					case LIST:
					case LM:
					case LONGMIXED:
					case LONGUPPER:
					case LPARENCHAR:
					case LU:
					case MAP:
					case MARGINS:
					case MAX:
					case MD:
					case MDECK:
					case MIG:
					case MIXED:
					case NAME:
					case NAT:
					case NATIONAL:
					case NATLANG:
					case NN:
					case NO:
					case NOADATA:
					case NOADV:
					case NOALIAS:
					case NOAWO:
					case NOBLOCK0:
					case NOC:
					case NOCBLCARD:
					case NOCICS:
					case NOCMPR2:
					case NOCOMPILE:
					case NOCPSM:
					case NOCURR:
					case NOCURRENCY:
					case NOD:
					case NODATEPROC:
					case NODBCS:
					case NODE:
					case NODEBUG:
					case NODECK:
					case NODIAGTRUNC:
					case NODLL:
					case NODU:
					case NODUMP:
					case NODP:
					case NODTR:
					case NODYN:
					case NODYNAM:
					case NOEDF:
					case NOEJPD:
					case NOEPILOG:
					case NOEXIT:
					case NOEXP:
					case NOEXPORTALL:
					case NOF:
					case NOFASTSRT:
					case NOFEPI:
					case NOFLAG:
					case NOFLAGMIG:
					case NOFLAGSTD:
					case NOFSRT:
					case NOGRAPHIC:
					case NOHOOK:
					case NOLENGTH:
					case NOLIB:
					case NOLINKAGE:
					case NOLIST:
					case NOMAP:
					case NOMD:
					case NOMDECK:
					case NONAME:
					case NONUM:
					case NONUMBER:
					case NOOBJ:
					case NOOBJECT:
					case NOOFF:
					case NOOFFSET:
					case NOOPSEQUENCE:
					case NOOPT:
					case NOOPTIMIZE:
					case NOOPTIONS:
					case NOP:
					case NOPFD:
					case NOPROLOG:
					case NORENT:
					case NOS:
					case NOSEP:
					case NOSEPARATE:
					case NOSEQ:
					case NOSOURCE:
					case NOSPIE:
					case NOSQL:
					case NOSQLC:
					case NOSQLCCSID:
					case NOSSR:
					case NOSSRANGE:
					case NOSTDTRUNC:
					case NOSEQUENCE:
					case NOTERM:
					case NOTERMINAL:
					case NOTEST:
					case NOTHREAD:
					case NOTRIG:
					case NOVBREF:
					case NOWORD:
					case NOX:
					case NOXREF:
					case NOZWB:
					case NS:
					case NSEQ:
					case NSYMBOL:
					case NUM:
					case NUMBER:
					case NUMPROC:
					case OBJ:
					case OBJECT:
					case OF:
					case OFF:
					case OFFSET:
					case ON:
					case OP:
					case OPMARGINS:
					case OPSEQUENCE:
					case OPT:
					case OPTFILE:
					case OPTIMIZE:
					case OPTIONS:
					case OUT:
					case OUTDD:
					case PFD:
					case PPTDBG:
					case PGMN:
					case PGMNAME:
					case PROCESS:
					case PROLOG:
					case QUOTE:
					case RENT:
					case REPLACING:
					case RMODE:
					case RPARENCHAR:
					case SEP:
					case SEPARATE:
					case SEQ:
					case SEQUENCE:
					case SHORT:
					case SIZE:
					case SOURCE:
					case SP:
					case SPACE:
					case SPIE:
					case SQL:
					case SQLC:
					case SQLCCSID:
					case SS:
					case SSR:
					case SSRANGE:
					case STD:
					case SYSEIB:
					case SZ:
					case TERM:
					case TERMINAL:
					case TEST:
					case THREAD:
					case TRIG:
					case TRUNC:
					case UE:
					case UPPER:
					case VBREF:
					case WD:
					case XMLPARSE:
					case XMLSS:
					case XOPTS:
					case XREF:
					case YEARWINDOW:
					case YW:
					case ZWB:
					case C_CHAR:
					case D_CHAR:
					case E_CHAR:
					case F_CHAR:
					case H_CHAR:
					case I_CHAR:
					case M_CHAR:
					case N_CHAR:
					case Q_CHAR:
					case S_CHAR:
					case U_CHAR:
					case W_CHAR:
					case X_CHAR:
					case COMMACHAR:
					case DOT:
					case NONNUMERICLITERAL:
					case INTEGERLITERAL:
					case NUMERICLITERAL:
					case IDENTIFIER:
					case FILENAME:
					case COMMENTENTRYLINE:
					case TEXT:
						{
						setState(526);
						charData();
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
				}
				setState(531);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,45,_ctx);
			}
			setState(533);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,46,_ctx) ) {
			case 1:
				{
				setState(532);
				replaceOffStatement();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ReplaceByStatementContext extends ParserRuleContext {
		public TerminalNode REPLACE() { return getToken(CobolPreprocessorParser.REPLACE, 0); }
		public TerminalNode DOT() { return getToken(CobolPreprocessorParser.DOT, 0); }
		public List<ReplaceClauseContext> replaceClause() {
			return getRuleContexts(ReplaceClauseContext.class);
		}
		public ReplaceClauseContext replaceClause(int i) {
			return getRuleContext(ReplaceClauseContext.class,i);
		}
		public ReplaceByStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_replaceByStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).enterReplaceByStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).exitReplaceByStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof CobolPreprocessorVisitor) return ((CobolPreprocessorVisitor<? extends T>) visitor).visitReplaceByStatement(this);
			return visitor.visitChildren(this);
		}
	}

	public final ReplaceByStatementContext replaceByStatement() throws RecognitionException {
		ReplaceByStatementContext _localctx = new ReplaceByStatementContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_replaceByStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(535);
			match(REPLACE);
			setState(537);
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(536);
				replaceClause();
				}
				}
				setState(539);
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( ((_la & ~0x3f) == 0 && ((1L << _la) & -2346656880870555650L) != 0) || (((_la - 64) & ~0x3f) == 0 && ((1L << (_la - 64)) & -16449L) != 0) || (((_la - 128) & ~0x3f) == 0 && ((1L << (_la - 128)) & -576460752303423489L) != 0) || (((_la - 192) & ~0x3f) == 0 && ((1L << (_la - 192)) & -2324912939422711809L) != 0) || (((_la - 256) & ~0x3f) == 0 && ((1L << (_la - 256)) & 2541882441455L) != 0) );
			setState(541);
			match(DOT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ReplaceOffStatementContext extends ParserRuleContext {
		public TerminalNode REPLACE() { return getToken(CobolPreprocessorParser.REPLACE, 0); }
		public TerminalNode OFF() { return getToken(CobolPreprocessorParser.OFF, 0); }
		public TerminalNode DOT() { return getToken(CobolPreprocessorParser.DOT, 0); }
		public ReplaceOffStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_replaceOffStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).enterReplaceOffStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).exitReplaceOffStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof CobolPreprocessorVisitor) return ((CobolPreprocessorVisitor<? extends T>) visitor).visitReplaceOffStatement(this);
			return visitor.visitChildren(this);
		}
	}

	public final ReplaceOffStatementContext replaceOffStatement() throws RecognitionException {
		ReplaceOffStatementContext _localctx = new ReplaceOffStatementContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_replaceOffStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(543);
			match(REPLACE);
			setState(544);
			match(OFF);
			setState(545);
			match(DOT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ReplaceClauseContext extends ParserRuleContext {
		public ReplaceableContext replaceable() {
			return getRuleContext(ReplaceableContext.class,0);
		}
		public TerminalNode BY() { return getToken(CobolPreprocessorParser.BY, 0); }
		public ReplacementContext replacement() {
			return getRuleContext(ReplacementContext.class,0);
		}
		public SubscriptContext subscript() {
			return getRuleContext(SubscriptContext.class,0);
		}
		public List<DirectoryPhraseContext> directoryPhrase() {
			return getRuleContexts(DirectoryPhraseContext.class);
		}
		public DirectoryPhraseContext directoryPhrase(int i) {
			return getRuleContext(DirectoryPhraseContext.class,i);
		}
		public FamilyPhraseContext familyPhrase() {
			return getRuleContext(FamilyPhraseContext.class,0);
		}
		public ReplaceClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_replaceClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).enterReplaceClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).exitReplaceClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof CobolPreprocessorVisitor) return ((CobolPreprocessorVisitor<? extends T>) visitor).visitReplaceClause(this);
			return visitor.visitChildren(this);
		}
	}

	public final ReplaceClauseContext replaceClause() throws RecognitionException {
		ReplaceClauseContext _localctx = new ReplaceClauseContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_replaceClause);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(547);
			replaceable();
			setState(548);
			match(BY);
			setState(549);
			replacement();
			setState(551);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,48,_ctx) ) {
			case 1:
				{
				setState(550);
				subscript();
				}
				break;
			}
			setState(556);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,49,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(553);
					directoryPhrase();
					}
					}
				}
				setState(558);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,49,_ctx);
			}
			setState(560);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,50,_ctx) ) {
			case 1:
				{
				setState(559);
				familyPhrase();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DirectoryPhraseContext extends ParserRuleContext {
		public TerminalNode OF() { return getToken(CobolPreprocessorParser.OF, 0); }
		public TerminalNode IN() { return getToken(CobolPreprocessorParser.IN, 0); }
		public LiteralContext literal() {
			return getRuleContext(LiteralContext.class,0);
		}
		public CobolWordContext cobolWord() {
			return getRuleContext(CobolWordContext.class,0);
		}
		public DirectoryPhraseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_directoryPhrase; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).enterDirectoryPhrase(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).exitDirectoryPhrase(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof CobolPreprocessorVisitor) return ((CobolPreprocessorVisitor<? extends T>) visitor).visitDirectoryPhrase(this);
			return visitor.visitChildren(this);
		}
	}

	public final DirectoryPhraseContext directoryPhrase() throws RecognitionException {
		DirectoryPhraseContext _localctx = new DirectoryPhraseContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_directoryPhrase);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(562);
			_la = _input.LA(1);
			if ( !(_la==IN || _la==OF) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(565);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NONNUMERICLITERAL:
			case INTEGERLITERAL:
			case NUMERICLITERAL:
				{
				setState(563);
				literal();
				}
				break;
			case ADATA:
			case ADV:
			case ALIAS:
			case ANSI:
			case ANY:
			case APOST:
			case AR:
			case ARITH:
			case AUTO:
			case AWO:
			case BIN:
			case BLOCK0:
			case BUF:
			case BUFSIZE:
			case BY:
			case CBL:
			case CBLCARD:
			case CO:
			case COBOL2:
			case COBOL3:
			case CODEPAGE:
			case COMPAT:
			case COMPILE:
			case CP:
			case CPP:
			case CPSM:
			case CS:
			case CURR:
			case CURRENCY:
			case DATA:
			case DATEPROC:
			case DBCS:
			case DD:
			case DEBUG:
			case DECK:
			case DIAGTRUNC:
			case DLI:
			case DLL:
			case DP:
			case DTR:
			case DU:
			case DUMP:
			case DYN:
			case DYNAM:
			case EDF:
			case EJPD:
			case EN:
			case ENGLISH:
			case EPILOG:
			case EXCI:
			case EXIT:
			case EXP:
			case EXPORTALL:
			case EXTEND:
			case FASTSRT:
			case FLAG:
			case FLAGSTD:
			case FSRT:
			case FULL:
			case GDS:
			case GRAPHIC:
			case HOOK:
			case IN:
			case INTDATE:
			case JA:
			case JP:
			case KA:
			case LANG:
			case LANGUAGE:
			case LC:
			case LENGTH:
			case LIB:
			case LILIAN:
			case LIN:
			case LINECOUNT:
			case LINKAGE:
			case LIST:
			case LM:
			case LONGMIXED:
			case LONGUPPER:
			case LU:
			case MAP:
			case MARGINS:
			case MAX:
			case MD:
			case MDECK:
			case MIG:
			case MIXED:
			case NAME:
			case NAT:
			case NATIONAL:
			case NATLANG:
			case NN:
			case NO:
			case NOADATA:
			case NOADV:
			case NOALIAS:
			case NOAWO:
			case NOBLOCK0:
			case NOC:
			case NOCBLCARD:
			case NOCICS:
			case NOCMPR2:
			case NOCOMPILE:
			case NOCPSM:
			case NOCURR:
			case NOCURRENCY:
			case NOD:
			case NODATEPROC:
			case NODBCS:
			case NODE:
			case NODEBUG:
			case NODECK:
			case NODIAGTRUNC:
			case NODLL:
			case NODU:
			case NODUMP:
			case NODP:
			case NODTR:
			case NODYN:
			case NODYNAM:
			case NOEDF:
			case NOEJPD:
			case NOEPILOG:
			case NOEXIT:
			case NOEXP:
			case NOEXPORTALL:
			case NOF:
			case NOFASTSRT:
			case NOFEPI:
			case NOFLAG:
			case NOFLAGMIG:
			case NOFLAGSTD:
			case NOFSRT:
			case NOGRAPHIC:
			case NOHOOK:
			case NOLENGTH:
			case NOLIB:
			case NOLINKAGE:
			case NOLIST:
			case NOMAP:
			case NOMD:
			case NOMDECK:
			case NONAME:
			case NONUM:
			case NONUMBER:
			case NOOBJ:
			case NOOBJECT:
			case NOOFF:
			case NOOFFSET:
			case NOOPSEQUENCE:
			case NOOPT:
			case NOOPTIMIZE:
			case NOOPTIONS:
			case NOP:
			case NOPFD:
			case NOPROLOG:
			case NORENT:
			case NOS:
			case NOSEP:
			case NOSEPARATE:
			case NOSEQ:
			case NOSOURCE:
			case NOSPIE:
			case NOSQL:
			case NOSQLC:
			case NOSQLCCSID:
			case NOSSR:
			case NOSSRANGE:
			case NOSTDTRUNC:
			case NOSEQUENCE:
			case NOTERM:
			case NOTERMINAL:
			case NOTEST:
			case NOTHREAD:
			case NOTRIG:
			case NOVBREF:
			case NOWORD:
			case NOX:
			case NOXREF:
			case NOZWB:
			case NS:
			case NSEQ:
			case NSYMBOL:
			case NUM:
			case NUMBER:
			case NUMPROC:
			case OBJ:
			case OBJECT:
			case OF:
			case OFF:
			case OFFSET:
			case ON:
			case OP:
			case OPMARGINS:
			case OPSEQUENCE:
			case OPT:
			case OPTFILE:
			case OPTIMIZE:
			case OPTIONS:
			case OUT:
			case OUTDD:
			case PFD:
			case PPTDBG:
			case PGMN:
			case PGMNAME:
			case PROCESS:
			case PROLOG:
			case QUOTE:
			case RENT:
			case REPLACING:
			case RMODE:
			case SEP:
			case SEPARATE:
			case SEQ:
			case SEQUENCE:
			case SHORT:
			case SIZE:
			case SOURCE:
			case SP:
			case SPACE:
			case SPIE:
			case SQL:
			case SQLC:
			case SQLCCSID:
			case SS:
			case SSR:
			case SSRANGE:
			case STD:
			case SYSEIB:
			case SZ:
			case TERM:
			case TERMINAL:
			case TEST:
			case THREAD:
			case TRIG:
			case TRUNC:
			case UE:
			case UPPER:
			case VBREF:
			case WD:
			case XMLPARSE:
			case XMLSS:
			case XOPTS:
			case XREF:
			case YEARWINDOW:
			case YW:
			case ZWB:
			case C_CHAR:
			case D_CHAR:
			case E_CHAR:
			case F_CHAR:
			case H_CHAR:
			case I_CHAR:
			case M_CHAR:
			case N_CHAR:
			case Q_CHAR:
			case S_CHAR:
			case U_CHAR:
			case W_CHAR:
			case X_CHAR:
			case COMMACHAR:
			case IDENTIFIER:
				{
				setState(564);
				cobolWord();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FamilyPhraseContext extends ParserRuleContext {
		public TerminalNode ON() { return getToken(CobolPreprocessorParser.ON, 0); }
		public LiteralContext literal() {
			return getRuleContext(LiteralContext.class,0);
		}
		public CobolWordContext cobolWord() {
			return getRuleContext(CobolWordContext.class,0);
		}
		public FamilyPhraseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_familyPhrase; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).enterFamilyPhrase(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).exitFamilyPhrase(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof CobolPreprocessorVisitor) return ((CobolPreprocessorVisitor<? extends T>) visitor).visitFamilyPhrase(this);
			return visitor.visitChildren(this);
		}
	}

	public final FamilyPhraseContext familyPhrase() throws RecognitionException {
		FamilyPhraseContext _localctx = new FamilyPhraseContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_familyPhrase);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(567);
			match(ON);
			setState(570);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NONNUMERICLITERAL:
			case INTEGERLITERAL:
			case NUMERICLITERAL:
				{
				setState(568);
				literal();
				}
				break;
			case ADATA:
			case ADV:
			case ALIAS:
			case ANSI:
			case ANY:
			case APOST:
			case AR:
			case ARITH:
			case AUTO:
			case AWO:
			case BIN:
			case BLOCK0:
			case BUF:
			case BUFSIZE:
			case BY:
			case CBL:
			case CBLCARD:
			case CO:
			case COBOL2:
			case COBOL3:
			case CODEPAGE:
			case COMPAT:
			case COMPILE:
			case CP:
			case CPP:
			case CPSM:
			case CS:
			case CURR:
			case CURRENCY:
			case DATA:
			case DATEPROC:
			case DBCS:
			case DD:
			case DEBUG:
			case DECK:
			case DIAGTRUNC:
			case DLI:
			case DLL:
			case DP:
			case DTR:
			case DU:
			case DUMP:
			case DYN:
			case DYNAM:
			case EDF:
			case EJPD:
			case EN:
			case ENGLISH:
			case EPILOG:
			case EXCI:
			case EXIT:
			case EXP:
			case EXPORTALL:
			case EXTEND:
			case FASTSRT:
			case FLAG:
			case FLAGSTD:
			case FSRT:
			case FULL:
			case GDS:
			case GRAPHIC:
			case HOOK:
			case IN:
			case INTDATE:
			case JA:
			case JP:
			case KA:
			case LANG:
			case LANGUAGE:
			case LC:
			case LENGTH:
			case LIB:
			case LILIAN:
			case LIN:
			case LINECOUNT:
			case LINKAGE:
			case LIST:
			case LM:
			case LONGMIXED:
			case LONGUPPER:
			case LU:
			case MAP:
			case MARGINS:
			case MAX:
			case MD:
			case MDECK:
			case MIG:
			case MIXED:
			case NAME:
			case NAT:
			case NATIONAL:
			case NATLANG:
			case NN:
			case NO:
			case NOADATA:
			case NOADV:
			case NOALIAS:
			case NOAWO:
			case NOBLOCK0:
			case NOC:
			case NOCBLCARD:
			case NOCICS:
			case NOCMPR2:
			case NOCOMPILE:
			case NOCPSM:
			case NOCURR:
			case NOCURRENCY:
			case NOD:
			case NODATEPROC:
			case NODBCS:
			case NODE:
			case NODEBUG:
			case NODECK:
			case NODIAGTRUNC:
			case NODLL:
			case NODU:
			case NODUMP:
			case NODP:
			case NODTR:
			case NODYN:
			case NODYNAM:
			case NOEDF:
			case NOEJPD:
			case NOEPILOG:
			case NOEXIT:
			case NOEXP:
			case NOEXPORTALL:
			case NOF:
			case NOFASTSRT:
			case NOFEPI:
			case NOFLAG:
			case NOFLAGMIG:
			case NOFLAGSTD:
			case NOFSRT:
			case NOGRAPHIC:
			case NOHOOK:
			case NOLENGTH:
			case NOLIB:
			case NOLINKAGE:
			case NOLIST:
			case NOMAP:
			case NOMD:
			case NOMDECK:
			case NONAME:
			case NONUM:
			case NONUMBER:
			case NOOBJ:
			case NOOBJECT:
			case NOOFF:
			case NOOFFSET:
			case NOOPSEQUENCE:
			case NOOPT:
			case NOOPTIMIZE:
			case NOOPTIONS:
			case NOP:
			case NOPFD:
			case NOPROLOG:
			case NORENT:
			case NOS:
			case NOSEP:
			case NOSEPARATE:
			case NOSEQ:
			case NOSOURCE:
			case NOSPIE:
			case NOSQL:
			case NOSQLC:
			case NOSQLCCSID:
			case NOSSR:
			case NOSSRANGE:
			case NOSTDTRUNC:
			case NOSEQUENCE:
			case NOTERM:
			case NOTERMINAL:
			case NOTEST:
			case NOTHREAD:
			case NOTRIG:
			case NOVBREF:
			case NOWORD:
			case NOX:
			case NOXREF:
			case NOZWB:
			case NS:
			case NSEQ:
			case NSYMBOL:
			case NUM:
			case NUMBER:
			case NUMPROC:
			case OBJ:
			case OBJECT:
			case OF:
			case OFF:
			case OFFSET:
			case ON:
			case OP:
			case OPMARGINS:
			case OPSEQUENCE:
			case OPT:
			case OPTFILE:
			case OPTIMIZE:
			case OPTIONS:
			case OUT:
			case OUTDD:
			case PFD:
			case PPTDBG:
			case PGMN:
			case PGMNAME:
			case PROCESS:
			case PROLOG:
			case QUOTE:
			case RENT:
			case REPLACING:
			case RMODE:
			case SEP:
			case SEPARATE:
			case SEQ:
			case SEQUENCE:
			case SHORT:
			case SIZE:
			case SOURCE:
			case SP:
			case SPACE:
			case SPIE:
			case SQL:
			case SQLC:
			case SQLCCSID:
			case SS:
			case SSR:
			case SSRANGE:
			case STD:
			case SYSEIB:
			case SZ:
			case TERM:
			case TERMINAL:
			case TEST:
			case THREAD:
			case TRIG:
			case TRUNC:
			case UE:
			case UPPER:
			case VBREF:
			case WD:
			case XMLPARSE:
			case XMLSS:
			case XOPTS:
			case XREF:
			case YEARWINDOW:
			case YW:
			case ZWB:
			case C_CHAR:
			case D_CHAR:
			case E_CHAR:
			case F_CHAR:
			case H_CHAR:
			case I_CHAR:
			case M_CHAR:
			case N_CHAR:
			case Q_CHAR:
			case S_CHAR:
			case U_CHAR:
			case W_CHAR:
			case X_CHAR:
			case COMMACHAR:
			case IDENTIFIER:
				{
				setState(569);
				cobolWord();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ReplaceableContext extends ParserRuleContext {
		public LiteralContext literal() {
			return getRuleContext(LiteralContext.class,0);
		}
		public CobolWordContext cobolWord() {
			return getRuleContext(CobolWordContext.class,0);
		}
		public PseudoTextContext pseudoText() {
			return getRuleContext(PseudoTextContext.class,0);
		}
		public CharDataLineNoDotContext charDataLineNoDot() {
			return getRuleContext(CharDataLineNoDotContext.class,0);
		}
		public ReplaceableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_replaceable; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).enterReplaceable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).exitReplaceable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof CobolPreprocessorVisitor) return ((CobolPreprocessorVisitor<? extends T>) visitor).visitReplaceable(this);
			return visitor.visitChildren(this);
		}
	}

	public final ReplaceableContext replaceable() throws RecognitionException {
		ReplaceableContext _localctx = new ReplaceableContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_replaceable);
		try {
			setState(576);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,53,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(572);
				literal();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(573);
				cobolWord();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(574);
				pseudoText();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(575);
				charDataLineNoDot();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ReplacementContext extends ParserRuleContext {
		public LiteralContext literal() {
			return getRuleContext(LiteralContext.class,0);
		}
		public CobolWordContext cobolWord() {
			return getRuleContext(CobolWordContext.class,0);
		}
		public PseudoTextContext pseudoText() {
			return getRuleContext(PseudoTextContext.class,0);
		}
		public CharDataLineNoDotContext charDataLineNoDot() {
			return getRuleContext(CharDataLineNoDotContext.class,0);
		}
		public ReplacementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_replacement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).enterReplacement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).exitReplacement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof CobolPreprocessorVisitor) return ((CobolPreprocessorVisitor<? extends T>) visitor).visitReplacement(this);
			return visitor.visitChildren(this);
		}
	}

	public final ReplacementContext replacement() throws RecognitionException {
		ReplacementContext _localctx = new ReplacementContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_replacement);
		try {
			setState(582);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,54,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(578);
				literal();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(579);
				cobolWord();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(580);
				pseudoText();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(581);
				charDataLineNoDot();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class EjectStatementContext extends ParserRuleContext {
		public TerminalNode EJECT() { return getToken(CobolPreprocessorParser.EJECT, 0); }
		public TerminalNode DOT() { return getToken(CobolPreprocessorParser.DOT, 0); }
		public EjectStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ejectStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).enterEjectStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).exitEjectStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof CobolPreprocessorVisitor) return ((CobolPreprocessorVisitor<? extends T>) visitor).visitEjectStatement(this);
			return visitor.visitChildren(this);
		}
	}

	public final EjectStatementContext ejectStatement() throws RecognitionException {
		EjectStatementContext _localctx = new EjectStatementContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_ejectStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(584);
			match(EJECT);
			setState(586);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,55,_ctx) ) {
			case 1:
				{
				setState(585);
				match(DOT);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SkipStatementContext extends ParserRuleContext {
		public TerminalNode SKIP1() { return getToken(CobolPreprocessorParser.SKIP1, 0); }
		public TerminalNode SKIP2() { return getToken(CobolPreprocessorParser.SKIP2, 0); }
		public TerminalNode SKIP3() { return getToken(CobolPreprocessorParser.SKIP3, 0); }
		public TerminalNode DOT() { return getToken(CobolPreprocessorParser.DOT, 0); }
		public SkipStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_skipStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).enterSkipStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).exitSkipStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof CobolPreprocessorVisitor) return ((CobolPreprocessorVisitor<? extends T>) visitor).visitSkipStatement(this);
			return visitor.visitChildren(this);
		}
	}

	public final SkipStatementContext skipStatement() throws RecognitionException {
		SkipStatementContext _localctx = new SkipStatementContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_skipStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(588);
			_la = _input.LA(1);
			if ( !(((_la - 239) & ~0x3f) == 0 && ((1L << (_la - 239)) & 7L) != 0) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(590);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,56,_ctx) ) {
			case 1:
				{
				setState(589);
				match(DOT);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TitleStatementContext extends ParserRuleContext {
		public TerminalNode TITLE() { return getToken(CobolPreprocessorParser.TITLE, 0); }
		public LiteralContext literal() {
			return getRuleContext(LiteralContext.class,0);
		}
		public TerminalNode DOT() { return getToken(CobolPreprocessorParser.DOT, 0); }
		public TitleStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_titleStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).enterTitleStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).exitTitleStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof CobolPreprocessorVisitor) return ((CobolPreprocessorVisitor<? extends T>) visitor).visitTitleStatement(this);
			return visitor.visitChildren(this);
		}
	}

	public final TitleStatementContext titleStatement() throws RecognitionException {
		TitleStatementContext _localctx = new TitleStatementContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_titleStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(592);
			match(TITLE);
			setState(593);
			literal();
			setState(595);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,57,_ctx) ) {
			case 1:
				{
				setState(594);
				match(DOT);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PseudoTextContext extends ParserRuleContext {
		public List<TerminalNode> DOUBLEEQUALCHAR() { return getTokens(CobolPreprocessorParser.DOUBLEEQUALCHAR); }
		public TerminalNode DOUBLEEQUALCHAR(int i) {
			return getToken(CobolPreprocessorParser.DOUBLEEQUALCHAR, i);
		}
		public CharDataContext charData() {
			return getRuleContext(CharDataContext.class,0);
		}
		public PseudoTextContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pseudoText; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).enterPseudoText(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).exitPseudoText(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof CobolPreprocessorVisitor) return ((CobolPreprocessorVisitor<? extends T>) visitor).visitPseudoText(this);
			return visitor.visitChildren(this);
		}
	}

	public final PseudoTextContext pseudoText() throws RecognitionException {
		PseudoTextContext _localctx = new PseudoTextContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_pseudoText);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(597);
			match(DOUBLEEQUALCHAR);
			setState(599);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la & ~0x3f) == 0 && ((1L << _la) & -2346656880870555650L) != 0) || (((_la - 64) & ~0x3f) == 0 && ((1L << (_la - 64)) & -16449L) != 0) || (((_la - 128) & ~0x3f) == 0 && ((1L << (_la - 128)) & -576460752303423489L) != 0) || (((_la - 192) & ~0x3f) == 0 && ((1L << (_la - 192)) & -2324912939422711809L) != 0) || (((_la - 256) & ~0x3f) == 0 && ((1L << (_la - 256)) & 2541345570543L) != 0)) {
				{
				setState(598);
				charData();
				}
			}

			setState(601);
			match(DOUBLEEQUALCHAR);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CharDataContext extends ParserRuleContext {
		public List<CharDataLineContext> charDataLine() {
			return getRuleContexts(CharDataLineContext.class);
		}
		public CharDataLineContext charDataLine(int i) {
			return getRuleContext(CharDataLineContext.class,i);
		}
		public CharDataContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_charData; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).enterCharData(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).exitCharData(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof CobolPreprocessorVisitor) return ((CobolPreprocessorVisitor<? extends T>) visitor).visitCharData(this);
			return visitor.visitChildren(this);
		}
	}

	public final CharDataContext charData() throws RecognitionException {
		CharDataContext _localctx = new CharDataContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_charData);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(604);
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(603);
					charDataLine();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(606);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,59,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CharDataSqlContext extends ParserRuleContext {
		public List<CharDataLineContext> charDataLine() {
			return getRuleContexts(CharDataLineContext.class);
		}
		public CharDataLineContext charDataLine(int i) {
			return getRuleContext(CharDataLineContext.class,i);
		}
		public List<TerminalNode> COPY() { return getTokens(CobolPreprocessorParser.COPY); }
		public TerminalNode COPY(int i) {
			return getToken(CobolPreprocessorParser.COPY, i);
		}
		public List<TerminalNode> REPLACE() { return getTokens(CobolPreprocessorParser.REPLACE); }
		public TerminalNode REPLACE(int i) {
			return getToken(CobolPreprocessorParser.REPLACE, i);
		}
		public CharDataSqlContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_charDataSql; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).enterCharDataSql(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).exitCharDataSql(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof CobolPreprocessorVisitor) return ((CobolPreprocessorVisitor<? extends T>) visitor).visitCharDataSql(this);
			return visitor.visitChildren(this);
		}
	}

	public final CharDataSqlContext charDataSql() throws RecognitionException {
		CharDataSqlContext _localctx = new CharDataSqlContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_charDataSql);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(611);
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				setState(611);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case ADATA:
				case ADV:
				case ALIAS:
				case ANSI:
				case ANY:
				case APOST:
				case AR:
				case ARITH:
				case AUTO:
				case AWO:
				case BIN:
				case BLOCK0:
				case BUF:
				case BUFSIZE:
				case BY:
				case CBL:
				case CBLCARD:
				case CO:
				case COBOL2:
				case COBOL3:
				case CODEPAGE:
				case COMPAT:
				case COMPILE:
				case CP:
				case CPP:
				case CPSM:
				case CS:
				case CURR:
				case CURRENCY:
				case DATA:
				case DATEPROC:
				case DBCS:
				case DD:
				case DEBUG:
				case DECK:
				case DIAGTRUNC:
				case DLI:
				case DLL:
				case DP:
				case DTR:
				case DU:
				case DUMP:
				case DYN:
				case DYNAM:
				case EDF:
				case EJPD:
				case EN:
				case ENGLISH:
				case EPILOG:
				case EXCI:
				case EXIT:
				case EXP:
				case EXPORTALL:
				case EXTEND:
				case FASTSRT:
				case FLAG:
				case FLAGSTD:
				case FSRT:
				case FULL:
				case GDS:
				case GRAPHIC:
				case HOOK:
				case IN:
				case INTDATE:
				case JA:
				case JP:
				case KA:
				case LANG:
				case LANGUAGE:
				case LC:
				case LENGTH:
				case LIB:
				case LILIAN:
				case LIN:
				case LINECOUNT:
				case LINKAGE:
				case LIST:
				case LM:
				case LONGMIXED:
				case LONGUPPER:
				case LPARENCHAR:
				case LU:
				case MAP:
				case MARGINS:
				case MAX:
				case MD:
				case MDECK:
				case MIG:
				case MIXED:
				case NAME:
				case NAT:
				case NATIONAL:
				case NATLANG:
				case NN:
				case NO:
				case NOADATA:
				case NOADV:
				case NOALIAS:
				case NOAWO:
				case NOBLOCK0:
				case NOC:
				case NOCBLCARD:
				case NOCICS:
				case NOCMPR2:
				case NOCOMPILE:
				case NOCPSM:
				case NOCURR:
				case NOCURRENCY:
				case NOD:
				case NODATEPROC:
				case NODBCS:
				case NODE:
				case NODEBUG:
				case NODECK:
				case NODIAGTRUNC:
				case NODLL:
				case NODU:
				case NODUMP:
				case NODP:
				case NODTR:
				case NODYN:
				case NODYNAM:
				case NOEDF:
				case NOEJPD:
				case NOEPILOG:
				case NOEXIT:
				case NOEXP:
				case NOEXPORTALL:
				case NOF:
				case NOFASTSRT:
				case NOFEPI:
				case NOFLAG:
				case NOFLAGMIG:
				case NOFLAGSTD:
				case NOFSRT:
				case NOGRAPHIC:
				case NOHOOK:
				case NOLENGTH:
				case NOLIB:
				case NOLINKAGE:
				case NOLIST:
				case NOMAP:
				case NOMD:
				case NOMDECK:
				case NONAME:
				case NONUM:
				case NONUMBER:
				case NOOBJ:
				case NOOBJECT:
				case NOOFF:
				case NOOFFSET:
				case NOOPSEQUENCE:
				case NOOPT:
				case NOOPTIMIZE:
				case NOOPTIONS:
				case NOP:
				case NOPFD:
				case NOPROLOG:
				case NORENT:
				case NOS:
				case NOSEP:
				case NOSEPARATE:
				case NOSEQ:
				case NOSOURCE:
				case NOSPIE:
				case NOSQL:
				case NOSQLC:
				case NOSQLCCSID:
				case NOSSR:
				case NOSSRANGE:
				case NOSTDTRUNC:
				case NOSEQUENCE:
				case NOTERM:
				case NOTERMINAL:
				case NOTEST:
				case NOTHREAD:
				case NOTRIG:
				case NOVBREF:
				case NOWORD:
				case NOX:
				case NOXREF:
				case NOZWB:
				case NS:
				case NSEQ:
				case NSYMBOL:
				case NUM:
				case NUMBER:
				case NUMPROC:
				case OBJ:
				case OBJECT:
				case OF:
				case OFF:
				case OFFSET:
				case ON:
				case OP:
				case OPMARGINS:
				case OPSEQUENCE:
				case OPT:
				case OPTFILE:
				case OPTIMIZE:
				case OPTIONS:
				case OUT:
				case OUTDD:
				case PFD:
				case PPTDBG:
				case PGMN:
				case PGMNAME:
				case PROCESS:
				case PROLOG:
				case QUOTE:
				case RENT:
				case REPLACING:
				case RMODE:
				case RPARENCHAR:
				case SEP:
				case SEPARATE:
				case SEQ:
				case SEQUENCE:
				case SHORT:
				case SIZE:
				case SOURCE:
				case SP:
				case SPACE:
				case SPIE:
				case SQL:
				case SQLC:
				case SQLCCSID:
				case SS:
				case SSR:
				case SSRANGE:
				case STD:
				case SYSEIB:
				case SZ:
				case TERM:
				case TERMINAL:
				case TEST:
				case THREAD:
				case TRIG:
				case TRUNC:
				case UE:
				case UPPER:
				case VBREF:
				case WD:
				case XMLPARSE:
				case XMLSS:
				case XOPTS:
				case XREF:
				case YEARWINDOW:
				case YW:
				case ZWB:
				case C_CHAR:
				case D_CHAR:
				case E_CHAR:
				case F_CHAR:
				case H_CHAR:
				case I_CHAR:
				case M_CHAR:
				case N_CHAR:
				case Q_CHAR:
				case S_CHAR:
				case U_CHAR:
				case W_CHAR:
				case X_CHAR:
				case COMMACHAR:
				case DOT:
				case NONNUMERICLITERAL:
				case INTEGERLITERAL:
				case NUMERICLITERAL:
				case IDENTIFIER:
				case FILENAME:
				case COMMENTENTRYLINE:
				case TEXT:
					{
					setState(608);
					charDataLine();
					}
					break;
				case COPY:
					{
					setState(609);
					match(COPY);
					}
					break;
				case REPLACE:
					{
					setState(610);
					match(REPLACE);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(613);
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( ((_la & ~0x3f) == 0 && ((1L << _la) & -2346656880837001218L) != 0) || (((_la - 64) & ~0x3f) == 0 && ((1L << (_la - 64)) & -16449L) != 0) || (((_la - 128) & ~0x3f) == 0 && ((1L << (_la - 128)) & -576460752303423489L) != 0) || (((_la - 192) & ~0x3f) == 0 && ((1L << (_la - 192)) & -2324912938885840897L) != 0) || (((_la - 256) & ~0x3f) == 0 && ((1L << (_la - 256)) & 2541345570543L) != 0) );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CharDataLineNoDotContext extends ParserRuleContext {
		public List<CobolWordContext> cobolWord() {
			return getRuleContexts(CobolWordContext.class);
		}
		public CobolWordContext cobolWord(int i) {
			return getRuleContext(CobolWordContext.class,i);
		}
		public List<LiteralContext> literal() {
			return getRuleContexts(LiteralContext.class);
		}
		public LiteralContext literal(int i) {
			return getRuleContext(LiteralContext.class,i);
		}
		public List<FilenameContext> filename() {
			return getRuleContexts(FilenameContext.class);
		}
		public FilenameContext filename(int i) {
			return getRuleContext(FilenameContext.class,i);
		}
		public List<CommentEntryContext> commentEntry() {
			return getRuleContexts(CommentEntryContext.class);
		}
		public CommentEntryContext commentEntry(int i) {
			return getRuleContext(CommentEntryContext.class,i);
		}
		public List<TerminalNode> TEXT() { return getTokens(CobolPreprocessorParser.TEXT); }
		public TerminalNode TEXT(int i) {
			return getToken(CobolPreprocessorParser.TEXT, i);
		}
		public List<TerminalNode> LPARENCHAR() { return getTokens(CobolPreprocessorParser.LPARENCHAR); }
		public TerminalNode LPARENCHAR(int i) {
			return getToken(CobolPreprocessorParser.LPARENCHAR, i);
		}
		public List<TerminalNode> RPARENCHAR() { return getTokens(CobolPreprocessorParser.RPARENCHAR); }
		public TerminalNode RPARENCHAR(int i) {
			return getToken(CobolPreprocessorParser.RPARENCHAR, i);
		}
		public CharDataLineNoDotContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_charDataLineNoDot; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).enterCharDataLineNoDot(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).exitCharDataLineNoDot(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof CobolPreprocessorVisitor) return ((CobolPreprocessorVisitor<? extends T>) visitor).visitCharDataLineNoDot(this);
			return visitor.visitChildren(this);
		}
	}

	public final CharDataLineNoDotContext charDataLineNoDot() throws RecognitionException {
		CharDataLineNoDotContext _localctx = new CharDataLineNoDotContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_charDataLineNoDot);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(622);
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					setState(622);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case ADATA:
					case ADV:
					case ALIAS:
					case ANSI:
					case ANY:
					case APOST:
					case AR:
					case ARITH:
					case AUTO:
					case AWO:
					case BIN:
					case BLOCK0:
					case BUF:
					case BUFSIZE:
					case BY:
					case CBL:
					case CBLCARD:
					case CO:
					case COBOL2:
					case COBOL3:
					case CODEPAGE:
					case COMPAT:
					case COMPILE:
					case CP:
					case CPP:
					case CPSM:
					case CS:
					case CURR:
					case CURRENCY:
					case DATA:
					case DATEPROC:
					case DBCS:
					case DD:
					case DEBUG:
					case DECK:
					case DIAGTRUNC:
					case DLI:
					case DLL:
					case DP:
					case DTR:
					case DU:
					case DUMP:
					case DYN:
					case DYNAM:
					case EDF:
					case EJPD:
					case EN:
					case ENGLISH:
					case EPILOG:
					case EXCI:
					case EXIT:
					case EXP:
					case EXPORTALL:
					case EXTEND:
					case FASTSRT:
					case FLAG:
					case FLAGSTD:
					case FSRT:
					case FULL:
					case GDS:
					case GRAPHIC:
					case HOOK:
					case IN:
					case INTDATE:
					case JA:
					case JP:
					case KA:
					case LANG:
					case LANGUAGE:
					case LC:
					case LENGTH:
					case LIB:
					case LILIAN:
					case LIN:
					case LINECOUNT:
					case LINKAGE:
					case LIST:
					case LM:
					case LONGMIXED:
					case LONGUPPER:
					case LU:
					case MAP:
					case MARGINS:
					case MAX:
					case MD:
					case MDECK:
					case MIG:
					case MIXED:
					case NAME:
					case NAT:
					case NATIONAL:
					case NATLANG:
					case NN:
					case NO:
					case NOADATA:
					case NOADV:
					case NOALIAS:
					case NOAWO:
					case NOBLOCK0:
					case NOC:
					case NOCBLCARD:
					case NOCICS:
					case NOCMPR2:
					case NOCOMPILE:
					case NOCPSM:
					case NOCURR:
					case NOCURRENCY:
					case NOD:
					case NODATEPROC:
					case NODBCS:
					case NODE:
					case NODEBUG:
					case NODECK:
					case NODIAGTRUNC:
					case NODLL:
					case NODU:
					case NODUMP:
					case NODP:
					case NODTR:
					case NODYN:
					case NODYNAM:
					case NOEDF:
					case NOEJPD:
					case NOEPILOG:
					case NOEXIT:
					case NOEXP:
					case NOEXPORTALL:
					case NOF:
					case NOFASTSRT:
					case NOFEPI:
					case NOFLAG:
					case NOFLAGMIG:
					case NOFLAGSTD:
					case NOFSRT:
					case NOGRAPHIC:
					case NOHOOK:
					case NOLENGTH:
					case NOLIB:
					case NOLINKAGE:
					case NOLIST:
					case NOMAP:
					case NOMD:
					case NOMDECK:
					case NONAME:
					case NONUM:
					case NONUMBER:
					case NOOBJ:
					case NOOBJECT:
					case NOOFF:
					case NOOFFSET:
					case NOOPSEQUENCE:
					case NOOPT:
					case NOOPTIMIZE:
					case NOOPTIONS:
					case NOP:
					case NOPFD:
					case NOPROLOG:
					case NORENT:
					case NOS:
					case NOSEP:
					case NOSEPARATE:
					case NOSEQ:
					case NOSOURCE:
					case NOSPIE:
					case NOSQL:
					case NOSQLC:
					case NOSQLCCSID:
					case NOSSR:
					case NOSSRANGE:
					case NOSTDTRUNC:
					case NOSEQUENCE:
					case NOTERM:
					case NOTERMINAL:
					case NOTEST:
					case NOTHREAD:
					case NOTRIG:
					case NOVBREF:
					case NOWORD:
					case NOX:
					case NOXREF:
					case NOZWB:
					case NS:
					case NSEQ:
					case NSYMBOL:
					case NUM:
					case NUMBER:
					case NUMPROC:
					case OBJ:
					case OBJECT:
					case OF:
					case OFF:
					case OFFSET:
					case ON:
					case OP:
					case OPMARGINS:
					case OPSEQUENCE:
					case OPT:
					case OPTFILE:
					case OPTIMIZE:
					case OPTIONS:
					case OUT:
					case OUTDD:
					case PFD:
					case PPTDBG:
					case PGMN:
					case PGMNAME:
					case PROCESS:
					case PROLOG:
					case QUOTE:
					case RENT:
					case REPLACING:
					case RMODE:
					case SEP:
					case SEPARATE:
					case SEQ:
					case SEQUENCE:
					case SHORT:
					case SIZE:
					case SOURCE:
					case SP:
					case SPACE:
					case SPIE:
					case SQL:
					case SQLC:
					case SQLCCSID:
					case SS:
					case SSR:
					case SSRANGE:
					case STD:
					case SYSEIB:
					case SZ:
					case TERM:
					case TERMINAL:
					case TEST:
					case THREAD:
					case TRIG:
					case TRUNC:
					case UE:
					case UPPER:
					case VBREF:
					case WD:
					case XMLPARSE:
					case XMLSS:
					case XOPTS:
					case XREF:
					case YEARWINDOW:
					case YW:
					case ZWB:
					case C_CHAR:
					case D_CHAR:
					case E_CHAR:
					case F_CHAR:
					case H_CHAR:
					case I_CHAR:
					case M_CHAR:
					case N_CHAR:
					case Q_CHAR:
					case S_CHAR:
					case U_CHAR:
					case W_CHAR:
					case X_CHAR:
					case COMMACHAR:
					case IDENTIFIER:
						{
						setState(615);
						cobolWord();
						}
						break;
					case NONNUMERICLITERAL:
					case INTEGERLITERAL:
					case NUMERICLITERAL:
						{
						setState(616);
						literal();
						}
						break;
					case FILENAME:
						{
						setState(617);
						filename();
						}
						break;
					case COMMENTENTRYLINE:
						{
						setState(618);
						commentEntry();
						}
						break;
					case TEXT:
						{
						setState(619);
						match(TEXT);
						}
						break;
					case LPARENCHAR:
						{
						setState(620);
						match(LPARENCHAR);
						}
						break;
					case RPARENCHAR:
						{
						setState(621);
						match(RPARENCHAR);
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(624);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,63,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CharDataLineContext extends ParserRuleContext {
		public List<CobolWordContext> cobolWord() {
			return getRuleContexts(CobolWordContext.class);
		}
		public CobolWordContext cobolWord(int i) {
			return getRuleContext(CobolWordContext.class,i);
		}
		public List<LiteralContext> literal() {
			return getRuleContexts(LiteralContext.class);
		}
		public LiteralContext literal(int i) {
			return getRuleContext(LiteralContext.class,i);
		}
		public List<FilenameContext> filename() {
			return getRuleContexts(FilenameContext.class);
		}
		public FilenameContext filename(int i) {
			return getRuleContext(FilenameContext.class,i);
		}
		public List<CommentEntryContext> commentEntry() {
			return getRuleContexts(CommentEntryContext.class);
		}
		public CommentEntryContext commentEntry(int i) {
			return getRuleContext(CommentEntryContext.class,i);
		}
		public List<TerminalNode> TEXT() { return getTokens(CobolPreprocessorParser.TEXT); }
		public TerminalNode TEXT(int i) {
			return getToken(CobolPreprocessorParser.TEXT, i);
		}
		public List<TerminalNode> DOT() { return getTokens(CobolPreprocessorParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(CobolPreprocessorParser.DOT, i);
		}
		public List<TerminalNode> LPARENCHAR() { return getTokens(CobolPreprocessorParser.LPARENCHAR); }
		public TerminalNode LPARENCHAR(int i) {
			return getToken(CobolPreprocessorParser.LPARENCHAR, i);
		}
		public List<TerminalNode> RPARENCHAR() { return getTokens(CobolPreprocessorParser.RPARENCHAR); }
		public TerminalNode RPARENCHAR(int i) {
			return getToken(CobolPreprocessorParser.RPARENCHAR, i);
		}
		public CharDataLineContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_charDataLine; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).enterCharDataLine(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).exitCharDataLine(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof CobolPreprocessorVisitor) return ((CobolPreprocessorVisitor<? extends T>) visitor).visitCharDataLine(this);
			return visitor.visitChildren(this);
		}
	}

	public final CharDataLineContext charDataLine() throws RecognitionException {
		CharDataLineContext _localctx = new CharDataLineContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_charDataLine);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(634);
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					setState(634);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case ADATA:
					case ADV:
					case ALIAS:
					case ANSI:
					case ANY:
					case APOST:
					case AR:
					case ARITH:
					case AUTO:
					case AWO:
					case BIN:
					case BLOCK0:
					case BUF:
					case BUFSIZE:
					case BY:
					case CBL:
					case CBLCARD:
					case CO:
					case COBOL2:
					case COBOL3:
					case CODEPAGE:
					case COMPAT:
					case COMPILE:
					case CP:
					case CPP:
					case CPSM:
					case CS:
					case CURR:
					case CURRENCY:
					case DATA:
					case DATEPROC:
					case DBCS:
					case DD:
					case DEBUG:
					case DECK:
					case DIAGTRUNC:
					case DLI:
					case DLL:
					case DP:
					case DTR:
					case DU:
					case DUMP:
					case DYN:
					case DYNAM:
					case EDF:
					case EJPD:
					case EN:
					case ENGLISH:
					case EPILOG:
					case EXCI:
					case EXIT:
					case EXP:
					case EXPORTALL:
					case EXTEND:
					case FASTSRT:
					case FLAG:
					case FLAGSTD:
					case FSRT:
					case FULL:
					case GDS:
					case GRAPHIC:
					case HOOK:
					case IN:
					case INTDATE:
					case JA:
					case JP:
					case KA:
					case LANG:
					case LANGUAGE:
					case LC:
					case LENGTH:
					case LIB:
					case LILIAN:
					case LIN:
					case LINECOUNT:
					case LINKAGE:
					case LIST:
					case LM:
					case LONGMIXED:
					case LONGUPPER:
					case LU:
					case MAP:
					case MARGINS:
					case MAX:
					case MD:
					case MDECK:
					case MIG:
					case MIXED:
					case NAME:
					case NAT:
					case NATIONAL:
					case NATLANG:
					case NN:
					case NO:
					case NOADATA:
					case NOADV:
					case NOALIAS:
					case NOAWO:
					case NOBLOCK0:
					case NOC:
					case NOCBLCARD:
					case NOCICS:
					case NOCMPR2:
					case NOCOMPILE:
					case NOCPSM:
					case NOCURR:
					case NOCURRENCY:
					case NOD:
					case NODATEPROC:
					case NODBCS:
					case NODE:
					case NODEBUG:
					case NODECK:
					case NODIAGTRUNC:
					case NODLL:
					case NODU:
					case NODUMP:
					case NODP:
					case NODTR:
					case NODYN:
					case NODYNAM:
					case NOEDF:
					case NOEJPD:
					case NOEPILOG:
					case NOEXIT:
					case NOEXP:
					case NOEXPORTALL:
					case NOF:
					case NOFASTSRT:
					case NOFEPI:
					case NOFLAG:
					case NOFLAGMIG:
					case NOFLAGSTD:
					case NOFSRT:
					case NOGRAPHIC:
					case NOHOOK:
					case NOLENGTH:
					case NOLIB:
					case NOLINKAGE:
					case NOLIST:
					case NOMAP:
					case NOMD:
					case NOMDECK:
					case NONAME:
					case NONUM:
					case NONUMBER:
					case NOOBJ:
					case NOOBJECT:
					case NOOFF:
					case NOOFFSET:
					case NOOPSEQUENCE:
					case NOOPT:
					case NOOPTIMIZE:
					case NOOPTIONS:
					case NOP:
					case NOPFD:
					case NOPROLOG:
					case NORENT:
					case NOS:
					case NOSEP:
					case NOSEPARATE:
					case NOSEQ:
					case NOSOURCE:
					case NOSPIE:
					case NOSQL:
					case NOSQLC:
					case NOSQLCCSID:
					case NOSSR:
					case NOSSRANGE:
					case NOSTDTRUNC:
					case NOSEQUENCE:
					case NOTERM:
					case NOTERMINAL:
					case NOTEST:
					case NOTHREAD:
					case NOTRIG:
					case NOVBREF:
					case NOWORD:
					case NOX:
					case NOXREF:
					case NOZWB:
					case NS:
					case NSEQ:
					case NSYMBOL:
					case NUM:
					case NUMBER:
					case NUMPROC:
					case OBJ:
					case OBJECT:
					case OF:
					case OFF:
					case OFFSET:
					case ON:
					case OP:
					case OPMARGINS:
					case OPSEQUENCE:
					case OPT:
					case OPTFILE:
					case OPTIMIZE:
					case OPTIONS:
					case OUT:
					case OUTDD:
					case PFD:
					case PPTDBG:
					case PGMN:
					case PGMNAME:
					case PROCESS:
					case PROLOG:
					case QUOTE:
					case RENT:
					case REPLACING:
					case RMODE:
					case SEP:
					case SEPARATE:
					case SEQ:
					case SEQUENCE:
					case SHORT:
					case SIZE:
					case SOURCE:
					case SP:
					case SPACE:
					case SPIE:
					case SQL:
					case SQLC:
					case SQLCCSID:
					case SS:
					case SSR:
					case SSRANGE:
					case STD:
					case SYSEIB:
					case SZ:
					case TERM:
					case TERMINAL:
					case TEST:
					case THREAD:
					case TRIG:
					case TRUNC:
					case UE:
					case UPPER:
					case VBREF:
					case WD:
					case XMLPARSE:
					case XMLSS:
					case XOPTS:
					case XREF:
					case YEARWINDOW:
					case YW:
					case ZWB:
					case C_CHAR:
					case D_CHAR:
					case E_CHAR:
					case F_CHAR:
					case H_CHAR:
					case I_CHAR:
					case M_CHAR:
					case N_CHAR:
					case Q_CHAR:
					case S_CHAR:
					case U_CHAR:
					case W_CHAR:
					case X_CHAR:
					case COMMACHAR:
					case IDENTIFIER:
						{
						setState(626);
						cobolWord();
						}
						break;
					case NONNUMERICLITERAL:
					case INTEGERLITERAL:
					case NUMERICLITERAL:
						{
						setState(627);
						literal();
						}
						break;
					case FILENAME:
						{
						setState(628);
						filename();
						}
						break;
					case COMMENTENTRYLINE:
						{
						setState(629);
						commentEntry();
						}
						break;
					case TEXT:
						{
						setState(630);
						match(TEXT);
						}
						break;
					case DOT:
						{
						setState(631);
						match(DOT);
						}
						break;
					case LPARENCHAR:
						{
						setState(632);
						match(LPARENCHAR);
						}
						break;
					case RPARENCHAR:
						{
						setState(633);
						match(RPARENCHAR);
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(636);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,65,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SubscriptContext extends ParserRuleContext {
		public TerminalNode LPARENCHAR() { return getToken(CobolPreprocessorParser.LPARENCHAR, 0); }
		public TerminalNode RPARENCHAR() { return getToken(CobolPreprocessorParser.RPARENCHAR, 0); }
		public List<LiteralContext> literal() {
			return getRuleContexts(LiteralContext.class);
		}
		public LiteralContext literal(int i) {
			return getRuleContext(LiteralContext.class,i);
		}
		public List<CobolWordContext> cobolWord() {
			return getRuleContexts(CobolWordContext.class);
		}
		public CobolWordContext cobolWord(int i) {
			return getRuleContext(CobolWordContext.class,i);
		}
		public List<TerminalNode> COMMACHAR() { return getTokens(CobolPreprocessorParser.COMMACHAR); }
		public TerminalNode COMMACHAR(int i) {
			return getToken(CobolPreprocessorParser.COMMACHAR, i);
		}
		public SubscriptContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_subscript; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).enterSubscript(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).exitSubscript(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof CobolPreprocessorVisitor) return ((CobolPreprocessorVisitor<? extends T>) visitor).visitSubscript(this);
			return visitor.visitChildren(this);
		}
	}

	public final SubscriptContext subscript() throws RecognitionException {
		SubscriptContext _localctx = new SubscriptContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_subscript);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(638);
			match(LPARENCHAR);
			setState(641);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NONNUMERICLITERAL:
			case INTEGERLITERAL:
			case NUMERICLITERAL:
				{
				setState(639);
				literal();
				}
				break;
			case ADATA:
			case ADV:
			case ALIAS:
			case ANSI:
			case ANY:
			case APOST:
			case AR:
			case ARITH:
			case AUTO:
			case AWO:
			case BIN:
			case BLOCK0:
			case BUF:
			case BUFSIZE:
			case BY:
			case CBL:
			case CBLCARD:
			case CO:
			case COBOL2:
			case COBOL3:
			case CODEPAGE:
			case COMPAT:
			case COMPILE:
			case CP:
			case CPP:
			case CPSM:
			case CS:
			case CURR:
			case CURRENCY:
			case DATA:
			case DATEPROC:
			case DBCS:
			case DD:
			case DEBUG:
			case DECK:
			case DIAGTRUNC:
			case DLI:
			case DLL:
			case DP:
			case DTR:
			case DU:
			case DUMP:
			case DYN:
			case DYNAM:
			case EDF:
			case EJPD:
			case EN:
			case ENGLISH:
			case EPILOG:
			case EXCI:
			case EXIT:
			case EXP:
			case EXPORTALL:
			case EXTEND:
			case FASTSRT:
			case FLAG:
			case FLAGSTD:
			case FSRT:
			case FULL:
			case GDS:
			case GRAPHIC:
			case HOOK:
			case IN:
			case INTDATE:
			case JA:
			case JP:
			case KA:
			case LANG:
			case LANGUAGE:
			case LC:
			case LENGTH:
			case LIB:
			case LILIAN:
			case LIN:
			case LINECOUNT:
			case LINKAGE:
			case LIST:
			case LM:
			case LONGMIXED:
			case LONGUPPER:
			case LU:
			case MAP:
			case MARGINS:
			case MAX:
			case MD:
			case MDECK:
			case MIG:
			case MIXED:
			case NAME:
			case NAT:
			case NATIONAL:
			case NATLANG:
			case NN:
			case NO:
			case NOADATA:
			case NOADV:
			case NOALIAS:
			case NOAWO:
			case NOBLOCK0:
			case NOC:
			case NOCBLCARD:
			case NOCICS:
			case NOCMPR2:
			case NOCOMPILE:
			case NOCPSM:
			case NOCURR:
			case NOCURRENCY:
			case NOD:
			case NODATEPROC:
			case NODBCS:
			case NODE:
			case NODEBUG:
			case NODECK:
			case NODIAGTRUNC:
			case NODLL:
			case NODU:
			case NODUMP:
			case NODP:
			case NODTR:
			case NODYN:
			case NODYNAM:
			case NOEDF:
			case NOEJPD:
			case NOEPILOG:
			case NOEXIT:
			case NOEXP:
			case NOEXPORTALL:
			case NOF:
			case NOFASTSRT:
			case NOFEPI:
			case NOFLAG:
			case NOFLAGMIG:
			case NOFLAGSTD:
			case NOFSRT:
			case NOGRAPHIC:
			case NOHOOK:
			case NOLENGTH:
			case NOLIB:
			case NOLINKAGE:
			case NOLIST:
			case NOMAP:
			case NOMD:
			case NOMDECK:
			case NONAME:
			case NONUM:
			case NONUMBER:
			case NOOBJ:
			case NOOBJECT:
			case NOOFF:
			case NOOFFSET:
			case NOOPSEQUENCE:
			case NOOPT:
			case NOOPTIMIZE:
			case NOOPTIONS:
			case NOP:
			case NOPFD:
			case NOPROLOG:
			case NORENT:
			case NOS:
			case NOSEP:
			case NOSEPARATE:
			case NOSEQ:
			case NOSOURCE:
			case NOSPIE:
			case NOSQL:
			case NOSQLC:
			case NOSQLCCSID:
			case NOSSR:
			case NOSSRANGE:
			case NOSTDTRUNC:
			case NOSEQUENCE:
			case NOTERM:
			case NOTERMINAL:
			case NOTEST:
			case NOTHREAD:
			case NOTRIG:
			case NOVBREF:
			case NOWORD:
			case NOX:
			case NOXREF:
			case NOZWB:
			case NS:
			case NSEQ:
			case NSYMBOL:
			case NUM:
			case NUMBER:
			case NUMPROC:
			case OBJ:
			case OBJECT:
			case OF:
			case OFF:
			case OFFSET:
			case ON:
			case OP:
			case OPMARGINS:
			case OPSEQUENCE:
			case OPT:
			case OPTFILE:
			case OPTIMIZE:
			case OPTIONS:
			case OUT:
			case OUTDD:
			case PFD:
			case PPTDBG:
			case PGMN:
			case PGMNAME:
			case PROCESS:
			case PROLOG:
			case QUOTE:
			case RENT:
			case REPLACING:
			case RMODE:
			case SEP:
			case SEPARATE:
			case SEQ:
			case SEQUENCE:
			case SHORT:
			case SIZE:
			case SOURCE:
			case SP:
			case SPACE:
			case SPIE:
			case SQL:
			case SQLC:
			case SQLCCSID:
			case SS:
			case SSR:
			case SSRANGE:
			case STD:
			case SYSEIB:
			case SZ:
			case TERM:
			case TERMINAL:
			case TEST:
			case THREAD:
			case TRIG:
			case TRUNC:
			case UE:
			case UPPER:
			case VBREF:
			case WD:
			case XMLPARSE:
			case XMLSS:
			case XOPTS:
			case XREF:
			case YEARWINDOW:
			case YW:
			case ZWB:
			case C_CHAR:
			case D_CHAR:
			case E_CHAR:
			case F_CHAR:
			case H_CHAR:
			case I_CHAR:
			case M_CHAR:
			case N_CHAR:
			case Q_CHAR:
			case S_CHAR:
			case U_CHAR:
			case W_CHAR:
			case X_CHAR:
			case COMMACHAR:
			case IDENTIFIER:
				{
				setState(640);
				cobolWord();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(652);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((_la & ~0x3f) == 0 && ((1L << _la) & -2346656880870555650L) != 0) || (((_la - 64) & ~0x3f) == 0 && ((1L << (_la - 64)) & -33570881L) != 0) || (((_la - 128) & ~0x3f) == 0 && ((1L << (_la - 128)) & -576460752303423489L) != 0) || (((_la - 192) & ~0x3f) == 0 && ((1L << (_la - 192)) & -2324912943717679105L) != 0) || (((_la - 256) & ~0x3f) == 0 && ((1L << (_la - 256)) & 32547798767L) != 0)) {
				{
				{
				setState(644);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,67,_ctx) ) {
				case 1:
					{
					setState(643);
					match(COMMACHAR);
					}
					break;
				}
				setState(648);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case NONNUMERICLITERAL:
				case INTEGERLITERAL:
				case NUMERICLITERAL:
					{
					setState(646);
					literal();
					}
					break;
				case ADATA:
				case ADV:
				case ALIAS:
				case ANSI:
				case ANY:
				case APOST:
				case AR:
				case ARITH:
				case AUTO:
				case AWO:
				case BIN:
				case BLOCK0:
				case BUF:
				case BUFSIZE:
				case BY:
				case CBL:
				case CBLCARD:
				case CO:
				case COBOL2:
				case COBOL3:
				case CODEPAGE:
				case COMPAT:
				case COMPILE:
				case CP:
				case CPP:
				case CPSM:
				case CS:
				case CURR:
				case CURRENCY:
				case DATA:
				case DATEPROC:
				case DBCS:
				case DD:
				case DEBUG:
				case DECK:
				case DIAGTRUNC:
				case DLI:
				case DLL:
				case DP:
				case DTR:
				case DU:
				case DUMP:
				case DYN:
				case DYNAM:
				case EDF:
				case EJPD:
				case EN:
				case ENGLISH:
				case EPILOG:
				case EXCI:
				case EXIT:
				case EXP:
				case EXPORTALL:
				case EXTEND:
				case FASTSRT:
				case FLAG:
				case FLAGSTD:
				case FSRT:
				case FULL:
				case GDS:
				case GRAPHIC:
				case HOOK:
				case IN:
				case INTDATE:
				case JA:
				case JP:
				case KA:
				case LANG:
				case LANGUAGE:
				case LC:
				case LENGTH:
				case LIB:
				case LILIAN:
				case LIN:
				case LINECOUNT:
				case LINKAGE:
				case LIST:
				case LM:
				case LONGMIXED:
				case LONGUPPER:
				case LU:
				case MAP:
				case MARGINS:
				case MAX:
				case MD:
				case MDECK:
				case MIG:
				case MIXED:
				case NAME:
				case NAT:
				case NATIONAL:
				case NATLANG:
				case NN:
				case NO:
				case NOADATA:
				case NOADV:
				case NOALIAS:
				case NOAWO:
				case NOBLOCK0:
				case NOC:
				case NOCBLCARD:
				case NOCICS:
				case NOCMPR2:
				case NOCOMPILE:
				case NOCPSM:
				case NOCURR:
				case NOCURRENCY:
				case NOD:
				case NODATEPROC:
				case NODBCS:
				case NODE:
				case NODEBUG:
				case NODECK:
				case NODIAGTRUNC:
				case NODLL:
				case NODU:
				case NODUMP:
				case NODP:
				case NODTR:
				case NODYN:
				case NODYNAM:
				case NOEDF:
				case NOEJPD:
				case NOEPILOG:
				case NOEXIT:
				case NOEXP:
				case NOEXPORTALL:
				case NOF:
				case NOFASTSRT:
				case NOFEPI:
				case NOFLAG:
				case NOFLAGMIG:
				case NOFLAGSTD:
				case NOFSRT:
				case NOGRAPHIC:
				case NOHOOK:
				case NOLENGTH:
				case NOLIB:
				case NOLINKAGE:
				case NOLIST:
				case NOMAP:
				case NOMD:
				case NOMDECK:
				case NONAME:
				case NONUM:
				case NONUMBER:
				case NOOBJ:
				case NOOBJECT:
				case NOOFF:
				case NOOFFSET:
				case NOOPSEQUENCE:
				case NOOPT:
				case NOOPTIMIZE:
				case NOOPTIONS:
				case NOP:
				case NOPFD:
				case NOPROLOG:
				case NORENT:
				case NOS:
				case NOSEP:
				case NOSEPARATE:
				case NOSEQ:
				case NOSOURCE:
				case NOSPIE:
				case NOSQL:
				case NOSQLC:
				case NOSQLCCSID:
				case NOSSR:
				case NOSSRANGE:
				case NOSTDTRUNC:
				case NOSEQUENCE:
				case NOTERM:
				case NOTERMINAL:
				case NOTEST:
				case NOTHREAD:
				case NOTRIG:
				case NOVBREF:
				case NOWORD:
				case NOX:
				case NOXREF:
				case NOZWB:
				case NS:
				case NSEQ:
				case NSYMBOL:
				case NUM:
				case NUMBER:
				case NUMPROC:
				case OBJ:
				case OBJECT:
				case OF:
				case OFF:
				case OFFSET:
				case ON:
				case OP:
				case OPMARGINS:
				case OPSEQUENCE:
				case OPT:
				case OPTFILE:
				case OPTIMIZE:
				case OPTIONS:
				case OUT:
				case OUTDD:
				case PFD:
				case PPTDBG:
				case PGMN:
				case PGMNAME:
				case PROCESS:
				case PROLOG:
				case QUOTE:
				case RENT:
				case REPLACING:
				case RMODE:
				case SEP:
				case SEPARATE:
				case SEQ:
				case SEQUENCE:
				case SHORT:
				case SIZE:
				case SOURCE:
				case SP:
				case SPACE:
				case SPIE:
				case SQL:
				case SQLC:
				case SQLCCSID:
				case SS:
				case SSR:
				case SSRANGE:
				case STD:
				case SYSEIB:
				case SZ:
				case TERM:
				case TERMINAL:
				case TEST:
				case THREAD:
				case TRIG:
				case TRUNC:
				case UE:
				case UPPER:
				case VBREF:
				case WD:
				case XMLPARSE:
				case XMLSS:
				case XOPTS:
				case XREF:
				case YEARWINDOW:
				case YW:
				case ZWB:
				case C_CHAR:
				case D_CHAR:
				case E_CHAR:
				case F_CHAR:
				case H_CHAR:
				case I_CHAR:
				case M_CHAR:
				case N_CHAR:
				case Q_CHAR:
				case S_CHAR:
				case U_CHAR:
				case W_CHAR:
				case X_CHAR:
				case COMMACHAR:
				case IDENTIFIER:
					{
					setState(647);
					cobolWord();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				}
				setState(654);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(655);
			match(RPARENCHAR);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CobolWordContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(CobolPreprocessorParser.IDENTIFIER, 0); }
		public CharDataKeywordContext charDataKeyword() {
			return getRuleContext(CharDataKeywordContext.class,0);
		}
		public CobolWordContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_cobolWord; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).enterCobolWord(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).exitCobolWord(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof CobolPreprocessorVisitor) return ((CobolPreprocessorVisitor<? extends T>) visitor).visitCobolWord(this);
			return visitor.visitChildren(this);
		}
	}

	public final CobolWordContext cobolWord() throws RecognitionException {
		CobolWordContext _localctx = new CobolWordContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_cobolWord);
		try {
			setState(659);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IDENTIFIER:
				enterOuterAlt(_localctx, 1);
				{
				setState(657);
				match(IDENTIFIER);
				}
				break;
			case ADATA:
			case ADV:
			case ALIAS:
			case ANSI:
			case ANY:
			case APOST:
			case AR:
			case ARITH:
			case AUTO:
			case AWO:
			case BIN:
			case BLOCK0:
			case BUF:
			case BUFSIZE:
			case BY:
			case CBL:
			case CBLCARD:
			case CO:
			case COBOL2:
			case COBOL3:
			case CODEPAGE:
			case COMPAT:
			case COMPILE:
			case CP:
			case CPP:
			case CPSM:
			case CS:
			case CURR:
			case CURRENCY:
			case DATA:
			case DATEPROC:
			case DBCS:
			case DD:
			case DEBUG:
			case DECK:
			case DIAGTRUNC:
			case DLI:
			case DLL:
			case DP:
			case DTR:
			case DU:
			case DUMP:
			case DYN:
			case DYNAM:
			case EDF:
			case EJPD:
			case EN:
			case ENGLISH:
			case EPILOG:
			case EXCI:
			case EXIT:
			case EXP:
			case EXPORTALL:
			case EXTEND:
			case FASTSRT:
			case FLAG:
			case FLAGSTD:
			case FSRT:
			case FULL:
			case GDS:
			case GRAPHIC:
			case HOOK:
			case IN:
			case INTDATE:
			case JA:
			case JP:
			case KA:
			case LANG:
			case LANGUAGE:
			case LC:
			case LENGTH:
			case LIB:
			case LILIAN:
			case LIN:
			case LINECOUNT:
			case LINKAGE:
			case LIST:
			case LM:
			case LONGMIXED:
			case LONGUPPER:
			case LU:
			case MAP:
			case MARGINS:
			case MAX:
			case MD:
			case MDECK:
			case MIG:
			case MIXED:
			case NAME:
			case NAT:
			case NATIONAL:
			case NATLANG:
			case NN:
			case NO:
			case NOADATA:
			case NOADV:
			case NOALIAS:
			case NOAWO:
			case NOBLOCK0:
			case NOC:
			case NOCBLCARD:
			case NOCICS:
			case NOCMPR2:
			case NOCOMPILE:
			case NOCPSM:
			case NOCURR:
			case NOCURRENCY:
			case NOD:
			case NODATEPROC:
			case NODBCS:
			case NODE:
			case NODEBUG:
			case NODECK:
			case NODIAGTRUNC:
			case NODLL:
			case NODU:
			case NODUMP:
			case NODP:
			case NODTR:
			case NODYN:
			case NODYNAM:
			case NOEDF:
			case NOEJPD:
			case NOEPILOG:
			case NOEXIT:
			case NOEXP:
			case NOEXPORTALL:
			case NOF:
			case NOFASTSRT:
			case NOFEPI:
			case NOFLAG:
			case NOFLAGMIG:
			case NOFLAGSTD:
			case NOFSRT:
			case NOGRAPHIC:
			case NOHOOK:
			case NOLENGTH:
			case NOLIB:
			case NOLINKAGE:
			case NOLIST:
			case NOMAP:
			case NOMD:
			case NOMDECK:
			case NONAME:
			case NONUM:
			case NONUMBER:
			case NOOBJ:
			case NOOBJECT:
			case NOOFF:
			case NOOFFSET:
			case NOOPSEQUENCE:
			case NOOPT:
			case NOOPTIMIZE:
			case NOOPTIONS:
			case NOP:
			case NOPFD:
			case NOPROLOG:
			case NORENT:
			case NOS:
			case NOSEP:
			case NOSEPARATE:
			case NOSEQ:
			case NOSOURCE:
			case NOSPIE:
			case NOSQL:
			case NOSQLC:
			case NOSQLCCSID:
			case NOSSR:
			case NOSSRANGE:
			case NOSTDTRUNC:
			case NOSEQUENCE:
			case NOTERM:
			case NOTERMINAL:
			case NOTEST:
			case NOTHREAD:
			case NOTRIG:
			case NOVBREF:
			case NOWORD:
			case NOX:
			case NOXREF:
			case NOZWB:
			case NS:
			case NSEQ:
			case NSYMBOL:
			case NUM:
			case NUMBER:
			case NUMPROC:
			case OBJ:
			case OBJECT:
			case OF:
			case OFF:
			case OFFSET:
			case ON:
			case OP:
			case OPMARGINS:
			case OPSEQUENCE:
			case OPT:
			case OPTFILE:
			case OPTIMIZE:
			case OPTIONS:
			case OUT:
			case OUTDD:
			case PFD:
			case PPTDBG:
			case PGMN:
			case PGMNAME:
			case PROCESS:
			case PROLOG:
			case QUOTE:
			case RENT:
			case REPLACING:
			case RMODE:
			case SEP:
			case SEPARATE:
			case SEQ:
			case SEQUENCE:
			case SHORT:
			case SIZE:
			case SOURCE:
			case SP:
			case SPACE:
			case SPIE:
			case SQL:
			case SQLC:
			case SQLCCSID:
			case SS:
			case SSR:
			case SSRANGE:
			case STD:
			case SYSEIB:
			case SZ:
			case TERM:
			case TERMINAL:
			case TEST:
			case THREAD:
			case TRIG:
			case TRUNC:
			case UE:
			case UPPER:
			case VBREF:
			case WD:
			case XMLPARSE:
			case XMLSS:
			case XOPTS:
			case XREF:
			case YEARWINDOW:
			case YW:
			case ZWB:
			case C_CHAR:
			case D_CHAR:
			case E_CHAR:
			case F_CHAR:
			case H_CHAR:
			case I_CHAR:
			case M_CHAR:
			case N_CHAR:
			case Q_CHAR:
			case S_CHAR:
			case U_CHAR:
			case W_CHAR:
			case X_CHAR:
			case COMMACHAR:
				enterOuterAlt(_localctx, 2);
				{
				setState(658);
				charDataKeyword();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LiteralContext extends ParserRuleContext {
		public TerminalNode NONNUMERICLITERAL() { return getToken(CobolPreprocessorParser.NONNUMERICLITERAL, 0); }
		public TerminalNode NUMERICLITERAL() { return getToken(CobolPreprocessorParser.NUMERICLITERAL, 0); }
		public TerminalNode INTEGERLITERAL() { return getToken(CobolPreprocessorParser.INTEGERLITERAL, 0); }
		public LiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_literal; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).enterLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).exitLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof CobolPreprocessorVisitor) return ((CobolPreprocessorVisitor<? extends T>) visitor).visitLiteral(this);
			return visitor.visitChildren(this);
		}
	}

	public final LiteralContext literal() throws RecognitionException {
		LiteralContext _localctx = new LiteralContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_literal);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(661);
			_la = _input.LA(1);
			if ( !(((_la - 287) & ~0x3f) == 0 && ((1L << (_la - 287)) & 7L) != 0) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FilenameContext extends ParserRuleContext {
		public TerminalNode FILENAME() { return getToken(CobolPreprocessorParser.FILENAME, 0); }
		public FilenameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_filename; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).enterFilename(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).exitFilename(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof CobolPreprocessorVisitor) return ((CobolPreprocessorVisitor<? extends T>) visitor).visitFilename(this);
			return visitor.visitChildren(this);
		}
	}

	public final FilenameContext filename() throws RecognitionException {
		FilenameContext _localctx = new FilenameContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_filename);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(663);
			match(FILENAME);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CommentEntryContext extends ParserRuleContext {
		public List<TerminalNode> COMMENTENTRYLINE() { return getTokens(CobolPreprocessorParser.COMMENTENTRYLINE); }
		public TerminalNode COMMENTENTRYLINE(int i) {
			return getToken(CobolPreprocessorParser.COMMENTENTRYLINE, i);
		}
		public CommentEntryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_commentEntry; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).enterCommentEntry(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).exitCommentEntry(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof CobolPreprocessorVisitor) return ((CobolPreprocessorVisitor<? extends T>) visitor).visitCommentEntry(this);
			return visitor.visitChildren(this);
		}
	}

	public final CommentEntryContext commentEntry() throws RecognitionException {
		CommentEntryContext _localctx = new CommentEntryContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_commentEntry);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(666);
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(665);
					match(COMMENTENTRYLINE);
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(668);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,71,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CharDataKeywordContext extends ParserRuleContext {
		public TerminalNode ADATA() { return getToken(CobolPreprocessorParser.ADATA, 0); }
		public TerminalNode ADV() { return getToken(CobolPreprocessorParser.ADV, 0); }
		public TerminalNode ALIAS() { return getToken(CobolPreprocessorParser.ALIAS, 0); }
		public TerminalNode ANSI() { return getToken(CobolPreprocessorParser.ANSI, 0); }
		public TerminalNode ANY() { return getToken(CobolPreprocessorParser.ANY, 0); }
		public TerminalNode APOST() { return getToken(CobolPreprocessorParser.APOST, 0); }
		public TerminalNode AR() { return getToken(CobolPreprocessorParser.AR, 0); }
		public TerminalNode ARITH() { return getToken(CobolPreprocessorParser.ARITH, 0); }
		public TerminalNode AUTO() { return getToken(CobolPreprocessorParser.AUTO, 0); }
		public TerminalNode AWO() { return getToken(CobolPreprocessorParser.AWO, 0); }
		public TerminalNode BIN() { return getToken(CobolPreprocessorParser.BIN, 0); }
		public TerminalNode BLOCK0() { return getToken(CobolPreprocessorParser.BLOCK0, 0); }
		public TerminalNode BUF() { return getToken(CobolPreprocessorParser.BUF, 0); }
		public TerminalNode BUFSIZE() { return getToken(CobolPreprocessorParser.BUFSIZE, 0); }
		public TerminalNode BY() { return getToken(CobolPreprocessorParser.BY, 0); }
		public TerminalNode CBL() { return getToken(CobolPreprocessorParser.CBL, 0); }
		public TerminalNode CBLCARD() { return getToken(CobolPreprocessorParser.CBLCARD, 0); }
		public TerminalNode CO() { return getToken(CobolPreprocessorParser.CO, 0); }
		public TerminalNode COBOL2() { return getToken(CobolPreprocessorParser.COBOL2, 0); }
		public TerminalNode COBOL3() { return getToken(CobolPreprocessorParser.COBOL3, 0); }
		public TerminalNode CODEPAGE() { return getToken(CobolPreprocessorParser.CODEPAGE, 0); }
		public TerminalNode COMMACHAR() { return getToken(CobolPreprocessorParser.COMMACHAR, 0); }
		public TerminalNode COMPAT() { return getToken(CobolPreprocessorParser.COMPAT, 0); }
		public TerminalNode COMPILE() { return getToken(CobolPreprocessorParser.COMPILE, 0); }
		public TerminalNode CP() { return getToken(CobolPreprocessorParser.CP, 0); }
		public TerminalNode CPP() { return getToken(CobolPreprocessorParser.CPP, 0); }
		public TerminalNode CPSM() { return getToken(CobolPreprocessorParser.CPSM, 0); }
		public TerminalNode CS() { return getToken(CobolPreprocessorParser.CS, 0); }
		public TerminalNode CURR() { return getToken(CobolPreprocessorParser.CURR, 0); }
		public TerminalNode CURRENCY() { return getToken(CobolPreprocessorParser.CURRENCY, 0); }
		public TerminalNode DATA() { return getToken(CobolPreprocessorParser.DATA, 0); }
		public TerminalNode DATEPROC() { return getToken(CobolPreprocessorParser.DATEPROC, 0); }
		public TerminalNode DBCS() { return getToken(CobolPreprocessorParser.DBCS, 0); }
		public TerminalNode DD() { return getToken(CobolPreprocessorParser.DD, 0); }
		public TerminalNode DEBUG() { return getToken(CobolPreprocessorParser.DEBUG, 0); }
		public TerminalNode DECK() { return getToken(CobolPreprocessorParser.DECK, 0); }
		public TerminalNode DIAGTRUNC() { return getToken(CobolPreprocessorParser.DIAGTRUNC, 0); }
		public TerminalNode DLI() { return getToken(CobolPreprocessorParser.DLI, 0); }
		public TerminalNode DLL() { return getToken(CobolPreprocessorParser.DLL, 0); }
		public TerminalNode DP() { return getToken(CobolPreprocessorParser.DP, 0); }
		public TerminalNode DTR() { return getToken(CobolPreprocessorParser.DTR, 0); }
		public TerminalNode DU() { return getToken(CobolPreprocessorParser.DU, 0); }
		public TerminalNode DUMP() { return getToken(CobolPreprocessorParser.DUMP, 0); }
		public TerminalNode DYN() { return getToken(CobolPreprocessorParser.DYN, 0); }
		public TerminalNode DYNAM() { return getToken(CobolPreprocessorParser.DYNAM, 0); }
		public TerminalNode EDF() { return getToken(CobolPreprocessorParser.EDF, 0); }
		public TerminalNode EJPD() { return getToken(CobolPreprocessorParser.EJPD, 0); }
		public TerminalNode EN() { return getToken(CobolPreprocessorParser.EN, 0); }
		public TerminalNode ENGLISH() { return getToken(CobolPreprocessorParser.ENGLISH, 0); }
		public TerminalNode EPILOG() { return getToken(CobolPreprocessorParser.EPILOG, 0); }
		public TerminalNode EXCI() { return getToken(CobolPreprocessorParser.EXCI, 0); }
		public TerminalNode EXIT() { return getToken(CobolPreprocessorParser.EXIT, 0); }
		public TerminalNode EXP() { return getToken(CobolPreprocessorParser.EXP, 0); }
		public TerminalNode EXPORTALL() { return getToken(CobolPreprocessorParser.EXPORTALL, 0); }
		public TerminalNode EXTEND() { return getToken(CobolPreprocessorParser.EXTEND, 0); }
		public TerminalNode FASTSRT() { return getToken(CobolPreprocessorParser.FASTSRT, 0); }
		public TerminalNode FLAG() { return getToken(CobolPreprocessorParser.FLAG, 0); }
		public TerminalNode FLAGSTD() { return getToken(CobolPreprocessorParser.FLAGSTD, 0); }
		public TerminalNode FULL() { return getToken(CobolPreprocessorParser.FULL, 0); }
		public TerminalNode FSRT() { return getToken(CobolPreprocessorParser.FSRT, 0); }
		public TerminalNode GDS() { return getToken(CobolPreprocessorParser.GDS, 0); }
		public TerminalNode GRAPHIC() { return getToken(CobolPreprocessorParser.GRAPHIC, 0); }
		public TerminalNode HOOK() { return getToken(CobolPreprocessorParser.HOOK, 0); }
		public TerminalNode IN() { return getToken(CobolPreprocessorParser.IN, 0); }
		public TerminalNode INTDATE() { return getToken(CobolPreprocessorParser.INTDATE, 0); }
		public TerminalNode JA() { return getToken(CobolPreprocessorParser.JA, 0); }
		public TerminalNode JP() { return getToken(CobolPreprocessorParser.JP, 0); }
		public TerminalNode KA() { return getToken(CobolPreprocessorParser.KA, 0); }
		public TerminalNode LANG() { return getToken(CobolPreprocessorParser.LANG, 0); }
		public TerminalNode LANGUAGE() { return getToken(CobolPreprocessorParser.LANGUAGE, 0); }
		public TerminalNode LC() { return getToken(CobolPreprocessorParser.LC, 0); }
		public TerminalNode LENGTH() { return getToken(CobolPreprocessorParser.LENGTH, 0); }
		public TerminalNode LIB() { return getToken(CobolPreprocessorParser.LIB, 0); }
		public TerminalNode LILIAN() { return getToken(CobolPreprocessorParser.LILIAN, 0); }
		public TerminalNode LIN() { return getToken(CobolPreprocessorParser.LIN, 0); }
		public TerminalNode LINECOUNT() { return getToken(CobolPreprocessorParser.LINECOUNT, 0); }
		public TerminalNode LINKAGE() { return getToken(CobolPreprocessorParser.LINKAGE, 0); }
		public TerminalNode LIST() { return getToken(CobolPreprocessorParser.LIST, 0); }
		public TerminalNode LM() { return getToken(CobolPreprocessorParser.LM, 0); }
		public TerminalNode LONGMIXED() { return getToken(CobolPreprocessorParser.LONGMIXED, 0); }
		public TerminalNode LONGUPPER() { return getToken(CobolPreprocessorParser.LONGUPPER, 0); }
		public TerminalNode LU() { return getToken(CobolPreprocessorParser.LU, 0); }
		public TerminalNode MAP() { return getToken(CobolPreprocessorParser.MAP, 0); }
		public TerminalNode MARGINS() { return getToken(CobolPreprocessorParser.MARGINS, 0); }
		public TerminalNode MAX() { return getToken(CobolPreprocessorParser.MAX, 0); }
		public TerminalNode MD() { return getToken(CobolPreprocessorParser.MD, 0); }
		public TerminalNode MDECK() { return getToken(CobolPreprocessorParser.MDECK, 0); }
		public TerminalNode MIG() { return getToken(CobolPreprocessorParser.MIG, 0); }
		public TerminalNode MIXED() { return getToken(CobolPreprocessorParser.MIXED, 0); }
		public TerminalNode NAME() { return getToken(CobolPreprocessorParser.NAME, 0); }
		public TerminalNode NAT() { return getToken(CobolPreprocessorParser.NAT, 0); }
		public TerminalNode NATIONAL() { return getToken(CobolPreprocessorParser.NATIONAL, 0); }
		public TerminalNode NATLANG() { return getToken(CobolPreprocessorParser.NATLANG, 0); }
		public TerminalNode NN() { return getToken(CobolPreprocessorParser.NN, 0); }
		public TerminalNode NO() { return getToken(CobolPreprocessorParser.NO, 0); }
		public TerminalNode NOADATA() { return getToken(CobolPreprocessorParser.NOADATA, 0); }
		public TerminalNode NOADV() { return getToken(CobolPreprocessorParser.NOADV, 0); }
		public TerminalNode NOALIAS() { return getToken(CobolPreprocessorParser.NOALIAS, 0); }
		public TerminalNode NOAWO() { return getToken(CobolPreprocessorParser.NOAWO, 0); }
		public TerminalNode NOBLOCK0() { return getToken(CobolPreprocessorParser.NOBLOCK0, 0); }
		public TerminalNode NOC() { return getToken(CobolPreprocessorParser.NOC, 0); }
		public TerminalNode NOCBLCARD() { return getToken(CobolPreprocessorParser.NOCBLCARD, 0); }
		public TerminalNode NOCICS() { return getToken(CobolPreprocessorParser.NOCICS, 0); }
		public TerminalNode NOCMPR2() { return getToken(CobolPreprocessorParser.NOCMPR2, 0); }
		public TerminalNode NOCOMPILE() { return getToken(CobolPreprocessorParser.NOCOMPILE, 0); }
		public TerminalNode NOCPSM() { return getToken(CobolPreprocessorParser.NOCPSM, 0); }
		public TerminalNode NOCURR() { return getToken(CobolPreprocessorParser.NOCURR, 0); }
		public TerminalNode NOCURRENCY() { return getToken(CobolPreprocessorParser.NOCURRENCY, 0); }
		public TerminalNode NOD() { return getToken(CobolPreprocessorParser.NOD, 0); }
		public TerminalNode NODATEPROC() { return getToken(CobolPreprocessorParser.NODATEPROC, 0); }
		public TerminalNode NODBCS() { return getToken(CobolPreprocessorParser.NODBCS, 0); }
		public TerminalNode NODE() { return getToken(CobolPreprocessorParser.NODE, 0); }
		public TerminalNode NODEBUG() { return getToken(CobolPreprocessorParser.NODEBUG, 0); }
		public TerminalNode NODECK() { return getToken(CobolPreprocessorParser.NODECK, 0); }
		public TerminalNode NODIAGTRUNC() { return getToken(CobolPreprocessorParser.NODIAGTRUNC, 0); }
		public TerminalNode NODLL() { return getToken(CobolPreprocessorParser.NODLL, 0); }
		public TerminalNode NODU() { return getToken(CobolPreprocessorParser.NODU, 0); }
		public TerminalNode NODUMP() { return getToken(CobolPreprocessorParser.NODUMP, 0); }
		public TerminalNode NODP() { return getToken(CobolPreprocessorParser.NODP, 0); }
		public TerminalNode NODTR() { return getToken(CobolPreprocessorParser.NODTR, 0); }
		public TerminalNode NODYN() { return getToken(CobolPreprocessorParser.NODYN, 0); }
		public TerminalNode NODYNAM() { return getToken(CobolPreprocessorParser.NODYNAM, 0); }
		public TerminalNode NOEDF() { return getToken(CobolPreprocessorParser.NOEDF, 0); }
		public TerminalNode NOEJPD() { return getToken(CobolPreprocessorParser.NOEJPD, 0); }
		public TerminalNode NOEPILOG() { return getToken(CobolPreprocessorParser.NOEPILOG, 0); }
		public TerminalNode NOEXIT() { return getToken(CobolPreprocessorParser.NOEXIT, 0); }
		public TerminalNode NOEXP() { return getToken(CobolPreprocessorParser.NOEXP, 0); }
		public TerminalNode NOEXPORTALL() { return getToken(CobolPreprocessorParser.NOEXPORTALL, 0); }
		public TerminalNode NOF() { return getToken(CobolPreprocessorParser.NOF, 0); }
		public TerminalNode NOFASTSRT() { return getToken(CobolPreprocessorParser.NOFASTSRT, 0); }
		public TerminalNode NOFEPI() { return getToken(CobolPreprocessorParser.NOFEPI, 0); }
		public TerminalNode NOFLAG() { return getToken(CobolPreprocessorParser.NOFLAG, 0); }
		public TerminalNode NOFLAGMIG() { return getToken(CobolPreprocessorParser.NOFLAGMIG, 0); }
		public TerminalNode NOFLAGSTD() { return getToken(CobolPreprocessorParser.NOFLAGSTD, 0); }
		public TerminalNode NOFSRT() { return getToken(CobolPreprocessorParser.NOFSRT, 0); }
		public TerminalNode NOGRAPHIC() { return getToken(CobolPreprocessorParser.NOGRAPHIC, 0); }
		public TerminalNode NOHOOK() { return getToken(CobolPreprocessorParser.NOHOOK, 0); }
		public TerminalNode NOLENGTH() { return getToken(CobolPreprocessorParser.NOLENGTH, 0); }
		public TerminalNode NOLIB() { return getToken(CobolPreprocessorParser.NOLIB, 0); }
		public TerminalNode NOLINKAGE() { return getToken(CobolPreprocessorParser.NOLINKAGE, 0); }
		public TerminalNode NOLIST() { return getToken(CobolPreprocessorParser.NOLIST, 0); }
		public TerminalNode NOMAP() { return getToken(CobolPreprocessorParser.NOMAP, 0); }
		public TerminalNode NOMD() { return getToken(CobolPreprocessorParser.NOMD, 0); }
		public TerminalNode NOMDECK() { return getToken(CobolPreprocessorParser.NOMDECK, 0); }
		public TerminalNode NONAME() { return getToken(CobolPreprocessorParser.NONAME, 0); }
		public TerminalNode NONUM() { return getToken(CobolPreprocessorParser.NONUM, 0); }
		public TerminalNode NONUMBER() { return getToken(CobolPreprocessorParser.NONUMBER, 0); }
		public TerminalNode NOOBJ() { return getToken(CobolPreprocessorParser.NOOBJ, 0); }
		public TerminalNode NOOBJECT() { return getToken(CobolPreprocessorParser.NOOBJECT, 0); }
		public TerminalNode NOOFF() { return getToken(CobolPreprocessorParser.NOOFF, 0); }
		public TerminalNode NOOFFSET() { return getToken(CobolPreprocessorParser.NOOFFSET, 0); }
		public TerminalNode NOOPSEQUENCE() { return getToken(CobolPreprocessorParser.NOOPSEQUENCE, 0); }
		public TerminalNode NOOPT() { return getToken(CobolPreprocessorParser.NOOPT, 0); }
		public TerminalNode NOOPTIMIZE() { return getToken(CobolPreprocessorParser.NOOPTIMIZE, 0); }
		public TerminalNode NOOPTIONS() { return getToken(CobolPreprocessorParser.NOOPTIONS, 0); }
		public TerminalNode NOP() { return getToken(CobolPreprocessorParser.NOP, 0); }
		public TerminalNode NOPFD() { return getToken(CobolPreprocessorParser.NOPFD, 0); }
		public TerminalNode NOPROLOG() { return getToken(CobolPreprocessorParser.NOPROLOG, 0); }
		public TerminalNode NORENT() { return getToken(CobolPreprocessorParser.NORENT, 0); }
		public TerminalNode NOS() { return getToken(CobolPreprocessorParser.NOS, 0); }
		public TerminalNode NOSEP() { return getToken(CobolPreprocessorParser.NOSEP, 0); }
		public TerminalNode NOSEPARATE() { return getToken(CobolPreprocessorParser.NOSEPARATE, 0); }
		public TerminalNode NOSEQ() { return getToken(CobolPreprocessorParser.NOSEQ, 0); }
		public TerminalNode NOSEQUENCE() { return getToken(CobolPreprocessorParser.NOSEQUENCE, 0); }
		public TerminalNode NOSOURCE() { return getToken(CobolPreprocessorParser.NOSOURCE, 0); }
		public TerminalNode NOSPIE() { return getToken(CobolPreprocessorParser.NOSPIE, 0); }
		public TerminalNode NOSQL() { return getToken(CobolPreprocessorParser.NOSQL, 0); }
		public TerminalNode NOSQLC() { return getToken(CobolPreprocessorParser.NOSQLC, 0); }
		public TerminalNode NOSQLCCSID() { return getToken(CobolPreprocessorParser.NOSQLCCSID, 0); }
		public TerminalNode NOSSR() { return getToken(CobolPreprocessorParser.NOSSR, 0); }
		public TerminalNode NOSSRANGE() { return getToken(CobolPreprocessorParser.NOSSRANGE, 0); }
		public TerminalNode NOSTDTRUNC() { return getToken(CobolPreprocessorParser.NOSTDTRUNC, 0); }
		public TerminalNode NOTERM() { return getToken(CobolPreprocessorParser.NOTERM, 0); }
		public TerminalNode NOTERMINAL() { return getToken(CobolPreprocessorParser.NOTERMINAL, 0); }
		public TerminalNode NOTEST() { return getToken(CobolPreprocessorParser.NOTEST, 0); }
		public TerminalNode NOTHREAD() { return getToken(CobolPreprocessorParser.NOTHREAD, 0); }
		public TerminalNode NOTRIG() { return getToken(CobolPreprocessorParser.NOTRIG, 0); }
		public TerminalNode NOVBREF() { return getToken(CobolPreprocessorParser.NOVBREF, 0); }
		public TerminalNode NOWORD() { return getToken(CobolPreprocessorParser.NOWORD, 0); }
		public TerminalNode NOX() { return getToken(CobolPreprocessorParser.NOX, 0); }
		public TerminalNode NOXREF() { return getToken(CobolPreprocessorParser.NOXREF, 0); }
		public TerminalNode NOZWB() { return getToken(CobolPreprocessorParser.NOZWB, 0); }
		public TerminalNode NSEQ() { return getToken(CobolPreprocessorParser.NSEQ, 0); }
		public TerminalNode NSYMBOL() { return getToken(CobolPreprocessorParser.NSYMBOL, 0); }
		public TerminalNode NS() { return getToken(CobolPreprocessorParser.NS, 0); }
		public TerminalNode NUM() { return getToken(CobolPreprocessorParser.NUM, 0); }
		public TerminalNode NUMBER() { return getToken(CobolPreprocessorParser.NUMBER, 0); }
		public TerminalNode NUMPROC() { return getToken(CobolPreprocessorParser.NUMPROC, 0); }
		public TerminalNode OBJ() { return getToken(CobolPreprocessorParser.OBJ, 0); }
		public TerminalNode OBJECT() { return getToken(CobolPreprocessorParser.OBJECT, 0); }
		public TerminalNode ON() { return getToken(CobolPreprocessorParser.ON, 0); }
		public TerminalNode OF() { return getToken(CobolPreprocessorParser.OF, 0); }
		public TerminalNode OFF() { return getToken(CobolPreprocessorParser.OFF, 0); }
		public TerminalNode OFFSET() { return getToken(CobolPreprocessorParser.OFFSET, 0); }
		public TerminalNode OPMARGINS() { return getToken(CobolPreprocessorParser.OPMARGINS, 0); }
		public TerminalNode OPSEQUENCE() { return getToken(CobolPreprocessorParser.OPSEQUENCE, 0); }
		public TerminalNode OPTIMIZE() { return getToken(CobolPreprocessorParser.OPTIMIZE, 0); }
		public TerminalNode OP() { return getToken(CobolPreprocessorParser.OP, 0); }
		public TerminalNode OPT() { return getToken(CobolPreprocessorParser.OPT, 0); }
		public TerminalNode OPTFILE() { return getToken(CobolPreprocessorParser.OPTFILE, 0); }
		public TerminalNode OPTIONS() { return getToken(CobolPreprocessorParser.OPTIONS, 0); }
		public TerminalNode OUT() { return getToken(CobolPreprocessorParser.OUT, 0); }
		public TerminalNode OUTDD() { return getToken(CobolPreprocessorParser.OUTDD, 0); }
		public TerminalNode PFD() { return getToken(CobolPreprocessorParser.PFD, 0); }
		public TerminalNode PGMN() { return getToken(CobolPreprocessorParser.PGMN, 0); }
		public TerminalNode PGMNAME() { return getToken(CobolPreprocessorParser.PGMNAME, 0); }
		public TerminalNode PPTDBG() { return getToken(CobolPreprocessorParser.PPTDBG, 0); }
		public TerminalNode PROCESS() { return getToken(CobolPreprocessorParser.PROCESS, 0); }
		public TerminalNode PROLOG() { return getToken(CobolPreprocessorParser.PROLOG, 0); }
		public TerminalNode QUOTE() { return getToken(CobolPreprocessorParser.QUOTE, 0); }
		public TerminalNode RENT() { return getToken(CobolPreprocessorParser.RENT, 0); }
		public TerminalNode REPLACING() { return getToken(CobolPreprocessorParser.REPLACING, 0); }
		public TerminalNode RMODE() { return getToken(CobolPreprocessorParser.RMODE, 0); }
		public TerminalNode SEQ() { return getToken(CobolPreprocessorParser.SEQ, 0); }
		public TerminalNode SEQUENCE() { return getToken(CobolPreprocessorParser.SEQUENCE, 0); }
		public TerminalNode SEP() { return getToken(CobolPreprocessorParser.SEP, 0); }
		public TerminalNode SEPARATE() { return getToken(CobolPreprocessorParser.SEPARATE, 0); }
		public TerminalNode SHORT() { return getToken(CobolPreprocessorParser.SHORT, 0); }
		public TerminalNode SIZE() { return getToken(CobolPreprocessorParser.SIZE, 0); }
		public TerminalNode SOURCE() { return getToken(CobolPreprocessorParser.SOURCE, 0); }
		public TerminalNode SP() { return getToken(CobolPreprocessorParser.SP, 0); }
		public TerminalNode SPACE() { return getToken(CobolPreprocessorParser.SPACE, 0); }
		public TerminalNode SPIE() { return getToken(CobolPreprocessorParser.SPIE, 0); }
		public TerminalNode SQL() { return getToken(CobolPreprocessorParser.SQL, 0); }
		public TerminalNode SQLC() { return getToken(CobolPreprocessorParser.SQLC, 0); }
		public TerminalNode SQLCCSID() { return getToken(CobolPreprocessorParser.SQLCCSID, 0); }
		public TerminalNode SS() { return getToken(CobolPreprocessorParser.SS, 0); }
		public TerminalNode SSR() { return getToken(CobolPreprocessorParser.SSR, 0); }
		public TerminalNode SSRANGE() { return getToken(CobolPreprocessorParser.SSRANGE, 0); }
		public TerminalNode STD() { return getToken(CobolPreprocessorParser.STD, 0); }
		public TerminalNode SYSEIB() { return getToken(CobolPreprocessorParser.SYSEIB, 0); }
		public TerminalNode SZ() { return getToken(CobolPreprocessorParser.SZ, 0); }
		public TerminalNode TERM() { return getToken(CobolPreprocessorParser.TERM, 0); }
		public TerminalNode TERMINAL() { return getToken(CobolPreprocessorParser.TERMINAL, 0); }
		public TerminalNode TEST() { return getToken(CobolPreprocessorParser.TEST, 0); }
		public TerminalNode THREAD() { return getToken(CobolPreprocessorParser.THREAD, 0); }
		public TerminalNode TRIG() { return getToken(CobolPreprocessorParser.TRIG, 0); }
		public TerminalNode TRUNC() { return getToken(CobolPreprocessorParser.TRUNC, 0); }
		public TerminalNode UE() { return getToken(CobolPreprocessorParser.UE, 0); }
		public TerminalNode UPPER() { return getToken(CobolPreprocessorParser.UPPER, 0); }
		public TerminalNode VBREF() { return getToken(CobolPreprocessorParser.VBREF, 0); }
		public TerminalNode WD() { return getToken(CobolPreprocessorParser.WD, 0); }
		public TerminalNode XMLPARSE() { return getToken(CobolPreprocessorParser.XMLPARSE, 0); }
		public TerminalNode XMLSS() { return getToken(CobolPreprocessorParser.XMLSS, 0); }
		public TerminalNode XOPTS() { return getToken(CobolPreprocessorParser.XOPTS, 0); }
		public TerminalNode XREF() { return getToken(CobolPreprocessorParser.XREF, 0); }
		public TerminalNode YEARWINDOW() { return getToken(CobolPreprocessorParser.YEARWINDOW, 0); }
		public TerminalNode YW() { return getToken(CobolPreprocessorParser.YW, 0); }
		public TerminalNode ZWB() { return getToken(CobolPreprocessorParser.ZWB, 0); }
		public TerminalNode C_CHAR() { return getToken(CobolPreprocessorParser.C_CHAR, 0); }
		public TerminalNode D_CHAR() { return getToken(CobolPreprocessorParser.D_CHAR, 0); }
		public TerminalNode E_CHAR() { return getToken(CobolPreprocessorParser.E_CHAR, 0); }
		public TerminalNode F_CHAR() { return getToken(CobolPreprocessorParser.F_CHAR, 0); }
		public TerminalNode H_CHAR() { return getToken(CobolPreprocessorParser.H_CHAR, 0); }
		public TerminalNode I_CHAR() { return getToken(CobolPreprocessorParser.I_CHAR, 0); }
		public TerminalNode M_CHAR() { return getToken(CobolPreprocessorParser.M_CHAR, 0); }
		public TerminalNode N_CHAR() { return getToken(CobolPreprocessorParser.N_CHAR, 0); }
		public TerminalNode Q_CHAR() { return getToken(CobolPreprocessorParser.Q_CHAR, 0); }
		public TerminalNode S_CHAR() { return getToken(CobolPreprocessorParser.S_CHAR, 0); }
		public TerminalNode U_CHAR() { return getToken(CobolPreprocessorParser.U_CHAR, 0); }
		public TerminalNode W_CHAR() { return getToken(CobolPreprocessorParser.W_CHAR, 0); }
		public TerminalNode X_CHAR() { return getToken(CobolPreprocessorParser.X_CHAR, 0); }
		public CharDataKeywordContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_charDataKeyword; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).enterCharDataKeyword(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CobolPreprocessorListener ) ((CobolPreprocessorListener)listener).exitCharDataKeyword(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof CobolPreprocessorVisitor) return ((CobolPreprocessorVisitor<? extends T>) visitor).visitCharDataKeyword(this);
			return visitor.visitChildren(this);
		}
	}

	public final CharDataKeywordContext charDataKeyword() throws RecognitionException {
		CharDataKeywordContext _localctx = new CharDataKeywordContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_charDataKeyword);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(670);
			_la = _input.LA(1);
			if ( !(((_la & ~0x3f) == 0 && ((1L << _la) & -2346656880870555650L) != 0) || (((_la - 64) & ~0x3f) == 0 && ((1L << (_la - 64)) & -33570881L) != 0) || (((_la - 128) & ~0x3f) == 0 && ((1L << (_la - 128)) & -576460752303423489L) != 0) || (((_la - 192) & ~0x3f) == 0 && ((1L << (_la - 192)) & -2324912943717679105L) != 0) || (((_la - 256) & ~0x3f) == 0 && ((1L << (_la - 256)) & 335544047L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\u0004\u0001\u0129\u02a1\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001"+
		"\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004"+
		"\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007"+
		"\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b"+
		"\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007"+
		"\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007"+
		"\u0012\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007"+
		"\u0015\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007"+
		"\u0018\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002\u001b\u0007"+
		"\u001b\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002\u001e\u0007"+
		"\u001e\u0002\u001f\u0007\u001f\u0002 \u0007 \u0002!\u0007!\u0001\u0000"+
		"\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0005\u0000"+
		"Q\b\u0000\n\u0000\f\u0000T\t\u0000\u0001\u0000\u0001\u0000\u0001\u0001"+
		"\u0001\u0001\u0003\u0001Z\b\u0001\u0001\u0001\u0001\u0001\u0004\u0001"+
		"^\b\u0001\u000b\u0001\f\u0001_\u0001\u0002\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0003\u0002f\b\u0002\u0001\u0002\u0005\u0002i\b\u0002\n\u0002\f"+
		"\u0002l\t\u0002\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0003"+
		"\u0003\u0084\b\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0003\u0003\u009d\b\u0003\u0001\u0003\u0003\u0003\u00a0\b\u0003"+
		"\u0001\u0003\u0003\u0003\u00a3\b\u0003\u0001\u0003\u0003\u0003\u00a6\b"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0003\u0003\u00ba\b\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0003\u0003\u00c2\b\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0003\u0003\u00e2"+
		"\b\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0003\u0003\u00ea\b\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0003\u0003\u00f0\b\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0003"+
		"\u0003\u0101\b\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0003\u0003\u014a\b\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0003\u0003\u0159\b\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0003\u0003\u016f\b\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0003"+
		"\u0003\u0179\b\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0003"+
		"\u0003\u017f\b\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0003\u0003\u018f\b\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0003\u0003\u0198\b\u0003\u0001\u0003\u0003\u0003\u019b\b\u0003"+
		"\u0001\u0003\u0003\u0003\u019e\b\u0003\u0001\u0003\u0003\u0003\u01a1\b"+
		"\u0003\u0001\u0003\u0003\u0003\u01a4\b\u0003\u0001\u0003\u0003\u0003\u01a7"+
		"\b\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0003\u0003\u01bb\b\u0003\u0001\u0003\u0003\u0003\u01be\b\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003"+
		"\u0003\u0003\u01c6\b\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0003\u0004\u01cd\b\u0004\u0001\u0005\u0001\u0005\u0001\u0005"+
		"\u0001\u0005\u0001\u0005\u0003\u0005\u01d4\b\u0005\u0001\u0006\u0001\u0006"+
		"\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0003\u0006\u01dc\b\u0006"+
		"\u0001\u0006\u0001\u0006\u0003\u0006\u01e0\b\u0006\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u01e7\b\u0007\u0001\b"+
		"\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0003\b\u01ef\b\b\u0005\b\u01f1"+
		"\b\b\n\b\f\b\u01f4\t\b\u0001\b\u0001\b\u0001\t\u0001\t\u0001\t\u0003\t"+
		"\u01fb\b\t\u0001\t\u0001\t\u0003\t\u01ff\b\t\u0001\n\u0001\n\u0003\n\u0203"+
		"\b\n\u0001\u000b\u0001\u000b\u0001\u000b\u0005\u000b\u0208\b\u000b\n\u000b"+
		"\f\u000b\u020b\t\u000b\u0001\f\u0001\f\u0001\f\u0005\f\u0210\b\f\n\f\f"+
		"\f\u0213\t\f\u0001\f\u0003\f\u0216\b\f\u0001\r\u0001\r\u0004\r\u021a\b"+
		"\r\u000b\r\f\r\u021b\u0001\r\u0001\r\u0001\u000e\u0001\u000e\u0001\u000e"+
		"\u0001\u000e\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0003\u000f"+
		"\u0228\b\u000f\u0001\u000f\u0005\u000f\u022b\b\u000f\n\u000f\f\u000f\u022e"+
		"\t\u000f\u0001\u000f\u0003\u000f\u0231\b\u000f\u0001\u0010\u0001\u0010"+
		"\u0001\u0010\u0003\u0010\u0236\b\u0010\u0001\u0011\u0001\u0011\u0001\u0011"+
		"\u0003\u0011\u023b\b\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012"+
		"\u0003\u0012\u0241\b\u0012\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013"+
		"\u0003\u0013\u0247\b\u0013\u0001\u0014\u0001\u0014\u0003\u0014\u024b\b"+
		"\u0014\u0001\u0015\u0001\u0015\u0003\u0015\u024f\b\u0015\u0001\u0016\u0001"+
		"\u0016\u0001\u0016\u0003\u0016\u0254\b\u0016\u0001\u0017\u0001\u0017\u0003"+
		"\u0017\u0258\b\u0017\u0001\u0017\u0001\u0017\u0001\u0018\u0004\u0018\u025d"+
		"\b\u0018\u000b\u0018\f\u0018\u025e\u0001\u0019\u0001\u0019\u0001\u0019"+
		"\u0004\u0019\u0264\b\u0019\u000b\u0019\f\u0019\u0265\u0001\u001a\u0001"+
		"\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0004"+
		"\u001a\u026f\b\u001a\u000b\u001a\f\u001a\u0270\u0001\u001b\u0001\u001b"+
		"\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b"+
		"\u0004\u001b\u027b\b\u001b\u000b\u001b\f\u001b\u027c\u0001\u001c\u0001"+
		"\u001c\u0001\u001c\u0003\u001c\u0282\b\u001c\u0001\u001c\u0003\u001c\u0285"+
		"\b\u001c\u0001\u001c\u0001\u001c\u0003\u001c\u0289\b\u001c\u0005\u001c"+
		"\u028b\b\u001c\n\u001c\f\u001c\u028e\t\u001c\u0001\u001c\u0001\u001c\u0001"+
		"\u001d\u0001\u001d\u0003\u001d\u0294\b\u001d\u0001\u001e\u0001\u001e\u0001"+
		"\u001f\u0001\u001f\u0001 \u0004 \u029b\b \u000b \f \u029c\u0001!\u0001"+
		"!\u0001!\u0000\u0000\"\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012"+
		"\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,.02468:<>@B\u0000S\u0002\u0000"+
		"\u0010\u0010\u00d9\u00d9\u0001\u0000\u0007\b\u0004\u0000\u0017\u0017;"+
		";\u010d\u010d\u010f\u010f\u0001\u0000\r\u000e\u0002\u0000\u0016\u0016"+
		"\u001a\u001a\u0002\u0000\u0018\u0018\u010d\u010d\u0001\u0000\u001e\u001f"+
		"\u0002\u0000!!))\u0002\u0000>>\u008c\u008c\u0002\u0000\u00b9\u00b9\u00fe"+
		"\u00fe\u0002\u0000%%\u010e\u010e\u0002\u0000&&**\u0001\u0000+,\u0001\u0000"+
		"-.\u0001\u00009:\u0002\u0000<<@@\u0002\u0000>>\u0110\u0110\u0003\u0000"+
		"\u010f\u010f\u0112\u0112\u0116\u0118\u0001\u0000\u0111\u0113\u0006\u0000"+
		"##ff\u00f2\u00f2\u010e\u010e\u0114\u0114\u0116\u0116\u0002\u0000\u0004"+
		"\u0004QQ\u0001\u0000KL\u0004\u0000\u001d\u001d23HJ\u0100\u0100\u0002\u0000"+
		"MMSS\u0001\u0000^_\u0004\u0000\u0018\u0018mmqq\u010d\u010d\u0002\u0000"+
		"\u0003\u0003jj\u0003\u0000\u001d\u001d22JJ\u0002\u0000mmqq\u0003\u0000"+
		"\u010f\u010f\u0116\u0116\u0118\u0118\u0001\u0000st\u0002\u0000vv\u007f"+
		"\u007f\u0002\u0000uuzz\u0001\u0000}~\u0002\u0000{{\u0080\u0080\u0001\u0000"+
		"\u0081\u0082\u0001\u0000\u0087\u0088\u0002\u0000\u008a\u008a\u008f\u008f"+
		"\u0002\u0000\u0089\u0089\u008c\u008c\u0001\u0000\u0097\u0098\u0001\u0000"+
		"\u009a\u009b\u0001\u0000\u009c\u009d\u0001\u0000\u009e\u009f\u0001\u0000"+
		"\u00a1\u00a2\u0002\u0000\u00ab\u00ab\u00b4\u00b4\u0002\u0000\u00a8\u00a8"+
		"\u00ac\u00ac\u0001\u0000\u00af\u00b0\u0001\u0000\u00b1\u00b2\u0001\u0000"+
		"\u00b5\u00b6\u0001\u0000\u00bb\u00bc\u0002\u0000\u00c0\u00c0\u00c2\u00c2"+
		"\u0002\u0000\"\"cd\u0001\u0000\u00bd\u00be\u0001\u0000\u00c3\u00c4\u0003"+
		"\u0000``\u00a5\u00a5\u00d5\u00d5\u0001\u0000\u00c6\u00c7\u0001\u0000\u00c9"+
		"\u00ca\u0002\u0000\u00cf\u00cf\u00d1\u00d1\u0002\u0000AA\u00f5\u00f5\u0001"+
		"\u0000\u00d3\u00d4\u0001\u0000\u00d7\u00d8\b\u0000\u0013\u0013\u0017\u0017"+
		"VXZZaa\u0101\u0101\u0113\u0113\u0117\u0117\u0002\u0000\u00db\u00db\u0115"+
		"\u0115\u0001\u0000\u00e3\u00e4\u0002\u0000\u00e6\u00e6\u00f8\u00f8\u0002"+
		"\u0000\u00e7\u00e7\u0116\u0116\u0001\u0000\u00ec\u00ed\u0001\u0000\u00f3"+
		"\u00f4\u0001\u0000\u00f9\u00fa\u0002\u0000DD\u0091\u0091\u0002\u0000\u00a9"+
		"\u00aa\u00e1\u00e2\u0002\u000011\u0084\u0084\u0003\u0000\u000b\u000b\u00cf"+
		"\u00cf\u00f5\u00f5\u0001\u0000\u0103\u0104\u0002\u0000\u0105\u0105\u0108"+
		"\u0108\u0004\u0000\u0017\u0017\u0106\u0106\u010d\u010d\u0119\u0119\u0002"+
		"\u0000\u0109\u0109\u0119\u0119\u0002\u0000AA\u00e5\u00e5\u0001\u0000\u010a"+
		"\u010b\u0002\u0000EE\u00c8\u00c8\u0001\u0000\u00ef\u00f1\u0001\u0000\u011f"+
		"\u0121\u0013\u0000\u0001\u0011\u0013\u0018\u001a/13568<>EGMOXZ\u00ba\u00bc"+
		"\u00dc\u00de\u00df\u00e1\u00ed\u00f2\u00f5\u00f7\u00fc\u00fe\u0103\u0105"+
		"\u0107\u0109\u0119\u011c\u011c\u0376\u0000R\u0001\u0000\u0000\u0000\u0002"+
		"W\u0001\u0000\u0000\u0000\u0004a\u0001\u0000\u0000\u0000\u0006\u01c5\u0001"+
		"\u0000\u0000\u0000\b\u01c7\u0001\u0000\u0000\u0000\n\u01ce\u0001\u0000"+
		"\u0000\u0000\f\u01d5\u0001\u0000\u0000\u0000\u000e\u01e1\u0001\u0000\u0000"+
		"\u0000\u0010\u01e8\u0001\u0000\u0000\u0000\u0012\u01fa\u0001\u0000\u0000"+
		"\u0000\u0014\u0202\u0001\u0000\u0000\u0000\u0016\u0204\u0001\u0000\u0000"+
		"\u0000\u0018\u020c\u0001\u0000\u0000\u0000\u001a\u0217\u0001\u0000\u0000"+
		"\u0000\u001c\u021f\u0001\u0000\u0000\u0000\u001e\u0223\u0001\u0000\u0000"+
		"\u0000 \u0232\u0001\u0000\u0000\u0000\"\u0237\u0001\u0000\u0000\u0000"+
		"$\u0240\u0001\u0000\u0000\u0000&\u0246\u0001\u0000\u0000\u0000(\u0248"+
		"\u0001\u0000\u0000\u0000*\u024c\u0001\u0000\u0000\u0000,\u0250\u0001\u0000"+
		"\u0000\u0000.\u0255\u0001\u0000\u0000\u00000\u025c\u0001\u0000\u0000\u0000"+
		"2\u0263\u0001\u0000\u0000\u00004\u026e\u0001\u0000\u0000\u00006\u027a"+
		"\u0001\u0000\u0000\u00008\u027e\u0001\u0000\u0000\u0000:\u0293\u0001\u0000"+
		"\u0000\u0000<\u0295\u0001\u0000\u0000\u0000>\u0297\u0001\u0000\u0000\u0000"+
		"@\u029a\u0001\u0000\u0000\u0000B\u029e\u0001\u0000\u0000\u0000DQ\u0003"+
		"\u0002\u0001\u0000EQ\u0003\u0010\b\u0000FQ\u0003\b\u0004\u0000GQ\u0003"+
		"\n\u0005\u0000HQ\u0003\f\u0006\u0000IQ\u0003\u000e\u0007\u0000JQ\u0003"+
		"\u001c\u000e\u0000KQ\u0003\u0018\f\u0000LQ\u0003(\u0014\u0000MQ\u0003"+
		"*\u0015\u0000NQ\u0003,\u0016\u0000OQ\u00036\u001b\u0000PD\u0001\u0000"+
		"\u0000\u0000PE\u0001\u0000\u0000\u0000PF\u0001\u0000\u0000\u0000PG\u0001"+
		"\u0000\u0000\u0000PH\u0001\u0000\u0000\u0000PI\u0001\u0000\u0000\u0000"+
		"PJ\u0001\u0000\u0000\u0000PK\u0001\u0000\u0000\u0000PL\u0001\u0000\u0000"+
		"\u0000PM\u0001\u0000\u0000\u0000PN\u0001\u0000\u0000\u0000PO\u0001\u0000"+
		"\u0000\u0000QT\u0001\u0000\u0000\u0000RP\u0001\u0000\u0000\u0000RS\u0001"+
		"\u0000\u0000\u0000SU\u0001\u0000\u0000\u0000TR\u0001\u0000\u0000\u0000"+
		"UV\u0005\u0000\u0000\u0001V\u0001\u0001\u0000\u0000\u0000W]\u0007\u0000"+
		"\u0000\u0000XZ\u0005\u011c\u0000\u0000YX\u0001\u0000\u0000\u0000YZ\u0001"+
		"\u0000\u0000\u0000Z[\u0001\u0000\u0000\u0000[^\u0003\u0006\u0003\u0000"+
		"\\^\u0003\u0004\u0002\u0000]Y\u0001\u0000\u0000\u0000]\\\u0001\u0000\u0000"+
		"\u0000^_\u0001\u0000\u0000\u0000_]\u0001\u0000\u0000\u0000_`\u0001\u0000"+
		"\u0000\u0000`\u0003\u0001\u0000\u0000\u0000ab\u0005\u0107\u0000\u0000"+
		"bc\u0005Y\u0000\u0000cj\u0003\u0006\u0003\u0000df\u0005\u011c\u0000\u0000"+
		"ed\u0001\u0000\u0000\u0000ef\u0001\u0000\u0000\u0000fg\u0001\u0000\u0000"+
		"\u0000gi\u0003\u0006\u0003\u0000he\u0001\u0000\u0000\u0000il\u0001\u0000"+
		"\u0000\u0000jh\u0001\u0000\u0000\u0000jk\u0001\u0000\u0000\u0000km\u0001"+
		"\u0000\u0000\u0000lj\u0001\u0000\u0000\u0000mn\u0005\u00e0\u0000\u0000"+
		"n\u0005\u0001\u0000\u0000\u0000o\u01c6\u0005\u0001\u0000\u0000p\u01c6"+
		"\u0005\u0002\u0000\u0000q\u01c6\u0005\u0006\u0000\u0000rs\u0007\u0001"+
		"\u0000\u0000st\u0005Y\u0000\u0000tu\u0007\u0002\u0000\u0000u\u01c6\u0005"+
		"\u00e0\u0000\u0000v\u01c6\u0005\n\u0000\u0000w\u01c6\u0005\f\u0000\u0000"+
		"xy\u0007\u0003\u0000\u0000yz\u0005Y\u0000\u0000z{\u0003<\u001e\u0000{"+
		"|\u0005\u00e0\u0000\u0000|\u01c6\u0001\u0000\u0000\u0000}\u01c6\u0005"+
		"\u0011\u0000\u0000~\u0083\u0005\u0012\u0000\u0000\u007f\u0080\u0005Y\u0000"+
		"\u0000\u0080\u0081\u0003<\u001e\u0000\u0081\u0082\u0005\u00e0\u0000\u0000"+
		"\u0082\u0084\u0001\u0000\u0000\u0000\u0083\u007f\u0001\u0000\u0000\u0000"+
		"\u0083\u0084\u0001\u0000\u0000\u0000\u0084\u01c6\u0001\u0000\u0000\u0000"+
		"\u0085\u01c6\u0005\u0014\u0000\u0000\u0086\u01c6\u0005\u0015\u0000\u0000"+
		"\u0087\u0088\u0007\u0004\u0000\u0000\u0088\u0089\u0005Y\u0000\u0000\u0089"+
		"\u008a\u0003<\u001e\u0000\u008a\u008b\u0005\u00e0\u0000\u0000\u008b\u01c6"+
		"\u0001\u0000\u0000\u0000\u008c\u01c6\u0007\u0005\u0000\u0000\u008d\u01c6"+
		"\u0005\u001b\u0000\u0000\u008e\u01c6\u0005\u001c\u0000\u0000\u008f\u0090"+
		"\u0007\u0006\u0000\u0000\u0090\u0091\u0005Y\u0000\u0000\u0091\u0092\u0003"+
		"<\u001e\u0000\u0092\u0093\u0005\u00e0\u0000\u0000\u0093\u01c6\u0001\u0000"+
		"\u0000\u0000\u0094\u0095\u0005 \u0000\u0000\u0095\u0096\u0005Y\u0000\u0000"+
		"\u0096\u0097\u0003<\u001e\u0000\u0097\u0098\u0005\u00e0\u0000\u0000\u0098"+
		"\u01c6\u0001\u0000\u0000\u0000\u0099\u00a5\u0007\u0007\u0000\u0000\u009a"+
		"\u009c\u0005Y\u0000\u0000\u009b\u009d\u0007\b\u0000\u0000\u009c\u009b"+
		"\u0001\u0000\u0000\u0000\u009c\u009d\u0001\u0000\u0000\u0000\u009d\u009f"+
		"\u0001\u0000\u0000\u0000\u009e\u00a0\u0005\u011c\u0000\u0000\u009f\u009e"+
		"\u0001\u0000\u0000\u0000\u009f\u00a0\u0001\u0000\u0000\u0000\u00a0\u00a2"+
		"\u0001\u0000\u0000\u0000\u00a1\u00a3\u0007\t\u0000\u0000\u00a2\u00a1\u0001"+
		"\u0000\u0000\u0000\u00a2\u00a3\u0001\u0000\u0000\u0000\u00a3\u00a4\u0001"+
		"\u0000\u0000\u0000\u00a4\u00a6\u0005\u00e0\u0000\u0000\u00a5\u009a\u0001"+
		"\u0000\u0000\u0000\u00a5\u00a6\u0001\u0000\u0000\u0000\u00a6\u01c6\u0001"+
		"\u0000\u0000\u0000\u00a7\u01c6\u0005\"\u0000\u0000\u00a8\u01c6\u0007\n"+
		"\u0000\u0000\u00a9\u01c6\u0005$\u0000\u0000\u00aa\u01c6\u0007\u000b\u0000"+
		"\u0000\u00ab\u01c6\u0005(\u0000\u0000\u00ac\u01c6\u0007\f\u0000\u0000"+
		"\u00ad\u01c6\u0007\r\u0000\u0000\u00ae\u01c6\u0005/\u0000\u0000\u00af"+
		"\u01c6\u00055\u0000\u0000\u00b0\u01c6\u00058\u0000\u0000\u00b1\u01c6\u0007"+
		"\u000e\u0000\u0000\u00b2\u01c6\u0007\u000f\u0000\u0000\u00b3\u01c6\u0005"+
		"=\u0000\u0000\u00b4\u00b5\u0007\u0010\u0000\u0000\u00b5\u00b6\u0005Y\u0000"+
		"\u0000\u00b6\u00b9\u0007\u0011\u0000\u0000\u00b7\u00b8\u0005\u011c\u0000"+
		"\u0000\u00b8\u00ba\u0007\u0011\u0000\u0000\u00b9\u00b7\u0001\u0000\u0000"+
		"\u0000\u00b9\u00ba\u0001\u0000\u0000\u0000\u00ba\u00bb\u0001\u0000\u0000"+
		"\u0000\u00bb\u01c6\u0005\u00e0\u0000\u0000\u00bc\u00bd\u0005?\u0000\u0000"+
		"\u00bd\u00be\u0005Y\u0000\u0000\u00be\u00c1\u0007\u0012\u0000\u0000\u00bf"+
		"\u00c0\u0005\u011c\u0000\u0000\u00c0\u00c2\u0007\u0013\u0000\u0000\u00c1"+
		"\u00bf\u0001\u0000\u0000\u0000\u00c1\u00c2\u0001\u0000\u0000\u0000\u00c2"+
		"\u00c3\u0001\u0000\u0000\u0000\u00c3\u01c6\u0005\u00e0\u0000\u0000\u00c4"+
		"\u01c6\u0005B\u0000\u0000\u00c5\u01c6\u0005C\u0000\u0000\u00c6\u00c7\u0005"+
		"G\u0000\u0000\u00c7\u00c8\u0005Y\u0000\u0000\u00c8\u00c9\u0007\u0014\u0000"+
		"\u0000\u00c9\u01c6\u0005\u00e0\u0000\u0000\u00ca\u00cb\u0007\u0015\u0000"+
		"\u0000\u00cb\u00cc\u0005Y\u0000\u0000\u00cc\u00cd\u0007\u0016\u0000\u0000"+
		"\u00cd\u01c6\u0005\u00e0\u0000\u0000\u00ce\u01c6\u0005N\u0000\u0000\u00cf"+
		"\u01c6\u0005O\u0000\u0000\u00d0\u01c6\u0005P\u0000\u0000\u00d1\u01c6\u0005"+
		"R\u0000\u0000\u00d2\u00d3\u0007\u0017\u0000\u0000\u00d3\u00d4\u0005Y\u0000"+
		"\u0000\u00d4\u00d5\u0003<\u001e\u0000\u00d5\u00d6\u0005\u00e0\u0000\u0000"+
		"\u00d6\u01c6\u0001\u0000\u0000\u0000\u00d7\u01c6\u0005T\u0000\u0000\u00d8"+
		"\u01c6\u0005U\u0000\u0000\u00d9\u01c6\u0005[\u0000\u0000\u00da\u00db\u0005"+
		"\\\u0000\u0000\u00db\u00dc\u0005Y\u0000\u0000\u00dc\u00dd\u0003<\u001e"+
		"\u0000\u00dd\u00de\u0005\u011c\u0000\u0000\u00de\u00e1\u0003<\u001e\u0000"+
		"\u00df\u00e0\u0005\u011c\u0000\u0000\u00e0\u00e2\u0003<\u001e\u0000\u00e1"+
		"\u00df\u0001\u0000\u0000\u0000\u00e1\u00e2\u0001\u0000\u0000\u0000\u00e2"+
		"\u00e3\u0001\u0000\u0000\u0000\u00e3\u00e4\u0005\u00e0\u0000\u0000\u00e4"+
		"\u01c6\u0001\u0000\u0000\u0000\u00e5\u00e9\u0007\u0018\u0000\u0000\u00e6"+
		"\u00e7\u0005Y\u0000\u0000\u00e7\u00e8\u0007\u0019\u0000\u0000\u00e8\u00ea"+
		"\u0005\u00e0\u0000\u0000\u00e9\u00e6\u0001\u0000\u0000\u0000\u00e9\u00ea"+
		"\u0001\u0000\u0000\u0000\u00ea\u01c6\u0001\u0000\u0000\u0000\u00eb\u00ef"+
		"\u0005b\u0000\u0000\u00ec\u00ed\u0005Y\u0000\u0000\u00ed\u00ee\u0007\u001a"+
		"\u0000\u0000\u00ee\u00f0\u0005\u00e0\u0000\u0000\u00ef\u00ec\u0001\u0000"+
		"\u0000\u0000\u00ef\u00f0\u0001\u0000\u0000\u0000\u00f0\u01c6\u0001\u0000"+
		"\u0000\u0000\u00f1\u00f2\u0005e\u0000\u0000\u00f2\u00f3\u0005Y\u0000\u0000"+
		"\u00f3\u00f4\u0007\u001b\u0000\u0000\u00f4\u01c6\u0005\u00e0\u0000\u0000"+
		"\u00f5\u01c6\u0005h\u0000\u0000\u00f6\u01c6\u0005i\u0000\u0000\u00f7\u01c6"+
		"\u0005k\u0000\u0000\u00f8\u01c6\u0005l\u0000\u0000\u00f9\u01c6\u0005n"+
		"\u0000\u0000\u00fa\u01c6\u0005o\u0000\u0000\u00fb\u01c6\u0005p\u0000\u0000"+
		"\u00fc\u0100\u0007\u001c\u0000\u0000\u00fd\u00fe\u0005Y\u0000\u0000\u00fe"+
		"\u00ff\u0007\u001d\u0000\u0000\u00ff\u0101\u0005\u00e0\u0000\u0000\u0100"+
		"\u00fd\u0001\u0000\u0000\u0000\u0100\u0101\u0001\u0000\u0000\u0000\u0101"+
		"\u01c6\u0001\u0000\u0000\u0000\u0102\u01c6\u0005r\u0000\u0000\u0103\u01c6"+
		"\u0007\u001e\u0000\u0000\u0104\u01c6\u0007\u001f\u0000\u0000\u0105\u01c6"+
		"\u0005w\u0000\u0000\u0106\u01c6\u0005y\u0000\u0000\u0107\u01c6\u0007 "+
		"\u0000\u0000\u0108\u01c6\u0005|\u0000\u0000\u0109\u01c6\u0005x\u0000\u0000"+
		"\u010a\u01c6\u0007!\u0000\u0000\u010b\u01c6\u0007\"\u0000\u0000\u010c"+
		"\u01c6\u0007#\u0000\u0000\u010d\u01c6\u0005\u0083\u0000\u0000\u010e\u01c6"+
		"\u0005\u0085\u0000\u0000\u010f\u01c6\u0005\u0086\u0000\u0000\u0110\u01c6"+
		"\u0007$\u0000\u0000\u0111\u01c6\u0007%\u0000\u0000\u0112\u01c6\u0005\u008b"+
		"\u0000\u0000\u0113\u01c6\u0007&\u0000\u0000\u0114\u01c6\u0005\u008d\u0000"+
		"\u0000\u0115\u01c6\u0005\u008e\u0000\u0000\u0116\u01c6\u0005\u0090\u0000"+
		"\u0000\u0117\u01c6\u0005\u0092\u0000\u0000\u0118\u01c6\u0005\u0093\u0000"+
		"\u0000\u0119\u01c6\u0005\u0094\u0000\u0000\u011a\u01c6\u0005\u0095\u0000"+
		"\u0000\u011b\u01c6\u0005\u0096\u0000\u0000\u011c\u01c6\u0007\'\u0000\u0000"+
		"\u011d\u01c6\u0005\u0099\u0000\u0000\u011e\u01c6\u0007(\u0000\u0000\u011f"+
		"\u01c6\u0007)\u0000\u0000\u0120\u01c6\u0007*\u0000\u0000\u0121\u01c6\u0005"+
		"\u00a0\u0000\u0000\u0122\u01c6\u0007+\u0000\u0000\u0123\u01c6\u0005\u00a3"+
		"\u0000\u0000\u0124\u01c6\u0005\u00a4\u0000\u0000\u0125\u01c6\u0005\u00a6"+
		"\u0000\u0000\u0126\u01c6\u0005\u00a7\u0000\u0000\u0127\u01c6\u0007,\u0000"+
		"\u0000\u0128\u01c6\u0007-\u0000\u0000\u0129\u01c6\u0005\u00ad\u0000\u0000"+
		"\u012a\u01c6\u0005\u00ae\u0000\u0000\u012b\u01c6\u0007.\u0000\u0000\u012c"+
		"\u01c6\u0007/\u0000\u0000\u012d\u01c6\u0005\u00b3\u0000\u0000\u012e\u01c6"+
		"\u00070\u0000\u0000\u012f\u01c6\u0005\u00b7\u0000\u0000\u0130\u01c6\u0005"+
		"\u00b8\u0000\u0000\u0131\u01c6\u0005\u00ba\u0000\u0000\u0132\u01c6\u0007"+
		"1\u0000\u0000\u0133\u01c6\u0005\u00c1\u0000\u0000\u0134\u0135\u00072\u0000"+
		"\u0000\u0135\u0136\u0005Y\u0000\u0000\u0136\u0137\u00073\u0000\u0000\u0137"+
		"\u01c6\u0005\u00e0\u0000\u0000\u0138\u01c6\u0005\u00ba\u0000\u0000\u0139"+
		"\u01c6\u00074\u0000\u0000\u013a\u01c6\u0005\u00bf\u0000\u0000\u013b\u01c6"+
		"\u00075\u0000\u0000\u013c\u013d\u0005\u00c5\u0000\u0000\u013d\u013e\u0005"+
		"Y\u0000\u0000\u013e\u013f\u00076\u0000\u0000\u013f\u01c6\u0005\u00e0\u0000"+
		"\u0000\u0140\u01c6\u00077\u0000\u0000\u0141\u01c6\u00078\u0000\u0000\u0142"+
		"\u0143\u0005\u00cd\u0000\u0000\u0143\u0144\u0005Y\u0000\u0000\u0144\u0145"+
		"\u0003<\u001e\u0000\u0145\u0146\u0005\u011c\u0000\u0000\u0146\u0149\u0003"+
		"<\u001e\u0000\u0147\u0148\u0005\u011c\u0000\u0000\u0148\u014a\u0003<\u001e"+
		"\u0000\u0149\u0147\u0001\u0000\u0000\u0000\u0149\u014a\u0001\u0000\u0000"+
		"\u0000\u014a\u014b\u0001\u0000\u0000\u0000\u014b\u014c\u0005\u00e0\u0000"+
		"\u0000\u014c\u01c6\u0001\u0000\u0000\u0000\u014d\u014e\u0005\u00ce\u0000"+
		"\u0000\u014e\u014f\u0005Y\u0000\u0000\u014f\u0150\u0003<\u001e\u0000\u0150"+
		"\u0151\u0005\u011c\u0000\u0000\u0151\u0152\u0003<\u001e\u0000\u0152\u0153"+
		"\u0005\u00e0\u0000\u0000\u0153\u01c6\u0001\u0000\u0000\u0000\u0154\u0158"+
		"\u00079\u0000\u0000\u0155\u0156\u0005Y\u0000\u0000\u0156\u0157\u0007:"+
		"\u0000\u0000\u0157\u0159\u0005\u00e0\u0000\u0000\u0158\u0155\u0001\u0000"+
		"\u0000\u0000\u0158\u0159\u0001\u0000\u0000\u0000\u0159\u01c6\u0001\u0000"+
		"\u0000\u0000\u015a\u01c6\u0005\u00d0\u0000\u0000\u015b\u01c6\u0005\u00d2"+
		"\u0000\u0000\u015c\u01c6\u0005\u00cc\u0000\u0000\u015d\u015e\u0007;\u0000"+
		"\u0000\u015e\u015f\u0005Y\u0000\u0000\u015f\u0160\u0003:\u001d\u0000\u0160"+
		"\u0161\u0005\u00e0\u0000\u0000\u0161\u01c6\u0001\u0000\u0000\u0000\u0162"+
		"\u0163\u0007<\u0000\u0000\u0163\u0164\u0005Y\u0000\u0000\u0164\u0165\u0007"+
		"=\u0000\u0000\u0165\u01c6\u0005\u00e0\u0000\u0000\u0166\u01c6\u0005\u00da"+
		"\u0000\u0000\u0167\u01c6\u0007>\u0000\u0000\u0168\u01c6\u0005\u00dc\u0000"+
		"\u0000\u0169\u016a\u0005\u00df\u0000\u0000\u016a\u016e\u0005Y\u0000\u0000"+
		"\u016b\u016f\u0005\u0005\u0000\u0000\u016c\u016f\u0005\t\u0000\u0000\u016d"+
		"\u016f\u0003<\u001e\u0000\u016e\u016b\u0001\u0000\u0000\u0000\u016e\u016c"+
		"\u0001\u0000\u0000\u0000\u016e\u016d\u0001\u0000\u0000\u0000\u016f\u0170"+
		"\u0001\u0000\u0000\u0000\u0170\u01c6\u0005\u00e0\u0000\u0000\u0171\u0178"+
		"\u0007?\u0000\u0000\u0172\u0173\u0005Y\u0000\u0000\u0173\u0174\u0003<"+
		"\u001e\u0000\u0174\u0175\u0005\u011c\u0000\u0000\u0175\u0176\u0003<\u001e"+
		"\u0000\u0176\u0177\u0005\u00e0\u0000\u0000\u0177\u0179\u0001\u0000\u0000"+
		"\u0000\u0178\u0172\u0001\u0000\u0000\u0000\u0178\u0179\u0001\u0000\u0000"+
		"\u0000\u0179\u01c6\u0001\u0000\u0000\u0000\u017a\u017b\u0007@\u0000\u0000"+
		"\u017b\u017e\u0005Y\u0000\u0000\u017c\u017f\u0005]\u0000\u0000\u017d\u017f"+
		"\u0003<\u001e\u0000\u017e\u017c\u0001\u0000\u0000\u0000\u017e\u017d\u0001"+
		"\u0000\u0000\u0000\u017f\u0180\u0001\u0000\u0000\u0000\u0180\u01c6\u0005"+
		"\u00e0\u0000\u0000\u0181\u01c6\u0007A\u0000\u0000\u0182\u01c6\u0005\u00e8"+
		"\u0000\u0000\u0183\u0184\u0005\u00e9\u0000\u0000\u0184\u0185\u0005Y\u0000"+
		"\u0000\u0185\u0186\u0003<\u001e\u0000\u0186\u0187\u0005\u00e0\u0000\u0000"+
		"\u0187\u01c6\u0001\u0000\u0000\u0000\u0188\u01c6\u0005\u00ea\u0000\u0000"+
		"\u0189\u018e\u0005\u00eb\u0000\u0000\u018a\u018b\u0005Y\u0000\u0000\u018b"+
		"\u018c\u0003<\u001e\u0000\u018c\u018d\u0005\u00e0\u0000\u0000\u018d\u018f"+
		"\u0001\u0000\u0000\u0000\u018e\u018a\u0001\u0000\u0000\u0000\u018e\u018f"+
		"\u0001\u0000\u0000\u0000\u018f\u01c6\u0001\u0000\u0000\u0000\u0190\u01c6"+
		"\u0007B\u0000\u0000\u0191\u01c6\u0007C\u0000\u0000\u0192\u01c6\u0005\u00f7"+
		"\u0000\u0000\u0193\u01c6\u0007D\u0000\u0000\u0194\u01a6\u0005\u00fb\u0000"+
		"\u0000\u0195\u0197\u0005Y\u0000\u0000\u0196\u0198\u0007E\u0000\u0000\u0197"+
		"\u0196\u0001\u0000\u0000\u0000\u0197\u0198\u0001\u0000\u0000\u0000\u0198"+
		"\u019a\u0001\u0000\u0000\u0000\u0199\u019b\u0005\u011c\u0000\u0000\u019a"+
		"\u0199\u0001\u0000\u0000\u0000\u019a\u019b\u0001\u0000\u0000\u0000\u019b"+
		"\u019d\u0001\u0000\u0000\u0000\u019c\u019e\u0007F\u0000\u0000\u019d\u019c"+
		"\u0001\u0000\u0000\u0000\u019d\u019e\u0001\u0000\u0000\u0000\u019e\u01a0"+
		"\u0001\u0000\u0000\u0000\u019f\u01a1\u0005\u011c\u0000\u0000\u01a0\u019f"+
		"\u0001\u0000\u0000\u0000\u01a0\u01a1\u0001\u0000\u0000\u0000\u01a1\u01a3"+
		"\u0001\u0000\u0000\u0000\u01a2\u01a4\u0007G\u0000\u0000\u01a3\u01a2\u0001"+
		"\u0000\u0000\u0000\u01a3\u01a4\u0001\u0000\u0000\u0000\u01a4\u01a5\u0001"+
		"\u0000\u0000\u0000\u01a5\u01a7\u0005\u00e0\u0000\u0000\u01a6\u0195\u0001"+
		"\u0000\u0000\u0000\u01a6\u01a7\u0001\u0000\u0000\u0000\u01a7\u01c6\u0001"+
		"\u0000\u0000\u0000\u01a8\u01c6\u0005\u00fc\u0000\u0000\u01a9\u01aa\u0005"+
		"\u00ff\u0000\u0000\u01aa\u01ab\u0005Y\u0000\u0000\u01ab\u01ac\u0007H\u0000"+
		"\u0000\u01ac\u01c6\u0005\u00e0\u0000\u0000\u01ad\u01c6\u0005\u0102\u0000"+
		"\u0000\u01ae\u01af\u0007I\u0000\u0000\u01af\u01b0\u0005Y\u0000\u0000\u01b0"+
		"\u01b1\u0003:\u001d\u0000\u01b1\u01b2\u0005\u00e0\u0000\u0000\u01b2\u01c6"+
		"\u0001\u0000\u0000\u0000\u01b3\u01b4\u0007J\u0000\u0000\u01b4\u01b5\u0005"+
		"Y\u0000\u0000\u01b5\u01b6\u0007K\u0000\u0000\u01b6\u01c6\u0005\u00e0\u0000"+
		"\u0000\u01b7\u01bd\u0007L\u0000\u0000\u01b8\u01ba\u0005Y\u0000\u0000\u01b9"+
		"\u01bb\u0007M\u0000\u0000\u01ba\u01b9\u0001\u0000\u0000\u0000\u01ba\u01bb"+
		"\u0001\u0000\u0000\u0000\u01bb\u01bc\u0001\u0000\u0000\u0000\u01bc\u01be"+
		"\u0005\u00e0\u0000\u0000\u01bd\u01b8\u0001\u0000\u0000\u0000\u01bd\u01be"+
		"\u0001\u0000\u0000\u0000\u01be\u01c6\u0001\u0000\u0000\u0000\u01bf\u01c0"+
		"\u0007N\u0000\u0000\u01c0\u01c1\u0005Y\u0000\u0000\u01c1\u01c2\u0003<"+
		"\u001e\u0000\u01c2\u01c3\u0005\u00e0\u0000\u0000\u01c3\u01c6\u0001\u0000"+
		"\u0000\u0000\u01c4\u01c6\u0005\u010c\u0000\u0000\u01c5o\u0001\u0000\u0000"+
		"\u0000\u01c5p\u0001\u0000\u0000\u0000\u01c5q\u0001\u0000\u0000\u0000\u01c5"+
		"r\u0001\u0000\u0000\u0000\u01c5v\u0001\u0000\u0000\u0000\u01c5w\u0001"+
		"\u0000\u0000\u0000\u01c5x\u0001\u0000\u0000\u0000\u01c5}\u0001\u0000\u0000"+
		"\u0000\u01c5~\u0001\u0000\u0000\u0000\u01c5\u0085\u0001\u0000\u0000\u0000"+
		"\u01c5\u0086\u0001\u0000\u0000\u0000\u01c5\u0087\u0001\u0000\u0000\u0000"+
		"\u01c5\u008c\u0001\u0000\u0000\u0000\u01c5\u008d\u0001\u0000\u0000\u0000"+
		"\u01c5\u008e\u0001\u0000\u0000\u0000\u01c5\u008f\u0001\u0000\u0000\u0000"+
		"\u01c5\u0094\u0001\u0000\u0000\u0000\u01c5\u0099\u0001\u0000\u0000\u0000"+
		"\u01c5\u00a7\u0001\u0000\u0000\u0000\u01c5\u00a8\u0001\u0000\u0000\u0000"+
		"\u01c5\u00a9\u0001\u0000\u0000\u0000\u01c5\u00aa\u0001\u0000\u0000\u0000"+
		"\u01c5\u00ab\u0001\u0000\u0000\u0000\u01c5\u00ac\u0001\u0000\u0000\u0000"+
		"\u01c5\u00ad\u0001\u0000\u0000\u0000\u01c5\u00ae\u0001\u0000\u0000\u0000"+
		"\u01c5\u00af\u0001\u0000\u0000\u0000\u01c5\u00b0\u0001\u0000\u0000\u0000"+
		"\u01c5\u00b1\u0001\u0000\u0000\u0000\u01c5\u00b2\u0001\u0000\u0000\u0000"+
		"\u01c5\u00b3\u0001\u0000\u0000\u0000\u01c5\u00b4\u0001\u0000\u0000\u0000"+
		"\u01c5\u00bc\u0001\u0000\u0000\u0000\u01c5\u00c4\u0001\u0000\u0000\u0000"+
		"\u01c5\u00c5\u0001\u0000\u0000\u0000\u01c5\u00c6\u0001\u0000\u0000\u0000"+
		"\u01c5\u00ca\u0001\u0000\u0000\u0000\u01c5\u00ce\u0001\u0000\u0000\u0000"+
		"\u01c5\u00cf\u0001\u0000\u0000\u0000\u01c5\u00d0\u0001\u0000\u0000\u0000"+
		"\u01c5\u00d1\u0001\u0000\u0000\u0000\u01c5\u00d2\u0001\u0000\u0000\u0000"+
		"\u01c5\u00d7\u0001\u0000\u0000\u0000\u01c5\u00d8\u0001\u0000\u0000\u0000"+
		"\u01c5\u00d9\u0001\u0000\u0000\u0000\u01c5\u00da\u0001\u0000\u0000\u0000"+
		"\u01c5\u00e5\u0001\u0000\u0000\u0000\u01c5\u00eb\u0001\u0000\u0000\u0000"+
		"\u01c5\u00f1\u0001\u0000\u0000\u0000\u01c5\u00f5\u0001\u0000\u0000\u0000"+
		"\u01c5\u00f6\u0001\u0000\u0000\u0000\u01c5\u00f7\u0001\u0000\u0000\u0000"+
		"\u01c5\u00f8\u0001\u0000\u0000\u0000\u01c5\u00f9\u0001\u0000\u0000\u0000"+
		"\u01c5\u00fa\u0001\u0000\u0000\u0000\u01c5\u00fb\u0001\u0000\u0000\u0000"+
		"\u01c5\u00fc\u0001\u0000\u0000\u0000\u01c5\u0102\u0001\u0000\u0000\u0000"+
		"\u01c5\u0103\u0001\u0000\u0000\u0000\u01c5\u0104\u0001\u0000\u0000\u0000"+
		"\u01c5\u0105\u0001\u0000\u0000\u0000\u01c5\u0106\u0001\u0000\u0000\u0000"+
		"\u01c5\u0107\u0001\u0000\u0000\u0000\u01c5\u0108\u0001\u0000\u0000\u0000"+
		"\u01c5\u0109\u0001\u0000\u0000\u0000\u01c5\u010a\u0001\u0000\u0000\u0000"+
		"\u01c5\u010b\u0001\u0000\u0000\u0000\u01c5\u010c\u0001\u0000\u0000\u0000"+
		"\u01c5\u010d\u0001\u0000\u0000\u0000\u01c5\u010e\u0001\u0000\u0000\u0000"+
		"\u01c5\u010f\u0001\u0000\u0000\u0000\u01c5\u0110\u0001\u0000\u0000\u0000"+
		"\u01c5\u0111\u0001\u0000\u0000\u0000\u01c5\u0112\u0001\u0000\u0000\u0000"+
		"\u01c5\u0113\u0001\u0000\u0000\u0000\u01c5\u0114\u0001\u0000\u0000\u0000"+
		"\u01c5\u0115\u0001\u0000\u0000\u0000\u01c5\u0116\u0001\u0000\u0000\u0000"+
		"\u01c5\u0117\u0001\u0000\u0000\u0000\u01c5\u0118\u0001\u0000\u0000\u0000"+
		"\u01c5\u0119\u0001\u0000\u0000\u0000\u01c5\u011a\u0001\u0000\u0000\u0000"+
		"\u01c5\u011b\u0001\u0000\u0000\u0000\u01c5\u011c\u0001\u0000\u0000\u0000"+
		"\u01c5\u011d\u0001\u0000\u0000\u0000\u01c5\u011e\u0001\u0000\u0000\u0000"+
		"\u01c5\u011f\u0001\u0000\u0000\u0000\u01c5\u0120\u0001\u0000\u0000\u0000"+
		"\u01c5\u0121\u0001\u0000\u0000\u0000\u01c5\u0122\u0001\u0000\u0000\u0000"+
		"\u01c5\u0123\u0001\u0000\u0000\u0000\u01c5\u0124\u0001\u0000\u0000\u0000"+
		"\u01c5\u0125\u0001\u0000\u0000\u0000\u01c5\u0126\u0001\u0000\u0000\u0000"+
		"\u01c5\u0127\u0001\u0000\u0000\u0000\u01c5\u0128\u0001\u0000\u0000\u0000"+
		"\u01c5\u0129\u0001\u0000\u0000\u0000\u01c5\u012a\u0001\u0000\u0000\u0000"+
		"\u01c5\u012b\u0001\u0000\u0000\u0000\u01c5\u012c\u0001\u0000\u0000\u0000"+
		"\u01c5\u012d\u0001\u0000\u0000\u0000\u01c5\u012e\u0001\u0000\u0000\u0000"+
		"\u01c5\u012f\u0001\u0000\u0000\u0000\u01c5\u0130\u0001\u0000\u0000\u0000"+
		"\u01c5\u0131\u0001\u0000\u0000\u0000\u01c5\u0132\u0001\u0000\u0000\u0000"+
		"\u01c5\u0133\u0001\u0000\u0000\u0000\u01c5\u0134\u0001\u0000\u0000\u0000"+
		"\u01c5\u0138\u0001\u0000\u0000\u0000\u01c5\u0139\u0001\u0000\u0000\u0000"+
		"\u01c5\u013a\u0001\u0000\u0000\u0000\u01c5\u013b\u0001\u0000\u0000\u0000"+
		"\u01c5\u013c\u0001\u0000\u0000\u0000\u01c5\u0140\u0001\u0000\u0000\u0000"+
		"\u01c5\u0141\u0001\u0000\u0000\u0000\u01c5\u0142\u0001\u0000\u0000\u0000"+
		"\u01c5\u014d\u0001\u0000\u0000\u0000\u01c5\u0154\u0001\u0000\u0000\u0000"+
		"\u01c5\u015a\u0001\u0000\u0000\u0000\u01c5\u015b\u0001\u0000\u0000\u0000"+
		"\u01c5\u015c\u0001\u0000\u0000\u0000\u01c5\u015d\u0001\u0000\u0000\u0000"+
		"\u01c5\u0162\u0001\u0000\u0000\u0000\u01c5\u0166\u0001\u0000\u0000\u0000"+
		"\u01c5\u0167\u0001\u0000\u0000\u0000\u01c5\u0168\u0001\u0000\u0000\u0000"+
		"\u01c5\u0169\u0001\u0000\u0000\u0000\u01c5\u0171\u0001\u0000\u0000\u0000"+
		"\u01c5\u017a\u0001\u0000\u0000\u0000\u01c5\u0181\u0001\u0000\u0000\u0000"+
		"\u01c5\u0182\u0001\u0000\u0000\u0000\u01c5\u0183\u0001\u0000\u0000\u0000"+
		"\u01c5\u0188\u0001\u0000\u0000\u0000\u01c5\u0189\u0001\u0000\u0000\u0000"+
		"\u01c5\u0190\u0001\u0000\u0000\u0000\u01c5\u0191\u0001\u0000\u0000\u0000"+
		"\u01c5\u0192\u0001\u0000\u0000\u0000\u01c5\u0193\u0001\u0000\u0000\u0000"+
		"\u01c5\u0194\u0001\u0000\u0000\u0000\u01c5\u01a8\u0001\u0000\u0000\u0000"+
		"\u01c5\u01a9\u0001\u0000\u0000\u0000\u01c5\u01ad\u0001\u0000\u0000\u0000"+
		"\u01c5\u01ae\u0001\u0000\u0000\u0000\u01c5\u01b3\u0001\u0000\u0000\u0000"+
		"\u01c5\u01b7\u0001\u0000\u0000\u0000\u01c5\u01bf\u0001\u0000\u0000\u0000"+
		"\u01c5\u01c4\u0001\u0000\u0000\u0000\u01c6\u0007\u0001\u0000\u0000\u0000"+
		"\u01c7\u01c8\u00057\u0000\u0000\u01c8\u01c9\u0005\u0012\u0000\u0000\u01c9"+
		"\u01ca\u00030\u0018\u0000\u01ca\u01cc\u00054\u0000\u0000\u01cb\u01cd\u0005"+
		"\u011d\u0000\u0000\u01cc\u01cb\u0001\u0000\u0000\u0000\u01cc\u01cd\u0001"+
		"\u0000\u0000\u0000\u01cd\t\u0001\u0000\u0000\u0000\u01ce\u01cf\u00057"+
		"\u0000\u0000\u01cf\u01d0\u0005\u00eb\u0000\u0000\u01d0\u01d1\u00032\u0019"+
		"\u0000\u01d1\u01d3\u00054\u0000\u0000\u01d2\u01d4\u0005\u011d\u0000\u0000"+
		"\u01d3\u01d2\u0001\u0000\u0000\u0000\u01d3\u01d4\u0001\u0000\u0000\u0000"+
		"\u01d4\u000b\u0001\u0000\u0000\u0000\u01d5\u01d6\u00057\u0000\u0000\u01d6"+
		"\u01d7\u0005\u00eb\u0000\u0000\u01d7\u01db\u0005F\u0000\u0000\u01d8\u01dc"+
		"\u0003<\u001e\u0000\u01d9\u01dc\u0003:\u001d\u0000\u01da\u01dc\u0003>"+
		"\u001f\u0000\u01db\u01d8\u0001\u0000\u0000\u0000\u01db\u01d9\u0001\u0000"+
		"\u0000\u0000\u01db\u01da\u0001\u0000\u0000\u0000\u01dc\u01dd\u0001\u0000"+
		"\u0000\u0000\u01dd\u01df\u00054\u0000\u0000\u01de\u01e0\u0005\u011d\u0000"+
		"\u0000\u01df\u01de\u0001\u0000\u0000\u0000\u01df\u01e0\u0001\u0000\u0000"+
		"\u0000\u01e0\r\u0001\u0000\u0000\u0000\u01e1\u01e2\u00057\u0000\u0000"+
		"\u01e2\u01e3\u0005\u00ee\u0000\u0000\u01e3\u01e4\u00030\u0018\u0000\u01e4"+
		"\u01e6\u00054\u0000\u0000\u01e5\u01e7\u0005\u011d\u0000\u0000\u01e6\u01e5"+
		"\u0001\u0000\u0000\u0000\u01e6\u01e7\u0001\u0000\u0000\u0000\u01e7\u000f"+
		"\u0001\u0000\u0000\u0000\u01e8\u01e9\u0005\u0019\u0000\u0000\u01e9\u01f2"+
		"\u0003\u0012\t\u0000\u01ea\u01ef\u0003 \u0010\u0000\u01eb\u01ef\u0003"+
		"\"\u0011\u0000\u01ec\u01ef\u0003\u0016\u000b\u0000\u01ed\u01ef\u0005\u00f6"+
		"\u0000\u0000\u01ee\u01ea\u0001\u0000\u0000\u0000\u01ee\u01eb\u0001\u0000"+
		"\u0000\u0000\u01ee\u01ec\u0001\u0000\u0000\u0000\u01ee\u01ed\u0001\u0000"+
		"\u0000\u0000\u01ef\u01f1\u0001\u0000\u0000\u0000\u01f0\u01ee\u0001\u0000"+
		"\u0000\u0000\u01f1\u01f4\u0001\u0000\u0000\u0000\u01f2\u01f0\u0001\u0000"+
		"\u0000\u0000\u01f2\u01f3\u0001\u0000\u0000\u0000\u01f3\u01f5\u0001\u0000"+
		"\u0000\u0000\u01f4\u01f2\u0001\u0000\u0000\u0000\u01f5\u01f6\u0005\u011d"+
		"\u0000\u0000\u01f6\u0011\u0001\u0000\u0000\u0000\u01f7\u01fb\u0003<\u001e"+
		"\u0000\u01f8\u01fb\u0003:\u001d\u0000\u01f9\u01fb\u0003>\u001f\u0000\u01fa"+
		"\u01f7\u0001\u0000\u0000\u0000\u01fa\u01f8\u0001\u0000\u0000\u0000\u01fa"+
		"\u01f9\u0001\u0000\u0000\u0000\u01fb\u01fe\u0001\u0000\u0000\u0000\u01fc"+
		"\u01fd\u0007O\u0000\u0000\u01fd\u01ff\u0003\u0014\n\u0000\u01fe\u01fc"+
		"\u0001\u0000\u0000\u0000\u01fe\u01ff\u0001\u0000\u0000\u0000\u01ff\u0013"+
		"\u0001\u0000\u0000\u0000\u0200\u0203\u0003<\u001e\u0000\u0201\u0203\u0003"+
		":\u001d\u0000\u0202\u0200\u0001\u0000\u0000\u0000\u0202\u0201\u0001\u0000"+
		"\u0000\u0000\u0203\u0015\u0001\u0000\u0000\u0000\u0204\u0205\u0005\u00de"+
		"\u0000\u0000\u0205\u0209\u0003\u001e\u000f\u0000\u0206\u0208\u0003\u001e"+
		"\u000f\u0000\u0207\u0206\u0001\u0000\u0000\u0000\u0208\u020b\u0001\u0000"+
		"\u0000\u0000\u0209\u0207\u0001\u0000\u0000\u0000\u0209\u020a\u0001\u0000"+
		"\u0000\u0000\u020a\u0017\u0001\u0000\u0000\u0000\u020b\u0209\u0001\u0000"+
		"\u0000\u0000\u020c\u0211\u0003\u001a\r\u0000\u020d\u0210\u0003\u0010\b"+
		"\u0000\u020e\u0210\u00030\u0018\u0000\u020f\u020d\u0001\u0000\u0000\u0000"+
		"\u020f\u020e\u0001\u0000\u0000\u0000\u0210\u0213\u0001\u0000\u0000\u0000"+
		"\u0211\u020f\u0001\u0000\u0000\u0000\u0211\u0212\u0001\u0000\u0000\u0000"+
		"\u0212\u0215\u0001\u0000\u0000\u0000\u0213\u0211\u0001\u0000\u0000\u0000"+
		"\u0214\u0216\u0003\u001c\u000e\u0000\u0215\u0214\u0001\u0000\u0000\u0000"+
		"\u0215\u0216\u0001\u0000\u0000\u0000\u0216\u0019\u0001\u0000\u0000\u0000"+
		"\u0217\u0219\u0005\u00dd\u0000\u0000\u0218\u021a\u0003\u001e\u000f\u0000"+
		"\u0219\u0218\u0001\u0000\u0000\u0000\u021a\u021b\u0001\u0000\u0000\u0000"+
		"\u021b\u0219\u0001\u0000\u0000\u0000\u021b\u021c\u0001\u0000\u0000\u0000"+
		"\u021c\u021d\u0001\u0000\u0000\u0000\u021d\u021e\u0005\u011d\u0000\u0000"+
		"\u021e\u001b\u0001\u0000\u0000\u0000\u021f\u0220\u0005\u00dd\u0000\u0000"+
		"\u0220\u0221\u0005\u00c9\u0000\u0000\u0221\u0222\u0005\u011d\u0000\u0000"+
		"\u0222\u001d\u0001\u0000\u0000\u0000\u0223\u0224\u0003$\u0012\u0000\u0224"+
		"\u0225\u0005\u000f\u0000\u0000\u0225\u0227\u0003&\u0013\u0000\u0226\u0228"+
		"\u00038\u001c\u0000\u0227\u0226\u0001\u0000\u0000\u0000\u0227\u0228\u0001"+
		"\u0000\u0000\u0000\u0228\u022c\u0001\u0000\u0000\u0000\u0229\u022b\u0003"+
		" \u0010\u0000\u022a\u0229\u0001\u0000\u0000\u0000\u022b\u022e\u0001\u0000"+
		"\u0000\u0000\u022c\u022a\u0001\u0000\u0000\u0000\u022c\u022d\u0001\u0000"+
		"\u0000\u0000\u022d\u0230\u0001\u0000\u0000\u0000\u022e\u022c\u0001\u0000"+
		"\u0000\u0000\u022f\u0231\u0003\"\u0011\u0000\u0230\u022f\u0001\u0000\u0000"+
		"\u0000\u0230\u0231\u0001\u0000\u0000\u0000\u0231\u001f\u0001\u0000\u0000"+
		"\u0000\u0232\u0235\u0007O\u0000\u0000\u0233\u0236\u0003<\u001e\u0000\u0234"+
		"\u0236\u0003:\u001d\u0000\u0235\u0233\u0001\u0000\u0000\u0000\u0235\u0234"+
		"\u0001\u0000\u0000\u0000\u0236!\u0001\u0000\u0000\u0000\u0237\u023a\u0005"+
		"\u00cb\u0000\u0000\u0238\u023b\u0003<\u001e\u0000\u0239\u023b\u0003:\u001d"+
		"\u0000\u023a\u0238\u0001\u0000\u0000\u0000\u023a\u0239\u0001\u0000\u0000"+
		"\u0000\u023b#\u0001\u0000\u0000\u0000\u023c\u0241\u0003<\u001e\u0000\u023d"+
		"\u0241\u0003:\u001d\u0000\u023e\u0241\u0003.\u0017\u0000\u023f\u0241\u0003"+
		"4\u001a\u0000\u0240\u023c\u0001\u0000\u0000\u0000\u0240\u023d\u0001\u0000"+
		"\u0000\u0000\u0240\u023e\u0001\u0000\u0000\u0000\u0240\u023f\u0001\u0000"+
		"\u0000\u0000\u0241%\u0001\u0000\u0000\u0000\u0242\u0247\u0003<\u001e\u0000"+
		"\u0243\u0247\u0003:\u001d\u0000\u0244\u0247\u0003.\u0017\u0000\u0245\u0247"+
		"\u00034\u001a\u0000\u0246\u0242\u0001\u0000\u0000\u0000\u0246\u0243\u0001"+
		"\u0000\u0000\u0000\u0246\u0244\u0001\u0000\u0000\u0000\u0246\u0245\u0001"+
		"\u0000\u0000\u0000\u0247\'\u0001\u0000\u0000\u0000\u0248\u024a\u00050"+
		"\u0000\u0000\u0249\u024b\u0005\u011d\u0000\u0000\u024a\u0249\u0001\u0000"+
		"\u0000\u0000\u024a\u024b\u0001\u0000\u0000\u0000\u024b)\u0001\u0000\u0000"+
		"\u0000\u024c\u024e\u0007P\u0000\u0000\u024d\u024f\u0005\u011d\u0000\u0000"+
		"\u024e\u024d\u0001\u0000\u0000\u0000\u024e\u024f\u0001\u0000\u0000\u0000"+
		"\u024f+\u0001\u0000\u0000\u0000\u0250\u0251\u0005\u00fd\u0000\u0000\u0251"+
		"\u0253\u0003<\u001e\u0000\u0252\u0254\u0005\u011d\u0000\u0000\u0253\u0252"+
		"\u0001\u0000\u0000\u0000\u0253\u0254\u0001\u0000\u0000\u0000\u0254-\u0001"+
		"\u0000\u0000\u0000\u0255\u0257\u0005\u011e\u0000\u0000\u0256\u0258\u0003"+
		"0\u0018\u0000\u0257\u0256\u0001\u0000\u0000\u0000\u0257\u0258\u0001\u0000"+
		"\u0000\u0000\u0258\u0259\u0001\u0000\u0000\u0000\u0259\u025a\u0005\u011e"+
		"\u0000\u0000\u025a/\u0001\u0000\u0000\u0000\u025b\u025d\u00036\u001b\u0000"+
		"\u025c\u025b\u0001\u0000\u0000\u0000\u025d\u025e\u0001\u0000\u0000\u0000"+
		"\u025e\u025c\u0001\u0000\u0000\u0000\u025e\u025f\u0001\u0000\u0000\u0000"+
		"\u025f1\u0001\u0000\u0000\u0000\u0260\u0264\u00036\u001b\u0000\u0261\u0264"+
		"\u0005\u0019\u0000\u0000\u0262\u0264\u0005\u00dd\u0000\u0000\u0263\u0260"+
		"\u0001\u0000\u0000\u0000\u0263\u0261\u0001\u0000\u0000\u0000\u0263\u0262"+
		"\u0001\u0000\u0000\u0000\u0264\u0265\u0001\u0000\u0000\u0000\u0265\u0263"+
		"\u0001\u0000\u0000\u0000\u0265\u0266\u0001\u0000\u0000\u0000\u02663\u0001"+
		"\u0000\u0000\u0000\u0267\u026f\u0003:\u001d\u0000\u0268\u026f\u0003<\u001e"+
		"\u0000\u0269\u026f\u0003>\u001f\u0000\u026a\u026f\u0003@ \u0000\u026b"+
		"\u026f\u0005\u0129\u0000\u0000\u026c\u026f\u0005Y\u0000\u0000\u026d\u026f"+
		"\u0005\u00e0\u0000\u0000\u026e\u0267\u0001\u0000\u0000\u0000\u026e\u0268"+
		"\u0001\u0000\u0000\u0000\u026e\u0269\u0001\u0000\u0000\u0000\u026e\u026a"+
		"\u0001\u0000\u0000\u0000\u026e\u026b\u0001\u0000\u0000\u0000\u026e\u026c"+
		"\u0001\u0000\u0000\u0000\u026e\u026d\u0001\u0000\u0000\u0000\u026f\u0270"+
		"\u0001\u0000\u0000\u0000\u0270\u026e\u0001\u0000\u0000\u0000\u0270\u0271"+
		"\u0001\u0000\u0000\u0000\u02715\u0001\u0000\u0000\u0000\u0272\u027b\u0003"+
		":\u001d\u0000\u0273\u027b\u0003<\u001e\u0000\u0274\u027b\u0003>\u001f"+
		"\u0000\u0275\u027b\u0003@ \u0000\u0276\u027b\u0005\u0129\u0000\u0000\u0277"+
		"\u027b\u0005\u011d\u0000\u0000\u0278\u027b\u0005Y\u0000\u0000\u0279\u027b"+
		"\u0005\u00e0\u0000\u0000\u027a\u0272\u0001\u0000\u0000\u0000\u027a\u0273"+
		"\u0001\u0000\u0000\u0000\u027a\u0274\u0001\u0000\u0000\u0000\u027a\u0275"+
		"\u0001\u0000\u0000\u0000\u027a\u0276\u0001\u0000\u0000\u0000\u027a\u0277"+
		"\u0001\u0000\u0000\u0000\u027a\u0278\u0001\u0000\u0000\u0000\u027a\u0279"+
		"\u0001\u0000\u0000\u0000\u027b\u027c\u0001\u0000\u0000\u0000\u027c\u027a"+
		"\u0001\u0000\u0000\u0000\u027c\u027d\u0001\u0000\u0000\u0000\u027d7\u0001"+
		"\u0000\u0000\u0000\u027e\u0281\u0005Y\u0000\u0000\u027f\u0282\u0003<\u001e"+
		"\u0000\u0280\u0282\u0003:\u001d\u0000\u0281\u027f\u0001\u0000\u0000\u0000"+
		"\u0281\u0280\u0001\u0000\u0000\u0000\u0282\u028c\u0001\u0000\u0000\u0000"+
		"\u0283\u0285\u0005\u011c\u0000\u0000\u0284\u0283\u0001\u0000\u0000\u0000"+
		"\u0284\u0285\u0001\u0000\u0000\u0000\u0285\u0288\u0001\u0000\u0000\u0000"+
		"\u0286\u0289\u0003<\u001e\u0000\u0287\u0289\u0003:\u001d\u0000\u0288\u0286"+
		"\u0001\u0000\u0000\u0000\u0288\u0287\u0001\u0000\u0000\u0000\u0289\u028b"+
		"\u0001\u0000\u0000\u0000\u028a\u0284\u0001\u0000\u0000\u0000\u028b\u028e"+
		"\u0001\u0000\u0000\u0000\u028c\u028a\u0001\u0000\u0000\u0000\u028c\u028d"+
		"\u0001\u0000\u0000\u0000\u028d\u028f\u0001\u0000\u0000\u0000\u028e\u028c"+
		"\u0001\u0000\u0000\u0000\u028f\u0290\u0005\u00e0\u0000\u0000\u02909\u0001"+
		"\u0000\u0000\u0000\u0291\u0294\u0005\u0122\u0000\u0000\u0292\u0294\u0003"+
		"B!\u0000\u0293\u0291\u0001\u0000\u0000\u0000\u0293\u0292\u0001\u0000\u0000"+
		"\u0000\u0294;\u0001\u0000\u0000\u0000\u0295\u0296\u0007Q\u0000\u0000\u0296"+
		"=\u0001\u0000\u0000\u0000\u0297\u0298\u0005\u0123\u0000\u0000\u0298?\u0001"+
		"\u0000\u0000\u0000\u0299\u029b\u0005\u0126\u0000\u0000\u029a\u0299\u0001"+
		"\u0000\u0000\u0000\u029b\u029c\u0001\u0000\u0000\u0000\u029c\u029a\u0001"+
		"\u0000\u0000\u0000\u029c\u029d\u0001\u0000\u0000\u0000\u029dA\u0001\u0000"+
		"\u0000\u0000\u029e\u029f\u0007R\u0000\u0000\u029fC\u0001\u0000\u0000\u0000"+
		"HPRY]_ej\u0083\u009c\u009f\u00a2\u00a5\u00b9\u00c1\u00e1\u00e9\u00ef\u0100"+
		"\u0149\u0158\u016e\u0178\u017e\u018e\u0197\u019a\u019d\u01a0\u01a3\u01a6"+
		"\u01ba\u01bd\u01c5\u01cc\u01d3\u01db\u01df\u01e6\u01ee\u01f2\u01fa\u01fe"+
		"\u0202\u0209\u020f\u0211\u0215\u021b\u0227\u022c\u0230\u0235\u023a\u0240"+
		"\u0246\u024a\u024e\u0253\u0257\u025e\u0263\u0265\u026e\u0270\u027a\u027c"+
		"\u0281\u0284\u0288\u028c\u0293\u029c";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}
