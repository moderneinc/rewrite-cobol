parser grammar DB2Parser;

options { caseInsensitive = true; tokenVocab=DB2Lexer; }

// DB2 for z/OS DDL. Every statement is modelled: there is no catch-all, so a statement this grammar
// cannot read is a syntax error rather than a node that says nothing.

compilationUnit
    : statement* EOF
    ;

statement
    : createTable
    | createIndex
    | createTablespace
    | createDatabase
    | createStogroup
    | createView
    | createAlias
    | createSynonym
    | createSequence
    | createRole
    | createAuxiliaryTable
    | createType
    | createVariable
    | createMask
    | createPermission
    | createTrustedContext
    | createTrigger
    | createProcedure
    | createFunction
    | declareGlobalTemporaryTable
    | alterTable
    | alterTablespace
    | alterIndex
    | alterDatabase
    | alterStogroup
    | alterSequence
    | alterView
    | alterProcedure
    | alterFunction
    | alterTrigger
    | alterMask
    | alterPermission
    | alterTrustedContext
    | dropStatement
    | grantStatement
    | revokeStatement
    | commentStatement
    | labelStatement
    | renameStatement
    | setStatement
    | commitStatement
    | rollbackStatement
    | savepointStatement
    | releaseSavepointStatement
    | lockStatement
    | insertStatement
    | queryStatement
    | terminator
    ;

// A query standing on its own. The .sql files next to a schema are usually catalog queries.
queryStatement
    : queryExpression end
    ;

// A semicolon of its own. DDL scripts written by a generator often end with one.
terminator
    : SEMI
    ;

//
// CREATE TABLE
//

createTable
    : CREATE TABLE qualifiedName tableContents tableOption* end
    ;

// A table is defined by its columns, by another table, or by a query.
tableContents
    : LPAREN tableElement (COMMA tableElement)* RPAREN
    | LIKE qualifiedName copyOption*
    | AS LPAREN queryExpression RPAREN copyOption*
    ;

copyOption
    : (INCLUDING | EXCLUDING) nonReserved+
    | WITH NO? DATA
    | nonReserved+
    ;

tableElement
    : tableConstraint
    | periodDefinition
    | columnDefinition
    ;

columnDefinition
    : identifier dataType columnAttribute*
    ;

columnAttribute
    : NOT NULL
    | NULL
    | DEFAULT defaultValue?
    | WITH DEFAULT defaultValue?
    | GENERATED (ALWAYS | BY DEFAULT) generatedAs?
    | (CONSTRAINT identifier)? PRIMARY KEY
    | (CONSTRAINT identifier)? UNIQUE
    | (CONSTRAINT identifier)? CHECK LPAREN searchCondition RPAREN
    | (CONSTRAINT identifier)? REFERENCES qualifiedName columnList? referentialAction*
    | FOR (SBCS | MIXED | BIT) DATA
    | CCSID identifier
    | FIELDPROC qualifiedName constant*
    | INLINE LENGTH NUMBER
    | IMPLICITLY HIDDEN_KW
    | NOT? VOLATILE
    | AS SECURITY LABEL
    | identifier
    ;

// The identity and row-change clauses a generated column carries.
generatedAs
    : AS IDENTITY (LPAREN sequenceOption (COMMA? sequenceOption)* RPAREN)?
    | AS LPAREN expression RPAREN
    | AS ROW (BEGIN | END | CHANGE TIMESTAMP)
    | AS TRANSACTION START ID
    ;

defaultValue
    : constant
    | USER
    | CURRENT SQLID
    | NULL
    | qualifiedName LPAREN (expression (COMMA expression)*)? RPAREN
    ;

// A temporal table's SYSTEM_TIME or BUSINESS_TIME period.
periodDefinition
    : PERIOD identifier LPAREN identifier COMMA identifier RPAREN
    | PERIOD FOR identifier LPAREN identifier COMMA identifier RPAREN
    ;

tableConstraint
    : (CONSTRAINT identifier)? constraintBody
    ;

constraintBody
    : PRIMARY KEY columnList constraintOption*
    | FOREIGN KEY identifier? columnList REFERENCES qualifiedName columnList? referentialAction* constraintOption*
    | UNIQUE (WHERE NOT NULL)? columnList constraintOption*
    | CHECK LPAREN searchCondition RPAREN constraintOption*
    ;

