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
package org.openrewrite.mainframe;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.SourceFile;
import org.openrewrite.mainframe.cobol.Corpus;
import org.openrewrite.mainframe.trait.Mention;
import org.openrewrite.mainframe.trait.RunBook;
import org.openrewrite.mainframe.trait.Script;
import org.openrewrite.text.PlainText;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Reads the members no grammar here reads — the scripts, the run books, the C and the PL/I — and
 * reports what the traits found in the two kinds that have traits at all. Gated on
 * {@code JCL_CORPUS} pointing at a checkout, because the corpus is not redistributed with this
 * repository, and on that variable because these members are one repository of a JCL estate.
 * <p>
 * The fixture is the measurement: {@code docs/INTERLINKS.md} sections 17 and 18 were written before any
 * of this read a CLIST, so every count below was somebody else's answer first. The public applications
 * are report-only — Zowe's execs and Z Open Editor's are real REXX and none of it belongs to an estate
 * whose members these traits could join to.
 */
@EnabledIfEnvironmentVariable(named = "JCL_CORPUS", matches = ".+")
class PlainTextCorpusTest {

    @Test
    void readsRealScriptsAndRunBooks() throws IOException {
        Path corpus = Paths.get(System.getenv("JCL_CORPUS"));

        int members = 0;
        int references = 0;
        int names = 0;
        List<String> failures = new ArrayList<>();
        boolean fixtureFound = false;

        System.out.println("scripts and run books read, by application:");
        for (Path repository : Corpus.repositories(corpus)) {
            List<Path> paths = new ArrayList<>(Corpus.scripts(repository));
            paths.addAll(Corpus.runBooks(repository));
            if (paths.isEmpty()) {
                continue;
            }
            fixtureFound |= Corpus.isFixture(repository);
            int read = 0;
            for (Path member : paths) {
                members++;
                String name = corpus.relativize(member).toString();
                PlainText cu = parse(corpus, member);
                if (!new String(Files.readAllBytes(member)).equals(cu.printAll())) {
                    failures.add(name + ": did not print back");
                    continue;
                }
                read++;
                for (Script script : new Script.Matcher().lower(cu).collect(Collectors.toList())) {
                    references += script.getReferences().size();
                    names += script.getMentions().size();
                }
                for (RunBook book : new RunBook.Matcher().lower(cu).collect(Collectors.toList())) {
                    if (book.getSubject() == null) {
                        failures.add(name + ": documents nothing");
                    }
                    names += book.getMentions().size();
                }
            }
            System.out.printf("  %-40s %3d of %3d%n", repository.getFileName(), read, paths.size());
        }
        assertThat(members).as("no script or run book found under %s", corpus).isPositive();

        System.out.printf("script and run book corpus: %d members, %d references, %d names%n",
          members, references, names);
        failures.forEach(failure -> System.out.println("  " + failure));

        assertThat(failures).isEmpty();
        assertThat(fixtureFound).as("mainframe-fixtures under %s", corpus).isTrue();
    }

    /**
     * C and PL/I are typed and nothing more — no grammar, no trait — so the whole of what there is to
     * measure is that a member comes back as the bytes it was written as, under the kind its extension
     * gives it. Report-only, and the fixture has neither: this is the public applications alone.
     */
    @Test
    void readsTheCAndPliOfTheEstate() throws IOException {
        Path corpus = Paths.get(System.getenv("JCL_CORPUS"));

        int members = 0;
        List<String> failures = new ArrayList<>();

        System.out.println("C and PL/I members read, by application:");
        for (Path repository : Corpus.repositories(corpus)) {
            Map<Members.Kind, List<Path>> libraries = new LinkedHashMap<>();
            libraries.put(Members.Kind.C, Corpus.cSources(repository));
            libraries.put(Members.Kind.PLI, Corpus.pliSources(repository));
            int found = 0;
            int read = 0;
            for (Map.Entry<Members.Kind, List<Path>> library : libraries.entrySet()) {
                for (Path member : library.getValue()) {
                    members++;
                    found++;
                    String name = corpus.relativize(member).toString();
                    PlainText cu = parse(corpus, member);
                    if (Members.kindOf(cu) != library.getKey()) {
                        failures.add(name + ": read as " + Members.kindOf(cu));
                    } else if (!new String(Files.readAllBytes(member)).equals(cu.printAll())) {
                        failures.add(name + ": did not print back");
                    } else {
                        read++;
                    }
                }
            }
            if (found > 0) {
                System.out.printf("  %-40s %3d of %3d%n", repository.getFileName(), read, found);
            }
        }

        System.out.printf("C and PL/I corpus: %d members%n", members);
        failures.forEach(failure -> System.out.println("  " + failure));

        assertThat(failures).isEmpty();
        assertThat(members).as("no C or PL/I member found under %s", corpus).isPositive();
    }

