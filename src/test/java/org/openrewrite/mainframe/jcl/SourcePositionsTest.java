/*
 * Copyright 2026 the original author or authors.
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
package org.openrewrite.mainframe.jcl;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.SourceFile;
import org.openrewrite.mainframe.jcl.tree.Jcl;
import org.openrewrite.mainframe.jcl.tree.Statement;
import org.openrewrite.marker.Range;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.mainframe.jcl.tree.ParserAssertions.procedureMember;

/**
 * Where each card of a job sits, read back out of the text the position was measured from.
 * <p>
 * These parse directly rather than through {@code ParserAssertions.jcl}, because the whole point is
 * where a character sits and the test framework trims a source block's common indentation.
 */
class SourcePositionsTest {

    private static final String JOB =
      "//PAYROLL  JOB (ACCT),'PAYROLL',CLASS=A\n" +
      "//STEP010  EXEC PGM=PAYCALC,PARM='WEEKLY'\n" +
      "//STEPLIB  DD DSN=CLM.PROD.LOADLIB,DISP=SHR\n" +
      "//SYSPRINT DD SYSOUT=*\n";

    @Test
    void placesEachStatementAtItsOwnWords() {
        Jcl.CompilationUnit cu = parse(JOB);
        SourcePositions positions = SourcePositions.of(cu);
        assertThat(positions.getSource()).isEqualTo(JOB);

        List<String> placed = new ArrayList<>();
        for (Statement statement : cu.getStatements()) {
            Range range = positions.get(statement);
            assertThat(range).isNotNull();
            placed.add(range.getStart().getLine() + ":" + range.getStart().getColumn() +
                       "-" + range.getEnd().getLine() + ":" + range.getEnd().getColumn() +
                       " " + positions.textOf(range));
        }

        assertThat(placed).containsExactly(
          "1:1-1:40 //PAYROLL  JOB (ACCT),'PAYROLL',CLASS=A",
          "2:1-2:42 //STEP010  EXEC PGM=PAYCALC,PARM='WEEKLY'",
          "3:1-3:44 //STEPLIB  DD DSN=CLM.PROD.LOADLIB,DISP=SHR",
          "4:1-4:23 //SYSPRINT DD SYSOUT=*");
    }

    @Test
    void placesEachParameterOfAStatement() {
        Jcl.CompilationUnit cu = parse(JOB);
        SourcePositions positions = SourcePositions.of(cu);
        Jcl.JobControlStatement step = statement(cu, "STEP010");

        Range program = positions.get(step.getParameter("PGM"));
        assertThat(program).isNotNull();
        assertThat(positions.textOf(program)).isEqualTo("PGM=PAYCALC,");
        assertThat(program.getStart().getLine()).isEqualTo(2);
        assertThat(program.getStart().getColumn()).isEqualTo(17);

        assertThat(positions.textOf(positions.get(step.getOperation()))).isEqualTo("EXEC");
        assertThat(positions.textOf(positions.get(step.getParameter("PARM")))).isEqualTo("PARM='WEEKLY'");
    }

    /**
     * A statement written over several cards is one statement, and its range covers every card of it.
     */
    @Test
    void placesAStatementContinuedOverSeveralCards() {
        String source =
          "//PAYROLL  JOB (ACCT),'PAYROLL'\n" +
          "//STEP010  EXEC PGM=PAYCALC,\n" +
          "//             PARM='WEEKLY',\n" +
          "//             REGION=4M\n";

        Jcl.CompilationUnit cu = parse(source);
        SourcePositions positions = SourcePositions.of(cu);
        Jcl.JobControlStatement step = statement(cu, "STEP010");

        Range range = positions.get(step);
        assertThat(range).isNotNull();
        assertThat(range.getStart().getLine()).isEqualTo(2);
        assertThat(range.getEnd().getLine()).isEqualTo(4);
        assertThat(positions.textOf(range)).endsWith("REGION=4M");

        Range region = positions.get(step.getParameter("REGION"));
        assertThat(region).isNotNull();
        assertThat(region.getStart().getLine()).isEqualTo(4);
        assertThat(positions.textOf(region)).isEqualTo("REGION=4M");
    }

