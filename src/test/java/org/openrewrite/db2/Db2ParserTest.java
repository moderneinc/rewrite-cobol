/*
 * Copyright 2025 the original author or authors.
 * <p>
 * Licensed under the Moderne Source Available License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://docs.moderne.io/licensing/moderne-source-available-license
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.db2;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.SourceFile;
import org.openrewrite.db2.marker.Semicolon;
import org.openrewrite.db2.tree.Db2;
import org.openrewrite.db2.tree.Db2Container;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What the grammar reads, and what it refuses to read.
 * <p>
 * Every case here asserts the file prints back byte for byte as well as what was read from it.
 * Nothing but printing proves a statement was read whole.
 */
class Db2ParserTest {

    @Test
    void aTableIsColumnsAndAPrimaryKey() {
        Db2.Ddl ddl = parse(
          """
            CREATE TABLE CARDDEMO.TRANSACTION_TYPE
            (   TR_TYPE                        CHAR(2) NOT NULL,
                TR_DESCRIPTION                 VARCHAR(50) NOT NULL,
                PRIMARY KEY(TR_TYPE));
            """);

        assertThat(ddl.getStatements()).hasSize(1);
        Db2.CreateTable table = (Db2.CreateTable) ddl.getStatements().get(0);
        assertThat(table.getName().getFullName()).isEqualTo("CARDDEMO.TRANSACTION_TYPE");
        assertThat(table.getName().getQualifier()).isEqualTo("CARDDEMO");
        assertThat(table.getName().getSimpleName()).isEqualTo("TRANSACTION_TYPE");

        assertThat(columns(table)).hasSize(2);
        Db2.ColumnDefinition trType = columns(table).get(0);
        assertThat(trType.getName().getSimpleName()).isEqualTo("TR_TYPE");
        assertThat(trType.getType().getName().getSimpleName()).isEqualTo("CHAR");
        assertThat(isNotNull(trType)).isTrue();

        assertThat(constraints(table)).hasSize(1);
        Db2.Constraint primaryKey = constraints(table).get(0);
        assertThat(Db2.has(primaryKey.getKeywords(), Db2.Keyword.Type.Primary)).isTrue();
        assertThat(names(primaryKey.getColumns())).containsExactly("TR_TYPE");
    }

    /**
     * A nullable column has no {@code NOT NULL} among its attributes rather than a {@code NULL} of
     * its own, so nullability is the absence of something. CardDemo pads its DDL out into columns,
     * which is where a parser reading attributes positionally would go wrong.
     */
    @Test
    void aColumnWithoutNotNullIsNullable() {
        Db2.Ddl ddl = parse(
          """
            CREATE TABLE CARDDEMO.AUTHFRDS
            (CARD_NUM              CHAR(16)    NOT NULL,
                AUTH_TS                TIMESTAMP   NOT NULL,
                TRANSACTION_AMT        DECIMAL(12,2)       ,
                POS_ENTRY_MODE         SMALLINT            ,
                FRAUD_RPT_DATE         DATE                ,
                PRIMARY KEY(CARD_NUM,AUTH_TS )             );
            """);

        Db2.CreateTable table = (Db2.CreateTable) ddl.getStatements().get(0);
        assertThat(columns(table)).extracting(c -> c.getName().getSimpleName())
          .containsExactly("CARD_NUM", "AUTH_TS", "TRANSACTION_AMT", "POS_ENTRY_MODE", "FRAUD_RPT_DATE");
        assertThat(columns(table)).extracting(Db2ParserTest::isNotNull)
          .containsExactly(true, true, false, false, false);
        // The parentheses and the comma belong to the container, so DECIMAL(12,2) is two arguments.
        Db2Container<Db2.Word> arguments = columns(table).get(2).getType().getArguments();
        assertThat(arguments).isNotNull();
        assertThat(arguments.getElements()).extracting(Db2.Word::getText).containsExactly("12", "2");
        assertThat(names(constraints(table).get(0).getColumns()))
          .containsExactly("CARD_NUM", "AUTH_TS");
    }