referentialAction
    : ON DELETE (RESTRICT | CASCADE | NO ACTION | SET NULL)
    | ON UPDATE (RESTRICT | NO ACTION)
    | NOT? ENFORCED
    | ENABLE QUERY OPTIMIZATION
    ;

constraintOption
    : NOT? ENFORCED
    | ENABLE QUERY OPTIMIZATION
    ;

columnList
    : LPAREN identifier (COMMA identifier)* RPAREN
    ;

// What follows the table body: where it lives, how it is encoded, and how DB2 should treat it.
tableOption
    : IN qualifiedName
    | IN DATABASE identifier
    | PARTITION BY (RANGE | SIZE)? (LPAREN partitionKey (COMMA partitionKey)* RPAREN)? partitionClause*
    | ORGANIZE BY (HASH LPAREN identifier (COMMA identifier)* RPAREN hashSpace? | KEY? SEQUENCE)
    | CCSID identifier
    | VALIDPROC qualifiedName
    | EDITPROC qualifiedName (WITH ROW ATTRIBUTES)?
    | AUDIT (NONE | CHANGES | ALL)
    | DATA CAPTURE (NONE | CHANGES)
    | NOT? VOLATILE (CARDINALITY)?
    | CARDINALITY
    | APPEND (YES | NO)
    | WITH RESTRICT ON DROP
    | OBID NUMBER
    | NOT? LOGGED
    | storageOption
    | nonReserved
    ;

hashSpace
    : HASH SPACE NUMBER identifier?
    ;

partitionKey
    : identifier (ASC | DESC)?
    ;

partitionClause
    : LPAREN partitionSpec (COMMA partitionSpec)* RPAREN
    | NUMPARTS NUMBER
    ;

partitionSpec
    : PARTITION NUMBER (ENDING AT? LPAREN constant (COMMA constant)* RPAREN INCLUSIVE?)? storageOption*
    ;

//
// CREATE INDEX
//

createIndex
    : CREATE indexModifier* INDEX qualifiedName ON qualifiedName
      (LPAREN indexKey (COMMA indexKey)* RPAREN | XMLPATTERN STRING)
      indexOption* end
    ;

indexModifier
    : UNIQUE (WHERE NOT NULL)?
    | TYPE NUMBER
    ;

indexKey
    : (identifier | expression) (ASC | DESC | RANDOM)?
    ;

indexOption
    : NOT? CLUSTER
    | NOT? PADDED
    | (INCLUDE | EXCLUDE) NULL KEYS
    | INCLUDE columnList
    | PARTITIONED
    | PARTITION BY (RANGE | SIZE)? partitionClause*
    | GENERATE KEY? USING (XMLPATTERN | expression) STRING?
    | BUFFERPOOL identifier
    | CLOSE (YES | NO)
    | COPY (YES | NO)
    | DEFER (YES | NO)
    | DEFINE (YES | NO)
    | COMPRESS (YES | NO)
    | PIECESIZE NUMBER identifier?
    | storageOption
    | nonReserved
    ;

//
// The other objects a schema is built from
//

createTablespace
    : CREATE (LOB | LARGE)? TABLESPACE identifier (IN identifier)? tablespaceOption* end
    ;

tablespaceOption
    : USING (STOGROUP identifier | VCAT identifier)
    | SEGSIZE NUMBER
    | NUMPARTS NUMBER partitionClause*
    | MAXPARTITIONS NUMBER
    | DSSIZE NUMBER identifier?
    | LOCKSIZE (ANY | TABLESPACE | TABLE | PAGE | ROW | LOB)
    | LOCKMAX (SYSTEM | NUMBER)
    | BUFFERPOOL identifier
    | CCSID identifier
    | CLOSE (YES | NO)
    | COMPRESS (YES | NO)
    | DEFINE (YES | NO)
    | TRACKMOD (YES | NO)
    | LOGGED | NOT LOGGED
    | MEMBER CLUSTER
    | PAGENUM (ABSOLUTE | RELATIVE)
    | MAXROWS NUMBER
    | storageOption
    | nonReserved
    ;

createDatabase
    : CREATE DATABASE identifier databaseOption* end
    ;

databaseOption
    : (AS WORKFILE (FOR identifier)?)
    | BUFFERPOOL identifier
    | INDEXBP identifier
    | STOGROUP identifier
    | CCSID identifier
    | nonReserved
    ;

