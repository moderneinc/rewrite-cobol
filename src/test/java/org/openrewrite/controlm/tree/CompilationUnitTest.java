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
package org.openrewrite.controlm.tree;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.controlm.tree.ParserAssertions.controlM;

class CompilationUnitTest implements RewriteTest {

    @Test
    void blankFile() {
        rewriteRun(
          controlM("")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
      "NAME",
      "N%%",
      "N..E",
      "N??E",
      "N**E",
      "N--E",
      "N&&E",
      "(||)"
    })
    void names(String input) {
        rewriteRun(
          controlM(
            // Code is intentionally shifted since whitespace may exist before the characters.
            """
              +---------------------------------- BROWSE -----------------------------------+
                | MEMNAME %s    MEMLIB   PRD.HELLO_WORLD.JCL                             |
                | OWNER   HELLO_WORLD    TASKTYPE JOB    PREVENT-NCT2 Y DFLT  N               |
                | APPL HELLO_WORLD                         GROUP HELLO_WORLD                  |
                | DESC PRINT HELLO WORLD                                                      |
                |                                                                             |
                | OVERLIB HELLO_WORLD                                       STAT CAL          |
                | SCHENV                         SYSTEM ID                  NJE NODE          |
                | SET VAR HELLO_WORLD=HELLO_WORLD                                             |
                | SET VAR                                                                     |
                | CTB STEP AT         NAME            TYPE                                    |
                | DOCMEM  HELLO_WORLD    DOCLIB   HELLO.WORLD.TEXT.JOB                        |
                | =========================================================================== |
                | SCHEDULE RBC                                                                |
                | RELATIONSHIP (AND/OR) O                                                     |
                | DAYS                                                          DCAL          |
                |                                                                    AND/OR   |
                |                                                                             |
                | WDAYS   0                                                     WCAL          |
                | MONTHS  1- Y 2- Y 3- Y 4- Y 5- Y 6- Y 7- Y 8- Y 9- Y 10- Y 11- Y 12- Y      |
                | DATES                                                                       |
                | CONFCAL          SHIFT       RETRO N MAXWAIT 70  D-CAT                      |
                | MINIMUM          PDS                                                        |
                | DEFINITION ACTIVE FROM          UNTIL                                       |
                | =========================================================================== |
                | IN       HELLO_WORLD    ODAT                                                |
                | CONTROL                                                                     |
                | RESOURCE INIT5                0001          UNICPAL              0001       |
                |          STOPALL              0001          DB2S                 0001       |
                |                                                                             |
                | FROM TIME         +     DAYS    UNTIL TIME      +     DAYS                  |
                | DUE OUT TIME      +     DAYS    PRIORITY NN  SAC    CONFIRM                 |
                | TIME ZONE:                                                                  |
                | =========================================================================== |
                | OUT      HELLO_WORLD    ODAT +                                              |
                | AUTO-ARCHIVE Y          SYSDB    Y      MAXDAYS      MAXRUNS                |
                | SYSOUT OP   (C,D,F,N,R)                                              FROM   |
                | MAXRERUN      RERUNMEM                                                      |
                | CAPTURE BY   (W - WORD / C - CHAR)                                          |
                | CYCLIC TYPE: C                                   INTERVAL         FROM      |
                | INTERVAL SEQUENCE: +         +         +         +         +                |
                | SPECIFIC TIMES:                                             TOLERANCE       |
                |                       +           +           +           +           +     |
                |                                                                             |
                | STEP RANGE         FR (PGM.PROC)          .          TO          .          |
                | ON PGMST ANYSTEP  PROCST          CODES <C0005                        A/O   |
                |   DO OK                                                                     |
                | ON PGMST ANYSTEP  PROCST          CODES NOTOK                         A/O   |
                |   DO IFRERUN  FROM $EXERR   .          TO          .              CONFIRM N |
                | ON PGMST ANYSTEP  PROCST          CODES OK                            A/O   |
                |   DO SYSOUT   OPT  C PRM C                                            FRM   |
                |   DO SYSOUT   OPT  R PRM                                              FRM   |
                |   DO                                                                        |
                | ON PGMST          PROCST          CODES                               A/O   |
                |   DO                                                                        |
                | ON SYSOUT                                          FROM     TO        A/O   |
                |   DO                                                                        |
                | ON VAR                                                                      |
                |   DO                                                                        |
                | SHOUT WHEN NOTOK    TIME       +     DAYS      TO U-MECO           URGN R   |
                |   MS %%JOBNAME ENDED NOTOK!   SUPPORT => H_247  -- J69                      |
                | SHOUT WHEN          TIME       +     DAYS      TO                  URGN     |
                |   MS                                                                        |
                | =========================================================================== |
                | APPL TYPE                                  APPL VER                         |
                | APPL FORM                                  CM   VER                         |
                | INSTREAM JCL: N                                                             |
                |                                                                             |
              """.formatted(input)
          )
        );
    }

