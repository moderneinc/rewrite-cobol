000000* Prevent trim
       IDENTIFICATION DIVISION.
       PROGRAM-ID . HELLO-WORLD.
       DATA DIVISION.
       COPY ISSUE_27.
       PROCEDURE DIVISION.
           SET X TO 10.
           SET Y TO 25.
           ADD X Y GIVING Z.
           DISPLAY "X + Y = "Z.
       STOP RUN.