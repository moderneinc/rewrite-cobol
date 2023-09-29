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
    | cntlStatement
    | endcntlStatement
    | ddStatement
    | ddStreamStatement
    | execStatement
    | exportStatement
    | ifStatement
    | includeStatement
//    | notifyStatement
    | outputStatement
    | pendStatement
    | procStatement
//    | scheduleStatement
    | setStatement
    | xmitStatement
    | emptyStatement
    ;

jobStatement
    : JCL_DOUBLE_SLASH jclName? jobName jclComma? parameterArgument*
    ;

// In JCL, a comma represent a continuation characte for parameters.
// So it may be followed by a trailing comment or a jcl comment area.
jclComma
    : JCL_COMMA_CHAR jclTrailingComment? jclCommentArea?
    ;

parameterArgument
    : parameter jclComma?
    | controlM
    ;

jobName
    : JCL_JOB jclCommentArea?
    ;

jclLibStatement
    : JCL_DOUBLE_SLASH jclName? jclLibName jclComma? parameterArgument*
    ;

jclLibName
    : JCL_JCLLIB jclCommentArea?
    ;

cntlStatement
    : JCL_DOUBLE_SLASH jclName? cntlName jclCommentArea?
    ;

cntlName
    : JCL_CNTL jclCommentArea?
    ;

endcntlStatement
    : JCL_DOUBLE_SLASH jclName? endcntlName jclCommentArea?
    ;

endcntlName
    : JCL_ENDCNTL jclCommentArea?
    ;

ddStatement
    : JCL_DOUBLE_SLASH jclName? ddName jclComma? parameterArgument* jclTrailingComment?
    ;

ddStreamStatement
    : JCL_DOUBLE_SLASH jclName? ddName parameter streamText* jclTrailingComment?
    ;

ddName
    : JCL_DD jclCommentArea?
    ;

streamText
    : (STREAM_TEXT | STREAM_STRINGLITERAL) streamJclCommentArea?
    ;

streamJclCommentArea
    : STREAM_CA_START streamText
    ;

execStatement
    : JCL_DOUBLE_SLASH jclName? execName jclComma? parameterArgument*
    ;

execName
    : JCL_EXEC jclCommentArea?
    ;

exportStatement
    : JCL_DOUBLE_SLASH jclName? exportName jclComma? parameterArgument*
    ;

exportName
    : JCL_EXPORT jclCommentArea?
    ;

ifStatement
    : JCL_DOUBLE_SLASH jclName? ifName IF_CONDITION_TEXT+ thenName statement* elseStatement? endifStatement
    ;

ifName
    : JCL_IF jclCommentArea?
    ;

thenName
    : IF_CONDITION_THEN jclCommentArea?
    ;

elseStatement
    : JCL_DOUBLE_SLASH jclName? elseName statement*
    ;

elseName
    : JCL_ELSE jclCommentArea?
    ;

endifStatement
    : JCL_DOUBLE_SLASH jclName? endifName jclCommentArea?
    ;

endifName
    : JCL_ENDIF jclCommentArea?
    ;

includeStatement
    : JCL_DOUBLE_SLASH jclName? includeName jclComma? parameterArgument*
    ;

includeName
    : JCL_INCLUDE jclCommentArea?
    ;

outputStatement
    : JCL_DOUBLE_SLASH jclName? outputName jclComma? parameterArgument*
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
    : JCL_DOUBLE_SLASH jclName? procName jclComma? parameterArgument*
    ;

procName
    : JCL_PROC jclCommentArea?
    ;

setStatement
    : JCL_DOUBLE_SLASH jclName? setName jclComma? parameterArgument*
    ;

setName
    : JCL_SET jclCommentArea?
    ;

xmitStatement
    : JCL_DOUBLE_SLASH jclName? xmitName jclComma? parameterArgument*
    ;

xmitName
    : JCL_XMIT jclCommentArea?
    ;

emptyStatement
    : JCL_DOUBLE_SLASH jclCommentArea?
    ;

parameter
    : name jclTrailingComment? commentCommentArea?
    | parameterAssignment jclTrailingComment? commentCommentArea?
    | parameterParentheses jclTrailingComment? commentCommentArea?
    ;

parameterParentheses
    : JCL_CONT? JCL_L_PAREN_CHAR jclComma? parameterArgument* JCL_R_PAREN_CHAR jclCommentArea?
    ;

parameterAssignment
    : jclName JCL_EQUAL_CHAR parameter
    ;

name
    : jclWord parameterParentheses?
    ;

jclWord
    : JCL_CONT? (JCL_STRINGLITERAL | jclName) jclCommentArea?
    ;

jclName
    : JCL_CONT? (JCL_NAME_FIELD | JCL_PARAMETER | jclKeyword) jclCommentArea?
    | jclStepName
    ;

jclStepName
    : JCL_STEP_START JCL_STEP_NAME JCL_STEP_END
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
    : cmIf
    | controlMWord+
    ;

cmIf
    : CM_IF cmCondition+ (statement | parameter)+ cmElse? cmEndIf
    ;

cmCondition
    : (CM_IF_CONDITION_TEXT | CM_IF_CONDITION_STRINGLITERAL)
    ;

cmElse
    : CM_ELSE (statement | parameter)+
    ;

cmEndIf
    : CM_ENDIF
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