createStogroup
    : CREATE STOGROUP identifier stogroupOption* end
    ;

stogroupOption
    : VOLUMES LPAREN (STRING | identifier) (COMMA (STRING | identifier))* RPAREN
    | VCAT identifier
    | DATACLAS identifier
    | MGMTCLAS identifier
    | STORCLAS identifier
    | nonReserved
    ;

createView
    : CREATE VIEW qualifiedName columnList? AS queryExpression viewOption* end
    ;

viewOption
    : WITH (CASCADED | LOCAL)? CHECK OPTION
    ;

createAlias
    : CREATE (PUBLIC)? ALIAS qualifiedName FOR (TABLE | SEQUENCE)? qualifiedName end
    ;

createSynonym
    : CREATE SYNONYM identifier FOR qualifiedName end
    ;

createSequence
    : CREATE SEQUENCE qualifiedName (AS dataType)? sequenceOption* end
    ;

sequenceOption
    : START WITH signedNumber
    | INCREMENT BY signedNumber
    | (MINVALUE signedNumber | NO MINVALUE)
    | (MAXVALUE signedNumber | NO MAXVALUE)
    | (CYCLE | NO CYCLE)
    | (CACHE NUMBER | NO CACHE)
    | (ORDER | NO ORDER)
    | nonReserved
    ;

createRole
    : CREATE ROLE identifier end
    ;

createAuxiliaryTable
    : CREATE (AUX | AUXILIARY) TABLE qualifiedName (IN qualifiedName)?
      STORES qualifiedName (APPEND (YES | NO))? (COLUMN identifier)? (PART NUMBER)? end
    ;

createType
    : CREATE (DISTINCT)? TYPE qualifiedName AS dataType (WITH COMPARISONS)? end
    ;

createVariable
    : CREATE VARIABLE qualifiedName dataType (DEFAULT defaultValue)? end
    ;

createMask
    : CREATE MASK qualifiedName ON qualifiedName (AS identifier)?
      FOR COLUMN identifier RETURN caseExpression (ENABLE | DISABLE)? end
    ;

createPermission
    : CREATE PERMISSION qualifiedName ON qualifiedName (AS identifier)?
      FOR ROW ACCESS CONTROL? USING? WHEN LPAREN searchCondition RPAREN
      (ENFORCED FOR ALL ACCESS)? (ENABLE | DISABLE)? end
    ;

createTrustedContext
    : CREATE TRUSTED CONTEXT identifier BASED UPON CONNECTION USING SYSTEM AUTHID identifier
      trustedContextOption* end
    ;

trustedContextOption
    : ATTRIBUTES LPAREN trustedAttribute (COMMA trustedAttribute)* RPAREN
    | (ENABLE | DISABLE)
    | NO? DEFAULT ROLE identifier?
    | WITH USE FOR trustedUser (COMMA trustedUser)*
    | nonReserved
    ;

trustedAttribute
    : nonReserved (EQ | ) STRING
    ;

trustedUser
    : (identifier | PUBLIC) (ROLE identifier)? (WITH | WITHOUT)? AUTHENTICATION?
    ;

caseExpression
    : CASE caseWhen+ (ELSE expression)? END
    | expression
    ;

declareGlobalTemporaryTable
    : DECLARE GLOBAL TEMPORARY TABLE qualifiedName tableContents tableOption* end
    ;

//
// The routine objects. Their bodies are SQL PL, which is a language of its own; the header is
// modelled and the body kept as the statements it is built from.
//

createTrigger
    : CREATE TRIGGER qualifiedName (NO CASCADE)? (BEFORE | AFTER | INSTEAD OF)
      triggerEvent (OR triggerEvent)* ON qualifiedName
      triggerCorrelation* triggerGranularity? (compoundStatement | triggeredStatement) end
    ;

triggerEvent
    : INSERT
    | DELETE
    | UPDATE (OF identifier (COMMA identifier)*)?
    ;

triggerCorrelation
    : REFERENCING (OLD | NEW | OLD_TABLE | NEW_TABLE) ROW? AS? identifier
    ;

triggerGranularity
    : FOR EACH (ROW | STATEMENT) (MODE DB2SQL)?
    ;

createProcedure
    : CREATE (OR REPLACE)? PROCEDURE qualifiedName
      (LPAREN (routineParameter (COMMA routineParameter)*)? RPAREN)?
      routineClause* compoundStatement? end
    ;

