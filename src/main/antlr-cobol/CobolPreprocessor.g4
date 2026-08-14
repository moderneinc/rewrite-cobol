
/*
* Copyright (C) 2017, Ulrich Wolffgang <ulrich.wolffgang@proleap.io>
* All rights reserved.
*
* This software may be modified and distributed under the terms
* of the MIT license. See the LICENSE file for details.
*/

/*
* COBOL Preprocessor Grammar for ANTLR4
*
* This is a preprocessor grammar for COBOL, which is part of the COBOL
* parser at https://github.com/uwol/proleap-cobol-parser.
*/

grammar CobolPreprocessor;

options { caseInsensitive = true; }

compilationUnit
   : (compilerOptions | copyStatement | execCicsStatement | execDliStatement | execSqlStatement | execSqlIncludeStatement | execSqlImsStatement | replaceOffStatement | replaceArea | ejectStatement | skipStatement | titleStatement | charDataLine)* EOF
   ;

// compiler options

compilerOptions
   : (PROCESS | CBL) (COMMACHAR? compilerOption | compilerXOpts)+
   ;

compilerXOpts
   : XOPTS LPARENCHAR compilerOption (COMMACHAR? compilerOption)* RPARENCHAR
   ;

compilerOption
   : ADATA | ADV | APOST
   | (ARITH | AR) LPARENCHAR (EXTEND | E_CHAR | COMPAT | C_CHAR) RPARENCHAR
   | AWO
   | BLOCK0
   | (BUFSIZE | BUF) LPARENCHAR literal RPARENCHAR
   | CBLCARD
   | CICS (LPARENCHAR literal RPARENCHAR)?
   | COBOL2 | COBOL3
   | (CODEPAGE | CP) LPARENCHAR literal RPARENCHAR
   | (COMPILE | C_CHAR)
   | CPP | CPSM
   | (CURRENCY | CURR) LPARENCHAR literal RPARENCHAR
   | DATA LPARENCHAR literal RPARENCHAR
   | (DATEPROC | DP) (LPARENCHAR (FLAG | NOFLAG)? COMMACHAR? (TRIG | NOTRIG)? RPARENCHAR)?
   | DBCS
   | (DECK | D_CHAR)
   | DEBUG
   | (DIAGTRUNC | DTR)
   | DLL
   | (DUMP | DU)
   | (DYNAM | DYN)
   | EDF | EPILOG
   | EXIT
   | (EXPORTALL | EXP)
   | (FASTSRT | FSRT)
   | FEPI
   | (FLAG | F_CHAR) LPARENCHAR (E_CHAR | I_CHAR | S_CHAR | U_CHAR | W_CHAR) (COMMACHAR (E_CHAR | I_CHAR | S_CHAR | U_CHAR | W_CHAR))? RPARENCHAR
   | FLAGSTD LPARENCHAR (M_CHAR | I_CHAR | H_CHAR) (COMMACHAR (D_CHAR | DD | N_CHAR | NN | S_CHAR | SS))? RPARENCHAR
   | GDS | GRAPHIC
   | INTDATE LPARENCHAR (ANSI | LILIAN) RPARENCHAR
   | (LANGUAGE | LANG) LPARENCHAR (ENGLISH | CS | EN | JA | JP | KA | UE) RPARENCHAR
   | LEASM | LENGTH | LIB | LIN
   | (LINECOUNT | LC) LPARENCHAR literal RPARENCHAR
   | LINKAGE | LIST
   | MAP
   | MARGINS LPARENCHAR literal COMMACHAR literal (COMMACHAR literal)? RPARENCHAR
   | (MDECK | MD) (LPARENCHAR (C_CHAR | COMPILE | NOC | NOCOMPILE) RPARENCHAR)?
   | NAME (LPARENCHAR (ALIAS | NOALIAS) RPARENCHAR)?
   | NATLANG LPARENCHAR (CS | EN | KA) RPARENCHAR
   | NOADATA | NOADV | NOAWO
   | NOBLOCK0
   | NOCBLCARD | NOCICS | NOCMPR2
   | (NOCOMPILE | NOC) (LPARENCHAR (S_CHAR | E_CHAR | W_CHAR) RPARENCHAR)?
   | NOCPSM
   | (NOCURRENCY | NOCURR)
   | (NODATEPROC | NODP)
   | NODBCS | NODEBUG
   | (NODECK | NOD)
   | NODLL | NODE
   | (NODUMP | NODU)
   | (NODIAGTRUNC | NODTR)
   | (NODYNAM | NODYN)
   | NOEDF | NOEPILOG | NOEXIT
   | (NOEXPORTALL | NOEXP)
   | (NOFASTSRT | NOFSRT)
   | NOFEPI
   | (NOFLAG | NOF)
   | NOFLAGMIG | NOFLAGSTD
   | NOGRAPHIC
   | NOLENGTH | NOLIB | NOLINKAGE | NOLIST
   | NOMAP
   | (NOMDECK | NOMD)
   | NONAME
   | (NONUMBER | NONUM)
   | (NOOBJECT | NOOBJ)
   | (NOOFFSET | NOOFF)
   | NOOPSEQUENCE
   | (NOOPTIMIZE | NOOPT)
   | NOOPTIONS
   | NOP | NOPROLOG
   | NORENT
   | (NOSEQUENCE | NOSEQ)
   | (NOSOURCE | NOS)
   | NOSPIE | NOSQL
   | (NOSQLCCSID | NOSQLC)
   | (NOSSRANGE | NOSSR)
   | NOSTDTRUNC
   | (NOTERMINAL | NOTERM) | NOTEST | NOTHREAD
   | NOVBREF
   | (NOWORD | NOWD)
   | NSEQ
   | (NSYMBOL | NS) LPARENCHAR (NATIONAL | NAT | DBCS) RPARENCHAR
   | NOVBREF
   | (NOXREF | NOX)
   | NOZWB
   | (NUMBER | NUM)
   | NUMPROC LPARENCHAR (MIG | NOPFD | PFD) RPARENCHAR
   | (OBJECT | OBJ)
   | (OFFSET | OFF)
   | OPMARGINS LPARENCHAR literal COMMACHAR literal (COMMACHAR literal)? RPARENCHAR
   | OPSEQUENCE LPARENCHAR literal COMMACHAR literal RPARENCHAR
   | (OPTIMIZE | OPT) (LPARENCHAR (FULL | STD) RPARENCHAR)?
   | OPTFILE | OPTIONS | OP
   | (OUTDD | OUT) LPARENCHAR cobolWord RPARENCHAR
   | (PGMNAME | PGMN) LPARENCHAR (CO | COMPAT | LM | LONGMIXED | LONGUPPER | LU | M_CHAR | MIXED | U_CHAR | UPPER) RPARENCHAR
   | PROLOG
   | (QUOTE | Q_CHAR)
   | RENT
   | RMODE LPARENCHAR (ANY | AUTO | literal) RPARENCHAR
   | (SEQUENCE | SEQ) (LPARENCHAR literal COMMACHAR literal RPARENCHAR)?
   | (SIZE | SZ) LPARENCHAR (MAX | literal) RPARENCHAR
   | (SOURCE | S_CHAR)
   | SP
   | SPACE LPARENCHAR literal RPARENCHAR
   | SPIE
   | SQL (LPARENCHAR literal RPARENCHAR)?
   | (SQLCCSID | SQLC)
   | (SSRANGE | SSR)
   | SYSEIB
   | (TERMINAL | TERM)
   | TEST (LPARENCHAR (HOOK | NOHOOK)? COMMACHAR? (SEP | SEPARATE | NOSEP | NOSEPARATE)? COMMACHAR? (EJPD | NOEJPD)? RPARENCHAR)?
   | THREAD
   | TRUNC LPARENCHAR (BIN | OPT | STD) RPARENCHAR
   | VBREF
   | (WORD | WD) LPARENCHAR cobolWord RPARENCHAR
   | (XMLPARSE | XP) LPARENCHAR (COMPAT | C_CHAR | XMLSS | X_CHAR) RPARENCHAR
   | (XREF | X_CHAR) (LPARENCHAR (FULL | SHORT)? RPARENCHAR)?
   | (YEARWINDOW | YW) LPARENCHAR literal RPARENCHAR
   | ZWB
   ;

