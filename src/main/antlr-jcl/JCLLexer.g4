lexer grammar JCLLexer;

UTF_8_BOM : '\uFEFF' -> skip;

WS : [ \t\f;]+ -> channel(HIDDEN);
NEWLINE : EOL -> channel(HIDDEN);

CONTINUATION : ',' NEWLINE '//' -> channel(HIDDEN);

// statement identifiers
JCL_STATEMENT : '//' ~[*] NAME_FIELD?;

// JES3 and Comments.
UNSUPPORTED : '//*' -> pushMode(INSIDE_UNSUPPORTED);

JES2 : '/*' -> pushMode(INSIDE_JES2);


LF : '\n';
CR : '\r';
CRLF : CR LF;
FORM_FEED : '\u000C';
EOL : LF | CR | CRLF | FORM_FEED;

//OPERATION_FIELD
//    : // TODO add operation keywords.
//    ;

// keywords
CNTL : 'CNTL';
DATASET : 'DATASET';
DD : 'DD';
ELSE : 'ELSE';
ENDCNTL : 'ENDCNTL';
ENDDATASET : 'ENDDATASET';
ENDIF : 'ENDIF';
ENDPROCESS : 'ENDPROCESS';
EXEC : 'EXEC';
EXPORT : 'EXPORT';
FORMAT : 'FORMAT';
IF : 'IF';
INCLUDE : 'INCLUDE';
JCLLIB : 'JCLLIB';
JOB : 'JOB';
JOBPARM : 'JOBPARM';
MAIN : 'MAIN';
MESSAGE : 'MESSAGE';
NET : 'NET';
NETACCT : 'NETACCT';
NOTIFY : 'NOTIFY';
OPERATOR : 'OPERATOR';
OUTPUT : 'OUTPUT';
PAUSE : 'PAUSE';
PEND : 'PEND';
PRIORITY : 'PRIORITY';
PROC : 'PROC';
PROCESS : 'PROCESS';
ROUTE : 'ROUTE';
SCHEDULE : 'SCHEDULE';
SET : 'SET';
SETUP : 'SETUP';
SIGNOFF : 'SIGNOFF';
SIGNON : 'SIGNON';
THEN : 'THEN';
XEQ : 'XEQ';
XMIT : 'XMIT';


