/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.controlm.tree;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.controlm.tree.ParserAssertions.controlM;

public class CompilationUnitTest implements RewriteTest {

    @Test
    void schedule() {
        rewriteRun(
          controlM(
            """
                +---------------------------------- BROWSE -----------------------------------+
                | MEMNAME =HELLO_WORLD=    MEMLIB   PRD.HELLO_WORLD.JCL                       |
                | OWNER   %%USER_NAME    TASKTYPE JOB    PREVENT-NCT2 Y DFLT  N               |
                | APPL H-E-L-L-O                          GROUP HWOR#090                      |
                | DESC PRINT HELLO WORLD                                                      |
                |                                                                             |
                | OVERLIB HELLO.WORLD.EXAMPLE.JCL.OVERLIB                   STAT CAL          |
                | SCHENV                         SYSTEM ID                  NJE NODE          |
                | SET VAR %%NAME=NAME                                                         |
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
                | IN       HWOR#_HELLOPARAM_OK    ODAT                                        |
                | CONTROL                                                                     |
                | RESOURCE INIT5                0001          UNICPAL              0001       |
                |          STOPALL              0001          DB2S                 0001       |
                |                                                                             |
                | FROM TIME         +     DAYS    UNTIL TIME      +     DAYS                  |
                | DUE OUT TIME      +     DAYS    PRIORITY NN  SAC    CONFIRM                 |
                | TIME ZONE:                                                                  |
                | =========================================================================== |
                | OUT      HWOR#_HELLOOUT_OK    ODAT +                                        |
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
}