    /**
     * DB2 lets a foreign key carry a name between {@code KEY} and its columns, which CardDemo writes
     * and genapp does not. Reading that name as the constrained column would make the table point at
     * itself.
     */
    @Test
    void aForeignKeyPointsAtAnotherTable() {
        Db2.Ddl ddl = parse(
          """
            CREATE TABLE CARDDEMO.TRANSACTION_TYPE_CATEGORY
            (   TRC_TYPE_CODE                  CHAR(2) NOT NULL,
                TRC_TYPE_CATEGORY              CHAR(4) NOT NULL,
                PRIMARY KEY(TRC_TYPE_CODE,TRC_TYPE_CATEGORY),
                FOREIGN KEY TRC_TYPE_CODE (TRC_TYPE_CODE)
                REFERENCES CARDDEMO.TRANSACTION_TYPE (TR_TYPE) ON DELETE RESTRICT);
            """);

        Db2.CreateTable table = (Db2.CreateTable) ddl.getStatements().get(0);
        assertThat(constraints(table)).hasSize(2);

        Db2.Constraint foreignKey = constraints(table).get(1);
        assertThat(Db2.has(foreignKey.getKeywords(), Db2.Keyword.Type.Foreign)).isTrue();
        assertThat(foreignKey.getKeyName()).isNotNull();
        assertThat(foreignKey.getKeyName().getSimpleName()).isEqualTo("TRC_TYPE_CODE");
        assertThat(names(foreignKey.getColumns())).containsExactly("TRC_TYPE_CODE");
        assertThat(foreignKey.getReferencedTable()).isNotNull();
        assertThat(foreignKey.getReferencedTable().getFullName())
          .isEqualTo("CARDDEMO.TRANSACTION_TYPE");
        assertThat(names(foreignKey.getReferencedColumns())).containsExactly("TR_TYPE");
    }

    @Test
    void anIndexNamesItsTableAndItsKeys() {
        Db2.Ddl ddl = parse(
          """
            CREATE UNIQUE INDEX CARDDEMO.XAUTHFRD
                ON CARDDEMO.AUTHFRDS
                (CARD_NUM ASC, AUTH_TS DESC)
                COPY YES;
            """);

        Db2.CreateIndex index = (Db2.CreateIndex) ddl.getStatements().get(0);
        assertThat(Db2.has(index.getKeywords(), Db2.Keyword.Type.Unique)).isTrue();
        assertThat(index.getName().getFullName()).isEqualTo("CARDDEMO.XAUTHFRD");
        assertThat(index.getTable().getFullName()).isEqualTo("CARDDEMO.AUTHFRDS");
        assertThat(index.getKeys().getElements()).extracting(k -> k.getName().getSimpleName())
          .containsExactly("CARD_NUM", "AUTH_TS");
        assertThat(index.getKeys().getElements()).extracting(
          k -> k.getDirection() == null ? null : k.getDirection().getType())
          .containsExactly(Db2.Keyword.Type.Asc, Db2.Keyword.Type.Desc);
    }

    /**
     * An index with no {@code UNIQUE} is not a unique index, and a key with no direction takes DB2's
     * ascending default rather than being unread.
     */
    @Test
    void anIndexNeedNotBeUniqueOrDirected() {
        Db2.Ddl ddl = parse(
          """
            CREATE INDEX BANKZ.ACCTCUST
               ON BANKZ.ACCOUNT(ACCOUNT_SORTCODE,ACCOUNT_CUSTOMER_NUMBER)
               ;
            """);

        Db2.CreateIndex index = (Db2.CreateIndex) ddl.getStatements().get(0);
        assertThat(Db2.has(index.getKeywords(), Db2.Keyword.Type.Unique)).isFalse();
        assertThat(index.getKeys().getElements()).extracting(Db2.IndexKey::getDirection)
          .containsOnlyNulls();
    }