    @Test
    void nullParameters() {
        rewriteRun(
          controlM(
            """
              +---------------------------------- BROWSE -----------------------------------+
              | MEMNAME                MEMLIB                                               |
              | OWNER                  TASKTYPE         PREVENT-NCT2 DFLT                   |
              | APPL                                    GROUP                               |
              | DESC                                                                        |
              |                                                                             |
              | OVERLIB                                                   STAT CAL          |
              | SCHENV                         SYSTEM ID                  NJE NODE          |
              | SET VAR                                                                     |
              | CTB STEP AT         NAME            TYPE                                    |
              | DOCMEM                 DOCLIB                                               |
              | =========================================================================== |
              | SCHEDULE RBC                                                                |
              | RELATIONSHIP (AND/OR) O                                                     |
              | DAYS                                                          DCAL          |
              |                                                                    AND/OR   |
              |                                                                             |
              | WDAYS   0                                                     WCAL          |
              | MONTHS  1- Y 2- Y 3- Y 4- Y 5- Y 6- Y 7- Y 8- Y 9- Y 10- Y 11- Y 12- Y      |
              | DATES                                                                       |
              | CONFCAL          SHIFT       RETRO N MAXWAIT 70  D-CAT                      |
              | MINIMUM          PDS                                                        |
              | DEFINITION ACTIVE FROM          UNTIL                                       |
              | =========================================================================== |
              | IN                            ODAT                                          |
              | CONTROL                                                                     |
              | RESOURCE INIT5                0001          UNICPAL              0001       |
              |          STOPALL              0001          DB2S                 0001       |
              |                                                                             |
              | FROM TIME         +     DAYS    UNTIL TIME      +     DAYS                  |
              | DUE OUT TIME      +     DAYS    PRIORITY NN  SAC    CONFIRM                 |
              | TIME ZONE:                                                                  |
              | =========================================================================== |
              | OUT                           ODAT                                          |
              | AUTO-ARCHIVE Y          SYSDB    Y      MAXDAYS      MAXRUNS                |
              | SYSOUT OP   (C,D,F,N,R)                                              FROM   |
              | MAXRERUN      RERUNMEM                                                      |
              | CAPTURE BY   (W - WORD / C - CHAR)                                          |
              | CYCLIC TYPE: C                                   INTERVAL         FROM      |
              | INTERVAL SEQUENCE: +         +         +         +         +                |
              | SPECIFIC TIMES:                                             TOLERANCE       |
              |                       +           +           +           +           +     |
              |                                                                             |
              | STEP RANGE         FR (PGM.PROC)          .          TO          .          |
              | ON PGMST ANYSTEP  PROCST          CODES <C0005                        A/O   |
              |   DO OK                                                                     |
              | ON PGMST ANYSTEP  PROCST          CODES NOTOK                         A/O   |
              |   DO IFRERUN  FROM $EXERR   .          TO          .              CONFIRM N |
              | ON PGMST ANYSTEP  PROCST          CODES OK                            A/O   |
              |   DO SYSOUT   OPT  C PRM C                                            FRM   |
              |   DO SYSOUT   OPT  R PRM                                              FRM   |
              |   DO                                                                        |
              | ON PGMST          PROCST          CODES                               A/O   |
              |   DO                                                                        |
              | ON SYSOUT                                          FROM     TO        A/O   |
              |   DO                                                                        |
              | ON VAR                                                                      |
              |   DO                                                                        |
              | SHOUT WHEN NOTOK    TIME       +     DAYS      TO U-MECO           URGN R   |
              |   MS %%JOBNAME ENDED NOTOK!   SUPPORT => H_247  -- J69                      |
              | SHOUT WHEN          TIME       +     DAYS      TO                  URGN     |
              |   MS                                                                        |
              | =========================================================================== |
              | APPL TYPE                                  APPL VER                         |
              | APPL FORM                                  CM   VER                         |
              | INSTREAM JCL: N                                                             |
              |                                                                             |
              """
          )
        );
    }

