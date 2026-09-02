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
package org.openrewrite.mainframe.jcl.tree;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openrewrite.mainframe.jcl.JclIsoVisitor;
import org.openrewrite.mainframe.jcl.JclVisitor;
import org.openrewrite.mainframe.jcl.marker.GeneratedParmContent;
import org.openrewrite.mainframe.jcl.marker.ParmMember;
import org.openrewrite.mainframe.jcl.tree.Jcl.DataDefinitionStream;
import org.openrewrite.mainframe.jcl.tree.Jcl.JobControlStatement;
import org.openrewrite.test.RewriteTest;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.mainframe.jcl.tree.ParserAssertions.jcl;
import static org.openrewrite.mainframe.jcl.tree.ParserAssertions.jclWithProcedures;
import static org.openrewrite.mainframe.jcl.tree.ParserAssertions.parmMember;
import static org.openrewrite.mainframe.jcl.tree.ParserAssertions.procedureMember;

class ExternalSysinTest implements RewriteTest {

    private static final List<Path> SORT_MEMBER = singletonList(parmMember("MGSLAP8F",
            """
              SORT FIELDS=(8,2,PD,A,10,4,PD,A,14,2,PD,A,16,1,CH,A)
              """));

    /**
     * The marker sits on the DSN parameter rather than on the statement, so the whole tree is
     * searched rather than the statement list.
     */
    private static Optional<ParmMember> parmMarker(Jcl.CompilationUnit cu) {
        List<ParmMember> found = new ArrayList<>();
        new JclIsoVisitor<List<ParmMember>>() {
            @Override
            public Jcl.KeywordParameter visitKeywordParameter(Jcl.KeywordParameter parameter,
                                                              List<ParmMember> markers) {
                parameter.getMarkers().findFirst(ParmMember.class).ifPresent(markers::add);
                return super.visitKeywordParameter(parameter, markers);
            }
        }.visit(cu, found);
        return found.stream().findFirst();
    }

    private static Optional<ParmMember> parmMarker(Jcl.CompilationUnit cu, ParmMember.Status status) {
        return parmMarker(cu).filter(m -> m.getStatus() == status);
    }

    private static List<String> graftedWords(Jcl.CompilationUnit cu) {
        return cu.getStatements().stream()
                .filter(s -> s instanceof DataDefinitionStream)
                .filter(s -> s.getMarkers().findFirst(GeneratedParmContent.class).isPresent())
                .map(s -> ((DataDefinitionStream) s).getWord().getText())
                .collect(toList());
    }

    /**
     * The grafted words wherever they were put, which for a procedure's own DD is inside the
     * expansion rather than among the job's own cards.
     */
    private static List<String> graftedAnywhere(Jcl.CompilationUnit cu) {
        List<String> found = new ArrayList<>();
        new JclVisitor<List<String>>() {
            @Override
            public Jcl visitDataDefinitionStream(DataDefinitionStream data, List<String> words) {
                if (data.getMarkers().findFirst(GeneratedParmContent.class).isPresent()) {
                    words.add(data.getWord().getText());
                }
                return super.visitDataDefinitionStream(data, words);
            }
        }.visit(cu, found);
        return found;
    }

    private static String wordText(Statement s) {
        if (s instanceof JobControlStatement) {
            return ((JobControlStatement) s).getName().getText();
        }
        if (s instanceof DataDefinitionStream) {
            return ((DataDefinitionStream) s).getWord().getText();
        }
        return "";
    }

    @Test
    void resolvesExternalSysinMember() {
        rewriteRun(
          jcl(
            "//SYSIN DD DSN=DWL.PARMLIB(MGSLAP8F),DISP=SHR",
            SORT_MEMBER,
            spec -> spec.afterRecipe(cu -> {
                Optional<ParmMember> marker = parmMarker(cu, ParmMember.Status.EXPANDED);
                assertThat(marker).isPresent();
                assertThat(marker.get().getDdName()).isEqualTo("SYSIN");
                assertThat(marker.get().getDataSetName()).isEqualTo("DWL.PARMLIB");
                assertThat(marker.get().getMemberName()).isEqualTo("MGSLAP8F");
            })
          )
        );
    }