    /**
     * CardDemo adds its foreign key after the fact, so an estate's referential integrity is not all
     * in the {@code CREATE TABLE}s.
     */
    @Test
    void anAlterTableCarriesAForeignKey() {
        Db2.Ddl ddl = parse(
          """
            ALTER TABLE CARDDEMO.TRANSACTION_TYPE_CATEGORY
              FOREIGN KEY (TRC_TYPE_CODE)
                REFERENCES CARDDEMO.TRANSACTION_TYPE (TR_TYPE)
            ON DELETE RESTRICT;
            """);

        Db2.AlterTable alter = (Db2.AlterTable) ddl.getStatements().get(0);
        assertThat(alter.getName().getFullName()).isEqualTo("CARDDEMO.TRANSACTION_TYPE_CATEGORY");
        List<Db2.Constraint> constraints = Db2.elementsOf(alter.getActions(), Db2.Constraint.class);
        assertThat(constraints).hasSize(1);
        assertThat(constraints.get(0).getReferencedTable()).isNotNull();
        assertThat(constraints.get(0).getReferencedTable().getFullName())
          .isEqualTo("CARDDEMO.TRANSACTION_TYPE");
    }

    /**
     * Every statement DB2 documents is a node of its own. None of these says anything the
     * relationship graph joins on, and all of them are still read as what they are rather than
     * demoted to something that says nothing.
     */
    @Test
    void everyStatementIsItsOwnNode() {
        Db2.Ddl ddl = parse(
          """
            SET CURRENT SQLID = 'SYSADM';
            CREATE DATABASE CARDDEMO
                   STOGROUP AWST1STG
                   BUFFERPOOL BP0
                   CCSID EBCDIC;
            COMMIT ;
            CREATE TABLESPACE CARDSPC1
              IN CARDDEMO
              SEGSIZE 4
              LOCKSIZE TABLE;
            GRANT DELETE, INSERT, SELECT, UPDATE
                  ON TABLE CARDDEMO.TRANSACTION_TYPE
                  TO PUBLIC;
            """);

        assertThat(ddl.getStatements()).hasSize(5);
        assertThat(ddl.getStatements()).extracting(s -> s.getClass().getSimpleName())
          .containsExactly("Set", "CreateDatabase", "Commit", "CreateTablespace", "Grant");

        Db2.CreateTablespace tablespace = (Db2.CreateTablespace) ddl.getStatements().get(3);
        assertThat(tablespace.getName().getSimpleName()).isEqualTo("CARDSPC1");
        assertThat(tablespace.getDatabase()).isNotNull();
        assertThat(tablespace.getDatabase().getSimpleName()).isEqualTo("CARDDEMO");

        Db2.Grant grant = (Db2.Grant) ddl.getStatements().get(4);
        assertThat(grant.getObjects().getElements()).extracting(Db2.Name::getFullName)
          .containsExactly("CARDDEMO.TRANSACTION_TYPE");
    }

    /**
     * {@code CREATE TABLESPACE} begins with the two words a {@code CREATE TABLE} does. The lexer
     * takes the longest match, so TABLESPACE is one word and never half of one.
     */
    @Test
    void createTablespaceIsNotCreateTable() {
        Db2.Ddl ddl = parse("CREATE TABLESPACE ACCOUNT IN BANKZ ;\n");
        assertThat(ddl.getStatements().get(0)).isInstanceOf(Db2.CreateTablespace.class);
    }

    /**
     * A table defined by another table, and one defined by a query. Neither has a column list to
     * read, so the shape of the contents is what says where the columns came from.
     */
    @Test
    void aTableNeedNotListItsColumns() {
        Db2.Ddl like = parse("CREATE TABLE CARDDEMO.ARCHIVE LIKE CARDDEMO.ACCOUNT;\n");
        Db2.TableLike contents = (Db2.TableLike) ((Db2.CreateTable) like.getStatements().get(0)).getContents();
        assertThat(contents.getTable().getFullName()).isEqualTo("CARDDEMO.ACCOUNT");

        Db2.Ddl asQuery = parse(
          """
            CREATE TABLE CARDDEMO.ACCOUNT_SUMMARY AS
              (SELECT ACCT_ID, ACCT_STATUS FROM CARDDEMO.ACCOUNT) WITH NO DATA;
            """);
        assertThat(((Db2.CreateTable) asQuery.getStatements().get(0)).getContents())
          .isInstanceOf(Db2.TableAsQuery.class);
    }