    @Test
    void multipleInAndOutNames() {
        rewriteRun(
          controlM(
            """
              +---------------------------------- BROWSE -----------------------------------+
              | MEMNAME                MEMLIB                                               |
              | OWNER                  TASKTYPE         PREVENT-NCT2 DFLT                   |
              | APPL                                    GROUP                               |
              | DESC                                                                        |
              |                                                                             |
              | OVERLIB                                                   STAT CAL          |
              | SCHENV                         SYSTEM ID                  NJE NODE          |
              | SET VAR                                                                     |
              | CTB STEP AT         NAME            TYPE                                    |
              | DOCMEM                 DOCLIB                                               |
              | =========================================================================== |
              | SCHEDULE RBC                                                                |
              | RELATIONSHIP (AND/OR) O                                                     |
              | DAYS                                                          DCAL          |
              |                                                                    AND/OR   |
              |                                                                             |
              | WDAYS   0                                                     WCAL          |
              | MONTHS  1- Y 2- Y 3- Y 4- Y 5- Y 6- Y 7- Y 8- Y 9- Y 10- Y 11- Y 12- Y      |
              | DATES                                                                       |
              | CONFCAL          SHIFT       RETRO N MAXWAIT 70  D-CAT                      |
              | MINIMUM          PDS                                                        |
              | DEFINITION ACTIVE FROM          UNTIL                                       |
              | =========================================================================== |
              | IN         NAME ODAT NAME2 ****                                             |
              |            NAME ODAT NAME2 ****                                             |
              |            NAME ODAT NAME2 ****                                             |
              |            NAME ODAT NAME2 ****                                             |
              | CONTROL                                                                     |
              | RESOURCE INIT5                0001          UNICPAL              0001       |
              |          STOPALL              0001          DB2S                 0001       |
              |                                                                             |
              | FROM TIME         +     DAYS    UNTIL TIME      +     DAYS                  |
              | DUE OUT TIME      +     DAYS    PRIORITY NN  SAC    CONFIRM                 |
              | TIME ZONE:                                                                  |
              | =========================================================================== |
              | OUT        NAME ODAT NAME2 ****                                             |
              |            NAME ODAT NAME2 ****                                             |
              |            NAME ODAT NAME2 ****                                             |
              |            NAME ODAT NAME2 ****                                             |
              | AUTO-ARCHIVE Y          SYSDB    Y      MAXDAYS      MAXRUNS                |
              | SYSOUT OP   (C,D,F,N,R)                                              FROM   |
              | MAXRERUN      RERUNMEM                                                      |
              | CAPTURE BY   (W - WORD / C - CHAR)                                          |
              | CYCLIC TYPE: C                                   INTERVAL         FROM      |
              | INTERVAL SEQUENCE: +         +         +         +         +                |
              | SPECIFIC TIMES:                                             TOLERANCE       |
              |                       +           +           +           +           +     |
              |                                                                             |
              | STEP RANGE         FR (PGM.PROC)          .          TO          .          |
              | ON PGMST ANYSTEP  PROCST          CODES <C0005                        A/O   |
              |   DO OK                                                                     |
              | ON PGMST ANYSTEP  PROCST          CODES NOTOK                         A/O   |
              |   DO IFRERUN  FROM $EXERR   .          TO          .              CONFIRM N |
              | ON PGMST ANYSTEP  PROCST          CODES OK                            A/O   |
              |   DO SYSOUT   OPT  C PRM C                                            FRM   |
              |   DO SYSOUT   OPT  R PRM                                              FRM   |
              |   DO                                                                        |
              | ON PGMST          PROCST          CODES                               A/O   |
              |   DO                                                                        |
              | ON SYSOUT                                          FROM     TO        A/O   |
              |   DO                                                                        |
              | ON VAR                                                                      |
              |   DO                                                                        |
              | SHOUT WHEN NOTOK    TIME       +     DAYS      TO U-MECO           URGN R   |
              |   MS %%JOBNAME ENDED NOTOK!   SUPPORT => H_247  -- J69                      |
              | SHOUT WHEN          TIME       +     DAYS      TO                  URGN     |
              |   MS                                                                        |
              | =========================================================================== |
              | APPL TYPE                                  APPL VER                         |
              | APPL FORM                                  CM   VER                         |
              | INSTREAM JCL: N                                                             |
              |                                                                             |
              """
          )
        );
    }

