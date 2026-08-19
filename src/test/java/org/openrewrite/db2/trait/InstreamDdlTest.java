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
package org.openrewrite.db2.trait;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.db2.tree.Db2;
import org.openrewrite.test.RewriteTest;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.jcl.Assertions.jcl;

/**
 * Reading the DDL a job runs, which is where most of an estate's tables are really created.
 */
class InstreamDdlTest implements RewriteTest {

    @Test
    void theTablesAJobCreates() {
        rewriteRun(
          jcl(
            """
              //GENADB2 JOB 241901,'Db2 create',NOTIFY=&SYSUID
              //CREATE  EXEC PGM=IKJEFT01,DYNAMNBR=20
              //SYSTSIN  DD *
                 DSN SYSTEM(DB2A)
                 RUN  PROGRAM(DSNTIAD) PLAN(DSNTIA11)
              /*
              //SYSIN    DD *
                SET CURRENT SQLID='SYSADM' ;
              CREATE TABLE GENASA1.customer (
                   customerNumber INTEGER NOT NULL,
                   firstName      CHAR(10),
                 PRIMARY KEY(customerNumber))
                 CCSID EBCDIC
                 IN GENASA1.GENATS01;
              CREATE UNIQUE INDEX GENASA1.iCustomer
                 ON GENASA1.customer (customerNumber) CLUSTER
                 COPY YES ;
              /*
              //
              """,
            spec -> spec.afterRecipe(cu -> {
                List<InstreamDdl> streams = new InstreamDdl.Matcher().lower(cu).collect(toList());

                // SYSTSIN carries the DSN and RUN commands that submit the DDL, not DDL itself.
                assertThat(streams).hasSize(1);
                assertThat(streams.get(0).getName()).isEqualTo("SYSIN");

                Db2.CompilationUnit ddl = streams.get(0).parse(new InMemoryExecutionContext(t -> {
                    throw new IllegalStateException(t);
                }));

                List<Table> tables = new Table.Matcher().lower(ddl).collect(toList());
                assertThat(tables).hasSize(1);
                assertThat(tables.get(0).getQualifiedName()).isEqualTo("GENASA1.CUSTOMER");
                assertThat(tables.get(0).getTablespace()).isEqualTo("GENASA1.GENATS01");
                assertThat(tables.get(0).getColumns()).extracting(Column::getName)
                  .containsExactly("customerNumber", "firstName");
                assertThat(tables.get(0).getPrimaryKey()).containsExactly("customerNumber");

                List<Index> indexes = new Index.Matcher().lower(ddl).collect(toList());
                assertThat(indexes).hasSize(1);
                assertThat(indexes.get(0).getTable()).isEqualTo("GENASA1.CUSTOMER");
                assertThat(indexes.get(0).isUnique()).isTrue();

                // The stream is read back exactly as the job holds it, which is what makes parsing
                // it the same as parsing a file.
                assertThat(ddl.printAll()).isEqualTo(streams.get(0).getText());
            })
          )
        );
    }

    /**
     * A SYSIN stream is far more often IDCAMS or sort cards than DDL. Reading every one of them as
     * DB2 would report nothing but cost the whole estate a parse.
     */
    @Test
    void aStreamThatIsNotDdlIsNotRead() {
        rewriteRun(
          jcl(
            """
              //DEFCLUST JOB 'VSAM',CLASS=A
              //STEP1   EXEC PGM=IDCAMS
              //SYSIN    DD *
                DEFINE CLUSTER (NAME(AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS) -
                       CYLINDERS(1 1) -
                       KEYS(11 0) -
                       RECORDSIZE(300 300))
              /*
              //
              """,
            spec -> spec.afterRecipe(cu ->
              assertThat(new InstreamDdl.Matcher().lower(cu).collect(toList())).isEmpty())
          )
        );
    }
}
