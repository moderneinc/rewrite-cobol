lexer grammar UtilityCardLexer;

UTF_8_BOM : '\uFEFF' -> skip;

WS : [ \t\f]+ -> channel(HIDDEN);
EOL : (CR? LF | FORM_FEED) -> channel(HIDDEN);

// A comment card's text never reaches the grammar. Both forms are only comments in column one, and
// the line reader is the only thing that has looked there.
COMMENT_CARD : '^^COMMENT^^' ~[\r\n]* -> channel(HIDDEN);

// Its own token rather than part of a word: it ends a block wherever it is written, and it is
// written both apart from the word before it and against it.
SEMICOLON : ';';

fragment LF : '\n';
fragment CR : '\r';
fragment FORM_FEED : '\u000C';

STRINGLITERAL : '\'' (~['\n\r] | '\'\'')* '\'';

TEXT : ~[ \t\f\r\n';]+;
