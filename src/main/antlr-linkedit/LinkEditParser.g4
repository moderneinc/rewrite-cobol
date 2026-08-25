parser grammar LinkEditParser;

options { caseInsensitive = true; tokenVocab=LinkEditLexer; }

compilationUnit
    : word* EOF
    ;

word
    : TEXT
    | STRINGLITERAL
    ;
