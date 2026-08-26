parser grammar IMSParser;

options { caseInsensitive = true; tokenVocab=IMSLexer; }

compilationUnit
    : statement* EOF
    ;

statement
    : ims
    | comment
    | unknown
    ;

ims
    : imsWord
    ;

imsWord
    : (IMS_TEXT | IMS_STRINGLITERAL) imsSequenceArea?
    ;

imsSequenceArea
    : CA_START (IMS_TEXT | IMS_STRINGLITERAL)
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