    @Test
    void resolvesSystsinMember() {
        rewriteRun(
          jcl(
            "//SYSTSIN DD DSN=DWL.PARMLIB(MGSLAP8F),DISP=SHR",
            SORT_MEMBER,
            spec -> spec.afterRecipe(cu ->
                assertThat(parmMarker(cu, ParmMember.Status.EXPANDED)).isPresent())
          )
        );
    }

    @Test
    void caseInsensitiveMemberLookup() {
        rewriteRun(
          jcl(
            "//SYSIN DD DSN=dwl.parmlib(mgslap8f),disp=shr",
            SORT_MEMBER,
            spec -> spec.afterRecipe(cu ->
                assertThat(parmMarker(cu, ParmMember.Status.EXPANDED)).isPresent())
          )
        );
    }

    @Test
    void graftsResolvedMemberContentAsStreamNodes() {
        rewriteRun(
          jcl(
            "//SYSIN DD DSN=DWL.PARMLIB(MGSLAP8F),DISP=SHR",
            SORT_MEMBER,
            spec -> spec.afterRecipe(cu -> assertThat(graftedWords(cu)).containsExactly(
                "SORT",
                "FIELDS=(8,2,PD,A,10,4,PD,A,14,2,PD,A,16,1,CH,A)"))
          )
        );
    }

    @Test
    void graftedContentDoesNotAffectRoundTrip() {
        // The before == after (implicit) assertion verifies the grafted DataDefinitionStream
        // nodes are not re-printed, so the source is reproduced byte-for-byte.
        rewriteRun(
          jcl(
            """
              //STEP1   EXEC PGM=SORT
              //SYSIN    DD DSN=DWL.PARMLIB(MGSLAP8F),DISP=SHR
              //SYSOUT   DD SYSOUT=*
              """,
            SORT_MEMBER,
            spec -> spec.afterRecipe(cu -> assertThat(graftedWords(cu)).isNotEmpty())
          )
        );
    }

    @Test
    void missingMemberGraftsNothing() {
        rewriteRun(
          jcl(
            "//SYSIN DD DSN=DWL.PARMLIB(NOTHERE),DISP=SHR",
            SORT_MEMBER,
            spec -> spec.afterRecipe(cu -> assertThat(graftedWords(cu)).isEmpty())
          )
        );
    }

    @Test
    void marksMissingMemberWhenNotSupplied() {
        rewriteRun(
          jcl(
            "//SYSIN DD DSN=DWL.PARMLIB(NOTHERE),DISP=SHR",
            SORT_MEMBER,
            spec -> spec.afterRecipe(cu -> {
                Optional<ParmMember> marker = parmMarker(cu, ParmMember.Status.MISSING);
                assertThat(marker).isPresent();
                assertThat(marker.get().getMemberName()).isEqualTo("NOTHERE");
            })
          )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"OLD", "(SHR)", "(OLD,KEEP)", "(SHR,KEEP,KEEP)"})
    void expandsInputDispositions(String disp) {
        rewriteRun(
          jcl(
            "//SYSIN DD DSN=DWL.PARMLIB(MGSLAP8F),DISP=" + disp,
            SORT_MEMBER,
            spec -> spec.afterRecipe(cu ->
                assertThat(parmMarker(cu, ParmMember.Status.EXPANDED)).isPresent())
          )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"MOD", "(NEW,CATLG,DELETE)", "(MOD,KEEP)", "(,DELETE)"})
    void doesNotExpandNonInputDispositions(String disp) {
        rewriteRun(
          jcl(
            "//SYSIN DD DSN=DWL.PARMLIB(MGSLAP8F),DISP=" + disp,
            SORT_MEMBER,
            spec -> spec.afterRecipe(cu -> assertThat(parmMarker(cu)).isEmpty())
          )
        );
    }

    @Test
    void doesNotExpandWhenDispositionAbsent() {
        rewriteRun(
          jcl(
            "//SYSIN DD DSN=DWL.PARMLIB(MGSLAP8F)",
            SORT_MEMBER,
            spec -> spec.afterRecipe(cu -> assertThat(parmMarker(cu)).isEmpty())
          )
        );
    }

    @Test
    void expandsNonSysinControlDd() {
        // Expansion is resolution-driven, not gated on the DD name: any input DD whose
        // member is supplied is expanded.
        rewriteRun(
          jcl(
            "//PARMIN DD DSN=DWL.CNTL(MGSLAP8F),DISP=SHR",
            SORT_MEMBER,
            spec -> spec.afterRecipe(cu -> {
                Optional<ParmMember> marker = parmMarker(cu, ParmMember.Status.EXPANDED);
                assertThat(marker).isPresent();
                assertThat(marker.get().getDdName()).isEqualTo("PARMIN");
                assertThat(graftedWords(cu)).isNotEmpty();
            })
          )
        );
    }

