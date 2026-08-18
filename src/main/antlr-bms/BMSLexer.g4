lexer grammar BMSLexer;

UTF_8_BOM : '\uFEFF' -> skip;

WS : [ \t\f]+ -> channel(HIDDEN);
EOL : (CR? LF | FORM_FEED) -> channel(HIDDEN);

// Hidden rather than skipped: a BMS line is a continuation only by virtue of column 72 of the line
// above it, and only the line reader has looked there. Skipping these would throw away the one thing
// needed to group words back into statements.
BMS_NAMED : '^^BMS_NAMED^^' -> channel(HIDDEN), pushMode(INSIDE_BMS);
BMS_STATEMENT : '^^BMS^^' -> channel(HIDDEN), pushMode(INSIDE_BMS);
BMS_CONTINUATION : '^^BMS_CONT^^' -> channel(HIDDEN), pushMode(INSIDE_BMS);
COMMENT : '^^COMMENT^^' -> skip, pushMode(INSIDE_COMMENT);
UNKNOWN : '^^UNKNOWN^^' -> skip, pushMode(INSIDE_UNKNOWN);

CA_START : '^^CA_START^^';
STRINGLITERAL : '\'' (~['\n\r] | '\'\'')* '\'';

TEXT : ~[ \r\n^']+ | '^' | '\'';

fragment LF : '\n';
fragment CR : '\r';
fragment FORM_FEED : '\u000C';

mode INSIDE_BMS;
BMS_WS : WS -> type(WS), channel(HIDDEN);
BMS_EOL : EOL -> type(EOL), channel(HIDDEN), popMode;

BMS_CA_START : CA_START -> type(CA_START);

BMS_STRINGLITERAL : STRINGLITERAL;
BMS_TEXT : TEXT;

mode INSIDE_COMMENT;
COMMENT_EOL : EOL -> type(EOL), channel(HIDDEN), popMode;

COMMENT_CA_START : CA_START -> type(CA_START);

// The whole line as one token. A comment says nothing that is worth breaking into words, and a
// banner of asterisks broken into words would be one statement per word.
COMMENT_TEXT : ~[\r\n^]+ | '^';

mode INSIDE_UNKNOWN;
UNKNOWN_WS : [ \t\f]+ -> channel(HIDDEN);
UNKNOWN_EOL : EOL -> type(EOL), channel(HIDDEN), popMode;

UNKNOWN_CA_START : CA_START -> type(CA_START);

UNKNOWN_STRINGLITERAL : STRINGLITERAL;
UNKNOWN_TEXT : TEXT;
