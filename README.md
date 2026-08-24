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

This project implements a [Rewrite module](https://github.com/openrewrite/rewrite) that provides parsers, visitors, and recipes for COBOL and related mainframe technologies. It supports parsing and transforming COBOL source code, JCL (Job Control Language), BMS map sets, and Control-M job definitions.

### Language Support

- **COBOL** — Full parsing of COBOL-85 (IBM ANSI 85 and HP Tandem dialects), including preprocessor directives (COPY, REPLACE) and copybook resolution; programs by `.cbl`, `.cob` and `.cobol`, copybooks by `.cpy`, `.copy` and `.dcl`
- **JCL** — Job Control Language parsing (`.jcl`, `.prc` and `.proc` files, and a `.txt` or extensionless PDS member whose first card is JCL)
- **BMS** — CICS Basic Mapping Support map sets (`.bms`): the `DFHMSD`/`DFHMDI`/`DFHMDF` macros, and the symbolic map names they generate, which is what joins a screen field to the COBOL data item a program reads it from
- **Control-M** — Control-M job scheduling definition parsing

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

Programs and Jobs say how many files of each kind the parser read, and out of how many: 197 of 210
COBOL programs and 465 of 505 JCL members, plus all 46 BMS map sets in the six applications that
have them. The technology columns say how many programs use each. A program counts as read when it
parsed and printed back byte for byte; a job when it also had exactly the EXEC cards the traits
found and nothing in it was left unplaced. The gaps are the measurement: `CorpusCoverageTest` groups
the COBOL failures by cause and `JclCorpusTest` names each member it could not read and why.

Bank of Z descends from CBSA, so the two are not independent measurements. 29 of CBSA's 31 program
names, 36 of its 37 copybooks and all 10 of its map sets are in Bank of Z too; of the shared
programs two are byte-identical, 19 differ by a few lines, and eight (BANKDATA, BNK1DCS, CREACC,
CRECUST, DELCUS, INQCUST, UPDCUST, XFRFUN) were rewritten. Bank of Z adds the 14 IMS programs; CBSA
adds the 110 jobs, among them DB2 DDL and BIND in JCL, where Bank of Z has one.

Clone them side by side into one directory and point the tests at it. The tests find files the way
the parsers accept them, whatever the case of the extension: programs by `.cbl`, `.cob` and
`.cobol`, copybooks by `.cpy`, `.copy` and `.dcl`, map sets by `.bms`, and jobs by `.jcl`, `.prc`
and `.proc` — or, since MainframeJCL, ADCD setup and Zowe's SZWESAMP keep their members as they came
off the PDS, by a `.txt` or extensionless file whose first card is JCL. A member whose name promises
a language its content is not — CBSA's `DFH$SIP1.jcl` is a CICS parameter member — is reported as
such, under `WrongLanguageException`, rather than as a grammar failure. The corpus tests skip
themselves when the variables are unset, so a normal `./gradlew test` does not need them:

```bash
COBOL_CORPUS=/path/to/corpus JCL_CORPUS=/path/to/corpus BMS_CORPUS=/path/to/corpus ./gradlew test
```

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

`CorpusCoverageTest` reports how much of each application parses rather than requiring all of it to:
what the parser cannot yet read is the point of the measurement. It asserts that every program it
did parse prints back byte-identical to the input, which is the property recipes depend on, and
`JclCorpusTest` asserts the same of every job.

`BmsCorpusTest` asserts the same over the 46 map sets, and counts the macros it read against an
independent count of the source. A map set that groups its continuation lines wrongly still prints
back perfectly — it just says something else — so printing alone would not catch it.

## Contributing

We appreciate all types of contributions. See the [contributing guide](https://github.com/openrewrite/.github/blob/main/CONTRIBUTING.md) for detailed instructions on how to get started.

## Licensing

This is a Moderne proprietary module available only for use by Moderne customers under the terms of a commercial contract.

For more information about licensing, please contact [Moderne](https://www.moderne.io/).
