/*
 * For commercial customers of Moderne Inc., this repository is licensed per the terms of our contract.
 * For everyone else, this is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International.
 * See: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
package org.openrewrite.jcl.tree;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.jcl.tree.ParserAssertions.jcl;

public class ControlMTest implements RewriteTest {

    @Test
    void controlM() {
        rewriteRun(
          jcl("%%LIBSYM NAME.FIELD %%MEMSYM NAME.FIELD")
        );
    }

    @Test
    void conditionalParameter() {
        rewriteRun(
          jcl(
            """
              //Name DD DSNAME=DS4,
              %%IF (1 EQ 1) THEN
              // DISP=(NEW,KEEP)
              %%ELSE
              // DISP=(OLD,DELETE)
              %%ENDIF
              //* CM condition changes the parameter in the JCL file.
              """
          )
        );
    }
}