    @Test
    void theTablespaceATableIsCreatedIn() {
        Db2.Ddl ddl = parse(
          """
            CREATE TABLE  CARDDEMO.TRANSACTION_TYPE
             (       TR_TYPE   CHAR(2)      NOT NULL,
                       PRIMARY KEY(TR_TYPE))
               IN CARDDEMO.CARDSPC1
               CCSID EBCDIC;
            """);

        Db2.CreateTable table = (Db2.CreateTable) ddl.getStatements().get(0);
        Db2.Option in = Db2.elementsOf(table.getOptions(), Db2.Option.class).get(0);
        assertThat(Db2.has(in.getKeywords(), Db2.Keyword.Type.In)).isTrue();
        assertThat(Db2.elementsOf(in.getValues(), Db2.Name.class).get(0).getFullName())
          .isEqualTo("CARDDEMO.CARDSPC1");
    }

    /**
     * genapp ships its DDL as a template. A placeholder stands where a schema belongs, and lexing it
     * as anything but a name would leave the whole statement unread.
     */
    @Test
    void aTemplatedSchemaIsStillAName() {
        Db2.Ddl ddl = parse(
          """
            CREATE TABLE <DB2DBID>.house (
                 policyNumber   INTEGER NOT NULL,
                 postcode       CHAR(8),
               PRIMARY KEY(policyNumber),
               FOREIGN KEY(policyNumber)
                      REFERENCES <DB2DBID>.policy (policyNumber) ON DELETE CASCADE)
               CCSID EBCDIC
               IN <DB2DBID>.GENATS04;
            """);

        Db2.CreateTable table = (Db2.CreateTable) ddl.getStatements().get(0);
        assertThat(table.getName().getQualifier()).isEqualTo("<DB2DBID>");
        assertThat(table.getName().getSimpleName()).isEqualTo("house");
        // The DDL writes the table in lower case and the COBOL that reads it in upper.
        assertThat(table.getName().getFullName()).isEqualTo("<DB2DBID>.HOUSE");
        assertThat(columns(table)).hasSize(2);
        Db2.Name referenced = constraints(table).get(1).getReferencedTable();
        assertThat(referenced).isNotNull();
        assertThat(referenced.getSimpleName()).isEqualTo("policy");
    }

    /**
     * GenevaERS writes its schema as a JCL symbolic, so the same placeholder can arrive with an
     * ampersand rather than in angle brackets.
     */
    @Test
    void aJclSymbolicIsAlsoAName() {
        Db2.Ddl ddl = parse("CREATE TABLE &$DBSCH..LOOKUP (ENVIRONID INTEGER NOT NULL);\n");
        Db2.CreateTable table = (Db2.CreateTable) ddl.getStatements().get(0);
        assertThat(table.getName().getQualifier()).isEqualTo("&$DBSCH.");
        assertThat(table.getName().getSimpleName()).isEqualTo("LOOKUP");
    }

    /**
     * A comment is not a node. It arrives in the prefix of whatever follows it, which is the only
     * reason a file of copyright banners prints back unchanged.
     */
    @Test
    void commentsRideInTheWhitespace() {
        String source =
          """
            /* Copyright Amazon.com, Inc. or its affiliates.                   */
            /* All Rights Reserved.                                            */
            -- the transaction type reference table
            CREATE TABLE CARDDEMO.TRANSACTION_TYPE
            (   TR_TYPE  CHAR(2) NOT NULL,  -- a two character code
                PRIMARY KEY(TR_TYPE));
            """;

        Db2.Ddl ddl = parse(source);
        assertThat(ddl.getStatements()).hasSize(1);
        assertThat(ddl.printAll()).isEqualTo(source);
    }

    /**
     * The contract that matters most: what this grammar cannot read is a syntax error, never a node
     * that says nothing. There is no catch-all to fall back on, so a schema can never be silently
     * missing a table.
     */
    @Test
    void whatTheGrammarCannotReadIsAnError() {
        // Neither is DDL. A DML UPDATE or DELETE changes rows, which is a program's business.
        assertThat(errorsIn("UPDATE CARDDEMO.ACCOUNT SET ACCT_STATUS = 'N';\n")).isNotEmpty();
        assertThat(errorsIn("DELETE FROM CARDDEMO.ACCOUNT WHERE ACCT_ID = 1;\n")).isNotEmpty();
        assertThat(errorsIn("CREATE TABLE CARDDEMO.ACCOUNT (ACCT_ID INTEGER;\n")).isNotEmpty();
    }

