/*
* Copyright (C) 2017, Ulrich Wolffgang <ulrich.wolffgang@proleap.io>
* All rights reserved.
*
* This software may be modified and distributed under the terms
* of the MIT license. See the LICENSE file for details.
*/

/*
* COBOL Grammar for ANTLR4
*
* This is a COBOL grammar, which is part of the COBOL parser at
* https://github.com/uwol/proleap-cobol-parser.
*
* The grammar passes the NIST test suite and has successfully been applied to
* numerous COBOL files from banking and insurance. To be used in conjunction
* with the provided preprocessor, which executes COPY and REPLACE statements.
*/

grammar Cobol;

options { caseInsensitive = true; }

/* Note:
 * The IBM-ANSI-85 spec defines a whitespace character as ' ', ', ', or `; `.
 * However, client code contains custom whitespace rules that do not match the language spec.
 * The customized whitespace allows for new lines to exist anywhere in the source code.
 */

compilationUnit
   : programUnit* EOF
   ;

programUnit
   : identificationDivision environmentDivision? dataDivision? procedureDivision? programUnit* endProgramStatement?
   ;

endProgramStatement
   : END PROGRAM programName DOT_FS
   ;

// --- identification division --------------------------------------------------------------------

identificationDivision
   : (IDENTIFICATION | ID) DIVISION DOT_FS programIdParagraph identificationDivisionBody*
   ;

identificationDivisionBody
   : authorParagraph | installationParagraph | dateWrittenParagraph | dateCompiledParagraph | securityParagraph | remarksParagraph
   ;

// - program id paragraph ----------------------------------

programIdParagraph
   : PROGRAM_ID DOT_FS programName (IS? (COMMON | INITIAL | LIBRARY | DEFINITION | RECURSIVE) PROGRAM?)? DOT_FS? commentEntry?
   ;

// - author paragraph ----------------------------------

authorParagraph
   : AUTHOR DOT_FS commentEntry?
   ;

// - installation paragraph ----------------------------------

installationParagraph
   : INSTALLATION DOT_FS commentEntry?
   ;

// - date written paragraph ----------------------------------

dateWrittenParagraph
   : DATE_WRITTEN DOT_FS commentEntry?
   ;

// - date compiled paragraph ----------------------------------

dateCompiledParagraph
   : DATE_COMPILED DOT_FS commentEntry?
   ;

// - security paragraph ----------------------------------

securityParagraph
   : SECURITY DOT_FS commentEntry?
   ;

// - remarks paragraph ----------------------------------

remarksParagraph
   : REMARKS DOT_FS commentEntry? END_REMARKS? DOT_FS?
   ;

// --- environment division --------------------------------------------------------------------

environmentDivision
   : ENVIRONMENT DIVISION DOT_FS environmentDivisionBody*
   ;

environmentDivisionBody
   : configurationSection | specialNamesParagraph | inputOutputSection
   ;

// -- configuration section ----------------------------------

configurationSection
   : CONFIGURATION SECTION DOT_FS configurationSectionParagraph*
   ;

// - configuration section paragraph ----------------------------------

configurationSectionParagraph
   : sourceComputerParagraph | objectComputerParagraph | specialNamesParagraph
   // strictly, specialNamesParagraph does not belong into configurationSectionParagraph, but IBM-COBOL allows this
   ;

// - source computer paragraph ----------------------------------

sourceComputerParagraph
   : SOURCE_COMPUTER DOT_FS (computerName (WITH? DEBUGGING MODE)? DOT_FS)?
   ;

// - object computer paragraph ----------------------------------

objectComputerParagraph
   : OBJECT_COMPUTER DOT_FS (computerName objectComputerClause* DOT_FS)?
   ;

objectComputerClause
   : memorySizeClause | diskSizeClause | collatingSequenceClause | segmentLimitClause | characterSetClause
   ;

memorySizeClause
   : MEMORY SIZE? (integerLiteral | cobolWord) (WORDS | CHARACTERS | MODULES)?
   ;

diskSizeClause
   : DISK SIZE? IS? (integerLiteral | cobolWord) (WORDS | MODULES)?
   ;

collatingSequenceClause
   : PROGRAM? COLLATING? SEQUENCE (IS? alphabetName+) collatingSequenceClauseAlphanumeric? collatingSequenceClauseNational?
   ;

collatingSequenceClauseAlphanumeric
   : FOR? ALPHANUMERIC IS? alphabetName
   ;

collatingSequenceClauseNational
   : FOR? NATIONAL IS? alphabetName
   ;

segmentLimitClause
   : SEGMENT_LIMIT IS? integerLiteral
   ;

characterSetClause
   : CHARACTER SET DOT_FS
   ;

// - special names paragraph ----------------------------------

specialNamesParagraph
   : SPECIAL_NAMES DOT_FS (specialNameClause+ DOT_FS)?
   ;

specialNameClause
   : channelClause | odtClause | alphabetClause | classClause | currencySignClause | decimalPointClause | symbolicCharactersClause | environmentSwitchNameClause | defaultDisplaySignClause | defaultComputationalSignClause | reserveNetworkClause
   ;

alphabetClause
   : alphabetClauseFormat1 | alphabetClauseFormat2
   ;

alphabetClauseFormat1
   : ALPHABET alphabetName (FOR ALPHANUMERIC)? IS? (EBCDIC | ASCII | STANDARD_1 | STANDARD_2 | NATIVE | cobolWord | alphabetLiterals+)
   ;

alphabetLiterals
   : literal (alphabetThrough | alphabetAlso+)?
   ;

alphabetThrough
   : (THROUGH | THRU) literal
   ;

alphabetAlso
   : ALSO literal+
   ;

alphabetClauseFormat2
   : ALPHABET alphabetName FOR? NATIONAL IS? (NATIVE | CCSVERSION literal)
   ;

channelClause
   : CHANNEL integerLiteral IS? mnemonicName
   ;

classClause
   : CLASS className (FOR? (ALPHANUMERIC | NATIONAL))? IS? classClauseThrough+
   ;

classClauseThrough
   : classClauseFrom ((THROUGH | THRU) classClauseTo)?
   ;

classClauseFrom
   : identifier | literal
   ;

classClauseTo
   : identifier | literal
   ;

currencySignClause
   : CURRENCY SIGN? IS? literal (WITH? PICTURE SYMBOL literal)?
   ;

decimalPointClause
   : DECIMAL_POINT IS? COMMA
   ;

defaultComputationalSignClause
   : DEFAULT (COMPUTATIONAL | COMP)? (SIGN IS?)? (LEADING | TRAILING)? (SEPARATE CHARACTER?)
   ;

defaultDisplaySignClause
   : DEFAULT_DISPLAY (SIGN IS?)? (LEADING | TRAILING) (SEPARATE CHARACTER?)?
   ;

environmentSwitchNameClause
   : environmentName IS? mnemonicName environmentSwitchNameSpecialNamesStatusPhrase? | environmentSwitchNameSpecialNamesStatusPhrase
   ;

environmentSwitchNameSpecialNamesStatusPhrase
   : ON STATUS? IS? condition (OFF STATUS? IS? condition)? | OFF STATUS? IS? condition (ON STATUS? IS? condition)?
   ;

odtClause
   : ODT IS? mnemonicName
   ;

reserveNetworkClause
   : RESERVE WORDS? LIST? IS? NETWORK CAPABLE?
   ;

symbolicCharactersClause
   : SYMBOLIC CHARACTERS? (FOR? (ALPHANUMERIC | NATIONAL))? symbolicCharacters+ (IN alphabetName)?
   ;

symbolicCharacters
   : symbolicCharacter+ (IS | ARE)? integerLiteral+
   ;

// -- input output section ----------------------------------

inputOutputSection
   : INPUT_OUTPUT SECTION DOT_FS inputOutputSectionParagraph*
   ;

// - input output section paragraph ----------------------------------

inputOutputSectionParagraph
   : fileControlParagraph | ioControlParagraph
   ;

// - file control paragraph ----------------------------------

fileControlParagraph
   : FILE_CONTROL? (DOT_FS? fileControlEntry)* DOT_FS
   ;

fileControlEntry
   : selectClause fileControlClause*
   ;

selectClause
   : SELECT OPTIONAL? fileName
   ;

fileControlClause
   : assignClause | reserveClause | organizationClause | paddingCharacterClause | recordDelimiterClause | accessModeClause | recordKeyClause | alternateRecordKeyClause | fileStatusClause | passwordClause | relativeKeyClause
   ;

assignClause
   : ASSIGN TO? (DISK | DISPLAY | KEYBOARD | PORT | PRINTER | READER | REMOTE | TAPE | VIRTUAL | (DYNAMIC | EXTERNAL)? assignmentName | literal)
   ;

reserveClause
   : RESERVE (NO | integerLiteral) ALTERNATE? (AREA | AREAS)?
   ;

organizationClause
   : (ORGANIZATION IS?)? (LINE | RECORD BINARY | RECORD | BINARY)? (SEQUENTIAL | RELATIVE | INDEXED)
   ;

paddingCharacterClause
   : PADDING CHARACTER? IS? (qualifiedDataName | literal)
   ;

recordDelimiterClause
   : RECORD DELIMITER IS? (STANDARD_1 | IMPLICIT | assignmentName)
   ;

accessModeClause
   : ACCESS MODE? IS? (SEQUENTIAL | RANDOM | DYNAMIC | EXCLUSIVE)
   ;

recordKeyClause
   : RECORD KEY? IS? qualifiedDataName passwordClause? (WITH? DUPLICATES)?
   ;

alternateRecordKeyClause
   : ALTERNATE RECORD KEY? IS? qualifiedDataName passwordClause? (WITH? DUPLICATES)?
   ;

passwordClause
   : PASSWORD IS? dataName
   ;

fileStatusClause
   : FILE? STATUS IS? qualifiedDataName qualifiedDataName?
   ;

relativeKeyClause
   : RELATIVE KEY? IS? qualifiedDataName
   ;

// - io control paragraph ----------------------------------

ioControlParagraph
   : I_O_CONTROL DOT_FS (fileName DOT_FS)? (ioControlClause* DOT_FS)?
   ;

ioControlClause
   : rerunClause | sameClause | multipleFileClause | commitmentControlClause
   ;

rerunClause
   : RERUN (ON (assignmentName | fileName))? EVERY (rerunEveryRecords | rerunEveryOf | rerunEveryClock)
   ;

rerunEveryRecords
   : integerLiteral RECORDS
   ;

rerunEveryOf
   : END? OF? (REEL | UNIT) OF fileName
   ;

rerunEveryClock
   : integerLiteral CLOCK_UNITS?
   ;

sameClause
   : SAME (RECORD | SORT | SORT_MERGE)? AREA? FOR? fileName+
   ;

multipleFileClause
   : MULTIPLE FILE TAPE? CONTAINS? multipleFilePosition+
   ;

multipleFilePosition
   : fileName (POSITION integerLiteral)?
   ;

commitmentControlClause
   : COMMITMENT CONTROL FOR? fileName
   ;

// --- data division --------------------------------------------------------------------

dataDivision
   : DATA DIVISION DOT_FS dataDivisionSection*
   ;

dataDivisionSection
   : fileSection | dataBaseSection | workingStorageSection | linkageSection | communicationSection | localStorageSection | screenSection | reportSection | programLibrarySection
   ;

// -- file section ----------------------------------

fileSection
   : FILE SECTION DOT_FS fileDescriptionEntry*
   ;

fileDescriptionEntry
   : (FD | SD) fileName ((DOT_FS | COMMACHAR)? fileDescriptionEntryClause)* DOT_FS dataDescriptionEntry*
   ;

fileDescriptionEntryClause
   : externalClause | globalClause | blockContainsClause | recordContainsClause | labelRecordsClause | valueOfClause | dataRecordsClause | linageClause | codeSetClause | reportClause | recordingModeClause
   ;

externalClause
   : IS? EXTERNAL
   ;

globalClause
   : IS? GLOBAL
   ;

blockContainsClause
   : BLOCK CONTAINS? integerLiteral blockContainsTo? (RECORDS | CHARACTERS)?
   ;