// exec cics statement

execCicsStatement
   : EXEC CICS charData END_EXEC DOT?
   ;

// exec dli statement

execDliStatement
   : EXEC DLI charData END_EXEC DOT?
   ;

// exec sql statement

execSqlStatement
   : EXEC SQL charDataSql END_EXEC DOT?
   ;

execSqlIncludeStatement
   : EXEC SQL INCLUDE (literal | cobolWord | filename) END_EXEC DOT?
   ;

// exec sql ims statement

execSqlImsStatement
   : EXEC SQLIMS charData END_EXEC DOT?
   ;

// copy statement

copyStatement
   : COPY copySource ((directoryPhrase | familyPhrase | replacingPhrase | SUPPRESS))* DOT
   ;

copySource
   : (literal | cobolWord | filename) ((OF | IN) copyLibrary)?
   ;

copyLibrary
   : literal | cobolWord
   ;

replacingPhrase
   : REPLACING replaceClause (replaceClause)*
   ;

// replace statement

replaceArea
   : replaceByStatement (copyStatement | charData)* replaceOffStatement?
   ;

replaceByStatement
   : REPLACE (replaceClause)+ DOT
   ;

replaceOffStatement
   : REPLACE OFF DOT
   ;

replaceClause
   : replaceable BY replacement subscript? (directoryPhrase)* (familyPhrase)?
   ;

