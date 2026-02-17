lexer grammar JCLLexer;

UTF_8_BOM : '\uFEFF' -> skip;

WS : [ \t\f;]+ -> channel(HIDDEN);
EOL : (CR? LF | FORM_FEED) -> channel(HIDDEN);

JCL_STATEMENT : ('^^JCL_STATEMENT^^' | '^^JCL_CONT^^' | '^^JCL^^' | '^^STREAM_END^^') -> skip, pushMode(INSIDE_JCL);
JCL_STREAM : '^^STREAM^^' -> skip, pushMode(INSIDE_STREAM);
JES2 : ('^^JES2^^' | '^^JES2_CONT^^') -> skip, pushMode(INSIDE_JES2);
JES3 : ('^^JES3^^' | '^^JES3_CONT^^') -> skip, pushMode(INSIDE_JES3);
CM : '^^CM^^' -> skip, pushMode(INSIDE_CM);
COMMENT : '^^COMMENT^^' -> skip, pushMode(INSIDE_COMMENT);
UNKNOWN : '^^UNKNOWN^^' -> skip, pushMode(INSIDE_UNKNOWN);

CA_START : '^^CA_START^^';
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