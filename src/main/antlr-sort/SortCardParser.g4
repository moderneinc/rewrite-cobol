parser grammar SortCardParser;

options { caseInsensitive = true; tokenVocab=SortCardLexer; }

compilationUnit
    : word* EOF
    ;

word
    : TEXT
    | STRINGLITERAL
    ;