    @Test
    void resolvesDsnameLongForm() {
        rewriteRun(
          jcl(
            "//SYSIN DD DSNAME=DWL.PARMLIB(MGSLAP8F),DISP=SHR",
            SORT_MEMBER,
            spec -> spec.afterRecipe(cu ->
                assertThat(parmMarker(cu, ParmMember.Status.EXPANDED)).isPresent())
          )
        );
    }

    @Test
    void graftsMultiLineMemberContent() {
        rewriteRun(
          jcl(
            "//SYSIN DD DSN=DWL.PARMLIB(MULTI),DISP=SHR",
            singletonList(parmMember("MULTI",
              """
                SORT FIELDS=(1,4,CH,A)
                MERGE FIELDS=(5,2,CH,D)
                """)),
            spec -> spec.afterRecipe(cu ->
                assertThat(graftedWords(cu)).containsExactly(
                    "SORT", "FIELDS=(1,4,CH,A)",
                    "MERGE", "FIELDS=(5,2,CH,D)"))
          )
        );
    }

    @Test
    void graftedNodesFollowReferencingDd() {
        rewriteRun(
          jcl(
            """
              //STEP1   EXEC PGM=SORT
              //SYSIN    DD DSN=DWL.PARMLIB(MGSLAP8F),DISP=SHR
              //SYSOUT   DD SYSOUT=*
              """,
            SORT_MEMBER,
            spec -> spec.afterRecipe(cu -> {
                List<String> texts = cu.getStatements().stream()
                        .map(ExternalSysinTest::wordText)
                        .collect(toList());
                int dsn = texts.indexOf("//SYSIN");
                int sort = texts.indexOf("SORT");
                int sysout = texts.indexOf("//SYSOUT");
                assertThat(sort).isGreaterThan(dsn).isLessThan(sysout);
            })
          )
        );
    }

    @Test
    void resolvesAcrossContinuation() {
        rewriteRun(
          jcl(
            """
              //SYSIN    DD DSN=DWL.PARMLIB(MGSLAP8F),
              //            DISP=SHR
              """,
            SORT_MEMBER,
            spec -> spec.afterRecipe(cu ->
                assertThat(parmMarker(cu, ParmMember.Status.EXPANDED)).isPresent())
          )
        );
    }

    @Test
    void skipsSymbolicMemberReference() {
        rewriteRun(
          jcl(
            "//SYSIN DD DSN=DWL.PARMLIB(&MBR),DISP=SHR",
            SORT_MEMBER,
            spec -> spec.afterRecipe(cu -> {
                assertThat(parmMarker(cu)).isEmpty();
                assertThat(graftedWords(cu)).isEmpty();
            })
          )
        );
    }

    @Test
    void resolvesConcreteMemberWithSymbolicDataSet() {
        // The data set qualifier is ignored for lookup, so a symbolic qualifier with a
        // concrete member still resolves.
        rewriteRun(
          jcl(
            "//SYSIN DD DSN=&HLQ..PARMLIB(MGSLAP8F),DISP=SHR",
            SORT_MEMBER,
            spec -> spec.afterRecipe(cu ->
                assertThat(parmMarker(cu, ParmMember.Status.EXPANDED)).isPresent())
          )
        );
    }

    @Test
    void concatenatedDdsEachExpand() {
        List<Path> members = List.of(
          parmMember("MGSLAP8F", "SORT FIELDS=(1,2,CH,A)\n"),
          parmMember("MGSLAP9G", "MERGE FIELDS=(3,4,CH,A)\n"));
        rewriteRun(
          jcl(
            """
              //SYSIN    DD DSN=DWL.PARMLIB(MGSLAP8F),DISP=SHR
              //         DD DSN=DWL.PARMLIB(MGSLAP9G),DISP=SHR
              """,
            members,
            spec -> spec.afterRecipe(cu ->
                assertThat(graftedWords(cu)).containsExactly(
                    "SORT", "FIELDS=(1,2,CH,A)",
                    "MERGE", "FIELDS=(3,4,CH,A)"))
          )
        );
    }

