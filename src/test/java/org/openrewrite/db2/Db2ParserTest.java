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

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.SourceFile;
import org.openrewrite.db2.tree.Db2;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What the island grammar reads, and what it leaves alone.
 * <p>
 * Every case here asserts the file prints back byte for byte as well as what was read from it. A
 * grammar that reads only some statements has to carry the rest untouched, and nothing but printing
 * proves it did.
 */
class Db2ParserTest {

    @Test
    void aTableIsColumnsAndAPrimaryKey() {
        Db2.Ddl cu = parse(
          """
            CREATE TABLE CARDDEMO.TRANSACTION_TYPE
            (   TR_TYPE                        CHAR(2) NOT NULL,
                TR_DESCRIPTION                 VARCHAR(50) NOT NULL,
                PRIMARY KEY(TR_TYPE));
            """);

        assertThat(cu.getStatements()).hasSize(1);
        Db2.CreateTable table = (Db2.CreateTable) cu.getStatements().get(0);
        assertThat(table.getName().getFullName()).isEqualTo("CARDDEMO.TRANSACTION_TYPE");
        assertThat(table.getName().getQualifier()).isEqualTo("CARDDEMO");
        assertThat(table.getName().getSimpleName()).isEqualTo("TRANSACTION_TYPE");

        assertThat(table.getColumns()).hasSize(2);
        Db2.ColumnDefinition trType = table.getColumns().get(0);
        assertThat(trType.getName().getSimpleName()).isEqualTo("TR_TYPE");
        assertThat(trType.getType().getName().getSimpleName()).isEqualTo("CHAR");
        assertThat(trType.isNotNull()).isTrue();

        assertThat(table.getConstraints()).hasSize(1);
        Db2.Constraint primaryKey = table.getConstraints().get(0);
        assertThat(primaryKey.getKind()).isEqualTo("PRIMARY");
        assertThat(names(primaryKey.getColumns())).containsExactly("TR_TYPE");
    }

    /**
     * A nullable column has no {@code NOT NULL} among its attributes rather than a {@code NULL} of
     * its own, so nullability is the absence of something. CardDemo pads its DDL out into columns,
     * which is where a parser reading attributes positionally would go wrong.
     */
    @Test
    void aColumnWithoutNotNullIsNullable() {
        Db2.Ddl cu = parse(
          """
            CREATE TABLE CARDDEMO.AUTHFRDS
            (CARD_NUM              CHAR(16)    NOT NULL,
                AUTH_TS                TIMESTAMP   NOT NULL,
                TRANSACTION_AMT        DECIMAL(12,2)       ,
                POS_ENTRY_MODE         SMALLINT            ,
                FRAUD_RPT_DATE         DATE                ,
                PRIMARY KEY(CARD_NUM,AUTH_TS )             );
            """);

        Db2.CreateTable table = (Db2.CreateTable) cu.getStatements().get(0);
        assertThat(table.getColumns()).extracting(c -> c.getName().getSimpleName())
          .containsExactly("CARD_NUM", "AUTH_TS", "TRANSACTION_AMT", "POS_ENTRY_MODE", "FRAUD_RPT_DATE");
        assertThat(table.getColumns()).extracting(Db2.ColumnDefinition::isNotNull)
          .containsExactly(true, true, false, false, false);
        assertThat(table.getColumns().get(2).getType().getArguments())
          .extracting(Db2.Word::getText).containsExactly("(", "12", ",", "2", ")");
        assertThat(names(table.getConstraints().get(0).getColumns()))
          .containsExactly("CARD_NUM", "AUTH_TS");
    }

