/*
 * Copyright 2022 the original author or authors.
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
package org.openrewrite.cobol.tree

import org.junit.jupiter.api.Test
import org.openrewrite.ExecutionContext
import org.openrewrite.cobol.Assertions.cobol
import org.openrewrite.cobol.CobolVisitor
import org.openrewrite.test.RecipeSpec
import org.openrewrite.test.RewriteTest
import org.openrewrite.test.RewriteTest.toRecipe

class CobolNistTest : RewriteTest {

    override fun defaults(spec: RecipeSpec) {
        spec.recipe(toRecipe {
            object : CobolVisitor<ExecutionContext>() {
                override fun visitSpace(space: Space, p: ExecutionContext): Space {
                    if (space.whitespace.trim().isNotEmpty()) {
                        return space.withWhitespace("(~~>${space.whitespace}<~~)")
                    }
                    return space
                }
            }
        })
    }

    @Test
    fun test() = rewriteRun(
        cobol("""
      *HEADER,COBOL,CM303M                                                      
000100 IDENTIFICATION DIVISION.                                         CM3034.2
000200 PROGRAM-ID.                                                      CM3034.2
000300     CM303M.                                                      CM3034.2
000400*THE FOLLOWING PROGRAM TESTS THE FLAGGING OF                      CM3034.2
000500*OBSOLETE FEATURES THAT ARE USED IN COMMUNICATIONS.               CM3034.2
000600 ENVIRONMENT DIVISION.                                            CM3034.2
000700 CONFIGURATION SECTION.                                           CM3034.2
000800 SOURCE-COMPUTER.                                                 CM3034.2
000900     XXXXX082.                                                    CM3034.2
001000 OBJECT-COMPUTER.                                                 CM3034.2
001100     XXXXX083.                                                    CM3034.2
001200                                                                  CM3034.2
001300                                                                  CM3034.2
001400 DATA DIVISION.                                                   CM3034.2
001500 FILE SECTION.                                                    CM3034.2
001600 COMMUNICATION SECTION.                                           CM3034.2
001700 CD COMMNAME FOR INITIAL INPUT.                                   CM3034.2
001800 01 CREC.                                                         CM3034.2
001900     03 CNAME1 PIC X(87).                                         CM3034.2
002000                                                                  CM3034.2
002100 PROCEDURE DIVISION.                                              CM3034.2
002200                                                                  CM3034.2
002300 CM303M-CONTROL.                                                  CM3034.2
002400     PERFORM CM303M-DISABLE THRU CM303M-ENABLE.                   CM3034.2
002500     STOP RUN.                                                    CM3034.2
002600                                                                  CM3034.2
002700 CM303M-DISABLE.                                                  CM3034.2
002800     DISABLE INPUT COMMNAME WITH KEY CNAME1.                      CM3034.2
002900*Message expected for above statement: OBSOLETE                   CM3034.2
003000                                                                  CM3034.2
003100 CM303M-ENABLE.                                                   CM3034.2
003200     ENABLE INPUT COMMNAME WITH KEY CNAME1.                       CM3034.2
003300*Message expected for above statement: OBSOLETE                   CM3034.2
003400                                                                  CM3034.2
003500*TOTAL NUMBER OF FLAGS EXPECTED = 2.                              CM3034.2
      *END-OF,CM303M                                                            
        """)
    )
}