blockContainsTo
   : TO integerLiteral
   ;

recordContainsClause
   : RECORD (recordContainsClauseFormat1 | recordContainsClauseFormat2 | recordContainsClauseFormat3)
   ;

recordContainsClauseFormat1
   : CONTAINS? integerLiteral CHARACTERS?
   ;

recordContainsClauseFormat2
   : IS? VARYING IN? SIZE? (FROM? integerLiteral recordContainsTo? CHARACTERS?)? (DEPENDING ON? qualifiedDataName)?
   ;

recordContainsClauseFormat3
   : CONTAINS? integerLiteral recordContainsTo CHARACTERS?
   ;

recordContainsTo
   : TO integerLiteral
   ;

labelRecordsClause
   : LABEL (RECORD | RECORDS) (IS | ARE)? (OMITTED | STANDARD | dataName+)
   ;

valueOfClause
   : VALUE OF valuePair+
   ;

valuePair
   : systemName IS? (qualifiedDataName | literal)
   ;

dataRecordsClause
   : DATA (RECORD | RECORDS) (IS | ARE)? dataName+
   ;

linageClause
   : LINAGE IS? (dataName | integerLiteral) LINES? linageAt*
   ;

linageAt
   : linageFootingAt | linageLinesAtTop | linageLinesAtBottom
   ;

linageFootingAt
   : WITH? FOOTING AT? (dataName | integerLiteral)
   ;

linageLinesAtTop
   : LINES? AT? TOP (dataName | integerLiteral)
   ;

linageLinesAtBottom
   : LINES? AT? BOTTOM (dataName | integerLiteral)
   ;

recordingModeClause
   : RECORDING MODE? IS? modeStatement
   ;

modeStatement
   : cobolWord
   ;

codeSetClause
   : CODE_SET IS? alphabetName
   ;

reportClause
   : (REPORT | REPORTS) (IS | ARE)? reportName+
   ;

// -- data base section ----------------------------------

dataBaseSection
   : DATA_BASE SECTION DOT_FS dataBaseSectionEntry*
   ;

dataBaseSectionEntry
   : integerLiteral literal INVOKE literal
   ;

// -- working storage section ----------------------------------

workingStorageSection
   : WORKING_STORAGE SECTION DOT_FS dataDescriptionEntry*
   ;

// -- linkage section ----------------------------------

linkageSection
   : LINKAGE SECTION DOT_FS dataDescriptionEntry*
   ;

// -- communication section ----------------------------------

communicationSection
   : COMMUNICATION SECTION DOT_FS (communicationDescriptionEntry | dataDescriptionEntry)*
   ;

communicationDescriptionEntry
   : communicationDescriptionEntryFormat1 | communicationDescriptionEntryFormat2 | communicationDescriptionEntryFormat3
   ;

communicationDescriptionEntryFormat1
   : CD cdName FOR? INITIAL? INPUT ((symbolicQueueClause | symbolicSubQueueClause | messageDateClause | messageTimeClause | symbolicSourceClause | textLengthClause | endKeyClause | statusKeyClause | messageCountClause) | dataDescName)* DOT_FS
   ;

communicationDescriptionEntryFormat2
   : CD cdName FOR? OUTPUT (destinationCountClause | textLengthClause | statusKeyClause | destinationTableClause | errorKeyClause | symbolicDestinationClause)* DOT_FS
   ;

communicationDescriptionEntryFormat3
   : CD cdName FOR? INITIAL I_O ((messageDateClause | messageTimeClause | symbolicTerminalClause | textLengthClause | endKeyClause | statusKeyClause) | dataDescName)* DOT_FS
   ;

destinationCountClause
   : DESTINATION COUNT IS? dataDescName
   ;

destinationTableClause
   : DESTINATION TABLE OCCURS integerLiteral TIMES (INDEXED BY indexName+)?
   ;

endKeyClause
   : END KEY IS? dataDescName
   ;

errorKeyClause
   : ERROR KEY IS? dataDescName
   ;

messageCountClause
   : MESSAGE? COUNT IS? dataDescName
   ;

messageDateClause
   : MESSAGE DATE IS? dataDescName
   ;

messageTimeClause
   : MESSAGE TIME IS? dataDescName
   ;

statusKeyClause
   : STATUS KEY IS? dataDescName
   ;

symbolicDestinationClause
   : SYMBOLIC? DESTINATION IS? dataDescName
   ;

symbolicQueueClause
   : SYMBOLIC? QUEUE IS? dataDescName
   ;

symbolicSourceClause
   : SYMBOLIC? SOURCE IS? dataDescName
   ;

symbolicTerminalClause
   : SYMBOLIC? TERMINAL IS? dataDescName
   ;

symbolicSubQueueClause
   : SYMBOLIC? (SUB_QUEUE_1 | SUB_QUEUE_2 | SUB_QUEUE_3) IS? dataDescName
   ;

textLengthClause
   : TEXT LENGTH IS? dataDescName
   ;

// -- local storage section ----------------------------------

localStorageSection
   : LOCAL_STORAGE SECTION DOT_FS (LD localName DOT_FS)? dataDescriptionEntry*
   ;

// -- screen section ----------------------------------

screenSection
   : SCREEN SECTION DOT_FS screenDescriptionEntry*
   ;

screenDescriptionEntry
   : INTEGERLITERAL (FILLER | screenName)? (screenDescriptionBlankClause | screenDescriptionBellClause | screenDescriptionBlinkClause | screenDescriptionEraseClause | screenDescriptionLightClause | screenDescriptionGridClause | screenDescriptionReverseVideoClause | screenDescriptionUnderlineClause | screenDescriptionSizeClause | screenDescriptionLineClause | screenDescriptionColumnClause | screenDescriptionForegroundColorClause | screenDescriptionBackgroundColorClause | screenDescriptionControlClause | screenDescriptionValueClause | screenDescriptionPictureClause | (screenDescriptionFromClause | screenDescriptionUsingClause) | screenDescriptionUsageClause | screenDescriptionBlankWhenZeroClause | screenDescriptionJustifiedClause | screenDescriptionSignClause | screenDescriptionAutoClause | screenDescriptionSecureClause | screenDescriptionRequiredClause | screenDescriptionPromptClause | screenDescriptionFullClause | screenDescriptionZeroFillClause)* DOT_FS
   ;

screenDescriptionBlankClause
   : BLANK (SCREEN | LINE)
   ;

screenDescriptionBellClause
   : BELL | BEEP
   ;

screenDescriptionBlinkClause
   : BLINK
   ;

screenDescriptionEraseClause
   : ERASE (EOL | EOS)
   ;

screenDescriptionLightClause
   : HIGHLIGHT | LOWLIGHT
   ;

screenDescriptionGridClause
   : GRID | LEFTLINE | OVERLINE
   ;

screenDescriptionReverseVideoClause
   : REVERSE_VIDEO
   ;

screenDescriptionUnderlineClause
   : UNDERLINE
   ;

screenDescriptionSizeClause
   : SIZE IS? (identifier | integerLiteral)
   ;

screenDescriptionLineClause
   : LINE (NUMBER? IS? (PLUS | PLUSCHAR | MINUSCHAR))? (identifier | integerLiteral)
   ;

screenDescriptionColumnClause
   : (COLUMN | COL) (NUMBER? IS? (PLUS | PLUSCHAR | MINUSCHAR))? (identifier | integerLiteral)
   ;

screenDescriptionForegroundColorClause
   : (FOREGROUND_COLOR | FOREGROUND_COLOUR) IS? (identifier | integerLiteral)
   ;

screenDescriptionBackgroundColorClause
   : (BACKGROUND_COLOR | BACKGROUND_COLOUR) IS? (identifier | integerLiteral)
   ;

screenDescriptionControlClause
   : CONTROL IS? identifier
   ;

screenDescriptionValueClause
   : (VALUE IS?) literal
   ;

screenDescriptionPictureClause
   : (PICTURE | PIC) IS? pictureString
   ;

screenDescriptionFromClause
   : FROM (identifier | literal) screenDescriptionToClause?
   ;

screenDescriptionToClause
   : TO identifier
   ;

screenDescriptionUsingClause
   : USING identifier
   ;

screenDescriptionUsageClause
   : (USAGE IS?) (DISPLAY | DISPLAY_1)
   ;

screenDescriptionBlankWhenZeroClause
   : BLANK WHEN? ZERO
   ;

screenDescriptionJustifiedClause
   : (JUSTIFIED | JUST) RIGHT?
   ;

screenDescriptionSignClause
   : (SIGN IS?)? (LEADING | TRAILING) (SEPARATE CHARACTER?)?
   ;

screenDescriptionAutoClause
   : AUTO | AUTO_SKIP
   ;

screenDescriptionSecureClause
   : SECURE | NO_ECHO
   ;

screenDescriptionRequiredClause
   : REQUIRED | EMPTY_CHECK
   ;

screenDescriptionPromptClause
   : PROMPT CHARACTER? IS? (identifier | literal) screenDescriptionPromptOccursClause?
   ;

screenDescriptionPromptOccursClause
   : OCCURS integerLiteral TIMES?
   ;

screenDescriptionFullClause
   : FULL | LENGTH_CHECK
   ;

screenDescriptionZeroFillClause
   : ZERO_FILL
   ;

// -- report section ----------------------------------

reportSection
   : REPORT SECTION DOT_FS reportDescription*
   ;

reportDescription
   : reportDescriptionEntry reportGroupDescriptionEntry+
   ;

reportDescriptionEntry
   : RD reportName reportDescriptionGlobalClause? (reportDescriptionPageLimitClause reportDescriptionHeadingClause? reportDescriptionFirstDetailClause? reportDescriptionLastDetailClause? reportDescriptionFootingClause?)? DOT_FS
   ;

reportDescriptionGlobalClause
   : IS? GLOBAL
   ;

reportDescriptionPageLimitClause
   : PAGE (LIMIT | LIMITS) (IS | ARE)? integerLiteral (LINE | LINES)?
   ;

reportDescriptionHeadingClause
   : HEADING integerLiteral
   ;

reportDescriptionFirstDetailClause
   : FIRST DETAIL integerLiteral
   ;

reportDescriptionLastDetailClause
   : LAST DETAIL integerLiteral
   ;

reportDescriptionFootingClause
   : FOOTING integerLiteral
   ;

reportGroupDescriptionEntry
   : reportGroupDescriptionEntryFormat1 | reportGroupDescriptionEntryFormat2 | reportGroupDescriptionEntryFormat3
   ;

reportGroupDescriptionEntryFormat1
   : integerLiteral dataName reportGroupLineNumberClause? reportGroupNextGroupClause? reportGroupTypeClause reportGroupUsageClause? DOT_FS
   ;

reportGroupDescriptionEntryFormat2
   : integerLiteral dataName? reportGroupLineNumberClause? reportGroupUsageClause DOT_FS
   ;

reportGroupDescriptionEntryFormat3
   : integerLiteral dataName? (reportGroupPictureClause | reportGroupUsageClause | reportGroupSignClause | reportGroupJustifiedClause | reportGroupBlankWhenZeroClause | reportGroupLineNumberClause | reportGroupColumnNumberClause | (reportGroupSourceClause | reportGroupValueClause | reportGroupSumClause | reportGroupResetClause) | reportGroupIndicateClause)* DOT_FS
   ;

reportGroupBlankWhenZeroClause
   : BLANK WHEN? ZERO
   ;

reportGroupColumnNumberClause
   : COLUMN NUMBER? IS? integerLiteral
   ;

reportGroupIndicateClause
   : GROUP INDICATE?
   ;

reportGroupJustifiedClause
   : (JUSTIFIED | JUST) RIGHT?
   ;

reportGroupLineNumberClause
   : LINE? NUMBER? IS? (reportGroupLineNumberNextPage | reportGroupLineNumberPlus)
   ;

reportGroupLineNumberNextPage
   : integerLiteral (ON? NEXT PAGE)?
   ;

reportGroupLineNumberPlus
   : PLUS integerLiteral
   ;

