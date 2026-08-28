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
package org.openrewrite.mainframe.jcl.tree;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.mainframe.jcl.tree.ParserAssertions.jcl;

class ContinuationTest implements RewriteTest {

    @Test
    void splitByParam() {
        rewriteRun(
          jcl(
            """
            //Name DD DSNAME=DS4,UNIT=3380,VOL=SER=111112,
            //      DISP=(NEW,KEEP),SPACE=(TRK,(5,1,2))
            """
          )
        );
    }

    @Test
    void endCommaWithWhitespace() {
        rewriteRun(
          jcl(
            """
            //Name DD DSNAME=DS4,UNIT=3380,VOL=SER=111112         ,
            //      DISP=(NEW,KEEP),SPACE=(TRK,(5,1,2))
            """
          )
        );
    }

    @Test
    void commentAreas() {
        rewriteRun(
          jcl(
            """
            //Name DD DSNAME=DS4,                                                   commentArea
            //        UNIT=3380,                                                    commentArea
            //        VOL=SER=111112         ,                                      commentArea
            //        DISP=(NEW,KEEP),SPACE=(TRK,                                   commentArea
            //                              (5,1,2))                                commentArea
            """
          )
        );
    }

    @Test
    void blankLabelsAndContinuations() {
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
            //Name DD DSNAME=DS4,UNIT=3380,VOL=SER=111112,
            //      DISP=(NEW,KEEP),SPACE=(TRK,(5,1,2))
            //LOG      DD DSN=SYS1.TSODUMP.LOG,DISP=SHR
            //SYSPRINT DD SYSOUT=*
            //         PEND
            //         EXEC DUMPCHK
            """
          )
        );
    }
}