    @Test
    void ignoresSequenceNumberArea() {
        // Columns 73-80 carry sequence numbers in fixed-form PDS members and must not
        // become part of the grafted stream content.
        String statement = "SORT FIELDS=(1,2,CH,A)";
        String numbered = statement + " ".repeat(72 - statement.length()) + "00010000\n";
        rewriteRun(
          jcl(
            "//SYSIN DD DSN=DWL.PARMLIB(NUMBERED),DISP=SHR",
            singletonList(parmMember("NUMBERED", numbered)),
            spec -> spec.afterRecipe(cu ->
                assertThat(graftedWords(cu)).containsExactly("SORT", "FIELDS=(1,2,CH,A)"))
          )
        );
    }

    @Test
    void keepsSymbolicsInGraftedContentLiteral() {
        rewriteRun(
          jcl(
            "//SYSIN DD DSN=DWL.PARMLIB(REPLMBR),DISP=SHR",
            singletonList(parmMember("REPLMBR", "REPL DBN=%%NAME.FIELD,SEG=&SEGNAME\n")),
            spec -> spec.afterRecipe(cu ->
                assertThat(graftedWords(cu)).containsExactly(
                    "REPL", "DBN=%%NAME.FIELD,SEG=&SEGNAME"))
          )
        );
    }

    @Test
    void graftsCrlfMemberContent() {
        String content = "SORT FIELDS=(1,4,CH,A)\r\nMERGE FIELDS=(5,2,CH,D)\r\n";
        rewriteRun(
          jcl(
            "//SYSIN DD DSN=DWL.PARMLIB(CRLFMBR),DISP=SHR",
            singletonList(parmMember("CRLFMBR", content)),
            spec -> spec.afterRecipe(cu ->
                assertThat(graftedWords(cu)).containsExactly(
                    "SORT", "FIELDS=(1,4,CH,A)",
                    "MERGE", "FIELDS=(5,2,CH,D)"))
          )
        );
    }

    @Test
    void expandsWithTrailingCommentOnDd() {
        rewriteRun(
          jcl(
            "//SYSIN DD DSN=DWL.PARMLIB(MGSLAP8F),DISP=SHR  *trailing comment                 commentArea\n",
            SORT_MEMBER,
            spec -> spec.afterRecipe(cu ->
                assertThat(parmMarker(cu, ParmMember.Status.EXPANDED)).isPresent())
          )
        );
    }

    @Test
    void noMarkersWhenNoMembersSupplied() {
        rewriteRun(
          jcl(
            "//SYSIN DD DSN=DWL.PARMLIB(MGSLAP8F),DISP=SHR",
            spec -> spec.afterRecipe(cu -> assertThat(parmMarker(cu)).isEmpty())
          )
        );
    }

    /**
     * The DD is a procedure's, and the member it names is the job's: neither exists until the job
     * has been expanded and the symbols filled in. This is how a Db2 unload job is written.
     */
    @Test
    void expandsAMemberNamedByAProceduresDd() {
        rewriteRun(
          jclWithProcedures(
            """
              //CLMJ030  JOB (CLM),'UNLOAD CLAIMS',CLASS=P
              //UNL      EXEC HPUUNL,MEM=CLMUNL01
              """,
            singletonList(procedureMember("HPUUNL",
              """
                //HPUUNL   PROC CTLLIB=DB2.PROD.CNTL,MEM=NONE
                //UNLOAD   EXEC PGM=INZUTILB
                //SYSIN    DD DSN=&CTLLIB(&MEM),DISP=SHR
                //SYSPRINT DD SYSOUT=*
                //         PEND
                """)),
            singletonList(parmMember("CLMUNL01",
              """
                UNLOAD TABLESPACE CLMDB.CLMTS
                """)),
            spec -> spec.afterRecipe(cu -> {
                Optional<ParmMember> marker = parmMarker(cu, ParmMember.Status.EXPANDED);
                assertThat(marker).isPresent();
                assertThat(marker.get().getDdName()).isEqualTo("SYSIN");
                assertThat(marker.get().getDataSetName()).isEqualTo("DB2.PROD.CNTL");
                assertThat(marker.get().getMemberName()).isEqualTo("CLMUNL01");
                assertThat(graftedAnywhere(cu)).containsExactly(
                    "UNLOAD", "TABLESPACE", "CLMDB.CLMTS");
            })
          )
        );
    }
}
