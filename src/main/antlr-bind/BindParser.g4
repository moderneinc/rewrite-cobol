parser grammar BindParser;

options { caseInsensitive = true; tokenVocab=BindLexer; }

compilationUnit
    : word* EOF
    ;

word
    : TEXT
    | STRINGLITERAL
    ;