    /**
     * DB2 lets a foreign key carry a name between {@code KEY} and its columns, which CardDemo writes
     * and genapp does not. Reading that name as the constrained column would make the table point at
     * itself.
     */
    @Test
    void aForeignKeyPointsAtAnotherTable() {
        Db2.Ddl cu = parse(
          """
            CREATE TABLE CARDDEMO.TRANSACTION_TYPE_CATEGORY
            (   TRC_TYPE_CODE                  CHAR(2) NOT NULL,
                TRC_TYPE_CATEGORY              CHAR(4) NOT NULL,
                PRIMARY KEY(TRC_TYPE_CODE,TRC_TYPE_CATEGORY),
                FOREIGN KEY TRC_TYPE_CODE (TRC_TYPE_CODE)
                REFERENCES CARDDEMO.TRANSACTION_TYPE (TR_TYPE) ON DELETE RESTRICT);
            """);

        Db2.CreateTable table = (Db2.CreateTable) cu.getStatements().get(0);
        assertThat(table.getConstraints()).hasSize(2);

        Db2.Constraint foreignKey = table.getConstraints().get(1);
        assertThat(foreignKey.getKind()).isEqualTo("FOREIGN");
        assertThat(names(foreignKey.getColumns())).containsExactly("TRC_TYPE_CODE");
        assertThat(foreignKey.getReferencedTable()).isNotNull();
        assertThat(foreignKey.getReferencedTable().getFullName())
          .isEqualTo("CARDDEMO.TRANSACTION_TYPE");
        assertThat(names(foreignKey.getReferencedColumns())).containsExactly("TR_TYPE");
        assertThat(foreignKey.getOptions()).extracting(Db2.Word::getText)
          .containsExactly("ON", "DELETE", "RESTRICT");
    }

    @Test
    void anIndexNamesItsTableAndItsKeys() {
        Db2.Ddl cu = parse(
          """
            CREATE UNIQUE INDEX CARDDEMO.XAUTHFRD
                ON CARDDEMO.AUTHFRDS
                (CARD_NUM ASC, AUTH_TS DESC)
                COPY YES;
            """);

        Db2.CreateIndex index = (Db2.CreateIndex) cu.getStatements().get(0);
        assertThat(index.isUnique()).isTrue();
        assertThat(index.getName().getFullName()).isEqualTo("CARDDEMO.XAUTHFRD");
        assertThat(index.getTable().getFullName()).isEqualTo("CARDDEMO.AUTHFRDS");
        assertThat(index.getIndexKeys()).extracting(k -> k.getName().getSimpleName())
          .containsExactly("CARD_NUM", "AUTH_TS");
        assertThat(index.getIndexKeys()).extracting(
          k -> k.getDirection() == null ? null : k.getDirection().getText())
          .containsExactly("ASC", "DESC");
    }

    /**
     * An index with no {@code UNIQUE} is not a unique index, and a key with no direction takes DB2's
     * ascending default rather than being unread.
     */
    @Test
    void anIndexNeedNotBeUniqueOrDirected() {
        Db2.Ddl cu = parse(
          """
            CREATE INDEX BANKZ.ACCTCUST
               ON BANKZ.ACCOUNT(ACCOUNT_SORTCODE,ACCOUNT_CUSTOMER_NUMBER)
               ;
            """);

        Db2.CreateIndex index = (Db2.CreateIndex) cu.getStatements().get(0);
        assertThat(index.isUnique()).isFalse();
        assertThat(index.getIndexKeys()).extracting(k -> k.getDirection()).containsOnlyNulls();
    }

    /**
     * CardDemo adds its foreign key after the fact, so an estate's referential integrity is not all
     * in the {@code CREATE TABLE}s.
     */
    @Test
    void anAlterTableCarriesAForeignKey() {
        Db2.Ddl cu = parse(
          """
            ALTER TABLE CARDDEMO.TRANSACTION_TYPE_CATEGORY
              FOREIGN KEY (TRC_TYPE_CODE)
                REFERENCES CARDDEMO.TRANSACTION_TYPE (TR_TYPE)
            ON DELETE RESTRICT;
            """);

        Db2.AlterTable alter = (Db2.AlterTable) cu.getStatements().get(0);
        assertThat(alter.getName().getFullName()).isEqualTo("CARDDEMO.TRANSACTION_TYPE_CATEGORY");
        assertThat(alter.getConstraints()).hasSize(1);
        assertThat(alter.getConstraints().get(0).getReferencedTable().getFullName())
          .isEqualTo("CARDDEMO.TRANSACTION_TYPE");
    }