    /**
     * INTERLINKS 17.1 and 17.2, statement by statement. Every member the eleven scripts name in a
     * statement whose shape says what it reaches: ten calls of another script, one {@code CALL} of a
     * load module, one {@code RUN} of a program under DSN, four submits, eleven allocations, fifteen
     * existence checks and one edit.
     * <p>
     * Not one of the four submits names a job: the fixture submits by argument throughout, and the job
     * a script really submits — {@code CLMCOMP} picks one of the four compile jobs, {@code CLMNITE}
     * offers the six of the nightly stream — is set into a variable and handed to {@code CLMSUB}. That
     * is a fact about two members and a parameter, and 17.1 is the human reading of it.
     */
    @Test
    void readsWhatEachStatementOfTheFixtureScriptsReaches() throws IOException {
        assertThat(references()).containsExactly(
          "CLMCOMP:24 EXEC CLMSETUP",
          "CLMCOMP:27 CHECK &MEM by argument",
          "CLMCOMP:28 CHECK &MEM by argument",
          "CLMCOMP:46 EXEC CLMSUB",
          "CLMCOMP1:19 EXEC CLMSETUP",
          "CLMCOMP1:22 CHECK &MEM by argument",
          "CLMCOMP1:23 CHECK &MEM by argument",
          "CLMCOMP1:40 CHECK &MEM by argument",
          "CLMCOMP1:45 CHECK &CNTL by argument",
          "CLMCOMP1:46 ALLOCATE &CNTL by argument",
          "CLMCOMP1:48 ALLOCATE &MEM by argument",
          "CLMCOMP1:70 SUBMIT &MEM by argument",
          "CLMDLG:12 EXEC CLMSETUP",
          "CLMDLG:16 ALLOCATE JCL",
          "CLMDLG:39 EDIT &JOB by argument",
          "CLMDLG:52 EXEC CLMSUB",
          "CLMFXTR:20 EXEC CLMSETUP",
          "CLMFXTR:25 CHECK &EXTRACT by argument",
          "CLMFXTR:26 ALLOCATE CLMMAST",
          "CLMFXTR:27 ALLOCATE &PARM by argument",
          "CLMFXTR:28 ALLOCATE &EXTRACT by argument",
          "CLMFXTR:31 CALL CLMB010",
          "CLMNITE:19 EXEC CLMSETUP",
          "CLMNITE:44 EXEC CLMSUB",
          "CLMRECON:20 EXEC CLMSETUP",
          "CLMRECON:23 ALLOCATE RECNRPT",
          "CLMRECON:25 RUN CLMD020",
          "CLMSETUP:25 ALLOCATE CLIST",
          "CLMSETUP:26 ALLOCATE REXX",
          "CLMSUB:16 EXEC CLMSETUP",
          "CLMSUB:18 CHECK &JOB by argument",
          "CLMSUB:19 CHECK &JOB by argument",
          "CLMSUB:32 SUBMIT &JOB by argument",
          "CLMCMPX:33 CHECK SOURCE by argument",
          "CLMCMPX:34 CHECK SOURCE by argument",
          "CLMCMPX:50 CHECK DECK by argument",
          "CLMPICK:12 CHECK JCL",
          "CLMPICK:33 ALLOCATE MEM.I by argument",
          "CLMPICK:53 SUBMIT JOB by argument",
          "CLMRERUN:18 CHECK JCL by argument",
          "CLMRERUN:19 CHECK JCL by argument",
          "CLMRERUN:22 ALLOCATE JCL by argument",
          "CLMRERUN:44 SUBMIT JCL by argument");
    }