reportGroupNextGroupClause
   : NEXT GROUP IS? (integerLiteral | reportGroupNextGroupNextPage | reportGroupNextGroupPlus)
   ;

reportGroupNextGroupPlus
   : PLUS integerLiteral
   ;

reportGroupNextGroupNextPage
   : NEXT PAGE
   ;

reportGroupPictureClause
   : (PICTURE | PIC) IS? pictureString
   ;

reportGroupResetClause
   : RESET ON? (FINAL | dataName)
   ;

reportGroupSignClause
   : SIGN IS? (LEADING | TRAILING) SEPARATE CHARACTER?
   ;

reportGroupSourceClause
   : SOURCE IS? identifier
   ;

reportGroupSumClause
   : SUM identifier+ (UPON dataName+)?
   ;

reportGroupTypeClause
   : TYPE IS? (reportGroupTypeReportHeading | reportGroupTypePageHeading | reportGroupTypeControlHeading | reportGroupTypeDetail | reportGroupTypeControlFooting | reportGroupTypePageFooting | reportGroupTypeReportFooting)
   ;

reportGroupTypeReportHeading
   : REPORT HEADING | RH
   ;

reportGroupTypePageHeading
   : PAGE HEADING | PH
   ;

reportGroupTypeControlHeading
   : (CONTROL HEADING | CH) (FINAL | dataName)
   ;

reportGroupTypeDetail
   : DETAIL | DE
   ;

reportGroupTypeControlFooting
   : (CONTROL FOOTING | CF) (FINAL | dataName)
   ;

reportGroupUsageClause
   : (USAGE IS?)? (DISPLAY | DISPLAY_1)
   ;

reportGroupTypePageFooting
   : PAGE FOOTING | PF
   ;

reportGroupTypeReportFooting
   : REPORT FOOTING | RF
   ;

reportGroupValueClause
   : VALUE IS? literal
   ;

// -- program library section ----------------------------------

programLibrarySection
   : PROGRAM_LIBRARY SECTION DOT_FS libraryDescriptionEntry*
   ;

libraryDescriptionEntry
   : libraryDescriptionEntryFormat1 | libraryDescriptionEntryFormat2
   ;

libraryDescriptionEntryFormat1
   : LD libraryName EXPORT libraryAttributeClauseFormat1? libraryEntryProcedureClauseFormat1?
   ;

libraryDescriptionEntryFormat2
   : LB libraryName IMPORT libraryIsGlobalClause? libraryIsCommonClause? (libraryAttributeClauseFormat2 | libraryEntryProcedureClauseFormat2)*
   ;

libraryAttributeClauseFormat1
   : ATTRIBUTE (SHARING IS? (DONTCARE | PRIVATE | SHAREDBYRUNUNIT | SHAREDBYALL))?
   ;

libraryAttributeClauseFormat2
   : ATTRIBUTE libraryAttributeFunction? (LIBACCESS IS? (BYFUNCTION | BYTITLE))? libraryAttributeParameter? libraryAttributeTitle?
   ;

libraryAttributeFunction
   : FUNCTIONNAME IS literal
   ;

libraryAttributeParameter
   : LIBPARAMETER IS? literal
   ;

libraryAttributeTitle
   : TITLE IS? literal
   ;

libraryEntryProcedureClauseFormat1
   : ENTRY_PROCEDURE programName libraryEntryProcedureForClause?
   ;

libraryEntryProcedureClauseFormat2
   : ENTRY_PROCEDURE programName libraryEntryProcedureForClause? libraryEntryProcedureWithClause? libraryEntryProcedureUsingClause? libraryEntryProcedureGivingClause?
   ;

libraryEntryProcedureForClause
   : FOR literal
   ;

libraryEntryProcedureGivingClause
   : GIVING dataName
   ;

libraryEntryProcedureUsingClause
   : USING libraryEntryProcedureUsingName+
   ;

libraryEntryProcedureUsingName
   : dataName | fileName
   ;

libraryEntryProcedureWithClause
   : WITH libraryEntryProcedureWithName+
   ;

libraryEntryProcedureWithName
   : localName | fileName
   ;

libraryIsCommonClause
   : IS? COMMON
   ;

libraryIsGlobalClause
   : IS? GLOBAL
   ;

// data description entry ----------------------------------

dataDescriptionEntry
   : dataDescriptionEntryFormat1 | dataDescriptionEntryFormat2 | dataDescriptionEntryFormat3 | dataDescriptionEntryExecSql
   ;

dataDescriptionEntryFormat1
   : (INTEGERLITERAL | LEVEL_NUMBER_77) (FILLER | dataName)? dataDescriptionEntryFormat1Clause* DOT_FS
   ;

dataDescriptionEntryFormat1Clause
   : dataRedefinesClause | dataIntegerStringClause | dataExternalClause | dataGlobalClause | dataTypeDefClause | dataThreadLocalClause | dataPictureClause | dataCommonOwnLocalClause | dataTypeClause | dataUsingClause | dataUsageClause | dataValueClause | dataReceivedByClause | dataOccursClause | dataSignClause | dataSynchronizedClause | dataJustifiedClause | dataBlankWhenZeroClause | dataWithLowerBoundsClause | dataAlignedClause | dataRecordAreaClause
   ;

dataDescriptionEntryFormat2
   : LEVEL_NUMBER_66 dataName dataRenamesClause DOT_FS
   ;

dataDescriptionEntryFormat3
   : LEVEL_NUMBER_88 conditionName dataValueClause DOT_FS
   ;

dataDescriptionEntryExecSql
   : EXECSQLLINE+ DOT_FS?
   ;

dataAlignedClause
   : ALIGNED
   ;

dataBlankWhenZeroClause
   : BLANK WHEN? (ZERO | ZEROS | ZEROES)
   ;

dataCommonOwnLocalClause
   : COMMON | OWN | LOCAL
   ;

dataExternalClause
   : IS? EXTERNAL (BY literal)?
   ;

dataGlobalClause
   : IS? GLOBAL
   ;

dataIntegerStringClause
   : INTEGER | STRING
   ;

dataJustifiedClause
   : (JUSTIFIED | JUST) RIGHT?
   ;

dataOccursClause
   : OCCURS (identifier | integerLiteral) dataOccursTo? TIMES? dataOccursDepending? (dataOccursSort | dataOccursIndexed)*
   ;

dataOccursTo
   : TO integerLiteral
   ;

dataOccursDepending
   : DEPENDING ON? qualifiedDataName
   ;

dataOccursSort
   : (ASCENDING | DESCENDING) KEY? IS? qualifiedDataName+
   ;

dataOccursIndexed
   : INDEXED BY? LOCAL? indexName+
   ;

dataPictureClause
   : (PICTURE | PIC) IS? pictureString
   ;

pictureString
   : picture+
   ;

picture
   : (pictureChars+ pictureCardinality?)
   ;

pictureChars
   : DOLLARCHAR | IDENTIFIER | NUMERICLITERAL | SLASHCHAR | COMMACHAR | DOT_FS | COLONCHAR | ASTERISKCHAR | DOUBLEASTERISKCHAR | LPARENCHAR | RPARENCHAR | PLUSCHAR | MINUSCHAR | LESSTHANCHAR | MORETHANCHAR | integerLiteral
   ;

pictureCardinality
   : LPARENCHAR integerLiteral RPARENCHAR
   ;

dataReceivedByClause
   : RECEIVED? BY? (CONTENT | REFERENCE | REF)
   ;

dataRecordAreaClause
   : RECORD AREA
   ;

dataRedefinesClause
   : REDEFINES dataName
   ;

dataRenamesClause
   : RENAMES qualifiedDataName ((THROUGH | THRU) qualifiedDataName)?
   ;

dataSignClause
   : (SIGN IS?)? (LEADING | TRAILING) (SEPARATE CHARACTER?)?
   ;

dataSynchronizedClause
   : (SYNCHRONIZED | SYNC) (LEFT | RIGHT)?
   ;

dataThreadLocalClause
   : IS? THREAD_LOCAL
   ;

dataTypeClause
   : TYPE IS? (SHORT_DATE | LONG_DATE | NUMERIC_DATE | NUMERIC_TIME | LONG_TIME | (CLOB | BLOB | DBCLOB) LPARENCHAR integerLiteral RPARENCHAR)
   ;

dataTypeDefClause
   : IS? TYPEDEF
   ;

dataUsageClause
   : (USAGE IS?)? (BINARY (TRUNCATED | EXTENDED)? | BIT | COMP | COMP_1 | COMP_2 | COMP_3 | COMP_4 | COMP_5 | COMPUTATIONAL | COMPUTATIONAL_1 | COMPUTATIONAL_2 | COMPUTATIONAL_3 | COMPUTATIONAL_4 | COMPUTATIONAL_5 | CONTROL_POINT | DATE | DISPLAY | DISPLAY_1 | DOUBLE | EVENT | FUNCTION_POINTER | INDEX | KANJI | LOCK | NATIONAL | PACKED_DECIMAL | POINTER | PROCEDURE_POINTER | REAL | SQL | TASK)
   ;

dataUsingClause
   : USING (LANGUAGE | CONVENTION) OF? (cobolWord | dataName)
   ;

dataValueClause
   : ((VALUE | VALUES) (IS | ARE)?)? dataValueInterval+
   ;

dataValueInterval
   : dataValueIntervalFrom dataValueIntervalTo?
   ;

dataValueIntervalFrom
   : literal | cobolWord
   ;

dataValueIntervalTo
   : (THROUGH | THRU) literal
   ;

dataWithLowerBoundsClause
   : WITH? LOWER BOUNDS
   ;

// --- procedure division --------------------------------------------------------------------

procedureDivision
   : PROCEDURE DIVISION procedureDivisionUsingClause? procedureDivisionGivingClause? DOT_FS procedureDeclaratives? procedureDivisionBody
   ;

procedureDivisionUsingClause
   : (USING | CHAINING) procedureDivisionUsingParameter+
   ;

procedureDivisionGivingClause
   : (GIVING | RETURNING) dataName
   ;

procedureDivisionUsingParameter
   : procedureDivisionByReferencePhrase | procedureDivisionByValuePhrase
   ;

procedureDivisionByReferencePhrase
   : (BY? REFERENCE)? procedureDivisionByReference+
   ;

procedureDivisionByReference
   : (OPTIONAL? (identifier | fileName)) | ANY
   ;

procedureDivisionByValuePhrase
   : BY? VALUE procedureDivisionByValue+
   ;

procedureDivisionByValue
   : identifier | literal | ANY
   ;

procedureDeclaratives
   : DECLARATIVES DOT_FS procedureDeclarative+ END DECLARATIVES DOT_FS
   ;

procedureDeclarative
   : procedureSectionHeader DOT_FS useStatement DOT_FS paragraphs
   ;

procedureSectionHeader
   : sectionName SECTION integerLiteral?
   ;

procedureDivisionBody
   : paragraphs procedureSection*
   ;

// -- procedure section ----------------------------------

procedureSection
   : procedureSectionHeader DOT_FS paragraphs
   ;

paragraphs
   : sentence* paragraph*
   ;

paragraph
   : paragraphName DOT_FS? (alteredGoTo | sentence*)
   ;

sentence
   : statement* DOT_FS
   ;

statement
   : acceptStatement | addStatement | alterStatement | callStatement | cancelStatement | closeStatement | computeStatement | continueStatement | deleteStatement | disableStatement | displayStatement | divideStatement | enableStatement | entryStatement | evaluateStatement | exhibitStatement | execCicsStatement | execSqlStatement | execSqlImsStatement | exitStatement | generateStatement | gobackStatement | goToStatement | ifStatement | initializeStatement | initiateStatement | inspectStatement | mergeStatement | moveStatement | multiplyStatement | nextSentenceStatement | openStatement | performStatement | purgeStatement | readStatement | receiveStatement | releaseStatement | returnStatement | rewriteStatement | searchStatement | sendStatement | setStatement | sortStatement | startStatement | stopStatement | stringStatement | subtractStatement | terminateStatement | unstringStatement | writeStatement
   ;

