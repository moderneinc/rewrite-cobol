/*
 * Copyright 2026 the original author or authors.
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
package org.openrewrite.controlcard.idcams.trait;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.controlcard.idcams.Assertions.idcamsCard;

class IdcamsCommandTest implements RewriteTest {

    @Test
    void defineClusterMakesTheDataSetAndItsComponents() {
        rewriteRun(
          idcamsCard(
            """
              /* DEFCLM01 - CLAIM MASTER KSDS.  RECORD LAYOUT COPYBOOK CLMREC.  */
              DELETE CLM.PROD.CLMMAST CLUSTER PURGE
              SET MAXCC = 0
              DEFINE CLUSTER (NAME(CLM.PROD.CLMMAST)               -
                              INDEXED                              -
                              KEYS(10 0)                           -
                              RECORDSIZE(300 300)                  -
                              VOLUMES(PRD001))                     -
                     DATA    (NAME(CLM.PROD.CLMMAST.DATA))         -
                     INDEX   (NAME(CLM.PROD.CLMMAST.INDEX))
              """,
            spec -> spec.afterRecipe(cu -> {
                List<IdcamsCommand> commands = new IdcamsCommand.Matcher().lower(cu).collect(Collectors.toList());
                assertThat(commands).hasSize(3);

                IdcamsCommand delete = commands.get(0);
                assertThat(delete.getVerb()).isEqualTo("DELETE");
                assertThat(delete.getObjectType()).isEqualTo("CLUSTER");
                assertThat(delete.getObjectName()).isEqualTo("CLM.PROD.CLMMAST");
                assertThat(delete.definesDataSet()).isFalse();

                IdcamsCommand define = commands.get(2);
                assertThat(define.getVerb()).isEqualTo("DEFINE");
                assertThat(define.getObjectType()).isEqualTo("CLUSTER");
                assertThat(define.getObjectName()).isEqualTo("CLM.PROD.CLMMAST");
                assertThat(define.definesDataSet()).isTrue();
                // The components are catalog entries of their own, so a job may name either.
                assertThat(define.getDefinedNames()).containsExactly(
                  "CLM.PROD.CLMMAST", "CLM.PROD.CLMMAST.DATA", "CLM.PROD.CLMMAST.INDEX");
                // The key the copybook has to agree with, which nothing else in the estate says.
                assertThat(define.getLine()).isEqualTo(4);
            })
          )
        );
    }

    @Test
    void defineGdgIsADataSetAndDefineAliasIsNot() {
        rewriteRun(
          idcamsCard(
            """
              DEFINE GDG (NAME(CLM.PROD.EXTRACT)         LIMIT(7)  NOEMPTY SCRATCH)
              DEF ALIAS (NAME('SYS1.DB2.V9.SDSNLOAD') -
              REL('SYS1.DB2.V12.SDSNLOAD')) CAT('CAT.MCAT')
              """,
            spec -> spec.afterRecipe(cu -> {
                List<IdcamsCommand> commands = new IdcamsCommand.Matcher().lower(cu).collect(Collectors.toList());

                IdcamsCommand gdg = commands.get(0);
                assertThat(gdg.getObjectType()).isEqualTo("GDG");
                assertThat(gdg.getObjectName()).isEqualTo("CLM.PROD.EXTRACT");
                assertThat(gdg.getDefinedNames()).containsExactly("CLM.PROD.EXTRACT");

                // DEF is DEFINE written short, and an alias is a catalog entry rather than a file.
                IdcamsCommand alias = commands.get(1);
                assertThat(alias.getVerb()).isEqualTo("DEFINE");
                assertThat(alias.getObjectType()).isEqualTo("ALIAS");
                assertThat(alias.getObjectName()).isEqualTo("SYS1.DB2.V9.SDSNLOAD");
                assertThat(alias.definesDataSet()).isFalse();
            })
          )
        );
    }

    /**
     * The object type is written on one card and the group it belongs to opened on the next, which is
     * how every GDG in CardDemo is defined.
     */
    @Test
    void groupOpenedOnTheCardAfterTheObjectType() {
        rewriteRun(
          idcamsCard(
            """
                 DEFINE GENERATIONDATAGROUP -
                 (NAME(AWS.M2.CARDDEMO.TRANSACT.BKUP) -
                  LIMIT(5) -
                  SCRATCH -
                 )
                 IF LASTCC=12 THEN SET MAXCC=0
              """,
            spec -> spec.afterRecipe(cu -> {
                List<IdcamsCommand> commands = new IdcamsCommand.Matcher().lower(cu).collect(Collectors.toList());
                assertThat(commands).hasSize(2);

                IdcamsCommand define = commands.get(0);
                assertThat(define.getObjectType()).isEqualTo("GDG");
                assertThat(define.getObjectName()).isEqualTo("AWS.M2.CARDDEMO.TRANSACT.BKUP");

                // A modal command names no object, and the DEFINE above it does not claim its cards.
                assertThat(commands.get(1).getVerb()).isEqualTo("IF");
                assertThat(commands.get(1).definesDataSet()).isFalse();
            })
          )
        );
    }

    /**
     * {@code NEWNAME} ends in the letters {@code NAME}, so reading a group's name has to know where a
     * keyword begins.
     */
    @Test
    void alterNamesTheEntryItRenames() {
        rewriteRun(
          idcamsCard(
            """
              ALTER VSAM.OLD NEWNAME(VSAM.NEW)
              LISTCAT ENTRIES(VSAM.NEW) ALL
              """,
            spec -> spec.afterRecipe(cu -> {
                List<IdcamsCommand> commands = new IdcamsCommand.Matcher().lower(cu).collect(Collectors.toList());
                assertThat(commands.get(0).getObjectName()).isEqualTo("VSAM.OLD");
                assertThat(commands.get(0).getParameter("NEWNAME")).isEqualTo("VSAM.NEW");
                assertThat(commands.get(1).getObjectName()).isEqualTo("VSAM.NEW");
            })
          )
        );
    }

    /**
     * A REPRO names DDs, and which data sets those are bound to is written in the JCL and nowhere
     * here.
     */
    @Test
    void reproNamesDdsRatherThanDataSets() {
        rewriteRun(
          idcamsCard(
            """
              REPRO INFILE(BACKUP) OUTFILE(MASTER) REPLACE
              """,
            spec -> spec.afterRecipe(cu -> {
                IdcamsCommand repro = new IdcamsCommand.Matcher().lower(cu).collect(Collectors.toList()).get(0);
                assertThat(repro.getVerb()).isEqualTo("REPRO");
                assertThat(repro.getObjectName()).isNull();
                assertThat(repro.definesDataSet()).isFalse();
                assertThat(repro.getParameter("INFILE", "IFILE")).isEqualTo("BACKUP");
                assertThat(repro.getParameter("OUTFILE", "OFILE")).isEqualTo("MASTER");
            })
          )
        );
    }
}