    /**
     * INTERLINKS 17.1 and 17.2 again, read the other way round: the library and the DD each statement
     * names, which is what a member reference resolves through. A {@code LIBRARY(member)} form says
     * which library the member is in even where the member itself is an argument.
     */
    @Test
    void readsTheLibraryAndDdNameOfEachStatement() throws IOException {
        Map<String, Script> scripts = scripts();

        List<String> allocations = new ArrayList<>();
        for (Map.Entry<String, Script> script : scripts.entrySet()) {
            for (Script.Reference reference : script.getValue().getReferences()) {
                if (reference.getKind() == Script.Reference.Kind.ALLOCATE) {
                    allocations.add(script.getKey() + " " + reference.getDdName() + " -> " +
                                    reference.getDataSet());
                }
            }
        }
        // The three DD names CLMB010 assigns and the one CLMD020 does, allocated the way the jobs
        // allocate them; DA(*) is the terminal, which is the whole point of running the report there.
        assertThat(allocations).containsExactly(
          "CLMCOMP1 null -> null",
          "CLMCOMP1 JCLOUT -> &CNTL",
          "CLMDLG null -> &CLMHLQ..JCL",
          "CLMFXTR CLMMAST -> &CLMHLQ..CLMMAST",
          "CLMFXTR PARMCARD -> &CLMHLQ..CTLCARD",
          "CLMFXTR CLMEXTR -> null",
          "CLMRECON RECNRPT -> null",
          "CLMSETUP null -> &CLMHLQ..CLIST",
          "CLMSETUP null -> &CLMHLQ..REXX",
          "CLMPICK JCLIN -> HLQ.JCL",
          "CLMRERUN JCLIN -> null");

        // A member named in a library the statement writes down, whether the member is written or
        // handed in as an argument.
        List<String> members = new ArrayList<>();
        for (Map.Entry<String, Script> script : scripts.entrySet()) {
            for (Script.Reference reference : script.getValue().getReferences()) {
                if (reference.getDataSet() != null && reference.getMember() != null) {
                    members.add(script.getKey() + " " + reference.getDataSet() + "(" +
                                reference.getMember() + ")");
                }
            }
        }
        assertThat(members).containsExactly(
          "CLMCOMP &CLMHLQ..COBOL(&MEM)",
          "CLMCOMP &CLMHLQ..COBOL(&MEM)",
          "CLMCOMP1 &CLMHLQ..COBOL(&MEM)",
          "CLMCOMP1 &CLMHLQ..COBOL(&MEM)",
          "CLMCOMP1 &CLMHLQ..LINKLIB(&MEM)",
          "CLMCOMP1 &CNTL(&MEM)",
          "CLMCOMP1 &CNTL(&MEM)",
          "CLMDLG &CLMHLQ..JCL(&JOB)",
          "CLMFXTR &CLMHLQ..CTLCARD(&PARM)",
          "CLMFXTR &CLMHLQ..LOADLIB(CLMB010)",
          "CLMSUB &CLMHLQ..JCL(&JOB)",
          "CLMSUB &CLMHLQ..JCL(&JOB)",
          "CLMSUB &CLMHLQ..JCL(&JOB)",
          "CLMPICK HLQ.JCL(MEM.I)",
          "CLMPICK HLQ.JCL(JOB)");
    }

