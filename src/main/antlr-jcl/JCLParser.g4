/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
parser grammar JCLParser;

options { caseInsensitive = true; tokenVocab=JCLLexer; }

compilationUnit
    : statement* EOF
    ;

statement
    : jclStatement
    | jes2
    | jes3
    | controlM
    | comment
    | unknown
    ;

jclStatement
    : jobStatement
    | jclLibStatement
    | ddStatement
    | ddStreamStatement
    | execStatement
    | outputStatement
    | pendStatement
    | procStatement
    | setStatement
    | xmitStatement
    ;

jobStatement
    : JCL_DOUBLE_SLASH jclName? jobName (JCL_COMMA_CHAR? parameter)*
    ;

jobName
    : JCL_JOB jclCommentArea?
    ;

jclLibStatement
    : JCL_DOUBLE_SLASH jclName? jclLibName (JCL_COMMA_CHAR? parameter)*
    ;

jclLibName
    : JCL_JCLLIB jclCommentArea?
    ;

ddStatement
    : JCL_DOUBLE_SLASH jclName? ddName (JCL_COMMA_CHAR? parameter)* jclTrailingComment?
    ;

ddStreamStatement
    : JCL_DOUBLE_SLASH jclName? ddName parameter (STREAM_COMMA_CHAR? streamParameter)* jclTrailingComment? ddStreamEnd?
    ;

ddName
    : JCL_DD jclCommentArea?
    ;

ddStreamEnd
    : STREAM_END_TOKEN streamJclCommentArea?
    ;

streamParameter
    : streamName
    | streamParameterAssignment
    | streamParameterParentheses
    ;

streamParameterAssignment
    : streamJclName STREAM_EQUAL_CHAR streamParameter
    ;

streamParameterParentheses
    : STREAM_L_PAREN_CHAR (STREAM_COMMA_CHAR? streamParameter)* STREAM_R_PAREN_CHAR  streamJclCommentArea?
    ;

streamName
    : streamJclWord streamParameterParentheses?
    ;

streamJclWord
    : JCL_CONT? (STREAM_STRINGLITERAL | streamJclName)  streamJclCommentArea?
    ;

streamJclName
    : JCL_CONT? (STREAM_PARAMETER | STREAM_NAME_FIELD | streamJclKeyword | streamCharacter)  streamJclCommentArea?
    ;

streamJclKeyword
    : STREAM_CNTL
    | STREAM_DATASET | STREAM_DD
    | STREAM_ELSE | STREAM_ENDCNTL | STREAM_ENDDATASET | STREAM_ENDIF | STREAM_ENDPROCESS | STREAM_EXEC | STREAM_EXPORT
    | STREAM_FORMAT
    | STREAM_IF | STREAM_INCLUDE
    | STREAM_JCLLIB | STREAM_JOB | STREAM_JOBPARM
    | STREAM_MAIN | STREAM_MESSAGE
    | STREAM_NET | STREAM_NETACCT | STREAM_NOTIFY
    | STREAM_OPERATOR | STREAM_OUTPUT
    | STREAM_PAUSE | STREAM_PEND | STREAM_PRIORITY | STREAM_PROC | STREAM_PROCESS
    | STREAM_ROUTE
    | STREAM_SCHEDULE | STREAM_SET | STREAM_SETUP | STREAM_SIGNOFF | STREAM_SIGNON
    | STREAM_THEN
    | STREAM_XEQ | STREAM_XMIT
    ;

streamCharacter
    : STREAM_ASTERISK_CHAR
    | STREAM_PLUS_CHAR
    | STREAM_MINUS_CHAR
    ;

streamJclCommentArea
    : STREAM_CA_START streamJclWord
    ;

execStatement
    : JCL_DOUBLE_SLASH jclName? execName (JCL_COMMA_CHAR? parameter)*
    ;

execName
    : JCL_EXEC jclCommentArea?
    ;

outputStatement
    : JCL_DOUBLE_SLASH jclName? outputName (JCL_COMMA_CHAR? parameter)*
    ;