directoryPhrase
   : (OF | IN) (literal | cobolWord)
   ;

familyPhrase
   : ON (literal | cobolWord)
   ;

replaceable
   : literal | cobolWord | pseudoText | charDataLineNoDot
   ;

replacement
   : literal | cobolWord | pseudoText | charDataLineNoDot
   ;

// eject statement

ejectStatement
   : EJECT DOT?
   ;

// skip statement

skipStatement
   : (SKIP1 | SKIP2 | SKIP3) DOT?
   ;

// title statement

titleStatement
   : TITLE literal DOT?
   ;

// literal ----------------------------------

pseudoText
   : DOUBLEEQUALCHAR charData? DOUBLEEQUALCHAR
   ;

charData
   : (charDataLine)+
   ;

charDataSql
   : (charDataLine | COPY | REPLACE)+
   ;

charDataLineNoDot
   : (cobolWord | literal | filename | commentEntry | TEXT | LPARENCHAR | RPARENCHAR)+
   ;

charDataLine
   : (cobolWord | literal | filename | commentEntry | TEXT | DOT | LPARENCHAR | RPARENCHAR)+
   ;

subscript
    : LPARENCHAR (literal | cobolWord) (COMMACHAR? (literal | cobolWord))* RPARENCHAR
    ;

cobolWord
   : IDENTIFIER | charDataKeyword
   ;

literal
   : NONNUMERICLITERAL | NUMERICLITERAL | INTEGERLITERAL
   ;

filename
   : FILENAME
   ;

commentEntry
   : COMMENTENTRYLINE+
   ;

// lexer rules --------------------------------------------------------------------------------

