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
    : JCL_TEXT jclCommentArea?
    ;

jclCommentArea
    : CA_START jclWord
    ;

jclTrailingComment
    : jclWord JCL_TC_START jclWord* JCL_TC_STOP  jclCommentArea?
    ;

jes2
    : jes2Word
    ;

jes2Word
    : JES2_TEXT jes2CommentArea?
    ;

jes2CommentArea
    : CA_START jes2Word
    ;

jes3
    : jes3Word
    ;

jes3Word
    : JES3_TEXT jes3CommentArea?
    ;

jes3CommentArea
    : CA_START jes3Word
    ;

stream
    : streamWord
    ;

streamWord
    : STREAM_TEXT streamCommentArea?
    ;

streamCommentArea
    : CA_START streamWord
    ;

controlM
    : controlMWord
    ;

controlMWord
    : CM_TEXT controlMCommentArea?
    ;

controlMCommentArea
    : CA_START controlMWord
    ;

comment
    : commentWord
    ;

commentWord
    : COMMENT_TEXT commentCommentArea?
    ;

commentCommentArea
    : CA_START commentWord
    ;

unknown
    : unknownWord
    ;

unknownWord
    : UNKNOWN_TEXT unknownCommentArea?
    ;

unknownCommentArea
    : CA_START unknownWord
    ;
