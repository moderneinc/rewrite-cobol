/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
lexer grammar ControlMLexer;

UTF_8_BOM : '\uFEFF' -> skip;

WS : [ \t\f]+ -> channel(HIDDEN);
EOL : (CR? LF | FORM_FEED) -> channel(HIDDEN);

DEFINITION_START :  '|DEFINITION_START|' -> skip, pushMode(DEFINITION);
SCHEDULE_START : '|SCHEDULE_START|' -> skip, pushMode(SCHEDULE);
INPUT_START : '|INPUT_START|' -> skip, pushMode(INPUT);
OUTPUT_START : '|OUTPUT_START|' -> skip, pushMode(OUTPUT);
APP_FORM_START : '|APP_FORM_START|' -> skip, pushMode(APP_FORM);

SECTION_HEADER : '| =========================================================================== |';

fragment LF : '\n';
fragment CR : '\r';
fragment FORM_FEED : '\u000C';

VERTICAL_BAR_CHAR : '|';
ODAT : 'ODAT';

NAME : [@#a-zA-Z0-9-_:.*?]+;

mode DEFINITION;
DEFINITION_END : '|DEFINITION_END|' -> skip, popMode;
DEFINITION_WS : WS -> channel(HIDDEN);
DEFINITION_EOL : EOL -> channel(HIDDEN);

DESC : 'DESC' -> pushMode(CONSUME_LINE);

BROWSE_HEADER : '+---------------------------------- BROWSE -----------------------------------+';
DEFINITION_VERTICAL_BAR_CHAR : VERTICAL_BAR_CHAR -> type(VERTICAL_BAR_CHAR);

APPL : 'APPL';
AT : 'AT';
CTB_STEP : 'CTB STEP';
DFLT : 'DFLT';
DOCLIB : 'DOCLIB';
DOCMEM : 'DOCMEM';
GROUP : 'GROUP';
MEMNAME : 'MEMNAME';
MEMLIB : 'MEMLIB';
NJE_NODE : 'NJE NODE';
OWNER : 'OWNER';
OVERLIB : 'OVERLIB';
PREVENT_NCT2 : 'PREVENT-NCT2';
SCHENV : 'SCHENV';
SET_VAR : 'SET VAR';
STAT_CAL : 'STAT CAL';
SYSTEM_ID : 'SYSTEM ID';
TASKTYPE : 'TASKTYPE';
TYPE : 'TYPE';

EQUALS_CHAR : '=';

DEF_NAME : NAME -> type(NAME);

mode SCHEDULE;
SCHEDULE_END : '|SCHEDULE_END|' -> skip, popMode;
SCHEDULE_WS : WS -> channel(HIDDEN);
SCHEDULE_EOL : EOL -> channel(HIDDEN);

SCHEDULE_SECTION_HEADER : SECTION_HEADER -> type(SECTION_HEADER);
SCHEDULE_VERTICAL_BAR_CHAR : VERTICAL_BAR_CHAR -> type(VERTICAL_BAR_CHAR);

SCHEDULE_TEXT : ~[\r\n|]+;

mode INPUT;
INPUT_END : '|INPUT_END|' -> skip, popMode;
INPUT_WS : WS -> channel(HIDDEN);
INPUT_EOL : EOL -> channel(HIDDEN);

INPUT_SECTION_HEADER : SECTION_HEADER -> type(SECTION_HEADER);
INPUT_VERTICAL_BAR_CHAR : VERTICAL_BAR_CHAR -> type(VERTICAL_BAR_CHAR);

IN : 'IN';
INPUT_ODAT : ODAT -> type(ODAT);
INPUT_NAME : NAME -> type(NAME);

INPUT_TEXT : ~[\r\n|]+;

mode OUTPUT;
OUTPUT_END : '|OUTPUT_END|' -> skip, popMode;
OUTPUT_WS : WS -> channel(HIDDEN);
OUTPUT_EOL : EOL -> channel(HIDDEN);

OUTPUT_SECTION_HEADER : SECTION_HEADER -> type(SECTION_HEADER);
OUTPUT_VERTICAL_BAR_CHAR : VERTICAL_BAR_CHAR -> type(VERTICAL_BAR_CHAR);

OUT : 'OUT';
OUTPUT_ODAT : ODAT -> type(ODAT);
OUTPUT_NAME : NAME -> type(NAME);

OUTPUT_TEXT : ~[\r\n|]+;

mode APP_FORM;
APP_FORM_END : '|APP_FORM_END|' -> skip, popMode;
APP_FORM_WS : WS -> channel(HIDDEN);
APP_FORM_EOL : EOL -> channel(HIDDEN);

APP_FORM_SECTION_HEADER : SECTION_HEADER -> type(SECTION_HEADER);
APP_FORM_VERTICAL_BAR_CHAR : VERTICAL_BAR_CHAR -> type(VERTICAL_BAR_CHAR);

APP_FORM_ODAT : ODAT -> type(ODAT);

APP_FORM_NAME : NAME -> type(NAME);
APP_FORM_TEXT : ~[\r\n|]+;

mode CONSUME_LINE;
CONSUME_LINE_VERTICAL_BAR_CHAR : VERTICAL_BAR_CHAR -> type(VERTICAL_BAR_CHAR), popMode;
LINE_TEXT : ~[\r\n|]+;