// keywords
ADATA : 'ADATA';
ADV : 'ADV';
ALIAS : 'ALIAS';
ANSI : 'ANSI';
ANY : 'ANY';
APOST : 'APOST';
AR : 'AR';
ARITH : 'ARITH';
AUTO : 'AUTO';
AWO : 'AWO';
BIN : 'BIN';
BLOCK0 : 'BLOCK0';
BUF : 'BUF';
BUFSIZE : 'BUFSIZE';
BY : 'BY' ;
CBL : 'CBL';
CBLCARD : 'CBLCARD';
CICS : 'CICS';
CO : 'CO';
COBOL2 : 'COBOL2';
COBOL3 : 'COBOL3';
CODEPAGE : 'CODEPAGE';
COMPAT : 'COMPAT';
COMPILE : 'COMPILE';
COPY : 'COPY';
CP : 'CP';
CPP : 'CPP';
CPSM : 'CPSM';
CS : 'CS';
CURR : 'CURR';
CURRENCY : 'CURRENCY';
DATA : 'DATA';
DATEPROC : 'DATEPROC';
DBCS : 'DBCS';
DD : 'DD';
DEBUG : 'DEBUG';
DECK : 'DECK';
DIAGTRUNC : 'DIAGTRUNC';
DLI : 'DLI';
DLL : 'DLL';
DP : 'DP';
DTR : 'DTR';
DU : 'DU';
DUMP : 'DUMP';
DYN : 'DYN';
DYNAM : 'DYNAM';
EDF : 'EDF';
EJECT : 'EJECT';
EJPD : 'EJPD';
EN : 'EN';
ENGLISH : 'ENGLISH';
END_EXEC : 'END-EXEC';
EPILOG : 'EPILOG';
EXCI : 'EXCI';
EXEC : 'EXEC';
EXIT : 'EXIT';
EXP : 'EXP';
EXPORTALL : 'EXPORTALL';
EXTEND : 'EXTEND';
FASTSRT : 'FASTSRT';
FEPI : 'FEPI';
FLAG : 'FLAG';
FLAGSTD : 'FLAGSTD';
FSRT : 'FSRT';
FULL : 'FULL';
GDS : 'GDS';
GRAPHIC : 'GRAPHIC';
HOOK : 'HOOK';
IN : 'IN';
INCLUDE : 'INCLUDE';
INTDATE : 'INTDATE';
JA : 'JA';
JP : 'JP';
KA : 'KA';
LANG : 'LANG';
LANGUAGE : 'LANGUAGE';
LC : 'LC';
LEASM : 'LEASM';
LENGTH : 'LENGTH';
LIB : 'LIB';
LILIAN : 'LILIAN';
LIN : 'LIN';
LINECOUNT : 'LINECOUNT';
LINKAGE : 'LINKAGE';
LIST : 'LIST';
LM : 'LM';
LONGMIXED : 'LONGMIXED';
LONGUPPER : 'LONGUPPER';
LPARENCHAR : '(';
LU : 'LU';
MAP : 'MAP';
MARGINS : 'MARGINS';
MAX : 'MAX';
MD : 'MD';
MDECK : 'MDECK';
MIG : 'MIG';
MIXED : 'MIXED';
NAME : 'NAME';
NAT : 'NAT';
NATIONAL : 'NATIONAL';
NATLANG : 'NATLANG';
NN : 'NN';
NO : 'NO';
NOADATA : 'NOADATA';
NOADV : 'NOADV';
NOALIAS : 'NOALIAS';
NOAWO : 'NOAWO';
NOBLOCK0 : 'NOBLOCK0';
NOC : 'NOC';
NOCBLCARD : 'NOCBLCARD';
NOCICS : 'NOCICS';
NOCMPR2 : 'NOCMPR2';
NOCOMPILE : 'NOCOMPILE';
NOCPSM : 'NOCPSM';
NOCURR : 'NOCURR';
NOCURRENCY : 'NOCURRENCY';
NOD : 'NOD';
NODATEPROC : 'NODATEPROC';
NODBCS : 'NODBCS';
NODE : 'NODE';
NODEBUG : 'NODEBUG';
NODECK : 'NODECK';
NODIAGTRUNC : 'NODIAGTRUNC';
NODLL : 'NODLL';
NODU : 'NODU';
NODUMP : 'NODUMP';
NODP : 'NODP';
NODTR : 'NODTR';
NODYN : 'NODYN';
NODYNAM : 'NODYNAM';
NOEDF : 'NOEDF';
NOEJPD : 'NOEJPD';
NOEPILOG : 'NOEPILOG';
NOEXIT : 'NOEXIT';
NOEXP : 'NOEXP';
NOEXPORTALL : 'NOEXPORTALL';
NOF : 'NOF';
NOFASTSRT : 'NOFASTSRT';
NOFEPI : 'NOFEPI';
NOFLAG : 'NOFLAG';
NOFLAGMIG : 'NOFLAGMIG';
NOFLAGSTD : 'NOFLAGSTD';
NOFSRT : 'NOFSRT';
NOGRAPHIC : 'NOGRAPHIC';
NOHOOK : 'NOHOOK';
NOLENGTH : 'NOLENGTH';
NOLIB : 'NOLIB';
NOLINKAGE : 'NOLINKAGE';
NOLIST : 'NOLIST';
NOMAP : 'NOMAP';
NOMD : 'NOMD';
NOMDECK : 'NOMDECK';
NONAME : 'NONAME';
NONUM : 'NONUM';
NONUMBER : 'NONUMBER';
NOOBJ : 'NOOBJ';
NOOBJECT : 'NOOBJECT';
NOOFF : 'NOOFF';
NOOFFSET : 'NOOFFSET';
NOOPSEQUENCE : 'NOOPSEQUENCE';
NOOPT : 'NOOPT';
NOOPTIMIZE : 'NOOPTIMIZE';
NOOPTIONS : 'NOOPTIONS';
NOP : 'NOP';
NOPFD : 'NOPFD';
NOPROLOG : 'NOPROLOG';
NORENT : 'NORENT';
NOS : 'NOS';
NOSEP : 'NOSEP';
NOSEPARATE : 'NOSEPARATE';
NOSEQ : 'NOSEQ';
NOSOURCE : 'NOSOURCE';
NOSPIE : 'NOSPIE';
NOSQL : 'NOSQL';
NOSQLC : 'NOSQLC';
NOSQLCCSID : 'NOSQLCCSID';
NOSSR : 'NOSSR';
NOSSRANGE : 'NOSSRANGE';
NOSTDTRUNC : 'NOSTDTRUNC';
NOSEQUENCE : 'NOSEQUENCE';
NOTERM : 'NOTERM';
NOTERMINAL : 'NOTERMINAL';
NOTEST : 'NOTEST';
NOTHREAD : 'NOTHREAD';
NOTRIG : 'NOTRIG';
NOVBREF : 'NOVBREF';
NOWD : 'NOWD';
NOWORD : 'NOWORD';
NOX : 'NOX';
NOXREF : 'NOXREF';
NOZWB : 'NOZWB';
NS : 'NS';
NSEQ : 'NSEQ';
NSYMBOL : 'NSYMBOL';
NUM : 'NUM';
NUMBER : 'NUMBER';
NUMPROC : 'NUMPROC';
OBJ : 'OBJ';
OBJECT : 'OBJECT';
OF : 'OF';
OFF : 'OFF';
OFFSET : 'OFFSET';
ON : 'ON';
OP : 'OP';
OPMARGINS : 'OPMARGINS';
OPSEQUENCE : 'OPSEQUENCE';
OPT : 'OPT';
OPTFILE : 'OPTFILE';
OPTIMIZE : 'OPTIMIZE';
OPTIONS : 'OPTIONS';
OUT : 'OUT';
OUTDD : 'OUTDD';
PFD : 'PFD';
PPTDBG : 'PPTDBG';
PGMN : 'PGMN';
PGMNAME : 'PGMNAME';
PROCESS : 'PROCESS';
PROLOG : 'PROLOG';
QUOTE : 'QUOTE';
RENT : 'RENT';
REPLACE : 'REPLACE';
REPLACING : 'REPLACING';
RMODE : 'RMODE';
RPARENCHAR : ')';
SEP : 'SEP';
SEPARATE : 'SEPARATE';
SEQ : 'SEQ';
SEQUENCE : 'SEQUENCE';
SHORT : 'SHORT';
SIZE : 'SIZE';
SOURCE : 'SOURCE';
SP : 'SP';
SPACE : 'SPACE';
SPIE : 'SPIE';
SQL : 'SQL';
SQLC : 'SQLC';
SQLCCSID : 'SQLCCSID';
SQLIMS : 'SQLIMS';
SKIP1 : 'SKIP1';
SKIP2 : 'SKIP2';
SKIP3 : 'SKIP3';
SS : 'SS';
SSR : 'SSR';
SSRANGE : 'SSRANGE';
STD : 'STD';
SUPPRESS : 'SUPPRESS';
SYSEIB : 'SYSEIB';
SZ : 'SZ';
TERM : 'TERM';
TERMINAL : 'TERMINAL';
TEST : 'TEST';
THREAD : 'THREAD';
TITLE : 'TITLE';
TRIG : 'TRIG';
TRUNC : 'TRUNC';
UE : 'UE';
UPPER : 'UPPER';
VBREF : 'VBREF';
WD : 'WD';
WORD : 'WORD';
XMLPARSE : 'XMLPARSE';
XMLSS : 'XMLSS';
XOPTS : 'XOPTS';
XP : 'XP';
XREF : 'XREF';
YEARWINDOW : 'YEARWINDOW';
YW : 'YW';
ZWB : 'ZWB';