    /**
     * The water. None of these say anything the relationship graph joins on, and all of them have to
     * survive being read.
     */
    @Test
    void everythingElseIsWater() {
        Db2.Ddl cu = parse(
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

        assertThat(cu.getStatements()).hasSize(5);
        assertThat(cu.getStatements()).allMatch(Db2.Unknown.class::isInstance);
    }

    /**
     * {@code CREATE TABLESPACE} begins with the two words a {@code CREATE TABLE} does. The lexer
     * takes the longest match, so TABLESPACE is one word and never half of one.
     */
    @Test
    void createTablespaceIsNotCreateTable() {
        Db2.Ddl cu = parse("CREATE TABLESPACE ACCOUNT IN BANKZ ;\n");
        assertThat(cu.getStatements().get(0)).isInstanceOf(Db2.Unknown.class);
    }

    @Test
    void theTablespaceATableIsCreatedIn() {
        Db2.Ddl cu = parse(
          """
            CREATE TABLE  CARDDEMO.TRANSACTION_TYPE
             (       TR_TYPE   CHAR(2)      NOT NULL,
                       PRIMARY KEY(TR_TYPE))
               IN CARDDEMO.CARDSPC1
               CCSID EBCDIC;
            """);

        Db2.CreateTable table = (Db2.CreateTable) cu.getStatements().get(0);
        assertThat(table.getTablespace()).isNotNull();
        assertThat(table.getTablespace().getFullName()).isEqualTo("CARDDEMO.CARDSPC1");
    }

    /**
     * genapp ships its DDL as a template. A placeholder stands where a schema belongs, and lexing it
     * as anything but a name would make the whole statement water.
     */
    @Test
    void aTemplatedSchemaIsStillAName() {
        Db2.Ddl cu = parse(
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

        Db2.CreateTable table = (Db2.CreateTable) cu.getStatements().get(0);
        assertThat(table.getName().getQualifier()).isEqualTo("<DB2DBID>");
        assertThat(table.getName().getSimpleName()).isEqualTo("house");
        // The DDL writes the table in lower case and the COBOL that reads it in upper.
        assertThat(table.getName().getFullName()).isEqualTo("<DB2DBID>.HOUSE");
        assertThat(table.getColumns()).hasSize(2);
        assertThat(table.getConstraints().get(1).getReferencedTable().getSimpleName())
          .isEqualTo("policy");
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

        Db2.Ddl cu = parse(source);
        assertThat(cu.getStatements()).hasSize(1);
        assertThat(cu.printAll()).isEqualTo(source);
    }

    /**
     * The island contract, and the half of it that matters most: a statement this grammar claims to
     * read and cannot is a syntax error, never an {@link Db2.Unknown}. Both of these are real DB2 the
     * grammar does not model, and both are reported rather than quietly demoted — a schema silently
     * missing a table is worse than one that says it could not read it.
     */
    @Test
    void aTableShapeTheGrammarDoesNotKnowIsAnError() {
        assertThat(errorsIn("CREATE TABLE CARDDEMO.ARCHIVE LIKE CARDDEMO.ACCOUNT;\n"))
          .isNotEmpty();
        assertThat(errorsIn(
          """
            CREATE TABLE CARDDEMO.ACCOUNT_SUMMARY AS
              (SELECT ACCT_ID, ACCT_STATUS FROM CARDDEMO.ACCOUNT) WITH NO DATA;
            """)).isNotEmpty();
    }

    /**
     * The other half: a statement the grammar never claimed is water, silently and for free. These
     * all begin with a word an island begins with, which is the case that has to be told apart by
     * more than the first token.
     */
    @Test
    void aStatementTheGrammarNeverClaimedIsNotAnError() {
        Db2.Ddl ddl = parse(
          """
            CREATE TABLESPACE CARDSPC1 IN CARDDEMO;
            CREATE DATABASE CARDDEMO CCSID EBCDIC;
            CREATE STOGROUP GENASG02 VOLUMES ('*') VCAT DB2RUN;
            CREATE VIEW CARDDEMO.V_ACCOUNT AS SELECT ACCT_ID FROM CARDDEMO.ACCOUNT;
            ALTER TABLESPACE CARDDEMO.CARDSPC1 BUFFERPOOL BP1;
            """);

        assertThat(ddl.getStatements()).hasSize(5);
        assertThat(ddl.getStatements()).allMatch(Db2.Unknown.class::isInstance);
    }

    /**
     * A statement written without its terminating semicolon must not swallow the one after it. The
     * water rules stop short of CREATE and ALTER for exactly this.
     */
    @Test
    void aMissingSemicolonDoesNotRunTwoStatementsTogether() {
        Db2.Ddl cu = parse(
          """
            COMMIT
            CREATE TABLE BANKZ.CONTROL (
                                CONTROL_NAME                   CHAR(32)
                               )
            """);

        assertThat(cu.getStatements()).hasSize(2);
        assertThat(cu.getStatements().get(0)).isInstanceOf(Db2.Unknown.class);
        assertThat(cu.getStatements().get(1)).isInstanceOf(Db2.CreateTable.class);
    }

    /**
     * A check constraint's expression is water, so it is kept as words rather than modelled — but it
     * is full of the commas and parentheses that separate one table element from the next, and being
     * read as several would lose the columns after it.
     */
    @Test
    void aCheckConstraintIsKeptWhole() {
        Db2.Ddl cu = parse(
          """
            CREATE TABLE CARDDEMO.ACCOUNT
            (   ACCT_ID     DECIMAL(11) NOT NULL,
                ACCT_STATUS CHAR(1) NOT NULL,
                CONSTRAINT STATUS_OK CHECK (ACCT_STATUS IN ('Y', 'N')),
                ACCT_GROUP  CHAR(10),
                PRIMARY KEY(ACCT_ID));
            """);

        Db2.CreateTable table = (Db2.CreateTable) cu.getStatements().get(0);
        assertThat(table.getColumns()).extracting(c -> c.getName().getSimpleName())
          .containsExactly("ACCT_ID", "ACCT_STATUS", "ACCT_GROUP");
        assertThat(table.getConstraints()).extracting(Db2.Constraint::getKind)
          .containsExactly("CHECK", "PRIMARY");
    }

    /**
     * {@code CREATE UNIQUE WHERE NOT NULL INDEX} is one index, not a syntax error with an index in
     * it.
     */
    @Test
    void theFourWordUniqueIndex() {
        Db2.Ddl cu = parse(
          """
            CREATE UNIQUE WHERE NOT NULL INDEX CARDDEMO.XACCTALT
                ON CARDDEMO.ACCOUNT (ACCT_GROUP);
            """);

        Db2.CreateIndex index = (Db2.CreateIndex) cu.getStatements().get(0);
        assertThat(index.isUnique()).isTrue();
        assertThat(index.getTable().getFullName()).isEqualTo("CARDDEMO.ACCOUNT");
    }

    /**
     * A delimited name keeps its case and its spaces in DB2, so the quotes are punctuation rather
     * than part of what the column is called.
     */
    @Test
    void aDelimitedName() {
        Db2.Ddl cu = parse(
          """
            CREATE TABLE "My Schema"."Account" (
                "Account Id" INTEGER NOT NULL,
                PRIMARY KEY("Account Id"));
            """);

        Db2.CreateTable table = (Db2.CreateTable) cu.getStatements().get(0);
        assertThat(table.getName().getSimpleName()).isEqualTo("Account");
        assertThat(table.getName().getQualifier()).isEqualTo("My Schema");
        assertThat(table.getColumns().get(0).getName().getSimpleName()).isEqualTo("Account Id");
    }

    /**
     * DB2 DDL is case insensitive and the corpus is not consistent about it.
     */
    @Test
    void lowerCaseKeywords() {
        Db2.Ddl cu = parse("create table bankz.control (control_name char(32));\n");
        Db2.CreateTable table = (Db2.CreateTable) cu.getStatements().get(0);
        assertThat(table.getName().getFullName()).isEqualTo("BANKZ.CONTROL");
        assertThat(table.getColumns()).hasSize(1);
    }

    private static List<String> names(Db2.@Nullable ColumnList columns) {
        assertThat(columns).isNotNull();
        return columns.getColumnNames().stream()
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
        Db2.Ddl cu = (Db2.Ddl) parsed.get(0);
        assertThat(cu.printAll()).as("prints back byte for byte").isEqualTo(source);
        return cu;
    }
}
