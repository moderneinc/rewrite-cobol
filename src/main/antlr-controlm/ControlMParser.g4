/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
parser grammar ControlMParser;

options { caseInsensitive = true; tokenVocab=ControlMLexer; }

compilationUnit
    : (definitionSection scheduleSection inputSection outputSection applicationFormSection)? EOF
    ;

definitionSection
    : DEFINITION_HEADER definitionLine+
    ;

definitionLine
    : DEFINITION_LINE_START (
        memLine
        | ownerLine
        | applLine
        | descLine
        | overlibLine
        | schenvLine
        | setVarLine
        | ctbSetLine
        | docLine
        )? DEFINITION_LINE_END
    ;

memLine
    : memName memLib
    ;

memName
    : DEFINITION_MEMNAME name?
    ;

memLib
    : DEFINITION_MEMLIB name?
    ;

ownerLine
    : owner taskType preventNc2 dflt
    ;

owner
    : DEFINITION_OWNER name?
    ;

taskType
    : DEFINITION_TASKTYPE name?
    ;

preventNc2
    : DEFINITION_PREVENT_NCT2 name?
    ;

dflt
    : DEFINITION_DFLT name?
    ;

applLine
    : appl group
    ;

appl
    : DEFINITION_APPL name?
    ;

group
    : DEFINITION_GROUP name?
    ;

descLine
    : DEFINITION_DESC DESC_TEXT_WORD*
    ;

overlibLine
    : overlib statCal
    ;

overlib
    : DEFINITION_OVERLIB name?
    ;

statCal
    : DEFINITION_STAT_CAL name?
    ;

schenvLine
    : schenv systemId njeNode
    ;

schenv
    : DEFINITION_SCHENV name?
    ;

systemId
    : DEFINITION_SYSTEM_ID name?
    ;

njeNode
    : DEFINITION_NJE_NODE name?
    ;

setVarLine
    : DEFINITION_SET_VAR name?
    | DEFINITION_SET_VAR name DEFINITION_EQUALS_CHAR name
    ;

ctbSetLine
    : DEFINITION_CTB_STEP DEFINITION_AT name DEFINITION_TYPE
    ;

docLine
    : docMem docLib
    ;

docMem
    : DEFINITION_DOCMEM name?
    ;

docLib
    : DEFINITION_DOCLIB name?
    ;

// Section 2.
scheduleSection
    : SCHEDULE_HEADER scheduleLine+
    ;

scheduleLine
    : SCHEDULE_LINE_START name* SCHEDULE_LINE_END
    ;

// Section 3.
inputSection
    : INPUT_HEADER inputNamesLine+ inputLine+
    ;

// The `IN` portion of an INPUT section is separated since the names are relevant for relationships.
inputNamesLine
    : INPUT_NAMES_LINE_START INPUT_NAMES_IN? (input)* INPUT_NAMES_LINE_END
    ;

input
    : name? date
    ;

date
    : (ODAT | DATE_WILDCARD) name?
    ;

inputLine
    : INPUT_LINE_START name* INPUT_LINE_END
    ;

outputSection
    :  OUTPUT_HEADER outputNamesLine+ outputLine+
    ;

// Output line is separated from consumed text since it contains relevant file names.
outputNamesLine
    : OUTPUT_NAMES_LINE_START OUTPUT_NAMES_OUT? (output)* OUTPUT_NAMES_LINE_END
    ;

output
    : name? date
    ;

outputLine
    : OUTPUT_LINE_START name* OUTPUT_LINE_END
    ;

applicationFormSection
    : SECTION_HEADER applicationFormLine+
    ;

applicationFormLine
    : APP_FORM_LINE_START name* APP_FORM_LINE_END
    ;

name
    : NAME
    ;