    @Test
    void descWithKeywords() {
        rewriteRun(
          controlM(
            """
              +---------------------------------- BROWSE -----------------------------------+
              | MEMNAME                MEMLIB                                               |
              | OWNER                  TASKTYPE         PREVENT-NCT2 DFLT                   |
              | APPL                                    GROUP                               |
              | DESC  MEMNAME MEMLIB OWNER TASKTYPE | | because it happens.                 |
              |                                                                             |
              | OVERLIB                                                   STAT CAL          |
              | SCHENV                         SYSTEM ID                  NJE NODE          |
              | SET VAR                                                                     |
              | CTB STEP AT         NAME            TYPE                                    |
              | DOCMEM                 DOCLIB                                               |
              | =========================================================================== |
              | SCHEDULE RBC                                                                |
              | RELATIONSHIP (AND/OR) O                                                     |
              | DAYS                                                          DCAL          |
              |                                                                    AND/OR   |
              |                                                                             |
              | WDAYS   0                                                     WCAL          |
              | MONTHS  1- Y 2- Y 3- Y 4- Y 5- Y 6- Y 7- Y 8- Y 9- Y 10- Y 11- Y 12- Y      |
              | DATES                                                                       |
              | CONFCAL          SHIFT       RETRO N MAXWAIT 70  D-CAT                      |
              | MINIMUM          PDS                                                        |
              | DEFINITION ACTIVE FROM          UNTIL                                       |
              | =========================================================================== |
              | IN                            ODAT                                          |
              | CONTROL                                                                     |
              | RESOURCE INIT5                0001          UNICPAL              0001       |
              |          STOPALL              0001          DB2S                 0001       |
              |                                                                             |
              | FROM TIME         +     DAYS    UNTIL TIME      +     DAYS                  |
              | DUE OUT TIME      +     DAYS    PRIORITY NN  SAC    CONFIRM                 |
              | TIME ZONE:                                                                  |
              | =========================================================================== |
              | OUT                           ODAT                                          |
              | AUTO-ARCHIVE Y          SYSDB    Y      MAXDAYS      MAXRUNS                |
              | SYSOUT OP   (C,D,F,N,R)                                              FROM   |
              | MAXRERUN      RERUNMEM                                                      |
              | CAPTURE BY   (W - WORD / C - CHAR)                                          |
              | CYCLIC TYPE: C                                   INTERVAL         FROM      |
              | INTERVAL SEQUENCE: +         +         +         +         +                |
              | SPECIFIC TIMES:                                             TOLERANCE       |
              |                       +           +           +           +           +     |
              |                                                                             |
              | STEP RANGE         FR (PGM.PROC)          .          TO          .          |
              | ON PGMST ANYSTEP  PROCST          CODES <C0005                        A/O   |
              |   DO OK                                                                     |
              | ON PGMST ANYSTEP  PROCST          CODES NOTOK                         A/O   |
              |   DO IFRERUN  FROM $EXERR   .          TO          .              CONFIRM N |
              | ON PGMST ANYSTEP  PROCST          CODES OK                            A/O   |
              |   DO SYSOUT   OPT  C PRM C                                            FRM   |
              |   DO SYSOUT   OPT  R PRM                                              FRM   |
              |   DO                                                                        |
              | ON PGMST          PROCST          CODES                               A/O   |
              |   DO                                                                        |
              | ON SYSOUT                                          FROM     TO        A/O   |
              |   DO                                                                        |
              | ON VAR                                                                      |
              |   DO                                                                        |
              | SHOUT WHEN NOTOK    TIME       +     DAYS      TO U-MECO           URGN R   |
              |   MS %%JOBNAME ENDED NOTOK!   SUPPORT => H_247  -- J69                      |
              | SHOUT WHEN          TIME       +     DAYS      TO                  URGN     |
              |   MS                                                                        |
              | =========================================================================== |
              | APPL TYPE                                  APPL VER                         |
              | APPL FORM                                  CM   VER                         |
              | INSTREAM JCL: N                                                             |
              |                                                                             |
              """
          )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
      "ODAT  ",
      "PREV  ",
      "STAT  ",
      "????  ",
      "****  ",
      "+123  ",
      "-123  ",
      "ODAT +",
      "ODAT -",
    })
    void outParams(String input) {
        rewriteRun(
          controlM(
            """
              +---------------------------------- BROWSE -----------------------------------+
              | MEMNAME                MEMLIB                                               |
              | OWNER                  TASKTYPE         PREVENT-NCT2 DFLT                   |
              | APPL                                    GROUP                               |
              | DESC                                                                        |
              |                                                                             |
              | OVERLIB                                                   STAT CAL          |
              | SCHENV                         SYSTEM ID                  NJE NODE          |
              | SET VAR                                                                     |
              | CTB STEP AT         NAME            TYPE                                    |
              | DOCMEM                 DOCLIB                                               |
              | =========================================================================== |
              | SCHEDULE RBC                                                                |
              | RELATIONSHIP (AND/OR) O                                                     |
              | DAYS                                                          DCAL          |
              |                                                                    AND/OR   |
              |                                                                             |
              | WDAYS   0                                                     WCAL          |
              | MONTHS  1- Y 2- Y 3- Y 4- Y 5- Y 6- Y 7- Y 8- Y 9- Y 10- Y 11- Y 12- Y      |
              | DATES                                                                       |
              | CONFCAL          SHIFT       RETRO N MAXWAIT 70  D-CAT                      |
              | MINIMUM          PDS                                                        |
              | DEFINITION ACTIVE FROM          UNTIL                                       |
              | =========================================================================== |
              | IN                            ODAT %s                                    |
              | CONTROL                                                                     |
              | RESOURCE INIT5                0001          UNICPAL              0001       |
              |          STOPALL              0001          DB2S                 0001       |
              |                                                                             |
              | FROM TIME         +     DAYS    UNTIL TIME      +     DAYS                  |
              | DUE OUT TIME      +     DAYS    PRIORITY NN  SAC    CONFIRM                 |
              | TIME ZONE:                                                                  |
              | =========================================================================== |
              | OUT                           ODAT                                          |
              | AUTO-ARCHIVE Y          SYSDB    Y      MAXDAYS      MAXRUNS                |
              | SYSOUT OP   (C,D,F,N,R)                                              FROM   |
              | MAXRERUN      RERUNMEM                                                      |
              | CAPTURE BY   (W - WORD / C - CHAR)                                          |
              | CYCLIC TYPE: C                                   INTERVAL         FROM      |
              | INTERVAL SEQUENCE: +         +         +         +         +                |
              | SPECIFIC TIMES:                                             TOLERANCE       |
              |                       +           +           +           +           +     |
              |                                                                             |
              | STEP RANGE         FR (PGM.PROC)          .          TO          .          |
              | ON PGMST ANYSTEP  PROCST          CODES <C0005                        A/O   |
              |   DO OK                                                                     |
              | ON PGMST ANYSTEP  PROCST          CODES NOTOK                         A/O   |
              |   DO IFRERUN  FROM $EXERR   .          TO          .              CONFIRM N |
              | ON PGMST ANYSTEP  PROCST          CODES OK                            A/O   |
              |   DO SYSOUT   OPT  C PRM C                                            FRM   |
              |   DO SYSOUT   OPT  R PRM                                              FRM   |
              |   DO                                                                        |
              | ON PGMST          PROCST          CODES                               A/O   |
              |   DO                                                                        |
              | ON SYSOUT                                          FROM     TO        A/O   |
              |   DO                                                                        |
              | ON VAR                                                                      |
              |   DO                                                                        |
              | SHOUT WHEN NOTOK    TIME       +     DAYS      TO U-MECO           URGN R   |
              |   MS %%JOBNAME ENDED NOTOK!   SUPPORT => H_247  -- J69                      |
              | SHOUT WHEN          TIME       +     DAYS      TO                  URGN     |
              |   MS                                                                        |
              | =========================================================================== |
              | APPL TYPE                                  APPL VER                         |
              | APPL FORM                                  CM   VER                         |
              | INSTREAM JCL: N                                                             |
              |                                                                             |
              """.formatted(input)
          )
        );
    }
}
