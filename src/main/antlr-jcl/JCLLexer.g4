lexer grammar JCLLexer;

UTF_8_BOM : '\uFEFF' -> skip;

WS : [ \t\f;]+ -> channel(HIDDEN);
EOL : (CR? LF | FORM_FEED) -> channel(HIDDEN);

// Hidden rather than skipped: the line reader has already worked out which lines begin a statement
// and which continue the one before, and that is the only place that information exists. Skipping it
// threw away the one thing needed to group these words back into statements.
JCL_STATEMENT : ('^^JCL_STATEMENT^^' | '^^JCL^^') -> channel(HIDDEN), pushMode(INSIDE_JCL);
JCL_CONTINUATION : '^^JCL_CONT^^' -> channel(HIDDEN), pushMode(INSIDE_JCL);
// Its own type so the delimiter statement ending in-stream data can be told from a statement.
JCL_STREAM_END : '^^STREAM_END^^' -> channel(HIDDEN), pushMode(INSIDE_JCL);
JCL_STREAM : '^^STREAM^^' -> skip, pushMode(INSIDE_STREAM);
JES2 : ('^^JES2^^' | '^^JES2_CONT^^') -> skip, pushMode(INSIDE_JES2);
JES3 : ('^^JES3^^' | '^^JES3_CONT^^') -> skip, pushMode(INSIDE_JES3);
CM : '^^CM^^' -> skip, pushMode(INSIDE_CM);
COMMENT : '^^COMMENT^^' -> skip, pushMode(INSIDE_COMMENT);
UNKNOWN : '^^UNKNOWN^^' -> skip, pushMode(INSIDE_UNKNOWN);

// The whole of columns 73-80 in one token. A sequence field holding a quote — `WAIT=30,F=WRAP')` —
// otherwise lexes into several, and only one of them was kept.
CA_START : '^^CA_START^^' ~[\r\n]*;
STRINGLITERAL
    : '"' (~["\n\r] | '""' | '\'')* '"'
    | '\'' (~['\n\r] | '\'\'' | '"')* '\''
    ;

TEXT : ~[ \r\n^'"]+ | '^' | '\'' | '"';

fragment LF : '\n';
fragment CR : '\r';
fragment FORM_FEED : '\u000C';

mode INSIDE_JCL;
JCL_WS : WS -> type(WS), channel(HIDDEN);
JCL_EOL : EOL -> type(EOL), channel(HIDDEN), popMode;

JCL_TC_START : '^^TC_START^^';
JCL_TC_STOP : '^^TC_STOP^^';
JCL_CA_START : CA_START -> type(CA_START);

JCL_STRINGLITERAL : STRINGLITERAL;
JCL_TEXT : TEXT;

mode INSIDE_STREAM;
STREAM_WS : [ \t\f]+ -> channel(HIDDEN);
STREAM_EOL : EOL -> type(EOL), channel(HIDDEN), popMode;

STREAM_CA_START : CA_START -> type(CA_START);

STREAM_STRINGLITERAL : STRINGLITERAL;
STREAM_TEXT : TEXT;

mode INSIDE_JES2;
JES2_WS : WS -> type(WS), channel(HIDDEN);
JES2_EOL : EOL -> type(EOL), channel(HIDDEN), popMode;

JES2_CA_START : CA_START -> type(CA_START);

JES2_STRINGLITERAL : STRINGLITERAL;
JES2_TEXT : TEXT;

mode INSIDE_JES3;
JES3_WS : WS -> type(WS), channel(HIDDEN);
JES3_EOL : EOL -> type(EOL), channel(HIDDEN), popMode;

JES3_CA_START : CA_START -> type(CA_START);

JES3_STRINGLITERAL : STRINGLITERAL;
JES3_TEXT : TEXT;

mode INSIDE_CM;
CM_WS : WS -> type(WS), channel(HIDDEN);
CM_EOL : EOL -> type(EOL), channel(HIDDEN), popMode;

CM_CA_START : CA_START -> type(CA_START);

CM_STRINGLITERAL : STRINGLITERAL;
CM_TEXT : TEXT;

mode INSIDE_COMMENT;
// JCL whitespace rules do not apply.
COMMENT_WS : [ \t\f]+ -> channel(HIDDEN);
COMMENT_EOL : EOL -> type(EOL), channel(HIDDEN), popMode;

COMMENT_CA_START : CA_START -> type(CA_START);

COMMENT_STRINGLITERAL : STRINGLITERAL;
COMMENT_TEXT : TEXT;

mode INSIDE_UNKNOWN;
// JCL whitespace rules do not apply.
UNKNOWN_WS : [ \t\f]+ -> channel(HIDDEN);
UNKNOWN_EOL : EOL -> type(EOL), channel(HIDDEN), popMode;

UNKNOWN_CA_START : CA_START -> type(CA_START);

UNKNOWN_STRINGLITERAL : STRINGLITERAL;
UNKNOWN_TEXT : TEXT;