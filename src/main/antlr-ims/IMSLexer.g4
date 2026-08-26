lexer grammar IMSLexer;

UTF_8_BOM : '\uFEFF' -> skip;

WS : [ \t\f]+ -> channel(HIDDEN);
EOL : (CR? LF | FORM_FEED) -> channel(HIDDEN);

// Hidden rather than skipped: an IMS gen line is a continuation only by virtue of column 72 of the
// line above it, and only the line reader has looked there. Skipping these would throw away the one
// thing needed to group words back into statements.
IMS_NAMED : '^^IMS_NAMED^^' -> channel(HIDDEN), pushMode(INSIDE_IMS);
IMS_STATEMENT : '^^IMS^^' -> channel(HIDDEN), pushMode(INSIDE_IMS);
IMS_CONTINUATION : '^^IMS_CONT^^' -> channel(HIDDEN), pushMode(INSIDE_IMS);
COMMENT : '^^COMMENT^^' -> skip, pushMode(INSIDE_COMMENT);
UNKNOWN : '^^UNKNOWN^^' -> skip, pushMode(INSIDE_UNKNOWN);

CA_START : '^^CA_START^^';
STRINGLITERAL : '\'' (~['\n\r] | '\'\'')* '\'';

TEXT : ~[ \r\n^']+ | '^' | '\'';

fragment LF : '\n';
fragment CR : '\r';
fragment FORM_FEED : '\u000C';

mode INSIDE_IMS;
IMS_WS : WS -> type(WS), channel(HIDDEN);
IMS_EOL : EOL -> type(EOL), channel(HIDDEN), popMode;

IMS_CA_START : CA_START -> type(CA_START);

IMS_STRINGLITERAL : STRINGLITERAL;
IMS_TEXT : TEXT;

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
