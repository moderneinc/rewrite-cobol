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
  <h1>rewrite-cobol</h1>
</div>

<div align="center">

<!-- Keep the gap above this line, otherwise they won't render correctly! -->
[![ci](https://github.com/moderneinc/rewrite-cobol/actions/workflows/ci.yml/badge.svg)](https://github.com/moderneinc/rewrite-cobol/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/org.openrewrite/rewrite-cobol.svg)](https://mvnrepository.com/artifact/org.openrewrite/rewrite-cobol)
</div>

## What is this?

This project implements a [Rewrite module](https://github.com/openrewrite/rewrite) that provides parsers, visitors, and recipes for COBOL and related mainframe technologies. It supports parsing and transforming COBOL source code, JCL (Job Control Language), BMS map sets, DB2 DDL, DB2 bind cards, link-edit decks, load module listings, DFSORT and IDCAMS control cards, and Control-M job definitions.

### Language Support

- **COBOL** — Full parsing of COBOL-85 (IBM ANSI 85 and HP Tandem dialects), including preprocessor directives (COPY, REPLACE) and copybook resolution; programs by `.cbl`, `.cob` and `.cobol`, copybooks by `.cpy`, `.copy` and `.dcl`
- **JCL** — Job Control Language parsing (`.jcl`, `.prc` and `.proc` files, a `.txt` or extensionless PDS member whose first card is JCL, and the `.j2` Jinja templates an installation ships its jobs as), and the resolution a job's cards leave to other members: the procedures and INCLUDE members a step runs through, the DD overrides merged in, and the symbols a data set name is written with, which is what a step's real program and real data sets are read from
- **BMS** — CICS Basic Mapping Support map sets (`.bms`): the `DFHMSD`/`DFHMDI`/`DFHMDF` macros, and the symbolic map names they generate, which is what joins a screen field to the COBOL data item a program reads it from
- **DB2 DDL** — DB2 for z/OS DDL (`.ddl`, `.sql`, and the `SYSIN` streams of the jobs that create a schema): every statement the SQL reference documents is modelled, so a statement that cannot be read is a syntax error rather than a node that says nothing
- **Bind cards** — DSN command decks (`.bnd`, and an extensionless CARDLIB member whose first subcommand binds): `BIND PLAN`, `BIND PACKAGE` and `REBIND` with their keyword operands, read from a member of their own or from the in-stream `SYSTSIN` of the job that runs them
- **Link-edit decks** — Binder control statements (`.lnk`, `.lked`, and an extensionless LINKLIB member whose first statement links): `INCLUDE ddname(member)`, `ENTRY`, `ALIAS`, `NAME xxx(R)`, `ORDER`, `MODE`, `SETCODE` and `SETOPT`, with column 72 saying which card continues which. A deck is the only place a load module's composition is written down — a step names the module, and the module names its programs here
- **Load module listings** — What AMBLIST and the binder printed about a load library (`.amblist`, `.binder`, `.listload`, and a control card member that asks for a report): the module and its entry point, aliases and size, the control sections it holds with their offsets and lengths, the alternate entry points within them, and the compiler each section came from. A deck says what a module was meant to hold; a listing says what it holds, including the language interface and the runtime nobody wrote a card for
- **Sort cards** — DFSORT and ICETOOL control statements: `SORT`/`MERGE FIELDS`, `INCLUDE`/`OMIT COND`, `INREC`/`OUTREC`/`OUTFIL`, `SUM`, `OPTION`, with the control fields read as byte positions into the record, which is what joins a sort card to the copybook that describes it
- **IDCAMS cards** — Access method services commands: `DEFINE CLUSTER`/`AIX`/`PATH`/`GDG` with their parameter groups, `REPRO`, `DELETE`, `LISTCAT`, `PRINT` and `ALTER`, which is where a VSAM file's key, record size and components are written down
- **Control-M** — Job scheduling definitions, both dialects a shop has: the z/OS panel (`.ctms`) and the XML an export writes (`.controlm`). The JCL member each job runs, the `IN` and `OUT` conditions that order them, the SMART table they sit in and the calendars they run on, which is what turns a library of jobs into the order they actually run in

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
| Zowe install packaging | [zowe/zowe-install-packaging](https://github.com/zowe/zowe-install-packaging) | — | 48 of 51 | — | — | — | — |
| MainframeJCL | [billrain/MainframeJCL](https://github.com/billrain/MainframeJCL) | — | 83 of 108 | — | — | — | — |
| zorow | [openmainframeproject/zorow](https://github.com/openmainframeproject/zorow) | — | 47 of 56 | — | — | — | — |
| CLAIMS fixture | [moderneinc/mainframe-fixtures](https://github.com/moderneinc/mainframe-fixtures) | 19 of 19 | 40 of 40 | 5 | 4 | 4 | 6 |

Programs and Jobs say how many files of each kind the parser read, and out of how many: 216 of 229
COBOL programs and 505 of 545 JCL members, plus all 49 BMS map sets in the seven applications that
have them. The technology columns say how many programs use each. A program counts as read when it
parsed and printed back byte for byte; a job when it also had exactly the EXEC cards the traits
found and nothing in it was left unplaced. The gaps are the measurement: `CorpusCoverageTest` groups
the COBOL failures by cause and `JclCorpusTest` names each member it could not read and why.

Bank of Z descends from CBSA, so the two are not independent measurements. 29 of CBSA's 31 program
names, 36 of its 37 copybooks and all 10 of its map sets are in Bank of Z too; of the shared
programs two are byte-identical, 19 differ by a few lines, and eight (BANKDATA, BNK1DCS, CREACC,
CRECUST, DELCUS, INQCUST, UPDCUST, XFRFUN) were rewritten. Bank of Z adds the 14 IMS programs; CBSA
adds the 110 jobs, among them DB2 DDL and BIND in JCL, where Bank of Z has one.

The last row is not a public application but a fixture: one fictional insurance claims
application, CLAIMS, written so that every `COPY`, `CALL`, `EXEC PROC`, `SEND MAP` and `SYSIN`
member in it resolves to a member of the same repository, and every member of it a parser here reads
parses. It also holds member kinds nothing here reads yet — IMS DBD, PSB, MFS and stage 1 decks,
HLASM programs and macros, CLISTs, REXX execs and run book members — and the
walks skip those rather than counting them against a parser. The public applications are measured;
the fixture is required. The tests know it by its directory name, `mainframe-fixtures`, and fail when
a program, copybook, job, procedure, map set, bind deck, link-edit deck, module listing, control card
or schedule of it does not parse, read or print back — or when the corpus root does not contain it, since a fixture
the walk cannot see, a symbolic link say, would otherwise pass as an empty application.

Clone them side by side into one directory and point the tests at it. The tests find files the way
the parsers accept them, whatever the case of the extension: programs by `.cbl`, `.cob` and
`.cobol`, copybooks by `.cpy`, `.copy` and `.dcl`, map sets by `.bms`, bind decks by `.bnd`, link-edit
decks by `.lnk` and `.lked`, module listings by `.amblist`, `.binder` and `.listload`, schedules by
`.ctms` and `.controlm`, and jobs by `.jcl`, `.prc` and
`.proc` — or, since MainframeJCL, ADCD setup and Zowe's SZWESAMP keep their members as they came off
the PDS, by a `.txt` or extensionless file whose first card is JCL, and by an extensionless file
whose first subcommand binds. Control card members are typed by what they say rather than by what
they are called: a `.ctl`, `.prm` or extensionless member is a sort deck, an IDCAMS deck, a
link-edit deck or an AMBLIST request deck if its first statement is one, and anything else stays
plain. A member whose name
promises a language its content is not — CBSA's `DFH$SIP1.jcl` is a CICS parameter member — is
reported as such, under
`WrongLanguageException`, rather than as a grammar failure. The corpus tests skip themselves when
the variables are unset, so a normal `./gradlew test` does not need them:

```bash
COBOL_CORPUS=/path/to/corpus JCL_CORPUS=/path/to/corpus BMS_CORPUS=/path/to/corpus \
  DB2_CORPUS=/path/to/corpus ./gradlew test
CONTROLM_CORPUS=/path/to/corpus ./gradlew test --rerun
```

Bind decks, link-edit decks, module listings and control cards ride on `JCL_CORPUS`, since a deck is
reached through the step that runs it and a listing is what that step printed. `--rerun` matters: the corpus path is an environment variable, not a task input, so a
run left up to date by an earlier `./gradlew test` is replayed without reading the corpus at all.

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
| CLAIMS fixture | `main` c23c837 | Apache-2.0 |

`CorpusCoverageTest` reports how much of each application parses rather than requiring all of it to:
what the parser cannot yet read is the point of the measurement. It asserts that every program it
did parse prints back byte-identical to the input, which is the property recipes depend on, and
`JclCorpusTest` asserts the same of every job.

`BmsCorpusTest` asserts the same over the 49 map sets, and counts the macros it read against an
independent count of the source. A map set that groups its continuation lines wrongly still prints
back perfectly — it just says something else — so printing alone would not catch it.

`Db2CorpusTest` does the same for the schema, over both places DDL is written: 97 files of DDL and
the 23 `SYSIN` streams the corpus's jobs submit. It reads 22 tables, 192 columns, 31 indexes and
9 foreign keys, and counts each `CREATE TABLE`, `CREATE INDEX` and `PRIMARY KEY` it read against an
independent count of the source. It also names the members it holds out — a Postgres port of one
schema, a file that sets its own statement terminator, and members whose banner comment is damaged —
and asserts how many there are, so that set cannot grow without someone saying why.
`BindCorpusTest` does the same for bind cards, over the fixture's ten `CARDLIB` decks and the eleven
decks the corpus writes in-stream, counting every `BIND` and `REBIND` it read against a line scan of
the source. It runs under `JCL_CORPUS`: a bind deck is reached through the jobs that run it.

`LinkEditCorpusTest` does the same for link-edit decks, over 74 members and the ten decks the corpus
writes in-stream, counting every control statement it read against a card scan of the source. It also
checks the fixture's `linklib` deck by deck against INTERLINKS section 12, which is where the
19 modules, the five that enter at a `DLITCBL` label, the one `ALIAS` and every statically included
object are written down. GenevaERS keeps its decks as build templates, so the 14 with no template
directive in them are read as the decks they are and the one with a directive is left alone.

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
anywhere but on the word it names is worse than no position at all. Over the 545 members it reads
199,046 words back and compares each to the text at the offset reported for it, and requires every one
of the 100,000 statements to be placed: 78,621 in the member they were written in, and the 21,379 a
procedure or INCLUDE member wrote against the `EXEC` or `INCLUDE` card that brought them in.

## Contributing

We appreciate all types of contributions. See the [contributing guide](https://github.com/openrewrite/.github/blob/main/CONTRIBUTING.md) for detailed instructions on how to get started.

## Licensing

This is a Moderne proprietary module available only for use by Moderne customers under the terms of a commercial contract.

For more information about licensing, please contact [Moderne](https://www.moderne.io/).