    /**
     * A catalog query is what the .sql files beside a schema usually hold, and the names it reads
     * are what a lineage graph joins on.
     */
    @Test
    void aQueryIsAStatementOfItsOwn() {
        Db2.Ddl ddl = parse(
          """
            SELECT TBNAME, COLNO, NAME, COLTYPE, NULLS
              FROM SYSIBM.SYSCOLUMNS
             WHERE TBCREATOR = 'CLM'
             ORDER BY TBNAME, COLNO;
            """);

        Db2.Query query = (Db2.Query) ddl.getStatements().get(0);
        assertThat(Db2.elementsOf(query.getParts(), Db2.Name.class))
          .extracting(Db2.Name::getFullName).contains("SYSIBM.SYSCOLUMNS");
    }

    /**
     * Whether a statement was terminated is a marker on its padding rather than a node, so a file
     * that ends without a semicolon prints back without one.
     */
    @Test
    void theSemicolonIsAMarkerNotANode() {
        Db2.Ddl terminated = parse("COMMIT;\n");
        assertThat(terminated.getPadding().getStatements().get(0).getMarkers()
          .findFirst(Semicolon.class)).isPresent();

        Db2.Ddl bare = parse("COMMIT\n");
        assertThat(bare.getPadding().getStatements().get(0).getMarkers()
          .findFirst(Semicolon.class)).isEmpty();
    }

    /**
     * A statement written without its terminating semicolon must not swallow the one after it.
     */
    @Test
    void aMissingSemicolonDoesNotRunTwoStatementsTogether() {
        Db2.Ddl ddl = parse(
          """
            COMMIT
            CREATE TABLE BANKZ.CONTROL (
                                CONTROL_NAME                   CHAR(32)
                               )
            """);

        assertThat(ddl.getStatements()).hasSize(2);
        assertThat(ddl.getStatements().get(0)).isInstanceOf(Db2.Commit.class);
        assertThat(ddl.getStatements().get(1)).isInstanceOf(Db2.CreateTable.class);
    }

    /**
     * A check constraint's expression is kept as words rather than modelled, but it is full of the
     * commas and parentheses that separate one table element from the next, and being read as
     * several would lose the columns after it.
     */
    @Test
    void aCheckConstraintIsKeptWhole() {
        Db2.Ddl ddl = parse(
          """
            CREATE TABLE CARDDEMO.ACCOUNT
            (   ACCT_ID     DECIMAL(11) NOT NULL,
                ACCT_STATUS CHAR(1) NOT NULL,
                CONSTRAINT STATUS_OK CHECK (ACCT_STATUS IN ('Y', 'N')),
                ACCT_GROUP  CHAR(10),
                PRIMARY KEY(ACCT_ID));
            """);

        Db2.CreateTable table = (Db2.CreateTable) ddl.getStatements().get(0);
        assertThat(columns(table)).extracting(c -> c.getName().getSimpleName())
          .containsExactly("ACCT_ID", "ACCT_STATUS", "ACCT_GROUP");
        assertThat(constraints(table)).extracting(c -> Db2.subjectOf(c.getKeywords()))
          .containsExactly(null, Db2.Keyword.Type.Key);

        Db2.Constraint check = constraints(table).get(0);
        assertThat(check.getConstraintName()).isNotNull();
        assertThat(check.getConstraintName().getSimpleName()).isEqualTo("STATUS_OK");
        assertThat(Db2.has(check.getKeywords(), Db2.Keyword.Type.Check)).isTrue();
    }

    /**
     * {@code CREATE UNIQUE WHERE NOT NULL INDEX} is one index, not a syntax error with an index in
     * it.
     */
    @Test
    void theFourWordUniqueIndex() {
        Db2.Ddl ddl = parse(
          """
            CREATE UNIQUE WHERE NOT NULL INDEX CARDDEMO.XACCTALT
                ON CARDDEMO.ACCOUNT (ACCT_GROUP);
            """);

        Db2.CreateIndex index = (Db2.CreateIndex) ddl.getStatements().get(0);
        assertThat(Db2.has(index.getKeywords(), Db2.Keyword.Type.Unique)).isTrue();
        assertThat(index.getTable().getFullName()).isEqualTo("CARDDEMO.ACCOUNT");
    }

