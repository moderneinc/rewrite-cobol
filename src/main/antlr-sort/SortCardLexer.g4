lexer grammar SortCardLexer;

UTF_8_BOM : '\uFEFF' -> skip;

WS : [ \t\f]+ -> channel(HIDDEN);
EOL : (CR? LF | FORM_FEED) -> channel(HIDDEN);

// Hidden rather than skipped: a card continues the statement above it only because that line ended
// in a comma, and only the line reader has looked there. Skipping these would throw away the one
// thing needed to group words back into statements.
CARD : '^^CARD^^' -> channel(HIDDEN), pushMode(INSIDE_CARD);
CARD_CONTINUATION : '^^CARD_CONT^^' -> channel(HIDDEN), pushMode(INSIDE_CARD);

// An asterisk in column 1 makes the whole card a comment, so its text never reaches the grammar.
COMMENT_CARD : '^^COMMENT^^' ~[\r\n]* -> channel(HIDDEN);

fragment LF : '\n';
fragment CR : '\r';
fragment FORM_FEED : '\u000C';

mode INSIDE_CARD;
INSIDE_WS : WS -> type(WS), channel(HIDDEN);
INSIDE_EOL : EOL -> type(EOL), channel(HIDDEN), popMode;

STRINGLITERAL : '\'' (~['\n\r] | '\'\'')* '\'';

TEXT : ~[ \t\f\r\n']+;
