parser grammar IdcamsCardParser;

options { caseInsensitive = true; tokenVocab=IdcamsCardLexer; }

compilationUnit
    : word* EOF
    ;

word
    : TEXT
    | STRINGLITERAL
    ;