// accept statement

acceptStatement
   : ACCEPT identifier (acceptFromDateStatement | acceptFromEscapeKeyStatement | acceptFromMnemonicStatement | acceptMessageCountStatement)? onExceptionClause? notOnExceptionClause? END_ACCEPT?
   ;

acceptFromDateStatement
   : FROM (DATE YYYYMMDD? | DAY YYYYDDD? | DAY_OF_WEEK | TIME | TIMER | TODAYS_DATE MMDDYYYY? | TODAYS_NAME | YEAR | YYYYMMDD | YYYYDDD)
   ;

acceptFromMnemonicStatement
   : FROM mnemonicName
   ;

acceptFromEscapeKeyStatement
   : FROM ESCAPE KEY
   ;

acceptMessageCountStatement
   : MESSAGE? COUNT
   ;

roundable
   : identifier ROUNDED?
   ;

// add statement

addStatement
   : ADD (addToStatement | addToGivingStatement | addCorrespondingStatement) onSizeErrorPhrase? notOnSizeErrorPhrase? END_ADD?
   ;

addToStatement
   : addFrom+ TO addTo+
   ;

addTo
   : roundable
   ;

addToGivingStatement
   : addFrom+ (TO addToGiving+)? GIVING addGiving+
   ;

addCorrespondingStatement
   : (CORRESPONDING | CORR) identifier TO addTo
   ;

addFrom
   : identifier | literal
   ;

addToGiving
   : identifier | literal
   ;

addGiving
   : roundable
   ;

// altered go to statement

alteredGoTo
   : GO TO? DOT_FS
   ;

// alter statement

alterStatement
   : ALTER alterProceedTo+
   ;

alterProceedTo
   : procedureName TO (PROCEED TO)? procedureName
   ;

// call statement

callStatement
   : CALL (identifier | literal) callUsingPhrase? callGivingPhrase? onOverflowPhrase? onExceptionClause? notOnExceptionClause? END_CALL?
   ;

callUsingPhrase
   : USING callUsingParameter+
   ;

callUsingParameter
   : callByReferencePhrase | callByValuePhrase | callByContentPhrase
   ;

callByReferencePhrase
   : (BY? REFERENCE)? callByReference+
   ;

callByReference
   : ((ADDRESS OF | INTEGER | STRING)? identifier | literal | fileName) | OMITTED
   ;

callByValuePhrase
   : BY? VALUE callByValue+
   ;

callByValue
   : (ADDRESS OF | LENGTH OF?)? (identifier | literal)
   ;

callByContentPhrase
   : BY? CONTENT callByContent+
   ;

callByContent
   : (ADDRESS OF | LENGTH OF?)? identifier | literal | OMITTED
   ;

callGivingPhrase
   : (GIVING | RETURNING) identifier
   ;

// cancel statement

cancelStatement
   : CANCEL cancelCall+
   ;

cancelCall
   : libraryName (BYTITLE | BYFUNCTION) | identifier | literal
   ;

// close statement
closeStatement
   : CLOSE closeFile+
   ;

closeFile
   : fileName (closeReelUnitStatement | closeRelativeStatement | closePortFileIOStatement)?
   ;

closeReelUnitStatement
   : (REEL | UNIT) (FOR? REMOVAL)? (WITH? (NO REWIND | LOCK))?
   ;

closeRelativeStatement
   : WITH? (NO REWIND | LOCK)
   ;

closePortFileIOStatement
   : (WITH? NO WAIT | WITH WAIT) (USING closePortFileIOUsing+)?
   ;

closePortFileIOUsing
   : closePortFileIOUsingCloseDisposition | closePortFileIOUsingAssociatedData | closePortFileIOUsingAssociatedDataLength
   ;

closePortFileIOUsingCloseDisposition
   : CLOSE_DISPOSITION OF? (ABORT | ORDERLY)
   ;

closePortFileIOUsingAssociatedData
   : ASSOCIATED_DATA (identifier | integerLiteral)
   ;

closePortFileIOUsingAssociatedDataLength
   : ASSOCIATED_DATA_LENGTH OF? (identifier | integerLiteral)
   ;

// compute statement

computeStatement
   : COMPUTE computeStore+ (EQUALCHAR | EQUAL) arithmeticExpression onSizeErrorPhrase? notOnSizeErrorPhrase? END_COMPUTE?
   ;

computeStore
   : roundable
   ;

// continue statement

continueStatement
   : CONTINUE
   ;

// delete statement

deleteStatement
   : DELETE fileName RECORD? invalidKeyPhrase? notInvalidKeyPhrase? END_DELETE?
   ;

// disable statement

disableStatement
   : DISABLE (INPUT TERMINAL? | I_O TERMINAL | OUTPUT) cdName WITH? KEY (identifier | literal)
   ;

// display statement

displayStatement
   : DISPLAY displayOperand+ displayAt? displayUpon? displayWith? onExceptionClause? notOnExceptionClause? END_DISPLAY?
   ;

displayOperand
   : identifier | literal
   ;

displayAt
   : AT (identifier | literal)
   ;

displayUpon
   : UPON (mnemonicName | environmentName)
   ;

displayWith
   : WITH? NO ADVANCING
   ;

// divide statement

divideStatement
   : DIVIDE (identifier | literal) (divideIntoStatement | divideIntoGivingStatement | divideByGivingStatement) divideRemainder? onSizeErrorPhrase? notOnSizeErrorPhrase? END_DIVIDE?
   ;

divideIntoStatement
   : INTO divideInto+
   ;

divideIntoGivingStatement
   : INTO (identifier | literal) divideGivingPhrase?
   ;

divideByGivingStatement
   : BY (identifier | literal) divideGivingPhrase?
   ;

divideGivingPhrase
   : GIVING divideGiving+
   ;

divideInto
   : roundable
   ;

divideGiving
   : roundable
   ;

divideRemainder
   : REMAINDER identifier
   ;

// enable statement

enableStatement
   : ENABLE (INPUT TERMINAL? | I_O TERMINAL | OUTPUT) cdName WITH? KEY (literal | identifier)
   ;

// entry statement

entryStatement
   : ENTRY literal (USING identifier+)?
   ;

// evaluate statement

evaluateStatement
   : EVALUATE evaluateSelect evaluateAlsoSelect* evaluateWhenPhrase* evaluateWhenOther? END_EVALUATE?
   ;

evaluateSelect
   : identifier | literal | arithmeticExpression | condition
   ;

evaluateAlsoSelect
   : ALSO evaluateSelect
   ;

evaluateWhenPhrase
   : evaluateWhen+ statement*
   ;

evaluateWhen
   : WHEN evaluateCondition evaluateAlsoCondition*
   ;

evaluateCondition
   : ANY | NOT? evaluateValue evaluateThrough? | condition | booleanLiteral
   ;

evaluateThrough
   : (THROUGH | THRU) evaluateValue
   ;

evaluateAlsoCondition
   : ALSO evaluateCondition
   ;

evaluateWhenOther
   : WHEN OTHER statement*
   ;

evaluateValue
   : identifier | literal | arithmeticExpression
   ;

// exec cics statement

execCicsStatement
   : EXECCICSLINE+
   ;

// exec sql statement

execSqlStatement
   : EXECSQLLINE+
   ;

// exec sql ims statement

execSqlImsStatement
   : EXECSQLIMSLINE+
   ;

// exhibit statement

exhibitStatement
   : EXHIBIT NAMED? CHANGED? exhibitOperand+
   ;

exhibitOperand
   : identifier | literal
   ;

// exit statement

exitStatement
   : EXIT PROGRAM?
   ;

// generate statement

generateStatement
   : GENERATE reportName
   ;

// goback statement

gobackStatement
   : GOBACK
   ;

// goto statement

goToStatement
   : GO TO? (goToStatementSimple | goToDependingOnStatement)
   ;

goToStatementSimple
   : procedureName
   ;

goToDependingOnStatement
   : MORE_LABELS | procedureName+ (DEPENDING ON? identifier)?
   ;

// if statement

ifStatement
   : IF condition ifThen ifElse? END_IF?
   ;

ifThen
   : THEN? (NEXT SENTENCE | statement*)
   ;

ifElse
   : ELSE (NEXT SENTENCE | statement*)
   ;

// initialize statement

initializeStatement
   : INITIALIZE identifier+ initializeReplacingPhrase?
   ;

initializeReplacingPhrase
   : REPLACING initializeReplacingBy+
   ;

initializeReplacingBy
   : (ALPHABETIC | ALPHANUMERIC | ALPHANUMERIC_EDITED | NATIONAL | NATIONAL_EDITED | NUMERIC | NUMERIC_EDITED | DBCS | EGCS) DATA? BY (identifier | literal)
   ;

// initiate statement

initiateStatement
   : INITIATE reportName+
   ;

// inspect statement

inspectStatement
   : INSPECT identifier (inspectTallyingPhrase | inspectReplacingPhrase | inspectTallyingReplacingPhrase | inspectConvertingPhrase)
   ;

inspectTallyingPhrase
   : TALLYING inspectFor+
   ;

inspectReplacingPhrase
   : REPLACING (inspectReplacingCharacters | inspectReplacingAllLeadings)+
   ;

inspectTallyingReplacingPhrase
   : TALLYING inspectFor+ inspectReplacingPhrase+
   ;

inspectConvertingPhrase
   : CONVERTING (identifier | literal) inspectTo inspectBeforeAfter*
   ;

inspectFor
   : identifier FOR (inspectCharacters | inspectAllLeadings)+
   ;

inspectCharacters
   : (CHARACTER | CHARACTERS) inspectBeforeAfter*
   ;

inspectReplacingCharacters
   : (CHARACTER | CHARACTERS) inspectBy inspectBeforeAfter*
   ;

inspectAllLeadings
   : (ALL | LEADING) inspectAllLeading+
   ;

inspectReplacingAllLeadings
   : (ALL | LEADING | FIRST) inspectReplacingAllLeading+
   ;

inspectAllLeading
   : (identifier | literal) inspectBeforeAfter*
   ;

inspectReplacingAllLeading
   : (identifier | literal) inspectBy inspectBeforeAfter*
   ;

inspectBy
   : BY (identifier | literal)
   ;

inspectTo
   : TO (identifier | literal)
   ;

inspectBeforeAfter
   : (BEFORE | AFTER) INITIAL? (identifier | literal)
   ;

// merge statement

mergeStatement
   : MERGE fileName mergeOnKeyClause+ mergeCollatingSequencePhrase? mergeUsing* mergeOutputProcedurePhrase? mergeGivingPhrase*
   ;

mergeOnKeyClause
   : ON? (ASCENDING | DESCENDING) KEY? qualifiedDataName+
   ;

mergeCollatingSequencePhrase
   : COLLATING? SEQUENCE IS? alphabetName+ mergeCollatingAlphanumeric? mergeCollatingNational?
   ;

mergeCollatingAlphanumeric
   : FOR? ALPHANUMERIC IS alphabetName
   ;

mergeCollatingNational
   : FOR? NATIONAL IS? alphabetName
   ;

mergeUsing
   : USING fileName+
   ;

mergeOutputProcedurePhrase
   : OUTPUT PROCEDURE IS? procedureName mergeOutputThrough?
   ;

mergeOutputThrough
   : (THROUGH | THRU) procedureName
   ;

mergeGivingPhrase
   : GIVING mergeGiving+
   ;

mergeGiving
   : fileName (LOCK | SAVE | NO REWIND | CRUNCH | RELEASE | WITH REMOVE CRUNCH)?
   ;

// move statement

moveStatement
   : MOVE ALL? (moveToStatement | moveCorrespondingToStatement)
   ;

moveToStatement
   : moveToSendingArea TO identifier+
   ;

moveToSendingArea
   : identifier | literal
   ;

moveCorrespondingToStatement
   : (CORRESPONDING | CORR) moveCorrespondingToSendingArea TO identifier+
   ;

moveCorrespondingToSendingArea
   : identifier
   ;

// multiply statement