    /**
     * INTERLINKS 17.1, the other half of a submit by argument. None of the four submits names a job,
     * and the eleven literal job names of 17.1 are all written in a {@code SET}: four under the
     * {@code SELECT} of {@code CLMCOMP}, six in stream order in {@code CLMNITE}, one as the dialog
     * default in {@code CLMDLG}. What a member sets, what it calls and what it takes are read here;
     * putting the three together across two members is a recipe's, since it holds both.
     */
    @Test
    void readsWhatTheFixtureClistsSetCallAndDeclare() throws IOException {
        Map<String, Script> scripts = scripts();

        List<String> declared = new ArrayList<>();
        List<String> called = new ArrayList<>();
        List<String> set = new ArrayList<>();
        scripts.forEach((member, script) -> {
            if (!script.getParameters().isEmpty()) {
                declared.add(member + " PROC " +
                             script.getParameters().stream().filter(Script.Parameter::isPositional).count() +
                             " " + script.getParameters().stream().map(Script.Parameter::toString)
                               .collect(Collectors.joining(" ")));
            }
            for (Script.Invocation invocation : script.getInvocations()) {
                called.add(member + ":" + invocation.getLine() + " " + invocation);
            }
            for (Script.Assignment assignment : script.getAssignments()) {
                if (assignment.isLiteral()) {
                    set.add(member + " " + assignment.getVariable() + " = " + assignment.getValue());
                }
            }
        });

        // The eight CLISTs and none of the three execs: a PROC statement is CLIST's way of saying what
        // a caller may hand it, and %CLMSUB &JOB NOASK binds to the JOB and NOASK of the first row.
        assertThat(declared).containsExactly(
          "CLMCOMP PROC 1 MEM ENV(PROD)",
          "CLMCOMP1 PROC 1 MEM ENV(PROD) CLASS(A) DB2",
          "CLMDLG PROC 0 ENV(PROD)",
          "CLMFXTR PROC 0 ENV(PROD) PARM(PRMCLM01)",
          "CLMNITE PROC 0 FROM(CLMJ010) ENV(PROD)",
          "CLMRECON PROC 0 ENV(PROD)",
          "CLMSETUP PROC 0 ENV(PROD)",
          "CLMSUB PROC 1 JOB NOASK");
        assertThat(called).containsExactly(
          "CLMCOMP:24 %CLMSETUP ENV(&ENV)",
          "CLMCOMP:46 %CLMSUB &JOB NOASK",
          "CLMCOMP1:19 %CLMSETUP ENV(&ENV)",
          "CLMDLG:12 %CLMSETUP ENV(&ENV)",
          "CLMDLG:52 %CLMSUB &JOB NOASK",
          "CLMFXTR:20 %CLMSETUP ENV(&ENV)",
          "CLMNITE:19 %CLMSETUP ENV(&ENV)",
          "CLMNITE:44 %CLMSUB &JOB NOASK",
          "CLMRECON:20 %CLMSETUP ENV(&ENV)",
          "CLMSUB:16 %CLMSETUP ENV(PROD)");

        // The eleven job names of 17.1 and nothing that is not written down: a return code and a
        // counter are literal too, and which of these is a member of the estate is the name index's
        // answer, not the reader's.
        assertThat(set).containsExactly(
          "CLMCOMP JOB = CLMCMPB",
          "CLMCOMP JOB = CLMCMPB",
          "CLMCOMP JOB = CLMCMPC",
          "CLMCOMP JOB = CLMCMPD",
          "CLMCOMP JOB = CLMCMPI",
          "CLMCOMP1 PROC = CLMCOB",
          "CLMCOMP1 PROC = CLMCLB",
          "CLMCOMP1 PROC = CLMCLB",
          "CLMCOMP1 PROC = CLMCLD",
          "CLMCOMP1 PROC = CLMCLC",
          "CLMCOMP1 PROC = CLMCLX",
          "CLMDLG CLMLASTJ = CLMJ010",
          "CLMDLG N = 0",
          "CLMNITE JOB1 = CLMJ010",
          "CLMNITE JOB2 = CLMJ020",
          "CLMNITE JOB3 = CLMJ030",
          "CLMNITE JOB4 = CLMJ040",
          "CLMNITE JOB5 = CLMJ050",
          "CLMNITE JOB6 = CLMJ060",
          "CLMNITE NJOBS = 6",
          "CLMNITE STARTED = NO",
          "CLMNITE SUBMITTED = 0",
          "CLMNITE I = 1",
          "CLMNITE STARTED = YES",
          "CLMSETUP CLMSSID = DB2P",
          "CLMSETUP CLMSSID = DB2Q",
          "CLMSETUP CLMSSID = DB2T",
          "CLMSETUP CLMRC = 12",
          "CLMSETUP CLMRC = 0",
          "CLMSUB CLMRC = 8",
          "CLMSUB CLMRC = 4");

        // Every name a script computes is set from another variable or a function: CLMSUB's &JOB is
        // its argument, CLMNITE reads its six back through the composed name &&JOB&I, and CLMDLG's
        // goes through &CLMLASTJ. So no submit of the fixture resolves inside the member that writes
        // it, and none is resolved here.
        assertThat(scripts.get("CLMSUB").getAssignments())
          .filteredOn(assignment -> "JOB".equals(assignment.getVariable()))
          .extracting(Script.Assignment::getValue, Script.Assignment::isLiteral)
          .containsExactly(tuple("&SYSCAPS(&JOB)", false));
    }