/* Info on parameter field ...
Parameter field
The parameter field consists of two types of parameters: positional parameters and keyword
parameters. All positional parameters must precede all keyword parameters. Keyword parameters follow
the positional parameters.
Commas: Use commas to separate positional parameters, keyword parameters, and subparameters in
the parameter field.
Positional Parameters: A positional parameter consists of:
• Characters that appear in uppercase in the syntax and must be coded as shown
• Variable information, or
• A combination.
For example, DATA on a DD statement, programmer's-name on a JOB statement, and PGM=programname
on an EXEC statement.
Code positional parameters first in the parameter field in the order shown in the syntax. If you omit a
positional parameter and code a following positional parameter, code a comma to indicate the omitted
parameter. Do not code the replacing comma if:
• The omitted positional parameter is the last positional parameter.
• All following positional parameters are also omitted.
• Only keyword parameters follow.
• All positional parameters are omitted.
Keyword parameters: A keyword consists of characters that appear in uppercase in the syntax and must
be coded as shown followed by an equals sign followed by either characters that must be coded as shown
or variable information. For example, RD=R and MSGCLASS=class-name on the JOB statement.
Code any of the keyword parameters for a statement in any order in the parameter field after the
positional parameters. Because of this positional independence, never code a comma to indicate the
absence of a keyword parameter.
Multiple subparameters: A positional parameter or the variable information in a keyword parameter
sometimes consists of more than one item, called a subparameter list. A subparameter list can consist of
both positional and keyword subparameters. These subparameters follow the same rules as positional
and keyword parameters.
When a parameter contains more than one subparameter, separate the subparameters by commas and
enclose the subparameter list in parentheses or, if indicated in the syntax, by apostrophes. If the list is a
single keyword subparameter or a single positional subparameter with no omitted preceding
subparameters, omit the parentheses or apostrophes.
Null positional subparameters: You are allowed to specify null (that is, omitted) positional subparameters
except where the Syntax section of a particular parameter states otherwise. (For example, null positional
subparameters are not allowed on a COND parameter of an EXEC statement or on an AMP parameter of a
DD statement.) You specify a null positi
*/
PARAMETER
    : ACCODE | ACCT | ADDRESS | ADDRSPC | AFF | AMP | AVGREC
    | BLKSIZE | BLKSZLIM | BUFND | BUFNI | BUFNO | BUFSP | BUILDING | BURST | BYTES
    | CCSID | CHARS | CHKPT | CKPTLINE | CKPTPAGE | CKPTSEC | CLASS | CNTL
    | COLORMAP | COMMAND | COMPACT | COMSETUP | COND | CONTROL | COPIES | CROPS
    | DATA | DATACK | DATACLAS | DCB | DDNAME | DEFAULT | DEN | DEPT | DEST | DISP | DLM
    | DPAGELBL | DSN | DSNTYPE | DSORG | DUMMY | DUPLEX | DYNAMNBR
    | ENDCNTL | EXEC | EXPDT
    | FCB | FILEDATA | FLASH | FORMDEF | FORMLEN | FORMS | FREE
    | GROUP | GROUPID
    | HOLD
    | IF | THEN | ELSE | ENDIF
    | INDEX
    | JCLLIB | JESDS | JOB | JOBCAT | JOBLIB
    | KEYOFF
    | LABEL | LGSTREAM | LIKE | LINDEX | LINECT | LINES | LRECL
    | MEMLIMIT | MGMTCLAS | MODIFY | MSGCLASS | MSGLEVEL
    | NAME | NOTIFY | NULLFILE
    | OFFSET | OPTCD | OUTBIN | OUTDISP | OUTLIM | OUTPUT | OVERLAY | OVFL
    | PAGEDEF | PAGES | PARM | PASSWORD | PATH | PATHDISP | PATHMODE | PATHOPTS | PEND
    | PERFORM | PGM | PIMSG | PRMODE | PROC | PROTECT | PRTERROR | PRTNO | PRTOPTNS | PRTQUEUE | PRTSP | PRTY
    | QNAME
    | RD | RECFM | RECORG | REF | REFDD | REGION | RESFMT | RESTART | RETAIN | RETRY | RETPD | RLS | ROOM
    | SCHENV | SECLABEL | SECMODEL | SEGMENT | SER | SORTCKPT | SPIN | SPACE | STEPCAT | STEPLIB
    | STORCLAS | STRNO | SUBSYS | SYNAD | SYMNAMES | SYSABEND | SYSAREA | SYSCHK | SYSCKEOV | SYSIN
    | SYSMDUMP | SYSOUT | SYSUDUMP
    | TERM | THRESHLD | TIME | TITLE | TRC | TRTCH | TYPRUN
    | UNIT | USER | USERDATA | USERLIB
    | VIO | VOL
    | WRITER
    | XMIT
    | ASTERISK
    ;

PARAMETER_LITERAL
    : SINGLEQUOTE .*? SINGLEQUOTE
    | SINGLEQUOTEFANCY .*? SINGLEQUOTEFANCY
    ;

