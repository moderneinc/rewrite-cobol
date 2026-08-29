<p align="center">
  <a href="https://docs.openrewrite.org">
    <picture>
      <source media="(prefers-color-scheme: dark)" srcset="https://github.com/openrewrite/rewrite/raw/main/doc/logo-oss-dark.svg">
      <source media="(prefers-color-scheme: light)" srcset="https://github.com/openrewrite/rewrite/raw/main/doc/logo-oss-light.svg">
      <img alt="OpenRewrite Logo" src="https://github.com/openrewrite/rewrite/raw/main/doc/logo-oss-light.svg" width='600px'>
    </picture>
  </a>
</p>

<div align="center">
  <h1>rewrite-mainframe</h1>
</div>

<div align="center">

<!-- Keep the gap above this line, otherwise they won't render correctly! -->
[![ci](https://github.com/moderneinc/rewrite-mainframe/actions/workflows/ci.yml/badge.svg)](https://github.com/moderneinc/rewrite-mainframe/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/org.openrewrite/rewrite-mainframe.svg)](https://mvnrepository.com/artifact/org.openrewrite/rewrite-mainframe)
</div>

## What is this?

This project implements a [Rewrite module](https://github.com/openrewrite/rewrite) that provides parsers, visitors, and recipes for COBOL and related mainframe technologies. It supports parsing and transforming COBOL source code, JCL (Job Control Language), BMS map sets, DB2 DDL, DB2 bind cards, link-edit decks, DFSORT and IDCAMS control cards, Control-M job definitions, IMS gen source and HLASM assembler, and it reads the SAS, load module listings, CLISTs, REXX execs, run book members, C and PL/I an estate keeps beside them out of the plain text a build keeps them as.

Three words recur in the documentation and are not interchangeable. The *shop* is the organization: its conventions, its libraries, what its people wrote down. The *estate* is the mainframe world the shop runs — jobs, load modules, databases, schedules — whether or not every piece of it was checked in. The *portfolio* is the set of sources an analysis was actually given, which is why a member the estate has and the portfolio does not is a finding rather than a surprise.

### Language Support

- **COBOL** — Full parsing of COBOL-85 (IBM ANSI 85 and HP Tandem dialects), including preprocessor directives (COPY, REPLACE) and copybook resolution; programs by `.cbl`, `.cob` and `.cobol`, copybooks by `.cpy`, `.copy` and `.dcl`
- **JCL** — Job Control Language parsing (`.jcl`, `.prc` and `.proc` files, a `.txt` or extensionless PDS member whose first card is JCL, and the `.j2` Jinja templates an installation ships its jobs as), and the resolution a job's cards leave to other members: the procedures and INCLUDE members a step runs through, the DD overrides merged in, and the symbols a data set name is written with, which is what a step's real program and real data sets are read from
- **BMS** — CICS Basic Mapping Support map sets (`.bms`): the `DFHMSD`/`DFHMDI`/`DFHMDF` macros, and the symbolic map names they generate, which is what joins a screen field to the COBOL data item a program reads it from
- **DB2 DDL** — DB2 for z/OS DDL (`.ddl`, `.sql`, and the `SYSIN` streams of the jobs that create a schema): every statement the SQL reference documents is modelled, so a statement that cannot be read is a syntax error rather than a node that says nothing
- **Bind cards** — DSN command decks (`.bnd`, and an extensionless CARDLIB member whose first subcommand binds): `BIND PLAN`, `BIND PACKAGE` and `REBIND` with their keyword operands, read from a member of their own or from the in-stream `SYSTSIN` of the job that runs them
- **Link-edit decks** — Binder control statements (`.lnk`, `.lked`, and an extensionless LINKLIB member whose first statement links): `INCLUDE ddname(member)`, `ENTRY`, `ALIAS`, `NAME xxx(R)`, `ORDER`, `MODE`, `SETCODE` and `SETOPT`, with column 72 saying which card continues which. A deck is the only place a load module's composition is written down — a step names the module, and the module names its programs here
- **Load module listings** — What AMBLIST and the binder printed about a load library (`.amblist`, `.binder`, `.listload`, and a control card member that asks for a report), kept as plain text and read for what it names: the module and its entry point, aliases and size, the control sections it holds with their offsets and lengths, the alternate entry points within them, and the compiler each section came from. A deck says what a module was meant to hold; a listing says what it holds, including the language interface and the runtime nobody wrote a card for
- **Sort cards** — DFSORT and ICETOOL control statements: `SORT`/`MERGE FIELDS`, `INCLUDE`/`OMIT COND`, `INREC`/`OUTREC`/`OUTFIL`, `SUM`, `OPTION`, with the control fields read as byte positions into the record, which is what joins a sort card to the copybook that describes it
- **IDCAMS cards** — Access method services commands: `DEFINE CLUSTER`/`AIX`/`PATH`/`GDG` with their parameter groups, `REPRO`, `DELETE`, `LISTCAT`, `PRINT` and `ALTER`, which is where a VSAM file's key, record size and components are written down
- **Control-M** — Job scheduling definitions, both dialects a shop has: the z/OS panel (`.ctms`) and the XML an export writes (`.controlm`). The JCL member each job runs, the `IN` and `OUT` conditions that order them, the SMART table they sit in and the calendars they run on, which is what turns a library of jobs into the order they actually run in
- **IMS gen source** — The DBDs, PSBs, MFS format sets and stage 1 decks a shop gens its IMS system from (`.dbd`, `.psb`, `.mfs`, `.gen`, and an `.asm` whose first macro is one that gens, since a gen library is assembler source and is often kept as such): the databases with their segments, keys and logical relationships, the PCBs a program is handed and the segments each is sensitive to, the screens a message is laid out on, and the transactions that schedule a PSB. A DL/I call names a PCB by position and a segment by name, and the gen source is the only place either resolves to anything
- **Assembler** — HLASM statement source (`.asm`, `.mac`): control sections and the DSECTs that are the assembler's copybooks, laid out constant by constant so a layout can be compared with the COBOL one; `COPY` members, macro prototypes and invocations, `CALL` and `V`-type constants, DCBs, entry points and external names. There is no grammar because the columns are the syntax — a card continues the one above it because of a character in column 72, and `ICTL` moves the columns at run time
- **SAS** — SAS programs (`.sas`, and the `SYSIN` a job carries in-stream), kept as plain text and read for `%INCLUDE`, `LIBNAME`, `INFILE`/`FILE`/`FILENAME`, macro definitions and invocations, `INPUT` column layouts and the tables a `PROC SQL` reads, which is where a batch extract is read again by the group that asks what the numbers mean. A statement runs to the first semicolon outside a quoted string or a comment and SAS has no reserved words, so what there is to read is a text search and a name reference rather than a grammar
- **CLISTs, REXX execs and run book members** — Kept as plain text and read for what they name, since a member no grammar claims still says who runs what. A CLIST (`.clist`, `.clst`) and an exec (`.rexx`, `.rex`, `.rx`, and an extensionless member whose first line is the `/* REXX */` comment TSO reads) are read for the jobs they submit, the programs they run and the scripts they call, which is the only place in an estate that says how a job is started by hand; a run book member (`.docjob`, `.docpgm`, `.docfich`, `.docappl`, `.docoper`) for the component it documents and the components it names. C (`.c`, `.h`) and PL/I (`.pli`, `.pl1`) are kept so that they are searchable as what they are, and nothing more is read from them

### Recipes

- **Search** — Find copybooks, words, indicators, references, and relationships across COBOL programs
- **Cleanup** — Remove debugging mode from SOURCE-COMPUTER paragraphs
- **Format** — Remove words and shift sequence areas

## Tested against

Unit tests cover the constructs one at a time, and the NIST COBOL-85 conformance suite in
`src/test/resources/gov/nist` covers the standard. Neither contains CICS, IMS, DB2 or JCL, so the
parser is also measured against real mainframe code — these public repositories, each measured on
its own:

| Application | Repository | Programs | Jobs | CICS | Embedded SQL | BMS maps | DL/I |
|---|---|---:|---:|---:|---:|---:|---:|
| CardDemo | [aws-samples/aws-mainframe-modernization-carddemo](https://github.com/aws-samples/aws-mainframe-modernization-carddemo) | 42 of 44 | 61 of 61 | 25 | 4 | 7 | 7 |
| GenApp | [cicsdev/cics-genapp](https://github.com/cicsdev/cics-genapp) | 31 of 31 | 29 of 29 | 31 | 8 | 5 | — |
| Bank of Z | [IBM/Bank-of-Z](https://github.com/IBM/Bank-of-Z) | 43 of 43 | 1 of 1 | 31 | 15 | 9 | 11 |
| CBSA | [cicsdev/cics-banking-sample-application-cbsa](https://github.com/cicsdev/cics-banking-sample-application-cbsa) | 31 of 31 | 109 of 110 | 30 | 12 | 9 | — |
| COBOL Programming Course | [openmainframeproject/cobol-programming-course](https://github.com/openmainframeproject/cobol-programming-course) | 26 of 30 | 43 of 43 | — | 3 | — | — |
| Cash Account | [IBMStockTrader/cash-account-cobol](https://github.com/IBMStockTrader/cash-account-cobol) | 1 of 1 | 3 of 3 | 1 | 1 | — | — |
| zAppBuild | [IBM/dbb-zappbuild](https://github.com/IBM/dbb-zappbuild) | 6 of 7 | — | 3 | 1 | 2 | — |
| DBB Samples | [ibmdbbdev/Samples](https://github.com/ibmdbbdev/Samples) | 16 of 17 | — | 3 | 1 | 2 | — |
| Z Open Editor sample | [IBM/zopeneditor-sample](https://github.com/IBM/zopeneditor-sample) | 0 of 5 | 8 of 10 | — | — | — | — |
| base64 | [cicsdev/base64](https://github.com/cicsdev/base64) | 1 of 1 | 1 of 1 | — | — | — | — |
| GenevaERS Workbench | [genevaers/Workbench](https://github.com/genevaers/Workbench) | — | 20 of 20 | — | — | — | — |
| GenevaERS Performance Engine | [genevaers/Performance-Engine](https://github.com/genevaers/Performance-Engine) | — | 1 of 1 | — | — | — | — |
| ADCD setup | [davidegirardi/adcdsetup](https://github.com/davidegirardi/adcdsetup) | — | 11 of 11 | — | — | — | — |
| Zowe install packaging | [zowe/zowe-install-packaging](https://github.com/zowe/zowe-install-packaging) | — | 50 of 53 | — | — | — | — |
| MainframeJCL | [billrain/MainframeJCL](https://github.com/billrain/MainframeJCL) | — | 83 of 108 | — | — | — | — |
| zorow | [openmainframeproject/zorow](https://github.com/openmainframeproject/zorow) | — | 50 of 56 | — | — | — | — |
| CLAIMS fixture | [moderneinc/mainframe-fixtures](https://github.com/moderneinc/mainframe-fixtures) | 19 of 19 | 42 of 42 | 5 | 4 | 4 | 6 |

Programs and Jobs say how many files of each kind the parser read, and out of how many: 216 of 229
COBOL programs and 512 of 549 JCL members, plus all 50 BMS map sets in the seven applications that
have them. The technology columns say how many programs use each. A program counts as read when it
parsed and printed back byte for byte; a job when it also had exactly the EXEC cards the traits
found and nothing in it was left unplaced. The gaps are the measurement: `CorpusCoverageTest` groups
the COBOL failures by cause and `JclCorpusTest` names each member it could not read and why.

The other member kinds are not spread the way COBOL and JCL are — most applications hold none of
them at all — so they are counted in a table of their own, read of found. CBSA, the COBOL
Programming Course, Cash Account, GenevaERS Workbench and MainframeJCL have none of the five and are
left out of it:

| Application | IMS gen | Assembler | SAS | Scripts and run books | C and PL/I |
|---|---:|---:|---:|---:|---:|
| CardDemo | 8 of 8 | 4 of 4 | — | — | — |
| GenApp | — | — | — | 2 of 2 | — |
| Bank of Z | 17 of 17 | — | — | — | 2 of 2 |
| zAppBuild | — | 1 of 1 | — | — | — |
| DBB Samples | 1 of 1 | — | — | — | 1 of 1 |
| Z Open Editor sample | — | 3 of 3 | — | 4 of 4 | 4 of 4 |
| base64 | — | 2 of 2 | — | — | 2 of 2 |
| GenevaERS Performance Engine | — | 116 of 116 | — | — | — |
| ADCD setup | — | — | — | 2 of 2 | — |
| Zowe install packaging | — | — | — | 22 of 22 | — |
| zorow | — | — | — | 45 of 45 | — |
| CLAIMS fixture | 19 of 19 | 8 of 8 | 4 of 4 | 42 of 42 | — |

That is 45 IMS gen members, 134 assembler members, 4 SAS members, 117 scripts and run book members
and 9 C and PL/I members, every one of them read and printed back byte for byte — which for the last
column is the whole of what is read, since there is no grammar for either language and a member of
one is held as the lines it was written as. Two of the five are thin outside the fixture: no public
application here has any SAS at all, and 25 of the 26 public IMS gen members belong to the two
applications that run IMS. The assembler goes the other way — 116 of the 134 are one build engine's,
and the C and PL/I are the one column the fixture has none of, since the claims application is a
COBOL shop. Bank of Z's seventeen count in the IMS column and not the assembler one because
that is what they are: `src/base/ims/DBD/*.asm` and `src/base/ims/PSB/*.asm` are a gen library kept
as assembler, which is the ordinary way to keep one.

Bank of Z descends from CBSA, so the two are not independent measurements. 29 of CBSA's 31 program
names, 36 of its 37 copybooks and all 10 of its map sets are in Bank of Z too; of the shared
programs two are byte-identical, 19 differ by a few lines, and eight (BANKDATA, BNK1DCS, CREACC,
CRECUST, DELCUS, INQCUST, UPDCUST, XFRFUN) were rewritten. Bank of Z adds the 14 IMS programs; CBSA
adds the 110 jobs, among them DB2 DDL and BIND in JCL, where Bank of Z has one.

The last row is not a public application but a fixture: one fictional insurance claims
application, CLAIMS, written so that every `COPY`, `CALL`, `EXEC PROC`, `SEND MAP` and `SYSIN`
member in it resolves to a member of the same repository, and every member of it a reader here takes
reads. The readers take 260 of its members between them and the only member kind left is the
plain control card — the nine IEBGENER, DSN, RUNSTATS and parm cards of `claims/ctlcard` — which the
walks skip rather than count against a parser. Nothing else in the repository is a member of a
library at all: its licence and its two markdown documents are the whole of the rest. The public
applications are measured;
the fixture is required. The tests know it by its directory name, `mainframe-fixtures`, and fail when
a program, copybook, job, procedure, map set, bind deck, link-edit deck, module listing, DDL member,
IMS gen member, assembler member, SAS member, CLIST, REXX exec, run book member, control card or schedule of it does not parse, read or print back — or when the corpus root does not contain it, since a fixture
the walk cannot see, a symbolic link say, would otherwise pass as an empty application.
`FixtureCoverageTest` puts every one of its members past every reader at once, so that a member is
claimed by one reader and no more and the kinds nothing reads are claimed by none: a reader that
quietly takes a parm card reports something plausible about a file it cannot read.

Clone them side by side into one directory and point the tests at it. The tests find files the way a
build does, whatever the case of the extension: programs by `.cbl`, `.cob` and
`.cobol`, copybooks by `.cpy`, `.copy` and `.dcl`, map sets by `.bms`, bind decks by `.bnd`, link-edit
decks by `.lnk` and `.lked`, module listings by `.amblist`, `.binder` and `.listload`, schedules by
`.ctms` and `.controlm`, IMS gen members by `.dbd`, `.psb`, `.gen` and `.mfs`, assembler programs and
macro library members by `.asm` and `.mac`, SAS members by `.sas`, CLISTs by `.clist` and `.clst`,
REXX execs by `.rexx`, `.rex` and `.rx`, run book members by `.docjob`, `.docpgm`, `.docfich`,
`.docappl` and `.docoper`, C by `.c` and `.h`, PL/I by `.pli` and `.pl1`, and jobs by `.jcl`, `.prc` and
`.proc`. Module listings, SAS members, scripts, run book members, C and PL/I have no grammar here: a
build keeps them as plain text — the CLI reads them with `Members.parser()`, as the tests here do — and
which technology a member is comes from its path, which is what the traits reading it are matched by.
Since MainframeJCL, ADCD setup and Zowe's SZWESAMP keep their members as they came off the PDS, a
`.txt` or extensionless file whose first card is JCL is a job, an extensionless file whose first
subcommand binds is a bind deck, and an extensionless file whose first line is the `/* REXX */`
comment TSO itself reads is an exec. Control card members are typed by what they say rather than by
what they are called: a `.ctl`, `.prm` or extensionless member is a sort deck, an IDCAMS deck, a
link-edit deck or an AMBLIST request deck if its first statement is one, and anything else stays
plain. An IMS gen library is often kept as `.asm` — Bank of Z writes its DBDs as
`src/base/ims/DBD/*.asm` and its PSBs as `src/base/ims/PSB/*.asm` — so an `.asm` is a gen member when
the first macro it invokes is one that gens, and the HLASM reader takes the rest. A
member whose name
promises a language its content is not — CBSA's `DFH$SIP1.jcl` is a CICS parameter member — is
reported as such, under
`WrongLanguageException`, rather than as a grammar failure. The corpus tests skip themselves when
the variables are unset, so a normal `./gradlew test` does not need them:

```bash
COBOL_CORPUS=/path/to/corpus JCL_CORPUS=/path/to/corpus BMS_CORPUS=/path/to/corpus \
  CONTROLM_CORPUS=/path/to/corpus DB2_CORPUS=/path/to/corpus IMS_CORPUS=/path/to/corpus \
  ASM_CORPUS=/path/to/corpus SAS_CORPUS=/path/to/corpus ./gradlew test
```

`IMS_CORPUS` reads 45 gen members — Bank of Z's 17, CardDemo's 8, DBB Samples' 1 and the fixture's 19
— for 19 databases of 20 segments and 72 fields with 9 names belonging to another database, 18 PSBs
of 43 PCBs and 49 sensitive segments, and 7 format sets of 8 device pages and 199 device fields
carrying 13 messages of 143 fields. The fixture's are the measurement and the rest are report-only:
`ImsCorpusTest` holds it to `INTERLINKS.md` sections 19.1 to 19.5 — six DBDs of eight segments and
thirty fields with seven references over five of them, six PSBs of ten PCBs, fourteen sensitive
segments and the seven `SENFLD`s that build a fifty byte I/O area out of a sixty five byte segment,
a stage 1 deck of six `APPLCTN`, two `TRANSACT` and five `DATABASE`, and six format sets of seven
device pages carrying four MIDs and seven MODs — and counts every `SEGM`, `FIELD`, `LCHILD`,
`PCB`, `SENSEG`, `SENFLD`, `DFLD` and `MFLD` of every member against an independent count of the
source, since a misgrouped continuation prints back byte for byte and says something else.

Which PCB a program's nth mask is depends on what runs under the PSB, and section 6.1's six cases are
tests of their own: a message driven program and a BMP are handed an I/O PCB no `PCB` statement
codes, so every database after it is one mask along, while a DL/I batch program under a PSB without
`CMPAT=YES` gets the database PCBs alone. `Psb.getPcbAtMask` is that rule, `Psb.getPcb` is the PCB an
AIB call names by `PCBNAME=`, and `Psb.getDatabasePcb` is the one an `EXEC DLI PCB(n)` numbers.

A format set is where MFS and COBOL meet. An `MFLD` names a `DFLD`, so a field of the message has a
place on the screen and a length, and the message is the layout of the area the program declares it
with — matched by order and length and never by name, after the four byte `LL`/`ZZ` prefix IMS
supplies and no `MFLD` describes. `Message.getLength` is what a copybook can be checked against and
`MessageField.getOffset` says where each field lands: section 19.5's totals of 31, 154, 312, 288,
392, 344 and 281 bytes are tests. The other direction is one line of COBOL — a program names a MOD by
passing it as the fourth argument of an `ISRT` against a message PCB, which `DliCall.getMod` resolves
from working storage. Nothing names a MID: the `NXT=` of the MOD says which format the reply arrives
on, and IMS applies it.

`ASM_CORPUS` reads 134 assembler members — GenevaERS Performance Engine's 116, CardDemo's 4, Z Open
Editor sample's 3, base64's 2, zAppBuild's 1 and the fixture's 8 — for 301 control sections of which
233 are dummy, 13,180 constants laid out in them, 221 `COPY` statements, 47 macro definitions and
6,744 macro invocations, 31 calls of which 2 are DL/I, 37 DCBs and 123 entry points. There is no
grammar: the columns are the syntax, so a hand-written statement reader over the lines does the whole
of it, and the 1.2 MB `GVBMR95.asm` reads in about 25 ms where an ANTLR lexer alone takes three times
that over the same text. The fixture's members are the measurement and the rest are report-only:
`AssemblerCorpusTest` holds it to `INTERLINKS.md` sections 20.2 to 20.6 — five `COPY` statements over
three copy members, fourteen invocations of the two macros the shop wrote (`CLMSAVE` three,
`CLMRTRN` eleven, which is also the count of each program's exits), the three calls written in
assembler of which two are `ASMTDLI`, the nineteen rows and three hundred bytes of `CLMRECD` at their
offsets against `cpy/CLMREC`, the fifty of `CLMPCBD` and the sixty five of `CLMROOTD`, the two DL/I
calls and the two DD names `CLMA010` opens — and counts every `CSECT`, `DSECT`, `COPY`, `MACRO`,
`CALL`, `DCB`, `ENTRY` and `EXTRN` of every member against an independent count of the source, since a
misgrouped continuation prints back byte for byte and says something else. It also requires that no
white space of a member holds anything but white space, which is the other way to print back perfectly
and say nothing: text nobody took into a node of its own comes back out of the space in front of the
next one. Both checks found a real defect on the corpus that the round trip did not.

A DSECT is the assembler's copybook and the only thing that can be checked against the other is the
layout, so `ControlSection.getFields` works the location counter forward through the constants —
duplication factor, type, length modifier, nominal value, and the boundary a type is aligned on — and
gives each one where it starts and how long it is. The section runs to the next one, to the `END`, or
to the `EQU *-name` that measures it, which is where a shop writes the length down. An operand this
does not read, a length given as an expression say, leaves the offsets after it null rather than
guessed at.

What tells a shop macro from IBM's is the library and not the program: the statement is the same
either way, so `CALL`, `DCB`, `OPEN`, `GET`, `PUT`, `CLOSE` and `WTO` are known to come out of
`SYS1.MACLIB` only because no member of the estate writes a prototype for them. The prototype is also
what tells a macro library member from a copy member — most of a `MACLIB` is DSECTs read by `COPY` —
and `FindRelationships` scans every assembler member before it writes an edge for the same kind of
reason: a COBOL `CALL 'CLMU030'` reaches an assembler subroutine, and nothing in the COBOL says so.
An operation that is neither an assembler directive nor a machine instruction is an invocation, so a
mnemonic missing from the table reports one invocation too many; reading the corpus rows rather than
the totals is what found the ones that were.

`SAS_CORPUS` reads 4 SAS members, all of them the fixture's: no public application in the corpus has
any SAS at all. There is no grammar because there is almost no syntax to have one for — a statement
runs to the first semicolon outside a quoted string or a comment, SAS has no reserved words, and
everything that is not a boundary stays a word, which is the depth a text search and a name reference
need. What does move a boundary is read: a `/* */` comment anywhere a blank may go, a statement
beginning `*` that runs to its own semicolon, and a quoted string, where a semicolon means nothing.
`SasCorpusTest` holds it to `INTERLINKS.md` sections 21.1 to 21.5 — four `%INCLUDE` statements over
one member, four `LIBNAME`s over the one libref `CLMSAS`, the libref `LIBRARY` that `PROC FORMAT`
writes its formats to and no statement of the member declares, two `INFILE`s whose DD names are the
ones the COBOL programs `SELECT` for the same two data sets, five invocations of the one macro the shop
wrote, the eleven and seven columns of the two `INPUT` layouts against the copybooks they were
written from, and the one DB2 name the subsystem reads — and counts the statements of every member
against the semicolons its source writes, since a boundary read in the wrong place prints back byte
for byte and says something else.

A SAS program has no name of its own the way a COBOL program has a `PROGRAM-ID`: the member name is
the only name it has. `jcl/CLMSTAT` runs three members of the SAS library and carries a fourth
program in-stream, and that fourth has no name at all, so `InstreamSas` reads it out of the job's
`SYSIN` and hangs its edges on the job — the same shape as the DDL most of an estate's tables are
actually created by. What the SAS reaches by DD name is left on the traits rather than resolved:
`SASSRC` is not a path, `CLMSAS` with no data set after it is whatever the step allocated, and a DD
name closes only against the job that ran the program. An `INPUT` layout is the other join, and it is
by position and never by name — a SAS variable holds eight characters and `AUD-OLD-RESERVE` is
fifteen — so `InputLayout.Field` gives the column each variable starts in and how many bytes its
informat reads.

`JCL_CORPUS` also reads 117 scripts and run book members — zorow's 45, Zowe install packaging's 22,
Z Open Editor sample's 4, GenApp's 2, ADCD setup's 2 and the fixture's 42 — every line of them kept
as it was written, since there is no grammar for any of the five kinds and what is to be had from
them is what they name. A verb is what makes a reference. Every script writes `SUBMIT &JOB (Y/N)?`
in a message somewhere, so a name in prose reaches nothing and only `SUBMIT`, `CALL`, `RUN PROGRAM`,
`EXEC`, `ALLOC`, `EDIT`
and `SELECT` are followed. A REXX command is built out of quoted strings and variables written side
by side, so the quotes come off and what was between them joins up — `"SUBMIT '"HLQ".JCL("JOB")'"`
is the one command `SUBMIT 'HLQ.JCL(JOB)'` — while what was written outside them is what the exec
computed. Nothing resolves a variable, and the fixture is why that is the honest answer rather than
a gap: not one of its four submits names a job, because the job a script really submits is a fact
about two members and a parameter.

`PlainTextCorpusTest` holds the fixture to `INTERLINKS.md` sections 17.1 to 17.3 and 18.1 to 18.2
row by row: forty three statements over eleven scripts, of which ten call another script, one
`CALL`s a load module and one `RUN`s a program under a plan; eleven allocations, four of them
carrying the DD names `CLMB010` and `CLMD020` assign; and all thirty one run book subjects — ten
jobs, fourteen programs and seven data sets. What a CLIST writes down beside its submits is held to
17.1 the same way: the `PROC` each of the eight declares, the ten calls they make of one another, and
the eleven job names a `SET` chooses. A run book is held to the labelled fields it writes — the fifty
one labels of the shop's own vocabulary — since a field is where the member says a name is a
component and a sentence is not. The names a member merely mentions are reported and not
resolved — 7,005 of them over the 117 members — and two of the fixture's matches are wrong on
purpose: an English word of eight letters or fewer is spelled exactly like a member name, so which
of them is a component of the estate is a join against the members a repository holds rather than
something one member says.

Bind decks, link-edit decks, module listings and control cards ride on `JCL_CORPUS` too, since a
deck is reached through the step that runs it and a listing is what that step printed. Each of the
eight variables and the files under it are declared as inputs of the `test` task, so a corpus that
grew, or a variable that was not set on the last run, invalidates the task rather than replaying its
old counts from the cache.

The numbers above were taken at these commits. CBSA's `main` is a README; the application is on the
`July2024Refresh` branch. DBB Samples declares an EBCDIC working-tree encoding in `.gitattributes`,
so check it out with `* -working-tree-encoding` in `.git/info/attributes`, or the files on disk are
not text the parser can read.

| Application | Checked out | License |
|---|---|---|
| CardDemo | `main` 59cc6c2 | Apache-2.0 |
| GenApp | `main` f6f3f4b | EPL-2.0 |
| Bank of Z | `main` 17a50f3 | Apache-2.0 |
| CBSA | `July2024Refresh` 4173345 | EPL-2.0 |
| COBOL Programming Course | `master` 61c573d | CC-BY-4.0, by the Open Mainframe Project's contributors |
| Cash Account | `main` c35db0d | Apache-2.0 |
| zAppBuild | `main` ddc51c7 | Apache-2.0 |
| DBB Samples | `master` 61a1a20 | Apache-2.0 |
| Z Open Editor sample | `main` c57a788 | Apache-2.0 |
| base64 | `main` 3960650 | EPL-2.0 |
| GenevaERS Workbench | `main` 907e21f | Apache-2.0 |
| GenevaERS Performance Engine | `main` 705a969 | Apache-2.0 |
| ADCD setup | `master` a931dba | MIT |
| Zowe install packaging | `v3.x/staging` 20977c3 | EPL-2.0 |
| MainframeJCL | `main` 598744a | MIT |
| zorow | `master` 9f1fdf0 | Apache-2.0 |
| CLAIMS fixture | `main` 15cb43b | Apache-2.0 |

`CorpusCoverageTest` reports how much of each application parses rather than requiring all of it to:
what the parser cannot yet read is the point of the measurement. It asserts that every program it
did parse prints back byte-identical to the input, which is the property recipes depend on, and
`JclCorpusTest` asserts the same of every job.

`BmsCorpusTest` asserts the same over the 50 map sets, and counts the macros it read against an
independent count of the source. A map set that groups its continuation lines wrongly still prints
back perfectly — it just says something else — so printing alone would not catch it. 47 of the
corpus's fields are initialised with a literal written over more than one card, and none of them
comes back with a quote nothing closes, which is what a field read only as far as its first blank
looks like.

`Db2CorpusTest` does the same for the schema, over both places DDL is written: 49 files of DDL and
the 23 `SYSIN` streams the corpus's jobs submit. It reads 22 tables, 192 columns, 31 indexes and
9 foreign keys, and counts each `CREATE TABLE`, `CREATE INDEX` and `PRIMARY KEY` it read against an
independent count of the source. The fixture is required to read where the rest is reported: its
`ddl/` members, its four catalog query decks and the index `CLMJ004` creates in-stream are checked
object by object against INTERLINKS sections 15 and 16, which is where the 2 tables, 18 columns,
2 primary keys, 1 foreign key and 5 indexes are written down. It also names the members it holds
out — a Postgres port of one schema, a file that sets its own statement terminator, and members
whose banner comment is damaged — and asserts how many there are, so that set cannot grow without
someone saying why.
`BindCorpusTest` does the same for bind cards, over the fixture's ten `CARDLIB` decks and the eleven
decks the corpus writes in-stream, counting every `BIND` and `REBIND` it read against a line scan of
the source. It runs under `JCL_CORPUS`: a bind deck is reached through the jobs that run it.

`LinkEditCorpusTest` does the same for link-edit decks, over 74 members and the ten decks the corpus
writes in-stream, counting every control statement it read against a card scan of the source. It also
checks the fixture's `linklib` deck by deck against INTERLINKS section 12, which is where the
19 modules, the five that enter at a `DLITCBL` label, the two `ALIAS` cards and every statically
included object are written down. GenevaERS keeps its decks as build templates, so the 14 with no
template directive in them are read as the decks they are and the one with a directive is left alone.

`ListLoadCorpusTest` does the same for load module listings, over the fixture's two AMBLIST reports,
thirteen binder listings and two request decks, counting every module and every control section it
read against a column scan of the source. It checks them against INTERLINKS section 14, which is
where the 16 modules, the 16 program CSECTs, the three map sets, the one module entered at a
`DLITCBL` label and the one alias are written down, and it requires each module's reported size to be
what its sections add up to and what the binder printed for a module to agree with what AMBLIST found
in the library afterwards.

`ControlCardCorpusTest` does the same for sort and IDCAMS cards, over 16 members and the 153 decks
the corpus writes in-stream, and requires that no member is claimed by both parsers. It also checks
the fixture's `ctlcard` library member by member against INTERLINKS section 8.3, which is where the
six IDCAMS members, the two sort members and the eleven that are neither are written down.

`SourcePositionsCorpusTest` reads every JCL position back out of the source, since a position landing
anywhere but on the word it names is worse than no position at all. Over the 547 members it reads
200,565 words back and compares each to the text at the offset reported for it, and requires every one
of the 100,877 statements to be placed: 78,903 in the member they were written in, and the 21,974 a
procedure or INCLUDE member wrote against the `EXEC` or `INCLUDE` card that brought them in.

## Contributing

We appreciate all types of contributions. See the [contributing guide](https://github.com/openrewrite/.github/blob/main/CONTRIBUTING.md) for detailed instructions on how to get started.

## Licensing

This is a Moderne proprietary module available only for use by Moderne customers under the terms of a commercial contract.

For more information about licensing, please contact [Moderne](https://www.moderne.io/).