    /**
     * INTERLINKS 17.3. What a search for a member name finds in the eleven scripts, comments included:
     * every name the table names is a name the trait finds. A {@code LIBRARY(member)} form is two names
     * here, since the parentheses end a token, and the table counts it as one.
     */
    @Test
    void findsEveryNameTheFixtureScriptsWriteDown() throws IOException {
        Map<String, List<String>> written = new TreeMap<>();
        scripts().forEach((member, script) -> written.put(member,
          script.getMentions().stream().map(Mention::getText).collect(Collectors.toList())));

        assertThat(written.get("CLMCOMP")).contains("CLMCMPB", "CLMCMPC", "CLMCMPD", "CLMCMPI",
          "CLMASMB", "COBOL", "CLMCOMP1", "CLMSETUP", "CLMSUB");
        assertThat(written.get("CLMCOMP1")).contains("LINKLIB", "COBOL", "CLMB010", "CLMC040",
          "CLMCLB", "CLMCLC", "CLMCLD", "CLMCOB", "CLMCLX", "CLMSETUP");
        assertThat(written.get("CLMDLG")).contains("JCL", "CLMJ010", "CLMSUB", "CLMSETUP");
        assertThat(written.get("CLMFXTR")).contains("CLMJ010", "LOADLIB", "CLMB010", "CTLCARD",
          "PRMCLM01", "CLMEXTR", "CLMSETUP");
        assertThat(written.get("CLMNITE")).contains("CLMJ010", "CLMJ020", "CLMJ030", "CLMJ040",
          "CLMJ050", "CLMJ060", "CLMJ070", "CLMNIGHT", "CLMSETUP", "CLMSUB");
        assertThat(written.get("CLMRECON")).contains("CLMJ050", "CLMD020", "RUNCLM02", "CLMPLAN",
          "CLMSETUP");
        assertThat(written.get("CLMSETUP")).contains("@JOBCARD");
        assertThat(written.get("CLMSUB")).contains("JCL", "CLMJ010", "CLMSETUP");
        assertThat(written.get("CLMCMPX")).contains("LINKLIB", "COBOL", "CLMB010", "CLMB020",
          "CLMC040", "CLMCLB", "CLMCLC", "CLMCLD", "CLMCLX", "CLMCOB", "CLMCOMP1");
        assertThat(written.get("CLMPICK")).contains("JCL");
        assertThat(written.get("CLMRERUN")).contains("JCL", "CLMJ030", "CLMJ010", "CLMJ001",
          "CLMJ060", "CLMJ070", "IMSP-DBR-CLMDBD01", "POLADM-EXTRACT-OK", "CLMMAST-CLOSED",
          "CLMDBD01");
    }

