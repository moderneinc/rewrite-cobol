/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
parser grammar ControlMParser;

options { caseInsensitive = true; tokenVocab=ControlMLexer; }

compilationUnit
    : definitionSection scheduleSection inputSection outputSection applicationFormSection EOF
    ;

definitionSection
    : BROWSE_HEADER definitionLine*
    ;

definitionLine
    : VERTICAL_BAR_CHAR (
        memLine
        | ownerLine
        | applLine
        | descLine
        | overlibLine
        | schenvLine
        | setVarLine
        | ctbSetLine
        | docLine
        )? VERTICAL_BAR_CHAR
    ;

memLine
    : memName memLib
    ;

memName
    : MEMNAME name
    ;

memLib
    : MEMLIB name
    ;

ownerLine
    : owner taskType preventNc2 dflt
    ;

owner
    : OWNER name
    ;

taskType
    : TASKTYPE name
    ;

preventNc2
    : PREVENT_NCT2 name
    ;

dflt
    : DFLT name
    ;

applLine
    : appl group
    ;

appl
    : APPL name?
    ;

group
    : GROUP name
    ;

descLine
    : DESC LINE_TEXT?
    ;

overlibLine
    : overlib statCal
    ;

overlib
    : OVERLIB name?
    ;

statCal
    : STAT_CAL name?
    ;

schenvLine
    : schenv systemId njeNode
    ;

schenv
    : SCHENV name?
    ;

systemId
    : SYSTEM_ID name?
    ;

njeNode
    : NJE_NODE name?
    ;

setVarLine
    : SET_VAR name?
    | SET_VAR name? EQUALS_CHAR name
    ;

ctbSetLine
    : CTB_STEP AT NAME TYPE
    ;

docLine
    : docMem docLib
    ;

docMem
    : DOCMEM name
    ;

docLib
    : DOCLIB name
    ;

scheduleSection
    : SECTION_HEADER scheduleLine+
    ;

scheduleLine
    : VERTICAL_BAR_CHAR SCHEDULE_TEXT* VERTICAL_BAR_CHAR
    ;

inputSection
    : SECTION_HEADER inputLine+
    ;

inputLine
    : VERTICAL_BAR_CHAR (inLine | INPUT_TEXT*) VERTICAL_BAR_CHAR
    ;

inLine
    : in odat
    ;

in
    : IN name
    ;

odat
    : ODAT name?
    ;

outputSection
    :  SECTION_HEADER outputLine+
    ;

outputLine
    : VERTICAL_BAR_CHAR (outLine | OUTPUT_TEXT*) VERTICAL_BAR_CHAR
    ;

outLine
    : out odat
    ;

out
    : OUT name
    ;

applicationFormSection
    : SECTION_HEADER applicationFormLine+
    ;

applicationFormLine
    : VERTICAL_BAR_CHAR APP_FORM_TEXT* VERTICAL_BAR_CHAR
    ;

name
    : NAME
    ;