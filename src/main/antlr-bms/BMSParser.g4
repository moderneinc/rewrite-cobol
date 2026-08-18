parser grammar BMSParser;

options { caseInsensitive = true; tokenVocab=BMSLexer; }

compilationUnit
    : statement* EOF
    ;

statement
    : bms
    | comment
    | unknown
    ;

bms
    : bmsWord
    ;

bmsWord
    : (BMS_TEXT | BMS_STRINGLITERAL) bmsSequenceArea?
    ;

bmsSequenceArea
    : CA_START (BMS_TEXT | BMS_STRINGLITERAL)
    ;

comment
    : commentWord
    ;

commentWord
    : COMMENT_TEXT commentSequenceArea?
    ;

commentSequenceArea
    : CA_START COMMENT_TEXT
    ;

unknown
    : unknownWord
    ;

unknownWord
    : (UNKNOWN_TEXT | UNKNOWN_STRINGLITERAL) unknownSequenceArea?
    | unknownSequenceArea
    ;

unknownSequenceArea
    : CA_START (UNKNOWN_TEXT | UNKNOWN_STRINGLITERAL)
    ;