    /**
     * INTERLINKS 18.1. What each of the thirty one run book members documents: ten jobs, fourteen
     * programs and seven data sets. A {@code DOCFICH} is named for the last qualifier of its data set
     * where that is a name and given one where it is a word, so the {@code FILE} line and not the member
     * name is what a reference resolves through.
     */
    @Test
    void readsWhatEachRunBookOfTheFixtureDocuments() throws IOException {
        List<String> documented = new ArrayList<>();
        for (RunBook book : runBooks()) {
            documented.add(book.getName() + " " + book.getShape() + " " +
                           (book.getSubject() == null ? "-" : book.getSubject().getText()));
        }
        assertThat(documented).containsExactly(
          "CLMAUDIT DOCFICH CLM.PROD.CLMAUDIT",
          "CLMB010 DOCPGM CLMB010",
          "CLMB020 DOCPGM CLMB020",
          "CLMB030 DOCPGM CLMB030",
          "CLMC010 DOCPGM CLMC010",
          "CLMC020 DOCPGM CLMC020",
          "CLMC030 DOCPGM CLMC030",
          "CLMC040 DOCPGM CLMC040",
          "CLMC050 DOCPGM CLMC050",
          "CLMD010 DOCPGM CLMD010",
          "CLMD020 DOCPGM CLMD020",
          "CLMD030 DOCPGM CLMD030",
          "CLMEXTR DOCFICH CLM.PROD.EXTRACT",
          "CLMI010 DOCPGM CLMI010",
          "CLMJ001 DOCJOB CLMJ001",
          "CLMJ010 DOCJOB CLMJ010",
          "CLMJ020 DOCJOB CLMJ020",
          "CLMJ030 DOCJOB CLMJ030",
          "CLMJ040 DOCJOB CLMJ040",
          "CLMJ050 DOCJOB CLMJ050",
          "CLMJ060 DOCJOB CLMJ060",
          "CLMJ070 DOCJOB CLMJ070",
          "CLMJ080 DOCJOB CLMJ080",
          "CLMJ090 DOCJOB CLMJ090",
          "CLMMAST DOCFICH CLM.PROD.CLMMAST",
          "CLMRPT DOCFICH CLM.PROD.CLMRPT",
          "CLMTYPE DOCFICH CLM.PROD.CLMTYPE",
          "CLMU010 DOCPGM CLMU010",
          "CLMU020 DOCPGM CLMU020",
          "EXTSORT DOCFICH CLM.PROD.EXTRACT.SORTED",
          "MASTBKUP DOCFICH CLM.PROD.CLMMAST.BACKUP");
    }