    /**
     * A delimited name keeps its case and its spaces in DB2, so the quotes are punctuation rather
     * than part of what the column is called.
     */
    @Test
    void aDelimitedName() {
        Db2.Ddl ddl = parse(
          """
            CREATE TABLE "My Schema"."Account" (
                "Account Id" INTEGER NOT NULL,
                PRIMARY KEY("Account Id"));
            """);

        Db2.CreateTable table = (Db2.CreateTable) ddl.getStatements().get(0);
        assertThat(table.getName().getSimpleName()).isEqualTo("Account");
        assertThat(table.getName().getQualifier()).isEqualTo("My Schema");
        assertThat(columns(table).get(0).getName().getSimpleName()).isEqualTo("Account Id");
    }

    /**
     * DB2 DDL is case insensitive and the corpus is not consistent about it. A keyword matches on
     * its type whichever case the file used, and prints back in the case the file used.
     */
    @Test
    void aKeywordKeepsTheCaseTheFileWroteIt() {
        Db2.Ddl ddl = parse("create table bankz.control (control_name char(32));\n");
        Db2.CreateTable table = (Db2.CreateTable) ddl.getStatements().get(0);
        assertThat(table.getName().getFullName()).isEqualTo("BANKZ.CONTROL");
        assertThat(columns(table)).hasSize(1);
        assertThat(table.getKeywords()).extracting(Db2.Keyword::getType)
          .containsExactly(Db2.Keyword.Type.Create, Db2.Keyword.Type.Table);
        assertThat(table.getKeywords()).extracting(Db2.Keyword::getText)
          .containsExactly("create", "table");
    }

    /**
     * The space in front of a keyword belongs to the keyword, so DDL that lines its words up prints
     * back lined up.
     */
    @Test
    void theSpaceBetweenTwoKeywordsSurvives() {
        parse("CREATE   TABLESPACE   GENATS04   IN   <DB2DBID>;\n");
    }

    private static List<Db2> elements(Db2.CreateTable table) {
        return ((Db2.TableElements) table.getContents()).getElements().getElements();
    }

    private static List<Db2.ColumnDefinition> columns(Db2.CreateTable table) {
        return Db2.elementsOf(elements(table), Db2.ColumnDefinition.class);
    }

    private static List<Db2.Constraint> constraints(Db2.CreateTable table) {
        return Db2.elementsOf(elements(table), Db2.Constraint.class);
    }

    private static boolean isNotNull(Db2.ColumnDefinition column) {
        for (Db2.Option option : Db2.elementsOf(column.getAttributes(), Db2.Option.class)) {
            if (Db2.has(option.getKeywords(), Db2.Keyword.Type.Not)
                && Db2.has(option.getKeywords(), Db2.Keyword.Type.Null)) {
                return true;
            }
        }
        return false;
    }

    private static List<String> names(Db2Container<Db2.Name> columns) {
        assertThat(columns).isNotNull();
        return columns.getElements().stream()
          .map(Db2.Name::getSimpleName)
          .collect(Collectors.toList());
    }

    private static List<String> errorsIn(String source) {
        List<String> errors = new ArrayList<>();
        Db2Parser.builder().build()
          .parse(new InMemoryExecutionContext(t -> errors.add(String.valueOf(t.getMessage()))), source)
          .forEach(cu -> {
          });
        return errors;
    }

    private static Db2.Ddl parse(String source) {
        List<SourceFile> parsed = Db2Parser.builder().build()
          .parse(new InMemoryExecutionContext(t -> {
              throw new IllegalStateException(t);
          }), source)
          .collect(Collectors.toList());
        assertThat(parsed).hasSize(1);
        assertThat(parsed.get(0)).isInstanceOf(Db2.Ddl.class);
        Db2.Ddl ddl = (Db2.Ddl) parsed.get(0);
        assertThat(ddl.printAll()).as("prints back byte for byte").isEqualTo(source);
        return ddl;
    }
}
