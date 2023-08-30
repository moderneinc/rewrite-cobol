000000*    EXEC SQL statement to declare a table
           EXEC SQL DECLARE PROD_TBL_02 TABLE
           ( NUM_1                  CHAR(3) NOT NULL,
             NUM_2                  CHAR(3) NOT NULL
           ) END-EXEC.
      * Create cursor for table 2
           EXEC SQL
               DECLARE CURSOR_IN_COPY CURSOR FOR
               SELECT NUM_1,
                      NUM_2
               FROM PROD_TBL_02
               FOR FETCH ONLY
           END-EXEC.