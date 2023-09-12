lexer grammar ControlMLexer;

UTF_8_BOM : '\uFEFF' -> skip;

WS : [ \t\f;]+ -> channel(HIDDEN);
EOL : (CR? LF | FORM_FEED) -> channel(HIDDEN);

CM : '^^CM^^' -> skip, pushMode(INSIDE_CM);

CA_START : '^^CA_START^^';
STRINGLITERAL
    : '"' (~["\n\r] | '""' | '\'')* '"'
    | '\'' (~['\n\r] | '\'\'' | '"')* '\''
    ;

TEXT : ~[ \r\n^'"]+ | '^' | '\'' | '"';

fragment LF : '\n';
fragment CR : '\r';
fragment FORM_FEED : '\u000C';

mode INSIDE_CM;
CM_WS : WS -> type(WS), channel(HIDDEN);
CM_EOL : EOL -> type(EOL), channel(HIDDEN), popMode;

CM_CA_START : CA_START -> type(CA_START);

CM_STRINGLITERAL : STRINGLITERAL;
CM_TEXT : TEXT;
