/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.jcl.tree;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.jcl.tree.ParserAssertions.jcl;

public class JclTest implements RewriteTest {

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
    void ddStream() {
        rewriteRun(
          jcl(
            """
              //%%JOBNAME.%%OTHER JOB (1,2,3),
              //    'NAME',
              //    MSGCLASS=A
              /*
              //*
              //JOB1 JOB ,'H.H. MORRILL'
              """
          )
        );
    }

    @Test
    void commentArea() {
        rewriteRun(
          jcl(
            "//NAME                                                                  commentArea"
          )
        );
    }

    @Test
    void trailingCommentAndCommentArea() {
        rewriteRun(
          jcl(
            """
              //OBJECT DD *    * Why is this possible?                                commentArea
                REPL DBN=%%NAME.FIELD,SEG=NAME,KEY=(1,2,3),
                 FIELDS=(ABC=XYZ)
                REPL DBN=%%NAME.FIELD,SEG=NAME,KEY=(4,5,6),
                 FIELDS=(ABC=XYZ)
              """
          )
        );
    }

    @Test
    void unknown() {
        rewriteRun(
          jcl(
            """
              //* %%RANGE 1 42
                TRACE OFF
                MAXCOMMAND 42
                CALLMEM SET42
                CALLMEM SET42
                 SETOLOC %%MAXRC = 42
              %%RANGE 1 42
              CALLMEM ORDERJOB %%LIBRO LCOPYJCL ALL
              %%DATTR FORCE C
              %% RANGE 1 42
              """
          )
        );
    }
}