multiplyStatement
   : MULTIPLY (identifier | literal) BY (multiplyRegular | multiplyGiving) onSizeErrorPhrase? notOnSizeErrorPhrase? END_MULTIPLY?
   ;

multiplyRegular
   : multiplyRegularOperand+
   ;

multiplyRegularOperand
   : roundable
   ;

multiplyGiving
   : multiplyGivingOperand GIVING multiplyGivingResult+
   ;

multiplyGivingOperand
   : identifier | literal
   ;

multiplyGivingResult
   : roundable
   ;

// next sentence

nextSentenceStatement
   : NEXT SENTENCE
   ;

// open statement

openStatement
   : OPEN (openInputStatement | openOutputStatement | openIOStatement | openExtendStatement)+
   ;

openInputStatement
   : INPUT openInput+
   ;

openInput
   : fileName (REVERSED | WITH? NO REWIND)?
   ;

openOutputStatement
   : OUTPUT openOutput+
   ;

openOutput
   : fileName (WITH? NO REWIND)?
   ;

openIOStatement
   : I_O fileName+
   ;

openExtendStatement
   : EXTEND fileName+
   ;

// perform statement

performStatement
   : PERFORM (performInlineStatement | performProcedureStatement)
   ;

performInlineStatement
   : performType? statement* END_PERFORM
   ;

performProcedureStatement
   : procedureName ((THROUGH | THRU) procedureName)? performType?
   ;

performType
   : performTimes | performUntil | performVarying
   ;

performTimes
   : (identifier | integerLiteral) TIMES
   ;

performUntil
   : performTestClause? UNTIL condition
   ;

performVarying
   : performTestClause performVaryingClause | performVaryingClause performTestClause?
   ;

performVaryingClause
   : VARYING performVaryingPhrase performAfter*
   ;

performVaryingPhrase
   : (identifier | literal) performFrom performBy performUntil
   ;

performAfter
   : AFTER performVaryingPhrase
   ;

performFrom
   : FROM (identifier | literal | arithmeticExpression)
   ;

performBy
   : BY (identifier | literal | arithmeticExpression)
   ;

performTestClause
   : WITH? TEST (BEFORE | AFTER)
   ;

// purge statement

purgeStatement
   : PURGE cdName+
   ;

// read statement

readStatement
   : READ fileName NEXT? RECORD? readInto? readWith? readKey? invalidKeyPhrase? notInvalidKeyPhrase? atEndPhrase? notAtEndPhrase? END_READ?
   ;

readInto
   : INTO identifier
   ;

readWith
   : WITH? ((KEPT | NO) LOCK | WAIT)
   ;

readKey
   : KEY IS? qualifiedDataName
   ;

// receive statement

receiveStatement
   : RECEIVE (receiveFromStatement | receiveIntoStatement) onExceptionClause? notOnExceptionClause? END_RECEIVE?
   ;

receiveFromStatement
   : dataName FROM receiveFrom (receiveBefore | receiveWith | receiveThread | receiveSize | receiveStatus)*
   ;

receiveFrom
   : THREAD dataName | LAST THREAD | ANY THREAD
   ;

receiveIntoStatement
   : cdName (MESSAGE | SEGMENT) INTO? identifier receiveNoData? receiveWithData?
   ;

receiveNoData
   : NO DATA statement*
   ;

receiveWithData
   : WITH DATA statement*
   ;

receiveBefore
   : BEFORE TIME? (numericLiteral | identifier)
   ;

receiveWith
   : WITH? NO WAIT
   ;

receiveThread
   : THREAD IN? dataName
   ;

receiveSize
   : SIZE IN? (numericLiteral | identifier)
   ;

receiveStatus
   : STATUS IN? (identifier)
   ;

// release statement

releaseStatement
   : RELEASE recordName (FROM qualifiedDataName)?
   ;

// return statement

returnStatement
   : RETURN fileName RECORD? returnInto? atEndPhrase notAtEndPhrase? END_RETURN?
   ;

returnInto
   : INTO qualifiedDataName
   ;

// rewrite statement

rewriteStatement
   : REWRITE recordName rewriteFrom? invalidKeyPhrase? notInvalidKeyPhrase? END_REWRITE?
   ;

rewriteFrom
   : FROM identifier
   ;

// search statement

searchStatement
   : SEARCH ALL? qualifiedDataName searchVarying? atEndPhrase? searchWhen+ END_SEARCH?
   ;

searchVarying
   : VARYING qualifiedDataName
   ;

searchWhen
   : WHEN condition (NEXT SENTENCE | statement*)
   ;

// send statement

sendStatement
   : SEND (sendStatementSync | sendStatementAsync) onExceptionClause? notOnExceptionClause?
   ;

sendStatementSync
   : (identifier | literal) sendFromPhrase? sendWithPhrase? sendReplacingPhrase? sendAdvancingPhrase?
   ;

sendStatementAsync
   : TO (TOP | BOTTOM) identifier
   ;

sendFromPhrase
   : FROM identifier
   ;

sendWithPhrase
   : WITH (EGI | EMI | ESI | identifier)
   ;

sendReplacingPhrase
   : REPLACING LINE?
   ;

sendAdvancingPhrase
   : (BEFORE | AFTER) ADVANCING? (sendAdvancingPage | sendAdvancingLines | sendAdvancingMnemonic)
   ;

sendAdvancingPage
   : PAGE
   ;

sendAdvancingLines
   : (identifier | literal) (LINE | LINES)?
   ;

sendAdvancingMnemonic
   : mnemonicName
   ;

// set statement

setStatement
   : SET (setToStatement+ | setUpDownByStatement)
   ;

setToStatement
   : setTo+ TO setToValue+
   ;

setUpDownByStatement
   : setTo+ (UP BY | DOWN BY) setByValue
   ;

setTo
   : identifier
   ;

setToValue
   : ON | OFF | ENTRY (identifier | literal) | identifier | literal
   ;

setByValue
   : identifier | literal
   ;

// sort statement

sortStatement
   : SORT fileName sortOnKeyClause+ sortDuplicatesPhrase? sortCollatingSequencePhrase? sortInputProcedurePhrase? sortUsing* sortOutputProcedurePhrase? sortGivingPhrase*
   ;

sortOnKeyClause
   : ON? (ASCENDING | DESCENDING) KEY? qualifiedDataName+
   ;

sortDuplicatesPhrase
   : WITH? DUPLICATES IN? ORDER?
   ;

sortCollatingSequencePhrase
   : COLLATING? SEQUENCE IS? alphabetName+ sortCollatingAlphanumeric? sortCollatingNational?
   ;

sortCollatingAlphanumeric
   : FOR? ALPHANUMERIC IS alphabetName
   ;

sortCollatingNational
   : FOR? NATIONAL IS? alphabetName
   ;

sortInputProcedurePhrase
   : INPUT PROCEDURE IS? procedureName sortInputThrough?
   ;

sortInputThrough
   : (THROUGH | THRU) procedureName
   ;

sortUsing
   : USING fileName+
   ;

sortOutputProcedurePhrase
   : OUTPUT PROCEDURE IS? procedureName sortOutputThrough?
   ;

sortOutputThrough
   : (THROUGH | THRU) procedureName
   ;

sortGivingPhrase
   : GIVING sortGiving+
   ;

sortGiving
   : fileName (LOCK | SAVE | NO REWIND | CRUNCH | RELEASE | WITH REMOVE CRUNCH)?
   ;

// start statement

startStatement
   : START fileName startKey? invalidKeyPhrase? notInvalidKeyPhrase? END_START?
   ;

startKey
   : KEY IS? (EQUAL TO? | EQUALCHAR | GREATER THAN? | MORETHANCHAR | NOT LESS THAN? | NOT LESSTHANCHAR | GREATER THAN? OR EQUAL TO? | MORETHANOREQUAL) qualifiedDataName
   ;

// stop statement

stopStatement
   : STOP (RUN | literal | stopStatementGiving)
   ;

stopStatementGiving
   : RUN (GIVING | RETURNING) (identifier | integerLiteral)
   ;

// string statement

stringStatement
   : STRING stringSendingPhrase+ stringIntoPhrase stringWithPointerPhrase? onOverflowPhrase? notOnOverflowPhrase? END_STRING?
   ;

stringSendingPhrase
   : stringSending+ (stringDelimitedByPhrase | stringForPhrase)
   ;

stringSending
   : identifier | literal
   ;

stringDelimitedByPhrase
   : DELIMITED BY? (SIZE | identifier | literal)
   ;

stringForPhrase
   : FOR (identifier | literal)
   ;

stringIntoPhrase
   : INTO identifier
   ;

stringWithPointerPhrase
   : WITH? POINTER qualifiedDataName
   ;

// subtract statement

subtractStatement
   : SUBTRACT (subtractFromStatement | subtractFromGivingStatement | subtractCorrespondingStatement) onSizeErrorPhrase? notOnSizeErrorPhrase? END_SUBTRACT?
   ;

subtractFromStatement
   : subtractSubtrahend+ FROM subtractMinuend+
   ;

subtractFromGivingStatement
   : subtractSubtrahend+ FROM subtractMinuendGiving GIVING subtractGiving+
   ;

subtractCorrespondingStatement
   : (CORRESPONDING | CORR) qualifiedDataName FROM subtractMinuendCorresponding
   ;

subtractSubtrahend
   : identifier | literal
   ;

subtractMinuend
   : roundable
   ;

subtractMinuendGiving
   : identifier | literal
   ;

subtractGiving
   : roundable
   ;

subtractMinuendCorresponding
   : qualifiedDataName ROUNDED?
   ;

// terminate statement

terminateStatement
   : TERMINATE reportName
   ;

// unstring statement

unstringStatement
   : UNSTRING unstringSendingPhrase unstringIntoPhrase unstringWithPointerPhrase? unstringTallyingPhrase? onOverflowPhrase? notOnOverflowPhrase? END_UNSTRING?
   ;

unstringSendingPhrase
   : identifier (unstringDelimitedByPhrase unstringOrAllPhrase*)?
   ;

unstringDelimitedByPhrase
   : DELIMITED BY? ALL? (identifier | literal)
   ;

unstringOrAllPhrase
   : OR ALL? (identifier | literal)
   ;

unstringIntoPhrase
   : INTO unstringInto+
   ;

unstringInto
   : identifier unstringDelimiterIn? unstringCountIn?
   ;

unstringDelimiterIn
   : DELIMITER IN? identifier
   ;

unstringCountIn
   : COUNT IN? identifier
   ;

unstringWithPointerPhrase
   : WITH? POINTER qualifiedDataName
   ;

unstringTallyingPhrase
   : TALLYING IN? qualifiedDataName
   ;

// use statement

useStatement
   : USE (useAfterClause | useDebugClause)
   ;

useAfterClause
   : GLOBAL? AFTER STANDARD? (EXCEPTION | ERROR) PROCEDURE ON? useAfterOn
   ;

useAfterOn
   : INPUT | OUTPUT | I_O | EXTEND | fileName+
   ;

useDebugClause
   : FOR? DEBUGGING ON? useDebugOn+
   ;

useDebugOn
   : ALL PROCEDURES | ALL REFERENCES? OF? identifier | procedureName | fileName
   ;

// write statement

writeStatement
   : WRITE recordName writeFromPhrase? writeAdvancingPhrase? writeAtEndOfPagePhrase? writeNotAtEndOfPagePhrase? invalidKeyPhrase? notInvalidKeyPhrase? END_WRITE?
   ;

writeFromPhrase
   : FROM (identifier | literal)
   ;

writeAdvancingPhrase
   : (BEFORE | AFTER) ADVANCING? (writeAdvancingPage | writeAdvancingLines | writeAdvancingMnemonic)
   ;

writeAdvancingPage
   : PAGE
   ;

writeAdvancingLines
   : (identifier | literal) (LINE | LINES)?
   ;

writeAdvancingMnemonic
   : mnemonicName
   ;

writeAtEndOfPagePhrase
   : AT? (END_OF_PAGE | EOP) statement*
   ;

writeNotAtEndOfPagePhrase
   : NOT AT? (END_OF_PAGE | EOP) statement*
   ;