    /**
     * A step a procedure wrote is a step the job runs, so it has to be anchored somewhere. Nothing of
     * it is written in the job, and the card that brought it in is: that is where the job opens, and
     * the member says where to look next.
     */
    @Test
    void anchorsAnExpandedStepAtTheCardThatBroughtItIn() {
        String source =
          "//PAYROLL  JOB (ACCT),'PAYROLL'\n" +
          "//STEP010  EXEC CLMBATCH,PGM=PAYCALC\n";

        Jcl.CompilationUnit cu = parse(source, singletonList(procedureMember("CLMBATCH",
          "//CLMBATCH PROC PGM=,HLQ=CLM.PROD\n" +
          "//RUN      EXEC PGM=&PGM\n" +
          "//STEPLIB  DD DISP=SHR,DSN=&HLQ..LOADLIB\n" +
          "//         PEND\n")));
        SourcePositions positions = SourcePositions.of(cu);

        Jcl.Expansion expansion = expansionIn(cu.getStatements());
        Statement run = expansion.getStatements().get(0);
        assertThat(positions.get(run)).isNull();

        SourcePositions.Expanded expanded = positions.expanded(run);
        assertThat(expanded).isNotNull();
        assertThat(expanded.getMemberName()).isEqualTo("CLMBATCH");
        assertThat(expanded.getText()).isEqualTo("//RUN      EXEC PGM=&PGM");
        assertThat(expanded.getRange().getStart().getLine()).isEqualTo(1);
        assertThat(expanded.getRange().getStart().getColumn()).isEqualTo(1);
        // The EXEC card, which is what the job has to open at.
        assertThat(expanded.getBroughtInAt().getStart().getLine()).isEqualTo(2);
        assertThat(positions.textOf(expanded.getBroughtInAt()))
          .isEqualTo("//STEP010  EXEC CLMBATCH,PGM=PAYCALC");

        SourcePositions.Expanded steplib = positions.expanded(expansion.getStatements().get(1));
        assertThat(steplib).isNotNull();
        assertThat(steplib.getRange().getStart().getLine()).isEqualTo(2);
        assertThat(steplib.getText()).isEqualTo("//STEPLIB  DD DISP=SHR,DSN=&HLQ..LOADLIB");
    }

    /**
     * A member that includes another is two members deep and one card wide: the statement was written
     * in the innermost member, and the job still opens at the card written in it.
     */
    @Test
    void namesTheInnermostMemberAStatementWasWrittenIn() {
        String source =
          "//PAYROLL  JOB (ACCT),'PAYROLL'\n" +
          "//         INCLUDE MEMBER=OUTER\n";

        List<Path> library = new ArrayList<>();
        library.add(procedureMember("OUTER",
          "//*        Shared setup.\n" +
          "//         INCLUDE MEMBER=INNER\n"));
        library.add(procedureMember("INNER",
          "//SYSPRINT DD SYSOUT=*\n"));

        Jcl.CompilationUnit cu = parse(source, library);
        SourcePositions positions = SourcePositions.of(cu);

        Jcl.Expansion outer = expansionIn(cu.getStatements());
        Jcl.Expansion inner = expansionIn(outer.getStatements());
        SourcePositions.Expanded expanded = positions.expanded(inner.getStatements().get(0));

        assertThat(expanded).isNotNull();
        assertThat(expanded.getMemberName()).isEqualTo("INNER");
        assertThat(expanded.getText()).isEqualTo("//SYSPRINT DD SYSOUT=*");
        assertThat(expanded.getRange().getStart().getLine()).isEqualTo(1);
        assertThat(positions.textOf(expanded.getBroughtInAt()))
          .isEqualTo("//         INCLUDE MEMBER=OUTER");
    }

    /**
     * Columns 73 to 80 carry a sequence number rather than JCL, so a statement ends at its last
     * operand. The whole card is what a viewer highlights and what a sort or IDCAMS deck is read
     * from, since a control card means something different in another column.
     */
    @Test
    void readsTheWholeCardAStatementWasWrittenOn() {
        String source =
          "//PAYROLL  JOB (ACCT),'PAYROLL'\n" +
          "//SYSPRINT DD SYSOUT=*" + blanks(52) + "00000200\n";

        Jcl.CompilationUnit cu = parse(source);
        SourcePositions positions = SourcePositions.of(cu);
        Statement dd = statement(cu, "SYSPRINT");

        assertThat(positions.textOf(positions.get(dd))).isEqualTo("//SYSPRINT DD SYSOUT=*");
        assertThat(positions.textOf(positions.card(dd)))
          .isEqualTo("//SYSPRINT DD SYSOUT=*" + blanks(52) + "00000200");
    }

    private static String blanks(int count) {
        StringBuilder blanks = new StringBuilder();
        for (int i = 0; i < count; i++) {
            blanks.append(' ');
        }
        return blanks.toString();
    }

    private static Jcl.JobControlStatement statement(Jcl.CompilationUnit cu, String name) {
        for (Statement statement : cu.getStatements()) {
            if (statement instanceof Jcl.JobControlStatement &&
                ((Jcl.JobControlStatement) statement).getSimpleName().equalsIgnoreCase(name)) {
                return (Jcl.JobControlStatement) statement;
            }
        }
        throw new AssertionError("no statement named " + name);
    }

    private static Jcl.Expansion expansionIn(List<Statement> statements) {
        for (Statement statement : statements) {
            if (statement instanceof Jcl.Expansion) {
                return (Jcl.Expansion) statement;
            }
        }
        throw new AssertionError("no expansion");
    }

    private static Jcl.CompilationUnit parse(String source) {
        return parse(source, emptyList());
    }

    private static Jcl.CompilationUnit parse(String source, List<Path> procedureLibrary) {
        List<SourceFile> parsed = JclParser.builder().procedureLibrary(procedureLibrary).build()
          .parse(new InMemoryExecutionContext(t -> {
              throw new IllegalStateException(t);
          }), source)
          .collect(Collectors.toList());
        assertThat(parsed).hasSize(1);
        assertThat(parsed.get(0)).isInstanceOf(Jcl.CompilationUnit.class);
        Jcl.CompilationUnit cu = (Jcl.CompilationUnit) parsed.get(0);
        assertThat(cu.printAll()).isEqualTo(source);
        return cu;
    }
}
