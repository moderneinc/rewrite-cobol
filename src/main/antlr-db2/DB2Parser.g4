parser grammar DB2Parser;

options { caseInsensitive = true; tokenVocab=DB2Lexer; }

// An island grammar. Only the statements that feed the relationship graph — the tables, their
// columns, the indexes over them and the foreign keys between them — are read. A GRANT, a COMMIT,
// a CREATE TABLESPACE or anything else is water: consumed to its terminating semicolon and kept
// verbatim, so a file still prints back byte for byte without DB2's whole DDL being modelled.

compilationUnit
    : statement* EOF
    ;

// Islands first, water last, and see `unknownStart` for why water cannot take an island's place.
statement
    : createTable
    | createIndex
    | alterTable
    | unknownStatement
    ;

createTable
    : CREATE TABLE qualifiedName
      LPAREN tableElement (COMMA tableElement)* RPAREN
      tableOption* SEMI?
    ;

tableElement
    : tableConstraint
    | columnDefinition
    ;

columnDefinition
    : identifier dataType columnAttribute*
    ;

// One word, so that `col INTEGER GENERATED ALWAYS AS IDENTITY` does not read GENERATED as half the
// type. A two word type — DOUBLE PRECISION, LONG VARCHAR — leaves its second word among the
// attributes, where it still prints but is not part of the name.
dataType
    : identifier (LPAREN NUMBER (COMMA NUMBER)? RPAREN)?
    ;

columnAttribute
    : NOT NULL
    | NULL
    | elementWater
    ;

tableConstraint
    : (CONSTRAINT identifier)? constraintBody
    ;

constraintBody
    : PRIMARY KEY columnList constraintOption*
    | FOREIGN KEY identifier? columnList REFERENCES qualifiedName columnList? constraintOption*
    | UNIQUE columnList constraintOption*
    | CHECK parenGroup constraintOption*
    ;

constraintOption
    : elementWater
    ;

columnList
    : LPAREN identifier (COMMA identifier)* RPAREN
    ;

// The tablespace a table is created in. Typed because it is the one table option that names
// something else in the estate; the rest are storage attributes nothing joins on.
tableOption
    : IN qualifiedName
    | water
    ;

createIndex
    : CREATE indexModifier* INDEX qualifiedName ON qualifiedName
      LPAREN indexKey (COMMA indexKey)* RPAREN
      indexOption* SEMI?
    ;

// CREATE UNIQUE WHERE NOT NULL INDEX is one of these, not four.
indexModifier
    : UNIQUE | WHERE | NOT | NULL
    ;

indexKey
    : identifier (ASC | DESC | RANDOM)?
    ;

indexOption
    : water
    ;

alterTable
    : ALTER TABLE qualifiedName alterAction+ SEMI?
    ;

alterAction
    : ADD? tableConstraint
    | water
    ;

unknownStatement
    : unknownStart water* SEMI?
    | SEMI
    ;

// Water may not begin with the words that open an island, so a CREATE TABLE this grammar cannot
// parse is a syntax error rather than an Unknown nobody notices.
unknownStart
    : CREATE ~(TABLE | INDEX | UNIQUE | SEMI)
    | ALTER ~(TABLE | SEMI)
    | water
    ;

qualifiedName
    : identifier (DOT identifier)*
    ;

identifier
    : IDENTIFIER | DELIMITED_IDENTIFIER | PLACEHOLDER
    ;

// Water inside a table body ends at the comma opening the next element or the paren closing the
// body; water between statements ends at the semicolon. Both stop short of CREATE and ALTER, so a
// statement written without its semicolon cannot swallow the one after it.
water
    : ~(SEMI | LPAREN | RPAREN | CREATE | ALTER)
    | parenGroup
    ;

elementWater
    : ~(COMMA | SEMI | LPAREN | RPAREN | CREATE | ALTER)
    | parenGroup
    ;

parenGroup
    : LPAREN (~(LPAREN | RPAREN) | parenGroup)* RPAREN
    ;