// statement phrases ----------------------------------

atEndPhrase
   : AT? END statement*
   ;

notAtEndPhrase
   : NOT AT? END statement*
   ;

invalidKeyPhrase
   : INVALID KEY? statement*
   ;

notInvalidKeyPhrase
   : NOT INVALID KEY? statement*
   ;

onOverflowPhrase
   : ON? OVERFLOW statement*
   ;

notOnOverflowPhrase
   : NOT ON? OVERFLOW statement*
   ;

onSizeErrorPhrase
   : ON? SIZE ERROR statement*
   ;

notOnSizeErrorPhrase
   : NOT ON? SIZE ERROR statement*
   ;

// statement clauses ----------------------------------

onExceptionClause
   : ON? EXCEPTION statement*
   ;

notOnExceptionClause
   : NOT ON? EXCEPTION statement*
   ;

// arithmetic expression ----------------------------------

arithmeticExpression
   : multDivs plusMinus*
   ;

plusMinus
   : (PLUSCHAR | MINUSCHAR) multDivs
   ;

multDivs
   : powers multDiv*
   ;

multDiv
   : (ASTERISKCHAR | SLASHCHAR) powers
   ;

powers
   : (PLUSCHAR | MINUSCHAR)? basis power*
   ;

power
   : DOUBLEASTERISKCHAR basis
   ;

basis
   : LPARENCHAR arithmeticExpression RPARENCHAR | identifier | literal
   ;

// condition ----------------------------------

condition
   : combinableCondition andOrCondition*
   ;

andOrCondition
   : (AND | OR) (combinableCondition | abbreviation+)
   ;

combinableCondition
   : NOT? simpleCondition
   ;

simpleCondition
   : LPARENCHAR condition RPARENCHAR | relationCondition | classCondition | conditionNameReference
   ;

classCondition
   : identifier IS? NOT? (NUMERIC | ALPHABETIC | ALPHABETIC_LOWER | ALPHABETIC_UPPER | DBCS | KANJI | className)
   ;

conditionNameReference
   : conditionName (inData* inFile? conditionNameSubscriptReference* | inMnemonic*)
   ;

conditionNameSubscriptReference
   : LPARENCHAR subscript+ RPARENCHAR
   ;

// relation ----------------------------------

relationCondition
   : relationSignCondition | relationArithmeticComparison | relationCombinedComparison
   ;

relationSignCondition
   : arithmeticExpression IS? NOT? (POSITIVE | NEGATIVE | ZERO)
   ;

relationArithmeticComparison
   : arithmeticExpression relationalOperator arithmeticExpression
   ;

relationCombinedComparison
   : arithmeticExpression relationalOperator LPARENCHAR relationCombinedCondition RPARENCHAR
   ;

relationCombinedCondition
   : arithmeticExpression ((AND | OR) arithmeticExpression)+
   ;

relationalOperator
   : (IS | ARE)? (NOT? (GREATER THAN? | MORETHANCHAR | LESS THAN? | LESSTHANCHAR | EQUAL TO? | EQUALCHAR) | NOTEQUALCHAR | GREATER THAN? OR EQUAL TO? | MORETHANOREQUAL | LESS THAN? OR EQUAL TO? | LESSTHANOREQUAL)
   ;

abbreviation
   : NOT? relationalOperator? (arithmeticExpression | LPARENCHAR arithmeticExpression abbreviation RPARENCHAR)
   ;

// identifier ----------------------------------

identifier
   : qualifiedDataName | tableCall | functionCall | specialRegister
   ;

tableCall
   : qualifiedDataName tableCallSubscripts* referenceModifier?
   ;

tableCallSubscripts
    : LPARENCHAR subscript+ RPARENCHAR
    ;

functionCall
   : FUNCTION functionName functionCallArguments* referenceModifier?
   ;

functionCallArguments
    : LPARENCHAR argument+ RPARENCHAR
    ;

referenceModifier
   : LPARENCHAR characterPosition COLONCHAR length? RPARENCHAR
   ;

characterPosition
   : arithmeticExpression
   ;

length
   : arithmeticExpression
   ;

subscript
   : ALL | integerLiteral | qualifiedDataName integerLiteral? | indexName integerLiteral? | arithmeticExpression
   ;

argument
   : literal | identifier | qualifiedDataName integerLiteral? | indexName integerLiteral? | arithmeticExpression
   ;

// qualified data name ----------------------------------

qualifiedDataName
   : qualifiedDataNameFormat1 | qualifiedDataNameFormat2 | qualifiedDataNameFormat3 | qualifiedDataNameFormat4
   ;

qualifiedDataNameFormat1
   : (dataName | conditionName) (qualifiedInData+ inFile? | inFile)?
   ;

qualifiedDataNameFormat2
   : paragraphName inSection
   ;

qualifiedDataNameFormat3
   : textName inLibrary
   ;

qualifiedDataNameFormat4
   : LINAGE_COUNTER inFile
   ;

qualifiedInData
   : inData | inTable
   ;

// in ----------------------------------

inData
   : (IN | OF) dataName
   ;

inFile
   : (IN | OF) fileName
   ;

inMnemonic
   : (IN | OF) mnemonicName
   ;

inSection
   : (IN | OF) sectionName
   ;

inLibrary
   : (IN | OF) libraryName
   ;

inTable
   : (IN | OF) tableCall
   ;

// names ----------------------------------

alphabetName
   : cobolWord
   ;

assignmentName
   : systemName
   ;

basisName
   : programName
   ;

cdName
   : cobolWord
   ;

className
   : cobolWord
   ;

computerName
   : systemName
   ;

conditionName
   : cobolWord
   ;

dataName
   : cobolWord
   ;

dataDescName
   : FILLER | CURSOR | dataName
   ;

environmentName
   : systemName
   ;

fileName
   : cobolWord
   ;

functionName
   : INTEGER | LENGTH | RANDOM | SUM | WHEN_COMPILED | cobolWord
   ;

indexName
   : cobolWord
   ;

languageName
   : systemName
   ;

libraryName
   : cobolWord
   ;

localName
   : cobolWord
   ;

mnemonicName
   : cobolWord
   ;

paragraphName
   : cobolWord | integerLiteral
   ;

procedureName
   : paragraphName inSection? | sectionName
   ;

programName
   : NONNUMERICLITERAL | cobolWord
   ;

recordName
   : qualifiedDataName
   ;

reportName
   : qualifiedDataName
   ;

routineName
   : cobolWord
   ;

screenName
   : cobolWord
   ;

sectionName
   : cobolWord | integerLiteral
   ;

systemName
   : cobolWord
   ;

symbolicCharacter
   : cobolWord
   ;

textName
   : cobolWord
   ;

// literal ----------------------------------

cobolWord
   : IDENTIFIER
   | ABORT | AS | ASCII | ASSOCIATED_DATA | ASSOCIATED_DATA_LENGTH | ATTRIBUTE | AUTO | AUTO_SKIP
   | BACKGROUND_COLOR | BACKGROUND_COLOUR | BEEP | BELL | BINARY | BIT | BLINK | BLOB | BOUNDS
   | CAPABLE | CCSVERSION | CHANGED | CHANNEL | CLOB | CLOSE_DISPOSITION | COBOL | COMMITMENT | CONTROL_POINT | CONVENTION | CRUNCH | CURSOR
   | DBCLOB | DEFAULT | DEFAULT_DISPLAY | DEFINITION | DFHRESP | DFHVALUE | DISK | DONTCARE | DOUBLE
   | EBCDIC | EMPTY_CHECK | ENTER | ENTRY_PROCEDURE | EOL | EOS | ERASE | ESCAPE | EVENT | EXCLUSIVE | EXPORT | EXTENDED
   | FOREGROUND_COLOR | FOREGROUND_COLOUR | FULL | FUNCTIONNAME | FUNCTION_POINTER
   | GRID
   | HIGHLIGHT
   | IMPLICIT | IMPORT | INTEGER
   | KEPT | KEYBOARD
   | LANGUAGE | LB | LD | LEFTLINE | LENGTH_CHECK | LIBACCESS | LIBPARAMETER | LIBRARY | LIST | LOCAL | LONG_DATE | LONG_TIME | LOWER | LOWLIGHT
   | MMDDYYYY
   | NAMED | NATIONAL | NATIONAL_EDITED | NETWORK | NO_ECHO | NUMERIC_DATE | NUMERIC_TIME
   | ODT | ORDERLY | OVERLINE | OWN
   | PASSWORD | PORT | PRINTER | PRIVATE | PROCESS | PROGRAM | PROMPT
   | READER | REAL | RECEIVED | RECURSIVE | REF | REMOTE | REMOVE | REQUIRED | REVERSE_VIDEO
   | SAVE | SECURE | SHARED | SHAREDBYALL | SHAREDBYRUNUNIT | SHARING | SHORT_DATE | SQL | SYMBOL
   | TASK | THREAD | THREAD_LOCAL | TIMER | TODAYS_DATE | TODAYS_NAME | TRUNCATED | TYPEDEF
   | UNDERLINE
   | VIRTUAL
   | WAIT
   | YEAR | YYYYMMDD | YYYYDDD
   | ZERO_FILL
   ;

literal
   : NONNUMERICLITERAL | figurativeConstant | numericLiteral | booleanLiteral | cicsDfhRespLiteral | cicsDfhValueLiteral
   ;

booleanLiteral
   : TRUE | FALSE
   ;

numericLiteral
   : NUMERICLITERAL | ZERO | integerLiteral
   ;

integerLiteral
   : INTEGERLITERAL | LEVEL_NUMBER_66 | LEVEL_NUMBER_77 | LEVEL_NUMBER_88
   ;

cicsDfhRespLiteral
   : DFHRESP LPARENCHAR (cobolWord | literal) RPARENCHAR
   ;

cicsDfhValueLiteral
   : DFHVALUE LPARENCHAR (cobolWord | literal) RPARENCHAR
   ;

// keywords ----------------------------------

figurativeConstant
   : ALL literal | HIGH_VALUE | HIGH_VALUES | LOW_VALUE | LOW_VALUES | NULL | NULLS | QUOTE | QUOTES | SPACE | SPACES | ZERO | ZEROS | ZEROES
   ;

specialRegister
   : ADDRESS OF identifier
   | DATE | DAY | DAY_OF_WEEK | DEBUG_CONTENTS | DEBUG_ITEM | DEBUG_LINE | DEBUG_NAME | DEBUG_SUB_1 | DEBUG_SUB_2 | DEBUG_SUB_3
   | LENGTH OF? identifier | LINAGE_COUNTER | LINE_COUNTER
   | PAGE_COUNTER
   | RETURN_CODE
   | SHIFT_IN | SHIFT_OUT | SORT_CONTROL | SORT_CORE_SIZE | SORT_FILE_SIZE | SORT_MESSAGE | SORT_MODE_SIZE | SORT_RETURN
   | TALLY | TIME
   | WHEN_COMPILED
   ;

// comment entry

commentEntry
   : COMMENTENTRYLINE+
   ;

// lexer rules --------------------------------------------------------------------------------