    /**
     * INTERLINKS 18.2. The names a run book mentions, which is what a name index joins on. Every name
     * the table gives for these four is one the trait finds — including the two that are wrong on
     * purpose: {@code CLMDB01} in {@code doc/CLMJ060} is the DD name of the database data set and not
     * the DB2 database of the same name, and a name index is expected to make that match.
     */
    @Test
    void findsTheNamesEachRunBookMentions() throws IOException {
        Map<String, List<String>> mentioned = new TreeMap<>();
        for (RunBook book : runBooks()) {
            mentioned.put(book.getName(),
              book.getMentions().stream().map(Mention::getText).collect(Collectors.toList()));
        }

        assertThat(mentioned.get("CLMJ010")).contains("CLMJ020", "CLMB010", "PRMCLM01", "CLMBATCH",
          "CLMEXTR", "CLM.PROD.CLMMAST", "CLM.PROD.EXTRACT", "CLMMAST-CLOSED", "CLMNIGHT",
          "CLMWORK", "CLMFXTR");
        assertThat(mentioned.get("CLMJ060")).contains("CLMJ030", "CLMJ020", "CLMI010", "CLMDLIB",
          "CLMEXTR", "CLM.PROD.IMS.CLMDB01.LOG", "CLM.PROD.EXTRACT.SORTED", "CLM.PROD.IMS.CLMDB01",
          "CLMNIGHT_CLMJ030_OK", "CLMNIGHT_CLMJ060_OK", "IMSP-DBR-CLMDBD01", "CLMNIGHT", "CLMWORK",
          "CLMDBD01", "CLMPSB01", "CLMDB01");
        // A data set name is one name, so the library a load module is in reads as CLM.PROD.LOADLIB
        // where the table writes LOADLIB(CLMB010); the member is the name that resolves either way.
        assertThat(mentioned.get("CLMB010")).contains("CLMCMPB", "CLMJ010", "CLMB010",
          "CLM.PROD.LOADLIB", "PRMCLM01", "CLMCLB", "CLMBATCH", "CLMREC", "CLMEXTR", "CLMPARM",
          "CLM.PROD.CLMMAST", "CLM.PROD.EXTRACT", "CLMFXTR");
        assertThat(mentioned.get("CLMMAST")).contains("CLMJ001", "CLMJ010", "CLMJ030", "CLMJ080",
          "CLMC020", "CLMB010", "CLMC030", "CLMB030", "DEFCLM01", "CLMREC", "CLM.PROD.CLMMAST",
          "CLMMAST-CLOSED", "MASTBKUP", "CLMAUDIT");

        // A run book names components and nothing but words otherwise, so what a name index has to
        // look up is the whole of it: 31 members, and no member of them silent.
        assertThat(mentioned).hasSize(31);
        assertThat(mentioned.values()).allSatisfy(names -> assertThat(names).isNotEmpty());
    }

    private static Map<String, Script> scripts() throws IOException {
        Map<String, Script> scripts = new LinkedHashMap<>();
        Path fixture = fixture();
        for (Path member : Corpus.scripts(fixture)) {
            scripts.put(memberName(member), new Script.Matcher().require(parse(fixture, member), null));
        }
        return scripts;
    }

    private static List<RunBook> runBooks() throws IOException {
        List<RunBook> books = new ArrayList<>();
        Path fixture = fixture();
        for (Path member : Corpus.runBooks(fixture)) {
            books.add(new RunBook.Matcher().require(parse(fixture, member), null));
        }
        return books;
    }

    /**
     * Every reference of every script, as the member that wrote it, the line, the statement and what it
     * reached — with a name the script computed said to be by argument, since that is the difference
     * between a job named here and a job named by whoever ran the script.
     */
    private static List<String> references() throws IOException {
        List<String> references = new ArrayList<>();
        scripts().forEach((member, script) -> {
            for (Script.Reference reference : script.getReferences()) {
                references.add(member + ":" + reference.getLine() + " " + reference.getKind() + " " +
                               reference.getName() + (reference.isSymbolic() ? " by argument" : ""));
            }
        });
        return references;
    }

    private static PlainText parse(Path relativeTo, Path member) {
        List<SourceFile> parsed = Corpus.plainTextReader()
          .parseInputs(Corpus.inputs(singletonList(member)), relativeTo, new InMemoryExecutionContext())
          .collect(Collectors.toList());
        assertThat(parsed).singleElement().isInstanceOf(PlainText.class);
        return (PlainText) parsed.get(0);
    }

    private static Path fixture() {
        Path directory = Paths.get(System.getenv("JCL_CORPUS")).resolve("mainframe-fixtures/claims");
        assertThat(Files.isDirectory(directory)).as("%s", directory).isTrue();
        return directory;
    }

    private static String memberName(Path member) {
        String name = member.getFileName().toString();
        return name.contains(".") ? name.substring(0, name.indexOf('.')) : name;
    }
}
