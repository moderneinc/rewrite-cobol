lexer grammar DB2Lexer;

options { caseInsensitive = true; }

WS : [ \t\f\r\n]+ -> channel(HIDDEN);
LINE_COMMENT : '--' ~[\r\n]* -> channel(HIDDEN);
BLOCK_COMMENT : '/*' .*? '*/' -> channel(HIDDEN);

// Only the words the modelled statements need. Everything else — a data type name, an option, a
// privilege — stays an IDENTIFIER, so that TABLESPACE does not read as TABLE and a column called
// DATA still parses.
CREATE : 'CREATE';
TABLE : 'TABLE';
INDEX : 'INDEX';
UNIQUE : 'UNIQUE';
ALTER : 'ALTER';
ADD : 'ADD';
ON : 'ON';
IN : 'IN';
PRIMARY : 'PRIMARY';
FOREIGN : 'FOREIGN';
KEY : 'KEY';
REFERENCES : 'REFERENCES';
CONSTRAINT : 'CONSTRAINT';
CHECK : 'CHECK';
NOT : 'NOT';
NULL : 'NULL';
WHERE : 'WHERE';
ASC : 'ASC';
DESC : 'DESC';
RANDOM : 'RANDOM';

LPAREN : '(';
RPAREN : ')';
COMMA : ',';
SEMI : ';';
DOT : '.';

STRING : '\'' (~['\r\n] | '\'\'')* '\'';
DELIMITED_IDENTIFIER : '"' (~["\r\n] | '""')* '"';

// The DDL on disk is a template: genapp writes <DB2DBID>.customer and Bank-of-Z {{ db2.sqlid }}.
// Lexing a placeholder as a name is what keeps the CREATE TABLE around it readable. Bounded to a
// bare word so that `x < 5` is still two tokens.
PLACEHOLDER : '<' [A-Z0-9_.]+ '>' | '{{' ~[}\r\n]* '}}';

NUMBER : [0-9]+ ('.' [0-9]+)?;
IDENTIFIER : [A-Z_@#$] [A-Z0-9_@#$]*;

// Nothing may fail to lex: what this grammar does not model, it still has to carry.
OTHER : .;