// keywords
ABORT : 'ABORT';
ACCEPT : 'ACCEPT';
ACCESS : 'ACCESS';
ADD : 'ADD';
ADDRESS : 'ADDRESS';
ADVANCING : 'ADVANCING';
AFTER : 'AFTER';
ALIGNED : 'ALIGNED';
ALL : 'ALL';
ALPHABET : 'ALPHABET';
ALPHABETIC : 'ALPHABETIC';
ALPHABETIC_LOWER : 'ALPHABETIC-LOWER';
ALPHABETIC_UPPER : 'ALPHABETIC-UPPER';
ALPHANUMERIC : 'ALPHANUMERIC';
ALPHANUMERIC_EDITED : 'ALPHANUMERIC-EDITED';
ALSO : 'ALSO';
ALTER : 'ALTER';
ALTERNATE : 'ALTERNATE';
AND : 'AND';
ANY : 'ANY';
ARE : 'ARE';
AREA : 'AREA';
AREAS : 'AREAS';
AS : 'AS';
ASCENDING : 'ASCENDING';
ASCII : 'ASCII';
ASSIGN : 'ASSIGN';
ASSOCIATED_DATA : 'ASSOCIATED-DATA';
ASSOCIATED_DATA_LENGTH : 'ASSOCIATED-DATA-LENGTH';
AT : 'AT';
ATTRIBUTE : 'ATTRIBUTE';
AUTHOR : 'AUTHOR';
AUTO : 'AUTO';
AUTO_SKIP : 'AUTO-SKIP';
BACKGROUND_COLOR : 'BACKGROUND-COLOR';
BACKGROUND_COLOUR : 'BACKGROUND-COLOUR';
BASIS : 'BASIS';
BEEP : 'BEEP';
BEFORE : 'BEFORE';
BEGINNING : 'BEGINNING';
BELL : 'BELL';
BINARY : 'BINARY';
BIT : 'BIT';
BLANK : 'BLANK';
BLINK : 'BLINK';
BLOB : 'BLOB';
BLOCK : 'BLOCK';
BOUNDS : 'BOUNDS';
BOTTOM : 'BOTTOM';
BY : 'BY';
BYFUNCTION : 'BYFUNCTION';
BYTITLE : 'BYTITLE';
CALL : 'CALL';
CANCEL : 'CANCEL';
CAPABLE : 'CAPABLE';
CCSVERSION : 'CCSVERSION';
CD : 'CD';
CF : 'CF';
CH : 'CH';
CHAINING : 'CHAINING';
CHANGED : 'CHANGED';
CHANNEL : 'CHANNEL';
CHARACTER : 'CHARACTER';
CHARACTERS : 'CHARACTERS';
CLASS : 'CLASS';
CLASS_ID : 'CLASS-ID';
CLOB : 'CLOB';
CLOCK_UNITS : 'CLOCK-UNITS';
CLOSE : 'CLOSE';
CLOSE_DISPOSITION : 'CLOSE-DISPOSITION';
COBOL : 'COBOL';
CODE : 'CODE';
CODE_SET : 'CODE-SET';
COLLATING : 'COLLATING';
COL : 'COL';
COLUMN : 'COLUMN';
COM_REG : 'COM-REG';
COMMA : 'COMMA';
COMMITMENT : 'COMMITMENT';
COMMON : 'COMMON';
COMMUNICATION : 'COMMUNICATION';
COMP : 'COMP';
COMP_1 : 'COMP-1';
COMP_2 : 'COMP-2';
COMP_3 : 'COMP-3';
COMP_4 : 'COMP-4';
COMP_5 : 'COMP-5';
COMPUTATIONAL : 'COMPUTATIONAL';
COMPUTATIONAL_1 : 'COMPUTATIONAL-1';
COMPUTATIONAL_2 : 'COMPUTATIONAL-2';
COMPUTATIONAL_3 : 'COMPUTATIONAL-3';
COMPUTATIONAL_4 : 'COMPUTATIONAL-4';
COMPUTATIONAL_5 : 'COMPUTATIONAL-5';
COMPUTE : 'COMPUTE';
CONFIGURATION : 'CONFIGURATION';
CONTAINS : 'CONTAINS';
CONTENT : 'CONTENT';
CONTINUE : 'CONTINUE';
CONTROL : 'CONTROL';
CONTROL_POINT : 'CONTROL-POINT';
CONTROLS : 'CONTROLS';
CONVENTION : 'CONVENTION';
CONVERTING : 'CONVERTING';
COPY : 'COPY';
CORR : 'CORR';
CORRESPONDING : 'CORRESPONDING';
COUNT : 'COUNT';
CRUNCH : 'CRUNCH';
CURRENCY : 'CURRENCY';
CURSOR : 'CURSOR';
DATA : 'DATA';
DATA_BASE : 'DATA-BASE';
DATE : 'DATE';
DATE_COMPILED : 'DATE-COMPILED';
DATE_WRITTEN : 'DATE-WRITTEN';
DAY : 'DAY';
DAY_OF_WEEK : 'DAY-OF-WEEK';
DBCS : 'DBCS';
DBCLOB : 'DBCLOB';
DE : 'DE';
DEBUG_CONTENTS : 'DEBUG-CONTENTS';
DEBUG_ITEM : 'DEBUG-ITEM';
DEBUG_LINE : 'DEBUG-LINE';
DEBUG_NAME : 'DEBUG-NAME';
DEBUG_SUB_1 : 'DEBUG-SUB-1';
DEBUG_SUB_2 : 'DEBUG-SUB-2';
DEBUG_SUB_3 : 'DEBUG-SUB-3';
DEBUGGING : 'DEBUGGING';
DECIMAL_POINT : 'DECIMAL-POINT';
DECLARATIVES : 'DECLARATIVES';
DEFAULT : 'DEFAULT';
DEFAULT_DISPLAY : 'DEFAULT-DISPLAY';
DEFINITION : 'DEFINITION';
DELETE : 'DELETE';
DELIMITED : 'DELIMITED';
DELIMITER : 'DELIMITER';
DEPENDING : 'DEPENDING';
DESCENDING : 'DESCENDING';
DESTINATION : 'DESTINATION';
DETAIL : 'DETAIL';
DFHRESP : 'DFHRESP';
DFHVALUE : 'DFHVALUE';
DISABLE : 'DISABLE';
DISK : 'DISK';
DISPLAY : 'DISPLAY';
DISPLAY_1 : 'DISPLAY-1';
DIVIDE : 'DIVIDE';
DIVISION : 'DIVISION';
DONTCARE : 'DONTCARE';
DOUBLE : 'DOUBLE';
DOWN : 'DOWN';
DUPLICATES : 'DUPLICATES';
DYNAMIC : 'DYNAMIC';
EBCDIC : 'EBCDIC';
EGCS : 'EGCS'; // EXTENSION
EGI : 'EGI';
ELSE : 'ELSE';
EMI : 'EMI';
EMPTY_CHECK : 'EMPTY-CHECK';
ENABLE : 'ENABLE';
END : 'END';
END_ACCEPT : 'END-ACCEPT';
END_ADD : 'END-ADD';
END_CALL : 'END-CALL';
END_COMPUTE : 'END-COMPUTE';
END_DELETE : 'END-DELETE';
END_DISPLAY : 'END-DISPLAY';
END_DIVIDE : 'END-DIVIDE';
END_EVALUATE : 'END-EVALUATE';
END_IF : 'END-IF';
END_MULTIPLY : 'END-MULTIPLY';
END_OF_PAGE : 'END-OF-PAGE';
END_PERFORM : 'END-PERFORM';
END_READ : 'END-READ';
END_RECEIVE : 'END-RECEIVE';
END_REMARKS : 'END-REMARKS';
END_RETURN : 'END-RETURN';
END_REWRITE : 'END-REWRITE';
END_SEARCH : 'END-SEARCH';
END_START : 'END-START';
END_STRING : 'END-STRING';
END_SUBTRACT : 'END-SUBTRACT';
END_UNSTRING : 'END-UNSTRING';
END_WRITE : 'END-WRITE';
ENDING : 'ENDING';
ENTER : 'ENTER';
ENTRY : 'ENTRY';
ENTRY_PROCEDURE : 'ENTRY-PROCEDURE';
ENVIRONMENT : 'ENVIRONMENT';
EOP : 'EOP';
EQUAL : 'EQUAL';
ERASE : 'ERASE';
ERROR : 'ERROR';
EOL : 'EOL';
EOS : 'EOS';
ESCAPE : 'ESCAPE';
ESI : 'ESI';
EVALUATE : 'EVALUATE';
EVENT : 'EVENT';
EVERY : 'EVERY';
EXCEPTION : 'EXCEPTION';
EXCLUSIVE : 'EXCLUSIVE';
EXHIBIT : 'EXHIBIT';
EXIT : 'EXIT';
EXPORT : 'EXPORT';
EXTEND : 'EXTEND';
EXTENDED : 'EXTENDED';
EXTERNAL : 'EXTERNAL';
FALSE : 'FALSE';
FD : 'FD';
FILE : 'FILE';
FILE_CONTROL : 'FILE-CONTROL';
FILLER : 'FILLER';
FINAL : 'FINAL';
FIRST : 'FIRST';
FOOTING : 'FOOTING';
FOR : 'FOR';
FOREGROUND_COLOR : 'FOREGROUND-COLOR';
FOREGROUND_COLOUR : 'FOREGROUND-COLOUR';
FROM : 'FROM';
FULL : 'FULL';
FUNCTION : 'FUNCTION';
FUNCTIONNAME : 'FUNCTIONNAME';
FUNCTION_POINTER : 'FUNCTION-POINTER';
GENERATE : 'GENERATE';
GOBACK : 'GOBACK';
GIVING : 'GIVING';
GLOBAL : 'GLOBAL';
GO : 'GO';
GREATER : 'GREATER';
GRID : 'GRID';
GROUP : 'GROUP';
HEADING : 'HEADING';
HIGHLIGHT : 'HIGHLIGHT';
HIGH_VALUE : 'HIGH-VALUE';
HIGH_VALUES : 'HIGH-VALUES';
I_O : 'I-O';
I_O_CONTROL : 'I-O-CONTROL';
ID : 'ID';
IDENTIFICATION : 'IDENTIFICATION';
IF : 'IF';
IMPLICIT : 'IMPLICIT';
IMPORT : 'IMPORT';
IN : 'IN';
INDEX : 'INDEX';
INDEXED : 'INDEXED';
INDICATE : 'INDICATE';
INITIAL : 'INITIAL';
INITIALIZE : 'INITIALIZE';
INITIATE : 'INITIATE';
INPUT : 'INPUT';
INPUT_OUTPUT : 'INPUT-OUTPUT';
INSPECT : 'INSPECT';
INSTALLATION : 'INSTALLATION';
INTEGER : 'INTEGER';
INTO : 'INTO';
INVALID : 'INVALID';
INVOKE : 'INVOKE';
IS : 'IS';
JUST : 'JUST';
JUSTIFIED : 'JUSTIFIED';
KANJI : 'KANJI';
KEPT : 'KEPT';
KEY : 'KEY';
KEYBOARD : 'KEYBOARD';
LABEL : 'LABEL';
LANGUAGE : 'LANGUAGE';
LAST : 'LAST';
LB : 'LB';
LD : 'LD';
LEADING : 'LEADING';
LEFT : 'LEFT';
LEFTLINE : 'LEFTLINE';
LENGTH : 'LENGTH';
LENGTH_CHECK : 'LENGTH-CHECK';
LESS : 'LESS';
LIBACCESS : 'LIBACCESS';
LIBPARAMETER : 'LIBPARAMETER';
LIBRARY : 'LIBRARY';
LIMIT : 'LIMIT';
LIMITS : 'LIMITS';
LINAGE : 'LINAGE';
LINAGE_COUNTER : 'LINAGE-COUNTER';
LINE : 'LINE';
LINES : 'LINES';
LINE_COUNTER : 'LINE-COUNTER';
LINKAGE : 'LINKAGE';
LIST : 'LIST';
LOCAL : 'LOCAL';
LOCAL_STORAGE : 'LOCAL-STORAGE';
LOCK : 'LOCK';
LONG_DATE : 'LONG-DATE';
LONG_TIME : 'LONG-TIME';
LOWER : 'LOWER';
LOWLIGHT : 'LOWLIGHT';
LOW_VALUE : 'LOW-VALUE';
LOW_VALUES : 'LOW-VALUES';
MEMORY : 'MEMORY';
MERGE : 'MERGE';
MESSAGE : 'MESSAGE';
MMDDYYYY : 'MMDDYYYY';
MODE : 'MODE';
MODULES : 'MODULES';
MORE_LABELS : 'MORE-LABELS';
MOVE : 'MOVE';
MULTIPLE : 'MULTIPLE';
MULTIPLY : 'MULTIPLY';
NAMED : 'NAMED';
NATIONAL : 'NATIONAL';
NATIONAL_EDITED : 'NATIONAL-EDITED';
NATIVE : 'NATIVE';
NEGATIVE : 'NEGATIVE';
NETWORK : 'NETWORK';
NEXT : 'NEXT';
NO : 'NO';
NO_ECHO : 'NO-ECHO';
NOT : 'NOT';
NULL : 'NULL';
NULLS : 'NULLS';
NUMBER : 'NUMBER';
NUMERIC : 'NUMERIC';
NUMERIC_DATE : 'NUMERIC-DATE';
NUMERIC_EDITED : 'NUMERIC-EDITED';
NUMERIC_TIME : 'NUMERIC-TIME';
OBJECT_COMPUTER : 'OBJECT-COMPUTER';
OCCURS : 'OCCURS';
ODT : 'ODT';
OF : 'OF';
OFF : 'OFF';
OMITTED : 'OMITTED';
ON : 'ON';
OPEN : 'OPEN';
OPTIONAL : 'OPTIONAL';
OR : 'OR';
ORDER : 'ORDER';
ORDERLY : 'ORDERLY';
ORGANIZATION : 'ORGANIZATION';
OTHER : 'OTHER';
OUTPUT : 'OUTPUT';
OVERFLOW : 'OVERFLOW';
OVERLINE : 'OVERLINE';
OWN : 'OWN';
PACKED_DECIMAL : 'PACKED-DECIMAL';
PADDING : 'PADDING';
PAGE : 'PAGE';
PAGE_COUNTER : 'PAGE-COUNTER';
PASSWORD : 'PASSWORD';
PERFORM : 'PERFORM';
PF : 'PF';
PH : 'PH';
PIC : 'PIC';
PICTURE : 'PICTURE';
PLUS : 'PLUS';
POINTER : 'POINTER';
POSITION : 'POSITION';
POSITIVE : 'POSITIVE';
PORT : 'PORT';
PRINTER : 'PRINTER';
PRINTING : 'PRINTING';
PRIVATE : 'PRIVATE';
PROCEDURE : 'PROCEDURE';
PROCEDURE_POINTER : 'PROCEDURE-POINTER';
PROCEDURES : 'PROCEDURES';
PROCEED : 'PROCEED';
PROCESS : 'PROCESS';
PROGRAM : 'PROGRAM';
PROGRAM_ID : 'PROGRAM-ID';
PROGRAM_LIBRARY : 'PROGRAM-LIBRARY';
PROMPT : 'PROMPT';
PURGE : 'PURGE';
QUEUE : 'QUEUE';
QUOTE : 'QUOTE';
QUOTES : 'QUOTES';
RANDOM : 'RANDOM';
READER : 'READER';
REMOTE : 'REMOTE';
RD : 'RD';
REAL : 'REAL';
READ : 'READ';
RECEIVE : 'RECEIVE';
RECEIVED : 'RECEIVED';
RECORD : 'RECORD';
RECORDING : 'RECORDING';
RECORDS : 'RECORDS';
RECURSIVE : 'RECURSIVE';
REDEFINES : 'REDEFINES';
REEL : 'REEL';
REF : 'REF';
REFERENCE : 'REFERENCE';
REFERENCES : 'REFERENCES';
RELATIVE : 'RELATIVE';
RELEASE : 'RELEASE';
REMAINDER : 'REMAINDER';
REMARKS : 'REMARKS';
REMOVAL : 'REMOVAL';
REMOVE : 'REMOVE';
RENAMES : 'RENAMES';
REPLACE : 'REPLACE';
REPLACING : 'REPLACING';
REPORT : 'REPORT';
REPORTING : 'REPORTING';
REPORTS : 'REPORTS';
REQUIRED : 'REQUIRED';
RERUN : 'RERUN';
RESERVE : 'RESERVE';
REVERSE_VIDEO : 'RESERVE-VIDEO';
RESET : 'RESET';
RETURN : 'RETURN';
RETURN_CODE : 'RETURN-CODE';
RETURNING : 'RETURNING';
REVERSED : 'REVERSED';
REWIND : 'REWIND';
REWRITE : 'REWRITE';
RF : 'RF';
RH : 'RH';
RIGHT : 'RIGHT';
ROUNDED : 'ROUNDED';
RUN : 'RUN';
SAME : 'SAME';
SAVE : 'SAVE';
SCREEN : 'SCREEN';
SD : 'SD';
SEARCH : 'SEARCH';
SECTION : 'SECTION';
SECURE : 'SECURE';
SECURITY : 'SECURITY';
SEGMENT : 'SEGMENT';
SEGMENT_LIMIT : 'SEGMENT-LIMIT';
SELECT : 'SELECT';
SEND : 'SEND';
SENTENCE : 'SENTENCE';
SEPARATE : 'SEPARATE';
SEQUENCE : 'SEQUENCE';
SEQUENTIAL : 'SEQUENTIAL';
SET : 'SET';
SHARED : 'SHARED';
SHAREDBYALL : 'SHAREDBYALL';
SHAREDBYRUNUNIT : 'SHAREDBYRUNUNIT';
SHARING : 'SHARING';
SHIFT_IN : 'SHIFT-IN';
SHIFT_OUT : 'SHIFT-OUT';
SHORT_DATE : 'SHORT-DATE';
SIGN : 'SIGN';
SIZE : 'SIZE';
SORT : 'SORT';
SORT_CONTROL : 'SORT-CONTROL';
SORT_CORE_SIZE : 'SORT-CORE-SIZE';
SORT_FILE_SIZE : 'SORT-FILE-SIZE';
SORT_MERGE : 'SORT-MERGE';
SORT_MESSAGE : 'SORT-MESSAGE';
SORT_MODE_SIZE : 'SORT-MODE-SIZE';
SORT_RETURN : 'SORT-RETURN';
SOURCE : 'SOURCE';
SOURCE_COMPUTER : 'SOURCE-COMPUTER';
SPACE : 'SPACE';
SPACES : 'SPACES';
SPECIAL_NAMES : 'SPECIAL-NAMES';
SQL : 'SQL';
STANDARD : 'STANDARD';
STANDARD_1 : 'STANDARD-1';
STANDARD_2 : 'STANDARD-2';
START : 'START';
STATUS : 'STATUS';
STOP : 'STOP';
STRING : 'STRING';
SUB_QUEUE_1 : 'SUB-QUEUE-1';
SUB_QUEUE_2 : 'SUB-QUEUE-2';
SUB_QUEUE_3 : 'SUB-QUEUE-3';
SUBTRACT : 'SUBTRACT';
SUM : 'SUM';
SUPPRESS : 'SUPPRESS';
SYMBOL : 'SYMBOL';
SYMBOLIC : 'SYMBOLIC';
SYNC : 'SYNC';
SYNCHRONIZED : 'SYNCHRONIZED';
TABLE : 'TABLE';
TALLY : 'TALLY';
TALLYING : 'TALLYING';
TASK : 'TASK';
TAPE : 'TAPE';
TERMINAL : 'TERMINAL';
TERMINATE : 'TERMINATE';
TEST : 'TEST';
TEXT : 'TEXT';
THAN : 'THAN';
THEN : 'THEN';
THREAD : 'THREAD';
THREAD_LOCAL : 'THREAD-LOCAL';
THROUGH : 'THROUGH';
THRU : 'THRU';
TIME : 'TIME';
TIMER : 'TIMER';
TIMES : 'TIMES';
TITLE : 'TITLE';
TO : 'TO';
TODAYS_DATE : 'TODAYS-DATE';
TODAYS_NAME : 'TODAYS-NAME';
TOP : 'TOP';
TRAILING : 'TRAILING';
TRUE : 'TRUE';
TRUNCATED : 'TRUNCATED';
TYPE : 'TYPE';
TYPEDEF : 'TYPEDEF';
UNDERLINE : 'UNDERLINE';
UNIT : 'UNIT';
UNSTRING : 'UNSTRING';
UNTIL : 'UNTIL';
UP : 'UP';
UPON : 'UPON';
USAGE : 'USAGE';
USE : 'USE';
USING : 'USING';
VALUE : 'VALUE';
VALUES : 'VALUES';
VARYING : 'VARYING';
VIRTUAL : 'VIRTUAL';
WAIT : 'WAIT';
WHEN : 'WHEN';
WHEN_COMPILED : 'WHEN-COMPILED';
WITH : 'WITH';
WORDS : 'WORDS';
WORKING_STORAGE : 'WORKING-STORAGE';
WRITE : 'WRITE';
YEAR : 'YEAR';
YYYYMMDD : 'YYYYMMDD';
YYYYDDD : 'YYYYDDD';
ZERO : 'ZERO';
ZERO_FILL : 'ZERO-FILL';
ZEROS : 'ZEROS';
ZEROES : 'ZEROES';