charDataKeyword
   : ADATA | ADV | ALIAS | ANSI | ANY | APOST | AR | ARITH | AUTO | AWO
   | BIN | BLOCK0 | BUF | BUFSIZE | BY
   | CBL | CBLCARD | CO | COBOL2 | COBOL3 | CODEPAGE | COMMACHAR | COMPAT | COMPILE | CP | CPP | CPSM | CS | CURR | CURRENCY
   | DATA | DATEPROC | DBCS | DD | DEBUG | DECK | DIAGTRUNC | DLI | DLL | DP | DTR | DU | DUMP | DYN | DYNAM
   | EDF | EJPD | EN | ENGLISH | EPILOG | EXCI | EXIT | EXP | EXPORTALL | EXTEND
   | FASTSRT | FLAG | FLAGSTD | FULL | FSRT
   | GDS | GRAPHIC
   | HOOK
   | IN | INTDATE
   | JA | JP
   | KA
   | LANG | LANGUAGE | LC | LENGTH | LIB | LILIAN | LIN | LINECOUNT | LINKAGE | LIST | LM | LONGMIXED | LONGUPPER | LU
   | MAP | MARGINS | MAX | MD | MDECK | MIG | MIXED
   | NAME | NAT | NATIONAL | NATLANG
   | NN
   | NO
   | NOADATA | NOADV | NOALIAS | NOAWO
   | NOBLOCK0
   | NOC | NOCBLCARD | NOCICS | NOCMPR2 | NOCOMPILE | NOCPSM | NOCURR | NOCURRENCY
   | NOD | NODATEPROC | NODBCS | NODE | NODEBUG | NODECK | NODIAGTRUNC | NODLL | NODU | NODUMP | NODP | NODTR | NODYN | NODYNAM
   | NOEDF | NOEJPD | NOEPILOG | NOEXIT | NOEXP | NOEXPORTALL
   | NOF | NOFASTSRT | NOFEPI | NOFLAG | NOFLAGMIG | NOFLAGSTD | NOFSRT
   | NOGRAPHIC
   | NOHOOK
   | NOLENGTH | NOLIB | NOLINKAGE | NOLIST
   | NOMAP | NOMD | NOMDECK
   | NONAME | NONUM | NONUMBER
   | NOOBJ | NOOBJECT | NOOFF | NOOFFSET | NOOPSEQUENCE | NOOPT | NOOPTIMIZE | NOOPTIONS
   | NOP | NOPFD | NOPROLOG
   | NORENT
   | NOS | NOSEP | NOSEPARATE | NOSEQ | NOSEQUENCE | NOSOURCE | NOSPIE | NOSQL | NOSQLC | NOSQLCCSID | NOSSR | NOSSRANGE | NOSTDTRUNC
   | NOTERM | NOTERMINAL | NOTEST | NOTHREAD | NOTRIG
   | NOVBREF
   | NOWORD
   | NOX | NOXREF
   | NOZWB
   | NSEQ | NSYMBOL | NS
   | NUM | NUMBER | NUMPROC
   | OBJ | OBJECT | ON | OF | OFF | OFFSET | OPMARGINS | OPSEQUENCE | OPTIMIZE | OP | OPT | OPTFILE | OPTIONS | OUT | OUTDD
   | PFD | PGMN | PGMNAME | PPTDBG | PROCESS | PROLOG
   | QUOTE
   | RENT | REPLACING | RMODE
   | SEQ | SEQUENCE | SEP | SEPARATE | SHORT | SIZE | SOURCE | SP | SPACE | SPIE | SQL | SQLC | SQLCCSID | SS | SSR | SSRANGE | STD | SYSEIB | SZ
   | TERM | TERMINAL | TEST | THREAD | TRIG | TRUNC
   | UE | UPPER
   | VBREF
   | WD
   | XMLPARSE | XMLSS | XOPTS | XREF
   | YEARWINDOW | YW
   | ZWB
   | C_CHAR | D_CHAR | E_CHAR | F_CHAR | H_CHAR | I_CHAR | M_CHAR | N_CHAR | Q_CHAR | S_CHAR | U_CHAR | W_CHAR | X_CHAR
   ;