createFunction
    : CREATE (OR REPLACE)? FUNCTION qualifiedName
      LPAREN (routineParameter (COMMA routineParameter)*)? RPAREN
      routineClause* compoundStatement? end
    ;

routineParameter
    : (IN | OUT | INOUT)? identifier? dataType
    ;

routineClause
    : RETURNS (dataType | TABLE LPAREN routineParameter (COMMA routineParameter)* RPAREN)
    | LANGUAGE identifier
    | EXTERNAL (NAME (STRING | identifier))?
    | PARAMETER STYLE identifier
    | SPECIFIC qualifiedName
    | (NOT)? DETERMINISTIC
    | (CONTAINS SQL | READS SQL DATA | MODIFIES SQL DATA | NO SQL)
    | (CALLED | RETURNS NULL) ON NULL INPUT
    | DYNAMIC? RESULT SETS NUMBER
    | (FENCED | NOT FENCED)
    | COLLID identifier
    | WLM ENVIRONMENT identifier
    | RUN OPTIONS STRING
    | (INHERIT | DEFAULT) SPECIAL REGISTERS
    | (ALLOW | DISALLOW | DISABLE | ENABLE) DEBUG MODE
    | ASUTIME (NO LIMIT | LIMIT NUMBER)
    | STAY RESIDENT (YES | NO)
    | PROGRAM TYPE (MAIN | SUB)
    | SECURITY (DB2 | USER | DEFINER)
    | (COMMIT ON RETURN (YES | NO))
    | PACKAGE PATH identifier
    | QUALIFIER identifier
    | nonReserved
    ;

// SQL PL is a procedural language of its own, not DDL. Its block structure is modelled so that a
// nested BEGIN, or an END that closes a control flow statement rather than the block, does not end
// the routine early; the statements inside it are not.
compoundStatement
    : BEGIN ATOMIC? bodyItem* END identifier?
    ;

bodyItem
    : compoundStatement
    | END (IF | WHILE | FOR | CASE | LOOP | REPEAT)
    | ~(BEGIN | END)
    ;

triggeredStatement
    : bodyItem+
    ;

//
// ALTER and DROP
//

alterTable
    : ALTER TABLE qualifiedName alterTableAction+ end
    ;

alterTableAction
    : ADD COLUMN? columnDefinition
    | ADD? tableConstraint
    | ADD periodDefinition
    | ADD PARTITION partitionSpec?
    | ADD (VERSIONING USE HISTORY TABLE qualifiedName)
    | ALTER COLUMN? identifier alterColumnAction
    | ALTER PARTITION NUMBER partitionSpec
    | DROP COLUMN? identifier (CASCADE | RESTRICT)?
    | DROP (PRIMARY KEY | FOREIGN KEY identifier | UNIQUE identifier | CHECK identifier | CONSTRAINT identifier)
    | DROP VERSIONING
    | RENAME COLUMN? identifier TO identifier
    | ROTATE PARTITION (FIRST | NUMBER) TO LAST ENDING AT? LPAREN constant RPAREN RESET?
    | tableOption
    ;

alterColumnAction
    : SET? DATA TYPE dataType
    | SET DEFAULT defaultValue?
    | DROP DEFAULT
    | SET NOT NULL
    | DROP NOT NULL
    | SET GENERATED (ALWAYS | BY DEFAULT) generatedAs?
    | DROP IDENTITY
    | SET INLINE LENGTH NUMBER
    | sequenceOption+
    ;

// Each ALTER takes the option shapes its matching CREATE does.
alterTablespace
    : ALTER (LOB | LARGE)? TABLESPACE qualifiedName (PART NUMBER)? tablespaceOption* end
    ;

alterIndex
    : ALTER INDEX qualifiedName (REGENERATE | indexOption*) end
    ;

alterDatabase
    : ALTER DATABASE identifier databaseOption* end
    ;

alterStogroup
    : ALTER STOGROUP identifier alterStogroupAction* end
    ;

alterSequence
    : ALTER SEQUENCE qualifiedName (RESTART (WITH signedNumber)? | sequenceOption)* end
    ;

alterView
    : ALTER VIEW qualifiedName REGENERATE end
    ;

alterProcedure
    : ALTER PROCEDURE qualifiedName (LPAREN (routineParameter (COMMA routineParameter)*)? RPAREN)?
      routineClause* compoundStatement? end
    ;