// parameter names
ACCODE : 'ACCODE';
ACCT : 'ACCT';
ADDRESS : 'ADDRESS';
ADDRSPC : 'ADDRSPC';
AFF : 'AFF';
AMP : 'AMP';
AVGREC : 'AVGREC';
BLKSIZE : 'BLKSIZE';
BLKSZLIM : 'BLKSZLIM';
BUFND : 'BUFND';
BUFNI : 'BUFNI';
BUFNO : 'BUFNO';
BUFSP : 'BUFSP';
BUILDING : 'BUILDING';
BURST : 'BURST';
BYTES : 'BYTES';
CCSID : 'CCSID';
CHARS : 'CHARS';
CHKPT : 'CHKPT';
CKPTLINE : 'CKPTLINE';
CKPTPAGE : 'CKPTPAGE';
CKPTSEC : 'CKPTSEC';
CLASS :'CLASS';
COLORMAP : 'COLORMAP';
COMMAND : 'COMMAND';
COMPACT : 'COMPACT';
COMSETUP : 'COMSETUP';
COND : 'COND';
CONTROL : 'CONTROL';
COPIES : 'COPIES';
CROPS : 'CROPS';
DATA : 'DATA';
DATACK : 'DATACK';
DATACLAS : 'DATACLAS';
DCB : 'DCB';
DDNAME : 'DDNAME';
DEFAULT : 'DEFAULT';
DEN : 'DEN';
DEPT : 'DEPT';
DEST : 'DEST';
DISP : 'DISP';
DLM : 'DLM';
DPAGELBL : 'DPAGELBL';
DSN : 'DSN';
DSNTYPE : 'DSNTYPE';
DSORG : 'DSORG';
DUMMY : 'DUMMY';
DUPLEX : 'DUPLEX';
DYNAMNBR : 'DYNAMNBR';
EXPDT : 'EXPDT';
FCB : 'FCB';
FILEDATA : 'FILEDATA';
FLASH : 'FLASH';
FORMDEF : 'FORMDEF';
FORMLEN : 'FORMLEN';
FORMS : 'FORMS';
FREE : 'FREE';
GROUP : 'GROUP';
GROUPID : 'GROUPID';
HOLD : 'HOLD';
INDEX : 'INDEX';
JESDS : 'JESDS';
JOBCAT : 'JOBCAT';
JOBLIB : 'JOBLIB';
KEYOFF : 'KEYOFF';
LABEL : 'LABEL';
LGSTREAM : 'LGSTREAM';
LIKE : 'LIKE';
LINDEX : 'LINDEX';
LINECT : 'LINECT';
LINES : 'LINES';
LRECL : 'LRECL';
MEMLIMIT : 'MEMLIMIT';
MGMTCLAS : 'MGMTCLAS';
MODIFY : 'MODIFY';
MSGCLASS : 'MSGCLASS';
MSGLEVEL : 'MSGLEVEL';
NAME : 'NAME';
NULLFILE : 'NULLFILE';
OFFSET : 'OFFSET';
OPTCD : 'OPTCD';
OUTBIN : 'OUTBIN';
OUTDISP : 'OUTDISP';
OUTLIM : 'OUTLIM';
OVERLAY : 'OVERLAY';
OVFL : 'OVFL';
PAGEDEF : 'PAGEDEF';
PAGES : 'PAGES';
PARM : 'PARM';
PASSWORD : 'PASSWORD';
PATH : 'PATH';
PATHDISP : 'PATHDISP';
PATHMODE : 'PATHMODE';
PATHOPTS : 'PATHOPTS';
PERFORM : 'PERFORM';
PGM : 'PGM';
PIMSG : 'PIMSG';
PRMODE : 'PRMODE';
PROTECT : 'PROTECT';
PRTERROR : 'PRTERROR';
PRTNO : 'PRTNO';
PRTOPTNS : 'PRTOPTNS';
PRTQUEUE : 'PRTQUEUE';
PRTSP : 'PRTSP';
PRTY : 'PRTY';
QNAME : 'QNAME';
RD : 'RD';
RECFM : 'RECFM';
RECORG : 'RECORG';
REF : 'REF';
REFDD : 'REFDD';
REGION : 'REGION';
RESFMT : 'RESFMT';
RESTART : 'RESTART';
RETAIN : 'RETAIN';
RETRY : 'RETRY';
RETPD : 'RETPDD';
RLS : 'RLS';
ROOM : 'ROOM';
SCHENV : 'SCHENV';
SECLABEL : 'SECLABEL';
SECMODEL :'SECMODEL';
SEGMENT : 'SEGMENT';
SER : 'SER';
SORTCKPT : 'SORTCKPT';
SPIN : 'SPIN';
SPACE : 'SPACE';
STEPCAT : 'STEPCAT';
STEPLIB : 'STEPLIB';
STORCLAS : 'STORCLAS';
STRNO : 'STRNO';
SUBSYS : 'SUBSYS';
SYNAD : 'SYNAD';
SYMNAMES : 'SYMNAMES';
SYSABEND : 'SYSABEND';
SYSAREA : 'SYSAREA';
SYSCHK : 'SYSCHK';
SYSCKEOV : 'SYSCKEOV';
SYSIN : 'SYSIN';
SYSMDUMP : 'SYSMDUMP';
SYSOUT : 'SYSOUT';
SYSUDUMP : 'SYSUDUMP';
TERM : 'TERM';
THRESHLD : 'THRESHLD';
TIME : 'TIME';
TITLE : 'TITLE';
TRC : 'TRC';
TRTCH : 'TRTCH';
TYPRUN : 'TYPRUN';
UNIT : 'UNIT';
USER : 'USER';
USERDATA : 'USERDATA';
USERLIB : 'USERLIB';
VIO : 'VIO';
VOL : 'VOL';
WRITER :'WRITER';

