/*
 * Copyright 2024 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.jcl.tree;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.jcl.tree.ParserAssertions.jcl;

class CommentStatementTest implements RewriteTest {

    @Test
    void commentStatement() {
        rewriteRun(
          jcl("//* some comment")
        );
    }

    @Test
    void multipleComments() {
        rewriteRun(
          jcl(
            """
            //* THE COMMENT STATEMENT CANNOT BE CONTINUED,
            //* BUT IF YOU HAVE A LOT TO SAY, YOU CAN FOLLOW A
            //* COMMENT STATEMENT WITH MORE COMMENT
            //* STATEMENTS.
            """
          )
        );
    }

    @Test
    void semiColons() {
        rewriteRun(
          jcl(
            """
            //* THE COMMENT ; STATEMENT CANNOT BE CONTINUED,
            //* BUT IF YOU HAVE;  A LOT TO SAY, YOU CAN FOLLOW A
            //* COMMENT STATEMENT ; WITH MORE ; COMMENT
            //* STATEMENTS; .
            """
          )
        );
    }
    @Test
    void slashes() {
        rewriteRun(
          jcl(
			"""
              //* FOO/BAR/BUZ
              """
          )
        );
    }

    @Test
    void emptyCommentWithCommentArea() {
        rewriteRun(
          jcl(
			"""
              //*
              //*                                                                     commentArea
              //*
              """
          )
        );
    }

    @Test
    void lineComments() {
        rewriteRun(
          jcl(
			"""
              //*====================================================================*
              //*                                                                    *
              //*====================================================================*
              //*
              //**********************************************************************
              //* C2                                                                 *
              //**********************************************************************
              //NAME EXEC PGM=IEFBR14
              //JOB DD DSN=&&NAME,DISP=(NEW,PASS),UNIT=SYSDA,SPACE=(TRK,(1,1))
              """
          )
        );
    }

    @Test
    void crlf() {
        rewriteRun(
          jcl("//*\r\n" +
              "//*\r\n" +
              "//Name JCLLIB ORDER=NAME\r\n" +
              "//*\r\n" +
              "// SET NAME=%%OTHER\r\n" +
              "//*\r\n"
          )
        );
    }
}