alterFunction
    : ALTER FUNCTION qualifiedName (LPAREN (routineParameter (COMMA routineParameter)*)? RPAREN)?
      routineClause* compoundStatement? end
    ;

alterTrigger
    : ALTER TRIGGER qualifiedName NOT? SECURED end
    ;

alterMask
    : ALTER MASK qualifiedName (ENABLE | DISABLE) end
    ;

alterPermission
    : ALTER PERMISSION qualifiedName (ENABLE | DISABLE) end
    ;

alterTrustedContext
    : ALTER TRUSTED CONTEXT identifier trustedContextOption* end
    ;

alterStogroupAction
    : (ADD | REMOVE) VOLUMES LPAREN (STRING | identifier) (COMMA (STRING | identifier))* RPAREN
    | stogroupOption
    ;

dropStatement
    : DROP droppedObject (RESTRICT | CASCADE)? end
    ;

droppedObject
    : TABLE qualifiedName
    | (LOB | LARGE)? TABLESPACE qualifiedName
    | INDEX qualifiedName
    | DATABASE identifier
    | STOGROUP identifier
    | VIEW qualifiedName
    | (PUBLIC)? ALIAS qualifiedName
    | SYNONYM identifier
    | SEQUENCE qualifiedName
    | ROLE identifier
    | TRIGGER qualifiedName
    | (PROCEDURE | FUNCTION) qualifiedName (LPAREN (dataType (COMMA dataType)*)? RPAREN)?
    | SPECIFIC (PROCEDURE | FUNCTION) qualifiedName
    | (DISTINCT)? TYPE qualifiedName
    | VARIABLE qualifiedName
    | (MASK | PERMISSION) qualifiedName
    | TRUSTED CONTEXT identifier
    | (AUX | AUXILIARY) TABLE qualifiedName
    | PACKAGE qualifiedName
    ;

//
// Authorisation and documentation
//

// A package privilege is granted IN a collection where every other privilege is granted ON an
// object: GRANT CREATE IN COLLECTION GVBCOLL TO GVBUSER.
grantStatement
    : GRANT USE OF privilegeObject qualifiedNameList TO grantee (COMMA grantee)* end
    | GRANT privilege (COMMA privilege)* (ON | IN) privilegeObject? qualifiedNameList
      TO grantee (COMMA grantee)* (WITH GRANT OPTION)? end
    | GRANT privilege (COMMA privilege)* TO grantee (COMMA grantee)* (WITH ADMIN OPTION)? end
    ;

revokeStatement
    : REVOKE USE OF privilegeObject qualifiedNameList FROM grantee (COMMA grantee)* end
    | REVOKE privilege (COMMA privilege)* (ON | IN) privilegeObject? qualifiedNameList
      FROM grantee (COMMA grantee)* (BY ALL)? (RESTRICT | CASCADE)? end
    | REVOKE privilege (COMMA privilege)* FROM grantee (COMMA grantee)* (BY ALL)? end
    ;

privilege
    : ALL PRIVILEGES?
    | SELECT | INSERT | UPDATE columnList? | DELETE | REFERENCES columnList?
    | ALTER | INDEX | TRIGGER | EXECUTE | USAGE | LOAD | UNLOAD | CREATE | DROP
    | USE OF
    | identifier
    ;

privilegeObject
    : TABLE | (LOB | LARGE)? TABLESPACE | DATABASE | STOGROUP | SCHEMA | SEQUENCE
    | (PROCEDURE | FUNCTION) | (DISTINCT)? TYPE | VARIABLE | PACKAGE | COLLECTION | PLAN
    | BUFFERPOOL | ROLE | SYSTEM
    ;

qualifiedNameList
    : qualifiedName (COMMA qualifiedName)*
    ;

grantee
    : PUBLIC (AT ALL)?
    | ROLE identifier
    | USER identifier
    | identifier
    ;

commentStatement
    : COMMENT ON commentTarget IS STRING end
    ;

commentTarget
    : (TABLE | VIEW | ALIAS | INDEX | TRIGGER | SEQUENCE | ROLE | VARIABLE | PACKAGE)? qualifiedName
    | COLUMN qualifiedName DOT identifier
    | (DISTINCT)? TYPE qualifiedName
    | (PROCEDURE | FUNCTION) qualifiedName
    ;