C_CHAR : 'C';
D_CHAR : 'D';
E_CHAR : 'E';
F_CHAR : 'F';
H_CHAR : 'H';
I_CHAR : 'I';
M_CHAR : 'M';
N_CHAR : 'N';
Q_CHAR : 'Q';
S_CHAR : 'S';
U_CHAR : 'U';
W_CHAR : 'W';
X_CHAR : 'X';


// symbols
COMMENTENTRYTAG : '*>CE';
COMMENTTAG : '*>';
COMMACHAR : ',';
DOT : '.';
DOUBLEEQUALCHAR : '==';

// literals
NONNUMERICLITERAL : STRINGLITERAL | HEXNUMBER;

INTEGERLITERAL : ('-' | '+')? [0-9]+;
NUMERICLITERAL : ('-' | '+')? [0-9]* ('.' | COMMACHAR) [0-9]+ ('E' ('-' | '+')? [0-9]+)?;

fragment HEXNUMBER
    : 'X' '"' [0-9A-F]+ '"'
    | 'X' '\'' [0-9A-F]+ '\''
    ;

fragment STRINGLITERAL
    : '"' (~["\n\r] | '""' | '\'')* '"'
    | '\'' (~['\n\r] | '\'\'' | '"')* '\''
    ;

IDENTIFIER : [A-Z0-9]+ ([-_]+ [A-Z0-9]+)*;
FILENAME : [A-Z0-9]+ '.' [A-Z0-9]+;


// whitespace, line breaks, comments, ...
/* Note:
 * The IBM-ANSI-85 spec defines a whitespace character as ' ', ', ', or `; `.
 * However, client code contains custom whitespace rules that do not match the language spec.
 * The customized whitespace allows for new lines to exist anywhere in the source code.
 *
 * SEPARATOR rule skips over the customized whitespace.
 */
SEPARATOR : (';' | COMMACHAR) (' ' | '\r'? '\n') -> skip;
NEWLINE : '\r'? '\n' -> channel(HIDDEN);
COMMENTENTRYLINE : COMMENTENTRYTAG WS ~('\n' | '\r')*;
COMMENTLINE : COMMENTTAG WS ~('\n' | '\r')* -> channel(HIDDEN);
WS : [ \t\f;]+ -> channel(HIDDEN);
TEXT : ~('\n' | '\r');