outputName
    : JCL_OUTPUT jclCommentArea?
    ;

pendStatement
    : JCL_DOUBLE_SLASH jclName? pendName
    ;

pendName
    : JCL_PEND jclCommentArea?
    ;

procStatement
    : JCL_DOUBLE_SLASH jclName? procName (JCL_COMMA_CHAR? parameter)*
    ;

procName
    : JCL_PROC jclCommentArea?
    ;

setStatement
    : JCL_DOUBLE_SLASH jclName? setName (JCL_COMMA_CHAR? parameter)*
    ;

setName
    : JCL_SET jclCommentArea?
    ;

xmitStatement
    : JCL_DOUBLE_SLASH jclName? xmitName (JCL_COMMA_CHAR? parameter)*
    ;

xmitName
    : JCL_XMIT jclCommentArea?
    ;

parameter
    : name jclTrailingComment?
    | parameterAssignment jclTrailingComment?
    | parameterParentheses jclTrailingComment?
    ;

parameterParentheses
    : JCL_L_PAREN_CHAR (JCL_COMMA_CHAR? parameter)* JCL_R_PAREN_CHAR  jclCommentArea?
    ;

parameterAssignment
    : jclName JCL_EQUAL_CHAR parameter
    ;

name
    : jclWord parameterParentheses?
    ;

jclWord
    : JCL_CONT? (JCL_STRINGLITERAL | jclName)  jclCommentArea?
    ;

jclName
    : JCL_CONT? (JCL_NAME_FIELD | JCL_PARAMETER | jclKeyword)  jclCommentArea?
    ;

jclKeyword
    : JCL_CNTL
    | JCL_DATASET | JCL_DD
    | JCL_ELSE | JCL_ENDCNTL | JCL_ENDDATASET | JCL_ENDIF | JCL_ENDPROCESS | JCL_EXEC | JCL_EXPORT
    | JCL_FORMAT
    | JCL_IF | JCL_INCLUDE
    | JCL_JCLLIB | JCL_JOB | JCL_JOBPARM
    | JCL_MAIN | JCL_MESSAGE
    | JCL_NET | JCL_NETACCT | JCL_NOTIFY
    | JCL_OPERATOR | JCL_OUTPUT
    | JCL_PAUSE | JCL_PEND | JCL_PRIORITY | JCL_PROC | JCL_PROCESS
    | JCL_ROUTE
    | JCL_SCHEDULE | JCL_SET | JCL_SETUP | JCL_SIGNOFF | JCL_SIGNON
    | JCL_THEN
    | JCL_XEQ | JCL_XMIT
    ;

jclCommentArea
    : JCL_CA_START jclWord
    ;

jclTrailingComment
    : JCL_TC_START TRAILING_COMMENT_TEXT* jclCommentArea?
    ;

jes2
    : jes2Word
    ;

jes2Word
    : (JES2_TEXT | JES2_STRINGLITERAL) jes2CommentArea?
    ;

jes2CommentArea
    : CA_START (JES2_TEXT | JES2_STRINGLITERAL)
    ;

jes3
    : jes3Word
    ;

jes3Word
    : (JES3_TEXT | JES3_STRINGLITERAL) jes3CommentArea?
    ;

jes3CommentArea
    : CA_START (JES3_TEXT | JES3_STRINGLITERAL)
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

comment
    : commentWord
    ;

commentWord
    : (COMMENT_TEXT | COMMENT_STRINGLITERAL) commentCommentArea?
    ;

commentCommentArea
    : CA_START (COMMENT_TEXT | COMMENT_STRINGLITERAL)
    ;

unknown
    : unknownWord
    ;

unknownWord
    : (UNKNOWN_TEXT | UNKNOWN_STRINGLITERAL) unknownCommentArea?
    | unknownCommentArea
    ;

unknownCommentArea
    : CA_START (UNKNOWN_TEXT | UNKNOWN_STRINGLITERAL)
    ;