labelStatement
    : LABEL ON labelTarget IS STRING end
    ;

labelTarget
    : (TABLE | ALIAS)? qualifiedName
    | COLUMN qualifiedName DOT identifier
    ;

renameStatement
    : RENAME TABLE? qualifiedName TO identifier end
    | RENAME INDEX qualifiedName TO identifier end
    ;

//
// Session and transaction
//

setStatement
    : SET specialRegister EQ? expression end
    ;

specialRegister
    : CURRENT? nonReserved+
    | CURRENT SQLID
    | CURRENT SCHEMA
    | CURRENT PATH
    | PATH
    | SCHEMA
    ;

commitStatement
    : COMMIT WORK? end
    ;

rollbackStatement
    : ROLLBACK WORK? (TO SAVEPOINT identifier?)? end
    ;

savepointStatement
    : SAVEPOINT identifier ON ROLLBACK RETAIN CURSORS? end
    ;

releaseSavepointStatement
    : RELEASE TO? SAVEPOINT identifier end
    ;

lockStatement
    : LOCK TABLE qualifiedName (PART NUMBER)? IN (SHARE | EXCLUSIVE) MODE end
    ;

insertStatement
    : INSERT INTO qualifiedName columnList?
      (VALUES valuesRow (COMMA valuesRow)* | queryExpression) end
    ;

valuesRow
    : LPAREN expression (COMMA expression)* RPAREN
    | expression
    ;

//
// Queries. A view and a materialized query table are defined by one, and the relationship graph
// wants the tables it names.
//

queryExpression
    : querySpecification (setOperator querySpecification)*
    ;

setOperator
    : (UNION | EXCEPT | INTERSECT) ALL?
    ;

querySpecification
    : SELECT (ALL | DISTINCT)? selectList
      FROM tableReference (COMMA tableReference)*
      (WHERE searchCondition)?
      (GROUP BY expression (COMMA expression)*)?
      (HAVING searchCondition)?
      (ORDER BY sortKey (COMMA sortKey)*)?
      (FETCH FIRST NUMBER? (ROW | ROWS) ONLY)?
    | LPAREN queryExpression RPAREN
    ;

selectList
    : STAR
    | selectItem (COMMA selectItem)*
    ;

selectItem
    : expression (AS? identifier)?
    | qualifiedName DOT STAR
    ;

tableReference
    : qualifiedName (AS? identifier)?
    | LPAREN queryExpression RPAREN (AS? identifier)?
    | tableReference joinType? JOIN tableReference ON searchCondition
    ;

joinType
    : INNER
    | (LEFT | RIGHT | FULL) OUTER?
    ;

sortKey
    : expression (ASC | DESC)?
    ;

//
// Conditions and expressions
//

searchCondition
    : searchCondition (AND | OR) searchCondition
    | NOT searchCondition
    | LPAREN searchCondition RPAREN
    | predicate
    ;

predicate
    : expression comparisonOperator expression
    | expression NOT? BETWEEN expression AND expression
    | expression NOT? IN LPAREN (expression (COMMA expression)* | queryExpression) RPAREN
    | expression NOT? LIKE expression (ESCAPE expression)?
    | expression IS NOT? NULL
    | EXISTS LPAREN queryExpression RPAREN
    | expression
    ;

comparisonOperator
    : EQ | NEQ | LT | GT | LTE | GTE
    ;

expression
    : expression (STAR | SLASH) expression
    | expression (PLUS | MINUS) expression
    | expression CONCAT_OP expression
    | expression CONCAT expression
    | (PLUS | MINUS) expression
    | LPAREN expression RPAREN
    | CASE caseWhen+ (ELSE expression)? END
    | CAST LPAREN expression AS dataType RPAREN
    | qualifiedName LPAREN (STAR | (expression (COMMA expression)*))? RPAREN
    | LPAREN queryExpression RPAREN
    | constant
    | specialValue
    | qualifiedName
    | HOST_VARIABLE
    | QUESTION
    ;

caseWhen
    : WHEN searchCondition THEN expression
    | WHEN expression THEN expression
    ;

specialValue
    : CURRENT nonReserved+
    | USER
    | NULL
    ;

constant
    : signedNumber
    | STRING
    | HEX_STRING
    | PLACEHOLDER
    ;

signedNumber
    : (PLUS | MINUS)? NUMBER
    ;

