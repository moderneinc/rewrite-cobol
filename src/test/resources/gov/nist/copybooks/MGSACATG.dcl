000000*    DCLGEN-generated DB2 host variable structure
           EXEC SQL DECLARE SAMPLE_TABLE TABLE
           ( COL_A                  CHAR(3) NOT NULL,
             COL_B                  CHAR(30) NOT NULL
           ) END-EXEC.
      *    COBOL DECLARATION FOR TABLE SAMPLE_TABLE
           01  DCLSAMPLE-TABLE.
               10 COL-A              PIC X(3).
               10 COL-B              PIC X(30).
