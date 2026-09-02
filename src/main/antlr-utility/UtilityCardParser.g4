parser grammar UtilityCardParser;

options { caseInsensitive = true; tokenVocab=UtilityCardLexer; }

compilationUnit
    : word* EOF
    ;

word
    : TEXT
    | STRINGLITERAL
    | SEMICOLON
    ;
