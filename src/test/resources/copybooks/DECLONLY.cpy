      ****************************************************************
      *  A table declaration is all this copybook has: preprocessing *
      *  elides the EXEC block, so it contributes no COBOL words.    *
      ****************************************************************
           EXEC SQL DECLARE CUSTOMER TABLE
              ( CUSTOMER_NUMBER                CHAR(10) NOT NULL,
                CUSTOMER_NAME                  CHAR(50) )
           END-EXEC.