//
// Names and types
//

qualifiedName
    : identifier (DOT identifier)*
    ;

identifier
    : IDENTIFIER
    | DELIMITED_IDENTIFIER
    | PLACEHOLDER
    | nonReserved
    ;

dataType
    : typeName (LPAREN NUMBER (COMMA NUMBER)? RPAREN)? typeAttribute*
    ;

typeName
    : CHAR VARYING?
    | CHARACTER VARYING?
    | LONG (VARCHAR | VARGRAPHIC)
    | DOUBLE PRECISION?
    | qualifiedName
    ;

typeAttribute
    : FOR (SBCS | MIXED | BIT) DATA
    | CCSID identifier
    | WITH TIME ZONE
    | WITHOUT TIME ZONE
    ;

// Storage attributes, which every physical object shares.
storageOption
    : PRIQTY signedNumber
    | SECQTY signedNumber
    | ERASE (YES | NO)
    | FREEPAGE NUMBER
    | PCTFREE NUMBER (FOR UPDATE NUMBER)?
    | GBPCACHE (CHANGED | ALL | NONE | SYSTEM)
    | USING (STOGROUP identifier | VCAT identifier)
    ;

end
    : SEMI?
    ;

// DB2's non-reserved keywords. A word here is a keyword in the statements that use it and an
// ordinary name everywhere else, which is why genapp can call a column `value`.
nonReserved
    : ABSOLUTE | ACCESS | ACTION | ADMIN | ALWAYS | ASUTIME | AT | ATOMIC | ATTRIBUTES | AUX
    | CACHE | CALLED | CARDINALITY | CASCADE | CHANGE | CHANGED | CHANGES | COMPARISONS | COMPRESS
    | CONTEXT | COPY | CURSORS | CYCLE | DATACLAS | DB2 | DB2SQL | DEBUG | DEFER | DEFINE | DEFINER
    | EACH | ENABLE | ENFORCED | ENVIRONMENT | EXCLUDE | EXCLUDING | EXCLUSIVE | GENERATE
    | HASH | HIDDEN_KW | HISTORY | ID | IDENTITY | IMPLICITLY | INCLUDE | INCLUDING | INCREMENT
    | INDEXBP | INLINE | INPUT | INSTEAD | KEYS | LARGE | LENGTH | LIMIT | LOAD | LOB | LOGGED
    | MAIN | MASK | MAXPARTITIONS | MAXROWS | MAXVALUE | MEMBER | MGMTCLAS | MINVALUE | MIXED
    | MODE | NAME | NEW | NEW_TABLE | OLD_TABLE | ONLY | OPTION | OPTIONS | ORGANIZE | PAGE
    | PAGENUM | PCTFREE | PERMISSION | QUALIFIER | RANDOM | RANGE | REGENERATE | REGISTERS
    | RELATIVE | REMOVE | REPLACE | RESET | RESIDENT | RESTART | RETAIN | ROTATE | ROWS | SBCS
    | SECURED | SEGSIZE | SETS | SHARE | SIZE | SPACE | SPECIAL | SQL | SQLID | START | STATEMENT
    | STORCLAS | SUB | TEMPORARY | TIME | TIMESTAMP | TRACKMOD | TRUSTED | UNLOAD | USAGE | USE
    | VARCHAR | VARGRAPHIC | VARYING | VERSIONING | WITHOUT | WORK | XMLPATTERN | YES
    | ASC | DESC | BIT | FREEPAGE | GBPCACHE | PRIMARY
    // Columns of SYSIBM.SYSCOLUMNS and SYSIBM.SYSTABLES, which a catalog query selects by name.
    | NULLS | TYPE
    | AUTHENTICATION | AUTHID | BASED | CONTROL | UPON
    | VALUE | SYSIBM | SYSFUN | SYSPROC | SOURCE | STANDARD | SIMPLE | SUMMARY | MAINTAINED | MATERIALIZED | ENCODING | LOCALE | NAME | PROGRAM | PLAN | QUERYNO | OPTIMIZE | OPTIMIZATION | ORGANIZATION | PADDED | CAPTURE | CLONE | STORES | STYLE | ISOBID | PSID | OBID | DBINFO | SCRATCHPAD | FINAL | VARIANT | LC_CTYPE | JAR | COLLID | ROWSET | PREVVAL | NEXT | LAST | FIRST
    ;
