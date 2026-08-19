parser grammar JCLParser;

options { caseInsensitive = true; tokenVocab=JCLLexer; }

compilationUnit
    : statement* EOF
    ;

statement
    : jcl
    | jes2
    | jes3
    | stream
    | controlM
    | comment
    | unknown
    ;

jcl
    : jclWord
    | jclTrailingComment
    ;

jclWord
    : (JCL_TEXT | JCL_STRINGLITERAL) commentArea?
    ;

jclTrailingComment
    : jclWord JCL_TC_START jclWord* JCL_TC_STOP  commentArea?
    ;

jes2
    : jes2Word
    ;

jes2Word
    : (JES2_TEXT | JES2_STRINGLITERAL) commentArea?
    ;

jes3
    : jes3Word
    ;

jes3Word
    : (JES3_TEXT | JES3_STRINGLITERAL) commentArea?
    ;

stream
    : streamWord
    ;

streamWord
    : (STREAM_TEXT | STREAM_STRINGLITERAL) commentArea?
    ;

controlM
    : controlMWord
    ;

controlMWord
    : (CM_TEXT | CM_STRINGLITERAL) commentArea?
    ;

comment
    : commentWord
    ;

commentWord
    : (COMMENT_TEXT | COMMENT_STRINGLITERAL) commentArea?
    ;

unknown
    : unknownWord
    ;

unknownWord
    : (UNKNOWN_TEXT | UNKNOWN_STRINGLITERAL) commentArea?
    | commentArea
    ;

// Columns 73-80, the sequence field. One token however many words are written there: the line ending
// that bounds it is on the hidden channel, so a parser repetition would run straight through it into
// the next line.
commentArea
    : CA_START
    ;