// symbols
AMPCHAR : '&';
ASTERISKCHAR : '*';
DOUBLEASTERISKCHAR : '**';
COLONCHAR : ':';
COMMACHAR : ',';
COMMENTENTRYTAG : '*>CE';
COMMENTTAG : '*>';
DOLLARCHAR : '$';
DOUBLEQUOTE : '"';
// period full stop
DOT_FS : '.' | '.' EOF;
EQUALCHAR : '=';
EXECCICSTAG : '*>EXECCICS';
EXECSQLTAG : '*>EXECSQL';
EXECSQLIMSTAG : '*>EXECSQLIMS';
LESSTHANCHAR : '<';
LESSTHANOREQUAL : '<=';
LPARENCHAR : '(';
MINUSCHAR : '-';
MORETHANCHAR : '>';
MORETHANOREQUAL : '>=';
NOTEQUALCHAR : '<>';
PLUSCHAR : '+';
SINGLEQUOTE : '\'';
RPARENCHAR : ')';
SLASHCHAR : '/';

// literals
NONNUMERICLITERAL : STRINGLITERAL | DBCSLITERAL | HEXNUMBER  | NULLTERMINATED;

fragment HEXNUMBER
    : 'X' '"' [0-9A-F]+ '"'
    | 'X' '\'' [0-9A-F]+ '\''
    ;

fragment NULLTERMINATED
    : 'Z' '"' (~["\n\r] | '""' | '\'')* '"'
    | 'Z' '\'' (~['\n\r] | '\'\'' | '"')* '\''
    ;

fragment STRINGLITERAL
    : '"' (~["\n\r] | '""' | '\'')* '"'
    | '\'' (~['\n\r] | '\'\'' | '"')* '\''
    ;

fragment DBCSLITERAL
    : [GN] '"' (~["\n\r] | '""' | '\'')* '"'
    | [GN] '\'' (~['\n\r] | '\'\'' | '"')* '\''
    ;

LEVEL_NUMBER_66 : '66';
LEVEL_NUMBER_77 : '77';
LEVEL_NUMBER_88 : '88';

INTEGERLITERAL : (PLUSCHAR | MINUSCHAR)? [0-9]+;

NUMERICLITERAL : (PLUSCHAR | MINUSCHAR)? [0-9]* ('.' | COMMACHAR) [0-9]+ ('E' (PLUSCHAR | MINUSCHAR)? [0-9]+)?;

IDENTIFIER : [A-Z0-9]+ ([-_]+ [A-Z0-9]+)*;

// whitespace, line breaks, comments, ...
// NEWLINE : '\r'? '\n' WS? '-'? -> channel(HIDDEN);
SEPARATOR : (', ' | ',''\r'?'\n') -> channel(HIDDEN);
NEWLINE : '\r'? '\n' -> channel(HIDDEN);
EXECCICSLINE : EXECCICSTAG WS ~('\n' | '\r' | '}')* ('\n' | '\r' | '}');
EXECSQLIMSLINE : EXECSQLIMSTAG WS ~('\n' | '\r' | '}')* ('\n' | '\r' | '}');
EXECSQLLINE : EXECSQLTAG WS ~('\n' | '\r' | '}')* ('\n' | '\r' | '}');
COMMENTENTRYLINE : COMMENTENTRYTAG WS ~('\n' | '\r')*;
COMMENTLINE : COMMENTTAG WS ~('\n' | '\r')* -> channel(HIDDEN);
WS : [ \t\f;]+ -> channel(HIDDEN);