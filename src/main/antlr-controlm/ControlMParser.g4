parser grammar ControlMParser;

options { caseInsensitive = true; tokenVocab=ControlMLexer; }

compilationUnit
    : statement* EOF
    ;

statement
    : controlM
    ;

controlM
    : controlMWord
    ;

controlMWord
    : (CM_TEXT | CM_STRINGLITERAL) controlMCommentArea?
    ;

controlMCommentArea
    : CA_START (CM_TEXT | CM_STRINGLITERAL)
    ;