// symbols
EQUAL : '=';
L_BRACE : '{';
R_BRACE : '}';

L_BRACKET : '[';
R_BRACKET : ']';

L_PAREN : '(';
R_PAREN : ')';

AMPERSAND : '&';
ASTERISK : '*';
PLUS : '+';
MINUS : '-';

SINGLEQUOTE : '\'';
SINGLEQUOTEFANCY : '’';
DOUBLEQUOTE : '"';

// names
NAME_FIELD : NAME_CHAR ((PERIOD NAME_CHAR)+)?;
PERIOD : '.';
COMMA : ',';
NAME_CHAR : ([a-zA-Z0-9$#@]+ | ASTERISK);

// add lexer rule for names
// JOB accounting
   //information
   //Hyphens (-) //JOBA JOB D58-D04
// JOB programmer's-name Hyphens (-), leading periods, or
   //embedded periods. Note that a trailing
   //period requires enclosing apostrophes.
   // //JOBB JOB ,S-M-TU
   // //JOBC JOB ,.ABC
   // //JOBD JOB ,P.F.M
   // //JOBE JOB ,'A.B.C.'

//EXEC ACCT Hyphens (-) or plus zero (+0, an
  //overpunch)
  // //S1 EXEC PGM=A,ACCT=D58-LOC
  // //S2 EXEC PGM=B,ACCT=D04+0

//DD DSNAME Hyphens (-) DSNAME=A-B-C

// Table 12. Special Characters that Do Not Require Enclosing Apostrophes (continued)
   //Statement and
   //parameter or
   //subparameter
   //Special characters not needing
   //enclosing apostrophes
   //Examples
   //Periods to indicate a qualified data set
   //name
   //DSNAME=A.B.C
   //Double ampersands to identify a
   //temporary data set name, and to
   //identify an in-stream or sysout data set
   //name
   //DSNAME=&&TEMPDS
   //DSNAME=&&PAYOUT
   //Parentheses to enclose the member
   //name of a partitioned data set (PDS) or
   //partitioned data set extended (PDSE) or
   //the generation number of a generation
   //data set
   //DSNAME=PDS1(MEMA)
   //DSNAME=ISDS(PRIME)
   //DSNAME=GDS(+1)
   //Plus (+) or minus (-) sign to identify a
   //generation of a generation data group
   //DSNAME=GDS(-2)
   //DD VOLUME=SER Hyphens (-) VOLUME=SER=PUB-RD
   //DD UNIT device-type Hyphens (-) UNIT=SYSDA


// Note: The system recognizes the following hexadecimal representations of the U.S. National characters;
//@ as X'7C'; $ as X'5B'; and # as X'7B'. In countries other than the U.S., the U.S. National characters
//represented on terminal keyboards might generate a different hexadecimal representation and cause an
//error. For example, in some countries the $ character may generate a X'4A'.

mode INSIDE_JES2;
JES2_NEWLINE : NEWLINE -> type(NEWLINE), channel(HIDDEN), popMode;

JES2_TEXT : ~[\r\n]+;

mode INSIDE_UNSUPPORTED;
UNSUPPORTED_NEWLINE : NEWLINE -> type(NEWLINE), channel(HIDDEN), popMode;

UNSUPPORTED_TEXT : ~[\r\n]+;
