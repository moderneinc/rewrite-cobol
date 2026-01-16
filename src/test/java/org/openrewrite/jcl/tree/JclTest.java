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
package org.openrewrite.jcl.tree;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.jcl.tree.ParserAssertions.jcl;

class JclTest implements RewriteTest {

    @Test
    void example() {
        rewriteRun(
          jcl(
            """
            //JOB1 JOB ,'H.H. MORRILL'
            //ADD1 OUTPUT COPIES=2
            //STEPA EXEC PROC=P
            //PS1.OUTA OUTPUT CONTROL=DOUBLE,COPIES=5
            //PS1.DSB DD OUTPUT=*.ADD1
            //PS1.DSE DD *
            """
          )
        );
    }

    @Test
    void blankLabels() {
        rewriteRun(
          jcl(
            """
            //DUMPCHK  JOB 'accounting_info',MSGLEVEL=(1,1)
            //DUMPCHK  PROC
            //DUMPCHK  EXEC PGM=DMPCHKO,REGION=5M,PARM='/&SG,&JDATE,&DAY'
            //STEPLIB  DD DSN=JCR.PGM.LOAD,DISP=SHR
            //CDS      DD DSN=DATAMGT.CDS,DISP=SHR
            //         DD DSN=DATAMGT.CDS.CLEAR,DISP=SHR
            //         DD DSN=DATAMGT.CDS.Y43DUMPS,DISP=SHR
            //LOG      DD DSN=SYS1.TSODUMP.LOG,DISP=SHR
            //SYSPRINT DD SYSOUT=*
            //         PEND
            //         EXEC DUMPCHK
            """
          )
        );
    }

    @Test
    void mixed() {
        rewriteRun(
          jcl(
            """
            //*some comment
            //Name JOB
            //*DATASET
            //Name JOB
            /*JOBPARM
            """
          )
        );
    }

    @Test
    void variableRef() {
        rewriteRun(
          jcl(
            """
            //JOB1 JOB
            //ADD1 OUTPUT COPIES=%SYSUID
            //PS1.DSB DD DISP=SHR,DSN=&NAME..&NAME..OBJECT(&MBR)
            """
          )
        );
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "",
            "                                                                      commentArea"
        }
    )
    void emptyStatement(String input) {
        rewriteRun(
          jcl(
            "//%s".formatted(input)
          )
        );
    }

    @Test
    void emptyParameters() {
        rewriteRun(
          jcl(
            """
            //Name DD PARM=(IFP,%%BD,,,,,,%%NAME)
            """
          )
        );
    }

    @Test
    void leadingKeyword() {
        rewriteRun(
          jcl(
            """
            //JCLLIB DD DSN=NAME1,DISP=NAME2
            """
          )
        );
    }

    @Test
    void unicodeCharacters() {
        rewriteRun(
          jcl(
            """
            //ADD1 OUTPUT COPIES='ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÖØÙÚÛÜÝ'
            //ADD1 OUTPUT COPIES='àáâãäåæçèéêëìíîïðñòóôöøùúûüý'
            """
          )
        );
    }
}
